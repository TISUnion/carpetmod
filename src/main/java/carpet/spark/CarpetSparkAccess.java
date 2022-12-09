package carpet.spark;

import carpet.spark.plugin.CarpetServerSparkPlugin;
import carpet.utils.TISCMConfig;
import net.minecraft.server.MinecraftServer;

/**
 * Interface for all spark accesses
 */
public class CarpetSparkAccess
{
	// ModInitializer
	public static void onInitialize()
	{
		if (TISCMConfig.MOD_SPARK)
		{
			new CarpetSparkMod().onInitialize();
		}
	}

	// fabric-api: ServerTickEvents.START_SERVER_TICK
	public static void ServerTickEvents_START_SERVER_TICK(MinecraftServer server)
	{
		if (TISCMConfig.MOD_SPARK)
		{
			CarpetServerSparkPlugin.getInstance().getTickHook().onStartServerTick();
			CarpetServerSparkPlugin.getInstance().getTickReporter().onStartServerTick();
		}
	}

	// fabric-api: ServerTickEvents.END_SERVER_TICK
	public static void ServerTickEvents_END_SERVER_TICK(MinecraftServer server)
	{
		if (TISCMConfig.MOD_SPARK)
		{
			CarpetServerSparkPlugin.getInstance().getTickReporter().onEndServerTick();
		}
	}

	// fabric-api: ServerLifecycleEvents.SERVER_STARTING
	public static void ServerLifecycleEvents_SERVER_STARTING(MinecraftServer server)
	{
		if (TISCMConfig.MOD_SPARK)
		{
			CarpetSparkMod.mod.initializeServer(server);
		}
	}

	// fabric-api: ServerLifecycleEvents.SERVER_STOPPING
	public static void ServerLifecycleEvents_SERVER_STOPPING(MinecraftServer server)
	{
		if (TISCMConfig.MOD_SPARK)
		{
			CarpetSparkMod.mod.onServerStopping(server);
		}
	}
}
