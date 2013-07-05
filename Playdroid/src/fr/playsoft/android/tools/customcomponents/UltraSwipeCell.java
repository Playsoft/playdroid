package fr.playsoft.android.tools.customcomponents;

import java.util.List;

import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import fr.playsoft.android.tools.customcomponents.interfaces.IRefreshable;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListCellConfig;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraSwipeConfig;
import fr.playsoft.android.tools.debug.Log;
import fr.playsoft.android.tools.drawables.DrawableCacher;
import fr.playsoft.android.tools.resource.DownloadTask;
import fr.playsoft.android.tools.resource.IDownloadFinishedListener;
import fr.playsoft.android.tools.resource.IFileChecker;
import fr.playsoft.android.tools.resource.ManagerResource;
import fr.playsoft.android.tools.resource.MemorySlot;
import fr.playsoft.android.tools.resource.ResourceDescriptor;

/**
 * Object representing single view in UltraSwipe
 * 
 * @author Klawikowski
 * 
 */
public class UltraSwipeCell
{
	public static final String TAG = "UltraSwipeCell";
	
	private int mSwipeItemType;
	private static IUltraSwipeConfig sSwipeConfig;
	private ElementSwipe[] mSwipeElements;
	private Object mTag;
	
	public int getType()
	{
		return mSwipeItemType;
	}
	
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
	
	public static void setCellConfiguration( IUltraSwipeConfig pSwipeConfig )
	{
		sSwipeConfig = pSwipeConfig;
	}
	
	public UltraSwipeCell( int pSwipeType , ElementSwipe... pElements )
	{
		mSwipeItemType = pSwipeType;
		mSwipeElements = pElements;
	}
	
	public View createView( LayoutInflater pInflater , final IRefreshable pParentView , boolean pIsAutoDownloadEnabled , int pPosition )
	{
		View lSwipeItemView = pInflater.inflate( sSwipeConfig.getCellLayoutId( mSwipeItemType ) , null , false );
		updateView( pInflater , lSwipeItemView , pParentView , pIsAutoDownloadEnabled , pPosition );
		return lSwipeItemView;
	}
	
	public void updateView( LayoutInflater pInflater , final View pView , final IRefreshable pParentView , boolean pIsAutoDownloadEnabled ,
			final int pPosition )
	{
		for ( final ElementSwipe lElement : mSwipeElements )
		{
			if ( lElement == null )
			{
				continue;
			}
			int[] lCurrentElementConfiguration = sSwipeConfig.getElementConfiguration( lElement.mElementId );
			int lCurrentElementType = lCurrentElementConfiguration[ IUltraListCellConfig.DATA_ELEMENT_TYPE ];
			final int lCurrentElementView = lCurrentElementConfiguration[ IUltraListCellConfig.DATA_ELEMENT_VIEW ];
			
			switch ( lCurrentElementType )
			{
				case IUltraSwipeConfig.ELEMENT_TYPE_STRING:
					TextView lStringView = null;
					try
					{
						lStringView = (TextView) pView.findViewById( lCurrentElementView );
					}
					catch( ClassCastException e )
					{
						Log.e( TAG , "Wrong view class. Expected TextView. Check your Swipe config! Swipe type = " + mSwipeItemType
								+ " element id = " + lElement.mElementId );
						continue;
					}
					if ( lStringView == null )
					{
						Log.e( TAG , "Missing TextView! Check your Swipe config! Swipe type = " + mSwipeItemType + " element id = "
								+ lElement.mElementId );
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
						Log.e( TAG , "Wrong value class. Expected String or Spanned. Check your Swipe constructor! Swipe type = " + mSwipeItemType
								+ " element id = " + lElement.mElementId );
						continue;
					}
					break;
				
				case IUltraSwipeConfig.ELEMENT_TYPE_IMAGE:
					ImageView lImageView = null;
					try
					{
						lImageView = (ImageView) pView.findViewById( lCurrentElementView );
					}
					catch( ClassCastException e )
					{
						Log.e( TAG , "Wrong view class. Expected ImageView. Check your Swipe config! Swipe type = " + mSwipeItemType
								+ " element id = " + lElement.mElementId );
						continue;
					}
					if ( lImageView == null )
					{
						Log.e( TAG , "Missing ImageView! Check your Swipe config! Swipe type = " + mSwipeItemType + " element id = "
								+ lElement.mElementId );
						continue;
					}
					
					if ( !( lElement.mElementValue instanceof ResourceDescriptor ) )
					{
						Log.e( TAG , "Wrong value class. Expected ResourceDescriptor. Check your cell constructor! Swipe type = " + mSwipeItemType
								+ " element id = " + lElement.mElementId );
						continue;
					}
					final ResourceDescriptor lImageResourceDescriptor = (ResourceDescriptor) lElement.mElementValue;
					final String lFileName = lImageResourceDescriptor.getMD5();
					final int lDesiredWidth = lImageView.getLayoutParams().width;
					final int lDesiredHeight = lImageView.getLayoutParams().height;
					final Drawable lDrawable = sCacher.getDrawableFromCache( lFileName , lDesiredWidth , lDesiredHeight );
					setupImageView( lImageView , lDrawable , lElement , lImageResourceDescriptor );
					
					if ( lDrawable == null )
					{
						// We dont have this image yet - lets get it to have it next time
						if ( ManagerResource.isFileAvailable( lFileName ) )
						{
							sCacher.addDrawableToCache( ManagerResource.getScaledBitmapDrawable( lImageResourceDescriptor.getMD5() , 500 , 500 ) ,
									lImageResourceDescriptor.getMD5() );
							lImageView.setImageDrawable( sCacher.getDrawableFromCache( lImageResourceDescriptor.getMD5() ) );
						}
						else
						{
							if ( true )
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
					break;
			
			}
			
		}
	}
	
	private void setupImageView( ImageView pImageView , Drawable pDrawable , ElementSwipe pElement , ResourceDescriptor pDescriptor )
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
}
