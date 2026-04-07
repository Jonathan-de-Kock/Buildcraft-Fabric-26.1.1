/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraft.lib.block.BlockBCTile;
import buildcraft.transport.BCTransportBlockEntities;
import buildcraft.transport.tile.BlockEntityPipeHolder;

/**
 * The block representing pipes in-world. Uses connection properties to
 * conditionally show arms extending toward connected neighbors.
 * Delegates creation and ticking to {@link BlockEntityPipeHolder}.
 */
public class BlockPipeHolder extends BlockBCTile<BlockEntityPipeHolder> {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    private static final VoxelShape CENTER = Block.box(4, 4, 4, 12, 12, 12);
    private static final VoxelShape ARM_DOWN = Block.box(4, 0, 4, 12, 4, 12);
    private static final VoxelShape ARM_UP = Block.box(4, 12, 4, 12, 16, 12);
    private static final VoxelShape ARM_NORTH = Block.box(4, 4, 0, 12, 12, 4);
    private static final VoxelShape ARM_SOUTH = Block.box(4, 4, 12, 12, 12, 16);
    private static final VoxelShape ARM_WEST = Block.box(0, 4, 4, 4, 12, 12);
    private static final VoxelShape ARM_EAST = Block.box(12, 4, 4, 16, 12, 12);

    public BlockPipeHolder(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
            .setValue(NORTH, false)
            .setValue(SOUTH, false)
            .setValue(EAST, false)
            .setValue(WEST, false)
            .setValue(UP, false)
            .setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityPipeHolder(BCTransportBlockEntities.PIPE_HOLDER, pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                    BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof BlockEntityPipeHolder holder) {
                holder.tick();
            }
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return computeShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return computeShape(state);
    }

    private static VoxelShape computeShape(BlockState state) {
        VoxelShape shape = CENTER;
        if (state.getValue(DOWN)) shape = Shapes.joinUnoptimized(shape, ARM_DOWN, BooleanOp.OR);
        if (state.getValue(UP)) shape = Shapes.joinUnoptimized(shape, ARM_UP, BooleanOp.OR);
        if (state.getValue(NORTH)) shape = Shapes.joinUnoptimized(shape, ARM_NORTH, BooleanOp.OR);
        if (state.getValue(SOUTH)) shape = Shapes.joinUnoptimized(shape, ARM_SOUTH, BooleanOp.OR);
        if (state.getValue(WEST)) shape = Shapes.joinUnoptimized(shape, ARM_WEST, BooleanOp.OR);
        if (state.getValue(EAST)) shape = Shapes.joinUnoptimized(shape, ARM_EAST, BooleanOp.OR);
        return shape;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                   Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, orientation, isMoving);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlockEntityPipeHolder holder) {
            holder.onNeighbourBlockChanged();
        }
    }

    /** Get the BooleanProperty for the given direction. */
    public static BooleanProperty getProperty(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }
}
