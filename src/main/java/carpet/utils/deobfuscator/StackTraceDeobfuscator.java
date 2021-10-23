package carpet.utils.deobfuscator;

import carpet.utils.Translator;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;

public class StackTraceDeobfuscator
{
	static final Translator translator = new Translator("util", "stack_trace");
	private static final String MAPPING_NAME = "MCP 1.13.2";

	public static StackTraceElement[] deobfuscateStackTrace(StackTraceElement[] stackTraceElements, String ignoreClassPath)
	{
		List<StackTraceElement> list = Lists.newArrayList();
		for (StackTraceElement element : stackTraceElements)
		{
			Optional<String> remappedClass = McpMapping.remapClass(element.getClassName());
			Optional<String> remappedMethod = McpMapping.remapMethod(element.getClassName(), element.getMethodName(), element.getLineNumber());
			StackTraceElement newElement = new StackTraceElement(
					remappedClass.orElseGet(element::getClassName),
					remappedMethod.orElseGet(element::getMethodName),
					remappedClass.map(StackTraceDeobfuscator::getFileName).orElseGet(element::getFileName),
					element.getLineNumber()
			);
			list.add(newElement);
			if (ignoreClassPath != null && newElement.getClassName().startsWith(ignoreClassPath))
			{
				list.clear();
			}
		}
		list.add(0, new StackTraceElement(translator.tr("Deobfuscated stack trace"), "", MAPPING_NAME, -1));
		return list.toArray(new StackTraceElement[0]);
	}

	public static StackTraceElement[] deobfuscateStackTrace(StackTraceElement[] stackTraceElements)
	{
		return deobfuscateStackTrace(stackTraceElements, null);
	}

	private static String getFileName(String className)
	{
		if (className.isEmpty())
		{
			return className;
		}
		return className.substring(className.lastIndexOf('.') + 1).split("\\$", 2)[0] + ".java";
	}
}
