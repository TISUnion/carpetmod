package carpet.microtiming;

import carpet.logging.LoggerRegistry;
import carpet.microtiming.enums.EventType;
import carpet.microtiming.enums.TickStage;
import carpet.microtiming.events.BaseEvent;
import carpet.microtiming.events.BlockStateChangeEvent;
import carpet.microtiming.message.IndentedMessage;
import carpet.microtiming.message.MessageList;
import carpet.microtiming.message.MessageType;
import carpet.microtiming.message.MicroTimingMessage;
import carpet.microtiming.tickstages.TickStageExtraBase;
import carpet.microtiming.utils.MicroTimingUtil;
import carpet.microtiming.utils.TranslatableBase;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.*;
import java.util.function.BiFunction;

public class MicroTimingLogger extends TranslatableBase
{
	// [stage][detail]^[extra]

	private TickStage stage;
	private String stageDetail;
	private TickStageExtraBase stageExtra;
	private final WorldServer world;
	public final MessageList messageList = new MessageList();
	private final ITextComponent dimensionDisplayTextGray;

	public MicroTimingLogger(WorldServer world)
	{
		this.world = world;
		if (world != null)
		{
			this.dimensionDisplayTextGray = MicroTimingUtil.getFancyText(
					"g",
					MicroTimingUtil.getDimensionNameText(this.world.getDimension().getType()),
					Messenger.s(this.world.getDimension().getType().toString()),
					null
			);
		}
		else
		{
			this.dimensionDisplayTextGray = null;
		}
	}
	
	public void setTickStage(TickStage stage)
	{
		this.stage = stage;
		this.stageDetail = null;
		this.stageExtra = null;
	}

	public TickStage getTickStage()
	{
		return this.stage;
	}

	public void setTickStageDetail(String stageDetail)
	{
		this.stageDetail = stageDetail;
	}

	public String getTickStageDetail()
	{
		return this.stageDetail;
	}

	public void setTickStageExtra(TickStageExtraBase extra)
	{
		this.stageExtra = extra;
	}

	public TickStageExtraBase getTickStageExtra()
	{
		return this.stageExtra;
	}

	public WorldServer getWorld()
	{
		return this.world;
	}

	private final static Set<IProperty<?>> INTEREST_PROPERTIES = new ReferenceArraySet<>();
	static
	{
		INTEREST_PROPERTIES.add(BlockStateProperties.POWERED);
		INTEREST_PROPERTIES.add(BlockStateProperties.LIT);
		INTEREST_PROPERTIES.add(BlockStateProperties.POWER_0_15);
	}

	public void onSetBlockState(World world, BlockPos pos, IBlockState oldState, IBlockState newState, Boolean returnValue, EventType eventType)
	{
		// lazy loading
		EnumDyeColor color = null;
		BlockStateChangeEvent event = new BlockStateChangeEvent(eventType, returnValue, newState.getBlock());

		for (IProperty<?> IProperty: newState.getProperties())
		{
			if (INTEREST_PROPERTIES.contains(IProperty))
			{
				if (color == null)
				{
					Optional<EnumDyeColor> optionalDyeColor = MicroTimingUtil.getWoolOrEndRodWoolColor(world, pos);
					if (!optionalDyeColor.isPresent())
					{
						break;
					}
					color = optionalDyeColor.get();
				}
				event.addChanges(IProperty.getName(), oldState.get(IProperty), newState.get(IProperty));
			}
		}
		if (event.hasChanges())
		{
			this.addMessage(color, world, pos, event);
		}
	}

	public void addMessage(EnumDyeColor color, World world, BlockPos pos, BaseEvent event)
	{
		MicroTimingMessage message = new MicroTimingMessage(this, world.getDimension().getType(), pos, color, event);
		if (message.getEvent().getEventType() != EventType.ACTION_END)
		{
			this.messageList.addMessageAndIndent(message);
		}
		else
		{
			this.messageList.addMessageAndUnIndent(message);
		}
	}

	public void addMessage(World world, BlockPos pos, BaseEvent event, BiFunction<World, BlockPos, Optional<EnumDyeColor>> woolGetter)
	{
		Optional<EnumDyeColor> color = woolGetter.apply(world, pos);
		color.ifPresent(EnumDyeColor -> this.addMessage(EnumDyeColor, world, pos, event));
	}

	public void addMessage(World world, BlockPos pos, BaseEvent event)
	{
		this.addMessage(world, pos, event, MicroTimingUtil::getWoolOrEndRodWoolColor);
	}

	private ITextComponent getMergedResult(int count, IndentedMessage previousMessage)
	{
		return Messenger.c(
				MicroTimingMessage.getIndentationText(previousMessage.getIndentation()),
				MicroTimingUtil.getFancyText("f", Messenger.s(String.format("  +%dx", count)), previousMessage.getMessage().toText(0, true), null)
		);
	}

	private ITextComponent[] getTrimmedMessages(List<IndentedMessage> flushedMessages, LoggingOption option)
	{
		List<ITextComponent> msg = Lists.newArrayList();
		Set<MicroTimingMessage> messageHashSet = Sets.newHashSet();
		msg.add(Messenger.s(" "));
		msg.add(Messenger.c(
				"f [",
				"f " + this.tr("GameTime"),
				"^w world.getGameTime()",
				"g  " + this.world.getGameTime(),
				"f  @ ",
				this.dimensionDisplayTextGray,
				"f ] ------------"
		));
		int skipCount = 0;
		Iterator<IndentedMessage> iterator = flushedMessages.iterator();
		IndentedMessage previousMessage = null;
		while (iterator.hasNext())
		{
			IndentedMessage message = iterator.next();
			boolean showThisMessage = option == LoggingOption.ALL || message.getMessage().getMessageType() == MessageType.PROCEDURE;
			if (!showThisMessage && option == LoggingOption.MERGED)
			{
				showThisMessage = previousMessage == null || !message.getMessage().equals(previousMessage.getMessage());
			}
			if (!showThisMessage && option == LoggingOption.UNIQUE)
			{
				showThisMessage = messageHashSet.add(message.getMessage());
			}
			if (showThisMessage)
			{
				if (option == LoggingOption.MERGED && previousMessage != null && skipCount > 0)
				{
					msg.add(this.getMergedResult(skipCount, previousMessage));
				}
				msg.add(message.toText());
				previousMessage = message;
				skipCount = 0;
			}
			else
			{
				skipCount++;
			}
			if (!iterator.hasNext() && option == LoggingOption.MERGED && skipCount > 0)
			{
				msg.add(this.getMergedResult(skipCount, previousMessage));
			}
		}
		return msg.toArray(new ITextComponent[0]);
	}

	public void flushMessages()
	{
		if (!this.messageList.isEmpty())
		{
			List<IndentedMessage> flushedMessages = this.messageList.flush();
			if (!flushedMessages.isEmpty())
			{
				Map<LoggingOption, ITextComponent[]> flushedTrimmedMessages = new EnumMap<>(LoggingOption.class);
				for (LoggingOption option : LoggingOption.values())
				{
					flushedTrimmedMessages.put(option, getTrimmedMessages(flushedMessages, option));
				}
				LoggerRegistry.getLogger("microTiming").log((option) -> flushedTrimmedMessages.get(LoggingOption.ofString(option)));
			}
		}
	}

	public enum LoggingOption
	{
		MERGED,
		ALL,
		UNIQUE;

		public static final LoggingOption DEFAULT = LoggingOption.MERGED;
		private static final Map<String, LoggingOption> OPTION_MAP = new Object2ObjectArrayMap<>();

		static
		{
			for (LoggingOption option : LoggingOption.values())
			{
				OPTION_MAP.put(option.name(), option);
				OPTION_MAP.put(option.name().toLowerCase(), option);
			}
		}

		public static LoggingOption ofString(String str)
		{
			return OPTION_MAP.getOrDefault(str, DEFAULT);
		}
	}
}
