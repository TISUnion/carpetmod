package carpet.helpers;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class UpdateSuppressionSimulator
{
	private static final Map<String, Runnable> SUPPORTED_ERRORS = ImmutableMap.of(
			"StackOverflowError", () -> {
				throw new StackOverflowError("TISCM UpdateSuppressionSimulator");
			},
			"OutOfMemoryError", () -> {
				throw new OutOfMemoryError("TISCM UpdateSuppressionSimulator");
			},
			"ClassCastException", () -> {
				throw new ClassCastException("TISCM UpdateSuppressionSimulator");
			}
	);

	private static final Runnable DUMMY = () -> {};
	private static Runnable nuke = DUMMY;

	public static boolean isActivated()
	{
		return nuke != DUMMY;
	}

	public static void kaboom()
	{
		nuke.run();
	}

	public static boolean tryAcceptRule(String ruleValue)
	{
		switch (ruleValue)
		{
			case "true":
				nuke = SUPPORTED_ERRORS.values().iterator().next();
				break;
			case "false":
				nuke = DUMMY;
				break;
			default:
				boolean[] ok = {false};
				SUPPORTED_ERRORS.forEach((key, value) -> {
					if (key.equalsIgnoreCase(ruleValue))
					{
						nuke = value;
						ok[0] = true;
					}
				});
				if (!ok[0])
				{
					return false;
				}
				break;
		}
		return true;
	}
}
