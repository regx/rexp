package eu.fox7.rexp.isc.analysis.corejobs2;

import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.isc.analysis.corejobs.FileHasher;
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

public class DuplicateFilter implements FilterProcessor.Filter {
	private Map<String, String> fileName2crc;
	private Map<String, String> crc2fileName;
	private String srcFileName;

	private FilterProcessor parentJob;

	public DuplicateFilter() {
		fileName2crc = new HashMap<String, String>();
		crc2fileName = new HashMap<String, String>();
	}

	@Override
	public void init(FilterProcessor parentJob) {
		this.parentJob = parentJob;
		srcFileName = PropertiesManager.getProperty(getJobMetaFileProperty());
		XmlMapUtils.readMapFromXmlAttributes(getXml(), fileName2crc, FileHasher.TAG_FILE, FileHasher.TAG_CRC);
	}

	@Override
	public boolean decide(File srcFile, File targetFile, String relativeFileName, String inputFileName) {
		if (fileName2crc.containsKey(relativeFileName)) {
			String crc = fileName2crc.get(relativeFileName);
			if (!crc2fileName.containsKey(crc)) {
				crc2fileName.put(crc, relativeFileName);
				return true;
			} else {
				File otherFile = parentJob.resolvePath(crc2fileName.get(crc));
				if (srcFile.length() != otherFile.length()) {
					Log.w("%s has same CRC as %s, but different size: %s != %s", srcFile, otherFile, srcFile.length(), otherFile.length());
					return true;
				} else {
					Log.v("Possible duplicate detection: %s, %s", srcFile, otherFile);
					return false;
				}
			}
		} else {
			Log.w("CRC of %s is unknown, skipping", relativeFileName);
			return false;
		}
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

	@Override
	public void setSourceFileName(String srcFileName) {
		this.srcFileName = srcFileName;
	}
}
