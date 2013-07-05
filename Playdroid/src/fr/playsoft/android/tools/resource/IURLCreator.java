package fr.playsoft.android.tools.resource;

/**
 * Tool for creating URLs basing on file name.
 */
public interface IURLCreator
{
	/**
	 * Generates URL for file. Will be launched in a new thread so can be slow.
	 * 
	 * @param pFileName Final file name.
	 * @return URL for this file.
	 */
	public String generateURLForFile( String pFileName );
}
