package redstone.multimeter.server;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;

import redstone.multimeter.block.Meterable;
import redstone.multimeter.block.PowerSource;
import redstone.multimeter.common.DimPos;
import redstone.multimeter.common.meter.Meter;
import redstone.multimeter.common.meter.MeterGroup;
import redstone.multimeter.common.meter.MeterProperties;
import redstone.multimeter.common.meter.event.EventType;
import redstone.multimeter.common.network.packets.ClearMeterGroupPacket;
import redstone.multimeter.common.network.packets.MeterGroupDefaultPacket;
import redstone.multimeter.common.network.packets.MeterGroupRefreshPacket;
import redstone.multimeter.common.network.packets.MeterGroupSubscriptionPacket;
import redstone.multimeter.server.meter.ServerMeterGroup;
import redstone.multimeter.server.meter.ServerMeterPropertiesManager;
import redstone.multimeter.server.meter.event.MeterEventPredicate;
import redstone.multimeter.server.meter.event.MeterEventSupplier;
import redstone.multimeter.server.option.Options;
import redstone.multimeter.server.option.OptionsManager;
import redstone.multimeter.util.TextUtils;

public class Multimeter {
	
	private final MultimeterServer server;
	private final Map<String, ServerMeterGroup> meterGroups;
	private final Map<UUID, ServerMeterGroup> subscriptions;
	private final ServerMeterPropertiesManager meterPropertiesManager;
	
	public Options options;
	
	public Multimeter(MultimeterServer server) {
		this.server = server;
		this.meterGroups = new LinkedHashMap<>();
		this.subscriptions = new HashMap<>();
		this.meterPropertiesManager = new ServerMeterPropertiesManager(this);
		
		reloadOptions();
	}
	
	public MultimeterServer getMultimeterServer() {
		return server;
	}
	
	public Collection<ServerMeterGroup> getMeterGroups() {
		return Collections.unmodifiableCollection(meterGroups.values());
	}
	
	public ServerMeterGroup getMeterGroup(String name) {
		return meterGroups.get(name);
	}
	
	public boolean hasMeterGroup(String name) {
		return meterGroups.containsKey(name);
	}
	
	public ServerMeterGroup getSubscription(EntityPlayerMP player) {
		return subscriptions.get(player.getUniqueID());
	}
	
	public boolean hasSubscription(EntityPlayerMP player) {
		return subscriptions.containsKey(player.getUniqueID());
	}
	
	public boolean isOwnerOfSubscription(EntityPlayerMP player) {
		ServerMeterGroup meterGroup = getSubscription(player);
		return meterGroup != null && meterGroup.isOwnedBy(player);
	}
	
	public void reloadOptions() {
		if (server.isDedicated()) {
			options = OptionsManager.load(server.getConfigFolder());
		} else {
			options = new Options();
		}
	}
	
	public void tickStart(boolean paused) {
		if (!paused) {
			if (options.meter_group.max_idle_time >= 0) {
				meterGroups.values().removeIf(meterGroup -> {
					return meterGroup.isIdle() && (!meterGroup.hasMeters() || meterGroup.getIdleTime() > options.meter_group.max_idle_time);
				});
			}
			
			for (ServerMeterGroup meterGroup : meterGroups.values()) {
				meterGroup.tick();
			}
		}
	}
	
	public void tickEnd(boolean paused) {
		broadcastMeterUpdates();
		
		if (!paused) {
			broadcastMeterLogs();
		}
	}
	
	private void broadcastMeterUpdates() {
		for (ServerMeterGroup meterGroup : meterGroups.values()) {
			meterGroup.flushUpdates();
		}
	}
	
	private void broadcastMeterLogs() {
		for (ServerMeterGroup meterGroup : meterGroups.values()) {
			meterGroup.getLogManager().flushLogs();
		}
	}
	
	public void onPlayerJoin(EntityPlayerMP player) {
		
	}
	
	public void onPlayerLeave(EntityPlayerMP player) {
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null) {
			removeSubscriberFromMeterGroup(meterGroup, player);
		}
	}
	
	public void addMeter(EntityPlayerMP player, MeterProperties properties) {
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null) {
			if (meterGroup.isPastMeterLimit()) {
				ITextComponent message = new TextComponentString(String.format("meter limit (%d) reached!", options.meter_group.meter_limit));
				server.sendMessage(player, message, true);
			} else if (!addMeter(meterGroup, properties)) {
				refreshMeterGroup(meterGroup, player);
			}
		}
	}
	
	public boolean addMeter(ServerMeterGroup meterGroup, MeterProperties properties) {
		if (!meterPropertiesManager.validate(properties) || !meterGroup.addMeter(properties)) {
			return false;
		}
		
		DimPos pos = properties.getPos();
		World world = server.getWorldOf(pos);
		BlockPos blockPos = pos.getBlockPos();
		IBlockState state = world.getBlockState(blockPos);
		
		logPowered(world, blockPos, state);
		logActive(world, blockPos, state);
		
		return true;
	}
	
	public void removeMeter(EntityPlayerMP player, long id) {
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null && !meterGroup.removeMeter(id)) {
			refreshMeterGroup(meterGroup, player);
		}
	}
	
	public void updateMeter(EntityPlayerMP player, long id, MeterProperties newProperties) {
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null && !meterGroup.updateMeter(id, newProperties)) {
			refreshMeterGroup(meterGroup, player);
		}
	}
	
	public void clearMeterGroup(EntityPlayerMP player) {
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null) {
			meterGroup.clear();
			
			ClearMeterGroupPacket packet = new ClearMeterGroupPacket();
			server.getPacketHandler().sendToSubscribers(packet, meterGroup);
		}
	}
	
	public void createMeterGroup(EntityPlayerMP player, String name) {
		if (!MeterGroup.isValidName(name) || meterGroups.containsKey(name)) {
			return;
		}
		
		ServerMeterGroup meterGroup = new ServerMeterGroup(this, name, player);
		meterGroups.put(name, meterGroup);
		
		subscribeToMeterGroup(meterGroup, player);
	}
	
	public void subscribeToMeterGroup(ServerMeterGroup meterGroup, EntityPlayerMP player) {
		ServerMeterGroup prevSubscription = getSubscription(player);
		
		if (prevSubscription == meterGroup) {
			refreshMeterGroup(meterGroup, player);
		} else {
			if (prevSubscription != null) {
				removeSubscriberFromMeterGroup(prevSubscription, player);
			}
			
			addSubscriberToMeterGroup(meterGroup, player);
			onSubscriptionChanged(player, prevSubscription, meterGroup);
		}
	}
	
	public void subscribeToDefaultMeterGroup(EntityPlayerMP player) {
		MeterGroupDefaultPacket packet = new MeterGroupDefaultPacket();
		server.getPacketHandler().sendToPlayer(packet, player);
	}
	
	private void addSubscriberToMeterGroup(ServerMeterGroup meterGroup, EntityPlayerMP player) {
		UUID playerUUID = player.getUniqueID();
		
		subscriptions.put(playerUUID, meterGroup);
		meterGroup.addSubscriber(playerUUID);
		meterGroup.updateIdleState();
	}
	
	public void unsubscribeFromMeterGroup(ServerMeterGroup meterGroup, EntityPlayerMP player) {
		if (meterGroup.hasSubscriber(player)) {
			removeSubscriberFromMeterGroup(meterGroup, player);
			onSubscriptionChanged(player, meterGroup, null);
		}
	}
	
	private void removeSubscriberFromMeterGroup(ServerMeterGroup meterGroup, EntityPlayerMP player) {
		UUID playerUUID = player.getUniqueID();
		
		subscriptions.remove(playerUUID, meterGroup);
		meterGroup.removeSubscriber(playerUUID);
		meterGroup.updateIdleState();
	}
	
	private void onSubscriptionChanged(EntityPlayerMP player, ServerMeterGroup prevSubscription, ServerMeterGroup newSubscription) {
		MeterGroupSubscriptionPacket packet;
		
		if (newSubscription == null) {
			packet = new MeterGroupSubscriptionPacket(prevSubscription.getName(), false);
		} else {
			packet = new MeterGroupSubscriptionPacket(newSubscription.getName(), true);
		}
		
		server.getPacketHandler().sendToPlayer(packet, player);
		server.getMinecraftServer().getPlayerList().updatePermissionLevel(player);
	}
	
	public void clearMembersOfMeterGroup(ServerMeterGroup meterGroup) {
		for (UUID playerUUID : meterGroup.getMembers()) {
			removeMemberFromMeterGroup(meterGroup, playerUUID);
		}
	}
	
	public void addMemberToMeterGroup(ServerMeterGroup meterGroup, UUID playerUUID) {
		if (meterGroup.hasMember(playerUUID) || meterGroup.isOwnedBy(playerUUID)) {
			return;
		}
		
		EntityPlayerMP player = server.getPlayer(playerUUID);
		
		if (player == null) {
			return;
		}
		
		meterGroup.addMember(playerUUID);
		
		ITextComponent message = new TextComponentString("").
			appendSibling(new TextComponentString(String.format("You have been invited to meter group \'%s\' - click ", meterGroup.getName()))).
			appendSibling(new TextComponentString("[here]").applyTextStyle(style -> {
				style.
					setColor(TextFormatting.GREEN).
					setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(String.format("Subscribe to meter group \'%s\'", meterGroup.getName())))).
					setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/metergroup subscribe %s", meterGroup.getName())));
			})).
			appendSibling(new TextComponentString(" to subscribe to it."));
		server.sendMessage(player, message, false);
	}
	
	public void removeMemberFromMeterGroup(ServerMeterGroup meterGroup, UUID playerUUID) {
		if (!meterGroup.hasMember(playerUUID)) {
			return;
		}
		
		meterGroup.removeMember(playerUUID);
		
		if (meterGroup.isPrivate()) {
		    EntityPlayerMP player = server.getPlayer(playerUUID);
			
			if (player != null && meterGroup.hasSubscriber(playerUUID)) {
				unsubscribeFromMeterGroup(meterGroup, player);
				
				ITextComponent message = new TextComponentString(String.format("The owner of meter group \'%s\' has removed you as a member!", meterGroup.getName()));
				server.sendMessage(player, message, false);
			}
		}
	}
	
	public void refreshMeterGroup(EntityPlayerMP player) {
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null) {
			refreshMeterGroup(meterGroup, player);
		}
	}
	
	private void refreshMeterGroup(ServerMeterGroup meterGroup, EntityPlayerMP player) {
		MeterGroupRefreshPacket packet = new MeterGroupRefreshPacket(meterGroup);
		server.getPacketHandler().sendToPlayer(packet, player);
	}
	
	public void teleportToMeter(EntityPlayerMP player, long id) {
		if (!options.meter.allow_teleports) {
			ITextComponent message = new TextComponentString("This server does not allow meter teleporting!");
			server.sendMessage(player, message, false);
			
			return;
		}
		
		ServerMeterGroup meterGroup = getSubscription(player);
		
		if (meterGroup != null) {
			Meter meter = meterGroup.getMeter(id);
			
			if (meter != null) {
				DimPos pos = meter.getPos();
				WorldServer newWorld = server.getWorldOf(pos);
				
				if (newWorld != null) {
					WorldServer oldWorld = player.getServerWorld();
					double oldX = player.posX;
					double oldY = player.posY;
					double oldZ = player.posZ;
					
					BlockPos blockPos = pos.getBlockPos();
					
					double newX = blockPos.getX() + 0.5D;
					double newY = blockPos.getY();
					double newZ = blockPos.getZ() + 0.5D;
					float yaw = player.rotationYaw;
					float pitch = player.rotationPitch;
					
					player.teleport(newWorld, newX, newY, newZ, yaw, pitch);
					sendClickableReturnMessage(oldWorld, oldX, oldY, oldZ, yaw, pitch, player);
				}
			}
		}
	}
	
	/**
	 * Send the player a message they can click to return
	 * to the location they were at before teleporting to
	 * a meter.
	 */
	private void sendClickableReturnMessage(WorldServer world, double _x, double _y, double _z, float _yaw, float _pitch, EntityPlayerMP player) {
		NumberFormat f = NumberFormat.getNumberInstance(Locale.US); // use . as decimal separator
		
		String dimensionId = DimensionType.getKey(world.dimension.getType()).toString();
		String x = f.format(_x);
		String y = f.format(_y);
		String z = f.format(_z);
		String yaw = f.format(_yaw);
		String pitch = f.format(_pitch);
		
		ITextComponent message = new TextComponentString("Click ").
		        appendSibling(new TextComponentString("[here]").applyTextStyle(style -> {
				style.
					setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Teleport to").
						appendSibling(TextUtils.formatFancyText("\n  dimension", dimensionId)).
						appendSibling(TextUtils.formatFancyText("\n  x", x)).
						appendSibling(TextUtils.formatFancyText("\n  y", y)).
						appendSibling(TextUtils.formatFancyText("\n  z", z)))).
					setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/execute in %s run tp @s %s %s %s %s %s", dimensionId, x, y, z, yaw, pitch))).
					setColor(TextFormatting.GREEN);
			})).
		        appendSibling(new TextComponentString(" to return to your previous location"));
		
		server.sendMessage(player, message, false);
	}
	
	public void onBlockChange(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		Block oldBlock = oldState.getBlock();
		Block newBlock = newState.getBlock();
		
		if (oldBlock == newBlock && newBlock.isPowerSource() && ((PowerSource)newBlock).logPowerChangeOnStateChange()) {
			logPowerChange(world, pos, oldState, newState);
		}
		
		boolean wasMeterable = oldBlock.isMeterable();
		boolean isMeterable = newBlock.isMeterable();
		
		if (wasMeterable || isMeterable) {
			logActive(world, pos, newState);
		}
	}
	
	public void logPowered(World world, BlockPos pos, boolean powered) {
		tryLogEvent(world, pos, EventType.POWERED, powered ? 1 : 0, (meterGroup, meter, event) -> meter.setPowered(powered));
	}
	
	public void logPowered(World world, BlockPos pos, IBlockState state) {
		tryLogEvent(world, pos, (meterGroup, meter, event) -> meter.setPowered(event.getMetadata() != 0), new MeterEventSupplier(EventType.POWERED, () -> {
			return state.getBlock().isPowered(world, pos, state) ? 1 : 0;
		}));
	}
	
	public void logActive(World world, BlockPos pos, boolean active) {
		tryLogEvent(world, pos, EventType.ACTIVE, active ? 1 : 0, (meterGroup, meter, event) -> meter.setActive(active));
	}
	
	public void logActive(World world, BlockPos pos, IBlockState state) {
		tryLogEvent(world, pos, (meterGroup, meter, event) -> meter.setActive(event.getMetadata() != 0), new MeterEventSupplier(EventType.ACTIVE, () -> {
			Block block = state.getBlock();
			return block.isMeterable() && ((Meterable)block).isActive(world, pos, state) ? 1 : 0;
		}));
	}
	
	public void logMoved(World world, BlockPos blockPos, EnumFacing dir) {
		tryLogEvent(world, blockPos, EventType.MOVED, dir.getIndex());
	}
	
	public void moveMeters(World world, BlockPos blockPos, EnumFacing dir) {
		DimPos pos = new DimPos(world, blockPos);
		
		for (ServerMeterGroup meterGroup : meterGroups.values()) {
			meterGroup.tryMoveMeter(pos, dir);
		}
	}
	
	public void logPowerChange(World world, BlockPos pos, int oldPower, int newPower) {
		if (oldPower != newPower) {
			tryLogEvent(world, pos, EventType.POWER_CHANGE, (oldPower << 8) | newPower);
		}
	}
	
	public void logPowerChange(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		tryLogEvent(world, pos, (meterGroup, meter, event) -> {
			int data = event.getMetadata();
			int oldPower = (data >> 8) & 0xFF;
			int newPower =  data       & 0xFF;
			
			return oldPower != newPower;
		}, new MeterEventSupplier(EventType.POWER_CHANGE, () -> {
			PowerSource block = (PowerSource)newState.getBlock();
			int oldPower = block.getPowerLevel(world, pos, oldState);
			int newPower = block.getPowerLevel(world, pos, newState);
			
			return (oldPower << 8) | newPower;
		}));
	}
	
	public void logRandomTick(World world, BlockPos pos) {
		tryLogEvent(world, pos, EventType.RANDOM_TICK, 0);
	}
	
	public void logScheduledTick(World world, NextTickListEntry<?> scheduledTick) {
		tryLogEvent(world, scheduledTick.position, EventType.SCHEDULED_TICK, scheduledTick.priority.getPriority());
	}
	
	public void logBlockEvent(World world, BlockEventData blockEvent) {
		tryLogEvent(world, blockEvent.getPosition(), EventType.BLOCK_EVENT, blockEvent.getEventID());
	}
	
	public void logEntityTick(World world, Entity entity) {
		tryLogEvent(world, entity.getPosition(), EventType.ENTITY_TICK, 0);
	}
	
	public void logBlockEntityTick(World world, TileEntity blockEntity) {
		tryLogEvent(world, blockEntity.getPos(), EventType.BLOCK_ENTITY_TICK, 0);
	}
	
	public void logBlockUpdate(World world, BlockPos pos) {
		tryLogEvent(world, pos, EventType.BLOCK_UPDATE, 0);
	}
	
	public void logComparatorUpdate(World world, BlockPos pos) {
		tryLogEvent(world, pos, EventType.COMPARATOR_UPDATE, 0);
	}
	
	public void logShapeUpdate(World world, BlockPos pos, EnumFacing dir) {
		tryLogEvent(world, pos, EventType.SHAPE_UPDATE, dir.getIndex());
	}
	
	public void logObserverUpdate(World world, BlockPos pos) {
		tryLogEvent(world, pos, EventType.OBSERVER_UPDATE, 0);
	}
	
	public void logInteractBlock(World world, BlockPos pos) {
		tryLogEvent(world, pos, EventType.INTERACT_BLOCK, 0);
	}
	
	private void tryLogEvent(World world, BlockPos pos, EventType type, int data) {
		tryLogEvent(world, pos, type, data, (meterGroup, meter, event) -> true);
	}
	
	private void tryLogEvent(World world, BlockPos pos, EventType type, int data, MeterEventPredicate predicate) {
		tryLogEvent(world, pos, predicate, new MeterEventSupplier(type, () -> data));
	}
	
	private void tryLogEvent(World world, BlockPos blockPos, MeterEventPredicate predicate, MeterEventSupplier supplier) {
		if (options.hasEventType(supplier.type())) {
			DimPos pos = new DimPos(world, blockPos);
			
			for (ServerMeterGroup meterGroup : meterGroups.values()) {
				if (!meterGroup.isIdle()) {
					meterGroup.tryLogEvent(pos, predicate, supplier);
				}
			}
		}
	}
}
