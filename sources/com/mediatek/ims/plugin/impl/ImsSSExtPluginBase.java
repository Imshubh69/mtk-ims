package com.mediatek.ims.plugin.impl;

import android.content.Context;
import android.telephony.ims.ImsCallForwardInfo;
import android.util.Log;
import com.android.internal.telephony.CallForwardInfo;
import com.mediatek.ims.ImsUtImpl;
import com.mediatek.ims.plugin.ImsSSExtPlugin;

public class ImsSSExtPluginBase implements ImsSSExtPlugin {
    private static final String TAG = "ImsSSExtPluginBase";
    private Context mContext;

    public ImsSSExtPluginBase(Context context) {
        this.mContext = context;
    }

    public ImsCallForwardInfo[] getImsCallForwardInfo(CallForwardInfo[] info) {
        ImsCallForwardInfo[] imsCfInfo = new ImsCallForwardInfo[1];
        if (info != null) {
            for (int i = 0; i < info.length; i++) {
                Log.d(TAG, "getImsCallForwardInfo: info[" + i + "] = " + info[i]);
                if (info[i].serviceClass == 1) {
                    imsCfInfo[0] = new ImsCallForwardInfo();
                    imsCfInfo[0].mCondition = ImsUtImpl.getConditionFromCFReason(info[i].reason);
                    imsCfInfo[0].mStatus = info[i].status;
                    imsCfInfo[0].mServiceClass = info[i].serviceClass;
                    imsCfInfo[0].mToA = info[i].toa;
                    imsCfInfo[0].mNumber = info[i].number;
                    imsCfInfo[0].mTimeSeconds = info[i].timeSeconds;
                    return imsCfInfo;
                }
            }
            imsCfInfo[0] = new ImsCallForwardInfo();
            imsCfInfo[0].mCondition = ImsUtImpl.getConditionFromCFReason(info[0].reason);
            imsCfInfo[0].mStatus = 0;
            imsCfInfo[0].mServiceClass = 1;
            imsCfInfo[0].mToA = 0;
            imsCfInfo[0].mNumber = "";
            imsCfInfo[0].mTimeSeconds = 0;
        }
        return imsCfInfo;
    }
}
