package carpet.logging.microtiming.events;

import carpet.logging.microtiming.enums.EventType;
import carpet.logging.microtiming.utils.MicroTimingUtil;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.state.IProperty;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.*;
import java.util.function.Function;

public class BlockStateChangeEvent extends AbstractSetblockStateEvent
{
	private final Map<IProperty<?>, PropertyChange> changes = Maps.newLinkedHashMap();

	public BlockStateChangeEvent(EventType eventType, IBlockState oldBlockState, IBlockState newBlockState, Boolean returnValue, int flags)
	{
		super(eventType, "block_state_change", oldBlockState, newBlockState, returnValue, flags);
	}

	public void setChanges(Collection<PropertyChange> changes)
	{
		this.changes.clear();
		changes.forEach(change -> this.changes.put(change.property, change));
	}

	private ITextComponent getChangesText(boolean isHover)
	{
		Function<IProperty<?>, ITextComponent> hoverMaker = currentProperty -> {
			List<ITextComponent> lines = Lists.newArrayList();
			lines.add(Messenger.formatting(Messenger.s(tr("State change details")), TextFormatting.BOLD));
			this.oldBlockState.getProperties().stream().
					map(property -> {
						ITextComponent text = Optional.ofNullable(this.changes.get(property)).
								map(PropertyTexts::change).
								orElseGet(() -> {
									Object value = this.oldBlockState.get(property);
									return PropertyTexts.value(": ", property, value);
								});
						if (property.equals(currentProperty))
						{
							text.appendSibling(Messenger.s("    <---", "g"));
						}
						return text;
					}).
					forEach(lines::add);
			return Messenger.join(Messenger.s("\n"), lines.toArray(new ITextComponent[0]));
		};
		if (isHover)
		{
			return hoverMaker.apply(null);
		}
		else
		{
			return Messenger.join(
					Messenger.s(" "),
					this.changes.values().stream().
							map(change -> Messenger.hover(
									PropertyTexts.value("=", change.property, change.newValue),
									hoverMaker.apply(change.property)
							)).
							toArray(ITextComponent[]::new)
			);
		}
	}

	@Override
	public ITextComponent toText()
	{
		List<Object> list = Lists.newArrayList();
		list.add(this.getEnclosedTranslatedBlockNameHeaderText(this.oldBlockState.getBlock()));
		ITextComponent titleText = Messenger.fancy(
				null,
				Messenger.c(COLOR_ACTION + this.tr("State Change")),
				this.getFlagsText(),
				null
		);
		if (this.getEventType() != EventType.ACTION_END)
		{
			list.add(Messenger.c(
					titleText,
					"g : ",
					this.getChangesText(false)
			));
		}
		else
		{
			list.add(Messenger.fancy(
					null,
					Messenger.c(titleText, Messenger.getSpaceText(), COLOR_RESULT + this.tr("finished")),
					this.getChangesText(true),
					null
			));
		}
		if (this.returnValue != null)
		{
			list.add("w  ");
			list.add(MicroTimingUtil.getSuccessText(this.returnValue, true));
		}
		return Messenger.c(list.toArray(new Object[0]));
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		BlockStateChangeEvent that = (BlockStateChangeEvent) o;
		return Objects.equals(changes, that.changes);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), changes);
	}

	public static class PropertyTexts
	{
		// xxx${divider}aaa
		public static ITextComponent value(String divider, IProperty<?> property, Object value)
		{
			return Messenger.c(
					Messenger.s(property.getName()),
					"g " + divider,
					Messenger.property(property, value)
			);
		}

		// xxx: aaa->bbb
		public static ITextComponent change(IProperty<?> property, Object oldValue, Object newValue)
		{
			return Messenger.c(
					Messenger.s(property.getName()),
					"g : ",
					Messenger.property(property, oldValue),
					"g ->",
					Messenger.property(property, newValue)
			);
		}

		public static ITextComponent change(PropertyChange propertyChange)
		{
			return change(propertyChange.property, propertyChange.oldValue, propertyChange.newValue);
		}
	}

	public static class PropertyChange
	{
		public final IProperty<?> property;
		public final Object oldValue;
		public final Object newValue;

		public PropertyChange(IProperty<?> property, Object oldValue, Object newValue)
		{
			this.property = property;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof PropertyChange)) return false;
			PropertyChange changes = (PropertyChange) o;
			return Objects.equals(property, changes.property) &&
					Objects.equals(oldValue, changes.oldValue) &&
					Objects.equals(newValue, changes.newValue);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(property, oldValue, newValue);
		}
	}
}
