/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core.tile;

import buildcraft.lib.engine.BlockEntityEngineBase;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Redstone engine: the simplest BuildCraft engine.
 * Produces a small amount of energy when powered by redstone.
 *
 * <p>Output: 13 E/t (~0.05 MJ/t = 12.5 E, rounded up).
 * Max stored: 250 E (1 MJ).
 * Max extract: 1000 E/t (4 MJ).
 */
public class BlockEntityEngineRedstone extends BlockEntityEngineBase {

    public BlockEntityEngineRedstone(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean isBurning() {
        return isRedstonePowered();
    }

    @Override
    public long getCurrentOutput() {
        return 13; // ~0.05 MJ/t = 12.5 E, rounded up
    }

    @Override
    public long getMaxPower() {
        return 250; // 1 MJ
    }

    @Override
    public long getMaxExtract() {
        return 1000; // 4 MJ
    }

    @Override
    protected float getPistonSpeed() {
        // Redstone engines run at half the normal speed
        return super.getPistonSpeed() * 0.5f;
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && !level.isClientSide() && isActive()) {
            // Gradual heat buildup, capped below ideal
            if (level.getLevelData().getGameTime() % 16 == 0) {
                heat = Math.min(heat + 4, IDEAL_HEAT * 0.8f);
            }
        }
    }
}
