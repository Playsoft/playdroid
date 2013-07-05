package fr.playsoft.android.tools.customcomponents.interfaces;

import android.graphics.drawable.Drawable;
import fr.playsoft.android.tools.customcomponents.UltraListCell;

/**
 * Interface providing custom Drawables for ultra list cell images.
 * 
 * @author Olgierd Uzieblo
 */
public interface IUltraListDrawableProvider
{
	/**
	 * Gets custom Drawable.
	 * 
	 * @param pListId List id.
	 * @param pPosition Position on the list.
	 * @param pCell UltraListCell instance.
	 * @param pElementId Element id from CellConfig.
	 * @return
	 */
	public Drawable getDrawable( int pListId , int pPosition , UltraListCell pCell , int pElementId );
}
