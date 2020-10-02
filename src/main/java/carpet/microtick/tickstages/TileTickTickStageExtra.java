package carpet.microtick.tickstages;

import carpet.microtick.MicroTickUtil;
import carpet.utils.Messenger;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;

public class TileTickTickStageExtra implements TickStage
{
	private final NextTickListEntry<?> nextTickListEntry;
	private final int order;

	public TileTickTickStageExtra(NextTickListEntry<?> nextTickListEntry, int order)
	{
		this.nextTickListEntry = nextTickListEntry;
		this.order = order;
	}

	@Override
	public ITextComponent toText()
	{
		BlockPos pos = this.nextTickListEntry.position;
		TickPriority priority = this.nextTickListEntry.priority;
		Object target = this.nextTickListEntry.getTarget();
		ITextComponent text = Messenger.c(
				String.format("w Order: %d\n", this.order),
				String.format("w Priority: %d (%s)\n", priority.getPriority(), priority),
				String.format("w Position: [%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ())
		);
		if (target instanceof Block)
		{
			text = Messenger.c(
					"w Block: ",
					MicroTickUtil.getTranslatedName((Block)target),
					"w \n",
					text
			);
		}
		return text;
	}
}
