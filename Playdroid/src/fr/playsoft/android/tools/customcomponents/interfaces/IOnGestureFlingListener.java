package fr.playsoft.android.tools.customcomponents.interfaces;

/**
 * Listener receiving call when UltraGestureDetector receives a gesture.
 * 
 * @author Klawikowski
 */
public interface IOnGestureFlingListener
{
	/**
	 * Called when flinged Right
	 */
	public void OnRightFling();
	
	/**
	 * Called when flinged Left
	 */
	public void OnLeftFling();
}
