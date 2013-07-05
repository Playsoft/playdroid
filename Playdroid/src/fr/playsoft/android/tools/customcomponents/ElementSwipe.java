package fr.playsoft.android.tools.customcomponents;

/**
 * Defines a single cell element to be used as parameter during creating of UltraSwipeCell.
 */
public class ElementSwipe
{
	public int mElementId;
	public Object mElementValue;
	
	public ElementSwipe( int pElementId , Object pElementValue )
	{
		mElementId = pElementId;
		mElementValue = pElementValue;
	}
}
