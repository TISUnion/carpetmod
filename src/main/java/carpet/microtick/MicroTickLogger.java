package carpet.microtick;

import carpet.logging.LoggerRegistry;
import carpet.microtick.enums.ActionRelation;
import carpet.microtick.enums.BlockUpdateType;
import carpet.microtick.enums.PistonBlockEventType;
import carpet.microtick.tickstages.TickStage;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;

import java.util.*;

public class MicroTickLogger
{
	private static final EnumFacing[] ENUM_FACING_VALUES = EnumFacing.values();
	private String stage;
	private String stageDetail;
	private TickStage stageExtra;
	private final World world;
	public final List<MicroTickMessage> messages = Lists.newLinkedList();
	private final LongOpenHashSet pistonBlockEventSuccessPosition = new LongOpenHashSet();
	private final ITextComponent dimensionDisplayTextGray;

	public MicroTickLogger(World world)
	{
		this.world = world;
		this.dimensionDisplayTextGray = MicroTickUtil.getDimensionNameText(this.world.getDimension().getType()).deepCopy().applyTextStyle(TextFormatting.GRAY);
	}
	
	public void setTickStage(String stage)
	{
		this.stage = stage;
	}
	public String getTickStage()
	{
		return this.stage;
	}
	public void setTickStageDetail(String stage)
	{
		this.stageDetail = stage;
	}
	public String getTickStageDetail()
	{
		return this.stageDetail;
	}
	public void setTickStageExtra(TickStage extra)
	{
		this.stageExtra = extra;
	}
	public TickStage getTickStageExtra()
	{
		return this.stageExtra;
	}

	public void onBlockUpdate(World world, BlockPos pos, Block fromBlock, ActionRelation actionType, BlockUpdateType updateType, String updateTypeExtra)
	{
		for (EnumFacing facing: ENUM_FACING_VALUES)
		{
			BlockPos blockEndRodPos = pos.offset(facing);
			IBlockState iBlockState = world.getBlockState(blockEndRodPos);
			if (iBlockState.getBlock() == Blocks.END_ROD && iBlockState.get(BlockStateProperties.FACING).getOpposite() == facing)
			{
				EnumDyeColor color = MicroTickUtil.getWoolColor(world, blockEndRodPos);
				if (color != null)
				{
					this.addMessage(color, pos, world, new Object[]{
							MicroTickUtil.getTranslatedName(fromBlock),
							String.format("q  %s", actionType),
							String.format("c  %s", updateType),
							String.format("^w %s", updateTypeExtra)
					});
				}
			}
		}
	}
	public void onComponentAddToTileTickList(World world, BlockPos pos, int delay, TickPriority priority)
	{
		EnumDyeColor color = MicroTickUtil.getWoolColor(world, pos);
		if (color != null)
		{
			System.err.println(world.getGameTime() + " " + delay);
			this.addMessage(color, pos, world, new Object[]{
					MicroTickUtil.getTranslatedName(world.getBlockState(pos).getBlock()),
					"q  Scheduled",
					"c  TileTick",
					String.format("^w Delay: %dgt\nPriority: %d (%s)", delay, priority.getPriority(), priority)
			});
		}
	}

	public void onPistonAddBlockEvent(World world, BlockPos pos, int eventID, int eventParam)
	{
		EnumDyeColor color = MicroTickUtil.getWoolColor(world, pos);
		if (color != null)
		{
			this.addMessage(color, pos, world, new Object[]{
					MicroTickUtil.getTranslatedName(world.getBlockState(pos).getBlock()),
					"q  Scheduled",
					"c  BlockEvent",
					MicroTickUtil.getBlockEventMessageExtra(eventID, eventParam)
			});
		}
	}

	// "block" only overwrites displayed name
	public void onPistonExecuteBlockEvent(World world, BlockPos pos, Block block, int eventID, int eventParam, boolean success)
	{
		EnumDyeColor color = MicroTickUtil.getWoolColor(world, pos);
		if (color != null)
		{
			if (success)
			{
				this.pistonBlockEventSuccessPosition.add(pos.toLong());
			}
			else if (pistonBlockEventSuccessPosition.contains(pos.toLong())) // ignore failure after a success blockevent of piston in the same gt
			{
				return;
			}
			addMessage(color, pos, world, new Object[]{
					MicroTickUtil.getTranslatedName(block),
					"q  Executed",
					String.format("c  %s", PistonBlockEventType.getById(eventID)),
					MicroTickUtil.getBlockEventMessageExtra(eventID, eventParam),
					String.format("%s  %s", MicroTickUtil.getBooleanColor(success), success ? "Succeed" : "Failed")
			});
		}
	}

	public void onComponentPowered(World world, BlockPos pos, boolean poweredState)
	{
		EnumDyeColor color = MicroTickUtil.getWoolColor(world, pos);
		if (color != null)
		{
			this.addMessage(color, pos, world, new Object[]{
					MicroTickUtil.getTranslatedName(world.getBlockState(pos).getBlock()),
					String.format("c  %s", poweredState ? "Powered" : "Depowered")
			});
		}
	}

	public void onRedstoneTorchLit(World world, BlockPos pos, boolean litState)
	{
		EnumDyeColor color = MicroTickUtil.getWoolColor(world, pos);
		if (color != null)
		{
			this.addMessage(color, pos, world, new Object[]{
					MicroTickUtil.getTranslatedName(world.getBlockState(pos).getBlock()),
					String.format("c  %s", litState ? "Lit" : "Unlit")
			});
		}
	}

	// #(color, pos) texts[] at stage(detail, extra, dimension)
	public void addMessage(EnumDyeColor color, BlockPos pos, int dimensionID, Object [] texts)
	{
		MicroTickMessage message = new MicroTickMessage(this, dimensionID, pos, color, texts);
		this.messages.add(message);
	}
	public void addMessage(EnumDyeColor color, BlockPos pos, World world, Object [] texts)
	{
		this.addMessage(color, pos, world.getDimension().getType().getId(), texts);
	}

	void flushMessages()
	{
		if (this.messages.isEmpty())
		{
			return;
		}
		LoggerRegistry.getLogger("microtick").log( (option) ->
		{
			boolean uniqueOnly = option.equals("unique");
			List<ITextComponent> msg = Lists.newLinkedList();
			Set<MicroTickMessage> messageHashSet = Sets.newHashSet();
			Iterator<MicroTickMessage> iterator = this.messages.iterator();
			msg.add(Messenger.s(" "));
			msg.add(Messenger.c(
					"f [GameTime ",
					"g " + this.world.getGameTime(),
					"f  @ ",
					this.dimensionDisplayTextGray,
					"f ] ------------"
			));
			while (iterator.hasNext())
			{
				MicroTickMessage message = iterator.next();

				boolean flag = !uniqueOnly;
				if (!messageHashSet.contains(message))
				{
					messageHashSet.add(message);
					flag = true;
				}
				if (flag)
				{
					List<Object> line = Lists.newLinkedList();
					line.add(message.getHashTag());
					for (Object text: message.texts)
					{
						if (text instanceof ITextComponent || text instanceof String)
						{
							line.add(text);
						}
					}
					line.add("w  ");
					line.add(message.getStage());
					line.add("w  ");
					line.add(message.getStackTrace());
					msg.add(Messenger.c(line.toArray(new Object[0])));
				}
			}
			return msg.toArray(new ITextComponent[0]);
		});
		this.messages.clear();
		this.pistonBlockEventSuccessPosition.clear();
	}
}
