package fr.playsoft.android.tools.customcomponents.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import fr.playsoft.android.tools.customcomponents.fragments.interfaces.IUltraFragment;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraActivityFragmentActionListener;
import fr.playsoft.android.tools.engine.ManagerEvent;
import fr.playsoft.android.tools.engine.interfaces.IManagerEventClient;

/**
 * Abstract class for Fragment class.
 * Constains all needed tools like Toasts, Loading Dialogs
 * Automatically registers to ManagerEvent class
 * 
 * @author Klawikowski
 * 
 */
public abstract class AUltraFragment extends Fragment implements IManagerEventClient , IUltraFragment
{
	
	protected Handler mHandler;
	protected FragmentManager mManager;
	
	/** Toast to display messages **/
	private Toast mToast;
	
	protected IUltraActivityFragmentActionListener mUltraActivityListener;
	
	@Override
	public View onCreateView( LayoutInflater inflater , ViewGroup container , Bundle savedInstanceState )
	{
		return inflater.inflate( getFragmentLayout() , container , false );
	}
	
	@Override
	public String getFragmentTag()
	{
		return String.valueOf( getFragmentLayout() );
	}
	
	@Override
	public void onAttach( Activity activity )
	{
		super.onAttach( activity );
		mHandler = new Handler();
		mUltraActivityListener = (IUltraActivityFragmentActionListener) activity;
		mToast = Toast.makeText( getActivity() , "" , Toast.LENGTH_SHORT );
		ManagerEvent.getInstance().addClient( this );
	}
	
	@Override
	public void onActivityCreated( Bundle savedInstanceState )
	{
		super.onActivityCreated( savedInstanceState );
		setupView();
	}
	
	/**
	 * Adds Fragment containing other fragments to chosen Layout Container
	 * 
	 * @param pFragment Fragment
	 * @param pContainerId Reference to Layout Container (R.id.example_container)
	 */
	protected void addNestedFragment( Fragment pFragment , int pContainerId )
	{
		FragmentManager lManager = getChildFragmentManager();
		FragmentTransaction lTransaction = lManager.beginTransaction();
		lTransaction.replace( pContainerId , pFragment );
		lTransaction.commit();
	}
	
	/**
	 * Shows Toast with proper message
	 * 
	 * @param pMessage int Resources value
	 */
	protected void showToastText( int pMessage )
	{
		mToast.setText( getString( pMessage ) );
		mToast.show();
	}
	
	/**
	 * Shows Toast with proper message
	 * 
	 * @param pMessage
	 */
	protected void showToastText( String pMessage )
	{
		mToast.setText( pMessage );
		mToast.show();
	}
	
}
