package carpet.logging.commandblock;

import carpet.logging.AbstractLogger;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class CommandBlockLogger extends AbstractLogger
{
	public static final String NAME = "commandBlock";
	public static final int MINIMUM_LOG_INTERVAL = 3 * 20;  // 3s
	public static final int MAXIMUM_PREVIEW_LENGTH = 16;
	private static final CommandBlockLogger INSTANCE = new CommandBlockLogger();

	public CommandBlockLogger()
	{
		super(NAME);
	}

	public static CommandBlockLogger getInstance()
	{
		return INSTANCE;
	}

	private void logCommandBlockExecution(World world, ITextComponent nameText, ITextComponent posText, CommandBlockBaseLogic executor)
	{
		if (!LoggerRegistry.__commandBlock)
		{
			return;
		}

		long time = world.getGameTime();
		String commandPreview = executor.getCommand();
		if (commandPreview.length() > MAXIMUM_PREVIEW_LENGTH)
		{
			commandPreview = commandPreview.substring(0, MAXIMUM_PREVIEW_LENGTH - 3) + "...";
		}
		String finalCommandPreview = commandPreview;

		LoggerRegistry.getLogger(NAME).log((option) -> {
			boolean isThrottledLogging = !option.equals("all");
			if (time - executor.getLastLoggedTime() < MINIMUM_LOG_INTERVAL && isThrottledLogging)
			{
				return null;
			}
			if (isThrottledLogging)
			{
				executor.setLastLoggedTime(time);
			}
			return new ITextComponent[]{Messenger.c(
					TextUtil.attachFormatting(TextUtil.copyText(nameText), TextFormatting.GOLD),
					TextUtil.getSpaceText(),
					"w " + this.tr("executed"),
					TextUtil.getSpaceText(),
					TextUtil.getFancyText(
							"c",
							Messenger.s(finalCommandPreview),
							Messenger.s(executor.getCommand()),
							null
					),
					"g  @ ",
					posText
			)};
		});
	}

	public void onCommandBlockActivated(World world, BlockPos pos, IBlockState state, CommandBlockBaseLogic executor)
	{
		this.logCommandBlockExecution(
				world,
				TextUtil.getBlockName(state.getBlock()),
				TextUtil.getCoordinateText("w", pos, world.getDimension().getType()),
				executor
		);
	}

	public void onCommandBlockMinecartActivated(EntityMinecartCommandBlock entity)
	{
		if (StringUtils.isNullOrEmpty(entity.getCommandBlockLogic().getCommand()))
		{
			return;
		}
		this.logCommandBlockExecution(
				entity.getEntityWorld(),
				TextUtil.getEntityText(null, entity),
				TextUtil.getCoordinateText("w", entity.getPositionVector(), entity.getEntityWorld().getDimension().getType()),
				entity.getCommandBlockLogic()
		);
	}
}
