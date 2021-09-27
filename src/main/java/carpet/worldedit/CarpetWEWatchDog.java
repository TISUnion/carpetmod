package carpet.worldedit;

import com.sk89q.worldedit.extension.platform.Watchdog;
import net.minecraft.server.MinecraftServer;

public class CarpetWEWatchDog implements Watchdog
{
	private final MinecraftServer server;

	public CarpetWEWatchDog(MinecraftServer server)
	{
		this.server = server;
	}

	@Override
	public void tick()
	{
		this.server.watchDogTick$worldedit();
	}
}
