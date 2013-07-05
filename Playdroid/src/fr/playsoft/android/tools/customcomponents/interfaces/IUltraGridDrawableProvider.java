package fr.playsoft.android.tools.customcomponents.interfaces;

import android.graphics.drawable.Drawable;
import fr.playsoft.android.tools.customcomponents.UltraGridCell;

/**
 * Interface providing custom Drawables for ultra grid cell images.
 * 
 * @author Przemyslaw Klawikowski
 */
public interface IUltraGridDrawableProvider
{
	/**
	 * Gets custom Drawable.
	 * 
	 * @param pListId List id.
	 * @param pPosition Position on the list.
	 * @param pCell UltraGridCell instance.
	 * @param pElementId Element id from CellConfig.
	 * 
	 *            return Drawable requested
	 */
	public Drawable getDrawable( int pListId , int pPosition , UltraGridCell pCell , int pElementId );
}
