package carpet.microtick.tickstages;

import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

public class StringTickStage implements TickStage
{
	private final String info;

	public StringTickStage(String info)
	{
		this.info = info;
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.s(this.info);
	}
}
