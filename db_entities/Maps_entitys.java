package db_entities;

import java.util.Map;

public class Maps_entitys {

	public static Map<String,Conflict_entity> conflictMap;
	public static Map<String,Location_entity> locationsMap;
	public static Map<String,Language_entity> langugagesMap;
	
	public static int nth_occurence(int numberOfOccur,String findIn,String subToFind){
		
		int index=-1; 
		do {
			index = findIn.indexOf(subToFind,index+1); //each iteration start looking char after the previous finding
			numberOfOccur--;
		} while (index>=0 && numberOfOccur>0);
		
		return index;
	}
	
}
