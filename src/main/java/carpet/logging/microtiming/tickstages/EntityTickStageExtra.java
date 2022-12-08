package carpet.logging.microtiming.tickstages;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.utils.TextUtil;
import carpet.utils.Messenger;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;

public class EntityTickStageExtra extends TickStageExtraBase
{
	private final Entity entity;
	private final int order;
	private final Vec3d pos;

	public EntityTickStageExtra(Entity entity, int order)
	{
		this.entity = entity;
		this.order = order;
		this.pos = entity.getPositionVector();
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				String.format("w %s: ", MicroTimingLoggerManager.tr("Entity")),
				this.entity.getDisplayName(),
				String.format("w \n%s: ", MicroTimingLoggerManager.tr("Type")),
				this.entity.getType().getName(),
				String.format("w \n%s: %d", MicroTimingLoggerManager.tr("Order"), this.order),
				String.format("w \n%s: [%.2f, %.2f, %.2f]", MicroTimingLoggerManager.tr("Position"), this.pos.x, this.pos.y, this.pos.z)
		);
	}

	@Override
	public ClickEvent getClickEvent()
	{
		return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.tp(this.entity));
	}
}
