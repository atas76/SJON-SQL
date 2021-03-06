package org.sjon.sql;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SjonRecord {
	
	private Map<String, Object> tuple = new HashMap<>();
	
	public void addColumn(String fieldName, Object fieldValue) {
		tuple.put(fieldName, fieldValue);
	}
	
	public Object getValue(String fieldName) {
		return tuple.get(fieldName);
	}
	
	public Set<String> getFieldNames() {
		return tuple.keySet();
	}
}
