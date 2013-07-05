package fr.playsoft.android.tools.simpledb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import fr.playsoft.android.tools.debug.Log;

/**
 * Simple database implementation.
 * Database was designed to have optimal performance - it is stored in memory and loaded/saved from internal storage only once.
 * 
 * @author Olgierd Uzieblo
 */
public abstract class SimpleDB
{
	/** Name of preferences file **/
	private static final String PREFERENCES_FILE_NAME = "SimpleDBPreferencesFile";
	
	/** Prefix of database name key preference **/
	private static final String PREFERENCES_KEY_DB_NAME_PREFIX = "SimpleDBName";
	
	/** TAG for LogCat **/
	private static final String TAG = "SimpleDB";
	
	/** Real file name used by this db - equals "mBaseFileName + timestamp of last save" **/
	private String mDBFileName;
	
	/** Additional suffix concatenated with db name, can be null **/
	private String mAdditionalNameSuffix;
	
	/** Base file name - equals "getClass().getSimpleName() + mAdditionalNameSuffix" **/
	private String mBaseFileName;
	
	/** Context used **/
	private Context mContext;
	
	/** All the tables **/
	private List< SimpleDBTable > mTables;
	
	/** Preferences to be used for saving current database name **/
	private SharedPreferences mPreferences;
	
	/** Flag to check if saving is needed (if something has been changed) **/
	private boolean mIsSavingNeeded;
	
	/**
	 * Flag to check if GZIP compression is enabled during database loading/saving.
	 * It can save ~50% of space but loading times are longer:
	 * - about ~80% longer for loading time
	 * - about ~20% longer for saving time
	 */
	private boolean mIsGZIPEnabled = false;
	
	/** Listener to be called when database is loaded/saved **/
	private ISimpleDBLoadSaveListener mDBListener;
	
	/** Flag to check if database saving is in progress **/
	private boolean mIsSavingInProgress;
	
	/** Flag to check if database loading is in progress **/
	private boolean mIsLoadingInProgress = true;
	
	/** Handler for posting messages on UI thread **/
	private Handler mHandler;
	
	/** Current database version. When version changes old database will not be loaded - it will be cleared **/
	private int mCurrentDBVersion;
	
	/**
	 * Creates all tables used by this database.
	 * Returns list containing empty tables.
	 */
	protected abstract List< SimpleDBTable > createTables();
	
	/**
	 * Creates this SimpleDB instance and loads the data.
	 * 
	 * @param pContext Context to use.
	 * @param pAdditionalNameSuffix Additional part of database name - can be null. Should be used if there are multiple instances of the same
	 *            database - so that they can be saved in different files.
	 * @param pIsGZIPEnabled Is GZIP compression enabled for this database.
	 * @param pCurrentDBVersion Database version - if database structure changes this int should be changed too.
	 * @param pDBLoadSaveListener Listener to be called when database is loaded/saved. Can be null if you dont care.
	 */
	public SimpleDB( Context pContext , String pAdditionalNameSuffix , boolean pIsGZIPEnabled , int pCurrentDBVersion ,
			ISimpleDBLoadSaveListener pDBLoadSaveListener )
	{
		mContext = pContext;
		mAdditionalNameSuffix = pAdditionalNameSuffix;
		mIsGZIPEnabled = pIsGZIPEnabled;
		mCurrentDBVersion = pCurrentDBVersion;
		mDBListener = pDBLoadSaveListener;
		
		mPreferences = mContext.getSharedPreferences( PREFERENCES_FILE_NAME , Context.MODE_PRIVATE );
		mHandler = new Handler();
		mBaseFileName = this.getClass().getSimpleName();
		if ( mAdditionalNameSuffix != null )
		{
			mBaseFileName = mBaseFileName + mAdditionalNameSuffix;
		}
		
		// Getting current file name
		mDBFileName = mPreferences.getString( PREFERENCES_KEY_DB_NAME_PREFIX + mBaseFileName , mBaseFileName );
		
		// Create empty tables
		mTables = createTables();
		
		Log.i( TAG , "Initializing SimpleDB with file " + mDBFileName + "..." );
		
		// Load everything
		Thread lLoadingThread = new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				load();
				if ( mDBListener != null )
				{
					mHandler.post( new Runnable()
					{
						@Override
						public void run()
						{
							mIsLoadingInProgress = false;
							mDBListener.onDBLoaded( mBaseFileName );
						}
					} );
				}
			}
		} );
		lLoadingThread.setPriority( Thread.MAX_PRIORITY );
		lLoadingThread.start();
	}
	
	/**
	 * Sets new listener to be called when database finished loading/saving.
	 * 
	 * @param pDBLoadSaveListener New listener, can be null if you dont care.
	 */
	public final void setOnLoadSaveListener( ISimpleDBLoadSaveListener pDBLoadSaveListener )
	{
		mDBListener = pDBLoadSaveListener;
	}
	
	/**
	 * Inserts/updates a single row.
	 * 
	 * @param pTableId Table id.
	 * @param pDataToInsert Row to insert/update.
	 */
	public final synchronized void insertOrUpdate( int pTableId , SimpleDBRow pDataToInsert )
	{
		mTables.get( pTableId ).insertOrUpdate( pDataToInsert );
	}
	
	/**
	 * Gets single table.
	 * 
	 * @param pTableId Table id.
	 * @return Desired table.
	 */
	public final synchronized SimpleDBTable getTable( int pTableId )
	{
		return mTables.get( pTableId );
	}
	
	/**
	 * Clears whole database - clears all tables.
	 * Also deletes physical file from internal memory and updates preferences.
	 */
	public final synchronized void clearAndDeleteDatabase()
	{
		mIsSavingNeeded = true;
		mTables = createTables();
		if ( !mDBFileName.equals( mBaseFileName ) )
		{
			mContext.deleteFile( mDBFileName );
			SharedPreferences.Editor lEditor = mPreferences.edit();
			// Set default database name
			lEditor.putString( PREFERENCES_KEY_DB_NAME_PREFIX + mBaseFileName , mBaseFileName );
			lEditor.commit();
		}
	}
	
	/**
	 * Checks if this database needs to be saved.
	 * 
	 * @return True if something has changed and this database should be saved.
	 */
	public final synchronized boolean isSavingNeeded()
	{
		for ( SimpleDBTable lTable : mTables )
		{
			mIsSavingNeeded |= lTable.isSavingNeeded();
		}
		
		return mIsSavingNeeded;
	}
	
	/**
	 * Gets file name of currently used database.
	 * 
	 * @return DB file name.
	 */
	public final String getCurrendDBName()
	{
		return mDBFileName;
	}
	
	/**
	 * Checks if chosen row exists for chosen table.
	 * Works only for indexed tables.
	 * 
	 * @param pTableId Table id.
	 * @param pKeyValue Unique key value.
	 * @return True if such row exists.
	 */
	public final synchronized boolean isIndexedRowExists( int pTableId , int pKeyValue )
	{
		if ( mTables.get( pTableId ).getIndexedRow( pKeyValue ) != null )
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Gets all rows for which main key equals chosen main key.
	 * For tables with unique main key this will return only one row.
	 * Returns null if list is empty.
	 * 
	 * @param pTableId Table id.
	 * @param pKeyValue Main key value.
	 * @return List of rows or null if it is empty.
	 */
	public final synchronized List< SimpleDBRow > getAllRowsForMainKey( int pTableId , int pKeyValue )
	{
		return mTables.get( pTableId ).getAllRowsForMainKey( pKeyValue );
	}
	
	/**
	 * Gets all rows from chosen table.
	 * 
	 * @param pTableId Table id.
	 * @return List with all rows in this table or null if there is no data!
	 */
	public final synchronized List< SimpleDBRow > getAllTableRows( int pTableId )
	{
		return mTables.get( pTableId ).getAllData();
	}
	
	/**
	 * Deletes all rows for which main key equals chosen main key.
	 * 
	 * @param pTableId Table id.
	 * @param pKeyValue Main key value.
	 */
	public final synchronized void deleteAllRowsForMainKey( int pTableId , int pKeyValue )
	{
		mTables.get( pTableId ).deleteAllRowsForMainKey( pKeyValue );
	}
	
	/**
	 * Deletes row from chosen table using its unique key.
	 * 
	 * @param pTableId Table id.
	 * @param pKeyValue Value of unique key.
	 */
	public final synchronized void delete( int pTableId , int pKeyValue )
	{
		mTables.get( pTableId ).delete( pKeyValue );
	}
	
	/**
	 * Checks if database saving is in progress.
	 * 
	 * @return True if saving is in progress.
	 */
	public final boolean isSavingInProgress()
	{
		return mIsSavingInProgress;
	}
	
	/**
	 * Checks if database loading is in progress.
	 * 
	 * @return True if loading is in progress.
	 */
	public final boolean isLoadingInProgress()
	{
		return mIsLoadingInProgress;
	}
	
	/**
	 * Loads the database if it exists.
	 */
	private final synchronized void load()
	{
		if ( mDBFileName.equals( mBaseFileName ) )
		{
			// Database name is the same as its base name = it is first launch, we have no db!
			return;
		}
		
		try
		{
			long lLoadStartTime = System.currentTimeMillis();
			
			byte[] lFileContent;
			FileInputStream lFile;
			
			try
			{
				lFile = mContext.openFileInput( mDBFileName );
				int lFileSize = lFile.available();
				lFileContent = new byte[ lFileSize ];
				lFile.read( lFileContent );
				lFile.close();
			}
			catch( Exception e )
			{
				// Unknown error, this should never happen!
				Log.e( TAG , "Failed to read SimpleDB file " + mDBFileName + " !!!" , e );
				return;
			}
			
			DataInputStream lStream;
			if ( mIsGZIPEnabled )
			{
				lStream = new DataInputStream( new GZIPInputStream( new ByteArrayInputStream( lFileContent ) ) );
			}
			else
			{
				lStream = new DataInputStream( new ByteArrayInputStream( lFileContent ) );
			}
			
			int lDBVersion = lStream.readInt();
			if ( lDBVersion != mCurrentDBVersion )
			{
				// We have old database... we have to skip the loading or it will crash
				lStream.close();
				Log.i( TAG , "Skipped database loading - version has changed!" );
				mDBListener.onDBVersionChanged( mBaseFileName );
				return;
			}
			
			short lNumTables = (short) mTables.size();
			for ( int i = 0; i < lNumTables; i++ )
			{
				Log.v( TAG , "Loading table " + i + " from " + mDBFileName );
				mTables.get( i ).load( lStream );
			}
			lStream.close();
			
			Log.i( TAG , "Successfully loaded SimpleDB from " + mDBFileName + " file size was " + lFileContent.length + " bytes." );
			
			long lLoadEndTime = System.currentTimeMillis();
			Log.v( TAG , "Loading time: " + ( lLoadEndTime - lLoadStartTime ) + "ms." );
		}
		catch( Exception e )
		{
			Log.e( TAG , "Unknown exception while loading " + mDBFileName + "." , e );
		}
	}
	
	/**
	 * Saves the database. Database is saved in a different file than original db.
	 * Original db is deleted only if save was successful.
	 */
	public final synchronized void save()
	{
		if ( !isSavingNeeded() )
		{
			Log.i( TAG , "Database saving was skipped - nothing has changed!" );
			return;
		}
		if ( mIsSavingInProgress )
		{
			Log.w( TAG , "Database saving was skipped - saving is already in progress!" );
			return;
		}
		if ( mIsLoadingInProgress )
		{
			Log.w( TAG , "Database saving was skipped - loading is in progress! Cant save/load at the same time." );
			return;
		}
		
		mIsSavingInProgress = true;
		Thread lSavingThread = new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				long lSaveStartTime = System.currentTimeMillis();
				try
				{
					ByteArrayOutputStream lByteArrayStream = new ByteArrayOutputStream();
					DataOutputStream lDataStream = new DataOutputStream( lByteArrayStream );
					
					// Save current database version
					lDataStream.writeInt( mCurrentDBVersion );
					
					short lNumTables = (short) mTables.size();
					
					// We first prepare all data in the array and then save it all at once
					for ( int i = 0; i < lNumTables; i++ )
					{
						Log.v( TAG , "Saving table " + i );
						mTables.get( i ).save( lDataStream );
					}
					
					lDataStream.close();
					byte[] lAllData = lByteArrayStream.toByteArray();
					lByteArrayStream.close();
					
					// Save all data!
					// We create a new file name using default prefix
					String lNewDBFileName = mBaseFileName + String.valueOf( System.currentTimeMillis() );
					FileOutputStream lFileStream = mContext.openFileOutput( lNewDBFileName , Context.MODE_PRIVATE );
					if ( mIsGZIPEnabled )
					{
						GZIPOutputStream lGZIPStream = new GZIPOutputStream( lFileStream );
						lGZIPStream.write( lAllData );
						lGZIPStream.close();
					}
					else
					{
						lFileStream.write( lAllData );
					}
					lFileStream.close();
					
					Log.i( TAG , "Successfully saved SimpleDB to file " + lNewDBFileName + " real data size was = " + lAllData.length + " bytes." );
					
					// Save new database name to preferences
					SharedPreferences.Editor lEditor = mPreferences.edit();
					lEditor.putString( PREFERENCES_KEY_DB_NAME_PREFIX + mBaseFileName , lNewDBFileName );
					lEditor.commit();
					
					// Copy old database file name
					String lOldDBFileName = mDBFileName;
					
					// Set new current name
					mDBFileName = lNewDBFileName;
					
					// Finally - delete old database from internal memory
					mContext.deleteFile( lOldDBFileName );
					
					// Set saving needed flag to false
					mIsSavingNeeded = false;
					
					// Set all tables state to saved
					for ( SimpleDBTable lTable : mTables )
					{
						lTable.setSaved();
					}
					
					// Thats all, database is saved successfully!
					Log.i( TAG , "Successfully deleted old database " + lOldDBFileName );
					
					mIsSavingInProgress = false;
					if ( mDBListener != null )
					{
						mHandler.post( new Runnable()
						{
							@Override
							public void run()
							{
								mDBListener.onDBSaved( mBaseFileName , true );
							}
						} );
					}
					
					long lSaveEndTime = System.currentTimeMillis();
					Log.v( TAG , "Saving time: " + ( lSaveEndTime - lSaveStartTime ) + "ms." );
				}
				catch( Exception e )
				{
					Log.e( TAG , "Failed to save SimpleDB to file! Out of memory maybe?" , e );
					if ( mDBListener != null )
					{
						mHandler.post( new Runnable()
						{
							@Override
							public void run()
							{
								mDBListener.onDBSaved( mBaseFileName , false );
							}
						} );
					}
				}
				
			}
		} );
		lSavingThread.setPriority( Thread.MAX_PRIORITY );
		lSavingThread.start();
	}
}
