package carpet.utils;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PreciseEntityPlacer
{
	public static final ThreadLocal<Vec3d> spawnEggTargetPos = ThreadLocal.withInitial(() -> null);

	public static void adjustEntity(Entity entity, Vec3d targetPos)
	{
		entity.setLocationAndAngles(targetPos.x, targetPos.y, targetPos.z, entity.rotationYaw, entity.rotationPitch);
	}
	public static void adjustEntity(Entity entity, ItemUseContext context)
	{
		adjustEntity(entity, vec3dFromContext(context));
	}

	/**
	 * The spawnEggTargetPos should be set in SpawnEggItemMixin in advanced
	 */
	public static void adjustEntityFromSpawnEgg(Entity entity)
	{
		Vec3d vec3d = spawnEggTargetPos.get();
		if (vec3d != null)
		{
			adjustEntity(entity, vec3d);
			spawnEggTargetPos.remove();
		}
	}

	public static Vec3d vec3dFromContext(ItemUseContext context)
	{
		BlockPos pos = context.getPos();
		return new Vec3d(pos.getX() + context.getHitX(), pos.getY() + context.getHitY(), pos.getZ() + context.getHitZ());
	}
}
