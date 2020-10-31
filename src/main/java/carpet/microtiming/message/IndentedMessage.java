package carpet.microtiming.message;

import carpet.microtiming.utils.ToTextAble;
import net.minecraft.util.text.ITextComponent;

public class IndentedMessage implements ToTextAble
{
	private final MicroTimingMessage message;
	private final int indentation;

	public IndentedMessage(MicroTimingMessage message, int indentation)
	{
		this.message = message;
		this.indentation = indentation;
	}

	public MicroTimingMessage getMessage()
	{
		return this.message;
	}

	public int getIndentation()
	{
		return indentation;
	}

	@Override
	public ITextComponent toText()
	{
		return this.message.toText(this.indentation, this.indentation == 0);
	}
}
