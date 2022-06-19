package com.mediatek.ims.rcse;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class LauncherUtils {
    private static final String LOG_TAG = "LauncherUtilsMtk";

    public static void launchRcsService(Context context, boolean boot, boolean user) {
        Log.d(LOG_TAG, " launchRcsService " + context);
        if (SystemProperties.getInt("persist.vendor.mtk_rcs_ua_support", 0) == 1) {
            Log.d(LOG_TAG, "launchRcsService");
            int simState = ((TelephonyManager) context.getSystemService("phone")).getSimState();
            Log.d(LOG_TAG, " launchRcsService simState " + simState);
            UaServiceManager.getInstance().startService(context, getCurrentUserPhoneId());
            return;
        }
        Log.d(LOG_TAG, "don't launchRcsService");
    }

    public static int getCurrentUserPhoneId() {
        return SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
    }
}
