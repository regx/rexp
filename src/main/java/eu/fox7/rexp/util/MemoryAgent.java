package eu.fox7.rexp.util;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Stack;
public class MemoryAgent {
	private static Instrumentation instrumentation;

	public static void premain(String agentArgs, Instrumentation inst) {
		instrumentation = inst;
	}

	public static void agentmain(String agentArgs, Instrumentation inst) {
		instrumentation = inst;
	}

	public static boolean isAvailable() {
		return instrumentation != null;
	}

	public static long sizeOf(Object obj) {
		if (instrumentation == null) {
			throw new IllegalStateException("Instrumentation environment not initialised.");
		}
		if (isSharedFlyweight(obj)) {
			return 0;
		}
		return instrumentation.getObjectSize(obj);
	}

	public static long deepSizeOf(Object obj) {
		Map<Object, Object> visited = new IdentityHashMap<Object, Object>();
		Stack<Object> stack = new Stack<Object>();
		stack.push(obj);

		long result = 0;
		do {
			result += internalSizeOf(stack.pop(), stack, visited);
		} while (!stack.isEmpty());
		return result;
	}

	private static boolean isSharedFlyweight(Object obj) {
		if (obj instanceof Comparable) {
			if (obj instanceof Enum) {
				return true;
			} else if (obj instanceof String) {
				return (obj == ((String) obj).intern());
			} else if (obj instanceof Boolean) {
				return (obj == Boolean.TRUE || obj == Boolean.FALSE);
			} else if (obj instanceof Integer) {
				return (obj == (Integer) obj);
			} else if (obj instanceof Short) {
				return (obj == (Short) obj);
			} else if (obj instanceof Byte) {
				return (obj == (Byte) obj);
			} else if (obj instanceof Long) {
				return (obj == (Long) obj);
			} else if (obj instanceof Character) {
				return (obj == (Character) obj);
			}
		}
		return false;
	}

	private static boolean skipObject(Object obj, Map<Object, Object> visited) {
		return obj == null
			|| visited.containsKey(obj)
			|| isSharedFlyweight(obj);
	}

	private static long internalSizeOf(Object obj, Stack<Object> stack, Map<Object, Object> visited) {
		if (skipObject(obj, visited)) {
			return 0;
		}

		Class<?> clazz = obj.getClass();
		if (clazz.isArray()) {
			addArrayElementsToStack(clazz, obj, stack);
		} else {
			while (clazz != null) {
				Field[] fields = clazz.getDeclaredFields();
				for (Field field : fields) {
					if (!Modifier.isStatic(field.getModifiers())
						&& !field.getType().isPrimitive()) {
						field.setAccessible(true);
						try {
							stack.add(field.get(obj));
						} catch (IllegalAccessException ex) {
							throw new RuntimeException(ex);
						}
					}
				}
				clazz = clazz.getSuperclass();
			}
		}
		visited.put(obj, null);
		return sizeOf(obj);
	}

	private static void addArrayElementsToStack(Class<?> clazz, Object obj, Stack<Object> stack) {
		if (!clazz.getComponentType().isPrimitive()) {
			int length = Array.getLength(obj);
			for (int i = 0; i < length; i++) {
				stack.add(Array.get(obj, i));
			}
		}
	}
}
