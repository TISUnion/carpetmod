package carpet.logging.microtiming.tickstages;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.utils.TextUtil;
import carpet.utils.Messenger;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;

public abstract class PlayerRelatedTickStageExtra extends TickStageExtraBase
{
	protected final EntityPlayerMP player;

	public PlayerRelatedTickStageExtra(EntityPlayerMP player)
	{
		this.player = player;
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				String.format("w %s: %s", MicroTimingLoggerManager.tr("Player"), this.player.getGameProfile().getName())
		);
	}

	@Override
	public ClickEvent getClickEvent()
	{
		return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.getTeleportCommand(this.player));
	}
}
