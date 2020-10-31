package carpet.microtiming.tickstages;

import carpet.microtiming.MicroTimingLoggerManager;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

public class StringTickStageExtra extends TickStageExtraBase
{
	public static final StringTickStageExtra SYNC_TASKS = new StringTickStageExtra("SyncTasks including player actions", "sync_tasks");
	public static final StringTickStageExtra ENTITY_WEATHER_EFFECT = new StringTickStageExtra("Ticking weather effects", "entity_weather_effect");
	public static final StringTickStageExtra ENTITY_REMOVING = new StringTickStageExtra("Removing entities", "entity_removing");
	public static final StringTickStageExtra ENTITY_PLAYER = new StringTickStageExtra("Ticking player entities", "entity_regular");
	public static final StringTickStageExtra ENTITY_REGULAR = new StringTickStageExtra("Ticking regular entities", "entity_regular");
	public static final StringTickStageExtra ADD_PENDING_BLOCK_ENTITIES = new StringTickStageExtra("Adding pending block entities", "add_pending_block_entities");
	private final String info;
	private final String translationKey;

	public StringTickStageExtra(String info, String translationKey)
	{
		this.info = info;
		this.translationKey = translationKey;
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.s(MicroTimingLoggerManager.tr("stage_extra." + translationKey, info));
	}
}
