package carpet.commands.lifetime;

import carpet.commands.lifetime.filter.EntityFilterManager;
import carpet.commands.lifetime.removal.RemovalReason;
import carpet.commands.lifetime.spawning.SpawningReason;
import carpet.commands.lifetime.trackeddata.BasicTrackedData;
import carpet.commands.lifetime.trackeddata.ExperienceOrbTrackedData;
import carpet.commands.lifetime.trackeddata.ItemTrackedData;
import carpet.commands.lifetime.utils.LifeTimeTrackerUtil;
import carpet.commands.lifetime.utils.SpecificDetailMode;
import carpet.utils.Messenger;
import carpet.utils.TranslationContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.WorldServer;

import java.util.*;

public class LifeTimeWorldTracker extends TranslationContext
{
	private final WorldServer world;
	private final Map<EntityType<?>, BasicTrackedData> dataMap = Maps.newHashMap();
	// a counter which accumulates when spawning stage occurs
	// it's used to determine life time
	private long spawnStageCounter;

	public LifeTimeWorldTracker(WorldServer world)
	{
		super(LifeTimeTracker.getInstance().getTranslator());
		this.world = world;
	}

	public Map<EntityType<?>, BasicTrackedData> getDataMap()
	{
		return this.dataMap;
	}

	public void initTracker()
	{
		this.dataMap.clear();
	}

	private Optional<BasicTrackedData> getTrackedData(Entity entity)
	{
		if (LifeTimeTracker.getInstance().willTrackEntity(entity))
		{
			return Optional.of(this.dataMap.computeIfAbsent(entity.getType(), (e -> {
				if (entity instanceof EntityItem)
				{
					return new ItemTrackedData();
				}
				if (entity instanceof EntityXPOrb)
				{
					return new ExperienceOrbTrackedData();
				}
				return new BasicTrackedData();
			})));
		}
		return Optional.empty();
	}

	public void onEntitySpawn(Entity entity, SpawningReason reason)
	{
		this.getTrackedData(entity).ifPresent(data -> data.updateSpawning(entity, reason));
	}

	public void onEntityRemove(Entity entity, RemovalReason reason)
	{
		this.getTrackedData(entity).ifPresent(data -> data.updateRemoval(entity, reason));
	}

	public void increaseSpawnStageCounter()
	{
		this.spawnStageCounter++;
	}

	public long getSpawnStageCounter()
	{
		return this.spawnStageCounter;
	}

	private List<ITextComponent> addIfEmpty(List<ITextComponent> list, ITextComponent text)
	{
		if (list.isEmpty())
		{
			list.add(text);
		}
		return list;
	}

	protected int print(CommandSource source, long ticks, EntityType<?> specificType, SpecificDetailMode detailMode)
	{
		// existence check
		BasicTrackedData specificData = this.dataMap.get(specificType);
		if (this.dataMap.isEmpty() || (specificType != null && specificData == null))
		{
			return 0;
		}

		// dimension name header
		// Overworld (minecraft:overworld)
		List<ITextComponent> result = Lists.newArrayList();
		result.add(Messenger.s(" "));
		result.add(Messenger.c(
				Messenger.formatting(Messenger.dimension(this.world), TextFormatting.BOLD, TextFormatting.GOLD),
				String.format("g  (%s)", this.world.getDimension().getType().toString())
		));

		if (specificType == null)
		{
			this.printAll(ticks, result);
		}
		else
		{
			this.printSpecific(ticks, specificType, specificData, detailMode, result);
		}
		Messenger.send(source, result);
		return 1;
	}

	private void printAll(long ticks, List<ITextComponent> result)
	{
		// sorted by spawn count
		// will being sorting by avg life time better?
		this.dataMap.entrySet().stream().
				sorted(Collections.reverseOrder(Comparator.comparingLong(a -> a.getValue().getSpawningCount()))).
				forEach((entry) -> {
					EntityType<?> entityType = entry.getKey();
					BasicTrackedData data = entry.getValue();
					List<ITextComponent> spawningReasons = data.getSpawningReasonsTexts(ticks, true);
					List<ITextComponent> removalReasons = data.getRemovalReasonsTexts(ticks, true);
					String currentCommandBase = String.format("/%s %s", LifeTimeTracker.getInstance().getCommandPrefix(), LifeTimeTrackerUtil.getEntityTypeDescriptor(entityType));
					// [Creeper] S/R: 21/8, L: 145/145/145.00 (gt)
					result.add(Messenger.c(
							"g - [",
							Messenger.fancy(
									null,
									entityType.getName(),
									Messenger.c(
											String.format("w %s: ", this.tr("filter_info_header", "Filter")),
											EntityFilterManager.getInstance().getEntityFilterText(entityType),
											"g  / [G] ",
											EntityFilterManager.getInstance().getEntityFilterText(null),
											"w \n" + this.tr("detail_hint", "Click to show detail")
									),
									new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, currentCommandBase)
							),
							"g ] ",
							Messenger.fancy(
									null,
									Messenger.c("e S", "g /", "r R"),
									Messenger.c(
											"e " + this.tr("Spawn Count"),
											"g  / ",
											"r " + this.tr("Removal Count")
									),
									null
							),
							"g : ",
							Messenger.fancy(
									null,
									Messenger.c("e " + data.getSpawningCount()),
									Messenger.c(
											data.getSpawningCountText(ticks),
											"w " + (spawningReasons.isEmpty() ? "" : "\n"),
											Messenger.c(spawningReasons.toArray(new Object[0]))
									),
									new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("%s %s", currentCommandBase, SpecificDetailMode.SPAWNING))
							),
							"g /",
							Messenger.fancy(
									null,
									Messenger.c("r " + data.getRemovalCount()),
									Messenger.c(
											data.getRemovalCountText(ticks),
											"w " + (removalReasons.isEmpty() ? "" : "\n"),
											Messenger.c(removalReasons.toArray(new Object[0]))
									),
									new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("%s %s", currentCommandBase, SpecificDetailMode.REMOVAL))
							),
							"g , ",
							Messenger.fancy(
									null,
									Messenger.c(
											"q L", "g : ",
											data.lifeTimeStatistic.getCompressedResult(true)
									),
									Messenger.c(
											String.format("q %s\n", this.tr("Life Time Overview")),
											data.lifeTimeStatistic.getResult("", true)
									),
									new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("%s %s", currentCommandBase, SpecificDetailMode.LIFE_TIME))
							)
					));
				});
	}

	private void printSpecific(long ticks, EntityType<?> specificType, BasicTrackedData specificData, SpecificDetailMode detailMode, List<ITextComponent> result)
	{
		result.add(Messenger.c(
				String.format("c %s: ", this.tr("filter_info_header", "Filter")),
				EntityFilterManager.getInstance().getEntityFilterText(specificType),
				"g  / ",
				Messenger.fancy("g", Messenger.s("[G]"), Messenger.s(EntityFilterManager.getInstance().tr("Global")), null),
				"g  ",
				EntityFilterManager.getInstance().getEntityFilterText(null)
		));
		boolean showLifeTime = detailMode == null || detailMode == SpecificDetailMode.LIFE_TIME;
		boolean showSpawning = detailMode == null || detailMode == SpecificDetailMode.SPAWNING;
		boolean showRemoval = detailMode == null || detailMode == SpecificDetailMode.REMOVAL;
		if (showSpawning)
		{
			result.add(specificData.getSpawningCountText(ticks));
		}
		if (showRemoval)
		{
			result.add(specificData.getRemovalCountText(ticks));
		}
		if (showLifeTime)
		{
			result.add(Messenger.fancy(
					"q",
					Messenger.s(this.tr("Life Time Overview")),
					Messenger.s(this.tr("life_time_explain", "The amount of spawning stage passing between entity spawning and entity removal")),
					null
			));
			result.add(specificData.lifeTimeStatistic.getResult("", false));
		}
		if (showSpawning)
		{
			result.add(Messenger.s(this.tr("Reasons for spawning"), "e"));
			result.addAll(this.addIfEmpty(specificData.getSpawningReasonsTexts(ticks, false), Messenger.s("  N/A", "g")));
		}
		if (showRemoval)
		{
			result.add(Messenger.s(this.tr("Reasons for removal"), "r"));
			result.addAll(this.addIfEmpty(specificData.getRemovalReasonsTexts(ticks, false), Messenger.s("  N/A", "g")));
		}
	}
}
