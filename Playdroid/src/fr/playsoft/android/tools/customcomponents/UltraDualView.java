package fr.playsoft.android.tools.customcomponents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Scroller;
import fr.playsoft.android.tools.customcomponents.interfaces.IRefreshable;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraDualViewDataProvider;

/**
 * Ultra Dual View is a fully customizable view similiar to youtube / facebook style
 * 
 * @author Klawikowski
 * 
 */
@SuppressLint( "ViewConstructor" )
public class UltraDualView extends ViewGroup implements IRefreshable
{
	private final ContentScrollController mContentScrollController;
	private final GestureDetector mGestureDetector;
	
	/**
	 * Width of bottom view
	 */
	private int mBottomViewWidth;
	
	/**
	 * Margin for bottom view - you may want to use it to show some navigation icons in bottom view
	 */
	private int mBottomViewOffset;
	
	/**
	 * Indicates whether swiping is enabled or not.
	 */
	private boolean mIsSwipingEnabled = true;
	
	/**
	 * Indicates how long flinging will take time in milliseconds.
	 */
	private int mViewSlideDuration = 250;
	
	/**
	 * Data Provider
	 */
	private IUltraDualViewDataProvider mDataProvider;
	
	/**
	 * Containers for Top and Bottom View
	 */
	private final FrameLayout mBottomViewContainer;
	private final FrameLayout mTopViewContainer;
	
	private final Rect mContentHitRect = new Rect();
	
	private int mScreenWidth;
	
	/**
	 * Initializes UltraDualView
	 * 
	 * @param pContext Context to Use.
	 * @param pDataProvider IUltraDualViewDataProvider listener
	 * @param pBottomViewWidth Width of bottom View
	 * @param pBottomViewOffset Offset of bottom View
	 * @param pIsSwipingEnabled Boolean for enabling / disabling Swipe Gestures
	 */
	public UltraDualView( Context pContext , IUltraDualViewDataProvider pDataProvider , int pBottomViewWidth , int pBottomViewOffset ,
			boolean pIsSwipingEnabled )
	{
		this( pContext , null , pDataProvider , pBottomViewWidth , pBottomViewOffset , pIsSwipingEnabled );
		
	}
	
	public UltraDualView( Context pContext , AttributeSet attrs , IUltraDualViewDataProvider pDataProvider , int pBottomViewWidth ,
			int pBottomViewOffset , boolean pIsSwipingEnabled )
	{
		this( pContext , attrs , 0 , pDataProvider , pBottomViewWidth , pBottomViewOffset , pIsSwipingEnabled );
	}
	
	public UltraDualView( Context pContext , AttributeSet attrs , int defStyle , IUltraDualViewDataProvider pDataProvider , int pBottomViewWidth ,
			int pBottomViewOffset , boolean pIsSwipingEnabled )
	{
		super( pContext , attrs , defStyle );
		WindowManager lWindowManager = (WindowManager) pContext.getSystemService( Context.WINDOW_SERVICE );
		Display lDisplay = lWindowManager.getDefaultDisplay();
		mScreenWidth = lDisplay.getWidth();
		
		mIsSwipingEnabled = pIsSwipingEnabled;
		setLayoutParams( new LayoutParams( LayoutParams.FILL_PARENT , LayoutParams.FILL_PARENT ) );
		setClipChildren( false );
		setClipToPadding( false );
		mDataProvider = pDataProvider;
		// setting width using percentages given in constructor
		mBottomViewWidth = mScreenWidth * ( 100 - pBottomViewWidth ) / 100;
		mBottomViewOffset = mScreenWidth * ( pBottomViewOffset ) / 100;
		
		mContentScrollController = new ContentScrollController( new Scroller( pContext ) );
		mGestureDetector = new GestureDetector( pContext , mContentScrollController );
		mGestureDetector.setIsLongpressEnabled( false );
		
		mBottomViewContainer = new FrameLayout( pContext );
		mBottomViewContainer.setLayoutParams( new LayoutParams( LayoutParams.FILL_PARENT , LayoutParams.FILL_PARENT ) );
		mBottomViewContainer.setBackgroundColor( 0xff001212 );
		mBottomViewContainer.addView( pDataProvider.getBottomView() );
		
		addView( mBottomViewContainer );
		
		mTopViewContainer = new FrameLayout( pContext )
		{
			@Override
			public boolean onTouchEvent( MotionEvent event )
			{
				// prevent ray cast of touch events to actions container
				getHitRect( mContentHitRect );
				mContentHitRect.offset( -mTopViewContainer.getScrollX() , mTopViewContainer.getScrollY() );
				if ( mContentHitRect.contains( (int) event.getX() , (int) event.getY() ) )
				{
					return true;
				}
				
				return super.onTouchEvent( event );
			}
		};
		mTopViewContainer.setLayoutParams( new LayoutParams( LayoutParams.FILL_PARENT , LayoutParams.FILL_PARENT ) );
		mTopViewContainer.addView( pDataProvider.getTopView() );
		addView( mTopViewContainer );
	}
	
	@Override
	public void refreshViewAndData()
	{
		// simply removing and adding newly created view
		mBottomViewContainer.removeAllViews();
		mBottomViewContainer.addView( mDataProvider.getBottomView() );
	}
	
	@Override
	public void refreshView()
	{
	}
	
	/**
	 * Returns bottom View Container
	 * 
	 * @return
	 */
	public ViewGroup getBottomContainer()
	{
		return mBottomViewContainer;
	}
	
	/**
	 * Returns top View Container
	 * 
	 * @return
	 */
	public ViewGroup getTopContainer()
	{
		return mTopViewContainer;
	}
	
	/**
	 * Returns boolean is bottom view visible
	 * 
	 * @return
	 */
	public boolean isBottomViewVisible()
	{
		return !mContentScrollController.isContentShown();
	}
	
	/**
	 * Shows Bottom View
	 */
	public void showBottomView()
	{
		mContentScrollController.hideContent( mViewSlideDuration );
	}
	
	public boolean isTopViewShown()
	{
		return mContentScrollController.isContentShown();
	}
	
	public void showTopView()
	{
		mContentScrollController.showContent( mViewSlideDuration );
	}
	
	/**
	 * Toggles between 2 views
	 * You can implement it to Button onClickListener to make a Facebook'like feature
	 * 
	 */
	public void flipViews()
	{
		if ( isBottomViewVisible() )
		{
			showTopView();
		}
		else
		{
			showBottomView();
		}
	}
	
	/**
	 * Set View slide duration
	 * 
	 * @param duration
	 */
	public void setFlingDuration( int duration )
	{
		mViewSlideDuration = duration;
	}
	
	/**
	 * Returns View Slide Duration
	 * 
	 * @return
	 */
	public int getFlingDuration()
	{
		return mViewSlideDuration;
	}
	
	/**
	 * Returns boolean for Swiping Gesture Enabled / Disabled
	 * 
	 * @return
	 */
	public boolean isSwipingEnabled()
	{
		return mIsSwipingEnabled;
	}
	
	/**
	 * Set Swiping Gesture feature enabled / disabled
	 * 
	 * @param enabled
	 */
	public void setSwipingEnabled( boolean enabled )
	{
		mIsSwipingEnabled = enabled;
	}
	
	@Override
	public boolean onTouchEvent( MotionEvent event )
	{
		// return true always as far we should handle touch event for swiping
		if ( mIsSwipingEnabled )
		{
			return true;
		}
		return super.onTouchEvent( event );
	}
	
	@Override
	public boolean dispatchTouchEvent( MotionEvent ev )
	{
		if ( !mIsSwipingEnabled )
		{
			return super.dispatchTouchEvent( ev );
		}
		
		final int action = ev.getAction();
		// if current touch event should be handled
		if ( mContentScrollController.isHandled() && action == MotionEvent.ACTION_UP )
		{
			mContentScrollController.onUp( ev );
			return false;
		}
		
		if ( mGestureDetector.onTouchEvent( ev ) || mContentScrollController.isHandled() )
		{
			clearPressedState( this );
			return false;
		}
		
		return super.dispatchTouchEvent( ev );
	}
	
	@Override
	protected void onMeasure( int widthMeasureSpec , int heightMeasureSpec )
	{
		final int width = MeasureSpec.getSize( widthMeasureSpec );
		
		final int childrenCount = getChildCount();
		for ( int i = 0; i < childrenCount; ++i )
		{
			final View v = getChildAt( i );
			if ( v == mBottomViewContainer )
			{
				// setting size of actions according to spacing parameters
				mBottomViewContainer.measure( MeasureSpec.makeMeasureSpec( width - mBottomViewWidth , MeasureSpec.EXACTLY ) , heightMeasureSpec );
			}
			else if ( v == mTopViewContainer )
			{
				final int contentWidth;
				contentWidth = MeasureSpec.getSize( widthMeasureSpec ) - mBottomViewOffset;
				
				v.measure( MeasureSpec.makeMeasureSpec( contentWidth , MeasureSpec.EXACTLY ) , heightMeasureSpec );
			}
			else
			{
				v.measure( widthMeasureSpec , heightMeasureSpec );
			}
		}
		
		super.onMeasure( widthMeasureSpec , heightMeasureSpec );
	}
	
	@Override
	protected void onLayout( boolean changed , int l , int t , int r , int b )
	{
		// putting every child view to top-left corner
		final int childrenCount = getChildCount();
		for ( int i = 0; i < childrenCount; ++i )
		{
			final View v = getChildAt( i );
			if ( v == mTopViewContainer )
			{
				v.layout( l + mBottomViewOffset , t , l + mBottomViewOffset + v.getMeasuredWidth() , t + v.getMeasuredHeight() );
			}
			else
			{
				v.layout( l , t , l + v.getMeasuredWidth() , t + v.getMeasuredHeight() );
			}
		}
	}
	
	@Override
	protected void onSizeChanged( int w , int h , int oldw , int oldh )
	{
		super.onSizeChanged( w , h , oldw , oldh );
		
		// set correct position of content view after view size was changed
		if ( w != oldw || h != oldh )
		{
			mContentScrollController.init();
		}
	}
	
	/**
	 * Clears pressed state for all views hierarchy starting from parent view.
	 * 
	 * @param parent
	 *            - parent view
	 * @return true is press state was cleared
	 */
	private static boolean clearPressedState( ViewGroup parent )
	{
		if ( parent.isPressed() )
		{
			parent.setPressed( false );
			return true;
		}
		
		final int count = parent.getChildCount();
		for ( int i = 0; i < count; ++i )
		{
			final View v = parent.getChildAt( i );
			if ( v.isPressed() )
			{
				v.setPressed( false );
				return true;
			}
			
			if ( !( v instanceof ViewGroup ) )
			{
				continue;
			}
			
			final ViewGroup vg = (ViewGroup) v;
			if ( clearPressedState( vg ) )
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Used to handle scrolling events and scroll content container on top of
	 * actions one.
	 * 
	 * @author Klawikowski
	 * 
	 */
	private class ContentScrollController implements GestureDetector.OnGestureListener , Runnable
	{
		/**
		 * Used to auto-scroll to closest bound on touch up event.
		 */
		private final Scroller mScroller;
		
		// using Boolean object to initialize while first scroll event
		private Boolean mHandleEvent = null;
		
		private int mLastFlingX = 0;
		
		/**
		 * Indicates whether we need initialize position of view after measuring
		 * is finished.
		 */
		private boolean isContentShown = true;
		
		public ContentScrollController( Scroller scroller )
		{
			mScroller = scroller;
		}
		
		/**
		 * Initializes visibility of content after views measuring is finished.
		 */
		public void init()
		{
			
			if ( isContentShown )
			{
				showContent( 0 );
			}
			else
			{
				hideContent( 0 );
			}
		}
		
		/**
		 * Returns handling lock value. It indicates whether all events should
		 * be marked as handled.
		 * 
		 * @return
		 */
		public boolean isHandled()
		{
			return mHandleEvent != null && mHandleEvent;
		}
		
		@Override
		public boolean onDown( MotionEvent e )
		{
			mHandleEvent = null;
			reset();
			return false;
		}
		
		public boolean onUp( MotionEvent e )
		{
			if ( !isHandled() )
			{
				return false;
			}
			
			mHandleEvent = null;
			completeScrolling();
			return true;
		}
		
		@Override
		public boolean onSingleTapUp( MotionEvent e )
		{
			return false;
		}
		
		@Override
		public void onShowPress( MotionEvent e )
		{
			// No-op
		}
		
		@Override
		public boolean onScroll( MotionEvent e1 , MotionEvent e2 , float distanceX , float distanceY )
		{
			
			// if there is first scroll event after touch down
			if ( mHandleEvent == null )
			{
				if ( Math.abs( distanceX ) < Math.abs( distanceY ) )
				{
					// if first event is more scroll by Y axis than X one
					// ignore all events until event up
					mHandleEvent = Boolean.FALSE;
					return mHandleEvent;
				}
				else
				{
					// handle all events of scrolling by X axis
					mHandleEvent = Boolean.TRUE;
					scrollBy( (int) distanceX );
				}
			}
			else if ( mHandleEvent )
			{
				// it is not first event we should handle as scrolling by X axis
				scrollBy( (int) distanceX );
			}
			
			return mHandleEvent;
		}
		
		@Override
		public void onLongPress( MotionEvent e )
		{
			// No-op
		}
		
		@Override
		public boolean onFling( MotionEvent e1 , MotionEvent e2 , float velocityX , float velocityY )
		{
			// does not work because onDown() method returns false always
			return false;
		}
		
		/**
		 * Scrolling content view according by given value.
		 * 
		 * @param dx
		 */
		private void scrollBy( int dx )
		{
			final int x = mTopViewContainer.getScrollX();
			
			final int scrollBy;
			if ( dx < 0 )
			{ // scrolling right
				final int rightBound = getRightBound();
				if ( x + dx < -rightBound )
				{
					scrollBy = -rightBound - x;
				}
				else
				{
					scrollBy = dx;
				}
			}
			else
			{ // scrolling left
				// don't scroll if we are at left bound
				if ( x == 0 )
				{
					return;
				}
				
				if ( x + dx > 0 )
				{
					scrollBy = -x;
				}
				else
				{
					scrollBy = dx;
				}
			}
			
			mTopViewContainer.scrollBy( scrollBy , 0 );
		}
		
		public boolean isContentShown()
		{
			final int x;
			if ( !mScroller.isFinished() )
			{
				x = mScroller.getFinalX();
			}
			else
			{
				x = mTopViewContainer.getScrollX();
			}
			
			return x == 0;
		}
		
		public void hideContent( int duration )
		{
			
			isContentShown = false;
			if ( mTopViewContainer.getMeasuredWidth() == 0 || mTopViewContainer.getMeasuredHeight() == 0 )
			{
				return;
			}
			
			final int startX = mTopViewContainer.getScrollX();
			final int dx = getRightBound() + startX;
			fling( startX , dx , duration );
		}
		
		public void showContent( int duration )
		{
			isContentShown = true;
			if ( mTopViewContainer.getMeasuredWidth() == 0 || mTopViewContainer.getMeasuredHeight() == 0 )
			{
				return;
			}
			
			final int startX = mTopViewContainer.getScrollX();
			final int dx = startX;
			fling( startX , dx , duration );
		}
		
		/**
		 * Starts auto-scrolling to bound which is closer to current position.
		 */
		private void completeScrolling()
		{
			final int startX = mTopViewContainer.getScrollX();
			
			final int rightBound = getRightBound();
			final int middle = -rightBound / 2;
			if ( startX > middle )
			{
				showContent( mViewSlideDuration );
			}
			else
			{
				hideContent( mViewSlideDuration );
			}
		}
		
		private void fling( int startX , int dx , int duration )
		{
			reset();
			
			if ( dx == 0 )
			{
				return;
			}
			
			if ( duration <= 0 )
			{
				mTopViewContainer.scrollBy( -dx , 0 );
				return;
			}
			
			mScroller.startScroll( startX , 0 , dx , 0 , duration );
			
			mLastFlingX = startX;
			mTopViewContainer.post( this );
		}
		
		/**
		 * Processes auto-scrolling to bound which is closer to current
		 * position.
		 */
		@Override
		public void run()
		{
			if ( mScroller.isFinished() )
			{
				return;
			}
			
			final boolean more = mScroller.computeScrollOffset();
			final int x = mScroller.getCurrX();
			final int diff = mLastFlingX - x;
			if ( diff != 0 )
			{
				mTopViewContainer.scrollBy( diff , 0 );
				mLastFlingX = x;
			}
			
			if ( more )
			{
				mTopViewContainer.post( this );
			}
		}
		
		/**
		 * Resets scroller controller. Stops flinging on current position.
		 */
		public void reset()
		{
			if ( !mScroller.isFinished() )
			{
				mScroller.forceFinished( true );
			}
		}
		
		/**
		 * Returns right bound (limit) for scroller.
		 * 
		 * @return right bound (limit) for scroller.
		 */
		private int getRightBound()
		{
			return getWidth() - mBottomViewWidth - mBottomViewOffset;
		}
	};
	
	public static class SavedState extends BaseSavedState
	{
		/**
		 * Indicates whether content was shown while saving state.
		 */
		private boolean isContentShown;
		
		public SavedState( Parcelable superState )
		{
			super( superState );
		}
		
		@Override
		public void writeToParcel( Parcel out , int flags )
		{
			super.writeToParcel( out , flags );
			out.writeBooleanArray( new boolean[]
			{
				isContentShown
			} );
		}
		
		public static final Parcelable.Creator< SavedState > CREATOR = new Parcelable.Creator< SavedState >()
		{
			@Override
			public SavedState[] newArray( int size )
			{
				return new SavedState[ size ];
			}
			
			@Override
			public SavedState createFromParcel( Parcel source )
			{
				return new SavedState( source );
			}
		};
		
		SavedState( Parcel in )
		{
			super( in );
			
			boolean[] showing = new boolean[ 1 ];
			in.readBooleanArray( showing );
			isContentShown = showing[ 0 ];
		}
	}
	
	@Override
	public Parcelable onSaveInstanceState()
	{
		final Parcelable superState = super.onSaveInstanceState();
		final SavedState ss = new SavedState( superState );
		ss.isContentShown = isTopViewShown();
		return ss;
	}
	
	@Override
	public void onRestoreInstanceState( Parcelable state )
	{
		if ( !( state instanceof SavedState ) )
		{
			super.onRestoreInstanceState( state );
			return;
		}
		
		final SavedState ss = (SavedState) state;
		super.onRestoreInstanceState( ss.getSuperState() );
		
		if ( ss.isContentShown )
		{
			showTopView();
		}
		else
		{
			showBottomView();
		}
	}
	
}
