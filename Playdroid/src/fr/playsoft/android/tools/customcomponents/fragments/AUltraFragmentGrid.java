package fr.playsoft.android.tools.customcomponents.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.playsoft.android.tools.customcomponents.UltraGrid;
import fr.playsoft.android.tools.customcomponents.UltraListCell;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraGridDataProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraGridOnClickListener;

/**
 * Abstract Fragment Class which constains fully initialized UltraGrid
 * 
 * @author Klawikowski
 * 
 */
public abstract class AUltraFragmentGrid extends AUltraFragment implements IUltraGridDataProvider , IUltraGridOnClickListener
{
	protected UltraGrid mUltraGrid;
	protected ArrayList< UltraListCell > mDataCell = new ArrayList< UltraListCell >();
	
	@Override
	public View onCreateView( LayoutInflater inflater , ViewGroup container , Bundle savedInstanceState )
	{
		mUltraGrid = new UltraGrid( getActivity() , inflater , 0 , 3 , this , this , true , true , null , null );
		return mUltraGrid;
	}
	
}
