package me.jellysquid.mods.lithium.common.world.chunk;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.world.chunk.IBlockStatePalette;
import net.minecraft.world.chunk.IBlockStatePaletteResizer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static it.unimi.dsi.fastutil.Hash.FAST_LOAD_FACTOR;

// from Lithium 0.7.5
// https://github.com/CaffeineMC/lithium-fabric/blob/mc1.17.1-0.7.5/src/main/java/me/jellysquid/mods/lithium/common/world/chunk/LithiumHashPalette.java
/**
 * Generally provides better performance over the vanilla {@link net.minecraft.world.chunk.BlockStatePaletteHashMap}
 * (net.minecraft.world.chunk.BiMapPalette in yarn) when calling
 * {@link LithiumHashPalette#idFor(Object)} through using a faster backing map and reducing pointer chasing.
 */
public class LithiumHashPalette<T> implements IBlockStatePalette<T> {
    private static final int ABSENT_VALUE = -1;

    private final ObjectIntIdentityMap<T> idList;
    private final IBlockStatePaletteResizer<T> resizeHandler;
    private final Function<NBTTagCompound, T> elementDeserializer;
    private final Function<T, NBTTagCompound> elementSerializer;
    private final int indexBits;

    private final Reference2IntMap<T> table;
    private T[] entries;
    private int size = 0;

    @SuppressWarnings("unchecked")
    public LithiumHashPalette(ObjectIntIdentityMap<T> ids, int bits, IBlockStatePaletteResizer<T> resizeHandler, Function<NBTTagCompound, T> deserializer, Function<T, NBTTagCompound> serializer) {
        this.idList = ids;
        this.indexBits = bits;
        this.resizeHandler = resizeHandler;
        this.elementDeserializer = deserializer;
        this.elementSerializer = serializer;

        int capacity = 1 << bits;

        this.entries = (T[]) new Object[capacity];
        this.table = new Reference2IntOpenHashMap<>(capacity, FAST_LOAD_FACTOR);
        this.table.defaultReturnValue(ABSENT_VALUE);
    }

    @Override
    public int idFor(T obj) {
        int id = this.table.getInt(obj);

        if (id == ABSENT_VALUE) {
            id = this.computeEntry(obj);
        }

        return id;
    }

    private int computeEntry(T obj) {
        int id = this.addEntry(obj);

        if (id >= 1 << this.indexBits) {
            if (this.resizeHandler == null) {
                throw new IllegalStateException("Cannot grow");
            } else {
                id = this.resizeHandler.onResize(this.indexBits + 1, obj);
            }
        }

        return id;
    }

    private int addEntry(T obj) {
        int nextId = this.size;

        if (nextId >= this.entries.length) {
            this.resize(this.size);
        }

        this.table.put(obj, nextId);
        this.entries[nextId] = obj;

        this.size++;

        return nextId;
    }

    private void resize(int neededCapacity) {
        this.entries = Arrays.copyOf(this.entries, HashCommon.nextPowerOfTwo(neededCapacity + 1));
    }

//    @Override
//    public boolean accepts(Predicate<T> predicate) {
//        for (int i = 0; i < this.size; ++i) {
//            if (predicate.test(this.entries[i])) {
//                return true;
//            }
//        }
//
//        return false;
//    }

    @Override
    public T get(int id) {
        T[] entries = this.entries;

        if (id >= 0 && id < entries.length) {
            return entries[id];
        }

        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void read(PacketBuffer buf) {
        this.clear();

        int entryCount = buf.readVarInt();

        for (int i = 0; i < entryCount; ++i) {
            this.addEntry(this.idList.getByValue(buf.readVarInt()));
        }
    }

    @Override
    public void write(PacketBuffer buf) {
        int size = this.size;
        buf.writeVarInt(size);

        for (int i = 0; i < size; ++i) {
            buf.writeVarInt(this.idList.get(this.get(i)));
        }
    }

    @Override
    public int getSerializedSize() {
        int size = PacketBuffer.getVarIntSize(this.size);

        for (int i = 0; i < this.size; ++i) {
            size += PacketBuffer.getVarIntSize(this.idList.get(this.get(i)));
        }

        return size;
    }

//    @Override
//    public int getIndexBits() {
//        return this.size;
//    }

    @Override
    public void read(NBTTagList list) {
        this.clear();

        for (int i = 0; i < list.size(); ++i) {
            this.addEntry(this.elementDeserializer.apply(list.getCompound(i)));
        }
    }

    public void toTag(NBTTagList list) {
        for (int i = 0; i < this.size; ++i) {
            list.add(this.elementSerializer.apply(this.get(i)));
        }
    }

    public int getSize() {
        return this.size;
    }

    private void clear() {
        Arrays.fill(this.entries, null);
        this.table.clear();
        this.size = 0;
    }

    // TISCM palette command added getter
    public List<T> getStates()
    {
        List<T> states = new ArrayList<>();
        for (int i = 0; i < this.getSize(); ++i)
        {
            states.add(this.get(i));
        }
        return states;
    }
}
