package com.jakubkaleta.checklist;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ViewFlipper;

/**
 * Works around Android Bug 6191 by catching IllegalArgumentException after
 * detached from the window.
 * 
 * @author Eric Burke (eric@squareup.com) http://pastie.org/1086467
 */
public class SafeViewFlipper extends ViewFlipper
{
	private final String TAG = this.getClass().toString();
	
	/**
	 * Initializes a new instance of SafeViewFlipper
	 * @param context The context to operate in
	 */
	public SafeViewFlipper(Context context)
	{
		super(context);
	}
	
	/**
	 * Initializes a new instance of SafeViewFlipper
	 * @param context Context to operate in
	 * @param attrs Attributes to apply to the new instance
	 */
	public SafeViewFlipper(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	/**
	 * Workaround for Android Bug 6191:
	 * http://code.google.com/p/android/issues/detail?id=6191
	 * <p/>
	 * ViewFlipper occasionally throws an IllegalArgumentException after screen
	 * rotations.
	 */
	@Override
	protected void onDetachedFromWindow()
	{
		try
		{
			super.onDetachedFromWindow();
		}
		catch (IllegalArgumentException e)
		{
			Log.d(TAG, "SafeViewFlipper ignoring IllegalArgumentException");

			// Call stopFlipping() in order to kick off updateRunning()
			stopFlipping();
		}
	}
}
