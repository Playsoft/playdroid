package fr.playsoft.android.tools.customcomponents;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Scroller;
import fr.playsoft.android.tools.customcomponents.interfaces.IRefreshable;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraGalleryDataProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraGalleryOnClickListener;
import fr.playsoft.android.tools.resource.DownloadTask;
import fr.playsoft.android.tools.resource.IDownloadFinishedListener;

/**
 * Universal Gallery class
 * 
 * @author Klawikowski
 * 
 */
public class UltraGallery extends AdapterView< ListAdapter > implements IRefreshable
{
	private Context mContext;
	public boolean mAlwaysOverrideTouch = true;
	protected ListAdapter mAdapter;
	private int mLeftViewIndex = -1;
	private int mRightViewIndex = 0;
	protected int mCurrentX;
	protected int mNextX;
	private int mMaxX = Integer.MAX_VALUE;
	private int mDisplayOffset = 0;
	protected Scroller mScroller;
	private GestureDetector mGesture;
	private Queue< View > mRemovedViewQueue = new LinkedList< View >();
	private OnItemSelectedListener mOnItemSelected;
	private OnItemClickListener mOnItemClicked;
	private OnItemLongClickListener mOnItemClickedLong;
	private IUltraGalleryOnClickListener mUltraGalleryOnClickListener;
	private boolean mDataChanged = false;
	private UltraGallery mInstance;
	
	public UltraGallery( Context context , AttributeSet attrs )
	{
		super( context , attrs );
		mContext = context;
	}
	
	public UltraGallery( Context context , LayoutInflater pInflater , IUltraGalleryDataProvider pProvider , IUltraGalleryOnClickListener pListener )
	{
		super( context );
		mInstance = this;
		mContext = context;
		initView();
		
		mInstance = this;
		mInstance.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT , LayoutParams.WRAP_CONTENT ) );
		mUltraGalleryOnClickListener = pListener;
		setAdapter( new UltraGalleryAdapter( mContext , pInflater , pProvider ) );
	}
	
	private synchronized void initView()
	{
		mLeftViewIndex = -1;
		mRightViewIndex = 0;
		mDisplayOffset = 0;
		mCurrentX = 0;
		mNextX = 0;
		mMaxX = Integer.MAX_VALUE;
		mScroller = new Scroller( getContext() );
		mGesture = new GestureDetector( getContext() , mOnGesture );
		
		mOnItemClicked = new OnItemClickListener()
		{
			@Override
			public void onItemClick( AdapterView< ? > arg0 , View arg1 , int arg2 , long arg3 )
			{
				mUltraGalleryOnClickListener.onUltraGalleryClick( (UltraGalleryCell) mAdapter.getItem( arg2 ) , arg2 );
			}
		};
		
		mOnItemClickedLong = new OnItemLongClickListener()
		{
			
			@Override
			public boolean onItemLongClick( AdapterView< ? > arg0 , View arg1 , int arg2 , long arg3 )
			{
				mUltraGalleryOnClickListener.onUltraGalleryLongClick( arg1 , arg2 );
				return false;
			}
		};
	}
	
	@Override
	public void setOnItemSelectedListener( AdapterView.OnItemSelectedListener listener )
	{
		mOnItemSelected = listener;
	}
	
	@Override
	public void setOnItemClickListener( AdapterView.OnItemClickListener listener )
	{
		mOnItemClicked = listener;
	}
	
	@Override
	public void setOnItemLongClickListener( android.widget.AdapterView.OnItemLongClickListener listener )
	{
		
		mOnItemClickedLong = listener;
	}
	
	private DataSetObserver mDataObserver = new DataSetObserver()
	{
		
		@Override
		public void onChanged()
		{
			synchronized( UltraGallery.this )
			{
				mDataChanged = true;
			}
			invalidate();
			requestLayout();
		}
		
		@Override
		public void onInvalidated()
		{
			reset();
			invalidate();
			requestLayout();
		}
		
	};
	
	@Override
	public ListAdapter getAdapter()
	{
		return mAdapter;
	}
	
	@Override
	public View getSelectedView()
	{
		return null;
	}
	
	@Override
	public void setAdapter( ListAdapter adapter )
	{
		if ( mAdapter != null )
		{
			// mAdapter.unregisterDataSetObserver( mDataObserver );
		}
		mAdapter = adapter;
		mAdapter.registerDataSetObserver( mDataObserver );
		reset();
	}
	
	private synchronized void reset()
	{
		initView();
		removeAllViewsInLayout();
		requestLayout();
	}
	
	@Override
	public void setSelection( int position )
	{
	}
	
	private void addAndMeasureChild( final View child , int viewPos )
	{
		LayoutParams params = child.getLayoutParams();
		if ( params == null )
		{
			params = new LayoutParams( LayoutParams.FILL_PARENT , LayoutParams.FILL_PARENT );
		}
		
		addViewInLayout( child , viewPos , params , true );
		child.measure( MeasureSpec.makeMeasureSpec( getWidth() , MeasureSpec.AT_MOST ) ,
				MeasureSpec.makeMeasureSpec( getHeight() , MeasureSpec.AT_MOST ) );
	}
	
	@Override
	protected synchronized void onLayout( boolean changed , int left , int top , int right , int bottom )
	{
		super.onLayout( changed , left , top , right , bottom );
		
		if ( mAdapter == null )
		{
			return;
		}
		
		if ( mDataChanged )
		{
			int oldCurrentX = mCurrentX;
			initView();
			removeAllViewsInLayout();
			mNextX = oldCurrentX;
			mDataChanged = false;
		}
		
		if ( mScroller.computeScrollOffset() )
		{
			int scrollx = mScroller.getCurrX();
			mNextX = scrollx;
		}
		
		if ( mNextX < 0 )
		{
			mNextX = 0;
			mScroller.forceFinished( true );
		}
		if ( mNextX > mMaxX )
		{
			mNextX = mMaxX;
			mScroller.forceFinished( true );
		}
		
		int dx = mCurrentX - mNextX;
		
		removeNonVisibleItems( dx );
		fillList( dx );
		positionItems( dx );
		
		mCurrentX = mNextX;
		
		if ( !mScroller.isFinished() )
		{
			post( new Runnable()
			{
				@Override
				public void run()
				{
					requestLayout();
				}
			} );
			
		}
	}
	
	private void fillList( final int dx )
	{
		int edge = 0;
		View child = getChildAt( getChildCount() - 1 );
		if ( child != null )
		{
			edge = child.getRight();
		}
		fillListRight( edge , dx );
		
		edge = 0;
		child = getChildAt( 0 );
		if ( child != null )
		{
			edge = child.getLeft();
		}
		fillListLeft( edge , dx );
		
	}
	
	private void fillListRight( int rightEdge , final int dx )
	{
		while( rightEdge + dx < getWidth() && mRightViewIndex < mAdapter.getCount() )
		{
			
			View child = mAdapter.getView( mRightViewIndex , mRemovedViewQueue.poll() , this );
			addAndMeasureChild( child , -1 );
			rightEdge += child.getMeasuredWidth();
			
			if ( mRightViewIndex == mAdapter.getCount() - 1 )
			{
				mMaxX = mCurrentX + rightEdge - getWidth();
			}
			mRightViewIndex++;
		}
		
	}
	
	private void fillListLeft( int leftEdge , final int dx )
	{
		while( leftEdge + dx > 0 && mLeftViewIndex >= 0 )
		{
			View child = mAdapter.getView( mLeftViewIndex , mRemovedViewQueue.poll() , this );
			addAndMeasureChild( child , 0 );
			leftEdge -= child.getMeasuredWidth();
			mLeftViewIndex--;
			mDisplayOffset -= child.getMeasuredWidth();
		}
	}
	
	private void removeNonVisibleItems( final int dx )
	{
		View child = getChildAt( 0 );
		while( child != null && child.getRight() + dx <= 0 )
		{
			mDisplayOffset += child.getMeasuredWidth();
			mRemovedViewQueue.offer( child );
			removeViewInLayout( child );
			mLeftViewIndex++;
			child = getChildAt( 0 );
			
		}
		
		child = getChildAt( getChildCount() - 1 );
		while( child != null && child.getLeft() + dx >= getWidth() )
		{
			mRemovedViewQueue.offer( child );
			removeViewInLayout( child );
			mRightViewIndex--;
			child = getChildAt( getChildCount() - 1 );
		}
	}
	
	private void positionItems( final int dx )
	{
		if ( getChildCount() > 0 )
		{
			mDisplayOffset += dx;
			int left = mDisplayOffset;
			for ( int i = 0; i < getChildCount(); i++ )
			{
				View child = getChildAt( i );
				int childWidth = child.getMeasuredWidth();
				child.layout( left , 0 , left + childWidth , child.getMeasuredHeight() );
				left += childWidth;
			}
		}
	}
	
	public synchronized void scrollTo( int x )
	{
		mScroller.startScroll( mNextX , 0 , x - mNextX , 0 );
		requestLayout();
	}
	
	@Override
	public boolean dispatchTouchEvent( MotionEvent ev )
	{
		boolean handled = mGesture.onTouchEvent( ev );
		if ( handled )
		{
			getParent().requestDisallowInterceptTouchEvent( true );
		}
		return handled;
	}
	
	protected boolean onFling( MotionEvent e1 , MotionEvent e2 , float velocityX , float velocityY )
	{
		synchronized( UltraGallery.this )
		{
			mScroller.fling( mNextX , 0 , (int) -velocityX , 0 , 0 , mMaxX , 0 , 0 );
		}
		requestLayout();
		return true;
	}
	
	protected boolean onDown( MotionEvent e )
	{
		mScroller.forceFinished( true );
		return true;
	}
	
	private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener()
	{
		
		@Override
		public boolean onDown( MotionEvent e )
		{
			return UltraGallery.this.onDown( e );
		}
		
		@Override
		public boolean onFling( MotionEvent e1 , MotionEvent e2 , float velocityX , float velocityY )
		{
			return UltraGallery.this.onFling( e1 , e2 , velocityX , velocityY );
		}
		
		@Override
		public boolean onScroll( MotionEvent e1 , MotionEvent e2 , float distanceX , float distanceY )
		{
			
			synchronized( UltraGallery.this )
			{
				mNextX += (int) distanceX;
			}
			requestLayout();
			return true;
		}
		
		@Override
		public boolean onSingleTapConfirmed( MotionEvent e )
		{
			Rect viewRect = new Rect();
			for ( int i = 0; i < getChildCount(); i++ )
			{
				View child = getChildAt( i );
				int left = child.getLeft();
				int right = child.getRight();
				int top = child.getTop();
				int bottom = child.getBottom();
				viewRect.set( left , top , right , bottom );
				if ( viewRect.contains( (int) e.getX() , (int) e.getY() ) )
				{
					if ( mOnItemClicked != null )
					{
						mOnItemClicked
								.onItemClick( UltraGallery.this , child , mLeftViewIndex + 1 + i , mAdapter.getItemId( mLeftViewIndex + 1 + i ) );
					}
					if ( mOnItemSelected != null )
					{
						mOnItemSelected.onItemSelected( UltraGallery.this , child , mLeftViewIndex + 1 + i ,
								mAdapter.getItemId( mLeftViewIndex + 1 + i ) );
					}
					break;
				}
			}
			return true;
		}
		
		@Override
		public void onLongPress( MotionEvent e )
		{
			Rect viewRect = new Rect();
			for ( int i = 0; i < getChildCount(); i++ )
			{
				View child = getChildAt( i );
				int left = child.getLeft();
				int right = child.getRight();
				int top = child.getTop();
				int bottom = child.getBottom();
				viewRect.set( left , top , right , bottom );
				if ( viewRect.contains( (int) e.getX() , (int) e.getY() ) )
				{
					if ( mOnItemClicked != null )
					{
						mOnItemClickedLong.onItemLongClick( UltraGallery.this , child , mLeftViewIndex + 1 + i ,
								mAdapter.getItemId( mLeftViewIndex + 1 + i ) );
					}
					if ( mOnItemSelected != null )
					{
						mOnItemClickedLong.onItemLongClick( UltraGallery.this , child , mLeftViewIndex + 1 + i ,
								mAdapter.getItemId( mLeftViewIndex + 1 + i ) );
					}
					break;
				}
			}
		};
	};
	
	@Override
	public void refreshView()
	{
	}
	
	@Override
	public void refreshViewAndData()
	{
		( (UltraGalleryAdapter) mAdapter ).refreshViewAndData();
	}
	
	public class UltraGalleryAdapter extends BaseAdapter implements IDownloadFinishedListener , IRefreshable
	{
		public static final String TAG = "UltraGalleryAdapter";
		private IUltraGalleryDataProvider mProvider;
		private ArrayList< UltraGalleryCell > mItems;
		private LayoutInflater mInflater;
		
		public UltraGalleryAdapter( Context context )
		{
			mItems = new ArrayList< UltraGalleryCell >();
		}
		
		public UltraGalleryAdapter( Context context , LayoutInflater pInflater , IUltraGalleryDataProvider pProvider )
		{
			this( context );
			mProvider = pProvider;
			mInflater = pInflater;
			mItems.clear();
			mItems.addAll( mProvider.getNewGalleryData() );
		}
		
		@Override
		public int getCount()
		{
			return mItems.size();
		}
		
		@Override
		public Object getItem( int position )
		{
			return mItems.get( position );
		}
		
		@Override
		public long getItemId( int position )
		{
			return position;
		}
		
		@Override
		public View getView( int position , View convertView , ViewGroup parent )
		{
			UltraGalleryCell lGalleryItem = mItems.get( position );
			if ( convertView == null )
			{
				convertView = lGalleryItem.createView( mInflater , this , true , position );
				convertView.setTag( mItems.get( position ) );
			}
			else
			{
				lGalleryItem.updateView( mInflater , convertView , mInstance , true , position );
			}
			return convertView;
		}
		
		@Override
		public boolean isDownloadCancelled( DownloadTask pTask )
		{
			return false;
		}
		
		@Override
		public void onDownloadingFinished()
		{
		}
		
		@Override
		public void onDownloadTaskSuccessful( DownloadTask pTask )
		{
			mItems.clear();
			mItems.addAll( mProvider.getNewGalleryData() );
			notifyDataSetChanged();
		}
		
		@Override
		public void onDownloadTaskFailed( DownloadTask pTask )
		{
		}
		
		@Override
		public void refreshView()
		{
			mItems.clear();
			mItems.addAll( mProvider.getNewGalleryData() );
			notifyDataSetChanged();
		}
		
		@Override
		public void refreshViewAndData()
		{
			mItems.clear();
			mItems.addAll( mProvider.getNewGalleryData() );
			notifyDataSetChanged();
		}
	}
}
