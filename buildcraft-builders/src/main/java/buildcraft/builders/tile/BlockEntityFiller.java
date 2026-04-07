/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.builders.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.lib.energy.BCEnergyStorage;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.tile.BlockEntityBCBase;

/**
 * Stub block entity for the Filler.
 * Consumes energy to fill or clear areas with blocks.
 * TODO: Implement fill patterns, block source inventory, and area definition.
 */
public class BlockEntityFiller extends BlockEntityBCBase {
    protected static final IdAllocator IDS = BlockEntityBCBase.IDS.makeChild("filler");

    /** Energy storage for fill/clear operations. */
    public final BCEnergyStorage energyStorage = new BCEnergyStorage(
        100000, // capacity
        2000,   // max insert per tick
        0       // no extraction
    );

    public BlockEntityFiller(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
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
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyStorage.setStored(input.getLongOr("energy", 0L));
    }
}
