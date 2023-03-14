package carpet.logging.microtiming.tickphase.substages;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;

public abstract class AbstractPlayerRelatedSubStage extends AbstractSubStage
{
	protected final EntityPlayerMP player;

	public AbstractPlayerRelatedSubStage(EntityPlayerMP player)
	{
		this.player = player;
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				Messenger.s(MicroTimingLoggerManager.tr("Player")),
				String.format("w : %s", this.player.getGameProfile().getName())
		);
	}

	@Override
	public ClickEvent getClickEvent()
	{
		return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.tp(this.player));
	}
}
