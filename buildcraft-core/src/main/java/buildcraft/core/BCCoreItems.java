/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core;

import net.minecraft.world.item.Item;

import buildcraft.lib.registry.BCRegistration;

public final class BCCoreItems {
    public static Item wrench;
    public static Item gearWood;
    public static Item gearStone;
    public static Item gearIron;
    public static Item gearGold;
    public static Item gearDiamond;
    // TODO Phase later: paintbrush, list, mapLocation, markerConnector, volumeBox, fragileFluidShard

    private BCCoreItems() {}

    public static void register() {
        String modId = BCCore.MOD_ID;
        wrench = BCRegistration.registerItem(modId, "wrench", new Item(new Item.Properties().stacksTo(1)));
        gearWood = BCRegistration.registerItem(modId, "gear_wood", new Item(new Item.Properties()));
        gearStone = BCRegistration.registerItem(modId, "gear_stone", new Item(new Item.Properties()));
        gearIron = BCRegistration.registerItem(modId, "gear_iron", new Item(new Item.Properties()));
        gearGold = BCRegistration.registerItem(modId, "gear_gold", new Item(new Item.Properties()));
        gearDiamond = BCRegistration.registerItem(modId, "gear_diamond", new Item(new Item.Properties()));
    }
}
