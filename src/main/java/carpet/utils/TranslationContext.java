package carpet.utils;

import net.minecraft.util.text.ITextComponent;

public class TranslationContext implements Translatable
{
	private final Translator translator;

	public TranslationContext(Translator translator)
	{
		this.translator = translator;
	}

	public TranslationContext(String type, String name)
	{
		this(new Translator(type, name));
	}

	public Translator getTranslator()
	{
		return translator;
	}

	@Override
	public String tr(String key, String text, boolean autoFormat)
	{
		return this.translator.tr(key, text, autoFormat);
	}

	@Override
	public String tr(String key, String text)
	{
		return this.translator.tr(key, text);
	}

	@Override
	public String tr(String key)
	{
		return this.translator.tr(key);
	}

	@Override
	public ITextComponent advTr(String key, String defaultKeyText, Object ...args)
	{
		return this.translator.advTr(key, defaultKeyText, args);
	}
}
