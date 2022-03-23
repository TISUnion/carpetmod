package me.fallenbreath.lmspaster.network;

import io.netty.buffer.Unpooled;
import me.fallenbreath.lmspaster.util.RegistryUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class Network
{
	public static final ResourceLocation CHANNEL = RegistryUtil.id("network");

	public static class C2S
	{
		public static final int HI = 0;
		public static final int CHAT = 1;

		public static CPacketCustomPayload packet(Consumer<PacketBuffer> byteBufBuilder)
		{
			PacketBuffer packetByteBuf = new PacketBuffer(Unpooled.buffer());
			byteBufBuilder.accept(packetByteBuf);
			return new CPacketCustomPayload(CHANNEL, packetByteBuf);
		}
	}

	public static class S2C
	{
		public static final int HI = 0;

		public static SPacketCustomPayload packet(Consumer<PacketBuffer> byteBufBuilder)
		{
			PacketBuffer packetByteBuf = new PacketBuffer(Unpooled.buffer());
			byteBufBuilder.accept(packetByteBuf);
			return new SPacketCustomPayload(CHANNEL, packetByteBuf);
		}
	}
}
