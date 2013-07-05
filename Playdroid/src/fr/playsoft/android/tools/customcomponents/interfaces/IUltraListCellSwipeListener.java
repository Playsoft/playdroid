package fr.playsoft.android.tools.customcomponents.interfaces;

/**
 * Listener interface for getting on click events for UltraSwipe cells.
 * 
 * @author Przemyslaw Klawikowski
 */
public interface IUltraListCellSwipeListener
{
	/**
	 * Launched after clicking on a cell.
	 * 
	 * @param Object item object.
	 * @param pPosition Cell position on the swipe view.
	 */
	public void onSwipeClick( Object pItem , int pPosition );
}
