package eu.fox7.rexp.xml.util;

import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.xml.util.XmlMapUtils.StringMapList;
import nu.xom.Element;

import java.io.File;

public class XmlFileMapList extends StringMapList {
	private static final long serialVersionUID = 1L;

	private File file;
	private String filePath;

	public XmlFileMapList(String filePath) {
		this.filePath = filePath;
		file = FileX.newFile(filePath);
	}

	public XmlFileMapList(File file) {
		this.file = file;
	}

	public String getFilePath() {
		return filePath != null ? filePath : file.getPath();
	}

	public void load() {
		Element loadElement = XmlUtils.readXml(file);
		if (loadElement != null) {
			XmlMapUtils.readMapsFromXmlAttributes(loadElement, this);
		}
	}

	public void save() {
		Element storeElement = XmlMapUtils.writeMapsToXmlAttributes(this);
		XmlUtils.serializeXml(storeElement, file);
	}
}
