package com.jakubkaleta.checklist.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.view.View;

/**
 * Contains utility methods for unit tests
 * @author Jakub Kaleta
 *
 */
public class Util
{
    /**
     * Sends a key press to the instrumented instance
     * @param mInst The instrumentation to receive key press
     * @param keycode The key to press
     */
    public static void press(Instrumentation mInst, int keycode) {
        mInst.sendKeyDownUpSync(keycode);
    }

    /**
     * Performs a tap on the selected view
     * @param testCase The executing text case
     * @param activity Activity tested
     * @param viewId Id of the view to tap
     * @return True if view was tapped, false otherwise
     */
    public static boolean tapView(InstrumentationTestCase testCase, Activity activity, int viewId) {
        View view = activity.findViewById(viewId);
        if(view != null) {
            TouchUtils.clickView(testCase, view);
            return true;
        }
        return false;
    }

}
