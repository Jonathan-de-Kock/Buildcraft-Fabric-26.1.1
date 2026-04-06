/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.fluid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Manages multiple {@link BCTank} instances for a block entity.
 * Handles serialization (both CompoundTag and ValueInput/ValueOutput),
 * network sync, and drop management.
 */
public class BCTankManager {
    private final List<BCTank> tanks = new ArrayList<>();

    public BCTankManager() {}

    /** Add a tank to this manager. Returns {@code this} for chaining. */
    public BCTankManager add(BCTank tank) {
        tanks.add(tank);
        return this;
    }

    public List<BCTank> getTanks() {
        return Collections.unmodifiableList(tanks);
    }

    public BCTank get(int index) {
        return tanks.get(index);
    }

    public int size() {
        return tanks.size();
    }

    // ##################
    //
    // ValueInput/ValueOutput persistence (MC 26.1)
    //
    // ##################

    /** Save all tanks via ValueOutput. Each tank is stored as a child keyed by its name. */
    public void save(ValueOutput output) {
        for (BCTank tank : tanks) {
            if (!tank.isEmpty()) {
                ValueOutput tankOutput = output.child(tank.getName());
                tank.save(tankOutput);
            }
        }
    }

    /** Load all tanks from ValueInput. Each tank reads from a child keyed by its name. */
    public void load(ValueInput input) {
        for (BCTank tank : tanks) {
            input.child(tank.getName()).ifPresent(tank::load);
        }
    }

    // ##################
    //
    // Network serialization
    //
    // ##################

    /** Write all tank data to a network buffer. */
    public void writeData(FriendlyByteBuf buf) {
        for (BCTank tank : tanks) {
            tank.writeToBuffer(buf);
        }
    }

    /** Read all tank data from a network buffer. */
    public void readData(FriendlyByteBuf buf) {
        for (BCTank tank : tanks) {
            tank.readFromBuffer(buf);
        }
    }

    // ##################
    //
    // Drops
    //
    // ##################

    /**
     * Add items to the drop list when the block is broken.
     * By default, fluids in tanks are lost when the block is broken.
     * Subclasses can override individual tank behavior to produce fluid containers.
     */
    public void addDrops(NonNullList<ItemStack> toDrop) {
        // Fluids are lost unless subclass overrides to create fluid containers
    }
}
