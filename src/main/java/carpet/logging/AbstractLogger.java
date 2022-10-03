package carpet.logging;

import carpet.CarpetServer;
import carpet.utils.TranslatableBase;
import com.google.common.base.Joiner;
import com.mojang.brigadier.StringReader;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractLogger extends TranslatableBase
{
	public final static String MULTI_OPTION_SEP_REG = "[,. ]";
	public final static String OPTION_SEP = ",";

	private final String name;

	public AbstractLogger(String name)
	{
		super("logger", name);
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	// Carpet Logging

	@Nullable
	public String getDefaultLoggingOption()
	{
		String[] suggested = this.getSuggestedLoggingOption();
		return suggested != null && suggested.length > 0 ? suggested[0] : null;
	}

	@Nullable
	public String[] getSuggestedLoggingOption()
	{
		return null;
	}

	public Logger createCarpetLogger()
	{
		return new Logger(
				this.getName(),
				wrapOption(this.getDefaultLoggingOption()),
				wrapOptions(this.getSuggestedLoggingOption())
		);
	}

	protected void actionWithLogger(Consumer<Logger> action)
	{
		Logger logger = LoggerRegistry.getLogger(this.getName());
		if (logger != null)
		{
			action.accept(logger);
		}
		else
		{
			CarpetServer.LOGGER.warn("Failed to get carpet logger {}", this.getName());
		}
	}

	public void log(Supplier<ITextComponent[]> messagePromise)
	{
		actionWithLogger(logger -> logger.log(messagePromise));
	}

	public void log(Logger.lMessage messagePromise)
	{
		actionWithLogger(logger -> logger.log(messagePromise));
	}

	public void log(Logger.lMessageIgnorePlayer messagePromise)
	{
		actionWithLogger(logger -> logger.log(messagePromise));
	}

	// Utils

	/**
	 * Fabric carpet 1.4.25+ (mc1.16+) uses {@code StringArgumentType.string()} as the option argument in the `/log` command
	 * So we might need to wrap our option with quotes if necessary
	 */
	protected static String wrapOption(@Nullable String option)
	{
		if (option == null)
		{
			return null;
		}
		boolean requiresQuotes = false;
		for (int i = 0; i < option.length(); i++)
		{
			if (!StringReader.isAllowedInUnquotedString(option.charAt(i)))
			{
				requiresQuotes = true;
				break;
			}
		}
		if (requiresQuotes)
		{
			option = "\"" + option.replace("\"", "\"\"") + "\"";
		}
		return option;
	}

	protected static String[] wrapOptions(@Nullable String... options)
	{
		if (options == null)
		{
			return null;
		}
		options = options.clone();
		for (int i = 0; i < options.length; i++)
		{
			options[i] = wrapOption(options[i]);
		}
		return options;
	}

	protected static String createCompoundOption(Iterable<String> options)
	{
		return Joiner.on(OPTION_SEP).join(options);
	}

	protected static String createCompoundOption(String... options)
	{
		return createCompoundOption(Arrays.asList(options));
	}
}
