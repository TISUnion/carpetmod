package me.fallenbreath.lmspaster;

import carpet.settings.CarpetSettings;
import me.fallenbreath.lmspaster.network.Network;
import me.fallenbreath.lmspaster.network.ServerNetworkHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;

/**
 * Litematica server master mod access interface for TISCM
 */
public class LitematicaServerPasterAccess
{
	public static void onPacket(CPacketCustomPayload packetIn, EntityPlayerMP player)
	{
		if (!CarpetSettings.litematicaServerPaster)
		{
			return;
		}

		ResourceLocation channel = packetIn.getChannel();
		if (Network.CHANNEL.equals(channel))
		{
			ServerNetworkHandler.handleClientPacket(packetIn.getData(), player);
		}
	}
}