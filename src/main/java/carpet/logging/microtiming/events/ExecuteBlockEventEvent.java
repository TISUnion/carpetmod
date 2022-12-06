package carpet.logging.microtiming.events;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.logging.microtiming.enums.EventType;
import carpet.logging.microtiming.enums.PistonBlockEventType;
import carpet.logging.microtiming.utils.MicroTimingUtil;
import carpet.utils.TextUtil;
import carpet.utils.ToTextAble;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;

import java.util.List;
import java.util.Objects;

public class ExecuteBlockEventEvent extends BaseEvent
{
	private final BlockEventData blockAction;
	private Boolean returnValue;
	private FailInfo failInfo;

	public ExecuteBlockEventEvent(EventType eventType, BlockEventData blockAction, Boolean returnValue, FailInfo failInfo)
	{
		super(eventType, "execute_block_event");
		this.blockAction = blockAction;
		this.returnValue = returnValue;
		this.failInfo = failInfo;
		if (this.failInfo != null)
		{
			this.failInfo.setEvent(this);
		}
	}

	public static String getMessageExtraMessengerHoverText(BlockEventData blockAction)
	{
		int eventID = blockAction.getEventID();
		int eventParam = blockAction.getEventParameter();
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("^w eventID: %d", eventID));
		if (blockAction.getBlock() instanceof BlockPistonBase)
		{
			builder.append(String.format(" (%s)", PistonBlockEventType.byId(eventID)));
		}
		builder.append(String.format("\neventParam: %d", eventParam));
		if (blockAction.getBlock() instanceof BlockPistonBase)
		{
			builder.append(String.format(" (%s)", MicroTimingUtil.getFormattedDirectionString(EnumFacing.byIndex(eventParam))));
		}
		return builder.toString();
	}

	@Override
	public ITextComponent toText()
	{
		List<Object> list = Lists.newArrayList();
		list.add(this.getEnclosedTranslatedBlockNameHeaderText(blockAction.getBlock()));
		list.add(COLOR_ACTION + this.tr("Execute"));
		if (this.blockAction.getBlock() instanceof BlockPistonBase)
		{
			list.add(Messenger.getSpaceText());
			list.add(COLOR_TARGET + PistonBlockEventType.byId(blockAction.getEventID()));
		}
		else
		{
			list.add(Messenger.getSpaceText());
			list.add(COLOR_TARGET + this.tr("BlockEvent"));
		}
		list.add(getMessageExtraMessengerHoverText(this.blockAction));
		if (this.getEventType() == EventType.ACTION_END)
		{
			list.add(Messenger.getSpaceText());
			list.add(COLOR_RESULT + MicroTimingLoggerManager.tr("ended"));
		}
		if (this.returnValue != null)
		{
			list.add("w  ");
			list.add(MicroTimingUtil.getSuccessText(this.returnValue, true, this.failInfo != null && !this.returnValue ? this.failInfo.toText() : null));
		}
		return Messenger.c(list.toArray(new Object[0]));
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof ExecuteBlockEventEvent)) return false;
		if (!super.equals(o)) return false;
		ExecuteBlockEventEvent that = (ExecuteBlockEventEvent) o;
		return Objects.equals(blockAction, that.blockAction) &&
				Objects.equals(returnValue, that.returnValue);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), blockAction, returnValue);
	}

	@Override
	public void mergeQuitEvent(BaseEvent quitEvent)
	{
		super.mergeQuitEvent(quitEvent);
		if (quitEvent instanceof ExecuteBlockEventEvent)
		{
			this.returnValue = ((ExecuteBlockEventEvent)quitEvent).returnValue;
			this.failInfo = ((ExecuteBlockEventEvent)quitEvent).failInfo;
		}
	}
	public static class FailInfo implements ToTextAble
	{
		private final FailReason reason;
		private final Block actualBlock;
		private ExecuteBlockEventEvent event;

		public FailInfo(FailReason reason, Block block)
		{
			this.reason = reason;
			this.actualBlock = block;  // for custom BLOCK_CHANGED reason
		}

		public void setEvent(ExecuteBlockEventEvent event)
		{
			this.event = event;
		}

		@Override
		public ITextComponent toText()
		{
			switch (this.reason)
			{
				case BLOCK_CHANGED:
					return Messenger.c(
							"w " + this.event.tr("fail_info.block_changed", "Block has changed"),
							"w : ",
							Messenger.block(this.event.blockAction.getBlock()),
							"g  -> ",
							Messenger.block(this.actualBlock)
					);
				case EVENT_FAIL:
				default:
					return Messenger.c(
							"w " + this.event.tr("fail_info.event_fail", "Event Failed")
					);
			}
		}
	}

	public enum FailReason
	{
		BLOCK_CHANGED,
		EVENT_FAIL
	}
}
