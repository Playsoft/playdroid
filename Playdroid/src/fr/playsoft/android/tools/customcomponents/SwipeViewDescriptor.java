package fr.playsoft.android.tools.customcomponents;

import java.util.ArrayList;

import android.view.LayoutInflater;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraSwipeCellOnClickListener;

/**
 * Class which wraps all data needed to show UltraSwipe in UltraList
 * 
 * @author Klawikowski
 * 
 */
public class SwipeViewDescriptor
{
	public ArrayList< UltraSwipeCell > mSwipeData = new ArrayList< UltraSwipeCell >();
	public int mViewId;
	public IUltraSwipeCellOnClickListener mListener;
	public LayoutInflater mInflater;
	public boolean mIsAutomaticSwipe;
	
	public SwipeViewDescriptor( ArrayList< UltraSwipeCell > pSwipeData , IUltraSwipeCellOnClickListener pListener , LayoutInflater pInflater ,
			boolean pIsAutomaticSwipe )
	{
		mSwipeData.addAll( pSwipeData );
		mListener = pListener;
		mInflater = pInflater;
		mIsAutomaticSwipe = pIsAutomaticSwipe;
	}
	
}
