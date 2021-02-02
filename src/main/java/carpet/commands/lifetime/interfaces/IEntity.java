package carpet.commands.lifetime.interfaces;

import carpet.commands.lifetime.removal.RemovalReason;
import carpet.commands.lifetime.spawning.SpawningReason;
import net.minecraft.util.math.Vec3d;

public interface IEntity
{
	int getTrackId();

	long getLifeTime();

	Vec3d getSpawningPosition();

	Vec3d getRemovalPosition();

	void recordSpawning(SpawningReason reason);

	void recordRemoval(RemovalReason reason);
}
