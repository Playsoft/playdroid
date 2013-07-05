package fr.playsoft.android.tools.customcomponents.interfaces;

import android.view.View;

/**
 * Interface for classes that provide data for the UltraDualView
 * 
 * @author Klawikowski
 * 
 */
public interface IUltraDualViewDataProvider
{
	/**
	 * Gets View for top layer
	 * 
	 * @return View for top layer.
	 */
	public View getTopView();
	
	/**
	 * Gets View for bottom layer
	 * 
	 * @return View for bottom layer.
	 */
	public View getBottomView();
}
