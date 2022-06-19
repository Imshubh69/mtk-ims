package com.mediatek.ims.plugin.impl;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.mediatek.ims.plugin.ImsCallPlugin;

public class ImsCallPluginBase implements ImsCallPlugin {
    private static final String MULTI_IMS_SUPPORT = "persist.vendor.mims_support";
    private static final String PROPERTY_CAPABILITY_SWITCH = "persist.vendor.radio.simswitch";
    private static final String TAG = "ImsConfigPluginBase";
    private Context mContext;

    public ImsCallPluginBase(Context context) {
        this.mContext = context;
    }

    public boolean isSupportMims() {
        return SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) > 1;
    }

    public int setImsFwkRequest(int request) {
        return request;
    }

    public int getRealRequest(int request) {
        return request;
    }

    public boolean isImsFwkRequest(int request) {
        return false;
    }

    public int getUpgradeCancelFlag() {
        return 65536;
    }

    public int getUpgradeCancelTimeoutFlag() {
        return 0;
    }

    public int getMainCapabilityPhoneId() {
        int phoneId = SystemProperties.getInt("persist.vendor.radio.simswitch", 1) - 1;
        Log.d(TAG, "getMainCapabilityPhoneId " + phoneId);
        return phoneId;
    }

    public boolean isCapabilitySwitching() {
        return false;
    }

    public int getSimCardState(int slotId) {
        return TelephonyManager.getDefault().getSimCardState();
    }

    public int getSimApplicationState(int slotId) {
        return TelephonyManager.getDefault().getSimApplicationState();
    }
}
