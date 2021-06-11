package carpet.commands.lifetime;

import carpet.commands.AbstractCommand;
import carpet.commands.lifetime.filter.EntityFilterManager;
import carpet.commands.lifetime.utils.LifeTimeTrackerUtil;
import carpet.commands.lifetime.utils.SpecificDetailMode;
import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.EntityType;

import javax.annotation.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;
import static net.minecraft.command.ISuggestionProvider.suggest;

public class LifeTimeCommand extends AbstractCommand
{
	private static final String NAME = "lifetime";
	private static final LifeTimeCommand INSTANCE = new LifeTimeCommand();

	private LifeTimeCommand()
	{
		super(NAME);
	}

	public static LifeTimeCommand getInstance()
	{
		return INSTANCE;
	}

	private int checkEntityTypeThen(CommandSource source, @Nullable String entityTypeName, Consumer<EntityType<?>> entityTypeConsumer)
	{
		EntityType<?> entityType;
		// global filter
		if (entityTypeName == null)
		{
			entityType = null;
		}
		// filter for specific entity type
		else
		{
			Optional<EntityType<?>> optionalEntityType = LifeTimeTrackerUtil.getEntityTypeFromName(entityTypeName);
			if (optionalEntityType.isPresent())
			{
				entityType = optionalEntityType.get();
			}
			else
			{
				LifeTimeTracker.getInstance().sendUnknownEntity(source, entityTypeName);
				return 0;
			}
		}
		entityTypeConsumer.accept(entityType);
		return 1;
	}

	private int setEntityFilter(CommandSource source, @Nullable String entityTypeName, EntitySelector selector)
	{
		return checkEntityTypeThen(source, entityTypeName, entityType ->
				EntityFilterManager.getInstance().setEntityFilter(source, entityType, selector)
		);
	}

	private int printEntityFilter(CommandSource source, @Nullable String entityTypeName)
	{
		return checkEntityTypeThen(source, entityTypeName, entityType ->
				EntityFilterManager.getInstance().displayFilter(source, entityType)
		);
	}

	/*
	 * ------------------
	 *   Node factories
	 * ------------------
	 */

	private ArgumentBuilder<CommandSource, ?> createFilterNode(ArgumentBuilder<CommandSource, ?> node, Function<CommandContext<CommandSource>, String> entityTypeNameSupplier)
	{
		return node.
				executes(c -> printEntityFilter(c.getSource(), entityTypeNameSupplier.apply(c))).
				then(literal("set").then(
						argument("filter", EntityArgument.entities()).
								executes(c -> setEntityFilter(c.getSource(), entityTypeNameSupplier.apply(c), c.getArgument("filter", EntitySelector.class)))
				)).
				then(literal("clear").executes(c -> setEntityFilter(c.getSource(), entityTypeNameSupplier.apply(c), null)));
	}

	/**
	 * make the node execute something with realtime=false,
	 * then with another extra literal "realtime" input it will execute something with realtime=true
	 */
	private ArgumentBuilder<CommandSource, ?> realtimeActionNode(ArgumentBuilder<CommandSource, ?> node, BiFunction<CommandContext<CommandSource>, Boolean, Integer> action)
	{
		return node.executes(c -> action.apply(c, false)).
				then(literal("realtime").executes(c -> action.apply(c, true)));
	}

	/*
	 * -----------------------
	 *   Node factories ends
	 * -----------------------
	 */

	@Override
	public void registerCommand(CommandDispatcher<CommandSource> dispatcher)
	{
		final String entityTypeArg = "entity_type";
		final String detailModeArg = "detail";
		LiteralArgumentBuilder<CommandSource> builder = literal(NAME).
				requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandLifeTime)).
				executes(c -> LifeTimeTracker.getInstance().showHelp(c.getSource())).
				// lifetime tracking [general tracker stuffs here]
						then(
						LifeTimeTracker.getInstance().getTrackingArgumentBuilder()
				).
				// lifetime filter
						then(
						literal("filter").
								executes(c -> EntityFilterManager.getInstance().displayAllFilters(c.getSource())).
								then(createFilterNode(literal("global"), c -> null)).
								then(createFilterNode(
										argument(entityTypeArg, string()).suggests((c, b) -> suggest(LifeTimeTrackerUtil.getEntityTypeDescriptorStream(), b)),
										c -> getString(c, entityTypeArg)
								))
				).
				// lifetime result display
						then(realtimeActionNode(
						argument(entityTypeArg, string()).suggests((c, b) -> suggest(LifeTimeTracker.getInstance().getAvailableEntityType(), b)).
								then(realtimeActionNode(
										argument(detailModeArg, string()).suggests((c, b) -> suggest(SpecificDetailMode.getSuggestion(), b)),
										// lifetime tracking creeper spawning
										(c, realtime) -> LifeTimeTracker.getInstance().printTrackingResultSpecific(
												c.getSource(), getString(c, entityTypeArg), getString(c, detailModeArg), realtime
										)
								)),
						// lifetime tracking creeper
						(c, realtime) -> LifeTimeTracker.getInstance().printTrackingResultSpecific(
								c.getSource(), getString(c, entityTypeArg), null, realtime
						)
				)).
						then(
								literal("help").
										executes(c -> LifeTimeTracker.getInstance().showHelp(c.getSource()))
						);
		dispatcher.register(builder);
	}
}
