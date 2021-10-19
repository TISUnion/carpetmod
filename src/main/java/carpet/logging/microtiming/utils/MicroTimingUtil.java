package carpet.logging.microtiming.utils;

import carpet.CarpetServer;
import carpet.logging.LoggerRegistry;
import carpet.logging.microtiming.MicroTimingLogger;
import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.logging.microtiming.enums.MicroTimingTarget;
import carpet.logging.microtiming.marker.MicroTimingMarkerManager;
import carpet.logging.microtiming.marker.MicroTimingMarkerType;
import carpet.settings.CarpetSettings;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import carpet.utils.WoolTool;
import com.google.common.collect.Maps;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MicroTimingUtil
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

	public static String getColorStyle(EnumDyeColor color)
	{
		return COLOR_STYLE.getOrDefault(color, "w");
	}

	public static ITextComponent getColoredValue(Object value)
	{
		ITextComponent text = Messenger.s(value.toString());
		TextFormatting color = null;
		if (Boolean.TRUE.equals(value))
		{
			color = TextFormatting.GREEN;
		}
		else if (Boolean.FALSE.equals(value))
		{
			color = TextFormatting.RED;
		}
		if (value instanceof Number)
		{
			color = TextFormatting.GOLD;
		}
		if (color != null)
		{
			text.getStyle().setColor(color);
		}
		return text;
	}

	public static ITextComponent getSuccessText(boolean bool, boolean showReturnValue, ITextComponent hoverExtra)
	{
		ITextComponent hintText = bool ?
				Messenger.c("e " + MicroTimingLoggerManager.tr("Successful")) :
				Messenger.c("r " + MicroTimingLoggerManager.tr("Failed"));
		if (hoverExtra != null)
		{
			hintText.appendSibling(Messenger.c("w \n", hoverExtra));
		}
		if (showReturnValue)
		{
			hintText.appendSibling(Messenger.c(
					String.format("w \n%s: ", MicroTimingLoggerManager.tr("Return value")),
					getColoredValue(bool)
			));
		}
		return bool ?
				TextUtil.getFancyText("e", Messenger.s("√"), hintText, null) :
				TextUtil.getFancyText("r", Messenger.s("×"), hintText, null);
	}
	public static ITextComponent getSuccessText(boolean bool, boolean showReturnValue)
	{
		return getSuccessText(bool, showReturnValue, null);
	}

	private static boolean isPositionAvailable(World world, BlockPos pos)
	{
		return world.isAreaLoaded(pos, 0);
	}

	public static Optional<EnumDyeColor> getWoolColor(World world, BlockPos pos)
	{
		if (!MicroTimingLoggerManager.isLoggerActivated())
		{
			return Optional.empty();
		}
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		BlockPos woolPos;

		if (block instanceof BlockObserver || block instanceof BlockEndRod ||
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
		else if (block instanceof BlockRedstoneTorchWall|| block instanceof BlockTripWireHook)
		{
			woolPos = pos.offset(state.get(BlockHorizontal.HORIZONTAL_FACING).getOpposite());
		}
		else if (block instanceof BlockRailPowered ||
				block instanceof BlockRedstoneDiode ||
				block instanceof BlockRedstoneTorch ||
				block instanceof BlockRedstoneWire ||
				block instanceof BlockBasePressurePlate
		)  // on block
		{
			woolPos = pos.down();
		}
		else
		{
			return Optional.empty();
		}

		return Optional.ofNullable(WoolTool.getWoolColorAtPosition(world, woolPos));
	}

	public static Optional<EnumDyeColor> getEndRodWoolColor(World world, BlockPos pos)
	{
		if (!MicroTimingLoggerManager.isLoggerActivated() || !isPositionAvailable(world, pos))
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
				EnumDyeColor color = WoolTool.getWoolColorAtPosition(world, woolPos);
				if (color != null)
				{
					return Optional.of(color);
				}
			}
		}
		return MicroTimingMarkerManager.getInstance().getColor(world, pos, MicroTimingMarkerType.END_ROD);
	}

	public static Optional<EnumDyeColor> getWoolOrEndRodWoolColor(World world, BlockPos pos)
	{
		Optional<EnumDyeColor> optionalDyeColor = getWoolColor(world, pos);
		if (!optionalDyeColor.isPresent())
		{
			optionalDyeColor = getEndRodWoolColor(world, pos);
		}
		boolean usingFallbackColor = false;
		if (!optionalDyeColor.isPresent())
		{
			switch (CarpetSettings.microTimingTarget)
			{
				case IN_RANGE:
					usingFallbackColor = world.getClosestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, MicroTimingTarget.IN_RANGE_RADIUS, player -> true) != null;
					break;
				case ALL:
					usingFallbackColor = true;
					break;
				case LABELLED:
				default:
					break;
			}
			if (usingFallbackColor)
			{
				optionalDyeColor = Optional.of(EnumDyeColor.LIGHT_GRAY);
			}
		}
		if (!optionalDyeColor.isPresent() || usingFallbackColor)
		{
			Optional<EnumDyeColor> markerColor = MicroTimingMarkerManager.getInstance().getColor(world, pos, MicroTimingMarkerType.REGULAR);
			if (markerColor.isPresent())
			{
				optionalDyeColor = markerColor;
			}
		}
		return optionalDyeColor;
	}

	public static String getFormattedDirectionString(EnumFacing direction)
	{
		String name = direction.toString();
		String translatedName = MicroTimingLoggerManager.tr("direction." + name, name);
		char sign = direction.getAxisDirection().getOffset() > 0 ? '+' : '-';
		return String.format("%s (%c%s)", translatedName, sign, direction.getAxis());
	}

	public static boolean isMarkerEnabled()
	{
		return MicroTimingLoggerManager.isLoggerActivated() && CarpetSettings.microTimingDyeMarker.equals("true");
	}

	public static boolean isPlayerSubscribed(EntityPlayer playerEntity)
	{
		Map<String, String> map = LoggerRegistry.getPlayerSubscriptions(playerEntity.getName().getString());
		return map != null && map.containsKey(MicroTimingLogger.NAME);
	}

	public static List<EntityPlayerMP> getSubscribedPlayers()
	{
		return CarpetServer.minecraft_server == null ?
				Collections.emptyList() :
				CarpetServer.minecraft_server.getPlayerList().getPlayers().stream().
						filter(MicroTimingUtil::isPlayerSubscribed).
						collect(Collectors.toList());
	}
}
