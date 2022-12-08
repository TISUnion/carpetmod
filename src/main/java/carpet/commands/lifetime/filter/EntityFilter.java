package carpet.commands.lifetime.filter;

import carpet.utils.Messenger;
import carpet.utils.TranslationContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.WorldServer;

import java.util.function.Predicate;

public class EntityFilter extends TranslationContext implements Predicate<Entity>
{
	private final EntitySelector entitySelector;
	private final CommandSource serverCommandSource;

	public EntityFilter(CommandSource serverCommandSource, EntitySelector entitySelector)
	{
		super(EntityFilterManager.getInstance().getTranslator());
		this.entitySelector = entitySelector;
		this.serverCommandSource = serverCommandSource;
	}

	private Vec3d getAnchorPos()
	{
		return this.entitySelector.getPositionGetter().apply(this.serverCommandSource.getPos());
	}

	@Override
	public boolean test(Entity testEntity)
	{
		if (testEntity == null)
		{
			return false;
		}
		if (this.entitySelector.getUsername() != null)
		{
			EntityPlayerMP serverPlayerEntity = this.serverCommandSource.getServer().getPlayerList().getPlayerByUsername(this.entitySelector.getUsername());
			return testEntity == serverPlayerEntity;
		} 
		else if (this.entitySelector.getUuid() != null) 
		{
			for (WorldServer serverWorld : this.serverCommandSource.getServer().getWorlds())
			{
				Entity entity = serverWorld.getEntityFromUuid(this.entitySelector.getUuid());
				if (testEntity == entity)
				{
					return true;
				}
			}
			return false;
		}
		Vec3d anchorPos = this.getAnchorPos();
		Predicate<Entity> predicate = this.entitySelector.invokeUpdateFilter(anchorPos);
		if (this.entitySelector.isSelf() && testEntity != this.serverCommandSource.getEntity())
		{
			return false;
		}
		if (this.entitySelector.isCurrentWorldOnly() && testEntity.getEntityWorld() != this.serverCommandSource.getWorld())
		{
			return false;
		}
		if (!this.entitySelector.getType().isAssignableFrom(testEntity.getClass()))
		{
			return false;
		}
		if (this.entitySelector.getAabb() != null && !testEntity.getBoundingBox().intersects(this.entitySelector.getAabb().offset(anchorPos)))
		{
			return false;
		}
		return predicate.test(testEntity);
	}

	public ITextComponent toText()
	{
		String inputText = this.entitySelector.getInputText();
		return Messenger.fancy(
				"y",
				Messenger.s(inputText),
				Messenger.c(
						String.format("w %s: ", this.tr("Dimension")),
						Messenger.dimension(this.serverCommandSource.getWorld().getDimension().getType()),
						String.format("w \n%s: %s", this.tr("Anchor Pos"), this.getAnchorPos())
				),
				new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, inputText)
		);
	}
}
