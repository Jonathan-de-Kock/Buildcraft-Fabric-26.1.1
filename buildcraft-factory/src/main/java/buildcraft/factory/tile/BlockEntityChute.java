/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.factory.tile;

import java.io.IOException;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.BlockEntityBCBase;

/**
 * Chute block entity -- drops items from a simple 1-slot inventory downward.
 * TODO: interact with pipes/containers, configurable output direction.
 */
public class BlockEntityChute extends BlockEntityBCBase {
    protected static final IdAllocator IDS = BlockEntityBCBase.IDS.makeChild("chute");

    /** Simple 1-slot inventory. */
    private ItemStack inventory = ItemStack.EMPTY;

    public BlockEntityChute(BlockPos pos, BlockState state) {
        super(BCFactoryBlockEntities.CHUTE, pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public ItemStack getInventory() {
        return inventory;
    }

    public void setInventory(ItemStack stack) {
        this.inventory = stack;
        setChanged();
    }

    /**
     * Server-side tick. Drops items from inventory downward.
     * TODO: try inserting into inventory below before dropping as entity.
     */
    public void tick() {
        if (level == null || level.isClientSide()) return;
        if (inventory.isEmpty()) return;

        // Drop one item per tick as an entity below the chute
        BlockPos below = worldPosition.below();
        ItemStack toDrop = inventory.split(1);
        Containers.dropItemStack(level, below.getX() + 0.5, below.getY() + 0.5, below.getZ() + 0.5, toDrop);
        if (inventory.isEmpty()) {
            inventory = ItemStack.EMPTY;
        }
        setChanged();
    }

    // ##################
    //
    // Drops
    //
    // ##################

    @Override
    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
        super.addDrops(toDrop, fortune);
        if (!inventory.isEmpty()) {
            toDrop.add(inventory.copy());
        }
    }

    // ##################
    //
    // Network
    //
    // ##################

    @Override
    public void writePayload(int id, PacketBufferBC buffer, boolean isClient) {
        super.writePayload(id, buffer, isClient);
        if (id == NET_RENDER_DATA) {
            // Sync whether chute has items (for rendering purposes)
            buffer.writeBoolean(!inventory.isEmpty());
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, boolean isClient) throws IOException {
        super.readPayload(id, buffer, isClient);
        if (id == NET_RENDER_DATA) {
            boolean hasItem = buffer.readBoolean();
            // Client only needs to know if there's an item for rendering hints
            // Full item data synced via GUI packets when needed
        }
    }

    // ##################
    //
    // NBT
    //
    // ##################

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!inventory.isEmpty()) {
            output.store("inventory", ItemStack.CODEC, inventory);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory = input.read("inventory", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }
}
