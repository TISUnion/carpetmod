package carpet.microtiming.utils.stacktrace;

import com.google.common.collect.Lists;

import java.util.List;

public class StackTraceDeobfuscator
{
	// TODO: proper deobfuscation for 1.13.2
	public static StackTraceElement[] deobfuscateStackTrace(StackTraceElement[] stackTraceElements, String ignoreClassPath)
	{
		List<StackTraceElement> list = Lists.newArrayList();
		for (StackTraceElement element : stackTraceElements)
		{
			list.add(element);
			if (element.getClassName().startsWith(ignoreClassPath))
			{
				list.clear();
			}
		}
		list.add(0, new StackTraceElement("Stack trace", "", null, -1));
		return list.toArray(new StackTraceElement[0]);
	}

	public static StackTraceElement[] deobfuscateStackTrace(StackTraceElement[] stackTraceElements)
	{
		return deobfuscateStackTrace(stackTraceElements, null);
	}
}
