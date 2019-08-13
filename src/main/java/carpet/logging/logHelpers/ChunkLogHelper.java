package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

public class ChunkLogHelper {

    public static void onChunkNewState(String dimname, int chunkx, int chunkz, String state){

        LoggerRegistry.getLogger("chunkdebug").log( () -> new ITextComponent[]{
                Messenger.c("t " + dimname + " ", "w X:" + chunkx + " ", "w Z:" + chunkz + " ", state)});
    }

}