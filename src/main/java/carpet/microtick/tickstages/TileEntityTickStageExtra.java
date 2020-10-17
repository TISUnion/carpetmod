package carpet.microtick.tickstages;

import carpet.microtick.MicroTickLoggerManager;
import carpet.microtick.utils.MicroTickUtil;
import carpet.utils.Messenger;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;

public class TileEntityTickStageExtra extends TickStageExtraBase
{
	private final World world;
	private final BlockPos pos;
	private final Block block;
	private final int order;

	public TileEntityTickStageExtra(TileEntity tileEntity, int order)
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
				String.format("w %s: ", MicroTickLoggerManager.tr("Block")),
				MicroTickUtil.getTranslatedText(this.block),
				String.format("w \n%s: %d", MicroTickLoggerManager.tr("Order"), this.order),
				String.format("w \n%s: [%d, %d, %d]", MicroTickLoggerManager.tr("Position"), this.pos.getX(), this.pos.getY(), this.pos.getZ())
		);
	}

	@Override
	public ClickEvent getClickEvent()
	{
		return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, MicroTickUtil.getTeleportCommand(this.pos, this.world.getDimension().getType()));
	}
}
