package carpet.microtick.events;

import carpet.microtick.MicroTickLoggerManager;
import carpet.microtick.enums.EventType;
import carpet.microtick.utils.MicroTickUtil;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

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

	@Override
	public ITextComponent toText()
	{
		List<Object> list = Lists.newArrayList();
		list.add(this.getEnclosedTranslatedBlockNameHeaderText(this.block));
		list.add(COLOR_ACTION + this.tr("Emit"));
		list.add(MicroTickUtil.getSpaceText());
		list.add(COLOR_TARGET + this.tr("Updates"));
		if (this.methodName != null)
		{
			list.add(String.format("^w %s: %s", this.tr("method_name", "Method name (yarn)"), this.methodName));
		}
		switch (this.getEventType())
		{
			case ACTION_START:
				list.add(MicroTickUtil.getSpaceText());
				list.add(COLOR_RESULT + MicroTickLoggerManager.tr("started"));
				break;
			case ACTION_END:
				list.add(MicroTickUtil.getSpaceText());
				list.add(COLOR_RESULT + MicroTickLoggerManager.tr("ended"));
				break;
			default:
				break;
		}
		return Messenger.c(list.toArray(new Object[0]));
	}
}
