package fr.playsoft.android.tools.resource;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import fr.playsoft.android.tools.debug.Log;

/**
 * Class that manages memory buffers. Creates them dynamically if needed and enables app to reuse unused buffers.
 * 
 * @author Olgierd Uzieblo
 */
public class MemoryManager
{
	/** Tag for LogCat **/
	private static final String TAG = "MemoryManager";
	
	/** Minimal number of memory slots - they are never released **/
	private static final int MINIMAL_MEMORY_SLOTS_NUMBER = 10;
	
	/** Is memory controller initialized **/
	private static boolean sIsMemoryControllerInitialized = false;
	
	/** Array of memory slots **/
	private static CopyOnWriteArrayList< MemorySlot > sMemorySlots;
	
	/**
	 * Initializes memory manager, creates first slots.
	 */
	private synchronized static void initializeMemoryManager()
	{
		if ( sIsMemoryControllerInitialized )
		{
			return;
		}
		sMemorySlots = new CopyOnWriteArrayList< MemorySlot >();
		for ( int i = 0; i < MINIMAL_MEMORY_SLOTS_NUMBER; i++ )
		{
			sMemorySlots.add( new MemorySlot() );
		}
		
		sIsMemoryControllerInitialized = true;
	}
	
	/**
	 * Gets more memory, allocates new slot if needed.
	 * 
	 * @return Free MemorySlot.
	 */
	public static synchronized MemorySlot getMemorySlot()
	{
		if ( !sIsMemoryControllerInitialized )
		{
			initializeMemoryManager();
		}
		
		// Look for empty memory slots.
		for ( MemorySlot lSlot : sMemorySlots )
		{
			if ( !lSlot.isAllocated() )
			{
				lSlot.allocate();
				return lSlot;
			}
		}
		
		// No more free slots - create a new slot and return it
		MemorySlot lSlot = new MemorySlot();
		lSlot.allocate();
		sMemorySlots.add( lSlot );
		return lSlot;
	}
	
	/**
	 * Removes unused memory slots except the initial slots (MINIMAL_MEMORY_SLOTS_NUMBER).
	 */
	public static synchronized void removeDeallocatedMemorySlots()
	{
		if ( sIsMemoryControllerInitialized )
		{
			Log.v( TAG , "Cleaning up temp memory. Max used memory = " + ( sMemorySlots.size() * MemorySlot.MEMORY_SLOT_SIZE ) + " bytes." );
			boolean lContinue = true;
			
			while( ( sMemorySlots.size() > MINIMAL_MEMORY_SLOTS_NUMBER ) && ( lContinue ) )
			{
				lContinue = false;
				for ( int i = 0; i < sMemorySlots.size(); i++ )
				{
					MemorySlot lSlot = sMemorySlots.get( i );
					if ( !lSlot.isAllocated() )
					{
						sMemorySlots.remove( i );
						lContinue = true;
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Deallocates multiple memory slots.
	 * 
	 * @param pSlots List of slots to deallocate.
	 */
	public static synchronized void deallocateMemorySlots( List< MemorySlot > pSlots )
	{
		for ( MemorySlot lSlot : pSlots )
		{
			lSlot.deallocate();
		}
	}
	
	/**
	 * Calculates total size of data in memory slots.
	 * 
	 * @param pMemorySlots List of filled memory slots.
	 * @return Size of data in those slots.
	 */
	public static synchronized int calculateTotalSize( List< MemorySlot > pMemorySlots )
	{
		int lResult = 0;
		for ( MemorySlot lSlot : pMemorySlots )
		{
			lResult += lSlot.getUsedAmount();
		}
		return lResult;
	}
}
