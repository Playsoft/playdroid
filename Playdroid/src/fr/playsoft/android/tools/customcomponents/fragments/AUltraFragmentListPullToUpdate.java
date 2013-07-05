package fr.playsoft.android.tools.customcomponents.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.playsoft.android.tools.R;
import fr.playsoft.android.tools.customcomponents.PullToUpdateList;
import fr.playsoft.android.tools.customcomponents.interfaces.IPullToUpdateListener;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListDataProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListFastScrollLetters;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListOnClickListener;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraListPaginationListener;

/**
 * Abstract Fragment Class which constains fully initialized UltraPullToUpdateList
 * 
 * @author Klawikowski
 * 
 */
public abstract class AUltraFragmentListPullToUpdate extends AUltraFragment implements IPullToUpdateListener , IUltraListFastScrollLetters ,
		IUltraListDataProvider , IUltraListOnClickListener , IUltraListPaginationListener
{
	protected PullToUpdateList mUltraList;
	
	@Override
	public View onCreateView( LayoutInflater inflater , ViewGroup container , Bundle savedInstanceState )
	{
		// Pull header must contain at least arrow image, progress bar and text
		View lPullHeader = inflater.inflate( R.layout.pull_to_update_header , container , false );
		mUltraList = new PullToUpdateList( getActivity() , inflater , 0 , this , this , this , true , false , lPullHeader ,
				R.id.pull_to_refresh_arrow , R.id.pull_to_refresh_progress , R.id.pull_to_refresh_text , R.string.STR_PULL_TO_REFRESH ,
				R.string.STR_RELEASE_TO_REFRESH , R.string.STR_DATA_FAKE_LOADING_IN_PROGRESS , this , null , null );
		mUltraList.initialize();
		return mUltraList;
	}
}
