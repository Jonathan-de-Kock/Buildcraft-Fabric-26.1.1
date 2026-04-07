/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.pipe.flow;

import javax.annotation.Nullable;

import com.mojang.serialization.DataResult;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeFlow;

/**
 * Simplified 7-section fluid flow for pipes. Each section stores a fluid amount
 * and a direction cooldown. The tick algorithm has three phases:
 * <ol>
 *   <li>moveFromPipe: push fluid from face sections into adjacent blocks/pipes</li>
 *   <li>moveFromCenter: distribute center fluid to connected face sections</li>
 *   <li>moveToCenter: pull fluid from face sections into the center</li>
 * </ol>
 *
 * Uses Fabric Transfer API for interaction with adjacent fluid containers.
 */
public class PipeFlowFluids extends PipeFlow {
    /** How many ticks a section remembers its last flow direction. */
    private static final int DIRECTION_COOLDOWN = 60;
    /** Droplets per bucket in the Fabric Transfer API. */
    private static final long DROPLETS_PER_BUCKET = 81_000;

    private final Section[] sections = new Section[7]; // 0-5 = Direction.ordinal(), 6 = center
    private FluidVariant currentFluid = FluidVariant.blank();
    private long transferPerTick;
    private long capacity;

    public PipeFlowFluids(IPipe pipe) {
        super(pipe);
        initFromConfig();
        initSections();
    }

    public PipeFlowFluids(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        initFromConfig();
        initSections();
        loadFromNbt(nbt);
    }

    private void initFromConfig() {
        PipeApi.FluidTransferInfo info = PipeApi.getFluidTransferInfo(pipe.getDefinition());
        // transferPerTick is in buckets in the config; convert to droplets
        this.transferPerTick = info.transferPerTick * DROPLETS_PER_BUCKET;
        this.capacity = transferPerTick * 10;
    }

    private void initSections() {
        for (int i = 0; i < 7; i++) {
            sections[i] = new Section();
        }
    }

    private void loadFromNbt(CompoundTag nbt) {
        if (nbt.contains("fluid")) {
            currentFluid = decodeFluidVariant(nbt.getCompoundOrEmpty("fluid"));
        }
        for (int i = 0; i < 7; i++) {
            String key = "section_" + i;
            if (nbt.contains(key)) {
                CompoundTag sTag = nbt.getCompoundOrEmpty(key);
                sections[i].amount = sTag.getLongOr("amount", 0);
                sections[i].ticksInDirection = sTag.getIntOr("ticksInDir", 0);
            }
        }
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        if (!currentFluid.isBlank()) {
            CompoundTag fluidTag = encodeFluidVariant(currentFluid);
            if (!fluidTag.isEmpty()) {
                nbt.put("fluid", fluidTag);
            }
        }
        for (int i = 0; i < 7; i++) {
            if (sections[i].amount > 0 || sections[i].ticksInDirection > 0) {
                CompoundTag sTag = new CompoundTag();
                sTag.putLong("amount", sections[i].amount);
                sTag.putInt("ticksInDir", sections[i].ticksInDirection);
                nbt.put("section_" + i, sTag);
            }
        }
        return nbt;
    }

    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, boolean isClient) {
        super.writePayload(id, buffer, isClient);
        if (id == NET_ID_FULL_STATE) {
            CompoundTag fluidTag = encodeFluidVariant(currentFluid);
            buffer.writeNbt(fluidTag);
            for (int i = 0; i < 7; i++) {
                buffer.writeLong(sections[i].amount);
            }
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, boolean isClient) {
        super.readPayload(id, buffer, isClient);
        if (id == NET_ID_FULL_STATE) {
            CompoundTag fluidTag = buffer.readNbt();
            if (fluidTag != null && !fluidTag.isEmpty()) {
                DataResult<FluidVariant> result = FluidVariant.CODEC.parse(NbtOps.INSTANCE, fluidTag);
                currentFluid = result.result().orElse(FluidVariant.blank());
            } else {
                currentFluid = FluidVariant.blank();
            }
            for (int i = 0; i < 7; i++) {
                sections[i].amount = buffer.readLong();
            }
        }
    }

    @Override
    public boolean canConnect(Direction face, PipeFlow other) {
        return other instanceof PipeFlowFluids;
    }

    @Override
    public boolean canConnect(Direction face, BlockEntity oTile) {
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null) return false;
        BlockPos pos = pipe.getHolder().getPipePos().relative(face);
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, face.getOpposite());
        return storage != null;
    }

    @Override
    public void onTick() {
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null || level.isClientSide()) return;

        // Decrement cooldowns
        for (int i = 0; i < 7; i++) {
            if (sections[i].ticksInDirection > 0) {
                sections[i].ticksInDirection--;
            }
        }

        // Check if all fluid has been consumed
        boolean hasFluid = false;
        for (int i = 0; i < 7; i++) {
            if (sections[i].amount > 0) {
                hasFluid = true;
                break;
            }
        }
        if (!hasFluid && !currentFluid.isBlank()) {
            currentFluid = FluidVariant.blank();
        }

        // Phase 1: Move from face sections into the world (adjacent blocks/pipes)
        moveFromPipe();

        // Phase 2: Move from center to face sections
        moveFromCenter();

        // Phase 3: Move from face sections into center (incoming fluid)
        moveToCenter();
    }

    // ===================
    // Phase 1: Face -> World
    // ===================
    private void moveFromPipe() {
        for (Direction dir : Direction.values()) {
            Section face = sections[dir.ordinal()];
            if (face.amount <= 0) continue;
            if (!pipe.isConnected(dir)) continue;

            long moved = 0;

            // Try adjacent pipe first
            IPipe neighbour = pipe.getConnectedPipe(dir);
            if (neighbour != null && neighbour.getFlow() instanceof PipeFlowFluids neighbourFlow) {
                moved = neighbourFlow.acceptFluid(currentFluid, face.amount, dir.getOpposite());
            } else {
                // Try adjacent block
                moved = insertIntoBlock(dir, face.amount);
            }

            if (moved > 0) {
                face.amount -= moved;
                face.ticksInDirection = DIRECTION_COOLDOWN;
            }
        }
    }

    // ===================
    // Phase 2: Center -> Faces
    // ===================
    private void moveFromCenter() {
        Section center = sections[6];
        if (center.amount <= 0) return;

        long available = Math.min(center.amount, transferPerTick);
        if (available <= 0) return;

        // Collect candidate faces with their weights (inverse of current fill)
        long totalWeight = 0;
        long[] weights = new long[6];
        int candidateCount = 0;

        for (Direction dir : Direction.values()) {
            if (!pipe.isConnected(dir)) continue;
            Section face = sections[dir.ordinal()];
            long space = capacity - face.amount;
            if (space <= 0) continue;

            // Prefer directions the fluid hasn't recently flowed FROM
            weights[dir.ordinal()] = space;
            totalWeight += space;
            candidateCount++;
        }

        if (candidateCount == 0 || totalWeight == 0) return;

        long totalMoved = 0;
        for (Direction dir : Direction.values()) {
            if (weights[dir.ordinal()] <= 0) continue;

            long share = (available * weights[dir.ordinal()]) / totalWeight;
            if (share <= 0) continue;

            Section face = sections[dir.ordinal()];
            long space = capacity - face.amount;
            long toMove = Math.min(share, space);
            toMove = Math.min(toMove, center.amount - totalMoved);
            if (toMove <= 0) continue;

            face.amount += toMove;
            totalMoved += toMove;
        }

        center.amount -= totalMoved;
    }

    // ===================
    // Phase 3: Faces -> Center (incoming)
    // ===================
    private void moveToCenter() {
        Section center = sections[6];
        long spaceInCenter = capacity - center.amount;
        if (spaceInCenter <= 0) return;

        for (Direction dir : Direction.values()) {
            Section face = sections[dir.ordinal()];
            // Only move fluid that is flowing inward (cooldown expired or no outward flow)
            if (face.amount <= 0) continue;
            if (face.ticksInDirection > 0) continue; // Still flowing outward

            long toMove = Math.min(face.amount, Math.min(transferPerTick, spaceInCenter));
            if (toMove <= 0) continue;

            center.amount += toMove;
            face.amount -= toMove;
            spaceInCenter -= toMove;
        }
    }

    // ===================
    // Fluid interaction helpers
    // ===================

    /**
     * Accept fluid from an adjacent pipe into a face section.
     * @return the amount actually accepted in droplets.
     */
    public long acceptFluid(FluidVariant fluid, long maxAmount, Direction fromDir) {
        if (fluid.isBlank() || maxAmount <= 0) return 0;

        // Only accept matching fluid, or if we're empty
        if (!currentFluid.isBlank() && !currentFluid.equals(fluid)) return 0;

        Section face = sections[fromDir.ordinal()];
        long space = capacity - face.amount;
        long accepted = Math.min(maxAmount, space);
        if (accepted <= 0) return 0;

        if (currentFluid.isBlank()) {
            currentFluid = fluid;
        }
        face.amount += accepted;
        return accepted;
    }

    /**
     * Extract fluid from an adjacent block into a face section. Used by wood pipe behaviour.
     */
    public void extractFluid(Direction dir, long maxAmount) {
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null) return;
        BlockPos neighbourPos = pipe.getHolder().getPipePos().relative(dir);
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, neighbourPos, dir.getOpposite());
        if (storage == null) return;

        Section face = sections[dir.ordinal()];
        long space = capacity - face.amount;
        long toExtract = Math.min(maxAmount, Math.min(transferPerTick, space));
        if (toExtract <= 0) return;

        try (Transaction transaction = Transaction.openOuter()) {
            if (!currentFluid.isBlank()) {
                // Extract matching fluid
                long extracted = storage.extract(currentFluid, toExtract, transaction);
                if (extracted > 0) {
                    face.amount += extracted;
                    transaction.commit();
                }
            } else {
                // Find any extractable fluid
                FluidVariant found = StorageUtil.findExtractableResource(storage, transaction);
                if (found != null && !found.isBlank()) {
                    long extracted = storage.extract(found, toExtract, transaction);
                    if (extracted > 0) {
                        currentFluid = found;
                        face.amount += extracted;
                        transaction.commit();
                    }
                }
            }
        }
    }

    private long insertIntoBlock(Direction dir, long maxAmount) {
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null || currentFluid.isBlank()) return 0;
        BlockPos neighbourPos = pipe.getHolder().getPipePos().relative(dir);
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, neighbourPos, dir.getOpposite());
        if (storage == null) return 0;

        long toInsert = Math.min(maxAmount, transferPerTick);
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(currentFluid, toInsert, transaction);
            if (inserted > 0) {
                transaction.commit();
                return inserted;
            }
        }
        return 0;
    }

    /** @return the fluid currently in this pipe, or blank if empty. */
    public FluidVariant getCurrentFluid() {
        return currentFluid;
    }

    /** @return the total amount of fluid across all sections, in droplets. */
    public long getTotalAmount() {
        long total = 0;
        for (Section s : sections) {
            total += s.amount;
        }
        return total;
    }

    /** @return the amount in a specific section (0-5 = faces, 6 = center). */
    public long getSectionAmount(int index) {
        if (index < 0 || index >= 7) return 0;
        return sections[index].amount;
    }

    // ===================
    // Codec helpers
    // ===================

    private static CompoundTag encodeFluidVariant(FluidVariant variant) {
        if (variant.isBlank()) return new CompoundTag();
        DataResult<Tag> result = FluidVariant.CODEC.encodeStart(NbtOps.INSTANCE, variant);
        return result.result()
            .filter(t -> t instanceof CompoundTag)
            .map(t -> (CompoundTag) t)
            .orElse(new CompoundTag());
    }

    private static FluidVariant decodeFluidVariant(@Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return FluidVariant.blank();
        DataResult<FluidVariant> result = FluidVariant.CODEC.parse(NbtOps.INSTANCE, tag);
        return result.result().orElse(FluidVariant.blank());
    }

    // ===================
    // Inner class
    // ===================

    private static class Section {
        long amount;
        int ticksInDirection;
    }
}
