package eu.fox7.rexp.util.incubator;

import eu.fox7.rexp.App;
import eu.fox7.rexp.util.mini.Callback;
import eu.fox7.rexp.util.mini.Transform;
import eu.fox7.rexp.xml.schema.XSchema;
import eu.fox7.rexp.xml.schema.Xsd2XSchema;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

import java.io.IOException;
import java.util.*;

public class ClassCollector {
	public static void main(String[] args) throws IOException {
		doStuffToLoadIt();

		String prefix = App.class.getPackage().getName();

		Set<Class<?>> set = ClassCollector.getAllClassesUsedBy(ClassCollector.class, prefix);
		for (Class<?> c : set) {
			String name = c.getName();
			if (!name.contains("$")) {
				System.out.println(name);
			}
		}
	}

	static void doStuffToLoadIt() {
		System.out.println(new XSchema().toPrettyString());
		System.out.println(new Xsd2XSchema().toString());
		System.out.println("----");
	}

	public static class SimpleCollector extends Collector {

		public SimpleCollector(final Set<Class<?>> classNames, final String prefix) {
			super(
				new Callback<Class<?>>() {
					@Override
					public void call(Class<?> data) {
						classNames.add(data);
					}
				},
				new Transform<Boolean, String>() {
					@Override
					public Boolean transform(String data) {
						return data.startsWith(prefix);
					}
				}
			);
		}
	}

	public static class Collector extends Remapper {
		private final Callback<Class<?>> outputter;
		private final Transform<Boolean, String> checker;

		public Collector(final Callback<Class<?>> outputter, Transform<Boolean, String> checker) {
			this.outputter = outputter;
			this.checker = checker;
		}

		@Override
		public String mapDesc(final String desc) {
			if (desc.startsWith("L")) {
				this.addType(desc.substring(1, desc.length() - 1));
			}
			return super.mapDesc(desc);
		}

		@Override
		public String[] mapTypes(final String[] typeNames) {
			for (final String typeName : typeNames) {
				this.addType(typeName);
			}
			return super.mapTypes(typeNames);
		}

		private void addType(final String typeName) {
			final String className = typeName.replace('/', '.');
			if (checker.transform(className)) {
				try {
					this.outputter.call(Class.forName(className));
				} catch (final ClassNotFoundException e) {
					throw new IllegalStateException(e);
				}
			}
		}

		@Override
		public String mapType(final String type) {
			this.addType(type);
			return type;
		}
	}

	private static class EmptyVisitor extends ClassVisitor {
		public EmptyVisitor(int api) {
			super(api);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodVisitor mv = new MethodVisitor(org.objectweb.asm.Opcodes.ASM4) {
			};
			return mv;
		}
	}

	public static Set<Class<?>> getClassesUsedBy(final String name, final String prefix) throws IOException {
		final ClassReader reader = new ClassReader(name);
		final Set<Class<?>> classes = new TreeSet<Class<?>>(CLASS_COMPARATOR);
		final Remapper remapper = new SimpleCollector(classes, prefix);
		final ClassVisitor inner = new EmptyVisitor(org.objectweb.asm.Opcodes.ASM4);
		final RemappingClassAdapter visitor = new RemappingClassAdapter(inner, remapper);
		reader.accept(visitor, ClassReader.EXPAND_FRAMES);
		return classes;
	}

	private static final Comparator<Class<?>> CLASS_COMPARATOR = new Comparator<Class<?>>() {
		@Override
		public int compare(final Class<?> o1, final Class<?> o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	public static Set<Class<?>> getAllClassesUsedBy(final Class<?> type, final String prefix) throws IOException {
		final SortedSet<Class<?>> classes = new TreeSet<Class<?>>(CLASS_COMPARATOR);
		classes.add(type);
		fixpoint(CLASS_COMPARATOR, classes, new ElementToSet<Class<?>>() {
			@Override
			public void calculate(Collection<Class<?>> outCollection, Class<?> inElem) {
				try {
					Set<Class<?>> c = getClassesUsedBy(inElem.getName(), prefix);
					outCollection.addAll(c);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		return classes;
	}

	public static interface ElementToSet<T> {
		void calculate(Collection<T> outCollection, T inElem);
	}

	public static <T> void fixpoint(Comparator<T> comparator, SortedSet<T> out, ElementToSet<T> func) {
		LinkedList<T> input = new LinkedList<T>(out);
		while (!input.isEmpty()) {
			T elem = input.remove();
			SortedSet<T> tempOut = new TreeSet<T>(comparator);
			func.calculate(tempOut, elem);
			tempOut.removeAll(out);
			input.addAll(tempOut);
			out.addAll(tempOut);
		}
	}
}
