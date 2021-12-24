package redstone.multimeter.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import redstone.multimeter.server.MultimeterServer;

public interface RSMMPacket {
	
	public void encode(NBTTagCompound data);
	
	public void decode(NBTTagCompound data);
	
	default boolean force() {
	    return false;
	}
	
	public void execute(MultimeterServer server, EntityPlayerMP player);
	
}
