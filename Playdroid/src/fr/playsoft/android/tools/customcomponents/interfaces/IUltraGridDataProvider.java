package fr.playsoft.android.tools.customcomponents.interfaces;

import java.util.ArrayList;

import android.view.View;
import fr.playsoft.android.tools.customcomponents.UltraGridCell;

/**
 * Interfaces for classes that can provide data for the UltraGrid.
 * 
 * @author Przemyslaw Klawikowski
 */
public interface IUltraGridDataProvider
{
	/**
	 * Gets new data. May be launched in a new thread depending on UltraGrid settings.
	 * 
	 * @param pListId Id of list that needs new data.
	 * @return New data for the UltraGrid.
	 */
	public ArrayList< UltraGridCell > getNewData( int pListId );
	
	/**
	 * Called when new cell was created or updated.
	 * 
	 * @param pListId Grid id.
	 * @param pCellPosition Cell position on the list.
	 * @param pCell UltraGridCell instance.
	 * @param pCellView View of this cell. Be careful when you modify it!
	 */
	public void onUltraGridCellUpdated( int pListId , int pCellPosition , UltraGridCell pCell , View pCellView );
}
