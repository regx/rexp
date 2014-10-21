package eu.fox7.rexp.isc.analysis.corejobs2;

import eu.fox7.rexp.isc.analysis.basejobs.FileJob;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.util.PropertiesManager;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilterProcessor extends FileJob {
	private static final boolean OVERWRITE_EXISTING_FILES = false;
	private String jobOutputDirectoryPath;
	private String srcFileName;

	protected String getJobOutputDirectoryPath() {
		return jobOutputDirectoryPath;
	}

	public void setJobOutputDirectoryPath(String jobOutputDirectory) {
		this.jobOutputDirectoryPath = jobOutputDirectory;
	}

	protected String getJobOutputDirectoryProperty() {
		return Director.PROP_XSD_FILTERED_DIR;
	}

	@Override
	protected String getJobDirectoryProperty() {
		return Director.PROP_XSD_NORMALIZED_DIR;
	}

	public void setSourceFileName(String srcFileName) {
		this.srcFileName = srcFileName;
	}
	

	public static interface Filter {
		void init(FilterProcessor parentJob);

		void setSourceFileName(String srcFileName);

		boolean decide(File srcFile, File targetFile, String relativeFileName, String inputFileName);
	}
	
	private List<Filter> filters;

	public FilterProcessor() {
		filters = new ArrayList<Filter>();
		filters.add(new DuplicateFilter());
		filters.add(new CounterSizeFilter());
	}

	public void setFilters(Filter... filterArray) {
		filters.clear();
		Collections.addAll(filters, filterArray);
	}

	public void clearFilters() {
		filters.clear();
	}

	public void addFilter(Filter filter) {
		filters.add(filter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (jobOutputDirectoryPath == null) {
			jobOutputDirectoryPath = PropertiesManager.getProperty(getJobOutputDirectoryProperty());
		}
		for (Filter filter : filters) {
			filter.init(this);
			filter.setSourceFileName(srcFileName);
		}
	}

	@Override
	protected void process(File file, String relativeFileName) {
		String inputFileName = relativeFileName.replaceFirst("\\.", getJobInputDirectoryPath());
		File targetFile = FileX.newFile(getJobOutputDirectoryPath(), relativeFileName);

		process(file, targetFile, relativeFileName, inputFileName);
	}

	private void process(File srcFile, File targetFile, String relativeFileName, String inputFileName) {
		Filter filter = classify(srcFile, targetFile, relativeFileName, inputFileName);
		if (filter == null) {
			Log.v("Positive filtering: %s", relativeFileName);
			doCopy(srcFile, targetFile);
		} else {
			Log.v("NEGATIVE filtering: %s, filter: %s", relativeFileName, filter.getClass().getSimpleName());
		}
	}

	private static void doCopy(File srcFile, File targetFile) {
		if (!targetFile.exists()) {
			FileX.prepareOutFile(targetFile);
			FileX.fileCopy(srcFile, targetFile);
		} else {
			Log.w("%s already exists, skipping", targetFile);
		}
	}

	protected Filter classify(File srcFile, File targetFile, String relativeFileName, String inputFileName) {
		for (Filter filter : filters) {
			if (!filter.decide(srcFile, targetFile, relativeFileName, inputFileName)) {
				return filter;
			}
		}
		return null;
	}
}
