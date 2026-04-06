/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core.tile;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.entity.BlockEntityType;

import buildcraft.core.BCCore;
import buildcraft.core.BCCoreBlocks;
import buildcraft.lib.registry.BCRegistration;

public final class BCCoreBlockEntities {
    public static BlockEntityType<BlockEntityMarkerVolume> MARKER_VOLUME;
    public static BlockEntityType<BlockEntityMarkerPath> MARKER_PATH;

    private BCCoreBlockEntities() {}

    public static void register() {
        MARKER_VOLUME = BCRegistration.registerBlockEntity(BCCore.MOD_ID, "marker_volume",
            FabricBlockEntityTypeBuilder.create(BlockEntityMarkerVolume::new, BCCoreBlocks.markerVolume).build());
        MARKER_PATH = BCRegistration.registerBlockEntity(BCCore.MOD_ID, "marker_path",
            FabricBlockEntityTypeBuilder.create(BlockEntityMarkerPath::new, BCCoreBlocks.markerPath).build());
    }
}
