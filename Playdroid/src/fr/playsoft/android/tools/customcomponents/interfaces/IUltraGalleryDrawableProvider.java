package fr.playsoft.android.tools.customcomponents.interfaces;

import android.graphics.drawable.Drawable;
import fr.playsoft.android.tools.customcomponents.UltraGalleryCell;

public interface IUltraGalleryDrawableProvider
{
	/**
	 * Gets custom Drawable
	 */
	public Drawable getDrawable( int pPosition , UltraGalleryCell pGalleryItem , int pElementId );
}
