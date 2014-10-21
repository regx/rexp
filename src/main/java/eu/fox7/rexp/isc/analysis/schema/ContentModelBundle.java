package eu.fox7.rexp.isc.analysis.schema;

import eu.fox7.rexp.regexp.base.RegExp;
import eu.fox7.rexp.xml.schema.XSchema;

public class ContentModelBundle {
	private final Object id;
	private String typeName;
	private RegExp contentModel;
	private int expNum;
	private final XSchema schema;

	public ContentModelBundle(XSchema schema, Object id, String typeName, RegExp contentModel, int expNum) {
		this.schema = schema;
		this.id = id;
		this.typeName = typeName;
		this.contentModel = contentModel;
		this.expNum = expNum;
	}

	public XSchema getSchema() {
		return schema;
	}

	public Object getId() {
		return id;
	}

	public String getTypeName() {
		return typeName;
	}

	public RegExp getContentModel() {
		return contentModel;
	}

	public int getExpNum() {
		return expNum;
	}
}