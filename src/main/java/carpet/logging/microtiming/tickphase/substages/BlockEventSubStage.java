package carpet.logging.microtiming.tickphase.substages;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import net.minecraft.block.BlockEventData;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;

public class BlockEventSubStage extends AbstractSubStage
{
	private final World world;
	private final BlockEventData blockEventData;
	private final int order;
	private final int depth;

	public BlockEventSubStage(World world, BlockEventData blockEventData, int order, int depth)
	{
		this.world = world;
		this.blockEventData = blockEventData;
		this.order = order;
		this.depth = depth;
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				Messenger.s(MicroTimingLoggerManager.tr("Block")), "w : ", Messenger.block(this.blockEventData.getBlock()), Messenger.newLine(),
				Messenger.s(MicroTimingLoggerManager.tr("Order")), String.format("w : %d\n", this.order),
				Messenger.s(MicroTimingLoggerManager.tr("Depth")), String.format("w : %d\n", this.depth),
				Messenger.s(MicroTimingLoggerManager.tr("Position")), String.format("w : %s", TextUtil.coord(this.blockEventData.getPosition()))
		);
	}

	@Override
	public ClickEvent getClickEvent()
	{
		return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.tp(this.blockEventData.getPosition(), this.world.getDimension().getType()));
	}
}
