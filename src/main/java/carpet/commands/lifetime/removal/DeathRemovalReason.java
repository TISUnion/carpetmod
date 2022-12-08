package carpet.commands.lifetime.removal;

import carpet.utils.Messenger;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;

import java.util.Objects;

public class DeathRemovalReason extends RemovalReason
{
	private final String damageSourceName;

	public DeathRemovalReason(DamageSource damageSource)
	{
		this.damageSourceName = damageSource.getDamageType();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof DeathRemovalReason)) return false;
		DeathRemovalReason that = (DeathRemovalReason) o;
		return Objects.equals(this.damageSourceName, that.damageSourceName);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.damageSourceName);
	}

	@Override
	public ITextComponent toText()
	{
		return Messenger.c(
				"w " + this.tr("Death"),
				"g  (",
				Messenger.fancy(
						null,
						Messenger.s(this.damageSourceName),
						Messenger.s(this.tr("Damage source")),
						null
				),
				"g )"
		);
	}
}
