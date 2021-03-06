package me.jellysquid.mods.lithium.common.util.collections;

import carpet.utils.TISCMOptimizationConfig;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.*;

@SuppressWarnings("NullableProblems")
public class TileEntityList implements List<TileEntity> {
    //TileEntityList does not support double-add of the same object. But it does support multiple at the same position.
    //This collection behaves like a set with insertion order. It also provides a position->TileEntity lookup.

    // TISCM Larger tile entity list
    // pre-allocate 256 volume in hashsets/hashmaps to avoid constantly rehash when the amount of TileEntity is small
    @SuppressWarnings("FieldCanBeLocal")
    private final int COLLECTION_DEFAULT_SIZE = TISCMOptimizationConfig.LARGER_TILE_ENTITY_LIST ? 256 : Long2ReferenceOpenHashMap.DEFAULT_INITIAL_SIZE;

    private final ReferenceLinkedOpenHashSet<TileEntity> allBlockEntities;

    //When there is only 1 TileEntity at a position, it is stored in posMap.
    //When there are multiple at a position, the first added is stored in posMap
    //and all of them are stored in posMapMulti using a List (in the order they were added)
    private final Long2ReferenceOpenHashMap<TileEntity> posMap;
    private final Long2ReferenceOpenHashMap<List<TileEntity>> posMapMulti;
    public TileEntityList(List<TileEntity> list, boolean hasPositionLookup) {
        this.posMap = hasPositionLookup ? new Long2ReferenceOpenHashMap<>(COLLECTION_DEFAULT_SIZE) : null;
        this.posMapMulti = hasPositionLookup ? new Long2ReferenceOpenHashMap<>(COLLECTION_DEFAULT_SIZE) : null;

        if (this.posMap != null) {
            this.posMap.defaultReturnValue(null);
            this.posMapMulti.defaultReturnValue(null);
        }

        this.allBlockEntities = new ReferenceLinkedOpenHashSet<>(COLLECTION_DEFAULT_SIZE);
        this.addAll(list);
    }

    @Override
    public int size() {
        return this.allBlockEntities.size();
    }

    @Override
    public boolean isEmpty() {
        return this.allBlockEntities.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.allBlockEntities.contains(o);
    }

    @Override
    public Iterator<TileEntity> iterator() {
        return this.allBlockEntities.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.allBlockEntities.toArray();
    }

    @Override
    @SuppressWarnings("SuspiciousToArrayCall")
    public <T> T[] toArray(T[] a) {
        return this.allBlockEntities.toArray(a);
    }

    @Override
    public boolean add(TileEntity TileEntity) {
        return this.addNoDoubleAdd(TileEntity, false);  // why true
    }

    private boolean addNoDoubleAdd(TileEntity TileEntity, boolean exceptionOnDoubleAdd) {
        boolean added = this.allBlockEntities.add(TileEntity);
        if (!added && exceptionOnDoubleAdd) {
            this.throwException(TileEntity);
        }

        if (added && this.posMap != null) {
            long pos = getEntityPos(TileEntity);

            TileEntity prev = this.posMap.putIfAbsent(pos, TileEntity);
            if (prev != null) {
                List<TileEntity> multiEntry = this.posMapMulti.computeIfAbsent(pos, (long l) -> new ArrayList<>());
                if (multiEntry.size() == 0) {
                    //newly created multi entry: make sure it contains all elements
                    multiEntry.add(prev);
                }
                multiEntry.add(TileEntity);
            }
        }
        return added;
    }

    private void throwException(TileEntity TileEntity) {
        throw new IllegalStateException("Lithium TileEntityList" + (this.posMap != null ? " with posMap" : "") + ": Adding the same TileEntity object twice: " + TileEntity.write(new NBTTagCompound()));
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof TileEntity) {
            TileEntity TileEntity = (TileEntity) o;
            if (this.allBlockEntities.remove(o)) {
                if (this.posMap != null) {
                    long pos = getEntityPos(TileEntity);
                    List<TileEntity> multiEntry = this.posMapMulti.get(pos);
                    if (multiEntry != null) {
                        multiEntry.remove(TileEntity);
                        if (multiEntry.size() <= 1) {
                            this.posMapMulti.remove(pos);
                        }
                    }
                    if (multiEntry != null && multiEntry.size() > 0) {
                        this.posMap.put(pos, multiEntry.get(0));
                    } else {
                        this.posMap.remove(pos);
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.allBlockEntities.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends TileEntity> c) {
        for (TileEntity TileEntity : c) {
            this.add(TileEntity);
        }

        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends TileEntity> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;

        for (Object obj : c) {
            modified |= this.remove(obj);
        }

        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        for (TileEntity TileEntity : this.allBlockEntities) {
            if (!c.contains(TileEntity)) {
                modified |= this.remove(TileEntity);
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        this.allBlockEntities.clear();
        if (this.posMap != null) {
            this.posMap.clear();
            this.posMapMulti.clear();
        }
    }

    @Override
    public TileEntity get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TileEntity set(int index, TileEntity element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, TileEntity element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TileEntity remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<TileEntity> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<TileEntity> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TileEntity> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    private static long getEntityPos(TileEntity e) {
        return e.getPos().toLong();
    }


    public boolean addIfAbsent(TileEntity TileEntity) {
        //we are not checking position equality but object/reference equality (like vanilla)
        //the hashset prevents double add of the same object
        return this.addNoDoubleAdd(TileEntity, false);
    }

    @SuppressWarnings("unused")
    public boolean hasPositionLookup() {
        return this.posMap != null;
    }

    //Methods only supported when posMap is present!
    public void markRemovedAndRemoveAllAtPosition(BlockPos blockPos) {
        long pos = blockPos.toLong();
        TileEntity TileEntity = this.posMap.remove(pos);
        if (TileEntity != null) {
            List<TileEntity> multiEntry = this.posMapMulti.remove(pos);
            if (multiEntry != null) {
                for (TileEntity TileEntity1 : multiEntry) {
                    TileEntity1.remove();
                    this.allBlockEntities.remove(TileEntity1);
                }
            } else {
                TileEntity.remove();
                this.allBlockEntities.remove(TileEntity);
            }
        }
    }

    public TileEntity getFirstNonRemovedTileEntityAtPosition(long pos) {
        if (this.isEmpty()) {
            return null;
        }
        TileEntity TileEntity = this.posMap.get(pos);
        //usual case: we find no TileEntity or only one that also is not removed
        if (TileEntity == null || !TileEntity.isRemoved()) {
            return TileEntity;
        }
        //vanilla edge case: two BlockEntities at the same position
        //Look up in the posMultiMap to find the first non-removed TileEntity
        List<TileEntity> multiEntry = this.posMapMulti.get(pos);
        if (multiEntry != null) {
            for (TileEntity TileEntity1 : multiEntry) {
                if (!TileEntity1.isRemoved()) {
                    return TileEntity1;
                }
            }
        }
        return null;
    }
}
