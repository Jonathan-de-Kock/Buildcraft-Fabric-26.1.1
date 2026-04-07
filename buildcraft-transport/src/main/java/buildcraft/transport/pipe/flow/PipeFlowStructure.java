/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.pipe.flow;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeFlow;

/**
 * Empty flow for structural pipes. These pipes only connect to other structure pipes
 * and do not transport anything. They are used for cosmetic connections and support.
 */
public class PipeFlowStructure extends PipeFlow {
    public PipeFlowStructure(IPipe pipe) {
        super(pipe);
    }

    public PipeFlowStructure(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
    }

    @Override
    public boolean canConnect(Direction face, PipeFlow other) {
        return other instanceof PipeFlowStructure;
    }

    @Override
    public boolean canConnect(Direction face, BlockEntity oTile) {
        // Structure pipes do not connect to any block entities
        return false;
    }
}
