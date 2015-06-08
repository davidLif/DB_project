package upload.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;

import db_entities.CityEntity;
import db_entities.CountryEntity;
import db_entities.Entity;

public class CapitalCitiesUploader extends EntityUploader{

	public CapitalCitiesUploader(Connection sqlConnection,
			Iterator<? extends Entity> entityIterator) {
		super(sqlConnection, entityIterator);
		
	}

	@Override
	protected String getQueryString() {
		return "UPDATE Country SET `idCapitalCity` = ? WHERE idCountry = ?";
	}
	
	
	@Override
	protected void setStatementArgs(PreparedStatement statement, Entity entity) throws SQLException
	{
		CountryEntity country = (CountryEntity) entity;
		
		CityEntity capital = country.getCapital();
		if(capital == null || !capital.isValid())
		{
			statement.setNull(1, Types.INTEGER);
		}
		else
		{
			statement.setInt(1, capital.getID());
		}
		
		statement.setInt(2, country.getID());
		
	}

	

}
