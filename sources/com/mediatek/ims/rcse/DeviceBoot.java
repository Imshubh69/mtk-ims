package com.mediatek.ims.rcse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DeviceBoot extends BroadcastReceiver {
    private static final String LOG_TAG = "DeviceBootMtk";

    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, " intent " + intent);
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            LauncherUtils.launchRcsService(context, true, false);
        }
    }
}
