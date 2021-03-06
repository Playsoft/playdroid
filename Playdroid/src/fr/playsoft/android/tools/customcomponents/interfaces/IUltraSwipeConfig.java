package fr.playsoft.android.tools.customcomponents.interfaces;

/**
 * Configuration of various UltraSwipeCells
 * 
 * @author Klawikowski
 * 
 */
public interface IUltraSwipeConfig
{
	/**
	 * Various configurable cell element types.
	 */
	/**
	 * Element type String - this element is a TextView with dynamic String or Spanned.
	 * Uses a String object as data.
	 */
	public static final int ELEMENT_TYPE_STRING = 0;
	/**
	 * Element type Image - this element is an ImageView with dynamic image.
	 * Uses a ResourceDescriptor object as data.
	 */
	public static final int ELEMENT_TYPE_IMAGE = 1;
	
	/** Data with elements type **/
	public static final int DATA_ELEMENT_TYPE = 0;
	
	/** Data with elements view id **/
	public static final int DATA_ELEMENT_VIEW = 1;
	
	/**
	 * Gets element configuration.
	 * 
	 * @param pElementId Element id - one of ints returned by getElementsForCellType.
	 * @return Array with element types and their view ids.
	 */
	public int[] getElementConfiguration( int pElementId );
	
	/**
	 * Gets cell layouts from R.layout.
	 * 
	 * @param pCellTypeId Cell type id.
	 * @return Cell layout id for cells of this type.
	 */
	public int getCellLayoutId( int pCellTypeId );
}
