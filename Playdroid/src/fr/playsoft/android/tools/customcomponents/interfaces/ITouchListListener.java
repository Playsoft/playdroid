package fr.playsoft.android.tools.customcomponents.interfaces;

import fr.playsoft.android.tools.customcomponents.UltraListCell;

/**
 * Listener notified about various touch events on UltraListTouch.
 * 
 * @author Olgierd Uzieblo
 */
public interface ITouchListListener
{
	/**
	 * Launched while cell is being dragged.
	 * 
	 * @param pFrom Previous position.
	 * @param pTo Current position.
	 */
	public void onDragged( int pFrom , int pTo );
	
	/**
	 * Launched after cell was dropped.
	 * 
	 * @param pFrom Previous position.
	 * @param pTo Current position.
	 */
	public void onDropped( int pFrom , int pTo );
	
	/**
	 * Launch after cell was deleted.
	 * 
	 * @param pWhich Cell position.
	 * @param pRemovedCell Cell that was removed.
	 */
	public void onRemoved( int pWhich , UltraListCell pRemovedCell );
}
