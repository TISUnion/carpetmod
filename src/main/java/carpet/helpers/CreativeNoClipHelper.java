package carpet.helpers;

import carpet.settings.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class CreativeNoClipHelper
{
	public static boolean canEntityIgnoreClip(Entity entity)
	{
		if (CarpetSettings.creativeNoClip && entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)entity;
			return player.isCreative() && player.abilities.isFlying;
		}
		return false;
	}
}
