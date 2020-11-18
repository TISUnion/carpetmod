package carpet;

import carpet.commands.*;
import carpet.helpers.TickSpeed;
import carpet.logging.LoggerRegistry;
import carpet.microtiming.MicroTimingLoggerManager;
import carpet.network.CarpetServerNetworkHandler;
import carpet.script.CarpetScriptServer;
import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import carpet.utils.HUDController;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class CarpetServer // static for now - easier to handle all around the code, its one anyways
{
    public static final String MINECRAFT_BRAND = "tis-carpet";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Random rand = new Random((int)((2>>16)*Math.random()));
    public static MinecraftServer minecraft_server;
    public static CarpetScriptServer scriptServer;
    public static SettingsManager settingsManager;
    static
    {
        SettingsManager.parseSettingsClass(carpet.settings.CarpetSettings.class);
        //...
    }
    public static void init(MinecraftServer server) //aka constructor of this static singleton class
    {
        CarpetServer.minecraft_server = server;
    }
    public static void onServerLoaded(MinecraftServer server)
    {
        settingsManager = new SettingsManager(server);
        scriptServer = new CarpetScriptServer();
        MicroTimingLoggerManager.attachServer(server);
        //ExpressionInspector.CarpetExpression_resetExpressionEngine();
    }
    // Separate from onServerLoaded, because a server can be loaded multiple times in singleplayer
    public static void onGameStarted() {
        LoggerRegistry.initLoggers();
    }

    public static void onServerClosed(MinecraftServer server)
    {
        MicroTimingLoggerManager.detachServer();
        disconnect();
    }

    public static void tick(MinecraftServer server)
    {
        TickSpeed.tick(server);
        HUDController.update_hud(server);
        scriptServer.events.tick(); // in 1.14 make sure its called in the aftertick
        //in case something happens
        CarpetSettings.impendingFillSkipUpdates = false;
    }

    public static void registerCarpetCommands(CommandDispatcher<CommandSource> dispatcher)
    {
        CarpetCommand.register(dispatcher);
        TickCommand.register(dispatcher);
        CounterCommand.register(dispatcher);
        LogCommand.register(dispatcher);
        SpawnCommand.register(dispatcher);
        PlayerCommand.register(dispatcher);
        CameraModeCommand.register(dispatcher);
        InfoCommand.register(dispatcher);
        DistanceCommand.register(dispatcher);
        PerimeterInfoCommand.register(dispatcher);
        DrawCommand.register(dispatcher);
        ScriptCommand.register(dispatcher);
        PingCommand.register(dispatcher);
        EpsCommand.register(dispatcher);

        //TestCommand.register(dispatcher);
    }

    public static void disconnect()
    {
        // this for whatever reason gets called multiple times even when joining;
        TickSpeed.reset();
        settingsManager.detachServer();
    }

    public static void onPlayerLoggedIn(EntityPlayerMP player)
    {
        CarpetServerNetworkHandler.onPlayerJoin(player);
        LoggerRegistry.playerConnected(player);
    }
}

