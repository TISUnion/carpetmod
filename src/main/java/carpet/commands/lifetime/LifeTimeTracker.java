package carpet.commands.lifetime;

import carpet.commands.AbstractTracker;
import carpet.commands.lifetime.utils.LifeTimeTrackerUtil;
import carpet.commands.lifetime.utils.SpecificDetailMode;
import carpet.utils.Messenger;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class LifeTimeTracker extends AbstractTracker
{
	private static boolean attachedServer = false;
	private static final LifeTimeTracker INSTANCE = new LifeTimeTracker();

	private int currentTrackId = 0;

	private final Map<WorldServer, LifeTimeWorldTracker> trackers = new Reference2ObjectArrayMap<>();

	public LifeTimeTracker()
	{
		super("LifeTime");
	}

	public static LifeTimeTracker getInstance()
	{
		return INSTANCE;
	}

	public LifeTimeWorldTracker getTracker(World world)
	{
		return world instanceof WorldServer ? this.trackers.get(world) : null;
	}

	public static void attachServer(MinecraftServer minecraftServer)
	{
		attachedServer = true;
		INSTANCE.trackers.clear();
		for (WorldServer world : minecraftServer.getWorlds())
		{
			INSTANCE.trackers.put(world, world.getLifeTimeWorldTracker());
		}
	}

	public static void detachServer()
	{
		attachedServer = false;
		INSTANCE.stop();
	}

	public static boolean isActivated()
	{
		return attachedServer && INSTANCE.isTracking();
	}

	public boolean willTrackEntity(Entity entity)
	{
		return isActivated() &&
				entity.getTrackId() == this.getCurrentTrackId() &&
				LifeTimeTrackerUtil.isTrackedEntity(entity);
	}
	public Stream<String> getAvailableEntityType()
	{
		if (!isActivated())
		{
			return Stream.empty();
		}
		return this.trackers.values().stream().
				flatMap(
						tracker -> tracker.getDataMap().keySet().
						stream().map(LifeTimeTrackerUtil::getEntityTypeDescriptor)
				).
				distinct();
	}

	public int getCurrentTrackId()
	{
		return this.currentTrackId;
	}

	@Override
	protected void initTracker()
	{
		this.currentTrackId++;
		this.trackers.values().forEach(LifeTimeWorldTracker::initTracker);
	}

	@Override
	protected void printTrackingResult(CommandSource source, boolean realtime)
	{
		try
		{
			long ticks = this.sendTrackedTime(source, realtime);
			int count = this.trackers.values().stream().
					mapToInt(tracker -> tracker.print(source, ticks, null, null)).
					sum();
			if (count == 0)
			{
				Messenger.tell(source, Messenger.s(this.tr("no_result", "No result yet")));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void sendUnknownEntity(CommandSource source, String entityTypeString)
	{
		Messenger.tell(source, Messenger.s(String.format(this.tr("unknown_entity_type", "Unknown entity type \"%s\""), entityTypeString), "r"));
	}

	private void printTrackingResultSpecificInner(CommandSource source, String entityTypeString, String detailModeString, boolean realtime)
	{
		Optional<EntityType<?>> entityTypeOptional = LifeTimeTrackerUtil.getEntityTypeFromName(entityTypeString);
		if (entityTypeOptional.isPresent())
		{
			SpecificDetailMode detailMode = null;
			if (detailModeString != null)
			{
				try
				{
					detailMode = SpecificDetailMode.fromString(detailModeString);
				}
				catch (IllegalArgumentException e)
				{
					Messenger.tell(source, Messenger.s(String.format(this.tr("invalid_detail", "Invalid statistic detail \"%s\""), detailModeString), "r"));
					return;
				}
			}

			long ticks = this.sendTrackedTime(source, realtime);
			EntityType<?> entityType = entityTypeOptional.get();
			source.sendFeedback(
					this.advTr("specific_result", "Life time result for %1$s", entityType.getName()),
					false
			);
			SpecificDetailMode finalDetailMode = detailMode;
			int count = this.trackers.values().stream().
					mapToInt(tracker -> tracker.print(source, ticks, entityType, finalDetailMode)).
					sum();
			if (count == 0)
			{
				Messenger.tell(source, Messenger.s(this.tr("no_result", "No result yet")));
			}
		}
		else
		{
			this.sendUnknownEntity(source, entityTypeString);
		}
	}

	public int printTrackingResultSpecific(CommandSource source, String entityTypeString, String detailModeString, boolean realtime)
	{
		return this.doWhenTracking(source, () -> this.printTrackingResultSpecificInner(source, entityTypeString, detailModeString, realtime));
	}

	protected int showHelp(CommandSource source)
	{
		String docLink = this.tr("help.doc_link", "https://github.com/TISUnion/TISCarpet113/blob/TIS-Server/docs/Features.md#lifetime");
		source.sendFeedback(Messenger.c(
				String.format("wb %s\n", this.getTranslatedNameFull()),
				String.format("w %s\n", this.tr("help.doc_summary", "A tracker to track lifetime and spawn / removal reasons from all newly spawned and removed entities")),
				String.format("w %s", this.tr("help.complete_doc_hint", "Complete doc")),
				Messenger.getSpaceText(),
				Messenger.fancy(
						null,
						Messenger.s(this.tr("help.here", "here"), "ut"),
						Messenger.s(docLink, "t"),
						new ClickEvent(ClickEvent.Action.OPEN_URL, docLink)
				)
		), false);
		return 1;
	}
}
