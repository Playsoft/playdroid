package fr.playsoft.android.tools.customcomponents;

import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import fr.playsoft.android.tools.customcomponents.interfaces.IOnGestureFlingListener;

/**
 * Universal Gesture Detector can be used on any view
 * 
 * @author Przemyslaw Klawikowski
 * 
 */
public class UltraGestureDetector implements View.OnTouchListener
{
	
	private static final int SWIPE_MIN_DISTANCE = 50;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 100;
	private IOnGestureFlingListener mGestureFlingListener;
	private GestureDetector mGestureDetector;
	
	public UltraGestureDetector( IOnGestureFlingListener pListener )
	{
		mGestureFlingListener = pListener;
		mGestureDetector = new GestureDetector( new mGestureDetector() );
	}
	
	@Override
	public boolean onTouch( View v , MotionEvent event )
	{
		return mGestureDetector.onTouchEvent( event );
	}
	
	class mGestureDetector extends SimpleOnGestureListener
	{
		@Override
		public boolean onFling( MotionEvent e1 , MotionEvent e2 , float velocityX , float velocityY )
		{
			try
			{
				if ( Math.abs( e1.getY() - e2.getY() ) > SWIPE_MAX_OFF_PATH )
				{
					return false;
				}
				if ( e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs( velocityX ) > SWIPE_THRESHOLD_VELOCITY )
				{
					mGestureFlingListener.OnLeftFling();
				}
				
				else if ( e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs( velocityX ) > SWIPE_THRESHOLD_VELOCITY )
				{
					mGestureFlingListener.OnRightFling();
				}
			}
			catch( Exception e )
			{
			}
			return false;
		}
		
	}
}
