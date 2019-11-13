package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

public class ChunkLogHelper {

    public static void onChunkNewState(String dimname, int chunkx, int chunkz, String state, long gameTime){

        LoggerRegistry.getLogger("chunkdebug").log( () -> new ITextComponent[]{
                Messenger.c(
                        "g [" + gameTime + "] ",
                        "w X:" + chunkx + " ", 
                        "w Z:" + chunkz + " ", 
                        state + " ",
                        "g at ", 
                        "y " + BlockUpdatesLogHelper.getTickStage(),
                        "g  in ",
                        "e " + dimname
                        )});
    }

}