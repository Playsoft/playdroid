package fr.playsoft.android.tools.customcomponents.interfaces;

import fr.playsoft.android.tools.customcomponents.fragments.AUltraFragment;

/**
 * Listener interface for getting on click events for UltraMultiSwipe fragments.
 * 
 * @author Przemyslaw Klawikowski
 */
public interface IUltraMultiSwipeOnClickListener
{
	/**
	 * Launched after clicking on a ultra multi swipe object.
	 * 
	 * @param pItem Object clicked
	 * @param pPosition Position of View
	 * @param pUltraSwipeItem Fragment displayed
	 */
	public void onSwipeClick( Object pItem , int pPosition , AUltraFragment pUltraSwipeItem );
}
