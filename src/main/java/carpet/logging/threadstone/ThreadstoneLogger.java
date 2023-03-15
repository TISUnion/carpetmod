package carpet.logging.threadstone;

import carpet.CarpetServer;
import carpet.logging.AbstractHUDLogger;
import carpet.logging.HUDLogger;
import carpet.settings.CarpetSettings;
import carpet.utils.GameUtil;
import carpet.utils.Messenger;
import carpet.utils.deobfuscator.StackTracePrinter;
import com.mojang.datafixers.util.Pair;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ThreadstoneLogger extends AbstractHUDLogger {

    public static final String NAME = "threadstone";
    private static final ThreadstoneLogger INSTANCE = new ThreadstoneLogger();

    private ThreadstoneLogger() {
        super(NAME);
    }

    public static ThreadstoneLogger getInstance() {
        return INSTANCE;
    }

    @Override
    public HUDLogger createCarpetLogger()
    {
        HUDLogger logger = super.createCarpetLogger();
        logger.addSubscriptionValidator((p, o) -> CarpetSettings.threadstoneLogger);
        return logger;
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

    /**
     * Show message on subscribed players in chat hud
     * We cannot use this.log() since this is a HUD logger
     */
    private void logText(ITextComponent msg) {
        ITextComponent message = Messenger.c(
                "g [", Messenger.s(Thread.currentThread().getName(), TextFormatting.LIGHT_PURPLE), "g ] ",
                msg, "w  ",
                StackTracePrinter.create().ignore(ThreadstoneLogger.class).deobfuscate().toSymbolText()
        );
        GameUtil.ensureOnServerThread(() ->
                this.log((playerOption, player) -> {
                    Messenger.tell(player, message);
                    return null;
                })
        );
    }
    private void logFormat(String formatter, Object... args) {
        this.logText(Messenger.format(formatter, args));
    }

    // ============================= hooks =============================
    // all hooks should be checked using CarpetSettings.threadstoneLogger && LoggerRegistry.__threadstone
    // to ensure that they only get triggered when necessary

    public void onExceptionallyEndedAsyncThread(Throwable throwable) {
        this.logFormat("Exception occurred: %s", throwable);
    }

    public void onConcurrentWriteCrash(ReportedException throwable) {
        this.logFormat("Concurrent writing detected, crashing the thread");

        // save the crash report for further inspecting
        GameUtil.ensureOnServerThread(() -> {
            File file1 = new File(
                    new File(CarpetServer.minecraft_server.getDataDirectory(), "crash-async"),
                    "ConcurrentWrite-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-" + Thread.currentThread().getName() + ".txt"
            );
            throwable.getCrashReport().saveToFile(file1);
        });
    }

    public static boolean isOnGlassThread() {
        return GlassThreadUtil.isOnGlassThread();
    }

    public void onAsyncLoadChunk(int x, int z) {
        this.logFormat("Async chunk loading @ %s", Messenger.coord(new ChunkPos(x, z)));
    }

    public void onAsyncGenerateChunk(int x, int z) {
        this.logFormat("Async chunk generating @ %s", Messenger.coord(new ChunkPos(x, z)));
    }

    public void onNoteBlockDebugThreadStarted() {
        this.logFormat("debugNoteBlocks thread started");
    }
}
