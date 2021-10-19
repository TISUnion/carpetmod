package carpet.logging.microtiming;

import carpet.logging.AbstractLogger;
import carpet.logging.LoggerRegistry;
import carpet.logging.microtiming.enums.EventType;
import carpet.logging.microtiming.enums.TickStage;
import carpet.logging.microtiming.events.BaseEvent;
import carpet.logging.microtiming.events.BlockStateChangeEvent;
import carpet.logging.microtiming.message.IndentedMessage;
import carpet.logging.microtiming.message.MessageList;
import carpet.logging.microtiming.message.MessageType;
import carpet.logging.microtiming.message.MicroTimingMessage;
import carpet.logging.microtiming.tickstages.TickStageExtraBase;
import carpet.logging.microtiming.utils.MicroTimingUtil;
import carpet.utils.TextUtil;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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

public class MicroTimingLogger extends AbstractLogger
{
	// [stage][detail]^[extra]
	public static final String NAME = "microTiming";

	private TickStage stage;
	private String stageDetail;
	private TickStageExtraBase stageExtra;
	private final WorldServer world;
	public final MessageList messageList = new MessageList();

	public MicroTimingLogger(WorldServer world)
	{
		super(NAME);
		this.world = world;
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
		INTEREST_PROPERTIES.add(BlockStateProperties.POWERED);  // redstone repeater, observer, etc.
		INTEREST_PROPERTIES.add(BlockStateProperties.LIT);  // redstone torch, redstone lamp
		INTEREST_PROPERTIES.add(BlockStateProperties.POWER_0_15);  // redstone dust, weighted pressure plates, etc.
		INTEREST_PROPERTIES.add(BlockStateProperties.LOCKED);  // redstone repeater
	}

	public void onSetBlockState(World world, BlockPos pos, IBlockState oldState, IBlockState newState, Boolean returnValue, int flags, EventType eventType)
	{
		// lazy loading
		EnumDyeColor color = null;
		BlockStateChangeEvent event = new BlockStateChangeEvent(eventType, returnValue, newState.getBlock(), flags);

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
				TextUtil.getFancyText(
						"g",
						Messenger.s(String.format("  +%dx", count)),
						Messenger.c(
								String.format("w %s\n", String.format(this.tr("merged_message", "Merged %d more same message" + (count > 1 ? "s" : "")), count)),
								previousMessage.getMessage().toText(0, true)
						),
						null
				)
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
				TextUtil.getFancyText(
						"g",
						TextUtil.getDimensionNameText(this.world.getDimension().getType()),
						Messenger.s(this.world.getDimension().getType().toString()),
						null
				),
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
				LoggerRegistry.getLogger(NAME).log((option) -> flushedTrimmedMessages.get(LoggingOption.getOrDefault(option)));
			}
		}
	}

	public enum LoggingOption
	{
		MERGED,
		ALL,
		UNIQUE;

		public static final LoggingOption DEFAULT = MERGED;

		public static LoggingOption getOrDefault(String option)
		{
			LoggingOption loggingOption;
			try
			{
				loggingOption = LoggingOption.valueOf(option.toUpperCase());
			}
			catch (IllegalArgumentException e)
			{
				loggingOption = LoggingOption.DEFAULT;
			}
			return loggingOption;
		}
	}
}
