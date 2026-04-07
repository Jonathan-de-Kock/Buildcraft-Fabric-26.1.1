/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.registry;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class BCRegistration {
    private BCRegistration() {}

    /** Create a ResourceKey for a block. */
    public static ResourceKey<Block> blockKey(String modId, String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(modId, name));
    }

    /** Create a ResourceKey for an item. */
    public static ResourceKey<Item> itemKey(String modId, String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modId, name));
    }

    /** Set the block ID on properties. Required in MC 26.1 before block construction. */
    public static BlockBehaviour.Properties withId(BlockBehaviour.Properties props, String modId, String name) {
        return props.setId(blockKey(modId, name));
    }

    public static <T extends Block> T registerBlock(String modId, String name, T block) {
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(modId, name), block);
    }

    /** Register a block using a factory. The factory receives Properties with the ID already set. */
    public static <T extends Block> T registerBlock(String modId, String name,
                                                     BlockBehaviour.Properties props,
                                                     Function<BlockBehaviour.Properties, T> factory) {
        T block = factory.apply(props.setId(blockKey(modId, name)));
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(modId, name), block);
    }

    public static <T extends Item> T registerItem(String modId, String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(modId, name), item);
    }

    /** Register an item using a factory. The factory receives Properties with the ID already set. */
    public static Item registerItem(String modId, String name, Item.Properties props) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(modId, name),
            new Item(props.setId(itemKey(modId, name))));
    }

    /** Register a block (via factory) and its BlockItem in one call. */
    public static <T extends Block> T registerBlockAndItem(String modId, String name,
                                                            BlockBehaviour.Properties blockProps,
                                                            Function<BlockBehaviour.Properties, T> factory) {
        T block = registerBlock(modId, name, blockProps, factory);
        Item.Properties itemProps = new Item.Properties().setId(itemKey(modId, name));
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(modId, name),
            new BlockItem(block, itemProps));
        return block;
    }

    public static <T extends BlockEntityType<?>> T registerBlockEntity(String modId, String name, T type) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(modId, name), type);
    }
}
