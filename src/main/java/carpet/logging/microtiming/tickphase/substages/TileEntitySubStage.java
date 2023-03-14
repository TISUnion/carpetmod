package carpet.logging.microtiming.tickphase.substages;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;

public class TileEntitySubStage extends AbstractSubStage
{
	private final World world;
	private final BlockPos pos;
	private final Block block;
	private final int order;

	public TileEntitySubStage(TileEntity tileEntity, int order)
	{
		this.world = tileEntity.getWorld();
		this.pos = tileEntity.getPos().toImmutable();
		this.block = tileEntity.getBlockState().getBlock();
		this.order = order;
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				Messenger.s(MicroTimingLoggerManager.tr("Block")), "w : ", Messenger.block(this.block), Messenger.newLine(),
				Messenger.s(MicroTimingLoggerManager.tr("Order")), String.format("w : %d\n", this.order),
				Messenger.s(MicroTimingLoggerManager.tr("Position")), String.format("w : %s", TextUtil.coord(this.pos))
		);
	}

	@Override
	public ClickEvent getClickEvent()
	{
		return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.tp(this.pos, this.world.getDimension().getType()));
	}
}
