package fr.playsoft.android.tools.customcomponents;

import android.app.Application;
import android.os.Handler;

/**
 * Application class
 * Holds basic methods which will help to run methods on UI Thread
 * 
 * @author Klawikowski
 * 
 */
public class AUltraApplication extends Application
{
	private static Handler sHandler;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		sHandler = new Handler();
	}
	
	/**
	 * Posts message on UI Thread.
	 * 
	 * @param pRunnable Runnable to run.
	 */
	public static void post( Runnable pRunnable )
	{
		sHandler.post( pRunnable );
	}
	
	/**
	 * Posts message on UI Thread with a delay.
	 * 
	 * @param pRunnable Runnable to run.
	 * @param pDelay Delay in ms.
	 */
	public static void postDelayed( Runnable pRunnable , int pDelay )
	{
		sHandler.postDelayed( pRunnable , pDelay );
	}
}
