package carpet.commands;

import carpet.utils.TranslatableBase;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

public abstract class AbstractCommand extends TranslatableBase
{
	public AbstractCommand(String name)
	{
		super("command", name);
	}

	public abstract void registerCommand(CommandDispatcher<CommandSource> dispatcher);
}
