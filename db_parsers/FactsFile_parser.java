package db_parsers;

import db_entities.*;

public class FactsFile_parser implements File_parser{
	
	static int happenedInNum = 0;
	static int participatedInNum = 0 ;
	
	static String happenedIn_relation = "<happenedIn>";
	static String participatedIn_relation = "<participatedIn>";

	public void init() {
		// For the meanwhile each entity holds the relation between each other.This might change
	}

	public void filter(String line) throws Exception {
		int indexEntityStart = Maps_entitys.nth_occurence(3,line,"<");
		if (indexEntityStart == -1) {
			throw new Exception("ERROR: out of bound substring. "+indexEntityStart+","+line.length());
		}
		String relation = line.substring(indexEntityStart, line.indexOf(">", indexEntityStart)+1);
		
		if (relation.contains(happenedIn_relation)){
			happenedInNum++;
			
			//now get the two entities related
			indexEntityStart = Maps_entitys.nth_occurence(2,line,"<");
			String left_entity = line.substring(indexEntityStart, line.indexOf(">", indexEntityStart)+1);
			
			indexEntityStart = Maps_entitys.nth_occurence(4,line,"<");
			String right_entity = line.substring(indexEntityStart, line.indexOf(">", indexEntityStart)+1);
			
			//now get the proper objects fro the maps and set relation reference
			Conflict_entity conflict = Maps_entitys.conflictMap.get(left_entity);
			if (conflict == null){
				return;
			}
			else{
				Location_entity occurencePlace = Maps_entitys.locationsMap.get(right_entity);
				if (occurencePlace == null) {
					return;
				}
				else{
					conflict.addOcuurencePlace(occurencePlace);
				}
			}
			
		}
		else if(relation.contains(participatedIn_relation)){
			participatedInNum++;
			
			//now get the two entities related
			indexEntityStart = Maps_entitys.nth_occurence(2,line,"<");
			String left_entity = line.substring(indexEntityStart, line.indexOf(">", indexEntityStart)+1);
			
			indexEntityStart = Maps_entitys.nth_occurence(4,line,"<");
			String right_entity = line.substring(indexEntityStart, line.indexOf(">", indexEntityStart)+1);
			
			//now get the proper objects fro the maps and set relation reference
			Conflict_entity conflict = Maps_entitys.conflictMap.get(right_entity);
			if (conflict == null){
				return;
			}
			else{
				Location_entity occurencePlace = Maps_entitys.locationsMap.get(left_entity);
				if (occurencePlace == null) {
					return;
				}
				else {
					conflict.addParticipantsINPlace(occurencePlace);
				}
			}
		}
		
	}
	
	

}
