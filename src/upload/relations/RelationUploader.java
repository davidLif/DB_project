package upload.relations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import upload.DBUploader;

import db_entities.Entity;

public abstract class RelationUploader extends DBUploader {

	public RelationUploader(Connection sqlConnection, Iterator<? extends Entity> entityIt) {
		super(sqlConnection);
		
		this.entityIt = entityIt;
		
		// set the first relation list
		this.currentIndex = 0;
		nextRelationList();
	}
	
	/**
	 * iterates over the entity collection
	 */
	protected Iterator<? extends Entity> entityIt;

	/**
	 * Entity list that is currently being uploaded
	 */

	protected List<? extends Entity> currentRelationList;
	
	/**
	* which relation entry we need to upload next
	*/
	protected int currentIndex; 
	
	
	protected boolean hasMore()
	{
		return entityIt.hasNext() || currentIndex < currentRelationList.size();
	}
	
	/**
	 * advance to the next list that is part of the relation
	 */
	protected abstract void nextRelationList();
	
	@Override
	protected void createBatch()
	{
		// do nothing, prepareBatch() takes cares of it
	
	}
	
	@Override
	protected void postCommit(PreparedStatement stmt) throws SQLException
	{ 
		// do nothing
	}

	
	
	/**
	 * Constructs a prepareStatement object
	 * No keys are generated
	 * @return
	 * @throws SQLException
	 */
	@Override
	protected PreparedStatement constructPreparedStatement() throws SQLException
	{
		return sqlConn.prepareStatement(getQueryString());
		
	}
	
	
	/**
	 * Fills the statement object with the information from each entity object in current batch
	 * @param stmt
	 * @throws SQLException
	 */
	@Override
	protected void prepareBatch(PreparedStatement stmt) throws SQLException
	{
		
		int index = 0;
		
		while(index < BATCH_SIZE)
		{
			while(currentIndex == currentRelationList.size())
			{
				// reached end of list
				if(!entityIt.hasNext())
				{
					// no more entities
					this.actualBatchSize = index;
					return;
				}
				
				nextRelationList();
				currentIndex = 0;
			}
			
			// add tuple to batch and advance to next index
			setStatementArgs(stmt, this.currentRelationList.get(currentIndex));
			stmt.addBatch();
			this.currentIndex++;   // list index
			index++;               // batch index
		}
		
		this.actualBatchSize = index;
	}
	
	
	/**
	 * Set parameters for current tuple, according to given entity
	 * @param statement
	 * @param entity
	 * @throws SQLException
	 */
	
	abstract void setStatementArgs(PreparedStatement statement, Entity entity) throws SQLException;
	
	
	

	

}
