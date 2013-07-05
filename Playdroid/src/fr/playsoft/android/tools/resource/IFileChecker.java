package fr.playsoft.android.tools.resource;

import java.util.List;

/**
 * Interface for custom file checker.
 * File checker is used to check if downloaded file is correct before saving it.
 * 
 * @author Olgierd Uzieblo
 */
public interface IFileChecker
{
	/**
	 * Checks if downloaded file is correct.
	 * 
	 * @param pTask DownloadTask that has just been finished.
	 * @param pMemorySlots All downloaded data.
	 * @return True if this file is correct. False if it should fail.
	 */
	public boolean checkFile( DownloadTask pTask , List< MemorySlot > pMemorySlots );
}
