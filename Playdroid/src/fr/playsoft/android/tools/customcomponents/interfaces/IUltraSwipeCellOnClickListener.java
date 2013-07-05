package fr.playsoft.android.tools.customcomponents.interfaces;

import fr.playsoft.android.tools.customcomponents.UltraSwipeCell;

/**
 * Listener interface for getting on click events for UltraSwipeCell fragments.
 * 
 * @author Przemyslaw Klawikowski
 */
public interface IUltraSwipeCellOnClickListener
{
	/**
	 * Launched after clicking on a ultra multi swipem cell object.
	 * 
	 * @param pItem Object clicked
	 * @param pPosition Position of View
	 * @param pUltraSwipeItem Fragment displayed
	 */
	public void onSwipeCellClick( Object pItem , int pPosition , UltraSwipeCell pUltraSwipeItem );
}
