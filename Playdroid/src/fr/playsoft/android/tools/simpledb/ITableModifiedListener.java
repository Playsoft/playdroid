package fr.playsoft.android.tools.simpledb;

/**
 * Listener to notify when SimpleDBTable has been changed.
 * 
 * @author Olgierd Uzieblo
 */
public interface ITableModifiedListener
{
	/**
	 * Method called when something was modified (insert/update/delete).
	 */
	public void onTableModified();
}
