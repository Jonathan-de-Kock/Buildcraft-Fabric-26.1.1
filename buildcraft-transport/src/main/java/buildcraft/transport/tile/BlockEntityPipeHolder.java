/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.tile;

import java.io.IOException;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.api.transport.pipe.*;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.BlockEntityBCBase;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.PipeRegistryImpl;

/**
 * Block entity that holds a single {@link Pipe} instance. Implements {@link IPipeHolder}
 * to bridge between the pipe system and the Minecraft block entity lifecycle.
 */
public class BlockEntityPipeHolder extends BlockEntityBCBase implements IPipeHolder {
    protected static final IdAllocator IDS = BlockEntityBCBase.IDS.makeChild("pipe_holder");
    public static final int NET_PIPE_DATA = IDS.allocId("PIPE_DATA");

    @Nullable
    private Pipe pipe;
    private boolean needsConnectionUpdate = true;

    public BlockEntityPipeHolder(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Initialize the pipe in this holder. Called when the block is placed. */
    public void initPipe(PipeDefinition definition) {
        this.pipe = new Pipe(this, definition);
        this.needsConnectionUpdate = true;
    }

    // ==================
    // Tick
    // ==================

    public void tick() {
        if (level == null || pipe == null) return;
        if (needsConnectionUpdate) {
            pipe.updateConnections();
            needsConnectionUpdate = false;
            if (!level.isClientSide()) {
                sendNetworkUpdate(NET_RENDER_DATA);
            }
        }
        if (!level.isClientSide()) {
            pipe.onTick();
        }
    }

    @Override
    public void onNeighbourBlockChanged() {
        super.onNeighbourBlockChanged();
        needsConnectionUpdate = true;
    }

    // ==================
    // NBT
    // ==================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (pipe != null) {
            output.putString("pipe_id", pipe.definition.identifier.toString());
            output.store("pipe_data", CompoundTag.CODEC, pipe.writeToNbt());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String pipeIdStr = input.getStringOr("pipe_id", "");
        if (!pipeIdStr.isEmpty()) {
            Identifier pipeId = Identifier.parse(pipeIdStr);
            PipeDefinition def = PipeRegistryImpl.INSTANCE.getDefinition(pipeId);
            if (def != null) {
                CompoundTag pipeData = input.read("pipe_data", CompoundTag.CODEC)
                    .orElse(new CompoundTag());
                pipe = new Pipe(this, def, pipeData);
                needsConnectionUpdate = true;
            }
        }
    }

    // ==================
    // Networking
    // ==================

    @Override
    public void writePayload(int id, PacketBufferBC buffer, boolean isClient) {
        super.writePayload(id, buffer, isClient);
        if (id == NET_RENDER_DATA && pipe != null) {
            buffer.writeUtf(pipe.definition.identifier.toString());
            pipe.writePayload(buffer, isClient);
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, boolean isClient) throws IOException {
        super.readPayload(id, buffer, isClient);
        if (id == NET_RENDER_DATA) {
            String pipeIdStr = buffer.readUtf();
            Identifier pipeId = Identifier.parse(pipeIdStr);
            PipeDefinition def = PipeRegistryImpl.INSTANCE.getDefinition(pipeId);
            if (def != null) {
                if (pipe == null || !pipe.definition.identifier.equals(pipeId)) {
                    pipe = new Pipe(this, def);
                }
                pipe.readPayload(buffer, isClient);
            }
        }
    }

    // ==================
    // IPipeHolder implementation
    // ==================

    @Override
    public Level getPipeWorld() {
        return level;
    }

    @Override
    public BlockPos getPipePos() {
        return worldPosition;
    }

    @Override
    public BlockEntity getPipeTile() {
        return this;
    }

    @Override
    public IPipe getPipe() {
        return pipe;
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        return canInteractWith(player);
    }

    // getNeighbourTile(Direction) is inherited from BlockEntityBCBase as final
    // and satisfies the IPipeHolder interface requirement.

    @Override
    @Nullable
    public IPipe getNeighbourPipe(Direction side) {
        BlockEntity be = getNeighbourTile(side);
        if (be instanceof IPipeHolder pipeHolder) {
            return pipeHolder.getPipe();
        }
        return null;
    }

    @Override
    public GameProfile getOwner() {
        return super.getOwner();
    }

    @Override
    public boolean fireEvent(PipeEvent event) {
        if (pipe != null) {
            return pipe.fireEvent(event);
        }
        return false;
    }

    @Override
    public void scheduleRenderUpdate() {
        redrawBlock();
    }

    @Override
    public void scheduleNetworkUpdate(PipeMessageReceiver... parts) {
        // Simplified: just send full render data when any part requests an update
        sendNetworkUpdate(NET_RENDER_DATA);
    }

    @Override
    public void scheduleNetworkGuiUpdate(PipeMessageReceiver... parts) {
        sendNetworkGuiUpdate(NET_GUI_DATA);
    }

    @Override
    public void sendMessage(PipeMessageReceiver to, IWriter writer) {
        // Simplified: wrap as a network update
        createAndSendMessage(NET_PIPE_DATA, buffer -> {
            buffer.writeByte(to.ordinal());
            writer.write(buffer);
        });
    }

    @Override
    public void sendGuiMessage(PipeMessageReceiver to, IWriter writer) {
        createAndSendGuiMessage(NET_PIPE_DATA, buffer -> {
            buffer.writeByte(to.ordinal());
            writer.write(buffer);
        });
    }

    @Override
    public void onPlayerOpen(Player player) {
        super.onPlayerOpen(player);
    }

    @Override
    public void onPlayerClose(Player player) {
        super.onPlayerClose(player);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }
}
