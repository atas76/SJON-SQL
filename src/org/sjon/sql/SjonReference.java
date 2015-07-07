package org.sjon.sql;

public class SjonReference {
	
	private String tableName;
	private String fromFieldName;
	private String toFieldName;
	
	public SjonReference(String tableName, String fromFieldName, String toFieldName) {
		this.tableName = tableName;
		this.fromFieldName = fromFieldName;
		this.toFieldName = toFieldName;
	}
}
