package carpet.utils;

import org.apache.logging.log4j.core.LoggerContext;

/**
 * Fix from 1.18.1-rc3
 */
public class Log4j2JndiPatch
{
	private static final LoggerContext loggerContext = LoggerContext.getContext(false);

	public static void patch()
	{
		// doesn't work before log4j2 2.10
		// System.setProperty("log4j2.formatMsgNoLookups", "true");

		loggerContext.addPropertyChangeListener(event -> applyFix());
		applyFix();
	}

	private static void applyFix()
	{
		try
		{
			loggerContext.getConfiguration().getStrSubstitutor().setVariableResolver(null);
		}
		catch (Throwable throwable)
		{
			throwable.printStackTrace();
			throw new RuntimeException("JNDI patch failed");
		}
	}
}
