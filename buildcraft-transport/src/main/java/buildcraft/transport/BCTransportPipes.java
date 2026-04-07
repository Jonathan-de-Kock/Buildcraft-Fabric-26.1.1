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
import buildcraft.transport.pipe.behaviour.PipeBehaviourVoid;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWood;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowStructure;

/**
 * Declares and registers all pipe definitions for the transport module.
 * Also initializes the flow types in {@link PipeApi}.
 */
public final class BCTransportPipes {
    // Item pipes
    public static PipeDefinition STONE_ITEM;
    public static PipeDefinition COBBLE_ITEM;
    public static PipeDefinition WOOD_ITEM;
    public static PipeDefinition GOLD_ITEM;
    public static PipeDefinition IRON_ITEM;
    public static PipeDefinition DIAMOND_ITEM;
    public static PipeDefinition VOID_ITEM;

    // Fluid pipes
    public static PipeDefinition STONE_FLUID;
    public static PipeDefinition COBBLE_FLUID;
    public static PipeDefinition WOOD_FLUID;

    // Power pipes
    public static PipeDefinition STONE_POWER;
    public static PipeDefinition COBBLE_POWER;
    public static PipeDefinition WOOD_POWER;

    // Structure pipe
    public static PipeDefinition STRUCTURE;

    private BCTransportPipes() {}

    /** Must be called before pipe definitions are created. */
    public static void registerFlowTypes() {
        PipeApi.flowItems = new PipeFlowType(PipeFlowItems::new, PipeFlowItems::new);
        PipeApi.flowFluids = new PipeFlowType(PipeFlowFluids::new, PipeFlowFluids::new);
        PipeApi.flowPower = new PipeFlowType(PipeFlowPower::new, PipeFlowPower::new);
        PipeApi.flowStructure = new PipeFlowType(PipeFlowStructure::new, PipeFlowStructure::new);
    }

    public static void register() {
        String modId = BCTransport.MOD_ID;

        // ========================
        // Item pipes
        // ========================

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

        GOLD_ITEM = new PipeDefinition.PipeDefinitionBuilder()
            .id(modId, "pipe_gold_item")
            .texPrefix(modId, "gold")
            .logic(PipeBehaviourStone::new, PipeBehaviourStone::new) // TODO: PipeBehaviourGold (speed boost)
            .flowItem()
            .enableColouring()
            .define();

        IRON_ITEM = new PipeDefinition.PipeDefinitionBuilder()
            .id(modId, "pipe_iron_item")
            .texPrefix(modId, "iron")
            .logic(PipeBehaviourStone::new, PipeBehaviourStone::new) // TODO: PipeBehaviourIron (directional)
            .flowItem()
            .enableColouring()
            .define();

        DIAMOND_ITEM = new PipeDefinition.PipeDefinitionBuilder()
            .id(modId, "pipe_diamond_item")
            .texPrefix(modId, "diamond")
            .logic(PipeBehaviourStone::new, PipeBehaviourStone::new) // TODO: PipeBehaviourDiamond (filtering)
            .flowItem()
            .enableColouring()
            .define();

        VOID_ITEM = new PipeDefinition.PipeDefinitionBuilder()
            .id(modId, "pipe_void_item")
            .texPrefix(modId, "void")
            .logic(PipeBehaviourVoid::new, PipeBehaviourVoid::new)
            .flowItem()
            .enableColouring()
            .define();

        // ========================
        // Fluid pipes
        // ========================

        STONE_FLUID = new PipeDefinition.PipeDefinitionBuilder()
            .id(modId, "pipe_stone_fluid")
            .texPrefix(modId, "stone")
            .logic(PipeBehaviourStone::new, PipeBehaviourStone::new)
            .flowFluid()
            .enableTranslucentColouring()
            .define();

        COBBLE_FLUID = new PipeDefinition.PipeDefinitionBuilder()
            .id(modId, "pipe_cobble_fluid")
            .texPrefix(modId, "cobble")
            .logic(PipeBehaviourCobble::new, PipeBehaviourCobble::new)
            .flowFluid()
            .enableTranslucentColouring()
            .define();

        WOOD_FLUID = new PipeDefinition.PipeDefinitionBuilder()
            .id(modId, "pipe_wood_fluid")
            .texPrefix(modId, "wood")
            .texSuffixes("_clear", "_filled")
            .logic(PipeBehaviourWood::new, PipeBehaviourWood::new)
            .flowFluid()
            .enableTranslucentColouring()
            .define();

        // ========================
        // Power pipes
        // ========================

        STONE_POWER = new PipeDefinition.PipeDefinitionBuilder()
            .id(modId, "pipe_stone_power")
            .texPrefix(modId, "stone")
            .logic(PipeBehaviourStone::new, PipeBehaviourStone::new)
            .flowPower()
            .enableColouring()
            .define();

        COBBLE_POWER = new PipeDefinition.PipeDefinitionBuilder()
            .id(modId, "pipe_cobble_power")
            .texPrefix(modId, "cobble")
            .logic(PipeBehaviourCobble::new, PipeBehaviourCobble::new)
            .flowPower()
            .enableColouring()
            .define();

        WOOD_POWER = new PipeDefinition.PipeDefinitionBuilder()
            .id(modId, "pipe_wood_power")
            .texPrefix(modId, "wood")
            .texSuffixes("_clear", "_filled")
            .logic(PipeBehaviourWood::new, PipeBehaviourWood::new)
            .flowPower()
            .enableColouring()
            .define();

        // ========================
        // Structure pipe
        // ========================

        STRUCTURE = new PipeDefinition.PipeDefinitionBuilder()
            .id(modId, "pipe_structure")
            .texPrefix(modId, "structure")
            .logic(PipeBehaviourStone::new, PipeBehaviourStone::new)
            .flow(PipeApi.flowStructure)
            .disableColouring()
            .define();
    }
}
