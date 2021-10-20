package carpet.commands;

import carpet.CarpetServer;
import carpet.script.Fluff;
import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionType;

import java.util.Map;
import java.util.Set;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class ChunkRegenCommand
{
    private static final Map<DimensionType, Set<ChunkPos>> toBeRegen = Maps.newLinkedHashMap();

    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(
                literal("chunkRegen").
                requires(s -> SettingsManager.canUseCommand(s, CarpetSettings.commandChunkRegen)).
                then(chunkOperation("add", ChunkRegenCommand::addChunk)).
                then(chunkOperation("remove", ChunkRegenCommand::removeChunk)).
                then(literal("clear").executes(c -> clearChunks(c.getSource()))).
                then(literal("list").executes(c -> listChunks(c.getSource())))
        );
    }

    private static ArgumentBuilder<CommandSource, ?> chunkOperation(String name, Fluff.TriFunction<CommandSource, Integer, Integer, Integer> consumer)
    {
        return literal(name).then(
                argument("chunkX", integer()).
                suggests((c, b) -> ISuggestionProvider.suggest(new String[]{String.valueOf(c.getSource().asPlayer().chunkCoordX)}, b)).
                then(
                        argument("chunkZ", integer()).
                        suggests((c, b) -> ISuggestionProvider.suggest(new String[]{String.valueOf(c.getSource().asPlayer().chunkCoordZ)}, b)).
                        executes(c -> consumer.apply(c.getSource(), getInteger(c, "chunkX"), getInteger(c, "chunkZ")))
                )
        );
    }

    private static int addChunk(CommandSource source, int chunkX, int chunkZ)
    {
        synchronized (toBeRegen)
        {
            boolean ret = toBeRegen.computeIfAbsent(source.getWorld().getDimension().getType(), x -> Sets.newLinkedHashSet()).add(new ChunkPos(chunkX, chunkZ));
            if (ret)
            {
                source.sendFeedback(Messenger.s(String.format("Marked chunk [%d, %d] to be regenerated", chunkX, chunkZ)), false);
            }
            else
            {
                source.sendFeedback(Messenger.s(String.format("Chunk [%d, %d] is already marked", chunkX, chunkZ)), false);
            }
            return ret ? 1 : 0;
        }
    }

    private static int removeChunk(CommandSource source, int chunkX, int chunkZ)
    {
        synchronized (toBeRegen)
        {
            boolean ret = toBeRegen.computeIfAbsent(source.getWorld().getDimension().getType(), x -> Sets.newLinkedHashSet()).remove(new ChunkPos(chunkX, chunkZ));
            if (ret)
            {
                source.sendFeedback(Messenger.s(String.format("Unmarked chunk [%d, %d] to be regenerated", chunkX, chunkZ)), false);
            }
            else
            {
                source.sendFeedback(Messenger.s(String.format("Chunk [%d, %d] is not marked", chunkX, chunkZ)), false);
            }
            return ret ? 1 : 0;
        }
    }

    private static int getMarkedChunkAmount()
    {
        synchronized (toBeRegen)
        {
            return toBeRegen.values().stream().mapToInt(Set::size).sum();
        }
    }

    private static int clearChunks(CommandSource source)
    {
        synchronized (toBeRegen)
        {
            int size = getMarkedChunkAmount();
            toBeRegen.clear();
            source.sendFeedback(Messenger.s(String.format("Cleared %d marked chunks", size)), false);
            return 1;
        }
    }

    private static int listChunks(CommandSource source)
    {
        synchronized (toBeRegen)
        {
            source.sendFeedback(Messenger.s(String.format("%d marked chunks", getMarkedChunkAmount())), false);
            toBeRegen.forEach((dim, chunks) ->
                    chunks.forEach(chunkPos ->
                            source.sendFeedback(Messenger.c(
                                    "g - ",
                                    TextUtil.getDimensionNameText(dim),
                                    String.format("w  [%d, %d]", chunkPos.x, chunkPos.z)
                            ), false)
                    )
            );
            return 1;
        }
    }

    /**
     * @return true: dont load, do regenerate; false: nothing happens
     */
    public static boolean skipLoading(DimensionType dimensionType, int chunkX, int chunkZ)
    {
        synchronized (toBeRegen)
        {
            Set<ChunkPos> chunks = toBeRegen.get(dimensionType);
            return chunks != null && chunks.contains(new ChunkPos(chunkX, chunkZ));
        }
    }

    public static void onChunkGenerated(DimensionType dimensionType, int chunkX, int chunkZ)
    {
        synchronized (toBeRegen)
        {
            Set<ChunkPos> chunks = toBeRegen.get(dimensionType);
            ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
            if (chunks != null && chunks.contains(chunkPos))
            {
                chunks.remove(chunkPos);
                Messenger.print_server_message(CarpetServer.minecraft_server, Messenger.s(String.format("!! Regenerating chunk [%d, %d] since it has been marked by /chunkregen", chunkX, chunkZ)));
            }
        }
    }
}