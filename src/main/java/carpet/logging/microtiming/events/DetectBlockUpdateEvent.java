package carpet.logging.microtiming.events;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.logging.microtiming.enums.BlockUpdateType;
import carpet.logging.microtiming.enums.EventType;
import carpet.utils.TextUtil;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.util.text.ITextComponent;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class DetectBlockUpdateEvent extends BaseEvent
{
	private final BlockUpdateType updateType;
	private final Supplier<String> updateTypeExtraMessage;
	private String updateTypeExtraMessageCache;
	private final Block fromBlock;

	public DetectBlockUpdateEvent(EventType eventType, Block fromBlock, BlockUpdateType blockUpdateType, Supplier<String> updateTypeExtraMessage)
	{
		super(eventType, "detect_block_update");
		this.fromBlock = fromBlock;
		this.updateType = blockUpdateType;
		this.updateTypeExtraMessage = updateTypeExtraMessage;
		this.updateTypeExtraMessageCache = null;
	}

	private String getUpdateTypeExtraMessage()
	{
		if (this.updateTypeExtraMessageCache == null)
		{
			this.updateTypeExtraMessageCache = this.updateTypeExtraMessage.get();
		}
		return this.updateTypeExtraMessageCache;
	}

	@Override
	public ITextComponent toText()
	{
		List<Object> list = Lists.newArrayList();
		list.add(this.getEnclosedTranslatedBlockNameHeaderText(this.fromBlock));
		list.add(COLOR_ACTION + MicroTimingLoggerManager.tr("Emit"));
		list.add(Messenger.getSpaceText());
		list.add(COLOR_TARGET + this.updateType);
		list.add("^w " + this.getUpdateTypeExtraMessage());
		list.add(Messenger.getSpaceText());
		switch (this.getEventType())
		{
			case ACTION_START:
				list.add(COLOR_RESULT + MicroTimingLoggerManager.tr("started"));
				break;
			case ACTION_END:
				list.add(COLOR_RESULT + MicroTimingLoggerManager.tr("ended"));
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
				Objects.equals(this.getUpdateTypeExtraMessage(), that.getUpdateTypeExtraMessage()) &&
				Objects.equals(fromBlock, that.fromBlock);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), updateType, updateTypeExtraMessage, fromBlock);
	}
}
