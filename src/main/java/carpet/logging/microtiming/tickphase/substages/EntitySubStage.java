package carpet.logging.microtiming.tickphase.substages;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;

public class EntitySubStage extends AbstractSubStage
{
	private final Entity entity;
	private final int order;
	private final Vec3d pos;

	public EntitySubStage(Entity entity, int order)
	{
		this.entity = entity;
		this.order = order;
		this.pos = entity.getPositionVector();
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				Messenger.s(MicroTimingLoggerManager.tr("Entity")), "w : ", this.entity.getDisplayName(), "w \n",
				Messenger.s(MicroTimingLoggerManager.tr("Type")), "w : ", Messenger.entityType(this.entity), "w \n",
				Messenger.s(MicroTimingLoggerManager.tr("Order")), String.format("w : %d\n", this.order),
				Messenger.s(MicroTimingLoggerManager.tr("Position")), String.format("w : %s", TextUtil.coord(this.pos))
		);
	}

	@Override
	public ClickEvent getClickEvent()
	{
		return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.tp(this.entity));
	}
}
