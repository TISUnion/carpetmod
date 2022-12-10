package carpet.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

	public static Optional<Object> construct(Class<?> clazz, Class<?>[] parameterTypes, Object[] args)
	{
		try
		{
			Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
			constructor.setAccessible(true);
			return Optional.of(constructor.newInstance(args));
		}
		catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e)
		{
			return Optional.empty();
		}
	}
}
