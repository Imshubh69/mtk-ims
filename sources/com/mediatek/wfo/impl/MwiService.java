package com.mediatek.wfo.impl;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.mediatek.wfo.IMwiService;
import com.mediatek.wfo.IWifiOffloadService;
import com.mediatek.wfo.ril.MwiRIL;
import java.util.Arrays;

public class MwiService extends IMwiService.Stub {
    static final String MWIS_LOG_TAG = "MWIS";
    private static final boolean VDBG = true;
    private static MwiService mInstance = null;
    /* access modifiers changed from: private */
    public static WifiPdnHandler mWifiPdnHandler;
    /* access modifiers changed from: private */
    public Context mContext;
    HandlerThread mHandlerThread = new HandlerThread("MwiServiceHandlerThread");
    private BroadcastReceiver mMultiSimConfigChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int activeModemCount = intent.getIntExtra("android.telephony.extra.ACTIVE_SIM_SUPPORTED_COUNT", 0);
            String processName = Application.getProcessName();
            Rlog.i(MwiService.MWIS_LOG_TAG, "notifyMultiSimConfigChanged ACTION_MULTI_SIM_CONFIG_CHANGED,mSimCount: " + MwiService.this.mSimCount + ", activeModemCount: " + activeModemCount + ", pid:" + processName);
            if (activeModemCount != 0 && MwiService.this.mSimCount != activeModemCount && !"com.mediatek.ims".equals(processName)) {
                int prevActiveModemCount = MwiService.this.mMwiRil.length;
                int unused = MwiService.this.mSimCount = activeModemCount;
                MwiService mwiService = MwiService.this;
                MwiRIL[] unused2 = mwiService.mMwiRil = (MwiRIL[]) Arrays.copyOf(mwiService.mMwiRil, activeModemCount);
                for (int i = prevActiveModemCount; i < activeModemCount; i++) {
                    MwiService.this.mMwiRil[i] = new MwiRIL(MwiService.this.mContext, i, MwiService.this.mHandlerThread.getLooper());
                }
                MwiService.mWifiPdnHandler.notifyMultiSimConfigChanged(activeModemCount, MwiService.this.mMwiRil);
                MwiService.this.mWfcHandler.notifyMultiSimConfigChanged(activeModemCount, MwiService.this.mMwiRil);
                MwiService.this.mWfcLocationHandler.notifyMultiSimConfigChanged(activeModemCount, MwiService.this.mMwiRil);
            }
        }
    };
    /* access modifiers changed from: private */
    public MwiRIL[] mMwiRil;
    /* access modifiers changed from: private */
    public int mSimCount;
    /* access modifiers changed from: private */
    public WfcHandler mWfcHandler;
    /* access modifiers changed from: private */
    public WfcLocationHandler mWfcLocationHandler;

    public static MwiService getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MwiService(context);
        }
        return mInstance;
    }

    public static MwiService getInstance() {
        return mInstance;
    }

    public MwiService(Context context) {
        logd("Construct MwiService");
        this.mContext = context;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager != null) {
            this.mSimCount = telephonyManager.getSimCount();
        } else {
            logd("telephonyManager = null");
        }
        logd("mSimCount: " + this.mSimCount);
        this.mHandlerThread.start();
        this.mMwiRil = new MwiRIL[this.mSimCount];
        for (int i = 0; i < this.mSimCount; i++) {
            this.mMwiRil[i] = new MwiRIL(context, i, this.mHandlerThread.getLooper());
        }
        WifiPdnHandler wifiPdnHandler = new WifiPdnHandler(this.mContext, this.mSimCount, this.mHandlerThread.getLooper(), this.mMwiRil);
        mWifiPdnHandler = wifiPdnHandler;
        this.mWfcHandler = WfcHandler.getInstance(this.mContext, wifiPdnHandler, this.mSimCount, this.mHandlerThread.getLooper(), this.mMwiRil);
        this.mWfcLocationHandler = new WfcLocationHandler(this.mContext, this.mWfcHandler, mWifiPdnHandler, this.mSimCount, this.mHandlerThread.getLooper(), this.mMwiRil);
        if (!"com.mediatek.ims".equals(Application.getProcessName())) {
            IntentFilter multiSimConfigChanged = new IntentFilter();
            multiSimConfigChanged.addAction("android.telephony.action.MULTI_SIM_CONFIG_CHANGED");
            context.registerReceiver(this.mMultiSimConfigChangedReceiver, multiSimConfigChanged);
        }
    }

    public MwiRIL getMwiRIL(int slotId) {
        if (slotId < this.mSimCount) {
            return this.mMwiRil[slotId];
        }
        Rlog.e(MWIS_LOG_TAG, "Out of the bounds, slotId is: " + slotId);
        return null;
    }

    private boolean is93RilMode() {
        if (SystemProperties.get("ro.vendor.mtk_ril_mode").equals("c6m_1rild")) {
            return VDBG;
        }
        return false;
    }

    public IWifiOffloadService getWfcHandlerInterface() {
        logd("getWfcHandlerInterface");
        return this.mWfcHandler.getWfoInterface();
    }

    public void dispose() {
        logd("dispose()");
        this.mHandlerThread.quit();
        this.mHandlerThread.interrupt();
    }

    private static void logd(String l) {
        Rlog.d(MWIS_LOG_TAG, "[MwiService] " + l);
    }
}
