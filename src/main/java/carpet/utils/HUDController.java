package carpet.utils;

import carpet.helpers.HopperCounter;
import carpet.helpers.TickSpeed;
import carpet.logging.AbstractHUDLogger;
import carpet.logging.LoggerRegistry;
import carpet.logging.lifetime.LifeTimeHUDLogger;
import carpet.logging.logHelpers.AutoSaveLogHelper;
import carpet.logging.logHelpers.PacketCounter;
import carpet.logging.threadstone.ThreadstoneLogger;
import carpet.logging.tickwarp.TickWarpHUDLogger;
import carpet.settings.CarpetSettings;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerListHeaderFooter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HUDController
{
    public static Map<EntityPlayer, List<ITextComponent>> player_huds = new HashMap<>();

    public static void addMessage(EntityPlayer player, ITextComponent hudMessage)
    {
        if (!player_huds.containsKey(player))
        {
            player_huds.put(player, new ArrayList<>());
        }
        else
        {
            player_huds.get(player).add(new TextComponentString("\n"));
        }
        player_huds.get(player).add(hudMessage);
    }
    public static void clear_player(EntityPlayer player)
    {
        SPacketPlayerListHeaderFooter packet = new SPacketPlayerListHeaderFooter();
        packet.setFooter(new TextComponentString(""));
        packet.setHeader(new TextComponentString(""));
        ((EntityPlayerMP)player).connection.sendPacket(packet);
    }


    public static void update_hud(MinecraftServer server)
    {
        if(server.getTickCounter() % CarpetSettings.HUDLoggerUpdateInterval != 0)
            return;

        player_huds.clear();

        if (LoggerRegistry.__tps)
            LoggerRegistry.getLogger("tps").log(()-> send_tps_display(server));

        if (LoggerRegistry.__mobcaps)
            LoggerRegistry.getLogger("mobcaps").log((option, player) -> {
                int dim = player.dimension.getId();
                switch (option)
                {
                    case "overworld":
                        dim = 0;
                        break;
                    case "nether":
                        dim = -1;
                        break;
                    case "end":
                        dim = 1;
                        break;
                }
                return send_mobcap_display(dim);
            });

        if(LoggerRegistry.__counter)
            LoggerRegistry.getLogger("counter").log((option)->send_counter_info(server, option));

        if (LoggerRegistry.__packets)
            LoggerRegistry.getLogger("packets").log(HUDController::packetCounter);

        if (LoggerRegistry.__villagecount)
            LoggerRegistry.getLogger("villagecount").log(() -> send_total_villages(server));

        if (LoggerRegistry.__memory)
            LoggerRegistry.getLogger("memory").log(HUDController::send_mem_usage);

        if (LoggerRegistry.__autosave)
            LoggerRegistry.getLogger("autosave").log(AutoSaveLogHelper::send_hud_info);


        doHudLogging(LoggerRegistry.__lifeTime, LifeTimeHUDLogger.NAME, LifeTimeHUDLogger.getInstance());
        doHudLogging(LoggerRegistry.__tickWarp, TickWarpHUDLogger.NAME, TickWarpHUDLogger.getInstance());
        doHudLogging(LoggerRegistry.__threadstone, ThreadstoneLogger.NAME, ThreadstoneLogger.getInstance());

        for (EntityPlayer player: player_huds.keySet())
        {
            SPacketPlayerListHeaderFooter packet = new SPacketPlayerListHeaderFooter();
            packet.setHeader(new TextComponentString(""));
            packet.setFooter(Messenger.c(player_huds.get(player).toArray(new Object[0])));
            ((EntityPlayerMP)player).connection.sendPacket(packet);
        }
    }

    // ported from carpet tis addition for easier formatting hud update
    private static void doHudLogging(boolean condition, String loggerName, AbstractHUDLogger logger)
    {
        if (condition)
        {
            LoggerRegistry.getLogger(loggerName).log(logger::onHudUpdate);
        }
    }

    private static final NumberFormat tpsNumberFormat = Util.make(() -> {
        DecimalFormat nf = new DecimalFormat();
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);
        return nf;
    });

    private static ITextComponent [] send_tps_display(MinecraftServer server)
    {
        double MSPT = MathHelper.average(server.tickTimeArray) * 1.0E-6D;
        double TPS = 1000.0D / Math.max((TickSpeed.time_warp_start_time != 0)?0.0:TickSpeed.mspt, MSPT);
        TextFormatting color = Messenger.heatmap_color(MSPT,TickSpeed.mspt);
        return new ITextComponent[]{Messenger.c(
                Messenger.s("TPS: ", TextFormatting.GRAY),
                Messenger.s(tpsNumberFormat.format(TPS), color),
                Messenger.s(" MSPT: ", TextFormatting.GRAY),
                Messenger.s(tpsNumberFormat.format(MSPT), color)
        )};
    }

    private static ITextComponent [] send_mobcap_display(int dim)
    {
        List<ITextComponent> components = new ArrayList<>();
        for (EnumCreatureType type:EnumCreatureType.values())
        {
            Tuple<Integer,Integer> counts = SpawnReporter.mobcaps.get(dim).getOrDefault(type, new Tuple<>(0,0));
            int actual = counts.getA(); int limit = counts.getB();
            components.add(Messenger.c(
                    (actual+limit == 0) ?
                            Messenger.s("-", TextFormatting.GRAY) :
                            Messenger.s(actual, Messenger.heatmap_color(actual,limit)),
                    Messenger.creatureTypeColor(type)+" /"+((actual+limit == 0)?"-":limit)
            ));
            components.add(Messenger.c("w  "));
        }
        components.remove(components.size()-1);
        return new ITextComponent[]{Messenger.c(components.toArray(new Object[0]))};
    }

    private static ITextComponent [] send_counter_info(MinecraftServer server, String color)
    {
        HopperCounter counter = HopperCounter.getCounter(color);
        List <ITextComponent> res = counter == null ? Collections.emptyList() : counter.format(server, false, true);
        return new ITextComponent[]{ Messenger.c(res.toArray(new Object[0]))};
    }
    private static ITextComponent [] packetCounter()
    {
        ITextComponent [] ret =  new ITextComponent[]{
                Messenger.c("w I/" + PacketCounter.totalIn + " O/" + PacketCounter.totalOut),
        };
        PacketCounter.reset();
        return ret;
    }

    private static ITextComponent [] send_total_villages(MinecraftServer server)
    {
        int villagecount = 0;
        for (WorldServer world : server.getWorlds()){
            villagecount += world.getVillageCollection().getVillageList().size();
        }

        ITextComponent [] ret =  new ITextComponent[]{
                Messenger.c("w Villages:" + villagecount),
        };
        return ret;
    }

    private static ITextComponent [] send_mem_usage()
    {
        final long bytesPerMB = 1024 * 1024;
        long free = Runtime.getRuntime().freeMemory();
        long total = Runtime.getRuntime().totalMemory();
        long max = Runtime.getRuntime().maxMemory();

        long usedMB = Math.max(total - free, 0) / bytesPerMB;
        long allocatedMB = total / bytesPerMB;
        long maxMB = max != Long.MAX_VALUE ? max / bytesPerMB : -1;
        return new ITextComponent[]{
                Messenger.c(String.format("g %dM / %dM | %dM", usedMB, allocatedMB, maxMB))
        };
    }
}
