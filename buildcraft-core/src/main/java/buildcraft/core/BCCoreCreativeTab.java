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
import net.minecraft.world.item.ItemStack;

public final class BCCoreCreativeTab {
    public static final ResourceKey<CreativeModeTab> TAB_KEY =
        ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(BCCore.MOD_ID, "main"));
    public static CreativeModeTab MAIN_TAB;

    private BCCoreCreativeTab() {}

    public static void register() {
        MAIN_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(BCCore.MOD_ID, "main"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
                .icon(() -> new ItemStack(BCCoreItems.wrench))
                .title(Component.translatable("itemGroup.buildcraft.main"))
                .displayItems((params, output) -> {
                    // Items
                    output.accept(BCCoreItems.wrench);
                    output.accept(BCCoreItems.gearWood);
                    output.accept(BCCoreItems.gearStone);
                    output.accept(BCCoreItems.gearIron);
                    output.accept(BCCoreItems.gearGold);
                    output.accept(BCCoreItems.gearDiamond);
                    // Blocks
                    output.accept(BCCoreBlocks.markerVolume);
                    output.accept(BCCoreBlocks.markerPath);
                    output.accept(BCCoreBlocks.decorated);
                    // Engines
                    output.accept(BCCoreBlocks.engineRedstone);
                    output.accept(BCCoreBlocks.engineCreative);
                })
                .build()
        );
    }
}
