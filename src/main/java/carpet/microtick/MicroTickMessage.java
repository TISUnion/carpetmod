package carpet.microtick;

import carpet.utils.Messenger;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.dimension.DimensionType;

import java.util.Arrays;

public class MicroTickMessage
{
	int dimensionID;
	BlockPos pos;
	EnumDyeColor color;
	String stage, stageDetail, stageExtra;
	StackTraceElement[] stackTrace;
	Object [] texts;

	MicroTickMessage(int dimensionID, BlockPos pos, EnumDyeColor color, Object[] texts)
	{
		this.dimensionID = dimensionID;
		this.pos = pos.toImmutable();
		this.color = color;
		this.texts = texts;
		this.stage = this.stageDetail = this.stageExtra = null;
	}

	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof MicroTickMessage))
		{
			return false;
		}

		MicroTickMessage o = (MicroTickMessage) obj;
		boolean ret = this.dimensionID == o.dimensionID && this.pos.equals(o.pos) && this.color.equals(o.color) && this.stage.equals(o.stage);
		ret |= this.texts.length == o.texts.length;
		if (!ret)
		{
			return ret;
		}
		for (int i = 0; i < this.texts.length; i++)
			if (this.texts[i] instanceof String && !this.texts[i].equals(o.texts[i]))
				return false;
		return ret;
	}

	public int hashCode()
	{
		return dimensionID + pos.hashCode() * 2 + color.hashCode();
	}

	ITextComponent getHashTag()
	{
		String text = MicroTickUtil.getColorStyle(this.color) + " # ";
		ITextComponent ret;
		if (this.pos != null)
		{
			ret = Messenger.c(
					text,
					String.format("!/execute in %s run tp @s %d %d %d", DimensionType.getById(this.dimensionID), this.pos.getX(), this.pos.getY(), this.pos.getZ()),
					String.format("^w [ %d, %d, %d ]", this.pos.getX(), this.pos.getY(), this.pos.getZ())
			);
		}
		else
		{
			ret = Messenger.c(text);
		}
		return ret;
	}

	ITextComponent getStage()
	{
		ITextComponent text = Messenger.c(
				"g at ",
				"y " + this.stage + this.stageDetail
		);
		text.getStyle().setHoverEvent(
				new HoverEvent(
						HoverEvent.Action.SHOW_TEXT,
						Messenger.c(
								String.format("w %sWorld: ", this.stageExtra),
								MicroTickUtil.getDimensionNameText(this.dimensionID)
						)
				)
		);
		return text;
	}

	ITextComponent getStackTrace()
	{
		return Messenger.c(
				"f $",
				"^w " + Arrays.toString(this.stackTrace)
		);
	}
}
