package carpet.logging.microtiming.tickphase.substages;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.utils.TranslationContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;

public abstract class AbstractSubStage extends TranslationContext
{
	public AbstractSubStage()
	{
		super(MicroTimingLoggerManager.TRANSLATOR.getDerivedTranslator("sub_stage"));
	}

	public ClickEvent getClickEvent()
	{
		return null;
	}

	public abstract ITextComponent toText();
}
