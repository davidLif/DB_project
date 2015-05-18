package db_parsers;

import db_entities.*;

public class FactsFile_parser implements File_parser{
	
	static int happenedInNum =0;
	
	static String happenedIn_relation = "<happenedIn>";
	static String locatedIn_relation = "<isLocatedIn>";

	public void init() {
		// For the meanwhile each entity holds the relation between each other.This might change
	}

	public void filter(String line) throws Exception {
		int indexEntityStart = nth_occurence(3,line,"<");
		if (indexEntityStart == -1) {
			throw new Exception("ERROR: out of bound substring. "+indexEntityStart+","+line.length());
		}
		String relation = line.substring(indexEntityStart, line.indexOf(">", indexEntityStart)+1);
		
		if (relation.contains(happenedIn_relation)){
			happenedInNum++;
			
			//now get the two entities related
			indexEntityStart = nth_occurence(2,line,"<");
			String left_entity = line.substring(indexEntityStart, line.indexOf(">", indexEntityStart));
			
			indexEntityStart = nth_occurence(4,line,"<");
			String right_entity = line.substring(indexEntityStart, line.indexOf(">", indexEntityStart));
			
			//now get the proper objects fro the maps and set relation reference
			Conflict_entity conflict = TransitiveType_parser.conflictMap.get(left_entity);
			if (conflict == null){
				return;
			}
			else{
				Location_entity occurencePlace = TransitiveType_parser.locationsMap.get(right_entity);
				if (occurencePlace == null) {
					return;
				}
				else{
					System.out.println(left_entity +" happened in "+right_entity);
					conflict.addOcuurencePlace(occurencePlace);
				}
			}
			
		}
		/*else if(relation.contains(locatedIn_relation)){
			
		}*/
		
	}
	
	public int nth_occurence(int numberOfOccur,String findIn,String subToFind){
		
		int index=-1; 
		do {
			index = findIn.indexOf(subToFind,index+1); //each iteration start looking char after the previous finding
			numberOfOccur--;
		} while (index>=0 && numberOfOccur>0);
		
		return index;
	}

}
