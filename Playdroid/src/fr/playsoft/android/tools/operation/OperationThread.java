package fr.playsoft.android.tools.operation;

import android.content.Context;

/**
 * Single operation performing thread.
 * 
 * @author Olgierd Uzieblo
 */
public class OperationThread implements Runnable
{
	/** Tag for this class **/
	public static final String TAG = "OperationThread";
	
	/** Max retries before task will be removed **/
	public static final int MAX_RETRIES = 3;
	
	/** Task currently being performed **/
	private Operation mCurrentOperation;
	
	/** Operation queue used by this OperationThread **/
	private OperationQueue mOperationQueue;
	
	/** Is thread running **/
	private boolean mIsRunning = true;
	
	/** Flag to trigger pause **/
	private boolean mTriggerPause = true;
	
	/** Context **/
	private Context mContext;
	
	/**
	 * Creates the downloading thread.
	 * 
	 * @param pContext Context to use.
	 * @param pOperationQueue Operation queue to use.
	 */
	public OperationThread( Context pContext , OperationQueue pOperationQueue )
	{
		mContext = pContext;
		mOperationQueue = pOperationQueue;
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
				mCurrentOperation = mOperationQueue.getNextOperation();
				
				if ( mCurrentOperation != null )
				{
					mCurrentOperation.start( mContext );
				}
				else
				{
					mTriggerPause = true;
				}
			}
		}
	}
	
	/**
	 * Pauses this operation thread.
	 */
	public void pause()
	{
		mTriggerPause = true;
	}
	
	/**
	 * Exits this operation thread.
	 */
	public void exit()
	{
		mIsRunning = false;
	}
	
	/**
	 * Wakes this operation thread.
	 */
	public void awake()
	{
		synchronized( this )
		{
			this.notify();
		}
	}
}
