package carpet.logging.microtiming.tickstages;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

public class LiteralTickStageExtra extends TickStageExtraBase
{
	public static final LiteralTickStageExtra ENTITY_WEATHER_EFFECT = new LiteralTickStageExtra("Ticking weather effects", "entity_weather_effect");
	public static final LiteralTickStageExtra ENTITY_REMOVING = new LiteralTickStageExtra("Removing entities", "entity_removing");
	public static final LiteralTickStageExtra ENTITY_PLAYER = new LiteralTickStageExtra("Ticking player entities", "entity_regular");
	public static final LiteralTickStageExtra ENTITY_REGULAR = new LiteralTickStageExtra("Ticking regular entities", "entity_regular");
	public static final LiteralTickStageExtra ADD_PENDING_BLOCK_ENTITIES = new LiteralTickStageExtra("Adding pending block entities", "add_pending_block_entities");
	private final String info;
	private final String translationKey;

	public LiteralTickStageExtra(String info, String translationKey)
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
