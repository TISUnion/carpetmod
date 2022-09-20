package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.logging.microtiming.enums.TickStage;
import carpet.utils.TextUtil;
import carpet.utils.Messenger;
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
        Optional<TickStage> tickStage = MicroTimingLoggerManager.getTickStage(worldIn);
        tickStage.ifPresent(stage -> LoggerRegistry.getLogger("chunkdebug").log(option -> {
            ITextComponent[] phase = new ITextComponent[]{Messenger.s(stage.tr(), "y")};
            MicroTimingLoggerManager.getTickStageDetail(worldIn).ifPresent(detail -> {
                phase[0].appendText("." + MicroTimingLoggerManager.tr("stage_detail." + detail, detail));
            });
            MicroTimingLoggerManager.getTickStageExtra(worldIn).ifPresent(extra -> {
                phase[0] = TextUtil.getFancyText(null, phase[0], extra.toText(), extra.getClickEvent());
            });
            ITextComponent text = Messenger.c(
                    "g [" + worldIn.getGameTime() + "] ",
                    "w X:" + x + " ",
                    "w Z:" + z + " ",
                    state + " ",
                    "g at ",
                    phase[0],
                    "g  in ",
                    TextUtil.getDimensionNameText(worldIn.getDimension().getType()).applyTextStyle(TextFormatting.DARK_GREEN)
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
        }));
    }
}
