/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.lib.tile.BlockEntityBCBase;

public abstract class BlockBCTile<T extends BlockEntityBCBase> extends BlockBCBase implements EntityBlock {
    public BlockBCTile(Properties properties) {
        super(properties);
    }

    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    // Override to hook into tile lifecycle for onRemove, onPlacedBy, etc.
    // We'll add these in later phases as needed
}
