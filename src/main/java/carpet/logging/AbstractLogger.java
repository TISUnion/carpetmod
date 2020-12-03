package carpet.logging;

import carpet.utils.TranslatableBase;

public abstract class AbstractLogger extends TranslatableBase
{
	protected final static String MULTI_OPTION_SEP_REG = "[,. ]";

	public AbstractLogger(String name)
	{
		super("logger", name);
	}
}
