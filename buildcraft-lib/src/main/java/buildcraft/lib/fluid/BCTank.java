/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.fluid;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A single fluid tank for BuildCraft machines.
 * Stores fluid as FluidVariant + amount (in droplets, 1 bucket = 81000).
 * Implements Fabric's SingleSlotStorage for Transfer API compatibility.
 */
public class BCTank implements SingleSlotStorage<FluidVariant> {
    /** Droplets per bucket (Fabric Transfer API standard). */
    public static final long BUCKET = 81000;

    private final String name;
    private final long capacity;
    private FluidVariant fluid = FluidVariant.blank();
    private long amount = 0;
    @Nullable
    private Predicate<FluidVariant> filter;

    public BCTank(String name, long capacityBuckets) {
        this.name = name;
        this.capacity = capacityBuckets * BUCKET;
    }

    public BCTank(String name, long capacityBuckets, @Nullable Predicate<FluidVariant> filter) {
        this(name, capacityBuckets);
        this.filter = filter;
    }

    public String getName() {
        return name;
    }

    public FluidVariant getFluid() {
        return fluid;
    }

    public boolean isEmpty() {
        return amount <= 0 || fluid.isBlank();
    }

    // ##################
    //
    // Transfer API
    //
    // ##################

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) return 0;
        if (filter != null && !filter.test(resource)) return 0;

        if (fluid.isBlank()) {
            long inserted = Math.min(maxAmount, capacity);
            transaction.addOuterCloseCallback(result -> {
                if (result.wasCommitted()) {
                    fluid = resource;
                    amount = inserted;
                }
            });
            return inserted;
        } else if (fluid.equals(resource)) {
            long inserted = Math.min(maxAmount, capacity - amount);
            if (inserted <= 0) return 0;
            transaction.addOuterCloseCallback(result -> {
                if (result.wasCommitted()) {
                    amount += inserted;
                }
            });
            return inserted;
        }
        return 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) return 0;
        if (!fluid.equals(resource)) return 0;

        long extracted = Math.min(maxAmount, amount);
        if (extracted <= 0) return 0;
        transaction.addOuterCloseCallback(result -> {
            if (result.wasCommitted()) {
                amount -= extracted;
                if (amount <= 0) {
                    fluid = FluidVariant.blank();
                    amount = 0;
                }
            }
        });
        return extracted;
    }

    @Override
    public boolean isResourceBlank() {
        return fluid.isBlank();
    }

    @Override
    public FluidVariant getResource() {
        return fluid;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    // ##################
    //
    // NBT serialization (CompoundTag-based, for network payloads and internal use)
    //
    // ##################

    /** Write tank contents to a CompoundTag. Returns an empty tag if the tank is empty. */
    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();
        if (!isEmpty()) {
            nbt.store("fluid", FluidVariant.CODEC, fluid);
            nbt.putLong("amount", amount);
        }
        return nbt;
    }

    /** Read tank contents from a CompoundTag. */
    public void readFromNBT(CompoundTag nbt) {
        fluid = nbt.read("fluid", FluidVariant.CODEC).orElse(FluidVariant.blank());
        amount = nbt.getLongOr("amount", 0);
        if (fluid.isBlank()) {
            amount = 0;
        }
    }

    // ##################
    //
    // ValueInput/ValueOutput serialization (for block entity persistence)
    //
    // ##################

    /** Save tank contents via ValueOutput (MC 26.1 persistence API). */
    public void save(ValueOutput output) {
        if (!isEmpty()) {
            output.store("fluid", FluidVariant.CODEC, fluid);
            output.putLong("amount", amount);
        }
    }

    /** Load tank contents from ValueInput (MC 26.1 persistence API). */
    public void load(ValueInput input) {
        fluid = input.read("fluid", FluidVariant.CODEC).orElse(FluidVariant.blank());
        amount = input.getLongOr("amount", 0);
        if (fluid.isBlank()) {
            amount = 0;
        }
    }

    // ##################
    //
    // Network serialization (FriendlyByteBuf-based)
    //
    // ##################

    /** Write tank contents to a network buffer. */
    public void writeToBuffer(FriendlyByteBuf buf) {
        if (isEmpty()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            // Serialize FluidVariant via its Codec into NBT, then write to buffer
            Tag fluidTag = FluidVariant.CODEC.encodeStart(NbtOps.INSTANCE, fluid)
                .getOrThrow(msg -> new RuntimeException("Failed to encode FluidVariant: " + msg));
            buf.writeNbt(fluidTag);
            buf.writeVarLong(amount);
        }
    }

    /** Read tank contents from a network buffer. */
    public void readFromBuffer(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            CompoundTag fluidNbt = buf.readNbt();
            if (fluidNbt != null) {
                fluid = FluidVariant.CODEC.parse(NbtOps.INSTANCE, fluidNbt)
                    .result().orElse(FluidVariant.blank());
            } else {
                fluid = FluidVariant.blank();
            }
            amount = buf.readVarLong();
            if (fluid.isBlank()) {
                amount = 0;
            }
        } else {
            fluid = FluidVariant.blank();
            amount = 0;
        }
    }

    // ##################
    //
    // Direct setters (for network sync, not for regular gameplay)
    //
    // ##################

    /** Set fluid directly. For use in network sync — not for regular gameplay (use Transfer API instead). */
    public void setFluid(FluidVariant fluid, long amount) {
        this.fluid = fluid;
        this.amount = amount;
    }
}
