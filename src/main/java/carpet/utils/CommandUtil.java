package carpet.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.Optional;

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

	public static Optional<EntityPlayerMP> getPlayer(CommandSource source)
	{
		if (source != null)
		{
			try
			{
				return Optional.of(source.asPlayer());
			}
			catch (CommandSyntaxException ignored)
			{
			}
		}
		return Optional.empty();
	}

	public static boolean isPlayerCommandSource(CommandSource source)
	{
		return getPlayer(source).isPresent();
	}

	public static boolean isCreativePlayer(CommandSource source)
	{
		return getPlayer(source).
				map(EntityPlayerMP::isCreative).
				orElse(false);
	}

	public static boolean canCheat(CommandSource source)
	{
		return source.hasPermissionLevel(2);  // commonly used in cheaty commands
	}
}
