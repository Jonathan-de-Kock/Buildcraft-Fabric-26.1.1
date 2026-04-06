/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.net;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Fabric replacement for the old MessageUpdateTile.
 * Carries a BlockPos and a binary payload to/from block entities.
 */
public record BlockEntityUpdatePayload(BlockPos pos, byte[] data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BlockEntityUpdatePayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("buildcraftlib", "tile_update"));

    public static final StreamCodec<FriendlyByteBuf, BlockEntityUpdatePayload> STREAM_CODEC =
        StreamCodec.of(BlockEntityUpdatePayload::write, BlockEntityUpdatePayload::read);

    private static BlockEntityUpdatePayload read(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int size = buf.readMedium();
        byte[] data = new byte[size];
        buf.readBytes(data);
        return new BlockEntityUpdatePayload(pos, data);
    }

    private static void write(FriendlyByteBuf buf, BlockEntityUpdatePayload payload) {
        buf.writeBlockPos(payload.pos);
        buf.writeMedium(payload.data.length);
        buf.writeBytes(payload.data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Create a payload from a writer function. The writer receives a PacketBufferBC
     * and writes the id + data into it, then the resulting bytes are stored in the payload.
     */
    public static BlockEntityUpdatePayload create(BlockPos pos, IPayloadWriter writer) {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
        writer.write(buffer);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        buffer.release();
        return new BlockEntityUpdatePayload(pos, bytes);
    }

    /** Get the payload as a PacketBufferBC for reading. Caller should release the buffer when done. */
    public PacketBufferBC toBuffer() {
        return new PacketBufferBC(Unpooled.wrappedBuffer(data));
    }
}
