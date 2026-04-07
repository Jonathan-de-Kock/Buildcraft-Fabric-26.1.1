/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.transport.pipe.flow.PipeFlowItems;

/**
 * Void pipe behaviour. Destroys all items that pass through the pipe.
 * Each tick, any travelling items in the flow are removed.
 */
public class PipeBehaviourVoid extends PipeBehaviour {
    public PipeBehaviourVoid(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourVoid(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
    }

    @Override
    public void onTick() {
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null || level.isClientSide()) return;

        // Void all items currently in the flow
        if (pipe.getFlow() instanceof PipeFlowItems flow) {
            flow.voidAllItems();
        }
    }
}
