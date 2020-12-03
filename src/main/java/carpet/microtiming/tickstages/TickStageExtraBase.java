package carpet.microtiming.tickstages;

import carpet.utils.ToTextAble;
import net.minecraft.util.text.event.ClickEvent;

public abstract class TickStageExtraBase implements ToTextAble
{
	public String tr(String key, String text)
	{
		return text;
	}

	public String tr(String text)
	{
		return text;
	}

	public ClickEvent getClickEvent()
	{
		return null;
	}
}
