package fr.playsoft.android.tools.customcomponents;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Toast;
import fr.playsoft.android.tools.customcomponents.fragments.AUltraFragment;
import fr.playsoft.android.tools.customcomponents.interfaces.IUltraActivityFragmentActionListener;
import fr.playsoft.android.tools.debug.Log;
import fr.playsoft.android.tools.engine.ManagerEvent;
import fr.playsoft.android.tools.engine.interfaces.IManagerEventClient;

/**
 * Abstract class for FragmentActivity class.
 * Constains all needed tools like Toasts, Loading Dialogs
 * Automatically registers to ManagerEvent class
 * 
 * @author Klawikowski
 * 
 */
@SuppressLint( "ShowToast" )
public abstract class AUltraActivity extends FragmentActivity implements OnDismissListener , IUltraActivityFragmentActionListener , IUltraActivity ,
		IManagerEventClient
{
	/** Toast to display messages **/
	private Toast mToast;
	
	/** Dialog to display progress **/
	private Dialog mDialog;
	
	/** Tools needed to create add fragment action in activity **/
	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	
	private Handler mHandler;
	
	@Override
	protected void onCreate( Bundle arg0 )
	{
		super.onCreate( arg0 );
		mToast = Toast.makeText( getApplicationContext() , "" , Toast.LENGTH_SHORT );
		mFragmentManager = getSupportFragmentManager();
		setContentView( getLayoutId() );
		ManagerEvent.getInstance().addClient( this );
		mHandler = new Handler();
		setupView();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		initializeLayoutObjects();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		ManagerEvent.getInstance().removeClient( this );
	}
	
	/**
	 * Adds Fragment to chosen Layout Container
	 * 
	 * @param pFragment Fragment
	 * @param pContainerId Reference to Layout Container (R.id.example_container)
	 */
	protected void addFragment( Fragment pFragment , int pContainerId )
	{
		if ( ( mFragmentManager.findFragmentByTag( ( (AUltraFragment) pFragment ).getFragmentTag() ) != null ) )
		{
			return;
		}
		mFragmentTransaction = mFragmentManager.beginTransaction();
		mFragmentTransaction.add( pContainerId , pFragment , ( (AUltraFragment) pFragment ).getFragmentTag() );
		mFragmentTransaction.commit();
	}
	
	/**
	 * Removes Fragment from View
	 * 
	 * @param pFragment Fragment to remove
	 */
	protected void removeFragment( Fragment pFragment )
	{
		mFragmentTransaction = mFragmentManager.beginTransaction();
		mFragmentTransaction.remove( pFragment );
		mFragmentTransaction.commit();
	}
	
	/**
	 * Replaces all fragments in chosen view, and adds new fragment to it
	 * 
	 * @param pFragment
	 * @param pContainerId
	 */
	protected void replaceFragment( Fragment pFragment , int pContainerId )
	{
		Log.i( "Replacing Fragment" );
		mFragmentTransaction = mFragmentManager.beginTransaction();
		mFragmentTransaction.replace( pContainerId , pFragment );
		mFragmentTransaction.commit();
	}
	
	/**
	 * Shows Toast with proper message
	 * 
	 * @param pMessage int Resources value
	 */
	protected void showToastText( final int pMessage )
	{
		mHandler.post( new Runnable()
		{
			
			@Override
			public void run()
			{
				mToast.setText( getString( pMessage ) );
				mToast.show();
			}
		} );
	}
	
	/**
	 * Shows Toast with proper message
	 * 
	 * @param pMessage
	 */
	protected void showToastText( final String pMessage )
	{
		mHandler.post( new Runnable()
		{
			
			@Override
			public void run()
			{
				mToast.setText( pMessage );
				mToast.show();
			}
		} );
	}
	
	/**
	 * Shows Progress Dialog
	 */
	protected void showProgressDialog()
	{
		mDialog = new Dialog( this );
		mDialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
		
		LinearLayout lDialogLayout = new LinearLayout( this );
		LayoutParams lDialogLayoutPreferences = new LayoutParams( android.view.ViewGroup.LayoutParams.WRAP_CONTENT ,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT );
		lDialogLayoutPreferences.gravity = Gravity.CENTER;
		ProgressBar lDialogProgress = new ProgressBar( this );
		
		lDialogLayout.addView( lDialogProgress );
		
		mDialog.setContentView( lDialogLayout );
		mDialog.setCancelable( true );
		mDialog.setOnDismissListener( new DialogInterface.OnDismissListener()
		{
			@Override
			public void onDismiss( DialogInterface dialog )
			{
				
			}
		} );
		mDialog.show();
	}
	
	/**
	 * Cancels currently visible Progress Dialog
	 */
	protected void cancelProgressDialog()
	{
		if ( mDialog != null )
		{
			if ( mDialog.isShowing() )
			{
				mDialog.cancel();
			}
		}
	}
	
	/**
	 * Hides Soft Keyboard displayed by currently focused EditText
	 * 
	 * @param pEditText EditText currenly focused EditText
	 */
	protected void hideSoftKeyboard( EditText pEditText )
	{
		InputMethodManager lInputMethodManager = (InputMethodManager) pEditText.getContext().getSystemService( Context.INPUT_METHOD_SERVICE );
		lInputMethodManager.hideSoftInputFromWindow( pEditText.getWindowToken() , 0 );
	}
	
	/**
	 * Shows Soft Keyboard for specified EditText
	 * 
	 * @param pEditText
	 */
	protected void showSoftKeyboard( EditText pEditText )
	{
		InputMethodManager lInputMethodManager = (InputMethodManager) pEditText.getContext().getSystemService( Context.INPUT_METHOD_SERVICE );
		lInputMethodManager.showSoftInput( pEditText , 0 );
		pEditText.setSelected( true );
	}
	
	@Override
	public void onDismiss( DialogInterface dialog )
	{
	}
	
}
