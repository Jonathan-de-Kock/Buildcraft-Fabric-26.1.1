/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.net;

import java.io.IOException;

import io.netty.buffer.Unpooled;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.lib.BCLib;

/**
 * Central networking registration for BuildCraft.
 * Replaces the old MessageManager with Fabric's networking API.
 */
public final class BCNetworking {

    private BCNetworking() {}

    /** Register all BuildCraft payload types. Call from BCLib.onInitialize(). */
    public static void registerPayloads() {
        PayloadTypeRegistry.serverboundPlay().register(
            BlockEntityUpdatePayload.TYPE, BlockEntityUpdatePayload.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            BlockEntityUpdatePayload.TYPE, BlockEntityUpdatePayload.STREAM_CODEC
        );
    }

    /** Register server-side handlers. Call from BCLib.onInitialize(). */
    public static void registerServerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(BlockEntityUpdatePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                BlockEntity tile = player.level().getBlockEntity(payload.pos());
                if (tile instanceof IPayloadReceiver receiver) {
                    PacketBufferBC buffer = payload.toBuffer();
                    try {
                        receiver.receivePayload(player, buffer);
                    } catch (IOException e) {
                        BCLib.LOGGER.error("Failed to read payload for tile at {}", payload.pos(), e);
                    } finally {
                        buffer.release();
                    }
                }
            });
        });
    }

    /** Register client-side handlers. Call from BCLibClient.onInitializeClient(). */
    public static void registerClientHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(BlockEntityUpdatePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                BlockEntity tile = context.player().level().getBlockEntity(payload.pos());
                if (tile instanceof IPayloadReceiver receiver) {
                    PacketBufferBC buffer = payload.toBuffer();
                    try {
                        receiver.receivePayload(null, buffer);
                    } catch (IOException e) {
                        BCLib.LOGGER.error("Failed to read payload for tile at {}", payload.pos(), e);
                    } finally {
                        buffer.release();
                    }
                }
            });
        });
    }

    /** Send a block entity update to all players watching the block. */
    public static void sendToAllWatching(BlockEntity tile, BlockEntityUpdatePayload payload) {
        if (tile.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                if (player.distanceToSqr(
                    tile.getBlockPos().getX() + 0.5,
                    tile.getBlockPos().getY() + 0.5,
                    tile.getBlockPos().getZ() + 0.5
                ) < 64 * 64) {
                    ServerPlayNetworking.send(player, payload);
                }
            }
        }
    }

    /** Send a block entity update to a specific player. */
    public static void sendTo(ServerPlayer player, BlockEntityUpdatePayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    /** Send a block entity update to the server (from client). */
    public static void sendToServer(BlockEntityUpdatePayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
