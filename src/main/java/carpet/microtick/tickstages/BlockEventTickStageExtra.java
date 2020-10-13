package carpet.microtick.tickstages;

import carpet.microtick.MicroTickUtil;
import carpet.utils.Messenger;
import net.minecraft.block.BlockEventData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class BlockEventTickStageExtra implements TickStage
{
	private final BlockEventData blockEventData;
	private final int order;
	private final int depth;

	public BlockEventTickStageExtra(BlockEventData blockEventData, int order, int depth)
	{
		this.blockEventData = blockEventData;
		this.order = order;
		this.depth = depth;
	}

	@Override
	public ITextComponent toText()
	{
		BlockPos pos = this.blockEventData.getPosition();
		return Messenger.c(
				"w Block: ",
				MicroTickUtil.getTranslatedName(this.blockEventData.getBlock()),
				String.format("w \nOrder: %d", this.order),
				String.format("w \nDepth: %d", this.depth),
				String.format("w \nPosition: [%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ())
		);
	}
}
