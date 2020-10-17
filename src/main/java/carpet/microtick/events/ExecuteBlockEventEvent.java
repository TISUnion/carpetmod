package carpet.microtick.events;

import carpet.microtick.enums.EventType;
import carpet.microtick.enums.PistonBlockEventType;
import carpet.microtick.utils.MicroTickUtil;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
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

	public ExecuteBlockEventEvent(EventType eventType, BlockEventData blockAction, Boolean returnValue)
	{
		super(eventType, "execute_block_event");
		this.blockAction = blockAction;
		this.returnValue = returnValue;
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
			builder.append(String.format(" (%s)", MicroTickUtil.getFormattedDirectionString(EnumFacing.byIndex(eventParam))));
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
			list.add(MicroTickUtil.getSpaceText());
			list.add(COLOR_TARGET + PistonBlockEventType.byId(blockAction.getEventID()));
		}
		else
		{
			list.add(MicroTickUtil.getSpaceText());
			list.add(COLOR_TARGET + this.tr("BlockEvent"));
		}
		list.add(getMessageExtraMessengerHoverText(blockAction));
		if (returnValue != null)
		{
			list.add("w  ");
			list.add(MicroTickUtil.getSuccessText(this.returnValue, true));
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
		if (quitEvent instanceof ExecuteBlockEventEvent)
		{
			super.mergeQuitEvent(quitEvent);
			this.returnValue = ((ExecuteBlockEventEvent)quitEvent).returnValue;
		}
	}
}
