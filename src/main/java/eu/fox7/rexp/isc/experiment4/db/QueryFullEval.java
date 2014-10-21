package eu.fox7.rexp.isc.experiment4.db;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.experiment4.ScratchEval;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.mini.Transform;

public class QueryFullEval extends QueryBuilder<QueryFullEval> implements Transform<String, String> {
	public static void main(String[] args) throws Exception {
		Director.setup();
		PostProcessor.process(System.out, FileX.newFile("./analysis/bench_fest.csv"), CsvHelper.TAB, INSTANCE1);
	}

	public static final QueryFullEval INSTANCE1 = new QueryFullEval(true);
	public static final QueryFullEval INSTANCE2 = new QueryFullEval(false);

	private static final String[] ids = {
		ScratchEval.CNFA_SIM.getTester().id(),
		ScratchEval.NFA_SIM.getTester().id(),
		ScratchEval.RE_SHARP.getTester().id(),
		ScratchEval.CNFA_INC.getTester().id(),
		ScratchEval.NFA_INC.getTester().id(),
	};

	private final boolean withLArgument;

	private QueryFullEval(boolean withLArgument) {
		this.withLArgument = withLArgument;
	}

	@Override
	public QueryFullEval getProxy() {
		return this;
	}

	@Override
	public String transform(String tableName) {
		StringBuilder a = merge(tableName);

		return DatabaseHelper.MAKER.c()
			.s("SELECT T1.x,T1.k")
			.s(withLArgument ? ",T1.l" : "")
			.s(",T1.r")

			.s(",")
			.s(ids[0]).s(0)
			.s(",")
			.s(ids[0])

			.s(",")
			.s(ids[1]).s(0)
			.s(",")
			.s(ids[1])

			.s(",")
			.s("(")
			.s(ids[0]).s(0)
			.s("+")
			.s(ids[0])
			.s(")")
			.s(" ")
			.s("AS")
			.s(" ")
			.s(ids[0]).s(2)

			.s(",")
			.s("(")
			.s(ids[1]).s(0)
			.s("+")
			.s(ids[1])
			.s(")")
			.s(" ")
			.s("AS")
			.s(" ")
			.s(ids[1]).s(2)
			.s(",")
			.s(ids[2])
			.s(",")
			.s(ids[3])
			.s(",")
			.s(ids[4])

			.s(" ")
			.s("FROM")
			.s(" ")
			.s(a)
			.s(" ORDER BY x")
			.s(";").mk().toString();
	}

	public StringBuilder merge(String tableName) {
		return this.c()
			.partWithSelect(tableName, 0, true)
			.leftJoin()
			.partWithSelect(tableName, 1, true)
			.partWithOn(1)
			.leftJoin()
			.partWithSelect(tableName, 2, false)
			.partWithOn(2)
			.leftJoin()
			.partWithSelect(tableName, 3, false)
			.partWithOn(3)
			.leftJoin()
			.partWithSelect(tableName, 4, false)
			.partWithOn(4)
			.mk();
	}

	private QueryFullEval partWithSelect(String tableName, int id, boolean flag) {
		partWithSelectBegin();
		if (flag) {
			partWithSelectOptMiddle(id);
		}
		partWithSelectEnd(tableName, id);
		return this;
	}

	private QueryFullEval partWithSelectBegin() {
		this.s("(SELECT x,k")
			.s(withLArgument ? ",l" : "")
			.s(",r")
		;
		return this;
	}

	private QueryFullEval partWithSelectOptMiddle(int id) {
		this.s(",")
			.s(DatabaseHelperEx.CUSTOM_AGGREGATE)
			.s("(")
			.s("ns0")
			.s(")")
			.s(" ")
			.s("AS")
			.s(" ")
			.s(ids[id]).s("0");
		return this;
	}

	private QueryFullEval partWithSelectEnd(String tableName, int id) {
		this.s(",")
			.s(DatabaseHelperEx.CUSTOM_AGGREGATE)
			.s("(")
			.s("ns")
			.s(")")
			.s(" ")
			.s("AS")
			.s(" ")
			.s(ids[id])

			.s(" ")
			.s("FROM")
			.s(" ")
			.s(tableName)
			.s(" ")
			.s("WHERE id='").s(ids[id]).s("'")

			.s(" ")
			.s("GROUP BY")
			.s(" ")
			.s("x")

			.s(") T").s(id + 1)
		;
		return this;
	}

	private QueryFullEval opt(QueryFullEval q, boolean condition) {
		return condition ? q : this;
	}

	private QueryFullEval partWithOn(int id) {
		this.s(" ")
			.s("ON")
			.s(" ")
			.s("T1.x=").s("T").s(id + 1).s(".x")
		;
		return this;
	}

	private QueryFullEval leftJoin() {
		this.s(" ")
			.s("LEFT JOIN")
			.s(" ");
		return this;
	}
}
