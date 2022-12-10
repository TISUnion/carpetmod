package carpet.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;

public class CommandUtil
{
	public static boolean isConsoleCommandSource(CommandSource commandSource)
	{
		if (commandSource != null)
		{
			return commandSource.getSource() instanceof MinecraftServer;
		}
		return false;
	}

	public static boolean isPlayerCommandSource(CommandSource commandSource)
	{
		if (commandSource != null)
		{
			try
			{
				commandSource.asPlayer();
				return true;
			}
			catch (CommandSyntaxException e)
			{
				return false;
			}
		}
		return false;
	}
}
