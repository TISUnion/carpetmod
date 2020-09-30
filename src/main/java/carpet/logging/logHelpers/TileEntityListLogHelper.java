package carpet.logging.logHelpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import carpet.microtick.MicroTickLoggerManager;
import carpet.microtick.MicroTickUtil;
import com.google.common.collect.Lists;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class TileEntityListLogHelper {
    private static final List<ITextComponent> messages = Lists.newArrayList();
    
    public static void log(World world, int dimensionID, String msg, BlockPos pos)
    {
        if (!LoggerRegistry.__tileentitylist)
        {
            return;
        }
        TileEntityListLogHelper.messages.add(Messenger.c(
                "g [" + world.getGameTime() + "] ",
                "w " + "TE" + " ",
                "t " + msg + " ",
                Messenger.tp("w", pos),
                "g  at ", 
                "y " + MicroTickLoggerManager.getTickStage(world) + " ",
                "g in ",
                MicroTickUtil.getDimensionNameText(dimensionID).applyTextStyle(TextFormatting.DARK_GREEN)
        ));
    }
    public static void log(World world, int dimensionID, String msg, TileEntity te)
    {
        TileEntityListLogHelper.log(world, dimensionID, msg, te.getPos());
    }
        
    public static void flush()
    {
        LoggerRegistry.getLogger("tileentitylist").log( () -> 
        {
            List<ITextComponent> comp = new ArrayList<>();
            Iterator<ITextComponent> iterator = TileEntityListLogHelper.messages.iterator();
            while (iterator.hasNext()) 
            {
                ITextComponent message = iterator.next();
                comp.add(message);
            }
            return comp.toArray(new ITextComponent[0]);
        });
        TileEntityListLogHelper.messages.clear();
    }
    
}
