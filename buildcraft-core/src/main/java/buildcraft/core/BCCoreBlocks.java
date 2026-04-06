/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import buildcraft.core.block.BlockEngineCreative;
import buildcraft.core.block.BlockEngineRedstone;
import buildcraft.core.block.BlockMarkerPath;
import buildcraft.core.block.BlockMarkerVolume;
import buildcraft.lib.block.BlockBCBase;
import buildcraft.lib.registry.BCRegistration;

public final class BCCoreBlocks {
    public static Block markerVolume;
    public static Block markerPath;
    public static Block decorated;
    public static Block engineRedstone;
    public static Block engineCreative;
    // TODO Phase 7: spring

    private BCCoreBlocks() {}

    public static void register() {
        String modId = BCCore.MOD_ID;

        BlockBehaviour.Properties markerProps = BlockBehaviour.Properties.of()
            .strength(0.25f)
            .sound(SoundType.METAL)
            .noOcclusion()
            .noCollision();

        markerVolume = BCRegistration.registerBlockAndItem(modId, "marker_volume",
            new BlockMarkerVolume(markerProps));

        markerPath = BCRegistration.registerBlockAndItem(modId, "marker_path",
            new BlockMarkerPath(markerProps));

        decorated = BCRegistration.registerBlockAndItem(modId, "decorated",
            new BlockBCBase(BlockBCBase.defaultBlockProperties()));

        BlockBehaviour.Properties engineProps = BlockBehaviour.Properties.of()
            .strength(5.0f, 10.0f)
            .sound(SoundType.METAL);

        engineRedstone = BCRegistration.registerBlockAndItem(modId, "engine_redstone",
            new BlockEngineRedstone(engineProps));

        engineCreative = BCRegistration.registerBlockAndItem(modId, "engine_creative",
            new BlockEngineCreative(engineProps));
    }
}
