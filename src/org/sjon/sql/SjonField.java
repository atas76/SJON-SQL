package org.sjon.sql;

public class SjonField {
	
	private String name;
	private SjonType type;
	
	private boolean autoIncrement;
	private boolean primaryKey;
	private boolean foreignKey;
	
	private String tableReference;
	private String fieldReference;
	
	public SjonField(String name, SjonType type) {
		this.name = name;
		this.type = type;
	}
	
	public SjonField(String name, SjonType type, boolean primaryKey, boolean autoIncrement) {
		this(name, type);
		this.primaryKey = primaryKey;
		this.autoIncrement = autoIncrement;
	}
	
	public void setPrimaryKey() {
		this.primaryKey = true;
	}
	
	public void setForeignKey() {
		this.foreignKey = true;
	}
	
	public void setTableReference(String tableReference) {
		this.tableReference = tableReference;
	}
	
	public void setFieldReference(String fieldReference) {
		this.fieldReference = fieldReference;
	}
	
	public String getName() {
		return this.name;
	}
	
	public SjonType getType() {
		return this.type;
	}
	
	public boolean isAutoIncrement() {
		return this.autoIncrement;
	}
	
	public String convert(Object value) {
		switch(this.type) {
		case INT:
			return (String) value;
		case TEXT:
			return "'" + value + "'";
		case BOOLEAN:
			switch((String) value) {
			case "true":
				return "1";
			case "false":
				return "0";
			default:
				return "-1";
			}
		default:
			return value.toString();
		}
	}
	
	public String getDefinition() {
		
		String definition = this.name + " " + this.type + " " 
				+ (this.primaryKey ? "PRIMARY KEY ":"")
				+ (this.autoIncrement ? "AUTOINCREMENT ":"") 
				+ (this.foreignKey ? "REFERENCES " + this.tableReference + "(" + this.fieldReference + ")":"");
		
		return definition;
	}
	
	@Override
	public boolean equals(Object fieldObj) {
		
		SjonField field = (SjonField) fieldObj;
		
		return (this.name.equals(field.name) && this.type == field.type);
	}
	
	@Override
	public int hashCode() {
	
		int c;
		int result = 26;
		
		c = this.name.hashCode();
		result = result * 31 + c;
		
		c = type.ordinal();
		result = result * 31 + c;
		
		return result;
	}
}
