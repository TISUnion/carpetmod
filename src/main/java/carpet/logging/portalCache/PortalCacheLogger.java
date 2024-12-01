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

	public ITextComponent posRange(World dstWorld, ChunkPos pos)
	{
		ITextComponent range = Messenger.s("?");
		String rangeStr = null;
		if (dstWorld.getDimension().getType() == DimensionType.OVERWORLD)
		{
			rangeStr = String.format("[%s, %s] -> [%s, %s]", pos.x / 8.0, pos.z / 8.0, (pos.x + 1) / 8.0, (pos.z + 1) / 8.0);
			range = Messenger.c(
					advTr("range", "Range in %s:", Messenger.dimension(DimensionType.NETHER)),
					Messenger.s("\n"),
					Messenger.s(rangeStr)
			);
		}
		else if (dstWorld.getDimension().getType() == DimensionType.NETHER)
		{
			rangeStr = String.format("[%s, %s] -> [%s, %s]", pos.x * 8, pos.z * 8, (pos.x + 1) * 8, (pos.z + 1) * 8);
			range = Messenger.c(
					advTr("range", "Range in %s:", Messenger.dimension(DimensionType.OVERWORLD)),
					Messenger.s("\n"),
					rangeStr
			);
		}
		return Messenger.fancy(
				Messenger.format("[%s, *, %s]", pos.x, pos.z),
				range,
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
						posRange(dstWorld, pos), Messenger.coord(target)
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
						posRange(dstWorld, pos), Messenger.coord(target)
				)
		)});
	}
}
