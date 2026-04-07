/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.factory.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraft.factory.tile.BlockEntityTank;
import buildcraft.lib.block.BlockBCTile;

/**
 * Transparent fluid storage block. Slightly thinner than a full block
 * with full-width top/bottom caps and inset glass sides.
 * Multi-tank stacking is TODO for a future phase.
 */
public class BlockTank extends BlockBCTile<BlockEntityTank> {
    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(0, 0, 0, 16, 1, 16),    // bottom cap
        Block.box(2, 0, 2, 14, 16, 14),   // glass body
        Block.box(0, 15, 0, 16, 16, 16)   // top cap
    );

    public BlockTank(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityTank(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return SHAPE;
    }
}
