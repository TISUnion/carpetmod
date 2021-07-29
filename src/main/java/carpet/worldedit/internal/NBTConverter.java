/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package carpet.worldedit.internal;

import com.sk89q.jnbt.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * Converts between JNBT and Minecraft NBT classes.
 */
public final class NBTConverter {

    private NBTConverter() {
    }

    public static net.minecraft.nbt.INBTBase toNative(Tag tag) {
        if (tag instanceof IntArrayTag) {
            return toNative((IntArrayTag) tag);

        } else if (tag instanceof ListTag) {
            return toNative((ListTag) tag);

        } else if (tag instanceof LongTag) {
            return toNative((LongTag) tag);

        } else if (tag instanceof LongArrayTag) {
            return toNative((LongArrayTag) tag);

        } else if (tag instanceof StringTag) {
            return toNative((StringTag) tag);

        } else if (tag instanceof IntTag) {
            return toNative((IntTag) tag);

        } else if (tag instanceof ByteTag) {
            return toNative((ByteTag) tag);

        } else if (tag instanceof ByteArrayTag) {
            return toNative((ByteArrayTag) tag);

        } else if (tag instanceof CompoundTag) {
            return toNative((CompoundTag) tag);

        } else if (tag instanceof FloatTag) {
            return toNative((FloatTag) tag);

        } else if (tag instanceof ShortTag) {
            return toNative((ShortTag) tag);

        } else if (tag instanceof DoubleTag) {
            return toNative((DoubleTag) tag);
        } else {
            throw new IllegalArgumentException("Can't convert tag of type " + tag.getClass().getCanonicalName());
        }
    }

    public static net.minecraft.nbt.NBTTagIntArray toNative(IntArrayTag tag) {
        int[] value = tag.getValue();
        return new net.minecraft.nbt.NBTTagIntArray(Arrays.copyOf(value, value.length));
    }

    public static net.minecraft.nbt.NBTTagList toNative(ListTag tag) {
        net.minecraft.nbt.NBTTagList list = new net.minecraft.nbt.NBTTagList();
        for (Tag child : tag.getValue()) {
            if (child instanceof EndTag) {
                continue;
            }
            list.add(toNative(child));
        }
        return list;
    }

    public static net.minecraft.nbt.NBTTagLong toNative(LongTag tag) {
        return new net.minecraft.nbt.NBTTagLong(tag.getValue());
    }

    public static net.minecraft.nbt.NBTTagLongArray toNative(LongArrayTag tag) {
        return new net.minecraft.nbt.NBTTagLongArray(tag.getValue().clone());
    }

    public static net.minecraft.nbt.NBTTagString toNative(StringTag tag) {
        return new net.minecraft.nbt.NBTTagString(tag.getValue());
    }

    public static net.minecraft.nbt.NBTTagInt toNative(IntTag tag) {
        return new net.minecraft.nbt.NBTTagInt(tag.getValue());
    }

    public static net.minecraft.nbt.NBTTagByte toNative(ByteTag tag) {
        return new net.minecraft.nbt.NBTTagByte(tag.getValue());
    }

    public static net.minecraft.nbt.NBTTagByteArray toNative(ByteArrayTag tag) {
        return new net.minecraft.nbt.NBTTagByteArray(tag.getValue().clone());
    }

    public static net.minecraft.nbt.NBTTagCompound toNative(CompoundTag tag) {
        net.minecraft.nbt.NBTTagCompound compound = new net.minecraft.nbt.NBTTagCompound();
        for (Entry<String, Tag> child : tag.getValue().entrySet()) {
            compound.put(child.getKey(), toNative(child.getValue()));
        }
        return compound;
    }

    public static net.minecraft.nbt.NBTTagFloat toNative(FloatTag tag) {
        return new net.minecraft.nbt.NBTTagFloat(tag.getValue());
    }

    public static net.minecraft.nbt.NBTTagShort toNative(ShortTag tag) {
        return new net.minecraft.nbt.NBTTagShort(tag.getValue());
    }

    public static net.minecraft.nbt.NBTTagDouble toNative(DoubleTag tag) {
        return new net.minecraft.nbt.NBTTagDouble(tag.getValue());
    }

    public static Tag fromNative(net.minecraft.nbt.INBTBase other) {
        if (other instanceof net.minecraft.nbt.NBTTagIntArray) {
            return fromNative((net.minecraft.nbt.NBTTagIntArray) other);

        } else if (other instanceof net.minecraft.nbt.NBTTagList) {
            return fromNative((net.minecraft.nbt.NBTTagList) other);

        } else if (other instanceof net.minecraft.nbt.NBTTagEnd) {
            return fromNative((net.minecraft.nbt.NBTTagEnd) other);

        } else if (other instanceof net.minecraft.nbt.NBTTagLong) {
            return fromNative((net.minecraft.nbt.NBTTagLong) other);

        } else if (other instanceof net.minecraft.nbt.NBTTagLongArray) {
            return fromNative((net.minecraft.nbt.NBTTagLongArray) other);

        } else if (other instanceof net.minecraft.nbt.NBTTagString) {
            return fromNative((net.minecraft.nbt.NBTTagString) other);

        } else if (other instanceof net.minecraft.nbt.NBTTagInt) {
            return fromNative((net.minecraft.nbt.NBTTagInt) other);

        } else if (other instanceof net.minecraft.nbt.NBTTagByte) {
            return fromNative((net.minecraft.nbt.NBTTagByte) other);

        } else if (other instanceof net.minecraft.nbt.NBTTagByteArray) {
            return fromNative((net.minecraft.nbt.NBTTagByteArray) other);

        } else if (other instanceof net.minecraft.nbt.NBTTagCompound) {
            return fromNative((net.minecraft.nbt.NBTTagCompound) other);

        } else if (other instanceof net.minecraft.nbt.NBTTagFloat) {
            return fromNative((net.minecraft.nbt.NBTTagFloat) other);

        } else if (other instanceof net.minecraft.nbt.NBTTagShort) {
            return fromNative((net.minecraft.nbt.NBTTagShort) other);

        } else if (other instanceof net.minecraft.nbt.NBTTagDouble) {
            return fromNative((net.minecraft.nbt.NBTTagDouble) other);
        } else {
            throw new IllegalArgumentException("Can't convert other of type " + other.getClass().getCanonicalName());
        }
    }

    public static IntArrayTag fromNative(net.minecraft.nbt.NBTTagIntArray other) {
        int[] value = other.getIntArray();
        return new IntArrayTag(Arrays.copyOf(value, value.length));
    }

    public static ListTag fromNative(net.minecraft.nbt.NBTTagList other) {
        other = other.copy();
        List<Tag> list = new ArrayList<>();
        Class<? extends Tag> listClass = StringTag.class;
        int tags = other.size();
        for (int i = 0; i < tags; i++) {
            Tag child = fromNative(other.remove(0));
            list.add(child);
            listClass = child.getClass();
        }
        return new ListTag(listClass, list);
    }

    public static EndTag fromNative(net.minecraft.nbt.NBTTagEnd other) {
        return new EndTag();
    }

    public static LongTag fromNative(net.minecraft.nbt.NBTTagLong other) {
        return new LongTag(other.getLong());
    }

    public static LongArrayTag fromNative(net.minecraft.nbt.NBTTagLongArray other) {
        return new LongArrayTag(other.getAsLongArray().clone());
    }

    public static StringTag fromNative(net.minecraft.nbt.NBTTagString other) {
        return new StringTag(other.getString());
    }

    public static IntTag fromNative(net.minecraft.nbt.NBTTagInt other) {
        return new IntTag(other.getInt());
    }

    public static ByteTag fromNative(net.minecraft.nbt.NBTTagByte other) {
        return new ByteTag(other.getByte());
    }

    public static ByteArrayTag fromNative(net.minecraft.nbt.NBTTagByteArray other) {
        return new ByteArrayTag(other.getByteArray().clone());
    }

    public static CompoundTag fromNative(net.minecraft.nbt.NBTTagCompound other) {
        Set<String> tags = other.keySet();
        Map<String, Tag> map = new HashMap<>();
        for (String tagName : tags) {
            map.put(tagName, fromNative(other.get(tagName)));
        }
        return new CompoundTag(map);
    }

    public static FloatTag fromNative(net.minecraft.nbt.NBTTagFloat other) {
        return new FloatTag(other.getFloat());
    }

    public static ShortTag fromNative(net.minecraft.nbt.NBTTagShort other) {
        return new ShortTag(other.getShort());
    }

    public static DoubleTag fromNative(net.minecraft.nbt.NBTTagDouble other) {
        return new DoubleTag(other.getDouble());
    }

}
