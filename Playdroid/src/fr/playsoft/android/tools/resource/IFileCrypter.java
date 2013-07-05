package fr.playsoft.android.tools.resource;

/**
 * Interface for file encryption/decryption.
 * 
 * @author Olgierd Uzieblo
 */
public interface IFileCrypter
{
	public void encrypt( int pPartNumber , byte[] pData , int pOffset , int pLength );
	
	public void decrypt( int pPartNumber , byte[] pData , int pOffset , int pLength );
}
