package eu.fox7.rexp.isc.analysis.corejobs;

import eu.fox7.rexp.isc.analysis.basejobs.Job;
import eu.fox7.rexp.isc.analysis.schema.*;
import eu.fox7.rexp.isc.analysis.util.PropertiesManager;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class Director extends Job {
	protected static final String ANALYSIS_BASE_DIR = "../../data/.rexp";

	public static final String PROP_BASE_DIR = "dir.base";
	public static final String PROP_GOOGLE_DIR = "dir.google";
	public static final String PROP_DOWNLOAD_DIR = "dir.download";
	public static final String PROP_XSD_DIR = "dir.xsd.raw";
	public static final String PROP_XSD_NORMALIZED_DIR = "dir.xsd.normalized";
	public static final String PROP_XSD_FILTERED_DIR = "dir.xsd.filtered";
	public static final String PROP_REGEXP_DIR = "dir.regexp";

	public static final String PROP_DOWNLOAD_FILE = "meta.download";
	public static final String PROP_LINK_FILE = "meta.link";
	public static final String PROP_GOOGLE_FILE = "meta.google";
	public static final String PROP_CRC_FILE = "meta.crc";

	public static final String PROP_SDT_RESULT = "analysis.sdt";
	public static final String PROP_CDEPTH_RESULT = "analysis.depth";
	public static final String PROP_CNUM_RESULT = "analysis.counters";
	public static final String PROP_RET_RESULT = "analysis.ret";

	public static final String PROP_MVN_REPO = "meta.mvn.repo";
	public static final String PROP_MVN_XSD = "meta.mvn.xsd";

	public static void main(String[] args) {
		Log.configureRootLogger(Level.INFO);
		UtilX.setCd(ANALYSIS_BASE_DIR);
		new Director().run();
	}

	static void testCwd() {
		UtilX.setCd("D:/home/dev/data/.rexp");

		System.out.println(new java.io.File(".").getAbsolutePath());
		System.out.println(System.getProperty("user.dir"));
		System.out.println(new java.io.File(".").exists());

		System.out.println(new java.io.File("xsd").getAbsolutePath());
		System.out.println(System.getProperty("user.dir"));

		System.out.println(new java.io.File("xsd").exists());
		System.out.println(new java.io.File(new java.io.File("xsd").getAbsolutePath()).exists());
		System.out.println(FileX.newFile("xsd").exists());
		System.out.println(FileX.newFile("xsd", "raw").exists());
		System.out.println(new java.io.File("./src").exists());
	}

	@Override
	protected void work() {
		Class<?>[] jobs = {
			SchemaProcessor.class,
		};
		executeJobs(jobs);
	}

	private static void executeJobs(Class<?>[] jobTypes) {
		Set<Class<?>> jobTypeSet = new HashSet<Class<?>>(Arrays.asList(jobTypes));

		if (jobTypeSet.contains(GoogleCrawler.class)) {
			GoogleCrawler gc = new GoogleCrawler();
			gc.setQuery(GoogleCrawler.XSD_FILETYPE_QUERY);
			gc.setHigh(1);
			gc.execute();
		}

		if (jobTypeSet.contains(GoogleLinkExtractor.class)) {
			GoogleLinkExtractor le = new GoogleLinkExtractor();
			le.setJobInputDirectoryPath(resolve(PROP_GOOGLE_DIR));
			le.setMetaFileName(resolve(PROP_LINK_FILE));
			le.execute();
		}

		if (jobTypeSet.contains(BatchDownloader.class)) {
			BatchDownloader bd = new BatchDownloader();
			bd.setLinkSourceFileName(resolve(PROP_LINK_FILE));
			bd.setMetaFileName(resolve(PROP_DOWNLOAD_FILE));
			bd.setDownloadDir(resolve(PROP_DOWNLOAD_DIR));
			bd.execute();
		}

		if (jobTypeSet.contains(SchemaNormalizer.class)) {
			SchemaNormalizer sn = new SchemaNormalizer();
			sn.setJobInputDirectoryPath(resolve(PROP_XSD_DIR));
			sn.setJobOutputDirectoryPath(resolve(PROP_XSD_NORMALIZED_DIR));
			sn.execute();
		}

		if (jobTypeSet.contains(FileHasher.class)) {
			FileHasher fh = new FileHasher();
			fh.setMetaFileName(resolve(PROP_CRC_FILE));
			fh.setJobInputDirectoryPath(resolve(PROP_XSD_NORMALIZED_DIR));
			fh.execute();
		}

		if (jobTypeSet.contains(FileDuplicateFilterer.class)) {
			FileDuplicateFilterer ff = new FileDuplicateFilterer();
			ff.setSourceFileName(resolve(PROP_CRC_FILE));
			ff.setJobInputDirectoryPath(resolve(PROP_XSD_NORMALIZED_DIR));
			ff.setJobOutputDirectoryPath(resolve(PROP_XSD_FILTERED_DIR));
			ff.execute();
		}

		if (jobTypeSet.contains(SchemaProcessor.class)) {
			SdtAnalyzer sdt = new SdtAnalyzer();
			CounterDepthAnalyzer cda = new CounterDepthAnalyzer();
			CounterNumAnalyzer cna = new CounterNumAnalyzer();
			SchemaAnalysis[] saa = {
				sdt,
				cda,
				cna,
			};
			SchemaAnalyzer sa = new SchemaAnalyzer(saa);
			sa.setJobInputDirectoryPath(resolve(PROP_XSD_FILTERED_DIR));
			sa.execute();
		}
	}

	public static String resolve(String propertyName) {
		return PropertiesManager.getProperty(propertyName);
	}

	public static void setup() {
		String base = resolve(PROP_BASE_DIR);
		if (base != null) {
			UtilX.setCd(base);
		}
		Log.configureRootLogger(Level.ALL);
	}
}
