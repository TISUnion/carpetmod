package carpet.logging.microtiming.events;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.logging.microtiming.enums.EventType;
import carpet.utils.TextUtil;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.List;
import java.util.Objects;

public class EmitBlockUpdateEvent extends BaseEvent
{
	private final Block block;
	private final String methodName;

	public EmitBlockUpdateEvent(EventType eventType, Block block, String methodName)
	{
		super(eventType, "emit_block_update");
		this.block = block;
		this.methodName = methodName;
	}

	@Override
	public boolean isImportant()
	{
		return false;
	}

	protected ITextComponent getUpdatesTextHoverText()
	{
		return Messenger.s(String.format("%s: %s", this.tr("method_name", "Method name (yarn)"), this.methodName));
	}

	@Override
	public ITextComponent toText()
	{
		List<Object> list = Lists.newArrayList();
		list.add(this.getEnclosedTranslatedBlockNameHeaderText(this.block));
		ITextComponent updatesText = Messenger.c(
				COLOR_ACTION + this.tr("Emit"),
				Messenger.getSpaceText(),
				COLOR_TARGET + this.tr("Updates")
		);
		if (this.methodName != null)
		{
			updatesText.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, this.getUpdatesTextHoverText()));
		}
		list.add(updatesText);
		switch (this.getEventType())
		{
			case ACTION_START:
				list.add(Messenger.getSpaceText());
				list.add(COLOR_RESULT + MicroTimingLoggerManager.tr("started"));
				break;
			case ACTION_END:
				list.add(Messenger.getSpaceText());
				list.add(COLOR_RESULT + MicroTimingLoggerManager.tr("ended"));
				break;
			default:
				break;
		}
		return Messenger.c(list.toArray(new Object[0]));
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof EmitBlockUpdateEvent)) return false;
		if (!super.equals(o)) return false;
		EmitBlockUpdateEvent that = (EmitBlockUpdateEvent) o;
		return Objects.equals(block, that.block) &&
				Objects.equals(methodName, that.methodName);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), block, methodName);
	}
}
