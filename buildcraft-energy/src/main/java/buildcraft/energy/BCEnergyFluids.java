/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.energy;

/**
 * Placeholder for BuildCraft Energy fluid registration.
 * TODO: Implement FlowableFluid subclasses for oil and fuel with proper flowing behavior.
 * For now, the fluid blocks are registered as simple solid blocks in {@link BCEnergyBlocks}.
 */
public final class BCEnergyFluids {
    // TODO: Register actual Fabric fluids (FlowableFluid subclasses)
    // public static FlowableFluid OIL_STILL;
    // public static FlowableFluid OIL_FLOWING;
    // public static FlowableFluid FUEL_STILL;
    // public static FlowableFluid FUEL_FLOWING;

    private BCEnergyFluids() {}

    public static void register() {
        // TODO Phase 8+: Implement actual flowing fluids
        // Fabric fluid registration requires:
        // 1. FlowableFluid subclass (still + flowing variants)
        // 2. Fluid block (LiquidBlock)
        // 3. Bucket item
        // 4. FluidRenderHandlerRegistry (client side)
        BCEnergy.LOGGER.info("BuildCraft Energy fluids registered (placeholder)");
    }
}
