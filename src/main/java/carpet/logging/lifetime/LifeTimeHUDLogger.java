package carpet.logging.lifetime;

import carpet.commands.lifetime.LifeTimeTracker;
import carpet.commands.lifetime.LifeTimeWorldTracker;
import carpet.commands.lifetime.trackeddata.BasicTrackedData;
import carpet.commands.lifetime.utils.LifeTimeTrackerUtil;
import carpet.logging.AbstractHUDLogger;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Optional;

/**
 * Independent of lifetime tracker
 * It only reads some data from the tracker
 */
public class LifeTimeHUDLogger extends AbstractHUDLogger
{
	public static final String NAME = "lifeTime";

	private static final LifeTimeHUDLogger INSTANCE = new LifeTimeHUDLogger();

	public LifeTimeHUDLogger()
	{
		super(NAME);
	}

	public static LifeTimeHUDLogger getInstance()
	{
		return INSTANCE;
	}

	@Override
	public ITextComponent[] onHudUpdate(String option, EntityPlayer playerEntity)
	{
		LifeTimeWorldTracker tracker = LifeTimeTracker.getInstance().getTracker(playerEntity.getEntityWorld());
		if (tracker != null)
		{
			Optional<EntityType<?>> entityTypeOptional = LifeTimeTrackerUtil.getEntityTypeFromName(option);
			if (entityTypeOptional.isPresent())
			{
				EntityType<?> entityType = entityTypeOptional.get();
				BasicTrackedData data = tracker.getDataMap().getOrDefault(entityType, new BasicTrackedData());
				return new ITextComponent[]{Messenger.c(
						Messenger.formatting(Messenger.copy(entityType.getName()), TextFormatting.GRAY),
						"g : ",
						"e " + data.getSpawningCount(),
						"g /",
						"r " + data.getRemovalCount(),
						"w  ",
						data.lifeTimeStatistic.getCompressedResult(false)
				)};
			}
		}
		return null;
	}

	public LifeTimeStandardCarpetHUDLogger getHUDLogger()
	{
		return new LifeTimeStandardCarpetHUDLogger();
	}
}
