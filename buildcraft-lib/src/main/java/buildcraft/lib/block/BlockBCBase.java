/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockBCBase extends Block {
    public BlockBCBase(Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties defaultBlockProperties() {
        return BlockBehaviour.Properties.of()
            .strength(5.0f, 10.0f)
            .sound(SoundType.METAL);
    }
}
