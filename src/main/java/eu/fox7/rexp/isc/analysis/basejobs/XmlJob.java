package eu.fox7.rexp.isc.analysis.basejobs;

import eu.fox7.rexp.isc.analysis.util.PropertiesManager;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import eu.fox7.rexp.util.UtilX;
import eu.fox7.rexp.xml.util.XmlUtils;
import nu.xom.Element;

import java.io.*;

public abstract class XmlJob<T extends BatchJob.Item<?>> extends BatchJob<T> {
	private String metaFileName;

	public XmlJob() {
		init();
	}

	private void init() {
		metaFileName = PropertiesManager.getProperty(getJobMetaFileProperty());
	}

	@Override
	protected void onStart() {
		super.onStart();
		load();
	}

	@Override
	protected void onStop() {
		save();
		super.onStop();
	}

	public void load() {
		Element e = getXml();
		if (e != null) {
			fromXmlElement(e);
		} else {
			Log.e("XML could not be loaded in %s", getClass().getName());
		}
	}

	public void save() {
		OutputStream o = getOutputStream();
		XmlUtils.serializeXml(toXmlElement(), o);
		UtilX.silentClose(o);
	}

	private OutputStream getOutputStream() {
		File outFile = null;
		FileOutputStream fos;
		try {
			outFile = FileX.newFile(getMetaFileName());
			FileX.prepareOutFile(outFile);
			fos = new FileOutputStream(outFile, false);
			return fos;
		} catch (IOException ex) {
			String fn = outFile.getAbsolutePath();
			Log.w("File %s cannot be written, continuing with stdout", fn);
			return System.out;
		}
	}

	public String getMetaFileName() {
		return metaFileName;
	}

	public void setMetaFileName(String metaFileName) {
		this.metaFileName = metaFileName;
	}

	private Element getXml() {
		try {
			File file = FileX.newFile(getMetaFileName());
			return XmlUtils.readXml(new FileInputStream(file));
		} catch (FileNotFoundException ex) {
			Log.e(ex.toString());
			return null;
		}
	}

	protected abstract String getJobMetaFileProperty();

	protected abstract Element toXmlElement();

	protected abstract void fromXmlElement(Element root);
}
