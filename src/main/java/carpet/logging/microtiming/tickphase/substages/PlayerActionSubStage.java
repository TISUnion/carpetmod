package carpet.logging.microtiming.tickphase.substages;

import carpet.utils.Messenger;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;

public class PlayerActionSubStage extends AbstractPlayerRelatedSubStage
{
	public PlayerActionSubStage(EntityPlayerMP player)
	{
		super(player);
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				Messenger.s(tr("player_action")), Messenger.newLine(),
				super.toText()
		);
	}
}
