/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.silicon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.lib.block.BlockBCTile;
import buildcraft.silicon.tile.BCSlnBlockEntities;
import buildcraft.silicon.tile.BlockEntityAssemblyTable;

public class BlockAssemblyTable extends BlockBCTile<BlockEntityAssemblyTable> {
    public BlockAssemblyTable(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAssemblyTable(BCSlnBlockEntities.ASSEMBLY_TABLE, pos, state);
    }
}
