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
    public static ItemPipe pipeStoneItem;
    public static ItemPipe pipeCobbleItem;
    public static ItemPipe pipeWoodItem;

    private BCTransportItems() {}

    public static void register() {
        pipeStoneItem = registerPipeItem("pipe_stone_item", BCTransportPipes.STONE_ITEM);
        pipeCobbleItem = registerPipeItem("pipe_cobble_item", BCTransportPipes.COBBLE_ITEM);
        pipeWoodItem = registerPipeItem("pipe_wood_item", BCTransportPipes.WOOD_ITEM);
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
