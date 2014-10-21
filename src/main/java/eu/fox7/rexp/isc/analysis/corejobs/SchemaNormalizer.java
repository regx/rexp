package eu.fox7.rexp.isc.analysis.corejobs;

import eu.fox7.rexp.isc.analysis.basejobs.FileJob;
import eu.fox7.rexp.isc.analysis.util.PropertiesManager;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.xml.util.XmlUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SchemaNormalizer extends FileJob {
	private static final boolean OVERWRITE_EXISTING_FILES = false;
	private String jobOutputDirectoryPath;

	public SchemaNormalizer() {
		init();
	}

	private void init() {
		jobOutputDirectoryPath = PropertiesManager.getProperty(getJobOutputDirectoryProperty());
	}

	public void setJobOutputDirectoryPath(String jobOutputDirectoryPath) {
		this.jobOutputDirectoryPath = jobOutputDirectoryPath;
	}

	@Override
	protected void process(File file, String relativeFileName) {
		FileInputStream fis = null;
		try {
			File outFile = FileX.newFile(getJobOutputDirectory(), relativeFileName);
			if (!outFile.exists()) {
				FileX.prepareOutFile(outFile);
				fis = new FileInputStream(file);
				FileOutputStream fos = new FileOutputStream(outFile);
				Log.i("Normalizing %s", relativeFileName);
				boolean normalizeSuccess = XmlUtils.normalizeXml(fis, fos);
				fis.close();
				fos.close();
				if (!normalizeSuccess) {
					Log.w("Could not normalize %s", relativeFileName);
					boolean b = outFile.delete();
					if (!b) {
						Log.w("Could not delete %s", outFile);
					}
				}
			}
		} catch (IOException ex) {
			Log.e("Exception while normalizing %s: %s", file, ex);
		} finally {
			try {
				if (fis != null) {
					fis.close();
				} else {
					Log.w("Nothing written");
				}
			} catch (IOException ex) {
				Log.e("%s", ex);
			}
		}
	}

	@Override
	protected String getJobDirectoryProperty() {
		return Director.PROP_XSD_DIR;
	}

	protected String getJobOutputDirectory() {
		return jobOutputDirectoryPath;
	}

	protected String getJobOutputDirectoryProperty() {
		return Director.PROP_XSD_NORMALIZED_DIR;
	}
}
