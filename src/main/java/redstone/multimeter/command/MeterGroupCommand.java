package redstone.multimeter.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiFunction;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import carpet.settings.CarpetSettings;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import redstone.multimeter.RedstoneMultimeter;
import redstone.multimeter.common.meter.MeterGroup;
import redstone.multimeter.server.Multimeter;
import redstone.multimeter.server.MultimeterServer;
import redstone.multimeter.server.meter.ServerMeterGroup;

public class MeterGroupCommand {
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> builder = Commands.
			literal("metergroup").
			requires(source -> isMultimeterClient(source)).
			then(Commands.
				literal("clear").
				executes(context -> clear(context.getSource()))).
			then(Commands.
				literal("subscribe").
				executes(context -> subscribe(context.getSource(), null)).
				then(Commands.
					argument("name", StringArgumentType.greedyString()).
					suggests((context, suggestionsBuilder) -> ISuggestionProvider.suggest(listMeterGroups(context.getSource()), suggestionsBuilder)).
					executes(context -> subscribe(context.getSource(), StringArgumentType.getString(context, "name"))))).
			then(Commands.
				literal("unsubscribe").
				executes(context -> unsubscribe(context.getSource()))).
			then(Commands.
				literal("private").
				requires(source -> isOwnerOfSubscription(source)).
				executes(context -> queryPrivate(context.getSource())).
				then(Commands.
					argument("private", BoolArgumentType.bool()).
					executes(context -> setPrivate(context.getSource(), BoolArgumentType.getBool(context, "private"))))).
			then(Commands.
				literal("members").
				requires(source -> isOwnerOfSubscription(source)).
				then(Commands.
					literal("clear").
					executes(context -> membersClear(context.getSource()))).
				then(Commands.
					literal("add").
					then(Commands.
						argument("player", EntityArgument.players()).
						executes(context -> membersAdd(context.getSource(), EntityArgument.getPlayers(context, "player"))))).
				then(Commands.
					literal("remove").
					then(Commands.
						argument("member", StringArgumentType.word()).
						suggests((context, suggestionsBuilder) -> ISuggestionProvider.suggest(listMembers(context.getSource()).keySet(), suggestionsBuilder)).
						executes(context -> membersRemovePlayer(context.getSource(), StringArgumentType.getString(context, "member"))))).
				then(Commands.
					literal("list").
					executes(context -> membersList(context.getSource())))).
			then(Commands.
				literal("list").
				executes(context -> list(context.getSource())));
		
		dispatcher.register(builder);
	}
	
	private static boolean isMultimeterClient(CommandSource source) {
	    return execute(source, (multimeter, player) -> multimeter.getMultimeterServer().isMultimeterClient(player));
	}
	
	private static boolean isOwnerOfSubscription(CommandSource source) {
		return execute(source, (multimeter, player) -> multimeter.isOwnerOfSubscription(player));
	}
	
	private static Collection<String> listMeterGroups(CommandSource source) {
		List<String> names = new ArrayList<>();
		
		command(source, (multimeter, player) -> {
			for (ServerMeterGroup meterGroup : multimeter.getMeterGroups()) {
				if (!meterGroup.isPrivate() || meterGroup.hasMember(player) || meterGroup.isOwnedBy(player)) {
					names.add(meterGroup.getName());
				}
			}
		});
		
		return names;
	}
	
	private static Map<String, UUID> listMembers(CommandSource source) {
		Map<String, UUID> names = new HashMap<>();
		
		command(source, (multimeter, player) -> {
			ServerMeterGroup meterGroup = multimeter.getSubscription(player);
			
			if (meterGroup != null && meterGroup.isOwnedBy(player)) {
				for (UUID playerUUID : meterGroup.getMembers()) {
					String playerName = multimeter.getMultimeterServer().getPlayerName(playerUUID);
					
					if (playerName != null) {
						names.put(playerName, playerUUID);
					}
				}
			}
		});
		
		return names;
	}
	
	private static int clear(CommandSource source) {
		return command(source, (multimeter, meterGroup, player) -> {
			multimeter.clearMeterGroup(player);
			source.sendFeedback(new TextComponentString(String.format("Removed all meters in meter group \'%s\'", multimeter.getSubscription(player).getName())), false);
		});
	}
	
	private static int subscribe(CommandSource source, String name) {
		return command(source, (multimeter, player) -> {
			if (!CarpetSettings.redstoneMultimeter) {
			    ITextComponent message = new TextComponentString("Please enable the 'redstoneMultimeter' carpet rule first!");
                source.sendFeedback(message, false);
			} else if (name == null) {
				multimeter.subscribeToDefaultMeterGroup(player);
				source.sendFeedback(new TextComponentString("Subscribed to default meter group"), false);
			} else if (multimeter.hasMeterGroup(name)) {
				ServerMeterGroup meterGroup = multimeter.getMeterGroup(name);
				
				if (!meterGroup.isPrivate() || meterGroup.hasMember(player) || meterGroup.isOwnedBy(player)) {
					multimeter.subscribeToMeterGroup(meterGroup, player);
					source.sendFeedback(new TextComponentString(String.format("Subscribed to meter group \'%s\'", name)), false);
				} else {
					source.sendFeedback(new TextComponentString("A meter group with that name already exists and it is private!"), false);
				}
			} else {
				if (MeterGroup.isValidName(name)) {
					multimeter.createMeterGroup(player, name);
					source.sendFeedback(new TextComponentString(String.format("Created meter group \'%s\'", name)), false);
				} else {
					source.sendFeedback(new TextComponentString(String.format("\'%s\' is not a valid meter group name!", name)), false);
				}
			}
		});
	}
	
	private static int unsubscribe(CommandSource source) {
		return command(source, (multimeter, meterGroup, player) -> {
			multimeter.unsubscribeFromMeterGroup(meterGroup, player);
			source.sendFeedback(new TextComponentString(String.format("Unsubscribed from meter group \'%s\'", meterGroup.getName())), false);
		});
	}
	
	private static int queryPrivate(CommandSource source) {
		return command(source, (multimeter, meterGroup, player) -> {
			String status = meterGroup.isPrivate() ? "private" : "public";
			source.sendFeedback(new TextComponentString(String.format("Meter group \'%s\' is %s", meterGroup.getName(), status)), false);
		});
	}
	
	private static int setPrivate(CommandSource source, boolean isPrivate) {
		return command(source, (multimeter, meterGroup, player) -> {
			if (meterGroup.isOwnedBy(player)) {
				meterGroup.setPrivate(isPrivate);
				source.sendFeedback(new TextComponentString(String.format("Meter group \'%s\' is now %s", meterGroup.getName(), (isPrivate ? "private" : "public"))), false);
			} else {
				source.sendFeedback(new TextComponentString("Only the owner of a meter group can change its privacy!"), false);
			}
		});
	}
	
	private static int membersClear(CommandSource source) {
		return commandMembers(source, (multimeter, meterGroup, owner) -> {
			multimeter.clearMembersOfMeterGroup(meterGroup);
			source.sendFeedback(new TextComponentString(String.format("Removed all members from meter group \'%s\'", meterGroup.getName())), false);
		});
	}
	
	private static int membersAdd(CommandSource source, Collection<EntityPlayerMP> players) {
		return commandMembers(source, (multimeter, meterGroup, owner) -> {
			for (EntityPlayerMP player : players) {
				if (player == owner) {
					source.sendFeedback(new TextComponentString("You cannot add yourself as a member!"), false);
				} else if (meterGroup.hasMember(player)) {
					source.sendFeedback(new TextComponentString(String.format("Player \'%s\' is already a member of meter group \'%s\'!", player.getScoreboardName(), meterGroup.getName())), false);
				} else if (!multimeter.getMultimeterServer().isMultimeterClient(player)) {
					source.sendFeedback(new TextComponentString(String.format("You cannot add player \'%s\' as a member; they do not have %s installed!", player.getScoreboardName(), RedstoneMultimeter.MOD_NAME)), false);
				} else {
					multimeter.addMemberToMeterGroup(meterGroup, player.getUniqueID());
					source.sendFeedback(new TextComponentString(String.format("Player \'%s\' is now a member of meter group \'%s\'", player.getScoreboardName(), meterGroup.getName())), false);
				}
			}
		});
	}
	
	private static int membersRemovePlayer(CommandSource source, String playerName) {
		return commandMembers(source, (multimeter, meterGroup, owner) -> {
			Entry<String, UUID> member = findMember(listMembers(source), playerName);
			
			if (member == null) {
				EntityPlayerMP player = multimeter.getMultimeterServer().getPlayer(playerName);
				
				if (player == owner) {
					source.sendFeedback(new TextComponentString("You cannot remove yourself as a member!"), false);
				} else {
					source.sendFeedback(new TextComponentString(String.format("Meter group \'%s\' has no member with the name \'%s\'!", meterGroup.getName(), playerName)), false);
				}
			} else {
				multimeter.removeMemberFromMeterGroup(meterGroup, member.getValue());
				source.sendFeedback(new TextComponentString(String.format("Player \'%s\' is no longer a member of meter group \'%s\'", member.getKey(), meterGroup.getName())), false);
			}
		});
	}
	
	private static Entry<String, UUID> findMember(Map<String, UUID> members, String playerName) {
		String key = playerName.toLowerCase();
		
		for (Entry<String, UUID> member : members.entrySet()) {
			if (member.getKey().toLowerCase().equals(key)) {
				return member;
			}
		}
		
		return null;
	}
	
	private static int membersList(CommandSource source) {
		Map<String, UUID> members = listMembers(source);
		
		return commandMembers(source, (multimeter, meterGroup, owner) -> {
			if (members.isEmpty()) {
				source.sendFeedback(new TextComponentString(String.format("Meter group \'%s\' has no members yet!", meterGroup.getName())), false);
			} else {
				String message = String.format("Members of meter group \'%s\':\n  ", meterGroup.getName()) + String.join("\n  ", members.keySet());
				source.sendFeedback(new TextComponentString(message), false);
			}
		});
	}
	
	private static int commandMembers(CommandSource source, MeterGroupCommandExecutor command) {
		return command(source, (multimeter, meterGroup, player) -> {
			if (meterGroup.isOwnedBy(player)) {
				command.execute(multimeter, meterGroup, player);
				
				if (!meterGroup.isPrivate()) {
					source.sendFeedback(new TextComponentString("NOTE: this meter group is public; adding/removing members will not have any effect until you make it private!"), false);
				}
			}
		});
	}
	
	private static int list(CommandSource source) {
		Collection<String> names = listMeterGroups(source);
		
		if (names.isEmpty()) {
			source.sendFeedback(new TextComponentString("There are no meter groups yet!"), false);
		} else {
			String message = "Meter groups:\n  " + String.join("\n  ", names);
			source.sendFeedback(new TextComponentString(message), false);
		}
		
		return Command.SINGLE_SUCCESS;
	}
	
	private static int command(CommandSource source, MeterGroupCommandExecutor command) {
		return command(source, (multimeter, player) -> {
			if (!CarpetSettings.redstoneMultimeter) {
			    ITextComponent message = new TextComponentString("Please enable the 'redstoneMultimeter' carpet rule first!");
	            source.sendFeedback(message, false);
	            
			    return;
			}
		    
		    ServerMeterGroup meterGroup = multimeter.getSubscription(player);
			
			if (meterGroup == null) {
				source.sendFeedback(new TextComponentString("Please subscribe to a meter group first!"), false);
			} else {
				command.execute(multimeter, meterGroup, player);
			}
		});
	}
	
	private static int command(CommandSource source, MultimeterCommandExecutor command) {
		return execute(source, (m, p) -> { command.execute(m, p); return true; }) ? Command.SINGLE_SUCCESS : 0;
	}
	
	private static boolean execute(CommandSource source, BiFunction<Multimeter, EntityPlayerMP, Boolean> command) {
		try {
			EntityPlayerMP player = source.asPlayer();
			MinecraftServer server = source.getServer();
			MultimeterServer multimeterServer = server.getMultimeterServer();
			Multimeter multimeter = multimeterServer.getMultimeter();
			
			return command.apply(multimeter, player);
		} catch (CommandSyntaxException e) {
			return false;
		}
	}
	
	@FunctionalInterface
	private static interface MultimeterCommandExecutor {
		
		public void execute(Multimeter multimeter, EntityPlayerMP player);
		
	}
	
	@FunctionalInterface
	private static interface MeterGroupCommandExecutor {
		
		public void execute(Multimeter multimeter, ServerMeterGroup meterGroup, EntityPlayerMP player);
		
	}
}
