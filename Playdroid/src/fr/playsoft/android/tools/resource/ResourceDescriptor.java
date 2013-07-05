package fr.playsoft.android.tools.resource;

/**
 * Container for keeping info about single resource (image usually).
 * 
 * @author Olgierd Uzieblo
 */
public class ResourceDescriptor
{
	/** URL to download this file from **/
	private String mURL;
	
	/** Unique file name of this resource, works best if it is md5 of its content **/
	private String mMD5;
	
	/** Optional tag with any object **/
	private Object mTag;
	
	/**
	 * Creates this ResourceDescriptor.
	 * 
	 * @param pURL URL of a resource.
	 * @param pMD5 MD5 of a resource.
	 */
	public ResourceDescriptor( String pURL , String pMD5 )
	{
		mURL = pURL;
		mMD5 = pMD5;
		mTag = null;
	}
	
	/**
	 * Creates this ResourceDescriptor.
	 * 
	 * @param pURL URL of a resource.
	 * @param pMD5 MD5 of a resource.
	 * @param pTag Optional tag object.
	 */
	public ResourceDescriptor( String pURL , String pMD5 , Object pTag )
	{
		mURL = pURL;
		mMD5 = pMD5;
		mTag = pTag;
	}
	
	/**
	 * Gets tag object.
	 * 
	 * @return Tag object, can be null.
	 */
	public Object getTag()
	{
		return mTag;
	}
	
	/**
	 * Gets URL of this ResourceDescriptor.
	 * 
	 * @return URL as String.
	 */
	public String getURL()
	{
		return mURL;
	}
	
	/**
	 * Gets MD5 of this ResourceDescriptor.
	 * 
	 * @return MD5 as String.
	 */
	public String getMD5()
	{
		return mMD5;
	}
	
	/**
	 * Clears md5 and url - sets them to "".
	 */
	public void clear()
	{
		mURL = "";
		mMD5 = "";
	}
	
	/**
	 * Checks if this ResourceDescriptor is empty.
	 * It is empty if URL or MD5 is "".
	 * 
	 * @return True if this resource is empty - there is nothing to download or display.
	 */
	public boolean isEmpty()
	{
		if ( ( mURL.equals( "" ) ) || ( mMD5.equals( "" ) ) )
		{
			return true;
		}
		return false;
	}
}
