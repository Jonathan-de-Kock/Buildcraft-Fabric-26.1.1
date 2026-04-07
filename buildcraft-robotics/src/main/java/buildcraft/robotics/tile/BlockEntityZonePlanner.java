/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.robotics.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.lib.energy.BCEnergyStorage;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.tile.BlockEntityBCBase;

/**
 * Stub block entity for the Zone Planner.
 * Stores selected zone data (min/max BlockPos) and consumes energy.
 * TODO: Implement full zone selection UI and map rendering.
 */
public class BlockEntityZonePlanner extends BlockEntityBCBase {
    protected static final IdAllocator IDS = BlockEntityBCBase.IDS.makeChild("zone_planner");

    /** Energy storage -- powers the zone planner's operation. */
    public final BCEnergyStorage energyStorage = new BCEnergyStorage(
        32000,  // capacity (32k MJ equivalent)
        1000,   // max insert per tick
        0       // no extraction -- energy is consumed internally
    );

    /** Selected zone bounds (min corner). */
    private BlockPos zoneMin = BlockPos.ZERO;
    /** Selected zone bounds (max corner). */
    private BlockPos zoneMax = BlockPos.ZERO;

    public BlockEntityZonePlanner(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    // ##################
    //
    // Zone data accessors
    //
    // ##################

    public BlockPos getZoneMin() {
        return zoneMin;
    }

    public BlockPos getZoneMax() {
        return zoneMax;
    }

    public void setZone(BlockPos min, BlockPos max) {
        this.zoneMin = min;
        this.zoneMax = max;
        setChanged();
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

        ValueOutput zoneOutput = output.child("zone");
        zoneOutput.putInt("minX", zoneMin.getX());
        zoneOutput.putInt("minY", zoneMin.getY());
        zoneOutput.putInt("minZ", zoneMin.getZ());
        zoneOutput.putInt("maxX", zoneMax.getX());
        zoneOutput.putInt("maxY", zoneMax.getY());
        zoneOutput.putInt("maxZ", zoneMax.getZ());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyStorage.setStored(input.getLongOr("energy", 0L));

        input.child("zone").ifPresent(zoneInput -> {
            zoneMin = new BlockPos(
                zoneInput.getIntOr("minX", 0),
                zoneInput.getIntOr("minY", 0),
                zoneInput.getIntOr("minZ", 0)
            );
            zoneMax = new BlockPos(
                zoneInput.getIntOr("maxX", 0),
                zoneInput.getIntOr("maxY", 0),
                zoneInput.getIntOr("maxZ", 0)
            );
        });
    }
}
