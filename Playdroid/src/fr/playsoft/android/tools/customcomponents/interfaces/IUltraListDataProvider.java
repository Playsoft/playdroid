package fr.playsoft.android.tools.customcomponents.interfaces;

import java.util.ArrayList;

import android.view.View;
import fr.playsoft.android.tools.customcomponents.UltraListCell;

/**
 * Interfaces for classes that can provide data for the UltraList.
 * 
 * @author Olgierd Uzieblo
 */
public interface IUltraListDataProvider
{
	/**
	 * Gets new data. May be launched in a new thread depending on UltraList settings.
	 * 
	 * @param pListId Id of list that needs new data.
	 * @return New data for the UltraList.
	 */
	public ArrayList< UltraListCell > getNewData( int pListId );
	
	/**
	 * Called when new cell was created or updated.
	 * 
	 * @param pListId List id.
	 * @param pCellPosition Cell position on the list.
	 * @param pCell UltraListCell instance.
	 * @param pCellView View of this cell.
	 */
	public void onUltraListCellUpdated( int pListId , int pCellPosition , UltraListCell pCell , View pCellView );
}
