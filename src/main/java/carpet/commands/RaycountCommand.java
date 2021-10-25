package carpet.commands;

import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class RaycountCommand
{
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> command = literal("raycount").
                requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandRaycount)).
                then(argument("block position", BlockPosArgument.blockPos()).
                        executes((c) -> updateRaycountCheckPosition(
                                c.getSource(),
                                BlockPosArgument.getBlockPos(c, "block position")))).
                then(literal("reset").
                        executes((c) -> updateRaycountCheckPosition(
                                c.getSource(),
                                null)));
        dispatcher.register(command);
    }

    private static int updateRaycountCheckPosition(CommandSource source, BlockPos blockPosition) {
        try
        {
            source.asPlayer().posToCheckRaycount = blockPosition;
            if (blockPosition != null)
            {
                source.asPlayer().sendMessage(new TextComponentString(
                        "Succesfully set the raycount position to x=" + blockPosition.getX() +
                                " y=" + blockPosition.getY() +
                                " z=" + blockPosition.getZ()
                ));
            }
            else
            {
                source.asPlayer().sendMessage(new TextComponentString(
                        "Sucessfully reset raycount position"
                ));
            }
        }
        catch (CommandSyntaxException ignored)
        {
            return 0;
        }
        return 1;
    }
}
