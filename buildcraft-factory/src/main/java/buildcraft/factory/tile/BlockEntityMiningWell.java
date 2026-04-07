/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.factory.tile;

import java.io.IOException;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.lib.energy.BCEnergyStorage;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.BlockEntityBCBase;

/**
 * Mining well block entity -- consumes energy to mine blocks downward.
 * Stores energy in a BCEnergyStorage (insert only, no extract).
 * Tracks the current Y level being mined.
 * TODO: actual mining logic (breaking blocks, dropping items).
 */
public class BlockEntityMiningWell extends BlockEntityBCBase {
    protected static final IdAllocator IDS = BlockEntityBCBase.IDS.makeChild("mining_well");

    /** Energy capacity: 10,000 E. Max insert: 500 E/t. No extraction. */
    private final BCEnergyStorage energyStorage = new BCEnergyStorage(10_000, 500, 0);

    /** The current Y level being mined. Starts at -1 (not yet started). */
    private int currentY = -1;

    public BlockEntityMiningWell(BlockPos pos, BlockState state) {
        super(BCFactoryBlockEntities.MINING_WELL, pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    /** @return The energy storage for API exposure. */
    public BCEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    /** @return The current Y level being mined, or -1 if not started. */
    public int getCurrentY() {
        return currentY;
    }

    /**
     * Server-side tick. TODO: implement actual mining logic.
     * For now, initializes currentY if not yet set.
     */
    public void tick() {
        if (level == null || level.isClientSide()) return;
        // Initialize mining position on first tick
        if (currentY == -1) {
            currentY = worldPosition.getY() - 1;
        }
        // TODO: consume energy, break block at currentY, drop items, decrement currentY
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
            buffer.writeLong(energyStorage.getAmount());
            buffer.writeInt(currentY);
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, boolean isClient) throws IOException {
        super.readPayload(id, buffer, isClient);
        if (id == NET_RENDER_DATA) {
            energyStorage.setStored(buffer.readLong());
            currentY = buffer.readInt();
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
        output.putLong("energy", energyStorage.getAmount());
        output.putInt("currentY", currentY);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyStorage.setStored(input.getLongOr("energy", 0));
        currentY = input.getIntOr("currentY", -1);
    }
}
