package fr.playsoft.android.tools.customcomponents.interfaces;

/**
 * Configuration of various UltraListCells.
 * 
 * @author Olgierd Uzieblo
 */
public interface IUltraListCellConfig
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
	
	/**
	 * Element type ImageButton - this element is an ImageButton with custom on click listener.
	 * Uses a IUltraListCellImageButtonClick object as data.
	 */
	public static final int ELEMENT_TYPE_IMAGEBUTTON = 2;
	
	/**
	 * Element type CheckBox - this element is a CheckBox with custom on checked listener.
	 * Uses a IUltraListCellCheckBoxChecked object as data.
	 */
	public static final int ELEMENT_TYPE_CHECKBOX = 3;
	
	public static final int ELEMENT_TYPE_SWIPE = 4;
	
	public static final int ELEMENT_TYPE_SWIPE_INDICATOR = 5;
	
	public static final int ELEMENT_TYPE_RADIO_BUTTON = 6;
	
	/**
	 * Cell elements configuration data.
	 */
	/** Data with elements type **/
	public static final int DATA_ELEMENT_TYPE = 0;
	
	/** Data with elements view id **/
	public static final int DATA_ELEMENT_VIEW = 1;
	
	/**
	 * Gets number of possible cell types.
	 */
	public int getNumberOfCellTypes();
	
	/**
	 * Gets element configuration.
	 * 
	 * @param pElementId Element id - one of ints returned by getElementsForCellType.
	 * @return Array with element types and their view ids. See example in ToolsDemo.
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
