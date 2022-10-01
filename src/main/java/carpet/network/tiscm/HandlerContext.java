package carpet.network.tiscm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;

public class HandlerContext
{
	public static class S2C
	{
		public final NetHandlerPlayClient networkHandler;
		public final PacketBuffer buf;
		public final Minecraft client;
		public final EntityPlayerSP player;

		public S2C(NetHandlerPlayClient networkHandler, PacketBuffer buf)
		{
			this.buf = buf;
			this.networkHandler = networkHandler;
			this.client = networkHandler.getClient();
			this.player = this.client.player;
		}

		public void runSynced(Runnable runnable)
		{
			this.client.addScheduledTask(runnable);
		}

		public void send(TISCMProtocol.C2S packetId, Consumer<PacketBuffer> byteBufBuilder)
		{
			TISCMClientPacketHandler.getInstance().sendPacket(packetId, byteBufBuilder);
		}
	}

	public static class C2S
	{
		public final NetHandlerPlayServer networkHandler;
		public final PacketBuffer buf;
		public final MinecraftServer server;
		public final EntityPlayerMP player;
		public final String playerName;

		public C2S(NetHandlerPlayServer networkHandler, PacketBuffer buf)
		{
			this.networkHandler = networkHandler;
			this.buf = buf;
			this.server = networkHandler.getServer();
			this.player = this.networkHandler.player;
			this.playerName = this.player.getName().getString();
		}

		public void runSynced(Runnable runnable)
		{
			this.server.addScheduledTask(runnable);
		}

		public void send(TISCMProtocol.S2C packetId, Consumer<PacketBuffer> byteBufBuilder)
		{
			TISCMServerPacketHandler.getInstance().sendPacket(this.networkHandler, packetId, byteBufBuilder);
		}
	}
}
