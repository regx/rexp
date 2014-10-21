package eu.fox7.rexp.isc.analysis.schema;

import eu.fox7.rexp.isc.analysis.util.PropertiesManager;
import nu.xom.Element;

public abstract class SchemaAnalysis implements SchemaProcessor {
	private String metaFileName;

	public SchemaAnalysis() {
		init();
	}

	private void init() {
		metaFileName = PropertiesManager.getProperty(getJobMetaFileProperty());
	}

	public String getMetaFileName() {
		return metaFileName;
	}

	public void setMetaFileName(String metaFileName) {
		this.metaFileName = metaFileName;
	}


	protected String getJobMetaFileProperty() {
		return null;
	}

	@Override
	public void fromXml(Element root) {
	}

	@Override
	public Element toXml() {
		return null;
	}

	public void onStart() {
	}

	public void onStop() {
	}
}
