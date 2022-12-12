package carpet.logging.threadstone;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class GlassThreadUtil
{
	// Thread.getAllStackTraces().keySet()
	private static final Supplier<Thread[]> getThreadsSupplier = Util.make(() -> {
		try
		{
			Method method = Thread.class.getDeclaredMethod("getThreads");
			method.setAccessible(true);
			return () -> {
				try
				{
					return (Thread[])method.invoke(null);
				}
				catch (IllegalAccessException | InvocationTargetException e)
				{
					throw new RuntimeException(e);
				}
			};
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	});

	public static Pair<Integer, Integer> getGlassThreadCount()
	{
		int runnable = 0;
		int total = 0;
		for (Thread thread : getThreadsSupplier.get())
		{
			if (thread.getName().startsWith("Downloader "))
			{
				total++;
				if (thread.getState() == Thread.State.RUNNABLE)
				{
					runnable++;
				}
			}
		}
		return Pair.of(runnable, total);
	}
}
