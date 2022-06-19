package com.mediatek.ims.config.internal;

import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.ims.ImsConfigListener;
import com.android.ims.ImsException;
import com.android.ims.internal.IImsConfig;
import com.mediatek.ims.ImsCommonUtil;
import com.mediatek.ims.internal.IMtkImsConfig;
import com.mediatek.ims.ril.ImsCommandsInterface;
import java.util.HashMap;

public class MtkImsConfigImpl extends IMtkImsConfig.Stub {
    private static final boolean DEBUG;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final String TAG = "MtkImsConfigImpl";
    private ImsConfigAdapter mConfigAdapter;
    private Context mContext;
    private HashMap<Integer, Integer> mImsCapabilities;
    private HashMap<Integer, Boolean> mImsCapabilitiesIsCache = new HashMap<>();
    private final IImsConfig mImsConfig;
    private int mPhoneId;
    private ImsCommandsInterface mRilAdapter;

    static {
        boolean z = false;
        if (TextUtils.equals(Build.TYPE, "eng") || SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        DEBUG = z;
    }

    public MtkImsConfigImpl(Context context, ImsCommandsInterface imsRilAdapter, IImsConfig imsConfig, ImsConfigAdapter adapter, int phoneId) {
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        this.mImsCapabilities = hashMap;
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mRilAdapter = imsRilAdapter;
        this.mImsConfig = imsConfig;
        this.mConfigAdapter = adapter;
        hashMap.put(0, 1);
        this.mImsCapabilities.put(1, 0);
        this.mImsCapabilities.put(2, 0);
        this.mImsCapabilities.put(3, 0);
        this.mImsCapabilities.put(4, 0);
        this.mImsCapabilities.put(5, 0);
        this.mImsCapabilities.put(6, 0);
        this.mImsCapabilities.put(7, 0);
        this.mImsCapabilitiesIsCache.put(0, false);
        this.mImsCapabilitiesIsCache.put(1, false);
        this.mImsCapabilitiesIsCache.put(2, false);
        this.mImsCapabilitiesIsCache.put(3, false);
        this.mImsCapabilitiesIsCache.put(4, false);
        this.mImsCapabilitiesIsCache.put(5, false);
        this.mImsCapabilitiesIsCache.put(6, false);
        this.mImsCapabilitiesIsCache.put(7, false);
    }

    public int getProvisionedValue(int item) {
        try {
            int result = this.mImsConfig.getProvisionedValue(item);
            Rlog.i(TAG, "getProvisionedValue(" + item + ") : " + result + " on phone" + this.mPhoneId + " from binder pid " + Binder.getCallingPid() + ", binder uid " + Binder.getCallingUid() + ", process pid " + Process.myPid() + ", process uid " + Process.myUid());
            return result;
        } catch (RemoteException e) {
            Rlog.e(TAG, "getProvisionedValue(" + item + ") remote failed!");
            throw new RuntimeException(e);
        }
    }

    public String getProvisionedStringValue(int item) {
        try {
            String result = this.mImsConfig.getProvisionedStringValue(item);
            Rlog.i(TAG, "getProvisionedStringValue(" + item + ") : " + result + " on phone " + this.mPhoneId + " from binder pid " + Binder.getCallingPid() + ", binder uid " + Binder.getCallingUid() + ", process pid " + Process.myPid() + ", process uid " + Process.myUid());
            return result;
        } catch (RemoteException e) {
            Rlog.e(TAG, "getProvisionedStringValue(" + item + ") remote failed!");
            throw new RuntimeException(e);
        }
    }

    public int setProvisionedValue(int item, int value) {
        try {
            return this.mImsConfig.setProvisionedValue(item, value);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setProvisionedValue(" + item + ") remote failed!");
            return 1;
        }
    }

    public int setProvisionedStringValue(int item, String value) {
        try {
            return this.mImsConfig.setProvisionedStringValue(item, value);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setProvisionedValue(" + item + ") remote failed!");
            return 1;
        }
    }

    public void getFeatureValue(int feature, int network, ImsConfigListener listener) {
        try {
            Rlog.i(TAG, "getFeatureValue(" + feature + ", " + network + ") : on phone " + this.mPhoneId);
            this.mImsConfig.getFeatureValue(feature, network, listener);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getFeatureValue(" + feature + ") remote failed!");
            throw new RuntimeException(e);
        }
    }

    public void setFeatureValue(int feature, int network, int value, ImsConfigListener listener) {
        try {
            this.mImsConfig.setFeatureValue(feature, network, value, listener);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setFeatureValue(" + feature + ") remote failed!");
            throw new RuntimeException(e);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    public void setMultiFeatureValues(int[] feature, int[] network, int[] value, ImsConfigListener listener) {
        int isLast;
        try {
            if (!ImsCommonUtil.supportMims()) {
                if (ImsCommonUtil.getMainCapabilityPhoneId() != this.mPhoneId) {
                    Rlog.i(TAG, "setFeatureValue is not allow on non main capability phoneId:" + this.mPhoneId + " in non MIMS project");
                    throw new ImsException("Do not setFeatureValue for non MIMS not main capability phoneId: " + this.mPhoneId, 102);
                }
            }
            for (int i = 0; i < feature.length; i++) {
                try {
                    if (!ImsCommonUtil.supportMdAutoSetupIms()) {
                        this.mConfigAdapter.mStorage.setFeatureValue(feature[i], network[i], value[i]);
                        switch (feature[i]) {
                            case 0:
                                if (value[i] != ImsConfigUtils.getFeaturePropValue(ImsConfigUtils.PROPERTY_VOLTE_ENALBE, this.mPhoneId)) {
                                    if (value[i] != 1) {
                                        this.mRilAdapter.turnOffVolte((Message) null);
                                        break;
                                    } else {
                                        this.mRilAdapter.turnOnVolte((Message) null);
                                        break;
                                    }
                                }
                                break;
                            case 1:
                                if (value[i] != ImsConfigUtils.getFeaturePropValue(ImsConfigUtils.PROPERTY_VILTE_ENALBE, this.mPhoneId)) {
                                    if (value[i] != 1) {
                                        this.mRilAdapter.turnOffVilte((Message) null);
                                        break;
                                    } else {
                                        this.mRilAdapter.turnOnVilte((Message) null);
                                        break;
                                    }
                                }
                                break;
                            case 2:
                                if (value[i] != ImsConfigUtils.getFeaturePropValue(ImsConfigUtils.PROPERTY_WFC_ENALBE, this.mPhoneId)) {
                                    if (value[i] != 1) {
                                        this.mRilAdapter.turnOffWfc((Message) null);
                                        break;
                                    } else {
                                        this.mRilAdapter.turnOnWfc((Message) null);
                                        break;
                                    }
                                }
                                break;
                            case 3:
                                if (value[i] != ImsConfigUtils.getFeaturePropValue(ImsConfigUtils.PROPERTY_VIWIFI_ENALBE, this.mPhoneId)) {
                                    if (value[i] != 1) {
                                        this.mRilAdapter.turnOffViwifi((Message) null);
                                        break;
                                    } else {
                                        this.mRilAdapter.turnOnViwifi((Message) null);
                                        break;
                                    }
                                }
                                break;
                        }
                    } else {
                        if (i == feature.length - 1) {
                            isLast = 1;
                        } else {
                            isLast = 0;
                        }
                        if (value[i] == 1 && "1".equals(SystemProperties.get("persist.vendor.mtk_dynamic_ims_switch")) && this.mConfigAdapter.getImsResCapability(feature[i]) != 1) {
                            Rlog.i(TAG, "setMultiFeatureValues, modify the value in ImsConfig.");
                            value[i] = 0;
                        }
                        Rlog.i(TAG, "setMultiFeatureValues i:" + i + " feature: " + feature[i] + " network: " + network[i] + " value: " + value[i] + " isLast: " + isLast);
                        this.mConfigAdapter.mController.setFeatureValue(feature[i], network[i], value[i], isLast);
                    }
                    if (listener != null) {
                        listener.onSetFeatureResponse(feature[i], network[i], value[i], 0);
                    }
                } catch (ImsException e) {
                    try {
                        Rlog.e(TAG, "setFeatureValue(" + feature[i] + ") failed, code: " + e.getCode());
                        if (listener != null) {
                            listener.onSetFeatureResponse(feature[i], network[i], 0, 1);
                        }
                    } catch (RemoteException e2) {
                        Rlog.e(TAG, "setMultiFeatureValues onSetFeatureResponse remote failed!");
                        throw new RuntimeException(e2);
                    }
                }
            }
        } catch (ImsException e3) {
            try {
                Rlog.e(TAG, "setMultiFeatureValues failed, code: " + e3.getCode());
                if (listener != null) {
                    for (int i2 = 0; i2 < feature.length; i2++) {
                        listener.onSetFeatureResponse(feature[i2], network[i2], 0, 1);
                    }
                }
            } catch (RemoteException e4) {
                Rlog.e(TAG, "setMultiFeatureValues onSetFeatureResponse remote failed!");
                throw new RuntimeException(e4);
            }
        }
    }

    public void getVideoQuality(ImsConfigListener listener) {
    }

    public void setVideoQuality(int quality, ImsConfigListener listener) {
    }

    public void setImsResCapability(int feature, int value) {
        this.mImsCapabilities.put(Integer.valueOf(feature), Integer.valueOf(value));
        this.mImsCapabilitiesIsCache.put(Integer.valueOf(feature), true);
        try {
            if (DEBUG) {
                Rlog.i(TAG, "setImsResCapability(" + feature + ") : " + value + " on phone " + this.mPhoneId + " from binder pid " + Binder.getCallingPid() + ", binder uid " + Binder.getCallingUid());
            }
            this.mConfigAdapter.setImsResCapability(feature, value);
        } catch (ImsException e) {
            Rlog.e(TAG, "setImsResCapability(" + feature + ") failed, code: " + e.getCode());
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    public int getImsResCapability(int feature) {
        int value;
        try {
            if (this.mImsCapabilitiesIsCache.get(Integer.valueOf(feature)).booleanValue()) {
                value = this.mImsCapabilities.get(Integer.valueOf(feature)).intValue();
            } else {
                value = this.mConfigAdapter.getImsResCapability(feature);
            }
            if (value != 1) {
                if (value != 0) {
                    throw new ImsException(" result value:" + value + " incorrect!", 101);
                }
            }
            return value;
        } catch (ImsException e) {
            Rlog.e(TAG, "getImsResCapability(" + feature + ") failed, code: " + e.getCode());
            return this.mImsCapabilities.get(Integer.valueOf(feature)).intValue();
        }
    }

    public void setWfcMode(int mode) {
        Rlog.i(TAG, "setWfcMode(" + mode + ")");
        int rilWfcMode = 1;
        switch (mode) {
            case 0:
                rilWfcMode = 3;
                break;
            case 1:
                rilWfcMode = 2;
                break;
            case 2:
                rilWfcMode = 1;
                break;
            default:
                Rlog.i(TAG, "setWfcMode mapping error, value is invalid!");
                break;
        }
        this.mConfigAdapter.sendWfcProfileInfo(rilWfcMode);
    }

    public void setVoltePreference(int mode) {
        this.mConfigAdapter.setVoltePreference(mode);
    }

    public int[] setModemImsCfg(String[] keys, String[] values, int phoneId) {
        Rlog.i(TAG, "setModemImsCfg phoneId:" + phoneId);
        return this.mConfigAdapter.setModemImsCfg(keys, values, 0);
    }

    public int[] setModemImsWoCfg(String[] keys, String[] values, int phoneId) {
        Rlog.i(TAG, "setModemImsWoCfg phoneId:" + phoneId);
        return this.mConfigAdapter.setModemImsCfg(keys, values, 1);
    }

    public int[] setModemImsIwlanCfg(String[] keys, String[] values, int phoneId) {
        Rlog.i(TAG, "setModemImsIwlanCfg phoneId:" + phoneId);
        return this.mConfigAdapter.setModemImsCfg(keys, values, 2);
    }
}
