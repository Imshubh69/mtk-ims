package com.mediatek.wfo.impl;

import android.app.Application;
import android.os.UserHandle;
import android.util.Log;

public class WfoApp extends Application {
    private static final String TAG = "WfoApp";
    private static final String VOWIFI_SERVIVE = "vowifi";

    public void onCreate() {
        if (UserHandle.myUserId() == 0) {
            super.onCreate();
            Log.d(TAG, "onCreate()");
        }
    }

    public void onTerminate() {
        Log.d(TAG, "WfoApp onTerminate");
    }
}
