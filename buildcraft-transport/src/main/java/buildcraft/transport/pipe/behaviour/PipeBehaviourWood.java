/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.pipe.behaviour;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipe.ConnectedType;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.transport.pipe.flow.PipeFlowItems;

/**
 * Extraction pipe behaviour. When powered by redstone, extracts items from
 * an adjacent inventory. Does not connect to other wood pipes.
 *
 * Extraction direction cycles through connected tile entities (inventories).
 */
public class PipeBehaviourWood extends PipeBehaviour {
    private static final int EXTRACT_COUNT = 1;
    private static final int EXTRACT_COOLDOWN = 10; // ticks between extractions
    private int cooldown = 0;

    @Nullable
    private Direction extractionDir;

    public PipeBehaviourWood(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWood(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        if (nbt.contains("extractionDir")) {
            extractionDir = Direction.byName(nbt.getStringOr("extractionDir", ""));
        }
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        if (extractionDir != null) {
            nbt.putString("extractionDir", extractionDir.getName());
        }
        return nbt;
    }

    @Override
    public boolean canConnect(Direction face, PipeBehaviour other) {
        // Wood pipes don't connect to other wood pipes
        return !(other instanceof PipeBehaviourWood);
    }

    @Override
    public void onTick() {
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null || level.isClientSide()) return;

        // Only extract when redstone powered
        if (!level.hasNeighborSignal(pipe.getHolder().getPipePos())) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        // Find an extraction direction — must be connected to a TILE (inventory), not a pipe
        if (extractionDir == null || !isValidExtractionDir(extractionDir)) {
            extractionDir = findExtractionDir();
        }

        if (extractionDir != null && pipe.getFlow() instanceof PipeFlowItems flow) {
            flow.extractItems(extractionDir, EXTRACT_COUNT);
            cooldown = EXTRACT_COOLDOWN;
        }
    }

    private boolean isValidExtractionDir(Direction dir) {
        return pipe.isConnected(dir) && pipe.getConnectedType(dir) == ConnectedType.TILE;
    }

    @Nullable
    private Direction findExtractionDir() {
        for (Direction dir : Direction.values()) {
            if (isValidExtractionDir(dir)) {
                return dir;
            }
        }
        return null;
    }
}
