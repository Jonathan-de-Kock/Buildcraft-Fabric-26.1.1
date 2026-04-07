/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.entity.BlockEntityType;

import buildcraft.lib.registry.BCRegistration;
import buildcraft.transport.tile.BlockEntityPipeHolder;

/**
 * Registers all block entity types for the transport module.
 */
public final class BCTransportBlockEntities {
    public static BlockEntityType<BlockEntityPipeHolder> PIPE_HOLDER;

    private BCTransportBlockEntities() {}

    public static void register() {
        PIPE_HOLDER = BCRegistration.registerBlockEntity(BCTransport.MOD_ID, "pipe_holder",
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new BlockEntityPipeHolder(PIPE_HOLDER, pos, state),
                BCTransportBlocks.pipeHolder
            ).build());
    }
}
