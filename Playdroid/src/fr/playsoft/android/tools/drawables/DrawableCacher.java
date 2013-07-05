package fr.playsoft.android.tools.drawables;

import java.util.Hashtable;

import android.graphics.drawable.Drawable;

/**
 * Class that manages Drawables cache. See sample usage in UltraListCell.
 * 
 * @author Olgierd Uzieblo
 */
public class DrawableCacher
{
	/** Constant for unspecified size **/
	private static final int UNSPECIFIED_SIZE = 0;
	
	/** Hashtable for keeping indexes of cached Drawables **/
	private Hashtable< Long , Integer > mDrawablesIndexes;
	
	/** Drawables cache **/
	private Drawable[] mDrawables;
	
	/** Drawable hashes for cache elements **/
	private Long[] mDrawableHash;
	
	/** Flags to check if cache slot is used **/
	private boolean[] mIsUsed;
	
	/** Priority for removing - least used Drawables are removed first **/
	private int[] mDeletePriority;
	
	/** Current max priority - the Drawable that was used last has this priority **/
	private int mCurrentMaxPriority;
	
	/** Number of Drawables in cache **/
	private int mCacheSize;
	
	/**
	 * Creates the DrawableCacher.
	 * 
	 * @param pMaxSize Maximum number of Drawables to be stored in cache.
	 */
	public DrawableCacher( int pMaxSize )
	{
		mCacheSize = pMaxSize;
		mDrawablesIndexes = new Hashtable< Long , Integer >( pMaxSize );
		mDrawables = new Drawable[ mCacheSize ];
		mDrawableHash = new Long[ pMaxSize ];
		mIsUsed = new boolean[ mCacheSize ];
		mDeletePriority = new int[ mCacheSize ];
		for ( int i = 0; i < mCacheSize; i++ )
		{
			mIsUsed[ i ] = false;
		}
	}
	
	/**
	 * Creates hash from Drawable parameters.
	 * 
	 * @param pId Drawable name as String.
	 * @param pMaxWidth Drawable width.
	 * @param pMaxHeight Drawable height.
	 * 
	 * @return A unique hash as Long.
	 */
	private synchronized Long createHash( String pId , int pMaxWidth , int pMaxHeight )
	{
		return new Long( ( ( (long) pId.hashCode() ) << 31 ) + ( ( (long) pMaxWidth ) << 15 ) + pMaxHeight );
	}
	
	/**
	 * Gets a Drawable from cache.
	 * 
	 * @param pId Unique Drawable id as a String (can be an md5 file name).
	 * 
	 * @return Drawable or null if it is missing in cache.
	 */
	public synchronized Drawable getDrawableFromCache( String pId )
	{
		return getDrawableFromCache( pId , UNSPECIFIED_SIZE , UNSPECIFIED_SIZE );
	}
	
	/**
	 * Gets a Drawable from cache.
	 * 
	 * @param pId Unique Drawable id as a String (can be an md5 file name).
	 * @param pWidth Max width of needed Drawable.
	 * @param pHeight Max height of needed Drawable.
	 * 
	 * @return Drawable or null if it is missing in cache.
	 */
	public synchronized Drawable getDrawableFromCache( String pId , int pMaxWidth , int pMaxHeight )
	{
		if ( pId != null )
		{
			Long lHash = createHash( pId , pMaxWidth , pMaxHeight );
			Drawable lDrawable = getDrawableFromCache( lHash );
			return lDrawable;
		}
		return null;
	}
	
	/**
	 * Gets a Drawable from cache.
	 * 
	 * @param pHash Hash of needed Drawable.
	 * 
	 * @return Drawable or null if it is missing in cache.
	 */
	public synchronized Drawable getDrawableFromCache( Long pHash )
	{
		Integer lIndex = mDrawablesIndexes.get( pHash );
		if ( lIndex != null )
		{
			mCurrentMaxPriority++;
			mDeletePriority[ lIndex ] = mCurrentMaxPriority;
			return mDrawables[ lIndex.intValue() ];
		}
		
		return null;
	}
	
	/**
	 * Adds new Drawable to the cache.
	 * 
	 * @param pDrawable Drawable to add.
	 * @param pId Unique Drawable id of this Drawable as a String (can be an md5 file name).
	 */
	public synchronized void addDrawableToCache( Drawable pDrawable , String pId )
	{
		addDrawableToCache( pDrawable , pId , UNSPECIFIED_SIZE , UNSPECIFIED_SIZE );
	}
	
	/**
	 * Adds new Drawable to the cache.
	 * 
	 * @param pDrawable Drawable to add.
	 * @param pId Unique Drawable id of this Drawable as a String (can be an md5 file name).
	 * @param pMaxWidth Integer describing this Drawable's maximum width.
	 * @param pMaxHeight Integer describing this Drawable's maximum height
	 */
	public synchronized void addDrawableToCache( Drawable pDrawable , String pId , int pMaxWidth , int pMaxHeight )
	{
		Long lHash = createHash( pId , pMaxWidth , pMaxHeight );
		
		if ( mDrawablesIndexes.get( lHash ) != null )
		{
			// Probably other thread already added this drawable
			return;
		}
		
		// Look for index with the smallest priority
		int lNextIndex = 0;
		int lSmallestPriority = 0xffff;
		for ( int i = 0; i < mDeletePriority.length; i++ )
		{
			if ( mDeletePriority[ i ] < lSmallestPriority )
			{
				lSmallestPriority = mDeletePriority[ i ];
				lNextIndex = i;
			}
		}
		
		if ( mIsUsed[ lNextIndex ] )
		{
			// Remove old entry first
			mDrawablesIndexes.remove( mDrawableHash[ lNextIndex ] );
		}
		
		mDrawablesIndexes.put( lHash , new Integer( lNextIndex ) );
		mIsUsed[ lNextIndex ] = true;
		mDrawables[ lNextIndex ] = pDrawable;
		mDrawableHash[ lNextIndex ] = lHash;
		mCurrentMaxPriority++;
		mDeletePriority[ lNextIndex ] = mCurrentMaxPriority;
	}
	
	/**
	 * Clears the cache - resets it to empty state.
	 */
	public synchronized void clear()
	{
		mCurrentMaxPriority = 0;
		mDrawablesIndexes.clear();
		for ( int i = 0; i < mCacheSize; i++ )
		{
			mIsUsed[ i ] = false;
			mDeletePriority[ i ] = 0;
			mDrawables[ i ] = null;
		}
	}
}
