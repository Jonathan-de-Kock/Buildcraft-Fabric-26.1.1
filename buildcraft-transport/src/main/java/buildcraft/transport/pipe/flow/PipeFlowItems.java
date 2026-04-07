/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.pipe.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.DataResult;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.transport.tile.BlockEntityPipeHolder;

/**
 * Simplified item flow for pipes. Items enter from one direction, travel to the center,
 * then are routed to an output direction and continue to the next pipe or inventory.
 *
 * Default traversal time is 10 ticks per pipe segment.
 */
public class PipeFlowItems extends PipeFlow implements IInjectable {
    public static final int DEFAULT_TRAVEL_TICKS = 10;

    private final List<TravellingItem> items = new ArrayList<>();

    public PipeFlowItems(IPipe pipe) {
        super(pipe);
    }

    public PipeFlowItems(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        if (nbt.contains("items")) {
            ListTag list = nbt.getListOrEmpty("items");
            for (int i = 0; i < list.size(); i++) {
                CompoundTag itemTag = list.getCompoundOrEmpty(i);
                TravellingItem item = new TravellingItem(itemTag);
                if (!item.stack.isEmpty()) {
                    items.add(item);
                }
            }
        }
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        if (!items.isEmpty()) {
            ListTag list = new ListTag();
            for (TravellingItem item : items) {
                list.add(item.writeToNbt());
            }
            nbt.put("items", list);
        }
        return nbt;
    }

    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, boolean isClient) {
        super.writePayload(id, buffer, isClient);
        if (id == NET_ID_FULL_STATE) {
            buffer.writeVarInt(items.size());
            for (TravellingItem item : items) {
                // Serialize ItemStack via codec to NBT, then write as compound
                CompoundTag stackTag = encodeItemStack(item.stack);
                buffer.writeNbt(stackTag);
                buffer.writeBoolean(item.from != null);
                if (item.from != null) buffer.writeByte(item.from.ordinal());
                buffer.writeBoolean(item.to != null);
                if (item.to != null) buffer.writeByte(item.to.ordinal());
                buffer.writeVarInt(item.ticksInPipe);
                buffer.writeVarInt(item.totalTicks);
            }
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, boolean isClient) {
        super.readPayload(id, buffer, isClient);
        if (id == NET_ID_FULL_STATE) {
            items.clear();
            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                CompoundTag stackTag = buffer.readNbt();
                ItemStack stack = decodeItemStack(stackTag);
                Direction from = buffer.readBoolean() ? Direction.values()[buffer.readByte()] : null;
                Direction to = buffer.readBoolean() ? Direction.values()[buffer.readByte()] : null;
                int ticksInPipe = buffer.readVarInt();
                int totalTicks = buffer.readVarInt();
                TravellingItem item = new TravellingItem(stack, from, to, totalTicks);
                item.ticksInPipe = ticksInPipe;
                items.add(item);
            }
        }
    }

    private static CompoundTag encodeItemStack(ItemStack stack) {
        if (stack.isEmpty()) return new CompoundTag();
        DataResult<Tag> result = ItemStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, stack);
        return result.result()
            .filter(t -> t instanceof CompoundTag)
            .map(t -> (CompoundTag) t)
            .orElse(new CompoundTag());
    }

    private static ItemStack decodeItemStack(@Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return ItemStack.EMPTY;
        DataResult<ItemStack> result = ItemStack.OPTIONAL_CODEC.parse(NbtOps.INSTANCE, tag);
        return result.result().orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean canConnect(Direction face, PipeFlow other) {
        return other instanceof PipeFlowItems;
    }

    @Override
    public boolean canConnect(Direction face, BlockEntity oTile) {
        // Connect to any tile that has an item storage on the queried side
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null) return false;
        BlockPos pos = pipe.getHolder().getPipePos().relative(face);
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, face.getOpposite());
        return storage != null;
    }

    @Override
    public void onTick() {
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null || level.isClientSide()) return;

        List<TravellingItem> finished = new ArrayList<>();
        for (TravellingItem item : items) {
            item.ticksInPipe++;
            if (item.isFinished()) {
                finished.add(item);
            }
        }
        items.removeAll(finished);

        for (TravellingItem item : finished) {
            if (item.to == null) {
                // At center, pick an output direction
                item.to = pickOutputDirection(item);
                if (item.to != null) {
                    item.ticksInPipe = 0;
                    item.totalTicks = DEFAULT_TRAVEL_TICKS / 2; // half-pipe travel
                    items.add(item);
                } else {
                    // No valid output, drop the item
                    dropItem(item);
                }
            } else {
                // Reached the end, try to insert into neighbour
                if (!insertIntoNeighbour(item, item.to)) {
                    dropItem(item);
                }
            }
        }

        if (!finished.isEmpty()) {
            pipe.getHolder().scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.FLOW);
        }
    }

    @Nullable
    private Direction pickOutputDirection(TravellingItem item) {
        List<Direction> candidates = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if (dir == item.from) continue; // Don't go back the way we came
            if (pipe.isConnected(dir)) {
                candidates.add(dir);
            }
        }
        if (candidates.isEmpty()) {
            // Allow going back as a last resort
            if (item.from != null && pipe.isConnected(item.from)) {
                return item.from;
            }
            return null;
        }
        // Simple random routing
        Collections.shuffle(candidates);
        return candidates.get(0);
    }

    private boolean insertIntoNeighbour(TravellingItem item, Direction dir) {
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null) return false;

        // Try inserting into an adjacent pipe first
        IPipe neighbour = pipe.getConnectedPipe(dir);
        if (neighbour != null && neighbour.getFlow() instanceof PipeFlowItems neighbourFlow) {
            neighbourFlow.acceptItem(item.stack, dir.getOpposite(), item.colour);
            return true;
        }

        // Try inserting into an adjacent inventory
        BlockPos neighbourPos = pipe.getHolder().getPipePos().relative(dir);
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, neighbourPos, dir.getOpposite());
        if (storage != null) {
            try (Transaction transaction = Transaction.openOuter()) {
                ItemVariant variant = ItemVariant.of(item.stack);
                long inserted = storage.insert(variant, item.stack.getCount(), transaction);
                if (inserted > 0) {
                    item.stack.shrink((int) inserted);
                    transaction.commit();
                    if (item.stack.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return item.stack.isEmpty();
    }

    private void dropItem(TravellingItem item) {
        if (item.stack.isEmpty()) return;
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null) return;
        BlockPos pos = pipe.getHolder().getPipePos();
        net.minecraft.world.Containers.dropItemStack(level,
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item.stack);
    }

    /** Accept an item into this pipe from an adjacent pipe or injection. */
    public void acceptItem(ItemStack stack, @Nullable Direction from, @Nullable DyeColor colour) {
        if (stack.isEmpty()) return;
        // Items travel to center first (half the total ticks)
        TravellingItem item = new TravellingItem(stack.copy(), from, null, DEFAULT_TRAVEL_TICKS / 2);
        item.colour = colour;
        items.add(item);
    }

    /** Extract items from an adjacent inventory in the given direction. Used by wood pipe behaviour. */
    public void extractItems(Direction dir, int maxCount) {
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null) return;
        BlockPos neighbourPos = pipe.getHolder().getPipePos().relative(dir);
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, neighbourPos, dir.getOpposite());
        if (storage == null) return;

        try (Transaction transaction = Transaction.openOuter()) {
            // Use StorageUtil to find and extract the first available item
            ItemVariant extracted = StorageUtil.findExtractableResource(storage, transaction);
            if (extracted != null && !extracted.isBlank()) {
                long amount = storage.extract(extracted, maxCount, transaction);
                if (amount > 0) {
                    ItemStack stack = extracted.toStack((int) amount);
                    acceptItem(stack, dir, null);
                    transaction.commit();
                    return;
                }
            }
        }
    }

    @Override
    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
        for (TravellingItem item : items) {
            if (!item.stack.isEmpty()) {
                toDrop.add(item.stack);
            }
        }
    }

    // ==================
    // IInjectable
    // ==================

    @Override
    public boolean canInjectItems(Direction from) {
        return pipe.isConnected(from);
    }

    @Override
    public ItemStack injectItem(ItemStack stack, boolean doAdd, Direction from,
                                @Nullable DyeColor color, double speed) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!canInjectItems(from)) return stack;
        if (doAdd) {
            acceptItem(stack, from, color);
            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY; // simulate: would accept all
    }

    /** Get a read-only view of the items currently travelling. */
    public List<TravellingItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Destroy all items currently in this pipe. Used by void pipe behaviour. */
    public void voidAllItems() {
        items.clear();
    }
}
