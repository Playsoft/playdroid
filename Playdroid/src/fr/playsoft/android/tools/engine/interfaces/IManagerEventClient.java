package fr.playsoft.android.tools.engine.interfaces;

public interface IManagerEventClient
{
	
	/**
	 * Interface of ManagerEvent clients.
	 * Client provides info about supported events and method to call when event is triggered.
	 * 
	 */
	/**
	 * Gets flags of all handled events.
	 * Return ManagerEvent.EVENT_NONE (0) if client does not have any dynamic content.
	 * Warning: returned value must be constant.
	 * 
	 * @return Integer with flags of events that client wants to be notified about.
	 */
	public int getHandledEvents();
	
	/**
	 * Called when some flags of a new event match flags returned by getHandledEvents().
	 * Will be always called from UI thread.
	 * 
	 * @param pEventFlags Flags of new event.
	 * @param pEventInfo Optional Object with details of this event - can be null.
	 */
	public void onIncomingEvent( int pEventFlags , Object pEventInfo );
}
