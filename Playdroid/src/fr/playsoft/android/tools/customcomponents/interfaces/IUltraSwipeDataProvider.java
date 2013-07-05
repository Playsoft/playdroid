package fr.playsoft.android.tools.customcomponents.interfaces;

import java.util.ArrayList;

import android.view.View;
import fr.playsoft.android.tools.customcomponents.UltraSwipeCell;

/**
 * Interfaces for classes that can provide data for the UltraSwipe
 * 
 * @author Klawikowski
 * 
 */
public interface IUltraSwipeDataProvider
{
	/**
	 * Gets new data.
	 * 
	 * @param pSwipeId
	 * @return
	 */
	public ArrayList< UltraSwipeCell > getNewSwipeData( int pSwipeId );
	
	/**
	 * Called when new cell was created or updated
	 * 
	 * @param pSwipeId Swipe Id
	 * @param pCellPosition Cell position on the Swipe.
	 * @param pCell UltraSwipeCell instance.
	 * @param pCellView View of this cell.
	 */
	public void onUltraSwipeCellUpdated( int pSwipeId , int pCellPosition , UltraSwipeCell pCell , View pCellView );
	
}
