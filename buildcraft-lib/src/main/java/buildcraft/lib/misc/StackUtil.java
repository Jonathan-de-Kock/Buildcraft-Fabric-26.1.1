/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Provides various utils for interacting with {@link ItemStack}, and multiples. */
public class StackUtil {

    /** A non-null version of {@link ItemStack#EMPTY}. */
    @NotNull
    public static final ItemStack EMPTY;

    static {
        ItemStack stack = ItemStack.EMPTY;
        if (stack == null) throw new NullPointerException("Empty ItemStack was null!");
        EMPTY = stack;
    }

    /** Checks to see if the two input stacks are equal in all but stack size. Note that this doesn't check anything
     * todo with stack size, so if you pass in two stacks of 64 cobblestone this will return true. If you pass in null
     * (at all) then this will only return true if both are null. */
    public static boolean canMerge(@NotNull ItemStack a, @NotNull ItemStack b) {
        // Checks item, damage
        if (!ItemStack.isSameItem(a, b)) {
            return false;
        }
        // checks tags
        return ItemStack.isSameItemSameComponents(a, b);
    }

    /** Checks to see if the given required stack is contained fully in the given container stack. */
    public static boolean contains(@NotNull ItemStack required, @NotNull ItemStack container) {
        if (canMerge(required, container)) {
            return container.getCount() >= required.getCount();
        }
        return false;
    }

    /** Checks to see if the given required stack is contained fully in a single stack in a list. */
    public static boolean contains(@NotNull ItemStack required, Collection<ItemStack> containers) {
        for (ItemStack possible : containers) {
            if (possible == null) {
                // Use an explicit null check here as the collection doesn't have @NotNull applied to its type
                throw new NullPointerException("Found a null itemstack in " + containers);
            }
            if (contains(required, possible)) {
                return true;
            }
        }
        return false;
    }

    /** Checks to see if the given required stacks are all contained within the collection of containers. Note that this
     * assumes that all of the required stacks are different. */
    public static boolean containsAll(Collection<ItemStack> required, Collection<ItemStack> containers) {
        for (ItemStack req : required) {
            if (req == null) {
                throw new NullPointerException("Found a null itemstack in " + containers);
            }
            if (req.isEmpty()) continue;
            if (!contains(req, containers)) {
                return false;
            }
        }
        return true;
    }

    public static CompoundTag stripNonFunctionNbt(@NotNull ItemStack from) {
        CompoundTag nbt = NBTUtilBC.getItemData(from).copy();
        if (nbt.isEmpty()) {
            return nbt;
        }
        nbt.remove("_data");
        return nbt;
    }

    public static boolean doesStackNbtMatch(@NotNull ItemStack target, @NotNull ItemStack with) {
        CompoundTag nbtTarget = stripNonFunctionNbt(target);
        CompoundTag nbtWith = stripNonFunctionNbt(with);
        return nbtTarget.equals(nbtWith);
    }

    /** Merges mergeSource into mergeTarget
     *
     * @param mergeSource - The stack to merge into mergeTarget, this stack is not modified
     * @param mergeTarget - The target merge, this stack is modified if doMerge is set
     * @param doMerge - To actually do the merge
     * @return The number of items that was successfully merged. */
    public static int mergeStacks(@NotNull ItemStack mergeSource, @NotNull ItemStack mergeTarget, boolean doMerge) {
        if (!canMerge(mergeSource, mergeTarget)) {
            return 0;
        }
        int mergeCount = Math.min(mergeTarget.getMaxStackSize() - mergeTarget.getCount(), mergeSource.getCount());
        if (mergeCount < 1) {
            return 0;
        }
        if (doMerge) {
            mergeTarget.setCount(mergeTarget.getCount() + mergeCount);
        }
        return mergeCount;
    }

    /** Takes a {@link Nullable} {@link Object} and checks to make sure that it is really {@link NotNull}, like it is
     * everywhere else in the codebase.
     *
     * @param obj The (potentially) null object.
     * @return A {@link NotNull} object, which will be the input object
     * @throws NullPointerException if the input object was actually null */
    @NotNull
    public static <T> T asNonNull(@Nullable T obj) {
        if (obj == null) {
            throw new NullPointerException("Object was null!");
        }
        return obj;
    }

    @NotNull
    public static <T> T asNonNullSoft(@Nullable T obj, @NotNull T fallback) {
        if (obj == null) {
            return fallback;
        } else {
            return obj;
        }
    }

    @NotNull
    public static ItemStack asNonNullSoft(@Nullable ItemStack stack) {
        return asNonNullSoft(stack, EMPTY);
    }

    /** Computes a hash code for the given {@link ItemStack}. This is based off of {@link ItemStack#save(CompoundTag)},
     * except if {@link ItemStack#isEmpty()} returns true, in which case the hash will be 0. */
    public static int hash(@NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return Objects.hash(stack.getItem(), stack.getComponents());
    }

    public static List<ItemStack> mergeSameItems(List<ItemStack> items) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack toAdd : items) {
            boolean found = false;
            for (ItemStack stack : stacks) {
                if (canMerge(stack, toAdd)) {
                    stack.grow(toAdd.getCount());
                    found = true;
                }
            }
            if (!found) {
                stacks.add(toAdd.copy());
            }
        }
        return stacks;
    }
}
