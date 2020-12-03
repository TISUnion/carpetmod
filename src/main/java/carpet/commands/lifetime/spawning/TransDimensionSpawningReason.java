package carpet.commands.lifetime.spawning;

import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.dimension.DimensionType;

import java.util.Objects;

public class TransDimensionSpawningReason extends SpawningReason
{
	private final DimensionType oldDimension;

	public TransDimensionSpawningReason(DimensionType oldDimension)
	{
		this.oldDimension = Objects.requireNonNull(oldDimension);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TransDimensionSpawningReason that = (TransDimensionSpawningReason) o;
		return Objects.equals(this.oldDimension, that.oldDimension);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.oldDimension);
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				"w " + this.tr("trans_dimension", "Trans-dimension"),
				"g  (" + this.tr("trans_dimension.from", "from"),
				TextUtil.getSpaceText(),
				TextUtil.attachFormatting(TextUtil.getDimensionNameText(this.oldDimension), TextFormatting.GRAY),
				"g )"
		);
	}
}
