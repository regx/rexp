package eu.fox7.rexp.isc.analysis2.gwc;

import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cli;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.PrettyPrinter;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.*;

public class MetaScanner {
	public static void main(String[] args) throws Exception {
		List<Cmd> cmds = new LinkedList<Cmd>();
		addMissingTypes(cmds, Parameters.class);
		System.out.println(PrettyPrinter.toString(cmds));
	}

	public static <T> void addMissingTypes(Collection<T> c, Class<? extends Annotation> markType) {
		Set<Class<?>> marked = safeCollect(markType);
		Set<Class<?>> added = getElementTypes(c);
		Set<Class<?>> set = new LinkedHashSet<Class<?>>(marked);
		set.removeAll(added);
		for (Class<?> type : set) {
			try {
				@SuppressWarnings("unchecked")
				T e = (T) type.newInstance();
				c.add(e);
			} catch (InstantiationException ex) {
				Log.v("%s", ex);
			} catch (IllegalAccessException ex) {
				Log.w("%s", ex);
			}
		}
	}

	protected static <T> Set<Class<?>> getElementTypes(Collection<T> c) {
		Set<Class<?>> types = new LinkedHashSet<Class<?>>();
		for (T obj : c) {
			types.add(obj.getClass());
		}
		return types;
	}

	protected static <T> Set<Class<?>> safeCollect(Class<? extends Annotation> markType) {
		String thisPackage = MetaScanner.class.getPackage().getName();
		String thatPackage = Cli.class.getPackage().getName();
		String packageName = getCommonPrefix(thisPackage, thatPackage);
		packageName = packageName.substring(0, packageName.length() - 1);
		Reflections reflections = new Reflections(packageName);
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(markType);
		return annotated;
	}

	public static String getCommonPrefix(String str1, String str2) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str1.length(); i++) {
			if (str1.charAt(i) == str2.charAt(i)) {
				sb.append(str1.charAt(i));
			} else {
				break;
			}
		}
		return sb.toString();
	}
}
