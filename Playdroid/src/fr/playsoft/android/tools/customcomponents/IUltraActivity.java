package fr.playsoft.android.tools.customcomponents;

/**
 * Gloval Interface for AUltraActivity
 * 
 * @author Klawikowski
 * 
 */
public interface IUltraActivity
{
	/**
	 * Should return Layout id which should be used in this Activity
	 * 
	 * @return
	 */
	int getLayoutId();
	
	/**
	 * Implementation of this interface should contain implementation, initialization and logic of all layout objects
	 */
	void setupView();
	
	/**
	 * Should contain initialization of layout components
	 */
	void initializeLayoutObjects();
	
}
