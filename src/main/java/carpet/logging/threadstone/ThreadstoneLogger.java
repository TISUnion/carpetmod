package carpet.logging.threadstone;

import carpet.logging.AbstractHUDLogger;
import carpet.utils.Messenger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadstoneLogger extends AbstractHUDLogger {

    public static final String NAME = "threadstone";

    public static final String HUD_DISPLAY_FORMAT = "C/T: %d/%d, avg lifetime (ns): %d";
    public static final String ASYNC_LOAD_CHUNK_FORMAT = "Detected possible async loading - @Chunk (%d,%d) from thread named %s. ";
    public static final String ASYNC_SETBLOCKSTATE_FORMAT =
            "Detected possible async setBlockState(...) - @BlockPos (%d,%d,%d) from thread named %s into new blockstate %s with flags %d. ";
    public static final String CONCURRENT_WRITE_CRASH_FORMAT =
            "Detected concurrent writing into a subchunk - crashing thread %s";


    public static final String ASYNC_EXCEPTION_FORMAT =
            "An exception occured on an async thread: %s\n";

    private AtomicInteger glassThreadCreations;
    private AtomicInteger glassThreadTerminations;
    private AtomicLong totalGlassThreadLifetime;

    public ThreadstoneLogger(String name) {
        super(name);
        glassThreadCreations = new AtomicInteger();
        glassThreadTerminations = new AtomicInteger();
        totalGlassThreadLifetime = new AtomicLong();
    }

    @Override
    public ITextComponent[] onHudUpdate(String option, EntityPlayer playerEntity) {
        return new ITextComponent[] {Messenger.s(this.getThreadLifetimes())};
    }

    private static ThreadstoneLogger INSTANCE;

    private void logString(String msg) {
        this.log(((playerOption, player) -> {
            Messenger.m(player, Messenger.s(msg));
            return null;
        }));
    }

    public String getThreadLifetimes() {
        int t = glassThreadTerminations.get();
        return String.format(HUD_DISPLAY_FORMAT, glassThreadCreations.get(), t, (t != 0) ? totalGlassThreadLifetime.get() / t : 0);
    }

    public synchronized static ThreadstoneLogger getInstance() {
        if (INSTANCE == null) INSTANCE = new ThreadstoneLogger(NAME);
        return INSTANCE;
    }

    public void clear() {
        glassThreadCreations.set(0);
        glassThreadTerminations.set(0);
        totalGlassThreadLifetime.set(0);
    }

    public void submitGlassThreadCreation(BlockPos glassPos) {
        glassThreadCreations.incrementAndGet();
    }

    public void submitGlassThreadTermination(BlockPos glassPos, long lifetimeNanos) {
        glassThreadTerminations.incrementAndGet();
        totalGlassThreadLifetime.addAndGet(lifetimeNanos);
    }

    public void submitGlassThreadAlive(BlockPos glassPos, long timeElapsedNanos) {
        totalGlassThreadLifetime.addAndGet(timeElapsedNanos);
    }

    public void onExceptionallyEndedAsyncThread(Throwable throwable) {
        logString((String.format(ASYNC_EXCEPTION_FORMAT, (Object) throwable.getStackTrace())));
    }

    public void onConcurrentWriteCrash(Throwable throwable) {
        logString(String.format(CONCURRENT_WRITE_CRASH_FORMAT, Thread.currentThread().getName()));
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

}
