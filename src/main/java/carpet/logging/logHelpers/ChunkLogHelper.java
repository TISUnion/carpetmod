package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.microtiming.MicroTimingLoggerManager;
import carpet.microtiming.enums.TickStage;
import carpet.microtiming.utils.MicroTimingUtil;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Optional;

public class ChunkLogHelper
{
    public static void onChunkNewState(World worldIn, int x, int z, String state)
    {
        Optional<TickStage> tickStage = MicroTimingLoggerManager.getTickStage(worldIn);
        tickStage.ifPresent(stage -> LoggerRegistry.getLogger("chunkdebug").log(() -> new ITextComponent[]{
                Messenger.c(
                        "g [" + worldIn.getGameTime() + "] ",
                        "w X:" + x + " ",
                        "w Z:" + z + " ",
                        state + " ",
                        "g at ",
                        "y " + stage,
                        "g  in ",
                        MicroTimingUtil.getDimensionNameText(worldIn.getDimension().getType()).applyTextStyle(TextFormatting.DARK_GREEN)
                )}));
    }
}
