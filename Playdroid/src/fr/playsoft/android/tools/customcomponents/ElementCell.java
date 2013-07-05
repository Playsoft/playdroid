package fr.playsoft.android.tools.customcomponents;

/**
 * Defines a single cell element to be used as parameter during creating of UltraListCell.
 */
public class ElementCell
{
	public int mElementId;
	public Object mElementValue;
	
	public ElementCell( int pElementId , Object pElementValue )
	{
		mElementId = pElementId;
		mElementValue = pElementValue;
	}
}
