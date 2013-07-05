package fr.playsoft.android.tools.customcomponents.interfaces;

import fr.playsoft.android.tools.customcomponents.UltraSwipeCell;

/**
 * Listener interface for getting on click events for UltraSwipe fragments.
 * 
 * @author Przemyslaw Klawikowski
 */
public interface IUltraSwipeOnClickListener
{
	/**
	 * Launched after clicking on a ultra multi swipe object.
	 * 
	 * @param pItem Object clicked
	 * @param pPosition Position of View
	 * @param pUltraSwipeItem Fragment displayed
	 */
	public void onSwipeClick( Object pItem , int pPosition , UltraSwipeCell pUltraSwipeItem );
}
