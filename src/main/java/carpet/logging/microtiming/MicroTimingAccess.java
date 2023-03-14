package carpet.logging.microtiming;

import carpet.logging.microtiming.tickphase.TickPhase;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class MicroTimingAccess
{
	public static TickPhase getTickPhase(WorldServer world)
	{
		return world.getMicroTickLogger().getTickPhase();
	}

	public static TickPhase getTickPhase(World world)
	{
		return world instanceof WorldServer ? getTickPhase((WorldServer)world) : getTickPhase();
	}

	public static TickPhase getTickPhase()
	{
		WorldServer serverWorld = MicroTimingLoggerManager.getCurrentWorld();
		if (serverWorld != null)
		{
			return getTickPhase(serverWorld);
		}
		else
		{
			return MicroTimingLoggerManager.getOffWorldTickPhase();
		}
	}
}
