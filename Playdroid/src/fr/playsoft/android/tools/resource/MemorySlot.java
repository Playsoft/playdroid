package fr.playsoft.android.tools.resource;

/**
 * Memory slot - a byte array for temporary storage of downloaded data.
 * 
 * @author Olgierd Uzieblo
 */
public class MemorySlot
{
	/** Size of single memory slot **/
	public static final int MEMORY_SLOT_SIZE = 10000;
	
	/** Temporary data **/
	private byte[] mData;
	
	/** Is this slot allocated **/
	private boolean mIsAllocated;
	
	/** Remembers amount of used bytes in this memory slot **/
	private int mUsedBytesAmount;
	
	/**
	 * Creates new memory slot.
	 */
	MemorySlot()
	{
		mData = new byte[ MEMORY_SLOT_SIZE ];
		mIsAllocated = false;
		mUsedBytesAmount = 0;
	}
	
	/**
	 * Allocates this memory slot.
	 */
	public synchronized void allocate()
	{
		mIsAllocated = true;
	}
	
	/**
	 * Deallocates this memory slot.
	 */
	public synchronized void deallocate()
	{
		mIsAllocated = false;
		mUsedBytesAmount = 0;
	}
	
	/**
	 * Checks if this memory slot is allocated.
	 * 
	 * @return True if someone is using it.
	 */
	public synchronized boolean isAllocated()
	{
		return mIsAllocated;
	}
	
	/**
	 * Gets data array of this memory slot.
	 * 
	 * @return Byte array.
	 */
	public synchronized byte[] getData()
	{
		return mData;
	}
	
	/**
	 * Sets amount of used bytes.
	 * 
	 * @param pUsedAmount Amount of byte used in data array.
	 */
	public synchronized void setUsedAmount( int pUsedAmount )
	{
		mUsedBytesAmount = pUsedAmount;
	}
	
	/**
	 * Gets amount of used data.
	 * 
	 * @return Amount of bytes used in data array.
	 */
	public synchronized int getUsedAmount()
	{
		return mUsedBytesAmount;
	}
	
	/**
	 * Gets amount of free bytes left in data array.
	 * 
	 * @return Amount of free bytes.
	 */
	public synchronized int getFreeAmount()
	{
		return MEMORY_SLOT_SIZE - mUsedBytesAmount;
	}
}
