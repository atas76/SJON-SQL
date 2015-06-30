package org.sjon.sql;

import java.util.Set;

public class SjonTable {
	
	private String name;
	private Set<SjonColumn> columns;
	private Set<SjonRecord> records;
	
	public String toDDL() {
		return null;
	}
	
	public String toDML() {
		return null;
	}
}
