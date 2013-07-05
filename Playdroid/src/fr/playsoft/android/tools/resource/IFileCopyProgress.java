package fr.playsoft.android.tools.resource;

/**
 * Interface that informs about file copy progress.
 * 
 * @author Olgierd Uzieblo
 */
public interface IFileCopyProgress
{
	/**
	 * Called while file copying is in progress.
	 * 
	 * @param pCopier FileCopier instance.
	 * @param pCurrentCopiedAmount Number of already copied bytes.
	 * @param pFileSize Total file size.
	 */
	public void onCopyProgressUpdate( FileCopier pCopier , int pCurrentCopiedAmount , int pFileSize );
	
	/**
	 * Called when copying is finished.
	 * 
	 * @param pCopier FileCopier instance.
	 */
	public void onCopyFinished( FileCopier pCopier );
}
