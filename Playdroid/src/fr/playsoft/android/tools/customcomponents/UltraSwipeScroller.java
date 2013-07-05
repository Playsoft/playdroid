package fr.playsoft.android.tools.customcomponents;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Class which implements scroller in UltraSwipe
 * It enables to operate the scrolling speed animation after fling
 * 
 * @author Klawikowski
 * 
 */
public class UltraSwipeScroller extends Scroller
{
	
	private double mScrollFactor = 1;
	
	public UltraSwipeScroller( Context context )
	{
		super( context );
	}
	
	public UltraSwipeScroller( Context context , Interpolator interpolator )
	{
		super( context , interpolator );
	}
	
	/**
	 * Set the factor by which the duration will change
	 */
	public void setScrollDurationFactor( double scrollFactor )
	{
		mScrollFactor = scrollFactor;
	}
	
	@Override
	public void startScroll( int startX , int startY , int dx , int dy , int duration )
	{
		super.startScroll( startX , startY , dx , dy , (int) ( duration * mScrollFactor ) );
	}
}
