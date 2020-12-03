package carpet.microtiming.enums;

import carpet.microtiming.MicroTimingLoggerManager;
import carpet.microtiming.utils.MicroTimingUtil;
import carpet.utils.TextUtil;
import com.google.common.base.Joiner;
import net.minecraft.util.EnumFacing;

public enum BlockUpdateType
{
	BLOCK_UPDATE("BlockUpdates", new String[]{"Neighbor Changed", "Neighbor Update"}, Constants.BLOCK_UPDATE_ORDER),
	BLOCK_UPDATE_EXCEPT("BlockUpdates Except", new String[]{"Neighbor Changed Except", "Neighbor Update Except"}, Constants.BLOCK_UPDATE_ORDER),
	STATE_UPDATE("StateUpdates", new String[]{"Post Placement", "Update Shape"}, Constants.STATE_UPDATE_ORDER);

	private final String name;
	private final String[] aka;
	private final EnumFacing[] updateOrder;

	BlockUpdateType(String name, String[] aka, EnumFacing[] updateOrder)
	{
		this.name = name;
		this.aka = aka;
		this.updateOrder = updateOrder;
	}

	private String tr(String text)
	{
		return MicroTimingLoggerManager.tr("block_update_type." + text, text, true);
	}

	@Override
	public String toString()
	{
		return tr(this.name);
	}

	public String getUpdateOrderList(EnumFacing skipSide)
	{
		int counter = 0;
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(tr("aka"));
		stringBuilder.append(TextUtil.getSpace());
		stringBuilder.append(Joiner.on(", ").join(this.aka));
		stringBuilder.append('\n');
		for (EnumFacing direction : this.updateOrder)
		{
			if (skipSide != direction)
			{
				if (counter > 0)
				{
					stringBuilder.append('\n');
				}
				stringBuilder.append(String.format("%d. %s", (++counter), MicroTimingUtil.getFormattedDirectionString(direction)));
			}
		}
		if (skipSide != null)
		{
			stringBuilder.append(String.format("\n%s: %s", tr("Except"), skipSide));
		}
		return stringBuilder.toString();
	}

	static class Constants
	{
		static final EnumFacing[] BLOCK_UPDATE_ORDER = new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH};
		static final EnumFacing[] STATE_UPDATE_ORDER = new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.UP};  // the same as Block.FACINGS
	}
}
