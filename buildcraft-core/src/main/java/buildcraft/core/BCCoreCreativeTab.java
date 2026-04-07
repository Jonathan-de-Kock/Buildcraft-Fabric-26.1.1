/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class BCCoreCreativeTab {
    public static final ResourceKey<CreativeModeTab> TAB_KEY =
        ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(BCCore.MOD_ID, "main"));
    public static CreativeModeTab MAIN_TAB;

    /** Buildcraft mod ID prefixes to include in the creative tab. */
    private static final String[] BC_NAMESPACES = {
        "buildcraftcore", "buildcrafttransport", "buildcraftfactory",
        "buildcraftenergy", "buildcraftsilicon", "buildcraftbuilders",
        "buildcraftrobotics"
    };

    private BCCoreCreativeTab() {}

    private static boolean isBuildCraftItem(Identifier id) {
        for (String ns : BC_NAMESPACES) {
            if (ns.equals(id.getNamespace())) return true;
        }
        return false;
    }

    public static void register() {
        MAIN_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(BCCore.MOD_ID, "main"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
                .icon(() -> new ItemStack(BCCoreItems.wrench))
                .title(Component.translatable("itemGroup.buildcraft.main"))
                .displayItems((params, output) -> {
                    // Dynamically add ALL BuildCraft items from the registry
                    for (Item item : BuiltInRegistries.ITEM) {
                        Identifier id = BuiltInRegistries.ITEM.getKey(item);
                        if (id != null && isBuildCraftItem(id)) {
                            output.accept(item);
                        }
                    }
                })
                .build()
        );
    }
}
