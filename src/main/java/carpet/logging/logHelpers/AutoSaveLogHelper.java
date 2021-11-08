package carpet.logging.logHelpers;

import carpet.CarpetServer;
import carpet.logging.Logger;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

public class AutoSaveLogHelper
{
    public static void onAutoSave(long gametime, long timeCostNano)
    {
        Logger logger = LoggerRegistry.getLogger("autosave");
        logger.log((option, player) -> {
            if (option.equals("all"))
            {
                player.sendMessage(Messenger.c(String.format("g Autosave @ GameTime %d, cost %.2fms ", gametime, timeCostNano / 1e6)));
            }
            return null;
        });
    }

    public static ITextComponent [] send_hud_info()
    {
        int tickCounter = CarpetServer.minecraft_server.getTickCounter();
        int toAutoSave = 900 - tickCounter % 900;
        return new ITextComponent[]{
                Messenger.c(
                        "g Autosave in ",
                        (toAutoSave <= 60 ? "r " : "g ") + toAutoSave,
                        "g  gt"
                )
        };
    }
}
