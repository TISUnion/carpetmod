package carpet.logging.microtiming.enums;

import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

public enum TickStage
{
	UNKNOWN("Unknown", false),
	SPAWNING("Spawning", true),
	CHUNK_UNLOADING("ChunkUnloading", true),
	WORLD_BORDER("WorldBorder", true),
	TILE_TICK("TileTick", true),
	PLAYER_CHUNK_MAP("PlayerChunkMap", true),
	VILLAGE("Village", true),
	PORTAL_FORCER("PortalForcer", true),
	NEW_LIGHT("NewLight", true),
	RAID("Raid", true),
	WANDERING_TRADER("WanderingTrader", true),
	BLOCK_EVENT("BlockEvent", true),
	ENTITY("Entity", true),
	CHUNK_TICK("ChunkTick", true),
	TILE_ENTITY("TileEntity", true),
	AUTO_SAVE("AutoSave", false),
	ASYNC_TASK("AsyncTask", false),
	PLAYER_ACTION("PlayerAction", false),
	COMMAND_FUNCTION("CommandFunction", false),
	NETWORK("Network", false),
	CONSOLE("Console", false);

	private final String name;
	private final boolean insideWorld;

	TickStage(String name, boolean insideWorld)
	{
		this.name = name;
		this.insideWorld = insideWorld;
	}

	@Override
	public String toString()
	{
		return this.getName();
	}

	public String getName()
	{
		return name;
	}

	public String tr()
	{
		return MicroTimingLoggerManager.tr("stage." + this.name, this.name);
	}

	public ITextComponent toText()
	{
		return Messenger.s(this.tr());
	}

	public boolean isInsideWorld()
	{
		return insideWorld;
	}
}
