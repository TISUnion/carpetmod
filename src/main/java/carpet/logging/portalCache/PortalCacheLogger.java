package carpet.logging.portalCache;

import carpet.logging.AbstractLogger;
import carpet.utils.Messenger;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class PortalCacheLogger extends AbstractLogger
{
	public static final String NAME = "portalCache";
	private static final PortalCacheLogger INSTANCE = new PortalCacheLogger();

	private PortalCacheLogger()
	{
		super(NAME);
	}

	public static PortalCacheLogger getInstance()
	{
		return INSTANCE;
	}

	private static DimensionType getOtherSideDimension(World world)
	{
		DimensionType dimensionType = world.getDimension().getType();
		if (dimensionType == DimensionType.OVERWORLD)
		{
			return DimensionType.NETHER;
		}
		else if (dimensionType == DimensionType.NETHER)
		{
			return DimensionType.OVERWORLD;
		}
		else
		{
			return null;
		}
	}

	public ITextComponent posRange(World dstWorld, ChunkPos pos)
	{
		DimensionType dstDim = dstWorld.getDimension().getType();
		DimensionType srcDim = getOtherSideDimension(dstWorld);

		String rangeStr = null;
		if (dstDim == DimensionType.OVERWORLD)
		{
			rangeStr = String.format("[%s, %s] -> [%s, %s]", pos.x / 8.0, pos.z / 8.0, (pos.x + 1) / 8.0, (pos.z + 1) / 8.0);
		}
		else if (dstDim == DimensionType.NETHER)
		{
			rangeStr = String.format("[%s, %s] -> [%s, %s]", pos.x * 8, pos.z * 8, (pos.x + 1) * 8, (pos.z + 1) * 8);
		}

		return Messenger.fancy(
				Messenger.format("[%s, *, %s]", pos.x, pos.z),
				rangeStr != null && srcDim != null ?
						Messenger.c(
								advTr("range", "Range in %s:", Messenger.dimension(srcDim)),
								Messenger.s("\n"),
								Messenger.s(rangeStr)
						) :
						Messenger.s("?"),
				rangeStr != null ? new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, rangeStr) : null
		);
	}

	public void onPortalCacheAdded(World dstWorld, ChunkPos pos, BlockPos target, long nowTime, Entity entity)
	{
		this.log(() -> new ITextComponent[]{Messenger.c(
				Messenger.hover(
						Messenger.s("[+] ", TextFormatting.GREEN),
						advTr("added_hover", "Now Time: %s\nEntity: %s", nowTime, Messenger.entity(entity))
				),
				Messenger.format("(%s) ", Messenger.dimension(dstWorld)),
				advTr(
						"added", "Portal Cache added: %s -> %s",
						posRange(dstWorld, pos),
						Messenger.coord(target, dstWorld.getDimension().getType())
				)
		)});
	}

	public void onPortalCacheDeleted(World dstWorld, ChunkPos pos, BlockPos target, long lastUpdateTime, long nowTime)
	{
		this.log(() -> new ITextComponent[]{Messenger.c(
				Messenger.hover(
						Messenger.s("[-] ", TextFormatting.RED),
						advTr("deleted_hover", "Now Time: %s\nLast Update: %s", nowTime, lastUpdateTime)
				),
				Messenger.format("(-> %s) ", Messenger.dimension(dstWorld)),
				advTr(
						"deleted", "Portal Cache deleted: %s -> %s",
						posRange(dstWorld, pos),
						Messenger.coord(target, dstWorld.getDimension().getType())
				)
		)});
	}
}
