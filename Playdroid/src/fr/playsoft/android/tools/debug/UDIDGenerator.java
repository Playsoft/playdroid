package fr.playsoft.android.tools.debug;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

/**
 * Generates and stores unique device id. Requires READ_PHONE_STATE permission.
 * The method first tries to get device id from TelephonyManager.
 * If it fails it gets the Secure.ANDROID_ID.
 * If it is a "bugged" id then generates a random device id.
 * 
 * @author Olgierd Uzieblo
 */
public class UDIDGenerator
{
	private static final String RANDOM_LETTERS = "0123456789abcdef";
	private static final String PREFERENCES_NAME = "UDIDPrefs";
	private static final String KEY_UDID = "UDIDValue";
	private static SharedPreferences sPrefs;
	private static String sUDID;
	
	/**
	 * Gets unique device id.
	 * 
	 * @param pContext Context to use.
	 * @return 32 character String with device id.
	 */
	public static synchronized String getUDID( Context pContext )
	{
		if ( sUDID != null )
		{
			return sUDID;
		}
		sPrefs = pContext.getSharedPreferences( PREFERENCES_NAME , Context.MODE_PRIVATE );
		sUDID = sPrefs.getString( KEY_UDID , null );
		if ( sUDID != null )
		{
			return sUDID;
		}
		// We dont know UDID yet, lets generate it
		sUDID = generateUDID( pContext );
		
		// And save to preferences
		Editor lEditor = sPrefs.edit();
		lEditor.putString( KEY_UDID , sUDID );
		lEditor.commit();
		
		return sUDID;
	}
	
	/**
	 * Generates UDID String.
	 * 
	 * @return A 32 char String with unique device id (warning: may be random).
	 */
	private static synchronized String generateUDID( Context pContext )
	{
		try
		{
			MessageDigest lDigest;
			lDigest = java.security.MessageDigest.getInstance( "MD5" );
			String lDeviceId = null;
			
			// Generating id with two methods, one should work
			String lTelephonyDeviceId = ( (TelephonyManager) pContext.getSystemService( Context.TELEPHONY_SERVICE ) ).getDeviceId();
			
			if ( lTelephonyDeviceId != null )
			{
				lDeviceId = "imei" + lTelephonyDeviceId;
			}
			else
			{
				String lAndroidId = Secure.getString( pContext.getContentResolver() , Secure.ANDROID_ID );
				if ( lAndroidId != null )
				{
					if ( !"9774d56d682e549c".equals( lAndroidId ) )
					{
						lDeviceId = "androidid" + lAndroidId;
					}
				}
			}
			
			// Hashing the generated id
			if ( lDeviceId != null )
			{
				lDigest.update( lDeviceId.getBytes() );
				byte lMessageDigest[] = lDigest.digest();
				
				// Create Hex String
				StringBuffer lHexString = new StringBuffer();
				for ( int i = 0; i < lMessageDigest.length; i++ )
				{
					String lHex = Integer.toHexString( 0xFF & lMessageDigest[ i ] );
					while( lHex.length() < 2 )
					{
						lHex = "0" + lHex;
					}
					lHexString.append( lHex );
				}
				return lHexString.toString();
			}
			else
			{
				// Ok both methods failed... lets generate a random id then
				return generateRandomId();
			}
		}
		catch( NoSuchAlgorithmException e )
		{
			// This will never happen but we handle it just in case
			return generateRandomId();
		}
	}
	
	/**
	 * Generates random 32 char string that uses letters a-f and digits 0-9.
	 * It will always start from "x" to indicate that its a random UDID.
	 * 
	 * @return Random UDID.
	 */
	private static synchronized String generateRandomId()
	{
		Random lRandom = new Random( System.currentTimeMillis() + android.os.Build.MODEL.hashCode() + android.os.Build.VERSION.SDK_INT );
		StringBuffer lBuffer = new StringBuffer( "x" );
		for ( int i = 0; i < 31; i++ )
		{
			lBuffer.append( RANDOM_LETTERS.charAt( Math.abs( lRandom.nextInt() % RANDOM_LETTERS.length() ) ) );
		}
		return lBuffer.toString();
	}
}
