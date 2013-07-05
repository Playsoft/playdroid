package fr.playsoft.android.tools.customcomponents.interfaces;

import java.util.ArrayList;

import fr.playsoft.android.tools.customcomponents.UltraGalleryCell;

/**
 * Interface for classes that provide data for the UltraGallery
 * 
 * @author Klawikowski
 * 
 */
public interface IUltraGalleryDataProvider
{
	/**
	 * Gets new data.
	 * 
	 * @return New data for the UltraGallery.
	 */
	public ArrayList< UltraGalleryCell > getNewGalleryData();
}
