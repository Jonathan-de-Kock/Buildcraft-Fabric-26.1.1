/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core.tile;

import java.io.IOException;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.lib.engine.BlockEntityEngineBase;
import buildcraft.lib.net.PacketBufferBC;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Creative engine: adjustable-output engine for testing and creative mode.
 * Always burning, cycles through output levels when interacted with.
 *
 * <p>Output levels: 1, 2, 4, 8, 16, 32, 64, 128, 256 MJ equivalent
 * (250, 500, 1000, 2000, 4000, 8000, 16000, 32000, 64000 E).
 */
public class BlockEntityEngineCreative extends BlockEntityEngineBase {
    private static final long[] OUTPUT_LEVELS = {
        250, 500, 1000, 2000, 4000, 8000, 16000, 32000, 64000
    };

    private int outputIndex = 0;

    public BlockEntityEngineCreative(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Cycle to the next output level. Called by wrench interaction or similar. */
    public void cycleOutput() {
        outputIndex = (outputIndex + 1) % OUTPUT_LEVELS.length;
        setChanged();
        sendNetworkUpdate(NET_RENDER_DATA);
    }

    public int getOutputIndex() {
        return outputIndex;
    }

    @Override
    public boolean isBurning() {
        return true; // Always burning in creative
    }

    @Override
    public long getCurrentOutput() {
        return OUTPUT_LEVELS[outputIndex];
    }

    @Override
    public long getMaxPower() {
        return getCurrentOutput() * 10_000;
    }

    @Override
    public long getMaxExtract() {
        return getCurrentOutput() * 20;
    }

    @Override
    public EnumPowerStage getPowerStage() {
        return EnumPowerStage.BLACK; // Creative engines always show black
    }

    @Override
    protected float getPistonSpeed() {
        return 0.01f + (outputIndex * 0.008f);
    }

    // ##################
    //
    // Networking
    //
    // ##################

    @Override
    public void writePayload(int id, PacketBufferBC buffer, boolean isClient) {
        super.writePayload(id, buffer, isClient);
        if (id == NET_RENDER_DATA) {
            buffer.writeVarInt(outputIndex);
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, boolean isClient) throws IOException {
        super.readPayload(id, buffer, isClient);
        if (id == NET_RENDER_DATA) {
            outputIndex = buffer.readVarInt();
        }
    }

    // ##################
    //
    // NBT persistence
    //
    // ##################

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("outputIndex", outputIndex);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        outputIndex = input.getIntOr("outputIndex", 0);
        if (outputIndex < 0 || outputIndex >= OUTPUT_LEVELS.length) {
            outputIndex = 0;
        }
    }
}
