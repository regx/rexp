package eu.fox7.rexp.isc.experiment4.db;

public abstract class QueryBuilder<T> {
	private StringBuilder cmd;

	public abstract T getProxy();

	public QueryBuilder() {
		this.cmd = new StringBuilder();
	}

	public T c() {
		cmd.setLength(0);
		return getProxy();
	}

	public T s(String str) {
		cmd.append(str);
		return getProxy();
	}

	public T s(int i) {
		cmd.append(String.valueOf(i));
		return getProxy();
	}

	public T s(StringBuilder sb) {
		cmd.append(sb);
		return getProxy();
	}

	public StringBuilder mk() {
		return new StringBuilder(cmd.toString());
	}

	public String getCmd() {
		String scmd = cmd.toString();
		return scmd;
	}
}
