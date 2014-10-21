package eu.fox7.rexp.isc.analysis.basejobs;

import eu.fox7.rexp.isc.analysis.util.FileIterator;
import eu.fox7.rexp.isc.analysis.util.PropertiesManager;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;

import java.io.File;

public abstract class FileJob extends Job {
	protected FileIterator fit;
	private String jobInputDirectoryPath;

	public FileJob() {
		init();
	}

	private void init() {
		jobInputDirectoryPath = PropertiesManager.getProperty(getJobDirectoryProperty());
	}

	public String getJobInputDirectoryPath() {
		return jobInputDirectoryPath;
	}

	public void setJobInputDirectoryPath(String jobDirectoryName) {
		this.jobInputDirectoryPath = jobDirectoryName;
	}

	protected void assignIterator() {
		File dir = getJobDirectory();
		if (dir.toString().endsWith(".zip")) {
			fit = new ZipVfsFileIterator(dir);
		} else {
			fit = new FileIterator(dir);
		}
	}

	@Override
	protected void work() {
		assignIterator();
		while (isRunnable() && fit.hasNext()) {
			File file = fit.next();
			String relativeFileName = fit.getPathRelativeToBase(file);
			Log.i("Processing %s", relativeFileName);
			process(file, relativeFileName);
		}
	}

	protected abstract void process(File file, String relativeFileName);

	protected File getJobDirectory() {
		return FileX.newFile(jobInputDirectoryPath);
	}

	protected abstract String getJobDirectoryProperty();
	public File resolvePath(String fileName) {
		if (!(fit instanceof ZipVfsFileIterator)) {
			return FileX.newFile(getJobDirectory(), fileName);
		} else {
			return FileX.newFile(ZipVfsFileIterator.TEMP_DIR, fileName);
		}
	}
}
