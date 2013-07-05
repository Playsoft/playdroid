package fr.playsoft.android.tools.customcomponents.interfaces;

import fr.playsoft.android.tools.customcomponents.UltraGridCell;

/**
 * Listener interface for getting on click events for UltraGrid cells.
 * 
 * @author Przemyslaw Klawikowski
 */
public interface IUltraGridOnClickListener
{
	/**
	 * Launched after clicking on a cell.
	 * 
	 * @param pListId List id.
	 * @param pPosition Cell position on the list.
	 * @param pCell The exact clicked cell.
	 */
	public void onUltraGridClick( int pListId , int pPosition , UltraGridCell pCell );
	
	/**
	 * Launched after long clicking on a cell.
	 * 
	 * @param pListId List id.
	 * @param pPosition Cell position on the list.
	 * @param pCell The exact clicked cell.
	 * 
	 * @return True if click was handled.
	 */
	public boolean onUltraGridLongClick( int pListId , int pPosition , UltraGridCell pCell );
}
