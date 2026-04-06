/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.engine;

import java.io.IOException;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.BlockEntityBCBase;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import team.reborn.energy.api.EnergyStorage;

/**
 * Base block entity for all BuildCraft engines.
 * Simplified port of TileEngineBase_BC8 from the Forge version.
 *
 * <p>Key features:
 * <ul>
 *   <li>Facing direction stored in blockstate via FACING property</li>
 *   <li>Heat system (MIN_HEAT=20, IDEAL_HEAT=100, MAX_HEAT=250)</li>
 *   <li>Power storage tracked as Team Reborn Energy units</li>
 *   <li>Power stage calculation (BLUE/GREEN/YELLOW/RED/OVERHEAT)</li>
 *   <li>Piston animation (progress 0-1, progressPart for client interpolation)</li>
 *   <li>Push-based energy output to the adjacent block on the facing direction</li>
 *   <li>Redstone check for activation</li>
 * </ul>
 */
public abstract class BlockEntityEngineBase extends BlockEntityBCBase {
    protected static final IdAllocator IDS = BlockEntityBCBase.IDS.makeChild("engine");

    public static final float MIN_HEAT = 20f;
    public static final float IDEAL_HEAT = 100f;
    public static final float MAX_HEAT = 250f;

    protected float heat = MIN_HEAT;
    /** Stored energy in Team Reborn Energy units. */
    protected long power;
    /** Piston animation progress, 0 to 1. */
    protected float progress;
    /** Sub-tick animation counter for smooth client-side piston movement. */
    protected int progressPart;

    private boolean isActive = false;

    public BlockEntityEngineBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ##################
    //
    // Abstract methods for subclasses
    //
    // ##################

    /** @return true if the engine is currently consuming fuel or otherwise producing energy. */
    public abstract boolean isBurning();

    /** @return Energy output per tick in Team Reborn Energy units. */
    public abstract long getCurrentOutput();

    /** @return Maximum stored energy in Team Reborn Energy units. */
    public abstract long getMaxPower();

    /** @return Maximum energy that can be pushed to an adjacent block per tick. */
    public abstract long getMaxExtract();

    // ##################
    //
    // State getters
    //
    // ##################

    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    public EnumPowerStage getPowerStage() {
        float heatPercent = (heat - MIN_HEAT) / (MAX_HEAT - MIN_HEAT);
        if (heatPercent < 0.25f) return EnumPowerStage.BLUE;
        if (heatPercent < 0.5f) return EnumPowerStage.GREEN;
        if (heatPercent < 0.75f) return EnumPowerStage.YELLOW;
        if (heatPercent < 0.9f) return EnumPowerStage.RED;
        return EnumPowerStage.OVERHEAT;
    }

    public float getProgress() {
        return progress;
    }

    public float getHeat() {
        return heat;
    }

    public long getPower() {
        return power;
    }

    /** Subtract energy from the internal power buffer. Used by the energy view for extraction. */
    public void extractPower(long amount) {
        power = Math.max(0, power - amount);
    }

    public boolean isActive() {
        return isActive;
    }

    // ##################
    //
    // Tick logic
    //
    // ##################

    /** Called every tick via BlockEntityTicker. */
    public void tick() {
        if (level == null) return;

        if (level.isClientSide()) {
            tickClient();
            return;
        }

        tickServer();
    }

    private void tickClient() {
        if (isActive) {
            progressPart++;
            float speed = getPistonSpeed();
            int cycleLength = Math.max(1, (int) (1f / speed));
            if (progressPart >= cycleLength) {
                progressPart = 0;
                progress += speed;
                if (progress >= 1.0f) {
                    progress = 0;
                }
            }
        } else {
            progress = 0;
            progressPart = 0;
        }
    }

    private void tickServer() {
        boolean wasBurning = isActive;
        isActive = isRedstonePowered() && isBurning();

        if (isActive) {
            burn();
        }

        // Push energy to the adjacent block on our output face
        if (power > 0) {
            pushEnergy();
        }

        // Gradual heat decay toward minimum
        if (heat > MIN_HEAT) {
            heat -= 0.1f;
            if (heat < MIN_HEAT) {
                heat = MIN_HEAT;
            }
        }

        if (wasBurning != isActive) {
            sendNetworkUpdate(NET_RENDER_DATA);
        }
    }

    protected void burn() {
        long output = getCurrentOutput();
        power = Math.min(power + output, getMaxPower());
    }

    protected void pushEnergy() {
        Direction facing = getFacing();
        BlockPos targetPos = worldPosition.relative(facing);

        EnergyStorage target = EnergyStorage.SIDED.find(level, targetPos, facing.getOpposite());
        if (target != null && target.supportsInsertion()) {
            long maxPush = Math.min(power, getMaxExtract());
            if (maxPush > 0) {
                try (Transaction transaction = Transaction.openOuter()) {
                    long inserted = target.insert(maxPush, transaction);
                    if (inserted > 0) {
                        power -= inserted;
                        transaction.commit();
                    }
                }
            }
        }
    }

    protected boolean isRedstonePowered() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    protected float getPistonSpeed() {
        return switch (getPowerStage()) {
            case BLUE -> 0.02f;
            case GREEN -> 0.04f;
            case YELLOW -> 0.08f;
            case RED -> 0.16f;
            default -> 0.01f;
        };
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
            buffer.writeBoolean(isActive);
            buffer.writeFloat(heat);
            buffer.writeLong(power);
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, boolean isClient) throws IOException {
        super.readPayload(id, buffer, isClient);
        if (id == NET_RENDER_DATA) {
            isActive = buffer.readBoolean();
            heat = buffer.readFloat();
            power = buffer.readLong();
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
        output.putFloat("heat", heat);
        output.putLong("power", power);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        heat = input.getFloatOr("heat", MIN_HEAT);
        power = input.getLongOr("power", 0);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }
}
