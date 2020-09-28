package me.jellysquid.mods.lithium.common.world.scheduler;

import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.ServerTickList;
import net.minecraft.world.TickPriority;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provides greatly improved performance when compared to the vanilla tick scheduler. Key highlights:
 * - Instead of using a TreeSet collection (which is generally very slow, relatively speaking) for ordering updates, we
 *   make use of bucketed array queues indexed by a unique key composed of the scheduled time and priority. When
 *   iterating back over these updates, we simply specify the maximum bucket key range to avoid iterating over
 *   unnecessary elements. Integer bucket keys are much faster to sort against (as they are logically sorted) and
 *   are computationally trivial to slice.
 *
 * - A single single collection is used for storing ticks in the pipeline and execution flags are set on the scheduled
 *   objects directly. This eliminates the need to move ticks between multiple queues and sets constantly.
 *
 * - We avoid repeatedly asking if a chunk is available by trying to re-use the previous computation if it involves the
 *   same chunk, reducing a lot of map operations elsewhere.
 *
 * - Ticks are stored in a HashMap with their execution state, meaning that redstone gates and other blocks which check
 *   to see if something is scheduled/executing will not have to scan a potentially very large array (which can occur
 *   when many ticks have been scheduled.)
 */
public class LithiumServerTickScheduler<T> extends ServerTickList<T> {
    private static final Predicate<TickEntry<?>> PREDICATE_ANY_TICK = entry -> true;
    private static final Predicate<TickEntry<?>> PREDICATE_ACTIVE_TICKS = entry -> !entry.consumed;

    private final Long2ObjectSortedMap<TickEntryQueue<T>> scheduledTicksOrdered = new Long2ObjectAVLTreeMap<>();
    private final Long2ObjectOpenHashMap<Set<TickEntry<T>>> scheduledTicksByChunk = new Long2ObjectOpenHashMap<>();

    private final Map<NextTickListEntry<T>, TickEntry<T>> scheduledTicks = new HashMap<>();
    private final ArrayList<TickEntry<T>> executingTicks = new ArrayList<>();

    private final Predicate<T> invalidObjPredicate;
    private final WorldServer world;
    private final Consumer<NextTickListEntry<T>> tickConsumer;

    public LithiumServerTickScheduler(WorldServer world, Predicate<T> invalidPredicate, Function<T, ResourceLocation> idToName, Function<ResourceLocation, T> deserializerIn, Consumer<NextTickListEntry<T>> tickConsumer) {
        super(world, invalidPredicate, idToName, deserializerIn, tickConsumer);

        this.invalidObjPredicate = invalidPredicate;
        this.world = world;
        this.tickConsumer = tickConsumer;
    }

    @Override
    public void tick() {
        this.world.profiler.startSection("cleaning");

        this.selectTicks(this.world.getChunkProvider(), this.world.getGameTime());

        this.world.profiler.endStartSection("executing");

        this.executeTicks(this.tickConsumer);

        this.world.profiler.endSection();
    }

    @Override
    public boolean isTickPending(BlockPos pos, T obj) {
        TickEntry<T> entry = this.scheduledTicks.get(new NextTickListEntry<>(pos, obj));

        if (entry == null) {
            return false;
        }

        return entry.executing;
    }

    @Override
    public boolean isTickScheduled(BlockPos pos, T obj) {
        TickEntry<T> entry = this.scheduledTicks.get(new NextTickListEntry<>(pos, obj));

        if (entry == null) {
            return false;
        }

        return entry.scheduled;
    }

    @Override
    public List<NextTickListEntry<T>> getPending(Chunk chunk, boolean mutates) {
        ChunkPos chunkPos = chunk.getPos();
        MutableBoundingBox box = new MutableBoundingBox(chunkPos.getXStart() - 2, chunkPos.getZStart() - 2, chunkPos.getXEnd() + 2, chunkPos.getZEnd() + 2);
        return this.getPending(box, mutates);
    }

    public List<NextTickListEntry<T>> getPending(MutableBoundingBox box, boolean remove) {
        return this.collectTicks(box, remove, PREDICATE_ACTIVE_TICKS);
    }

    @Override
    public void copyTicks(MutableBoundingBox box, BlockPos pos) {
        List<NextTickListEntry<T>> list = this.getPending(box, false);

        for (NextTickListEntry<T> tick : list) {
            this.addScheduledTick(new NextTickListEntry<>(tick.position.add(pos), tick.getTarget(), tick.scheduledTime, tick.priority));
        }
    }

    @Override
    public void scheduleTick(BlockPos pos, T obj, int delay, TickPriority priority) {
        // An extra isBlockLoaded check is needed in 1.13
        if (this.world.isBlockLoaded(pos)) {
            this.scheduleUpdateNoLoadedCheck(pos, obj, delay, priority);
        }
    }

    /**
     * Method that only exists in 1.13
     * It is used to add TileTick events from the disk
     * @author Fallen_Breath
     */
    @Override
    protected void scheduleUpdateNoLoadedCheck(BlockPos pos, T obj, int delay, TickPriority priority) {
        if (!this.invalidObjPredicate.test(obj)) {
            this.addScheduledTick(new NextTickListEntry<>(pos, obj, (long) delay + this.world.getGameTime(), priority));
        }
    }

    /**
     * It should never invoke this
     * @author Fallen_Breath
     */
    @Override
    protected void addEntry(BlockPos p_205370_1_, T p_205370_2_, int p_205370_3_, TickPriority p_205370_4_) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the number of currently scheduled ticks.
     */
    public int getTicks() {
        int count = 0;

        for (TickEntry<T> entry : this.scheduledTicks.values()) {
            if (entry.scheduled) {
                count += 1;
            }
        }

        return count;
    }

    /**
     * Enqueues all scheduled ticks before the specified time and prepares them for execution.
     */
    public void selectTicks(ChunkProviderServer chunkManager, long time) {
        // Calculates the maximum key value which includes all ticks scheduled before the specified time
        long headKey = getBucketKey(time + 1, TickPriority.EXTREMELY_HIGH) - 1;

        // [VanillaCopy] ServerTickScheduler#tick
        // In order to fulfill the promise of not breaking vanilla behaviour, we keep the vanilla artifact of
        // tick suppression.
        int limit = 65565;

        boolean canTick = true;
        long prevChunk = Long.MIN_VALUE;

        // Create an iterator over only
        Iterator<TickEntryQueue<T>> it = this.scheduledTicksOrdered.headMap(headKey).values().iterator();

        // Iterate over all scheduled ticks and enqueue them for until we exceed our budget
        while (limit > 0 && it.hasNext()) {
            TickEntryQueue<T> list = it.next();

            // Pointer for writing scheduled ticks back into the queue
            int w = 0;

            // Re-builds the scheduled tick queue in-place
            for (int i = 0; i < list.size(); i++) {
                TickEntry<T> tick = list.getTickAtIndex(i);

                if (!tick.scheduled) {
                    continue;
                }

                // If no more ticks can be scheduled for execution this phase, then we leave it in its current time
                // bucket and skip it. This deliberately introduces a bug where backlogged ticks will not be re-scheduled
                // properly, re-producing the vanilla issue of tick suppression.
                if (limit > 0) {
                    long chunk = ChunkPos.asLong(tick.position.getX() >> 4, tick.position.getZ() >> 4);

                    // Take advantage of the fact that if any position in a chunk can be updated, then all other positions
                    // in the same chunk can be updated. This avoids the more expensive check to the chunk manager.
                    if (prevChunk != chunk) {
                        prevChunk = chunk;
                        // canTick = chunkManager.shouldTickBlock(tick.pos);
                        canTick = world.isBlockLoaded(tick.position);
                        //TODO: Verify if it completely copies vanilla behavior
                    }

                    // If the tick can be executed right now, then add it to the executing list and decrement our
                    // budget limit.
                    if (canTick) {
                        tick.scheduled = false;
                        tick.executing = true;

                        this.executingTicks.add(tick);

                        limit--;

                        // Avoids the tick being kept in the scheduled queue
                        continue;
                    }
                }

                // Nothing happened to this tick, so re-add it to the queue
                list.setTickAtIndex(w++, tick);
            }

            // Finalize our changes to the queue and notify it of the new length
            list.resize(w);

            // If the queue is empty, remove it from the map
            if (list.isEmpty()) {
                it.remove();
            }
        }
    }

    public void executeTicks(Consumer<NextTickListEntry<T>> consumer) {
        // Mark and execute all executing ticks
        for (TickEntry<T> tick : this.executingTicks) {
            try {
                // Mark as consumed before execution per vanilla behaviour
                tick.executing = false;

                // Perform tick execution
                consumer.accept(tick);

                // If the tick didn't get re-scheduled, we're finished and this tick should be deleted
                if (!tick.scheduled) {
                    this.removeTickEntry(tick);
                }
            } catch (Throwable e) {
                CrashReport crash = CrashReport.makeCrashReport(e, "Exception while ticking");
                CrashReportCategory section = crash.makeCategory("Block being ticked");
                CrashReportCategory.addBlockInfo(section, tick.position, null);

                throw new ReportedException(crash);
            }
        }


        // We finished executing those ticks, so empty the list.
        this.executingTicks.clear();
    }

    private List<NextTickListEntry<T>> collectTicks(MutableBoundingBox box, boolean remove, Predicate<TickEntry<?>> predicate) {
        List<NextTickListEntry<T>> ret = new ArrayList<>();

        int minChunkX = box.minX >> 4;
        int maxChunkX = box.maxX >> 4;

        int minChunkZ = box.minZ >> 4;
        int maxChunkZ = box.maxZ >> 4;

        // Iterate over all chunks encompassed by the block box
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                long chunk = ChunkPos.asLong(chunkX, chunkZ);

                Set<TickEntry<T>> set = this.scheduledTicksByChunk.get(chunk);

                if (set == null) {
                    continue;
                }

                for (TickEntry<T> tick : set) {
                    if (!box.isVecInside(tick.position) || !predicate.test(tick)) {
                        continue;
                    }

                    ret.add(tick);
                }
            }
        }

        if (remove) {
            for (NextTickListEntry<T> tick : ret) {
                // It's not possible to downcast a collection, so we have to upcast here
                // This will always succeed
                this.removeTickEntry((TickEntry<T>) tick);
            }
        }

        return ret;
    }

    /**
     * Schedules a tick for execution if it has not already been. To match vanilla, we do not re-schedule matching
     * scheduled ticks which are set to execute at a different time.
     */
    private void addScheduledTick(NextTickListEntry<T> tick) {
        TickEntry<T> entry = this.scheduledTicks.computeIfAbsent(tick, this::createTickEntry);

        if (!entry.scheduled) {
            TickEntryQueue<T> timeIdx = this.scheduledTicksOrdered.computeIfAbsent(getBucketKey(tick.scheduledTime, tick.priority), key -> new TickEntryQueue<>());
            timeIdx.push(entry);

            entry.scheduled = true;
        }
    }

    private TickEntry<T> createTickEntry(NextTickListEntry<T> tick) {
        Set<TickEntry<T>> chunkIdx = this.scheduledTicksByChunk.computeIfAbsent(getChunkKey(tick.position), LithiumServerTickScheduler::createChunkIndex);

        return new TickEntry<>(tick, chunkIdx);
    }

    private void removeTickEntry(TickEntry<T> tick) {
        tick.scheduled = false;
        tick.consumed = true;

        tick.chunkIdx.remove(tick);

        if (tick.chunkIdx.isEmpty()) {
            this.scheduledTicksByChunk.remove(getChunkKey(tick.position));
        }

        this.scheduledTicks.remove(tick);
    }

    private static <T> Set<TickEntry<T>> createChunkIndex(long pos) {
        return new ObjectOpenHashSet<>(8);
    }

    // Computes a chunk key from a block position
    private static long getChunkKey(BlockPos pos) {
        return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
    }

    // Computes a timestamped key including the tick's priority
    // Keys can be sorted in descending order to find what should be executed first
    // 60 time bits, 4 priority bits
    private static long getBucketKey(long time, TickPriority priority) {
        return (time << 4L) | (priority.ordinal() & 15);
    }
}