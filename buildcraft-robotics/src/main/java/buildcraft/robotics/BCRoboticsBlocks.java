/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.robotics;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import buildcraft.lib.registry.BCRegistration;
import buildcraft.robotics.block.BlockZonePlanner;

public final class BCRoboticsBlocks {
    public static Block zonePlanner;

    private BCRoboticsBlocks() {}

    public static void register() {
        String modId = BCRobotics.MOD_ID;

        BlockBehaviour.Properties plannerProps = BlockBehaviour.Properties.of()
            .strength(5.0f, 10.0f)
            .sound(SoundType.METAL);

        zonePlanner = BCRegistration.registerBlockAndItem(modId, "zone_planner",
            plannerProps, BlockZonePlanner::new);
    }
}
