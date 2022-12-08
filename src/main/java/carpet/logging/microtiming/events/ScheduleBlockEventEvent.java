package carpet.logging.microtiming.events;

import carpet.logging.microtiming.enums.EventType;
import carpet.logging.microtiming.utils.MicroTimingUtil;
import carpet.utils.TextUtil;
import carpet.utils.Messenger;
import net.minecraft.block.BlockEventData;
import net.minecraft.util.text.ITextComponent;

import java.util.Objects;

public class ScheduleBlockEventEvent extends BaseEvent
{
	private final BlockEventData blockAction;
	private final boolean success;

	public ScheduleBlockEventEvent(BlockEventData blockAction, boolean success)
	{
		super(EventType.EVENT, "schedule_block_event");
		this.blockAction = blockAction;
		this.success = success;
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				this.getEnclosedTranslatedBlockNameHeaderText(blockAction.getBlock()),
				COLOR_ACTION + this.tr("Scheduled"),
				Messenger.getSpaceText(),
				COLOR_TARGET + this.tr("BlockEvent"),
				ExecuteBlockEventEvent.getMessageExtraMessengerHoverText(blockAction),
				Messenger.getSpaceText(),
				MicroTimingUtil.getSuccessText(this.success, false)
		);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof ScheduleBlockEventEvent)) return false;
		if (!super.equals(o)) return false;
		ScheduleBlockEventEvent that = (ScheduleBlockEventEvent) o;
		return Objects.equals(blockAction, that.blockAction);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), blockAction);
	}
}
