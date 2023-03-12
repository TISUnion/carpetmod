package carpet.logging.threadstone;

import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.function.BiFunction;

public class GlassThreadStatistic
{
	private static final GlassThreadStatistic INSTANCE = new GlassThreadStatistic();
	private static final int[] MAX_RATIO = new int[]{50, 10, 1};
	private static final int MAX_RECORD = 10000;
	
	private final Deque<Long> lifespans = Lists.newLinkedList();
	private final Object lock = new Object();

	private volatile boolean dirty = true;
	private ITextComponent cachedMessage = null;

	public static GlassThreadStatistic getInstance()
	{
		return INSTANCE;
	}

	public void reset()
	{
		synchronized (this.lock)
		{
			this.lifespans.clear();
		}
		this.dirty = true;
	}

	public void onGlassThreadTerminated(long lifespanNs)
	{
		synchronized (this.lock)
		{
			this.lifespans.addLast(lifespanNs);
			if (this.lifespans.size() > MAX_RECORD)
			{
				this.lifespans.removeFirst();
			}
		}
		this.dirty = true;
	}

	public ITextComponent report()
	{
		if (!this.dirty)
		{
			return this.cachedMessage;
		}

		LongList lifespans = new LongArrayList();
		synchronized (this.lock)
		{
			lifespans.addAll(this.lifespans);
		}

		int n = lifespans.size();
		BiFunction<String, String, ITextComponent> builder = (a, b) -> Messenger.c(Messenger.s(a), Messenger.s(b, TextFormatting.GRAY));
		ITextComponent message;
		if (n > 0)
		{
			lifespans.sort(Comparator.reverseOrder());

			List<ITextComponent> items = Lists.newArrayList();
			items.add(builder.apply(String.format("GS: %d", n), "x"));
			items.add(builder.apply(String.format("A: %.0f", Arrays.stream(lifespans.toLongArray()).average().orElse(0) / 1000), "us"));
			for (int r : MAX_RATIO)
			{
				int i = (int)Math.round((n - 1) * (r / 100.0));
				items.add(builder.apply(String.format("%d%%: %d", r, lifespans.getLong(i) / 1000), "us"));
			}

			message = Messenger.join(Messenger.s(", ", TextFormatting.GRAY), items);
		}
		else
		{
			message = builder.apply("Glass stats: ", "N/A");
		}

		this.cachedMessage = message;
		this.dirty = false;
		return message;
	}
}
