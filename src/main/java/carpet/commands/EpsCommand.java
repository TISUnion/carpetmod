package carpet.commands;

import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import carpet.utils.Messenger;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class EpsCommand
{
    public final static long epsTestTimeSecondDefault = 30;
    public static long epsTestTimeSecond = epsTestTimeSecondDefault;
    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralArgumentBuilder<CommandSource> command = literal("epsTest").
                requires( (player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandEPSTest)).
                executes( (c) -> prepareTest(c.getSource(), epsTestTimeSecondDefault)).
                then(argument("seconds", integer(1, 3600)).
                        suggests( (c, b) -> ISuggestionProvider.suggest(new String[]{"10", "120"}, b)).
                        executes( (c) -> prepareTest(c.getSource(), getInteger(c, "seconds"))));

        dispatcher.register(command);
    }

    private static int prepareTest(CommandSource source, long duration)
    {
        Messenger.m(source,String.format("[TISCM]: EPS test is now active, duration: %ds", duration));
        CarpetSettings.isEpsActive = true;
        epsTestTimeSecond = duration;
        return 1;
    }

    public static void runTest(MinecraftServer server)
    {
        WorldServer world = server.getWorld(DimensionType.OVERWORLD);

        long endtime = Util.milliTime() + EpsCommand.epsTestTimeSecond * 1000;
        long counter = 0;
        server.logInfo(String.format("[TISCM]: starting EPS test for %d seconds... Please wait", EpsCommand.epsTestTimeSecond));

        while (endtime > Util.milliTime())
        {
            world.createExplosion(null, 0, -100, 0, 4, false);
            counter++;
            //trick that dog
            server.dontPanic();
        }
        Messenger.print_server_message(server, "[TISCM]: EPS test finished, result is:" + counter / EpsCommand.epsTestTimeSecond);
        CarpetSettings.isEpsActive = false;
    }
}