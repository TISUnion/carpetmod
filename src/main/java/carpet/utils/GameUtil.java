package carpet.utils;

import carpet.CarpetServer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.dimension.DimensionType;

public class GameUtil
{
	public static long getGameTime()
	{
		return CarpetServer.minecraft_server.getWorld(DimensionType.OVERWORLD).getGameTime();
	}

	public static boolean isOnServerThread()
	{
		return CarpetServer.minecraft_server != null && CarpetServer.minecraft_server.isCallingFromMinecraftThread();
	}

	public static boolean countsTowardsMobcap(Entity entity)
	{
		if (entity instanceof EntityLiving)
		{
			EntityLiving entityLiving = (EntityLiving)entity;
			return !entityLiving.isNoDespawnRequired();
		}
		return false;
	}

	public static void ensureOnServerThread(Runnable runnable)
	{
		CarpetServer.minecraft_server.addScheduledTask(runnable);
	}
}
