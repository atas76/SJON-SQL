package org.sjon.sql;

public enum SjonType {
	
	INT,
	TEXT,
	BOOLEAN,
	DATE;
	
	@Override
	public String toString() {
		switch (this.ordinal()) {
		case 0:
			return "INTEGER";
		case 1:
			return "TEXT";
		case 2:
			return "INTEGER";
		case 3:
			return "TEXT"; // Tightly bound to SQLite at the moment
		default:
			return "TEXT";
		}
	}
}
