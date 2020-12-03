package carpet.commands.lifetime.removal;

import carpet.commands.lifetime.LifeTimeTracker;
import carpet.utils.ToTextAble;
import carpet.utils.TranslatableBase;

public abstract class RemovalReason extends TranslatableBase implements ToTextAble
{
	public RemovalReason()
	{
		super(LifeTimeTracker.getInstance().getTranslator().getTranslationPath(), "removal_reason");
	}
}
