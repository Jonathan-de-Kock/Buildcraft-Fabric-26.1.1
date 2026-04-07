/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.transport.pipe.PipeRegistryImpl;

public class BCTransport implements ModInitializer {
    public static final String MOD_ID = "buildcrafttransport";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("BuildCraft Transport initializing");

        // 1. Set up the pipe registry
        PipeApi.pipeRegistry = PipeRegistryImpl.INSTANCE;

        // 2. Register flow types (must come before pipe definitions)
        BCTransportPipes.registerFlowTypes();

        // 3. Register pipe definitions
        BCTransportPipes.register();

        // 4. Register blocks
        BCTransportBlocks.register();

        // 5. Register block entities
        BCTransportBlockEntities.register();

        // 6. Register items (depends on pipe definitions and blocks)
        BCTransportItems.register();

        LOGGER.info("BuildCraft Transport initialized with {} pipe definitions",
            PipeRegistryImpl.INSTANCE.getAllDefinitions().size());
    }
}
