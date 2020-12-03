package carpet.commands.lifetime.spawning;

import carpet.commands.lifetime.LifeTimeTracker;
import carpet.utils.ToTextAble;
import carpet.utils.TranslatableBase;

public abstract class SpawningReason extends TranslatableBase implements ToTextAble
{
	public SpawningReason()
	{
		super(LifeTimeTracker.getInstance().getTranslator().getTranslationPath(), "spawn_reason");
	}
}
