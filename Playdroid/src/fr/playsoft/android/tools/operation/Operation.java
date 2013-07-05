package fr.playsoft.android.tools.operation;

import android.content.Context;
import android.os.Handler;

/**
 * Abstract class for single, runnable operation.
 * 
 * @author Olgierd Uzieblo
 */
public abstract class Operation
{
	/**
	 * Listener interested in Operation results.
	 */
	private IOperationResultListener mResultListener;
	
	/**
	 * Handler used for posting results.
	 */
	private Handler mHandler;
	
	/**
	 * Starts this operation.
	 * 
	 * @param pContext Context that can be used during running.
	 */
	public final synchronized void start( Context pContext )
	{
		final Operation lInstance = this;
		boolean lIsSuccessful = run( pContext );
		
		if ( lIsSuccessful )
		{
			if ( ( getListener() != null ) & ( mHandler != null ) )
			{
				mHandler.post( new Runnable()
				{
					@Override
					public void run()
					{
						getListener().onOperationSuccess( lInstance , getOperationResult() );
					}
				} );
			}
		}
		else
		{
			if ( ( getListener() != null ) & ( mHandler != null ) )
			{
				mHandler.post( new Runnable()
				{
					@Override
					public void run()
					{
						if ( getOperationResult() == null )
						{
							getListener().onOperationFailed( lInstance , "Unknown error, see LogCat" );
						}
						else
						{
							getListener().onOperationFailed( lInstance , getOperationResult() );
						}
					}
				} );
			}
		}
	}
	
	/**
	 * Runs this operation.
	 * 
	 * @param pContext Context to use.
	 * @return True if Operation completed successfully, false otherwise.
	 */
	protected abstract boolean run( Context pContext );
	
	/**
	 * Should return a result of this operation or null.
	 * 
	 * @return Object containing result of this operation.
	 */
	protected abstract Object getOperationResult();
	
	/**
	 * Gets priority of this Operation. The higher the Priority the earlier it will be launched.
	 * 
	 * @return Priority of this Operation.
	 */
	protected abstract Integer getOperationPriority();
	
	/**
	 * Gets object interested in results of this Operation.
	 * 
	 * @return Listener to be notified about this Operation results. Returns null if listener does not exist.
	 */
	private IOperationResultListener getListener()
	{
		return mResultListener;
	}
	
	/**
	 * Sets object interested in result of this Operation.
	 * 
	 * @param pListener Listener to use.
	 * @param pHandler Handler to use for posting results (should be created in UI thread).
	 */
	public final void setListener( IOperationResultListener pListener , Handler pHandler )
	{
		mResultListener = pListener;
		mHandler = pHandler;
	}
}
