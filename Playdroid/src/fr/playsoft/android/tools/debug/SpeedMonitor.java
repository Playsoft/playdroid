package fr.playsoft.android.tools.debug;

import java.util.ArrayList;

/**
 * Debug tool for speed measurements.
 * 
 * @author Olgierd Uzieblo
 */
public class SpeedMonitor
{
	private static String TAG = "SpeedMonitor";
	
	private static class MeasurementData
	{
		private String mId;
		private long mLastStart;
		private long mLastStop;
		private long mNumMeasures;
		private long mTotalTime;
		private long mMaxTime;
		private long mMinTime = -1;
		
		MeasurementData( String pId )
		{
			mId = pId;
		}
		
		public synchronized void startMeasurement( long pStart )
		{
			mLastStart = pStart;
		}
		
		public synchronized void stopMeasurement( long pStop )
		{
			mLastStop = pStop;
			long mDuration = mLastStop - mLastStart;
			mNumMeasures++;
			mTotalTime += mDuration;
			if ( mDuration > mMaxTime )
			{
				mMaxTime = mDuration;
			}
			if ( ( mMinTime == -1 ) || ( mDuration < mMinTime ) )
			{
				mMinTime = mDuration;
			}
		}
		
		public synchronized void printResult()
		{
			if ( mNumMeasures > 0 )
			{
				Log.d( TAG , "*********************************************" );
				Log.d( TAG , "Measurement id = " + mId );
				Log.d( TAG , "Longest time = " + mMaxTime );
				Log.d( TAG , "Shortest time = " + mMinTime );
				Log.d( TAG , "Average time = " + ( mTotalTime / mNumMeasures ) );
				Log.d( TAG , "Number of measures = " + mNumMeasures );
			}
		}
	}
	
	private static ArrayList< MeasurementData > sMeasurements = new ArrayList< SpeedMonitor.MeasurementData >();
	private static ArrayList< String > sMeasurementsIds = new ArrayList< String >();
	
	public static synchronized void startMeasure( String pId )
	{
		int index = sMeasurementsIds.indexOf( pId );
		if ( index == -1 )
		{
			sMeasurementsIds.add( pId );
			sMeasurements.add( new MeasurementData( pId ) );
			index = sMeasurementsIds.size() - 1;
		}
		sMeasurements.get( index ).startMeasurement( System.currentTimeMillis() );
	}
	
	public static synchronized void finishMeasure( String pId )
	{
		int index = sMeasurementsIds.indexOf( pId );
		if ( index != -1 )
		{
			sMeasurements.get( index ).stopMeasurement( System.currentTimeMillis() );
		}
	}
	
	public static synchronized void printAllStats()
	{
		long lTotalTime = 0;
		for ( int i = 0; i < sMeasurements.size(); i++ )
		{
			lTotalTime += sMeasurements.get( i ).mTotalTime;
			sMeasurements.get( i ).printResult();
		}
		Log.d( TAG , "---------------------------------------------------" );
		Log.d( TAG , "Total time spent in above methods: " + lTotalTime );
		Log.d( TAG , "---------------------------------------------------" );
	}
	
	public static synchronized void resetStats()
	{
		sMeasurements = new ArrayList< SpeedMonitor.MeasurementData >();
		sMeasurementsIds = new ArrayList< String >();
	}
}
