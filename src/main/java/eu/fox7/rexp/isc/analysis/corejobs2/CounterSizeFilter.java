package eu.fox7.rexp.isc.analysis.corejobs2;

import eu.fox7.rexp.xml.util.XmlUtils;
import eu.fox7.rexp.xml.util.iterators.ElementIterable;
import nu.xom.Attribute;
import nu.xom.Element;

import java.io.File;

public class CounterSizeFilter implements FilterProcessor.Filter {
	private static final int THRESHOLD = 2;
	private int threshold = THRESHOLD;

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	@Override
	public void init(FilterProcessor parentJob) {
	}

	@Override
	public void setSourceFileName(String srcFileName) {
	}

	@Override
	public boolean decide(File srcFile, File targetFile, String relativeFileName, String inputFileName) {
		Element e = XmlUtils.readXml(srcFile);
		return check(e);
	}

	private boolean check(Element e) {
		if (checkSingle(e)) {
			return true;
		} else {
			for (Element c : ElementIterable.iterateElements(e)) {
				if (check(c)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkSingle(Element e) {
		Attribute minAtt = e.getAttribute("minOccurs");
		Attribute maxAtt = e.getAttribute("maxOccurs");
		int min = -1;
		int max = -1;
		try {
			if (minAtt != null) {
				min = Integer.parseInt(minAtt.getValue());
			}
		} catch (NumberFormatException ignored) {
		}
		try {
			if (maxAtt != null) {
				max = Integer.parseInt(maxAtt.getValue());
			}
		} catch (NumberFormatException ignored) {
		}
		return min >= this.threshold || max >= this.threshold;
	}
}
