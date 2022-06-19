package com.mediatek.wfo.impl;

import android.content.Context;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.mediatek.wfo.IWifiOffloadService;
import com.mediatek.wfo.MwisConstants;
import com.mediatek.wfo.WifiOffloadManager;
import com.mediatek.wfo.plugin.ExtensionFactory;

public class WfoService {
    static final String TAG = "WfoService";
    public static WfoService mInstance = null;
    private Context mContext;
    private MwiService mMwiService;
    private IWifiOffloadService.Stub mService;

    public static WfoService getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new WfoService(context);
        }
        return mInstance;
    }

    private WfoService(Context context) {
        this.mContext = context;
    }

    public void makeWfoService() {
        if (SystemProperties.get("ro.vendor.mtk_ril_mode").equals("c6m_1rild")) {
            Rlog.d(TAG, "WfoService new MWIService");
            MwiService instance = MwiService.getInstance(this.mContext);
            this.mMwiService = instance;
            ServiceManager.addService(MwisConstants.MWI_SERVICE, instance.asBinder(), true);
            return;
        }
        Rlog.d(TAG, "WfoService new WifiOffloadService");
        IWifiOffloadService.Stub makeWifiOffloadService = ExtensionFactory.makeLegacyComponentFactory(this.mContext).makeWifiOffloadService(this.mContext);
        this.mService = makeWifiOffloadService;
        if (makeWifiOffloadService == null) {
            Rlog.e(TAG, "WfoService cannot be found");
        } else {
            ServiceManager.addService(WifiOffloadManager.WFO_SERVICE, makeWifiOffloadService.asBinder(), true);
        }
    }
}
