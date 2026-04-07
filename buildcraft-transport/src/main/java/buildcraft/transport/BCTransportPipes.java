/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeFlowType;
import buildcraft.transport.pipe.behaviour.PipeBehaviourCobble;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStone;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWood;
import buildcraft.transport.pipe.flow.PipeFlowItems;

/**
 * Declares and registers all pipe definitions for the transport module.
 * Also initializes the flow types in {@link PipeApi}.
 */
public final class BCTransportPipes {
    public static PipeDefinition STONE_ITEM;
    public static PipeDefinition COBBLE_ITEM;
    public static PipeDefinition WOOD_ITEM;

    private BCTransportPipes() {}

    /** Must be called before pipe definitions are created. */
    public static void registerFlowTypes() {
        PipeApi.flowItems = new PipeFlowType(PipeFlowItems::new, PipeFlowItems::new);
        // Placeholder flow types for fluids and power — not implemented yet
        PipeApi.flowFluids = new PipeFlowType(
            pipe -> { throw new UnsupportedOperationException("Fluid flow not yet implemented"); },
            (pipe, nbt) -> { throw new UnsupportedOperationException("Fluid flow not yet implemented"); }
        );
        PipeApi.flowPower = new PipeFlowType(
            pipe -> { throw new UnsupportedOperationException("Power flow not yet implemented"); },
            (pipe, nbt) -> { throw new UnsupportedOperationException("Power flow not yet implemented"); }
        );
    }

    public static void register() {
        String modId = BCTransport.MOD_ID;

        STONE_ITEM = new PipeDefinition.PipeDefinitionBuilder()
            .id(modId, "pipe_stone_item")
            .texPrefix(modId, "stone")
            .logic(PipeBehaviourStone::new, PipeBehaviourStone::new)
            .flowItem()
            .enableColouring()
            .define();

        COBBLE_ITEM = new PipeDefinition.PipeDefinitionBuilder()
            .id(modId, "pipe_cobble_item")
            .texPrefix(modId, "cobble")
            .logic(PipeBehaviourCobble::new, PipeBehaviourCobble::new)
            .flowItem()
            .enableColouring()
            .define();

        WOOD_ITEM = new PipeDefinition.PipeDefinitionBuilder()
            .id(modId, "pipe_wood_item")
            .texPrefix(modId, "wood")
            .texSuffixes("_clear", "_filled")
            .logic(PipeBehaviourWood::new, PipeBehaviourWood::new)
            .flowItem()
            .enableColouring()
            .define();
    }
}
