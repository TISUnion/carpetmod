package carpet.utils;

import carpet.CarpetServer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.ai.attributes.BaseAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Messenger
{
    public static final Logger LOG = LogManager.getLogger();

    /*
     messsage: "desc me ssa ge"
     desc contains:
     i = italic
     s = strikethrough
     u = underline
     b = bold
     o = obfuscated

     w = white
     y = yellow
     m = magenta (light purple)
     r = red
     c = cyan (aqua)
     l = lime (green)
     t = light blue (blue)
     f = dark gray
     g = gray
     d = gold
     p = dark purple (purple)
     n = dark red (brown)
     q = dark aqua
     e = dark green
     v = dark blue (navy)
     k = black

     / = action added to the previous component
     */

    private static ITextComponent _applyStyleToTextComponent(ITextComponent comp, String style)
    {
        //could be rewritten to be more efficient
        if (style.indexOf('i')>=0) comp.getStyle().setItalic(true);
        if (style.indexOf('s')>=0) comp.getStyle().setStrikethrough(true);
        if (style.indexOf('u')>=0) comp.getStyle().setUnderlined(true);
        if (style.indexOf('b')>=0) comp.getStyle().setBold(true);
        if (style.indexOf('o')>=0) comp.getStyle().setObfuscated(true);
        comp.getStyle().setColor(TextFormatting.WHITE);
        if (style.indexOf('w')>=0) comp.getStyle().setColor(TextFormatting.WHITE); // not needed
        if (style.indexOf('y')>=0) comp.getStyle().setColor(TextFormatting.YELLOW);
        if (style.indexOf('m')>=0) comp.getStyle().setColor(TextFormatting.LIGHT_PURPLE);
        if (style.indexOf('r')>=0) comp.getStyle().setColor(TextFormatting.RED);
        if (style.indexOf('c')>=0) comp.getStyle().setColor(TextFormatting.AQUA);
        if (style.indexOf('l')>=0) comp.getStyle().setColor(TextFormatting.GREEN);
        if (style.indexOf('t')>=0) comp.getStyle().setColor(TextFormatting.BLUE);
        if (style.indexOf('f')>=0) comp.getStyle().setColor(TextFormatting.DARK_GRAY);
        if (style.indexOf('g')>=0) comp.getStyle().setColor(TextFormatting.GRAY);
        if (style.indexOf('d')>=0) comp.getStyle().setColor(TextFormatting.GOLD);
        if (style.indexOf('p')>=0) comp.getStyle().setColor(TextFormatting.DARK_PURPLE);
        if (style.indexOf('n')>=0) comp.getStyle().setColor(TextFormatting.DARK_RED);
        if (style.indexOf('q')>=0) comp.getStyle().setColor(TextFormatting.DARK_AQUA);
        if (style.indexOf('e')>=0) comp.getStyle().setColor(TextFormatting.DARK_GREEN);
        if (style.indexOf('v')>=0) comp.getStyle().setColor(TextFormatting.DARK_BLUE);
        if (style.indexOf('k')>=0) comp.getStyle().setColor(TextFormatting.BLACK);
        return comp;
    }
    public static TextFormatting heatmap_color(double actual, double reference)
    {
        TextFormatting color = TextFormatting.DARK_GREEN;
        if (actual > 0.5D*reference) color = TextFormatting.YELLOW;
        if (actual > 0.8D*reference) color = TextFormatting.RED;
        if (actual > reference) color = TextFormatting.LIGHT_PURPLE;
        return color;
    }
    public static String creatureTypeColor(EnumCreatureType type)
    {
        switch (type)
        {
            case MONSTER:
                return "n";
            case CREATURE:
                return "e";
            case AMBIENT:
                return "f";
            case WATER_CREATURE:
                return "v";
        }
        return "w";
    }

    private static ITextComponent _getChatComponentFromDesc(String message, ITextComponent previous_message)
    {
        if (message.equalsIgnoreCase(""))
        {
            return new TextComponentString("");
        }
        String[] parts = StringUtils.splitPreserveAllTokens(message, " ", 2);
        String desc = parts[0];
        String str = "";
        if (parts.length > 1) str = parts[1];
        if (desc.charAt(0) == '/') // deprecated
        {
            if (previous_message != null)
                previous_message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message));
            return previous_message;
        }
        if (desc.charAt(0) == '?')
        {
            if (previous_message != null)
                previous_message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message.substring(1)));
            return previous_message;
        }
        if (desc.charAt(0) == '!')
        {
            if (previous_message != null)
                previous_message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, message.substring(1)));
            return previous_message;
        }
        if (desc.charAt(0) == '^')
        {
            if (previous_message != null)
                previous_message.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, c(message.substring(1))));
            return previous_message;
        }
        ITextComponent txt = new TextComponentString(str);
        return _applyStyleToTextComponent(txt, desc);
    }
    public static ITextComponent tp(String desc, Vec3d pos) { return tp(desc, pos.x, pos.y, pos.z); }
    public static ITextComponent tp(String desc, BlockPos pos) { return tp(desc, pos.getX(), pos.getY(), pos.getZ()); }
    public static ITextComponent tp(String desc, double x, double y, double z) { return tp(desc, (float)x, (float)y, (float)z);}
    public static ITextComponent tp(String desc, float x, float y, float z)
    {
        return _getCoordsTextComponent(desc, x, y, z, false);
    }
    public static ITextComponent tp(String desc, int x, int y, int z)
    {
        return _getCoordsTextComponent(desc, (float)x, (float)y, (float)z, true);
    }

    /// to be continued
    public static ITextComponent dbl(String style, double double_value)
    {
        return c(String.format("%s %.1f",style,double_value),String.format("^w %f",double_value));
    }
    public static ITextComponent dbls(String style, double ... doubles)
    {
        StringBuilder str = new StringBuilder(style + " [ ");
        String prefix = "";
        for (double dbl : doubles)
        {
            str.append(String.format("%s%.1f", prefix, dbl));
            prefix = ", ";
        }
        str.append(" ]");
        return c(str.toString());
    }
    public static ITextComponent dblf(String style, double ... doubles)
    {
        StringBuilder str = new StringBuilder(style + " [ ");
        String prefix = "";
        for (double dbl : doubles)
        {
            str.append(String.format("%s%f", prefix, dbl));
            prefix = ", ";
        }
        str.append(" ]");
        return c(str.toString());
    }
    public static ITextComponent dblt(String style, double ... doubles)
    {
        List<Object> components = new ArrayList<>();
        components.add(style+" [ ");
        String prefix = "";
        for (double dbl:doubles)
        {

            components.add(String.format("%s %s%.1f",style, prefix, dbl));
            components.add("?"+dbl);
            components.add("^w "+dbl);
            prefix = ", ";
        }
        //components.remove(components.size()-1);
        components.add(style+"  ]");
        return c(components.toArray(new Object[0]));
    }

    private static ITextComponent _getCoordsTextComponent(String style, float x, float y, float z, boolean isInt)
    {
        String text;
        String command;
        if (isInt)
        {
            text = String.format("%s [ %d, %d, %d ]",style, (int)x,(int)y, (int)z );
            command = String.format("!/tp %d %d %d",(int)x,(int)y, (int)z);
        }
        else
        {
            text = String.format("%s [ %.1f, %.1f, %.1f]",style, x, y, z);
            command = String.format("!/tp %.3f %.3f %.3f",x, y, z);
        }
        return c(text, command);
    }

    //message source
    public static void m(CommandSource source, Object ... fields)
    {
        source.sendFeedback(Messenger.c(fields),true);
    }
    public static void m(EntityPlayer player, Object ... fields)
    {
        player.sendMessage(Messenger.c(fields));
    }

    /*
    composes single line, multicomponent message, and returns as one chat messagge
     */
    public static ITextComponent c(Object ... fields)
    {
        ITextComponent message = new TextComponentString("");
        ITextComponent previous_component = null;
        for (Object o: fields)
        {
            if (o instanceof ITextComponent)
            {
                message.appendSibling((ITextComponent)o);
                previous_component = (ITextComponent)o;
                continue;
            }
            String txt = o.toString();
            ITextComponent comp = _getChatComponentFromDesc(txt,previous_component);
            if (comp != previous_component) message.appendSibling(comp);
            previous_component = comp;
        }
        return message;
    }

    //simple text

    public static ITextComponent s(String text)
    {
        return s(text,"");
    }
    public static ITextComponent s(String text, String style)
    {
        ITextComponent message = new TextComponentString(text);
        _applyStyleToTextComponent(message, style);
        return message;
    }

    public static void send(EntityPlayer player, Collection<ITextComponent> lines)
    {
        lines.forEach(player::sendMessage);
    }
    public static void send(CommandSource source, Collection<ITextComponent> lines)
    {
        lines.stream().forEachOrdered((s) -> source.sendFeedback(s, false));
    }

    public static void print_server_message(MinecraftServer server, String message)
    {
        if (server == null)
            LOG.error("Message not delivered: "+message);
        server.sendMessage(new TextComponentString(message));
        ITextComponent txt = c("gi "+message);
        for (EntityPlayer entityplayer : server.getPlayerList().getPlayers())
        {
            entityplayer.sendMessage(txt);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// ^ Legacy Carpet Mod Stuffs ^  //////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////  v       TISCM stuffs      v  //////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    private static final Translator translator = new Translator("util");

    /*
     * ----------------------------
     *    Text Factories - Basic
     * ----------------------------
     */

    // Simple Text
    public static ITextComponent s(Object text)
    {
        return new TextComponentString(text.toString());
    }

    // Simple Text with carpet style
    public static ITextComponent s(Object text, String carpetStyle)
    {
        return formatting(s(text), carpetStyle);
    }

    // Simple Text with formatting
    public static ITextComponent s(Object text, TextFormatting ...textFormattings)
    {
        return formatting(s(text), textFormattings);
    }

    public static ITextComponent newLine()
    {
        return s("\n");
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
            formatting(text, color);
        }
        return text;
    }

    public static ITextComponent colored(Object value)
    {
        return colored(s(value), value);
    }

    public static ITextComponent property(IProperty<?> property, Object value)
    {
        return colored(s(TextUtil.property(property, value)), value);
    }

    // Translation Text
    public static ITextComponent tr(String key, Object ... args)
    {
        return new TextComponentTranslation(key, args);
    }

    // Fancy text
    // A copy will be made to make sure the original displayText will not be modified
    public static ITextComponent fancy(String carpetStyle, ITextComponent displayText, ITextComponent hoverText, ClickEvent clickEvent)
    {
        ITextComponent text = copy(displayText);
        if (carpetStyle != null)
        {
            text.setStyle(parseCarpetStyle(carpetStyle));
        }
        if (hoverText != null)
        {
            hover(text, hoverText);
        }
        if (clickEvent != null)
        {
            click(text, clickEvent);
        }
        return text;
    }

    public static ITextComponent fancy(ITextComponent displayText, ITextComponent hoverText, ClickEvent clickEvent)
    {
        return fancy(null, displayText, hoverText, clickEvent);
    }

    public static ITextComponent join(ITextComponent joiner, ITextComponent... items)
    {
        ITextComponent text = s("");
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
    public static ITextComponent join(ITextComponent joiner, Iterable<ITextComponent> items)
    {
        List<ITextComponent> list = Lists.newArrayList(items);
        return join(joiner, list.toArray(new ITextComponent[0]));
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

    /*
     * -------------------------------
     *    Text Factories - Advanced
     * -------------------------------
     */

    public static ITextComponent bool(boolean value)
    {
        return s(String.valueOf(value), value ? TextFormatting.GREEN : TextFormatting.RED);
    }

    private static ITextComponent getTeleportHint(ITextComponent dest)
    {
        return translator.advTr("teleport_hint", "Click to teleport to %1$s", dest);
    }

    private static ITextComponent __coord(String style, @Nullable DimensionType dim, String posStr, String command)
    {
        ITextComponent hoverText = Messenger.s("");
        hoverText.appendSibling(getTeleportHint(Messenger.s(posStr)));
        if (dim != null)
        {
            hoverText.appendText("\n");
            hoverText.appendSibling(translator.advTr("teleport_hint.dimension", "Dimension"));
            hoverText.appendText(": ");
            hoverText.appendSibling(dimension(dim));
        }
        return fancy(style, Messenger.s(posStr), hoverText, new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
    }

    public static ITextComponent coord(String style, Vec3d pos, DimensionType dim) {return __coord(style, dim, TextUtil.coord(pos), TextUtil.tp(pos, dim));}
    public static ITextComponent coord(String style, Vec3i pos, DimensionType dim) {return __coord(style, dim, TextUtil.coord(pos), TextUtil.tp(pos, dim));}
    public static ITextComponent coord(String style, ChunkPos pos, DimensionType dim) {return __coord(style, dim, TextUtil.coord(pos), TextUtil.tp(pos, dim));}
    public static ITextComponent coord(String style, Vec3d pos) {return __coord(style, null, TextUtil.coord(pos), TextUtil.tp(pos));}
    public static ITextComponent coord(String style, Vec3i pos) {return __coord(style, null, TextUtil.coord(pos), TextUtil.tp(pos));}
    public static ITextComponent coord(String style, ChunkPos pos) {return __coord(style, null, TextUtil.coord(pos), TextUtil.tp(pos));}
    public static ITextComponent coord(Vec3d pos, DimensionType dim) {return coord(null, pos, dim);}
    public static ITextComponent coord(Vec3i pos, DimensionType dim) {return coord(null, pos, dim);}
    public static ITextComponent coord(ChunkPos pos, DimensionType dim) {return coord(null, pos, dim);}
    public static ITextComponent coord(Vec3d pos) {return coord(null, pos);}
    public static ITextComponent coord(Vec3i pos) {return coord(null, pos);}
    public static ITextComponent coord(ChunkPos pos) {return coord(null, pos);}

    private static ITextComponent __vector(String style, String displayText, String detailedText)
    {
        return fancy(style, Messenger.s(displayText), Messenger.s(detailedText), new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, detailedText));
    }
    public static ITextComponent vector(String style, Vec3d vec) {return __vector(style, TextUtil.vector(vec), TextUtil.vector(vec, 6));}
    public static ITextComponent vector(Vec3d vec) {return vector(null, vec);}

    public static ITextComponent entityType(EntityType<?> entityType)
    {
        return entityType.getName();
    }
    public static ITextComponent entityType(Entity entity)
    {
        return entityType(entity.getType());
    }

    public static ITextComponent entity(String style, Entity entity)
    {
        ITextComponent entityBaseName = entityType(entity);
        ITextComponent entityDisplayName = entity.getName();
        String entityTypeStr = Optional.ofNullable(EntityType.getId(entity.getType())).map(ResourceLocation::toString).orElse("?");
        ITextComponent hoverText = Messenger.c(
                translator.advTr("entity_type", "Entity type: %1$s (%2$s)", entityBaseName, s(entityTypeStr), newLine()),
                getTeleportHint(entityDisplayName)
        );
        return fancy(style, entityDisplayName, hoverText, new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.tp(entity)));
    }

    public static ITextComponent entity(Entity entity)
    {
        return entity(null, entity);
    }

    public static ITextComponent attribute(BaseAttribute attribute)
    {
        return tr("attribute.name." + attribute.getName());
    }

    private static final ImmutableMap<DimensionType, ITextComponent> DIMENSION_NAME = ImmutableMap.of(
            DimensionType.OVERWORLD, tr("createWorld.customize.preset.overworld"),
            DimensionType.NETHER, tr("advancements.nether.root.title"),
            DimensionType.THE_END, tr("advancements.end.root.title")
    );

    public static ITextComponent dimension(DimensionType dim)
    {
        ITextComponent dimText = DIMENSION_NAME.get(dim);
        return dimText != null ? copy(dimText) : Messenger.s(dim.toString());
    }
    public static ITextComponent dimension(World world)
    {
        return dimension(world.getDimension().getType());
    }

    public static ITextComponent getColoredDimensionSymbol(DimensionType dimensionType)
    {
        if (dimensionType.equals(DimensionType.OVERWORLD))
        {
            return s("O", TextFormatting.DARK_GREEN);  // DARK_GREEN
        }
        if (dimensionType.equals(DimensionType.NETHER))
        {
            return s("N", TextFormatting.DARK_RED);  // DARK_RED
        }
        if (dimensionType.equals(DimensionType.THE_END))
        {
            return s("E", TextFormatting.DARK_PURPLE);  // DARK_PURPLE
        }
        return s(dimensionType.toString().toUpperCase().substring(0, 1));
    }

    public static ITextComponent block(Block block)
    {
        return hover(tr(block.getTranslationKey()), s(TextUtil.block(block)));
    }

    public static ITextComponent block(IBlockState blockState)
    {
        List<ITextComponent> hovers = Lists.newArrayList();
        hovers.add(s(TextUtil.block(blockState.getBlock())));
        for (IProperty<?> property: blockState.getProperties())
        {
            hovers.add(Messenger.c(
                    Messenger.s(property.getName()),
                    "g : ",
                    property(property, blockState.get(property))
            ));
        }
        return fancy(
                block(blockState.getBlock()),
                join(s("\n"), hovers.toArray(new ITextComponent[0])),
                new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.block(blockState))
        );
    }

    public static ITextComponent fluid(Fluid fluid)
    {
        String fluidId = Optional.ofNullable(IRegistry.FLUID.getKey(fluid)).map(ResourceLocation::toString).orElse("?");
        return hover(block(fluid.getDefaultState().getBlockState().getBlock()), s(fluidId));
    }

    public static ITextComponent fluid(FluidState fluid)
    {
        return fluid(fluid.getFluid());
    }

    public static ITextComponent blockEntity(TileEntity blockEntity)
    {
        ResourceLocation id = IRegistry.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
        return s(id != null ?
                id.toString() : // vanilla block entity
                blockEntity.getClass().getSimpleName()  // modded block entity, assuming the class name is not obfuscated
        );
    }

    public static ITextComponent item(Item item)
    {
        return tr(item.getTranslationKey());
    }

    public static ITextComponent color(EnumDyeColor color)
    {
        String colorStr = color.getName().toLowerCase();
        return translator.advTr("color." + colorStr, colorStr.replace('_', ' '));
    }

    /*
     * --------------------
     *    Text Modifiers
     * --------------------
     */

    public static ITextComponent hover(ITextComponent text, HoverEvent hoverEvent)
    {
        text.getStyle().setHoverEvent(hoverEvent);
        return text;
    }

    public static ITextComponent hover(ITextComponent text, ITextComponent hoverText)
    {
        return hover(text, new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
    }

    public static ITextComponent click(ITextComponent text, ClickEvent clickEvent)
    {
        text.getStyle().setClickEvent(clickEvent);
        return text;
    }

    public static ITextComponent formatting(ITextComponent text, TextFormatting... formattings)
    {
        text.applyTextStyles(formattings);
        return text;
    }

    public static ITextComponent formatting(ITextComponent text, String carpetStyle)
    {
        Style textStyle = text.getStyle();
        Style parsedStyle = parseCarpetStyle(carpetStyle);

        textStyle.setColor(parsedStyle.getColorField());
        textStyle.setBold(parsedStyle.getBoldField());
        textStyle.setItalic(parsedStyle.getItalicField());
        textStyle.setUnderlined(parsedStyle.getUnderlineField());
        textStyle.setStrikethrough(parsedStyle.getStrikethroughField());
        textStyle.setObfuscated(parsedStyle.getObfuscatedField());

        return style(text, textStyle);
    }

    public static ITextComponent style(ITextComponent text, Style style)
    {
        text.setStyle(style);
        return text;
    }

    public static ITextComponent copy(ITextComponent text)
    {
        return text.deepCopy();
    }

    /*
     * ------------------
     *    Text Senders
     * ------------------
     */

    private static void __tell(CommandSource source, ITextComponent text, boolean broadcastToOps)
    {
        // translation logic is handled in carpettisaddition.mixins.translations.ServerPlayerEntityMixin
        source.sendFeedback(text, broadcastToOps);
    }

    public static void tell(CommandSource source, ITextComponent text, boolean broadcastToOps)
    {
        __tell(source, text, broadcastToOps);
    }
    public static void tell(EntityPlayer player, ITextComponent text, boolean broadcastToOps)
    {
        tell(player.getCommandSource(), text, broadcastToOps);
    }
    public static void tell(CommandSource source, ITextComponent text)
    {
        tell(source, text, false);
    }
    public static void tell(EntityPlayer player, ITextComponent text)
    {
        tell(player, text, false);
    }
    public static void tell(CommandSource source, Iterable<ITextComponent> texts, boolean broadcastToOps)
    {
        texts.forEach(text -> tell(source, text, broadcastToOps));
    }
    public static void tell(EntityPlayer player, Iterable<ITextComponent> texts, boolean broadcastToOps)
    {
        texts.forEach(text -> tell(player, text, broadcastToOps));
    }
    public static void tell(CommandSource source, Iterable<ITextComponent> texts)
    {
        tell(source, texts, false);
    }
    public static void tell(EntityPlayer player, Iterable<ITextComponent> texts)
    {
        tell(player, texts, false);
    }
    public static void tell(CommandSource source, String text)
    {
        tell(source, s(text));
    }
    public static void tell(EntityPlayer player, String text)
    {
        tell(player, s(text));
    }

    public static void reminder(EntityPlayer player, ITextComponent text)
    {
        player.sendStatusMessage(text, true);
    }

    public static void sendToConsole(ITextComponent text)
    {
        if (CarpetServer.minecraft_server != null)
        {
            CarpetServer.minecraft_server.sendMessage(text);
        }
    }

    public static void broadcast(ITextComponent text)
    {
        sendToConsole(text);
        if (CarpetServer.minecraft_server != null)
        {
            CarpetServer.minecraft_server.getPlayerList().getPlayers().forEach(player -> tell(player, text));
        }
    }


    /*
     * ----------
     *    Misc
     * ----------
     */

    public static Style parseCarpetStyle(String style)
    {
        return _applyStyleToTextComponent(s(""), style).getStyle();
    }

    // some language doesn't use space char to divide word
    // so here comes the compatibility
    @Deprecated
    public static ITextComponent getSpaceText()
    {
        return translator.advTr("language.space", " ");
    }
}

