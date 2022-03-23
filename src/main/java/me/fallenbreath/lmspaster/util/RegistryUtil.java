package me.fallenbreath.lmspaster.util;

import me.fallenbreath.lmspaster.LitematicaServerPasterMod;
import net.minecraft.util.ResourceLocation;

public class RegistryUtil
{
	public static ResourceLocation id(String name)
	{
		return new ResourceLocation(LitematicaServerPasterMod.MOD_ID, name);
	}
}
