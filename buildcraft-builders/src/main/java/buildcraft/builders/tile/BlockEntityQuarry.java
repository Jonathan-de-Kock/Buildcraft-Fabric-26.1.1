/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.builders.tile;

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
 * Stub block entity for the Quarry.
 * Consumes energy to mine blocks in a defined area.
 * TODO: Implement mining logic, frame building, drill head, and item collection.
 */
public class BlockEntityQuarry extends BlockEntityBCBase {
    protected static final IdAllocator IDS = BlockEntityBCBase.IDS.makeChild("quarry");

    /** Energy storage for mining operations. */
    public final BCEnergyStorage energyStorage = new BCEnergyStorage(
        100000, // capacity (100k)
        2000,   // max insert per tick
        0       // no extraction
    );

    /** The mining area corners. Null if not yet defined. */
    @Nullable
    private BlockPos miningAreaMin = null;
    @Nullable
    private BlockPos miningAreaMax = null;

    /** Current Y level being mined. */
    private int currentMiningY = -1;

    public BlockEntityQuarry(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    /**
     * Called each tick by the block's ticker.
     * TODO: Implement mining sequence (build frame, drill, collect items).
     */
    public void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }
        // TODO Phase 9+: Mining logic
        // 1. Check for marker-defined area or use default 11x11
        // 2. Build frame
        // 3. Mine layer by layer
        // 4. Collect drops into adjacent inventory or pipe
    }

    @Nullable
    public BlockPos getMiningAreaMin() {
        return miningAreaMin;
    }

    @Nullable
    public BlockPos getMiningAreaMax() {
        return miningAreaMax;
    }

    public void setMiningArea(BlockPos min, BlockPos max) {
        this.miningAreaMin = min;
        this.miningAreaMax = max;
        this.currentMiningY = max.getY();
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
        output.putInt("miningY", currentMiningY);
        if (miningAreaMin != null) {
            output.putInt("minX", miningAreaMin.getX());
            output.putInt("minY", miningAreaMin.getY());
            output.putInt("minZ", miningAreaMin.getZ());
        }
        if (miningAreaMax != null) {
            output.putInt("maxX", miningAreaMax.getX());
            output.putInt("maxY", miningAreaMax.getY());
            output.putInt("maxZ", miningAreaMax.getZ());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyStorage.setStored(input.getLongOr("energy", 0L));
        currentMiningY = input.getIntOr("miningY", -1);
        if (input.getIntOr("minX", Integer.MIN_VALUE) != Integer.MIN_VALUE) {
            miningAreaMin = new BlockPos(
                input.getIntOr("minX", 0),
                input.getIntOr("minY", 0),
                input.getIntOr("minZ", 0)
            );
        }
        if (input.getIntOr("maxX", Integer.MIN_VALUE) != Integer.MIN_VALUE) {
            miningAreaMax = new BlockPos(
                input.getIntOr("maxX", 0),
                input.getIntOr("maxY", 0),
                input.getIntOr("maxZ", 0)
            );
        }
    }
}
