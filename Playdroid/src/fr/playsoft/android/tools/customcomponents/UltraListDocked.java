package fr.playsoft.android.tools.customcomponents;

import android.content.Context;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListDataProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListDockedHeaderProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListDrawableProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListFastScrollLetters;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListOnClickListener;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListPaginationListener;

/**
 * UltraList with a custom "dockable" header inside it.
 * 
 * @author Olgierd Uzieblo
 */
public class UltraListDocked extends UltraList
{
	private View mHeaderView;
	private boolean mHeaderViewVisible;
	
	private int mHeaderViewWidth;
	private int mHeaderViewHeight;
	
	protected DockedListAdapter mAdapter;
	
	private IUltraListDockedHeaderProvider mHeaderProvider;
	
	public UltraListDocked( Context pContext , LayoutInflater pInflater , int pListId , IUltraListDataProvider pDataProvider ,
			IUltraListOnClickListener pListener , boolean pIsAutoDownloadEnabled , boolean pIsUpdatedInNewThread ,
			IUltraListDockedHeaderProvider pHeaderProvider , IUltraListFastScrollLetters pLettersProvider ,
			IUltraListPaginationListener pUltraListPaginationListener , IUltraListDrawableProvider pDrawableProvider )
	{
		super( pContext , pInflater , pListId , pDataProvider , pListener , pIsAutoDownloadEnabled , pIsUpdatedInNewThread , pLettersProvider ,
				pUltraListPaginationListener , pDrawableProvider );
		mHeaderProvider = pHeaderProvider;
		setVerticalFadingEdgeEnabled( false );
	}
	
	@Override
	public void initialize()
	{
		if ( mIsInitialized )
		{
			return;
		}
		mAdapter.setDockedViewPosition( mHeaderProvider.getDockedHeaderPosition() );
		mHeaderView = mInflater.inflate( mHeaderProvider.getHeaderLayoutId() , this , false );
		mHeaderProvider.configureHeaderView( mHeaderView );
		requestLayout();
		mInstance.setAdapter( mAdapter );
		mIsInitialized = true;
	}
	
	@Override
	protected UltraListAdapter createAdapter()
	{
		mAdapter = new DockedListAdapter( mInflater , mDataProvider );
		return mAdapter;
	}
	
	@Override
	protected void onMeasure( int widthMeasureSpec , int heightMeasureSpec )
	{
		super.onMeasure( widthMeasureSpec , heightMeasureSpec );
		measureChild( mHeaderView , widthMeasureSpec , heightMeasureSpec );
		mHeaderViewWidth = mHeaderView.getMeasuredWidth();
		mHeaderViewHeight = mHeaderView.getMeasuredHeight();
	}
	
	@Override
	public boolean onInterceptTouchEvent( MotionEvent ev )
	{
		if ( mHeaderViewVisible )
		{
			if ( mHeaderView.dispatchTouchEvent( ev ) )
			{
				invalidate();
				return true;
			}
		}
		return super.onInterceptTouchEvent( ev );
	}
	
	@Override
	public boolean onTouchEvent( MotionEvent ev )
	{
		if ( mHeaderViewVisible )
		{
			if ( mHeaderView.dispatchTouchEvent( ev ) )
			{
				invalidate();
				return true;
			}
		}
		return super.onTouchEvent( ev );
	}
	
	@Override
	protected void onLayout( boolean changed , int left , int top , int right , int bottom )
	{
		super.onLayout( changed , left , top , right , bottom );
		mHeaderView.layout( 0 , 0 , mHeaderViewWidth , mHeaderViewHeight );
		checkHeaderVisibility( getFirstVisiblePosition() );
	}
	
	public void checkHeaderVisibility( int position )
	{
		if ( mAdapter.isHeaderVisible( position ) )
		{
			mHeaderViewVisible = true;
		}
		else
		{
			mHeaderViewVisible = false;
		}
	}
	
	@Override
	protected void dispatchDraw( Canvas canvas )
	{
		super.dispatchDraw( canvas );
		if ( mHeaderViewVisible )
		{
			drawChild( canvas , mHeaderView , getDrawingTime() );
		}
	}
	
	protected class DockedListAdapter extends UltraListAdapter
	{
		private int mDockedViewPosition;
		
		public DockedListAdapter( LayoutInflater pInflater , IUltraListDataProvider pDataProvider )
		{
			super( pInflater , pDataProvider );
		}
		
		public void setDockedViewPosition( int pDockedViewPosition )
		{
			mDockedViewPosition = pDockedViewPosition;
		}
		
		public boolean isHeaderVisible( int position )
		{
			if ( position < mDockedViewPosition )
			{
				return false;
			}
			
			return true;
		}
		
		@Override
		public void onScroll( AbsListView view , int firstVisibleItem , int visibleItemCount , int totalItemCount )
		{
			( (UltraListDocked) view ).checkHeaderVisibility( firstVisibleItem );
		}
		
		@Override
		public void onScrollStateChanged( AbsListView view , int scrollState )
		{
		}
		
		@Override
		public final View getView( int position , View convertView , ViewGroup parent )
		{
			if ( position != mDockedViewPosition )
			{
				return super.getView( position , convertView , parent );
			}
			else
			{
				View lHeaderAsCell = mInflater.inflate( mHeaderProvider.getHeaderLayoutId() , null , false );
				mHeaderProvider.configureHeaderView( lHeaderAsCell );
				return lHeaderAsCell;
			}
		}
		
		@Override
		public int getItemViewType( int position )
		{
			if ( position == mDockedViewPosition )
			{
				return super.getViewTypeCount();
			}
			else
			{
				return super.getItemViewType( position );
			}
		}
		
		@Override
		public int getViewTypeCount()
		{
			return super.getViewTypeCount() + 1;
		}
	}
}
