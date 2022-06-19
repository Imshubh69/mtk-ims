package com.mediatek.ims.config;

import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.ims.ProvisioningManager;
import android.telephony.ims.RcsClientConfiguration;
import android.telephony.ims.stub.ImsConfigImplBase;
import android.util.Log;
import com.android.ims.internal.IImsConfig;
import com.mediatek.ims.rcse.UaServiceManager;
import com.mediatek.ims.rcsua.AcsConfiguration;
import com.mediatek.ims.rcsua.AcsEventCallback;
import com.mediatek.ims.rcsua.RcsUaService;

public class MtkImsConfigImpl extends ImsConfigImplBase {
    public static final int FAILED = 1;
    public static final int SUCCESS = 0;
    private static final String TAG = "MtkImsConfigImpl";
    public static final int UNKNOWN = -1;
    public static int reason = 1;
    private final IImsConfig mOldConfigInterface;
    private RcsEventCallback mRcsEventCallback;
    private RcsUaService rcsUaService;

    public MtkImsConfigImpl(IImsConfig config) {
        this.mOldConfigInterface = config;
        String optr = SystemProperties.get("persist.vendor.operator.optr");
        Log.d(TAG, "optr value  " + optr);
        if ("op07".equalsIgnoreCase(optr)) {
            this.mRcsEventCallback = new RcsEventCallback();
            UaServiceManager.getInstance().registerAcsCallback(this.mRcsEventCallback);
        }
    }

    public int setConfig(int item, int value) {
        try {
            if (this.mOldConfigInterface.setProvisionedValue(item, value) == 0) {
                return 0;
            }
            return 1;
        } catch (RemoteException e) {
            loge("setConfig: item=" + item + " value=" + value + "failed: " + e.getMessage());
            return 1;
        }
    }

    public int setConfig(int item, String value) {
        try {
            if (this.mOldConfigInterface.setProvisionedStringValue(item, value) == 0) {
                return 0;
            }
            return 1;
        } catch (RemoteException e) {
            loge("setConfig: item=" + item + " value=" + value + "failed: " + e.getMessage());
            return 1;
        }
    }

    public int getConfigInt(int item) {
        try {
            int value = this.mOldConfigInterface.getProvisionedValue(item);
            if (value != -1) {
                return value;
            }
            return -1;
        } catch (RemoteException e) {
            loge("getConfigInt: item=" + item + "failed: " + e.getMessage());
        }
    }

    public String getConfigString(int item) {
        try {
            return this.mOldConfigInterface.getProvisionedStringValue(item);
        } catch (RemoteException e) {
            loge("getConfigInt: item=" + item + "failed: " + e.getMessage());
            return null;
        }
    }

    public void setRcsClientConfiguration(RcsClientConfiguration rcc) {
        Log.d(TAG, "setRcsClientConfig called in ACS vendor with rcc value " + rcc);
        String mRcsVersion = rcc.getRcsVersion();
        String mRcsProfile = rcc.getRcsProfile();
        String mClientVendor = rcc.getClientVendor();
        String mClientVersion = rcc.getClientVersion();
        String optr = SystemProperties.get("persist.vendor.operator.optr");
        if ("op07".equalsIgnoreCase(optr)) {
            if (this.rcsUaService == null) {
                this.rcsUaService = UaServiceManager.getInstance().getService();
                Log.d(TAG, "rcsUaService instance " + this.rcsUaService);
            }
            RcsUaService rcsUaService2 = this.rcsUaService;
            if (rcsUaService2 != null) {
                rcsUaService2.setAcsonfiguration(mRcsVersion, mRcsProfile, mClientVendor, mClientVersion);
            }
        } else if ("op08".equalsIgnoreCase(optr)) {
            Log.d(TAG, "inside TMO check ");
            try {
                getIImsConfig().notifyRcsAutoConfigurationReceived("Default config".getBytes(), false);
            } catch (RemoteException e) {
                throw e.rethrowAsRuntimeException();
            }
        } else {
            Log.d(TAG, "Not a valid operator ");
            try {
                getIImsConfig().notifyRcsAutoConfigurationReceived("Not a valid operator".getBytes(), false);
            } catch (RemoteException e2) {
                throw e2.rethrowAsRuntimeException();
            }
        }
    }

    public class RcsEventCallback extends AcsEventCallback {
        public RcsEventCallback() {
        }

        public void onConfigurationStatusChanged(boolean valid, int version) {
            Log.d(MtkImsConfigImpl.TAG, "onConfigurationStatusChanged: valid:" + valid + " version:" + version);
            if (valid) {
                MtkImsConfigImpl.this.updateConfiguration();
            } else {
                MtkImsConfigImpl.this.resetConfiguration();
            }
        }

        public void onAcsConnected() {
            Log.d(MtkImsConfigImpl.TAG, "onAcsConnected");
        }

        public void onAcsDisconnected() {
            Log.d(MtkImsConfigImpl.TAG, "onAcsDisconnected");
        }
    }

    /* access modifiers changed from: private */
    public void updateConfiguration() {
        if (this.rcsUaService == null) {
            RcsUaService service = UaServiceManager.getInstance().getService();
            this.rcsUaService = service;
            if (service == null) {
                Log.d(TAG, "updateConfiguration, rcsUaService is null");
                return;
            }
        }
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            try {
                ProvisioningManager pm = ProvisioningManager.createForSubscriptionId(subId);
                AcsConfiguration config = this.rcsUaService.getAcsConfiguration();
                byte[] infoByte = null;
                String info = config != null ? config.readXmlData() : null;
                if (info != null) {
                    infoByte = info.getBytes();
                }
                if (pm != null && infoByte != null) {
                    pm.setProvisioningIntValue(18, this.rcsUaService.getAcsConfigInt("capInfoExpiry"));
                    pm.setProvisioningIntValue(21, this.rcsUaService.getAcsConfigInt("source-throttlepublish") * 1000);
                    pm.notifyRcsAutoConfigurationReceived(infoByte, false);
                }
            } catch (Exception e) {
                Log.e(TAG, "updateConfiguration, excpetion happened! " + e);
            }
        } else {
            Log.d(TAG, "updateConfiguration, invalid subId");
        }
    }

    /* access modifiers changed from: private */
    public void resetConfiguration() {
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            try {
                ProvisioningManager pm = ProvisioningManager.createForSubscriptionId(subId);
                if (pm != null) {
                    if (pm.getProvisioningIntValue(18) != 21600) {
                        pm.setProvisioningIntValue(18, 21600);
                        Log.d(TAG, "resetConfiguration, reset capability cache expiration");
                    }
                    if (pm.getProvisioningIntValue(21) != 30000) {
                        pm.setProvisioningIntValue(21, 30000);
                        Log.d(TAG, "resetConfiguration, reset publish source throttle");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "resetConfiguration, excpetion happened! " + e);
            }
        } else {
            Log.d(TAG, "resetConfiguration, invalid subId");
        }
    }

    private static void log(String msg) {
        Rlog.d(TAG, msg);
    }

    private static void loge(String msg) {
        Rlog.e(TAG, msg);
    }
}
