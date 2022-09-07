package me.fallenbreath.lmspaster.network;

import me.fallenbreath.lmspaster.LitematicaServerPasterMod;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;

public class ServerNetworkHandler
{
	private static final Map<NetHandlerPlayServer, StringBuilder> VERY_LONG_CHATS = new WeakHashMap<>();

	private static Optional<StringBuilder> getVeryLongChatBuilder(EntityPlayerMP player)
	{
		return Optional.ofNullable(VERY_LONG_CHATS.get(player.connection));
	}

	public static void handleClientPacket(PacketBuffer data, EntityPlayerMP player)
	{
		String playerName = player.getName().getString();
		int id = data.readVarInt();
		switch (id)
		{
			case Network.C2S.HI:
				String clientModVersion = data.readString(Short.MAX_VALUE);
				LitematicaServerPasterMod.LOGGER.info("Player {} connected with {} @ {}", playerName, LitematicaServerPasterMod.MOD_NAME, clientModVersion);
				player.connection.sendPacket(Network.S2C.packet(buf -> buf.
						writeVarInt(Network.S2C.HI).
						writeString(LitematicaServerPasterMod.VERSION)
				));
				player.connection.sendPacket(Network.S2C.packet(buf -> buf.
						writeVarInt(Network.S2C.ACCEPT_PACKETS).
						writeVarIntArray(Network.C2S.ALL_PACKET_IDS))
				);
				break;

			case Network.C2S.CHAT:
				LitematicaServerPasterMod.LOGGER.debug("Received chat from player {}", playerName);
				String message = data.readString(Short.MAX_VALUE);
				triggerCommand(player, playerName, message);
				break;

			case Network.C2S.VERY_LONG_CHAT_START:
				LitematicaServerPasterMod.LOGGER.debug("Received VERY_LONG_CHAT_START from player {}", playerName);
				VERY_LONG_CHATS.put(player.connection, new StringBuilder());
				break;

			case Network.C2S.VERY_LONG_CHAT_CONTENT:
				String content = data.readString(Short.MAX_VALUE);
				LitematicaServerPasterMod.LOGGER.debug("Received VERY_LONG_CHAT_CONTENT from player {} with length {}", playerName, content.length());
				getVeryLongChatBuilder(player).ifPresent(builder -> builder.append(content));
				break;

			case Network.C2S.VERY_LONG_CHAT_END:
				LitematicaServerPasterMod.LOGGER.debug("Received VERY_LONG_CHAT_END from player {}", playerName);
				getVeryLongChatBuilder(player).ifPresent(builder ->triggerCommand(player, playerName, builder.toString()));
				VERY_LONG_CHATS.remove(player.connection);
				break;
		}
	}

	private static void triggerCommand(EntityPlayerMP player, String playerName, String command)
	{
		if (command.isEmpty())
		{
			LitematicaServerPasterMod.LOGGER.warn("Player {} sent an empty command", playerName);
		}
		else
		{
			LitematicaServerPasterMod.LOGGER.debug("Player {} is sending a command with length {}", playerName, command.length());
			Objects.requireNonNull(player.getServer()).addScheduledTask(
					() -> player.connection.invokeExecuteCommand(command)
			);
		}
	}
}
