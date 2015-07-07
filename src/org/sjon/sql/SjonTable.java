package org.sjon.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SjonTable {
	
	private String name;
	
	private Set<SjonField> domain = new HashSet<SjonField>();
	private Map<String, SjonField> fieldIndex = new HashMap<>();
	
	private String primaryKey;
	
	private Set<SjonReference> references = new HashSet<>();
	
	private Set<SjonRecord> records;
	
	public SjonTable(String name) {
		this.name = name;
	}
	
	public void addField(SjonField field) {
		domain.add(field);
		fieldIndex.put(field.getName(), field);
	}
	
	public void addReference(SjonReference reference) {
		this.references.add(reference);
	}
	
	public void setPrimaryKey(String fieldName) {
		this.primaryKey = fieldName;
	}
	
	public void addAutoIncrement() {
		if (this.primaryKey == null) {
			addField(new SjonField(this.name.toLowerCase() + "Id", SjonType.INT, true, true));
		}
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	public String toDDL() {
		
		StringBuilder query = new StringBuilder();
		
		query.append("CREATE TABLE ");
		query.append(this.name);
		query.append("(");
		
		for (SjonField field:domain) {
			query.append(field.getDefinition());
			query.append(",");
		}
		
		query.deleteCharAt(query.length() - 1);
		
		query.append(")");
		
		return query.toString();
	}
	
	public String toDML() {
		return null;
	}
}
