package fr.playsoft.android.tools.customcomponents.interfaces;

/**
 * Interface which should be implemented by all FragmentActivities
 * It enables communication Fragment -> Activity
 * All UltraActivities should implement it! Otherwise UltraFragment will crash when added to activity
 * 
 * @author Klawikowski
 * 
 */
public interface IUltraActivityFragmentActionListener
{
	/**
	 * Should be used to communicate with activity to inform about events
	 * 
	 * @param pAction int ACTION ID, to recognize action
	 * @param pObject optional. Object to pass
	 */
	public void onFragmentAction( int pAction , Object pObject );
}
