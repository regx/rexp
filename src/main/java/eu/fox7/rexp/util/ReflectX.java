package eu.fox7.rexp.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
public class ReflectX {
	public static Object construct(Class<?> type, Object... args) throws InstantiationException, IllegalAccessException {
		Class<?>[] argTypes = getArgTypes(args);
		return construct(type, argTypes, args);
	}

	public static Object construct(Class<?> type, Class<?>[] argTypes, Object... args) throws InstantiationException, IllegalAccessException {
		try {
			Constructor<?> c = type.getConstructor(argTypes);
			return c.newInstance(args);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		} catch (SecurityException ex) {
			throw new RuntimeException(ex);
		} catch (InstantiationException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Object getField(Class<?> type, String name, Object object) {
		try {
			Field f = URL.class.getDeclaredField(name);
			f.setAccessible(true);
			return f.get(object);
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		} catch (SecurityException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void setField(Class<?> type, String name, Object object, Object value) {
		try {
			Field f = URL.class.getDeclaredField(name);
			f.setAccessible(true);
			f.set(object, value);
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		} catch (SecurityException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Object invoke(Class<?> type, String name, Object object, Object... args) {
		try {
			Method m = type.getDeclaredMethod(name, getArgTypes(args));
			m.setAccessible(true);
			return m.invoke(object, args);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		} catch (SecurityException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static Class<?>[] getArgTypes(Object... args) {
		Class<?>[] argTypes = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			argTypes[i] = args[i].getClass();
		}
		return argTypes;
	}
}
