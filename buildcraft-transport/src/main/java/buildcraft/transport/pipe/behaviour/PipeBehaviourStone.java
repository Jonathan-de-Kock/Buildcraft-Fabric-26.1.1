/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.CompoundTag;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;

/**
 * Basic passthrough pipe behaviour. Items pass through without any special logic.
 * Used by stone and cobblestone pipes.
 */
public class PipeBehaviourStone extends PipeBehaviour {
    public PipeBehaviourStone(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourStone(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
    }
}
