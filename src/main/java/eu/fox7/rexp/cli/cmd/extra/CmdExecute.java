package eu.fox7.rexp.cli.cmd.extra;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import eu.fox7.rexp.cli.Cmd;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Parameters(commandDescription = "Execute main method of class by name")
public class CmdExecute extends Cmd {
	protected static final String[] CMD_NAMES = {"x"};

	@Parameter(description = "Class names")
	protected List<String> classNames;

	@Override
	public void init() {
	}

	@Override
	public String[] getCommandNames() {
		return CMD_NAMES;
	}

	@Override
	public void execute() {
		if (classNames != null) {
			for (String className : classNames) {
				try {
					String[] args = new String[0];
					Class<?> c = Reflections.forName(className, getClass().getClassLoader());
					Class<?> a = args.getClass();
					Method m = c.getMethod("main", a);
					m.invoke(null, (Object) args);
				} catch (IllegalAccessException ex) {
					println("Could not invoke %s", className);
					throw new RuntimeException(ex);
				} catch (IllegalArgumentException ex) {
					println("Could not invoke %s", className);
					throw new RuntimeException(ex);
				} catch (InvocationTargetException ex) {
					println("Could not invoke %s", className);
					throw new RuntimeException(ex);
				} catch (NoSuchMethodException ex) {
					println("Could not invoke %s", className);
					throw new RuntimeException(ex);
				} catch (SecurityException ex) {
					println("Could not invoke %s", className);
					throw new RuntimeException(ex);
				}
			}
		}
	}
}
