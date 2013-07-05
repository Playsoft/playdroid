package fr.playsoft.android.tools.operation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

import android.content.Context;

/**
 * Class that manages a queue of Operations. Operations are launched
 * simultaneously using a pool of Threads.
 * 
 * @author Olgierd Uzieblo
 */
public class OperationQueue
{
	/** Number of operation performing threads **/
	public static final int MAX_THREADS_NUM = 5;
	
	/** ArrayList with all our operation threads **/
	private ArrayList< OperationThread > mThreadList;
	
	/** Queue of Operations to perform **/
	private PriorityBlockingQueue< Operation > mQueue;
	
	/** Context to use by Operations **/
	private Context mContext;
	
	/** Current maximum priority **/
	private int mCurrentMaxPriority;
	
	/**
	 * Comparator for choosing Operations with higher priority.
	 */
	private static final Comparator< Operation > PRIORITY_COMPARATOR = new Comparator< Operation >()
	{
		@Override
		public int compare( Operation object1 , Operation object2 )
		{
			// The higher the priority the earlier the task will start
			return -object1.getOperationPriority().compareTo( object2.getOperationPriority() );
		}
	};
	
	/**
	 * Creates this OperationQueue.
	 * 
	 * @param pContext Context to use.
	 */
	public OperationQueue( Context pContext )
	{
		mContext = pContext;
		mQueue = new PriorityBlockingQueue< Operation >( 1 , PRIORITY_COMPARATOR );
		mThreadList = new ArrayList< OperationThread >( MAX_THREADS_NUM );
		for ( int i = 0; i < MAX_THREADS_NUM; i++ )
		{
			OperationThread lOperationThread = new OperationThread( mContext , this );
			mThreadList.add( lOperationThread );
			Thread lThread = new Thread( lOperationThread );
			lThread.setPriority( Thread.MIN_PRIORITY );
			lThread.setName( "OperationThread number " + Integer.toString( i ) );
			lThread.start();
		}
	}
	
	/**
	 * Finishes - stops all threads.
	 */
	public void finish()
	{
		for ( OperationThread lThread : mThreadList )
		{
			lThread.exit();
			lThread.awake();
		}
	}
	
	/**
	 * Checks if there are any operations left to start.
	 * 
	 * @return True if something is still left in Operations queue.
	 */
	public synchronized boolean isBusy()
	{
		return mQueue.isEmpty();
	}
	
	/**
	 * Adds new Operation to the queue.
	 * 
	 * @param pOperation Operation to add.
	 */
	public synchronized void addOperation( Operation pOperation )
	{
		mCurrentMaxPriority = Math.max( mCurrentMaxPriority , pOperation.getOperationPriority() );
		mQueue.add( pOperation );
		startOperating();
	}
	
	/**
	 * Gets priority that is currently the highest used.
	 * 
	 * @return Current max priority.
	 */
	public int getCurrentMaxPriority()
	{
		return ( mCurrentMaxPriority + 1 );
	}
	
	/**
	 * Gets next Operation with the highest priority.
	 * 
	 * @return Next Operation that is not running yet. Return null if there are
	 *         no operations left.
	 */
	public synchronized Operation getNextOperation()
	{
		return mQueue.poll();
	}
	
	/**
	 * Starts to perform queued operations.
	 */
	private synchronized void startOperating()
	{
		for ( OperationThread lThread : mThreadList )
		{
			lThread.awake();
		}
	}
}
