package me.fallenbreath.lmspaster.network;

import me.fallenbreath.lmspaster.LitematicaServerPasterMod;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import java.util.Objects;

public class ServerNetworkHandler
{
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
				break;

			case Network.C2S.CHAT:
				LitematicaServerPasterMod.LOGGER.debug("Received chat from player {}", playerName);
				String message = data.readString(Short.MAX_VALUE);
				if (message.isEmpty() || message.charAt(0) != '/')
				{
					LitematicaServerPasterMod.LOGGER.warn("Player {} sent a non-command chat message with length {}", playerName, message.length());
				}
				else
				{
					Objects.requireNonNull(player.getServer()).addScheduledTask(
							() -> player.connection.invokeExecuteCommand(message)
					);
				}
				break;
		}
	}
}
