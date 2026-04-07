/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import buildcraft.lib.registry.BCRegistration;
import buildcraft.transport.block.BlockPipeHolder;

/**
 * Registers all blocks for the transport module.
 */
public final class BCTransportBlocks {
    public static BlockPipeHolder pipeHolder;

    private BCTransportBlocks() {}

    public static void register() {
        String modId = BCTransport.MOD_ID;

        BlockBehaviour.Properties pipeProps = BlockBehaviour.Properties.of()
            .strength(0.25f)
            .sound(SoundType.METAL)
            .noOcclusion()
            .dynamicShape();

        // Register pipe holder block without a BlockItem — pipe items are registered separately
        pipeHolder = BCRegistration.registerBlock(modId, "pipe_holder",
            pipeProps, BlockPipeHolder::new);
    }
}
