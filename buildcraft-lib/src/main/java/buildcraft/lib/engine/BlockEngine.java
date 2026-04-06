/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.engine;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import buildcraft.lib.block.BlockBCTile;

/**
 * Base block class for all BuildCraft engines.
 * Provides 6-directional facing via the FACING blockstate property
 * and a ticker that delegates to {@link BlockEntityEngineBase#tick()}.
 */
public abstract class BlockEngine<T extends BlockEntityEngineBase> extends BlockBCTile<T> {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public BlockEngine(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Default facing up; player can rotate with a wrench later
        return defaultBlockState().setValue(FACING, Direction.UP);
    }

    @Override
    public <E extends BlockEntity> BlockEntityTicker<E> getTicker(Level level, BlockState state, BlockEntityType<E> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof BlockEntityEngineBase engine) {
                engine.tick();
            }
        };
    }
}
