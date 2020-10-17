package carpet.microtick.tickstages;

import carpet.microtick.utils.ToTextAble;
import net.minecraft.util.text.event.ClickEvent;

public abstract class TickStageExtraBase implements ToTextAble
{
	public ClickEvent getClickEvent()
	{
		return null;
	}
}
