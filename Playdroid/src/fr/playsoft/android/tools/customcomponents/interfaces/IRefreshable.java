package fr.playsoft.android.tools.customcomponents.interfaces;

/**
 * Interface for refreshing views.
 * If something is refreshable it means it can be forced to update view (for example if images were missing).
 * 
 * @author Olgierd Uzieblo
 */
public interface IRefreshable
{
	/**
	 * Refreshes currently visible view.
	 * To be used if for example missing images have been downloaded.
	 */
	public void refreshView();
	
	/**
	 * Refreshes currently visible view and reinitializes data.
	 * Should refetch all data, to be used if there were some structure changes.
	 */
	public void refreshViewAndData();
}
