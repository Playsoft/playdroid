package fr.playsoft.android.tools.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.os.Environment;
import fr.playsoft.android.tools.debug.Log;

/**
 * Tool for copying of large files between internal/external memory.
 * 
 * @author Olgierd Uzieblo
 */
public class FileCopier
{
	/** Tag for LogCat **/
	public static final String TAG = "FileCopier";
	
	/** Type of storage of source file **/
	private int mSourceStorageType;
	
	/** File name of source file **/
	private String mSourceFileName;
	
	/** Type of storage of destination file **/
	private int mDestinationStorageType;
	
	/** File name of destination file **/
	private String mDestinationFileName;
	
	/** True will delete source file after copying **/
	private boolean mDeleteSourceAfterCopy;
	
	/** Context to use **/
	private Context mContext;
	
	/** Optional file crypter to encrypt file **/
	private IFileCrypter mFileCrypter;
	
	/** True copies file in a new thread **/
	private boolean mIsCopyInNewThread;
	
	/** Optional progress listener **/
	private IFileCopyProgress mCopyProgress;
	
	/** Our own instance **/
	private FileCopier mInstance;
	
	/**
	 * Creates new FileCopier.
	 * 
	 * @param pContext Context to use.
	 * @param pSourceMemoryType Type of memory for source file.
	 * @param pSourceFileName Source file name.
	 * @param pDestinationMemoryType Type of memory for destination file.
	 * @param pDestinationFileName Destination file name.
	 * @param pDeleteSourceAfterCopy True will delete source file after copy.
	 * @param pIsCopyInNewThread True will launch copying in a new thread.
	 * @param pFileCrypter Optional file crypter to use while copying. Can be null.
	 * @param pCopyProgress Optional progress listener. Can be null.
	 */
	public FileCopier( Context pContext , int pSourceMemoryType , String pSourceFileName , int pDestinationMemoryType , String pDestinationFileName ,
			boolean pDeleteSourceAfterCopy , boolean pIsCopyInNewThread , IFileCrypter pFileCrypter , IFileCopyProgress pCopyProgress )
	{
		mInstance = this;
		mSourceStorageType = pSourceMemoryType;
		mSourceFileName = pSourceFileName;
		mDestinationStorageType = pDestinationMemoryType;
		mDestinationFileName = pDestinationFileName;
		mDeleteSourceAfterCopy = pDeleteSourceAfterCopy;
		mContext = pContext;
		mIsCopyInNewThread = pIsCopyInNewThread;
		mFileCrypter = pFileCrypter;
		mCopyProgress = pCopyProgress;
	}
	
	/**
	 * Copies the file.
	 * 
	 * @return True if copy was successful. If copying is made in a new thread we return true immediately.
	 */
	public boolean copy()
	{
		if ( mIsCopyInNewThread )
		{
			Thread lCopyThread = new Thread( new Runnable()
			{
				@Override
				public void run()
				{
					performCopying();
				}
			} );
			lCopyThread.setPriority( Thread.MIN_PRIORITY );
			lCopyThread.start();
			return true;
		}
		else
		{
			return performCopying();
		}
	}
	
	private boolean performCopying()
	{
		MemorySlot lMemorySlot = MemoryManager.getMemorySlot();
		try
		{
			long lStartTime = System.currentTimeMillis();
			FileInputStream lFileInputStream = getFileInputStream( mContext , mSourceStorageType , mSourceFileName );
			int lInputFileSize = lFileInputStream.available();
			FileOutputStream lFileOutputStream = getFileOutputStream( mContext , mDestinationStorageType , mDestinationFileName );
			byte[] lData = lMemorySlot.getData();
			int lTotalRead = 0; // Total amount of copied data
			int lNumRead;
			int lPartNumber = 0;
			do
			{
				lNumRead = lFileInputStream.read( lData , 0 , lData.length );
				if ( lNumRead != -1 )
				{
					if ( mFileCrypter != null )
					{
						if ( mSourceStorageType == ManagerResource.STORAGE_TYPE_INTERNAL )
						{
							mFileCrypter.encrypt( lPartNumber , lData , 0 , lNumRead );
						}
						else
						{
							mFileCrypter.decrypt( lPartNumber , lData , 0 , lNumRead );
						}
						lPartNumber++;
					}
					lFileOutputStream.write( lData , 0 , lNumRead );
					lTotalRead += lNumRead;
					if ( mCopyProgress != null )
					{
						mCopyProgress.onCopyProgressUpdate( mInstance , lTotalRead , lInputFileSize );
					}
				}
			}
			while( lNumRead != -1 );
			lFileOutputStream.close();
			lFileInputStream.close();
			lMemorySlot.deallocate();
			
			if ( mDeleteSourceAfterCopy )
			{
				if ( ManagerResource.isFileAvailable( mSourceFileName ) )
				{
					ManagerResource.deleteFile( mSourceFileName );
				}
				else
				{
					if ( mSourceStorageType == ManagerResource.STORAGE_TYPE_INTERNAL )
					{
						mContext.deleteFile( mSourceFileName );
					}
					else
					{
						File lDirectory = new File( Environment.getExternalStorageDirectory() , ManagerResource.getExternalStoragePath() );
						new File( lDirectory , mSourceFileName ).delete();
					}
				}
			}
			// Remove destination file from file list and add it again to be sure its in correct place
			if ( ManagerResource.isFileAvailable( mDestinationFileName ) )
			{
				ManagerResource.removeFileFromFileList( mDestinationFileName );
			}
			ManagerResource.addFileToFileList( mDestinationFileName , mDestinationStorageType , lInputFileSize );
			long lEndTime = System.currentTimeMillis();
			Log.i( TAG , "Copied " + mSourceFileName + " to " + mDestinationFileName + " in " + ( lEndTime - lStartTime ) + " ms." );
			if ( mCopyProgress != null )
			{
				mCopyProgress.onCopyFinished( mInstance );
			}
			return true;
		}
		catch( Exception e )
		{
			Log.e( TAG , "Error copying file from " + mSourceFileName + " to " + mDestinationFileName , e );
			lMemorySlot.deallocate();
			return false;
		}
	}
	
	private FileInputStream getFileInputStream( Context pContext , int pSourceMemoryType , String pSourceFileName ) throws FileNotFoundException
	{
		FileInputStream lFileInputStream;
		if ( pSourceMemoryType == ManagerResource.STORAGE_TYPE_INTERNAL )
		{
			lFileInputStream = pContext.openFileInput( pSourceFileName );
		}
		else
		{
			File lDirectory = new File( Environment.getExternalStorageDirectory() , ManagerResource.getExternalStoragePath() );
			lFileInputStream = new FileInputStream( new File( lDirectory , pSourceFileName ) );
		}
		return lFileInputStream;
	}
	
	private FileOutputStream getFileOutputStream( Context pContext , int pDestinationMemoryType , String pDestinationFileName )
			throws FileNotFoundException
	{
		FileOutputStream lFileOutputStream;
		if ( pDestinationMemoryType == ManagerResource.STORAGE_TYPE_INTERNAL )
		{
			lFileOutputStream = pContext.openFileOutput( pDestinationFileName , Context.MODE_PRIVATE );
		}
		else
		{
			File lDirectory = new File( Environment.getExternalStorageDirectory() , ManagerResource.getExternalStoragePath() );
			lDirectory.mkdirs();
			lFileOutputStream = new FileOutputStream( new File( lDirectory , pDestinationFileName ) );
		}
		return lFileOutputStream;
	}
}
