package fr.playsoft.android.tools.resource;

/**
 * Listener notified when there is not enough memory to save downloaded file.
 * 
 * @author Olgierd Uzieblo
 */
public interface IOutOfMemoryListener
{
	/**
	 * Called when there is not enough memory to save new file in internal memory and there is no SD card available.
	 */
	public void onOufOfMemory();
}
