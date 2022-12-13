package carpet.logging.threadstone;

import carpet.CarpetServer;
import carpet.logging.AbstractHUDLogger;
import carpet.utils.Messenger;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ThreadstoneLogger extends AbstractHUDLogger {

    public static final String NAME = "threadstone";

    public static final String ASYNC_LOAD_CHUNK_FORMAT = "Detected possible async loading - @Chunk (%d,%d) from thread named %s. ";
    public static final String ASYNC_SETBLOCKSTATE_FORMAT =
            "Detected possible async setBlockState(...) - @BlockPos (%d,%d,%d) from thread named %s into new blockstate %s with flags %d. ";
    public static final String CONCURRENT_WRITE_CRASH_FORMAT =
            "Detected concurrent writing into a subchunk - crashing thread %s";


    public static final String ASYNC_EXCEPTION_FORMAT =
            "An exception occured on an async thread: %s\n";

    public ThreadstoneLogger(String name) {
        super(name);
    }

    @Override
    public ITextComponent[] onHudUpdate(String option, EntityPlayer playerEntity) {
        WorldServer world = (WorldServer)playerEntity.getEntityWorld();
        Pair<Integer, Integer> glassTheadInfo = GlassThreadUtil.getGlassThreadCount();
        return new ITextComponent[] {
                world.getChunkProvider().chunkLoadingCacheStatistic.report(),
                GlassThreadStatistic.getInstance().report(),
                Messenger.s(String.format("Glass threads: %d/%d", glassTheadInfo.getFirst(), glassTheadInfo.getSecond()))
        };
    }

    private static ThreadstoneLogger INSTANCE;

    private void logText(ITextComponent msg) {
        this.log(((playerOption, player) -> {
            Messenger.m(player, msg);
            return null;
        }));
    }

    private void logString(String msg) {
        this.logText(Messenger.s(msg));
    }

    public synchronized static ThreadstoneLogger getInstance() {
        if (INSTANCE == null) INSTANCE = new ThreadstoneLogger(NAME);
        return INSTANCE;
    }

    public void onExceptionallyEndedAsyncThread(Throwable throwable) {
        logString((String.format(ASYNC_EXCEPTION_FORMAT, throwable)));
    }

    public void onConcurrentWriteCrash(ReportedException throwable) {
        logString(String.format(CONCURRENT_WRITE_CRASH_FORMAT, Thread.currentThread().getName()));

        // save the crash report for further inspecting
        File file1 = new File(
                new File(CarpetServer.minecraft_server.getDataDirectory(), "crash-async"),
                "ConcurrentWrite-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-" + Thread.currentThread().getName() + ".txt"
        );
        throwable.getCrashReport().saveToFile(file1);
    }

    public static boolean isCurrentThreadAsync(WorldServer worldIn) {
        Thread cur = Thread.currentThread();
        return !(cur.equals(worldIn.getServer().getServerThread())) && !(cur.getName().contains("Client"));
    }

    public void onAsyncLoadChunk(int x, int z) {
//        logString(String.format(ASYNC_LOAD_CHUNK_FORMAT, x, z, Thread.currentThread().getName()));
    }

    public void onAsyncSetBlockState(BlockPos pos, IBlockState newState, int flags) {
//        logString(String.format(ASYNC_SETBLOCKSTATE_FORMAT, pos.getX(), pos.getY(),
//                pos.getZ(), Thread.currentThread().getName(), newState.toString(), flags));
    }

    public void onNoteBlockDebugThreadStarted() {
        logString(String.format("debugNoteBlocks thread %s started", Thread.currentThread().getName()));
    }
}
