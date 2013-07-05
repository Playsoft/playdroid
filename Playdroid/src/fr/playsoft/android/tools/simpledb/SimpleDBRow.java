package fr.playsoft.android.tools.simpledb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A single row used inside each simple db table.
 * 
 * @author Olgierd Uzieblo
 */
public abstract class SimpleDBRow
{
	/**
	 * Loads this row from a stream.
	 * 
	 * @param pStream DataInputStream with data.
	 * @throws IOException If something goes wrong.
	 */
	public abstract void loadRow( DataInputStream pStream ) throws IOException;
	
	/**
	 * Saves this row to a stream.
	 * 
	 * @param pStream DataInputStream to save to.
	 * @throws IOException If something goes wrong.
	 */
	public abstract void saveRow( DataOutputStream pStream ) throws IOException;
	
	/**
	 * Gets main key for this row.
	 * Should return main key - should be unique if table has unique key.
	 * If table does not have unique key this may be any int.
	 * 
	 * @return Main key value for this row.
	 */
	public abstract int getMainKey();
	
	/**
	 * Saves array of Strings to the stream.
	 * 
	 * @param pStringArray Array of Strings to be saved.
	 * @param pStream Stream to save it to.
	 */
	protected final void saveStringArray( List< String > pStringArray , DataOutputStream pStream ) throws IOException
	{
		short lArraySize = (short) pStringArray.size();
		pStream.writeShort( lArraySize );
		for ( int i = 0; i < lArraySize; i++ )
		{
			pStream.writeUTF( pStringArray.get( i ) );
		}
	}
	
	/**
	 * Reads array of Strings from the stream.
	 * 
	 * @param pStream Stream to load from.
	 * @return List of Strings.
	 */
	protected final List< String > loadStringArray( DataInputStream pStream ) throws IOException
	{
		short lSize = pStream.readShort();
		List< String > lResult = new ArrayList< String >( lSize );
		for ( int i = 0; i < lSize; i++ )
		{
			lResult.add( pStream.readUTF() );
		}
		return lResult;
	}
	
	/**
	 * Saves array of integers to the stream.
	 * 
	 * @param pIntArray List of Integers to be saved.
	 * @param pStream Stream to save it to.
	 */
	protected final void saveIntArray( List< Integer > pIntArray , DataOutputStream pStream ) throws IOException
	{
		short lArraySize = (short) pIntArray.size();
		pStream.writeShort( lArraySize );
		for ( int i = 0; i < lArraySize; i++ )
		{
			pStream.writeInt( pIntArray.get( i ) );
		}
	}
	
	/**
	 * Reads array of integers from the stream.
	 * 
	 * @param pStream Stream to load from.
	 * @return List of Integers.
	 */
	protected final List< Integer > loadIntArray( DataInputStream pStream ) throws IOException
	{
		short lSize = pStream.readShort();
		List< Integer > lResult = new ArrayList< Integer >( lSize );
		for ( int i = 0; i < lSize; i++ )
		{
			lResult.add( pStream.readInt() );
		}
		return lResult;
	}
}
