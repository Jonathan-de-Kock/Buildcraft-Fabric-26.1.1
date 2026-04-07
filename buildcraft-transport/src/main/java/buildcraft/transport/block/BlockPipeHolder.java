/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraft.lib.block.BlockBCTile;
import buildcraft.transport.BCTransportBlockEntities;
import buildcraft.transport.tile.BlockEntityPipeHolder;

/**
 * The block representing pipes in-world. Not a full cube, uses a small center shape.
 * Delegates creation and ticking to {@link BlockEntityPipeHolder}.
 */
public class BlockPipeHolder extends BlockBCTile<BlockEntityPipeHolder> {
    /** Small center cube shape for collision/outline. 4/16 to 12/16 in each axis. */
    private static final VoxelShape CENTER_SHAPE = Block.box(4, 4, 4, 12, 12, 12);

    public BlockPipeHolder(Properties properties) {
        super(properties);
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
        // TODO: Compute dynamic shape based on pipe connections
        return CENTER_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return CENTER_SHAPE;
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
}
