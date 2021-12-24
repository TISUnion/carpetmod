package redstone.multimeter.common.meter;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import redstone.multimeter.common.DimPos;

public abstract class MeterPropertiesManager {
	
	public boolean validate(MeterProperties properties) {
		DimPos pos = properties.getPos();
		
		if (pos == null) {
			return false;
		}
		
		World world = getWorldOf(pos);
		
		if (world == null) {
			return false;
		}
		
		postValidation(properties, world, pos.getBlockPos());
		
		return true;
	}
	
	protected abstract World getWorldOf(DimPos pos);
	
	protected abstract void postValidation(MeterProperties properties, World world, BlockPos pos);
	
}
