package eu.fox7.rexp.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class RuleTransformer {
	public static void main(String[] args) {
		String s = "file:/D:/home/dev/data/.rexp/xsd/filtered/www.assembla.com/wml.xsd";
		String rule = "file://?/?($prefix/?)(.*)>>http://$2";
		String vars = "prefix=D:/home/dev/data/.rexp/xsd/filtered";
		RuleTransformer rt = new RuleTransformer(rule, vars);
		System.out.println(rt.apply(s));
	}

	private static final String HEAD_BODY_DELIM = ">>";
	private static final String VARS_DELIM = ",";
	private static final String ASSIGN_DELIM = "=";

	private String body;
	private String head;
	private Map<String, String> varMap;

	public RuleTransformer(String rule, String vars) {
		String[] rs = rule.split(HEAD_BODY_DELIM);
		if (rs.length == 2) {
			body = rs[0];
			head = rs[1];
		}

		varMap = new LinkedHashMap<String, String>();
		String[] vs = vars.split(VARS_DELIM);
		for (String var : vs) {
			String[] as = var.split(ASSIGN_DELIM);
			if (as.length == 2) {
				varMap.put(as[0], as[1]);
			}
		}
	}

	public String apply(String s) {
		return transform(s, head, body, varMap);
	}

	public static String transform(String s, String head, String body, Map<String, String> varMap) {
		for (Entry<String, String> e : varMap.entrySet()) {
			String p = "\\$\\{?" + e.getKey() + "\\}?";
			head = head.replaceAll(p, e.getValue());
			body = body.replaceAll(p, e.getValue());
		}
		return s.replaceAll(body, head);
	}
}
