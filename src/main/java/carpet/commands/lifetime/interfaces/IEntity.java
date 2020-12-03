package carpet.commands.lifetime.interfaces;

import carpet.commands.lifetime.removal.RemovalReason;
import carpet.commands.lifetime.spawning.SpawningReason;
import net.minecraft.util.math.Vec3d;

public interface IEntity
{
	long getLifeTime();

	Vec3d getSpawnPosition();

	void recordSpawning(SpawningReason reason);

	void recordRemoval(RemovalReason reason);
}
