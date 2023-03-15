package carpet.logging.instantfall;

import net.minecraft.block.BlockFalling;

public class InstantFallFlagWatcher
{
	private static final InstantFallFlagWatcher INSTANCE = new InstantFallFlagWatcher();

	private boolean previousFlag;

	private InstantFallFlagWatcher()
	{
		this.previousFlag = BlockFalling.fallInstantly;
	}

	public static InstantFallFlagWatcher getInstance() {
		return INSTANCE;
	}

	public void tick()
	{
		boolean currentFlag = BlockFalling.fallInstantly;
		if (currentFlag != this.previousFlag)
		{
			InstantFallLogger.getInstance().onInstantFallFlagFlipped(currentFlag);
		}
		this.previousFlag = currentFlag;
	}
}
