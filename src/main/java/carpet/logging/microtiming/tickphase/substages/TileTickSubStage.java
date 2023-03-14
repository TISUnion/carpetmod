package carpet.logging.microtiming.tickphase.substages;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;

import java.util.List;

public class TileTickSubStage extends AbstractSubStage
{
	private final World world;
	private final NextTickListEntry<?> nextTickListEntry;
	private final int order;

	public TileTickSubStage(World world, NextTickListEntry<?> nextTickListEntry, int order)
	{
		this.world = world;
		this.nextTickListEntry = nextTickListEntry;
		this.order = order;
	}

	@Override
	public ITextComponent toText()
	{
		BlockPos pos = this.nextTickListEntry.position;
		TickPriority priority = this.nextTickListEntry.priority;
		Object target = this.nextTickListEntry.getTarget();
		List<Object> list = Lists.newArrayList();

		if (target instanceof Block)
		{
			list.add(Messenger.c(Messenger.s(MicroTimingLoggerManager.tr("Block")), "w : ", Messenger.block((Block)target)));
		}
		else if (target instanceof Fluid)
		{
			list.add(Messenger.c(Messenger.s(MicroTimingLoggerManager.tr("Fluid")), "w : ", Messenger.fluid((Fluid)target)));
		}
		list.add(Messenger.newLine());

		list.add(Messenger.c(Messenger.s(MicroTimingLoggerManager.tr("Order")), String.format("w : %d\n", this.order)));
		list.add(Messenger.c(Messenger.s(MicroTimingLoggerManager.tr("Priority")), String.format("w : %d (%s)\n", priority.getPriority(), priority)));
		list.add(Messenger.c(Messenger.s(MicroTimingLoggerManager.tr("Position")), String.format("w : %s", TextUtil.coord(pos))));

		return Messenger.c(list.toArray(new Object[0]));
	}

	@Override
	public ClickEvent getClickEvent()
	{
		BlockPos pos = this.nextTickListEntry.position;
		return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.tp(pos, this.world.getDimension().getType()));
	}
}
