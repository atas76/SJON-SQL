package org.sjon.sql;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sjon.only.parser.ColumnGroup;
import org.sjon.only.parser.ColumnMap;
import org.sjon.only.parser.ObjectParser;
import org.sjon.only.scanner.ObjectAnalyzer;
import org.sjon.sql.exceptions.SjonParsingException;

public class SjonTable {
	
	private String name;
	
	private Set<SjonField> domain = new HashSet<SjonField>();
	private Map<String, SjonField> fieldIndex = new HashMap<>();
	
	private String primaryKey;
	
	private Set<SjonReference> references = new HashSet<>();
	
	private Set<SjonRecord> records = new HashSet<>();
	
	public SjonTable(String name) {
		this.name = name;
	}
	
	public Set<SjonRecord> getRecords() {
		return this.records;
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
	
	public String toDML() {
		return null;
	}
}
