package me.jellysquid.mods.lithium.common.nbt.io;

import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.nbt.NBTSizeTracker;

import java.io.IOException;

public class NbtFastIo {
    public static void write(NBTTagCompound compoundTag, NbtFastWriter writer) {
        write((INBTBase) compoundTag, writer);
    }

    private static void write(INBTBase tag, NbtFastWriter writer) {
        writer.writeByte(tag.getId());

        if (tag.getId() != 0) {
            writer.writeString("");

            tag.serialize(writer);
        }
    }

    public static NBTTagCompound read(NbtFastReader reader) throws IOException {
        return read(reader, NBTSizeTracker.INFINITE);
    }

    public static NBTTagCompound read(NbtFastReader reader, NBTSizeTracker nbtSizeTracker) throws IOException {
        INBTBase tag = read(reader, 0, nbtSizeTracker);

        if (tag instanceof NBTTagCompound) {
            return (NBTTagCompound) tag;
        }

        throw new IOException("Root tag must be a named compound tag");
    }

    private static INBTBase read(NbtFastReader reader, int level, NBTSizeTracker nbtSizeTracker) {
        byte type = reader.readByte();

        if (type == 0) {
            return new NBTTagEnd();
        }

        reader.readString();

        INBTBase tag = INBTBase.create(type);
        tag.deserialize(reader, level, nbtSizeTracker);

        return tag;
    }
}
