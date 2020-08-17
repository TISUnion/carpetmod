package me.jellysquid.mods.lithium.common.world.chunk.palette;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.world.chunk.IBlockStatePalette;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;

public class LithiumHashPalette<T> implements IBlockStatePalette<T> {
    private final ObjectIntIdentityMap<T> idList;
    private final LithiumInt2ObjectBiMap<T> map;
    private final LithiumPaletteResizeListener<T> resizeHandler;
    private final Function<NBTTagCompound, T> elementDeserializer;
    private final Function<T, NBTTagCompound> elementSerializer;
    private final int indexBits;
    private T prevObj;
    private int prevKey;

    public LithiumHashPalette(ObjectIntIdentityMap<T> ids, int bits, LithiumPaletteResizeListener<T> resizeHandler, Function<NBTTagCompound, T> deserializer, Function<T, NBTTagCompound> serializer) {
        this.idList = ids;
        this.indexBits = bits;
        this.resizeHandler = resizeHandler;
        this.elementDeserializer = deserializer;
        this.elementSerializer = serializer;
        this.map = new LithiumInt2ObjectBiMap<>(1 << bits);
    }

    @Override
    public int idFor(T obj) {
        if (this.prevObj == obj) {
            return this.prevKey;
        }

        int id = this.map.getId(obj);

        if (id == -1) {
            id = this.map.add(obj);

            if (id >= 1 << this.indexBits) {
                if (this.resizeHandler == null) {
                    throw new IllegalStateException("Cannot grow");
                } else {
                    id = this.resizeHandler.onLithiumResize(this.indexBits + 1, obj);
                }
            }
        }

        this.prevObj = obj;
        this.prevKey = id;

        return id;
    }

    @Override
    public T get(int id) {
        return this.map.get(id);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.map.clear();
        int entryCount = buf.readVarInt();

        for (int i = 0; i < entryCount; ++i) {
            this.map.add(this.idList.getByValue(buf.readVarInt()));
        }
    }

    @Override
    public void write(PacketBuffer buf) {
        int paletteBits = this.getIndexBits();
        buf.writeVarInt(paletteBits);

        for (int i = 0; i < paletteBits; ++i) {
            buf.writeVarInt(this.idList.get(this.map.get(i)));
        }
    }

    @Override
    public int getSerializedSize() {
        int size = PacketBuffer.getVarIntSize(this.getIndexBits());

        for (int i = 0; i < this.getIndexBits(); ++i) {
            size += PacketBuffer.getVarIntSize(this.idList.get(this.map.get(i)));
        }

        return size;
    }

    private int getIndexBits() {
        return this.map.size();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void read(NBTTagList list) {
        this.map.clear();

        for (int i = 0; i < list.size(); ++i) {
            this.map.add(this.elementDeserializer.apply(list.getCompound(i)));
        }
    }

    public void toTag(NBTTagList list) {
        for (int i = 0; i < this.getIndexBits(); ++i) {
            list.add(this.elementSerializer.apply(this.map.get(i)));
        }
    }

    public int getSize() {
        return this.map.size();
    }
}
