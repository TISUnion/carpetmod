package carpet.commands.lifetime.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.registry.IRegistry;

public class LifeTimeTrackerUtil
{
	public static boolean isTrackedEntity(Entity entity)
	{
		return entity instanceof EntityCreature || entity instanceof EntityItem || entity instanceof EntityXPOrb;
	}

	public static String getEntityTypeDescriptor(EntityType<?> entityType)
	{
		return IRegistry.ENTITY_TYPE.getKey(entityType).getPath();
	}
}
