package fr.playsoft.android.tools.drawables;

import java.util.concurrent.CopyOnWriteArrayList;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import fr.playsoft.android.tools.resource.ManagerResource;

/**
 * Static class designed to load & scale thumbnails.
 * init() must be launched before using!
 * 
 * @author Olgierd Uzieblo
 */
public class DrawableLoader
{
	/**
	 * Descriptor of single thumbnail loading task.
	 */
	private static class ThumbnailLoadingTaskDescriptor
	{
		/** Thumbnail md5 (file name) **/
		private String mMD5;
		
		/** Max specified image width **/
		private int mMaxWidth;
		
		/** Max specified image height **/
		private int mMaxHeight;
		
		/** Listener waiting for info about this thumbnail **/
		private IDrawableLoadingFinishedListener mListener;
		
		ThumbnailLoadingTaskDescriptor( String pMD5 , int pMaxWidth , int pMaxHeight , IDrawableLoadingFinishedListener pListener )
		{
			mMD5 = pMD5;
			mMaxWidth = pMaxWidth;
			mMaxHeight = pMaxHeight;
			mListener = pListener;
		}
		
		@Override
		public boolean equals( Object pTask )
		{
			ThumbnailLoadingTaskDescriptor lTask = (ThumbnailLoadingTaskDescriptor) pTask;
			
			if ( mMaxWidth != lTask.mMaxWidth )
			{
				return false;
			}
			if ( mMaxHeight != lTask.mMaxHeight )
			{
				return false;
			}
			if ( !mMD5.equals( lTask.mMD5 ) )
			{
				return false;
			}
			
			return true;
		}
	}
	
	/**
	 * Runnable that loads images.
	 */
	private static class LoaderTask implements Runnable
	{
		@Override
		public void run()
		{
			while( sIsInitialized )
			{
				if ( sTriggerPause )
				{
					sTriggerPause = false;
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
					while( !sRemainingTasks.isEmpty() )
					{
						final ThumbnailLoadingTaskDescriptor lTask = sRemainingTasks.get( sRemainingTasks.size() - 1 );
						final Drawable lResult;
						lResult = ManagerResource.getScaledBitmapDrawable( lTask.mMD5 , lTask.mMaxWidth , lTask.mMaxHeight );
						
						sRemainingTasks.remove( lTask );
						sHandler.post( new Runnable()
						{
							@Override
							public void run()
							{
								lTask.mListener.onDrawableLoaded( lResult );
							}
						} );
					}
					sTriggerPause = true;
				}
			}
		}
		
		public void awake()
		{
			synchronized( this )
			{
				this.notify();
			}
		}
	}
	
	/** Thread which loads thumbnails **/
	private static Thread sThumbnailLoadingThread;
	
	/** List of images to load **/
	private static CopyOnWriteArrayList< ThumbnailLoadingTaskDescriptor > sRemainingTasks;
	
	/** Flag to check if class is initialized **/
	private static boolean sIsInitialized = false;
	
	/** Handler for posting results **/
	private static Handler sHandler;
	
	/** Flag to trigger pause **/
	private static boolean sTriggerPause = true;
	
	/** Runnable that does the loading **/
	private static LoaderTask sLoaderTask;
	
	/**
	 * Initializes ThumbnailLoader. Must be called at the beginning!
	 * 
	 * @param pContext Context to use.
	 */
	public static void init()
	{
		if ( sIsInitialized )
		{
			return;
		}
		sIsInitialized = true;
		sHandler = new Handler();
		sRemainingTasks = new CopyOnWriteArrayList< ThumbnailLoadingTaskDescriptor >();
		sLoaderTask = new LoaderTask();
		sThumbnailLoadingThread = new Thread( sLoaderTask );
		sThumbnailLoadingThread.setName( "Drawable loading thread" );
		sThumbnailLoadingThread.setPriority( Thread.MIN_PRIORITY );
		sThumbnailLoadingThread.start();
	}
	
	/**
	 * Adds new thumbnail loading task.
	 * 
	 * @param pMD5 Thumbnail MD5.
	 * @param pMaxWidth Thumbnail max width.
	 * @param pMaxHeight Thumbnail max height.
	 * @param pListener Listener to notify when loading is finished.
	 */
	public static synchronized void addTask( String pMD5 , int pMaxWidth , int pMaxHeight , IDrawableLoadingFinishedListener pListener )
	{
		if ( !sIsInitialized )
		{
			return;
		}
		
		ThumbnailLoadingTaskDescriptor lNewTask = new ThumbnailLoadingTaskDescriptor( pMD5 , pMaxWidth , pMaxHeight , pListener );
		if ( !sRemainingTasks.contains( lNewTask ) )
		{
			sRemainingTasks.add( lNewTask );
			sLoaderTask.awake();
		}
	}
	
	/**
	 * Finishes, kills the thread.
	 */
	public static void finish()
	{
		sIsInitialized = false;
		sThumbnailLoadingThread.interrupt();
	}
}
