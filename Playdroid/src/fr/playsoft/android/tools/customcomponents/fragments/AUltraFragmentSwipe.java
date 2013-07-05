package fr.playsoft.android.tools.customcomponents.fragments;

import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import fr.playsoft.android.tools.customcomponents.UltraSwipe;
import fr.playsoft.android.tools.customcomponents.UltraSwipeIndicator;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraSwipeDataProvider;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraSwipeOnClickListener;

/**
 * Abstract Fragment Class which constains fully initialized UltraSwipe
 * 
 * @author Klawikowski
 * 
 */

public abstract class AUltraFragmentSwipe extends AUltraFragment implements IUltraSwipeDataProvider , IUltraSwipeOnClickListener ,
		OnPageChangeListener
{
	
	protected UltraSwipe mUltraSwipe;
	
	@Override
	public View onCreateView( LayoutInflater inflater , ViewGroup container , Bundle savedInstanceState )
	{
		
		mUltraSwipe = new UltraSwipe( getActivity() , inflater , 0 , this , this , null , this );
		RelativeLayout lContainer = new RelativeLayout( getActivity() );
		lContainer.setId( 123 );
		
		LinearLayout lSwipeIndicator = new UltraSwipeIndicator( getActivity() , 14 );
		lSwipeIndicator.setId( 1234 );
		android.widget.RelativeLayout.LayoutParams lSwipeIndicatorParams = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT , android.view.ViewGroup.LayoutParams.WRAP_CONTENT );
		
		lSwipeIndicatorParams.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM , 123 );
		lSwipeIndicator.setGravity( Gravity.CENTER );
		lSwipeIndicator.setLayoutParams( lSwipeIndicatorParams );
		lContainer.addView( lSwipeIndicator );
		
		lContainer.addView( mUltraSwipe );
		android.widget.RelativeLayout.LayoutParams lSwipeParams = new RelativeLayout.LayoutParams( android.view.ViewGroup.LayoutParams.FILL_PARENT ,
				android.view.ViewGroup.LayoutParams.FILL_PARENT );
		lSwipeParams.addRule( RelativeLayout.ABOVE , 1234 );
		mUltraSwipe.setLayoutParams( lSwipeParams );
		mUltraSwipe.setId( 321 );
		
		mUltraSwipe.setPageIndicator( (UltraSwipeIndicator) lSwipeIndicator );
		return lContainer;
	}
	
	@Override
	public void onPageSelected( int arg0 )
	{
	}
	
	@Override
	public void onPageScrollStateChanged( int arg0 )
	{
	}
	
	@Override
	public void onPageScrolled( int arg0 , float arg1 , int arg2 )
	{
	}
	
}
