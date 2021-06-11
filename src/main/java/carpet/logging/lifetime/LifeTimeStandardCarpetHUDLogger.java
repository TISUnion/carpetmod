package carpet.logging.lifetime;

import carpet.commands.lifetime.LifeTimeTracker;
import carpet.commands.lifetime.utils.LifeTimeTrackerUtil;
import carpet.logging.HUDLogger;
import net.minecraft.entity.player.EntityPlayer;

public class LifeTimeStandardCarpetHUDLogger extends HUDLogger
{
	public LifeTimeStandardCarpetHUDLogger()
	{
		super(LifeTimeHUDLogger.NAME, null, null);
	}

	@Override
	public void addPlayer(String playerName, String option)
	{
		super.addPlayer(playerName, option);
		EntityPlayer player = this.playerFromName(playerName);
		if (player != null)
		{
			if (!LifeTimeTrackerUtil.getEntityTypeFromName(option).isPresent())
			{
				LifeTimeTracker.getInstance().sendUnknownEntity(player.getCommandSource(), option);
			}
		}
	}

	@Override
	public String[] getOptions()
	{
		return LifeTimeTracker.getInstance().getAvailableEntityType().toArray(String[]::new);
	}
}
