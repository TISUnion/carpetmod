package carpet.commands.lifetime.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.registry.IRegistry;

import java.util.Optional;
import java.util.stream.Stream;

public class LifeTimeTrackerUtil
{
	public static boolean isTrackedEntity(Entity entity)
	{
		return entity instanceof EntityLiving || entity instanceof EntityItem || entity instanceof EntityXPOrb;
	}

	public static String getEntityTypeDescriptor(EntityType<?> entityType)
	{
		return IRegistry.ENTITY_TYPE.getKey(entityType).getPath();
	}

	public static Optional<EntityType<?>> getEntityTypeFromName(String name)
	{
		return IRegistry.ENTITY_TYPE.stream().filter(entityType -> getEntityTypeDescriptor(entityType).equals(name)).findFirst();
	}

	public static Stream<String> getEntityTypeDescriptorStream()
	{
		return IRegistry.ENTITY_TYPE.stream().map(LifeTimeTrackerUtil::getEntityTypeDescriptor);
	}
}
