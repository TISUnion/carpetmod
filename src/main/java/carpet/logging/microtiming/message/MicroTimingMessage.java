package carpet.logging.microtiming.message;

import carpet.logging.microtiming.MicroTimingLogger;
import carpet.logging.microtiming.MicroTimingLoggerManager;
import carpet.logging.microtiming.enums.EventType;
import carpet.logging.microtiming.enums.TickStage;
import carpet.logging.microtiming.events.BaseEvent;
import carpet.logging.microtiming.tickstages.TickStageExtraBase;
import carpet.logging.microtiming.utils.MicroTimingContext;
import carpet.logging.microtiming.utils.MicroTimingUtil;
import carpet.utils.TextUtil;
import carpet.utils.Messenger;
import carpet.utils.deobfuscator.StackTracePrinter;
import com.google.common.collect.Lists;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.dimension.DimensionType;

import java.util.List;
import java.util.Objects;

import static java.lang.Integer.min;

public class MicroTimingMessage
{
	private static final int MAX_INDENT = 10;
	private static final int SPACE_PER_INDENT = 2;
	private static final List<String> INDENTATIONS = Lists.newArrayList();
	static
	{
		String indent = "";
		for (int i = 0; i <= MAX_INDENT; i++)
		{
			INDENTATIONS.add(indent);
			for (int j = 0; j < SPACE_PER_INDENT; j++)
			{
				indent += ' ';
			}
		}
	}

	private final DimensionType dimensionType;
	private final BlockPos pos;
	private final EnumDyeColor color;
	private final TickStage stage;
	private final String stageDetail;
	private final TickStageExtraBase stageExtra;
	private final ITextComponent stackTraceText;
	private final BaseEvent event;
	private final String blockName;

	public MicroTimingMessage(MicroTimingLogger logger, MicroTimingContext context)
	{
		this.dimensionType = context.getWorld().getDimension().getType();
		this.pos = context.getBlockPos();
		this.color = context.getColor();
		this.event = context.getEventSupplier().get();
		this.blockName = context.getBlockName();
		this.stage = logger.getTickStage();
		this.stageDetail = logger.getTickStageDetail();
		this.stageExtra = logger.getTickStageExtra();
		this.stackTraceText = StackTracePrinter.create().ignore(MicroTimingLoggerManager.class).deobfuscate().toSymbolText();
	}

	public MessageType getMessageType()
	{
		return MessageType.fromEventType(event.getEventType());
	}

	public BaseEvent getEvent()
	{
		return this.event;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof MicroTimingMessage)) return false;
		MicroTimingMessage message = (MicroTimingMessage) o;
		return Objects.equals(dimensionType, message.dimensionType) &&
				Objects.equals(pos, message.pos) &&
				color == message.color &&
				stage == message.stage &&
				Objects.equals(stageDetail, message.stageDetail) &&
				Objects.equals(stageExtra, message.stageExtra) &&
				Objects.equals(event, message.event);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(dimensionType, pos, color, stage, stageDetail, stageExtra, event);
	}

	private ITextComponent getHashTagText()
	{
		String text = MicroTimingUtil.getColorStyle(this.color) + " # ";
		ITextComponent ret;
		if (this.pos != null)
		{
			ret = Messenger.c(
					text,
					"?" + TextUtil.tp(pos, this.dimensionType),
					String.format("^w [ %d, %d, %d ]", this.pos.getX(), this.pos.getY(), this.pos.getZ())
			);
		}
		else
		{
			ret = Messenger.c(text);
		}
		return ret;
	}

	private ITextComponent getStageText()
	{
		List<Object> stageText = Lists.newArrayList();
		stageText.add("y  " + this.stage.tr());
		if (this.stageDetail != null)
		{
			stageText.add("y ." + MicroTimingLoggerManager.tr("stage_detail." + this.stageDetail, this.stageDetail));
		}
		List<Object> hoverTextList = Lists.newArrayList();
		hoverTextList.add(this.stageExtra != null ? Messenger.c(this.stageExtra.toText(), "w \n"): Messenger.s(""));
		hoverTextList.add(String.format("w %s: ", MicroTimingLoggerManager.tr("Dimension")));
		hoverTextList.add(this.stage.isInsideWorld() ? Messenger.dimension(this.dimensionType) : "w N/A");
		return Messenger.c(
				"g  @",
				Messenger.fancy(
						null,
						Messenger.c(stageText.toArray(new Object[0])),
						Messenger.c(hoverTextList.toArray(new Object[0])),
						this.stageExtra != null ? this.stageExtra.getClickEvent() : null
				)
		);
	}

	public static ITextComponent getIndentationText(int indentation)
	{
		return Messenger.s(INDENTATIONS.get(min(indentation, MAX_INDENT)));
	}

	public ITextComponent toText(int indentation, boolean showStage)
	{
		List<Object> line = Lists.newArrayList();
		if (indentation > 0)
		{
			line.add(getIndentationText(indentation));
		}
		line.add(this.getHashTagText());
		line.add(event.toText());
		if (this.event.getEventType() != EventType.ACTION_END)
		{
			if (showStage)
			{
				line.add(this.getStageText());
			}
		}
		line.add("w  ");
		line.add(this.stackTraceText);
		return Messenger.c(line.toArray(new Object[0]));
	}

	public void mergeQuiteMessage(MicroTimingMessage quiteMessage)
	{
		if (quiteMessage != null)
		{
			this.event.mergeQuitEvent(quiteMessage.event);
		}
	}
}
