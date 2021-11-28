package carpet.helpers;

import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;

public enum DummyPropertyEnum implements IStringSerializable
{
	;
	public static final String NAME = "$TISCM$DUMMY$";
	public static final EnumProperty<DummyPropertyEnum> DUMMY_PROPERTY = EnumProperty.create(NAME, DummyPropertyEnum.class);

	@Override
	public String getName()
	{
		return NAME;
	}
}
