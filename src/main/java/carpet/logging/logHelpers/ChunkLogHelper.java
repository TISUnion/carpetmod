package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.logging.microtiming.MicroTimingAccess;
import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.logging.microtiming.enums.TickStage;
import carpet.logging.microtiming.tickphase.TickPhase;
import carpet.utils.Messenger;
import carpet.utils.deobfuscator.StackTracePrinter;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ChunkLogHelper
{
    public static void onChunkNewState(World worldIn, int x, int z, String state)
    {
        TickPhase tickPhase = MicroTimingAccess.getTickPhase(worldIn);
        LoggerRegistry.getLogger("chunkdebug").log(option -> {
            ITextComponent text = Messenger.c(
                    "g [" + worldIn.getGameTime() + "] ",
                    "w X:" + x + " ",
                    "w Z:" + z + " ",
                    state + " ",
                    "g at ",
                    tickPhase.toText("y"),
                    "g  in ",
                    Messenger.dimension(worldIn).applyTextStyle(TextFormatting.DARK_GREEN),
                    "w  ",
                    StackTracePrinter.create().ignore(ChunkLogHelper.class).deobfuscate().toSymbolText()
            );
            if (option != null)
            {
                try
                {
                    Pattern p = Pattern.compile(option);
                    Matcher m = p.matcher(text.getString());
                    if (!m.find())
                    {
                        return null;
                    }
                }
                catch (PatternSyntaxException ignored)
                {
                }
            }
            return new ITextComponent[]{text};
        });
    }
}
