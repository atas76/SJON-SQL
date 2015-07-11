package org.sjon.main;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sjon.sql.SjonSchema;
import org.sjon.sql.SjonTable;

public class Main {

	public static void main(String[] args) {
		
		if (args.length < 1) {
			System.out.println("USAGE: <directory>");
			System.exit(1);
		}
		
		// Check that directory exists
		
		Path dbPath = Paths.get(args[0]);
		
		if (!Files.isDirectory(dbPath)) {
			System.out.println("A directory must be specified");
			System.exit(2);
		}
		
		Map<String, Path> candidateTables = new HashMap<String, Path>();
		Path schemaFile = null;
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dbPath)) {
		    for (Path file: stream) {
		    	if (file.getFileName().toString().endsWith(".sjon")) {
		    		// System.out.println(file.getFileName());
		    		if (file.getFileName().toString().equals("schema.sjon")) {
		    			schemaFile = file;
		    		} else {
		    			candidateTables.put(file.getFileName().toString().substring(0, file.getFileName().toString().length() - 5), file);
		    		}
		    	}
		    }
		} catch (IOException | DirectoryIteratorException x) {
		    // IOException can never be thrown by the iteration.
		    // In this snippet, it can only be thrown by newDirectoryStream.
		    System.err.println(x);
		}
		
		if (schemaFile == null) {
			System.out.println("Schema file not specified");
			System.exit(3);
		}
		
		List<SjonTable> tables = new ArrayList<SjonTable>();
		
		try {
			
			SjonSchema schema = new SjonSchema(schemaFile.toFile());
			
			for (String table: candidateTables.keySet()) {
				SjonTable currentTable = schema.getTable(table);
				// System.out.println(candidateTables.get(table).toAbsolutePath().toString());
				currentTable.loadData(candidateTables.get(table).toAbsolutePath().toString());
				tables.add(currentTable);
			}
			
			generateSchema(schema);
			
			for (SjonTable table: tables) {
				System.out.println();
				generateTable(table);
			}
			
			/*
			for (String candidateTable: candidateTables) {
				System.out.println(candidateTable);
			}
			*/
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	// TODO: Write the output in a proper SQL file
	public static void generateSchema(SjonSchema schema) {
		System.out.println(schema.toSQL());
	}
	
	// TODO: Write the output in a proper SQL file
	public static void generateTable(SjonTable table) throws Exception {
		System.out.println(table.toDML());
	}
}
