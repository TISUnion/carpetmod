package carpet.commands.lifetime.utils;

import carpet.commands.lifetime.LifeTimeTracker;
import carpet.utils.ToTextAble;
import carpet.utils.TranslationContext;

public abstract class AbstractReason extends TranslationContext implements ToTextAble
{
	public AbstractReason(String reasonType)
	{
		super(LifeTimeTracker.getInstance().getTranslator().getDerivedTranslator(reasonType));
	}
}
