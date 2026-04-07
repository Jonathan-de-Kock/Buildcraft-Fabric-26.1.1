/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.robotics.tile;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.entity.BlockEntityType;

import team.reborn.energy.api.EnergyStorage;

import buildcraft.lib.registry.BCRegistration;
import buildcraft.robotics.BCRobotics;
import buildcraft.robotics.BCRoboticsBlocks;

public final class BCRobBlockEntities {
    public static BlockEntityType<BlockEntityZonePlanner> ZONE_PLANNER;

    private BCRobBlockEntities() {}

    public static void register() {
        ZONE_PLANNER = BCRegistration.registerBlockEntity(BCRobotics.MOD_ID, "zone_planner",
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new BlockEntityZonePlanner(ZONE_PLANNER, pos, state),
                BCRoboticsBlocks.zonePlanner
            ).build());

        registerEnergyConsumers();
    }

    /**
     * Register EnergyStorage.SIDED so that engines/pipes can push energy into the zone planner.
     */
    private static void registerEnergyConsumers() {
        EnergyStorage.SIDED.registerForBlockEntity(
            (planner, direction) -> planner.energyStorage,
            ZONE_PLANNER
        );
    }
}
