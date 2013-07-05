package fr.playsoft.android.tools.customcomponents.interfaces;

import android.view.View;

/**
 * Provides info for UltraListDocked list.
 * 
 * @author Olgierd Uzieblo
 */
public interface IUltraListDockedHeaderProvider
{
	/**
	 * Gets layout id to use as header from R.layout
	 * 
	 * @return Layout id from R.layout
	 */
	public int getHeaderLayoutId();
	
	/**
	 * Configures header view.
	 * 
	 * @param pHeaderView An already created header.
	 */
	public void configureHeaderView( View pHeaderView );
	
	/**
	 * Gets header position on the list.
	 * 
	 * @return Position of header cell.
	 */
	public int getDockedHeaderPosition();
}
