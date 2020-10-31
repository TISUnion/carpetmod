package carpet.microtiming.tickstages;

import carpet.utils.Messenger;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;

public class PlayerActionTickStageExtra extends PlayerRelatedTickStageExtra
{
	public PlayerActionTickStageExtra(EntityPlayerMP player)
	{
		super(player);
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				String.format("w %s\n", this.tr("sync_tasks", "Sync task executions in main thread including player actions")),
				super.toText()
		);
	}
}
