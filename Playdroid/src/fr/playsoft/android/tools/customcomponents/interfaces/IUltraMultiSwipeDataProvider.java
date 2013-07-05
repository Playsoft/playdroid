package fr.playsoft.android.tools.customcomponents.interfaces;

import java.util.ArrayList;

import fr.playsoft.android.tools.customcomponents.fragments.AUltraFragment;

/**
 * Interfaces for classes that can provide data for the UltraMultiSwipe.
 * 
 * @author Klawikowski
 */
public interface IUltraMultiSwipeDataProvider
{
	/**
	 * Gets new data for UltraMultiSwipe
	 * 
	 * @param pSwipeId Id of swipe
	 * @return ArrayList<AUltraFragment> Fragments to display in UltraMultiSwipe
	 */
	public ArrayList< AUltraFragment > getNewSwipeData( int pSwipeId );
}
