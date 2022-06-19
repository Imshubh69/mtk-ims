package com.mediatek.ims.plugin.impl;

import android.content.Context;
import com.mediatek.ims.plugin.ImsCallOemPlugin;

public class ImsCallOemPluginBase implements ImsCallOemPlugin {
    private static final String TAG = "ImsCallOemPluginBase";
    private Context mContext;

    public ImsCallOemPluginBase(Context context) {
        this.mContext = context;
    }

    public String getVTUsageAction() {
        return "com.mediatek.ims.ACTION_VT_DATA_USAGE";
    }

    public String getVTUsagePermission() {
        return "android.permission.READ_NETWORK_USAGE_HISTORY";
    }

    public boolean needHangupOtherCallWhenEccDialing() {
        return false;
    }

    public boolean needTurnOnVolteBeforeE911() {
        return true;
    }

    public boolean needTurnOffVolteAfterE911() {
        return true;
    }

    public boolean isUpdateViwifiFeatureValueAsViLTE() {
        return false;
    }

    public boolean needReportCallTerminatedForFdn() {
        return false;
    }

    public boolean useNormalDialForEmergencyCall() {
        return false;
    }

    public boolean needNotifyBadBitRate() {
        return true;
    }

    public boolean alwaysSetPreviewSurface() {
        return true;
    }
}
