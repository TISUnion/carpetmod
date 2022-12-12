package carpet.helpers;

import carpet.settings.CarpetSettings;
import carpet.utils.CommandUtil;
import carpet.utils.Messenger;
import carpet.utils.Translator;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.event.ClickEvent;

public class StopCommandDoubleConfirmation
{
	private static final Translator translator = new Translator("command.stop.double_confirmation");
	private static final Object LOCK = new Object();
	private static final long CONFIRM_WAIT_DURATION_MS = 60 * 1000;  // 60s
	private static long previousExecuteMs = -1;

	/**
	 * @return if the /stop command should be cancelled
	 */
	public static boolean handleDoubleConfirmation(CommandContext<CommandSource> commandContext)
	{
		if (!CarpetSettings.stopCommandDoubleConfirmation)
		{
			return false;
		}

		// only apply this double command on player
		if (!CommandUtil.isPlayerCommandSource(commandContext.getSource()))
		{
			return false;
		}

		long currentTimeMs = System.currentTimeMillis();
		synchronized (LOCK)
		{
			if (previousExecuteMs > 0 && currentTimeMs - previousExecuteMs <= CONFIRM_WAIT_DURATION_MS)
			{
				// double confirmed, do the /stop
				return false;
			}

			// 1st time or confirmation timeout
			previousExecuteMs = currentTimeMs;
		}

		Messenger.tell(commandContext.getSource(), Messenger.fancy(
				translator.advTr("message", "Execute this command again to confirm the shutdown of the server"),
				translator.advTr("hover_hint", "Double confirmation provided by rule stopCommandDoubleConfirmation to prevent unintended activation"),
				new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/carpet stopCommandDoubleConfirmation")
		), true);

		return true;
	}
}
