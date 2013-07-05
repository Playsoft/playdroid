package fr.playsoft.android.tools.customcomponents.interfaces;

import fr.playsoft.android.tools.customcomponents.UltraGridCell;

/**
 * Interface providing first letters for fast scroll.
 * 
 * @author Przemyslaw Klawikowski
 */
public interface IUltraGridFastScrollLetters
{
	/**
	 * Gets first letter as String.
	 * 
	 * @param pCell Cell to extract first letter.
	 * @param pPosition Position of this cell.
	 * @param pListId Id of list.
	 * @return String with first letter of this cell.
	 */
	public String getFirstLetter( UltraGridCell pCell , int pPosition , int pListId );
}
