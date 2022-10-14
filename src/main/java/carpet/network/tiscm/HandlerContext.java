package carpet.network.tiscm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;

public class HandlerContext
{
	public static class S2C
	{
		public final NetHandlerPlayClient networkHandler;
		public final NBTTagCompound payload;
		public final Minecraft client;
		public final EntityPlayerSP player;

		public S2C(NetHandlerPlayClient networkHandler, NBTTagCompound payload)
		{
			this.payload = payload;
			this.networkHandler = networkHandler;
			this.client = networkHandler.getClient();
			this.player = this.client.player;
		}

		public void runSynced(Runnable runnable)
		{
			this.client.addScheduledTask(runnable);
		}

		public void send(TISCMProtocol.C2S packetId, Consumer<NBTTagCompound> payloadBuilder)
		{
			TISCMClientPacketHandler.getInstance().sendPacket(packetId, payloadBuilder);
		}
	}

	public static class C2S
	{
		public final NetHandlerPlayServer networkHandler;
		public final NBTTagCompound payload;
		public final MinecraftServer server;
		public final EntityPlayerMP player;
		public final String playerName;

		public C2S(NetHandlerPlayServer networkHandler, NBTTagCompound payload)
		{
			this.networkHandler = networkHandler;
			this.payload = payload;
			this.server = networkHandler.getServer();
			this.player = this.networkHandler.player;
			this.playerName = this.player.getName().getString();
		}

		public void runSynced(Runnable runnable)
		{
			this.server.addScheduledTask(runnable);
		}

		public void send(TISCMProtocol.S2C packetId, Consumer<NBTTagCompound> payloadBuilder)
		{
			TISCMServerPacketHandler.getInstance().sendPacket(this.networkHandler, packetId, payloadBuilder);
		}
	}
}
