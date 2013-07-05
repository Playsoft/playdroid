package fr.playsoft.android.tools.simpledb;

/**
 * Interface for getting notifications when database finished loading/saving.
 * 
 * @author Olgierd Uzieblo
 */
public interface ISimpleDBLoadSaveListener
{
	/**
	 * Called when database finished loading.
	 * 
	 * @param pDBName Base name of database that has just finished loading.
	 */
	public void onDBLoaded( String pDBName );
	
	/**
	 * Called when database failed to load because database version has been changed.
	 * 
	 * @param pDBName Base name of database that has failed to load because of different version.
	 */
	public void onDBVersionChanged( String pDBName );
	
	/**
	 * Called when database finished saving.
	 * 
	 * @param pDBName Base name of database that has just finished saving.
	 * @param pIsSaveSuccessful True if database was saved successfully. False means error.
	 */
	public void onDBSaved( String pDBName , boolean pIsSaveSuccessful );
}
