package fr.playsoft.android.tools.customcomponents.interfaces;

import fr.playsoft.android.tools.customcomponents.UltraListCell;

/**
 * Listener interface for clicking on CheckBox inside cells that have them.
 * 
 * @author Olgierd Uzieblo
 */
public interface IUltraListCellCheckBoxChecked
{
	/**
	 * Launched after clicking on cell element of ELEMENT_TYPE_CHECKBOX type.
	 * 
	 * @param pCell UltraListCell instance whose CheckBox was clicked.
	 * @param pElementId Element id of clicked CheckBox.
	 * @param pIsChecked Current checkbox state - checked or not.
	 */
	public void onCheckBoxCheckedChanged( UltraListCell pCell , int pElementId , boolean pIsChecked );
	
	/**
	 * Launched while CheckBox is initializing. Sets initial CheckBox state.
	 * 
	 * @param pCell UltraListCell instance with CheckBox.
	 * @param pElementId Element id of clicked CheckBox.
	 * @return Initial state of this CheckBox - checked or not.
	 */
	public boolean getInitialCheckboxIsChecked( UltraListCell pCell , int pElementId );
}
