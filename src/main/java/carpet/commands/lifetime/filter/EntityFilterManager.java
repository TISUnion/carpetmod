package carpet.commands.lifetime.filter;

import carpet.commands.lifetime.LifeTimeTracker;
import carpet.commands.lifetime.utils.LifeTimeTrackerUtil;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import carpet.utils.TranslatableBase;
import com.google.common.collect.Maps;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Predicate;

public class EntityFilterManager extends TranslatableBase
{
	private static final EntityFilterManager INSTANCE = new EntityFilterManager();
	private static final Predicate<Entity> DEFAULT_FILTER = entity -> true;

	// key null is for global filter
	private final Map<EntityType<?>, Predicate<Entity>> entityFilter = Maps.newLinkedHashMap();

	public EntityFilterManager()
	{
		super(LifeTimeTracker.getInstance().getTranslator().getDerivedTranslator("filter"));
	}

	public static EntityFilterManager getInstance()
	{
		return INSTANCE;
	}

	public Predicate<Entity> getFilter(@Nullable EntityType<?> entityType)
	{
		return this.entityFilter.getOrDefault(entityType, DEFAULT_FILTER);
	}

	/**
	 * Test right before recording entity spawning
	 * So the entity should be fully initialized
	 */
	public boolean test(Entity entity)
	{
		// global filter, then specific filter
		return this.getFilter(null).test(entity) && this.getFilter(entity.getType()).test(entity);
	}

	public void setEntityFilter(CommandSource source, @Nullable EntityType<?> entityType, @Nullable EntitySelector entitySelector)
	{
		ITextComponent typeName = entityType != null ? entityType.getName() : Messenger.s(this.tr(""));
		if (entitySelector != null)
		{
			if (!entitySelector.isIncludeNonPlayers() || entitySelector.getUsername() != null)
			{
				Messenger.m(source, Messenger.s(this.tr("unsupported.0", "Unsupported entity filter")));
				Messenger.m(source, Messenger.s(this.tr("unsupported.1", "Please enter a @e style entity selector")));
			}
			else
			{
				EntityFilter entityFilter = new EntityFilter(source, entitySelector);
				this.entityFilter.put(entityType, entityFilter);
				Messenger.m(source, this.advTr(
						"filter_set", "Entity filter of %1$s is set to %2$s",
						typeName,
						entityFilter.toText()
				));
			}
		}
		else
		{
			this.entityFilter.remove(entityType);
			Messenger.m(source, this.advTr(
					"filter_removed", "Entity filter of %1$s removed",
					typeName
			));
		}
	}

	public ITextComponent getEntityFilterText(@Nullable EntityType<?> entityType)
	{
		Predicate<Entity> entityPredicate = this.getFilter(entityType);
		return entityPredicate instanceof EntityFilter ? ((EntityFilter)entityPredicate).toText() : Messenger.s(this.tr("None"));
	}

	public ITextComponent getEntityTypeText(@Nullable EntityType<?> entityType)
	{
		return entityType != null ? entityType.getName() : Messenger.s(this.tr("global"));
	}

	public void displayFilter(CommandSource source, @Nullable EntityType<?> entityType)
	{
		Messenger.m(source, this.advTr(
				"display", "Entity filter of %1$s is %2$s",
				this.getEntityTypeText(entityType),
				this.getEntityFilterText(entityType)
		));
	}

	public int displayAllFilters(CommandSource source)
	{
		Messenger.m(source, Messenger.s(String.format(this.tr("display_total", "There are %s activated filters"), this.entityFilter.size())));
		this.entityFilter.keySet().forEach(entityType -> Messenger.m(
				source,
				Messenger.c(
						"f - ",
						this.getEntityTypeText(entityType),
						"g : ",
						this.getEntityFilterText(entityType),
						"w  ",
						TextUtil.getFancyText(
								null,
								Messenger.s("[×]", "r"),
								Messenger.s(this.tr("click_to_clear", "Click to clear filter")),
								new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(
										"/%s filter %s clear",
										LifeTimeTracker.getInstance().getCommandPrefix(),
										entityType != null ? LifeTimeTrackerUtil.getEntityTypeDescriptor(entityType) : "global"
								))
						)
				)
		));
		return 1;
	}
}
