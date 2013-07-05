package fr.playsoft.android.tools.resource;

/**
 * Object describing single download task.
 * 
 * @author Olgierd Uzieblo
 */
public class DownloadTask
{
	/** Tag for LogCat **/
	public static final String TAG = "DownloadTask";
	
	/** Task states **/
	public static final int TASK_STATE_NOT_STARTED = 0;
	public static final int TASK_STATE_IN_PROGRESS = 1;
	public static final int TASK_STATE_FINISHED = 2;
	
	/** Resource descriptor of resource to download **/
	private ResourceDescriptor mResourceDescriptor;
	
	/** Current state of this downloading task **/
	private int mState;
	
	/** Listener which gets notifications about this task **/
	private IDownloadFinishedListener mListener;
	
	/** Our own instance **/
	private DownloadTask mInstance;
	
	/** Number of retries **/
	private int mNumRetries;
	
	/** Priority of this task **/
	private Integer mPriority;
	
	/** File checker to check if file is correct **/
	private IFileChecker mFileChecker;
	
	/** Type of this task **/
	private int mTaskType;
	
	/** Optional file crypter for this file **/
	private IFileCrypter mFileCrypter;
	
	/** Optional url creator for this file **/
	private IURLCreator mURLCreator;
	
	/** Optional tag object **/
	private Object mTag;
	
	/**
	 * Creates new download task.
	 * 
	 * @param pResourceDescriptor ResourceDescriptor for this task.
	 * @param pListener DownloadFinishedListener to be notified when it is finished.
	 * @param pPriority Priority of this task - the bigger the priority the sooner it will start.
	 * @param pChecker File checker of this task.
	 * @param pTaskType Type of task - large or small file.
	 * @param pFileCrypter Optional file crypter, can be null if file is not encrypted.
	 * @param pURLCreator Optional url creator, can be null.
	 */
	public DownloadTask( ResourceDescriptor pResourceDescriptor , IDownloadFinishedListener pListener , int pPriority , IFileChecker pChecker ,
			int pTaskType , IFileCrypter pFileCrypter , IURLCreator pURLCreator )
	{
		mResourceDescriptor = pResourceDescriptor;
		mState = TASK_STATE_NOT_STARTED;
		mListener = pListener;
		mPriority = pPriority;
		mFileChecker = pChecker;
		mTaskType = pTaskType;
		mFileCrypter = pFileCrypter;
		mURLCreator = pURLCreator;
		mInstance = this;
		mNumRetries = 0;
	}
	
	/**
	 * Sets any tag Object.
	 * 
	 * @param pTag Tag Object.
	 */
	public void setTag( Object pTag )
	{
		mTag = pTag;
	}
	
	/**
	 * Gets tag object.
	 * 
	 * @return Object that was set with setTag.
	 */
	public Object getTag()
	{
		return mTag;
	}
	
	/**
	 * Returns listener used by this download task.
	 * 
	 * @return DownloadFinishedListener of this task.
	 */
	public IDownloadFinishedListener getListener()
	{
		return mListener;
	}
	
	/**
	 * Gets priority of this task.
	 * 
	 * @return Priority, the higher the better.
	 */
	public Integer getPriority()
	{
		return mPriority;
	}
	
	/**
	 * Gets file checker of this task.
	 * 
	 * @return File checker instance.
	 */
	public IFileChecker getFileChecker()
	{
		return mFileChecker;
	}
	
	/**
	 * Gets file crypter of this task.
	 * 
	 * @return IFileCrypter instance or null.
	 */
	public IFileCrypter getFileCrypter()
	{
		return mFileCrypter;
	}
	
	/**
	 * Gets URL creator of this task.
	 * 
	 * @return IURLCreator instance or null.
	 */
	public IURLCreator getURLCreator()
	{
		return mURLCreator;
	}
	
	/**
	 * Compares this DownloadTask with another DownloadTask.
	 * 
	 * @param pTask Other DownloadTask.
	 * @return True if they are the same = point to the same resource and listener.
	 */
	@Override
	public boolean equals( Object pTask )
	{
		DownloadTask lTask = (DownloadTask) pTask;
		
		if ( ( lTask.getListener() == mListener ) || ( mListener == null ) || ( lTask.getListener() == null ) )
		{
			if ( lTask.getFileName().equals( mResourceDescriptor.getMD5() ) )
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets number of retries.
	 * 
	 * @return Number of times we tried to download this task.
	 */
	public int getNumRetries()
	{
		return mNumRetries;
	}
	
	/**
	 * Sets different num retries.
	 * 
	 * @param pNumRetries New num retries.
	 */
	public void setNumRetries( int pNumRetries )
	{
		mNumRetries = pNumRetries;
	}
	
	/**
	 * Increases num retries and restarts task.
	 */
	public void restartTask()
	{
		mNumRetries++;
		mState = TASK_STATE_NOT_STARTED;
		if ( mPriority > 0 )
		{
			mPriority = Math.max( mPriority - 1 , 1 );
		}
		else
		{
			mPriority--;
		}
		ManagerResource.addTask( this );
	}
	
	/**
	 * Gets URL of this task.
	 * 
	 * @return URL of this task.
	 */
	public String getURL()
	{
		return mResourceDescriptor.getURL();
	}
	
	/**
	 * Gets result filename of this task.
	 * 
	 * @return Result filename without path.
	 */
	public String getFileName()
	{
		return mResourceDescriptor.getMD5();
	}
	
	/**
	 * Gets type of this task.
	 * 
	 * @return TASK_TYPE_SMALL_FILE or TASK_TYPE_LARGE_FILE
	 */
	public int getTaskType()
	{
		return mTaskType;
	}
	
	/**
	 * Checks if this task is still not started.
	 * 
	 * @return True if task is not started yet.
	 */
	public boolean isWaitingForStart()
	{
		if ( mState == TASK_STATE_NOT_STARTED )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Informs this task that download is starting.
	 * 
	 * @return True if it is ok, false if task is already being downloaded.
	 */
	public synchronized boolean startDownloading()
	{
		if ( isWaitingForStart() )
		{
			mState = TASK_STATE_IN_PROGRESS;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Checks if this task is finished.
	 * 
	 * @return True if it has finished.
	 */
	public synchronized boolean isFinished()
	{
		return mState > TASK_STATE_IN_PROGRESS;
	}
	
	/**
	 * Informs this task that download has finished successfully.
	 */
	public synchronized void downloadSuccessful()
	{
		mState = TASK_STATE_FINISHED;
		
		if ( mListener != null )
		{
			ManagerResource.getHandler().post( new Runnable()
			{
				@Override
				public void run()
				{
					mListener.onDownloadTaskSuccessful( mInstance );
				}
			} );
		}
	}
	
	/**
	 * Informs this task that download has failed.
	 */
	public synchronized void downloadFailed()
	{
		mState = TASK_STATE_FINISHED;
		
		if ( mListener != null )
		{
			ManagerResource.getHandler().post( new Runnable()
			{
				@Override
				public void run()
				{
					mListener.onDownloadTaskFailed( mInstance );
				}
			} );
		}
	}
	
	/**
	 * Informs this task that download has been cancelled.
	 */
	public synchronized void downloadCancelled()
	{
		mState = TASK_STATE_FINISHED;
	}
}
