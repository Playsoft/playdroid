package fr.playsoft.android.tools.engine;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Looper;
import fr.playsoft.android.tools.engine.interfaces.IManagerEventClient;

public class ManagerEvent
{
	
	/** Current instance **/
	private static ManagerEvent sInstance;
	
	/** Handler for going back to UI thread **/
	private static Handler sHandler;
	
	/** List of clients interested in getting updates **/
	private static ArrayList< IManagerEventClient > sClients;
	
	/**
	 * Gets DataUpdater instance.
	 * 
	 * @return A new or existing instance of DataUpdater.
	 */
	public static ManagerEvent getInstance()
	{
		if ( sInstance == null )
		{
			sInstance = new ManagerEvent();
		}
		return sInstance;
	}
	
	/**
	 * Private constructor - prepares DataUpdater.
	 */
	private ManagerEvent()
	{
		sClients = new ArrayList< IManagerEventClient >();
		sHandler = new Handler( Looper.getMainLooper() );
	}
	
	public void removeClient( IManagerEventClient pClient )
	{
		sClients.remove( pClient );
	}
	
	/**
	 * Adds new client to list of clients.
	 * 
	 * @param pClient Instance of IDataUpdaterClient.
	 * @return True if client was added, false if it was already on the list.
	 */
	public void addClient( IManagerEventClient pClient )
	{
		synchronized( sClients )
		{
			sClients.add( pClient );
		}
	}
	
	/**
	 * Calls update on all clients that handle matching event.
	 * Update calls will happen in UI thread - so it may take some time before they happen.
	 * 
	 * @param pEventFlags Event flags.
	 * @param pEventInfo Optional Object describing this event - can be null.
	 * @return Number of clients that will be updated as a result of this event.
	 */
	public int callUpdate( final int pEventFlags , final Object pEventInfo )
	{
		synchronized( sClients )
		{
			int lNumClients = 0;
			for ( IManagerEventClient lClient : sClients )
			{
				if ( lClient != null )
				{
					if ( ( lClient.getHandledEvents() & pEventFlags ) > 0 )
					{
						lNumClients++;
						final IManagerEventClient lClientToUpdate = lClient;
						sHandler.post( new Runnable()
						{
							@Override
							public void run()
							{
								lClientToUpdate.onIncomingEvent( pEventFlags , pEventInfo );
							}
						} );
					}
				}
			}
			return lNumClients;
		}
	}
}
