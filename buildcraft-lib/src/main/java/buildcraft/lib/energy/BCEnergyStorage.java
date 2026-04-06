/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.energy;

import net.minecraft.nbt.CompoundTag;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;

/**
 * A simple {@link EnergyStorage} implementation for BuildCraft blocks and items.
 * Replaces the old MJ battery system with Team Reborn Energy (Fabric).
 */
public class BCEnergyStorage implements EnergyStorage {
    private long stored;
    private final long capacity;
    private final long maxInsert;
    private final long maxExtract;

    public BCEnergyStorage(long capacity, long maxInsert, long maxExtract) {
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
        this.stored = 0;
    }

    public BCEnergyStorage(long capacity, long maxTransfer) {
        this(capacity, maxTransfer, maxTransfer);
    }

    public BCEnergyStorage(long capacity) {
        this(capacity, capacity, capacity);
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        long toInsert = Math.min(maxAmount, Math.min(maxInsert, capacity - stored));
        if (toInsert <= 0) {
            return 0;
        }
        transaction.addCloseCallback((context, result) -> {
            if (result.wasCommitted()) {
                stored += toInsert;
            }
        });
        return toInsert;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        long toExtract = Math.min(maxAmount, Math.min(maxExtract, stored));
        if (toExtract <= 0) {
            return 0;
        }
        transaction.addCloseCallback((context, result) -> {
            if (result.wasCommitted()) {
                stored -= toExtract;
            }
        });
        return toExtract;
    }

    @Override
    public long getAmount() {
        return stored;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    /**
     * Sets the stored energy directly. Useful for initialization or syncing.
     */
    public void setStored(long amount) {
        this.stored = Math.max(0, Math.min(amount, capacity));
    }

    /**
     * @return The maximum amount of energy that can be inserted per transaction.
     */
    public long getMaxInsert() {
        return maxInsert;
    }

    /**
     * @return The maximum amount of energy that can be extracted per transaction.
     */
    public long getMaxExtract() {
        return maxExtract;
    }

    /**
     * Saves this energy storage to an NBT compound tag.
     */
    public CompoundTag writeToNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("stored", stored);
        return nbt;
    }

    /**
     * Saves this energy storage into an existing NBT compound tag under a sub-key.
     */
    public void writeToNbt(CompoundTag nbt, String key) {
        nbt.put(key, writeToNbt());
    }

    /**
     * Loads this energy storage from an NBT compound tag.
     */
    public void readFromNbt(CompoundTag nbt) {
        stored = Math.max(0, Math.min(nbt.getLongOr("stored", 0L), capacity));
    }

    /**
     * Loads this energy storage from an existing NBT compound tag under a sub-key.
     */
    public void readFromNbt(CompoundTag nbt, String key) {
        if (nbt.contains(key)) {
            readFromNbt(nbt.getCompoundOrEmpty(key));
        }
    }
}
