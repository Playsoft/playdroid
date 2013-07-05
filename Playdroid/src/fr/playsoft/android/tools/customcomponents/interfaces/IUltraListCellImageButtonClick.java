package fr.playsoft.android.tools.customcomponents.interfaces;

import fr.playsoft.android.tools.customcomponents.UltraListCell;

/**
 * Listener interface for clicking on ImageButtons inside cells that have them.
 * 
 * @author Olgierd Uzieblo
 */
public interface IUltraListCellImageButtonClick
{
	/**
	 * Launched after clicking on cell element of ELEMENT_TYPE_IMAGEBUTTON type.
	 * 
	 * @param pCell UltraListCell instance that has clicked ImageButton.
	 * @param pElementId Element id of clicked button.
	 */
	public void onImageButtonClick( UltraListCell pCell , int pElementId );
}
