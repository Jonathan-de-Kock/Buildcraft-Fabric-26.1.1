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
 * Basic passthrough pipe behaviour, identical to stone. Cobblestone pipes are the
 * cheapest tier and do not connect to stone pipes (in the original BC). For this
 * simplified port they connect to everything.
 */
public class PipeBehaviourCobble extends PipeBehaviour {
    public PipeBehaviourCobble(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourCobble(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
    }
}
