package carpet.logging.logHelpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;

public class BlockUpdatesLogHelper {
    private static String stage = new String();
    private static long gameTime;
    private static List<ITextComponent> messages = Lists.newArrayList();
    
    public static String getDimension(int dimensionID)
    {
        String dimension = new String();
        switch (dimensionID)
        {
        case 0: 
            dimension = "Overworld";
            break;
        case 1: 
            dimension = "End";
            break;
        case -1: 
            dimension = "Nether";
            break;
        }
        return dimension;
    }

    public static void setTickStage(String stage)
    {
        BlockUpdatesLogHelper.stage = stage;
    }

    public static String getTickStage()
    {
        return BlockUpdatesLogHelper.stage;
    }
    
    public static void setGameTime(long gameTime)
    {
        if (BlockUpdatesLogHelper.gameTime != gameTime)
        {
            BlockUpdatesLogHelper.logMessages(BlockUpdatesLogHelper.gameTime);
        }
        BlockUpdatesLogHelper.gameTime = gameTime;
    }
    
    private static String getColorStype(EnumDyeColor color)
    {
        switch (color)
        {
            case WHITE: return "w";
            case ORANGE: return "d";
            case MAGENTA: return "m";
            case LIGHT_BLUE: return "c";
            case YELLOW: return "y";
            case LIME: return "l";
            case PINK: return "r";
            case GRAY: return "f";
            case LIGHT_GRAY: return "g";
            case CYAN: return "q";
            case PURPLE: return "p";
            case BLUE: return "v";
            case BROWN: return "n";
            case GREEN: return "e";
            case RED: return "r";
            case BLACK: return "k";
        }
        return "t";
    }
    
    public static void onBlockUpdated(BlockPos pos, String type, int dimensionID, EnumDyeColor color)
    {
        BlockUpdatesLogHelper.messages.add(Messenger.c(
                BlockUpdatesLogHelper.getColorStype(color) + " # ",
                Messenger.tp("w", pos),
                "w  ",
                "t " + type + " ",
                "g at ",
                "y " + BlockUpdatesLogHelper.stage + " ",
                "g in ",
                "e " + BlockUpdatesLogHelper.getDimension(dimensionID)));
    }
    
    private static void logMessages(long gameTime)
    {
        if (BlockUpdatesLogHelper.messages.isEmpty())
        {
            return;
        }
        Set<ITextComponent> messageHashSet = Sets.newHashSet();
        Iterator<ITextComponent> iterator = BlockUpdatesLogHelper.messages.iterator();
        LoggerRegistry.getLogger("blockupdates").log( (option) -> 
        {
            List<ITextComponent> comp = new ArrayList<>();
            comp.add(Messenger.c("g [GameTime ", "w " + gameTime, "g ] ---------------------------"));
            while (iterator.hasNext()) 
            {
                ITextComponent message = iterator.next();
                iterator.remove();
                
                boolean flag = option.equals("all");
                if (!messageHashSet.contains(message))
                {
                    messageHashSet.add(message);
                    flag = true;
                }
                if (flag)
                {
                    comp.add(message);
                }
            }
            return comp.toArray(new ITextComponent[0]);
        });
        BlockUpdatesLogHelper.messages.clear();
    }
}
