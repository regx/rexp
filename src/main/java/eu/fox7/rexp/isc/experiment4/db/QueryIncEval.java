package eu.fox7.rexp.isc.experiment4.db;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.experiment4.IncEval;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.mini.Transform;

public class QueryIncEval extends QueryBuilder<QueryIncEval> implements Transform<String, String> {
	public static void main(String[] args) throws Exception {
		Director.setup();
		PostProcessor.process(System.out, FileX.newFile("./analysis/bench_iest.csv"), CsvHelper.TAB, INSTANCE);
	}

	public static final QueryIncEval INSTANCE = new QueryIncEval();

	@Override
	public QueryIncEval getProxy() {
		return this;
	}

	@Override
	public String transform(String tableName) {
		String id1 = IncEval.CNFA_TREE_FACTORY.name();
		String id2 = IncEval.NFA_TREE_FACTORY.name();
		StringBuilder a1 = collect(tableName, id1, false);
		StringBuilder b1 = collect(tableName, id2, false);
		StringBuilder c1 = make(tableName, id1, id2, 1, 2, a1, b1);
		StringBuilder a2 = collect(tableName, id1, true);
		StringBuilder b2 = collect(tableName, id2, true);
		StringBuilder c2 = make(tableName, id1, id2, 3, 4, a2, b2);
		StringBuilder c = DatabaseHelper.MAKER.c()
			.s("(")
			.s(c1)
			.s(")")
			.s(" UNION ")
			.s("(")
			.s(c2)
			.s(")")
			.s(" ORDER BY TAG,x")
			.s(";").mk();
		return c.toString();
	}

	public StringBuilder make(String tableName, String id1, String id2, int n1, int n2, StringBuilder a, StringBuilder b) {
		DatabaseHelper c = DatabaseHelper.MAKER.c()
			.s("SELECT ")
			.s("T").s(n1).s(".x,")
			.s("T").s(n1).s(".TAG,")
			.s("T").s(n1).s(".")
			.s(id1)
			.s(",")
			.s("T").s(n2).s(".")
			.s(id2)
			.s(" ")
			.s("FROM")
			.s(" ")
			.s("(")
			.s(a)
			.s(") T").s(n1).s(" LEFT JOIN (")
			.s(b)
			.s(") T").s(n2)
			.s(" ON ")
			.s("T").s(n1).s(".x=T").s(n2).s(".x")
			.s(" AND ")
			.s("T").s(n1).s(".TAG=T").s(n2).s(".TAG");
		return c.mk();
	}

	public StringBuilder collect(String tableName, String id, boolean flag) {
		DatabaseHelper help = DatabaseHelper.MAKER.c()
			.s("SELECT x,")
			;

		if (!flag) {
			help.s("TAG,");
		} else {
			help.s("'O' AS TAG,");
		}

		help.s(DatabaseHelperEx.CUSTOM_AGGREGATE)
			.s("(val) AS")
			.s(" ")
			.s(id)
			.s(" ")
			.s("FROM")
			.s(" ")
			.s(tableName)
		;

		help.s(" ")
			.s("WHERE id=")
			.s("'")
			.s(id)
			.s("'")
		;

		if (flag) {
			help.s(" ")
				.s("AND")
				.s(" ")
				.s("(")
				.s("TAG=")
				.s("'")
				.s("I")
				.s("'")
				.s(" ")
				.s("OR")
				.s(" ")
				.s("TAG=")
				.s("'")
				.s("D")
				.s("'")
				.s(")")
			;
		}

		help.s(" ")
			.s("GROUP BY x")
			.s(",TAG")
			.s(" ")
			.s("ORDER BY ")
			.s("TAG,")
			.s("x")
		;
		return help.mk();
	}
}
