package fr.playsoft.android.tools.customcomponents.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.playsoft.android.tools.customcomponents.UltraList;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListDataProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListFastScrollLetters;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListOnClickListener;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListPaginationListener;

/**
 * Abstract Fragment Class which constains fully initialized UltraList
 * 
 * @author Klawikowski
 * 
 */
public abstract class AUltraFragmentList extends AUltraFragment implements IUltraListOnClickListener , IUltraListDataProvider ,
		IUltraListPaginationListener , IUltraListFastScrollLetters
{
	protected UltraList mUltraList;
	
	protected abstract boolean isFastLetterEnabled();
	
	@Override
	public View onCreateView( LayoutInflater inflater , ViewGroup container , Bundle savedInstanceState )
	{
		mUltraList = new UltraList( getActivity() , inflater , 0 , this , this , true , true , isFastLetterEnabled() ? this : null , this , null );
		if ( getHeaderView() != null )
		{
			mUltraList.addHeaderView( getHeaderView() );
		}
		mUltraList.initialize();
		return mUltraList;
	}
	
	protected abstract View getHeaderView();
}
