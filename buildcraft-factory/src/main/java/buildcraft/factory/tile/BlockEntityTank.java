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
import buildcraft.lib.fluid.BCTank;
import buildcraft.lib.fluid.BCTankManager;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.BlockEntityBCBase;

/**
 * Tank block entity -- stores a single fluid tank of 16 buckets capacity.
 * Exposes its fluid storage via FluidStorage.SIDED (registered in BCFactoryBlockEntities).
 * Multi-tank stacking is TODO for a future phase.
 */
public class BlockEntityTank extends BlockEntityBCBase {
    protected static final IdAllocator IDS = BlockEntityBCBase.IDS.makeChild("tank");

    private final BCTank tank = new BCTank("tank", 16);
    private final BCTankManager tankManager = new BCTankManager().add(tank);

    public BlockEntityTank(BlockPos pos, BlockState state) {
        super(BCFactoryBlockEntities.TANK, pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    /** @return The fluid tank for API exposure. */
    public BCTank getTank() {
        return tank;
    }

    public BCTankManager getTankManager() {
        return tankManager;
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
            tankManager.writeData(buffer);
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, boolean isClient) throws IOException {
        super.readPayload(id, buffer, isClient);
        if (id == NET_RENDER_DATA) {
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
        ValueOutput tanksOutput = output.child("tanks");
        tankManager.save(tanksOutput);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("tanks").ifPresent(tankManager::load);
    }
}
