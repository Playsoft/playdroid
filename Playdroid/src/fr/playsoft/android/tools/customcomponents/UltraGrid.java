package fr.playsoft.android.tools.customcomponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SectionIndexer;
import fr.playsoft.android.tools.customcomponents.interfaces.IRefreshable;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraGridDataProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraGridDrawableProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraGridFastScrollLetters;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraGridOnClickListener;

/**
 * Universal grid to be used for various lists in the app.
 * 
 * @author Klawikowski
 */
public class UltraGrid extends GridView implements IRefreshable
{
	/** Our own instance **/
	protected UltraGrid mInstance;
	
	/** Our adapter **/
	protected UltraGridAdapter mAdapter;
	
	/** Our list id **/
	protected int mListId;
	
	/** Is this list initialized? List must be initialized before using. **/
	protected boolean mIsInitialized;
	
	/** Flag for controlling auto download feature **/
	protected boolean mIsAutoDownloadEnabled;
	
	/** Should update of adapter be called in a new thread **/
	protected boolean mIsUpdatedInNewThread;
	
	/** Instance of LayoutInflater **/
	protected LayoutInflater mInflater;
	
	/** Data provider for this list **/
	protected IUltraGridDataProvider mDataProvider;
	
	/** Provides letters for fast scroll **/
	protected IUltraGridFastScrollLetters mFirstLettersProvider;
	
	/** Called after cells are clicked **/
	private IUltraGridOnClickListener mUltraListOnClickListener;
	
	/** Custom drawable provider **/
	private IUltraGridDrawableProvider mDrawableProvider;
	
	/**
	 * Creates this UltraList.
	 * 
	 * @param pContext Context to use.
	 * @param pInflater LayoutInflater to use.
	 * @param pListId List id if there are multiple lists on the screen.
	 * @param pNumOfColumns List id if there are multiple lists on the screen.
	 * @param pDataProvider Data provider for this list.
	 * @param pCellClickListener On click listener.
	 * @param pIsAutoDownloadEnabled True will automatically download missing images with highest priority.
	 * @param pIsUpdatedInNewThread True will always update this list in new thread. Can be false if update is fast.
	 * @param pLettersProvider Provides letters for fast scroll.
	 * @param pDrawableProvider Provides custom drawables, can be null.
	 */
	public UltraGrid( Context pContext , LayoutInflater pInflater , final int pListId , final int pNumOfColumns ,
			final IUltraGridDataProvider pDataProvider , final IUltraGridOnClickListener pCellClickListener , boolean pIsAutoDownloadEnabled ,
			boolean pIsUpdatedInNewThread , IUltraGridFastScrollLetters pLettersProvider , IUltraGridDrawableProvider pDrawableProvider )
	{
		super( pContext );
		mIsInitialized = false;
		mIsUpdatedInNewThread = pIsUpdatedInNewThread;
		mInstance = this;
		mInstance.setNumColumns( pNumOfColumns );
		mInflater = pInflater;
		mDataProvider = pDataProvider;
		this.setId( 222 );
		mUltraListOnClickListener = pCellClickListener;
		mInstance.setLayoutParams( new LinearLayout.LayoutParams( android.view.ViewGroup.LayoutParams.FILL_PARENT ,
				android.view.ViewGroup.LayoutParams.FILL_PARENT ) );
		mListId = pListId;
		mIsAutoDownloadEnabled = pIsAutoDownloadEnabled;
		mFirstLettersProvider = pLettersProvider;
		mDrawableProvider = pDrawableProvider;
		if ( mFirstLettersProvider != null )
		{
			this.setFastScrollEnabled( true );
		}
		else
		{
			this.setFastScrollEnabled( false );
		}
		mAdapter = createAdapter();
		initialize();
	}
	
	/**
	 * Gets current data used by this list.
	 * 
	 * @return List with cells. Can be null.
	 */
	public ArrayList< UltraGridCell > getCellData()
	{
		return mAdapter.getCellData();
	}
	
	/**
	 * Sets new cell if possible and refreshes the view.
	 * 
	 * @param pCellId Id of old cell to replace.
	 * @param pNewCell New cell to insert in its place.
	 */
	public void replaceSingleCell( int pCellId , UltraGridCell pNewCell )
	{
		if ( ( mAdapter.mCellData != null ) && ( mAdapter.mCellData.size() > pCellId ) )
		{
			mAdapter.mCellData.set( pCellId , pNewCell );
			mAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * Gets info about scroll position of current top item.
	 * 
	 * @return ScrollInfo with info about top cell.
	 */
	public ScrollInfo getScrollInfo()
	{
		ScrollInfo lResult = new ScrollInfo();
		lResult.mSelectedItem = mInstance.getFirstVisiblePosition();
		View v = mInstance.getChildAt( 0 );
		lResult.mSelectedOffset = ( v == null ) ? 0 : v.getTop();
		return lResult;
	}
	
	/**
	 * Initializes the UltraList - must be called before using.
	 * Sets the adapter which fills the list with actual data.
	 */
	public void initialize()
	{
		if ( mIsInitialized )
		{
			return;
		}
		mInstance.setAdapter( mAdapter );
		mIsInitialized = true;
	}
	
	@Override
	public void setAdapter( ListAdapter adapter )
	{
		this.setOnScrollListener( (UltraGridAdapter) adapter );
		super.setAdapter( adapter );
	}
	
	protected UltraGridAdapter createAdapter()
	{
		return new UltraGridAdapter( mInflater , mDataProvider );
	}
	
	/**
	 * Creates a place holder - an uninitialized UltraList.
	 */
	public UltraGrid( Context pContext )
	{
		super( pContext );
		mIsInitialized = false;
	}
	
	/**
	 * Checks if this list is initialized.
	 * 
	 * @return True if it is.
	 */
	public boolean isInitialized()
	{
		return mIsInitialized;
	}
	
	/**
	 * Gets list id of this UltraList.
	 * 
	 * @return List id.
	 */
	public int getListId()
	{
		return mListId;
	}
	
	/**
	 * Refreshes currently visible view.
	 * To be used if for example missing images have been downloaded.
	 */
	@Override
	public void refreshView()
	{
		if ( isInitialized() )
		{
			mAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * Refreshes currently visible view and reinitializes data.
	 * Should refetch all data, to be used if there were some structure changes.
	 */
	@Override
	public void refreshViewAndData()
	{
		if ( isInitialized() )
		{
			mAdapter.reInitializeAdapter();
		}
	}
	
	@Override
	protected void onSizeChanged( int w , int h , int oldw , int oldh )
	{
		super.onSizeChanged( w , h , oldw , oldh );
		if ( mInstance.getLayoutParams().height != android.view.ViewGroup.LayoutParams.FILL_PARENT )
		{
			mInstance.post( new Runnable()
			{
				@Override
				public void run()
				{
					mInstance.setLayoutParams( new LinearLayout.LayoutParams( android.view.ViewGroup.LayoutParams.FILL_PARENT ,
							android.view.ViewGroup.LayoutParams.FILL_PARENT ) );
				}
			} );
		}
	}
	
	/**
	 * Adapter providing data for this UltraGrid.
	 * 
	 * @author Przemyslaw Klawikowski
	 */
	protected class UltraGridAdapter extends BaseAdapter implements OnScrollListener , SectionIndexer
	{
		/** LayoutInflater to use **/
		protected LayoutInflater mInflater;
		
		/** Data provider for this list **/
		private IUltraGridDataProvider mProvider;
		
		/** Handler used to post results to **/
		private Handler mHandler;
		
		/** Is adapter being initialized **/
		private boolean mIsInitializing;
		
		/** Data for all cells **/
		protected ArrayList< UltraGridCell > mCellData = new ArrayList< UltraGridCell >();
		
		/** Index storing first letters **/
		protected HashMap< String , Integer > mFirstLettersIndexer;
		
		/** Array with all first letters in current data **/
		protected String[] mSectionLetters;
		
		/**
		 * Initializes this adapter.
		 * 
		 * @param pInflater LayoutInflater to use.
		 * @param pDataProvider Data provider for this list.
		 */
		public UltraGridAdapter( LayoutInflater pInflater , IUltraGridDataProvider pDataProvider )
		{
			mInflater = pInflater;
			mProvider = pDataProvider;
			mHandler = new Handler();
			reInitializeAdapter();
		}
		
		/**
		 * Reinitializes the list - gets new data and refreshes view.
		 */
		public synchronized void reInitializeAdapter()
		{
			if ( mIsInitializing )
			{
				return;
			}
			mIsInitializing = true;
			
			if ( mIsUpdatedInNewThread )
			{
				Thread lInitializingThread = new Thread( new Runnable()
				{
					@Override
					public void run()
					{
						performUpdate();
					}
				} );
				lInitializingThread.setPriority( Thread.MIN_PRIORITY );
				lInitializingThread.start();
			}
			else
			{
				performUpdate();
			}
		}
		
		/**
		 * Performs the update - calls getNewData.
		 */
		private void performUpdate()
		{
			final ArrayList< UltraGridCell > lNewCellData = mProvider.getNewData( mListId );
			if ( mFirstLettersProvider != null )
			{
				// Preparing sections index
				mFirstLettersIndexer = new HashMap< String , Integer >();
				int lSize = lNewCellData.size();
				for ( int i = lSize - 1; i >= 0; i-- )
				{
					mFirstLettersIndexer.put( mFirstLettersProvider.getFirstLetter( lNewCellData.get( i ) , i , mListId ) , i );
				}
				
				Set< String > lKeys = mFirstLettersIndexer.keySet();
				Iterator< String > lIterator = lKeys.iterator();
				ArrayList< String > lKeyList = new ArrayList< String >();
				
				while( lIterator.hasNext() )
				{
					String key = lIterator.next();
					lKeyList.add( key );
				}
				
				Collections.sort( lKeyList );
				
				mSectionLetters = new String[ lKeyList.size() ];
				lKeyList.toArray( mSectionLetters );
			}
			if ( mIsUpdatedInNewThread )
			{
				mHandler.post( new Runnable()
				{
					@Override
					public void run()
					{
						finishUpdateAndRefreshView( lNewCellData );
					}
				} );
			}
			else
			{
				finishUpdateAndRefreshView( lNewCellData );
			}
		}
		
		private void finishUpdateAndRefreshView( ArrayList< UltraGridCell > pNewCellData )
		{
			mCellData = pNewCellData;
			mIsInitializing = false;
			if ( mFirstLettersProvider != null )
			{
				setFastScrollEnabled( false );
			}
			notifyDataSetChanged();
			if ( mFirstLettersProvider != null )
			{
				setFastScrollEnabled( true );
				mInstance
						.setLayoutParams( new LinearLayout.LayoutParams( android.view.ViewGroup.LayoutParams.FILL_PARENT , mInstance.getHeight() - 1 ) );
			}
		}
		
		@Override
		public Object getItem( int position )
		{
			return mCellData.get( position );
		}
		
		@Override
		public long getItemId( int position )
		{
			return position;
		}
		
		@Override
		public int getItemViewType( int position )
		{
			return mCellData.get( position ).getType();
		}
		
		@Override
		public int getViewTypeCount()
		{
			return UltraGridCell.getCellConfiguration().getNumberOfCellTypes();
		}
		
		@Override
		public View getView( int position , View convertView , ViewGroup parent )
		{
			UltraGridCell lCurrentCell = mCellData.get( position );
			if ( convertView == null )
			{
				convertView = lCurrentCell.createView( mInflater , mInstance , mIsAutoDownloadEnabled , mListId , position , mDrawableProvider ,
						mDataProvider );
				convertView.setOnClickListener( new OnCellClickListener( convertView ) );
				convertView.setOnLongClickListener( new OnCellLongClickListener( convertView ) );
			}
			else
			{
				lCurrentCell.updateView( convertView , mInstance , mIsAutoDownloadEnabled , mListId , position , mDrawableProvider , mDataProvider );
			}
			convertView.setTag( position );
			return convertView;
		}
		
		@Override
		public int getCount()
		{
			if ( mCellData != null )
			{
				return mCellData.size();
			}
			else
			{
				return 0;
			}
		}
		
		public ArrayList< UltraGridCell > getCellData()
		{
			return mCellData;
		}
		
		@Override
		public void onScroll( AbsListView view , int firstVisibleItem , int visibleItemCount , int totalItemCount )
		{
		}
		
		@Override
		public void onScrollStateChanged( AbsListView view , int scrollState )
		{
		}
		
		@Override
		public int getPositionForSection( int section )
		{
			if ( mFirstLettersProvider != null )
			{
				int lPosition;
				if ( section >= mSectionLetters.length )
				{
					lPosition = mCellData.size() + 1;
				}
				else
				{
					lPosition = mFirstLettersIndexer.get( mSectionLetters[ section ] );
				}
				return lPosition;
			}
			else
			{
				return 0;
			}
		}
		
		@Override
		public int getSectionForPosition( int position )
		{
			// Not used
			return 0;
		}
		
		@Override
		public Object[] getSections()
		{
			if ( mFirstLettersProvider != null )
			{
				return mSectionLetters;
			}
			else
			{
				return null;
			}
		}
	}
	
	/**
	 * Class holding info about selected item and scroll offset.
	 */
	public static class ScrollInfo
	{
		public int mSelectedItem;
		public int mSelectedOffset;
	}
	
	/**
	 * Class that extends OnClickListener to provide info about cell position.
	 */
	protected class OnCellClickListener implements View.OnClickListener
	{
		private View mCellView;
		
		OnCellClickListener( View pCellView )
		{
			mCellView = pCellView;
		}
		
		@Override
		public void onClick( View v )
		{
			int lPosition = (Integer) mCellView.getTag();
			mUltraListOnClickListener.onUltraGridClick( mListId , lPosition , (UltraGridCell) mAdapter.getItem( lPosition ) );
		}
	}
	
	/**
	 * Class that extends OnClickListener to provide info about cell position.
	 */
	protected class OnCellLongClickListener implements View.OnLongClickListener
	{
		private View mCellView;
		
		OnCellLongClickListener( View pCellView )
		{
			mCellView = pCellView;
		}
		
		@Override
		public boolean onLongClick( View arg0 )
		{
			int lPosition = (Integer) mCellView.getTag();
			return mUltraListOnClickListener.onUltraGridLongClick( mListId , lPosition , (UltraGridCell) mAdapter.getItem( lPosition ) );
		}
	}
}
