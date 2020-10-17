package carpet.microtick.events;

import carpet.microtick.enums.EventType;
import carpet.microtick.utils.MicroTickUtil;
import carpet.utils.Messenger;
import net.minecraft.block.BlockEventData;
import net.minecraft.util.text.ITextComponent;

import java.util.Objects;

public class ScheduleBlockEventEvent extends BaseEvent
{
	private final BlockEventData blockAction;

	public ScheduleBlockEventEvent(BlockEventData blockAction)
	{
		super(EventType.EVENT, "schedule_block_event");
		this.blockAction = blockAction;
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				this.getEnclosedTranslatedBlockNameHeaderText(blockAction.getBlock()),
				COLOR_ACTION + this.tr("Scheduled"),
				MicroTickUtil.getSpaceText(),
				COLOR_TARGET + this.tr("BlockEvent"),
				ExecuteBlockEventEvent.getMessageExtraMessengerHoverText(blockAction)
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
