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
import org.sjon.sql.exceptions.TypeValidationException;

public class Main {

	public static void main(String[] args) {
		
		if (args.length < 1) {
			System.out.println("USAGE: <input-directory> [<output-directory>]");
			System.exit(1);
		}
		
		// Check that directory exists
		
		Path dbPath = Paths.get(args[0]);
		Path sqlPath = Paths.get(args[0]);
		
		if (args.length == 2) {
			sqlPath = Paths.get(args[1]);
		} 
		
		if (!Files.isDirectory(dbPath)) {
			System.out.println("An input directory must be specified");
			System.exit(2);
		}
		
		if (!Files.isDirectory(sqlPath)) {
			System.out.println("An output directory must be specified");
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
			
			generateSchema(schema, sqlPath);
			
			for (SjonTable table: tables) {
				System.out.println();
				generateTable(table, sqlPath);
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
	
	// Simply output to console
	public static void generateSchema(SjonSchema schema) {
		System.out.println(schema.toSQL());
	}
	
	public static void generateSchema(SjonSchema schema, Path sqlPath) throws IOException {
		Files.write(Paths.get(sqlPath.toString(), "schema.sql"), schema.toSQL().getBytes());	
	}
	
	// Simply output to console
	public static void generateTable(SjonTable table) throws Exception {
		// System.out.println("Generating table: " + table.getName());
		System.out.println(table.toDML());
	}
	
	public static void generateTable(SjonTable table, Path sqlPath) throws IOException, TypeValidationException {
		Files.write(Paths.get(sqlPath.toString(), table.getName() + ".sql"), table.toDML().getBytes());
	}
}
