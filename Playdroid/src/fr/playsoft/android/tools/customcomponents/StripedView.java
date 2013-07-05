package fr.playsoft.android.tools.customcomponents;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Class which creates a striped background for any View
 * 
 * @author Klawikowski
 * 
 */
public class StripedView extends Drawable
{
	private int mWidth;
	private int mHeight;
	private Paint mPaint;
	
	public StripedView( Context context , View pView )
	{
		mPaint = new Paint();
		mPaint.setColor( 0x000 );
		mWidth = pView.getMeasuredWidth();
		mHeight = pView.getMeasuredHeight();
	}
	
	public StripedView( Context context , int pHeight , int pWidth )
	{
		mPaint = new Paint();
		mPaint.setColor( 0x000 );
		mWidth = pWidth;
		mHeight = pHeight;
	}
	
	@Override
	public void draw( Canvas canvas )
	{
		for ( int i = 0; i < mWidth; i += 5 )
		{
			canvas.drawLine( i , 0 , mHeight + i , mHeight , mPaint );
			if ( i != 0 && i < mHeight )
			{
				canvas.drawLine( 0 , i , mHeight - i , mHeight , mPaint );
			}
		}
	}
	
	@Override
	public int getOpacity()
	{
		return 0;
	}
	
	@Override
	public void setAlpha( int alpha )
	{
	}
	
	@Override
	public void setColorFilter( ColorFilter cf )
	{
	}
}
