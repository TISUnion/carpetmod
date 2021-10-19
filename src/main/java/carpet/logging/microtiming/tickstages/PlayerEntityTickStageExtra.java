package carpet.logging.microtiming.tickstages;

import carpet.utils.Messenger;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;

public class PlayerEntityTickStageExtra extends PlayerRelatedTickStageExtra
{
	public PlayerEntityTickStageExtra(EntityPlayerMP player)
	{
		super(player);
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				String.format("w %s\n", this.tr("Ticking player entity")),
				super.toText()
		);
	}
}
