/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.silicon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraft.lib.block.BlockBCTile;
import buildcraft.silicon.tile.BCSlnBlockEntities;
import buildcraft.silicon.tile.BlockEntityAssemblyTable;

public class BlockAssemblyTable extends BlockBCTile<BlockEntityAssemblyTable> {
    /** Table shape: base + support ring + main body, top at y=9. */
    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(0, 0, 0, 16, 1, 16),
        Block.box(1, 1, 1, 15, 3, 15),
        Block.box(0, 3, 0, 16, 9, 16)
    );

    public BlockAssemblyTable(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAssemblyTable(BCSlnBlockEntities.ASSEMBLY_TABLE, pos, state);
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
