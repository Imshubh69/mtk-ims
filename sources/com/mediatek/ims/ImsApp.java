package com.mediatek.ims;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.mediatek.ims.config.internal.ImsConfigUtils;
import com.mediatek.ims.internal.ImsVTProviderUtil;

public class ImsApp extends Application {
    private static final String IMS_SERVICE = "ims";
    private static final String TAG = "ImsApp";
    /* access modifiers changed from: private */
    public ImsService mImsService = null;
    private BroadcastReceiver mMultiSimConfigChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int activeModemCount = intent.getIntExtra("android.telephony.extra.ACTIVE_SIM_SUPPORTED_COUNT", 0);
            String processName = Application.getProcessName();
            Rlog.i(ImsApp.TAG, "notifyMultiSimConfigChanged, ACTION_MULTI_SIM_CONFIG_CHANGED, phone:" + ImsApp.this.mNumOfPhones + "->" + activeModemCount + ", pid:" + processName);
            if (activeModemCount != 0 && ImsApp.this.mNumOfPhones != activeModemCount) {
                if ("com.mediatek.ims".equals(processName)) {
                    if (ImsApp.this.mImsService != null) {
                        ImsApp.this.mImsService.clearImsRilRequest();
                    }
                    Process.killProcess(Process.myPid());
                    return;
                }
                ImsVTProviderUtil.getInstance().notifyMultiSimConfigChanged(activeModemCount);
                ImsApp.this.mImsService.notifyMultiSimConfigChanged(context, activeModemCount);
                int unused = ImsApp.this.mNumOfPhones = activeModemCount;
            }
        }
    };
    /* access modifiers changed from: private */
    public int mNumOfPhones = 0;

    public void onCreate() {
        if (SystemProperties.get(ImsConfigUtils.PROPERTY_IMS_SUPPORT).equals("0")) {
            Rlog.w(TAG, "IMS not support, do not init ImsService");
            return;
        }
        super.onCreate();
        if (UserHandle.myUserId() == 0) {
            Rlog.i(TAG, "ImsApp onCreate begin");
            this.mImsService = ImsService.getInstance(this);
            ImsVTProviderUtil.getInstance().setContextAndInitRefVTP(this);
            ServiceManager.addService("mtkIms", new MtkImsService(this, this.mImsService).asBinder(), true);
            Rlog.i(TAG, "ImsApp onCreate end");
            this.mNumOfPhones = TelephonyManager.getDefault().getPhoneCount();
            getApplicationContext().sendBroadcast(new Intent("com.mediatek.ims.MTK_IMS_SERVICE_UP"));
            IntentFilter multiSimConfigChanged = new IntentFilter();
            multiSimConfigChanged.addAction("android.telephony.action.MULTI_SIM_CONFIG_CHANGED");
            registerReceiver(this.mMultiSimConfigChangedReceiver, multiSimConfigChanged);
        }
    }
}
