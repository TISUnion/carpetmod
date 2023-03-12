package carpet.logging;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;

public abstract class AbstractHUDLogger extends AbstractLogger
{
	public AbstractHUDLogger(String name)
	{
		super(name);
	}

	public abstract ITextComponent[] onHudUpdate(String option, EntityPlayer playerEntity);

	@Override
	public HUDLogger createCarpetLogger()
	{
		return new HUDLogger(
				this.getName(),
				wrapOption(this.getDefaultLoggingOption()),
				wrapOptions(this.getSuggestedLoggingOption())
		);
	}
}
