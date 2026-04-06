/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NBTUtilBC {
    private static final Logger LOGGER = LoggerFactory.getLogger("BuildCraft/NBTUtilBC");

    // NBT type constants (replaces Constants.NBT.TAG_*)
    public static final int TAG_COMPOUND = 10;
    public static final int TAG_INT_ARRAY = 11;
    public static final int TAG_STRING = 8;
    public static final int TAG_BYTE = 1;
    public static final int TAG_BYTE_ARRAY = 7;
    public static final int TAG_LIST = 9;

    @SuppressWarnings("WeakerAccess")
    public static final CompoundTag NBT_NULL = new CompoundTag();

    public static <N extends Tag> Optional<N> toOptional(N value) {
        return value == NBTUtilBC.NBT_NULL ? Optional.empty() : Optional.of(value);
    }

    public static Tag merge(Tag destination, Tag source) {
        if (source == null) {
            return null;
        }
        if (destination == null) {
            return source;
        }
        if (destination.getId() == TAG_COMPOUND && source.getId() == TAG_COMPOUND) {
            CompoundTag result = new CompoundTag();
            CompoundTag destCompound = (CompoundTag) destination;
            CompoundTag srcCompound = (CompoundTag) source;
            for (String key : Sets.union(destCompound.keySet(), srcCompound.keySet())) {
                if (!srcCompound.contains(key)) {
                    result.put(key, destCompound.get(key));
                } else if (srcCompound.get(key) != NBT_NULL) {
                    if (!destCompound.contains(key)) {
                        result.put(key, srcCompound.get(key));
                    } else {
                        result.put(key, merge(destCompound.get(key), srcCompound.get(key)));
                    }
                }
            }
            return result;
        }
        return source;
    }

    public static CompoundTag getItemData(@NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return new CompoundTag();
        }
        CustomData customData = stack.getComponents().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag();
    }

    public static IntArrayTag writeBlockPos(BlockPos pos) {
        if (pos == null) {
            throw new NullPointerException("Cannot return a null NBTTag -- pos was null!");
        }
        return new IntArrayTag(new int[] { pos.getX(), pos.getY(), pos.getZ() });
    }

    @SuppressWarnings("unused")
    public static CompoundTag writeBlockPosAsCompound(BlockPos pos) {
        if (pos == null) {
            throw new NullPointerException("Cannot return a null NBTTag -- pos was null!");
        }
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
        return nbt;
    }

    @Nullable
    public static BlockPos readBlockPos(Tag base) {
        if (base == null) {
            return null;
        }
        switch (base.getId()) {
            case TAG_INT_ARRAY: {
                int[] array = ((IntArrayTag) base).getAsIntArray();
                if (array.length == 3) {
                    return new BlockPos(array[0], array[1], array[2]);
                }
                return null;
            }
            case TAG_COMPOUND: {
                CompoundTag nbt = (CompoundTag) base;
                BlockPos pos = null;
                if (nbt.contains("i")) {
                    int i = nbt.getIntOr("i", 0);
                    int j = nbt.getIntOr("j", 0);
                    int k = nbt.getIntOr("k", 0);
                    pos = new BlockPos(i, j, k);
                } else if (nbt.contains("x")) {
                    int x = nbt.getIntOr("x", 0);
                    int y = nbt.getIntOr("y", 0);
                    int z = nbt.getIntOr("z", 0);
                    pos = new BlockPos(x, y, z);
                } else if (nbt.contains("pos")) {
                    return readBlockPos(nbt.get("pos"));
                } else {
                    LOGGER.warn("Attempted to read a block position from a compound tag without the correct sub-tags! ({})", base, new Throwable());
                }
                return pos;
            }
        }
        LOGGER.warn("Attempted to read a block position from an invalid tag! ({})", base, new Throwable());
        return null;
    }

    public static ListTag writeVec3(Vec3 vec3) {
        ListTag list = new ListTag();
        list.add(DoubleTag.valueOf(vec3.x));
        list.add(DoubleTag.valueOf(vec3.y));
        list.add(DoubleTag.valueOf(vec3.z));
        return list;
    }

    @Nullable
    public static Vec3 readVec3(Tag nbt) {
        if (nbt instanceof ListTag) {
            return readVec3((ListTag) nbt);
        }
        return null;
    }

    public static Vec3 readVec3(ListTag list) {
        return new Vec3(list.getDoubleOr(0, 0.0), list.getDoubleOr(1, 0.0), list.getDoubleOr(2, 0.0));
    }

    private static final String NULL_ENUM_STRING = "_NULL";

    public static <E extends Enum<E>> Tag writeEnum(E value) {
        if (value == null) {
            return StringTag.valueOf(NULL_ENUM_STRING);
        }
        return StringTag.valueOf(value.name());
    }

    public static <E extends Enum<E>> E readEnum(Tag nbt, Class<E> clazz) {
        if (nbt instanceof StringTag stringTag) {
            String value = stringTag.value();
            if (NULL_ENUM_STRING.equals(value)) {
                return null;
            }
            try {
                return Enum.valueOf(clazz, value);
            } catch (Throwable t) {
                LOGGER.warn("Tried and failed to read the value({}) from {}", value, clazz.getSimpleName(), t);
                return null;
            }
        } else if (nbt instanceof ByteTag byteTag) {
            byte value = byteTag.byteValue();
            if (value < 0 || value >= clazz.getEnumConstants().length) {
                return null;
            } else {
                return clazz.getEnumConstants()[value];
            }
        } else if (nbt == null) {
            return null;
        } else {
            LOGGER.warn("Tried to read an enum value when it was not a string! This is probably not good!", new IllegalArgumentException());
            return null;
        }
    }

    public static Tag writeDoubleArray(double[] data) {
        ListTag list = new ListTag();
        for (double d : data) {
            list.add(DoubleTag.valueOf(d));
        }
        return list;
    }

    public static double[] readDoubleArray(Tag tag, int intendedLength) {
        double[] arr = new double[intendedLength];
        if (tag instanceof ListTag) {
            ListTag list = (ListTag) tag;
            for (int i = 0; i < list.size() && i < intendedLength; i++) {
                arr[i] = list.getDoubleOr(i, 0.0);
            }
        }
        return arr;
    }

    /** Writes an {@link EnumSet} to a {@link Tag}. The returned type will either be {@link ByteTag} or
     * {@link ByteArrayTag}.
     *
     * @param clazz The class that the {@link EnumSet} is of. */
    public static <E extends Enum<E>> Tag writeEnumSet(EnumSet<E> set, Class<E> clazz) {
        E[] constants = clazz.getEnumConstants();
        if (constants == null) throw new IllegalArgumentException("Not an enum type " + clazz);
        BitSet bitset = new BitSet();
        for (E e : constants) {
            if (set.contains(e)) {
                bitset.set(e.ordinal());
            }
        }
        byte[] bytes = bitset.toByteArray();
        if (bytes.length == 1) {
            return ByteTag.valueOf(bytes[0]);
        } else {
            return new ByteArrayTag(bytes);
        }
    }

    public static <E extends Enum<E>> EnumSet<E> readEnumSet(Tag tag, Class<E> clazz) {
        E[] constants = clazz.getEnumConstants();
        if (constants == null) throw new IllegalArgumentException("Not an enum type " + clazz);
        byte[] bytes;
        if (tag instanceof ByteTag) {
            bytes = new byte[] { ((ByteTag) tag).byteValue() };
        } else if (tag instanceof ByteArrayTag) {
            bytes = ((ByteArrayTag) tag).getAsByteArray();
        } else {
            bytes = new byte[] {};
            LOGGER.warn("[lib.nbt] Tried to read an enum set from {}", tag);
        }
        BitSet bitset = BitSet.valueOf(bytes);
        EnumSet<E> set = EnumSet.noneOf(clazz);
        for (E e : constants) {
            if (bitset.get(e.ordinal())) {
                set.add(e);
            }
        }
        return set;
    }

    public static ListTag writeCompoundList(Stream<CompoundTag> stream) {
        ListTag list = new ListTag();
        stream.forEach(list::add);
        return list;
    }

    public static Stream<CompoundTag> readCompoundList(Tag list) {
        if (list == null) {
            return Stream.empty();
        }
        if (!(list instanceof ListTag)) {
            throw new IllegalArgumentException();
        }
        ListTag listTag = (ListTag) list;
        return IntStream.range(0, listTag.size()).mapToObj(listTag::getCompoundOrEmpty);
    }

    public static ListTag writeStringList(Stream<String> stream) {
        ListTag list = new ListTag();
        stream.map(StringTag::valueOf).forEach(list::add);
        return list;
    }

    public static Stream<String> readStringList(Tag list) {
        if (list == null) {
            return Stream.empty();
        }
        if (!(list instanceof ListTag)) {
            throw new IllegalArgumentException();
        }
        ListTag listTag = (ListTag) list;
        return IntStream.range(0, listTag.size()).mapToObj(i -> listTag.getStringOr(i, ""));
    }
}
