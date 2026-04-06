/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.core.tile.BlockEntityMarkerPath;
import buildcraft.lib.block.BlockBCTile;

public class BlockMarkerPath extends BlockBCTile<BlockEntityMarkerPath> {
    public BlockMarkerPath(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMarkerPath(pos, state);
    }
}
