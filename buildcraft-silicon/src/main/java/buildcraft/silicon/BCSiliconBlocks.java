/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.silicon;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import buildcraft.lib.registry.BCRegistration;
import buildcraft.silicon.block.BlockAssemblyTable;
import buildcraft.silicon.block.BlockLaser;

public final class BCSiliconBlocks {
    public static Block assemblyTable;
    public static Block integrationTable;
    public static Block chargingTable;
    public static Block laser;

    private BCSiliconBlocks() {}

    public static void register() {
        String modId = BCSilicon.MOD_ID;

        BlockBehaviour.Properties tableProps = BlockBehaviour.Properties.of()
            .strength(5.0f, 10.0f)
            .sound(SoundType.METAL)
            .noOcclusion();

        assemblyTable = BCRegistration.registerBlockAndItem(modId, "assembly_table",
            tableProps, BlockAssemblyTable::new);

        integrationTable = BCRegistration.registerBlockAndItem(modId, "integration_table",
            BlockBehaviour.Properties.of().strength(5.0f, 10.0f).sound(SoundType.METAL).noOcclusion(),
            BlockAssemblyTable::new);  // Reuse assembly table block class for now

        chargingTable = BCRegistration.registerBlockAndItem(modId, "charging_table",
            BlockBehaviour.Properties.of().strength(5.0f, 10.0f).sound(SoundType.METAL).noOcclusion(),
            BlockAssemblyTable::new);  // Reuse assembly table block class for now

        BlockBehaviour.Properties laserProps = BlockBehaviour.Properties.of()
            .strength(5.0f, 10.0f)
            .sound(SoundType.METAL)
            .noOcclusion();

        laser = BCRegistration.registerBlockAndItem(modId, "laser",
            laserProps, BlockLaser::new);
    }
}
