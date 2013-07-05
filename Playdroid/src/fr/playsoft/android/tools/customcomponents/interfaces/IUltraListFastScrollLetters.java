package fr.playsoft.android.tools.customcomponents.interfaces;

import fr.playsoft.android.tools.customcomponents.UltraListCell;

/**
 * Interface providing first letters for fast scroll.
 * 
 * @author Olgierd Uzieblo
 */
public interface IUltraListFastScrollLetters
{
	/**
	 * Gets first letter as String.
	 * 
	 * @param pCell Cell to extract first letter.
	 * @param pPosition Position of this cell.
	 * @param pListId Id of list.
	 * @return String with first letter of this cell.
	 */
	public String getFirstLetter( UltraListCell pCell , int pPosition , int pListId );
}
