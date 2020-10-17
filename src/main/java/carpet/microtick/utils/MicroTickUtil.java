package carpet.microtick.utils;

import carpet.microtick.MicroTickLoggerManager;
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
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.Map;
import java.util.Optional;

public class MicroTickUtil
{
	public static final EnumFacing[] DIRECTION_VALUES = EnumFacing.values();
	private static final Map<EnumDyeColor, String> COLOR_STYLE = Maps.newHashMap();
	static
	{
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
		COLOR_STYLE.put(EnumDyeColor.BLUE, "v");
		COLOR_STYLE.put(EnumDyeColor.BROWN, "n");
		COLOR_STYLE.put(EnumDyeColor.GREEN, "e");
		COLOR_STYLE.put(EnumDyeColor.RED, "r");
		COLOR_STYLE.put(EnumDyeColor.BLACK, "k");
	}

	public static ITextComponent getFancyText(String style, ITextComponent displayText, ITextComponent hoverText, ClickEvent clickEvent)
	{
		ITextComponent text = (ITextComponent)displayText.deepCopy();
		if (style != null)
		{
			text.setStyle(Messenger.c(style + "  ").getSiblings().get(0).getStyle());
		}
		text.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
		if (clickEvent != null)
		{
			text.getStyle().setClickEvent(clickEvent);
		}
		return text;
	}

	public static String getColorStyle(EnumDyeColor color)
	{
		return COLOR_STYLE.getOrDefault(color, "w");
	}

	public static ITextComponent getColoredValue(Object value)
	{
		ITextComponent text = Messenger.s(value.toString());
		if (Boolean.TRUE.equals(value))
		{
			text.getStyle().setColor(TextFormatting.GREEN);
		}
		else if (Boolean.FALSE.equals(value))
		{
			text.getStyle().setColor(TextFormatting.RED);
		}
		return text;
	}

	public static ITextComponent getSuccessText(boolean bool, boolean showReturnValue)
	{
		ITextComponent hintText = bool ?
				Messenger.c("e " + MicroTickLoggerManager.tr("Successful")) :
				Messenger.c("r " + MicroTickLoggerManager.tr("Failed"));
		if (showReturnValue)
		{
			hintText.appendSibling(Messenger.c(
					String.format("w \n%s: ", MicroTickLoggerManager.tr("Return value")),
					getColoredValue(bool)
			));
		}
		return bool ?
				MicroTickUtil.getFancyText("e", Messenger.s("√"), hintText, null) :
				MicroTickUtil.getFancyText("r", Messenger.s("×"), hintText, null);
	}

	private static boolean isPositionAvailable(World world, BlockPos pos)
	{
		return world.isAreaLoaded(pos, 0);
	}

	public static Optional<EnumDyeColor> getWoolColor(World world, BlockPos pos)
	{
		if (!MicroTickLoggerManager.isLoggerActivated())
		{
			return Optional.empty();
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
			return Optional.empty();
		}

		return Optional.ofNullable(WoolTool.getWoolColorAtPosition(world.getWorld(), woolPos));
	}

	public static Optional<EnumDyeColor> getEndRodWoolColor(World world, BlockPos pos)
	{
		if (!MicroTickLoggerManager.isLoggerActivated() || !isPositionAvailable(world, pos))
		{
			return Optional.empty();
		}
		for (EnumFacing facing: DIRECTION_VALUES)
		{
			BlockPos blockEndRodPos = pos.offset(facing);
			IBlockState iBlockState = world.getBlockState(blockEndRodPos);
			if (iBlockState.getBlock() == Blocks.END_ROD && iBlockState.get(BlockStateProperties.FACING).getOpposite() == facing)
			{
				BlockPos woolPos = blockEndRodPos.offset(facing);
				EnumDyeColor color = WoolTool.getWoolColorAtPosition(world.getWorld(), woolPos);
				if (color != null)
				{
					return Optional.of(color);
				}
			}
		}
		return Optional.empty();
	}

	public static Optional<EnumDyeColor> getWoolOrEndRodWoolColor(World world, BlockPos pos)
	{
		Optional<EnumDyeColor> optionalDyeColor = getWoolColor(world, pos);
		if (!optionalDyeColor.isPresent())
		{
			optionalDyeColor = getEndRodWoolColor(world, pos);
		}
		return optionalDyeColor;
	}

	public static ITextComponent getTranslatedText(Block block)
	{
		ITextComponent name = new TextComponentTranslation(block.getTranslationKey());
		name.getStyle().setColor(TextFormatting.WHITE);
		return name;
	}

	public static String getFormattedDirectionString(EnumFacing direction)
	{
		String name = direction.toString();
		String translatedName = MicroTickLoggerManager.tr("direction." + name, name);
		char sign = direction.getAxisDirection().getOffset() > 0 ? '+' : '-';
		return String.format("%s (%c%s)", translatedName, sign, direction.getAxis());
	}

	// from carpettisaddition/utils/Util.java
	private static final Map<DimensionType, ITextComponent> DIMENSION_NAME = Maps.newHashMap();
	static
	{
		DIMENSION_NAME.put(DimensionType.OVERWORLD, new TextComponentTranslation("createWorld.customize.preset.overworld"));
		DIMENSION_NAME.put(DimensionType.NETHER, new TextComponentTranslation("advancements.nether.root.title"));
		DIMENSION_NAME.put(DimensionType.THE_END, new TextComponentTranslation("advancements.end.root.title"));
	}

	public static ITextComponent getDimensionNameText(DimensionType dim)
	{
		return DIMENSION_NAME.getOrDefault(dim, Messenger.s(dim.toString())).deepCopy();
	}

	public static String getSpace()
	{
		return " ";
	}
	public static ITextComponent getSpaceText()
	{
		return Messenger.s(getSpace());
	}

	public static String getTeleportCommand(Vec3i pos, DimensionType dimensionType)
	{
		return String.format("/execute in %s run tp %d %d %d", dimensionType, pos.getX(), pos.getY(), pos.getZ());
	}
}
