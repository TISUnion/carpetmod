package carpet.helpers;

import net.minecraft.entity.Entity;

import java.util.function.Predicate;

public class PushableEntityPredicateWrapper implements Predicate<Entity>
{
	private final Predicate<Entity> predicate;

	public PushableEntityPredicateWrapper(Predicate<Entity> predicate)
	{
		this.predicate = predicate;
	}

	@Override
	public boolean test(Entity entity)
	{
		return this.predicate.test(entity);
	}
}
