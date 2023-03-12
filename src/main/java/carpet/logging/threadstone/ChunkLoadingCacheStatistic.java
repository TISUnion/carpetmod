package carpet.logging.threadstone;

import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

import java.util.concurrent.atomic.AtomicLong;

public class ChunkLoadingCacheStatistic
{
	private final AtomicLong missCount = new AtomicLong();
	private final AtomicLong hitCount = new AtomicLong();

	public void reset()
	{
		this.missCount.set(0);
		this.hitCount.set(0);
	}

	public void onLRUMissed()
	{
		if (GlassThreadUtil.isOnGlassThread())
		{
			this.missCount.incrementAndGet();
		}
	}

	public void onLRUHit()
	{
		if (GlassThreadUtil.isOnGlassThread())
		{
			this.hitCount.incrementAndGet();
		}
	}

	public ITextComponent report()
	{
		long miss = this.missCount.get();
		long hit = this.hitCount.get();
		long total = miss + hit;

		ITextComponent message = Messenger.s(String.format("CL-LRU M/H: %d/%d", miss, hit));
		if (total > 0)
		{
			message.appendText(String.format(" %.1f%%", 100.0 * miss / total));
		}
		return message;
	}
}
