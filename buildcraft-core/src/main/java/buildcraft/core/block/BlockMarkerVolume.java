/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.core.tile.BlockEntityMarkerVolume;
import buildcraft.lib.block.BlockBCTile;

public class BlockMarkerVolume extends BlockBCTile<BlockEntityMarkerVolume> {
    public BlockMarkerVolume(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMarkerVolume(pos, state);
    }
}
