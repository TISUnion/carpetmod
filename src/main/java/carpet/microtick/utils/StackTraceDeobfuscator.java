package carpet.microtick.utils;

import com.google.common.collect.Maps;

import java.util.Map;

public class StackTraceDeobfuscator
{
	private static final String MAPPING_FILE_NAME = "yarn-1.15.2+build.17-v2.tiny";
	private static final String IGNORE_CLASS_PATH = "carpet.microtick";
	private static final Map<String, String> mappings = Maps.newHashMap();

	public static StackTraceElement[] deobfuscateStackTrace(StackTraceElement[] stackTraceElements)
	{
		return stackTraceElements;
	}
}
