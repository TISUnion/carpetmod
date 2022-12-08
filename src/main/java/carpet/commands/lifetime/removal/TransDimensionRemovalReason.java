package carpet.commands.lifetime.removal;

import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.dimension.DimensionType;

import java.util.Objects;

public class TransDimensionRemovalReason extends RemovalReason
{
	private final DimensionType newDimension;

	public TransDimensionRemovalReason(DimensionType newDimension)
	{
		this.newDimension = Objects.requireNonNull(newDimension);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TransDimensionRemovalReason that = (TransDimensionRemovalReason) o;
		return Objects.equals(this.newDimension, that.newDimension);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.newDimension);
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				"w " + this.tr("trans_dimension", "Trans-dimension"),
				"g  (" + this.tr("trans_dimension.to", "to"),
				Messenger.getSpaceText(),
				Messenger.formatting(Messenger.dimension(this.newDimension), TextFormatting.GRAY),
				"g )"
		);
	}
}
