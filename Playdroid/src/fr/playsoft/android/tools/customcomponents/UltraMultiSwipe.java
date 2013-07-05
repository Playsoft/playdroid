package fr.playsoft.android.tools.customcomponents;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import fr.playsoft.android.tools.customcomponents.fragments.AUltraFragment;
import fr.playsoft.android.tools.customcomponents.interfaces.IRefreshable;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraMultiSwipeDataProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraMultiSwipeOnClickListener;

/**
 * UltraSwipe
 * Swipeable view displaying Gallery look a like
 * 
 * @author Klawikowski
 * 
 */
public class UltraMultiSwipe extends ViewPager implements IRefreshable
{
	/** Page Indicator **/
	private UltraSwipeIndicator mPageIndicator;
	
	/** Our Instance **/
	private UltraMultiSwipe mInstance;
	
	/** Adapter **/
	private UltraMultiSwipeAdapter mAdapter;
	
	/** Current Visible item position **/
	protected int mCurrentPosition;
	
	/**
	 * Id Ultra Swipe
	 * Should be unique, since you might want to display some ultra swipes at same screen and you can recognize each Ultra Swipe by this ID
	 */
	private int mUltraSwipeId;
	
	private UltraSwipeScroller mScroller;
	
	/**
	 * Creates UltraSwipe instance
	 * 
	 * @param context
	 * @param pInflater Layout Inflater
	 * @param pUltraSwipeId Unique Id of UltraSwipe
	 * @param pProvider IUltraSwipeDataProvider pProvider
	 * @param pListener IUltraSwipeOnClickListener pListener
	 * @param pUltraSwipeIndicator UltraSwipeIndicator
	 */
	public UltraMultiSwipe( Context context , LayoutInflater pInflater , FragmentManager pFragmentManager , int pUltraSwipeId ,
			IUltraMultiSwipeDataProvider pProvider , IUltraMultiSwipeOnClickListener pListener , UltraSwipeIndicator pUltraSwipeIndicator )
	{
		super( context );
		mInstance = this;
		mInstance.setId( 123 );
		mUltraSwipeId = pUltraSwipeId;
		mInstance.setLayoutParams( new LinearLayout.LayoutParams( android.view.ViewGroup.LayoutParams.FILL_PARENT ,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT ) );
		mAdapter = new UltraMultiSwipeAdapter( pFragmentManager , pProvider );
		mInstance.setAdapter( mAdapter );
		// we're adding ultra swipe indicator if passed in constructor
		if ( pUltraSwipeIndicator != null )
		{
			setPageIndicator( pUltraSwipeIndicator );
		}
		postInitViewPager();
		init();
	}
	
	public UltraMultiSwipe( Context context , AttributeSet attrs )
	{
		super( context , attrs );
		postInitViewPager();
	}
	
	/**
	 * Override the Scroller instance with our own class so we can change the
	 * duration
	 */
	private void postInitViewPager()
	{
		try
		{
			Class< ? > viewpager = ViewPager.class;
			Field scroller = viewpager.getDeclaredField( "mScroller" );
			scroller.setAccessible( true );
			Field interpolator = viewpager.getDeclaredField( "sInterpolator" );
			interpolator.setAccessible( true );
			
			mScroller = new UltraSwipeScroller( getContext() , (Interpolator) interpolator.get( null ) );
			scroller.set( this , mScroller );
		}
		catch( Exception e )
		{
		}
	}
	
	/**
	 * Set the factor by which the duration will change
	 */
	public void setScrollDurationFactor( double scrollFactor )
	{
		mScroller.setScrollDurationFactor( scrollFactor );
	}
	
	public int getCurrentPosition()
	{
		return mCurrentPosition;
	}
	
	/**
	 * Inialization of page indicator, onclick listener
	 */
	public void init()
	{
		
		mInstance.setOnPageChangeListener( new OnPageChangeListener()
		{
			
			@Override
			public void onPageSelected( int arg0 )
			{
				mCurrentPosition = arg0;
				setCurrentPage( mCurrentPosition );
			}
			
			@Override
			public void onPageScrolled( int arg0 , float arg1 , int arg2 )
			{
			}
			
			@Override
			public void onPageScrollStateChanged( int arg0 )
			{
			}
		} );
		refreshViewAndData();
		mInstance.setCurrentItem( mCurrentPosition );
	}
	
	/**
	 * Sets current visible page indicator
	 * 
	 * @param pPosition
	 */
	public void setCurrentPage( int pPosition )
	{
		if ( mPageIndicator != null )
		{
			mPageIndicator.setCurrentPage( pPosition );
		}
	}
	
	/**
	 * Initialization of page indicator
	 * 
	 * @param pageIndicator
	 */
	public void setPageIndicator( UltraSwipeIndicator pageIndicator )
	{
		mPageIndicator = pageIndicator;
		mPageIndicator.setPageCount( mAdapter.getCount() );
		mPageIndicator.setCurrentPage( mCurrentPosition );
	}
	
	@Override
	public void refreshViewAndData()
	{
		mAdapter.refreshViewAndData();
	}
	
	@Override
	public void refreshView()
	{
		mAdapter.refreshViewAndData();
	}
	
	/**
	 * Custom Adapter for Ultra Swipe
	 * 
	 * @author Klawikowski
	 * 
	 */
	public class UltraMultiSwipeAdapter extends FragmentPagerAdapter implements IRefreshable
	{
		public UltraMultiSwipeAdapter( FragmentManager fm , IUltraMultiSwipeDataProvider pProvider )
		{
			super( fm );
			mDataProvider = pProvider;
		}
		
		/** Tag for LogCat **/
		public static final String TAG = "UltraViewPagerAdapter";
		
		/** Data to display **/
		private ArrayList< AUltraFragment > mViewPagerData = new ArrayList< AUltraFragment >();
		
		/** DataProvider Interface for data gathering **/
		private IUltraMultiSwipeDataProvider mDataProvider;
		
		/**
		 * Gets Virtual Count which we're using to create a loop while swiping from item X -> 0 and 0 -> Xx
		 */
		@Override
		public int getCount()
		{
			return mViewPagerData.size();
		}
		
		@Override
		public void refreshView()
		{
			notifyDataSetChanged();
		}
		
		@Override
		public void refreshViewAndData()
		{
			mViewPagerData.clear();
			mViewPagerData.addAll( mDataProvider.getNewSwipeData( mUltraSwipeId ) );
			mInstance.setCurrentItem( mCurrentPosition );
			if ( mPageIndicator != null )
			{
				mPageIndicator.initPageControl();
				mPageIndicator.setPageCount( mAdapter.getCount() );
				mPageIndicator.setCurrentPage( mCurrentPosition );
			}
			notifyDataSetChanged();
		}
		
		@Override
		public Fragment getItem( int arg0 )
		{
			return mViewPagerData.get( arg0 );
		}
	}
	
}
