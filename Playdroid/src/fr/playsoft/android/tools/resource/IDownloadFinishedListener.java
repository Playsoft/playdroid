package fr.playsoft.android.tools.resource;

/**
 * Interface to get callbacks from ResourceManager after new file has been downloaded.
 * 
 * @author Olgierd Uzieblo
 */
public interface IDownloadFinishedListener
{
	/**
	 * Called while download is starting/finishing to check if it should be cancelled.
	 * First call is made just before opening connection.
	 * Next calls are made while downloading and just before saving file.
	 * 
	 * @param pTask Task that is being started/downloaded/finished.
	 * @return False to continue downloading this task. True to cancel it.
	 */
	public boolean isDownloadCancelled( DownloadTask pTask );
	
	/**
	 * Method called by ResourceManager when all downloading tasks have finished.
	 */
	public void onDownloadingFinished();
	
	/**
	 * Method called by DownloadTask once it finished and was saved successfully.
	 */
	public void onDownloadTaskSuccessful( DownloadTask pTask );
	
	/**
	 * Method called by DownloadTask once it failed miserably.
	 */
	public void onDownloadTaskFailed( DownloadTask pTask );
}
