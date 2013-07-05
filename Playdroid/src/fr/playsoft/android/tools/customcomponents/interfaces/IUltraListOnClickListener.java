package fr.playsoft.android.tools.customcomponents.interfaces;

import fr.playsoft.android.tools.customcomponents.UltraListCell;

/**
 * Listener interface for getting on click events for UltraList cells.
 * 
 * @author Olgierd Uzieblo
 */
public interface IUltraListOnClickListener
{
	/**
	 * Launched after clicking on a cell.
	 * 
	 * @param pListId List id.
	 * @param pPosition Cell position on the list.
	 * @param pCell The exact clicked cell.
	 */
	public void onUltraListClick( int pListId , int pPosition , UltraListCell pCell );
	
	/**
	 * Launched after long clicking on a cell.
	 * 
	 * @param pListId List id.
	 * @param pPosition Cell position on the list.
	 * @param pCell The exact clicked cell.
	 * 
	 * @return True if click was handled.
	 */
	public boolean onUltraListLongClick( int pListId , int pPosition , UltraListCell pCell );
}
