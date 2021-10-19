package carpet.logging.microtiming.marker;

import net.minecraft.util.text.TextFormatting;

public enum MicroTimingMarkerType
{
	/**
	 * Dont log block update
	 */
	REGULAR(2.5F, TextFormatting.GRAY),
	/**
	 * Log everything
	 */
	END_ROD(7.0F, TextFormatting.LIGHT_PURPLE);

	public final float lineWidth;
	private final TextFormatting color;

	MicroTimingMarkerType(float lineWidth, TextFormatting color)
	{
		this.lineWidth = lineWidth;
		this.color = color;
	}

	public float getLineWidth()
	{
		return this.lineWidth;
	}

	public String getFancyString()
	{
		return this.color.toString() + super.toString() + TextFormatting.RESET;
	}
}
