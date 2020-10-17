package carpet.microtick;

import carpet.logging.LoggerRegistry;
import carpet.microtick.enums.BlockUpdateType;
import carpet.microtick.enums.EventType;
import carpet.microtick.enums.TickStage;
import carpet.microtick.events.*;
import carpet.microtick.message.IndentedMessage;
import carpet.microtick.message.MessageList;
import carpet.microtick.message.MicroTickMessage;
import carpet.microtick.tickstages.TickStageExtraBase;
import carpet.microtick.utils.MicroTickUtil;
import carpet.microtick.utils.TranslatableBase;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class MicroTickLogger extends TranslatableBase
{
	// [stage][detail]^[extra]

	private TickStage stage;
	private String stageDetail;
	private TickStageExtraBase stageExtra;
	private final WorldServer world;
	public final MessageList messageList = new MessageList();
	private final ITextComponent dimensionDisplayTextGray;

	public MicroTickLogger(WorldServer world)
	{
		this.world = world;
		if (world != null)
		{
			this.dimensionDisplayTextGray = MicroTickUtil.getDimensionNameText(this.world.getDimension().getType());
			this.dimensionDisplayTextGray.getStyle().setColor(TextFormatting.GRAY);
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

	/*
	 * --------------
	 *  Block Update
	 * --------------
	 */

	public void onBlockUpdate(World world, BlockPos pos, Block fromBlock, BlockUpdateType updateType, Supplier<String> updateTypeExtra, EventType eventType)
	{
		Optional<EnumDyeColor> color = MicroTickUtil.getEndRodWoolColor(world, pos);
		color.ifPresent(dyeColor -> this.addMessage(dyeColor, pos, world, new DetectBlockUpdateEvent(eventType, fromBlock, updateType, updateTypeExtra.get())));
	}

	private final static Set<IProperty<?>> INTEREST_PROPERTIES = new ReferenceArraySet<>();
	static
	{
		INTEREST_PROPERTIES.add(BlockStateProperties.POWERED);
		INTEREST_PROPERTIES.add(BlockStateProperties.LIT);
	}

	public void onSetBlockState(World world, BlockPos pos, IBlockState oldState, IBlockState newState, Boolean returnValue, EventType eventType)
	{
		// lazy loading
		EnumDyeColor color = null;
		BlockStateChangeEvent event = new BlockStateChangeEvent(eventType, returnValue, newState.getBlock());

		for (IProperty<?> property: newState.getProperties())
		{
			if (INTEREST_PROPERTIES.contains(property))
			{
				if (color == null)
				{
					Optional<EnumDyeColor> optionalDyeColor = MicroTickUtil.getWoolOrEndRodWoolColor(world, pos);
					if (!optionalDyeColor.isPresent())
					{
						break;
					}
					color = optionalDyeColor.get();
				}
				event.addChanges(property.getName(), oldState.get(property), newState.get(property));
			}
		}
		if (event.hasChanges())
		{
			this.addMessage(color, pos, world, event);
		}
	}

	/*
	 * -----------
	 *  Tile Tick
	 * -----------
	 */

	public void onExecuteTileTick(World world, NextTickListEntry<Block> event, EventType eventType)
	{
		Optional<EnumDyeColor> color = MicroTickUtil.getWoolOrEndRodWoolColor(world, event.position);
		color.ifPresent(dyeColor -> this.addMessage(dyeColor, event.position, world, new ExecuteTileTickEvent(eventType, event)));
	}

	public void onScheduleTileTick(World world, Block block, BlockPos pos, int delay, TickPriority priority, Boolean success)
	{
		Optional<EnumDyeColor> color = MicroTickUtil.getWoolOrEndRodWoolColor(world, pos);
		color.ifPresent(dyeColor -> this.addMessage(dyeColor, pos, world, new ScheduleTileTickEvent(block, pos, delay, priority, success)));
	}

	/*
	 * -------------
	 *  Block Event
	 * -------------
	 */

	public void onExecuteBlockEvent(World world, BlockEventData blockAction, Boolean returnValue, EventType eventType)
	{
		Optional<EnumDyeColor> color = MicroTickUtil.getWoolOrEndRodWoolColor(world, blockAction.getPosition());
		color.ifPresent(dyeColor -> this.addMessage(dyeColor, blockAction.getPosition(), world, new ExecuteBlockEventEvent(eventType, blockAction, returnValue)));
	}

	public void onScheduleBlockEvent(World world, BlockEventData blockAction)
	{
		Optional<EnumDyeColor> color = MicroTickUtil.getWoolOrEndRodWoolColor(world, blockAction.getPosition());
		color.ifPresent(dyeColor -> this.addMessage(dyeColor, blockAction.getPosition(), world, new ScheduleBlockEventEvent(blockAction)));
	}

	/*
	 * ------------------
	 *  Component things
	 * ------------------
	 */

	public void onEmitBlockUpdate(World world, Block block, BlockPos pos, EventType eventType, String methodName)
	{
		Optional<EnumDyeColor> color = MicroTickUtil.getWoolOrEndRodWoolColor(world, pos);
		color.ifPresent(dyeColor -> this.addMessage(dyeColor, pos, world, new EmitBlockUpdateEvent(eventType, block, methodName)));
	}

	/*
	 * -----------------------
	 *  Component things ends
	 * -----------------------
	 */

	public void addMessage(EnumDyeColor color, BlockPos pos, World world, BaseEvent event)
	{
		MicroTickMessage message = new MicroTickMessage(this, world.getDimension().getType(), pos, color, event);
		if (message.getEvent().getEventType() != EventType.ACTION_END)
		{
			this.messageList.addMessageAndIndent(message);
		}
		else
		{
			this.messageList.addMessageAndUnIndent(message);
		}
	}

	private ITextComponent[] getTrimmedMessages(List<IndentedMessage> flushedMessages, boolean uniqueOnly)
	{
		List<ITextComponent> msg = Lists.newArrayList();
		Set<MicroTickMessage> messageHashSet = Sets.newHashSet();
		msg.add(Messenger.s(" "));
		msg.add(Messenger.c(
				String.format("f [%s ", this.tr("GameTime")),
				"g " + this.world.getGameTime(),
				"f  @ ",
				this.dimensionDisplayTextGray,
				"f ] ------------"
		));
		for (IndentedMessage message : flushedMessages)
		{
			boolean showThisMessage = !uniqueOnly || messageHashSet.add(message.getMessage());
			if (showThisMessage)
			{
				msg.add(message.toText());
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
				Map<Boolean, ITextComponent[]> flushedTrimmedMessages = new Reference2ObjectArrayMap<>();
				flushedTrimmedMessages.put(false, getTrimmedMessages(flushedMessages, false));
				flushedTrimmedMessages.put(true, getTrimmedMessages(flushedMessages, true));
				LoggerRegistry.getLogger("microtick").log((option) ->
				{
					boolean uniqueOnly = option.equals("unique");
					return flushedTrimmedMessages.get(uniqueOnly);
				});
			}
		}
	}
}
