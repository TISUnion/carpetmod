package carpet.logging.instantfall;

import carpet.logging.AbstractLogger;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

public class InstantFallLogger extends AbstractLogger
{
	public static final String NAME = "instantfall";
	private static final InstantFallLogger INSTANCE = new InstantFallLogger();

	public InstantFallLogger()
	{
		super(NAME);
	}

	public static InstantFallLogger getInstance()
	{
		return INSTANCE;
	}

	public void onInstantFallFlagFlipped(boolean currentFlag)
	{
		this.log(() -> new ITextComponent[]{advTr("flag_changed", "InstantFall flag has changed to %s", Messenger.bool(currentFlag))});
	}
}
