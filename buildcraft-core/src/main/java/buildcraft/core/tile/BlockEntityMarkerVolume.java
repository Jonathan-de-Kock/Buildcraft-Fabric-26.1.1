/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core.tile;

import java.io.IOException;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.BlockEntityBCBase;

public class BlockEntityMarkerVolume extends BlockEntityBCBase {
    protected static final IdAllocator IDS = BlockEntityBCBase.IDS.makeChild("marker_volume");

    /** The volume defined by this marker (if connected). Null means not connected. */
    private BlockPos min = null;
    private BlockPos max = null;

    public BlockEntityMarkerVolume(BlockPos pos, BlockState state) {
        super(BCCoreBlockEntities.MARKER_VOLUME, pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public boolean isConnected() {
        return min != null && max != null;
    }

    public BlockPos getVolumeMin() {
        return min;
    }

    public BlockPos getVolumeMax() {
        return max;
    }

    public void setVolume(BlockPos min, BlockPos max) {
        this.min = min;
        this.max = max;
        setChanged();
        sendNetworkUpdate(NET_RENDER_DATA);
    }

    public void clearVolume() {
        this.min = null;
        this.max = null;
        setChanged();
        sendNetworkUpdate(NET_RENDER_DATA);
    }

    // ##################
    //
    // Network
    //
    // ##################

    @Override
    public void writePayload(int id, PacketBufferBC buffer, boolean isClient) {
        super.writePayload(id, buffer, isClient);
        if (id == NET_RENDER_DATA) {
            buffer.writeBoolean(isConnected());
            if (isConnected()) {
                buffer.writeBlockPos(min);
                buffer.writeBlockPos(max);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, boolean isClient) throws IOException {
        super.readPayload(id, buffer, isClient);
        if (id == NET_RENDER_DATA) {
            if (buffer.readBoolean()) {
                min = buffer.readBlockPos();
                max = buffer.readBlockPos();
            } else {
                min = null;
                max = null;
            }
        }
    }

    // ##################
    //
    // NBT
    //
    // ##################

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (isConnected()) {
            output.putBoolean("connected", true);
            output.putInt("minX", min.getX());
            output.putInt("minY", min.getY());
            output.putInt("minZ", min.getZ());
            output.putInt("maxX", max.getX());
            output.putInt("maxY", max.getY());
            output.putInt("maxZ", max.getZ());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if (input.getBooleanOr("connected", false)) {
            min = new BlockPos(
                input.getIntOr("minX", 0),
                input.getIntOr("minY", 0),
                input.getIntOr("minZ", 0)
            );
            max = new BlockPos(
                input.getIntOr("maxX", 0),
                input.getIntOr("maxY", 0),
                input.getIntOr("maxZ", 0)
            );
        } else {
            min = null;
            max = null;
        }
    }
}
