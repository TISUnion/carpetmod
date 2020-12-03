package carpet.utils;

import carpet.CarpetServer;
import net.minecraft.world.dimension.DimensionType;

public class GameUtil
{
	public static long getGameTime()
	{
		return CarpetServer.minecraft_server.getWorld(DimensionType.OVERWORLD).getGameTime();
	}

	public boolean isOnServerThread()
	{
		return CarpetServer.minecraft_server != null && CarpetServer.minecraft_server.isCallingFromMinecraftThread();
	}
}
