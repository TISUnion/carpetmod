package carpet.microtick.events;

import carpet.microtick.MicroTickLoggerManager;
import carpet.microtick.enums.EventType;
import carpet.microtick.utils.MicroTickUtil;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.NextTickListEntry;

import java.util.List;
import java.util.Objects;

public class ExecuteTileTickEvent extends BaseEvent
{
	private final NextTickListEntry<Block> tileTickEntry;
	public ExecuteTileTickEvent(EventType eventType, NextTickListEntry<Block> tileTickEntry)
	{
		super(eventType, "execute_tile_tick");
		this.tileTickEntry = tileTickEntry;
	}

	@Override
	public ITextComponent toText()
	{
		List<Object> list = Lists.newArrayList();
		list.add(this.getEnclosedTranslatedBlockNameHeaderText(this.tileTickEntry.getTarget()));
		list.add(COLOR_ACTION + this.tr("Execute"));
		list.add(MicroTickUtil.getSpaceText());
		list.add(COLOR_TARGET + this.tr("TileTick Event"));
		if (this.getEventType() == EventType.ACTION_END)
		{
			list.add(MicroTickUtil.getSpaceText());
			list.add(COLOR_RESULT + MicroTickLoggerManager.tr("ended"));
		}
		list.add(String.format("^w %s: %d (%s)", MicroTickLoggerManager.tr("Priority"), this.tileTickEntry.priority.getPriority(), this.tileTickEntry.priority));
		return Messenger.c(list.toArray(new Object[0]));
	}


	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof ExecuteTileTickEvent)) return false;
		if (!super.equals(o)) return false;
		ExecuteTileTickEvent that = (ExecuteTileTickEvent) o;
		return Objects.equals(tileTickEntry, that.tileTickEntry);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), tileTickEntry);
	}
}
