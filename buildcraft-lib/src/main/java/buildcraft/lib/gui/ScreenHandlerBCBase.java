/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.gui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Base screen handler for all BuildCraft GUIs.
 * Replaces the old ContainerBC_Neptune from Forge.
 *
 * Provides standard player inventory slot layout and shift-click transfer logic.
 * Full widget/property system can be added later.
 */
public abstract class ScreenHandlerBCBase extends AbstractContainerMenu {

    protected ScreenHandlerBCBase(MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    /** Add the standard player inventory (3x9 main + hotbar) at the given pixel position. */
    protected void addFullPlayerInventory(Inventory playerInventory, int startX, int startY) {
        // Main inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                    startX + col * 18, startY + row * 18));
            }
        }
        // Hotbar (1 row of 9, offset 58 pixels below main inventory start)
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col,
                startX + col * 18, startY + 58));
        }
    }

    /** Add the standard player inventory at the default position (8, 84) used by most BC GUIs. */
    protected void addFullPlayerInventory(Inventory playerInventory) {
        addFullPlayerInventory(playerInventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();
            int containerSlots = slots.size() - 36; // total minus player inventory (27 main + 9 hotbar)
            if (index < containerSlots) {
                // Move from container to player inventory
                if (!moveItemStackTo(slotStack, containerSlots, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player inventory to container
                if (!moveItemStackTo(slotStack, 0, containerSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // Override in subclasses for proper distance/validity checks
    }
}
