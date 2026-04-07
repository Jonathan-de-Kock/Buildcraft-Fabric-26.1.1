/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport;

import net.minecraft.world.item.Item;

import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.lib.registry.BCRegistration;
import buildcraft.transport.item.ItemPipe;
import buildcraft.transport.pipe.PipeRegistryImpl;

/**
 * Registers all items for the transport module, including pipe items.
 */
public final class BCTransportItems {
    // Item pipes
    public static ItemPipe pipeStoneItem;
    public static ItemPipe pipeCobbleItem;
    public static ItemPipe pipeWoodItem;
    public static ItemPipe pipeGoldItem;
    public static ItemPipe pipeIronItem;
    public static ItemPipe pipeDiamondItem;
    public static ItemPipe pipeVoidItem;

    // Fluid pipes
    public static ItemPipe pipeStoneFluid;
    public static ItemPipe pipeCobbleFluid;
    public static ItemPipe pipeWoodFluid;

    // Power pipes
    public static ItemPipe pipeStonePower;
    public static ItemPipe pipeCobblePower;
    public static ItemPipe pipeWoodPower;

    // Structure pipe
    public static ItemPipe pipeStructure;

    private BCTransportItems() {}

    public static void register() {
        // Item pipes
        pipeStoneItem = registerPipeItem("pipe_stone_item", BCTransportPipes.STONE_ITEM);
        pipeCobbleItem = registerPipeItem("pipe_cobble_item", BCTransportPipes.COBBLE_ITEM);
        pipeWoodItem = registerPipeItem("pipe_wood_item", BCTransportPipes.WOOD_ITEM);
        pipeGoldItem = registerPipeItem("pipe_gold_item", BCTransportPipes.GOLD_ITEM);
        pipeIronItem = registerPipeItem("pipe_iron_item", BCTransportPipes.IRON_ITEM);
        pipeDiamondItem = registerPipeItem("pipe_diamond_item", BCTransportPipes.DIAMOND_ITEM);
        pipeVoidItem = registerPipeItem("pipe_void_item", BCTransportPipes.VOID_ITEM);

        // Fluid pipes
        pipeStoneFluid = registerPipeItem("pipe_stone_fluid", BCTransportPipes.STONE_FLUID);
        pipeCobbleFluid = registerPipeItem("pipe_cobble_fluid", BCTransportPipes.COBBLE_FLUID);
        pipeWoodFluid = registerPipeItem("pipe_wood_fluid", BCTransportPipes.WOOD_FLUID);

        // Power pipes
        pipeStonePower = registerPipeItem("pipe_stone_power", BCTransportPipes.STONE_POWER);
        pipeCobblePower = registerPipeItem("pipe_cobble_power", BCTransportPipes.COBBLE_POWER);
        pipeWoodPower = registerPipeItem("pipe_wood_power", BCTransportPipes.WOOD_POWER);

        // Structure pipe
        pipeStructure = registerPipeItem("pipe_structure", BCTransportPipes.STRUCTURE);
    }

    private static ItemPipe registerPipeItem(String name, PipeDefinition definition) {
        Item.Properties props = new Item.Properties()
            .setId(BCRegistration.itemKey(BCTransport.MOD_ID, name));
        ItemPipe item = new ItemPipe(props, definition);
        BCRegistration.registerItem(BCTransport.MOD_ID, name, item);
        PipeRegistryImpl.INSTANCE.setItemForPipe(definition, item);
        return item;
    }
}
