/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core.block;

import buildcraft.core.tile.BCCoreBlockEntities;
import buildcraft.core.tile.BlockEntityEngineCreative;
import buildcraft.lib.engine.BlockEngine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEngineCreative extends BlockEngine<BlockEntityEngineCreative> {
    public BlockEngineCreative(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEngineCreative(BCCoreBlockEntities.ENGINE_CREATIVE, pos, state);
    }
}
