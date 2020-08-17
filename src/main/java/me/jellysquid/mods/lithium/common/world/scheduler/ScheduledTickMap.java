package me.jellysquid.mods.lithium.common.world.scheduler;

import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

class ScheduledTickMap<T> {
    private final Long2ObjectSortedMap<UpdateTimeIndex<T>> activeTicksByTime = new Long2ObjectAVLTreeMap<>();
    private final Map<NextTickListEntry<T>, Mut<T>> scheduled = new HashMap<>();

    void addScheduledTick(NextTickListEntry<T> tick) {
        Mut<T> mut = this.scheduled.computeIfAbsent(tick, Mut::new);

        if (mut.status == Status.SCHEDULED) {
            return;
        }

        mut.status = Status.SCHEDULED;

        UpdateList<T> idx = this.activeTicksByTime.computeIfAbsent(getTimeKey(tick.scheduledTime, tick.priority), UpdateTimeIndex::new)
                .computeIfAbsent(getChunkKey(tick.position), UpdateList::new);
        idx.add(mut);
    }

    private static long getChunkKey(BlockPos pos) {
        return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
    }

    boolean getScheduledTickStatus(BlockPos pos, T obj, boolean executing) {
        Mut<T> index = this.scheduled.get(new NextTickListEntry<>(pos, obj));

        return index != null && index.status == (executing ? Status.EXECUTING : Status.SCHEDULED);
    }

    private static long getTimeKey(long time, TickPriority priority) {
        return (time << 4L) | (priority.ordinal() & 15);
    }

    Iterator<UpdateList<T>> getTicksForChunk(long chunk) {
        return this.activeTicksByTime
                .values()
                .stream()
                .filter(table -> table.key == chunk)
                .flatMap(table -> table.values().stream())
                .iterator();
    }

    Iterable<Mut<T>> getAllTicks() {
        return this.scheduled.values();
    }

    private final ArrayList<UpdateList<T>> updating = new ArrayList<>();

    void cleanup(ChunkProviderServer chunks, long time) {
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (ObjectIterator<UpdateTimeIndex<T>> timeIdxIt = this.activeTicksByTime.headMap(time << 4L).values().iterator(); timeIdxIt.hasNext(); ) {
            UpdateTimeIndex<T> table = timeIdxIt.next();

            for (Iterator<UpdateList<T>> chunkIdxIt = table.values().iterator(); chunkIdxIt.hasNext(); ) {
                UpdateList<T> list = chunkIdxIt.next();

                int chunkX = ChunkPos.getX(list.key);
                int chunkZ = ChunkPos.getZ(list.key);

                if (list.executed) {
                    chunkIdxIt.remove();

                    continue;
                }

                // Hack to determine if the chunk is loaded
                if (chunks.chunkExists(chunkX,chunkZ)) {
                    for (Mut<T> mut : list) {
                        mut.status = Status.EXECUTING;
                    }

                    this.updating.add(list);

                    list.executed = true;
                }
            }

            if (table.isEmpty()) {
                timeIdxIt.remove();
            }
        }
    }

    void performTicks(Consumer<NextTickListEntry<T>> consumer) {
        for (UpdateList<T> list : this.updating) {
            this.execute(list, consumer);
        }

        this.updating.clear();
    }

    private void execute(UpdateList<T> list, Consumer<NextTickListEntry<T>> consumer) {
        for (Mut<T> mut : list) {
            try {
                mut.status = Status.CONSUMED;

                consumer.accept(mut.tick);
            } catch (Throwable e) {
                CrashReport crash = CrashReport.makeCrashReport(e, "Exception while ticking");
                CrashReportCategory section = crash.makeCategory("Block being ticked");
                CrashReportCategory.addBlockInfo(section, mut.tick.position, null);
                throw new ReportedException(crash);
            }
        }
    }

    int getScheduledCount() {
        int count = 0;

        for (Mut<T> mut : this.scheduled.values()) {
            if (mut.status == Status.SCHEDULED) {
                count += 1;
            }
        }

        return count;
    }

    private static class UpdateTimeIndex<T> extends Long2ObjectOpenHashMap<UpdateList<T>> {
        private final long key;

        private UpdateTimeIndex(long key) {
            this.key = key;
        }
    }

    static class UpdateList<T> extends ArrayList<Mut<T>> {
        private final long key;
        private boolean executed = false;

        private UpdateList(long key) {
            this.key = key;
        }
    }

    enum Status {
        SCHEDULED,
        EXECUTING,
        CONSUMED
    }

    static class Mut<T> {
        final NextTickListEntry<T> tick;

        Status status = null;

        private Mut(NextTickListEntry<T> tick) {
            this.tick = tick;
        }
    }

    void removeTick(NextTickListEntry<T> tick) {
        this.scheduled.remove(tick);
    }
}
