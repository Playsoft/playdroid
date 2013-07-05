package fr.playsoft.android.tools.customcomponents;

import java.util.ArrayList;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import fr.playsoft.android.tools.customcomponents.interfaces.IRefreshable;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraSwipeDataProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraSwipeOnClickListener;

/**
 * UltraSwipe
 * Swipeable view displaying Gallery look a like
 * 
 * @author Klawikowski
 * 
 */
public class UltraSwipe extends ViewPager implements IRefreshable
{
	/** Page Indicator **/
	private UltraSwipeIndicator mPageIndicator;
	
	/** Our Instance **/
	private UltraSwipe mInstance;
	
	/** Context **/
	private Context mContext;
	
	/** Adapter **/
	private UltraSwipeAdapter mAdapter;
	
	/** Current Visible item position **/
	private int mCurrentPosition;
	
	/**
	 * Id Ultra Swipe
	 * Should be unique, since you might want to display some ultra swipes at same screen and you can recognize each Ultra Swipe by this ID
	 */
	private int mUltraSwipeId;
	
	/** Ultra Swipe OnClick Listener handling all onclick actions **/
	private IUltraSwipeOnClickListener mSwipeOnClickListener;
	
	private OnPageChangeListener mOnPageChangeListener;
	
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
	public UltraSwipe( Context context , LayoutInflater pInflater , int pUltraSwipeId , IUltraSwipeDataProvider pProvider ,
			IUltraSwipeOnClickListener pListener , UltraSwipeIndicator pUltraSwipeIndicator , OnPageChangeListener pOnPageChangeListener )
	{
		super( context );
		mContext = context;
		mInstance = this;
		mUltraSwipeId = pUltraSwipeId;
		mInstance.setLayoutParams( new LinearLayout.LayoutParams( android.view.ViewGroup.LayoutParams.FILL_PARENT ,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT ) );
		mAdapter = new UltraSwipeAdapter( mContext , pInflater , pProvider );
		mInstance.setAdapter( mAdapter );
		mSwipeOnClickListener = pListener;
		mOnPageChangeListener = pOnPageChangeListener;
		// we're adding ultra swipe indicator if passed in constructor
		if ( pUltraSwipeIndicator != null )
		{
			setPageIndicator( pUltraSwipeIndicator );
		}
		init();
	}
	
	public UltraSwipe( Context context , AttributeSet attrs )
	{
		super( context , attrs );
		mContext = context;
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
				mOnPageChangeListener.onPageSelected( arg0 );
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
		
		mAdapter.setOnClickListener( new OnClickListener()
		{
			
			@Override
			public void onClick( View v )
			{
				mSwipeOnClickListener.onSwipeClick( mAdapter.getCurrentItem().getTag() , getCurrentPosition() , mAdapter.getCurrentItem() );
			}
		} );
		refreshViewAndData();
		mInstance.setCurrentItem( 0 );
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
		mPageIndicator.setPageCount( mAdapter.getRealCount() );
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
	
	@Override
	public boolean onInterceptTouchEvent( MotionEvent arg0 )
	{
		if ( mAdapter.getRealCount() == 1 )
		{
			return false;
		}
		boolean ret = super.onInterceptTouchEvent( arg0 );
		if ( ret )
		{
			getParent().requestDisallowInterceptTouchEvent( true );
		}
		return ret;
	}
	
	/**
	 * Custom Adapter for Ultra Swipe
	 * 
	 * @author Klawikowski
	 * 
	 */
	public class UltraSwipeAdapter extends PagerAdapter implements IRefreshable
	{
		/** Tag for LogCat **/
		public static final String TAG = "UltraViewPagerAdapter";
		
		/** Context **/
		private final Context mContext;
		
		/** Data to display **/
		private ArrayList< UltraSwipeCell > mViewPagerData = new ArrayList< UltraSwipeCell >();
		
		/** OnClick listener to handle onclick events **/
		private OnClickListener mOnClickListener;
		
		/** DataProvider Interface for data gathering **/
		private IUltraSwipeDataProvider mDataProvider;
		
		/** LayoutInflater **/
		private LayoutInflater mInflater;
		
		public UltraSwipeAdapter( Context context , LayoutInflater pInflater , IUltraSwipeDataProvider pProvider )
		{
			mContext = context;
			mDataProvider = pProvider;
			mInflater = pInflater;
		}
		
		public void setOnClickListener( OnClickListener pListener )
		{
			mOnClickListener = pListener;
		}
		
		/**
		 * Returns curent item from Adapter from which we are getting data from which we create views
		 * 
		 * @return UltraSwipeCell instance
		 */
		public UltraSwipeCell getCurrentItem()
		{
			return ( mViewPagerData.get( mInstance.mCurrentPosition ) );
		}
		
		/**
		 * Gets current real count of items in adapter
		 * 
		 * @return Real count of all cells in adapter.
		 */
		public int getRealCount()
		{
			return mViewPagerData.size();
		}
		
		/**
		 * Gets Virtual Count which we're using to create a loop while swiping from item X -> 0 and 0 -> Xx
		 */
		@Override
		public int getCount()
		{
			return getRealCount();
		}
		
		@Override
		public Object instantiateItem( View pager , final int position )
		{
			int lVirtualPosition = position % getRealCount();
			View lCurrentView = new View( mContext );
			if ( mViewPagerData.size() > 0 )
			{
				UltraSwipeCell lSwipeItem = mViewPagerData.get( lVirtualPosition );
				lCurrentView = lSwipeItem.createView( mInflater , this , true , lVirtualPosition );
				lCurrentView.setTag( mViewPagerData.get( lVirtualPosition ) );
				lCurrentView.setOnClickListener( mOnClickListener );
			}
			mDataProvider.onUltraSwipeCellUpdated( mUltraSwipeId , position , mViewPagerData.get( lVirtualPosition ) , lCurrentView );
			( (ViewPager) pager ).addView( lCurrentView , 0 );
			return lCurrentView;
		}
		
		@Override
		public int getItemPosition( Object object )
		{
			// hack used to refreshing data
			return POSITION_NONE;
		}
		
		@Override
		public void destroyItem( View pager , int position , Object view )
		{
			// we're removing not visible views to free memory
			( (ViewPager) pager ).removeView( (View) view );
		}
		
		@Override
		public boolean isViewFromObject( View view , Object object )
		{
			return view.equals( object );
		}
		
		@Override
		public void finishUpdate( View view )
		{
		}
		
		@Override
		public void restoreState( Parcelable p , ClassLoader c )
		{
		}
		
		@Override
		public Parcelable saveState()
		{
			return null;
		}
		
		@Override
		public void startUpdate( View view )
		{
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
				mPageIndicator.setPageCount( mAdapter.getRealCount() );
				mPageIndicator.setCurrentPage( mCurrentPosition );
			}
			notifyDataSetChanged();
		}
	}
	
}
