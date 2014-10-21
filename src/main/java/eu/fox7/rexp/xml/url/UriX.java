package eu.fox7.rexp.xml.url;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UriX {
	private static final String PROT = "(([^:]*):)";
	private static final String AUTH = "(([^:]*)(:([^@]*))?@)?";
	private static final String HOST = "(([^/:]*)(:(\\d*))?)";
	private static final String PATH = "((.*)/)?";
	private static final String NAME = "([^.]*)";
	private static final String EXT = "(\\.([^?]*))?";
	private static final String QUERY = "(\\?[^#]*)?";
	private static final String FRAG = "(#.*)?";
	private static final String PHOST = String.format("(%s(//%s%s))?", PROT, AUTH, HOST);
	private static final String REGEX = PHOST + PATH + NAME + EXT + QUERY + FRAG;

	protected static final String TEST_STR = "pr://un:pw@host:8080/p1/p2/name.ext?query=val#fragment";
	private static final String EMPTY_STR = "";

	protected static final int INDEX_ROOT = 1;

	protected static final int INDEX_PROT_EX = 2;
	protected static final int INDEX_PROT = 3;

	protected static final int INDEX_AUTHORITY = 4;
	protected static final int INDEX_AUTH_EX = 5;
	protected static final int INDEX_AUTH = INDEX_AUTH_EX;

	protected static final int INDEX_USER = 6;
	protected static final int INDEX_PASS_EX = 7;
	protected static final int INDEX_PASS = 8;

	protected static final int INDEX_HOST_EX = 9;
	protected static final int INDEX_HOST = 10;
	protected static final int INDEX_PORT_EX = 11;
	protected static final int INDEX_PORT = 12;

	protected static final int INDEX_PATH_EX = 13;
	protected static final int INDEX_PATH = 14;

	protected static final int INDEX_NAME = 15;

	protected static final int INDEX_EXT_EX = 16;
	protected static final int INDEX_EXT = 17;

	protected static final int INDEX_QUERY_EX = 18;
	protected static final int INDEX_QUERY = INDEX_QUERY_EX;

	protected static final int INDEX_FRAG_EX = 19;
	protected static final int INDEX_FRAG = INDEX_FRAG_EX;

	private String str;
	private Matcher m;

	public static void main(String[] args) {
		new UriX(TEST_STR).printDebugInfo();
	}

	public UriX(String str) {
		this.str = str;
		parse(str);
	}

	private void parse(String str) {
		Pattern p = Pattern.compile(REGEX);
		m = p.matcher(str);
	}

	public String protocol() {
		return extract(INDEX_PROT);
	}

	public String username() {
		return extract(INDEX_USER);
	}

	public String password() {
		return extract(INDEX_PASS);
	}

	public String fullHost() {
		return extract(INDEX_HOST_EX);
	}

	public String hostname() {
		return extract(INDEX_HOST);
	}

	public String port() {
		return extract(INDEX_PORT);
	}

	public String subPath() {
		return extract(INDEX_PATH);
	}

	public String name() {
		return extract(INDEX_NAME);
	}

	public String ext() {
		return extract(INDEX_EXT);
	}

	public String queryEx() {
		return extract(INDEX_QUERY_EX);
	}

	public String fragmentEx() {
		return extract(INDEX_FRAG_EX);
	}

	public String protocolEx() {
		return extract(INDEX_PROT_EX);
	}

	public String authority() {
		return extract(INDEX_AUTHORITY);
	}

	public String auth() {
		return extract(INDEX_AUTH_EX);
	}

	public String subPathEx() {
		return extract(INDEX_PATH_EX);
	}

	public String extEx() {
		return extract(INDEX_EXT_EX);
	}

	public String fullName() {
		return name() + extEx();
	}

	public String fullPath() {
		return subPathEx() + fullName();
	}

	public String hostedSubPath() {
		return fullHost() + subPath();
	}

	public String hostedSubPathEx() {
		return fullHost() + subPathEx();
	}

	public String fullHostedPath() {
		return hostedSubPathEx() + fullName();
	}

	private String extract(int i) {
		if (m.matches()) {
			String s = m.group(i);
			return s != null ? s : EMPTY_STR;
		} else {
			return EMPTY_STR;
		}
	}

	public String base() {
		return root() + subPath();
	}

	public String baseEx() {
		return root() + subPathEx();
	}

	public String root() {
		return extract(INDEX_ROOT);
	}

	@Override
	public String toString() {
		return str;
	}

	public String externalHostedSubPath() {
		return hostedSubPath().replace(":", "%3A");
	}

	public String externalFullHostedPath() {
		return fullHostedPath().replace(":", "%3A").replace("?", "%3F");
	}
	

	public static String concatBaseWithSub(String base, String sub) {
		base = base.endsWith("/") ? base : base + "/";
		return base + sub;
	}

	public static String concatPaths(String... paths) {
		StringBuilder sb = new StringBuilder();
		String last = null;
		for (String path : paths) {
			if (last != null && !last.endsWith("/")) {
				sb.append("/");
			}
			sb.append(path);
			last = path;
		}
		return sb.toString();
	}

	public static String inverseFullHostedPath(String protocol, String fullHostedPath) {
		fullHostedPath = fullHostedPath.replaceFirst("^./", "");
		return String.format("%s://%s", protocol, fullHostedPath);
	}

	public static String trimDot(String urlStr) {
		StringBuilder sb = new StringBuilder();
		final String SEP = "/";
		boolean separate = false;
		for (String part : urlStr.split(SEP)) {
			if (separate) {
				sb.append(SEP);
			}
			if (".".equals(part)) {
				separate = false;
			} else {
				sb.append(part);
				separate = true;
			}
		}
		return sb.toString();
	}
	

	protected void printDebugInfo() {
		printGroups(m);
		testNoArgMethods(this);
	}

	protected String toDebugString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(protocol());
		sb.append("|");
		sb.append(username());
		sb.append("|");
		sb.append(password());
		sb.append("|");
		sb.append(fullHost());
		sb.append("|");
		sb.append(subPath());
		sb.append("|");
		sb.append(name());
		sb.append("|");
		sb.append(ext());
		sb.append("|");
		sb.append(queryEx());
		sb.append("|");
		sb.append(fragmentEx());
		sb.append(":");
		sb.append(":");
		sb.append(fullName());
		sb.append("|");
		sb.append(fullPath());
		sb.append("|");
		sb.append(hostedSubPath());
		sb.append("|");
		sb.append(fullHostedPath());
		sb.append(")");
		return sb.toString();
	}

	protected static void testNoArgMethods(Object o) {
		for (Method m : o.getClass().getDeclaredMethods()) {
			if (m.getModifiers() == Modifier.PUBLIC) {
				if (m.getParameterTypes().length == 0) {
					try {
						Object r = m.invoke(o);
						System.out.println(String.format("%s: %s", m.getName(), r));
					} catch (IllegalAccessException ignored) {
					} catch (IllegalArgumentException ignored) {
					} catch (InvocationTargetException ignored) {
					}
				}
			}
		}
	}

	protected static void printGroups(Matcher m) {
		if (m.matches()) {
			for (int i = 0; i < m.groupCount(); i++) {
				System.out.println(String.format("Group %s: %s", i + 1, m.group(i + 1)));
			}
		}
	}
}
