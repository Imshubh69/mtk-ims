package com.mediatek.ims.plugin;

import android.telephony.ims.ImsCallForwardInfo;
import com.android.internal.telephony.CallForwardInfo;

public interface ImsSSExtPlugin {
    ImsCallForwardInfo[] getImsCallForwardInfo(CallForwardInfo[] callForwardInfoArr);
}
