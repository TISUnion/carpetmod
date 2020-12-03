package carpet.commands.lifetime;

import carpet.commands.AbstractCommand;
import carpet.commands.lifetime.utils.SpecificDetailMode;
import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

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

	@Override
	public void registerCommand(CommandDispatcher<CommandSource> dispatcher)
	{
		String entityTypeArg = "entity_type";
		String detailModeArg = "detail";
		LiteralArgumentBuilder<CommandSource> builder = literal(NAME).
				requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandLifeTime)).
				// lifetime tracking [general tracker stuffs here]
				then(
						LifeTimeTracker.getInstance().getTrackingArgumentBuilder()
				).
				then(
						argument(entityTypeArg, string()).
						suggests((c, b) -> ISuggestionProvider.suggest(LifeTimeTracker.getInstance().getAvailableEntityType(), b)).
								// lifetime tracking creeper
								executes(c -> LifeTimeTracker.getInstance().printTrackingResultSpecific(
										c.getSource(), getString(c, entityTypeArg), null, false)
								).
								then(
										// lifetime tracking creeper realtime
										literal("realtime").
												executes(c -> LifeTimeTracker.getInstance().printTrackingResultSpecific(
														c.getSource(), getString(c, entityTypeArg), null, true)
												)
								).
								then(
										argument(detailModeArg, string()).
										suggests((c, b) -> ISuggestionProvider.suggest(SpecificDetailMode.getSuggestion(), b)).
												// lifetime tracking creeper spawning
												executes(c -> LifeTimeTracker.getInstance().printTrackingResultSpecific(
														c.getSource(),
														getString(c, entityTypeArg),
														getString(c, detailModeArg),
														false
												)).
												then(
														// lifetime tracking creeper spawning realtime
														literal("realtime").
																executes(c -> LifeTimeTracker.getInstance().printTrackingResultSpecific(
																		c.getSource(),
																		getString(c, entityTypeArg),
																		getString(c, detailModeArg),
																		true)
																)
												)
								)
				).
				then(
						literal("help").
								executes(c -> LifeTimeTracker.getInstance().showHelp(c.getSource()))
				);
		dispatcher.register(builder);
	}
}
