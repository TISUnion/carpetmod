package carpet.logging.phantom;

import carpet.logging.AbstractLogger;
import carpet.logging.LoggerRegistry;
import carpet.utils.CounterUtil;
import carpet.utils.Messenger;
import carpet.utils.StringUtil;
import carpet.utils.TextUtil;
import com.google.common.collect.Lists;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PhantomLogger extends AbstractLogger
{
	public static final String NAME = "phantom";
	private static final PhantomLogger INSTANCE = new PhantomLogger();
	private static final int PHANTOM_SPAWNING_TIME = 72000;
	private static final List<Integer> REMINDER_TICKS = Lists.newArrayList(PHANTOM_SPAWNING_TIME * 3 / 4, PHANTOM_SPAWNING_TIME);
	private static final ITextComponent PHANTOM_NAME = EntityType.PHANTOM.getName();

	private PhantomLogger()
	{
		super(NAME);
	}

	public static PhantomLogger getInstance()
	{
		return INSTANCE;
	}

	@Override
	public @Nullable String getDefaultLoggingOption()
	{
		return LoggingOption.DEFAULT.getName();
	}

	@Override
	public @Nullable String[] getSuggestedLoggingOption()
	{
		return LoggingOption.getSuggestions();
	}

	private ITextComponent pack(ITextComponent message)
	{
		String command = String.format("/log %s", this.getName());
		return Messenger.c(
				TextUtil.getFancyText(
						null,
						Messenger.c("g [", advTr("header", "Phantom Reminder"), "g ] "),
						Messenger.s(command),
						new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)
				),
				message
		);
	}

	public void onPhantomSpawn(EntityPlayer spawnerPlayer, int phantomAmount)
	{
		if (!LoggerRegistry.__phantom)
		{
			return;
		}
		this.log(option -> {
			if (LoggingOption.SPAWNING.isContainedIn(option))
			{
				return new ITextComponent[]{
						pack(advTr("summon", "%1$s summoned %2$sx %3$s", TextUtil.getEntityText("b", spawnerPlayer), phantomAmount, PHANTOM_NAME))
				};
			}
			return null;
		});
	}

	public void tick()
	{
		if (!LoggerRegistry.__phantom)
		{
			return;
		}

		this.log((option, player) -> {
			if (LoggingOption.REMINDER.isContainedIn(option))
			{
				StatisticsManagerServer serverStatHandler = ((EntityPlayerMP)player).getStats();
				int timeSinceRest = MathHelper.clamp(serverStatHandler.getValue(StatList.CUSTOM.get(StatList.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
				if (REMINDER_TICKS.contains(timeSinceRest))
				{
					int timeUntilSpawn = PHANTOM_SPAWNING_TIME - timeSinceRest;
					String sinceRest = StringUtil.fractionDigit(CounterUtil.tickToMinute(timeSinceRest), 0);
					String untilSpawn = StringUtil.fractionDigit(CounterUtil.tickToMinute(timeUntilSpawn), 0);
					return new ITextComponent[]{
							pack(advTr("reminder.time_since_rest", "You haven't slept for %1$s minutes", sinceRest)),
							pack(timeUntilSpawn != 0 ?
									advTr("reminder.regular", "%1$s might spawn after %2$s minutes", PHANTOM_NAME, untilSpawn) :
									advTr("reminder.now", "%1$s might spawn since now%s", PHANTOM_NAME)
							)
					};
				}
			}
			return null;
		});
	}

	public enum LoggingOption
	{
		SPAWNING,
		REMINDER;

		public static final LoggingOption DEFAULT = SPAWNING;

		public String getName()
		{
			return this.name().toLowerCase();
		}

		public static String[] getSuggestions()
		{
			List<String> suggestions = Lists.newArrayList();
			suggestions.addAll(Arrays.stream(values()).map(LoggingOption::getName).collect(Collectors.toList()));
			suggestions.add(createCompoundOption(SPAWNING.getName(), REMINDER.getName()));
			return suggestions.toArray(new String[0]);
		}

		public boolean isContainedIn(String option)
		{
			return Arrays.asList(option.split(MULTI_OPTION_SEP_REG)).contains(this.getName());
		}
	}
}
