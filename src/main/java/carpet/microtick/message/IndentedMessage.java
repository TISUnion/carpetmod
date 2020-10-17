package carpet.microtick.message;

import carpet.microtick.utils.ToTextAble;
import net.minecraft.util.text.ITextComponent;

public class IndentedMessage implements ToTextAble
{
	private final MicroTickMessage message;
	private final int indentation;

	public IndentedMessage(MicroTickMessage message, int indentation)
	{
		this.message = message;
		this.indentation = indentation;
	}

	public MicroTickMessage getMessage()
	{
		return this.message;
	}

	@Override
	public ITextComponent toText()
	{
		return this.message.toText(this.indentation, this.indentation == 0);
	}
}
