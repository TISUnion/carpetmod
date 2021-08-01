package carpet.commands;

import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;

import java.util.Collections;

import static net.minecraft.command.Commands.literal;

public class RefreshCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> literalargumentbuilder = literal("refresh").
				requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandRefresh)).
				then(literal("inventory").executes(c -> refreshInventory(c.getSource())));
		dispatcher.register(literalargumentbuilder);
	}

	private static int refreshInventory(CommandSource source) throws CommandSyntaxException
	{
		source.getServer().getPlayerList().sendInventory(source.asPlayer());
		Messenger.send(source, Collections.singleton(Messenger.s("Inventory refreshed")));
		return 1;
	}
}
