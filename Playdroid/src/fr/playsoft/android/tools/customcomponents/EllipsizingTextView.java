package fr.playsoft.android.tools.customcomponents;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Custom TextView with extended ellipsizing functions
 * 
 * @author Klawikowski
 * 
 */
public class EllipsizingTextView extends TextView
{
	private static final String ELLIPSIS = "…";
	private static final Pattern DEFAULT_END_PUNCTUATION = Pattern.compile( "[\\.,…;\\:\\s]*$" , Pattern.DOTALL );
	
	/**
	 * Basic Interface returning OnEllipsizeStateChange listener call
	 * 
	 * @author Klawikowski
	 * 
	 */
	public interface EllipsizeListener
	{
		void OnEllipsizeStateChanged( boolean pIsEllipsized );
	}
	
	private final List< EllipsizeListener > mEllipsizeListeners = new ArrayList< EllipsizeListener >();
	private boolean mIsEllipsized;
	private boolean mIsStale;
	private boolean mProgrammaticChange;
	private String mFullText;
	private int mMaxLines;
	private float mLineSpacingMultiplier = 1.0f;
	private float mLineAdditionalVerticalPadding = 0.0f;
	/**
	 * The end punctuation which will be removed when appending #ELLIPSIS.
	 */
	private Pattern mEndPunctuationPattern;
	
	public EllipsizingTextView( Context pContext )
	{
		this( pContext , null );
	}
	
	public EllipsizingTextView( Context pContext , AttributeSet pAttrs )
	{
		this( pContext , pAttrs , 0 );
	}
	
	public EllipsizingTextView( Context pContext , AttributeSet pAttrs , int pDefStyle )
	{
		super( pContext , pAttrs , pDefStyle );
		super.setEllipsize( null );
		TypedArray lTypedArray = pContext.obtainStyledAttributes( pAttrs , new int[]
		{
			android.R.attr.maxLines
		} );
		setMaxLines( lTypedArray.getInt( 0 , Integer.MAX_VALUE ) );
		setEndPunctuationPattern( DEFAULT_END_PUNCTUATION );
		lTypedArray.recycle();
	}
	
	public void setEndPunctuationPattern( Pattern pPattern )
	{
		this.mEndPunctuationPattern = pPattern;
	}
	
	public void addEllipsizeListener( EllipsizeListener pListener )
	{
		if ( pListener == null )
		{
			throw new NullPointerException();
		}
		mEllipsizeListeners.add( pListener );
	}
	
	public void removeEllipsizeListener( EllipsizeListener pListener )
	{
		mEllipsizeListeners.remove( pListener );
	}
	
	/**
	 * Returns bool about text being or not ellipsized
	 * 
	 * @return True is text was ellipsized
	 */
	public boolean isEllipsized()
	{
		return mIsEllipsized;
	}
	
	@Override
	public void setMaxLines( int maxLines )
	{
		super.setMaxLines( maxLines );
		this.mMaxLines = maxLines;
		mIsStale = true;
	}
	
	@SuppressLint( "Override" )
	public int getMaxLines()
	{
		return mMaxLines;
	}
	
	public boolean ellipsizingLastFullyVisibleLine()
	{
		return mMaxLines == Integer.MAX_VALUE;
	}
	
	@Override
	public void setLineSpacing( float add , float mult )
	{
		this.mLineAdditionalVerticalPadding = add;
		this.mLineSpacingMultiplier = mult;
		super.setLineSpacing( add , mult );
	}
	
	@Override
	protected void onTextChanged( CharSequence text , int start , int before , int after )
	{
		super.onTextChanged( text , start , before , after );
		if ( !mProgrammaticChange )
		{
			mFullText = text.toString();
			mIsStale = true;
		}
	}
	
	@Override
	protected void onSizeChanged( int w , int h , int oldw , int oldh )
	{
		super.onSizeChanged( w , h , oldw , oldh );
		if ( ellipsizingLastFullyVisibleLine() )
		{
			mIsStale = true;
		}
	}
	
	@Override
	public void setPadding( int left , int top , int right , int bottom )
	{
		super.setPadding( left , top , right , bottom );
		if ( ellipsizingLastFullyVisibleLine() )
		{
			mIsStale = true;
		}
	}
	
	@Override
	protected void onDraw( Canvas canvas )
	{
		if ( mIsStale )
		{
			resetText();
		}
		super.onDraw( canvas );
	}
	
	private void resetText()
	{
		String lWorkingText = mFullText;
		boolean lIsEllipsized = false;
		Layout lLayout = createWorkingLayout( lWorkingText );
		int lLinesCount = getLinesCount();
		if ( lLayout.getLineCount() > lLinesCount )
		{
			// We have more lines of text than we are allowed to display.
			lWorkingText = mFullText.substring( 0 , lLayout.getLineEnd( lLinesCount - 1 ) ).trim();
			while( createWorkingLayout( lWorkingText + ELLIPSIS ).getLineCount() > lLinesCount )
			{
				int lastSpace = lWorkingText.lastIndexOf( ' ' );
				if ( lastSpace == -1 )
				{
					break;
				}
				lWorkingText = lWorkingText.substring( 0 , lastSpace );
			}
			// We should do this in the loop above, but it's cheaper this way.
			lWorkingText = mEndPunctuationPattern.matcher( lWorkingText ).replaceFirst( "" );
			lWorkingText = lWorkingText + ELLIPSIS;
			lIsEllipsized = true;
		}
		if ( !lWorkingText.equals( getText() ) )
		{
			mProgrammaticChange = true;
			try
			{
				setText( lWorkingText );
			}
			finally
			{
				mProgrammaticChange = false;
			}
		}
		mIsStale = false;
		if ( lIsEllipsized != mIsEllipsized )
		{
			mIsEllipsized = lIsEllipsized;
			for ( EllipsizeListener listener : mEllipsizeListeners )
			{
				listener.OnEllipsizeStateChanged( lIsEllipsized );
			}
		}
	}
	
	/**
	 * Get how many lines of text we are allowed to display.
	 */
	private int getLinesCount()
	{
		if ( ellipsizingLastFullyVisibleLine() )
		{
			int lFullyVisibleLinesCount = getFullyVisibleLinesCount();
			if ( lFullyVisibleLinesCount == -1 )
			{
				return 1;
			}
			else
			{
				return lFullyVisibleLinesCount;
			}
		}
		else
		{
			return mMaxLines;
		}
	}
	
	/**
	 * Get how many lines of text we can display so their full height is visible.
	 */
	private int getFullyVisibleLinesCount()
	{
		Layout lLayout = createWorkingLayout( "" );
		int lHeight = getHeight() - getPaddingTop() - getPaddingBottom();
		int lLineHeight = lLayout.getLineBottom( 0 );
		return lHeight / lLineHeight;
	}
	
	private Layout createWorkingLayout( String workingText )
	{
		return new StaticLayout( workingText , getPaint() , getWidth() - getPaddingLeft() - getPaddingRight() , Alignment.ALIGN_NORMAL ,
				mLineSpacingMultiplier , mLineAdditionalVerticalPadding , false /* includepad */);
	}
	
	@Override
	public void setEllipsize( TruncateAt where )
	{
	}
}
