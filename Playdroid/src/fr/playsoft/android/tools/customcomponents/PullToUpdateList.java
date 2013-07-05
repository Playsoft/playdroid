package fr.playsoft.android.tools.customcomponents;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import fr.playsoft.android.tools.customcomponents.interfaces.IPullToUpdateListener;
import fr.playsoft.android.tools.customcomponents.interfaces.IRefreshable;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListDataProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListDrawableProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListFastScrollLetters;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListOnClickListener;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListPaginationListener;

/**
 * An UltraList implementation that uses "pull to update" mechanism.
 * 
 * @author Olgierd Uzieblo
 */
public class PullToUpdateList extends UltraList implements IRefreshable , OnScrollListener
{
	/** Listener for pull to update requests, can be null **/
	private IPullToUpdateListener mPullToUpdateListener;
	
	/** States of pull to refresh mechanics **/
	private static final int HIDDEN = 0;
	private static final int PULL_TO_REFRESH = 1;
	private static final int RELEASE_TO_REFRESH = 2;
	private static final int REFRESHING = 3;
	
	/**
	 * Listener that will receive notifications every time the list scrolls.
	 */
	private OnScrollListener mOnScrollListener;
	
	/** PullToUpdate view **/
	private View mRefreshView;
	
	/** Main message, changed depending on state **/
	private TextView mRefreshViewText;
	
	/** Pull arrow image **/
	private ImageView mRefreshViewImage;
	
	/** Drawable used by mRefreshViewImage **/
	private Drawable mRefreshArrowDrawable;
	
	/** Progress bar visible in REFRESHING state **/
	private ProgressBar mRefreshViewProgress;
	
	/** Current scroll state **/
	private int mCurrentScrollState;
	
	/** Current refresh state **/
	private int mRefreshState;
	
	/** Context used to retrieve strings **/
	private Context mContext;
	
	private RotateAnimation mFlipAnimation;
	private RotateAnimation mReverseFlipAnimation;
	
	private int mRefreshViewHeight;
	private int mRefreshOriginalTopPadding;
	private int mRefreshOriginalBottomPadding;
	private int mLastMotionY;
	
	/** String with "Pull to update" **/
	private int mPullStringId;
	
	/** String with "Release to update" **/
	private int mReleaseStringId;
	
	/** String with "Update in progress" **/
	private int mUpdateStringId;
	
	/**
	 * Creates this UltraList.
	 * 
	 * @param pContext Context to use.
	 * @param pInflater LayoutInflater to use.
	 * @param pListId List id if there are multiple lists on the screen.
	 * @param pDataProvider Data provider for this list.
	 * @param pOnClickListener On click listener.
	 * @param pOnPullToUpdateListener Listener called after performing "pull to update".
	 * @param pIsAutoDownloadEnabled True will automatically download missing images with highest priority.
	 * @param pIsUpdatedInNewThread True will always update this list in new thread. Can be false if update is fast.
	 * @param pPullHeader View with pull to update header.
	 * @param pArrowImageId Id of arrow image to animate.
	 * @param pProgressId Id of progress bar.
	 * @param pPullStringId Id of text with pull to update.
	 * @param pReleaseStringId Id of text with release to update.
	 * @param pUpdateStringId Id of text with update in progress.
	 * @param pLettersProvider Provides letters for fast scroll. Can be null.
	 * @param pDrawableProvider Provides custom drawables, can be null.
	 */
	public PullToUpdateList( Context pContext , LayoutInflater pInflater , final int pListId , final IUltraListDataProvider pDataProvider ,
			final IUltraListOnClickListener pOnClickListener , IPullToUpdateListener pOnPullToUpdateListener , boolean pIsAutoDownloadEnabled ,
			boolean pIsUpdatedInNewThread , View pPullHeader , int pArrowImageId , int pProgressId , int pTextViewId , int pPullStringId ,
			int pReleaseStringId , int pUpdateStringId , IUltraListFastScrollLetters pLettersProvider ,
			IUltraListPaginationListener pUltraListPaginationListener , IUltraListDrawableProvider pDrawableProvider )
	{
		super( pContext , pInflater , pListId , pDataProvider , pOnClickListener , pIsAutoDownloadEnabled , pIsUpdatedInNewThread , pLettersProvider ,
				pUltraListPaginationListener , pDrawableProvider );
		setVerticalFadingEdgeEnabled( false );
		mContext = pContext;
		mPullToUpdateListener = pOnPullToUpdateListener;
		mRefreshView = pPullHeader;
		mRefreshViewImage = (ImageView) mRefreshView.findViewById( pArrowImageId );
		mRefreshArrowDrawable = mRefreshViewImage.getDrawable();
		mRefreshViewProgress = (ProgressBar) mRefreshView.findViewById( pProgressId );
		mRefreshViewText = (TextView) mRefreshView.findViewById( pTextViewId );
		mPullStringId = pPullStringId;
		mReleaseStringId = pReleaseStringId;
		mUpdateStringId = pUpdateStringId;
	}
	
	/**
	 * Initializes the UltraList - must be called before using.
	 * Sets the adapter which fills the list with actual data.
	 */
	@Override
	public void initialize()
	{
		initPullToRefresh();
		super.initialize();
	}
	
	/**
	 * Initializes views.
	 */
	private void initPullToRefresh()
	{
		mFlipAnimation = new RotateAnimation( 0 , -180 , Animation.RELATIVE_TO_SELF , 0.5f , Animation.RELATIVE_TO_SELF , 0.5f );
		mFlipAnimation.setInterpolator( new LinearInterpolator() );
		mFlipAnimation.setDuration( 250 );
		mFlipAnimation.setFillAfter( true );
		mReverseFlipAnimation = new RotateAnimation( -180 , 0 , Animation.RELATIVE_TO_SELF , 0.5f , Animation.RELATIVE_TO_SELF , 0.5f );
		mReverseFlipAnimation.setInterpolator( new LinearInterpolator() );
		mReverseFlipAnimation.setDuration( 250 );
		mReverseFlipAnimation.setFillAfter( true );
		
		mRefreshOriginalTopPadding = mRefreshView.getPaddingTop();
		mRefreshOriginalBottomPadding = mRefreshView.getPaddingBottom();
		
		mRefreshState = HIDDEN;
		addHeaderView( mRefreshView );
		
		super.setOnScrollListener( this );
		
		measureView( mRefreshView );
		mRefreshViewHeight = mRefreshView.getMeasuredHeight();
		
		hidePullToRefreshMessage();
	}
	
	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		hidePullToRefreshMessage();
	}
	
	private void hidePullToRefreshMessage()
	{
		if ( isInitialized() )
		{
			if ( mRefreshState != REFRESHING )
			{
				mRefreshView.setPadding( mRefreshView.getPaddingLeft() , 0 , mRefreshView.getPaddingRight() , 0 );
				mRefreshView.setVisibility( View.GONE );
				mRefreshViewText.setVisibility( View.GONE );
			}
		}
	}
	
	@Override
	public void refreshView()
	{
		if ( isInitialized() )
		{
			mAdapter.notifyDataSetChanged();
			hidePullToRefreshMessage();
		}
	}
	
	@Override
	public void refreshViewAndData()
	{
		if ( isInitialized() )
		{
			mAdapter.reInitializeAdapter();
			hidePullToRefreshMessage();
		}
	}
	
	@Override
	public void setOnScrollListener( AbsListView.OnScrollListener l )
	{
		mOnScrollListener = l;
	}
	
	@Override
	public boolean onTouchEvent( MotionEvent event )
	{
		final int y = (int) event.getY();
		
		switch ( event.getAction() )
		{
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				if ( getFirstVisiblePosition() == 0 && mRefreshState != REFRESHING )
				{
					if ( mRefreshState == RELEASE_TO_REFRESH )
					{
						// Initiate the refresh
						mRefreshState = REFRESHING;
						resetHeaderPadding();
						
						mRefreshViewImage.setVisibility( View.GONE );
						// We need this hack, otherwise it will keep the previous drawable.
						mRefreshViewImage.setImageDrawable( null );
						mRefreshViewProgress.setVisibility( View.VISIBLE );
						
						// Set refresh view text to the refreshing label
						mRefreshViewText.setText( mContext.getResources().getString( mUpdateStringId ) );
						mRefreshState = REFRESHING;
						setSelection( 0 );
						
						onRefresh();
					}
					else
					{
						resetAndHideHeader();
						setSelection( 1 );
					}
				}
				break;
			case MotionEvent.ACTION_DOWN:
				mLastMotionY = y;
				break;
			case MotionEvent.ACTION_MOVE:
				applyHeaderPadding( event );
				break;
		}
		return super.onTouchEvent( event );
	}
	
	private void applyHeaderPadding( MotionEvent ev )
	{
		final int historySize = ev.getHistorySize();
		int pointerCount = ev.getPointerCount();
		
		for ( int h = 0; h < historySize; h++ )
		{
			for ( int p = 0; p < pointerCount; p++ )
			{
				if ( mRefreshState != REFRESHING )
				{
					int historicalY = (int) ev.getHistoricalY( p , h );
					
					// Calculate the padding to apply, we divide by 3 to
					// simulate a more resistant effect during pull.
					int feedback = (int) ( mRefreshViewHeight * 1.5 );
					int topPadding = (int) ( ( ( historicalY - mLastMotionY - feedback ) - mRefreshViewHeight ) / 1.5 );
					topPadding = Math.min( topPadding , (int) ( 0.8 * mRefreshViewHeight ) );
					mRefreshView.setPadding( mRefreshView.getPaddingLeft() , topPadding , mRefreshView.getPaddingRight() ,
							mRefreshOriginalBottomPadding );
					mRefreshView.setVisibility( View.VISIBLE );
					mRefreshViewText.setVisibility( View.VISIBLE );
				}
			}
		}
	}
	
	/**
	 * Sets the header padding back to original size.
	 */
	private void resetHeaderPadding()
	{
		mRefreshView.setPadding( mRefreshView.getPaddingLeft() , mRefreshOriginalTopPadding , mRefreshView.getPaddingRight() ,
				mRefreshOriginalBottomPadding );
	}
	
	/**
	 * Resets the header to the original state.
	 */
	private void resetAndHideHeader()
	{
		// Set refresh view text to the pull label
		mRefreshViewText.setText( mContext.getResources().getString( mPullStringId ) );
		// Replace refresh drawable with arrow drawable
		mRefreshViewImage.setImageDrawable( mRefreshArrowDrawable );
		// Clear the full rotation animation
		mRefreshViewImage.clearAnimation();
		// Hide progress bar and arrow.
		mRefreshViewImage.setVisibility( View.GONE );
		mRefreshViewProgress.setVisibility( View.GONE );
		
		mRefreshState = HIDDEN;
		hidePullToRefreshMessage();
	}
	
	private void measureView( View child )
	{
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if ( p == null )
		{
			p = new ViewGroup.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT );
		}
		
		int childWidthSpec = ViewGroup.getChildMeasureSpec( 0 , 0 + 0 , p.width );
		int lpHeight = p.height;
		int childHeightSpec;
		if ( lpHeight > 0 )
		{
			childHeightSpec = MeasureSpec.makeMeasureSpec( lpHeight , MeasureSpec.EXACTLY );
		}
		else
		{
			childHeightSpec = MeasureSpec.makeMeasureSpec( 0 , MeasureSpec.UNSPECIFIED );
		}
		child.measure( childWidthSpec , childHeightSpec );
	}
	
	@Override
	public void onScroll( AbsListView view , int firstVisibleItem , int visibleItemCount , int totalItemCount )
	{
		// When the refresh view is completely visible, change the text to say
		// "Release to refresh..." and flip the arrow drawable.
		if ( mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL && mRefreshState != REFRESHING )
		{
			if ( firstVisibleItem == 0 )
			{
				mRefreshViewImage.setVisibility( View.VISIBLE );
				if ( ( mRefreshView.getBottom() > (int) ( 1.8 * mRefreshViewHeight ) ) )
				{
					if ( mRefreshState != RELEASE_TO_REFRESH )
					{
						mRefreshViewText.setText( mContext.getResources().getString( mReleaseStringId ) );
						mRefreshViewImage.clearAnimation();
						mRefreshViewImage.startAnimation( mFlipAnimation );
						mRefreshState = RELEASE_TO_REFRESH;
					}
				}
				else if ( mRefreshView.getBottom() < (int) ( 1.8 * mRefreshViewHeight ) )
				{
					if ( mRefreshState == RELEASE_TO_REFRESH )
					{
						mRefreshViewText.setText( mContext.getResources().getString( mPullStringId ) );
						if ( mRefreshState != HIDDEN )
						{
							mRefreshViewImage.clearAnimation();
							mRefreshViewImage.startAnimation( mReverseFlipAnimation );
						}
						mRefreshState = PULL_TO_REFRESH;
					}
				}
			}
			else
			{
				mRefreshViewImage.setVisibility( View.GONE );
				resetAndHideHeader();
			}
		}
		else if ( mCurrentScrollState == SCROLL_STATE_FLING && firstVisibleItem == 0 && mRefreshState != REFRESHING )
		{
			hidePullToRefreshMessage();
		}
		
		if ( mOnScrollListener != null )
		{
			mOnScrollListener.onScroll( view , firstVisibleItem , visibleItemCount , totalItemCount );
		}
	}
	
	@Override
	public void onScrollStateChanged( AbsListView view , int scrollState )
	{
		mCurrentScrollState = scrollState;
		
		if ( mOnScrollListener != null )
		{
			mOnScrollListener.onScrollStateChanged( view , scrollState );
		}
	}
	
	public void onRefresh()
	{
		if ( isInitialized() )
		{
			mPullToUpdateListener.onRefreshRequested( (PullToUpdateList) mInstance );
		}
	}
	
	/**
	 * Resets the list to a normal state after a refresh.
	 */
	public void onRefreshComplete()
	{
		resetAndHideHeader();
		
		// If refresh view is visible when loading completes, scroll down to
		// the next item.
		if ( mRefreshView.getBottom() > 0 )
		{
			hidePullToRefreshMessage();
			setSelection( 1 );
		}
	}
}
