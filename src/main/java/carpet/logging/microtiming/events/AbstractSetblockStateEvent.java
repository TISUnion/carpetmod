package carpet.logging.microtiming.events;

import carpet.logging.microtiming.enums.EventType;
import carpet.logging.microtiming.utils.MicroTimingUtil;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AbstractSetblockStateEvent extends BaseEvent
{
	protected final IBlockState oldBlockState;
	protected final IBlockState newBlockState;
	protected final int flags;
	@Nullable
	protected Boolean returnValue;

	private static final List<FlagData> SET_BLOCK_STATE_FLAGS = Lists.newArrayList();

	static
	{
		SET_BLOCK_STATE_FLAGS.add(new FlagData(1, "emits block updates", false));
		SET_BLOCK_STATE_FLAGS.add(new FlagData(2, "updates listeners", false));
		SET_BLOCK_STATE_FLAGS.add(new FlagData(4, "updates client listeners", true));
		SET_BLOCK_STATE_FLAGS.add(new FlagData(8, null, false));
		SET_BLOCK_STATE_FLAGS.add(new FlagData(16, "emits state updates", true));
		SET_BLOCK_STATE_FLAGS.add(new FlagData(32, null, false));
		SET_BLOCK_STATE_FLAGS.add(new FlagData(64, "caused by piston", false));
		SET_BLOCK_STATE_FLAGS.add(new FlagData(128, null, false));
	}

	protected AbstractSetblockStateEvent(EventType eventType, String translateKey, IBlockState oldBlockState, IBlockState newBlockState, @Nullable Boolean returnValue, int flags)
	{
		super(eventType, translateKey);
		this.oldBlockState = oldBlockState;
		this.newBlockState = newBlockState;
		this.returnValue = returnValue;
		this.flags = flags;
	}

	protected ITextComponent getFlagsText()
	{
		String bits = Integer.toBinaryString(this.flags);
		bits = String.join("", Collections.nCopies(Math.max(SET_BLOCK_STATE_FLAGS.size() - bits.length(), 0), "0")) + bits;
		List<Object> list = Lists.newArrayList();
		list.add(Messenger.s(String.format("setBlockState flags = %d (%s)", this.flags, bits)));
		for (FlagData flagData: SET_BLOCK_STATE_FLAGS)
		{
			if (flagData.isValid())
			{
				int currentBit = (this.flags & flagData.mask) > 0 ? 1 : 0;
				list.add(Messenger.c(
						String.format("w \nbit %d = %d: ", flagData.bitPos, currentBit),
						String.format("^w 2^%d = %d", flagData.bitPos, flagData.mask),
						MicroTimingUtil.getSuccessText((currentBit ^ flagData.revert) != 0, false),
						"w  ",
						Messenger.s(this.tr("flag_data." + flagData.bitPos, flagData.detail))
				));
			}
		}
		return Messenger.c(list.toArray(new Object[0]));
	}

	@Override
	public void mergeQuitEvent(BaseEvent quitEvent)
	{
		super.mergeQuitEvent(quitEvent);
		if (quitEvent instanceof AbstractSetblockStateEvent)
		{
			this.returnValue = ((AbstractSetblockStateEvent)quitEvent).returnValue;
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		AbstractSetblockStateEvent that = (AbstractSetblockStateEvent) o;
		return flags == that.flags && Objects.equals(oldBlockState, that.oldBlockState) && Objects.equals(newBlockState, that.newBlockState) && Objects.equals(returnValue, that.returnValue);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), oldBlockState, newBlockState, flags, returnValue);
	}

	private static class FlagData
	{
		private final int mask;
		private final String detail;
		private final int revert;
		private final int bitPos;

		private FlagData(int mask, String detail, boolean revert)
		{
			if (mask <= 0)
			{
				throw new IllegalArgumentException(String.format("mask = %d < 0", mask));
			}
			this.mask = mask;
			this.detail = detail;
			this.revert = revert ? 1 : 0;
			int pos = 0;
			for (int n = this.mask; n > 0; n >>= 1)
			{
				pos++;
			}
			this.bitPos = pos - 1;
		}

		private boolean isValid()
		{
			return this.detail != null;
		}
	}
}
