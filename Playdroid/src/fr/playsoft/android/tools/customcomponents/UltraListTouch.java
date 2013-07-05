package fr.playsoft.android.tools.customcomponents;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import fr.playsoft.android.tools.customcomponents.interfaces.ITouchListListener;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListDataProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListDrawableProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListFastScrollLetters;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListOnClickListener;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListPaginationListener;

/**
 * UltraList with drag&drop & slide to remove effects.
 */
public class UltraListTouch extends UltraList
{
	public static final int REMOVE_MODE_NONE = 0;
	public static final int REMOVE_MODE_SLIDE_RIGHT = 1;
	public static final int REMOVE_MODE_SLIDE_LEFT = 2;
	
	private ImageView mDragView;
	private View mDraggedItem;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mWindowParams;
	private int mDragPos; // which item is being dragged
	private int mFirstDragPos; // where was the dragged item originally
	private int mDragPoint; // at what offset inside the item did the user grab it
	private int mCoordOffset; // the difference between screen coordinates and coordinates in this view
	private int mUpperBound;
	private int mLowerBound;
	private int mHeight;
	private int mRemoveMode;
	private Rect mTempRect = new Rect();
	private Bitmap mDragBitmap;
	private final int mTouchSlop;
	private int mGrabberViewId;
	private ITouchListListener mTouchListener;
	private int mBitmapEdgeHeight; // Height of replaced edge - 5% of view height, at least 1 pixel
	private int[] mBitmapTopEdge; // Original pixels from top bitmap edge
	private int[] mBitmapBottomEdge; // Original pixels from bottom bitmap edge
	private int[] mBitmapBlackPixels; // Black pixels to insert as top/bottom edge
	
	public UltraListTouch( Context pContext , LayoutInflater pInflater , int pListId , IUltraListDataProvider pDataProvider ,
			IUltraListOnClickListener pListener , boolean pIsAutoDownloadEnabled , boolean pIsUpdatedInNewThread , ITouchListListener pTouchListener ,
			int pGrabberViewId , int pRemoveMode , IUltraListFastScrollLetters pLettersProvider ,
			IUltraListPaginationListener pUltraListPaginationListener , IUltraListDrawableProvider pDrawableProvider )
	{
		super( pContext , pInflater , pListId , pDataProvider , pListener , pIsAutoDownloadEnabled , pIsUpdatedInNewThread , pLettersProvider ,
				pUltraListPaginationListener , pDrawableProvider );
		mTouchListener = pTouchListener;
		this.setLayoutParams( new ViewGroup.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT , ViewGroup.LayoutParams.FILL_PARENT ) );
		mTouchSlop = ViewConfiguration.get( pContext ).getScaledTouchSlop();
		mGrabberViewId = pGrabberViewId;
		mRemoveMode = pRemoveMode;
	}
	
	/**
	 * Custom handler for touch/move/up events when dragging an item.
	 * 
	 * @param ev MotionEvent instance.
	 * @return True if event is handled.
	 */
	private boolean customTouchHandle( MotionEvent ev )
	{
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		int itemnum = pointToPosition( x , y );
		int action = ev.getAction();
		
		switch ( action )
		{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				if ( mDragView == null )
				{
					break;
				}
				
				dragView( x , y );
				
				if ( itemnum >= 0 )
				{
					if ( action == MotionEvent.ACTION_DOWN || itemnum != mDragPos )
					{
						onDragged( mDragPos , itemnum );
						mDragPos = itemnum;
					}
					int speed = 0;
					adjustScrollBounds( y );
					if ( y > mLowerBound )
					{
						// scroll the list up a bit
						speed = y > ( mHeight + mLowerBound ) / 2 ? 16 : 4;
					}
					else if ( y < mUpperBound )
					{
						// scroll the list down a bit
						speed = y < mUpperBound / 2 ? -16 : -4;
					}
					if ( speed != 0 )
					{
						int ref = pointToPosition( 0 , mHeight / 2 );
						if ( ref == AdapterView.INVALID_POSITION )
						{
							// we hit a divider or an invisible view, check somewhere else
							ref = pointToPosition( 0 , mHeight / 2 + getDividerHeight() + 64 );
						}
						View v = getChildAt( ref - getFirstVisiblePosition() );
						if ( v != null )
						{
							int pos = v.getTop();
							setSelectionFromTop( ref , pos - speed );
						}
					}
				}
				return true;
				
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				if ( mDragView == null )
				{
					break;
				}
				
				Rect r = mTempRect;
				mDragView.getDrawingRect( r );
				stopDragging();
				
				if ( mRemoveMode == REMOVE_MODE_SLIDE_RIGHT && ev.getX() > r.left + ( r.width() * 3 / 4 ) )
				{
					onRemoved( mFirstDragPos );
				}
				else if ( mRemoveMode == REMOVE_MODE_SLIDE_LEFT && ev.getX() < r.left + ( r.width() / 4 ) )
				{
					onRemoved( mFirstDragPos );
				}
				else
				{
					if ( mDragPos >= 0 && mDragPos < getCount() )
					{
						onDropped( mFirstDragPos , mDragPos );
					}
				}
				return true;
		}
		
		return false;
	}
	
	@Override
	public boolean onInterceptTouchEvent( MotionEvent ev )
	{
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		int itemnum = pointToPosition( x , y );
		int action = ev.getAction();
		
		switch ( action )
		{
			case MotionEvent.ACTION_DOWN:
				if ( itemnum == AdapterView.INVALID_POSITION )
				{
					break;
				}
				
				View item = getChildAt( itemnum - getFirstVisiblePosition() );
				
				if ( isDraggableRow( item ) )
				{
					mDragPoint = y - item.getTop();
					mCoordOffset = ( (int) ev.getRawY() ) - y;
					View dragger = item.findViewById( mGrabberViewId );
					Rect r = mTempRect;
					
					r.left = dragger.getLeft();
					r.right = dragger.getRight();
					r.top = dragger.getTop();
					r.bottom = dragger.getBottom();
					
					if ( ( r.left < x ) && ( x < r.right ) && ( r.top < mDragPoint ) && ( mDragPoint < r.bottom ) )
					{
						item.setDrawingCacheEnabled( true );
						// Create a copy of the drawing cache so that it does not get recycled
						// by the framework when the list tries to clean up memory
						Bitmap bitmap = Bitmap.createBitmap( item.getDrawingCache() );
						mBitmapEdgeHeight = Math.max( 1 , ( ( bitmap.getHeight() * 5 ) / 100 ) );
						mBitmapTopEdge = new int[ bitmap.getWidth() * mBitmapEdgeHeight ];
						mBitmapBottomEdge = new int[ bitmap.getWidth() * mBitmapEdgeHeight ];
						mBitmapBlackPixels = new int[ bitmap.getWidth() * mBitmapEdgeHeight ];
						bitmap.getPixels( mBitmapTopEdge , 0 , bitmap.getWidth() , 0 , 0 , bitmap.getWidth() , mBitmapEdgeHeight );
						bitmap.getPixels( mBitmapBottomEdge , 0 , bitmap.getWidth() , 0 , bitmap.getHeight() - mBitmapEdgeHeight , bitmap.getWidth() ,
								mBitmapEdgeHeight );
						
						Rect listBounds = new Rect();
						
						getGlobalVisibleRect( listBounds , null );
						mDraggedItem = item;
						startDragging( bitmap , listBounds.left , y );
						mDragPos = itemnum;
						mFirstDragPos = mDragPos;
						mHeight = getHeight();
						int touchSlop = mTouchSlop;
						mUpperBound = Math.min( y - touchSlop , mHeight / 3 );
						mLowerBound = Math.max( y + touchSlop , mHeight * 2 / 3 );
						return false;
					}
					
					mDragView = null;
				}
				
				break;
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				if ( customTouchHandle( ev ) )
				{
					return true;
				}
				break;
		}
		return super.onInterceptTouchEvent( ev );
	}
	
	protected boolean isDraggableRow( View view )
	{
		return ( view.findViewById( mGrabberViewId ) != null );
	}
	
	private void onDragged( int pFrom , int pTo )
	{
		if ( ( pFrom == pTo ) || ( pTo == mFirstDragPos ) )
		{
			mDragBitmap.setPixels( mBitmapBlackPixels , 0 , mDragBitmap.getWidth() , 0 , 0 , mDragBitmap.getWidth() , mBitmapEdgeHeight );
			mDragBitmap.setPixels( mBitmapBlackPixels , 0 , mDragBitmap.getWidth() , 0 , mDragBitmap.getHeight() - mBitmapEdgeHeight ,
					mDragBitmap.getWidth() , mBitmapEdgeHeight );
			mDragView.invalidate();
		}
		else if ( pFrom < pTo )
		{
			mDragBitmap.setPixels( mBitmapTopEdge , 0 , mDragBitmap.getWidth() , 0 , 0 , mDragBitmap.getWidth() , mBitmapEdgeHeight );
			mDragBitmap.setPixels( mBitmapBlackPixels , 0 , mDragBitmap.getWidth() , 0 , mDragBitmap.getHeight() - mBitmapEdgeHeight ,
					mDragBitmap.getWidth() , mBitmapEdgeHeight );
			mDragView.invalidate();
		}
		else if ( pFrom > pTo )
		{
			mDragBitmap.setPixels( mBitmapBlackPixels , 0 , mDragBitmap.getWidth() , 0 , 0 , mDragBitmap.getWidth() , mBitmapEdgeHeight );
			mDragBitmap.setPixels( mBitmapBottomEdge , 0 , mDragBitmap.getWidth() , 0 , mDragBitmap.getHeight() - mBitmapEdgeHeight ,
					mDragBitmap.getWidth() , mBitmapEdgeHeight );
			mDragView.invalidate();
		}
		
		if ( mTouchListener != null )
		{
			mTouchListener.onDragged( pFrom , pTo );
		}
	}
	
	private void onDropped( int pFrom , int pTo )
	{
		UltraListCell lCell = mAdapter.getCellData().remove( pFrom );
		mAdapter.getCellData().add( pTo , lCell );
		mAdapter.notifyDataSetChanged();
		if ( mTouchListener != null )
		{
			mTouchListener.onDropped( pFrom , pTo );
		}
	}
	
	private void onRemoved( int pWhich )
	{
		UltraListCell lCell = mAdapter.getCellData().remove( pWhich );
		mAdapter.notifyDataSetChanged();
		if ( mTouchListener != null )
		{
			mTouchListener.onRemoved( pWhich , lCell );
		}
	}
	
	private void adjustScrollBounds( int y )
	{
		if ( y >= mHeight / 3 )
		{
			mUpperBound = mHeight / 3;
		}
		if ( y <= mHeight * 2 / 3 )
		{
			mLowerBound = mHeight * 2 / 3;
		}
	}
	
	@Override
	public boolean onTouchEvent( MotionEvent ev )
	{
		if ( mDragView != null )
		{
			customTouchHandle( ev );
			return true;
		}
		return super.onTouchEvent( ev );
	}
	
	private void startDragging( Bitmap bm , int x , int y )
	{
		stopDragging();
		
		mWindowParams = new WindowManager.LayoutParams();
		mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowParams.x = x;
		mWindowParams.y = y - mDragPoint + mCoordOffset;
		
		mWindowParams.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		mWindowParams.width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		mWindowParams.format = PixelFormat.TRANSLUCENT;
		mWindowParams.windowAnimations = 0;
		
		mDragBitmap = bm;
		ImageView v = new ImageView( getContext() );
		v.setBackgroundColor( 0xff000000 );
		v.setAlpha( 200 );
		v.setImageBitmap( mDragBitmap );
		
		mWindowManager = (WindowManager) getContext().getSystemService( "window" );
		mWindowManager.addView( v , mWindowParams );
		mDragView = v;
	}
	
	private void dragView( int x , int y )
	{
		float alpha = 1.0f;
		int width = mDragView.getWidth();
		
		if ( mRemoveMode == REMOVE_MODE_SLIDE_RIGHT )
		{
			if ( x > width / 2 )
			{
				alpha = ( (float) ( width - x ) ) / ( width / 2 );
			}
		}
		else if ( mRemoveMode == REMOVE_MODE_SLIDE_LEFT )
		{
			if ( x < width / 2 )
			{
				alpha = ( (float) x ) / ( width / 2 );
			}
			
		}
		alpha = alpha * 0.7f;
		mWindowParams.alpha = alpha;
		mWindowParams.y = y - mDragPoint + mCoordOffset;
		mWindowManager.updateViewLayout( mDragView , mWindowParams );
	}
	
	private void stopDragging()
	{
		if ( mDraggedItem != null )
		{
			mDraggedItem.setDrawingCacheEnabled( false );
			mDraggedItem = null;
		}
		if ( mDragView != null )
		{
			WindowManager wm = (WindowManager) getContext().getSystemService( "window" );
			wm.removeView( mDragView );
			mDragView.setImageDrawable( null );
			mDragView = null;
		}
		if ( mDragBitmap != null )
		{
			mDragBitmap.recycle();
			mDragBitmap = null;
		}
	}
}
