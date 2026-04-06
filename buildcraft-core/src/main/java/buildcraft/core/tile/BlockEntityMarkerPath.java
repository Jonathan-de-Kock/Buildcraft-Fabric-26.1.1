/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.BlockEntityBCBase;

public class BlockEntityMarkerPath extends BlockEntityBCBase {
    protected static final IdAllocator IDS = BlockEntityBCBase.IDS.makeChild("marker_path");

    private final List<BlockPos> path = new ArrayList<>();

    public BlockEntityMarkerPath(BlockPos pos, BlockState state) {
        super(BCCoreBlockEntities.MARKER_PATH, pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public List<BlockPos> getPath() {
        return path;
    }

    public void setPath(List<BlockPos> newPath) {
        path.clear();
        path.addAll(newPath);
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
            buffer.writeVarInt(path.size());
            for (BlockPos p : path) {
                buffer.writeBlockPos(p);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, boolean isClient) throws IOException {
        super.readPayload(id, buffer, isClient);
        if (id == NET_RENDER_DATA) {
            path.clear();
            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                path.add(buffer.readBlockPos());
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
        output.putInt("pathSize", path.size());
        for (int i = 0; i < path.size(); i++) {
            output.putInt("px" + i, path.get(i).getX());
            output.putInt("py" + i, path.get(i).getY());
            output.putInt("pz" + i, path.get(i).getZ());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        path.clear();
        int size = input.getIntOr("pathSize", 0);
        for (int i = 0; i < size; i++) {
            path.add(new BlockPos(
                input.getIntOr("px" + i, 0),
                input.getIntOr("py" + i, 0),
                input.getIntOr("pz" + i, 0)
            ));
        }
    }
}
