package eu.fox7.rexp.xml.schemax;

public class SchemaImportDecl {
	private String namespace;
	private String schemaLocation;

	public SchemaImportDecl() {
		namespace = null;
		schemaLocation = null;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getSchemaLocation() {
		return schemaLocation;
	}

	public void setSchemaLocation(String schemaLocation) {
		this.schemaLocation = schemaLocation;
	}

	public SchemaImportDecl(String namespace, String schemaLocation) {
		this.namespace = namespace;
		this.schemaLocation = schemaLocation;
	}

	public boolean isValidDeclaration() {
		return namespace != null || schemaLocation != null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		if (namespace != null) {
			sb.append(String.format("%s=%s", SchemaUtil.XS_ATTR_NAMESPACE, namespace));
		}

		if (schemaLocation != null) {
			if (namespace != null) {
				sb.append(", ");
			}
			sb.append(String.format("%s=%s", SchemaUtil.XS_ATTR_SCHEMA_LOCATION, schemaLocation));
		}
		sb.append("}");

		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SchemaImportDecl other = (SchemaImportDecl) obj;
		if ((this.namespace == null) ? (other.namespace != null) : !this.namespace.equals(other.namespace)) {
			return false;
		}
		if ((this.schemaLocation == null) ? (other.schemaLocation != null) : !this.schemaLocation.equals(other.schemaLocation)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 41 * hash + (this.namespace != null ? this.namespace.hashCode() : 0);
		hash = 41 * hash + (this.schemaLocation != null ? this.schemaLocation.hashCode() : 0);
		return hash;
	}
}
