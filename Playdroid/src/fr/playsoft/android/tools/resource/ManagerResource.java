package fr.playsoft.android.tools.resource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import fr.playsoft.android.tools.debug.Log;
import fr.playsoft.android.tools.drawables.DrawableLoader;

/**
 * Download manager - controls the download queue. Downloads and caches files.
 * All methods are static. It has to be initialized with init( Context ) before using.
 * 
 * @author Olgierd Uzieblo
 */
public class ManagerResource
{
	/** Tag for LogCat **/
	private static final String TAG = "ResourceManager";
	
	/** Constant for small files **/
	public static final int TASK_TYPE_SMALL_FILE = DownloadThread.THREAD_TYPE_SMALL_FILES;
	
	/** Constant for large files **/
	public static final int TASK_TYPE_LARGE_FILE = DownloadThread.THREAD_TYPE_LARGE_FILES;
	
	/** Constant used for files stored in internal memory **/
	public static final int STORAGE_TYPE_INTERNAL = 0;
	
	/** Constant used for files stored in external memory **/
	public static final int STORAGE_TYPE_EXTERNAL = 1;
	
	/** Constant used for files stored in apk assets folder **/
	public static final int STORAGE_TYPE_EMBEDDED = 2;
	
	/**
	 * Minimum amount of free space that must be available to save file in internal storage (kB).
	 * When there is less free internal memory than DEFAULT_MEMORY_LIMIT_HIGH files will be saved on SD card automatically.
	 * If there are problems saving them on SD card (no card, no space) we continue saving in internal memory until we reach DEFAULT_MEMORY_LIMIT_LOW.
	 */
	private static final int DEFAULT_MEMORY_LIMIT_HIGH = 100000;
	
	/**
	 * If internal memory reaches DEFAULT_MEMORY_LIMIT_LOW and there is no SD card we are out of memory.
	 * IOutOfMemoryListener will be called to handle it.
	 */
	private static final int DEFAULT_MEMORY_LIMIT_LOW = 2000;
	
	/** Current high memory limit **/
	private static int sMemoryLimitHigh;
	
	/** Current low memory limit **/
	private static int sMemoryLimitLow;
	
	/** Priority queue with all small file tasks waiting to be downloaded **/
	private static PriorityBlockingQueue< DownloadTask > sTaskQueueSmallFiles;
	
	/** Priority queue with all large file tasks waiting to be downloaded **/
	private static PriorityBlockingQueue< DownloadTask > sTaskQueueLargeFiles;
	
	/** Full internal file path **/
	private static String sFullInternalFilePath;
	
	/** Full external file path **/
	private static String sFullExternalFilePath;
	
	/**
	 * Comparator used for getting tasks with highest priority.
	 */
	private static final Comparator< DownloadTask > PRIORITY_COMPARATOR = new Comparator< DownloadTask >()
	{
		@Override
		public int compare( DownloadTask object1 , DownloadTask object2 )
		{
			// The higher the priority the earlier the task will start
			return -object1.getPriority().compareTo( object2.getPriority() );
		}
	};
	
	/** Path used to store files on external storage **/
	private static String sPathExternalStorage;
	
	/** All downloading threads **/
	private static ArrayList< DownloadThread > sThreadList;
	
	/** Is ResourceManager already initialized? **/
	private static boolean sIsInitialized = false;
	
	/** Context to use **/
	private static Context sContext;
	
	/** Resources to use **/
	private static Resources sResources;
	
	/** Total number number of downloading threads **/
	private static final int MAX_THREADS_NUM = 9;
	
	/** Number of downloading threads for large files engine **/
	private static final int MAX_THREADS_NUM_LARGE_FILES = 1;
	
	/** Number of downloading threads for small files engine **/
	private static final int MAX_THREADS_NUM_SMALL_FILES = MAX_THREADS_NUM - MAX_THREADS_NUM_LARGE_FILES;
	
	/** Total size of downloaded files **/
	private static int sTotalDownloadSize;
	
	/** Total number of downloaded files **/
	private static int sTotalDownloadFiles;
	
	/** Total time spent on data downloading **/
	private static long sTotalDownloadTime;
	
	/** Play button bitmap used to generate video thumbnails **/
	private static Bitmap sPlayButtonBitmap = null;
	
	/** Minimum time between checks of memory left **/
	private static final int MEMORY_CHECK_INTERVAL = 5000;
	
	/** Last time of memory check **/
	private static long sLastMemoryCheckTime;
	
	/** Amount of free internal memory last time we checked **/
	private static float sLastFreeInternalMemory;
	
	/** Minimum time between checks of sd card availability **/
	private static final int SD_CHECK_INTERVAL = 20000;
	
	/** Last time of sd card check **/
	private static long sLastSDAvailableCheckTime;
	
	/** Last result of sd card check **/
	private static boolean sLastSDAvailableCheckResult;
	
	/** Flag to check if any of downloading thread is busy **/
	private static boolean sIsDownloadingInProgress;
	
	/** Time when current downloading started **/
	private static long sDownloadingStartTime;
	
	/** Time when current downloading ended **/
	private static long sDownloadingEndTime;
	
	/** Handler for posting download results **/
	private static Handler sHandler;
	
	/** Instance of ConnectivityManager **/
	private static ConnectivityManager sConnectivityManager;
	
	/** Constant for no connection **/
	public static final int CONNECTION_TYPE_OFFLINE = 0;
	
	/** Constant for gsm connection type **/
	public static final int CONNECTION_TYPE_GSM = 1;
	
	/** Constant for wifi connection type **/
	public static final int CONNECTION_TYPE_WIFI = 2;
	
	/** Contains device language name **/
	private static String sDeviceLanguageName;
	
	/**
	 * Empty resource image that can be used if image is missing.
	 * It is always kept cached in memory to improve performance.
	 */
	private static Drawable sEmptyResourceDrawable = null;
	
	/** List of all files existing in internal and external memory **/
	private static ConcurrentHashMap< String , FileDescriptor > sFileList;
	
	/** Initial size of file list hashmap **/
	private static final int FILE_LIST_INITIAL_SIZE = 100;
	
	/** Initial load factor of file list hashmap **/
	private static final float FILE_LIST_LOAD_FACTOR = 0.75f;
	
	/** Priority that is currently the largest one **/
	private static int sCurrentMaxPriority = 0;
	
	/** Priority that is currently the smallest one **/
	private static int sCurrentMinPriority = 0;
	
	/** Listener to notify if we are out of memory **/
	private static IOutOfMemoryListener sOutOfMemoryListener;
	
	/** Default file checker that always returns true - used if there is no custom checker **/
	private static final IFileChecker DEFAULT_FILE_CHECKER = new IFileChecker()
	{
		@Override
		public boolean checkFile( DownloadTask pTask , List< MemorySlot > pMemorySlots )
		{
			return true;
		}
	};
	
	/**
	 * Initializes ResourceManager with default memory limits.
	 * 
	 * @param pContext Context to use.
	 */
	public static void init( Context pContext )
	{
		init( pContext , DEFAULT_MEMORY_LIMIT_HIGH , DEFAULT_MEMORY_LIMIT_LOW );
	}
	
	/**
	 * Initializes ResourceManager.
	 * 
	 * @param pContext Context to use.
	 * @param pMemoryLimitHigh High memory limit to use (kB).
	 * @param pMemoryLimitLow Low memory limit to use (kB).
	 */
	public static void init( Context pContext , int pMemoryLimitHigh , int pMemoryLimitLow )
	{
		if ( sIsInitialized )
		{
			return;
		}
		DrawableLoader.init();
		sMemoryLimitHigh = pMemoryLimitHigh;
		sMemoryLimitLow = pMemoryLimitLow;
		sHandler = new Handler();
		sContext = pContext;
		sResources = sContext.getResources();
		sTaskQueueSmallFiles = new PriorityBlockingQueue< DownloadTask >( 1 , PRIORITY_COMPARATOR );
		sTaskQueueLargeFiles = new PriorityBlockingQueue< DownloadTask >( 1 , PRIORITY_COMPARATOR );
		sThreadList = new ArrayList< DownloadThread >( MAX_THREADS_NUM );
		sPathExternalStorage = "/Android/data/" + sContext.getPackageName() + "/cache/";
		sFullInternalFilePath = "file://" + sContext.getFilesDir() + "/";
		sFullExternalFilePath = "file://" + Environment.getExternalStorageDirectory() + sPathExternalStorage;
		sConnectivityManager = (ConnectivityManager) sContext.getSystemService( Context.CONNECTIVITY_SERVICE );
		sDeviceLanguageName = sResources.getConfiguration().locale.getLanguage();
		
		for ( int i = 0; i < MAX_THREADS_NUM; i++ )
		{
			DownloadThread lDownloadThread = new DownloadThread( sContext , i < MAX_THREADS_NUM_SMALL_FILES ? DownloadThread.THREAD_TYPE_SMALL_FILES
					: DownloadThread.THREAD_TYPE_LARGE_FILES );
			sThreadList.add( lDownloadThread );
			Thread lThread = new Thread( lDownloadThread );
			lThread.setPriority( Thread.MIN_PRIORITY );
			lThread.setName( "DownloadThread " + Integer.toString( i ) + "type " + sThreadList.get( i ).getThreadType() );
			lThread.start();
		}
		
		// Prepare file names cache
		sFileList = new ConcurrentHashMap< String , FileDescriptor >( FILE_LIST_INITIAL_SIZE , FILE_LIST_LOAD_FACTOR , MAX_THREADS_NUM_SMALL_FILES );
		initFileList();
		
		// Set initialized flag
		sIsInitialized = true;
	}
	
	/**
	 * Gets path of our external folder.
	 */
	public static String getExternalStoragePath()
	{
		return sPathExternalStorage;
	}
	
	/**
	 * Gets device language name. Check is performed only once at init!
	 * 
	 * @return Language code for current language.
	 */
	public static String getDeviceLanguage()
	{
		return sDeviceLanguageName;
	}
	
	/**
	 * Finishes resource manager - resets it to initial state and stops downloading.
	 */
	public static void finish()
	{
		sIsInitialized = false;
		sFileList.clear();
		sTaskQueueSmallFiles.clear();
		sTaskQueueLargeFiles.clear();
		clearTotalDownloadStats();
		sIsDownloadingInProgress = false;
		for ( DownloadThread lThread : sThreadList )
		{
			lThread.exit();
			lThread.awake();
		}
		sThreadList.clear();
		MemoryManager.removeDeallocatedMemorySlots();
	}
	
	/**
	 * Clears download queue from all tasks.
	 */
	public static void clearDownloadQueue()
	{
		sTaskQueueSmallFiles.clear();
		sTaskQueueLargeFiles.clear();
	}
	
	/**
	 * Checks if RM is initialized.
	 * 
	 * @return True if it is.
	 */
	public static boolean isInitialized()
	{
		return sIsInitialized;
	}
	
	/**
	 * Sets out of memory listener.
	 * 
	 * @param pListener Listener to notify when there is not enough flash memory to save file.
	 */
	public static void setOutOfMemoryListener( IOutOfMemoryListener pListener )
	{
		sOutOfMemoryListener = pListener;
	}
	
	/**
	 * Gets current out of memory listener.
	 * 
	 * @return Current listener (may be null).
	 */
	public static IOutOfMemoryListener getOutOfMemoryListener()
	{
		return sOutOfMemoryListener;
	}
	
	/**
	 * Notifies listener about out of memory.
	 */
	public static void notifyAboutOutOfMemory()
	{
		if ( sOutOfMemoryListener != null )
		{
			sHandler.post( new Runnable()
			{
				@Override
				public void run()
				{
					sOutOfMemoryListener.onOufOfMemory();
				}
			} );
		}
	}
	
	/**
	 * Sets empty resource drawable to be used if image is missing.
	 * 
	 * @param pEmptyDrawable Drawable to use.
	 */
	public static void setEmptyResourceDrawable( Drawable pEmptyDrawable )
	{
		sEmptyResourceDrawable = pEmptyDrawable;
	}
	
	/**
	 * Sets play button drawable. Will be used for thumbnails containing play button on them.
	 * 
	 * @param pPlayDrawableId Resource id of this Drawable.
	 */
	public static void setPlayButtonDrawable( int pPlayDrawableId )
	{
		// Preloading of play button bitmap - it is small and we keep it in memory all the time for better performance
		Bitmap lTempBitmap = BitmapFactory.decodeResource( sResources , pPlayDrawableId );
		
		// Prepare color matrix that adds transparency to the pixels
		float[] lColorMatrixValues =
		{
				1 , 0 , 0 , 0 , 0 , // R'
				0 , 1 , 0 , 0 , 0 , // G'
				0 , 0 , 1 , 0 , 0 , // B'
				0 , 0 , 0 , 0.7f , 0
		// A'
		};
		
		ColorMatrix lColorMatrix = new ColorMatrix( lColorMatrixValues );
		ColorMatrixColorFilter lFilter = new ColorMatrixColorFilter( lColorMatrix );
		Paint lPaint = new Paint();
		lPaint.setColorFilter( lFilter );
		
		// Create empty bitmap with the same size
		sPlayButtonBitmap = Bitmap.createBitmap( lTempBitmap.getWidth() , lTempBitmap.getHeight() , Bitmap.Config.ARGB_8888 );
		
		// Get canvas for it
		Canvas lCanvas = new Canvas( sPlayButtonBitmap );
		
		// Draw original bitmap with changed transparency
		lCanvas.drawBitmap( lTempBitmap , new Matrix() , lPaint );
		
		// Cleanup
		lTempBitmap = null;
		lCanvas = null;
	}
	
	/**
	 * Gets Handler for posting messages on UI thread.
	 * 
	 * @return Handler instance.
	 */
	public static Handler getHandler()
	{
		return sHandler;
	}
	
	/**
	 * Gets dimension from resources.
	 * 
	 * @param pDimenId Dimension id from R.dimen.
	 * @return This dimension.
	 */
	public static float getDimension( int pDimenId )
	{
		return sResources.getDimension( pDimenId );
	}
	
	/**
	 * Gets Resources.
	 * 
	 * @return Resources instance.
	 */
	public static Resources getResources()
	{
		return sResources;
	}
	
	/**
	 * Checks if there is available Internet connection.
	 * 
	 * @return True if there is a connection, false otherwise.
	 */
	public static boolean isConnectionAvailable()
	{
		NetworkInfo lNetInfo = sConnectivityManager.getActiveNetworkInfo();
		if ( lNetInfo != null )
		{
			if ( lNetInfo.isConnected() )
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets network connection type.
	 * 
	 * @return Connection type - CONNECTION_TYPE_OFFLINE / CONNECTION_TYPE_GSM / CONNECTION_TYPE_WIFI.
	 */
	public static int getConnectionType()
	{
		NetworkInfo lNetInfo = sConnectivityManager.getActiveNetworkInfo();
		if ( lNetInfo != null )
		{
			if ( lNetInfo.isConnected() )
			{
				if ( lNetInfo.getType() == ConnectivityManager.TYPE_WIFI )
				{
					return CONNECTION_TYPE_WIFI;
				}
				else
				{
					return CONNECTION_TYPE_GSM;
				}
			}
		}
		return CONNECTION_TYPE_OFFLINE;
	}
	
	/**
	 * Gets amount of free internal memory.
	 * 
	 * @return Free internal memory in kB.
	 */
	private static synchronized float getFreeInternalMemory()
	{
		long lCurrentTime = System.currentTimeMillis();
		if ( lCurrentTime - sLastMemoryCheckTime < MEMORY_CHECK_INTERVAL )
		{
			// Remaining free space in Kbyte
			return sLastFreeInternalMemory / 1024;
		}
		sLastMemoryCheckTime = lCurrentTime;
		StatFs lStats = new StatFs( "/data" );
		
		sLastFreeInternalMemory = (float) lStats.getAvailableBlocks() * (float) lStats.getBlockSize();
		
		Log.v( TAG , "Remaining internal storage = " + ( sLastFreeInternalMemory / 1024 ) );
		return sLastFreeInternalMemory;
	}
	
	/**
	 * Gets amount of free internal memory in bytes.
	 * 
	 * @return Free internal memory.
	 */
	public static synchronized float getFreeInternalMemoryBytes()
	{
		return sLastFreeInternalMemory;
	}
	
	/**
	 * Checks if we have low internal memory - data should be saved on sd card.
	 * 
	 * @return True if we have low memory - there is less internal memory than sMemoryLimitHigh.
	 */
	public static synchronized boolean isInternalMemoryLow()
	{
		if ( getFreeInternalMemory() < sMemoryLimitHigh || getFreeInternalMemory() < 0 )
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if we are out of internal memory - data MUST be saved on sd card only.
	 * 
	 * @return True if we are out of internal memory - there is less internal memory than sMemoryLimitLow.
	 */
	public static synchronized boolean isOutOfInternalMemory()
	{
		if ( getFreeInternalMemory() < sMemoryLimitLow || getFreeInternalMemory() < 0 )
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Initializes file list - adds all existing files to it.
	 */
	private static void initFileList()
	{
		long lTime1 = System.currentTimeMillis();
		
		// Get list of internal files
		String[] lInternalFileList = sContext.fileList();
		
		// Get list of external files
		File lExtStorageDir = new File( Environment.getExternalStorageDirectory() , sPathExternalStorage );
		String[] lExternalFileList = lExtStorageDir.list();
		if ( lExternalFileList == null )
		{
			lExternalFileList = new String[ 0 ];
		}
		
		// Get list of embedded files
		String[] lEmbeddedFileList = new String[ 0 ];
		try
		{
			lEmbeddedFileList = sContext.getAssets().list( "" );
		}
		catch( IOException e )
		{
			Log.e( TAG , "Error loading embedded files" , e );
		}
		
		// Prepare temporary HashMap with all files
		HashMap< String , FileDescriptor > lFilesToAdd = new HashMap< String , FileDescriptor >( lInternalFileList.length + lExternalFileList.length
				+ lEmbeddedFileList.length );
		
		// Add internal files
		for ( String lInternalFileName : lInternalFileList )
		{
			File lFile = new File( sContext.getFilesDir() , lInternalFileName );
			long lSize = lFile.length();
			lFilesToAdd.put( lInternalFileName , new FileDescriptor( STORAGE_TYPE_INTERNAL , lSize ) );
		}
		
		// Add external files
		for ( String lExternalFileName : lExternalFileList )
		{
			File lFile = new File( Environment.getExternalStorageDirectory() , sPathExternalStorage + lExternalFileName );
			long lSize = lFile.length();
			lFilesToAdd.put( lExternalFileName , new FileDescriptor( STORAGE_TYPE_EXTERNAL , lSize ) );
		}
		
		// Add embedded files
		for ( String lEmbeddedFileName : lEmbeddedFileList )
		{
			long lSize = 0;
			try
			{
				lSize = sContext.getAssets().openFd( lEmbeddedFileName ).getLength();
			}
			catch( IOException e )
			{
			}
			lFilesToAdd.put( lEmbeddedFileName , new FileDescriptor( STORAGE_TYPE_EMBEDDED , lSize ) );
		}
		
		// Add everything at once for better performance
		sFileList.putAll( lFilesToAdd );
		
		long lTime2 = System.currentTimeMillis();
		
		Log.v( TAG , "Initialized file list: " + lInternalFileList.length + " internal, " + lExternalFileList.length + " external and "
				+ lEmbeddedFileList.length + " embedded files. Took: " + ( lTime2 - lTime1 ) + " millis." );
	}
	
	/**
	 * Checks if this file exists and can be opened.
	 * File can not be opened if it is stored on SD card that is not accessible!
	 * 
	 * @param pName File name.
	 * @return True if file is available.
	 */
	public static boolean isFileAvailable( String pName )
	{
		// Protection for nullpointers
		if ( pName == null )
		{
			// This should never happen but we will handle it anyway
			return false;
		}
		FileDescriptor lFile = sFileList.get( pName );
		if ( lFile == null )
		{
			// We surely dont have this file anywhere
			return false;
		}
		switch ( lFile.getStorageType() )
		{
			case STORAGE_TYPE_INTERNAL:
			case STORAGE_TYPE_EMBEDDED:
				// We have this file internally or in assets
				return true;
				
			case STORAGE_TYPE_EXTERNAL:
				// We have it in external memory, it may be unavailable...
				if ( isExternalStorageAvailable() )
				{
					// It should be accessible
					return true;
				}
				break;
		}
		
		// External memory unavailable, this file is lost to us :(
		return false;
	}
	
	/**
	 * Gets full file path to chosen file.
	 * This path will be different depending on file location - it can be in internal memory or on sd card.
	 * 
	 * @param pFileName File name.
	 * @return Full path leading to this file or null if this file is not available!
	 */
	public static String getFullFilePath( String pFileName )
	{
		FileDescriptor lFile = sFileList.get( pFileName );
		if ( lFile == null )
		{
			// We surely dont have this file anywhere
			return null;
		}
		
		switch ( lFile.getStorageType() )
		{
			case STORAGE_TYPE_INTERNAL:
				return sFullInternalFilePath + pFileName;
				
			case STORAGE_TYPE_EXTERNAL:
				// We have it in external memory, it may be unavailable...
				if ( isExternalStorageAvailable() )
				{
					// It should be accessible
					return sFullExternalFilePath + pFileName;
				}
				break;
			
			case STORAGE_TYPE_EMBEDDED:
				return "file:///android_asset/" + pFileName;
		}
		return null;
	}
	
	/**
	 * Removes file from file list.
	 * 
	 * @param pFileName File name of file to remove.
	 */
	public static synchronized void removeFileFromFileList( String pFileName )
	{
		if ( !ManagerResource.isInitialized() )
		{
			return;
		}
		if ( pFileName != null )
		{
			sFileList.remove( pFileName );
		}
	}
	
	/**
	 * Adds new file to the file list.
	 * 
	 * @param pFileName File name.
	 * @param pStorageType Type of storage.
	 * @param pFileSize Size of this file.
	 */
	public static synchronized void addFileToFileList( String pFileName , int pStorageType , long pFileSize )
	{
		if ( !ManagerResource.isInitialized() )
		{
			return;
		}
		sFileList.put( pFileName , new FileDescriptor( pStorageType , pFileSize ) );
	}
	
	/**
	 * Gets missing resource image as Drawable.
	 * Returns null if it was not initialized.
	 * 
	 * @return Drawable that should be used if image is missing.
	 */
	public static Drawable getEmptyDrawable()
	{
		return sEmptyResourceDrawable;
	}
	
	/**
	 * Saves data in external storage in "cache" folder.
	 * 
	 * @param pFileName File name to use.
	 * @param pMemorySlots ArrayList with MemorySlots containing downloaded data.
	 * @return True if data was saved correctly.
	 */
	public static synchronized boolean saveInExternalStorage( String pFileName , List< MemorySlot > pMemorySlots )
	{
		File lDirectory = new File( Environment.getExternalStorageDirectory() , sPathExternalStorage );
		try
		{
			lDirectory.mkdirs();
			File lOutputFile = new File( lDirectory , pFileName );
			FileOutputStream lFileStream = new FileOutputStream( lOutputFile );
			for ( int i = 0; i < pMemorySlots.size(); i++ )
			{
				lFileStream.write( pMemorySlots.get( i ).getData() , 0 , pMemorySlots.get( i ).getUsedAmount() );
			}
			lFileStream.close();
			return true;
		}
		catch( IOException e )
		{
			Log.e( TAG , "Error saving in external storage. " , e );
			return false;
		}
	}
	
	/**
	 * Saves data as new internal file if possible.
	 * 
	 * @param pData Data to save.
	 * @param pFileName File name.
	 */
	public static synchronized void saveDataAsNewInternalFile( byte[] pData , String pFileName )
	{
		try
		{
			FileOutputStream lFile = sContext.openFileOutput( pFileName , Context.MODE_PRIVATE );
			lFile.write( pData , 0 , pData.length );
			lFile.close();
			ManagerResource.addFileToFileList( pFileName , STORAGE_TYPE_INTERNAL , pData.length );
		}
		catch( Exception e )
		{
			Log.e( TAG , "Error saving in internal storage. " , e );
		}
	}
	
	/**
	 * Check if the external storage is available to read and write
	 * 
	 * @return true if the external storage is available, false otherwise
	 */
	public static synchronized boolean isExternalStorageAvailable()
	{
		long lTime = System.currentTimeMillis();
		if ( lTime - sLastSDAvailableCheckTime < SD_CHECK_INTERVAL )
		{
			return sLastSDAvailableCheckResult;
		}
		
		sLastSDAvailableCheckTime = lTime;
		sLastSDAvailableCheckResult = false;
		
		String state = Environment.getExternalStorageState();
		
		if ( Environment.MEDIA_MOUNTED.equals( state ) )
		{
			// We can read and write the media
			sLastSDAvailableCheckResult = true;
		}
		
		return sLastSDAvailableCheckResult;
	}
	
	/**
	 * Deletes a file.
	 * 
	 * @param pFileName File name without path.
	 */
	public static synchronized void deleteFile( String pFileName )
	{
		FileDescriptor lFile = sFileList.get( pFileName );
		if ( lFile != null )
		{
			switch ( lFile.getStorageType() )
			{
				case STORAGE_TYPE_INTERNAL:
					sContext.deleteFile( pFileName );
					removeFileFromFileList( pFileName );
					break;
				
				case STORAGE_TYPE_EXTERNAL:
					File lDirectory = new File( Environment.getExternalStorageDirectory() , sPathExternalStorage );
					File lFileToDelete = new File( lDirectory , pFileName );
					lFileToDelete.delete();
					removeFileFromFileList( pFileName );
					break;
				
				case STORAGE_TYPE_EMBEDDED:
					// Cant delete embedded file!
					break;
			}
		}
	}
	
	/**
	 * Deletes many files.
	 * 
	 * @param pFilesToDelete List of files to delete - as ResourceDescriptors.
	 */
	public static synchronized void deleteFilesWithResourceDescriptors( List< ResourceDescriptor > pFilesToDelete )
	{
		for ( ResourceDescriptor lFile : pFilesToDelete )
		{
			deleteFile( lFile.getMD5() );
		}
	}
	
	/**
	 * Deletes many files.
	 * 
	 * @param pFilesToDelete List of files to delete - as file names.
	 */
	public static synchronized void deleteFilesWithFileNames( List< String > pFilesToDelete )
	{
		for ( String lFileName : pFilesToDelete )
		{
			deleteFile( lFileName );
		}
	}
	
	/**
	 * Deletes all files.
	 */
	public static synchronized void deleteAllFiles()
	{
		Set< String > lFileList = sFileList.keySet();
		for ( String lFileName : lFileList )
		{
			deleteFile( lFileName );
		}
	}
	
	/**
	 * Gets list of file names that contain wanted String.
	 * 
	 * @param pNamePart String to look for in all file names.
	 * @return List of file names that contain this string.
	 */
	public static ArrayList< String > getFileNamesContaining( String pNamePart )
	{
		ArrayList< String > lResult = new ArrayList< String >();
		Set< String > lAllFileNames = sFileList.keySet();
		for ( String lFileName : lAllFileNames )
		{
			if ( lFileName.contains( pNamePart ) )
			{
				lResult.add( lFileName );
			}
		}
		return lResult;
	}
	
	/**
	 * Gets list of all stored files.
	 * 
	 * @return List of all file names.
	 */
	public static ArrayList< String > getAllFileNames()
	{
		ArrayList< String > lResult = new ArrayList< String >();
		Set< String > lAllFileNames = sFileList.keySet();
		for ( String lFileName : lAllFileNames )
		{
			lResult.add( lFileName );
		}
		return lResult;
	}
	
	/**
	 * Gets size of single file.
	 * 
	 * @param pFileName File name.
	 * @return Size of this file in bytes.
	 */
	public static long getFileSize( String pFileName )
	{
		FileDescriptor lFileDescriptor = sFileList.get( pFileName );
		if ( lFileDescriptor == null )
		{
			return 0;
		}
		else
		{
			return lFileDescriptor.getSize();
		}
	}
	
	/**
	 * Gets total size of wanted files.
	 * 
	 * @param pFileNames List of file names.
	 * @return Sum of size of all those files.
	 */
	public static long getTotalFileSize( ArrayList< String > pFileNames )
	{
		int lResult = 0;
		for ( String lName : pFileNames )
		{
			lResult += getFileSize( lName );
		}
		return lResult;
	}
	
	/**
	 * Gets total file size of all downloaded and embedded files.
	 * 
	 * @return Total size in bytes.
	 */
	public static long getTotalFileSize()
	{
		return getTotalFileSize( getAllFileNames() );
	}
	
	/**
	 * Gets next waiting task. High priority tasks are returned first. Other tasks are returned later.
	 * 
	 * @param Type of task to get - large or small file.
	 * 
	 * @return Next DownloadTask or null if there are no tasks left.
	 */
	public static synchronized DownloadTask getNextTask( int pTaskType )
	{
		switch ( pTaskType )
		{
			case TASK_TYPE_SMALL_FILE:
				return sTaskQueueSmallFiles.poll();
			case TASK_TYPE_LARGE_FILE:
				return sTaskQueueLargeFiles.poll();
		}
		return null;
	}
	
	/**
	 * Checks if there is already the same task in download queue.
	 * 
	 * @param pTask DownloadTask to be checked.
	 * @return True if there is already a task for the same resource and listener.
	 */
	public static synchronized boolean isTaskAlreadyExisting( DownloadTask pTask )
	{
		boolean lResult = ( sTaskQueueSmallFiles.contains( pTask ) ) || ( sTaskQueueLargeFiles.contains( pTask ) );
		if ( lResult )
		{
			return true;
		}
		else
		{
			for ( DownloadThread lThread : sThreadList )
			{
				DownloadTask lCurrentTask = lThread.getCurrentTask();
				if ( lCurrentTask != null )
				{
					if ( lCurrentTask.equals( pTask ) )
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if task queue contains task with chosen file name.
	 * 
	 * @param pResource ResourceDescriptor of file to check for.
	 * @return True if there is a task for this file already.
	 */
	public static synchronized boolean isFileInTaskQueue( ResourceDescriptor pResource )
	{
		DownloadTask lTempTask = new DownloadTask( pResource , null , 0 , null , TASK_TYPE_SMALL_FILE , null , null );
		return isTaskAlreadyExisting( lTempTask );
	}
	
	/**
	 * Checks if download is possible.
	 * It is impossible if there is less free space than sMemoryLow and we dont have SD card.
	 * 
	 * @return True if download is impossible.
	 */
	private static boolean isDownloadPossible()
	{
		if ( isOutOfInternalMemory() )
		{
			if ( !isExternalStorageAvailable() )
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Creates and adds multiple tasks to the downloading queue.
	 * 
	 * @param pResourceDescriptors Resource descriptors array.
	 * @param pPriority Priority that will be set for all those tasks.
	 * @param pListener IDownloadFinishedListener for those
	 * @return True if at least one task was added, false it nothing was added.
	 */
	public static synchronized boolean addTasks( ResourceDescriptor[] pResourceDescriptors , int pPriority , IDownloadFinishedListener pListener )
	{
		return addTasks( pResourceDescriptors , pPriority , pListener , DEFAULT_FILE_CHECKER , TASK_TYPE_SMALL_FILE , null , null );
	}
	
	/**
	 * Creates and adds multiple tasks to the downloading queue.
	 * 
	 * @param pResourceDescriptors Resource descriptors array.
	 * @param pPriority Priority that will be set for all those tasks.
	 * @param pListener IDownloadFinishedListener for those
	 * @param pTastTypeFile File type eg. TASK_TYPE_SMALL_FILE
	 * @return True if at least one task was added, false it nothing was added.
	 */
	public static synchronized boolean addTasks( ResourceDescriptor[] pResourceDescriptors , int pPriority , IDownloadFinishedListener pListener ,
			int pTaskTypeFile )
	{
		return addTasks( pResourceDescriptors , pPriority , pListener , DEFAULT_FILE_CHECKER , pTaskTypeFile , null , null );
	}
	
	/**
	 * Adds task to correct queue.
	 * 
	 * @param pTask Task to add to large or small files queue.
	 */
	private static synchronized void addTaskToCorrectQueue( DownloadTask pTask )
	{
		switch ( pTask.getTaskType() )
		{
			case TASK_TYPE_SMALL_FILE:
				sTaskQueueSmallFiles.add( pTask );
				break;
			case TASK_TYPE_LARGE_FILE:
				sTaskQueueLargeFiles.add( pTask );
				break;
		}
	}
	
	/**
	 * Creates and adds multiple tasks to the downloading queue.
	 * 
	 * @param pResourceDescriptors Resource descriptors array.
	 * @param pPriority Priority that will be set for all those tasks.
	 * @param pListener IDownloadFinishedListener for those tasks.
	 * @param pChecker Custom file checker for those tasks.
	 * @param pTaskType Type of task - small or large file.
	 * @param pFileCrypter Optional file crypter.
	 * @param pURLCreator Optional url creator.
	 * @return True if at least one task was added, false it nothing was added.
	 */
	public static synchronized boolean addTasks( ResourceDescriptor[] pResourceDescriptors , int pPriority , IDownloadFinishedListener pListener ,
			IFileChecker pChecker , int pTaskType , IFileCrypter pFileCrypter , IURLCreator pURLCreator )
	{
		if ( !isDownloadPossible() )
		{
			Log.v( TAG , "Skipped downloading - we dont have enough free space to save new files." );
			return false;
		}
		
		int lNumAdded = 0;
		
		for ( int i = 0; i < pResourceDescriptors.length; i++ )
		{
			if ( !isFileAvailable( pResourceDescriptors[ i ].getMD5() ) )
			{
				DownloadTask lNewTask = new DownloadTask( pResourceDescriptors[ i ] , pListener , pPriority , pChecker , pTaskType , pFileCrypter ,
						pURLCreator );
				if ( !isTaskAlreadyExisting( lNewTask ) )
				{
					sCurrentMaxPriority = Math.max( sCurrentMaxPriority , pPriority );
					sCurrentMinPriority = Math.min( sCurrentMinPriority , pPriority );
					addTaskToCorrectQueue( lNewTask );
					lNumAdded++;
				}
			}
		}
		
		if ( lNumAdded > 0 )
		{
			Log.i( TAG , "Starting to download " + lNumAdded + " new tasks." );
			startDownloading();
			return true;
		}
		else
		{
			Log.v( TAG , "Nothing new to add, all files are already cached or are present in download queue." );
			return false;
		}
	}
	
	/**
	 * Adds again an already created task.
	 * 
	 * @param pTask DownloadTask instance.
	 * 
	 * @return True if it was added correctly.
	 */
	public static synchronized boolean addTask( DownloadTask pTask )
	{
		if ( !isTaskAlreadyExisting( pTask ) )
		{
			addTaskToCorrectQueue( pTask );
			Log.i( TAG , "Starting to download one new task." );
			startDownloading();
			return true;
		}
		return false;
	}
	
	/**
	 * Creates and adds one tasks to the downloading queue.
	 * 
	 * @param pResourceDescriptor Resource descriptor.
	 * @param pPriority Priority of this task.
	 * @param pListener DownloadFinishedListener for this task.
	 * @param pIsLowPriority True sets task to low priority.
	 * @return True if at least one task was added, false it nothing was added.
	 */
	public static synchronized boolean addTask( ResourceDescriptor pResourceDescriptor , int pPriority , IDownloadFinishedListener pListener )
	{
		return addTask( pResourceDescriptor , pPriority , pListener , DEFAULT_FILE_CHECKER , TASK_TYPE_SMALL_FILE , null , null , null );
	}
	
	/**
	 * Creates and adds one tasks to the downloading queue.
	 * 
	 * @param pResourceDescriptor Resource descriptor.
	 * @param pPriority Priority of this task.
	 * @param pListener DownloadFinishedListener for this task.
	 * @param pIsLowPriority True sets task to low priority.
	 * @param pChecker Custom file checked for those tasks.
	 * @param pTaskType Task type - small or large file.
	 * @param pFileCrypter Optional file crypter.
	 * @param pURLCreator Optional URL creator.
	 * @param pTag Optional tag object.
	 * @return True if at least one task was added, false it nothing was added.
	 */
	public static synchronized boolean addTask( ResourceDescriptor pResourceDescriptor , int pPriority , IDownloadFinishedListener pListener ,
			IFileChecker pChecker , int pTaskType , IFileCrypter pFileCrypter , IURLCreator pURLCreator , Object pTag )
	{
		if ( !isDownloadPossible() )
		{
			Log.v( TAG , "Skipped downloading - we dont have enough free space to save new files." );
			return false;
		}
		if ( !isFileAvailable( pResourceDescriptor.getMD5() ) )
		{
			DownloadTask lNewTask = new DownloadTask( pResourceDescriptor , pListener , pPriority , pChecker , pTaskType , pFileCrypter , pURLCreator );
			lNewTask.setTag( pTag );
			if ( !isTaskAlreadyExisting( lNewTask ) )
			{
				sCurrentMaxPriority = Math.max( sCurrentMaxPriority , pPriority );
				sCurrentMinPriority = Math.min( sCurrentMinPriority , pPriority );
				addTaskToCorrectQueue( lNewTask );
				Log.i( TAG , "Starting to download one new task." );
				startDownloading();
				return true;
			}
			else
			{
				Log.i( TAG , "The same task is already in download queue. Nothing was added." );
				return false;
			}
		}
		
		Log.v( TAG , "Nothing new to add, file is already cached." );
		return false;
	}
	
	/**
	 * Gets file content as byte array.
	 * 
	 * @param pFileName Filename of an existing file.
	 * @return File content or null if file does not exits.
	 */
	public static synchronized byte[] getFileContentAsByteArray( String pFileName )
	{
		InputStream lFileStream = getFileContentAsStream( pFileName );
		if ( lFileStream == null )
		{
			return null;
		}
		try
		{
			int lFileSize = lFileStream.available();
			byte[] lFileContent = new byte[ lFileSize ];
			lFileStream.read( lFileContent );
			lFileStream.close();
			return lFileContent;
		}
		catch( Exception e )
		{
			// Something is wrong with this file, this error should never happen!
			Log.e( TAG , "Exception in getFileContentAsByteArray." , e );
			return null;
		}
	}
	
	/**
	 * Gets file content as JSONObject if possible.
	 * 
	 * @param pFileName Filename of an existing file.
	 * @return JSONObject or null in case of any error.
	 */
	public static synchronized JSONObject getFileAsJSONObject( String pFileName )
	{
		byte[] lFileContent = getFileContentAsByteArray( pFileName );
		if ( lFileContent == null )
		{
			return null;
		}
		try
		{
			String lFileAsString = new String( lFileContent );
			JSONObject lResult = new JSONObject( lFileAsString );
			// If we are here it means everything is ok!
			return lResult;
		}
		catch( Exception e )
		{
			// Probably JSON is damaged
			Log.e( TAG , "Error while creating JSONObject." , e );
			return null;
		}
	}
	
	/**
	 * Gets file content as JSONArray if possible.
	 * 
	 * @param pFileName Filename of an existing file.
	 * @return JSONArray or null in case of any error.
	 */
	public static synchronized JSONArray getFileAsJSONArray( String pFileName )
	{
		byte[] lFileContent = getFileContentAsByteArray( pFileName );
		if ( lFileContent == null )
		{
			return null;
		}
		try
		{
			String lFileAsString = new String( lFileContent );
			JSONArray lResult = new JSONArray( lFileAsString );
			// If we are here it means everything is ok!
			return lResult;
		}
		catch( Exception e )
		{
			// Probably JSON is damaged
			Log.e( TAG , "Error while creating JSONArray." , e );
			return null;
		}
	}
	
	/**
	 * Gets file content as FileInputStream.
	 * 
	 * @param pFileName Filename of an existing file (without path).
	 * @return File content as stream or null if file does not exists.
	 */
	public static synchronized InputStream getFileContentAsStream( String pFileName )
	{
		FileDescriptor lFileDescriptor = sFileList.get( pFileName );
		if ( lFileDescriptor == null )
		{
			return null;
		}
		
		switch ( lFileDescriptor.getStorageType() )
		{
			case STORAGE_TYPE_EMBEDDED:
				try
				{
					return sContext.getAssets().open( pFileName );
				}
				catch( IOException e )
				{
					Log.e( TAG , "Error opening embedded file" , e );
				}
				return null;
				
			case STORAGE_TYPE_INTERNAL:
				try
				{
					FileInputStream lFileStream = sContext.openFileInput( pFileName );
					return lFileStream;
				}
				catch( Exception e )
				{
					// Something is wrong with this file, this error should never happen!
					Log.e( TAG , "Exception in getFileContentAsStream." , e );
					// Update file list - this file is missing forever
					removeFileFromFileList( pFileName );
					return null;
				}
				
			case STORAGE_TYPE_EXTERNAL:
				// The file is in external memory...
				if ( ManagerResource.isExternalStorageAvailable() )
				{
					// OK the SD is ready!
					try
					{
						File lFile = new File( Environment.getExternalStorageDirectory() , sPathExternalStorage + pFileName );
						FileInputStream lFileStream = new FileInputStream( lFile );
						return lFileStream;
					}
					catch( Exception ex )
					{
						// Something is wrong with this file, someone deleted it probably...
						Log.e( TAG , "Exception in getFileContentAsStream." , ex );
						// Update file list - this file is probably missing forever
						removeFileFromFileList( pFileName );
						return null;
					}
				}
				else
				{
					Log.e( TAG , "SD card is missing and we looked for a file on it..." );
					return null;
				}
		}
		
		// This line wont be reached
		return null;
	}
	
	/**
	 * Gets image from resource, scales it down maintaining aspect ratio and returns as a new Bitmap.
	 * 
	 * @param pFilename File name (without path).
	 * @param pDesiredWidth Desired maximum width of the image.
	 * @param pDesiredHeight Desired maximum height of the image.
	 * @return New Bitmap or null if it was impossible to create it (missing image).
	 */
	public static synchronized Bitmap getScaledBitmap( String pFilename , int pDesiredWidth , int pDesiredHeight )
	{
		if ( !isFileAvailable( pFilename ) )
		{
			return null;
		}
		
		try
		{
			Bitmap lBitmap = null;
			InputStream lFileStream = getFileContentAsStream( pFilename );
			lBitmap = BitmapFactory.decodeStream( lFileStream );
			lFileStream.close();
			
			if ( lBitmap == null )
			{
				// We could not read image = it is probably being created right now and not ready yet!
				return null;
			}
			
			int lBitmapWidth = lBitmap.getWidth();
			int lBitmapHeight = lBitmap.getHeight();
			
			// Final width - at first we assume bitmap is small enough to display it fully
			int lFinalWidth = lBitmapWidth;
			int lFinalHeight = lBitmapHeight;
			
			// It is too large!
			if ( lBitmapWidth > pDesiredWidth )
			{
				lFinalWidth = pDesiredWidth;
				lFinalHeight = ( lBitmapHeight * lFinalWidth ) / lBitmapWidth;
				
				if ( pDesiredHeight < lFinalHeight )
				{
					lFinalHeight = pDesiredHeight;
					lFinalWidth = ( lBitmapWidth * lFinalHeight ) / lBitmapHeight;
				}
			}
			else if ( lBitmapHeight > pDesiredHeight )
			{
				lFinalHeight = pDesiredHeight;
				lFinalWidth = ( lBitmapWidth * lFinalHeight ) / lBitmapHeight;
			}
			
			if ( ( lFinalWidth != lBitmapWidth ) || ( lFinalHeight != lBitmapHeight ) )
			{
				// Do the scaling
				Bitmap lResizedBitmap = Bitmap.createScaledBitmap( lBitmap , lFinalWidth , lFinalHeight , false );
				
				// Recycle memory
				lBitmap.recycle();
				
				// Return scaled bitmap
				return lResizedBitmap;
			}
			else
			{
				// Return original bitmap - it does not need scaling
				return lBitmap;
			}
		}
		catch( Throwable t )
		{
			// Probably an out of memory error during loading
			Log.e( TAG , "Error during creating bitmap." , t );
			return null;
		}
	}
	
	/**
	 * Gets image from resource, scales it down maintaining aspect ratio and returns as a new BitmapDrawable.
	 * 
	 * @param pFilename File name (without path).
	 * @param pDesiredWidth Desired maximum width of the image.
	 * @param pDesiredHeight Desired maximum height of the image.
	 * @return New BitmapDrawable or null if it was impossible to create it (missing image).
	 */
	public static synchronized BitmapDrawable getScaledBitmapDrawable( String pFilename , int pDesiredWidth , int pDesiredHeight )
	{
		Bitmap lBitmap = getScaledBitmap( pFilename , pDesiredWidth , pDesiredHeight );
		
		if ( lBitmap != null )
		{
			return new BitmapDrawable( sContext.getResources() , lBitmap );
		}
		
		return null;
	}
	
	/**
	 * Saves Bitmap to Storage depending which is available firstly trying to save on External one
	 * 
	 * @param pBitmap Bitmap to save
	 * @param pFileName Bitmap name to use
	 */
	public static void saveImageToStorage( Bitmap pBitmap , String pFileName )
	{
		Bitmap lBitmapImage = pBitmap;
		ByteArrayOutputStream lByteArray = new ByteArrayOutputStream();
		lBitmapImage.compress( Bitmap.CompressFormat.PNG , 100 , lByteArray );
		byte[] lBitmapByteArray = lByteArray.toByteArray();
		if ( isExternalStorageAvailable() )
		{
			saveDataToExternalStorage( lBitmapByteArray , pFileName , getExternalStoragePath() );
		}
		else
		{
			saveDataAsNewInternalFile( lBitmapByteArray , pFileName );
		}
	}
	
	/**
	 * Saves Drawable to Storage depending which is available firstly trying to save on External one
	 * 
	 * @param pDrawable Drawable to save
	 * @param pFileName Drawable name to use
	 */
	public static void saveImageToStorage( Drawable pDrawable , String pFileName )
	{
		Bitmap lBitmapImage = ( ( (BitmapDrawable) pDrawable ).getBitmap() );
		ByteArrayOutputStream lByteArray = new ByteArrayOutputStream();
		lBitmapImage.compress( Bitmap.CompressFormat.PNG , 100 , lByteArray );
		byte[] lBitmapByteArray = lByteArray.toByteArray();
		if ( isExternalStorageAvailable() )
		{
			saveDataToExternalStorage( lBitmapByteArray , pFileName , getExternalStoragePath() );
		}
		else
		{
			saveDataAsNewInternalFile( lBitmapByteArray , pFileName );
		}
	}
	
	/**
	 * Saves data to external storage in specified folder and file.
	 * 
	 * @param pData Byte array with data to save.
	 * @param pFileName Desired file name.
	 * @param pDestinationFolderName Name of destination folder where the file will be saved (for example "Images").
	 * @return True if everything is ok, false otherwise.
	 */
	public static boolean saveDataToExternalStorage( byte[] pData , String pFileName , String pDestinationFolderName )
	{
		try
		{
			// Prepare full path
			String lFullDestinationPath = Environment.getExternalStorageDirectory() + "/" + pDestinationFolderName;
			
			// Create external storage folder
			File lFolder = new File( lFullDestinationPath );
			lFolder.mkdirs();
			
			// Create file and save data
			File lFile = new File( lFullDestinationPath , pFileName );
			FileOutputStream lFileStream = new FileOutputStream( lFile );
			lFileStream.write( pData );
			lFileStream.flush();
			lFileStream.close();
			// Add file to file list so we can use it
			addFileToFileList( pFileName , STORAGE_TYPE_EXTERNAL , lFile.length() );
			return true;
		}
		catch( Exception e )
		{
			Log.e( TAG , "Exception in saveDataToExternalStorage." , e );
			return false;
		}
	}
	
	/**
	 * Gets priority that higher than all current priorities.
	 * 
	 * @return Current max priority.
	 */
	public static int getCurrentMaxPriority()
	{
		return ( sCurrentMaxPriority + 1 );
	}
	
	/**
	 * Gets priority that is lower than all current priorities.
	 * 
	 * @return Current min priority.
	 */
	public static int getCurrentMinPriority()
	{
		return ( sCurrentMinPriority - 1 );
	}
	
	/**
	 * Clears total size of downloaded files and downloaded files counter.
	 */
	public static void clearTotalDownloadStats()
	{
		sTotalDownloadSize = 0;
		sTotalDownloadFiles = 0;
		sTotalDownloadTime = 0;
	}
	
	/**
	 * Adds one file to download stats.
	 */
	public static synchronized void statsAddFile()
	{
		sTotalDownloadFiles++;
	}
	
	/**
	 * Adds more time to total spent time.
	 * 
	 * @param pTime Time to add.
	 */
	public static synchronized void statsAddTime( long pTime )
	{
		sTotalDownloadTime += pTime;
	}
	
	/**
	 * Adds downloaded bytes number to statistics.
	 * 
	 * @param pNumBytes Number of downloaded bytes.
	 */
	public static synchronized void statsAddSize( int pNumBytes )
	{
		sTotalDownloadSize += pNumBytes;
	}
	
	/**
	 * Gets total number of downloaded files.
	 * 
	 * @return Number of downloaded files.
	 */
	public static int getDownloadFilesNumber()
	{
		return sTotalDownloadFiles;
	}
	
	/**
	 * Gets total size of downloaded files.
	 * 
	 * @return Total size of downloaded files.
	 */
	public static int getDownloadSize()
	{
		return sTotalDownloadSize;
	}
	
	/**
	 * Gets total time spent on downloading.
	 * 
	 * @return Total time spent in ms.
	 */
	public static long getDownloadTime()
	{
		return sTotalDownloadTime;
	}
	
	/**
	 * Gets average download speed in bytes per second.
	 * 
	 * @return Average download speed.
	 */
	public static int getAverageDownloadSpeed()
	{
		int lTotalTimeInSeconds = (int) ( ( sTotalDownloadTime + 500 ) / 1000 );
		if ( lTotalTimeInSeconds == 0 )
		{
			lTotalTimeInSeconds = 1;
		}
		return ( sTotalDownloadSize / lTotalTimeInSeconds );
	}
	
	/**
	 * Checks if ResourceManager is busy with any tasks.
	 * 
	 * @return True if there is work to do.
	 */
	public static synchronized boolean isBusy()
	{
		if ( !sTaskQueueSmallFiles.isEmpty() || !sTaskQueueLargeFiles.isEmpty() )
		{
			return true;
		}
		for ( DownloadThread lThread : sThreadList )
		{
			if ( lThread.getCurrentTask() != null )
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if ResourceManager is busy with tasks of specific DownloadFinishedListener.
	 * 
	 * @param pListener DownloadFinishedListener to check for.
	 * @return True if ResourceManager is downloading something for this DownloadFinishedListener.
	 */
	public synchronized static boolean isBusy( IDownloadFinishedListener pListener )
	{
		if ( !isBusy() )
		{
			return false;
		}
		
		for ( DownloadTask lTask : sTaskQueueSmallFiles )
		{
			if ( lTask.getListener() == pListener )
			{
				return true;
			}
		}
		for ( DownloadTask lTask : sTaskQueueLargeFiles )
		{
			if ( lTask.getListener() == pListener )
			{
				return true;
			}
		}
		for ( DownloadThread lThread : sThreadList )
		{
			DownloadTask lTask = lThread.getCurrentTask();
			if ( lTask != null )
			{
				if ( lTask.getListener() == pListener )
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if there are any other tasks left for chosen listener.
	 * Finishes the download if there is nothing.
	 * Launched every time a task is finished.
	 * 
	 * @param pListener IDownloadFinishedListener belonging to task that has just been finished.
	 */
	public static synchronized void checkTasks( final IDownloadFinishedListener pListener )
	{
		// Notify listener that all tasks are finished if they are
		if ( pListener != null )
		{
			if ( !isBusy( pListener ) )
			{
				sHandler.post( new Runnable()
				{
					@Override
					public void run()
					{
						// We just finished last download
						pListener.onDownloadingFinished();
					}
				} );
			}
		}
		
		if ( !ManagerResource.isBusy() )
		{
			// We just finished all possible tasks!
			sIsDownloadingInProgress = false;
			sDownloadingEndTime = System.currentTimeMillis();
			statsAddTime( sDownloadingEndTime - sDownloadingStartTime );
			Log.i( TAG , "Finished all downloading tasks! Total downloaded bytes = " + getDownloadSize() + " in " + getDownloadFilesNumber()
					+ " files." );
			Log.i( TAG , "Total downloading time = " + getDownloadTime() + " average speed = " + getAverageDownloadSpeed() + " bytes per second." );
			freeMemory();
		}
	}
	
	/**
	 * Frees memory buffers in DownloadThread.
	 */
	public static void freeMemory()
	{
		if ( !ManagerResource.isBusy() )
		{
			for ( DownloadThread lThread : sThreadList )
			{
				if ( lThread.getCurrentTask() == null )
				{
					lThread.deallocateLocalMemorySlots();
				}
			}
			MemoryManager.removeDeallocatedMemorySlots();
		}
	}
	
	/**
	 * Starts to download tasks.
	 */
	private static void startDownloading()
	{
		for ( DownloadThread lThread : sThreadList )
		{
			lThread.awake();
		}
		if ( !sIsDownloadingInProgress )
		{
			sDownloadingStartTime = System.currentTimeMillis();
		}
		sIsDownloadingInProgress = true;
	}
	
	/**
	 * Container for keeping info about single file.
	 * Each file can be stored in internal or external memory.
	 * 
	 * @author Olgierd Uzieblo
	 */
	private static class FileDescriptor
	{
		/** Where this file is stored **/
		private int mStorageType;
		
		/** Size of this file **/
		private long mFileSize;
		
		/**
		 * Creates this FileDescriptor.
		 * 
		 * @param pStorageType One of 3 storage types.
		 */
		public FileDescriptor( int pStorageType , long pFileSize )
		{
			mStorageType = pStorageType;
			mFileSize = pFileSize;
		}
		
		/**
		 * Gets storage type of this file.
		 */
		public int getStorageType()
		{
			return mStorageType;
		}
		
		/**
		 * Gets file size.
		 * 
		 * @return Size of this file in bytes.
		 */
		public long getSize()
		{
			return mFileSize;
		}
	}
}
