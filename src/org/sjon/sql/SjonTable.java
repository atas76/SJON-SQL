package org.sjon.sql;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sjon.only.parser.ColumnGroup;
import org.sjon.only.parser.ColumnMap;
import org.sjon.only.parser.ObjectParser;
import org.sjon.only.scanner.ObjectAnalyzer;
import org.sjon.sql.exceptions.BooleanValidationException;
import org.sjon.sql.exceptions.DateValidationException;
import org.sjon.sql.exceptions.IntegerValidationException;
import org.sjon.sql.exceptions.SjonParsingException;
import org.sjon.sql.exceptions.TypeValidationException;

public class SjonTable {
	
	private String name;
	
	private Set<SjonField> domain = new HashSet<SjonField>();
	private Map<String, SjonField> fieldIndex = new HashMap<>();
	
	private String primaryKey;
	
	private Set<SjonReference> references = new HashSet<>();
	
	private List<SjonRecord> records = new ArrayList<>();
	
	public SjonTable(String name) {
		this.name = name;
	}
	
	public List<SjonRecord> getRecords() {
		return this.records;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void loadData(String file) throws FileNotFoundException, IOException, SjonParsingException {
		
		ObjectAnalyzer analyzer;
		ObjectParser parser;
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
	    try {
	    	
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        
	        analyzer = new ObjectAnalyzer(sb.toString());
	        
	    } finally {
	        br.close();
	    }
	    
	    analyzer.analyze();
		parser = new ObjectParser(analyzer);
		
		try {
			parser.parse();
		} catch (Exception ex) {
			throw new SjonParsingException();
		}
		
		List<ColumnGroup> data = parser.getDocument();
		
		for (ColumnGroup colGroup:data) {
			ColumnMap record = (ColumnMap) colGroup;
			SjonRecord sjonRecord = new SjonRecord();
			for (String fieldName: record.getColumns()) {
				sjonRecord.addColumn(fieldName, record.getColumn(fieldName).getValue());
			}
			records.add(sjonRecord);
		}
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
	
	public String toDML() throws TypeValidationException {
		
		int validationRecordCounter = 0;
		
		// Validate values
		for (SjonRecord record: this.records) {
			validationRecordCounter++;
			for (String fieldName:record.getFieldNames()) {
				try {
					validateValue( (String) record.getValue(fieldName), fieldIndex.get(fieldName).getType());
				} catch (IntegerValidationException | DateValidationException | BooleanValidationException vex) {
					throw new TypeValidationException("Record counter: " + validationRecordCounter + ", Field name: " + fieldName + ", " + vex.getMessage());
				} catch (NullPointerException npex) {
					System.out.println("Field missing: " + fieldName);
				}
			}
		}
		
		// Create INSERT query
		StringBuilder query = new StringBuilder();
		
		// Keeping fields order consistent
		List<String> queryFields = new ArrayList<String>();
		for (String fieldName: fieldIndex.keySet()) {
			if (!fieldIndex.get(fieldName).isAutoIncrement()) {
				queryFields.add(fieldName);
			}
		}
		
		for (SjonRecord record: this.records) {
			query.append("INSERT INTO " + this.name);
			query.append("(");
			for (String fieldName: queryFields) {				
				query.append(fieldName);
				query.append(",");
			}
			query.deleteCharAt(query.length() - 1);
			query.append(") ");
			query.append("VALUES (");
			for (String fieldName: queryFields) {
				query.append(fieldIndex.get(fieldName).convert(record.getValue(fieldName)));
				query.append(",");
			}
			query.deleteCharAt(query.length() - 1);
			query.append(");");
			query.append("\n");
		}
		
		return query.toString();
	}
	
	private void validateValue(String value, SjonType type) throws IntegerValidationException, BooleanValidationException, DateValidationException {
		
		// System.out.println(value + ", type: " + type.ordinal());
		
		switch(type) {
		case INT:
			try {
				Integer.parseInt(value);
			} catch (NumberFormatException nfex) {
				throw new IntegerValidationException("Invalid integer: " + value);
			}
			break;
		case BOOLEAN:
			if (!value.equals("true") && !value.equals("false")) {
				throw new BooleanValidationException("Invalid boolean value: " + value);
			}
			break;
		case DATE:
			try {
			    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			    formatter.setLenient(false);
			    value = value.replace('.', ':'); // a hack to bypass escaping the date format for avoiding collision with the key value separator
			    Date date = formatter.parse(value); // We don't really care about the actual value, as it will be entered and manipulated as TEXT by SQLite
			} catch (ParseException e) { 
			    throw new DateValidationException("Invalid date format: " + value);
			}
		}	
	}
}
