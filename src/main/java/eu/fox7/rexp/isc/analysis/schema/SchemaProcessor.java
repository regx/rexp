package eu.fox7.rexp.isc.analysis.schema;

import eu.fox7.rexp.xml.schema.XSchema;
import nu.xom.Element;

public interface SchemaProcessor {
	void process(XSchema schema, Object id);

	Element toXml();

	void fromXml(Element root);
}
