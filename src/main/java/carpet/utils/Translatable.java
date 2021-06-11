package carpet.utils;

import net.minecraft.util.text.ITextComponent;

public interface Translatable
{
	String tr(String key, String text, boolean autoFormat);

	String tr(String key, String text);

	String tr(String key);

	ITextComponent advTr(String key, String defaultKeyText, Object... args);
}
