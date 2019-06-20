package com.amorenew.alps_finger_scan.fingerscan;

import android.util.Log;

/**
 * Created by admin on 2016/11/25.
 */
public class Loger {

    public static boolean debugFlag = false;

    public static void e(String tag, Object object) {
        if (debugFlag)
            Log.e(tag, "" + object.toString());
    }
}
