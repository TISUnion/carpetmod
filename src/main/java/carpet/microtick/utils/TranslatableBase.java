package carpet.microtick.utils;

public class TranslatableBase
{
	public String tr(String key, String text, boolean autoFormat)
	{
		return text;
	}

	public String tr(String key, String text)
	{
		return tr(key, text, false);
	}

	public String tr(String key)
	{
		return key;
	}
}
