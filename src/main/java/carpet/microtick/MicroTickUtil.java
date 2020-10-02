package carpet.microtick;

import carpet.CarpetServer;
import carpet.logging.LoggerRegistry;
import carpet.microtick.enums.PistonBlockEventType;
import carpet.settings.CarpetSettings;
import carpet.utils.Messenger;
import carpet.utils.WoolTool;
import com.google.common.collect.Maps;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;

public class MicroTickUtil
{
	private static final Map<DimensionType, ITextComponent> DIMENSION_NAME = Maps.newHashMap();
	private static final Map<EnumDyeColor, String> COLOR_STYLE = Maps.newHashMap();
	static
	{
		DIMENSION_NAME.put(DimensionType.OVERWORLD, new TextComponentTranslation("createWorld.customize.preset.overworld"));
		DIMENSION_NAME.put(DimensionType.NETHER, new TextComponentTranslation("advancements.nether.root.title"));
		DIMENSION_NAME.put(DimensionType.THE_END, new TextComponentTranslation("advancements.end.root.title"));

		COLOR_STYLE.put(EnumDyeColor.WHITE, "w");
		COLOR_STYLE.put(EnumDyeColor.ORANGE, "d");
		COLOR_STYLE.put(EnumDyeColor.MAGENTA, "m");
		COLOR_STYLE.put(EnumDyeColor.LIGHT_BLUE, "c");
		COLOR_STYLE.put(EnumDyeColor.YELLOW, "y");
		COLOR_STYLE.put(EnumDyeColor.LIME, "l");
		COLOR_STYLE.put(EnumDyeColor.PINK, "r");
		COLOR_STYLE.put(EnumDyeColor.GRAY, "f");
		COLOR_STYLE.put(EnumDyeColor.LIGHT_GRAY, "g");
		COLOR_STYLE.put(EnumDyeColor.CYAN, "q");
		COLOR_STYLE.put(EnumDyeColor.PURPLE, "p");
		COLOR_STYLE.put(EnumDyeColor.BLUE, "b");
		COLOR_STYLE.put(EnumDyeColor.BROWN, "n");
		COLOR_STYLE.put(EnumDyeColor.GREEN, "e");
		COLOR_STYLE.put(EnumDyeColor.RED, "r");
		COLOR_STYLE.put(EnumDyeColor.BLACK, "k");
	}

	public static ITextComponent getDimensionNameText(DimensionType dim)
	{
		return DIMENSION_NAME.getOrDefault(dim, null);
	}
	public static ITextComponent getDimensionNameText(int dimensionId)
	{
		return getDimensionNameText(DimensionType.getById(dimensionId));
	}

	static String getColorStyle(EnumDyeColor color)
	{
		return COLOR_STYLE.getOrDefault(color, "w");
	}
	static String getBooleanColor(boolean bool)
	{
		return bool ? "e" : "r";
	}

	static EnumDyeColor getWoolColor(World world, BlockPos pos)
	{
		if (!MicroTickLoggerManager.isLoggerActivated())
		{
			return null;
		}
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		BlockPos woolPos = pos;

		if (block == Blocks.OBSERVER || block == Blocks.END_ROD ||
				block instanceof BlockPistonBase || block instanceof BlockPistonMoving)
		{
			woolPos = pos.offset(state.get(BlockStateProperties.FACING).getOpposite());
		}
		else if (block instanceof BlockButton || block instanceof BlockLever)
		{
			EnumFacing facing;
			if (state.get(BlockStateProperties.FACE) == AttachFace.FLOOR)
			{
				facing = EnumFacing.UP;
			}
			else if (state.get(BlockStateProperties.FACE) == AttachFace.CEILING)
			{
				facing = EnumFacing.DOWN;
			}
			else
			{
				facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
			}
			woolPos = pos.offset(facing.getOpposite());
		}
		else if (block == Blocks.REDSTONE_WALL_TORCH || block == Blocks.TRIPWIRE_HOOK)
		{
			woolPos = pos.offset(state.get(BlockHorizontal.HORIZONTAL_FACING).getOpposite());
		}
		else if (block instanceof BlockRailPowered ||
				block == Blocks.REPEATER || block == Blocks.COMPARATOR || block == Blocks.REDSTONE_TORCH ||
				block instanceof BlockBasePressurePlate)  // on block
		{
			woolPos = pos.down();
		}
		else
		{
			return null;
		}

		return WoolTool.getWoolColorAtPosition(world.getWorld(), woolPos);
	}

	public static ITextComponent getTranslatedName(Block block)
	{
		ITextComponent name = new TextComponentTranslation(block.getTranslationKey());
		name.getStyle().setColor(TextFormatting.WHITE);
		return name;
	}

	static String getBlockEventMessageExtra(int eventID, int eventParam)
	{
		return String.format("^w eventID: %d (%s)\neventParam: %d (%s)",
				eventID, PistonBlockEventType.getById(eventID), eventParam, EnumFacing.byIndex(eventParam));
	}

	// Debug things

	public static void printAllDimensionGameTime()
	{
		for (World world : CarpetServer.minecraft_server.getWorlds())
		{
			System.err.println("    " + world.getDimension().getType() + " " + world.getGameTime() + " " + world.getWorldInfo());
		}
	}
}
