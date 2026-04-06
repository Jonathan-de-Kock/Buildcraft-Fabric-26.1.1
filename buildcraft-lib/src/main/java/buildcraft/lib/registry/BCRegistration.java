/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BCRegistration {
    private BCRegistration() {}

    public static <T extends Block> T registerBlock(String modId, String name, T block) {
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(modId, name), block);
    }

    public static <T extends Item> T registerItem(String modId, String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(modId, name), item);
    }

    /** Register a block and its corresponding BlockItem in one call. */
    public static <T extends Block> T registerBlockAndItem(String modId, String name, T block, Item.Properties itemProps) {
        registerBlock(modId, name, block);
        registerItem(modId, name, new BlockItem(block, itemProps));
        return block;
    }

    /** Register a block and a default BlockItem. */
    public static <T extends Block> T registerBlockAndItem(String modId, String name, T block) {
        return registerBlockAndItem(modId, name, block, new Item.Properties());
    }

    public static <T extends BlockEntityType<?>> T registerBlockEntity(String modId, String name, T type) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(modId, name), type);
    }
}
