package carpet.logging.microtiming;

import carpet.CarpetServer;
import carpet.logging.LoggerRegistry;
import carpet.logging.microtiming.enums.BlockUpdateType;
import carpet.logging.microtiming.enums.EventType;
import carpet.logging.microtiming.enums.TickStage;
import carpet.logging.microtiming.events.*;
import carpet.logging.microtiming.marker.MicroTimingMarkerManager;
import carpet.logging.microtiming.tickstages.TickStageExtraBase;
import carpet.logging.microtiming.utils.MicroTimingContext;
import carpet.logging.microtiming.utils.MicroTimingUtil;
import carpet.settings.CarpetSettings;
import carpet.utils.GameUtil;
import carpet.utils.Translator;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.IProperty;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MicroTimingLoggerManager
{
    private static MicroTimingLoggerManager instance;
    public static final Translator TRANSLATOR = (new MicroTimingLogger(null)).getTranslator();

    private final Map<WorldServer, MicroTimingLogger> loggers = new Reference2ObjectArrayMap<>();
    private long lastFlushTime;

    public MicroTimingLoggerManager(MinecraftServer minecraftServer)
    {
        this.lastFlushTime = -1;
        for (WorldServer world : minecraftServer.getWorlds())
        {
            this.loggers.put(world, world.getMicroTickLogger());
        }
    }

    public static MicroTimingLoggerManager getInstance()
    {
        return instance;
    }

    public Map<WorldServer, MicroTimingLogger> getLoggers()
    {
        return loggers;
    }

    public static boolean isLoggerActivated()
    {
        return CarpetSettings.microTiming && LoggerRegistry.__microTiming && instance != null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean shouldRecordEvent()
    {
        return isLoggerActivated() && GameUtil.isOnServerThread();
    }

    public static void attachServer(MinecraftServer minecraftServer)
    {
        instance = new MicroTimingLoggerManager(minecraftServer);
        CarpetServer.LOGGER.debug("Attached MicroTick loggers to " + instance.loggers.size() + " worlds");
    }

    public static void detachServer()
    {
        instance = null;
        CarpetServer.LOGGER.debug("Detached MicroTick loggers");
    }

    public static Optional<MicroTimingLogger> getWorldLogger(World world)
    {
        if (instance != null && world instanceof WorldServer)
        {
            return Optional.of(((WorldServer) world).getMicroTickLogger());
        }
        return Optional.empty();
    }

    public static String tr(String key, String text, boolean autoFormat)
    {
        return TRANSLATOR.tr(key, text, autoFormat);
    }

    public static String tr(String key, String text)
    {
        return TRANSLATOR.tr(key, text);
    }

    public static String tr(String key)
    {
        return TRANSLATOR.tr(key);
    }

    /*
     * -------------------------
     *  General Event Operation
     * -------------------------
     */

    public static void onEvent(MicroTimingContext context)
    {
        getWorldLogger(context.getWorld()).ifPresent(logger -> logger.addMessage(context));
    }

    /*
     * ----------------------------------
     *  Block Update and Block Operation
     * ----------------------------------
     */

    public static void onBlockUpdate(World world, BlockPos pos, Block sourceBlock, BlockUpdateType updateType, EnumFacing exceptSide, EventType eventType)
    {
        if (!shouldRecordEvent())
        {
            return;
        }
        onEvent(MicroTimingContext.create().
                withWorld(world).withBlockPos(pos).
                withEventSupplier(() -> new DetectBlockUpdateEvent(eventType, sourceBlock, updateType,  () -> updateType.getUpdateOrderList(exceptSide))).
                withWoolGetter(MicroTimingUtil::blockUpdateColorGetter)
        );
    }

    public static void onSetBlockState(World world, BlockPos pos, IBlockState oldState, IBlockState newState, Boolean returnValue, int flags, EventType eventType)
    {
        if (shouldRecordEvent())
        {
            if (oldState.getBlock() == newState.getBlock())
            {
                // lazy loading
                EnumDyeColor color = null;
                List<BlockStateChangeEvent.PropertyChange> changes = Lists.newArrayList();

                for (IProperty<?> property : newState.getProperties())
                {
                    if (color == null)
                    {
                        Optional<EnumDyeColor> optionalDyeColor = MicroTimingUtil.defaultColorGetter(world, pos);
                        if (!optionalDyeColor.isPresent())
                        {
                            break;
                        }
                        color = optionalDyeColor.get();
                    }
                    if (oldState.get(property) != newState.get(property))
                    {
                        changes.add(new BlockStateChangeEvent.PropertyChange(property, oldState.get(property), newState.get(property)));
                    }
                }
                if (!changes.isEmpty())
                {
                    onEvent(MicroTimingContext.create().
                            withWorld(world).withBlockPos(pos).withColor(color).
                            withEventSupplier(() -> {
                                BlockStateChangeEvent event = new BlockStateChangeEvent(eventType, oldState, newState, returnValue, flags);
                                event.setChanges(changes);
                                return event;
                            })
                    );
                }
            }
            else
            {
                onEvent(MicroTimingContext.create().
                        withWorld(world).withBlockPos(pos).
                        withEventSupplier(() -> new BlockReplaceEvent(eventType, oldState, newState, returnValue, flags)).
                        withWoolGetter(MicroTimingUtil::defaultColorGetter)
                );
            }
        }
    }

    /*
     * -----------
     *  Tile Tick
     * -----------
     */

    public static void onExecuteTileTickEvent(World world, NextTickListEntry<Block> event, EventType eventType)
    {
        if (!shouldRecordEvent())
        {
            return;
        }
        onEvent(MicroTimingContext.create().
                withWorld(world).withBlockPos(event.position).
                withEventSupplier(() -> new ExecuteTileTickEvent(eventType, event))
        );
    }

    public static void onScheduleTileTickEvent(World world, Block block, BlockPos pos, int delay, TickPriority priority, Boolean success)
    {
        if (!shouldRecordEvent())
        {
            return;
        }
        onEvent(MicroTimingContext.create().
                withWorld(world).withBlockPos(pos).
                withEventSupplier(() -> new ScheduleTileTickEvent(block, pos, delay, priority, success))
        );
    }

    /*
     * -------------
     *  Block Event
     * -------------
     */

    public static void onExecuteBlockEvent(World world, BlockEventData blockAction, Boolean returnValue, ExecuteBlockEventEvent.FailInfo failInfo, EventType eventType)
    {
        if (!shouldRecordEvent())
        {
            return;
        }
        onEvent(MicroTimingContext.create().
                withWorld(world).withBlockPos(blockAction.getPosition()).
                withEvent(new ExecuteBlockEventEvent(eventType, blockAction, returnValue, failInfo))
        );
    }

    public static void onScheduleBlockEvent(World world, BlockEventData blockAction, boolean success)
    {
        if (!shouldRecordEvent())
        {
            return;
        }
        onEvent(MicroTimingContext.create().
                withWorld(world).withBlockPos(blockAction.getPosition()).
                withEvent(new ScheduleBlockEventEvent(blockAction, success))
        );
    }

    /*
     * ------------------
     *  Component things
     * ------------------
     */

    public static void onEmitBlockUpdate(World world, Block block, BlockPos pos, EventType eventType, String methodName)
    {
        if (!shouldRecordEvent())
        {
            return;
        }
        onEvent(MicroTimingContext.create().
                withWorld(world).withBlockPos(pos).
                withEvent(new EmitBlockUpdateEvent(eventType, block, methodName))
        );
    }

    public static void onEmitBlockUpdateRedstoneDust(World world, Block block, BlockPos pos, EventType eventType, String methodName, Collection<BlockPos> updateOrder)
    {
        if (!shouldRecordEvent())
        {
            return;
        }
        onEvent(MicroTimingContext.create().
                withWorld(world).withBlockPos(pos).
                withEvent(new EmitBlockUpdateRedstoneDustEvent(eventType, block, methodName, pos, updateOrder))
        );
    }

    /*
     * ------------
     *  Tick Stage
     * ------------
     */

    public static void setTickStage(World world, TickStage stage)
    {
        getWorldLogger(world).ifPresent(logger -> logger.setTickStage(stage));
    }
    public static void setTickStage(TickStage stage)
    {
        if (instance != null)
        {
            for (MicroTimingLogger logger : instance.loggers.values())
            {
                logger.setTickStage(stage);
            }
        }
    }

    public static void setTickStageDetail(World world, String detail)
    {
        getWorldLogger(world).ifPresent(logger -> logger.setTickStageDetail(detail));
    }

    public static void setTickStageExtra(World world, TickStageExtraBase stage)
    {
        getWorldLogger(world).ifPresent(logger -> logger.setTickStageExtra(stage));
    }
    public static void setTickStageExtra(TickStageExtraBase stage)
    {
        if (instance != null)
        {
            for (MicroTimingLogger logger : instance.loggers.values())
            {
                logger.setTickStageExtra(stage);
            }
        }
    }

    /*
     * ------------
     *  Interfaces
     * ------------
     */

    private void flush(long gameTime) // needs to call at the end of a gt
    {
        if (gameTime != this.lastFlushTime)
        {
            this.lastFlushTime = gameTime;
            for (MicroTimingLogger logger : this.loggers.values())
            {
                logger.flushMessages();
            }
        }
    }

    public static void flushMessages(long gameTime) // needs to call at the end of a gt
    {
        if (instance != null && isLoggerActivated())
        {
            instance.flush(gameTime);
        }
    }

    public static Optional<TickStage> getTickStage(World world)
    {
        return getWorldLogger(world).map(MicroTimingLogger::getTickStage);
    }

    public static Optional<String> getTickStageDetail(World world)
    {
        return getWorldLogger(world).map(MicroTimingLogger::getTickStageDetail);
    }

    public static Optional<TickStageExtraBase> getTickStageExtra(World world)
    {
        return getWorldLogger(world).map(MicroTimingLogger::getTickStageExtra);
    }

    /*
     * ----------------
     *   Marker Logic
     * ----------------
     */

    public static boolean onPlayerRightClick(EntityPlayer playerEntity, EnumHand hand, BlockPos blockPos)
    {
        if (MicroTimingUtil.isMarkerEnabled() && playerEntity instanceof EntityPlayerMP && hand == EnumHand.MAIN_HAND && MicroTimingUtil.isPlayerSubscribed(playerEntity))
        {
            ItemStack itemStack = playerEntity.getHeldItemMainhand();
            Item holdingItem = itemStack.getItem();
            if (holdingItem instanceof ItemDye)
            {
                ITextComponent name = null;
                if (itemStack.hasDisplayName())
                {
                    name = itemStack.getDisplayName();
                }
                // server-side check will be in addMarker
                MicroTimingMarkerManager.getInstance().addMarker(playerEntity, blockPos, ((ItemDye)holdingItem).getDyeColor(), name);
                return true;
            }
            if (holdingItem == Items.SLIME_BALL)
            {
                return MicroTimingMarkerManager.getInstance().tweakMarkerMobility(playerEntity, blockPos);
            }
        }
        return false;
    }

    public static void moveMarker(World world, BlockPos blockPos, EnumFacing direction)
    {
        if (MicroTimingUtil.isMarkerEnabled())
        {
            MicroTimingMarkerManager.getInstance().moveMarker(world, blockPos, direction);
        }
    }
}
