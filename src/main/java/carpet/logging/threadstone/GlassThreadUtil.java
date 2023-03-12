package carpet.logging.threadstone;

import com.mojang.datafixers.util.Pair;
import org.apache.commons.lang3.ThreadUtils;

public class GlassThreadUtil
{
	private static boolean isGlassThread(Thread thread)
	{
		return thread.getName().startsWith("Downloader ");
	}

	public static Pair<Integer, Integer> getGlassThreadCount()
	{
		int runnable = 0;
		int total = 0;

		// don't use Thread.getAllStackTraces().keySet(), since that's very slow
		for (Thread thread : ThreadUtils.findThreads(GlassThreadUtil::isGlassThread))
		{
			total++;
			if (thread.getState() == Thread.State.RUNNABLE)
			{
				runnable++;
			}
		}
		return Pair.of(runnable, total);
	}

	public static boolean isOnGlassThread()
	{
		return isGlassThread(Thread.currentThread());
	}
}
