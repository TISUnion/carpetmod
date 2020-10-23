package carpet.microtick.utils;

import com.google.common.collect.Lists;

import java.util.List;

public class StackTraceDeobfuscator
{
	private static final String IGNORE_CLASS_PATH = "carpet.microtick";

	public static StackTraceElement[] deobfuscateStackTrace(StackTraceElement[] stackTraceElements)
	{
		List<StackTraceElement> list = Lists.newArrayList();
		for (StackTraceElement element : stackTraceElements)
		{
			list.add(element);
			if (element.getClassName().startsWith(IGNORE_CLASS_PATH))
			{
				list.clear();
			}
		}
		list.add(0, new StackTraceElement("Stack trace", "", null, -1));
		return list.toArray(new StackTraceElement[0]);
	}
}
