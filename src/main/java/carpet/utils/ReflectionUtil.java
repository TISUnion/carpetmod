package carpet.utils;

import java.lang.reflect.Field;
import java.util.Optional;

public class ReflectionUtil
{
	public static Optional<Object> getField(Object object, String fieldName)
	{
		try
		{
			Field field = object.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return Optional.ofNullable(field.get(object));
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			return Optional.empty();
		}
	}
}
