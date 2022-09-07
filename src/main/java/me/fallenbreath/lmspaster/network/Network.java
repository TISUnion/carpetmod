package me.fallenbreath.lmspaster.network;

import com.google.common.collect.Sets;
import io.netty.buffer.Unpooled;
import me.fallenbreath.lmspaster.LitematicaServerPasterMod;
import me.fallenbreath.lmspaster.util.RegistryUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.function.Consumer;

public class Network
{
	public static final ResourceLocation CHANNEL = RegistryUtil.id("network");

	public static class C2S
	{
		public static final int HI = 0;
		public static final int CHAT = 1;
		public static final int VERY_LONG_CHAT_START = 2;
		public static final int VERY_LONG_CHAT_CONTENT = 3;
		public static final int VERY_LONG_CHAT_END = 4;

		public static final int[] ALL_PACKET_IDS;

		static
		{
			Set<Integer> allPacketIds = Sets.newLinkedHashSet();
			for (Field field : C2S.class.getFields())
			{
				if (field.getType() == int.class && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
				{
					try
					{
						int id = (int) field.get(null);
						boolean notExists = allPacketIds.add(id);
						if (!notExists)
						{
							LitematicaServerPasterMod.LOGGER.error("Duplicated packet id {} ({})", id, field.getName());
						}
					}
					catch (Exception e)
					{
						LitematicaServerPasterMod.LOGGER.error("Failed to access field {}: {}", field, e);
					}
				}
			}
			ALL_PACKET_IDS = new int[allPacketIds.size()];
			int i = 0;
			for (Integer id : allPacketIds)
			{
				ALL_PACKET_IDS[i++] = id;
			}
		}

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
		public static final int ACCEPT_PACKETS = 1;

		public static SPacketCustomPayload packet(Consumer<PacketBuffer> byteBufBuilder)
		{
			PacketBuffer packetByteBuf = new PacketBuffer(Unpooled.buffer());
			byteBufBuilder.accept(packetByteBuf);
			return new SPacketCustomPayload(CHANNEL, packetByteBuf);
		}
	}
}
