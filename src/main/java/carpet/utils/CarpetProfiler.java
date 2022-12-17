package carpet.utils;

import carpet.settings.CarpetSettings;
import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CarpetProfiler
{
    private static final Object2LongLinkedOpenHashMap<SectionKey> SECTION_STATS = new Object2LongLinkedOpenHashMap<>();
    private static final Object2LongLinkedOpenHashMap<SectionKey> ENTITY_TIMES = new Object2LongLinkedOpenHashMap<>();
    private static final Object2LongLinkedOpenHashMap<SectionKey> ENTITY_COUNT = new Object2LongLinkedOpenHashMap<>();
    private static final SectionKey GAMETICK_KEY = new SectionKey(null, "tick", false);
    public static int tick_health_requested = 0;
    private static int tick_health_elapsed = 0;
    private static TestType test_type = TestType.NONE; //1 for ticks, 2 for entities;
    private static ProfilerToken current_section = null;
    private static long current_tick_start = 0;

    private enum TestType
    {
        NONE, TICK, ENTITIES
    }

    private static boolean isTesting(TestType type)
    {
        return tick_health_requested != 0L && test_type == type && current_tick_start != 0L;
    }

    public static void prepare_tick_report(int ticks)
    {
        //maybe add so it only spams the sending player, but honestly - all may want to see it
        SECTION_STATS.clear();
        test_type = TestType.TICK;

        tick_health_elapsed = ticks;
        tick_health_requested = ticks;
        current_tick_start = 0L;
        current_section = null;
    }

    public static void prepare_entity_report(int ticks)
    {
        //maybe add so it only spams the sending player, but honestly - all may want to see it
        ENTITY_COUNT.clear();
        ENTITY_TIMES.clear();
        test_type = TestType.ENTITIES;

        tick_health_elapsed = ticks;
        tick_health_requested = ticks;
        current_tick_start = 0L;
        current_section = null;
    }

    public static void start_section(@Nullable DimensionType dimension, String name)
    {
        if (isTesting(TestType.TICK))
        {
            if (current_section != null)
            {
                end_current_section();
            }
            current_section = new ProfilerToken(dimension, name, false);
        }
    }

    public static ProfilerToken start_section_concurrent(DimensionType dimension, String name, boolean isRemote)
    {
        return isTesting(TestType.TICK) ? new ProfilerToken(dimension, name, isRemote) : null;
    }

    public static ProfilerToken start_entity_section(DimensionType dimension, Entity e)
    {
        return isTesting(TestType.ENTITIES) ? new ProfilerToken(e.getEntityWorld(), e.getType()) : null;
    }

    public static ProfilerToken start_tileentity_section(DimensionType dimension, TileEntity te)
    {
        return isTesting(TestType.ENTITIES) && te.getWorld() != null ? new ProfilerToken(te.getWorld(), te.getType()) : null;
    }

    public static void end_current_section()
    {
        if (isTesting(TestType.TICK))
        {
            if (current_section == null)
            {
                CarpetSettings.LOG.error("finishing section that hasn't started");
                return;
            }
            current_section.endTickSection();
            current_section = null;
        }
    }

    public static void end_current_section_concurrent(ProfilerToken tok)
    {
        if (isTesting(TestType.TICK))
        {
            if (tok == null)
            {
                CarpetSettings.LOG.error("finishing section that hasn't started");
                return;
            }
            tok.endTickSection();
        }
    }

    public static void end_current_entity_section(ProfilerToken tok)
    {
        if (isTesting(TestType.ENTITIES))
        {
            if (tok == null)
            {
                CarpetSettings.LOG.error("finishing entity/TE section that hasn't started");
                return;
            }
            tok.endEntitySection();
        }
    }

    public static void start_tick_profiling()
    {
        current_tick_start = System.nanoTime();
    }

    public static void end_tick_profiling(MinecraftServer server)
    {
        if (current_tick_start == 0L)
        {
            return;
        }
        SECTION_STATS.addTo(GAMETICK_KEY, System.nanoTime()-current_tick_start);
        tick_health_elapsed --;
        //CarpetSettings.LOG.error("tick count current at "+time_repo.get("tick"));
        if (tick_health_elapsed <= 0)
        {
            finalize_tick_report(server);
        }
    }

    public static void finalize_tick_report(MinecraftServer server)
    {
        if (test_type == TestType.TICK)
        {
            finalize_tick_report_for_time(server);
        }
        if (test_type == TestType.ENTITIES)
        {
            finalize_tick_report_for_entities(server);
        }
        cleanup_tick_report();
    }

    private static void cleanup_tick_report()
    {
        test_type = TestType.NONE;
        tick_health_elapsed = 0;
        tick_health_requested = 0;
        current_tick_start = 0L;
        current_section = null;
    }

    private static final NumberFormat nf = Util.make(() -> {
        DecimalFormat nf = new DecimalFormat();
        nf.setMinimumFractionDigits(3);
        nf.setMaximumFractionDigits(3);
        return nf;
    });

    private static void broadcast(String formatter, Object... args)
    {
        Messenger.broadcast(Messenger.formatting(Messenger.format(formatter, args), TextFormatting.ITALIC, TextFormatting.GRAY));
    }

    public static void finalize_tick_report_for_time(MinecraftServer server)
    {
        //print stats
        long total_tick_time = SECTION_STATS.getLong(GAMETICK_KEY);
        double divider = 1.0D/tick_health_requested/1000000;
        broadcast("Average tick time: %sms",nf.format(divider*total_tick_time));
        SECTION_STATS.removeLong(GAMETICK_KEY);

        AtomicDouble accumulated = new AtomicDouble(0L);
        Map<Optional<DimensionType>, List<Pair<SectionKey, Double>>> stats = SECTION_STATS.object2LongEntrySet().
                stream().
                map(entry -> Pair.of(entry.getKey(), entry.getLongValue() * divider)).
                filter(pair -> pair.getSecond() >= 0.01).
                peek(pair -> accumulated.addAndGet(pair.getSecond())).
                collect(Collectors.groupingBy(pair -> Optional.ofNullable(pair.getFirst().dimensionType)));

        stats.getOrDefault(Optional.<DimensionType>empty(), Collections.emptyList()).forEach(pair -> {
            broadcast("%s: %sms", pair.getFirst().name(), nf.format(pair.getSecond()));
        });

        Arrays.asList(DimensionType.OVERWORLD, DimensionType.NETHER, DimensionType.THE_END).forEach(dimensionType -> {
            List<Pair<SectionKey, Double>> list = stats.get(Optional.of(dimensionType));
            if (list != null)
            {
                broadcast("%s:", Messenger.dimension(dimensionType));
                list.forEach(pair -> {
                    broadcast(" - %s: %sms", pair.getFirst().name(), nf.format(pair.getSecond()));
                });
            }
        });

        double rest = total_tick_time * divider - accumulated.get();
        broadcast(String.format("The Rest, whatever that might be: %sms", nf.format(rest)));
    }

    public static void finalize_tick_report_for_entities(MinecraftServer server)
    {
        //print stats
        long total_tick_time = SECTION_STATS.getLong(GAMETICK_KEY);
        double divider = 1.0D/tick_health_requested/1000000;
        broadcast("Average tick time: %sms",nf.format(divider*total_tick_time));
        SECTION_STATS.removeLong(GAMETICK_KEY);

        int topN = 10;
        Messenger.print_server_message(server, "Top " + topN + " counts:");
        ENTITY_COUNT.object2LongEntrySet().stream().
                sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
                limit(topN).
                forEach(entry -> {
                    SectionKey key = entry.getKey();
                    double count = 1.0D * entry.getLongValue() / tick_health_requested;
                    broadcast(" - %s (%s): %s", key.name(), Messenger.dimension(key.dimensionType), nf.format(count));
                });
        Messenger.print_server_message(server, "Top 10 grossing:");
        ENTITY_TIMES.object2LongEntrySet().stream().
                sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
                limit(topN).
                forEach(entry -> {
                    SectionKey key = entry.getKey();
                    double ms = divider * entry.getLongValue();
                    broadcast(" - %s (%s): %sms",key.name(), Messenger.dimension(key.dimensionType), nf.format(ms));
                });
    }

    public static class SectionKey
    {
        public final DimensionType dimensionType;
        public final Object section;
        public final boolean isClient;

        public SectionKey(DimensionType dimensionType, Object section, boolean isClient)
        {
            this.dimensionType = dimensionType;
            this.section = section;
            this.isClient = isClient;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SectionKey that = (SectionKey)o;
            return isClient == that.isClient && Objects.equals(dimensionType, that.dimensionType) && Objects.equals(section, that.section);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(dimensionType, section, isClient);
        }

        public ITextComponent name()
        {
            ITextComponent name;
            if (this.section instanceof EntityType<?>)
            {
                name = Messenger.entityType((EntityType<?>)this.section);
            }
            else if (this.section instanceof TileEntityType<?>)
            {
                name = Messenger.blockEntity((TileEntityType<?>)this.section);
            }
            else
            {
                name = Messenger.s(this.section);
            }
            if (this.isClient)
            {
                name = Messenger.c(name, Messenger.s(" (client)"));
            }
            return name;
        }
    }

    public static class ProfilerToken
    {
        public final long start;
        public final SectionKey key;

        public ProfilerToken(DimensionType dimensionType, Object section, boolean isClient)
        {
            this.start = System.nanoTime();
            this.key = new SectionKey(dimensionType, section, isClient);
        }

        public ProfilerToken(World world, Object section)
        {
            this(world.getDimension().getType(), section, world.isRemote);
        }

        public void endTickSection()
        {
            long duration = System.nanoTime() - this.start;
            SECTION_STATS.addTo(this.key, duration);
        }

        public void endEntitySection()
        {
            long duration = System.nanoTime() - this.start;
            ENTITY_TIMES.addTo(this.key, duration);
            ENTITY_COUNT.addTo(this.key, 1);
        }
    }
}
