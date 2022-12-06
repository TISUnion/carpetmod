package carpet.logging.microtiming.tickstages;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.utils.TextUtil;
import carpet.utils.Messenger;
import net.minecraft.block.BlockEventData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;

public class BlockEventTickStageExtra extends TickStageExtraBase
{
	private final World world;
	private final BlockEventData blockEventData;
	private final int order;
	private final int depth;

	public BlockEventTickStageExtra(World world, BlockEventData blockEventData, int order, int depth)
	{
		this.world = world;
		this.blockEventData = blockEventData;
		this.order = order;
		this.depth = depth;
	}

	@Override
	public ITextComponent toText()
	{
		BlockPos pos = this.blockEventData.getPosition();
		return Messenger.c(
				String.format("w %s: ", MicroTimingLoggerManager.tr("Block")),
				Messenger.block(this.blockEventData.getBlock()),
				String.format("w \n%s: %d", MicroTimingLoggerManager.tr("Order"), this.order),
				String.format("w \n%s: %d", MicroTimingLoggerManager.tr("Depth"), this.depth),
				String.format("w \n%s: [%d, %d, %d]", MicroTimingLoggerManager.tr("Position"), pos.getX(), pos.getY(), pos.getZ())
		);
	}

	@Override
	public ClickEvent getClickEvent()
	{
		return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.tp(this.blockEventData.getPosition(), this.world.getDimension().getType()));
	}
}
