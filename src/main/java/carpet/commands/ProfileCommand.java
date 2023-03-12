package carpet.commands;

import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import static carpet.commands.TickCommand.healthEntities;
import static carpet.commands.TickCommand.healthReport;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class ProfileCommand
{
    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralArgumentBuilder<CommandSource> literalargumentbuilder = literal("profile").
                requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandProfile)).
                executes( (c) -> healthReport(c.getSource(), 100)).
                then(literal("health").
                        executes( (c) -> healthReport(c.getSource(), 100)).
                        then(argument("ticks", integer(1, 24000)).
                                executes( (c) -> healthReport(c.getSource(), getInteger(c, "ticks"))))).
                then(literal("entities").
                        executes((c) -> healthEntities(c.getSource(), 100)).
                        then(argument("ticks", integer(1, 24000)).
                                executes((c) -> healthEntities(c.getSource(), getInteger(c, "ticks"))).
                                then(argument("topN", integer(-1)).
                                        executes((c) -> healthEntities(c.getSource(), getInteger(c, "ticks"), getInteger(c, "topN"))))));
        dispatcher.register(literalargumentbuilder);
    }
}
