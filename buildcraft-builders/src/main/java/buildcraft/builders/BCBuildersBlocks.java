/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.builders;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import buildcraft.builders.block.BlockQuarry;
import buildcraft.lib.block.BlockBCBase;
import buildcraft.lib.block.BlockBCTile;
import buildcraft.lib.registry.BCRegistration;

public final class BCBuildersBlocks {
    public static Block quarry;
    public static Block filler;
    public static Block builder;
    public static Block architectTable;
    public static Block electronicLibrary;

    private BCBuildersBlocks() {}

    public static void register() {
        String modId = BCBuilders.MOD_ID;

        BlockBehaviour.Properties machineProps = BlockBehaviour.Properties.of()
            .strength(5.0f, 10.0f)
            .sound(SoundType.METAL);

        quarry = BCRegistration.registerBlockAndItem(modId, "quarry",
            machineProps, BlockQuarry::new);

        filler = BCRegistration.registerBlockAndItem(modId, "filler",
            BlockBehaviour.Properties.of().strength(5.0f, 10.0f).sound(SoundType.METAL),
            BlockQuarry::new);  // Reuse quarry block class (has FACING) for now

        builder = BCRegistration.registerBlockAndItem(modId, "builder",
            BlockBehaviour.Properties.of().strength(5.0f, 10.0f).sound(SoundType.METAL),
            BlockQuarry::new);  // Reuse quarry block class (has FACING) for now

        architectTable = BCRegistration.registerBlockAndItem(modId, "architect_table",
            BlockBehaviour.Properties.of().strength(5.0f, 10.0f).sound(SoundType.METAL),
            BlockBCBase::new);  // Simple block, no tile entity yet

        electronicLibrary = BCRegistration.registerBlockAndItem(modId, "electronic_library",
            BlockBehaviour.Properties.of().strength(5.0f, 10.0f).sound(SoundType.METAL),
            BlockBCBase::new);  // Simple block, no tile entity yet
    }
}
