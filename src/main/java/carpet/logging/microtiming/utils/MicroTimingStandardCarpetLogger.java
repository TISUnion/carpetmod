package carpet.logging.microtiming.utils;

import carpet.logging.Logger;
import carpet.logging.microtiming.MicroTimingLogger;
import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.logging.microtiming.marker.MicroTimingMarkerManager;
import carpet.utils.Messenger;
import carpet.utils.Translator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Arrays;

public class MicroTimingStandardCarpetLogger extends Logger
{
	public static final String NAME = MicroTimingLogger.NAME;
	private static final MicroTimingStandardCarpetLogger INSTANCE = create();
	private static final Translator translator = new Translator("logger", NAME + ".carpet_logger");

	public MicroTimingStandardCarpetLogger(String logName, String def, String[] options)
	{
		super(logName, def, options);
	}

	public static MicroTimingStandardCarpetLogger getInstance()
	{
		return INSTANCE;
	}

	private static MicroTimingStandardCarpetLogger create()
	{
		String def = MicroTimingLogger.LoggingOption.DEFAULT.toString();
		String[] options = Arrays.stream(MicroTimingLogger.LoggingOption.values()).map(MicroTimingLogger.LoggingOption::toString).map(String::toLowerCase).toArray(String[]::new);
		return new MicroTimingStandardCarpetLogger(NAME, def, options);
	}

	@Override
	public void addPlayer(String playerName, String option)
	{
		super.addPlayer(playerName, option);
		EntityPlayer player = this.playerFromName(playerName);
		if (player instanceof EntityPlayerMP && !MicroTimingLoggerManager.isLoggerActivated())
		{
			String command = "/carpet microTiming true";
			Messenger.tell(player, Messenger.c(
					"w " + String.format(translator.tr("rule_hint", "Use command %s to start logging"), command),
					"?" + command,
					"^w " + translator.tr("Click to execute")
			));
			MicroTimingMarkerManager.getInstance().sendAllMarkersForPlayer((EntityPlayerMP) player);
		}
	}

	@Override
	public void removePlayer(String playerName)
	{
		super.removePlayer(playerName);
		EntityPlayer player = this.playerFromName(playerName);
		if (player instanceof EntityPlayerMP)
		{
			MicroTimingMarkerManager.getInstance().cleanAllMarkersForPlayer((EntityPlayerMP) player);
		}
	}

	public void onCarpetClientHello(EntityPlayerMP player)
	{
		if (MicroTimingUtil.isPlayerSubscribed(player))
		{
			MicroTimingMarkerManager.getInstance().sendAllMarkersForPlayer(player);
		}
	}
}
