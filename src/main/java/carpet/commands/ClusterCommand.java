package carpet.commands;

import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class ClusterCommand extends AbstractCommand
{
	private static final SuggestionProvider<CommandSource> chunkXSuggest = (c, b) -> ISuggestionProvider.suggest(new String[]{String.valueOf(c.getSource().asPlayer().chunkCoordX)}, b);
	private static final SuggestionProvider<CommandSource> chunkZSuggest = (c, b) -> ISuggestionProvider.suggest(new String[]{String.valueOf(c.getSource().asPlayer().chunkCoordZ)}, b);

	private static final String NAME = "cluster";
	private static final ClusterCommand INSTANCE = new ClusterCommand();

	private final ClusterArgument args = new ClusterArgument();
	private final ClusterCalculator calculator = new ClusterCalculator();

	private ClusterCommand()
	{
		super(NAME);
	}

	public static ClusterCommand getInstance()
	{
		return INSTANCE;
	}

	@Override
	public void registerCommand(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(literal(NAME).
				requires(s -> SettingsManager.canUseCommand(s, CarpetSettings.commandCluster)).
				then(literal("setglass").
						then(literal("here").
								executes(c -> setGlassChunkPosHere(c.getSource()))
						).
						then(argument("glassX", integer()).
								suggests(chunkXSuggest).
								then(argument("glassZ", integer()).
										suggests(chunkZSuggest).
										executes(c -> setGlassChunkPos(c.getSource(), getInteger(c, "glassX"), getInteger(c, "glassZ")))
								)
						)
				).
				then(literal("setarea").
						then(argument("startX", integer()).
								then(argument("startZ", integer()).
										then(argument("endX", integer()).
												then(argument("endZ", integer()).
														executes(c -> setSearchArea(c.getSource(), getInteger(c, "startX"), getInteger(c, "startZ"), getInteger(c, "endX"), getInteger(c, "endZ")))
												)
										)
								)
						)
				).
				then(literal("setmask").
						then(argument("mask", integer()).
								executes(c -> setHashmapMask(c.getSource(), getInteger(c, "mask")))
						)
				).
				then(literal("setamount").
						then(argument("amount", integer()).
								executes(c -> setRequestAmount(c.getSource(), getInteger(c, "amount")))
						)
				).
				then(literal("info").
						executes(c -> showArgumentInfo(c.getSource()))
				).
				then(literal("calc").
						executes(c -> calcCluster(c.getSource()))
				).
				then(literal("list").
						executes(c -> showClusterList(c.getSource(), 10)).
						then(literal("all").
								executes(c -> showClusterList(c.getSource(), -1))
						).
						then(argument("limit", integer()).
								executes(c -> showClusterList(c.getSource(), getInteger(c, "limit")))
						)
				).
				then(literal("load").
						executes(c -> loadClusters(c.getSource()))
				)
		);
	}

	private int setGlassChunkPos(CommandSource source, int glassX, int glassZ)
	{
		ChunkPos glassChunkPos = new ChunkPos(glassX, glassZ);
		this.args.glassChunkPos = glassChunkPos;
		Messenger.tell(source, Messenger.format("Set glass chunk to %s", Messenger.coord(glassChunkPos)));
		return 1;
	}

	private int setGlassChunkPosHere(CommandSource source) throws CommandSyntaxException
	{
		Entity entity = source.assertIsEntity();
		return setGlassChunkPos(source, entity.chunkCoordX, entity.chunkCoordZ);
	}

	private int setSearchArea(CommandSource source, int x1, int z1, int x2, int z2)
	{
		int minX = Math.min(x1, x2);
		int maxX = Math.max(x1, x2);
		int minZ = Math.min(z1, z2);
		int maxZ = Math.max(z1, z2);
		ChunkPos startPos = new ChunkPos(minX, minZ);
		ChunkPos endPos = new ChunkPos(maxX, maxZ);
		this.args.areaStart = startPos;
		this.args.areaEnd = endPos;
		Messenger.tell(source, Messenger.format("Set search area to %s -> %s", Messenger.coord(startPos), Messenger.coord(endPos)));
		return 1;
	}

	private int setHashmapMask(CommandSource source, int mask)
	{
		if (mask > 0 && (mask & (mask + 1L)) == 0)
		{
			this.args.mask = mask;
			Messenger.tell(source, String.format("Set hashmap mask to %d", mask));
			return 1;
		}
		else
		{
			Messenger.tell(source, String.format("Invalid mask %d, should be 2^n-1", mask));
			return 0;
		}
	}

	private int setRequestAmount(CommandSource source, int requestedAmount)
	{
		this.args.requestedAmount = requestedAmount;
		Messenger.tell(source, String.format("Set requested amount of cluster chunks to %d", requestedAmount));
		return 1;
	}

	private int showArgumentInfo(CommandSource source)
	{
		Function<ChunkPos, ITextComponent> chunkPosPrinter = cp -> cp == null ? Messenger.s("unset", TextFormatting.GRAY) : Messenger.coord(cp);

		Messenger.tell(source, Messenger.format("Glass chunk: %s", chunkPosPrinter.apply(this.args.glassChunkPos)));
		Messenger.tell(source, Messenger.format("Grid start: %s", chunkPosPrinter.apply(this.args.areaStart)));
		Messenger.tell(source, Messenger.format("Grid end: %s", chunkPosPrinter.apply(this.args.areaEnd)));
		Messenger.tell(source, Messenger.format("Hashmap mask: %s", this.args.mask));
		Messenger.tell(source, Messenger.format("Required cluster chunks: %s", this.args.requestedAmount));

		return 0;
	}

	private int calculate(CommandSource source, Function<CalculationResult, Integer> command)
	{
		try
		{
			CalculationResult clusters = this.calculator.calculate(this.args);
			return command.apply(clusters);
		}
		catch (CalculationException e)
		{
			Messenger.tell(source, Messenger.s(String.format("Calculation failed: %s", e.getMessage()), TextFormatting.RED));
			return 0;
		}
	}

	private int calcCluster(CommandSource source)
	{
		return this.calculate(source, result -> {
			Messenger.tell(source, String.format("Cluster chunk calculation succeed for %d clusters", this.args.requestedAmount));
			Messenger.tell(source, String.format("Maximum possible cluster chunk amount: %d", result.maxAmount));
			return result.maxAmount;
		});
	}

	private int showClusterList(CommandSource source, int limit)
	{
		return this.calculate(source, result -> {
			int loopLimit = limit > 0 ? limit : result.size();
			result.forEach((i, clusterPos, hashDelta) -> {
				Messenger.tell(source, Messenger.format("%s. %s %s", i, Messenger.coord(clusterPos), hashDelta));
				return i < loopLimit - 1;
			});
			if (loopLimit < result.size())
			{
				Messenger.tell(source, String.format("... %d cluster chunks", result.size() - loopLimit));
			}
			return loopLimit;
		});
	}

	private int loadClusters(CommandSource source)
	{
		return this.calculate(source, result -> {
			AtomicInteger counter = new AtomicInteger();
			source.getWorld().getChunkProvider().loadChunks(result.getClusters(), chunk -> {
				int cnt = counter.incrementAndGet();
				Messenger.tell(source, String.format("Loaded cluster chunk %s %.1f%%", TextUtil.coord(chunk.getPos()), 100.0 * cnt / result.size()));
			}).join();
			return result.size();
		});
	}

	private static class ClusterArgument
	{
		@Nullable public ChunkPos glassChunkPos;
		@Nullable public ChunkPos areaStart;
		@Nullable public ChunkPos areaEnd;
		public int mask;
		public int requestedAmount;

		public ClusterArgument()
		{
			this.mask = 16383;
			this.requestedAmount = 100;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ClusterArgument that = (ClusterArgument) o;
			return mask == that.mask && requestedAmount == that.requestedAmount && Objects.equals(glassChunkPos, that.glassChunkPos) && Objects.equals(areaStart, that.areaStart) && Objects.equals(areaEnd, that.areaEnd);
		}

		public ClusterArgument copy()
		{
			ClusterArgument other = new ClusterArgument();
			other.glassChunkPos = this.glassChunkPos;
			other.areaStart = this.areaStart;
			other.areaEnd = this.areaEnd;
			other.mask = this.mask;
			other.requestedAmount = this.requestedAmount;
			return other;
		}
	}

	private static class CalculationException extends Exception
	{
		public CalculationException(String message) {
			super(message);
		}
	}

	// pos -> hashDelta
	private static class CalculationResult
	{
		private final List<ChunkPos> clusters = Lists.newArrayList();
		private final List<Integer> deltas = Lists.newArrayList();
		public int maxAmount;

		public void add(ChunkPos clusterPos, int hashDelta)
		{
			this.clusters.add(clusterPos);
			this.deltas.add(hashDelta);
		}

		public int size()
		{
			return this.clusters.size();
		}

		public void forEach(ResultIterator consumer)
		{
			for (int i = 0; i < this.clusters.size(); i++)
			{
				if (!consumer.accept(i, this.clusters.get(i), this.deltas.get(i)))
				{
					break;
				}
			}
		}

		public Iterable<ChunkPos> getClusters()
		{
			return this.clusters;
		}

		@FunctionalInterface
		public interface ResultIterator
		{
			/**
			 * @return true: keep iterating; false: break now
			 */
			boolean accept(int i, ChunkPos clusterPos, int hashDelta);
		}
	}

	private static class ClusterCalculator
	{
		private ClusterArgument lastArg = null;
		private CalculationResult lastResult = null;

		private static void notNull(@Nullable Object value, String message) throws CalculationException
		{
			if (value == null)
			{
				throw new CalculationException(message);
			}
		}

		public CalculationResult calculate(ClusterArgument args) throws CalculationException
		{
			if (args.equals(this.lastArg))
			{
				return this.lastResult;
			}
			notNull(args.glassChunkPos, "glass chunk unset");
			notNull(args.areaStart, "search area unset");
			notNull(args.areaEnd, "search area unset");

			Function<ChunkPos, Integer> hash = pos -> (int)HashCommon.mix(pos.asLong()) & args.mask;
			int glassHash = hash.apply(args.glassChunkPos);
			List<Pair<ChunkPos, Integer>> hashes = Lists.newArrayList();

			for (int x = args.areaStart.x; x <= args.areaEnd.x; x++)
			{
				for (int z = args.areaStart.z; z <= args.areaEnd.z; z++)
				{
					ChunkPos chunkPos = new ChunkPos(x, z);
					int chunkHash = hash.apply(chunkPos);
					int delta = (chunkHash + (args.mask + 1) - glassHash) & args.mask;
					hashes.add(Pair.of(chunkPos, delta));
				}
			}

			if (hashes.size() < args.requestedAmount)
			{
				throw new CalculationException(String.format("cluster not enough: only %d chunk in area but %d cluster requested", hashes.size(), args.requestedAmount));
			}

			hashes.sort(Comparator.comparing(Pair::getSecond));
			int maxAmount = 0;
			for (int i = 0; i < hashes.size(); i++)
			{
				if (i < hashes.get(i).getSecond())
				{
					break;
				}
				maxAmount = i + 1;
			}
			if (maxAmount < args.requestedAmount)
			{
				throw new CalculationException(String.format("cluster not enough: cluster chunk amount %d < requested %d", maxAmount, args.requestedAmount));
			}

			CalculationResult result = new CalculationResult();
			result.maxAmount = maxAmount;
			for (int i = 0; i < args.requestedAmount; i++)
			{
				result.add(hashes.get(i).getFirst(), hashes.get(i).getSecond());
			}

			this.lastArg = args.copy();
			this.lastResult = result;
			return result;
		}
	}
}
