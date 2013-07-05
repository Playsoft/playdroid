package fr.playsoft.android.tools.customcomponents;

import java.util.List;

import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import fr.playsoft.android.tools.customcomponents.interfaces.IRefreshable;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListCellCheckBoxChecked;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListCellConfig;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListCellImageButtonClick;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListDataProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListDrawableProvider;
import fr.playsoft.android.tools.debug.Log;
import fr.playsoft.android.tools.drawables.DrawableCacher;
import fr.playsoft.android.tools.drawables.DrawableLoader;
import fr.playsoft.android.tools.drawables.IDrawableLoadingFinishedListener;
import fr.playsoft.android.tools.resource.DownloadTask;
import fr.playsoft.android.tools.resource.IDownloadFinishedListener;
import fr.playsoft.android.tools.resource.IFileChecker;
import fr.playsoft.android.tools.resource.ManagerResource;
import fr.playsoft.android.tools.resource.MemorySlot;
import fr.playsoft.android.tools.resource.ResourceDescriptor;

/**
 * Object representing single cell in UltraList.
 * 
 * @author Olgierd Uzieblo
 */
public class UltraListCell
{
	/** Tag for LogCat **/
	private static final String TAG = "UltraListCell";
	
	/** Constant with empty string to avoid bugs **/
	public static final String EMPTY_STRING = "";
	
	/** Cell type **/
	private int mType;
	
	/** Cell Strings **/
	private ElementCell[] mCellElements;
	
	/** Custom drawable that will be used as cell background. Can be null. **/
	private Drawable mCustomBackgroundDrawable;
	
	/** Custom object that can be kept with this cell **/
	private Object mTag;
	
	/** Cell configuration **/
	private static IUltraListCellConfig sCellConfig;
	
	/** Max cache size for images used in cell views **/
	private static final int MAX_CACHE_SIZE = 80;
	
	/** Cache reused by all cells **/
	private static DrawableCacher sCacher = new DrawableCacher( MAX_CACHE_SIZE );
	
	/**
	 * Simple file checker that tries to guess if image is an image or a small json.
	 */
	public static final IFileChecker SIMPLE_IMAGE_CHECKER = new IFileChecker()
	{
		@Override
		public boolean checkFile( DownloadTask pTask , List< MemorySlot > pMemorySlots )
		{
			int lTotalSize = 0;
			for ( int i = 0; i < pMemorySlots.size(); i++ )
			{
				lTotalSize += pMemorySlots.get( i ).getUsedAmount();
			}
			if ( lTotalSize == 0 )
			{
				return false;
			}
			if ( lTotalSize < 1000 )
			{
				String lDataAsString = new String( pMemorySlots.get( 0 ).getData() , 0 , 2 );
				if ( lDataAsString.contains( "{" ) )
				{
					Log.v( TAG , "Downloaded image is incorrect! Name = " + pTask.getFileName() );
					return false;
				}
			}
			
			return true;
		}
	};
	
	/**
	 * Sets cell configuration that will be used by all cells.
	 */
	public static void setCellConfiguration( IUltraListCellConfig pConfiguration )
	{
		sCellConfig = pConfiguration;
	}
	
	/**
	 * Gets cell configuration.
	 * 
	 * @return IUltraListCellConfig used by cells.
	 */
	public static IUltraListCellConfig getCellConfiguration()
	{
		return sCellConfig;
	}
	
	/**
	 * Clears drawable cache.
	 */
	public static void clearCache()
	{
		sCacher.clear();
	}
	
	/**
	 * Creates new cell with custom elements.
	 * 
	 * @param pType Cell type.
	 * @param pElements CellElement objects that define values for this cell.
	 */
	public UltraListCell( int pType , ElementCell... pElements )
	{
		mType = pType;
		mCellElements = pElements;
	}
	
	/**
	 * Sets drawable to be used as cell background.
	 * 
	 * @param pBackgroundDrawable Drawable to use.
	 */
	public void setCustomBackgroundDrawable( Drawable pBackgroundDrawable )
	{
		mCustomBackgroundDrawable = pBackgroundDrawable;
	}
	
	/**
	 * Sets custom object as tag.
	 * 
	 * @param pTag Any object.
	 */
	public void setTag( Object pTag )
	{
		mTag = pTag;
	}
	
	/**
	 * Gets tag object.
	 * 
	 * @return Tag object.
	 */
	public Object getTag()
	{
		return mTag;
	}
	
	/**
	 * Gets cell type.
	 * 
	 * @return Cell type.
	 */
	public int getType()
	{
		return mType;
	}
	
	/**
	 * Gets all cell elements.
	 * 
	 * @return CellElement array.
	 */
	public ElementCell[] getCellElements()
	{
		return mCellElements;
	}
	
	/**
	 * Setups the ImageView.
	 * 
	 * @param pImageView ImageView to setup.
	 * @param pDrawable Drawable to put inside, can be null.
	 * @param pElement CellElement for this ImageView.
	 * @param pDescriptor ResourceDescriptor of the image. Can be null.
	 */
	private void setupImageView( ImageView pImageView , Drawable pDrawable , ElementCell pElement , ResourceDescriptor pDescriptor )
	{
		if ( pDrawable != null )
		{
			// We have the image already
			if ( ( pDescriptor != null ) && ( pDescriptor.getTag() != null ) )
			{
				// We have a custom ScaleType
				pImageView.setScaleType( (ScaleType) pDescriptor.getTag() );
				pImageView.setImageDrawable( pDrawable );
				pImageView.setVisibility( View.VISIBLE );
			}
			else
			{
				// Default MATRIX ScaleType
				Matrix lMatrix = new Matrix();
				lMatrix.postTranslate( ( pImageView.getLayoutParams().width - pDrawable.getIntrinsicWidth() ) / 2 ,
						( pImageView.getLayoutParams().height - pDrawable.getIntrinsicHeight() ) / 2 );
				pImageView.setScaleType( ScaleType.MATRIX );
				pImageView.setImageMatrix( lMatrix );
				pImageView.setImageDrawable( pDrawable );
				pImageView.setVisibility( View.VISIBLE );
			}
		}
		else
		{
			// We dont have the image yet
			if ( ManagerResource.getEmptyDrawable() != null )
			{
				// Lets use the default drawable
				pImageView.setScaleType( ScaleType.CENTER_CROP );
				pImageView.setImageDrawable( ManagerResource.getEmptyDrawable() );
				pImageView.setVisibility( View.VISIBLE );
			}
			else
			{
				// We have nothing, hiding the ImageView
				pImageView.setVisibility( View.INVISIBLE );
			}
		}
	}
	
	/**
	 * Creates View for this cell.
	 * 
	 * @param pInflater LayoutInflater to use.
	 * @param pParentView Refreshable view containing this cell.
	 * @param pIsAutoDownloadEnabled True will automatically download missing images with highest priority.
	 * @param pListId List id.
	 * @param pPosition Cell position on the list.
	 * @param pDrawableProvider Custom drawable provider, can be null.
	 * @param pDataProvider Data provider that will be called when view is updated.
	 * @return View for chosen cell.
	 */
	public View createView( LayoutInflater pInflater , final IRefreshable pParentView , boolean pIsAutoDownloadEnabled , int pListId , int pPosition ,
			IUltraListDrawableProvider pDrawableProvider , IUltraListDataProvider pDataProvider )
	{
		View lCellView = pInflater.inflate( sCellConfig.getCellLayoutId( mType ) , null , false );
		updateView( pInflater , lCellView , pParentView , pIsAutoDownloadEnabled , pListId , pPosition , pDrawableProvider , pDataProvider );
		return lCellView;
	}
	
	/**
	 * Updates View with data from this cell.
	 * 
	 * @param pView View to update.
	 * @param pParentView Refreshable view containing this cell.
	 * @param pIsAutoDownloadEnabled True will automatically download missing images with highest priority.
	 * @param pListId List id.
	 * @param pPosition Cell position on the list.
	 * @param pDrawableProvider Custom drawable provider, can be null.
	 * @param pDataProvider Data provider that will be called when view is updated.
	 */
	public void updateView( LayoutInflater pInflater , final View pView , final IRefreshable pParentView , boolean pIsAutoDownloadEnabled ,
			int pListId , final int pPosition , final IUltraListDrawableProvider pDrawableProvider , IUltraListDataProvider pDataProvider )
	{
		if ( mCustomBackgroundDrawable != null )
		{
			pView.setBackgroundDrawable( mCustomBackgroundDrawable );
		}
		pDataProvider.onUltraListCellUpdated( pListId , pPosition , this , pView );
		for ( ElementCell lElement : mCellElements )
		{
			if ( lElement == null )
			{
				continue;
			}
			int[] lCurrentElementConfiguration = sCellConfig.getElementConfiguration( lElement.mElementId );
			int lCurrentElementType = lCurrentElementConfiguration[ IUltraListCellConfig.DATA_ELEMENT_TYPE ];
			int lCurrentElementView = lCurrentElementConfiguration[ IUltraListCellConfig.DATA_ELEMENT_VIEW ];
			
			switch ( lCurrentElementType )
			{
				case IUltraListCellConfig.ELEMENT_TYPE_STRING:
					TextView lStringView = null;
					try
					{
						lStringView = (TextView) pView.findViewById( lCurrentElementView );
					}
					catch( ClassCastException e )
					{
						Log.e( TAG , "Wrong view class. Expected TextView. Check your cell config! Cell type = " + mType + " element id = "
								+ lElement.mElementId );
						continue;
					}
					if ( lStringView == null )
					{
						Log.e( TAG , "Missing TextView! Check your cell config! Cell type = " + mType + " element id = " + lElement.mElementId );
						continue;
					}
					
					if ( lElement.mElementValue instanceof String )
					{
						lStringView.setText( (String) lElement.mElementValue );
					}
					else if ( lElement.mElementValue instanceof Spanned )
					{
						lStringView.setText( (Spanned) lElement.mElementValue );
					}
					else
					{
						Log.e( TAG , "Wrong value class. Expected String or Spanned. Check your cell constructor! Cell type = " + mType
								+ " element id = " + lElement.mElementId );
						continue;
					}
					lStringView = null;
					break;
				
				case IUltraListCellConfig.ELEMENT_TYPE_IMAGE:
					ImageView lImageView = null;
					try
					{
						lImageView = (ImageView) pView.findViewById( lCurrentElementView );
					}
					catch( ClassCastException e )
					{
						Log.e( TAG , "Wrong view class. Expected ImageView. Check your cell config! Cell type = " + mType + " element id = "
								+ lElement.mElementId );
						continue;
					}
					if ( lImageView == null )
					{
						Log.e( TAG , "Missing ImageView! Check your cell config! Cell type = " + mType + " element id = " + lElement.mElementId );
						continue;
					}
					
					if ( pDrawableProvider != null )
					{
						Drawable lDrawable = pDrawableProvider.getDrawable( pListId , pPosition , this , lElement.mElementId );
						setupImageView( lImageView , lDrawable , lElement , null );
						lDrawable = null;
						break;
					}
					
					if ( !( lElement.mElementValue instanceof ResourceDescriptor ) )
					{
						Log.e( TAG , "Wrong value class. Expected ResourceDescriptor. Check your cell constructor! Cell type = " + mType
								+ " element id = " + lElement.mElementId );
						continue;
					}
					ResourceDescriptor lImageResourceDescriptor = (ResourceDescriptor) lElement.mElementValue;
					final String lFileName = lImageResourceDescriptor.getMD5();
					final int lDesiredWidth = lImageView.getLayoutParams().width;
					final int lDesiredHeight = lImageView.getLayoutParams().height;
					Drawable lDrawable = sCacher.getDrawableFromCache( lFileName , lDesiredWidth , lDesiredHeight );
					setupImageView( lImageView , lDrawable , lElement , lImageResourceDescriptor );
					if ( lDrawable == null )
					{
						// We dont have this image yet - lets get it to have it next time
						if ( ManagerResource.isFileAvailable( lFileName ) )
						{
							// Load the image and refresh view
							DrawableLoader.addTask( lFileName , lDesiredWidth , lDesiredHeight , new IDrawableLoadingFinishedListener()
							{
								@Override
								public void onDrawableLoaded( Drawable pDrawable )
								{
									if ( pDrawable != null )
									{
										sCacher.addDrawableToCache( pDrawable , lFileName , lDesiredWidth , lDesiredHeight );
										pParentView.refreshView();
									}
								}
							} );
						}
						else
						{
							if ( pIsAutoDownloadEnabled )
							{
								// Download the image and refresh view
								if ( !ManagerResource.isFileInTaskQueue( lImageResourceDescriptor ) )
								{
									ManagerResource.addTask( lImageResourceDescriptor , ManagerResource.getCurrentMaxPriority() ,
											new IDownloadFinishedListener()
											{
												@Override
												public void onDownloadingFinished()
												{
												}
												
												@Override
												public void onDownloadTaskSuccessful( DownloadTask pTask )
												{
													pParentView.refreshView();
												}
												
												@Override
												public void onDownloadTaskFailed( DownloadTask pTask )
												{
												}
												
												@Override
												public boolean isDownloadCancelled( DownloadTask pTask )
												{
													return false;
												}
											} , SIMPLE_IMAGE_CHECKER , ManagerResource.TASK_TYPE_SMALL_FILE , null , null , null );
								}
							}
						}
					}
					lImageView = null;
					lDrawable = null;
					break;
				
				case IUltraListCellConfig.ELEMENT_TYPE_IMAGEBUTTON:
					ImageButton lImageButton = null;
					try
					{
						lImageButton = (ImageButton) pView.findViewById( lCurrentElementView );
					}
					catch( ClassCastException e )
					{
						Log.e( TAG , "Wrong view class. Expected ImageButton. Check your cell config! Cell type = " + mType + " element id = "
								+ lElement.mElementId );
						continue;
					}
					if ( lImageButton == null )
					{
						Log.e( TAG , "Missing ImageButton! Check your cell config! Cell type = " + mType + " element id = " + lElement.mElementId );
						continue;
					}
					
					if ( !( lElement.mElementValue instanceof IUltraListCellImageButtonClick ) )
					{
						Log.e( TAG , "Wrong value class. Expected IUltraListCellImageButtonClick. Check your cell constructor! Cell type = " + mType
								+ " element id = " + lElement.mElementId );
						continue;
					}
					
					final int lImageButtonElementId = lElement.mElementId;
					final IUltraListCellImageButtonClick lImageButtonClickListener = (IUltraListCellImageButtonClick) lElement.mElementValue;
					lImageButton.setOnClickListener( new View.OnClickListener()
					{
						@Override
						public void onClick( View v )
						{
							lImageButtonClickListener.onImageButtonClick( UltraListCell.this , lImageButtonElementId );
						}
					} );
					lImageButton.setFocusable( false );
					break;
				
				case IUltraListCellConfig.ELEMENT_TYPE_CHECKBOX:
					CheckBox lCheckBox = null;
					try
					{
						lCheckBox = (CheckBox) pView.findViewById( lCurrentElementView );
					}
					catch( ClassCastException e )
					{
						Log.e( TAG , "Wrong view class. Expected CheckBox. Check your cell config! Cell type = " + mType + " element id = "
								+ lElement.mElementId );
						continue;
					}
					if ( lCheckBox == null )
					{
						Log.e( TAG , "Missing CheckBox! Check your cell config! Cell type = " + mType + " element id = " + lElement.mElementId );
						continue;
					}
					
					if ( !( lElement.mElementValue instanceof IUltraListCellCheckBoxChecked ) )
					{
						Log.e( TAG , "Wrong value class. Expected IUltraListCellCheckBoxChecked. Check your cell constructor! Cell type = " + mType
								+ " element id = " + lElement.mElementId );
						continue;
					}
					
					final int lCheckBoxElementId = lElement.mElementId;
					final IUltraListCellCheckBoxChecked lCheckBoxCheckedListener = (IUltraListCellCheckBoxChecked) lElement.mElementValue;
					lCheckBox.setOnCheckedChangeListener( new OnCheckedChangeListener()
					{
						@Override
						public void onCheckedChanged( CompoundButton buttonView , boolean isChecked )
						{
							lCheckBoxCheckedListener.onCheckBoxCheckedChanged( UltraListCell.this , lCheckBoxElementId , isChecked );
						}
					} );
					lCheckBox.setFocusable( false );
					lCheckBox.setChecked( lCheckBoxCheckedListener.getInitialCheckboxIsChecked( UltraListCell.this , lCheckBoxElementId ) );
					break;
				case IUltraListCellConfig.ELEMENT_TYPE_SWIPE:
					final UltraSwipeListCell lSwipe = (UltraSwipeListCell) pView.findViewById( lCurrentElementView );
					final SwipeViewDescriptor lSwipeViewDescriptor = (SwipeViewDescriptor) lElement.mElementValue;
					// +1 since we're not able to do anything with it ... we must hardcode it :
					int[] SwipeElementConfiguration = sCellConfig.getElementConfiguration( lElement.mElementId + 1 );
					int lSwipeElementView = SwipeElementConfiguration[ IUltraListCellConfig.DATA_ELEMENT_VIEW ];
					
					UltraSwipeIndicator lSwipeIndicator = (UltraSwipeIndicator) pView.findViewById( lSwipeElementView );
					lSwipe.init( lSwipeIndicator , lSwipeViewDescriptor );
					break;
			}
		}
	}
}
