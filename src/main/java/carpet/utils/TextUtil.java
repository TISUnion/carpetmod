package carpet.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.Fluid;
import net.minecraft.state.IProperty;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.dimension.DimensionType;

import java.util.List;
import java.util.Map;

public class TextUtil
{
	// mojang compatibility thing <3
	// these get changed in 1.16 so for easier compatible coding just wrap these methods
	public static ITextComponent attachHoverEvent(ITextComponent text, HoverEvent hoverEvent)
	{
		text.getStyle().setHoverEvent(hoverEvent);
		return text;
	}

	public static ITextComponent attachHoverText(ITextComponent text, ITextComponent hoverText)
	{
		return attachHoverEvent(text, new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
	}

	public static ITextComponent attachClickEvent(ITextComponent text, ClickEvent clickEvent)
	{
		text.getStyle().setClickEvent(clickEvent);
		return text;
	}

	public static ITextComponent attachFormatting(ITextComponent text, TextFormatting... formattings)
	{
		text.applyTextStyles(formattings);
		return text;
	}

	public static ITextComponent copyText(ITextComponent text)
	{
		return text.deepCopy();
	}
	// mojang compatibility thing ends

	private static final Map<DimensionType, ITextComponent> DIMENSION_NAME = Maps.newHashMap();

	static
	{
		DIMENSION_NAME.put(DimensionType.OVERWORLD, new TextComponentTranslation("createWorld.customize.preset.overworld"));
		DIMENSION_NAME.put(DimensionType.NETHER, new TextComponentTranslation("advancements.nether.root.title"));
		DIMENSION_NAME.put(DimensionType.THE_END, new TextComponentTranslation("advancements.end.root.title"));
	}

	private static String getTeleportHint()
	{
		return Translations.tr("util.teleport_hint", "Click to teleport to");
	}

	public static String getTeleportCommand(Vec3d pos, DimensionType dimensionType)
	{
		return String.format("/execute in %s run tp %s %s %s", dimensionType, pos.x, pos.y, pos.z);
	}

	public static String getTeleportCommand(Vec3i pos, DimensionType dimensionType)
	{
		return String.format("/execute in %s run tp %d %d %d", dimensionType, pos.getX(), pos.getY(), pos.getZ());
	}

	public static String getTeleportCommandPlayer(EntityPlayer player)
	{
		String name = player.getGameProfile().getName();
		return String.format("/execute at %1$s run tp %1$s", name);
	}

	public static String getTeleportCommand(Entity entity)
	{
		if (entity instanceof EntityPlayer)
		{
			return getTeleportCommandPlayer((EntityPlayer)entity);
		}
		String uuid = entity.getUniqueID().toString();
		return String.format("/tp %s", uuid);
	}

	public static ITextComponent join(ITextComponent joiner, ITextComponent... items)
	{
		ITextComponent text = Messenger.s("");
		for (int i = 0; i < items.length; i++)
		{
			if (i > 0)
			{
				text.appendSibling(joiner);
			}
			text.appendSibling(items[i]);
		}
		return text;
	}

	public static Style parseCarpetStyle(String style)
	{
		return Messenger.parseStyle(style);
	}

	public static ITextComponent getFancyText(String style, ITextComponent displayText, ITextComponent hoverText, ClickEvent clickEvent)
	{
		ITextComponent text = (ITextComponent)displayText.deepCopy();
		if (style != null)
		{
			text.setStyle(Messenger.c(style + "  ").getSiblings().get(0).getStyle());
		}
		if (hoverText != null)
		{
			text.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
		}
		if (clickEvent != null)
		{
			text.getStyle().setClickEvent(clickEvent);
		}
		return text;
	}

	private static ITextComponent __getCoordinateText(String style, DimensionType dim, String posText, String command)
	{
		ITextComponent hoverText = Messenger.s("");
		hoverText.appendText(String.format("%s %s\n", getTeleportHint(), posText));
		hoverText.appendText(Translations.tr("util.teleport_hint_dimension", "Dimension: "));
		hoverText.appendSibling(getDimensionNameText(dim));
		return getFancyText(style, Messenger.s(posText), hoverText, new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
	}

	public static String getCoordinateString(Vec3d pos)
	{
		return String.format("[%.1f, %.1f, %.1f]", pos.x, pos.y, pos.z);
	}

	public static String getCoordinateString(Vec3i pos)
	{
		return String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());
	}

	public static ITextComponent getCoordinateText(String style, Vec3d pos, DimensionType dim)
	{
		return __getCoordinateText(style, dim, getCoordinateString(pos), getTeleportCommand(pos, dim));
	}

	public static ITextComponent getCoordinateText(String style, Vec3i pos, DimensionType dim)
	{
		return __getCoordinateText(style, dim, getCoordinateString(pos), getTeleportCommand(pos, dim));
	}

	public static ITextComponent getEntityText(String style, Entity entity)
	{
		ITextComponent entityBaseName = entity.getType().getName().deepCopy();
		ITextComponent entityDisplayName = entity.getName().deepCopy();
		ITextComponent hoverText = Messenger.c(entityBaseName, Messenger.s("\n"), String.format("w %s ", getTeleportHint()), entityDisplayName);
		return getFancyText(style, entityDisplayName, hoverText, new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, getTeleportCommand(entity)));
	}

//	public static ITextComponent getAttributeText(AttributeModifier attribute)
//	{
//		return new TextComponentTranslation("attribute.name." + attribute.getName());
//	}

	public static ITextComponent getDimensionNameText(DimensionType dim)
	{
		return DIMENSION_NAME.getOrDefault(dim, Messenger.s(dim.toString())).deepCopy();
	}

	public static TextComponentTranslation getTranslatedName(String key, TextFormatting color, Object... args)
	{
		TextComponentTranslation text = new TextComponentTranslation(key, args);
		if (color != null)
		{
			attachFormatting(text, color);
		}
		return text;
	}
	public static TextComponentTranslation getTranslatedName(String key, Object... args)
	{
		return getTranslatedName(key, null, args);
	}

	public static ITextComponent colored(ITextComponent text, Object value)
	{
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

	public static ITextComponent colored(Object value)
	{
		return colored(Messenger.s(value.toString()), value);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> ITextComponent property(IProperty<T> property, Object value)
	{
		return colored(Messenger.s(property.getName((T)value)), value);
	}

	public static ITextComponent getBlockName(Block block)
	{
		return TextUtil.attachFormatting(new TextComponentTranslation(block.getTranslationKey()), TextFormatting.WHITE);
	}

	public static ITextComponent getBlockName(IBlockState blockState)
	{
		List<ITextComponent> hovers = Lists.newArrayList();
		hovers.add(getBlockName(blockState.getBlock()));
		for (IProperty<?> property: blockState.getProperties())
		{
			hovers.add(Messenger.c(
					Messenger.s(property.getName()),
					"g : ",
					property(property, blockState.get(property))
			));
		}
		return getFancyText(
				null,
				getBlockName(blockState.getBlock()),
				join(Messenger.s("\n"), hovers.toArray(new ITextComponent[0])),
				new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, BlockStateParser.toString(blockState, null))
		);
	}

	public static ITextComponent getFluidName(Fluid fluid)
	{
		return getBlockName(fluid.getDefaultState().getBlockState().getBlock());
	}

	// some language doesn't use space char to divide word
	// so here comes the compatibility
	public static String getSpace()
	{
		return Translations.tr("language_tool.space", " ");
	}

	public static ITextComponent getSpaceText()
	{
		return Messenger.s(getSpace());
	}

	public static ITextComponent format(String formatter, Object... args)
	{
		TextComponentTranslation dummy = new TextComponentTranslation(formatter, args);
		try
		{
			dummy.getChildren().clear();
			dummy.invokeInitializeFromFormat(formatter);
			return Messenger.c(dummy.getChildren().toArray(new Object[0]));
		}
		catch (TextComponentTranslationFormatException e)
		{
			return Messenger.s(formatter);
		}
	}
}
