package org.sjon.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

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
	
	@Test
	public void testDataLoading() throws Exception {
	
		SjonTable season = new SjonTable("season");
		SjonTable competition = new SjonTable("competition");
		SjonTable team = new SjonTable("team");
		
		season.loadData("resources/data/season.sjon");
		competition.loadData("resources/data/competition.sjon");
		team.loadData("resources/data/team.sjon");
		
		assertEquals(5, season.getRecords().size());
		assertEquals(7, competition.getRecords().size());
		assertEquals(318, team.getRecords().size());
		
		boolean seasonTested = false;
		
		// Check that the '2015' season exists in data
		for (SjonRecord seasonRecord:season.getRecords()) {
			if (seasonRecord.getValue("name").equals("2015")) {
				assertEquals("2015", seasonRecord.getValue("year"));
				assertEquals("false", seasonRecord.getValue("winter"));
				seasonTested = true;
				break;
			}
		}
		
		Set<String> dynamicRankingCompetitionIds = new HashSet<String>(); 
		
		for (SjonRecord competitionRecord:competition.getRecords()) {
			if (competitionRecord.getValue("staticRanking").equals("false")) {
				dynamicRankingCompetitionIds.add( (String) competitionRecord.getValue("id"));
			}
		}
		
		assertEquals(6, dynamicRankingCompetitionIds.size());
		assertEquals(true, dynamicRankingCompetitionIds.contains("3"));
		
		Set<String> germanTeamNames = new HashSet<String>();
		Set<String> finnishTeamNames = new HashSet<String>();
		
		for (SjonRecord teamRecord:team.getRecords()) {
			if (teamRecord.getValue("nation").equals("Germany")) {
				germanTeamNames.add( (String) teamRecord.getValue("name"));
			}
			if (teamRecord.getValue("nation").equals("Finland")) {
				finnishTeamNames.add( (String) teamRecord.getValue("name"));
			}
		}
		
		assertEquals(48, germanTeamNames.size());
		assertEquals(true, germanTeamNames.contains("Dresden"));
		
		assertEquals(32, finnishTeamNames.size());
		assertEquals(true, finnishTeamNames.contains("JIPPO"));
		
		if (!seasonTested) {
			fail("Season test record not found");
		}
	}
}
