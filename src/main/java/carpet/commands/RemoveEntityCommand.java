package carpet.commands;

import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;
import static net.minecraft.command.arguments.EntityArgument.entities;
import static net.minecraft.command.arguments.EntityArgument.getEntities;

public class RemoveEntityCommand extends AbstractCommand
{
	private static final String NAME = "removeentity";
	private static final RemoveEntityCommand INSTANCE = new RemoveEntityCommand();

	private RemoveEntityCommand()
	{
		super(NAME);
	}

	public static RemoveEntityCommand getInstance()
	{
		return INSTANCE;
	}

	@Override
	public void registerCommand(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> node = literal(NAME).
				requires(
						player -> SettingsManager.canUseCommand(player, CarpetSettings.commandRemoveEntity)
				).
				then(argument("target", entities()).
						executes(c -> removeEntities(c.getSource(), getEntities(c, "target")))
				);

		dispatcher.register(node);
	}

	private int removeEntities(CommandSource source, Collection<? extends Entity> entities)
	{
		List<? extends Entity> nonPlayerEntities = entities.stream().
				filter(entity -> !(entity instanceof EntityPlayer)).
				collect(Collectors.toList());
		nonPlayerEntities.forEach(Entity::remove);
		Messenger.tell(source, advTr("success", "Removed %1$s entities", nonPlayerEntities.size()), true);
		return nonPlayerEntities.size();
	}
}
