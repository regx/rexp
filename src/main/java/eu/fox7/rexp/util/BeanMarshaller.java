package eu.fox7.rexp.util;

import eu.fox7.rexp.isc.analysis2.mvn.MvnRepo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class BeanMarshaller {
	public static void main(String[] args) {
		Object o = new MvnRepo.Doc();
		Map<String, String> map = new LinkedHashMap<String, String>();
		toMap(map, o);
		System.out.println(PrettyPrinter.toString(map));

		map.put("id", "2");
		toBean(o, map);
		System.out.println(o);
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FieldName {
		String value();
	}

	public static <V> void toBean(Object tgtBean, Map<String, String> srcMap) {
		Class<?> c = tgtBean.getClass();
		for (Field f : c.getFields()) {
			FieldName a = f.getAnnotation(FieldName.class);
			String name = (a != null) ? a.value() : f.getName();
			Object value = fromString(srcMap.get(name), f.getType());
			try {
				if (value != null) {
					f.setAccessible(true);
					f.set(tgtBean, value);
				}
			} catch (IllegalArgumentException ignored) {
			} catch (IllegalAccessException ignored) {
			}
		}
	}
	public static Object fromString(String objectStr, Class<?> type) {
		if (String.class.equals(type)) {
			return objectStr;

		} else if (long.class.equals(type)) {
			return Long.parseLong(objectStr);
		} else if (int.class.equals(type)) {
			return Integer.parseInt(objectStr);
		} else if (short.class.equals(type)) {
			return Short.parseShort(objectStr);
		} else if (double.class.equals(type)) {
			return Double.parseDouble(objectStr);
		} else if (float.class.equals(type)) {
			return Float.parseFloat(objectStr);

		} else if (Long.class.equals(type)) {
			return Long.parseLong(objectStr);
		} else if (Integer.class.equals(type)) {
			return Integer.parseInt(objectStr);
		} else if (Short.class.equals(type)) {
			return Short.parseShort(objectStr);
		} else if (Double.class.equals(type)) {
			return Double.parseDouble(objectStr);
		} else if (Float.class.equals(type)) {
			return Float.parseFloat(objectStr);

		} else {
			return null;
		}
	}

	public static <V> void toMap(Map<String, String> tgtMap, Object srcBean) {
		Class<?> c = srcBean.getClass();
		for (Field f : c.getFields()) {
			FieldName a = f.getAnnotation(FieldName.class);
			String name = (a != null) ? a.value() : f.getName();

			try {
				f.setAccessible(true);
				String value = String.valueOf(f.get(srcBean));
				tgtMap.put(name, value);
			} catch (IllegalArgumentException ignored) {
			} catch (IllegalAccessException ignored) {
			}
		}
	}

	public static <T> void toMapList(Collection<Map<String, String>> mapList, Collection<T> c) {
		for (T item : c) {
			Map<String, String> map = new LinkedHashMap<String, String>();
			BeanMarshaller.toMap(map, item);
			mapList.add(map);
		}
	}

	public static <T> void toBeanList(Collection<T> c, Collection<Map<String, String>> mapList, Class<T> type) {
		for (Map<String, String> map : mapList) {
			try {
				T bean = type.newInstance();
				BeanMarshaller.toBean(bean, map);
				c.add(bean);
			} catch (InstantiationException ignored) {
			} catch (IllegalAccessException ignored) {
			}
		}
	}
}
