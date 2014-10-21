package eu.fox7.rexp.isc.analysis2.gwc;

import eu.fox7.rexp.isc.analysis.basejobs.XmlJob;
import eu.fox7.rexp.isc.analysis.corejobs.Director;
import eu.fox7.rexp.xml.util.XmlMapUtils;
import nu.xom.Element;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static eu.fox7.rexp.isc.analysis.corejobs.GoogleLinkExtractor.*;

public class GoogleWebCrawler extends XmlJob<Item<String>> {
	private String query;
	private int loopMax;
	private Map<String, String> links2file;

	public GoogleWebCrawler() {
		links2file = new LinkedHashMap<String, String>();
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void setLoopMax(int loopMax) {
		this.loopMax = loopMax;
	}

	@Override
	protected String getJobMetaFileProperty() {
		return Director.PROP_LINK_FILE;
	}

	@Override
	protected void fromXmlElement(Element root) {
		XmlMapUtils.readMapFromXmlAttributes(root, links2file, TAG_URL, FILE_TAG);
	}

	@Override
	protected Element toXmlElement() {
		return XmlMapUtils.writeMapToXmlAttributes(links2file, TAG_URL, FILE_TAG);
	}

	@Override
	protected Iterator<Item<String>> iterateItems() {
		GoogleWebIterator it = new GoogleWebIterator(query);
		it.setLoopMax(loopMax);
		return Item.wrap(it);
	}

	@Override
	protected void process(Item<String> item) {
		String info = String.format("%s", query);
		links2file.put(item.get(), info);
	}
}
