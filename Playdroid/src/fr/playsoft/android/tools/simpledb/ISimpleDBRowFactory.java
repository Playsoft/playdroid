package fr.playsoft.android.tools.simpledb;

/**
 * Row factory interface - it is used for creating new instances of table rows.
 * 
 * @author Olgierd Uzieblo
 */
public interface ISimpleDBRowFactory
{
	/**
	 * Creates and returns a new instance of SimpleDBRow with empty content.
	 */
	public SimpleDBRow createNewEmptyRow();
}
