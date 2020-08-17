package me.jellysquid.mods.lithium.common.world.scheduler;

import com.google.common.collect.Lists;
import me.jellysquid.mods.lithium.common.world.scheduler.ScheduledTickMap.Status;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LithiumServerTickScheduler<T> extends ServerTickList<T> {
    private final Predicate<T> invalidObjPredicate;
    private final Function<T, ResourceLocation> idToName;
    private final Function<ResourceLocation, T> nameToId;
    private final WorldServer world;
    private final Consumer<NextTickListEntry<T>> tickConsumer;
    private final ScheduledTickMap<T> tickMap = new ScheduledTickMap<>();

    public LithiumServerTickScheduler(WorldServer world, Predicate<T> invalidPredicate, Function<T, ResourceLocation> idToName, Function<ResourceLocation, T> nameToId, Consumer<NextTickListEntry<T>> consumer) {
        super(world, invalidPredicate, idToName, nameToId, consumer);

        this.invalidObjPredicate = invalidPredicate;
        this.idToName = idToName;
        this.nameToId = nameToId;
        this.world = world;
        this.tickConsumer = consumer;
    }

    @Override
    public void tick() {
        this.world.profiler.startSection("cleaning");

        this.tickMap.cleanup(this.world.getChunkProvider(), this.world.getGameTime() + 1);

        this.world.profiler.endStartSection("executing");

        this.tickMap.performTicks(this.tickConsumer);

        this.world.profiler.endSection();
    }

    @Override
    public boolean isTickPending(BlockPos pos, T obj) {
        return this.tickMap.getScheduledTickStatus(pos, obj, true);
    }

    @Override
    public List<NextTickListEntry<T>> getPending(Chunk chunk, boolean remove) {
        List<NextTickListEntry<T>> ret = new ArrayList<>();

        Iterator<ScheduledTickMap.UpdateList<T>> listIt = this.tickMap.getTicksForChunk(chunk.getPos().asLong());

        while (listIt.hasNext()) {
            ScheduledTickMap.UpdateList<T> next = listIt.next();

            for (Iterator<ScheduledTickMap.Mut<T>> mutIt = next.iterator(); mutIt.hasNext(); ) {
                ScheduledTickMap.Mut<T> mut = mutIt.next();

                if (mut.status == Status.CONSUMED) {
                    continue;
                }

                NextTickListEntry<T> tick = mut.tick;
                ret.add(tick);

                if (remove) {
                    mutIt.remove();

                    this.tickMap.removeTick(tick);
                }
            }
        }

        return ret;
    }

    @Override
    public List<NextTickListEntry<T>> getPending(MutableBoundingBox box, boolean remove) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyTicks(MutableBoundingBox box, BlockPos pos) {
        List<NextTickListEntry<T>> ret = null;

        for (ScheduledTickMap.Mut<T> mut : this.tickMap.getAllTicks()) {
            NextTickListEntry<T> tick = mut.tick;

            if (tick.position.getX() >= box.minX && tick.position.getX() < box.maxX && tick.position.getZ() >= box.minZ && tick.position.getZ() < box.maxZ) {
                if (ret == null) {
                    ret = Lists.newArrayList();
                }

                ret.add(tick);
            }
        }

        if (ret == null) {
            return;
        }

        for (NextTickListEntry<T> tick : ret) {
            if (box.isVecInside(tick.position)) {
                this.addScheduledTick(new NextTickListEntry<>(tick.position.add(pos), tick.getTarget(), tick.scheduledTime, tick.priority));
            }
        }
    }

    @Override
    public NBTTagList write(Chunk chunk) {
        List<NextTickListEntry<T>> ticks = this.getPending(chunk, false);
        return serializeScheduledTicks(this.idToName, ticks, this.world.getGameTime());
    }

    @Override
    public void read(NBTTagList p_205369_1_) {
        for (int i = 0; i < p_205369_1_.size(); ++i) {
            NBTTagCompound nbttagcompound = p_205369_1_.getCompound(i);
            T t = this.nameToId.apply(new ResourceLocation(nbttagcompound.getString("i")));

            if (t != null) {
                this.scheduleTick(
                        new BlockPos(nbttagcompound.getInt("x"), nbttagcompound.getInt("y"), nbttagcompound.getInt("z")),
                        t,
                        nbttagcompound.getInt("t"),
                        TickPriority.getPriority(nbttagcompound.getInt("p"))
                );
            }
        }
    }

    public static <T> NBTTagList serializeScheduledTicks(Function<T, ResourceLocation> function_1, Iterable<NextTickListEntry<T>> ticks, long offset) {
        NBTTagList listTag_1 = new NBTTagList();

        for (NextTickListEntry<T> tick : ticks) {
            NBTTagCompound compoundTag_1 = new NBTTagCompound();
            compoundTag_1.putString("i", function_1.apply(tick.getTarget()).toString());
            compoundTag_1.putInt("x", tick.position.getX());
            compoundTag_1.putInt("y", tick.position.getY());
            compoundTag_1.putInt("z", tick.position.getZ());
            compoundTag_1.putInt("t", (int) (tick.scheduledTime - offset));
            compoundTag_1.putInt("p", tick.priority.getPriority());
            listTag_1.add(compoundTag_1);
        }

        return listTag_1;
    }

    @Override
    public boolean isTickScheduled(BlockPos pos, T obj) {
        return this.tickMap.getScheduledTickStatus(pos, obj, false);
    }

    @Override
    public void scheduleTick(BlockPos pos, T obj, int delay, TickPriority priority) {
        if (!this.invalidObjPredicate.test(obj)) {
            this.addScheduledTick(new NextTickListEntry<>(pos, obj, (long) delay + this.world.getGameTime(), priority));
        }
    }

    private void addScheduledTick(NextTickListEntry<T> tick) {
        this.tickMap.addScheduledTick(tick);
    }
}