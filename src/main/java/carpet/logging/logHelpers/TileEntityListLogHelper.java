package carpet.logging.logHelpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class TileEntityListLogHelper {
    private static List<ITextComponent> messages = Lists.newArrayList();
    
    public static void log(long gameTime, int dimensionID, String msg, BlockPos pos)
    {
        
        TileEntityListLogHelper.messages.add(Messenger.c(
                "g [" + gameTime + "] ",
                "w " + "TE" + " ",
                "t " + msg + " ",
                Messenger.tp("w", pos),
                "g  at ", 
                "y " + BlockUpdatesLogHelper.getTickStage() + " ",
                "g in ",
                "e " + BlockUpdatesLogHelper.getDimension(dimensionID)
                ));
    }

    public static void log(long gameTime, int dimensionID, String msg, TileEntity te)
    {
        TileEntityListLogHelper.log(gameTime, dimensionID, msg, te.getPos());
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
                iterator.remove();
                comp.add(message);
            }
            return comp.toArray(new ITextComponent[0]);
        });
    }
    
}
