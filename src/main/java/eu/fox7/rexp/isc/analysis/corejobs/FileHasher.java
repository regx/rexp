package eu.fox7.rexp.isc.analysis.corejobs;

import eu.fox7.rexp.isc.analysis.basejobs.FileJob;
import eu.fox7.rexp.isc.analysis.util.PropertiesManager;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.util.XmlMapUtils;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Element;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

public class FileHasher extends FileJob {
	public final static String TAG_FILE = "file";
	public final static String TAG_CRC = "crc";
	private final int BUF_SIZE = 256;
	private Map<String, String> fileName2crc;

	public FileHasher() {
		super();
		fileName2crc = new HashMap<String, String>();
		init();
	}

	@Override
	protected void process(File file, String relativeFileName) {
		FileInputStream fis = null;
		try {
			CRC32 crc = new CRC32();
			fis = new FileInputStream(file);
			byte[] buffer = new byte[BUF_SIZE];
			int len = fis.read(buffer, 0, BUF_SIZE);
			while (len > 0) {
				crc.update(buffer, 0, len);
				len = fis.read(buffer);
			}
			fis.close();
			String crcStr = Long.toHexString(crc.getValue()).toUpperCase();
			String fileName = relativeFileName;
			Log.i("CRC of %s: %s", fileName, crcStr);
			fileName2crc.put(fileName, crcStr);
		} catch (IOException ex) {
			Log.e("%s", ex);
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException ex) {
				Log.e("%s", ex);
			}
		}
	}

	@Override
	protected String getJobDirectoryProperty() {
		return Director.PROP_XSD_NORMALIZED_DIR;
	}
	

	protected void fromXmlElement(Element root) {
		XmlMapUtils.readMapFromXmlAttributes(root, fileName2crc, TAG_FILE, TAG_CRC);
	}

	protected Element toXmlElement() {
		return XmlMapUtils.writeMapToXmlAttributes(fileName2crc, TAG_FILE, TAG_CRC);
	}

	protected String getJobMetaFileProperty() {
		return Director.PROP_CRC_FILE;
	}

	private String metaFileName;

	private void init() {
		metaFileName = PropertiesManager.getProperty(getJobMetaFileProperty());
	}
	

	@Override
	protected void onStart() {
		super.onStart();
		Element e = getXml();
		if (e != null) {
			fromXmlElement(e);
		} else {
			Log.e("XML could not be loaded in %s", getClass().getName());
		}
	}

	@Override
	protected void onStop() {
		OutputStream o = getOutputStream();
		XmlUtils.serializeXml(toXmlElement(), o);
		UtilX.silentClose(o);
		super.onStop();
	}

	private OutputStream getOutputStream() {
		FileOutputStream fos;
		try {
			File outFile = FileX.newFile(getMetaFilename());
			FileX.prepareOutFile(outFile);
			fos = new FileOutputStream(outFile, false);
			return fos;
		} catch (FileNotFoundException ex) {
			Log.w("File %s not found, continuing with stdout", getMetaFilename());
			return System.out;
		}
	}

	private Element getXml() {
		try {
			File file = FileX.newFile(getMetaFilename());
			return XmlUtils.readXml(new FileInputStream(file));
		} catch (FileNotFoundException ex) {
			Log.e(ex.toString());
			return null;
		}
	}
	private String getMetaFilename() {
		return metaFileName;
	}

	public void setMetaFileName(String metaFileName) {
		this.metaFileName = metaFileName;
	}
}
