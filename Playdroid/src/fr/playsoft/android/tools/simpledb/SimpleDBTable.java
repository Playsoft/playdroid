package fr.playsoft.android.tools.simpledb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import fr.playsoft.android.tools.debug.Log;

/**
 * Simple table used by SimpleDB.
 * Its final class - there is no reason to override it.
 * 
 * @author Olgierd Uzieblo
 */
public final class SimpleDBTable
{
	/** Tag for LogCat **/
	public static final String TAG = "SimpleDBTable";
	
	/** Flag to check if this table was changed - database will need saving **/
	private boolean mIsSavingNeeded = false;
	
	/** Is main key of this table unique for each row **/
	private boolean mIsMainKeyUnique = false;
	
	/**
	 * Data stored in this table.
	 */
	private List< SimpleDBRow > mData;
	
	/**
	 * Hashtable for fast getting of indexed primary keys.
	 */
	private Hashtable< Integer , Integer > mHashIndex;
	
	/**
	 * Factory for creating of new empty rows.
	 */
	private ISimpleDBRowFactory mRowFactory;
	
	/** Listener notified after any changes **/
	private ITableModifiedListener mTableChangedListener;
	
	/** Current largest id - useful for auto increment of row unique ids **/
	private int mCurrentLargestId;
	
	/**
	 * Creates SimpleDBTable.
	 */
	public SimpleDBTable( boolean pIsMainKeyUnique , ISimpleDBRowFactory pRowFactory )
	{
		mData = new CopyOnWriteArrayList< SimpleDBRow >();
		mHashIndex = new Hashtable< Integer , Integer >();
		mIsMainKeyUnique = pIsMainKeyUnique;
		mRowFactory = pRowFactory;
	}
	
	/**
	 * Sets listener to be notified after any change
	 * 
	 * @param pListener ITableChangedListener instance.
	 */
	public void setOnChangedListener( ITableModifiedListener pListener )
	{
		mTableChangedListener = pListener;
	}
	
	/**
	 * Tells this table that it has been changed and it should not skip saving.
	 */
	public void setSaveNeeded()
	{
		notifyOnChangedListener();
	}
	
	/**
	 * Gets current largest unique id. Useful for auto increment.
	 * 
	 * @return Largest id.
	 */
	public int getCurrentLargestId()
	{
		return mCurrentLargestId;
	}
	
	/**
	 * Notifies listener that something is changed.
	 */
	private void notifyOnChangedListener()
	{
		mIsSavingNeeded = true;
		if ( mTableChangedListener != null )
		{
			mTableChangedListener.onTableModified();
		}
	}
	
	/**
	 * Checks if this table has been changed - needs saving.
	 * 
	 * @return True if it has changed.
	 */
	protected boolean isSavingNeeded()
	{
		return mIsSavingNeeded;
	}
	
	/**
	 * Sets table changed status to false - should be called after table was saved.
	 */
	protected void setSaved()
	{
		mIsSavingNeeded = false;
	}
	
	/**
	 * Gets number of rows in this table.
	 * 
	 * @return Number of rows.
	 */
	public final short getCount()
	{
		return (short) ( mData.size() );
	}
	
	/**
	 * Gets all data in this table.
	 * 
	 * @return List with all rows in this table or null if there are no rows.
	 */
	public final List< SimpleDBRow > getAllData()
	{
		if ( mData.isEmpty() )
		{
			return null;
		}
		else
		{
			return mData;
		}
	}
	
	/**
	 * Inserts or updates this table.
	 * 
	 * @param pNewRow New row to insert/replace.
	 */
	public final synchronized void insertOrUpdate( SimpleDBRow pNewRow )
	{
		if ( mIsMainKeyUnique )
		{
			Integer lNewMainKey = pNewRow.getMainKey();
			mCurrentLargestId = Math.max( mCurrentLargestId , lNewMainKey );
			Integer lFinalIndex = mHashIndex.get( lNewMainKey );
			if ( lFinalIndex != null )
			{
				// This row exists - we can update it
				mData.set( lFinalIndex , pNewRow );
				notifyOnChangedListener();
				return;
			}
			else
			{
				// It does not exist, we should add it and add a new index
				mData.add( pNewRow );
				mHashIndex.put( lNewMainKey , mData.size() - 1 );
				notifyOnChangedListener();
				return;
			}
		}
		else
		{
			// This table is not indexed so we always perform insert
			// Perform an insert
			mData.add( pNewRow );
			notifyOnChangedListener();
		}
	}
	
	/**
	 * Rebuilds hash index after deleting a row in indexed table.
	 */
	private final synchronized void rebuildHashIndex()
	{
		if ( mIsMainKeyUnique )
		{
			mHashIndex.clear();
			int lRowIndex = 0;
			int lMainKey = 0;
			mCurrentLargestId = 0;
			
			for ( SimpleDBRow lRow : mData )
			{
				lMainKey = lRow.getMainKey();
				mHashIndex.put( lMainKey , lRowIndex );
				mCurrentLargestId = Math.max( mCurrentLargestId , lMainKey );
				lRowIndex++;
			}
		}
	}
	
	/**
	 * Gets all rows for which main key equals chosen main key.
	 * For tables with unique main key this will return only one row.
	 * Returns null if list is empty.
	 * 
	 * @param pKeyValue Main key value.
	 * @return List of rows or null if it is empty.
	 */
	public final synchronized List< SimpleDBRow > getAllRowsForMainKey( int pKeyValue )
	{
		List< SimpleDBRow > lResultRows = new ArrayList< SimpleDBRow >();
		for ( SimpleDBRow lRow : mData )
		{
			if ( lRow.getMainKey() == pKeyValue )
			{
				lResultRows.add( lRow );
			}
		}
		if ( lResultRows.isEmpty() )
		{
			return null;
		}
		else
		{
			return lResultRows;
		}
	}
	
	/**
	 * Deletes all rows for which main key equals chosen main key.
	 * 
	 * @param pKeyValue Main key value.
	 */
	public final synchronized void deleteAllRowsForMainKey( int pKeyValue )
	{
		List< SimpleDBRow > lResultRows = new ArrayList< SimpleDBRow >();
		for ( SimpleDBRow lRow : mData )
		{
			if ( lRow.getMainKey() == pKeyValue )
			{
				lResultRows.add( lRow );
			}
		}
		
		mData.removeAll( lResultRows );
		rebuildHashIndex();
		notifyOnChangedListener();
	}
	
	/**
	 * Deletes multiple rows at once.
	 * 
	 * @param pRowsToDelete List of rows to delete.
	 */
	public final synchronized void deleteMultipleRows( List< SimpleDBRow > pRowsToDelete )
	{
		if ( pRowsToDelete.size() > 0 )
		{
			mData.removeAll( pRowsToDelete );
			rebuildHashIndex();
			notifyOnChangedListener();
		}
	}
	
	/**
	 * Deletes row from chosen table using its unique key.
	 * 
	 * @param pKeyValue Value of unique key.
	 * 
	 * @return True if something was deleted.
	 */
	public final synchronized boolean delete( int pKeyValue )
	{
		Integer lRowIdToDelete = mHashIndex.get( pKeyValue );
		
		if ( lRowIdToDelete != null )
		{
			mData.remove( lRowIdToDelete.intValue() );
			mHashIndex.remove( pKeyValue );
			rebuildHashIndex();
			notifyOnChangedListener();
			return true;
		}
		
		return false;
	}
	
	/**
	 * Deletes all rows for this table.
	 */
	public final synchronized void deleteAllRows()
	{
		mData.clear();
		mHashIndex.clear();
		mCurrentLargestId = 0;
		notifyOnChangedListener();
	}
	
	/**
	 * Gets indexed row.
	 * 
	 * @param pKeyValue Key value - unique indexed value.
	 * @return Row from chosen table containing this key or null.
	 */
	public final synchronized SimpleDBRow getIndexedRow( int pKeyValue )
	{
		Integer lFinalIndex = mHashIndex.get( pKeyValue );
		if ( lFinalIndex != null )
		{
			return mData.get( lFinalIndex.intValue() );
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Loads this table from the DataInputStream.
	 * 
	 * @param pStream DataInputStream with data to use.
	 */
	protected final synchronized void load( DataInputStream pStream ) throws IOException
	{
		short lSize = pStream.readShort();
		Log.v( TAG , "Loading " + lSize + " rows..." );
		List< SimpleDBRow > lLoadedList = new ArrayList< SimpleDBRow >( lSize );
		mHashIndex.clear();
		mData.clear();
		int lMainKeyValue = 0;
		mCurrentLargestId = 0;
		
		for ( int i = 0; i < lSize; i++ )
		{
			SimpleDBRow lNewRow = mRowFactory.createNewEmptyRow();
			lNewRow.loadRow( pStream );
			lLoadedList.add( lNewRow );
			
			if ( mIsMainKeyUnique )
			{
				lMainKeyValue = lNewRow.getMainKey();
				mHashIndex.put( lMainKeyValue , i );
				mCurrentLargestId = Math.max( mCurrentLargestId , lMainKeyValue );
			}
		}
		
		mData.addAll( lLoadedList );
	}
	
	/**
	 * Saves this table to the DataOutputStream.
	 * 
	 * @param pStream DataOutputStream to save data to.
	 */
	protected final synchronized void save( DataOutputStream pStream ) throws IOException
	{
		short lSize = getCount();
		Log.v( TAG , "Saving " + lSize + " rows..." );
		pStream.writeShort( lSize );
		
		for ( SimpleDBRow lSavedRow : mData )
		{
			lSavedRow.saveRow( pStream );
		}
	}
}
