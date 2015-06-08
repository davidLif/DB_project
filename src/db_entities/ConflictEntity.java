package db_entities;

import java.util.ArrayList;
import java.util.List;


/**
 * This is an abstract parent class for the battle and war entities.
 * All the information held within the objects of this class will go to a record in MilitaryActions.
 */

public abstract class ConflictEntity extends Entity{
	
	
	private static final long serialVersionUID = 8077778786343056042L;
	//The starting date of the conflict
	public Date happenedOnDate;
	
	// A Wikipedia link to the article about this conflict
	public String wikiURL;
	
	// The length of the Wikipedia article about this conflict
	public int wikiLen;
	
	//A list of the locations where the conflict happened
	public List<AdministrativeLocationEntity> conflictLocations;
	
	// A list of countries/cities that participated in this conflict
	public List<AdministrativeLocationEntity> conflictParticipants;
	
	ConflictEntity(){
		
		conflictLocations = new ArrayList<AdministrativeLocationEntity>(); 
		conflictParticipants = new ArrayList<AdministrativeLocationEntity>(); 
		
		wikiLen = 0;
	}
	
	/**
	 * @return the conflictLocations
	 */
	public List<AdministrativeLocationEntity> getConflictLocations() {
		return conflictLocations;
	}

	/**
	 * @return the conflictParticipants
	 */
	public List<AdministrativeLocationEntity> getConflictParticipants() {
		return conflictParticipants;
	}

	public int getWikiLen() {
		return wikiLen;
	}

	public void setWikiLen(int wikiLen) {
		this.wikiLen = wikiLen;
	}

	public String getWikiURL() {
		return wikiURL;
	}

	public void setWikiURL(String wikiURL) {
		this.wikiURL = wikiURL;
	}

	public Date getHappenedOnDate() {
		return happenedOnDate;
	}

	public void setHappenedOnDate(Date happenedOnDate) {
		this.happenedOnDate = happenedOnDate;
	}

	public void addOcuurencePlace(AdministrativeLocationEntity loc){
		conflictLocations.add(loc);
	}
	
	public void addParticipantsINPlace(AdministrativeLocationEntity loc){
		conflictParticipants.add(loc);
	}
	

	
	

}
