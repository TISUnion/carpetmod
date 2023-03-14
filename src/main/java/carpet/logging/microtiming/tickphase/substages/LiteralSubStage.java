package carpet.logging.microtiming.tickphase.substages;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

public class LiteralSubStage extends AbstractSubStage
{
	public static final LiteralSubStage ENTITY_WEATHER_EFFECT = new LiteralSubStage("Ticking weather effects", "entity_weather_effect");
	public static final LiteralSubStage ENTITY_REMOVING = new LiteralSubStage("Removing entities", "entity_removing");
	public static final LiteralSubStage ENTITY_PLAYER = new LiteralSubStage("Ticking player entities", "entity_regular");
	public static final LiteralSubStage ENTITY_REGULAR = new LiteralSubStage("Ticking regular entities", "entity_regular");
	public static final LiteralSubStage ADD_PENDING_BLOCK_ENTITIES = new LiteralSubStage("Adding pending block entities", "add_pending_block_entities");
	private final String info;
	private final String translationKey;

	public LiteralSubStage(String info, String translationKey)
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
