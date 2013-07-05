package fr.playsoft.android.tools.customcomponents.fragments.interfaces;

/**
 * Interface for UltraFragment
 * 
 * @author Klawikowski
 * 
 */
public interface IUltraFragment
{
	/**
	 * Should return Id of layout which should be used as layout for this fragment
	 * 
	 * @return R.layout. reference
	 */
	int getFragmentLayout();
	
	/**
	 * Place where all view Objects should be initialized
	 */
	void setupView();
	
	/**
	 * Method which should return Fragment tag
	 * 
	 * @return String Fragment Tag
	 */
	String getFragmentTag();
}
