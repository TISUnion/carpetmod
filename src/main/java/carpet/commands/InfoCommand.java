package carpet.commands;

import carpet.CarpetServer;
import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import carpet.utils.*;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.BlockEventData;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkCacheNeighborNotification;
import net.minecraft.world.storage.WorldInfo;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class InfoCommand
{
    public static HashMap<String, BlockPos[]> posToCheckRaycount = new HashMap<>();

    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralArgumentBuilder<CommandSource> command = literal("info").
                requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandInfo)).
                then(literal("block").
                        then(argument("block position", BlockPosArgument.blockPos()).
                                executes( (c) -> infoBlock(
                                        c.getSource(),
                                        BlockPosArgument.getBlockPos(c, "block position"), null)).
                                then(literal("grep").
                                        then(argument("regexp",greedyString()).
                                                executes( (c) -> infoBlock(
                                                        c.getSource(),
                                                        BlockPosArgument.getBlockPos(c, "block position"),
                                                        getString(c, "regexp"))))))).
                then(literal("entity").
                        then(argument("entity selector", EntityArgument.entities()).
                                executes( (c) -> infoEntities(
                                        c.getSource(), EntityArgument.getEntities(c,"entity selector"), null)).
                                then(literal("grep").
                                        then(argument("regexp",greedyString()).
                                                executes( (c) -> infoEntities(
                                                        c.getSource(),
                                                        EntityArgument.getEntities(c,"entity selector"),
                                                        getString(c, "regexp"))))))).
                then(literal("world").
                        then(literal("container").
                                executes((c) -> showWorldContainer(c.getSource(), false)).
                                then(literal("detailed").
                                        executes(c -> showWorldContainer(c.getSource(), true)))).
                        then(literal("ticking_order").
                                executes((c) -> showWorldTickOrder(c.getSource()))).
                        then(literal("weather").
                                executes((c) -> showWeather(c.getSource()))));

        dispatcher.register(command);
    }


    private static <T, K> void showCountsInCollection(CommandSource source, Collection<T> collection, Function<T, K> keyExtractor, Function<K, ITextComponent> nameGetter)
    {
        Multiset<K> counter = HashMultiset.create();
        collection.stream().map(keyExtractor).forEach(counter::add);
        List<Multiset.Entry<K>> topN = counter.entrySet().stream().
                sorted(Collections.reverseOrder(Comparator.comparingInt(Multiset.Entry::getCount))).
                collect(Collectors.toList());

        for (int i = 0; i < topN.size(); i++)
        {
            Multiset.Entry<K> entry = topN.get(i);
            Messenger.m(source,
                    String.format("g %d. ", i + 1),
                    nameGetter.apply(entry.getElement()),
                    String.format("w  %d", entry.getCount()),
                    "g x"
            );
        }
    }

    private static int showWorldContainer(CommandSource source, boolean detailed)
    {
        WorldServer world = source.getWorld();

        Messenger.m(source, Messenger.s(""));
        Messenger.m(source, "w Current World: ", Messenger.dimension(world));
        Messenger.m(source, Messenger.s(String.format("Loaded chunks: %d", world.getChunkProvider().getLoadedChunkCount())));
        Long2ObjectMap<Chunk> chunks = world.getChunkProvider().getLoadedChunks$TISCM();
        // it.unimi.dsi.fastutil.longs.Long2ObjectMaps.SynchronizedMap#SynchronizedMap
        ReflectionUtil.getField(chunks, "map").ifPresent(map -> {
            if (map instanceof ChunkCacheNeighborNotification)
            {
                Messenger.m(source, Messenger.s(String.format("  Map mask: %d", ((ChunkCacheNeighborNotification)map).getMask())));
            }
        });

        Messenger.m(source, Messenger.s(String.format("Regular entities: %d", world.loadedEntityList.size())));
        if (detailed)
        {
            showCountsInCollection(source, world.loadedEntityList, Entity::getType, EntityType::getName);
        }

        Messenger.m(source, Messenger.s(String.format("Player entities: %d", world.playerEntities.size())));
        if (detailed)
        {
            world.playerEntities.forEach(player -> Messenger.m(source, "g - ", player.getName()));
        }

        Messenger.m(source, Messenger.s(String.format("Weather entities: %d", world.weatherEffects.size())));
        Messenger.m(source, Messenger.s(String.format("Ticking tile entities: %d", world.tickableTileEntities.size())));
        Messenger.m(source, Messenger.s(String.format("Loaded tile entities: %d", world.loadedTileEntityList.size())));
        if (detailed)
        {
            Set<TileEntityType<?>> tickableSet = world.loadedTileEntityList.stream().
                    filter(te -> te instanceof ITickable).
                    map(TileEntity::getType).
                    collect(Collectors.toSet());
            showCountsInCollection(source, world.loadedTileEntityList, TileEntity::getType, tileEntityType -> {
                ITextComponent text = Optional.ofNullable(TileEntityType.getId(tileEntityType)).
                        map(id ->
                                Optional.ofNullable(IRegistry.BLOCK.get(id)).
                                        map(Messenger::block).
                                        orElse(Messenger.s(id.toString()))
                        ).
                        orElse(Messenger.s("unknown"));
                boolean tickable = tickableSet.contains(tileEntityType);
                text.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Messenger.s("Tickable: " + tickable)));
                if (!tickable)
                {
                    text.getStyle().setColor(TextFormatting.GRAY);
                }
                return text;
            });
        }

        Messenger.m(source, Messenger.s(String.format("Block tile tick: %d", world.getPendingBlockTicks().getEntryCount())));
        if (detailed)
        {
            showCountsInCollection(source, Lists.newArrayList(world.getPendingBlockTicks().getEntryIterator()), NextTickListEntry::getTarget, Messenger::block);
        }

        Messenger.m(source, Messenger.s(String.format("Fluid tile tick: %d", world.getPendingFluidTicks().getEntryCount())));
        if (detailed)
        {
            showCountsInCollection(source, Lists.newArrayList(world.getPendingFluidTicks().getEntryIterator()), NextTickListEntry::getTarget, Messenger::fluid);
        }

        Messenger.m(source, Messenger.s(String.format("Block event: %d", world.blockEventQueue.size())));
        if (detailed)
        {
            showCountsInCollection(source, world.blockEventQueue, BlockEventData::getBlock, Messenger::block);
        }

        return 1;
    }

    @SuppressWarnings("ConstantConditions")
    private static int showWeather(CommandSource source)
    {
        WorldServer world = source.getWorld();
        WorldInfo worldInfo = world.getWorldInfo();
        Function<Integer, String> pack = ticks -> String.format("%.1f", (double)ticks / 20 / 60);

        boolean raining = worldInfo.isRaining();
        boolean thundering = worldInfo.isThundering();
        int rainTime = worldInfo.getRainTime();
        int thunderTime = worldInfo.getThunderTime();

        Messenger.m(source, Messenger.s(""));
        Messenger.m(source, "g ======= ", "w Weather Telemetry Data", "g  =======");
        Messenger.m(source, Messenger.s("clearWeatherTime = " + worldInfo.getClearWeatherTime()));
        Messenger.m(source, Messenger.s("rainTime = " + rainTime));
        Messenger.m(source, Messenger.s("raining = " + raining));
        Messenger.m(source, Messenger.s("thunderTime = " + thunderTime));
        Messenger.m(source, Messenger.s("thundering = " + thundering));

        Messenger.m(source, Messenger.s(""));
        Messenger.m(source, "g ======= ", "w Weather Forecast", "g  =======");
        if (raining && thundering)
        {
            Messenger.m(source, "w Current weather: Thundering");
            Messenger.m(source, String.format("w Rain duration: %s minutes", pack.apply(rainTime)));
            Messenger.m(source, String.format("w Thundering duration: %s minutes", pack.apply(Math.min(rainTime, thunderTime))));
        }
        else if (raining && !thundering)
        {
            Messenger.m(source, "w Current weather: Raining");
            Messenger.m(source, String.format("w Rain duration: %s minutes", pack.apply(rainTime)));
            if (thunderTime < rainTime)
            {
                Messenger.m(source, String.format("w Thunder in: %s minutes", pack.apply(thunderTime)));
            }
            else
            {
                Messenger.m(source, "w No thunder during this rain");
            }
        }
        else
        {
            Messenger.m(source, "w Current weather: Clear sky");
            Messenger.m(source, String.format("w Rain in: %s minutes", pack.apply(rainTime)));
            if (thundering)
            {
                if (rainTime < thunderTime)
                {
                    Messenger.m(source, String.format("w It will be a thunderstorm which will last up to %s minutes", pack.apply(thunderTime - rainTime)));
                }
                else
                {
                    Messenger.m(source, String.format("w Thunder forecast unavailable, you can retry in %s minutes", pack.apply(thunderTime)));
                }
            }
            else
            {
                Messenger.m(source, String.format("w No thunder for at least %s minutes", pack.apply(thunderTime)));
            }
        }
        return 1;
    }

    private static int showWorldTickOrder(CommandSource source)
    {
        int order = 0;
        for (World world : CarpetServer.minecraft_server.getWorlds())
        {
            order++;
            Messenger.m(source, Messenger.c(
                    "g " + order + ". ",
                    Messenger.dimension(world)
            ));
        }
        return 1;
    }

    public static void printEntity(List<ITextComponent> messages, CommandSource source, String grep)
    {
        List<ITextComponent> actual = new ArrayList<>();
        if (grep != null)
        {
            Pattern p = Pattern.compile(grep);
            actual.add(messages.get(0));
            boolean empty = true;
            for (int i = 1; i<messages.size(); i++)
            {
                ITextComponent line = messages.get(i);
                Matcher m = p.matcher(line.getString());
                if (m.find())
                {
                    empty = false;
                    actual.add(line);
                }
            }
            if (empty)
            {
                return;
            }
        }
        else
        {
            actual = messages;
        }
        Messenger.m(source, "");
        Messenger.send(source, actual);
    }

    public static void printBlock(List<ITextComponent> messages, CommandSource source, String grep)
    {
        Messenger.m(source, "");
        if (grep != null)
        {
            Pattern p = Pattern.compile(grep);
            Messenger.m(source, messages.get(0));
            for (int i = 1; i<messages.size(); i++)
            {
                ITextComponent line = messages.get(i);
                Matcher m = p.matcher(line.getString());
                if (m.find())
                {
                    Messenger.m(source, line);
                }
            }
        }
        else
        {
            Messenger.send(source, messages);
        }
    }


    public static void printTntExplosion(Entity e, CommandSource source) {
        if (e instanceof EntityTNTPrimed) {
            try {
                String uuid = source.asPlayer().getUniqueID().toString();
                if (!posToCheckRaycount.containsKey(uuid)) {
                    return;
                }
                DimensionType dim = source.asPlayer().dimension;
                BlockPos pos = posToCheckRaycount.get(uuid)[dim.getId() + 1];
                if (pos == null || !SettingsManager.canUseCommand(source, CarpetSettings.commandRaycount)) {return;}
                List<ITextComponent> messages = TntInfo.simulateTntExplosion((EntityTNTPrimed) e, pos);
                Messenger.send(source, messages);
            } catch (CommandSyntaxException ex) {
                throw new CommandException(new TextComponentString("Failed to simulate explosion"));
            }
        }
    }


    private static int infoEntities(CommandSource source, Collection<? extends Entity> entities, String grep)
    {
        for (Entity e: entities)
        {
            List<ITextComponent> report = EntityInfo.entityInfo(e, source.getWorld());
            printEntity(report, source, grep);
            printTntExplosion(e, source);
        }
        return 1;
    }
    private static int infoBlock(CommandSource source, BlockPos pos, String grep)
    {
        printBlock(BlockInfo.blockInfo(pos, source.getWorld()),source, grep);
        return 1;
    }

}
