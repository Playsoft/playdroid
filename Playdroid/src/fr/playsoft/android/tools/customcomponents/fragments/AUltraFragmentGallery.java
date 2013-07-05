package fr.playsoft.android.tools.customcomponents.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.playsoft.android.tools.customcomponents.UltraGallery;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraGalleryDataProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraGalleryOnClickListener;

/**
 * Abstract Fragment Class which constains fully initialized UltraGallery
 * 
 * @author Klawikowski
 * 
 */
public abstract class AUltraFragmentGallery extends AUltraFragment implements IUltraGalleryDataProvider , IUltraGalleryOnClickListener
{
	protected UltraGallery mUltraGallery;
	
	@Override
	public View onCreateView( LayoutInflater inflater , ViewGroup container , Bundle savedInstanceState )
	{
		mUltraGallery = new UltraGallery( getActivity() , inflater , this , this );
		return mUltraGallery;
	}
}
