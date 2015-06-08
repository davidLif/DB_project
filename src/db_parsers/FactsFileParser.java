package db_parsers;

import java.util.HashMap;

import db_entities.*;

public class FactsFileParser extends FileParser{
	
	public static int happenedInNum = 0;
	public static int participatedInNum = 0 ;
	public static int isLeaderOfNum = 0;
	public static int locatedInNum = 0;
	public static int hasCapitalNum = 0;
	public static int hasCurrencyNum = 0;
	public static int hasOfficialLanguageNum = 0;
	public static int ownsNum = 0;
	public static int diedInNum = 0;
	public static int bornInNum = 0;
	public static int hasGenderNum = 0;
	
	
	static String happenedIn_relation = "<happenedIn>";
	static String participatedIn_relation = "<participatedIn>";
	static String isLeaderOf_relation = "<isLeaderOf>";
	static String locatedIn_relation = "<isLocatedIn>";
	static String hasCapital_relation = "<hasCapital>";
	static String hasCurrency_relation = "<hasCurrency>";
	static String hasOfficialLanguage_relation = "<hasOfficialLanguage>";
	static String owns_relation = "<owns>";
	static String diedIn_relation = "<diedIn>";
	static String bornIn_relation = "<wasBornIn>";
	static String hasGender_relation = "<hasGender>";
	


	public void init() {
		// For the meanwhile each entity holds the relation between each other.This might change
		ParsedData.leadersMap = new HashMap<String,LeaderEntity>(3000);
		ParsedData.constructionsMap = new HashMap<String,ConstructionEntity>();
	}

	public void filter(String line){
		int indexEntityStart = FileParser.nth_occurence(3,line,"<");
		
		String relation = line.substring(indexEntityStart, line.indexOf(">", indexEntityStart)+1);
		
		if (relation.contains(happenedIn_relation)){
			
			happnedInRelation_mapSetter(line);
			
		}
		else if(relation.contains(participatedIn_relation)){
			
			participatedInRelation_mapSetter(line);
		}
		else if(relation.contains(isLeaderOf_relation)){
			
			isLeaderOfRelation_mapSetter(line);
		}
		else if(relation.contains(locatedIn_relation)){
			
			locatedInRelation_mapSetter(line);
		}
		else if(relation.contains(hasCapital_relation)){
			
			hasCapitalRelation_mapSetter(line);
		}
		else if(relation.contains(hasCurrency_relation)){
			hasCurrencyRelation_mapSetter(line);
		}
		else if(relation.contains(hasOfficialLanguage_relation)){
			hasOfficialLanguage_mapSetter(line);
		}
		else if(relation.contains(owns_relation)){
			owns_mapSetter(line);
		}
		else if(relation.contains(bornIn_relation)){
			bornIn_mapSetter(line);
		}
		else if(relation.contains(diedIn_relation)){
			diedIn_mapSetter(line);
		}
		else if(relation.contains(hasGender_relation)){
			hasGender_mapSetter(line);
		}
		
	}

	private void hasGender_mapSetter(String line) {
		//now get the two entities related
		StringBuilder left_container = new StringBuilder();
		String right_entity = get_Left_Right_Entities(line,left_container);
		String left_entity = left_container.toString();
		
		LeaderEntity leader = ParsedData.leadersMap.get(left_entity);
		if (leader == null){
			return;
		}
		else{
			hasGenderNum++;
			if (right_entity.equals("<male>")){
				leader.setLeaderGender(LeaderEntity.gender.male);
			}
			else {
				leader.setLeaderGender(LeaderEntity.gender.female);
			}
		}
	}

	private void diedIn_mapSetter(String line) {
		//now get the two entities related
		StringBuilder left_container = new StringBuilder();
		String right_entity = get_Left_Right_Entities(line,left_container);
		String left_entity = left_container.toString();

		//now get the proper objects from the maps and set relation reference
		AdministrativeLocationEntity location = ParsedData.locationsMap.get(right_entity);
			
		if (location == null) {
			return;
		}
		else {
			LeaderEntity leader = ParsedData.leadersMap.get(left_entity);
			if (leader == null){
				return;
			}
			else {
				diedInNum++;
				leader.setDeathLocation(location);
			}
		}
	}

	private void bornIn_mapSetter(String line) {
		//now get the two entities related
		StringBuilder left_container = new StringBuilder();
		String right_entity = get_Left_Right_Entities(line,left_container);
		String left_entity = left_container.toString();

		//now get the proper objects from the maps and set relation reference
		AdministrativeLocationEntity location = ParsedData.locationsMap.get(right_entity);
			
		if (location == null) {
			return;
		}
		else {
			LeaderEntity leader = ParsedData.leadersMap.get(left_entity);
			if (leader == null){
				return;
			}
			else {
				bornInNum++;
				leader.setBreathLocation(location);
			}
		}
	}

	private void owns_mapSetter(String line) {
		//now get the two entities related
		StringBuilder left_container = new StringBuilder();
		String right_entity = get_Left_Right_Entities(line,left_container);
		String left_entity = left_container.toString();
		
		//now get the proper objects from the maps and set relation reference
		AdministrativeLocationEntity location = ParsedData.locationsMap.get(left_entity);
			
		if (location == null) {
			return;
		}
		else {
			//nof map check because the same construction cann't have 2 owners
			ownsNum++;
			ConstructionEntity construction= new ConstructionEntity();
			construction.setConstructionLocation(location);
			location.addConstruction(construction);
			ParsedData.constructionsMap.put(right_entity,construction);
		}
	}

	private void hasOfficialLanguage_mapSetter(String line) {
		//now get the two entities related
		StringBuilder left_container = new StringBuilder();
		String right_entity = get_Left_Right_Entities(line,left_container);
		String left_entity = left_container.toString();
		
		//now get the proper objects from the maps and set relation reference
		AdministrativeLocationEntity location = ParsedData.locationsMap.get(left_entity);
			
		if (location == null || !(location instanceof CountryEntity)) {
			return;
		}
		else {
			LanguageEntity officialLang = ParsedData.langugagesMap.get(right_entity);
			if (officialLang == null){
				return;
			}
			else {
				hasOfficialLanguageNum++;
				((CountryEntity)location).addOfficialLanguage(officialLang);
			}
		}
	}

	private void hasCurrencyRelation_mapSetter(String line) {
		//now get the two entities related
		StringBuilder left_container = new StringBuilder();
		String right_entity = get_Left_Right_Entities(line,left_container);
		String left_entity = left_container.toString();
		
		//now get the proper objects from the maps and set relation reference
		AdministrativeLocationEntity location = ParsedData.locationsMap.get(left_entity);
			
		if (location == null || !(location instanceof CountryEntity)) {
			return;
		}
		else {
			CurrencyEntity currency = ParsedData.currenciesMap.get(right_entity);
			if (currency == null){
				return;
			}
			else {
				hasCurrencyNum++;
				((CountryEntity)location).setCurrency(currency);
			}
		}
	}

	private void happnedInRelation_mapSetter(String line) {
		//now get the two entities related
		StringBuilder left_container = new StringBuilder();
		String right_entity = get_Left_Right_Entities(line,left_container);
		String left_entity = left_container.toString();
		
		//now get the proper objects fro the maps and set relation reference
		ConflictEntity conflict = ParsedData.conflictMap.get(left_entity);
		if (conflict == null){
			return;
		}
		else{
			AdministrativeLocationEntity occurencePlace = ParsedData.locationsMap.get(right_entity);
			if (occurencePlace == null) {
				return;
			}
			else{
				happenedInNum++;
				conflict.addOcuurencePlace(occurencePlace);
			}
		}
	}

	private void participatedInRelation_mapSetter(String line) {
		//now get the two entities related
		StringBuilder left_container = new StringBuilder();
		String right_entity = get_Left_Right_Entities(line,left_container);
		String left_entity = left_container.toString();
		
		//now get the proper objects fro the maps and set relation reference
		ConflictEntity conflict = ParsedData.conflictMap.get(right_entity);
		if (conflict == null){
			return;
		}
		else{
			AdministrativeLocationEntity occurencePlace = ParsedData.locationsMap.get(left_entity);
			if (occurencePlace == null) {
				return;
			}
			else {
				participatedInNum++;
				conflict.addParticipantsINPlace(occurencePlace);
			}
		}
	}

	private void isLeaderOfRelation_mapSetter(String line) {
		//now get the two entities related
		StringBuilder left_container = new StringBuilder();
		String right_entity = get_Left_Right_Entities(line,left_container);
		String left_entity = left_container.toString();
		
		//now get the proper objects from the maps and set relation reference
		AdministrativeLocationEntity location = ParsedData.locationsMap.get(right_entity);
			
		if (location == null) {
			return;
		}
		else {
			isLeaderOfNum++;
			if (ParsedData.leadersMap.get(left_entity) != null){
				ParsedData.leadersMap.get(left_entity).addLeaderOf(location);
			}
			else { //make the new leader and add his relation and to map
				LeaderEntity leaderOfLocation = new LeaderEntity();
				leaderOfLocation.addLeaderOf(location);
				ParsedData.leadersMap.put(left_entity,leaderOfLocation);
			}
		}
	}

	private void locatedInRelation_mapSetter(String line) {
		//now get the two entities related
		StringBuilder left_container = new StringBuilder();
		String right_entity = get_Left_Right_Entities(line,left_container);
		String left_entity = left_container.toString();

		//now get the proper objects from the maps and set relation reference
		AdministrativeLocationEntity inlocation = ParsedData.locationsMap.get(left_entity);
			
		if (inlocation == null) {
			return;
		}
		else {
			//first case - city in country
			if (inlocation instanceof CityEntity){ //check if city
				AdministrativeLocationEntity outerlocation = ParsedData.locationsMap.get(right_entity);
				if (outerlocation == null || !(outerlocation instanceof CountryEntity)){ //second if case so we wont have city in city
					return;
				}
				else {
					locatedInNum++;
					((CityEntity)inlocation).setLocatedIn((CountryEntity)outerlocation);
				}
				
			}
			//second case - country in continent
			else if (inlocation instanceof CountryEntity){ //country
				ContinentEntity continentForCity = ParsedData.continentsMap.get(right_entity);
				if(continentForCity == null){
					return;
				}
				else{
					locatedInNum++;
					((CountryEntity)inlocation).setLocatedIn((ContinentEntity)continentForCity);
				}
			}
		}
	}

	private void hasCapitalRelation_mapSetter(String line) {
		//now get the two entities related
		StringBuilder left_container = new StringBuilder();
		String right_entity = get_Left_Right_Entities(line,left_container);
		String left_entity = left_container.toString();
		
		//now get the proper objects fro the maps and set relation reference
		AdministrativeLocationEntity capitalCity = ParsedData.locationsMap.get(right_entity);
		if (capitalCity == null || !(capitalCity instanceof CityEntity)){
			return;
		}
		else{
			AdministrativeLocationEntity  countryLoc = ParsedData.locationsMap.get(left_entity);
			if (countryLoc == null || !(countryLoc instanceof CountryEntity)) {
				return;
			}
			else {
				hasCapitalNum++;
				((CountryEntity)countryLoc).setCapital((CityEntity)capitalCity);
			}
		}
	}
	
	//This function will return the right entity and return by reference the left one in 
	private String get_Left_Right_Entities(String line,StringBuilder left_entity){
		
		int indexEntityStart = FileParser.nth_occurence(2,line,"<");
		left_entity.append(line.substring(indexEntityStart, line.indexOf(">", indexEntityStart)+1));
		
		
		indexEntityStart = FileParser.nth_occurence(4,line,"<");
		return line.substring(indexEntityStart, line.indexOf(">", indexEntityStart)+1);
	}
	
	

}