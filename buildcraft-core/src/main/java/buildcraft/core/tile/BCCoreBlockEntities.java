/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core.tile;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;

import team.reborn.energy.api.EnergyStorage;

import buildcraft.core.BCCore;
import buildcraft.core.BCCoreBlocks;
import buildcraft.lib.engine.BlockEntityEngineBase;
import buildcraft.lib.registry.BCRegistration;

public final class BCCoreBlockEntities {
    public static BlockEntityType<BlockEntityMarkerVolume> MARKER_VOLUME;
    public static BlockEntityType<BlockEntityMarkerPath> MARKER_PATH;
    public static BlockEntityType<BlockEntityEngineRedstone> ENGINE_REDSTONE;
    public static BlockEntityType<BlockEntityEngineCreative> ENGINE_CREATIVE;

    private BCCoreBlockEntities() {}

    public static void register() {
        MARKER_VOLUME = BCRegistration.registerBlockEntity(BCCore.MOD_ID, "marker_volume",
            FabricBlockEntityTypeBuilder.create(BlockEntityMarkerVolume::new, BCCoreBlocks.markerVolume).build());
        MARKER_PATH = BCRegistration.registerBlockEntity(BCCore.MOD_ID, "marker_path",
            FabricBlockEntityTypeBuilder.create(BlockEntityMarkerPath::new, BCCoreBlocks.markerPath).build());

        ENGINE_REDSTONE = BCRegistration.registerBlockEntity(BCCore.MOD_ID, "engine_redstone",
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new BlockEntityEngineRedstone(ENGINE_REDSTONE, pos, state),
                BCCoreBlocks.engineRedstone
            ).build());
        ENGINE_CREATIVE = BCRegistration.registerBlockEntity(BCCore.MOD_ID, "engine_creative",
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new BlockEntityEngineCreative(ENGINE_CREATIVE, pos, state),
                BCCoreBlocks.engineCreative
            ).build());

        registerEnergyProviders();
    }

    /**
     * Register Fabric API Lookup providers so that adjacent blocks can pull energy
     * from engines via the standard Team Reborn Energy sided lookup.
     * Engines only expose energy on their output face (the opposite of their facing direction).
     */
    private static void registerEnergyProviders() {
        EnergyStorage.SIDED.registerForBlockEntity(
            (engine, direction) -> createEngineEnergyView(engine, direction),
            ENGINE_REDSTONE
        );
        EnergyStorage.SIDED.registerForBlockEntity(
            (engine, direction) -> createEngineEnergyView(engine, direction),
            ENGINE_CREATIVE
        );
    }

    private static EnergyStorage createEngineEnergyView(BlockEntityEngineBase engine, Direction queriedSide) {
        // Engines output energy on their facing direction.
        // The queried side is from the perspective of the querying block, so we check
        // if the queried side is opposite to the engine's facing (i.e., the output face).
        if (queriedSide != null && queriedSide == engine.getFacing().getOpposite()) {
            return new EngineEnergyView(engine);
        }
        return null;
    }

    /**
     * A read-only energy view that exposes the engine's stored power for extraction.
     * Insertion is not supported -- engines generate their own power.
     */
    private static final class EngineEnergyView implements EnergyStorage {
        private final BlockEntityEngineBase engine;

        EngineEnergyView(BlockEntityEngineBase engine) {
            this.engine = engine;
        }

        @Override
        public boolean supportsInsertion() {
            return false;
        }

        @Override
        public long insert(long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
            return 0;
        }

        @Override
        public boolean supportsExtraction() {
            return true;
        }

        @Override
        public long extract(long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
            long toExtract = Math.min(maxAmount, Math.min(engine.getMaxExtract(), engine.getPower()));
            if (toExtract <= 0) return 0;
            transaction.addCloseCallback((ctx, result) -> {
                if (result.wasCommitted()) {
                    engine.extractPower(toExtract);
                }
            });
            return toExtract;
        }

        @Override
        public long getAmount() {
            return engine.getPower();
        }

        @Override
        public long getCapacity() {
            return engine.getMaxPower();
        }
    }
}
