package fr.playsoft.android.tools.customcomponents;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import fr.playsoft.android.tools.customcomponents.interfaces.IRefreshable;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraSwipeCellOnClickListener;

/**
 * UltraList / UltraGridCell which implements UltraSwipe component
 * 
 * @author Klawikowski
 * 
 */
public class UltraSwipeListCell extends ViewPager implements IRefreshable
{
	
	/** Handler **/
	private Handler mHandler;
	
	/** Timeout to start automatic swipe after users last touch event **/
	public static final int TIMEOUT_RESUME_AUTOMATIC_SWIPE_AFTER_TOUCH = 2000;
	
	/** Time to take until automatic swipe **/
	public static final int TIMEOUT_AUTOMATIC_SWIPE = 2000;
	
	/** Active Indicator Colour **/
	private static final int ACTIVE_COLOUR = 0xff000000;
	/** Inactive Indicator Colour **/
	private static final int INACTIVE_COLOUR = 0xffffffff;
	
	/** Dot page indicator **/
	private UltraSwipeIndicator mPageIndicator;
	
	/** Our instance **/
	private UltraSwipeListCell mInstance;
	
	/** Context **/
	private Context mContext;
	
	/** Custom Adapter **/
	private UltraSwipeAdapter mAdapter;
	private int mCurrentPosition;
	private int mVirtualPosition;
	private boolean mIsStartup = true;
	private boolean mIsAutomaticSwipe;
	private IUltraSwipeCellOnClickListener mSwipeOnClickListener;
	private Timer mTimer;
	private Timer mTimerTouch;
	private TimerTask mTimerTouchTask;
	private boolean mIsTouchModeEnabled = false;
	private LayoutInflater mInflater;
	
	public UltraSwipeListCell( Context context , UltraSwipeIndicator pPageIndicator , SwipeViewDescriptor lSwipeViewDescriptor )
	{
		super( context );
		mContext = context;
		mHandler = new Handler();
	}
	
	public UltraSwipeListCell( Context context , AttributeSet attrs )
	{
		super( context , attrs );
		mContext = context;
		mHandler = new Handler();
	}
	
	public int getCurrentPosition()
	{
		return mCurrentPosition;
	}
	
	public void init( UltraSwipeIndicator pPageIndicator , SwipeViewDescriptor lSwipeViewDescriptor )
	{
		if ( mIsStartup )
		{
			mInstance = this;
			mInstance.setOnPageChangeListener( new OnPageChangeListener()
			{
				
				@Override
				public void onPageSelected( int arg0 )
				{
					mVirtualPosition = arg0;
					mCurrentPosition = arg0 % mAdapter.getRealCount();
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
			mSwipeOnClickListener = lSwipeViewDescriptor.mListener;
			mAdapter = new UltraSwipeAdapter( mContext );
			mInflater = lSwipeViewDescriptor.mInflater;
			mInstance.setAdapter( mAdapter );
			mInstance.setDrawingCacheEnabled( false );
			mAdapter.setOnClickListener( new OnClickListener()
			{
				
				@Override
				public void onClick( View v )
				{
					mSwipeOnClickListener.onSwipeCellClick( getTag() , getCurrentPosition() , mAdapter.getCurrentItem() );
				}
			} );
			mAdapter.setAdapterData( lSwipeViewDescriptor.mSwipeData );
			mInstance.setCurrentItem( mAdapter.getRealCount() + mAdapter.getCount() / 2 );
			if ( pPageIndicator != null )
			{
				setPageIndicator( pPageIndicator );
			}
			mIsStartup = false;
			mIsAutomaticSwipe = lSwipeViewDescriptor.mIsAutomaticSwipe;
			if ( mIsAutomaticSwipe )
			{
				
				mTimer = new Timer();
				mTimerTouch = new Timer();
				
				mTimer.schedule( new UpdateTimeTask() , TIMEOUT_AUTOMATIC_SWIPE , TIMEOUT_AUTOMATIC_SWIPE );
			}
		}
	}
	
	class UpdateTimeTask extends TimerTask
	{
		@Override
		public void run()
		{
			mHandler.post( new Runnable()
			{
				
				@Override
				public void run()
				{
					if ( !mIsTouchModeEnabled )
					{
						mInstance.setCurrentItem( mVirtualPosition++ , true );
					}
				}
			} );
			
		}
	}
	
	@Override
	public Object getTag()
	{
		return ( ( (UltraSwipeCell) mAdapter.getView( mCurrentPosition ) ).getTag() );
	}
	
	public void setCurrentPage( int pPosition )
	{
		if ( mPageIndicator != null )
		{
			if ( mAdapter.mViewPagerData.size() > 0 )
			{
				mPageIndicator.redrawIndicators( ACTIVE_COLOUR , INACTIVE_COLOUR );
			}
			mPageIndicator.setCurrentPage( pPosition );
		}
	}
	
	public void setPageIndicator( UltraSwipeIndicator pageIndicator )
	{
		if ( mIsStartup )
		{
			mPageIndicator = pageIndicator;
			if ( mAdapter.mViewPagerData.size() > 0 )
			{
				mPageIndicator.redrawIndicators( ACTIVE_COLOUR , INACTIVE_COLOUR );
			}
			mPageIndicator.setPageCount( mAdapter.getRealCount() );
			mPageIndicator.setCurrentPage( mCurrentPosition );
		}
	}
	
	@Override
	public void refreshViewAndData()
	{
		mInstance.getAdapter().notifyDataSetChanged();
	}
	
	@Override
	public void refreshView()
	{
		mInstance.getAdapter().notifyDataSetChanged();
	}
	
	@Override
	public boolean onInterceptTouchEvent( MotionEvent arg0 )
	{
		if ( mAdapter.mViewPagerData.size() == 0 )
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
	
	@Override
	public boolean onTouchEvent( MotionEvent arg0 )
	{
		if ( arg0.getAction() != MotionEvent.ACTION_UP )
		{
			mIsTouchModeEnabled = true;
		}
		else
		{
			if ( mIsAutomaticSwipe )
			{
				
				mTimerTouch = new Timer();
				
				mTimerTouchTask = new TimerTask()
				{
					
					@Override
					public void run()
					{
						mIsTouchModeEnabled = false;
					}
				};
				mTimerTouch.schedule( mTimerTouchTask , TIMEOUT_RESUME_AUTOMATIC_SWIPE_AFTER_TOUCH );
			}
			
		}
		return super.onTouchEvent( arg0 );
	}
	
	public class UltraSwipeAdapter extends PagerAdapter implements IRefreshable
	{
		
		public static final String TAG = "UltraViewPagerAdapter";
		private final Context mContext;
		private ArrayList< UltraSwipeCell > mViewPagerData = new ArrayList< UltraSwipeCell >();
		private OnClickListener mOnClickListener;
		
		public UltraSwipeAdapter( Context context )
		{
			mContext = context;
			
		}
		
		public void setOnClickListener( OnClickListener pListener )
		{
			mOnClickListener = pListener;
		}
		
		@SuppressWarnings( "unchecked" )
		public void setAdapterData( ArrayList< ? > pViewData )
		{
			mViewPagerData = (ArrayList< UltraSwipeCell >) pViewData;
		}
		
		public UltraSwipeCell getCurrentItem()
		{
			if ( mViewPagerData.size() > 0 )
			{
				return ( mViewPagerData.get( mInstance.mCurrentPosition ) );
			}
			return null;
		}
		
		public Object getView( int pPosition )
		{
			return mViewPagerData.get( pPosition );
		}
		
		public int getRealCount()
		{
			if ( mViewPagerData.size() == 0 )
			{
				return 1;
			}
			return mViewPagerData.size();
		}
		
		@Override
		public int getCount()
		{
			return getRealCount() * 100;
			// return mViewPagerData.size();
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
				( (ViewPager) pager ).addView( lCurrentView , 0 );
				return lCurrentView;
			}
			RelativeLayout.LayoutParams lParams = new RelativeLayout.LayoutParams( android.view.ViewGroup.LayoutParams.FILL_PARENT ,
					android.view.ViewGroup.LayoutParams.FILL_PARENT );
			RelativeLayout lLayout = new RelativeLayout( mContext );
			lLayout.setLayoutParams( lParams );
			TextView lTextView = new TextView( mContext );
			lTextView.setLayoutParams( lParams );
			lTextView.setGravity( Gravity.CENTER );
			lTextView.setTextColor( 0xff000000 );
			lLayout.addView( lTextView );
			lTextView.setText( "No data available" );
			( (ViewPager) pager ).addView( lLayout , 0 );
			
			return lLayout;
		}
		
		@Override
		public int getItemPosition( Object object )
		{
			return POSITION_NONE;
		}
		
		@Override
		public void destroyItem( View pager , int position , Object view )
		{
			( (ViewPager) pager ).removeView( (RelativeLayout) view );
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
			notifyDataSetChanged();
		}
	}
	
}
