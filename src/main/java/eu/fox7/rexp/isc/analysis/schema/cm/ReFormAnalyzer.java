package eu.fox7.rexp.isc.analysis.schema.cm;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.corejobs.SchemaAnalyzer;
import eu.fox7.rexp.isc.analysis.schema.ContentModelAnalyzer;
import eu.fox7.rexp.isc.analysis.schema.MapListHandle;
import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.regexp.core.extended.Sequence;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.util.mini.Key;
import eu.fox7.rexp.xml.schema.XSchema;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ReFormAnalyzer extends ContentModelAnalyzer {
	private static final String COL_SORE = "sore";
	private static final String COL_SOREU = "soreu";
	private static final String COL_CHARE_WITH_COUNTER = "cchare";
	private static final String COL_CHARE1 = "chare";
	private static final String COL_CANONICAL_FORM = "form";
	private static final String COL_CLASS = "class";
	private static final String COL_WCHARE = "wchare";
	private static final String COL_CWCHARE = "cwchare";
	private static final String COL_VECTOR = "vector";
	private boolean logCanonicalForm = true;
	private String complexDumpFileName = null;
	private String classSeqLengthFileName = null;

	public void setLogCanonicalForm(boolean logCanonicalForm) {
		this.logCanonicalForm = logCanonicalForm;
	}

	public void setComplexDumpFileName(String complexDumpFileName) {
		this.complexDumpFileName = complexDumpFileName;
	}

	public void setClassSeqLengthFileName(String classSeqLengthFileName) {
		this.classSeqLengthFileName = classSeqLengthFileName;
	}

	@Override
	protected void process(XSchema schema, Object id, String loc, String typeName, RegExp contentModel, MapListHandle outputHandle) {

		if (RegExpProperties.testRegExp(contentModel, RegExpProperties.IS_NT_COUNTER)) {
			hasCounterNum++;
		}
		int maxOcc = RegExpAnalyzer.maxSymbolOccurrence(contentModel);
		if (maxOcc > globalMaxOcc) {
			globalMaxOcc = maxOcc;
			maxOccExp = contentModel;
		}
		boolean isSore = (maxOcc <= 1);
		boolean isSoreU = (maxOcc <= 1) && !RegExpProperties.HAS_NT_COUNTER.transform(contentModel);
		boolean isStrictChare = RegExpAnalyzer.isStrictChare(contentModel);
		boolean isCchare = RegExpAnalyzer.isCounterChare(contentModel);
		boolean isWchare = RegExpAnalyzer.isWordChare(contentModel);
		boolean isCwchare = RegExpAnalyzer.isCounterWordChare(contentModel);

		outputHandle.put(COL_SORE, Boolean.valueOf(isSore).toString());
		outputHandle.put(COL_SOREU, Boolean.valueOf(isSoreU).toString());
		outputHandle.put(COL_CHARE1, Boolean.valueOf(isStrictChare).toString());
		outputHandle.put(COL_CHARE_WITH_COUNTER, Boolean.valueOf(isCchare).toString());
		outputHandle.put(COL_WCHARE, Boolean.valueOf(isWchare).toString());
		outputHandle.put(COL_CWCHARE, Boolean.valueOf(isCwchare).toString());
		outputHandle.put(COL_VECTOR, bitVectorString(isSore, isStrictChare, isCchare, isWchare, isCwchare));

		if (logCanonicalForm) {
			RegExp canon = RegExpCanonizer.toCanon(contentModel);
			outputHandle.put(COL_CANONICAL_FORM, ToPrettyReString.INSTANCE.apply(canon));
			if (isCwchare) {
				outputHandle.put(COL_CLASS, RegExpCanonizer.toClassificationString(contentModel));
			}
		}

		if (!isCchare && !isWchare) {
			logComplexExpr(contentModel);
		} else {
			logClassSeqLengthData(contentModel);
		}

		Log.d("id: %s, t: %s, cm: %s, SORE: %s, CHARE: %s", id, typeName, contentModel, isSore, isStrictChare);
	}
	
	private void logComplexExpr(RegExp contentModel) {
		RegExp canon = RegExpCanonizer.toCanon(contentModel);
		String s = ToPrettyReString.INSTANCE.apply(canon);
		Log.v("Complex expression: %s", s);
		if (complexDumpFileName != null) {
			dumpString(complexDumpFileName, s, true);
		}
	}

	private static void dumpString(String fileName, String s, boolean append) {
		try {
			File file = FileX.newFile(fileName);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file, append);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.append(s);
			bw.newLine();
			bw.flush();
			fw.close();
		} catch (IOException ex) {
			Log.w("Could not write to file %s: %s", fileName, ex);
		}
	}
	private void logClassSeqLengthData(RegExp contentModel) {
		if (classSeqLengthFileName != null) {
			RegExp canon = RegExpCanonizer.toCanon(contentModel);
			String s = RegExpCanonizer.toClassificationString(contentModel);
			int n = 1;
			if (canon instanceof Sequence) {
				Sequence seq = (Sequence) canon;
				n = seq.size();
			}
			Key<String> k = new Key<String>(s, String.valueOf(n));
			UtilX.mapIncrement(reformXfactorNum2Count, k);
		}
	}

	private Map<Key<String>, Integer> reformXfactorNum2Count;
	private int globalMaxOcc;
	private RegExp maxOccExp;
	private int hasCounterNum;

	@Override
	public void onStart() {
		super.onStart();
		reformXfactorNum2Count = new TreeMap<Key<String>, Integer>();
		globalMaxOcc = 0;
		maxOccExp = null;
		hasCounterNum = 0;
	}

	@Override
	public void onStop() {
		if (classSeqLengthFileName != null) {
			StringBuilder sb = new StringBuilder();
			try {
				for (Entry<Key<String>, Integer> e : reformXfactorNum2Count.entrySet()) {
					sb.append(String.format("%s\t%s\t%s\n", e.getKey().get(0), e.getKey().get(1), e.getValue()));
				}
			} catch (NullPointerException e) {
				Log.w("Unexpectec NPE: %s", e);
			}
			dumpString(classSeqLengthFileName, sb.toString(), true);
		}

		dumpString(Director.resolve(SchemaAnalyzer.PROP_MAX_OCC), String.valueOf(globalMaxOcc), false);
		dumpString(Director.resolve(SchemaAnalyzer.PROP_MAX_OCC), String.valueOf(maxOccExp), true);
		dumpString(Director.resolve(SchemaAnalyzer.PROP_HAS_COUNTER), String.valueOf(hasCounterNum), false);
		super.onStop();
	}
	

	@Override
	public String getJobMetaFileProperty() {
		return Director.PROP_RET_RESULT;
	}

	private static String bitVectorString(boolean... ba) {
		StringBuilder sb = new StringBuilder();
		for (boolean b : ba) {
			sb.append(b ? "1" : "0");
		}
		return sb.toString();
	}
}
