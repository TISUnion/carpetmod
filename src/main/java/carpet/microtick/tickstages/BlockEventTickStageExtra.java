package carpet.microtick.tickstages;

import carpet.microtick.MicroTickLoggerManager;
import carpet.microtick.utils.MicroTickUtil;
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
				String.format("w %s: ", MicroTickLoggerManager.tr("Block")),
				MicroTickUtil.getTranslatedText(this.blockEventData.getBlock()),
				String.format("w \n%s: %d", MicroTickLoggerManager.tr("Order"), this.order),
				String.format("w \n%s: %d", MicroTickLoggerManager.tr("Depth"), this.depth),
				String.format("w \n%s: [%d, %d, %d]", MicroTickLoggerManager.tr("Position"), pos.getX(), pos.getY(), pos.getZ())
		);
	}

	@Override
	public ClickEvent getClickEvent()
	{
		return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, MicroTickUtil.getTeleportCommand(this.blockEventData.getPosition(), this.world.getDimension().getType()));
	}
}
