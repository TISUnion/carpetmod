package carpet.logging.microtiming.marker;

import carpet.logging.microtiming.utils.MicroTimingUtil;
import carpet.script.value.ListValue;
import carpet.script.value.NumericValue;
import carpet.script.value.Value;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import com.google.common.collect.Maps;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.Map;

public class MicroTimingMarker
{
	public static final int MARKER_SYNC_INTERVAL = 5 * 20;  // 5sec
	public static final int MARKER_RENDER_DURATION = 10 * 60 * 20;  // 5min
	// 5min / 5sec = 60, so it should works fine unless the server tps is below 20 / 60 = 0.333

	private final WorldServer serverWorld;
	private final BlockPos blockPos;
	public final EnumDyeColor color;
//	private final ShapeData<ShapeDispatcher.Box> box;
//	@Nullable
//	private final ShapeData<ShapeDispatcher.Text> text;
//	@Nullable
	private final ITextComponent markerName;
	private MicroTimingMarkerType markerType;
	private boolean movable;

	public long tickCounter;

	@SuppressWarnings("ConstantConditions")
	private MicroTimingMarker(WorldServer serverWorld, BlockPos blockPos, EnumDyeColor color, @Nullable ITextComponent markerName, MicroTimingMarkerType markerType, boolean movable)
	{
		this.serverWorld = serverWorld;
		this.blockPos = blockPos;
		this.color = color;
		this.markerName = markerName;
		this.markerType = markerType;
		this.movable = movable;
		Map<String, Value> boxParams = Maps.newHashMap();
//		boxParams.put("shape", new StringValue("box"));
//		boxParams.put("color", new NumericValue(((long) ((EnumDyeColorAccessor) (Object) this.color).getTextColor() << 8) | 0xAF));
//		boxParams.put("dim", new StringValue(serverWorld.getDimension().getType().toString()));
//		boxParams.put("duration", new NumericValue(Integer.MAX_VALUE));
//		boxParams.put("from", listFromBlockPos(blockPos));
//		boxParams.put("to", listFromBlockPos(blockPos.add(1, 1, 1)));
//		this.box = new ShapeData<>(new ShapeDispatcher.Box(), boxParams);
//		if (this.markerName != null)
//		{
//			Map<String, Value> textParams = Maps.newHashMap();
//			textParams.put("shape", new StringValue("label"));
//			textParams.put("dim", new StringValue(serverWorld.getDimension().getType().toString()));
//			textParams.put("duration", new NumericValue(Integer.MAX_VALUE));
//			textParams.put("pos", ListValue.of(new NumericValue(blockPos.getX() + 0.5D), new NumericValue(blockPos.getY() + 0.5D), new NumericValue(blockPos.getZ() + 0.5D)));
//			textParams.put("text", new FormattedTextValue(Messenger.c(Messenger.s(Messenger.parseCarpetStyle(MicroTimingUtil.getColorStyle(this.color)).getColor() + "# " + Formatting.RESET), Messenger.copy(this.markerName))));
//			textParams.put("align", new StringValue(ScarpetDisplayedTextHack.MICRO_TIMING_TEXT_MAGIC_STRING));
//			this.text = new ShapeData<>(new ShapeDispatcher.Text(), textParams);
//		}
//		else
//		{
//			this.text = null;
//		}
		this.updateLineWidth();
	}

	public MicroTimingMarker(WorldServer serverWorld, BlockPos blockPos, EnumDyeColor color, @Nullable ITextComponent markerName)
	{
		this(serverWorld, blockPos, color, markerName, MicroTimingMarkerType.REGULAR, false);
	}

	public boolean isMovable()
	{
		return this.movable;
	}

	public void setMovable(boolean movable)
	{
		this.movable = movable;
	}

	@Nullable
	public String getMarkerNameString()
	{
		return this.markerName != null ? this.markerName.getString() : null;
	}

	public MicroTimingMarkerType getMarkerType()
	{
		return this.markerType;
	}

	private void updateLineWidth()
	{
//		this.box.updateLineWidth(this.markerType.getLineWidth());
	}

	public boolean rollMarkerType()
	{
		boolean hasNext;
		switch (this.markerType)
		{
			case REGULAR:
				this.markerType = MicroTimingMarkerType.END_ROD;
				hasNext = true;
				break;
			case END_ROD:
			default:
				hasNext = false;
				break;
		}
		if (hasNext)
		{
			this.cleanShapeToAll();
			this.updateLineWidth();
			this.sendShapeToAll();
		}
		return hasNext;
	}

	private static ListValue listFromBlockPos(BlockPos blockPos)
	{
		return ListValue.of(new NumericValue(blockPos.getX()), new NumericValue(blockPos.getY()), new NumericValue(blockPos.getZ()));
	}

//	public List<Pair<ShapeDispatcher.ExpiringShape, Map<String, Value>>> getShapeDataList(boolean display)
//	{
//		List<Pair<ShapeDispatcher.ExpiringShape, Map<String, Value>>> result = Lists.newArrayList();
//		result.add(this.box.toPair(display));
//		if (this.text != null)
//		{
//			result.add(this.text.toPair(display));
//		}
//		return result;
//	}

	public void sendShapeToAll()
	{
//		ShapeDispatcher.sendShape(MicroTimingUtil.getSubscribedPlayers(), this.getShapeDataList(true));
	}

	public void cleanShapeToAll()
	{
//		ShapeDispatcher.sendShape(MicroTimingUtil.getSubscribedPlayers(), this.getShapeDataList(false));
	}

	public StorageKey getStorageKey()
	{
		return new StorageKey(this.serverWorld, this.blockPos);
	}

	/**
	 * Create a copied marker at offset direction
	 */
	public MicroTimingMarker offsetCopy(EnumFacing direction)
	{
		return new MicroTimingMarker(this.serverWorld, this.blockPos.offset(direction), this.color, this.markerName, this.markerType, this.movable);
	}

	// 1.15 client cannot response to text component color, so just use Formatting symbol here
	private ITextComponent withFormattingSymbol(String text)
	{
		return Messenger.s(Messenger.parseCarpetStyle(MicroTimingUtil.getColorStyle(color)).getColor() + text + TextFormatting.RESET);
	}

	// [1, 2, 3]
	public ITextComponent toShortText()
	{
		return this.withFormattingSymbol(TextUtil.coord(this.blockPos));
	}

	// [1, 2, 3] red
	public ITextComponent toFullText()
	{
		return Messenger.c(
				Messenger.coord(this.blockPos),
				this.withFormattingSymbol(" " + this.color.toString())
		);
	}

//	private static class ShapeData<T extends ShapeDispatcher.ExpiringShape>
//	{
//		public final T shape;
//		public final Map<String, Value> params;
//		public final Map<String, Value> emptyParams;
//
//		private ShapeData(T shape, Map<String, Value> params)
//		{
//			this.shape = shape;
//			this.params = params;
//			this.emptyParams = Maps.newHashMap(this.params);
//			this.emptyParams.put("duration", new NumericValue(0));
//			this.syncShapeInstance();
//		}
//
//		private void syncShapeInstance()
//		{
//			// the shape instance is useful for non-carpet players
//			((ExpiringShapeInvoker)this.shape).callInit(this.params);
//		}
//
//		private Pair<ShapeDispatcher.ExpiringShape, Map<String, Value>> toPair(boolean display)
//		{
//			return Pair.of(this.shape, display ? this.params : this.emptyParams);
//		}
//
//		public void updateLineWidth(float lineWidth)
//		{
//			this.params.put("line", new NumericValue(lineWidth));
//			this.emptyParams.put("line", new NumericValue(lineWidth));
//			this.syncShapeInstance();
//		}
//	}
}
