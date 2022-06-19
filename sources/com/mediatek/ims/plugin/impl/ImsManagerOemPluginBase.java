package com.mediatek.ims.plugin.impl;

import android.content.Context;
import android.telephony.Rlog;
import com.android.ims.ImsManager;
import com.mediatek.ims.plugin.ImsManagerOemPlugin;

public class ImsManagerOemPluginBase implements ImsManagerOemPlugin {
    private static final String TAG = "ImsManagerOemPluginBase";

    public boolean hasPlugin() {
        return false;
    }

    public boolean isWfcSupport() {
        Rlog.d(TAG, "default isWfcSupport");
        return true;
    }

    public void updateImsServiceConfig(Context context, int phoneId) {
        ImsManager imsManager = ImsManager.getInstance(context, phoneId);
        if (imsManager != null) {
            imsManager.updateImsServiceConfig();
        }
    }
}
