package redstone.multimeter.server;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import carpet.helpers.TickSpeed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;

import redstone.multimeter.RedstoneMultimeter;
import redstone.multimeter.common.TickPhase;
import redstone.multimeter.common.TickTask;
import redstone.multimeter.common.DimPos;
import redstone.multimeter.common.network.packets.HandshakePacket;
import redstone.multimeter.common.network.packets.ServerTickPacket;
import redstone.multimeter.server.meter.ServerMeterGroup;

public class MultimeterServer {
	
	private final MinecraftServer server;
	private final ServerPacketHandler packetHandler;
	private final Multimeter multimeter;
	private final Map<UUID, String> connectedPlayers;
	private final Map<UUID, String> playerNameCache;
	
	private TickPhase tickPhase;
	/** true if the OverWorld already ticked time */
	private boolean tickedTime;
	
	public MultimeterServer(MinecraftServer server) {
		this.server = server;
		this.packetHandler = new ServerPacketHandler(this);
		this.multimeter = new Multimeter(this);
		this.connectedPlayers = new HashMap<>();
		this.playerNameCache = new HashMap<>();
		
		this.tickPhase = TickPhase.UNKNOWN;
		this.tickedTime = false;
	}
	
	public MinecraftServer getMinecraftServer() {
		return server;
	}
	
	public ServerPacketHandler getPacketHandler() {
		return packetHandler;
	}
	
	public Multimeter getMultimeter() {
		return multimeter;
	}
	
	public boolean isDedicated() {
		return server.isDedicatedServer();
	}
	
	public File getConfigFolder() {
		return new File(server.getDataDirectory(), RedstoneMultimeter.CONFIG_PATH);
	}
	
	public TickPhase getTickPhase() {
		return tickPhase;
	}
	
	public void startTickTask(TickTask task) {
		tickPhase = tickPhase.startTask(task);
	}
	
	public void endTickTask() {
		tickPhase = tickPhase.endTask();
	}
	
	public void swapTickTask(TickTask task) {
		tickPhase = tickPhase.swapTask(task);
	}
	
	public void onOverworldTickTime() {
		tickedTime = true;
	}
	
	public long getCurrentTick() {
		long tick = server.getWorld(DimensionType.OVERWORLD).getGameTime();
		
		if (!tickedTime) {
			tick++;
		}
		
		return tick;
	}
	
	public boolean isPaused() {
		return !TickSpeed.process_entities || server.isPaused();
	}
	
	public void tickStart() {
		boolean paused = isPaused();
		
		if (!paused) {
			tickedTime = false;
			
			if (server.getTickCounter() % 72000 == 0) {
				cleanPlayerNameCache();
			}
		}
		
		tickPhase = TickPhase.UNKNOWN;
		multimeter.tickStart(paused);
	}
	
	private void cleanPlayerNameCache() {
		playerNameCache.keySet().removeIf(playerUUID -> {
			for (ServerMeterGroup meterGroup : multimeter.getMeterGroups()) {
				if (meterGroup.hasMember(playerUUID)) {
					return false;
				}
			}
			
			return true;
		});
	}
	
	public void tickEnd() {
		boolean paused = isPaused();
		
		if (!paused) {
			ServerTickPacket packet = new ServerTickPacket(getCurrentTick());
			
			for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
				if (multimeter.hasSubscription(player)) {
					packetHandler.sendToPlayer(packet, player);
				}
			}
		}
		
		tickPhase = TickPhase.UNKNOWN;
		multimeter.tickEnd(paused);
	}
	
	public void onPlayerJoin(EntityPlayerMP player) {
		multimeter.onPlayerJoin(player);
		playerNameCache.remove(player.getUniqueID());
	}
	
	public void onPlayerLeave(EntityPlayerMP player) {
		multimeter.onPlayerLeave(player);
		connectedPlayers.remove(player.getUniqueID());
		playerNameCache.put(player.getUniqueID(), player.getScoreboardName());
	}
	
	public void onHandshake(EntityPlayerMP player, String modVersion) {
		if (connectedPlayers.put(player.getUniqueID(), modVersion) == null) {
			HandshakePacket packet = new HandshakePacket();
			packetHandler.sendToPlayer(packet, player);
		}
	}
	
	public WorldServer getWorld(ResourceLocation dimensionId) {
		DimensionType type = DimensionType.byName(dimensionId);
		return server.getWorld(type);
	}
	
	public WorldServer getWorldOf(DimPos pos) {
		return getWorld(pos.getDimensionId());
	}
	
	public IBlockState getBlockState(DimPos pos) {
		World world = getWorldOf(pos);
		
		if (world != null) {
			return world.getBlockState(pos.getBlockPos());
		}
		
		return null;
	}
	
	public EntityPlayerMP getPlayer(UUID playerUUID) {
		return server.getPlayerList().getPlayerByUUID(playerUUID);
	}
	
	public String getPlayerName(UUID playerUUID) {
	    EntityPlayerMP player = getPlayer(playerUUID);
		return player == null ? playerNameCache.get(playerUUID) : player.getScoreboardName();
	}
	
	public EntityPlayerMP getPlayer(String playerName) {
		return server.getPlayerList().getPlayerByUsername(playerName);
	}
	
	public boolean isMultimeterClient(UUID playerUUID) {
		return connectedPlayers.containsKey(playerUUID);
	}
	
	public boolean isMultimeterClient(EntityPlayerMP player) {
		return connectedPlayers.containsKey(player.getUniqueID());
	}
	
	public Collection<EntityPlayerMP> collectPlayers(Collection<UUID> playerUUIDs) {
		Set<EntityPlayerMP> players = new LinkedHashSet<>();
		
		for (UUID playerUUID : playerUUIDs) {
		    EntityPlayerMP player = getPlayer(playerUUID);
			
			if (player != null) {
				players.add(player);
			}
		}
		
		return players;
	}
	
	public void sendMessage(EntityPlayerMP player, ITextComponent message, boolean actionBar) {
		player.sendStatusMessage(message, actionBar);
	}
}
