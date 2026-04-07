/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.factory;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.world.level.block.entity.BlockEntityType;

import team.reborn.energy.api.EnergyStorage;

import buildcraft.factory.tile.BlockEntityChute;
import buildcraft.factory.tile.BlockEntityMiningWell;
import buildcraft.factory.tile.BlockEntityPump;
import buildcraft.factory.tile.BlockEntityTank;
import buildcraft.lib.registry.BCRegistration;

public final class BCFactoryBlockEntities {
    public static BlockEntityType<BlockEntityTank> TANK;
    public static BlockEntityType<BlockEntityPump> PUMP;
    public static BlockEntityType<BlockEntityMiningWell> MINING_WELL;
    public static BlockEntityType<BlockEntityChute> CHUTE;

    private BCFactoryBlockEntities() {}

    public static void register() {
        String modId = BCFactory.MOD_ID;

        TANK = BCRegistration.registerBlockEntity(modId, "tank",
            FabricBlockEntityTypeBuilder.create(BlockEntityTank::new, BCFactoryBlocks.tank).build());

        PUMP = BCRegistration.registerBlockEntity(modId, "pump",
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new BlockEntityPump(pos, state),
                BCFactoryBlocks.pump
            ).build());

        MINING_WELL = BCRegistration.registerBlockEntity(modId, "mining_well",
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new BlockEntityMiningWell(pos, state),
                BCFactoryBlocks.miningWell
            ).build());

        CHUTE = BCRegistration.registerBlockEntity(modId, "chute",
            FabricBlockEntityTypeBuilder.create(BlockEntityChute::new, BCFactoryBlocks.chute).build());

        registerProviders();
    }

    /**
     * Register Fabric API Lookup providers for energy and fluid interaction.
     */
    private static void registerProviders() {
        // Tank exposes its fluid storage on all sides
        FluidStorage.SIDED.registerForBlockEntity(
            (tank, direction) -> tank.getTank(),
            TANK
        );

        // Pump accepts energy on all sides
        EnergyStorage.SIDED.registerForBlockEntity(
            (pump, direction) -> pump.getEnergyStorage(),
            PUMP
        );

        // Mining well accepts energy on all sides
        EnergyStorage.SIDED.registerForBlockEntity(
            (miningWell, direction) -> miningWell.getEnergyStorage(),
            MINING_WELL
        );
    }
}
