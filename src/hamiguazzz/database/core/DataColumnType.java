package hamiguazzz.database.core;

public enum DataColumnType {
	BEAN("bean"),
	VARCHAR("varchar"),
	CHAR("char"),
	INT("int"),
	LONG("long"),
	DOUBLE("double"),
	UNSIGNED_INT("unsigned"),
	DATETIME("datetime"),
	DATE("date");

	private String typeName;

	DataColumnType(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeName() {
		return typeName;
	}

	public static DataColumnType get(String typeName) {
		typeName = typeName.toLowerCase();
		switch (typeName) {
			case "varchar":
				return VARCHAR;
			case "char":
				return CHAR;
			case "int":
				return INT;
			case "unsigned":
				return UNSIGNED_INT;
			case "datetime":
				return DATETIME;
			case "date":
				return DATE;
			case "long":
				return LONG;
			case "double":
				return DOUBLE;
			case "bean":
				return BEAN;
			default:
				return VARCHAR;
		}
	}
}
