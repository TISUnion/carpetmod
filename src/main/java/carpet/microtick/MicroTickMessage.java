package carpet.microtick;

import carpet.microtick.tickstages.TickStage;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.dimension.DimensionType;

import java.util.Arrays;
import java.util.List;

public class MicroTickMessage
{
	public final int dimensionID;
	public final BlockPos pos;
	public final EnumDyeColor color;
	public final String stage, stageDetail;
	public final TickStage stageExtra;
	public final StackTraceElement[] stackTrace;
	public final Object [] texts;

	MicroTickMessage(MicroTickLogger logger, int dimensionID, BlockPos pos, EnumDyeColor color, Object[] texts)
	{
		this.dimensionID = dimensionID;
		this.pos = pos.toImmutable();
		this.color = color;
		this.texts = texts;
		this.stage = logger.getTickStage();
		this.stageDetail = logger.getTickStageDetail();
		this.stageExtra = logger.getTickStageExtra();
		this.stackTrace = (new Exception(logger.getClass().getName())).getStackTrace();
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
		List<Object> comps = Lists.newLinkedList();
		comps.add("g at ");
		comps.add("y " + this.stage);
		if (this.stageDetail != null)
		{
			comps.add("y ." + this.stageDetail);
		}
		ITextComponent tickStageExtraText = this.stageExtra != null ? Messenger.c(this.stageExtra.toText(), "w \n"): Messenger.s("");
		ITextComponent text = Messenger.c(comps.toArray(new Object[0]));
		text.getStyle().setHoverEvent(
				new HoverEvent(
						HoverEvent.Action.SHOW_TEXT,
						Messenger.c(
								tickStageExtraText,
								"w World: ",
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
