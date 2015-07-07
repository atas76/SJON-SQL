package org.sjon.sql;

public enum SjonType {
	
	INT,
	TEXT,
	BOOLEAN;
	
	@Override
	public String toString() {
		switch (this.ordinal()) {
		case 0:
			return "INTEGER";
		case 1:
			return "TEXT";
		case 2:
			return "INTEGER";
		default:
			return "TEXT";
		}
	}
}
