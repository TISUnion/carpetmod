package carpet.microtick.events;

import carpet.microtick.enums.EventType;
import carpet.microtick.utils.MicroTickUtil;
import carpet.microtick.utils.ToTextAble;
import carpet.microtick.utils.TranslatableBase;
import carpet.utils.Messenger;
import net.minecraft.block.Block;
import net.minecraft.util.text.ITextComponent;

import java.util.Objects;

public abstract class BaseEvent extends TranslatableBase implements ToTextAble
{
	protected static final String COLOR_ACTION = "c ";
	protected static final String COLOR_TARGET = "c ";
	protected static final String COLOR_RESULT = "q ";

	private EventType eventType;

	protected BaseEvent(EventType eventType, String translateKey)
	{
		this.eventType = eventType;
	}

	// if it's not important, it can be ignore if it's on a leaf node
	public boolean isImportant()
	{
		return true;
	}

	public EventType getEventType()
	{
		return this.eventType;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof BaseEvent)) return false;
		BaseEvent baseEvent = (BaseEvent) o;
		return eventType == baseEvent.eventType;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(eventType);
	}

	protected EventType getMergedEventType(BaseEvent quitEvent)
	{
		if (this.eventType == EventType.ACTION_START && quitEvent.eventType == EventType.ACTION_END)
		{
			return EventType.ACTION;
		}
		else
		{
			return this.eventType;
		}
	}

	public void mergeQuitEvent(BaseEvent quitEvent)
	{
		this.eventType = this.getMergedEventType(quitEvent);
	}

	protected ITextComponent getEnclosedTranslatedBlockNameHeaderText(Block block)
	{
		return Messenger.c(
				"g [",
				MicroTickUtil.getTranslatedText(block),
				"g ] "
		);
	}
}
