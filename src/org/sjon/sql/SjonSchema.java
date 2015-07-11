package org.sjon.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sjon.only.parser.Column;
import org.sjon.only.parser.ColumnGroup;
import org.sjon.only.parser.ColumnList;
import org.sjon.only.parser.ColumnMap;
import org.sjon.only.parser.ObjectParser;
import org.sjon.only.scanner.ObjectAnalyzer;
import org.sjon.sql.exceptions.SjonParsingException;
import org.sjon.sql.exceptions.UndefinedFieldTypeException;
import org.sjon.sql.exceptions.UndefinedSjonMappingException;

public class SjonSchema {
	
	private Set<SjonTable> tables = new HashSet<SjonTable>();
	private Map<String, SjonTable> tableIndex = new HashMap<>();
	
	public SjonSchema(File file) 
			throws FileNotFoundException, IOException, SjonParsingException, UndefinedFieldTypeException, UndefinedSjonMappingException {
		
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
		
		interpretSchema(parser.getDocument());
	}
	
	private void interpretSchema (List<ColumnGroup> doc) throws UndefinedFieldTypeException, UndefinedSjonMappingException {
		
		for (ColumnGroup columnGroup: doc) {
	
			ColumnMap columnMap = (ColumnMap) columnGroup;
			
			String type = (String) columnMap.getColumn("type").getValue();
			
			switch(type) {
			case "relation":
				
				String name = (String) columnMap.getColumn("name").getValue();
				ColumnList fields = (ColumnList) columnMap.getColumn("fields").getValue();
				
				SjonTable currentTable = new SjonTable(name);
				
				for (Object field: fields.getValues()) {
					
					ColumnList fieldDetails = (ColumnList) field;
					String fieldName = (String) fieldDetails.getValue(0);
					String fieldTypeStr = (String) fieldDetails.getValue(1);
					
					int fieldParams = fieldDetails.getValues().size();
					
					SjonType fieldType;
					
					switch(fieldTypeStr) {
					case "int":
						fieldType = SjonType.INT;
						break;
					case "string":
						fieldType = SjonType.TEXT;
						break;
					case "boolean":
						fieldType = SjonType.BOOLEAN;
						break;
					default:
						throw new UndefinedFieldTypeException();
					}
					
					SjonField currentField = new SjonField(fieldName, fieldType);
					
					if (fieldParams > 2) {
						for (int i = 2; i < fieldParams; i++) {
							switch( (String) fieldDetails.getValue(i)) {
							case "primary key":
								currentField.setPrimaryKey();
								currentTable.setPrimaryKey(fieldName);
								break;
							case "foreign key":
								// Look for reference definition
								ColumnMap referenceDefinition = (ColumnMap) columnMap.getColumn(fieldName).getValue();
								Column tableName = referenceDefinition.getColumn("table");
								Column referencedField = referenceDefinition.getColumn("references");
								currentField.setForeignKey();
								currentField.setTableReference( (String) tableName.getValue());
								currentField.setFieldReference( (String) referencedField.getValue());
							}
						}
					}
					currentTable.addField(currentField);
				}
				
				currentTable.addAutoIncrement();
				
				this.tables.add(currentTable);
				this.tableIndex.put(name, currentTable);
				break;
			default:
				throw new UndefinedSjonMappingException();
			}
		}
	}
	
	public SjonTable getTable(String name) {
		return this.tableIndex.get(name);
	}
	
	public String toSQL() {
		
		StringBuilder schemaText = new StringBuilder();
		
		for (SjonTable table: this.tables) {
			schemaText.append(table.toDDL());
			schemaText.append(";");
			schemaText.append("\n");
		}
		return schemaText.toString();
	}
}
