/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.pipe.flow;

import javax.annotation.Nullable;

import com.mojang.serialization.DataResult;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/**
 * Represents an item travelling through a pipe segment. Tracks where it entered,
 * where it is heading, and how many ticks remain.
 */
public class TravellingItem {
    public ItemStack stack;
    /** The direction this item entered from (null if injected without a direction). */
    @Nullable
    public Direction from;
    /** The direction this item is heading towards. */
    @Nullable
    public Direction to;
    /** How many ticks this item has been in the current pipe. */
    public int ticksInPipe;
    /** Total ticks to traverse this pipe segment. */
    public int totalTicks;
    /** Optional colour for sorting. */
    @Nullable
    public DyeColor colour;

    public TravellingItem(ItemStack stack, @Nullable Direction from, @Nullable Direction to, int totalTicks) {
        this.stack = stack;
        this.from = from;
        this.to = to;
        this.ticksInPipe = 0;
        this.totalTicks = totalTicks;
    }

    public TravellingItem(CompoundTag nbt) {
        // Decode ItemStack via CODEC
        CompoundTag stackTag = nbt.getCompoundOrEmpty("stack");
        DataResult<ItemStack> result = ItemStack.OPTIONAL_CODEC.parse(NbtOps.INSTANCE, stackTag);
        this.stack = result.result().orElse(ItemStack.EMPTY);
        if (nbt.contains("from")) {
            this.from = Direction.byName(nbt.getStringOr("from", ""));
        }
        if (nbt.contains("to")) {
            this.to = Direction.byName(nbt.getStringOr("to", ""));
        }
        this.ticksInPipe = nbt.getIntOr("ticksInPipe", 0);
        this.totalTicks = nbt.getIntOr("totalTicks", 10);
    }

    public CompoundTag writeToNbt() {
        CompoundTag nbt = new CompoundTag();
        if (!stack.isEmpty()) {
            // Encode ItemStack via CODEC
            DataResult<Tag> result = ItemStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, stack);
            result.result().ifPresent(tag -> {
                if (tag instanceof CompoundTag ct) {
                    nbt.put("stack", ct);
                }
            });
        }
        if (from != null) {
            nbt.putString("from", from.getName());
        }
        if (to != null) {
            nbt.putString("to", to.getName());
        }
        nbt.putInt("ticksInPipe", ticksInPipe);
        nbt.putInt("totalTicks", totalTicks);
        return nbt;
    }

    /** @return true if the item has reached its destination in this pipe. */
    public boolean isFinished() {
        return ticksInPipe >= totalTicks;
    }
}
