package carpet.commands;

import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class SleepCommand extends AbstractCommand
{
	private static final String NAME = "sleep";
	private static final SleepCommand INSTANCE = new SleepCommand();

	private SleepCommand()
	{
		super(NAME);
	}

	public static SleepCommand getInstance()
	{
		return INSTANCE;
	}

	@Override
	public void registerCommand(CommandDispatcher<CommandSource> dispatcher)
	{
		RequiredArgumentBuilder<CommandSource, Integer> durationNode = argument("duration", integer(0, 60_000));
		for (TimeUnit timeUnit : TimeUnit.values())
		{
			durationNode.then(literal(timeUnit.name).
					executes(c -> doLag(c.getSource(), timeUnit, getInteger(c, "duration")))
			);
		}

		dispatcher.register(
				literal(NAME).
				requires(s -> SettingsManager.canUseCommand(s, CarpetSettings.commandSleep)).
				executes(c -> showHelp(c.getSource())).
				then(durationNode)
		);
	}

	private int showHelp(CommandSource source)
	{
		ITextComponent usage = Messenger.c(
				"w /" + NAME,
				"g  <", "w duration", "g > (",
				Messenger.join(
						Messenger.s("|", TextFormatting.GRAY),
						Arrays.stream(TimeUnit.values()).
								map(tu -> Messenger.s(tu.name)).
								toArray(ITextComponent[]::new)
				),
				"g )"
		);
		Messenger.tell(source, Messenger.s("Immediately Block the current thread for given duration, can be used to create lag"));
		Messenger.tell(source, Messenger.format("Usage: %1$s", usage));
		Messenger.tell(source, Messenger.c(
				Messenger.s("Warning", TextFormatting.BOLD),
				Messenger.s(": Durations greater than 15s might disconnect clients or even disturb server watch dog")
		));
		return 0;
	}

	private int doLag(CommandSource source, TimeUnit timeUnit, int duration)
	{
		try
		{
			timeUnit.action.sleep(duration);
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
		return 0;
	}

	private enum TimeUnit
	{
		SECOND("s", duration -> Thread.sleep(duration * 1000L)),
		MILLI_SECOND("ms", duration -> Thread.sleep(duration)),
		MICRO_SECOND("us", duration -> Thread.sleep(duration / 1000, (int)(duration % 1000)));

		public final String name;
		public final SleepAction action;

		TimeUnit(String name, SleepAction action)
		{
			this.name = name;
			this.action = action;
		}

		@FunctionalInterface
		interface SleepAction
		{
			void sleep(long duration) throws InterruptedException;
		}
	}
}
