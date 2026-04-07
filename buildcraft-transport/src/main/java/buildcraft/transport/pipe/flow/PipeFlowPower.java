/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.pipe.flow;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import team.reborn.energy.api.EnergyStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeFlow;

/**
 * Simplified 6-section power flow for pipes using Team Reborn Energy.
 * Each face has a section that stores power and a query value.
 *
 * <p>Algorithm per tick:
 * <ol>
 *   <li>Collect power queries from neighbours (how much they want)</li>
 *   <li>Distribute stored power proportionally to queries</li>
 *   <li>Apply power loss per section</li>
 *   <li>Swap query buffers</li>
 * </ol>
 *
 * <p>Energy units: Team Reborn Energy (1 MJ = 250 E).
 */
public class PipeFlowPower extends PipeFlow {
    /** Maximum power stored per section. */
    private long maxPower;
    /** Power lost per section per tick (converted to E). */
    private long powerLoss;

    private final Section[] sections = new Section[6]; // one per Direction

    public PipeFlowPower(IPipe pipe) {
        super(pipe);
        initFromConfig();
        initSections();
    }

    public PipeFlowPower(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        initFromConfig();
        initSections();
        loadFromNbt(nbt);
    }

    private void initFromConfig() {
        PipeApi.PowerTransferInfo info = PipeApi.getPowerTransferInfo(pipe.getDefinition());
        this.maxPower = info.transferPerTick;
        this.powerLoss = info.lossPerTick;
    }

    private void initSections() {
        for (int i = 0; i < 6; i++) {
            sections[i] = new Section();
        }
    }

    private void loadFromNbt(CompoundTag nbt) {
        for (int i = 0; i < 6; i++) {
            String key = "power_" + i;
            if (nbt.contains(key)) {
                CompoundTag sTag = nbt.getCompoundOrEmpty(key);
                sections[i].internalPower = sTag.getLongOr("power", 0);
                sections[i].powerQuery = sTag.getLongOr("query", 0);
            }
        }
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        for (int i = 0; i < 6; i++) {
            if (sections[i].internalPower > 0 || sections[i].powerQuery > 0) {
                CompoundTag sTag = new CompoundTag();
                sTag.putLong("power", sections[i].internalPower);
                sTag.putLong("query", sections[i].powerQuery);
                nbt.put("power_" + i, sTag);
            }
        }
        return nbt;
    }

    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, boolean isClient) {
        super.writePayload(id, buffer, isClient);
        if (id == NET_ID_FULL_STATE) {
            for (int i = 0; i < 6; i++) {
                buffer.writeLong(sections[i].internalPower);
            }
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, boolean isClient) {
        super.readPayload(id, buffer, isClient);
        if (id == NET_ID_FULL_STATE) {
            for (int i = 0; i < 6; i++) {
                sections[i].internalPower = buffer.readLong();
            }
        }
    }

    @Override
    public boolean canConnect(Direction face, PipeFlow other) {
        return other instanceof PipeFlowPower;
    }

    @Override
    public boolean canConnect(Direction face, BlockEntity oTile) {
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null) return false;
        BlockPos pos = pipe.getHolder().getPipePos().relative(face);
        EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, face.getOpposite());
        return storage != null;
    }

    @Override
    public void onTick() {
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null || level.isClientSide()) return;

        // Step 1: Push power to adjacent blocks/pipes based on their queries
        distributePower();

        // Step 2: Apply power loss
        applyLoss();

        // Step 3: Propagate queries to neighbours (ask neighbours for power)
        propagateQueries();

        // Step 4: Swap query buffers
        for (Section section : sections) {
            section.powerQuery = section.nextPowerQuery;
            section.nextPowerQuery = 0;
        }
    }

    // ===================
    // Power distribution
    // ===================
    private void distributePower() {
        // Compute total available power
        long totalPower = 0;
        for (Section s : sections) {
            totalPower += s.internalPower;
        }
        if (totalPower <= 0) return;

        // Compute total query
        long totalQuery = 0;
        for (Direction dir : Direction.values()) {
            if (!pipe.isConnected(dir)) continue;
            totalQuery += sections[dir.ordinal()].powerQuery;
        }
        if (totalQuery <= 0) return;

        // Distribute proportionally
        long totalSent = 0;
        for (Direction dir : Direction.values()) {
            if (!pipe.isConnected(dir)) continue;
            Section section = sections[dir.ordinal()];
            if (section.powerQuery <= 0) continue;

            long share = Math.min(
                (totalPower * section.powerQuery) / totalQuery,
                section.internalPower
            );
            if (share <= 0) continue;

            long sent = sendPower(dir, share);
            section.internalPower -= sent;
            totalSent += sent;
        }
    }

    private long sendPower(Direction dir, long amount) {
        // Try adjacent pipe first
        IPipe neighbour = pipe.getConnectedPipe(dir);
        if (neighbour != null && neighbour.getFlow() instanceof PipeFlowPower neighbourFlow) {
            return neighbourFlow.acceptPower(amount, dir.getOpposite());
        }

        // Try adjacent energy storage
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null) return 0;
        BlockPos neighbourPos = pipe.getHolder().getPipePos().relative(dir);
        EnergyStorage storage = EnergyStorage.SIDED.find(level, neighbourPos, dir.getOpposite());
        if (storage == null) return 0;

        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(amount, transaction);
            if (inserted > 0) {
                transaction.commit();
                return inserted;
            }
        }
        return 0;
    }

    // ===================
    // Query propagation
    // ===================
    private void propagateQueries() {
        for (Direction dir : Direction.values()) {
            if (!pipe.isConnected(dir)) continue;

            // Check if the adjacent block/pipe wants power
            long query = getNeighbourQuery(dir);
            if (query > 0) {
                // Propagate this query to the opposite side of the pipe
                Direction opposite = dir.getOpposite();
                if (pipe.isConnected(opposite)) {
                    // Tell the opposite section that there's demand on this side
                    sections[opposite.ordinal()].nextPowerQuery += query;
                } else {
                    // Spread to all other connected sides
                    for (Direction other : Direction.values()) {
                        if (other == dir) continue;
                        if (!pipe.isConnected(other)) continue;
                        sections[other.ordinal()].nextPowerQuery += query / 5;
                    }
                }
            }
        }
    }

    private long getNeighbourQuery(Direction dir) {
        // If neighbour is a power pipe, relay its query
        IPipe neighbour = pipe.getConnectedPipe(dir);
        if (neighbour != null && neighbour.getFlow() instanceof PipeFlowPower neighbourFlow) {
            return neighbourFlow.sections[dir.getOpposite().ordinal()].powerQuery;
        }

        // If neighbour is an energy storage that can receive, generate a query
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null) return 0;
        BlockPos neighbourPos = pipe.getHolder().getPipePos().relative(dir);
        EnergyStorage storage = EnergyStorage.SIDED.find(level, neighbourPos, dir.getOpposite());
        if (storage != null && storage.supportsInsertion()) {
            // Query for the maximum we can transfer
            return maxPower;
        }
        return 0;
    }

    // ===================
    // Power loss
    // ===================
    private void applyLoss() {
        for (Section section : sections) {
            if (section.internalPower > 0 && powerLoss > 0) {
                section.internalPower = Math.max(0, section.internalPower - powerLoss);
            }
        }
    }

    // ===================
    // Accept power from neighbours
    // ===================

    /**
     * Accept power from an adjacent pipe or block into this pipe.
     * @return the amount actually accepted.
     */
    public long acceptPower(long amount, Direction fromDir) {
        if (amount <= 0) return 0;
        Section section = sections[fromDir.ordinal()];
        long space = maxPower - section.internalPower;
        long accepted = Math.min(amount, space);
        if (accepted > 0) {
            section.internalPower += accepted;
        }
        return accepted;
    }

    /**
     * Extract power from an adjacent energy storage into this pipe. Used by wood pipe behaviour.
     */
    public void extractPower(Direction dir, long maxAmount) {
        Level level = pipe.getHolder().getPipeWorld();
        if (level == null) return;
        BlockPos neighbourPos = pipe.getHolder().getPipePos().relative(dir);
        EnergyStorage storage = EnergyStorage.SIDED.find(level, neighbourPos, dir.getOpposite());
        if (storage == null) return;

        Section section = sections[dir.ordinal()];
        long space = maxPower - section.internalPower;
        long toExtract = Math.min(maxAmount, space);
        if (toExtract <= 0) return;

        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = storage.extract(toExtract, transaction);
            if (extracted > 0) {
                section.internalPower += extracted;
                transaction.commit();
            }
        }
    }

    /** @return the total power across all sections. */
    public long getTotalPower() {
        long total = 0;
        for (Section s : sections) {
            total += s.internalPower;
        }
        return total;
    }

    /** @return the power in a specific face section. */
    public long getSectionPower(Direction dir) {
        return sections[dir.ordinal()].internalPower;
    }

    // ===================
    // Inner class
    // ===================

    private static class Section {
        long internalPower;
        long powerQuery;
        long nextPowerQuery;
    }
}
