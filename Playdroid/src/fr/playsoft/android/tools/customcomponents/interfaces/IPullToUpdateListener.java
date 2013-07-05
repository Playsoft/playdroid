package fr.playsoft.android.tools.customcomponents.interfaces;

import fr.playsoft.android.tools.customcomponents.PullToUpdateList;

/**
 * Listener launched when user performs the "pull to update" action on the PullToUpdateList.
 * 
 * @author Olgierd Uzieblo
 */
public interface IPullToUpdateListener
{
	/**
	 * Launched after performing "pull to update".
	 * Implementation should get new data from server and call onRefreshComplete() on pList when finished.
	 * 
	 * @param pList PullToUpdateList that has requested an update.
	 */
	public void onRefreshRequested( final PullToUpdateList pList );
}
