package carpet.microtiming.tickstages;

import carpet.microtiming.MicroTimingLoggerManager;
import carpet.microtiming.utils.MicroTimingUtil;
import carpet.microtiming.utils.TextUtil;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;

import java.util.List;

public class TileTickTickStageExtra extends TickStageExtraBase
{
	private final World world;
	private final NextTickListEntry<?> nextTickListEntry;
	private final int order;

	public TileTickTickStageExtra(World world, NextTickListEntry<?> nextTickListEntry, int order)
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
			list.add(String.format("w %s: ", MicroTimingLoggerManager.tr("Block")));
			list.add(MicroTimingUtil.getTranslatedText((Block)target));
			list.add("w \n");
		}
		list.add(String.format("w %s: %d\n", MicroTimingLoggerManager.tr("Order"), this.order));
		list.add(String.format("w %s: %d (%s)\n", MicroTimingLoggerManager.tr("Priority"), priority.getPriority(), priority));
		list.add(String.format("w %s: [%d, %d, %d]", MicroTimingLoggerManager.tr("Position"), pos.getX(), pos.getY(), pos.getZ()));
		return Messenger.c(list.toArray(new Object[0]));
	}

	@Override
	public ClickEvent getClickEvent()
	{
		return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.getTeleportCommand(this.nextTickListEntry.position, this.world.getDimension().getType()));
	}
}
