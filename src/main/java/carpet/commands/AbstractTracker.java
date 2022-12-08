package carpet.commands;

import carpet.utils.GameUtil;
import carpet.utils.Messenger;
import carpet.utils.TranslationContext;
import carpet.utils.Translator;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import static net.minecraft.command.Commands.literal;

public abstract class AbstractTracker extends TranslationContext
{
	private static final Translator baseTranslator = new Translator("tracker");
	private final String name;
	private boolean tracking;
	private long startTick;
	private long startMillis;

	public AbstractTracker(String name)
	{
		super("tracker", name.toLowerCase());
		this.name = name;
	}

	/*
	 * ---------------------
	 *  tracker name things
	 * ---------------------
	 */

	public String getName()
	{
		return this.name;
	}

	public String getCommandPrefix()
	{
		return this.name.toLowerCase();
	}

	// Xxx
	public String getTranslatedName()
	{
		return this.tr("name", this.name);
	}

	// Xxx Tracker
	public String getTranslatedNameFull()
	{
		return String.format(baseTranslator.tr("tracker_name_full", "%s Tracker"), this.getTranslatedName());
	}

	/*
	 * -----------------------
	 *  status / info getters
	 * -----------------------
	 */

	public boolean isTracking()
	{
		return this.tracking;
	}

	public long getStartMillis()
	{
		return this.startMillis;
	}

	public long getStartTick()
	{
		return this.startTick;
	}

	/*
	 * ------------------------
	 *  for command executions
	 * ------------------------
	 */

	public int startTracking(CommandSource source, boolean showFeedback)
	{
		if (this.isTracking())
		{
			if (showFeedback)
			{
				Messenger.tell(source, Messenger.c(
						"r " + String.format(baseTranslator.tr("tracking_already_started", "%s is already running"), this.getTranslatedNameFull())
				));
			}
			return 1;
		}
		this.tracking = true;
		this.startTick = GameUtil.getGameTime();
		this.startMillis = System.currentTimeMillis();
		if (showFeedback)
		{
			Messenger.tell(source, Messenger.s(String.format(baseTranslator.tr("tracking_started", "%s started"), this.getTranslatedNameFull())), true);
		}
		this.initTracker();
		return 1;
	}

	public int stopTracking(CommandSource source, boolean showFeedback)
	{
		if (source != null)
		{
			if (this.isTracking())
			{
				this.reportTracking(source, false);
				if (showFeedback)
				{
					Messenger.tell(source, Messenger.s(" "));
					Messenger.tell(source, Messenger.s(String.format(baseTranslator.tr("tracking_stopped", "%s stopped"), this.getTranslatedNameFull())), true);
				}
			}
			else if (showFeedback)
			{
				Messenger.tell(source, Messenger.c(
						"r " + String.format(baseTranslator.tr("tracking_not_started", "%s has not started"), this.getTranslatedNameFull())
				));
			}
		}
		this.tracking = false;
		return 1;
	}

	public int restartTracking(CommandSource source)
	{
		boolean wasTracking = this.isTracking();
		this.stopTracking(source, false);
		this.startTracking(source, false);
		if (wasTracking)
		{
			source.sendFeedback(Messenger.s(" "), false);
		}
		Messenger.tell(source, Messenger.s(String.format(baseTranslator.tr("tracking_restarted", "%s restarted"), this.getTranslatedNameFull())), true);
		return 1;
	}

	protected int doWhenTracking(CommandSource source, Runnable runnable)
	{
		if (this.isTracking())
		{
			runnable.run();
		}
		else
		{
			Messenger.tell(source, Messenger.c(
					"r " + String.format(baseTranslator.tr("tracking_not_started", "%s has not started"), this.getTranslatedNameFull())
			));
		}
		return 1;
	}

	public int reportTracking(CommandSource source, boolean realtime)
	{
		return this.doWhenTracking(source, () -> this.printTrackingResult(source, realtime));
	}

	public LiteralArgumentBuilder<CommandSource> getTrackingArgumentBuilder()
	{
		return literal("tracking").
				executes(c -> this.reportTracking(c.getSource(), false)).
				then(literal("start").
						executes(c -> this.startTracking(c.getSource(), true))
				).
				then(literal("stop")
						.executes(c -> this.stopTracking(c.getSource(), true))
				).
				then(literal("restart").
						executes(c -> this.restartTracking(c.getSource()))
				).
				then(literal("realtime").
						executes(c -> this.reportTracking(c.getSource(), true))
				);
	}

	/*
	 * -------
	 *  Utils
	 * -------
	 */

	protected long getTrackedTick(boolean realtime)
	{
		return Math.max(1, realtime ? (System.currentTimeMillis() - this.getStartMillis()) / 50 : GameUtil.getGameTime() - this.getStartTick());
	}

	// send general header for tracking report and return the processed "ticks"
	protected long sendTrackedTime(CommandSource source, boolean realtime)
	{
		long ticks = this.getTrackedTick(realtime);
		source.sendFeedback(Messenger.c(
				"w  \n",
				"g ----------- ",
				"w " + this.getTranslatedNameFull(),
				"g  -----------\n",
				String.format(
						"w %s %.2f min (%s)",
						baseTranslator.tr("Tracked"),
						(double)ticks / (20 * 60),
						baseTranslator.tr(realtime ? "real time" : "in game")
				)
		), false);
		return ticks;
	}

	/*
	 * ------------
	 *  Interfaces
	 * ------------
	 */

	/**
	 * Stop tracking, call this when server stops
	 * e.g. inside {@link carpet.CarpetServer#onServerClosed}
	 */
	public void stop()
	{
		this.stopTracking(null, false);
	}

	/**
	 * Called when the tracker starts tracking
	 * Go initialize necessary statistics
	 */
	protected abstract void initTracker();

	/**
	 * Show tracking result to the command source
	 * @param realtime use real time or not. if not, use in-game time
	 */
	protected abstract void printTrackingResult(CommandSource source, boolean realtime);
}
