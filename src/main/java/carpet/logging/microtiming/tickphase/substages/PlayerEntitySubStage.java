package carpet.logging.microtiming.tickphase.substages;

import carpet.utils.Messenger;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;

public class PlayerEntitySubStage extends AbstractPlayerRelatedSubStage
{
	public PlayerEntitySubStage(EntityPlayerMP player)
	{
		super(player);
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				Messenger.s(tr("Ticking player entity")), Messenger.newLine(),
				super.toText()
		);
	}
}
