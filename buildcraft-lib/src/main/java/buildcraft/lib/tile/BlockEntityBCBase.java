/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.tile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.api.core.IPlayerOwned;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.BCNetworking;
import buildcraft.lib.net.BlockEntityUpdatePayload;
import buildcraft.lib.net.IPayloadReceiver;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;

/**
 * Base block entity class for all BuildCraft block entities.
 * Replaces the old TileBC_Neptune from Forge.
 *
 * Provides:
 * - ID-based network message dispatch (render data, gui data, gui tick, deltas)
 * - Owner tracking (GameProfile)
 * - Player tracking for open GUIs
 * - NBT serialization framework
 */
public abstract class BlockEntityBCBase extends BlockEntity implements IPayloadReceiver, IPlayerOwned {

    protected static final IdAllocator IDS = new IdAllocator("tile");

    /** Used for sending all data used for rendering the tile on a client. */
    public static final int NET_RENDER_DATA = IDS.allocId("RENDER_DATA");
    /** Used for sending all data in the GUI. */
    public static final int NET_GUI_DATA = IDS.allocId("GUI_DATA");
    /** Used for sending per-tick GUI changes. */
    public static final int NET_GUI_TICK = IDS.allocId("GUI_TICK");

    public static final int NET_REN_DELTA_SINGLE = IDS.allocId("REN_DELTA_SINGLE");
    public static final int NET_REN_DELTA_CLEAR = IDS.allocId("REN_DELTA_CLEAR");
    public static final int NET_GUI_DELTA_SINGLE = IDS.allocId("GUI_DELTA_SINGLE");
    public static final int NET_GUI_DELTA_CLEAR = IDS.allocId("GUI_DELTA_CLEAR");

    /** Used to tell the client to redraw the block. */
    public static final int NET_REDRAW = IDS.allocId("REDRAW");

    private final Set<Player> usingPlayers = Sets.newIdentityHashSet();
    private GameProfile owner;

    public BlockEntityBCBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ##################
    //
    // Local state getters
    //
    // ##################

    public final BlockState getCurrentState() {
        return level != null ? level.getBlockState(worldPosition) : getBlockState();
    }

    public final BlockState getNeighbourState(Direction offset) {
        return level != null ? level.getBlockState(worldPosition.relative(offset)) : getBlockState();
    }

    @Nullable
    public final BlockEntity getNeighbourTile(Direction offset) {
        return level != null ? level.getBlockEntity(worldPosition.relative(offset)) : null;
    }

    /** @return The {@link IdAllocator} for this class hierarchy. Subclasses should override if they allocate IDs. */
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    // ##################
    //
    // Lifecycle
    //
    // ##################

    /** Called when the block holding this tile is removed. Drop items, clean up. */
    public void onRemove() {
        NonNullList<ItemStack> toDrop = NonNullList.create();
        addDrops(toDrop, 0);
        if (level != null) {
            for (ItemStack stack : toDrop) {
                net.minecraft.world.Containers.dropItemStack(level,
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
            }
        }
    }

    /** Override to add items that should be dropped when the block is broken. */
    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
        // Subclasses add their items/tank contents here
    }

    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        if (level != null && !level.isClientSide()) {
            if (placer instanceof Player player) {
                owner = player.getGameProfile();
            }
        }
    }

    public void onPlayerOpen(Player player) {
        if (owner == null) {
            owner = player.getGameProfile();
        }
        sendNetworkUpdate(NET_GUI_DATA, player);
        usingPlayers.add(player);
    }

    public void onPlayerClose(Player player) {
        usingPlayers.remove(player);
    }

    public boolean onActivated(Player player, InteractionHand hand, Direction facing,
                               float hitX, float hitY, float hitZ) {
        return false;
    }

    public void onNeighbourBlockChanged() {
        // Override in subclasses for cache invalidation etc.
    }

    // ##################
    //
    // Owner / Permissions
    //
    // ##################

    @Override
    public GameProfile getOwner() {
        return owner;
    }

    protected boolean hasOwner() {
        return owner != null;
    }

    public boolean canPlayerEdit(Player player) {
        // Simple distance + owner check
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
            worldPosition.getZ() + 0.5) <= 64.0;
    }

    public boolean canInteractWith(Player player) {
        return canPlayerEdit(player);
    }

    // ##################
    //
    // Network helpers
    //
    // ##################

    /** Tells MC to redraw this block. */
    public final void redrawBlock() {
        if (level != null) {
            if (level.isClientSide()) {
                BlockState state = level.getBlockState(worldPosition);
                level.sendBlockUpdated(worldPosition, state, state, 0);
            } else {
                sendNetworkUpdate(NET_REDRAW);
            }
        }
    }

    /** Send a network update of the specified ID to all watching players (or to server if client). */
    public final void sendNetworkUpdate(int id) {
        if (level == null) return;
        BlockEntityUpdatePayload payload = createNetworkPayload(id);
        if (payload == null) return;
        if (level.isClientSide()) {
            BCNetworking.sendToServer(payload);
        } else {
            BCNetworking.sendToAllWatching(this, payload);
        }
    }

    /** Send a network update to a specific player. */
    public final void sendNetworkUpdate(int id, Player target) {
        if (level != null && target instanceof ServerPlayer sp) {
            BlockEntityUpdatePayload payload = createNetworkPayload(id);
            if (payload != null) {
                BCNetworking.sendTo(sp, payload);
            }
        }
    }

    /** Send a GUI tick update to a specific player. Only sent if the payload has actual data. */
    public final void sendNetworkGuiTick(Player player) {
        if (level != null && !level.isClientSide() && player instanceof ServerPlayer sp) {
            BlockEntityUpdatePayload payload = createNetworkPayload(NET_GUI_TICK);
            if (payload != null && payload.data().length > 2) {
                BCNetworking.sendTo(sp, payload);
            }
        }
    }

    /** Send a GUI update to all players with an open GUI. */
    public final void sendNetworkGuiUpdate(int id) {
        if (level == null) return;
        for (Player player : usingPlayers) {
            sendNetworkUpdate(id, player);
        }
    }

    @Nullable
    public final BlockEntityUpdatePayload createNetworkPayload(int id) {
        if (level == null) return null;
        boolean isClient = level.isClientSide();
        return BlockEntityUpdatePayload.create(worldPosition, buffer -> {
            buffer.writeShort(id);
            writePayload(id, buffer, isClient);
        });
    }

    /** Create and send a message with custom writer to all watchers (or server). */
    public final void createAndSendMessage(int id, IPayloadWriter writer) {
        if (level == null) return;
        BlockEntityUpdatePayload payload = BlockEntityUpdatePayload.create(worldPosition, buffer -> {
            buffer.writeShort(id);
            writer.write(buffer);
        });
        if (level.isClientSide()) {
            BCNetworking.sendToServer(payload);
        } else {
            BCNetworking.sendToAllWatching(this, payload);
        }
    }

    /** Create and send a message to all players with an open GUI. */
    public final void createAndSendGuiMessage(int id, IPayloadWriter writer) {
        if (level == null) return;
        BlockEntityUpdatePayload payload = BlockEntityUpdatePayload.create(worldPosition, buffer -> {
            buffer.writeShort(id);
            writer.write(buffer);
        });
        if (level.isClientSide()) {
            BCNetworking.sendToServer(payload);
        } else {
            for (Player player : usingPlayers) {
                if (player instanceof ServerPlayer sp) {
                    BCNetworking.sendTo(sp, payload);
                }
            }
        }
    }

    // ##################
    //
    // Vanilla sync packet
    //
    // ##################

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        // Embed render data as byte array
        PacketBufferBC buf = new PacketBufferBC(Unpooled.buffer());
        buf.writeShort(NET_RENDER_DATA);
        writePayload(NET_RENDER_DATA, buf, false);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        buf.release();
        nbt.putByteArray("d", bytes);
        return nbt;
    }

    /**
     * Called on the client when an update tag is received (e.g., on chunk load).
     * In MC 26.1, handleUpdateTag is no longer an override on BlockEntity.
     * We process it via the loadAdditional pathway instead.
     */
    public void handleUpdateTag(CompoundTag tag) {
        if (!tag.contains("d")) return;
        byte[] bytes = tag.getByteArray("d").orElse(new byte[0]);
        if (bytes.length < 2) return;
        PacketBufferBC buf = new PacketBufferBC(Unpooled.wrappedBuffer(bytes));
        try {
            int id = buf.readUnsignedShort();
            readPayload(id, buf, true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read update tag!", e);
        } finally {
            buf.release();
        }
    }

    // ##################
    //
    // IPayloadReceiver
    //
    // ##################

    @Override
    public void receivePayload(@Nullable ServerPlayer player, PacketBufferBC buffer) throws IOException {
        int id = buffer.readUnsignedShort();
        boolean isClient = player == null; // null player means client-side receive
        readPayload(id, buffer, isClient);
    }

    // ##################
    //
    // Network overridables
    //
    // ##################

    /**
     * Write payload data for the given message ID.
     * @param id The message ID
     * @param buffer The buffer to write to
     * @param isClient True if writing on the client side
     */
    public void writePayload(int id, PacketBufferBC buffer, boolean isClient) {
        if (id == NET_GUI_DATA) {
            writePayload(NET_RENDER_DATA, buffer, isClient);
            if (!isClient) {
                // Write owner from server
                writeGameProfile(buffer, owner);
            }
        }
    }

    /**
     * Read payload data for the given message ID.
     * @param id The message ID
     * @param buffer The buffer to read from
     * @param isClient True if reading on the client side
     */
    public void readPayload(int id, PacketBufferBC buffer, boolean isClient) throws IOException {
        if (id == NET_GUI_DATA) {
            readPayload(NET_RENDER_DATA, buffer, isClient);
            if (isClient) {
                owner = readGameProfile(buffer);
            }
        } else if (id == NET_REDRAW && isClient) {
            redrawBlock();
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
        if (owner != null) {
            ValueOutput ownerOutput = output.child("owner");
            ownerOutput.putString("name", owner.name() != null ? owner.name() : "");
            ownerOutput.putString("id", owner.id() != null ? owner.id().toString() : "");
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("owner").ifPresent(ownerInput -> {
            String name = ownerInput.getStringOr("name", "");
            String idStr = ownerInput.getStringOr("id", "");
            UUID id = idStr.isEmpty() ? null : UUID.fromString(idStr);
            if (id != null || !name.isEmpty()) {
                owner = new GameProfile(id, name.isEmpty() ? null : name);
            }
        });
    }

    // ##################
    //
    // GameProfile helpers
    //
    // ##################

    public static void writeGameProfile(PacketBufferBC buffer, @Nullable GameProfile profile) {
        buffer.writeBoolean(profile != null);
        if (profile != null) {
            buffer.writeBoolean(profile.id() != null);
            if (profile.id() != null) {
                buffer.writeUUID(profile.id());
            }
            buffer.writeBoolean(profile.name() != null);
            if (profile.name() != null) {
                buffer.writeUtf(profile.name());
            }
        }
    }

    @Nullable
    public static GameProfile readGameProfile(PacketBufferBC buffer) {
        if (buffer.readBoolean()) {
            UUID id = buffer.readBoolean() ? buffer.readUUID() : null;
            String name = buffer.readBoolean() ? buffer.readUtf() : null;
            if (id != null || name != null) {
                return new GameProfile(id, name);
            }
        }
        return null;
    }

    // ##################
    //
    // Utility
    //
    // ##################

    /** Cheaper version of markDirty that only flags the chunk for save. */
    public void markChunkDirty() {
        if (level != null) {
            level.blockEntityChanged(worldPosition);
        }
    }

    protected Set<Player> getUsingPlayers() {
        return usingPlayers;
    }
}
