package carpet.commands;

import carpet.utils.TranslationContext;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

public abstract class AbstractCommand extends TranslationContext
{
	public AbstractCommand(String name)
	{
		super("command", name);
	}

	public abstract void registerCommand(CommandDispatcher<CommandSource> dispatcher);
}
