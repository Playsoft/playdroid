package fr.playsoft.android.tools.drawables;

import android.graphics.drawable.Drawable;

/**
 * Interface for getting notification when Drawable finished loading.
 * 
 * @author Olgierd Uzieblo
 */
public interface IDrawableLoadingFinishedListener
{
	/**
	 * Launched when Drawable loading has finished.
	 * 
	 * @param pDrawable New Drawable or null if failed to load it.
	 */
	public void onDrawableLoaded( Drawable pDrawable );
}
