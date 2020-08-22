package me.jellysquid.mods.lithium.common.util;

import com.google.common.collect.Iterators;
import net.minecraft.util.ClassInheritanceMultiMap;

import java.util.*;
import java.util.stream.Collectors;

public class LithiumClassInheritanceMultiMap<T> extends ClassInheritanceMultiMap<T> {
    public LithiumClassInheritanceMultiMap(Class<T> baseClassIn) {
        super(baseClassIn, true);
    }


    @Override
    public boolean add(T e) {
        boolean bl = false;
        for (Map.Entry<Class<?>, List<T>> classListEntry : this.map.entrySet()) {
            Map.Entry entry = (Map.Entry) classListEntry;
            if (((Class) entry.getKey()).isInstance(e)) {
                bl |= ((List) entry.getValue()).add(e);
            }
        }
        return bl;
    }

    @Override
    public boolean remove(Object o) {
        boolean bl = false;
        for (Map.Entry<Class<?>, List<T>> classListEntry : this.map.entrySet()) {
            if (((Class) ((Map.Entry) classListEntry).getKey()).isInstance(o)) {
                List list = (List) ((Map.Entry) classListEntry).getValue();
                bl |= list.remove(o);
            }
        }
        return bl;
    }

    @Override
    public boolean contains(Object o) {
        return ((Collection) this.getByClass(o.getClass())).contains(o);
    }

    @Override
    public <S> Iterable<S> getByClass(Class<S> type) {
        List<T> list = this.map.computeIfAbsent(type, (t) -> {
            if (!this.baseClass.isAssignableFrom(t)) {
                throw new IllegalArgumentException("Don't know how to search for " + t);
            }
            return this.values.stream().filter(t::isInstance).collect(Collectors.toList());
        });
        return (Collection<S>) Collections.unmodifiableCollection(list);
    }


    @Override
    public Iterator<T> iterator() {
        return (Iterator<T>) (this.values.isEmpty() ? Collections.emptyIterator() : Iterators.unmodifiableIterator(this.values.iterator()));
    }

    @Override
    public int size() {
        return this.values.size();
    }
}
