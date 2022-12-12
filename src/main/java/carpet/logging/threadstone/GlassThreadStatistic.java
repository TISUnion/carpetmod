package carpet.logging.threadstone;

import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.*;

public class GlassThreadStatistic
{
	private static final GlassThreadStatistic INSTANCE = new GlassThreadStatistic();
	private static final int[] MAX_RATIO = new int[]{50, 10, 1};
	private static final int MAX_RECORD = 10000;
	
	private int creationCount = 0;
	private final Deque<Long> lifespans = Lists.newLinkedList();
	private final Object lock = new Object();

	private boolean dirty = true;
	private ITextComponent cachedMessage = null;

	public static GlassThreadStatistic getInstance()
	{
		return INSTANCE;
	}

	public void reset()
	{
		synchronized (this.lock)
		{
			this.creationCount = 0;
			this.lifespans.clear();
			this.dirty = true;
		}
	}

	public void onGlassThreadCreated(BlockPos glassPos)
	{
		synchronized (this.lock)
		{
			this.creationCount++;
			this.dirty = true;
		}
	}

	public void onGlassThreadTerminated(BlockPos glassPos, long lifespanNs)
	{
		synchronized (this.lock)
		{
			this.lifespans.addLast(lifespanNs);
			if (this.lifespans.size() > MAX_RECORD)
			{
				this.lifespans.removeFirst();
				this.creationCount--;
			}
			this.dirty = true;
		}
	}

	public ITextComponent report()
	{
		if (!this.dirty)
		{
			return this.cachedMessage;
		}

		int c;
		int t;
		List<Long> lifespans = Lists.newArrayList();

		synchronized (this.lock)
		{
			c = this.creationCount;
			lifespans.addAll(this.lifespans);
			t = lifespans.size();
		}

		ITextComponent message = Messenger.format("C/T: %s/%s", c, t);
		if (t > 0)
		{
			lifespans.sort(Comparator.reverseOrder());
			List<ITextComponent> mins = Lists.newArrayList();
			for (int r : MAX_RATIO)
			{
				int i = (int)Math.round((t - 1) * (r / 100.0));
				mins.add(Messenger.s(String.format("%d%%: %dus", r, lifespans.get(i) / 1000)));
			}
			message.appendSibling(Messenger.s(" | ", TextFormatting.DARK_GRAY));
			message.appendSibling(Messenger.join(Messenger.s(", ", TextFormatting.GRAY), mins));
			message.appendSibling(Messenger.s(" (TOP%)", TextFormatting.GRAY));
		}

		this.cachedMessage = message;
		synchronized (this.lock)
		{
			this.dirty = false;
		}
		return message;
	}
}
