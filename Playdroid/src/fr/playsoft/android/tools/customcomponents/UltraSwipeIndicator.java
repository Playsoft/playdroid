package fr.playsoft.android.tools.customcomponents;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * UltraSwipeIndicator
 * View which shows all possible items and selected one, as a horizontal dot bar
 * 
 * @author Klawikowski
 * 
 */
public class UltraSwipeIndicator extends LinearLayout
{
	/** Size of Indicator Size **/
	private int mIndicatorSize = 16;
	
	/** Real indicator size measured from device multiplied by indicator size **/
	private int mRealIndicatorSize;
	
	/** Selected Indicator Drawable **/
	private Drawable mActiveDrawable;
	
	/** Not Selected Indictor Drawable **/
	private Drawable mInactiveDrawable;
	
	/** List of all indicators **/
	private ArrayList< ImageView > mIndicatorsView;
	
	/** List of all pages **/
	private int mPageCount = 0;
	
	/** Current selected page index **/
	private int mCurrentPage = 0;
	
	/** Active Page Indicator color **/
	private int mActiveColor = 0xFFFFFFFF;
	
	/** Inactive Page Indicator color **/
	private int mInactiveColor = 0xFF5c5c5c;
	
	private Context mContext;
	
	public UltraSwipeIndicator( Context context , int pIndicatorSize )
	{
		super( context );
		mContext = context;
		initPageControl();
	}
	
	public UltraSwipeIndicator( Context context , AttributeSet attrs )
	{
		super( context , attrs );
		mContext = context;
	}
	
	@Override
	protected void onFinishInflate()
	{
		initPageControl();
	}
	
	public void setIndicatorColor( int pActive , int pInactive )
	{
		mActiveColor = pActive;
		mInactiveColor = pInactive;
	}
	
	/**
	 * Inialization of all controls, drawables
	 */
	public void initPageControl()
	{
		
		mIndicatorsView = new ArrayList< ImageView >();
		mRealIndicatorSize = (int) ( mIndicatorSize * getResources().getDisplayMetrics().scaledDensity / 2 );
		
		mActiveDrawable = new ShapeDrawable();
		mInactiveDrawable = new ShapeDrawable();
		
		mActiveDrawable.setBounds( 0 , 0 , mRealIndicatorSize , mRealIndicatorSize );
		mInactiveDrawable.setBounds( 0 , 0 , mRealIndicatorSize , mRealIndicatorSize );
		
		Shape s1 = new OvalShape();
		s1.resize( mRealIndicatorSize , mRealIndicatorSize );
		
		Shape s2 = new OvalShape();
		s2.resize( mRealIndicatorSize , mRealIndicatorSize );
		
		( (ShapeDrawable) mActiveDrawable ).getPaint().setColor( mActiveColor );
		( (ShapeDrawable) mInactiveDrawable ).getPaint().setColor( mInactiveColor );
		
		( (ShapeDrawable) mActiveDrawable ).setShape( s1 );
		( (ShapeDrawable) mInactiveDrawable ).setShape( s2 );
		
	}
	
	/**
	 * Redraws Indicators,
	 * should be called when you want to change drawable colors
	 * 
	 * @param pActiveColor
	 * @param pInactiveColor
	 */
	public void redrawIndicators( int pActiveColor , int pInactiveColor )
	{
		mRealIndicatorSize = (int) ( mIndicatorSize * getResources().getDisplayMetrics().scaledDensity / 2 );
		
		mActiveDrawable = new ShapeDrawable();
		mInactiveDrawable = new ShapeDrawable();
		
		mActiveDrawable.setBounds( 0 , 0 , mRealIndicatorSize , mRealIndicatorSize );
		mInactiveDrawable.setBounds( 0 , 0 , mRealIndicatorSize , mRealIndicatorSize );
		
		Shape s1 = new OvalShape();
		s1.resize( mRealIndicatorSize , mRealIndicatorSize );
		
		Shape s2 = new OvalShape();
		s2.resize( mRealIndicatorSize , mRealIndicatorSize );
		
		( (ShapeDrawable) mActiveDrawable ).getPaint().setColor( pActiveColor );
		( (ShapeDrawable) mInactiveDrawable ).getPaint().setColor( pInactiveColor );
		
		( (ShapeDrawable) mActiveDrawable ).setShape( s1 );
		( (ShapeDrawable) mInactiveDrawable ).setShape( s2 );
	}
	
	/**
	 * Set the drawable object for an active page indicator
	 * 
	 * @param d
	 *            The drawable object for an active page indicator
	 */
	public void setActiveDrawable( Drawable d )
	{
		mActiveDrawable = d;
		
		mIndicatorsView.get( mCurrentPage ).setBackgroundDrawable( mActiveDrawable );
		
	}
	
	/**
	 * Return the current drawable object for an active page indicator
	 * 
	 * @return Returns the current drawable object for an active page indicator
	 */
	public Drawable getActiveDrawable()
	{
		return mActiveDrawable;
	}
	
	/**
	 * Set the drawable object for an inactive page indicator
	 * 
	 * @param d
	 *            The drawable object for an inactive page indicator
	 */
	public void setInactiveDrawable( Drawable d )
	{
		mInactiveDrawable = d;
		
		for ( int i = 0; i < mPageCount; i++ )
		{
			mIndicatorsView.get( i ).setBackgroundDrawable( mInactiveDrawable );
		}
		
		mIndicatorsView.get( mCurrentPage ).setBackgroundDrawable( mActiveDrawable );
	}
	
	/**
	 * Return the current drawable object for an inactive page indicator
	 * 
	 * @return Returns the current drawable object for an inactive page
	 *         indicator
	 */
	public Drawable getInactiveDrawable()
	{
		return mInactiveDrawable;
	}
	
	/**
	 * Set the number of pages this PageControl should have
	 * 
	 * @param pageCount
	 *            The number of pages this PageControl should have
	 */
	public void setPageCount( int pageCount )
	{
		mPageCount = pageCount;
		removeAllViews();
		for ( int i = 0; i < pageCount; i++ )
		{
			final ImageView imageView = new ImageView( mContext );
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( mRealIndicatorSize , mRealIndicatorSize );
			params.setMargins( mRealIndicatorSize / 2 , mRealIndicatorSize , mRealIndicatorSize / 2 , mRealIndicatorSize );
			imageView.setLayoutParams( params );
			imageView.setBackgroundDrawable( mInactiveDrawable );
			
			mIndicatorsView.add( imageView );
			addView( imageView );
		}
	}
	
	/**
	 * Return the number of pages this PageControl has
	 * 
	 * @return Returns the number of pages this PageControl has
	 */
	public int getPageCount()
	{
		return mPageCount;
	}
	
	/**
	 * Set the current page the PageControl should be on
	 * 
	 * @param currentPage
	 *            The current page the PageControl should be on
	 */
	public void setCurrentPage( int currentPage )
	{
		if ( currentPage < mPageCount )
		{
			mIndicatorsView.get( mCurrentPage ).setBackgroundDrawable( mInactiveDrawable );// reset old
																							// indicator
			mIndicatorsView.get( currentPage ).setBackgroundDrawable( mActiveDrawable );// set
			// up
			// new
			// indicator
			mCurrentPage = currentPage;
		}
	}
	
	/**
	 * Return the current page the PageControl is on
	 * 
	 * @return Returns the current page the PageControl is on
	 */
	public int getCurrentPage()
	{
		return mCurrentPage;
	}
	
	/**
	 * Set the size of the page indicator drawables
	 * 
	 * @param indicatorSize
	 *            The size of the page indicator drawables
	 */
	public void setIndicatorSize( int indicatorSize )
	{
		mIndicatorSize = indicatorSize;
		for ( int i = 0; i < mPageCount; i++ )
		{
			mIndicatorsView.get( i ).setLayoutParams( new LayoutParams( mRealIndicatorSize , mRealIndicatorSize ) );
		}
	}
	
	/**
	 * Return the size of the page indicator drawables
	 * 
	 * @return Returns the size of the page indicator drawables
	 */
	public int getIndicatorSize()
	{
		return mIndicatorSize;
	}
}
