package carpet.commands;

import carpet.CarpetServer;
import carpet.logging.threadstone.GlassThreadStatistic;
import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.command.CommandSource;
import net.minecraft.init.Blocks;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class ThreadstoneCommand extends AbstractCommand
{
	private static final ThreadstoneCommand INSTANCE = new ThreadstoneCommand();
	private static final String NAME = "threadstone";

	private static final ThreadPoolExecutor REDSTONE_FLAG_RECORDER_THREAD_POOL = new ThreadPoolExecutor(
			1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setNameFormat(RedstoneFlagRecorder.class.getSimpleName() + "%d").build()
	);

	@Nullable
	private RedstoneFlagRecorder redstoneFlagRecorder;

	private ThreadstoneCommand()
	{
		super(NAME);
	}

	public static ThreadstoneCommand getInstance()
	{
		return INSTANCE;
	}

	@Override
	public void registerCommand(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralCommandNode<CommandSource> root = dispatcher.register(literal(NAME).
				requires(s -> SettingsManager.canUseCommand(s, CarpetSettings.commandThreadstone)).
				then(literal("logger").
						then(literal("reset").
								executes(c -> resetThreadstoneLogger(c.getSource(), ResetTarget.values())).
								then(literal("glassthread").
										executes(c -> resetThreadstoneLogger(c.getSource(), new ResetTarget[]{ResetTarget.GLASS_THREAD}))
								).
								then(literal("chunkloadcache").
										executes(c -> resetThreadstoneLogger(c.getSource(), new ResetTarget[]{ResetTarget.CHUNK_LOAD_CACHE}))
								)
						)
				).
				then(literal("rsf").
						executes(c -> showRedstoneFlagHelp(c.getSource())).
						then(literal("get").
								executes(c -> getRedstoneFlag(c.getSource()))
						).
						then(literal("record").
								then(literal("start").
										executes(c -> startRedstoneFlagRecord(c.getSource(), 1000000, 0)).
										then(argument("amount", integer(0, 2_000_000_000)).
												executes(c -> startRedstoneFlagRecord(c.getSource(), getInteger(c, "amount"), 0)).
												then(argument("interval_us", integer(-1, 10_000_000)).
														executes(c -> startRedstoneFlagRecord(c.getSource(), getInteger(c, "amount"), getInteger(c, "interval_us")))
												)
										)
								).
								then(literal("stop").
										executes(c -> stopRedstoneFlagRecord(c.getSource()))
								).
								then(literal("status").
										executes(c -> displayRedstoneFlagRecordStatus(c.getSource()))
								)
						)
				));
		dispatcher.register(
				literal("ts").
				requires(s -> SettingsManager.canUseCommand(s, CarpetSettings.commandThreadstone)).
				redirect(root)
		);
	}

	//////////////////////////////////////////////////
	//          Threadstone Logger control          //
	//////////////////////////////////////////////////

	private int resetThreadstoneLogger(CommandSource source, ResetTarget[] targets)
	{
		for (ResetTarget target : targets)
		{
			target.trigger(source);
		}
		return targets.length;
	}

	private enum ResetTarget
	{
		GLASS_THREAD(source -> {
			GlassThreadStatistic.getInstance().reset();
			Messenger.tell(source, "Reset glass thread statistic");
		}),
		CHUNK_LOAD_CACHE(source -> {
			for (WorldServer world : CarpetServer.minecraft_server.getWorlds())
			{
				world.getChunkProvider().chunkLoadingCacheStatistic.reset();
			}
			Messenger.tell(source, "Reset chunk loading cache statistic");
		});

		private final Consumer<CommandSource> impl;

		ResetTarget(Consumer<CommandSource> impl)
		{
			this.impl = impl;
		}

		public void trigger(CommandSource source)
		{
			this.impl.accept(source);
		}
	}

	//////////////////////////////////////////////////
	//             Redstone Flag stuffs             //
	//////////////////////////////////////////////////

	private int showRedstoneFlagHelp(CommandSource source)
	{
		String prefix = "/ts rsf";
		Messenger.tell(source, "======= Threadstone Command =======");
		Messenger.tell(source, "Available prefixes: /threadstone /ts");
		Messenger.tell(source, prefix + ": show help");
		Messenger.tell(source, prefix + " get: show current redstone flag value");
		Messenger.tell(source, prefix + " record [start|stop|status]: start/stop/show status of the redstone flag value recorder");
		Messenger.tell(source, prefix + " record start [<amount>] [<interval_us>]:");
		Messenger.tell(source, "  <amount>: record amount. Default 10^7");
		Messenger.tell(source, "  <interval_us>: Thread.sleep interval between each record operation, in us (micro-second). interval_us=-1 means no Thread.sleep call. Default 0");
		return 1;
	}

	private int displayRedstoneFlagRecordStatus(CommandSource source)
	{
		if (this.redstoneFlagRecorder == null)
		{
			Messenger.tell(source, "Redstone flag record not started yet");
			return 0;
		}

		this.redstoneFlagRecorder.showStatus(source);
		return 1;
	}

	private int stopRedstoneFlagRecord(CommandSource source)
	{
		if (this.redstoneFlagRecorder == null)
		{
			Messenger.tell(source, "Redstone flag record not started yet");
			return 0;
		}

		this.redstoneFlagRecorder.interrupt();
		return 1;
	}

	private void onRedstoneFlagRecordFinished()
	{
		this.redstoneFlagRecorder = null;
	}

	private int startRedstoneFlagRecord(CommandSource source, int amount, int intervalUs)
	{
		if (this.redstoneFlagRecorder != null)
		{
			String cmd = "/ts rsf record stop";
			Messenger.tell(source, Messenger.format(
					"Already recording, use %s to stop",
					Messenger.fancy(
							Messenger.s(cmd, TextFormatting.ITALIC),
							Messenger.s(cmd),
							new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd)
					)
			));
			return 0;
		}

		this.redstoneFlagRecorder = new RedstoneFlagRecorder(source, amount, intervalUs);
		this.redstoneFlagRecorder.start();

		Messenger.tell(source, "Redstone flag recorder started");
		return 1;
	}

	private int getRedstoneFlag(CommandSource source)
	{
		BlockRedstoneWire redstoneWire = (BlockRedstoneWire)Blocks.REDSTONE_WIRE.getDefaultState().getBlock();
		boolean flag = redstoneWire.getCanProvidePower();
		Messenger.tell(source, Messenger.format("Redstone flag = %s", Messenger.bool(flag)));
		return flag ? 1 : 0;
	}

	private class RedstoneFlagRecorder implements Runnable
	{
		private final CommandSource source;
		private final int amount;
		private final long intervalUs;
		private final long startMili;
		private volatile boolean interrupted;
		private final AtomicInteger i;

		private RedstoneFlagRecorder(CommandSource source, int amount, long intervalUs)
		{
			this.source = source;
			this.amount = amount;
			this.intervalUs = intervalUs;
			this.startMili = System.currentTimeMillis();
			this.interrupted = false;
			this.i = new AtomicInteger();
		}

		private void start()
		{
			REDSTONE_FLAG_RECORDER_THREAD_POOL.execute(this);
		}

		private void interrupt()
		{
			this.interrupted = true;
		}

		@Override
		public void run()
		{
			BlockRedstoneWire redstoneWire = (BlockRedstoneWire)Blocks.REDSTONE_WIRE.getDefaultState().getBlock();
			int[] counter = new int[2];

			// record
			for (this.i.set(0); this.i.get() < this.amount; this.i.getAndIncrement())
			{
				if (this.intervalUs >= 0)
				{
					try
					{
						//noinspection BusyWait
						Thread.sleep(this.intervalUs / 1000, (int)(this.intervalUs % 1000 * 1000));
					}
					catch (InterruptedException e)
					{
						CarpetServer.LOGGER.error("Interrupted", e);
					}
				}
				if (this.interrupted)
				{
					break;
				}

				int index = redstoneWire.getCanProvidePower() ? 1 : 0;
				counter[index]++;
			}

			// report
			CarpetServer.minecraft_server.addScheduledTask(() -> {
				int total = counter[0] + counter[1];
				Messenger.tell(this.source, "Redstone flag recorder finished");
				Messenger.tell(this.source, String.format("  false: %d %.2f%%", counter[0], 100.0 * counter[0] / total));
				Messenger.tell(this.source, String.format("  true : %d %.2f%%", counter[1], 100.0 * counter[1] / total));
				this.showStatus(this.source);

				ThreadstoneCommand.this.onRedstoneFlagRecordFinished();
			});
		}

		public void showStatus(CommandSource source)
		{
			int i = this.i.get();
			int n = this.amount;
			long now = System.currentTimeMillis();
			Messenger.tell(source, String.format("Progress rate: %d/%d %.1f%%", i, n, 100.0 * i / n));
			Messenger.tell(source, String.format("Time elapsed: %.2fs", (now - this.startMili) / 1E3));
		}
	}

	//////////////////////////////////////////////////
	//           Redstone Flag stuffs ends          //
	//////////////////////////////////////////////////
}
