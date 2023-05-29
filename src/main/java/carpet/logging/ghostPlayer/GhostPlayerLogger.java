package carpet.logging.ghostPlayer;

import carpet.logging.AbstractLogger;
import carpet.logging.Logger;
import carpet.logging.LoggerRegistry;
import carpet.logging.microtiming.MicroTimingAccess;
import carpet.settings.CarpetSettings;
import carpet.utils.Messenger;
import carpet.utils.deobfuscator.StackTracePrinter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class GhostPlayerLogger extends AbstractLogger
{
	public static final String NAME = "ghostPlayer";
	private static final GhostPlayerLogger INSTANCE = new GhostPlayerLogger();

	private GhostPlayerLogger()
	{
		super(NAME);
	}

	public static GhostPlayerLogger getInstance()
	{
		return INSTANCE;
	}

	@Override
	public Logger createCarpetLogger()
	{
		Logger logger = super.createCarpetLogger();
		logger.addSubscriptionValidator((p, o) -> CarpetSettings.loggerGhostPlayer);
		return logger;
	}

	@Nullable
	@Override
	public String[] getSuggestedLoggingOption()
	{
		return new String[]{"me", "all", "me,no_subchunk", "all,no_subchunk"};
	}

	private void logFormat(Type type, World world, EntityPlayer entity, String formatter, Object... args)
	{
		ITextComponent message = Messenger.format(
				"%s %s (%s) %s %s %s %s %s %s",
				Messenger.hover(
						Messenger.c("g [", Messenger.s(NAME, TextFormatting.AQUA), "g ]"),
						Messenger.s(NAME + " logger")
				),
				Messenger.entity(entity), entity.getEntityId(),
				Messenger.format(formatter, args),
				Messenger.c("g @"),
				Messenger.hover(Messenger.dimension(world), Messenger.s("GameTime: " + world.getGameTime())),
				MicroTimingAccess.getTickPhase().toText("y"),
				StackTracePrinter.create().ignore(GhostPlayerLogger.class).deobfuscate().toSymbolText()
		);
		this.log((option, player) -> {
			List<String> options = Arrays.asList(option.split(MULTI_OPTION_SEP_REG));
			boolean ok = options.contains("me") ? player.getUniqueID().equals(entity.getUniqueID()) : options.contains("all");
			ok &= !(type == Type.SUBCHUNK_OP && options.contains("no_subchunk"));
			return ok ? new ITextComponent[]{message} : null;
		});
	}

	// ============================= hooks =============================
	// all hooks should be checked using GhostPlayerLogger#isEnabled()
	// to ensure that they only get triggered when necessary

	public static boolean isEnabled()
	{
		return CarpetSettings.loggerGhostPlayer && LoggerRegistry.__ghostPlayer;
	}

	public void onWorldRemoveEntity(World world, EntityPlayer entity, String actionName)
	{
		if (!isEnabled())
		{
			return;
		}
		this.logFormat(Type.WORLD_REMOVE, world, entity, actionName);
	}

	public void onChunkUnloadPlayer(Chunk chunk, EntityPlayer entity)
	{
		if (!isEnabled())
		{
			return;
		}
		this.logFormat(
				Type.CHUNK_UNLOAD,
				chunk.getWorld(), entity,
				"chunk unload -> unloadEntities %s",
				Messenger.coord(chunk.getPos(), chunk.getWorld().getDimension().getType())
		);
	}

	public void onChunkSectionAddOrRemovePlayer(Chunk chunk, EntityPlayer entity, int y, String actionName)
	{
		if (!isEnabled())
		{
			return;
		}
        this.logFormat(
		        Type.SUBCHUNK_OP,
		        chunk.getWorld(), entity,
	            "subchunk %s %s",
		        Messenger.fancy(
						Messenger.format("[%s, %s, %s]", chunk.x, y, chunk.z),
				        Messenger.s("Chunk section coordinate"),
				        new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/tp %d %d %d", chunk.x * 16 + 8, y * 16 + 8, chunk.z * 16 + 8))
		        ),
		        actionName
        );
	}

	private enum Type
	{
		WORLD_REMOVE,
		CHUNK_UNLOAD,
		SUBCHUNK_OP
	}
}
