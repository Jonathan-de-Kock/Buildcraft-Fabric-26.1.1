/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import buildcraft.lib.tile.BlockEntityBCBase;

/**
 * Screen handler tied to a specific block entity.
 * Replaces the old ContainerBCTile from Forge.
 *
 * Delegates {@link #stillValid(Player)} to the block entity's
 * {@link BlockEntityBCBase#canInteractWith(Player)} check (distance + ownership).
 */
public class ScreenHandlerBCTile<T extends BlockEntityBCBase> extends ScreenHandlerBCBase {
    public final T tile;

    public ScreenHandlerBCTile(MenuType<?> type, int syncId, T tile) {
        super(type, syncId);
        this.tile = tile;
    }

    @Override
    public boolean stillValid(Player player) {
        return tile.canInteractWith(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        tile.onPlayerClose(player);
    }

    // Note: onPlayerOpen is called when the screen handler is created,
    // typically in the block's use() method or ExtendedScreenHandlerFactory.
}
