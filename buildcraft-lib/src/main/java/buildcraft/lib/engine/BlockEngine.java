/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraft.lib.block.BlockBCTile;

/**
 * Base block class for all BuildCraft engines.
 * Provides 6-directional facing via the FACING blockstate property,
 * a non-full-block shape matching the engine model, and a ticker
 * that delegates to {@link BlockEntityEngineBase#tick()}.
 */
public abstract class BlockEngine<T extends BlockEntityEngineBase> extends BlockBCTile<T> {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    // Resting engine shape: base + piston (full width, y 0-8), trunk above (y 8-16).
    private static final VoxelShape SHAPE_UP = Shapes.or(
        Block.box(0, 0, 0, 16, 8, 16),
        Block.box(4, 8, 4, 12, 16, 12)
    );
    private static final VoxelShape SHAPE_DOWN = Shapes.or(
        Block.box(0, 8, 0, 16, 16, 16),
        Block.box(4, 0, 4, 12, 8, 12)
    );
    private static final VoxelShape SHAPE_NORTH = Shapes.or(
        Block.box(0, 0, 8, 16, 16, 16),
        Block.box(4, 4, 0, 12, 12, 8)
    );
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
        Block.box(0, 0, 0, 16, 16, 8),
        Block.box(4, 4, 8, 12, 12, 16)
    );
    private static final VoxelShape SHAPE_WEST = Shapes.or(
        Block.box(8, 0, 0, 16, 16, 16),
        Block.box(0, 4, 4, 8, 12, 12)
    );
    private static final VoxelShape SHAPE_EAST = Shapes.or(
        Block.box(0, 0, 0, 8, 16, 16),
        Block.box(8, 4, 4, 16, 12, 12)
    );

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
        return defaultBlockState().setValue(FACING, Direction.UP);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return getEngineShape(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return getEngineShape(state.getValue(FACING));
    }

    private static VoxelShape getEngineShape(Direction facing) {
        return switch (facing) {
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
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
