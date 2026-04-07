/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.energy;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import buildcraft.lib.block.BlockBCBase;
import buildcraft.lib.registry.BCRegistration;

/**
 * Registers placeholder blocks for BuildCraft Energy fluids.
 * These are simple solid blocks for now; they will be replaced with proper
 * LiquidBlock instances once FlowableFluid subclasses are implemented.
 */
public final class BCEnergyBlocks {
    /** Placeholder block for oil fluid. Will become a LiquidBlock. */
    public static Block oilBlock;
    /** Placeholder block for fuel fluid. Will become a LiquidBlock. */
    public static Block fuelBlock;

    private BCEnergyBlocks() {}

    public static void register() {
        String modId = BCEnergy.MOD_ID;

        // Oil: black, viscous — slow movement speed, high resistance
        BlockBehaviour.Properties oilProps = BlockBehaviour.Properties.of()
            .strength(100.0f)  // unbreakable in survival (like fluid blocks)
            .sound(SoundType.EMPTY)
            .noOcclusion()
            .noLootTable()
            .speedFactor(0.3f)  // slow movement through oil
            .replaceable();

        oilBlock = BCRegistration.registerBlock(modId, "oil", oilProps, BlockBCBase::new);

        // Fuel: yellow-ish, flammable
        BlockBehaviour.Properties fuelProps = BlockBehaviour.Properties.of()
            .strength(100.0f)
            .sound(SoundType.EMPTY)
            .noOcclusion()
            .noLootTable()
            .speedFactor(0.6f)
            .replaceable()
            .ignitedByLava();

        fuelBlock = BCRegistration.registerBlock(modId, "fuel", fuelProps, BlockBCBase::new);
    }
}
