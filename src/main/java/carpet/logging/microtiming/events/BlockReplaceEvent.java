package carpet.logging.microtiming.events;

import carpet.logging.microtiming.enums.EventType;
import carpet.logging.microtiming.utils.MicroTimingUtil;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class BlockReplaceEvent extends AbstractSetblockStateEvent
{
	public BlockReplaceEvent(EventType eventType, IBlockState oldBlockState, IBlockState newBlockState, Boolean returnValue, int flags)
	{
		super(eventType, "block_replace", oldBlockState, newBlockState, returnValue, flags);
	}

	@Override
	public ITextComponent toText()
	{
		List<Object> list = Lists.newArrayList();
		list.add(this.getEnclosedTranslatedBlockNameHeaderText(this.oldBlockState.getBlock()));
		ITextComponent titleText = Messenger.fancy(
				null,
				Messenger.c(COLOR_ACTION + tr("Block Replace")),
				this.getFlagsText(),
				null
		);
		ITextComponent infoText = Messenger.c(
				Messenger.block(this.oldBlockState),
				"g ->",
				Messenger.block(this.newBlockState)
		);
		if (this.getEventType() != EventType.ACTION_END)
		{
			list.add(Messenger.c(titleText, "g : ", infoText));
		}
		else
		{
			list.add(Messenger.fancy(
					"w",
					Messenger.c(titleText, Messenger.getSpaceText(), COLOR_RESULT + tr("finished")),
					infoText,
					null
			));
		}
		if (this.returnValue != null)
		{
			list.add("w  ");
			list.add(MicroTimingUtil.getSuccessText(this.returnValue, true));
		}
		return Messenger.c(list.toArray(new Object[0]));
	}
}
