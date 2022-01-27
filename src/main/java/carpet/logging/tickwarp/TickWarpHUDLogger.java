package carpet.logging.tickwarp;

import carpet.logging.AbstractHUDLogger;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class TickWarpHUDLogger extends AbstractHUDLogger
{
	public static final String NAME = "tickWarp";
	private static final TickWarpHUDLogger INSTANCE = new TickWarpHUDLogger();

	private TickWarpInfo info = new TickWarpInfo();
	private final MemorizedTickWarpInfo historyInfo = new MemorizedTickWarpInfo();

	private TickWarpHUDLogger()
	{
		super(NAME);
	}

	public static TickWarpHUDLogger getInstance()
	{
		return INSTANCE;
	}

	private long getTotalTicks()
	{
		return this.info.getTotalTicks();
	}

	private long getRemainingTicks()
	{
		return this.info.getRemainingTicks();
	}

	private long getCompletedTicks()
	{
		return this.getTotalTicks() - this.getRemainingTicks();
	}

	private long getCurrentTime()
	{
		return this.info.getCurrentTime();
	}

	private long getStartTime()
	{
		return this.info.getStartTime();
	}

	private EntityPlayer getTimeAdvancer()
	{
		return this.info.getTimeAdvancer();
	}

	private double getAverageMSPT()
	{
		double milliSeconds = Math.max(this.getCurrentTime() - this.getStartTime(), 1) / 1e6;
		return milliSeconds / this.getCompletedTicks();
	}

	private boolean isWarping()
	{
		return this.info.isWarping();
	}

	private double getAverageTPS()
	{
		double secondPerTick = this.getAverageMSPT() / 1e3;
		return 1.0 / secondPerTick;
	}

	private ITextComponent getSourceName()
	{
		EntityPlayer advancer = this.getTimeAdvancer();
		return advancer != null ? advancer.getName() : Messenger.s(this.tr("Server"));
	}

	private double getProgressRate()
	{
		return (double)this.getCompletedTicks() / Math.max(this.getTotalTicks(), 1);
	}

	private ITextComponent getProgressBar()
	{
		double progressRate = this.getProgressRate();
		List<Object> list = Lists.newArrayList();
		list.add("g [");
		for (int i = 1; i <= 10; i++)
		{
			list.add(progressRate >= i / 10.0D ? "g #" : "f -");
		}
		list.add("g ]");
		return Messenger.c(list.toArray(new Object[0]));
	}

	private ITextComponent getDurationRatio()
	{
		return Messenger.c(
				String.format("g %d", this.getCompletedTicks()),
				"f /",
				String.format("g %d", this.getTotalTicks())
		);
	}

	private ITextComponent getProgressPercentage()
	{
		return Messenger.c(String.format("g %.1f%%", this.getProgressRate() * 100));
	}

	@Override
	public ITextComponent[] onHudUpdate(String option, EntityPlayer playerEntity)
	{
		if (!this.isWarping())
		{
			return null;
		}
		List<Object> list = Lists.newArrayList();
		list.add("g Warp ");
		if (option.equals("bar"))  // progress bar
		{
			list.add(this.getProgressBar());
		}
		else if (option.equals("value"))  // regular value
		{
			list.add(this.getDurationRatio());
		}
		else  // fallback
		{
			list.add(this.getProgressBar());
		}
		list.add("w  ");
		list.add(this.getProgressPercentage());
		return new ITextComponent[]{Messenger.c(list.toArray(new Object[0]))};
	}

	private void addLine(List<ITextComponent> list, String info, Object data)
	{
		list.add(Messenger.c(String.format("w %s", info), "g : ", data));
	}

	private ITextComponent getTimeInfo(long ticks)
	{
		return this.advTr(
				"time_info", "%1$smin (in game) / %2$smin (real time)",
				String.format("%.2f", ticks / 20.0D / 60.0D),
				String.format("%.2f", ticks / this.getAverageTPS() / 60.0D)
		);
	}

	private synchronized void generateTickWarpInfo(List<ITextComponent> result, TickWarpInfo specifiedInfo)
	{
		TickWarpInfo infoBackup = this.info;
		try
		{
			this.info = specifiedInfo;
			result.add(Messenger.s(" "));
			this.addLine(result, this.tr("Starter"), this.getSourceName());
			this.addLine(result, this.tr("Average TPS"), String.format("w %.2f", this.getAverageTPS()));
			this.addLine(result, this.tr("Average MSPT"), String.format("w %.2f", this.getAverageMSPT()));
			this.addLine(result, this.tr("elapsed_time", "Time elapsed"), this.getTimeInfo(this.getCompletedTicks()));
			this.addLine(result, this.tr("estimated_time", "Estimated remaining time"), this.getTimeInfo(this.getRemainingTicks()));
			result.add(Messenger.c(
					this.getProgressBar(),
					"w  ",
					this.getProgressPercentage(),
					"w  ",
					this.getDurationRatio()
			));
		}
		finally
		{
			this.info = infoBackup;
		}
	}

	public int showTickWarpInfo(CommandSource source)
	{
		List<ITextComponent> result = Lists.newArrayList();
		if (this.isWarping())
		{
			this.generateTickWarpInfo(result, this.info);
		}
		else if (this.historyInfo.hasData())
		{
			result.add(Messenger.s(String.format(this.tr("show_history_header", "Last tick warp result (%.2fmin ago)"), (System.nanoTime() - this.historyInfo.getLastRecordingTime()) / 1e9 / 60.0D)));
			this.generateTickWarpInfo(result, this.historyInfo);
		}
		else
		{
			result.add(Messenger.s(this.tr("not_started", "Tick warp has not started")));
		}
		Messenger.send(source, result);
		return 1;
	}

	public void recordTickWarpResult()
	{
		this.historyInfo.recordResultIfsuitable();
	}

	public void recordTickWarpAdvancer()
	{
		this.historyInfo.recordTickWarpAdvancer();
	}
}
