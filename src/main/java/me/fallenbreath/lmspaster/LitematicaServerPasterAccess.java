package me.fallenbreath.lmspaster;

import carpet.settings.CarpetSettings;
import me.fallenbreath.lmspaster.network.LmsPasterPayload;
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
		if (!CarpetSettings.modLitematicaServerPaster)
		{
			return;
		}

		ResourceLocation channel = packetIn.getChannel();
		if (Network.CHANNEL.equals(channel))
		{
			LmsPasterPayload payload = new LmsPasterPayload(packetIn.getData());
			ServerNetworkHandler.handleClientPacket(payload, player);
		}
	}
}
