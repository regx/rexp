package eu.fox7.rexp.isc.experiment2;

import eu.fox7.rexp.data.relation.Pair;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.experiment2.measure.MemoryTimeMeasurer;
import eu.fox7.rexp.isc.fast.tree.CnfaAaTree;
import eu.fox7.rexp.isc.fast.tree.NfaAaTree;
import eu.fox7.rexp.tree.nfa.evaltree.EvalTree;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Element;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class IscBenchmark {
	private static final String KEY_TAG = "TAG";
	private static final int DEFAULT_ITERATIONS = 10;
	public static final String PROP_ISB = "analysis.isb";

	public static final int LOW = 1;
	public static final int HIGH = 50;
	public static final int STEP_SIZE = 1;
	public static final int ITERATIONS = 10;
	public static final int REPS = 10;
	public static final String DEFAULT_FILE_NAME = Director.resolve(PROP_ISB);
	public static final EvalTree[] DEFAULT_EVALUATORS = new EvalTree[]{
		new NfaAaTree(),
		new CnfaAaTree(),
	};

	public static void main(String[] args) {
		Director.setup();
		Log.configureRootLogger(Level.INFO);
		IscBenchmark benchmark = new IscBenchmark();
		benchmark.setIterations(ITERATIONS);
		benchmark.addSuite(new BasicInputs(LOW, HIGH, STEP_SIZE), new BasicExecutor2(REPS));
		benchmark.setEvaluators(DEFAULT_EVALUATORS);
		benchmark.setClear(true);
		benchmark.run();
		processResults(true);
	}

	static void processResults(boolean showPdf) {
		Log.i("Main processing done");
		CmdIsr isr = new CmdIsr();
		isr.init();
		isr.execute();
		if (showPdf) {
			isr.init();
			isr.setPdf(true);
			isr.setOpen(true);
			isr.execute();
		}
	}

	private final ResultHolder results;
	private int iterations = DEFAULT_ITERATIONS;

	private List<MemoryTimeMeasurer> measurers;
	private List<EvalTree> evaluators;
	private List<Suite<?>> suites;
	private File file;
	private boolean clear;
	private boolean cancellationRequested = false;

	public IscBenchmark() {
		results = new ResultHolder();
		measurers = new LinkedList<MemoryTimeMeasurer>();
		measurers.add(new MemoryTimeMeasurer());
		evaluators = new LinkedList<EvalTree>();
		suites = new LinkedList<Suite<?>>();
		file = FileX.newFile(DEFAULT_FILE_NAME);
		clear = false;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setClear(boolean clear) {
		this.clear = clear;
	}

	public void run() {
		if (!clear) {
			load();
		}
		try {
			multiRun();
		} catch (OutOfMemoryError e) {
			Log.e("Out of memory error, trying to go on");
			evaluators = null;
			suites = null;
			measurers = null;
		}
		store();
	}
	void multiRun() {
		for (int i = 0; i < iterations && !cancellationRequested; i++) {
			Log.i("Iteration %s", i + 1);
			Pair<Object> tag = new Pair<Object>(KEY_TAG, System.nanoTime() + i);
			results.setResultMeta(ResultHolder.pairResultAdder(tag));

			singleRun();
		}
	}

	private <T> void singleRun() {
		for (Suite<?> suite : suites) {
			suite.rewind();
			while (suite.hasNextInput() && !cancellationRequested) {
				suite.selectNextInput();

				for (EvalTree evaluator : evaluators) {
					for (MemoryTimeMeasurer measurer : measurers) {
						measurer.setContext(evaluator);

						suite.run(results, evaluator, measurer);
						postSuiteRun(suite);
					}
				}
			}
		}
	}

	private void postSuiteRun(Suite<?> suite) {
		if (Boolean.TRUE.equals(suite.getExecutor().stopRequested.get())) {
			cancellationRequested = true;
		}
	}

	public void load() {
		File inFile = getFile();
		if (inFile != null) {
			Element e = XmlUtils.readXml(inFile);
			if (e != null) {
				results.fromXmlElement(e);
			}
		}
	}

	public void store() {
		File outFile = getFile();
		if (outFile != null) {
			synchronized (results) {
				Element e = results.toXmlElement();
				XmlUtils.serializeXml(e, outFile);
			}
		}
	}

	public File getFile() {
		return file;
	}

	public void setMeasurers(MemoryTimeMeasurer[] measurers) {
		this.measurers = Arrays.asList(measurers);
	}

	public void setEvaluators(EvalTree... evaluators) {
		this.evaluators = Arrays.asList(evaluators);
	}

	public void setSuites(Suite<?>... suites) {
		this.suites = Arrays.asList(suites);
	}

	public <T> void addSuite(Iterable<T> inputs, Executor<T, EvalTree> executor) {
		Suite<?> suite = new Suite<T>(inputs, executor);
		suites.add(suite);
	}

	public ResultHolder getResults() {
		return results;
	}
}
