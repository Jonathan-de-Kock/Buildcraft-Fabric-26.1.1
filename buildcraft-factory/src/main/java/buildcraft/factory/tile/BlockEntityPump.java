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
import buildcraft.lib.fluid.BCTank;
import buildcraft.lib.fluid.BCTankManager;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.BlockEntityBCBase;

/**
 * Pump block entity -- consumes energy to pump fluid from the world.
 * Stores energy in a BCEnergyStorage (insert only, no extract).
 * Has an internal fluid buffer tank.
 * TODO: actual pumping logic (scanning for fluid, removing source blocks).
 */
public class BlockEntityPump extends BlockEntityBCBase {
    protected static final IdAllocator IDS = BlockEntityBCBase.IDS.makeChild("pump");

    /** Energy capacity: 10,000 E. Max insert: 500 E/t. No extraction. */
    private final BCEnergyStorage energyStorage = new BCEnergyStorage(10_000, 500, 0);

    /** Internal fluid buffer: 4 buckets. */
    private final BCTank tank = new BCTank("buffer", 4);
    private final BCTankManager tankManager = new BCTankManager().add(tank);

    public BlockEntityPump(BlockPos pos, BlockState state) {
        super(BCFactoryBlockEntities.PUMP, pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    /** @return The energy storage for API exposure. */
    public BCEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public BCTank getTank() {
        return tank;
    }

    /**
     * Server-side tick. TODO: implement actual pump logic.
     * For now, just a placeholder that marks the entity as dirty if energy is present.
     */
    public void tick() {
        if (level == null || level.isClientSide()) return;
        // TODO: consume energy, scan for fluids below, pump into internal tank
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
            tankManager.writeData(buffer);
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, boolean isClient) throws IOException {
        super.readPayload(id, buffer, isClient);
        if (id == NET_RENDER_DATA) {
            energyStorage.setStored(buffer.readLong());
            tankManager.readData(buffer);
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
        ValueOutput tanksOutput = output.child("tanks");
        tankManager.save(tanksOutput);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyStorage.setStored(input.getLongOr("energy", 0));
        input.child("tanks").ifPresent(tankManager::load);
    }
}
