package fr.playsoft.android.tools.customcomponents.interfaces;

import android.view.View;
import fr.playsoft.android.tools.customcomponents.UltraGalleryCell;

/**
 * Listener interface for getting on click events for UltraGallery cells.
 * 
 * @author Klawikowski
 * 
 */
public interface IUltraGalleryOnClickListener
{
	/**
	 * Launched after clicking on a cell.
	 * 
	 * @param pCell The exact clicked cell.
	 * @param pPosition Cell position.
	 */
	public void onUltraGalleryClick( UltraGalleryCell pCell , int pPosition );
	
	/**
	 * Launched after long clicking on a cell.
	 * 
	 * @param pView view of cell.
	 * @param pPosition Cell position.
	 * 
	 */
	public void onUltraGalleryLongClick( View pView , int pPosition );
}
