package carpet.logging.logHelpers;

import carpet.CarpetServer;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;

public class SaveStateLogHelper
{
	private static final double LOGGING_RADIO = 0.5;
	public static final ThreadLocal<ChunkPos> currentSavingChunkPos = ThreadLocal.withInitial(() -> null);

	public static void log(int size, int saveStateThreshold)
	{
		ChunkPos chunkPos = currentSavingChunkPos.get();
		if (chunkPos == null)
		{
			return;
		}
		currentSavingChunkPos.remove();
		double radio = (double)size / saveStateThreshold;
		if (radio < LOGGING_RADIO)
		{
			return;
		}
		CarpetServer.minecraft_server.addScheduledTask(() -> {
			LoggerRegistry.getLogger("savestate").log(() -> new ITextComponent[]{
					Messenger.s(String.format("Chunk [%d, %d] %.2fKB (%.2f%%)%s", chunkPos.x, chunkPos.z, size / 1024.0D, radio * 100, radio >= 1 ? " OVERFLOW" : ""))
			});
		});
	}
}
