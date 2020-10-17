package carpet.microtick.events;

import carpet.microtick.MicroTickLoggerManager;
import carpet.microtick.enums.BlockUpdateType;
import carpet.microtick.enums.EventType;
import carpet.microtick.utils.MicroTickUtil;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.util.text.ITextComponent;

import java.util.List;
import java.util.Objects;

public class DetectBlockUpdateEvent extends BaseEvent
{
	private final BlockUpdateType updateType;
	private final String updateTypeExtraMessage;
	private final Block fromBlock;

	public DetectBlockUpdateEvent(EventType eventType, Block fromBlock, BlockUpdateType blockUpdateType, String updateTypeExtraMessage)
	{
		super(eventType, "detect_block_update");
		this.fromBlock = fromBlock;
		this.updateType = blockUpdateType;
		this.updateTypeExtraMessage = updateTypeExtraMessage;
	}

	@Override
	public ITextComponent toText()
	{
		List<Object> list = Lists.newArrayList();
		list.add(this.getEnclosedTranslatedBlockNameHeaderText(this.fromBlock));
		list.add(COLOR_ACTION + MicroTickLoggerManager.tr("Emit"));
		list.add(MicroTickUtil.getSpaceText());
		list.add(COLOR_TARGET + this.updateType);
		list.add("^w " + this.updateTypeExtraMessage);
		list.add(MicroTickUtil.getSpaceText());
		switch (this.getEventType())
		{
			case ACTION_START:
				list.add(COLOR_RESULT + MicroTickLoggerManager.tr("started"));
				break;
			case ACTION_END:
				list.add(COLOR_RESULT + MicroTickLoggerManager.tr("ended"));
				break;
			default:
				list.add(COLOR_RESULT + this.tr("detected"));
				break;
		}
		return Messenger.c(list.toArray(new Object[0]));
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof DetectBlockUpdateEvent)) return false;
		if (!super.equals(o)) return false;
		DetectBlockUpdateEvent that = (DetectBlockUpdateEvent) o;
		return updateType == that.updateType &&
				Objects.equals(updateTypeExtraMessage, that.updateTypeExtraMessage) &&
				Objects.equals(fromBlock, that.fromBlock);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), updateType, updateTypeExtraMessage, fromBlock);
	}
}
