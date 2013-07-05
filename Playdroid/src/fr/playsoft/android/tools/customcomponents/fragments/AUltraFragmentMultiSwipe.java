package fr.playsoft.android.tools.customcomponents.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import fr.playsoft.android.tools.customcomponents.UltraMultiSwipe;
import fr.playsoft.android.tools.customcomponents.UltraSwipeIndicator;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraMultiSwipeDataProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraMultiSwipeOnClickListener;

/**
 * Abstract Fragment Class which constains fully initialized UltraMultiSwipe
 * 
 * @author Klawikowski
 * 
 */
public abstract class AUltraFragmentMultiSwipe extends AUltraFragment implements IUltraMultiSwipeDataProvider , IUltraMultiSwipeOnClickListener
{
	
	protected UltraMultiSwipe mUltraMultiSwipe;
	
	@Override
	public View onCreateView( LayoutInflater inflater , ViewGroup container , Bundle savedInstanceState )
	{
		
		mUltraMultiSwipe = new UltraMultiSwipe( getActivity() , inflater , getChildFragmentManager() , 0 , this , this , null );
		RelativeLayout lContainer = new RelativeLayout( getActivity() );
		mUltraMultiSwipe.setLayoutParams( new RelativeLayout.LayoutParams( LayoutParams.FILL_PARENT , LayoutParams.FILL_PARENT ) );
		
		android.widget.RelativeLayout.LayoutParams lSwipeIndicatorLayoutParams = new RelativeLayout.LayoutParams( LayoutParams.FILL_PARENT ,
				LayoutParams.WRAP_CONTENT );
		lSwipeIndicatorLayoutParams.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM );
		LinearLayout lSwipeIndicator = new UltraSwipeIndicator( getActivity() , 5 );
		lSwipeIndicator.setGravity( Gravity.CENTER );
		lSwipeIndicator.setLayoutParams( lSwipeIndicatorLayoutParams );
		lContainer.addView( mUltraMultiSwipe );
		lContainer.addView( lSwipeIndicator );
		mUltraMultiSwipe.setPageIndicator( (UltraSwipeIndicator) lSwipeIndicator );
		return lContainer;
	}
}
