package org.sjon.sql;

import java.io.File;

import org.junit.Test;

public class SjonSchemaTest {
	
	@Test
	public void testSchema() {
		
		try {
			SjonSchema schema = new SjonSchema(new File("resources/data/schema.sjon"));
			System.out.println(schema.toSQL());
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
