package fr.playsoft.android.tools.debug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;

import android.os.Environment;
import fr.playsoft.android.tools.resource.ManagerResource;

/**
 * Class with Log Tools
 * Automatically fills TAG field and line of Log
 * 
 * @author Klawikowski
 * 
 */
public class Log
{
	/**
	 * Global log enable flag - should be disabled before publishing.
	 */
	public static final boolean ENABLE_LOG = true;
	public static final boolean WITH_FILE_NAME_AND_LINE_NUM = true;
	private static File sExternalDirectory = null;
	private static File sExternalFile = null;
	private static final String DEBUG_LOG_FILE_NAME = "log.txt";
	
	private static final int INDEX_TAG = 0;
	private static final int INDEX_SUFFIX = 1;
	
	/**
	 * Logs data to file on sd card.
	 * 
	 * @param tag Tag String.
	 * @param msg Message String.
	 */
	public static void file( String tag , String msg )
	{
		if ( ManagerResource.isExternalStorageAvailable() )
		{
			if ( sExternalDirectory == null )
			{
				// Its first start, lets initialize directory and file
				sExternalDirectory = new File( Environment.getExternalStorageDirectory() , ManagerResource.getExternalStoragePath() );
				sExternalDirectory.mkdirs();
				sExternalFile = new File( sExternalDirectory , DEBUG_LOG_FILE_NAME );
				// Checking if file already exists
				if ( !sExternalFile.exists() || !sExternalFile.isFile() )
				{
					// Lets create the initial file!
					try
					{
						FileOutputStream lFileStream = new FileOutputStream( sExternalFile );
						PrintStream lStackStream = new PrintStream( lFileStream );
						lStackStream.println( "Initialized on " + getCurrentDate() );
						lStackStream.println( "Manufacturer: " + android.os.Build.MANUFACTURER );
						lStackStream.println( "Model: " + android.os.Build.MODEL );
						lStackStream.println( "SDK version: " + android.os.Build.VERSION.SDK_INT );
						lStackStream.close();
						lFileStream.close();
					}
					catch( Exception e )
					{
						// If it happens we are lost :(
					}
				}
			}
			// Add new data at the end of file
			try
			{
				FileOutputStream lFileStream = new FileOutputStream( sExternalFile , true );
				PrintStream lStackStream = new PrintStream( lFileStream );
				lStackStream.println( getCurrentDate() + "|" + tag + "|" + msg );
				lStackStream.close();
				lFileStream.close();
			}
			catch( Exception e )
			{
				// If it happens we are lost :(
			}
		}
	}
	
	private static String[] getDefaultTagAndMessageSuffix()
	{
		Throwable lThrowable = new Throwable();
		StackTraceElement lMethodCaller = lThrowable.getStackTrace()[ 2 ];
		String lFullClass = lMethodCaller.getClassName();
		int lIndex = lFullClass.lastIndexOf( "." );
		String lTag = lFullClass.substring( lIndex + 1 , lFullClass.length() );
		
		String lSuffix = "";
		if ( WITH_FILE_NAME_AND_LINE_NUM )
		{
			String lFileName = lMethodCaller.getFileName();
			int lLineNum = lMethodCaller.getLineNumber();
			lSuffix = " (" + lFileName + ":" + lLineNum + ")";
		}
		String[] lResult = new String[ 2 ];
		lResult[ INDEX_TAG ] = lTag;
		lResult[ INDEX_SUFFIX ] = lSuffix;
		return lResult;
	}
	
	private static String getCurrentDate()
	{
		return new Date( System.currentTimeMillis() ).toString();
	}
	
	public static int d( String msg )
	{
		String[] lDefaults = getDefaultTagAndMessageSuffix();
		return android.util.Log.d( lDefaults[ INDEX_TAG ] , msg + lDefaults[ INDEX_SUFFIX ] );
	}
	
	public static int d( String tag , String msg )
	{
		if ( ENABLE_LOG )
		{
			return android.util.Log.d( tag , msg );
		}
		return 0;
	}
	
	public static int d( String msg , Throwable tr )
	{
		if ( ENABLE_LOG )
		{
			String[] lDefaults = getDefaultTagAndMessageSuffix();
			return android.util.Log.d( lDefaults[ INDEX_TAG ] , msg , tr );
		}
		return 0;
	}
	
	public static int d( String tag , String msg , Throwable tr )
	{
		if ( ENABLE_LOG )
		{
			return android.util.Log.d( tag , msg , tr );
		}
		return 0;
	}
	
	public static int e( String msg )
	{
		if ( ENABLE_LOG )
		{
			String[] lDefaults = getDefaultTagAndMessageSuffix();
			return android.util.Log.e( lDefaults[ INDEX_TAG ] , msg + lDefaults[ INDEX_SUFFIX ] );
		}
		return 0;
	}
	
	public static int e( String tag , String msg )
	{
		if ( ENABLE_LOG )
		{
			return android.util.Log.e( tag , msg );
		}
		return 0;
	}
	
	public static int e( String msg , Throwable tr )
	{
		if ( ENABLE_LOG )
		{
			String[] lDefaults = getDefaultTagAndMessageSuffix();
			return android.util.Log.e( lDefaults[ INDEX_TAG ] , msg , tr );
		}
		return 0;
	}
	
	public static int e( String tag , String msg , Throwable tr )
	{
		if ( ENABLE_LOG )
		{
			return android.util.Log.e( tag , msg , tr );
		}
		return 0;
	}
	
	public static String getStackTraceString( Throwable tr )
	{
		if ( ENABLE_LOG )
		{
			return android.util.Log.getStackTraceString( tr );
		}
		return new String( "" );
	}
	
	public static int i( String msg )
	{
		if ( ENABLE_LOG )
		{
			String[] lDefaults = getDefaultTagAndMessageSuffix();
			return android.util.Log.i( lDefaults[ INDEX_TAG ] , msg + lDefaults[ INDEX_SUFFIX ] );
		}
		return 0;
	}
	
	public static int y( String msg )
	{
		if ( ENABLE_LOG )
		{
			return android.util.Log.d( "Yarek" , msg );
		}
		return 0;
	}
	
	public static int i( String tag , String msg )
	{
		if ( ENABLE_LOG )
		{
			return android.util.Log.i( tag , msg );
		}
		return 0;
	}
	
	public static int i( String msg , Throwable tr )
	{
		if ( ENABLE_LOG )
		{
			String[] lDefaults = getDefaultTagAndMessageSuffix();
			return android.util.Log.i( lDefaults[ INDEX_TAG ] , msg , tr );
		}
		return 0;
	}
	
	public static int i( String tag , String msg , Throwable tr )
	{
		if ( ENABLE_LOG )
		{
			return android.util.Log.i( tag , msg , tr );
		}
		return 0;
	}
	
	public static boolean isLoggable( int level )
	{
		if ( ENABLE_LOG )
		{
			String[] lDefaults = getDefaultTagAndMessageSuffix();
			return android.util.Log.isLoggable( lDefaults[ INDEX_TAG ] , level );
		}
		return false;
	}
	
	public static boolean isLoggable( String tag , int level )
	{
		if ( ENABLE_LOG )
		{
			return android.util.Log.isLoggable( tag , level );
		}
		return false;
	}
	
	public static int println( int priority , String msg )
	{
		if ( ENABLE_LOG )
		{
			String[] lDefaults = getDefaultTagAndMessageSuffix();
			return android.util.Log.println( priority , lDefaults[ INDEX_TAG ] , msg + lDefaults[ INDEX_SUFFIX ] );
		}
		return 0;
	}
	
	public static int println( int priority , String tag , String msg )
	{
		if ( ENABLE_LOG )
		{
			return android.util.Log.println( priority , tag , msg );
		}
		return 0;
	}
	
	public static int v( String msg )
	{
		if ( ENABLE_LOG )
		{
			String[] lDefaults = getDefaultTagAndMessageSuffix();
			return android.util.Log.v( lDefaults[ INDEX_TAG ] , msg + lDefaults[ INDEX_SUFFIX ] );
		}
		return 0;
	}
	
	public static int v( String tag , String msg )
	{
		if ( ENABLE_LOG )
		{
			return android.util.Log.v( tag , msg );
		}
		return 0;
	}
	
	public static int v( String msg , Throwable tr )
	{
		if ( ENABLE_LOG )
		{
			String[] lDefaults = getDefaultTagAndMessageSuffix();
			return android.util.Log.v( lDefaults[ INDEX_TAG ] , msg , tr );
		}
		return 0;
	}
	
	public static int v( String tag , String msg , Throwable tr )
	{
		if ( ENABLE_LOG )
		{
			return android.util.Log.v( tag , msg , tr );
		}
		return 0;
	}
	
	public static int w( Throwable tr )
	{
		if ( ENABLE_LOG )
		{
			String[] lDefaults = getDefaultTagAndMessageSuffix();
			return android.util.Log.w( lDefaults[ INDEX_TAG ] , tr );
		}
		return 0;
	}
	
	public static int w( String msg , Throwable tr )
	{
		if ( ENABLE_LOG )
		{
			String[] lDefaults = getDefaultTagAndMessageSuffix();
			return android.util.Log.w( lDefaults[ INDEX_TAG ] , msg , tr );
		}
		return 0;
	}
	
	public static int w( String tag , String msg , Throwable tr )
	{
		if ( ENABLE_LOG )
		{
			return android.util.Log.w( tag , msg , tr );
		}
		return 0;
	}
	
	public static int w( String msg )
	{
		if ( ENABLE_LOG )
		{
			String[] lDefaults = getDefaultTagAndMessageSuffix();
			return android.util.Log.w( lDefaults[ INDEX_TAG ] , msg + lDefaults[ INDEX_SUFFIX ] );
		}
		return 0;
	}
	
	public static int w( String tag , String msg )
	{
		if ( ENABLE_LOG )
		{
			return android.util.Log.w( tag , msg );
		}
		return 0;
	}
}
