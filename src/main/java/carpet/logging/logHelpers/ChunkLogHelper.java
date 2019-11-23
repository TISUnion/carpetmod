package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ChunkLogHelper
{
    public static void onChunkNewState(World worldIn, int x, int z, String state)
    {
        LoggerRegistry.getLogger("chunkdebug").log( () -> new ITextComponent[]{
                Messenger.c(
                        "g [" + worldIn.getGameTime() + "] ",
                        "w X:" + x + " ",
                        "w Z:" + z + " ",
                        state + " ",
                        "g at ",
                        "y " + MicroTickLogHelper.getTickStage(),
                        "g  in ",
                        "e " + MicroTickLogHelper.getDimension(worldIn.getDimension().getType().getId())
                )});
    }
}
