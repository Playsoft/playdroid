package fr.playsoft.android.tools.customcomponents;

/**
 * Defines a single cell element to be used as parameter during creating of UltraGalleryCell.
 */
public class ElementGallery
{
	public int mElementId;
	public Object mElementValue;
	
	public ElementGallery( int pElementId , Object pElementValue )
	{
		mElementId = pElementId;
		mElementValue = pElementValue;
	}
}
