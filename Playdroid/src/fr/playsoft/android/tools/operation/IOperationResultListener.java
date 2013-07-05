package fr.playsoft.android.tools.operation;

/**
 * Callback interface for posting results of each Operation.
 * 
 * @author Olgierd Uzieblo
 */
public interface IOperationResultListener
{
	/**
	 * Launched when operation has finished successfully.
	 * 
	 * @param pOperation Operation that has just finished.
	 * @param pResult Any object returned as operation result.
	 */
	public void onOperationSuccess( Operation pOperation , Object pResult );
	
	/**
	 * Launched when operation has failed miserably.
	 * 
	 * @param pOperation Operation that has just finished.
	 * @param pResult Any object returned as operation result.
	 */
	public void onOperationFailed( Operation pOperation , Object pResult );
}
