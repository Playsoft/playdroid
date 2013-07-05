package fr.playsoft.android.tools.resource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import fr.playsoft.android.tools.debug.Log;

/**
 * Single data downloading thread.
 * 
 * @author Olgierd Uzieblo
 */
public class DownloadThread implements Runnable
{
	/** Tag for this class **/
	public static final String TAG = "DownloadThread";
	
	/** Thread used for downloading of small files **/
	public static final int THREAD_TYPE_SMALL_FILES = 0;
	
	/** Thread used for downloading of large files **/
	public static final int THREAD_TYPE_LARGE_FILES = 1;
	
	/** Max retries before task will be failed **/
	public static final int MAX_RETRIES = 3;
	
	/** Max physical size of file for small files engine - if image is larger download will be terminated! **/
	public static final int MAX_IMAGE_SIZE = 500000;
	
	/** Task currently being downloaded **/
	private DownloadTask mCurrentTask;
	
	/** Is thread running **/
	private boolean mIsRunning = true;
	
	/** Flag to trigger pause **/
	private boolean mTriggerPause = true;
	
	/** Context **/
	private Context mContext;
	
	/** Array of locally used memory slots **/
	private CopyOnWriteArrayList< MemorySlot > mLocalMemorySlots;
	
	/** Type of this thread **/
	private int mThreadType;
	
	/**
	 * Deallocates all local memory slots used by this thread.
	 */
	public void deallocateLocalMemorySlots()
	{
		MemoryManager.deallocateMemorySlots( mLocalMemorySlots );
		mLocalMemorySlots.clear(); // To make it empty
	}
	
	/**
	 * Creates the downloading thread.
	 * 
	 * @param pContext Context to use.
	 * @param pType Thread type.
	 */
	public DownloadThread( Context pContext , int pType )
	{
		mContext = pContext;
		mLocalMemorySlots = new CopyOnWriteArrayList< MemorySlot >();
		mThreadType = pType;
	}
	
	/**
	 * Gets thread type.
	 */
	public int getThreadType()
	{
		return mThreadType;
	}
	
	/**
	 * Gets currently running task or null if there is none.
	 * 
	 * @return Current task or null.
	 */
	public DownloadTask getCurrentTask()
	{
		if ( mCurrentTask != null )
		{
			try
			{
				if ( mCurrentTask.isFinished() )
				{
					return null;
				}
			}
			catch( Exception e )
			{
				return null;
			}
		}
		
		return mCurrentTask;
	}
	
	@Override
	public void run()
	{
		while( mIsRunning )
		{
			if ( mTriggerPause )
			{
				mTriggerPause = false;
				synchronized( this )
				{
					try
					{
						this.wait();
					}
					catch( InterruptedException e )
					{
					}
				}
			}
			else
			{
				if ( !ManagerResource.isInitialized() )
				{
					return;
				}
				mCurrentTask = ManagerResource.getNextTask( mThreadType );
				
				if ( mCurrentTask != null )
				{
					if ( mCurrentTask.startDownloading() )
					{
						if ( ManagerResource.isFileAvailable( mCurrentTask.getFileName() ) )
						{
							// Someone has already downloaded this for us - we can finish now
							mCurrentTask.downloadSuccessful();
							ManagerResource.checkTasks( mCurrentTask.getListener() );
							continue;
						}
						if ( ( mCurrentTask.getListener() != null ) && ( mCurrentTask.getListener().isDownloadCancelled( mCurrentTask ) ) )
						{
							mCurrentTask.downloadCancelled();
							ManagerResource.checkTasks( mCurrentTask.getListener() );
							continue;
						}
						
						// Total number of downloaded bytes
						int lDownloadedDataSize = 0;
						
						// Downloading data
						try
						{
							URL lURL;
							try
							{
								if ( mCurrentTask.getURLCreator() != null )
								{
									lURL = new URL( mCurrentTask.getURLCreator().generateURLForFile( mCurrentTask.getFileName() ) );
								}
								else
								{
									lURL = new URL( mCurrentTask.getURL() );
								}
							}
							catch( Exception e )
							{
								// Malformed URL!
								Log.e( TAG , "Bad resource URL!!! URL: " + mCurrentTask.getURL() + " MD5: " + mCurrentTask.getFileName() );
								mCurrentTask.downloadFailed();
								ManagerResource.checkTasks( mCurrentTask.getListener() );
								deallocateLocalMemorySlots();
								continue;
							}
							
							InputStream lInputStream = null;
							lInputStream = lURL.openStream();
							
							if ( lInputStream == null )
							{
								Log.w( TAG , "Input stream is null..." );
							}
							
							// Number of downloaded bytes in previous try
							int lNumRead;
							
							// Free local memory slots if there are any
							deallocateLocalMemorySlots();
							
							// Get an empty memory slot
							MemorySlot lMemorySlot = MemoryManager.getMemorySlot();
							mLocalMemorySlots.add( lMemorySlot );
							
							// Downloading continues until there is nothing left
							do
							{
								// Check if app is still alive
								if ( !ManagerResource.isInitialized() )
								{
									return;
								}
								
								// Check if there is still free space left in current memory slot
								if ( lMemorySlot.getFreeAmount() == 0 )
								{
									// Request additional memory slot
									lMemorySlot = MemoryManager.getMemorySlot();
									mLocalMemorySlots.add( lMemorySlot );
								}
								
								lNumRead = lInputStream.read( lMemorySlot.getData() , lMemorySlot.getUsedAmount() , lMemorySlot.getFreeAmount() );
								if ( lNumRead != -1 )
								{
									ManagerResource.statsAddSize( lNumRead );
									lDownloadedDataSize += lNumRead;
									lMemorySlot.setUsedAmount( lMemorySlot.getUsedAmount() + lNumRead );
								}
								
								if ( lDownloadedDataSize > MAX_IMAGE_SIZE )
								{
									if ( mThreadType == THREAD_TYPE_LARGE_FILES )
									{
										saveNextMultiPartToInternalMemory();
										deallocateLocalMemorySlots();
										lMemorySlot = MemoryManager.getMemorySlot();
										mLocalMemorySlots.add( lMemorySlot );
										lDownloadedDataSize = 0;
									}
									else
									{
										mCurrentTask.setNumRetries( MAX_RETRIES );
										throw new IOException( "Image is too large to download by small files engine! Name: "
												+ mCurrentTask.getFileName() + " URL: " + mCurrentTask.getURL() );
									}
								}
								if ( ( mCurrentTask.getListener() != null ) && ( mCurrentTask.getListener().isDownloadCancelled( mCurrentTask ) ) )
								{
									break;
								}
							}
							while( lNumRead != -1 );
							if ( ( mCurrentTask.getListener() != null ) && ( mCurrentTask.getListener().isDownloadCancelled( mCurrentTask ) ) )
							{
								mCurrentTask.downloadCancelled();
								ManagerResource.checkTasks( mCurrentTask.getListener() );
								continue;
							}
							if ( mThreadType == THREAD_TYPE_LARGE_FILES )
							{
								saveNextMultiPartToInternalMemory();
								deallocateLocalMemorySlots();
							}
							else
							{
								ManagerResource.statsAddFile();
							}
						}
						catch( Exception e )
						{
							Log.e( TAG , "Exception while downloading." , e );
							if ( mThreadType == THREAD_TYPE_LARGE_FILES )
							{
								mContext.deleteFile( getTempFileName( mCurrentTask.getFileName() ) );
							}
							
							deallocateLocalMemorySlots();
							if ( mCurrentTask.getNumRetries() < MAX_RETRIES )
							{
								DownloadTask lTask = mCurrentTask;
								mCurrentTask = null;
								lTask.restartTask();
							}
							else
							{
								mCurrentTask.downloadFailed();
								ManagerResource.checkTasks( mCurrentTask.getListener() );
								mCurrentTask = null;
							}
							continue;
						}
						
						if ( !ManagerResource.isInitialized() )
						{
							return;
						}
						
						if ( ( mCurrentTask.getListener() != null ) && ( mCurrentTask.getListener().isDownloadCancelled( mCurrentTask ) ) )
						{
							mCurrentTask.downloadCancelled();
							ManagerResource.checkTasks( mCurrentTask.getListener() );
							continue;
						}
						
						if ( mThreadType == THREAD_TYPE_LARGE_FILES )
						{
							FileCopier lCopier = new FileCopier( mContext , ManagerResource.STORAGE_TYPE_INTERNAL ,
									getTempFileName( mCurrentTask.getFileName() ) , ManagerResource.STORAGE_TYPE_EXTERNAL ,
									mCurrentTask.getFileName() , true , false , mCurrentTask.getFileCrypter() , null );
							
							if ( lCopier.copy() )
							{
								mCurrentTask.downloadSuccessful();
							}
							else
							{
								mCurrentTask.downloadFailed();
								ManagerResource.notifyAboutOutOfMemory();
							}
							
							ManagerResource.checkTasks( mCurrentTask.getListener() );
							deallocateLocalMemorySlots();
							continue;
						}
						
						if ( !mLocalMemorySlots.isEmpty() )
						{
							// Checking if file was downloaded correctly
							if ( !mCurrentTask.getFileChecker().checkFile( mCurrentTask , mLocalMemorySlots ) )
							{
								if ( mCurrentTask.getNumRetries() < MAX_RETRIES )
								{
									DownloadTask lTask = mCurrentTask;
									mCurrentTask = null;
									lTask.restartTask();
									deallocateLocalMemorySlots();
									continue;
								}
								else
								{
									Log.v( TAG , "Failing incorrect file after " + MAX_RETRIES + " retries. File name " + mCurrentTask.getFileName() );
									mCurrentTask.downloadFailed();
									ManagerResource.checkTasks( mCurrentTask.getListener() );
									mCurrentTask = null;
									deallocateLocalMemorySlots();
									continue;
								}
							}
							else if ( ( ManagerResource.isInternalMemoryLow() && ManagerResource.isExternalStorageAvailable() )
									|| ( ManagerResource.isOutOfInternalMemory() ) )
							{
								// We have to save in internal memory
								if ( ManagerResource.isExternalStorageAvailable() == true )
								{
									if ( ManagerResource.isFileAvailable( mCurrentTask.getFileName() ) )
									{
										// Someone has already downloaded this for us - we can finish now
										mCurrentTask.downloadSuccessful();
										ManagerResource.checkTasks( mCurrentTask.getListener() );
										deallocateLocalMemorySlots();
										continue;
									}
									if ( ManagerResource.saveInExternalStorage( mCurrentTask.getFileName() , mLocalMemorySlots ) )
									{
										mCurrentTask.downloadSuccessful();
										// Add this new file to file list
										ManagerResource.addFileToFileList( mCurrentTask.getFileName() , ManagerResource.STORAGE_TYPE_EXTERNAL ,
												MemoryManager.calculateTotalSize( mLocalMemorySlots ) );
									}
									else
									{
										mCurrentTask.downloadFailed();
										Log.e( TAG , "Failed to save file on SD card!" );
									}
								}
								else
								{
									// We dont have external storage and we are low on memory
									// There is nothing else we can do - time to fail ;(
									mCurrentTask.downloadFailed();
									ManagerResource.notifyAboutOutOfMemory();
									Log.e( TAG , "Not enough memory and no SD card!" );
								}
							}
							else
							{
								// We have enough free internal memory - we can save file there
								try
								{
									if ( ManagerResource.isFileAvailable( mCurrentTask.getFileName() ) )
									{
										// Someone has already downloaded this for us - we can finish now
										mCurrentTask.downloadSuccessful();
										ManagerResource.checkTasks( mCurrentTask.getListener() );
										deallocateLocalMemorySlots();
										continue;
									}
									FileOutputStream lFile = mContext.openFileOutput( mCurrentTask.getFileName() , Context.MODE_PRIVATE );
									for ( int i = 0; i < mLocalMemorySlots.size(); i++ )
									{
										lFile.write( mLocalMemorySlots.get( i ).getData() , 0 , mLocalMemorySlots.get( i ).getUsedAmount() );
									}
									lFile.close();
									ManagerResource.addFileToFileList( mCurrentTask.getFileName() , ManagerResource.STORAGE_TYPE_INTERNAL ,
											MemoryManager.calculateTotalSize( mLocalMemorySlots ) );
									mCurrentTask.downloadSuccessful();
								}
								catch( Exception e )
								{
									Log.e( TAG , "Exception while saving in internal memory." , e );
									mCurrentTask.downloadFailed();
								}
							}
						}
						
						// Free used memory.
						deallocateLocalMemorySlots();
						ManagerResource.checkTasks( mCurrentTask.getListener() );
					}
				}
				else
				{
					mTriggerPause = true;
				}
			}
		}
	}
	
	/**
	 * Pauses this downloading thread.
	 */
	public void pause()
	{
		mTriggerPause = true;
	}
	
	/**
	 * Exits this downloading thread.
	 */
	public void exit()
	{
		mIsRunning = false;
		deallocateLocalMemorySlots();
	}
	
	/**
	 * Wakes this downloading thread.
	 */
	public void awake()
	{
		synchronized( this )
		{
			this.notify();
		}
	}
	
	private String getTempFileName( String pFileName )
	{
		return "temp_" + pFileName;
	}
	
	/**
	 * Saves current temp data.
	 */
	private void saveNextMultiPartToInternalMemory() throws IOException
	{
		if ( saveLargeFileDataToTempFileInInternalStorage( mLocalMemorySlots , mCurrentTask.getFileName() ) )
		{
			deallocateLocalMemorySlots();
		}
		else
		{
			mCurrentTask.setNumRetries( MAX_RETRIES );
			throw new IOException( "Failed to save temp file! Name: " + mCurrentTask.getFileName() + " URL: " + mCurrentTask.getURL() );
		}
	}
	
	/**
	 * Saves new data to temporary file.
	 * 
	 * @param pData Data to save.
	 * @param pFileName Final file name.
	 * @return True if everything was ok.
	 */
	private boolean saveLargeFileDataToTempFileInInternalStorage( List< MemorySlot > pData , String pFileName )
	{
		try
		{
			int lTotalSaved = 0;
			FileOutputStream lFile = mContext.openFileOutput( getTempFileName( pFileName ) , Context.MODE_APPEND );
			for ( int i = 0; i < pData.size(); i++ )
			{
				lFile.write( pData.get( i ).getData() , 0 , pData.get( i ).getUsedAmount() );
				lTotalSaved += pData.get( i ).getUsedAmount();
			}
			Log.v( TAG , "Saved " + lTotalSaved + " new bytes to temporary file " + getTempFileName( pFileName ) );
			lFile.close();
			return true;
		}
		catch( Exception e )
		{
			Log.e( TAG , "Error saving temp file for " + pFileName , e );
			return false;
		}
	}
}
