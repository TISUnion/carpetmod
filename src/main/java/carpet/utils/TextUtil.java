package carpet.utils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.state.IProperty;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.dimension.DimensionType;

import java.util.Objects;

public class TextUtil
{
	public static String tp(Vec3d pos) {return String.format("/tp %s %s %s", pos.x, pos.y, pos.z);}
	public static String tp(Vec3i pos) {return String.format("/tp %d %d %d", pos.getX(), pos.getY(), pos.getZ());}
	public static String tp(ChunkPos pos) {return String.format("/tp %d ~ %d", pos.x * 16 + 8, pos.z * 16 + 8);}
	public static String tp(Vec3d pos, DimensionType dimensionType) {return String.format("/execute in %s run", dimensionType) + tp(pos).replace('/', ' ');}
	public static String tp(Vec3i pos, DimensionType dimensionType) {return String.format("/execute in %s run", dimensionType) + tp(pos).replace('/', ' ');}
	public static String tp(ChunkPos pos, DimensionType dimensionType) {return String.format("/execute in %s run", dimensionType) + tp(pos).replace('/', ' ');}

	public static String tp(Entity entity)
	{
		if (entity instanceof EntityPlayer)
		{
			String name = ((EntityPlayer)entity).getGameProfile().getName();
			return String.format("/tp %s", name);
		}
		String uuid = entity.getUniqueID().toString();
		return String.format("/tp %s", uuid);
	}

	public static String coord(Vec3d pos) {return String.format("[%.1f, %.1f, %.1f]", pos.x, pos.y, pos.z);}
	public static String coord(Vec3i pos) {return String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());}
	public static String coord(ChunkPos pos) {return String.format("[%d, %d]", pos.x, pos.z);}

	public static String vector(Vec3d vec, int digits)
	{
		return String.format("(%s, %s, %s)", StringUtil.fractionDigit(vec.x, digits), StringUtil.fractionDigit(vec.y, digits), StringUtil.fractionDigit(vec.z, digits));
	}
	public static String vector(Vec3d vec) {return vector(vec, 2);}

	public static String block(Block block)
	{
		return Objects.requireNonNull(IRegistry.BLOCK.getKey(block)).toString();
	}

	public static String block(IBlockState blockState)
	{
		return BlockStateParser.toString(blockState, null);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> String property(IProperty<T> property, Object value)
	{
		return property.getName((T)value);
	}
}
