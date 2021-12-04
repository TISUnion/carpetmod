package redstone.multimeter.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public interface MeterableBlock extends Meterable {
	
	default void logPowered(World world, BlockPos pos, boolean powered) {
		if (!world.isRemote()) {
			((WorldServer)world).getMultimeter().logPowered(world, pos, powered);
		}
	}
}
