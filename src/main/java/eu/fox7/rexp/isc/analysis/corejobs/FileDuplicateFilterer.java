package eu.fox7.rexp.isc.analysis.corejobs;

import eu.fox7.rexp.isc.analysis.basejobs.FileJob;
import eu.fox7.rexp.isc.analysis.util.PropertiesManager;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.xml.util.XmlMapUtils;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
public class FileDuplicateFilterer extends FileJob {
	private static final boolean OVERWRITE_EXISTING_FILES = false;
	private Map<String, String> fileName2crc;
	private Map<String, String> crc2fileName;
	private String jobOutputDirectoryPath;
	private String srcFileName;

	public FileDuplicateFilterer() {
		fileName2crc = new HashMap<String, String>();
		crc2fileName = new HashMap<String, String>();
		init();
	}

	private void init() {
		jobOutputDirectoryPath = PropertiesManager.getProperty(getJobOutputDirectoryProperty());
		srcFileName = PropertiesManager.getProperty(getJobMetaFileProperty());
	}

	@Override
	protected void process(File file, String relativeFileName) {
		String fileName = relativeFileName.replaceFirst("\\.", getJobInputDirectoryPath());
		if (fileName2crc.containsKey(fileName)) {
			String crc = fileName2crc.get(fileName);
			if (!crc2fileName.containsKey(crc)) {
				passToOutputDirectory(file, relativeFileName);
				crc2fileName.put(crc, fileName);
			} else {
				File otherFile = FileX.newFile(getJobDirectory(), crc2fileName.get(crc));
				if (file.length() != otherFile.length()) {
					Log.i("%s has same CRC as %s, but different size", file, otherFile);
					passToOutputDirectory(file, relativeFileName);
				} else {
					Log.i("%s is probably a duplicate of %s, skipping", file, otherFile);
				}
			}
		} else {
			Log.w("CRC of %s is unknown, skipping", fileName);
		}
	}
	private void passToOutputDirectory(File file, String relativeFileName) {
		File targetFile = FileX.newFile(getJobOutputDirectoryPath(), relativeFileName);
		if (!targetFile.exists()) {
			FileX.prepareOutFile(targetFile);
			FileX.fileCopy(file, targetFile);
		} else {
			Log.i("%s already exists, skipping", targetFile);
		}
	}

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

	@Override
	protected void onStart() {
		super.onStart();
		XmlMapUtils.readMapFromXmlAttributes(getXml(), fileName2crc, FileHasher.TAG_FILE, FileHasher.TAG_CRC);
	}

	protected String getJobMetaFileProperty() {
		return Director.PROP_CRC_FILE;
	}

	private Element getXml() {
		try {
			File file = FileX.newFile(getSourceFilename());
			return XmlUtils.readXml(new FileInputStream(file));
		} catch (FileNotFoundException ex) {
			Log.e(ex.toString());
			return null;
		}
	}

	private String getSourceFilename() {
		return srcFileName;
	}

	public void setSourceFileName(String srcFileName) {
		this.srcFileName = srcFileName;
	}
}
