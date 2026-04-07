/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.silicon.tile;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.entity.BlockEntityType;

import team.reborn.energy.api.EnergyStorage;

import buildcraft.lib.registry.BCRegistration;
import buildcraft.silicon.BCSilicon;
import buildcraft.silicon.BCSiliconBlocks;

public final class BCSlnBlockEntities {
    public static BlockEntityType<BlockEntityAssemblyTable> ASSEMBLY_TABLE;
    public static BlockEntityType<BlockEntityAssemblyTable> INTEGRATION_TABLE;
    public static BlockEntityType<BlockEntityAssemblyTable> CHARGING_TABLE;
    public static BlockEntityType<BlockEntityLaser> LASER;

    private BCSlnBlockEntities() {}

    public static void register() {
        ASSEMBLY_TABLE = BCRegistration.registerBlockEntity(BCSilicon.MOD_ID, "assembly_table",
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new BlockEntityAssemblyTable(ASSEMBLY_TABLE, pos, state),
                BCSiliconBlocks.assemblyTable
            ).build());

        INTEGRATION_TABLE = BCRegistration.registerBlockEntity(BCSilicon.MOD_ID, "integration_table",
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new BlockEntityAssemblyTable(INTEGRATION_TABLE, pos, state),
                BCSiliconBlocks.integrationTable
            ).build());

        CHARGING_TABLE = BCRegistration.registerBlockEntity(BCSilicon.MOD_ID, "charging_table",
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new BlockEntityAssemblyTable(CHARGING_TABLE, pos, state),
                BCSiliconBlocks.chargingTable
            ).build());

        LASER = BCRegistration.registerBlockEntity(BCSilicon.MOD_ID, "laser",
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new BlockEntityLaser(LASER, pos, state),
                BCSiliconBlocks.laser
            ).build());

        registerEnergyConsumers();
    }

    /**
     * Register EnergyStorage.SIDED so that engines/pipes can push energy into tables and lasers.
     */
    private static void registerEnergyConsumers() {
        // Tables accept energy from all sides (lasers beam energy in)
        EnergyStorage.SIDED.registerForBlockEntity(
            (table, direction) -> table.energyStorage,
            ASSEMBLY_TABLE
        );
        EnergyStorage.SIDED.registerForBlockEntity(
            (table, direction) -> table.energyStorage,
            INTEGRATION_TABLE
        );
        EnergyStorage.SIDED.registerForBlockEntity(
            (table, direction) -> table.energyStorage,
            CHARGING_TABLE
        );

        // Lasers accept energy from all sides
        EnergyStorage.SIDED.registerForBlockEntity(
            (laser, direction) -> laser.energyStorage,
            LASER
        );
    }
}
