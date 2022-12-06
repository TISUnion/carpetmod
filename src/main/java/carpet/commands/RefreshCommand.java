package carpet.commands;

import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NettyCompressionEncoder;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ChatType;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;
import static net.minecraft.command.arguments.EntityArgument.getPlayers;
import static net.minecraft.command.arguments.EntityArgument.players;

public class RefreshCommand extends AbstractCommand
{
	private static final String NAME = "refresh";
	private static final RefreshCommand INSTANCE = new RefreshCommand();

	/**
	 * `/refresh chunk all` is costly on network thread due to packet compression in {@link NettyCompressionEncoder}
	 * Here's a check to see if the connection thread is already heavily-loaded
	 */
	private final Set<EntityPlayerMP> refreshingChunkPlayers = Collections.newSetFromMap(new WeakHashMap<>());

	public RefreshCommand()
	{
		super(NAME);
	}

	public static RefreshCommand getInstance()
	{
		return INSTANCE;
	}

	@Override
	public void registerCommand(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> builder = literal(NAME).
				requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandRefresh)).
				then(
						literal("inventory").
								executes(c -> refreshSelfInventory(c.getSource())).
								then(
										argument("players", players()).
												requires(s -> s.hasPermissionLevel(2)).
												executes(c -> refreshSelectedPlayerInventory(c.getSource(), getPlayers(c, "players")))
								)
				).
				then(
						literal("chunk").
								executes(c -> refreshCurrentChunk(c.getSource(), c.getSource().asPlayer())).
								then(literal("current").executes(c -> refreshCurrentChunk(c.getSource(), c.getSource().asPlayer()))).
								then(literal("all").executes(c -> refreshAllChunks(c.getSource()))).
								then(literal("inrange").then(
										argument("chebyshevDistance", integer()).
												executes(c -> refreshChunksInRange(c.getSource(), getInteger(c, "chebyshevDistance")))
								)).
								then(literal("at").
										then(argument("chunkX", integer()).
												then(
														argument("chunkZ", integer()).
																executes(c -> refreshSelectedChunk(c.getSource(), getInteger(c, "chunkX"), getInteger(c, "chunkZ")))
												)
										)
								)
				);
		dispatcher.register(builder);
	}

	/*
	 * ---------------------
	 *   Refresh Inventory
	 * ---------------------
	 */

	private void refreshPlayerInventory(CommandSource source, EntityPlayerMP player)
	{
		source.getServer().getPlayerList().sendInventory(player);
		Messenger.tell(player, Messenger.s(this.tr("inventory.done", "Inventory refreshed")));
	}

	private int refreshSelfInventory(CommandSource source) throws CommandSyntaxException
	{
		this.refreshPlayerInventory(source, source.asPlayer());
		return 1;
	}

	private int refreshSelectedPlayerInventory(CommandSource source, Collection<EntityPlayerMP> players)
	{
		players.forEach(player -> this.refreshPlayerInventory(source, player));
		return players.size();
	}

	/*
	 * ---------------------
	 *     Refresh Chunk
	 * ---------------------
	 */

	private boolean isChunkInPlayerViewDistance(ChunkPos pos, EntityPlayerMP player, int viewDistance)
	{
		int playerCX = (int)player.managedPosX >> 4;
		int playerCZ = (int)player.managedPosZ >> 4;
		return playerCX - viewDistance <= pos.x && pos.x <= playerCX + viewDistance && playerCZ - viewDistance <= pos.z && pos.z <= playerCZ + viewDistance;
	}

	private int refreshChunks(CommandSource source, @Nullable ChunkPos chunkPos, @Nullable Predicate<ChunkPos> predicate) throws CommandSyntaxException
	{
		EntityPlayerMP player = source.asPlayer();
		synchronized (this.refreshingChunkPlayers)
		{
			if (this.refreshingChunkPlayers.contains(player))
			{
				source.sendErrorMessage(Messenger.s(this.tr("chunk.overloaded", "Refresh failed: Network connection overloaded")));
				return 0;
			}
		}
		PlayerChunkMap chunkMap = player.getServerWorld().getPlayerChunkMap();
		int playerCX = (int)player.managedPosX >> 4;
		int playerCZ = (int)player.managedPosZ >> 4;
		int viewDistance = chunkMap.getPlayerViewRadius();
		MutableInt counter = new MutableInt(0);
		Consumer<ChunkPos> chunkRefresher = pos -> {
			PlayerChunkMapEntry entry = chunkMap.getEntry(pos.x, pos.z);
			if (entry != null)
			{
				entry.sendToPlayer(player);
				counter.add(1);
			}
		};
		Predicate<ChunkPos> inPlayerViewDistance = pos -> this.isChunkInPlayerViewDistance(pos, player, viewDistance);
		if (chunkPos != null)
		{
			if (inPlayerViewDistance.test(chunkPos))
			{
				chunkRefresher.accept(chunkPos);
			}
			else
			{
				source.sendErrorMessage(Messenger.s(this.tr("chunk.too_far", "Selected chunk is not within your view distance")));
			}
		}
		else
		{
			Objects.requireNonNull(predicate);
			for (int x = playerCX - viewDistance; x <= playerCX + viewDistance; ++x)
			{
				for (int z = playerCZ - viewDistance; z <= playerCZ + viewDistance; ++z)
				{
					ChunkPos pos = new ChunkPos(x, z);
					if (predicate.test(pos))
					{
						chunkRefresher.accept(pos);
					}
				}
			}
			synchronized (this.refreshingChunkPlayers)
			{
				this.refreshingChunkPlayers.add(player);
			}
		}
		player.connection.sendPacket(
				new SPacketChat(this.advTr("chunk.done", "Refreshed %1$s chunks", counter.getValue()), ChatType.SYSTEM),
				future -> {
					synchronized (this.refreshingChunkPlayers)
					{
						this.refreshingChunkPlayers.remove(player);
					}
				}
		);
		return counter.getValue();
	}
	private int refreshSingleChunk(CommandSource source, @Nullable ChunkPos chunkPos) throws CommandSyntaxException
	{
		return this.refreshChunks(source, chunkPos, null);
	}

	private int refreshAllChunks(CommandSource source) throws CommandSyntaxException
	{
		return this.refreshChunks(source, null, chunkPos -> true);
	}

	private int refreshCurrentChunk(CommandSource source, EntityPlayerMP player) throws CommandSyntaxException
	{
		return this.refreshSingleChunk(source, new ChunkPos(player.chunkCoordX, player.chunkCoordZ));
	}

	private int refreshSelectedChunk(CommandSource source, int x, int z) throws CommandSyntaxException
	{
		return this.refreshSingleChunk(source, new ChunkPos(x, z));
	}

	private int refreshChunksInRange(CommandSource source, int distance) throws CommandSyntaxException
	{
		EntityPlayerMP player = source.asPlayer();
		return this.refreshChunks(source, null, chunkPos -> this.isChunkInPlayerViewDistance(chunkPos, player, distance));
	}
}
