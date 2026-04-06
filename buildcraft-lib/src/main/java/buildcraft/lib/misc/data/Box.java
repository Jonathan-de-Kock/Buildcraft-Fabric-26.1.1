/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;

/** MUTABLE integer variant of AABB, with a few BC-specific methods */
public class Box {

    private BlockPos min, max;

    public Box() {
        reset();
    }

    public Box(BlockPos min, BlockPos max) {
        this();
        this.min = VecUtil.min(min, max);
        this.max = VecUtil.max(min, max);
    }

    public void reset() {
        min = null;
        max = null;
    }

    public boolean isInitialized() {
        return min != null && max != null;
    }

    public void extendToEncompassBoth(BlockPos newMin, BlockPos newMax) {
        this.min = VecUtil.min(this.min, newMin, newMax);
        this.max = VecUtil.max(this.max, newMin, newMax);
    }

    public void setMin(BlockPos min) {
        if (min == null) return;
        this.min = min;
        this.max = VecUtil.max(min, max);
    }

    public void setMax(BlockPos max) {
        if (max == null) return;
        this.min = VecUtil.min(min, max);
        this.max = max;
    }

    public void initialize(CompoundTag nbt) {
        reset();
        if (nbt.contains("xMin")) {
            min = new BlockPos(nbt.getIntOr("xMin", 0), nbt.getIntOr("yMin", 0), nbt.getIntOr("zMin", 0));
            max = new BlockPos(nbt.getIntOr("xMax", 0), nbt.getIntOr("yMax", 0), nbt.getIntOr("zMax", 0));
        } else {
            min = NBTUtilBC.readBlockPos(nbt.get("min"));
            max = NBTUtilBC.readBlockPos(nbt.get("max"));
        }
        if (min != null && max != null) {
            extendToEncompassBoth(min, max);
        }
    }

    public void writeToNBT(CompoundTag nbt) {
        if (min != null) nbt.put("min", NBTUtilBC.writeBlockPos(min));
        if (max != null) nbt.put("max", NBTUtilBC.writeBlockPos(max));
    }

    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();
        writeToNBT(nbt);
        return nbt;
    }

    public void initializeCenter(BlockPos center, int size) {
        initializeCenter(center, new BlockPos(size, size, size));
    }

    public void initializeCenter(BlockPos center, Vec3i size) {
        extendToEncompassBoth(center.subtract(size), center.offset(size));
    }

    public List<BlockPos> getBlocksInArea() {
        List<BlockPos> blocks = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            blocks.add(pos.immutable());
        }
        return blocks;
    }

    public Box expand(int amount) {
        if (!isInitialized()) return this;
        Vec3i am = new BlockPos(amount, amount, amount);
        setMin(min().subtract(am));
        setMax(max().offset(am));
        return this;
    }

    public Box contract(int amount) {
        return expand(-amount);
    }

    public boolean contains(Vec3 p) {
        AABB bb = getBoundingBox();
        if (p.x < bb.minX || p.x >= bb.maxX) return false;
        if (p.y < bb.minY || p.y >= bb.maxY) return false;
        if (p.z < bb.minZ || p.z >= bb.maxZ) return false;
        return true;
    }

    public boolean contains(BlockPos i) {
        return contains(Vec3.atLowerCornerOf(i));
    }

    public BlockPos min() {
        return min;
    }

    public BlockPos max() {
        return max;
    }

    public BlockPos size() {
        if (!isInitialized()) return BlockPos.ZERO;
        return max.subtract(min).offset(VecUtil.POS_ONE);
    }

    public BlockPos center() {
        return BlockPos.containing(centerExact());
    }

    public Vec3 centerExact() {
        return new Vec3(size().getX(), size().getY(), size().getZ()).scale(0.5).add(Vec3.atLowerCornerOf(min()));
    }

    @Override
    public String toString() {
        return "Box[min = " + min + ", max = " + max + "]";
    }

    public Box extendToEncompass(Box toBeContained) {
        if (toBeContained == null) {
            return this;
        }
        extendToEncompassBoth(toBeContained.min(), toBeContained.max());
        return this;
    }

    /** IMPORTANT: Use {@link #contains(Vec3)} instead of the returned {@link AABB#contains(Vec3)} as the
     * logic is different! */
    public AABB getBoundingBox() {
        return new AABB(min.getX(), min.getY(), min.getZ(),
            max.getX() + 1, max.getY() + 1, max.getZ() + 1);
    }

    public Box extendToEncompass(Vec3 toBeContained) {
        setMin(VecUtil.min(min, VecUtil.convertFloor(toBeContained)));
        setMax(VecUtil.max(max, VecUtil.convertCeiling(toBeContained)));
        return this;
    }

    public Box extendToEncompass(BlockPos toBeContained) {
        setMin(VecUtil.min(min, toBeContained));
        setMax(VecUtil.max(max, toBeContained));
        return this;
    }

    public double distanceTo(BlockPos index) {
        return Math.sqrt(distanceToSquared(index));
    }

    public double distanceToSquared(BlockPos index) {
        return closestInsideTo(index).distSqr(index);
    }

    public BlockPos closestInsideTo(BlockPos toTest) {
        return VecUtil.max(min, VecUtil.min(max, toTest));
    }

    public boolean doesIntersectWith(Box box) {
        if (isInitialized() && box.isInitialized()) {
            return min.getX() <= box.max.getX() && max.getX() >= box.min.getX()
                && min.getY() <= box.max.getY() && max.getY() >= box.min.getY()
                && min.getZ() <= box.max.getZ() && max.getZ() >= box.min.getZ();
        }
        return false;
    }

    /** @return The intersection box (if these two boxes are intersecting) or null if they were not. */
    @Nullable
    public Box getIntersect(Box box) {
        if (doesIntersectWith(box)) {
            BlockPos min2 = VecUtil.max(min, box.min);
            BlockPos max2 = VecUtil.min(max, box.max);
            return new Box(min2, max2);
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        Box box = (Box) obj;
        if (!Objects.equals(min, box.min)) return false;
        if (!Objects.equals(max, box.max)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }
}
