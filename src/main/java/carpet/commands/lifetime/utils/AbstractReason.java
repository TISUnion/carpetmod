package carpet.commands.lifetime.utils;

import carpet.commands.lifetime.LifeTimeTracker;
import carpet.utils.ToTextAble;
import carpet.utils.TranslatableBase;

public abstract class AbstractReason extends TranslatableBase implements ToTextAble
{
	public AbstractReason(String reasonType)
	{
		super(LifeTimeTracker.getInstance().getTranslator().getTranslationPath(), reasonType);
	}
}
