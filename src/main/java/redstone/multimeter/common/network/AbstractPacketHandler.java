package redstone.multimeter.common.network;

import carpet.utils.NetworkUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public abstract class AbstractPacketHandler {
	
	protected <P extends RSMMPacket> Packet<?> encode(P packet) {
		ResourceLocation id = PacketManager.getId(packet);
		
		if (id == null) {
			throw new IllegalStateException("Unable to encode packet: " + packet.getClass());
		}
		
		NBTTagCompound data = new NBTTagCompound();
		packet.encode(data);
		
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		
		buffer.writeResourceLocation(id);
		buffer.writeCompoundTag(data);
		
		return toCustomPayload(PacketManager.getPacketChannelId(), buffer);
	}
	
	protected abstract Packet<?> toCustomPayload(ResourceLocation id, PacketBuffer buffer);
	
	public abstract <P extends RSMMPacket> void send(P packet);
	
	protected <P extends RSMMPacket> P decode(PacketBuffer buffer) {
		ResourceLocation id = buffer.readResourceLocation();
		P packet = PacketManager.createPacket(id);
		
		if (packet == null) {
			throw new IllegalStateException("Unable to decode packet: " + id);
		}
		
		NBTTagCompound data = NetworkUtil.readNbt(buffer);
		packet.decode(data);
		
		return packet;
	}
}
