package carpet.commands;

import carpet.utils.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ChunkLoader;

import java.util.*;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.minecraft.command.Commands.*;

public class ClusterCommand {
    // syntax:
    // /cluster glass <x> <z>
    // /cluster area <xStart> <zStart> <xEnd> <zEnd>
    // /cluster calculate <clusterNum>
    // /cluster loadCluster

    private static final LiteralArgumentBuilder<CommandSource> GLASS_LITERAL;
    private static final LiteralArgumentBuilder<CommandSource> AREA_LITERAL;
    private static final LiteralArgumentBuilder<CommandSource> MASK_LITERAL;
    private static final LiteralArgumentBuilder<CommandSource> CALCULATE_LITERAL;
    private static final LiteralArgumentBuilder<CommandSource> LOAD_LITERAL;

    static {
        GLASS_LITERAL = literal("glass")
                .then(argument("glassX", integer()).suggests((c, b) ->
                    ISuggestionProvider.suggest(new String[] {String.valueOf(c.getSource().asPlayer().chunkCoordX)}, b))
                .then(argument("glassZ", integer()).suggests((c, b) ->
                    ISuggestionProvider.suggest(new String[] {String.valueOf(c.getSource().asPlayer().chunkCoordZ)}, b))
                .executes(ClusterCommand::cmdGlass)));
        AREA_LITERAL = literal("area")
                .then(argument("startX", integer())
                .then(argument("startZ", integer())
                .then(argument("endX", integer())
                .then(argument("endZ", integer())
                .executes(ClusterCommand::cmdArea)))));
        CALCULATE_LITERAL = literal("calculate")
                .then(argument("clusterNum", integer())
                .executes(ClusterCommand::cmdCalculate));
        MASK_LITERAL = literal("mask")
                .then(argument("mask", integer())
                .executes(ClusterCommand::cmdMask));
        LOAD_LITERAL = literal("load").executes(ClusterCommand::cmdLoad);
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = literal("cluster")
                .then(GLASS_LITERAL)
                .then(AREA_LITERAL)
                .then(MASK_LITERAL)
                .then(CALCULATE_LITERAL)
                .then(LOAD_LITERAL);
        dispatcher.register(builder);
    }

    private static final ClusterHelper helper = new ClusterHelper(
            new ChunkPos(0, 0), new ChunkPos(0, 0), new ChunkPos(0, 0), 1);
    private static List<ChunkPos> lastResult = Collections.emptyList();

    private static int cmdGlass(CommandContext<CommandSource> commandContext) {
        helper.setGlassChunkPos(new ChunkPos(
                getIntArgument("glassX", commandContext),
                getIntArgument("glassZ", commandContext)));
        return 1;
    }

    private static int cmdArea(CommandContext<CommandSource> commandContext) {
        helper.setGridStart(new ChunkPos(
                getIntArgument("startX", commandContext),
                getIntArgument("startZ", commandContext)));
        helper.setGridEnd(new ChunkPos(
                getIntArgument("endX", commandContext),
                getIntArgument("endZ", commandContext)));
        return 1;
    }

    private static int cmdCalculate(CommandContext<CommandSource> commandContext) {
        int clusterNum = getIntArgument("clusterNum", commandContext);
        List<ChunkPos> clusters = helper.getClusters(clusterNum);
        lastResult = clusters;
        String msg = clusters.stream().map(ChunkPos::toString).collect(Collectors.joining("\n"));
        Messenger.m(commandContext.getSource(), "w Calculated cluster chunks: \n" + msg);
        return 1;
    }

    private static int cmdMask(CommandContext<CommandSource> commandContext) {
        helper.setMask(getIntArgument("mask", commandContext));
        return 1;
    }

    private static int cmdLoad(CommandContext<CommandSource> commandContext) {
        commandContext.getSource().getWorld().getChunkProvider().loadChunks(lastResult, (chunk) -> {
            Messenger.m(commandContext.getSource(), "w Loaded cluster chunk: " + chunk.getPos());
        });
        return 1;
    }

    private static int getIntArgument(String arg, CommandContext<CommandSource> commandContext) {
        return commandContext.getArgument(arg, int.class);
    }

    public static class ClusterHelper implements Iterable<ChunkPos> {
        private ChunkPos glassChunkPos;

        private ChunkPos gridStart;
        private ChunkPos gridEnd;

        private Map<Integer, List<ChunkPos>> offsetToClusterList = new HashMap<>();
        private int mask;
        private boolean dirty;

        public ClusterHelper(ChunkPos glassChunkPos, ChunkPos gridStart, ChunkPos gridEnd, int mask) {
            this.glassChunkPos = glassChunkPos;
            this.gridStart = gridStart;
            this.gridEnd = gridEnd;
            this.mask = mask;
            this.dirty = true;
            calculateClusters();
        }

        private static int hashChunkPos(ChunkPos pos) {
            return (int) it.unimi.dsi.fastutil.HashCommon.mix((pos.asLong()));
        }

        private static int hashChunkPos(int x, int z) {
            return (int) it.unimi.dsi.fastutil.HashCommon.mix(ChunkPos.asLong(x, z));
        }

        private void calculateClusters() {
            int glassHash = hashChunkPos(glassChunkPos);
            for (ChunkPos chunkPos : this) {
                int offset = (hashChunkPos(chunkPos) - glassHash) & mask;
                offsetToClusterList.computeIfAbsent(offset, (ignore) -> new LinkedList<>()).add(chunkPos);
            }
        }

        public List<ChunkPos> getClusters(int clusterNum) {
            if (dirty) {
                calculateClusters();
                dirty = false;
            }
            List<ChunkPos> result = new LinkedList<>();
            List<ChunkPos> currentOffset;
            for (int i = 0; i <= mask; i ++) {
                currentOffset = offsetToClusterList.get(i);
                if (currentOffset == null || currentOffset.isEmpty()) break;
                result.addAll(currentOffset);
                if (result.size() >= clusterNum) break;
            }
            return result.size() <= clusterNum ? result : result.subList(0, clusterNum);
        }

        public ChunkPos getGlassChunkPos() {
            return glassChunkPos;
        }

        public void setGlassChunkPos(ChunkPos glassChunkPos) {
            markDirty();
            this.glassChunkPos = glassChunkPos;
        }

        public ChunkPos getGridStart() {
            return gridStart;
        }

        public void setGridStart(ChunkPos gridStart) {
            markDirty();
            this.gridStart = gridStart;
        }

        public ChunkPos getGridEnd() {
            return gridEnd;
        }

        public void setGridEnd(ChunkPos gridEnd) {
            markDirty();
            this.gridEnd = gridEnd;
        }

        public int getMask() {
            return mask;
        }

        public void setMask(int mask) {
            markDirty();
            this.mask = mask;
        }

        private void markDirty() {
            this.dirty = true;
        }

        @Override
        public Iterator<ChunkPos> iterator() {
            return new Iterator<ChunkPos>() {
                public int currentX = gridStart.x;
                public int currentZ = gridStart.z;
                @Override
                public boolean hasNext() {
                    if (currentX < gridStart.x || currentX >= gridEnd.x || currentZ < gridStart.z)
                        // throw new IllegalStateException();
                        return false;
                    return currentZ < gridEnd.z;
                }

                @Override
                public ChunkPos next() {
                    ChunkPos result = new ChunkPos(currentX, currentZ);
                    currentX ++;
                    if (currentX == gridEnd.x) {
                        currentX = gridStart.x;
                        currentZ ++;
                    }
                    return result;
                }
            };
        }
    }
}
