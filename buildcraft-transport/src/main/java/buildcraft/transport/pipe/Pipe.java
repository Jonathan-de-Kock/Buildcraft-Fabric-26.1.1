/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.pipe;

import java.util.EnumMap;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.api.transport.pipe.*;
import buildcraft.api.transport.pipe.IPipe.ConnectedType;
import buildcraft.lib.misc.NBTUtilBC;

/**
 * Core pipe composition object. Holds a {@link PipeDefinition}, {@link PipeBehaviour},
 * {@link PipeFlow}, and connection state for each direction.
 *
 * Simplified port of the original BuildCraft Pipe class.
 */
public class Pipe implements IPipe {
    public final IPipeHolder holder;
    public final PipeDefinition definition;
    public final PipeBehaviour behaviour;
    public final PipeFlow flow;
    private final PipeEventBus eventBus = new PipeEventBus();

    @Nullable
    private DyeColor colour;

    private final EnumMap<Direction, Boolean> connected = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, ConnectedType> connectedTypes = new EnumMap<>(Direction.class);

    /** Create a brand-new pipe (no saved data). */
    public Pipe(IPipeHolder holder, PipeDefinition definition) {
        this.holder = holder;
        this.definition = definition;
        this.behaviour = definition.logicConstructor.createBehaviour(this);
        this.flow = definition.flowType.creator.createFlow(this);
        for (Direction dir : Direction.values()) {
            connected.put(dir, false);
            connectedTypes.put(dir, ConnectedType.PIPE);
        }
        eventBus.registerHandler(behaviour);
        eventBus.registerHandler(flow);
    }

    /** Load a pipe from NBT. */
    public Pipe(IPipeHolder holder, PipeDefinition definition, CompoundTag nbt) {
        this.holder = holder;
        this.definition = definition;
        CompoundTag behaviourTag = nbt.getCompoundOrEmpty("behaviour");
        this.behaviour = definition.logicLoader.loadBehaviour(this, behaviourTag);
        CompoundTag flowTag = nbt.getCompoundOrEmpty("flow");
        this.flow = definition.flowType.loader.loadFlow(this, flowTag);
        this.colour = NBTUtilBC.readEnum(nbt.get("colour"), DyeColor.class);
        for (Direction dir : Direction.values()) {
            String key = "conn_" + dir.getName();
            connected.put(dir, nbt.getBooleanOr(key, false));
            String typeKey = "conn_type_" + dir.getName();
            ConnectedType ct = NBTUtilBC.readEnum(nbt.get(typeKey), ConnectedType.class);
            connectedTypes.put(dir, ct != null ? ct : ConnectedType.PIPE);
        }
        eventBus.registerHandler(behaviour);
        eventBus.registerHandler(flow);
    }

    public CompoundTag writeToNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("behaviour", behaviour.writeToNbt());
        nbt.put("flow", flow.writeToNbt());
        if (colour != null) {
            nbt.put("colour", NBTUtilBC.writeEnum(colour));
        }
        for (Direction dir : Direction.values()) {
            nbt.putBoolean("conn_" + dir.getName(), connected.getOrDefault(dir, false));
            nbt.put("conn_type_" + dir.getName(),
                NBTUtilBC.writeEnum(connectedTypes.getOrDefault(dir, ConnectedType.PIPE)));
        }
        return nbt;
    }

    public void writePayload(FriendlyByteBuf buffer, boolean isClient) {
        // Colour
        buffer.writeBoolean(colour != null);
        if (colour != null) {
            buffer.writeByte(colour.ordinal());
        }
        // Connections
        for (Direction dir : Direction.values()) {
            buffer.writeBoolean(connected.getOrDefault(dir, false));
            buffer.writeByte(connectedTypes.getOrDefault(dir, ConnectedType.PIPE).ordinal());
        }
        behaviour.writePayload(buffer, isClient);
        flow.writePayload(PipeFlow.NET_ID_FULL_STATE, buffer, isClient);
    }

    public void readPayload(FriendlyByteBuf buffer, boolean isClient) {
        // Colour
        if (buffer.readBoolean()) {
            int ordinal = buffer.readByte();
            DyeColor[] values = DyeColor.values();
            colour = (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : null;
        } else {
            colour = null;
        }
        // Connections
        for (Direction dir : Direction.values()) {
            connected.put(dir, buffer.readBoolean());
            int typeOrdinal = buffer.readByte();
            ConnectedType[] types = ConnectedType.values();
            connectedTypes.put(dir, (typeOrdinal >= 0 && typeOrdinal < types.length)
                ? types[typeOrdinal] : ConnectedType.PIPE);
        }
        behaviour.readPayload(buffer, isClient);
        flow.readPayload(PipeFlow.NET_ID_FULL_STATE, buffer, isClient);
    }

    /** Update connection state by checking neighbours. */
    public void updateConnections() {
        for (Direction dir : Direction.values()) {
            IPipe neighbour = holder.getNeighbourPipe(dir);
            if (neighbour != null) {
                // Check both sides agree to connect
                if (canConnect(dir, neighbour) && canPeerConnect(dir, neighbour)) {
                    connected.put(dir, true);
                    connectedTypes.put(dir, ConnectedType.PIPE);
                    continue;
                }
            }
            BlockEntity tile = holder.getNeighbourTile(dir);
            if (tile != null && !(tile instanceof IPipeHolder)) {
                if (canConnectToTile(dir, tile)) {
                    connected.put(dir, true);
                    connectedTypes.put(dir, ConnectedType.TILE);
                    continue;
                }
            }
            connected.put(dir, false);
        }
    }

    private boolean canConnect(Direction dir, IPipe other) {
        if (other.getColour() != null && colour != null && other.getColour() != colour) {
            return false;
        }
        return behaviour.canConnect(dir, other.getBehaviour())
            && flow.canConnect(dir, other.getFlow());
    }

    private boolean canPeerConnect(Direction dir, IPipe other) {
        Direction opposite = dir.getOpposite();
        return other.getBehaviour().canConnect(opposite, behaviour)
            && other.getFlow().canConnect(opposite, flow);
    }

    private boolean canConnectToTile(Direction dir, BlockEntity tile) {
        return behaviour.canConnect(dir, tile) || behaviour.shouldForceConnection(dir, tile)
            || flow.canConnect(dir, tile) || flow.shouldForceConnection(dir, tile);
    }

    public void onTick() {
        behaviour.onTick();
        flow.onTick();
    }

    public boolean fireEvent(PipeEvent event) {
        return eventBus.fireEvent(event);
    }

    // ==================
    // IPipe implementation
    // ==================

    @Override
    public IPipeHolder getHolder() {
        return holder;
    }

    @Override
    public PipeDefinition getDefinition() {
        return definition;
    }

    @Override
    public PipeBehaviour getBehaviour() {
        return behaviour;
    }

    @Override
    public PipeFlow getFlow() {
        return flow;
    }

    @Override
    @Nullable
    public DyeColor getColour() {
        return colour;
    }

    @Override
    public void setColour(@Nullable DyeColor colour) {
        this.colour = colour;
    }

    @Override
    public void markForUpdate() {
        holder.scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
    }

    @Override
    @Nullable
    public BlockEntity getConnectedTile(Direction side) {
        if (Boolean.TRUE.equals(connected.get(side))) {
            return holder.getNeighbourTile(side);
        }
        return null;
    }

    @Override
    @Nullable
    public IPipe getConnectedPipe(Direction side) {
        if (Boolean.TRUE.equals(connected.get(side))
            && connectedTypes.get(side) == ConnectedType.PIPE) {
            return holder.getNeighbourPipe(side);
        }
        return null;
    }

    @Override
    public boolean isConnected(Direction side) {
        return Boolean.TRUE.equals(connected.get(side));
    }

    @Override
    @Nullable
    public ConnectedType getConnectedType(Direction side) {
        if (!isConnected(side)) return null;
        return connectedTypes.get(side);
    }
}
