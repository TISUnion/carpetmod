package carpet.commands;

import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

import static net.minecraft.command.Commands.literal;

public class PingCommand
{
    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralArgumentBuilder<CommandSource> command = literal("ping").
                requires( (player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandPing)).
                        executes( (c) ->
                        {
                            EntityPlayerMP player = c.getSource().asPlayer();
                            int ping = player.ping;
                            player.sendMessage(new TextComponentString("Your ping is: " + ping + " ms"));
                            return 1;
                        });
        
        dispatcher.register(command);
    }
}
