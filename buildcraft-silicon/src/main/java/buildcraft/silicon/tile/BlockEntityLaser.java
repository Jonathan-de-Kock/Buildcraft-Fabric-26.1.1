/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.silicon.tile;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.lib.energy.BCEnergyStorage;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.tile.BlockEntityBCBase;

/**
 * Stub block entity for the Laser.
 * Consumes energy and transfers it to a nearby table (assembly, integration, or charging).
 * TODO: Implement table scanning, beam rendering, and energy transfer logic.
 */
public class BlockEntityLaser extends BlockEntityBCBase {
    protected static final IdAllocator IDS = BlockEntityBCBase.IDS.makeChild("laser");

    /** Energy storage -- consumes RF/MJ to power nearby tables. */
    public final BCEnergyStorage energyStorage = new BCEnergyStorage(
        10000,  // capacity
        400,    // max insert per tick
        0       // no extraction
    );

    /** The position of the table this laser is targeting. Null if no target. */
    @Nullable
    private BlockPos targetTable = null;

    public BlockEntityLaser(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    /**
     * Called each tick by the block's ticker.
     * TODO: Scan for nearby tables, transfer energy, update beam rendering.
     */
    public void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }
        // TODO: Find and cache nearby table positions
        // TODO: Transfer energy to the target table
        // TODO: Send render updates for laser beam
    }

    @Nullable
    public BlockPos getTargetTable() {
        return targetTable;
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
        if (targetTable != null) {
            output.putInt("targetX", targetTable.getX());
            output.putInt("targetY", targetTable.getY());
            output.putInt("targetZ", targetTable.getZ());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyStorage.setStored(input.getLongOr("energy", 0L));
        if (input.getIntOr("targetX", Integer.MIN_VALUE) != Integer.MIN_VALUE) {
            targetTable = new BlockPos(
                input.getIntOr("targetX", 0),
                input.getIntOr("targetY", 0),
                input.getIntOr("targetZ", 0)
            );
        } else {
            targetTable = null;
        }
    }
}
