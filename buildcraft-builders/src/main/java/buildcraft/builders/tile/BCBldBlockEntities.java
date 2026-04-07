/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.builders.tile;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.entity.BlockEntityType;

import team.reborn.energy.api.EnergyStorage;

import buildcraft.builders.BCBuilders;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.lib.registry.BCRegistration;

public final class BCBldBlockEntities {
    public static BlockEntityType<BlockEntityQuarry> QUARRY;
    public static BlockEntityType<BlockEntityFiller> FILLER;
    public static BlockEntityType<BlockEntityQuarry> BUILDER;

    private BCBldBlockEntities() {}

    public static void register() {
        QUARRY = BCRegistration.registerBlockEntity(BCBuilders.MOD_ID, "quarry",
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new BlockEntityQuarry(QUARRY, pos, state),
                BCBuildersBlocks.quarry
            ).build());

        FILLER = BCRegistration.registerBlockEntity(BCBuilders.MOD_ID, "filler",
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new BlockEntityFiller(FILLER, pos, state),
                BCBuildersBlocks.filler
            ).build());

        BUILDER = BCRegistration.registerBlockEntity(BCBuilders.MOD_ID, "builder",
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new BlockEntityQuarry(BUILDER, pos, state),
                BCBuildersBlocks.builder
            ).build());

        registerEnergyConsumers();
    }

    /**
     * Register EnergyStorage.SIDED so that engines/pipes can push energy into builders machines.
     */
    private static void registerEnergyConsumers() {
        EnergyStorage.SIDED.registerForBlockEntity(
            (quarry, direction) -> quarry.energyStorage,
            QUARRY
        );
        EnergyStorage.SIDED.registerForBlockEntity(
            (filler, direction) -> filler.energyStorage,
            FILLER
        );
        EnergyStorage.SIDED.registerForBlockEntity(
            (builder, direction) -> builder.energyStorage,
            BUILDER
        );
    }
}
