package com.mediatek.ims.config.internal;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.ImsException;
import com.android.internal.telephony.CommandException;
import com.mediatek.ims.ImsCommonUtil;
import com.mediatek.ims.config.DefaultConfigPolicyFactory;
import com.mediatek.ims.config.ImsConfigContract;
import com.mediatek.ims.config.ImsConfigPolicy;
import com.mediatek.ims.config.ImsConfigSettings;
import com.mediatek.ims.plugin.ExtensionFactory;
import com.mediatek.ims.plugin.ImsManagerOemPlugin;
import com.mediatek.ims.ril.ImsCommandsInterface;
import java.util.HashMap;
import java.util.Map;

public class ImsConfigStorage {
    private static final String ACTION_CXP_NOTIFY_FEATURE = "com.mediatek.common.carrierexpress.cxp_notify_feature";
    /* access modifiers changed from: private */
    public static final boolean DEBUG = (TextUtils.equals(Build.TYPE, "eng") || SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1);
    static final int EVENT_GET_PROVISION_DONE_URC = 6;
    static ImsFeatureMap[] FeatureSendArray = new ImsFeatureMap[4];
    static final int MDCONFIG_CMD_ERROR = 32;
    static final int MDCONFIG_CMD_SUCCESS = 33;
    static final int MDCONFIG_INTERRUPT_ERROR = 31;
    static final int MDCONFIG_TIMEOUT_ERROR = 30;
    static final int MSG_FORCE_TO_SEND_WFC_MODE = 10;
    static final int MSG_IMS_GET_PROVISION_DONE = 4;
    static final int MSG_IMS_SET_MDCFG_DONE = 7;
    static final int MSG_IMS_SET_PROVISION_DONE = 5;
    static final int MSG_LOAD_CONFIG_STORAGE = 1;
    static final int MSG_RESET_BROADCAST_FLAG = 2;
    static final int MSG_RESET_CONFIG_STORAGE = 0;
    static final int MSG_RESET_WFC_MODE_FLAG = 8;
    static final int MSG_SIM_ABSENT_ECC_BROADCAST = 3;
    static final int MSG_UPDATE_IMS_SERVICE_CONFIG = 9;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    static final int PROVISION_CMD_ERROR = 22;
    static final int PROVISION_CMD_SUCCESS = 23;
    static final int PROVISION_INTERRUPT_ERROR = 21;
    static final int PROVISION_NO_DEFAULT_VALUE = 25;
    static final int PROVISION_TIMEOUT_ERROR = 20;
    static final int PROVISION_URC_PARSE_ERROR = 24;
    private static final String TAG = "ImsConfigStorage";
    /* access modifiers changed from: private */
    public static final boolean TELDBG;
    private int IMS_PROVISION_NO_DEFAULT_ERROR = 6604;
    /* access modifiers changed from: private */
    public int curWfcMode = -1;
    /* access modifiers changed from: private */
    public ConfigHelper mConfigHelper = null;
    /* access modifiers changed from: private */
    public Context mContext = null;
    /* access modifiers changed from: private */
    public FeatureHelper mFeatureHelper = null;
    private Object mFeatureLock = new Object();
    private Handler mHandler;
    /* access modifiers changed from: private */
    public ImsManagerOemPlugin mImsManagerOemPlugin = null;
    private Object mMdCfgLock = new Object();
    private int mPhoneId = -1;
    private Object mProvisionedStringValueLock = new Object();
    private Object mProvisionedValueLock = new Object();
    private BroadcastReceiver mReceiver;
    private ResourceHelper mResourceHelper = null;
    private ImsCommandsInterface mRilAdapter;
    private Object mWfcLock = new Object();

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        TELDBG = z;
    }

    private ImsConfigStorage() {
    }

    private class ProvisioningResult {
        Object lockObj;
        String[] provisionInfo;
        int provisionResult;

        private ProvisioningResult() {
            this.provisionResult = 20;
            this.lockObj = new Object();
        }
    }

    private class MdConfigResult {
        int configResult;
        Object lockObj;
        int requestConfigNum;
        int[] resultArray;

        private MdConfigResult() {
            this.requestConfigNum = 0;
            this.resultArray = null;
            this.configResult = 30;
            this.lockObj = new Object();
        }
    }

    public ImsConfigStorage(Context context, int phoneId, ImsCommandsInterface imsRilAdapter) {
        Log.d(TAG, "ImsConfigStorage() on phone " + phoneId);
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mRilAdapter = imsRilAdapter;
        FeatureSendArray[phoneId] = new ImsFeatureMap(phoneId);
        HandlerThread thread = new HandlerThread("ImsConfig-" + this.mPhoneId);
        thread.start();
        this.mHandler = new CarrierConfigHandler(this.mPhoneId, thread.getLooper());
        this.mFeatureHelper = new FeatureHelper(this.mContext, this.mPhoneId);
        this.mConfigHelper = new ConfigHelper(this.mContext, this.mHandler, this.mPhoneId);
        this.mResourceHelper = new ResourceHelper(this.mContext, this.mPhoneId);
        this.mFeatureHelper.initFeatureStorage();
        this.mRilAdapter.registerForGetProvisionComplete(this.mHandler, 6, (Object) null);
        resetFeatureSendCmd();
        this.mReceiver = new ImsConfigReceiver(this.mHandler, this.mPhoneId, this.mRilAdapter);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        filter.addAction(ACTION_CXP_NOTIFY_FEATURE);
        filter.addAction("com.mediatek.ims.MTK_MMTEL_READY");
        if (ImsCommonUtil.isDssNoResetSupport()) {
            filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        }
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    class CarrierConfigHandler extends Handler {
        private boolean isGetProvisionUrc;
        private int mPhoneId;
        private String[] provisionInfoTemp;

        CarrierConfigHandler(int phoneId, Looper looper) {
            super(looper);
            this.mPhoneId = phoneId;
        }

        public void handleMessage(Message msg) {
            if (ImsConfigStorage.DEBUG) {
                Log.d(ImsConfigStorage.TAG, "Received msg = " + msg.hashCode() + ", what = " + msg.what);
            }
            switch (msg.what) {
                case 0:
                    Log.d(ImsConfigStorage.TAG, "Reset config storage");
                    ImsConfigStorage.this.mConfigHelper.clear();
                    return;
                case 1:
                    synchronized (ImsConfigStorage.this.mConfigHelper) {
                        int opCode = ((Integer) msg.obj).intValue();
                        if (ImsConfigStorage.this.mConfigHelper.getOpCode() != opCode) {
                            ImsConfigStorage.this.mConfigHelper.setOpCode(opCode);
                            Log.d(ImsConfigStorage.TAG, "Start load config storage for " + opCode + " on phone " + this.mPhoneId);
                            ImsConfigStorage.this.mConfigHelper.clear();
                            ImsConfigStorage.this.mConfigHelper.init(opCode);
                            ImsConfigStorage.this.mConfigHelper.setInitDone(true);
                            Log.d(ImsConfigStorage.TAG, "Finish Loading config storage for " + opCode);
                        } else {
                            Log.d(ImsConfigStorage.TAG, "Skip reloading config by same opCode: " + opCode + " on phone " + this.mPhoneId);
                            ImsConfigStorage.this.mConfigHelper.setInitDone(true);
                        }
                    }
                    return;
                case 2:
                    ImsConfigStorage.this.resetFeatureSendCmd();
                    ImsConfigStorage.this.mFeatureHelper.resetBroadcastFlag();
                    return;
                case 3:
                    ImsConfigProvider.ECCAllowSendCmd.put(Integer.valueOf(this.mPhoneId), true);
                    if (ImsConfigStorage.this.mFeatureHelper.isAllFeatureFalse()) {
                        ImsConfigProvider.ECCAllowBroadcast.put(Integer.valueOf(this.mPhoneId), true);
                        Log.d(ImsConfigStorage.TAG, "All feature false after sim absent,should broadcast VoLTE feature value once for ECC");
                        return;
                    }
                    return;
                case 4:
                    if (ImsConfigStorage.DEBUG) {
                        Log.d(ImsConfigStorage.TAG, "MSG_IMS_GET_PROVISION_DONE: Enter messege");
                    }
                    AsyncResult ar = (AsyncResult) msg.obj;
                    ProvisioningResult result = (ProvisioningResult) ar.userObj;
                    synchronized (result.lockObj) {
                        if (ar.exception == null) {
                            if (this.isGetProvisionUrc) {
                                if (this.provisionInfoTemp.length >= 2) {
                                    result.provisionInfo = new String[2];
                                    result.provisionInfo[0] = this.provisionInfoTemp[0];
                                    result.provisionInfo[1] = this.provisionInfoTemp[1];
                                    result.provisionResult = 23;
                                    Log.d(ImsConfigStorage.TAG, "MSG_IMS_GET_PROVISION_DONE: provisionInfo[0]:" + result.provisionInfo[0] + ", provisionInfo[1]:" + result.provisionInfo[1]);
                                }
                            }
                            Log.e(ImsConfigStorage.TAG, "MSG_IMS_GET_PROVISION_DONE: Error getting, URC error or no URC received!");
                            result.provisionResult = 22;
                        } else if (!(ar.exception instanceof CommandException) || ar.exception.getCommandError() != CommandException.Error.OEM_ERROR_24) {
                            result.provisionResult = 22;
                            Log.d(ImsConfigStorage.TAG, "MSG_IMS_GET_PROVISION_DONE: error ret null, e=" + ar.exception);
                        } else {
                            result.provisionResult = 25;
                            Log.d(ImsConfigStorage.TAG, "MSG_IMS_GET_PROVISION_DONE: MD no default value");
                        }
                        result.lockObj.notify();
                        if (ImsConfigStorage.DEBUG) {
                            Log.d(ImsConfigStorage.TAG, "MSG_IMS_GET_PROVISION_DONE: notify result");
                        }
                    }
                    return;
                case 5:
                    if (ImsConfigStorage.DEBUG) {
                        Log.d(ImsConfigStorage.TAG, "MSG_IMS_SET_PROVISION_DONE: Enter messege");
                    }
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    ProvisioningResult result2 = (ProvisioningResult) ar2.userObj;
                    synchronized (result2.lockObj) {
                        if (ar2.exception != null) {
                            result2.provisionResult = 22;
                            Log.e(ImsConfigStorage.TAG, "MSG_IMS_SET_PROVISION_DONE: error ret null, e=" + ar2.exception);
                        } else {
                            result2.provisionResult = 23;
                            Log.d(ImsConfigStorage.TAG, "MSG_IMS_SET_PROVISION_DONE: Finish set provision!");
                        }
                        result2.lockObj.notify();
                        if (ImsConfigStorage.DEBUG) {
                            Log.d(ImsConfigStorage.TAG, "MSG_IMS_SET_PROVISION_DONE: notify result");
                        }
                    }
                    return;
                case 6:
                    if (ImsConfigStorage.DEBUG) {
                        Log.d(ImsConfigStorage.TAG, "EVENT_GET_PROVISION_DONE_URC: Enter messege");
                    }
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    this.provisionInfoTemp = (String[]) ar3.result;
                    this.isGetProvisionUrc = false;
                    if (ar3.exception != null) {
                        Log.e(ImsConfigStorage.TAG, "EVENT_GET_PROVISION_DONE_URC: error, e=" + ar3.exception);
                        return;
                    }
                    Log.d(ImsConfigStorage.TAG, "EVENT_GET_PROVISION_DONE_URC: provisionInfoTemp.length: " + this.provisionInfoTemp.length);
                    String[] strArr = this.provisionInfoTemp;
                    if (strArr != null && strArr.length >= 2) {
                        this.isGetProvisionUrc = true;
                        return;
                    }
                    return;
                case 7:
                    if (ImsConfigStorage.DEBUG) {
                        Log.d(ImsConfigStorage.TAG, "MSG_IMS_SET_MDCFG_DONE: Enter messege");
                    }
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    MdConfigResult cfgResult = (MdConfigResult) ar4.userObj;
                    synchronized (cfgResult.lockObj) {
                        if (ar4.exception != null) {
                            int[] errorResult = new int[cfgResult.requestConfigNum];
                            for (int i = 0; i < errorResult.length; i++) {
                                errorResult[i] = -1;
                            }
                            cfgResult.resultArray = errorResult;
                            cfgResult.configResult = 32;
                            Log.e(ImsConfigStorage.TAG, "SET_MDCFG_DONE, error ret, e=" + ar4.exception);
                        } else {
                            String[] resultStrArray = ((String) ar4.result).split(",");
                            int[] resultIntArray = new int[resultStrArray.length];
                            for (int i2 = 0; i2 < resultStrArray.length; i2++) {
                                resultIntArray[i2] = Integer.parseInt(resultStrArray[i2]);
                            }
                            cfgResult.resultArray = resultIntArray;
                            cfgResult.configResult = 33;
                            Log.d(ImsConfigStorage.TAG, "SET_MDCFG_DONE, finish set MD Ims config!");
                        }
                        cfgResult.lockObj.notify();
                        if (ImsConfigStorage.DEBUG) {
                            Log.d(ImsConfigStorage.TAG, "SET_MDCFG_DONE, notify result");
                        }
                    }
                    return;
                case 8:
                    ImsConfigStorage.this.resetWfcModeFlag();
                    return;
                case 9:
                    if (ImsConfigStorage.this.mImsManagerOemPlugin == null) {
                        ImsConfigStorage imsConfigStorage = ImsConfigStorage.this;
                        ImsManagerOemPlugin unused = imsConfigStorage.mImsManagerOemPlugin = ExtensionFactory.makeOemPluginFactory(imsConfigStorage.mContext).makeImsManagerPlugin(ImsConfigStorage.this.mContext);
                    }
                    ImsConfigStorage.this.mImsManagerOemPlugin.updateImsServiceConfig(ImsConfigStorage.this.mContext, ImsCommonUtil.getMainCapabilityPhoneId());
                    return;
                case 10:
                    if (ImsConfigStorage.this.curWfcMode == -1) {
                        Log.i(ImsConfigStorage.TAG, "Should not set invalid wfc mode");
                        return;
                    }
                    int oldWfcMode = ImsConfigStorage.this.curWfcMode;
                    ImsConfigStorage.this.resetWfcModeFlag();
                    ImsConfigStorage.this.sendWfcProfileInfo(oldWfcMode);
                    return;
                default:
                    return;
            }
        }
    }

    public int getFeatureValue(int featureId, int network) throws ImsException {
        int featureValue;
        synchronized (this.mFeatureLock) {
            featureValue = this.mFeatureHelper.getFeatureValue(featureId, network);
        }
        return featureValue;
    }

    public void setFeatureValue(int featureId, int network, int value) throws ImsException {
        synchronized (this.mFeatureLock) {
            this.mFeatureHelper.updateFeature(featureId, network, value);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public int getProvisionedValue(int configId) throws ImsException {
        synchronized (this.mProvisionedValueLock) {
            if (isProvisionStoreModem(configId)) {
                ProvisioningResult result = new ProvisioningResult();
                String mProvisionStr = ImsConfigSettings.getProvisionStr(configId);
                Message msg = this.mHandler.obtainMessage(4, result);
                synchronized (result.lockObj) {
                    this.mRilAdapter.getProvisionValue(this.mPhoneId, mProvisionStr, msg);
                    try {
                        result.lockObj.wait(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        result.provisionResult = 21;
                    }
                }
                if (result.provisionResult == 25) {
                    enforceConfigStorageInit("MD no default value, getProvisionedValue(" + configId + ")");
                    int access$1400 = this.mConfigHelper.getConfigValue(ImsConfigContract.TABLE_MASTER, configId);
                    return access$1400;
                } else if (isProvisionSuccess(result.provisionResult)) {
                    int parseInt = Integer.parseInt(result.provisionInfo[1]);
                    return parseInt;
                } else {
                    throw new ImsException("Something wrong, reason:" + result.provisionResult, 101);
                }
            } else {
                enforceConfigStorageInit("getProvisionedValue(" + configId + ")");
                int access$14002 = this.mConfigHelper.getConfigValue(ImsConfigContract.TABLE_MASTER, configId);
                return access$14002;
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public String getProvisionedStringValue(int configId) throws ImsException {
        synchronized (this.mProvisionedStringValueLock) {
            if (isProvisionStoreModem(configId)) {
                ProvisioningResult result = new ProvisioningResult();
                String mProvisionStr = ImsConfigSettings.getProvisionStr(configId);
                Message msg = this.mHandler.obtainMessage(4, result);
                synchronized (result.lockObj) {
                    this.mRilAdapter.getProvisionValue(this.mPhoneId, mProvisionStr, msg);
                    try {
                        result.lockObj.wait(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        result.provisionResult = 21;
                    }
                }
                if (result.provisionResult == 25) {
                    enforceConfigStorageInit("MD no default value, getProvisionedStringValue(" + configId + ")");
                    String access$1500 = this.mConfigHelper.getConfigStringValue(ImsConfigContract.TABLE_MASTER, configId);
                    return access$1500;
                } else if (isProvisionSuccess(result.provisionResult)) {
                    String str = result.provisionInfo[1];
                    return str;
                } else {
                    throw new ImsException("Something wrong, reason:" + result.provisionResult, 101);
                }
            } else {
                enforceConfigStorageInit("getProvisionedStringValue(" + configId + ")");
                String access$15002 = this.mConfigHelper.getConfigStringValue(ImsConfigContract.TABLE_MASTER, configId);
                return access$15002;
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public void setProvisionedValue(int configId, int value) throws ImsException {
        synchronized (this.mProvisionedValueLock) {
            enforceConfigStorageInit("setProvisionedValue(" + configId + ", " + value + ")");
            if (isProvisionStoreModem(configId)) {
                ProvisioningResult result = new ProvisioningResult();
                String mProvisionStr = ImsConfigSettings.getProvisionStr(configId);
                Message msg = this.mHandler.obtainMessage(5, result);
                synchronized (result.lockObj) {
                    this.mRilAdapter.setProvisionValue(this.mPhoneId, mProvisionStr, Integer.toString(value), msg);
                    try {
                        result.lockObj.wait(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        result.provisionResult = 21;
                    }
                }
                if (!isProvisionSuccess(result.provisionResult)) {
                    throw new ImsException("Something wrong, reason:" + result.provisionResult, 101);
                }
            }
            Uri unused = this.mConfigHelper.addConfig(ImsConfigContract.TABLE_PROVISION, configId, 0, value);
            int unused2 = this.mConfigHelper.updateConfig(ImsConfigContract.TABLE_MASTER, configId, 0, value);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public void setProvisionedStringValue(int configId, String value) throws ImsException {
        synchronized (this.mProvisionedStringValueLock) {
            enforceConfigStorageInit("setProvisionedStringValue(" + configId + ", " + value + ")");
            if (isProvisionStoreModem(configId)) {
                ProvisioningResult result = new ProvisioningResult();
                String mProvisionStr = ImsConfigSettings.getProvisionStr(configId);
                Message msg = this.mHandler.obtainMessage(5, result);
                synchronized (result.lockObj) {
                    this.mRilAdapter.setProvisionValue(this.mPhoneId, mProvisionStr, value, msg);
                    try {
                        result.lockObj.wait(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        result.provisionResult = 21;
                    }
                }
                if (!isProvisionSuccess(result.provisionResult)) {
                    throw new ImsException("Something wrong, reason:" + result.provisionResult, 101);
                }
            }
            Uri unused = this.mConfigHelper.addConfig(ImsConfigContract.TABLE_PROVISION, configId, 1, value);
            int unused2 = this.mConfigHelper.updateConfig(ImsConfigContract.TABLE_MASTER, configId, 1, value);
        }
    }

    public synchronized void setImsResCapability(int featureId, int value) throws ImsException {
        this.mResourceHelper.updateResource(featureId, value);
    }

    public synchronized int getImsResCapability(int featureId) throws ImsException {
        return this.mResourceHelper.getResourceValue(featureId);
    }

    private void enforceConfigStorageInit(String msg) throws ImsException {
        if (!this.mConfigHelper.isInitDone()) {
            Log.e(TAG, msg);
            throw new ImsException("Config storage not ready", 102);
        }
    }

    private static boolean isProvisionStoreModem(int configId) {
        boolean checkIsStoreModem = ImsConfigSettings.getIsStoreModem(configId);
        if (DEBUG) {
            Log.d(TAG, "isProvisionStoreModem: " + configId + ", checkIsStoreModem: " + checkIsStoreModem);
        }
        return checkIsStoreModem;
    }

    /* access modifiers changed from: private */
    public void resetFeatureSendCmd() {
        HashMap<Integer, Boolean> map = FeatureSendArray[this.mPhoneId].getFeatureMap();
        map.put(0, false);
        map.put(1, false);
        map.put(2, false);
        map.put(3, false);
    }

    private static boolean isProvisionSuccess(int reason) {
        switch (reason) {
            case 23:
                return true;
            default:
                return false;
        }
    }

    public void resetConfigStorage() {
        resetConfigStorage(0);
    }

    public void resetConfigStorage(int opCode) {
        Log.d(TAG, "resetConfigStorage(" + opCode + ")");
        synchronized (this.mConfigHelper) {
            this.mConfigHelper.clear();
            this.mConfigHelper.init(opCode);
        }
    }

    public void resetFeatureStorage() {
        Log.d(TAG, "resetFeatureStorage()");
        synchronized (this.mFeatureHelper) {
            this.mFeatureHelper.clear();
        }
    }

    public synchronized void setVoltePreference(int mode) {
        Log.i(TAG, "setVoltePreference mode:" + mode + ", phoneId:" + this.mPhoneId);
        this.mRilAdapter.setVoiceDomainPreference(mode, (Message) null);
    }

    public void sendWfcProfileInfo(int rilWfcMode) {
        synchronized (this.mWfcLock) {
            Log.i(TAG, "sendWfcProfileInfo rilWfcMode:" + rilWfcMode + ", curWfcMode:" + this.curWfcMode);
            if (rilWfcMode != this.curWfcMode) {
                this.mRilAdapter.sendWfcProfileInfo(rilWfcMode, (Message) null);
                if (rilWfcMode != 0) {
                    if (DEBUG) {
                        Log.d(TAG, "Not wifi-only mode, trun radio ON");
                    }
                    ImsConfigUtils.sendWifiOnlyModeIntent(this.mContext, this.mPhoneId, false);
                } else if (ImsConfigUtils.isWfcEnabledByUser(this.mContext, this.mPhoneId)) {
                    if (DEBUG) {
                        Log.d(TAG, "Wifi-only and WFC setting enabled, send intent to turn radio OFF");
                    }
                    ImsConfigUtils.sendWifiOnlyModeIntent(this.mContext, this.mPhoneId, true);
                } else {
                    if (DEBUG) {
                        Log.d(TAG, "Wifi-only and WFC setting disabled, send intent to turn radio ON");
                    }
                    ImsConfigUtils.sendWifiOnlyModeIntent(this.mContext, this.mPhoneId, false);
                }
                this.curWfcMode = rilWfcMode;
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    public int[] setModemImsCfg(String[] keys, String[] values, int type) {
        synchronized (this.mMdCfgLock) {
            if (keys == null) {
                Log.d(TAG, "keys is null, return null");
                return null;
            }
            if (keys.length >= 1) {
                if (values.length >= 1) {
                    if (keys.length == values.length) {
                        Log.d(TAG, "keys and values length equals");
                        String keysStr = ImsConfigUtils.arrayToString(keys);
                        String valuesStr = ImsConfigUtils.arrayToString(values);
                        Log.d(TAG, "keysStr:" + keysStr + ", valuesStr:" + valuesStr);
                        MdConfigResult cfgResult = new MdConfigResult();
                        cfgResult.requestConfigNum = keys.length;
                        Message msg = this.mHandler.obtainMessage(7, cfgResult);
                        synchronized (cfgResult.lockObj) {
                            this.mRilAdapter.setModemImsCfg(keysStr, valuesStr, type, msg);
                            try {
                                cfgResult.lockObj.wait(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                cfgResult.configResult = 31;
                            }
                        }
                        int[] resultArray = cfgResult.resultArray;
                        return resultArray;
                    }
                    Log.d(TAG, "keys and values length not equals");
                    return null;
                }
            }
            Log.d(TAG, "keys or values length is smaller than 1, return null");
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void resetWfcModeFlag() {
        if (TELDBG) {
            Log.d(TAG, "resetWfcModeFlag()");
        }
        synchronized (this.mWfcLock) {
            this.curWfcMode = -1;
        }
    }

    private static class FeatureHelper {
        private ContentResolver mContentResolver = null;
        private Context mContext = null;
        private HashMap<Integer, Integer> mIsFeatureBroadcast = new HashMap<>();
        private int mPhoneId;

        FeatureHelper(Context context, int phoneId) {
            this.mPhoneId = phoneId;
            this.mContext = context;
            this.mContentResolver = context.getContentResolver();
            resetBroadcastFlag();
        }

        /* access modifiers changed from: private */
        public void initFeatureStorage() {
            int volte = ImsConfigUtils.getFeaturePropValue(ImsConfigUtils.PROPERTY_VOLTE_ENALBE, this.mPhoneId);
            updateFeature(0, 13, volte);
            Log.d(ImsConfigStorage.TAG, "updateFeature: VoLTE initial value:" + volte + " for phoneId:" + this.mPhoneId);
            int vilte = ImsConfigUtils.getFeaturePropValue(ImsConfigUtils.PROPERTY_VILTE_ENALBE, this.mPhoneId);
            updateFeature(1, 13, vilte);
            Log.d(ImsConfigStorage.TAG, "updateFeature: ViLTE initial value:" + vilte + " for phoneId:" + this.mPhoneId);
            int vowifi = ImsConfigUtils.getFeaturePropValue(ImsConfigUtils.PROPERTY_WFC_ENALBE, this.mPhoneId);
            updateFeature(2, 18, vowifi);
            Log.d(ImsConfigStorage.TAG, "updateFeature: VoWIFI initial value:" + vowifi + " for phoneId:" + this.mPhoneId);
        }

        /* access modifiers changed from: private */
        public void clear() {
            try {
                this.mContentResolver.delete(ImsConfigContract.Feature.CONTENT_URI, "phone_id = ?", new String[]{String.valueOf(this.mPhoneId)});
            } catch (IllegalArgumentException e) {
                Log.e(ImsConfigStorage.TAG, "FeatureHelper.clear IllegalArgumentException");
            } catch (SecurityException e2) {
                Log.e(ImsConfigStorage.TAG, "FeatureHelper.clear SecurityException");
            } catch (Exception e3) {
                Log.e(ImsConfigStorage.TAG, "FeatureHelper.clear Exception");
            }
        }

        /* access modifiers changed from: private */
        public void updateFeature(int featureId, int network, int value) {
            ContentValues cv = new ContentValues();
            cv.put("phone_id", Integer.valueOf(this.mPhoneId));
            cv.put("feature_id", Integer.valueOf(featureId));
            cv.put(ImsConfigContract.Feature.NETWORK_ID, Integer.valueOf(network));
            cv.put("value", Integer.valueOf(value));
            try {
                int curValue = getFeatureValue(featureId, network);
                if (ImsConfigStorage.DEBUG) {
                    Log.d(ImsConfigStorage.TAG, "updateFeature() comparing: curValue: " + curValue + ", value:" + value);
                }
                if (!checkIfBroadcastOnce(featureId, this.mPhoneId) || curValue != value || curValue == -1) {
                    this.mContentResolver.update(ImsConfigContract.Feature.getUriWithFeatureId(this.mPhoneId, featureId, network), cv, (String) null, (String[]) null);
                }
            } catch (ImsException e) {
                Log.e(ImsConfigStorage.TAG, "updateFeature() ImsException featureId:" + featureId + ", value:" + value);
                try {
                    this.mContentResolver.insert(ImsConfigContract.Feature.CONTENT_URI, cv);
                } catch (IllegalArgumentException e2) {
                    Log.e(ImsConfigStorage.TAG, "updateFeature IllegalArgumentException");
                } catch (SecurityException e3) {
                    Log.e(ImsConfigStorage.TAG, "updateFeature SecurityException");
                } catch (Exception e4) {
                    Log.e(ImsConfigStorage.TAG, "updateFeature Exception");
                }
            }
        }

        /* Debug info: failed to restart local var, previous not found, register: 16 */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0043, code lost:
            if (r5 != null) goto L_0x0045;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a1, code lost:
            if (r5 != null) goto L_0x0045;
         */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0097 A[Catch:{ IllegalArgumentException -> 0x0098, SecurityException -> 0x008c, Exception -> 0x006d, all -> 0x0069, all -> 0x008a }] */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x00a7  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int getFeatureValue(int r17, int r18) throws com.android.ims.ImsException {
            /*
                r16 = this;
                r1 = r16
                r2 = r17
                java.lang.String r3 = "Feature "
                java.lang.String r4 = "ImsConfigStorage"
                r5 = 0
                r6 = -1
                java.lang.String r0 = "phone_id"
                java.lang.String r7 = "feature_id"
                java.lang.String r8 = "network_id"
                java.lang.String r9 = "value"
                java.lang.String[] r12 = new java.lang.String[]{r0, r7, r8, r9}
                r7 = 101(0x65, float:1.42E-43)
                android.content.ContentResolver r10 = r1.mContentResolver     // Catch:{ IllegalArgumentException -> 0x0098, SecurityException -> 0x008c, Exception -> 0x006d, all -> 0x0069 }
                int r0 = r1.mPhoneId     // Catch:{ IllegalArgumentException -> 0x0098, SecurityException -> 0x008c, Exception -> 0x006d, all -> 0x0069 }
                r8 = r18
                android.net.Uri r11 = com.mediatek.ims.config.ImsConfigContract.Feature.getUriWithFeatureId(r0, r2, r8)     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                r13 = 0
                r14 = 0
                r15 = 0
                android.database.Cursor r0 = r10.query(r11, r12, r13, r14, r15)     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                r5 = r0
                if (r5 == 0) goto L_0x0049
                int r0 = r5.getCount()     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                r10 = 1
                if (r0 != r10) goto L_0x0049
                r5.moveToFirst()     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                int r0 = r5.getColumnIndex(r9)     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                int r9 = r5.getInt(r0)     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                r6 = r9
                r5.close()     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                if (r5 == 0) goto L_0x00a4
            L_0x0045:
                r5.close()
                goto L_0x00a4
            L_0x0049:
                com.android.ims.ImsException r0 = new com.android.ims.ImsException     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                r9.<init>()     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                r9.append(r3)     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                r9.append(r2)     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                java.lang.String r10 = " not assigned with value!"
                r9.append(r10)     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                java.lang.String r9 = r9.toString()     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                r0.<init>(r9, r7)     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
                throw r0     // Catch:{ IllegalArgumentException -> 0x0067, SecurityException -> 0x0065, Exception -> 0x0063 }
            L_0x0063:
                r0 = move-exception
                goto L_0x0070
            L_0x0065:
                r0 = move-exception
                goto L_0x008f
            L_0x0067:
                r0 = move-exception
                goto L_0x009b
            L_0x0069:
                r0 = move-exception
                r8 = r18
                goto L_0x00a5
            L_0x006d:
                r0 = move-exception
                r8 = r18
            L_0x0070:
                com.android.ims.ImsException r4 = new com.android.ims.ImsException     // Catch:{ all -> 0x008a }
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ all -> 0x008a }
                r9.<init>()     // Catch:{ all -> 0x008a }
                r9.append(r3)     // Catch:{ all -> 0x008a }
                r9.append(r2)     // Catch:{ all -> 0x008a }
                java.lang.String r3 = " not assigned with value! or something wrong with cursor"
                r9.append(r3)     // Catch:{ all -> 0x008a }
                java.lang.String r3 = r9.toString()     // Catch:{ all -> 0x008a }
                r4.<init>(r3, r7)     // Catch:{ all -> 0x008a }
                throw r4     // Catch:{ all -> 0x008a }
            L_0x008a:
                r0 = move-exception
                goto L_0x00a5
            L_0x008c:
                r0 = move-exception
                r8 = r18
            L_0x008f:
                java.lang.String r3 = "getFeatureValue SecurityException"
                android.util.Log.e(r4, r3)     // Catch:{ all -> 0x008a }
                if (r5 == 0) goto L_0x00a4
                goto L_0x0045
            L_0x0098:
                r0 = move-exception
                r8 = r18
            L_0x009b:
                java.lang.String r3 = "getFeatureValue IllegalArgumentException"
                android.util.Log.e(r4, r3)     // Catch:{ all -> 0x008a }
                if (r5 == 0) goto L_0x00a4
                goto L_0x0045
            L_0x00a4:
                return r6
            L_0x00a5:
                if (r5 == 0) goto L_0x00aa
                r5.close()
            L_0x00aa:
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.config.internal.ImsConfigStorage.FeatureHelper.getFeatureValue(int, int):int");
        }

        private boolean checkIfBroadcastOnce(int feature, int phoneId) {
            String simState = ImsConfigProvider.LatestSimState.get(Integer.valueOf(phoneId));
            if (ImsConfigStorage.TELDBG) {
                Log.d(ImsConfigStorage.TAG, "checkIfBroadcastOnce() phoneId: " + phoneId + ", Sim state: " + simState);
            }
            if (simState == null) {
                return false;
            }
            if (simState != null && !simState.equals("READY") && !simState.equals("IMSI") && !simState.equals("LOADED") && !simState.equals("LOCKED")) {
                return false;
            }
            if (this.mIsFeatureBroadcast.get(Integer.valueOf(feature)).intValue() != 0) {
                return true;
            }
            this.mIsFeatureBroadcast.put(Integer.valueOf(feature), 1);
            return false;
        }

        /* access modifiers changed from: private */
        public void resetBroadcastFlag() {
            this.mIsFeatureBroadcast.put(0, 0);
            this.mIsFeatureBroadcast.put(1, 0);
            this.mIsFeatureBroadcast.put(2, 0);
            this.mIsFeatureBroadcast.put(3, 0);
        }

        /* access modifiers changed from: private */
        public boolean isAllFeatureFalse() {
            try {
                int volte = getFeatureValue(0, ImsConfigContract.getNetworkTypeByFeature(0));
                int vilte = getFeatureValue(1, ImsConfigContract.getNetworkTypeByFeature(1));
                int wfc = getFeatureValue(2, ImsConfigContract.getNetworkTypeByFeature(2));
                if (volte == 0 && vilte == 0 && wfc == 0) {
                    return true;
                }
                return false;
            } catch (ImsException e) {
                Log.e(ImsConfigStorage.TAG, "isAllFeatureFalse volte:" + -1 + ", vilte:" + -1 + ", wfc:" + -1);
                return false;
            }
        }
    }

    private static class ConfigHelper {
        private ContentResolver mContentResolver = null;
        private Context mContext = null;
        DefaultConfigPolicyFactory mDefConfigPolicyFactory = null;
        private Handler mHandler = null;
        private boolean mInitDone = false;
        private int mOpCode = -1;
        private int mPhoneId = -1;

        ConfigHelper(Context context, Handler handler, int phoneId) {
            this.mContext = context;
            this.mHandler = handler;
            this.mPhoneId = phoneId;
            this.mContentResolver = context.getContentResolver();
            String opCode = null;
            try {
                opCode = getConfigSetting(0);
                this.mOpCode = Integer.parseInt(opCode);
            } catch (ImsException e) {
                this.mOpCode = -1;
            } catch (NumberFormatException e2) {
                Log.e(ImsConfigStorage.TAG, "Parse SETTING_ID_OPCODE error: " + opCode);
                this.mOpCode = -1;
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void setOpCode(int opCode) {
            this.mOpCode = opCode;
        }

        /* access modifiers changed from: package-private */
        public synchronized int getOpCode() {
            return this.mOpCode;
        }

        /* access modifiers changed from: package-private */
        public synchronized void setInitDone(boolean done) {
            this.mInitDone = done;
            Intent intent = new Intent(ImsConfigContract.ACTION_CONFIG_LOADED);
            intent.putExtra("phone_id", this.mPhoneId);
            this.mContext.sendBroadcast(intent, "android.permission.READ_PHONE_STATE");
        }

        /* access modifiers changed from: package-private */
        public synchronized boolean isInitDone() {
            return this.mInitDone;
        }

        /* access modifiers changed from: package-private */
        public void init() {
            initDefaultStorage(0);
            initMasterStorage();
        }

        /* access modifiers changed from: package-private */
        public void init(int opCode) {
            initDefaultStorage(opCode);
            initMasterStorage();
            initConfigSettingStorage(opCode);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:22:0x004f, code lost:
            if (r1 == null) goto L_0x0052;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:7:0x002c, code lost:
            if (r1 != null) goto L_0x002e;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean isStorageInitialized() {
            /*
                r12 = this;
                java.lang.String r0 = "ImsConfigStorage"
                r1 = 0
                r2 = 0
                java.lang.String r3 = "phone_id"
                java.lang.String r4 = "setting_id"
                java.lang.String r5 = "value"
                java.lang.String[] r8 = new java.lang.String[]{r3, r4, r5}
                android.content.ContentResolver r6 = r12.mContentResolver     // Catch:{ IllegalArgumentException -> 0x0048, SecurityException -> 0x003e, Exception -> 0x0034 }
                int r3 = r12.mPhoneId     // Catch:{ IllegalArgumentException -> 0x0048, SecurityException -> 0x003e, Exception -> 0x0034 }
                r4 = 0
                android.net.Uri r7 = com.mediatek.ims.config.ImsConfigContract.ConfigSetting.getUriWithSettingId(r3, r4)     // Catch:{ IllegalArgumentException -> 0x0048, SecurityException -> 0x003e, Exception -> 0x0034 }
                r9 = 0
                r10 = 0
                r11 = 0
                android.database.Cursor r3 = r6.query(r7, r8, r9, r10, r11)     // Catch:{ IllegalArgumentException -> 0x0048, SecurityException -> 0x003e, Exception -> 0x0034 }
                r1 = r3
                if (r1 == 0) goto L_0x002c
                int r3 = r1.getCount()     // Catch:{ IllegalArgumentException -> 0x0048, SecurityException -> 0x003e, Exception -> 0x0034 }
                r4 = 1
                if (r3 != r4) goto L_0x002c
                r2 = 1
                r1.close()     // Catch:{ IllegalArgumentException -> 0x0048, SecurityException -> 0x003e, Exception -> 0x0034 }
            L_0x002c:
                if (r1 == 0) goto L_0x0052
            L_0x002e:
                r1.close()
                goto L_0x0052
            L_0x0032:
                r0 = move-exception
                goto L_0x0053
            L_0x0034:
                r3 = move-exception
                java.lang.String r4 = "isStorageInitialized Exception"
                android.util.Log.e(r0, r4)     // Catch:{ all -> 0x0032 }
                if (r1 == 0) goto L_0x0052
                goto L_0x002e
            L_0x003e:
                r3 = move-exception
                java.lang.String r4 = "isStorageInitialized SecurityException"
                android.util.Log.e(r0, r4)     // Catch:{ all -> 0x0032 }
                if (r1 == 0) goto L_0x0052
                goto L_0x002e
            L_0x0048:
                r3 = move-exception
                java.lang.String r4 = "isStorageInitialized IllegalArgumentException"
                android.util.Log.e(r0, r4)     // Catch:{ all -> 0x0032 }
                if (r1 == 0) goto L_0x0052
                goto L_0x002e
            L_0x0052:
                return r2
            L_0x0053:
                if (r1 == 0) goto L_0x0058
                r1.close()
            L_0x0058:
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.config.internal.ImsConfigStorage.ConfigHelper.isStorageInitialized():boolean");
        }

        static void loadConfigStorage(Handler handler, int operatorCode) {
            if (handler != null) {
                handler.removeMessages(1);
                Message msg = new Message();
                msg.what = 1;
                msg.obj = Integer.valueOf(operatorCode);
                if (ImsConfigStorage.DEBUG) {
                    Log.d(ImsConfigStorage.TAG, "LoadConfigStorage() msg = " + msg.hashCode());
                }
                handler.sendMessage(msg);
            }
        }

        private void initConfigSettingStorage(int opCode) {
            addConfigSetting(0, Integer.toString(opCode));
        }

        /* Debug info: failed to restart local var, previous not found, register: 6 */
        private void addConfigSetting(int id, String value) {
            ContentValues cv = new ContentValues();
            cv.put("phone_id", Integer.valueOf(this.mPhoneId));
            cv.put(ImsConfigContract.ConfigSetting.SETTING_ID, Integer.valueOf(id));
            cv.put("value", value);
            try {
                if (this.mContentResolver.insert(ImsConfigContract.ConfigSetting.CONTENT_URI, cv) == null) {
                    throw new IllegalArgumentException("addConfigSetting " + id + " for phone " + this.mPhoneId + " failed!");
                }
            } catch (IllegalArgumentException e) {
                Log.e(ImsConfigStorage.TAG, "addConfigSetting IllegalArgumentException");
            } catch (SecurityException e2) {
                Log.e(ImsConfigStorage.TAG, "addConfigSetting SecurityException");
            } catch (Exception e3) {
                Log.e(ImsConfigStorage.TAG, "addConfigSetting Exception");
            }
        }

        /* Debug info: failed to restart local var, previous not found, register: 7 */
        private void updateConfigSetting(int id, int value) {
            ContentValues cv = new ContentValues();
            cv.put("phone_id", Integer.valueOf(this.mPhoneId));
            cv.put(ImsConfigContract.ConfigSetting.SETTING_ID, Integer.valueOf(id));
            cv.put("value", Integer.valueOf(value));
            try {
                if (this.mContentResolver.update(ImsConfigContract.getConfigUri(ImsConfigContract.TABLE_CONFIG_SETTING, this.mPhoneId, id), cv, (String) null, (String[]) null) != 1) {
                    throw new IllegalArgumentException("updateConfigSetting " + id + " for phone " + this.mPhoneId + " failed!");
                }
            } catch (IllegalArgumentException e) {
                Log.e(ImsConfigStorage.TAG, "updateConfigSetting IllegalArgumentException");
            } catch (SecurityException e2) {
                Log.e(ImsConfigStorage.TAG, "updateConfigSetting SecurityException");
            } catch (Exception e3) {
                Log.e(ImsConfigStorage.TAG, "updateConfigSetting Exception");
            }
        }

        /* Debug info: failed to restart local var, previous not found, register: 14 */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x009c, code lost:
            if (r3 == null) goto L_0x009f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:7:0x003e, code lost:
            if (r3 != null) goto L_0x0040;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private java.lang.String getConfigSetting(int r15) throws com.android.ims.ImsException {
            /*
                r14 = this;
                java.lang.String r0 = " for phone "
                java.lang.String r1 = "getConfigSetting "
                java.lang.String r2 = "ImsConfigStorage"
                r3 = 0
                java.lang.String r4 = ""
                java.lang.String r5 = "phone_id"
                java.lang.String r6 = "setting_id"
                java.lang.String r7 = "value"
                java.lang.String[] r10 = new java.lang.String[]{r5, r6, r7}
                r5 = 102(0x66, float:1.43E-43)
                android.content.ContentResolver r8 = r14.mContentResolver     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                int r6 = r14.mPhoneId     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                android.net.Uri r9 = com.mediatek.ims.config.ImsConfigContract.ConfigSetting.getUriWithSettingId(r6, r15)     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                r11 = 0
                r12 = 0
                r13 = 0
                android.database.Cursor r6 = r8.query(r9, r10, r11, r12, r13)     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                r3 = r6
                if (r3 == 0) goto L_0x0044
                int r6 = r3.getCount()     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                r8 = 1
                if (r6 != r8) goto L_0x0044
                r3.moveToFirst()     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                int r6 = r3.getColumnIndex(r7)     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                java.lang.String r7 = r3.getString(r6)     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                r4 = r7
                r3.close()     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                if (r3 == 0) goto L_0x009f
            L_0x0040:
                r3.close()
                goto L_0x009f
            L_0x0044:
                com.android.ims.ImsException r6 = new com.android.ims.ImsException     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                r7.<init>()     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                r7.append(r1)     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                r7.append(r15)     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                r7.append(r0)     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                int r8 = r14.mPhoneId     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                r7.append(r8)     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                java.lang.String r8 = " not found"
                r7.append(r8)     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                java.lang.String r7 = r7.toString()     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                r6.<init>(r7, r5)     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
                throw r6     // Catch:{ IllegalArgumentException -> 0x0095, SecurityException -> 0x008b, Exception -> 0x0068 }
            L_0x0066:
                r0 = move-exception
                goto L_0x00a0
            L_0x0068:
                r2 = move-exception
                com.android.ims.ImsException r6 = new com.android.ims.ImsException     // Catch:{ all -> 0x0066 }
                java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x0066 }
                r7.<init>()     // Catch:{ all -> 0x0066 }
                r7.append(r1)     // Catch:{ all -> 0x0066 }
                r7.append(r15)     // Catch:{ all -> 0x0066 }
                r7.append(r0)     // Catch:{ all -> 0x0066 }
                int r0 = r14.mPhoneId     // Catch:{ all -> 0x0066 }
                r7.append(r0)     // Catch:{ all -> 0x0066 }
                java.lang.String r0 = " not found or something wrong with cursor"
                r7.append(r0)     // Catch:{ all -> 0x0066 }
                java.lang.String r0 = r7.toString()     // Catch:{ all -> 0x0066 }
                r6.<init>(r0, r5)     // Catch:{ all -> 0x0066 }
                throw r6     // Catch:{ all -> 0x0066 }
            L_0x008b:
                r0 = move-exception
                java.lang.String r1 = "getConfigSetting SecurityException"
                android.util.Log.e(r2, r1)     // Catch:{ all -> 0x0066 }
                if (r3 == 0) goto L_0x009f
                goto L_0x0040
            L_0x0095:
                r0 = move-exception
                java.lang.String r1 = "getConfigSetting IllegalArgumentException"
                android.util.Log.e(r2, r1)     // Catch:{ all -> 0x0066 }
                if (r3 == 0) goto L_0x009f
                goto L_0x0040
            L_0x009f:
                return r4
            L_0x00a0:
                if (r3 == 0) goto L_0x00a5
                r3.close()
            L_0x00a5:
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.config.internal.ImsConfigStorage.ConfigHelper.getConfigSetting(int):java.lang.String");
        }

        private void initDefaultStorage(int opCode) {
            Map<Integer, ImsConfigSettings.Setting> configSettings = ImsConfigSettings.getConfigSettings();
            new HashMap();
            DefaultConfigPolicyFactory instanceByOpCode = DefaultConfigPolicyFactory.getInstanceByOpCode(opCode);
            this.mDefConfigPolicyFactory = instanceByOpCode;
            Map<Integer, ImsConfigPolicy.DefaultConfig> defSettings = instanceByOpCode.load();
            if (defSettings == null || !defSettings.isEmpty()) {
                for (Integer configId : configSettings.keySet()) {
                    String value = ImsConfigContract.VALUE_NO_DEFAULT;
                    int unitId = -1;
                    if (this.mDefConfigPolicyFactory.hasDefaultValue(configId.intValue())) {
                        ImsConfigPolicy.DefaultConfig base = defSettings.get(configId);
                        if (base != null) {
                            value = base.defVal;
                            unitId = base.unitId;
                        }
                        ImsConfigSettings.Setting setting = configSettings.get(configId);
                        try {
                            if (setting.mimeType == 0) {
                                ContentValues cv = getConfigCv(configId.intValue(), setting.mimeType, Integer.parseInt(value));
                                cv.put(ImsConfigContract.Default.UNIT_ID, Integer.valueOf(unitId));
                                this.mContentResolver.insert(ImsConfigContract.Default.CONTENT_URI, cv);
                            } else if (1 == setting.mimeType) {
                                ContentValues cv2 = getConfigCv(configId.intValue(), setting.mimeType, value);
                                cv2.put(ImsConfigContract.Default.UNIT_ID, Integer.valueOf(unitId));
                                this.mContentResolver.insert(ImsConfigContract.Default.CONTENT_URI, cv2);
                            }
                        } catch (IllegalArgumentException e) {
                            Log.e(ImsConfigStorage.TAG, "initDefaultStorage IllegalArgumentException");
                        } catch (SecurityException e2) {
                            Log.e(ImsConfigStorage.TAG, "initDefaultStorage SecurityException");
                        } catch (Exception e3) {
                            Log.e(ImsConfigStorage.TAG, "initDefaultStorage Exception");
                        }
                    }
                }
                return;
            }
            Log.d(ImsConfigStorage.TAG, "No default value");
        }

        /* Debug info: failed to restart local var, previous not found, register: 20 */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x00be, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x00c8, code lost:
            if (r10 != null) goto L_0x00ca;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x00ca, code lost:
            r10.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x00d3, code lost:
            r10.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x00dd, code lost:
            if (r10 == null) goto L_0x00e0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x00e0, code lost:
            if (r13 == false) goto L_0x00e4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x00e2, code lost:
            if (r10 != null) goto L_0x0105;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x00e4, code lost:
            r11.put("phone_id", java.lang.Integer.valueOf(r1.mPhoneId));
            r11.put("config_id", r9);
            r11.put(com.mediatek.ims.config.ImsConfigContract.BasicConfigTable.MIMETYPE_ID, java.lang.Integer.valueOf(com.mediatek.ims.config.ImsConfigSettings.getMimeType(r9.intValue())));
            r11.put(com.mediatek.ims.config.ImsConfigContract.BasicConfigTable.DATA, com.mediatek.ims.config.ImsConfigContract.VALUE_NO_DEFAULT);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
            r1.mContentResolver.insert(com.mediatek.ims.config.ImsConfigContract.Master.CONTENT_URI, r11);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x010c, code lost:
            if (0 == 0) goto L_0x012b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x010e, code lost:
            r1.mContentResolver.insert(com.mediatek.ims.config.ImsConfigContract.Provision.CONTENT_URI, r11);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x0117, code lost:
            android.util.Log.e(com.mediatek.ims.config.internal.ImsConfigStorage.TAG, "initMasterStorage Exception");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x011e, code lost:
            android.util.Log.e(com.mediatek.ims.config.internal.ImsConfigStorage.TAG, "initMasterStorage SecurityException");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:48:0x0125, code lost:
            android.util.Log.e(com.mediatek.ims.config.internal.ImsConfigStorage.TAG, "initMasterStorage IllegalArgumentException");
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x00be A[ExcHandler: all (th java.lang.Throwable), Splitter:B:15:0x008f] */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x00d3  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void initMasterStorage() {
            /*
                r20 = this;
                r1 = r20
                java.lang.String r2 = "data"
                java.lang.String r3 = "mimetype_id"
                java.lang.String r4 = "config_id"
                java.lang.String r5 = "phone_id"
                java.lang.String r6 = "ImsConfigStorage"
                java.util.Map r7 = com.mediatek.ims.config.ImsConfigSettings.getConfigSettings()
                java.util.Set r0 = r7.keySet()
                java.util.Iterator r8 = r0.iterator()
            L_0x0018:
                boolean r0 = r8.hasNext()
                if (r0 != 0) goto L_0x001f
                return
            L_0x001f:
                java.lang.Object r0 = r8.next()
                r9 = r0
                java.lang.Integer r9 = (java.lang.Integer) r9
                r10 = 0
                android.content.ContentValues r0 = new android.content.ContentValues
                r0.<init>()
                r11 = r0
                r12 = 0
                r13 = 1
                com.android.ims.ImsException r0 = new com.android.ims.ImsException     // Catch:{ ImsException -> 0x0039 }
                java.lang.String r14 = "here"
                r15 = 102(0x66, float:1.43E-43)
                r0.<init>(r14, r15)     // Catch:{ ImsException -> 0x0039 }
                throw r0     // Catch:{ ImsException -> 0x0039 }
            L_0x0039:
                r0 = move-exception
                r14 = r0
                java.lang.String r0 = "tb_default"
                int r15 = r9.intValue()     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                android.database.Cursor r0 = r1.getConfigFirstCursor(r0, r15)     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                r10 = r0
                if (r10 == 0) goto L_0x00c4
                int r0 = r10.getColumnIndex(r5)     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                int r15 = r10.getColumnIndex(r4)     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                int r16 = r10.getColumnIndex(r3)     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                r17 = r16
                int r16 = r10.getColumnIndex(r2)     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                r18 = r16
                int r16 = r10.getInt(r0)     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                r19 = r0
                java.lang.Integer r0 = java.lang.Integer.valueOf(r16)     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                r11.put(r5, r0)     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                int r0 = r10.getInt(r15)     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                r11.put(r4, r0)     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                r0 = r17
                int r16 = r10.getInt(r0)     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                r17 = r0
                java.lang.Integer r0 = java.lang.Integer.valueOf(r16)     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                r11.put(r3, r0)     // Catch:{ ImsException -> 0x00d7, all -> 0x00ce }
                r16 = r7
                r0 = r18
                java.lang.String r7 = r10.getString(r0)     // Catch:{ ImsException -> 0x00c0, all -> 0x00be }
                r11.put(r2, r7)     // Catch:{ ImsException -> 0x00c0, all -> 0x00be }
                java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ ImsException -> 0x00c0, all -> 0x00be }
                r7.<init>()     // Catch:{ ImsException -> 0x00c0, all -> 0x00be }
                r18 = r8
                java.lang.String r8 = "Load default value "
                r7.append(r8)     // Catch:{ ImsException -> 0x00bc, all -> 0x00be }
                java.lang.String r8 = r10.getString(r0)     // Catch:{ ImsException -> 0x00bc, all -> 0x00be }
                r7.append(r8)     // Catch:{ ImsException -> 0x00bc, all -> 0x00be }
                java.lang.String r8 = " for config "
                r7.append(r8)     // Catch:{ ImsException -> 0x00bc, all -> 0x00be }
                r7.append(r9)     // Catch:{ ImsException -> 0x00bc, all -> 0x00be }
                java.lang.String r7 = r7.toString()     // Catch:{ ImsException -> 0x00bc, all -> 0x00be }
                android.util.Log.d(r6, r7)     // Catch:{ ImsException -> 0x00bc, all -> 0x00be }
                r10.close()     // Catch:{ ImsException -> 0x00bc, all -> 0x00be }
                goto L_0x00c8
            L_0x00bc:
                r0 = move-exception
                goto L_0x00dc
            L_0x00be:
                r0 = move-exception
                goto L_0x00d1
            L_0x00c0:
                r0 = move-exception
                r18 = r8
                goto L_0x00dc
            L_0x00c4:
                r16 = r7
                r18 = r8
            L_0x00c8:
                if (r10 == 0) goto L_0x00e0
            L_0x00ca:
                r10.close()
                goto L_0x00e0
            L_0x00ce:
                r0 = move-exception
                r16 = r7
            L_0x00d1:
                if (r10 == 0) goto L_0x00d6
                r10.close()
            L_0x00d6:
                throw r0
            L_0x00d7:
                r0 = move-exception
                r16 = r7
                r18 = r8
            L_0x00dc:
                r13 = 0
                if (r10 == 0) goto L_0x00e0
                goto L_0x00ca
            L_0x00e0:
                if (r13 == 0) goto L_0x00e4
                if (r10 != 0) goto L_0x0105
            L_0x00e4:
                int r0 = r1.mPhoneId
                java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
                r11.put(r5, r0)
                r11.put(r4, r9)
                int r0 = r9.intValue()
                int r0 = com.mediatek.ims.config.ImsConfigSettings.getMimeType(r0)
                java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
                r11.put(r3, r0)
                java.lang.String r0 = "n/a"
                r11.put(r2, r0)
            L_0x0105:
                android.content.ContentResolver r0 = r1.mContentResolver     // Catch:{ IllegalArgumentException -> 0x0124, SecurityException -> 0x011d, Exception -> 0x0116 }
                android.net.Uri r7 = com.mediatek.ims.config.ImsConfigContract.Master.CONTENT_URI     // Catch:{ IllegalArgumentException -> 0x0124, SecurityException -> 0x011d, Exception -> 0x0116 }
                r0.insert(r7, r11)     // Catch:{ IllegalArgumentException -> 0x0124, SecurityException -> 0x011d, Exception -> 0x0116 }
                if (r12 == 0) goto L_0x012a
                android.content.ContentResolver r0 = r1.mContentResolver     // Catch:{ IllegalArgumentException -> 0x0124, SecurityException -> 0x011d, Exception -> 0x0116 }
                android.net.Uri r7 = com.mediatek.ims.config.ImsConfigContract.Provision.CONTENT_URI     // Catch:{ IllegalArgumentException -> 0x0124, SecurityException -> 0x011d, Exception -> 0x0116 }
                r0.insert(r7, r11)     // Catch:{ IllegalArgumentException -> 0x0124, SecurityException -> 0x011d, Exception -> 0x0116 }
                goto L_0x012a
            L_0x0116:
                r0 = move-exception
                java.lang.String r7 = "initMasterStorage Exception"
                android.util.Log.e(r6, r7)
                goto L_0x012b
            L_0x011d:
                r0 = move-exception
                java.lang.String r7 = "initMasterStorage SecurityException"
                android.util.Log.e(r6, r7)
                goto L_0x012a
            L_0x0124:
                r0 = move-exception
                java.lang.String r7 = "initMasterStorage IllegalArgumentException"
                android.util.Log.e(r6, r7)
            L_0x012a:
            L_0x012b:
                r7 = r16
                r8 = r18
                goto L_0x0018
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.config.internal.ImsConfigStorage.ConfigHelper.initMasterStorage():void");
        }

        /* access modifiers changed from: private */
        public void clear() {
            String[] args = {String.valueOf(this.mPhoneId)};
            try {
                this.mContentResolver.delete(ImsConfigContract.ConfigSetting.CONTENT_URI, "phone_id = ?", args);
                this.mContentResolver.delete(ImsConfigContract.Provision.CONTENT_URI, "phone_id = ?", args);
                this.mContentResolver.delete(ImsConfigContract.Master.CONTENT_URI, "phone_id = ?", args);
                this.mContentResolver.delete(ImsConfigContract.Default.CONTENT_URI, "phone_id = ?", args);
            } catch (IllegalArgumentException e) {
                Log.e(ImsConfigStorage.TAG, "ConfigHelper.clear IllegalArgumentException");
            } catch (SecurityException e2) {
                Log.e(ImsConfigStorage.TAG, "ConfigHelper.clear SecurityException");
            } catch (Exception e3) {
                Log.e(ImsConfigStorage.TAG, "ConfigHelper.clear Exception");
            }
        }

        private ContentValues getConfigCv(int configId, int mimeType, int value) {
            ContentValues cv = new ContentValues();
            cv.put("phone_id", Integer.valueOf(this.mPhoneId));
            cv.put("config_id", Integer.valueOf(configId));
            cv.put(ImsConfigContract.BasicConfigTable.MIMETYPE_ID, Integer.valueOf(mimeType));
            cv.put(ImsConfigContract.BasicConfigTable.DATA, Integer.valueOf(value));
            return cv;
        }

        private ContentValues getConfigCv(int configId, int mimeType, String value) {
            ContentValues cv = new ContentValues();
            cv.put("phone_id", Integer.valueOf(this.mPhoneId));
            cv.put("config_id", Integer.valueOf(configId));
            cv.put(ImsConfigContract.BasicConfigTable.MIMETYPE_ID, Integer.valueOf(mimeType));
            cv.put(ImsConfigContract.BasicConfigTable.DATA, value);
            return cv;
        }

        /* access modifiers changed from: private */
        public Uri addConfig(String table, int configId, int mimeType, int value) throws ImsException {
            enforceConfigId(configId);
            try {
                return this.mContentResolver.insert(ImsConfigContract.getTableUri(table), getConfigCv(configId, mimeType, value));
            } catch (IllegalArgumentException e) {
                Log.e(ImsConfigStorage.TAG, "addConfig int IllegalArgumentException");
                return null;
            } catch (SecurityException e2) {
                Log.e(ImsConfigStorage.TAG, "addConfig int SecurityException");
                return null;
            } catch (Exception e3) {
                Log.e(ImsConfigStorage.TAG, "addConfig int Exception");
                return null;
            }
        }

        /* access modifiers changed from: private */
        public Uri addConfig(String table, int configId, int mimeType, String value) throws ImsException {
            enforceConfigId(configId);
            try {
                return this.mContentResolver.insert(ImsConfigContract.getTableUri(table), getConfigCv(configId, mimeType, value));
            } catch (IllegalArgumentException e) {
                Log.e(ImsConfigStorage.TAG, "addConfig string IllegalArgumentException");
                return null;
            } catch (SecurityException e2) {
                Log.e(ImsConfigStorage.TAG, "addConfig string SecurityException");
                return null;
            } catch (Exception e3) {
                Log.e(ImsConfigStorage.TAG, "addConfig string Exception");
                return null;
            }
        }

        /* access modifiers changed from: private */
        public int updateConfig(String table, int configId, int mimeType, int value) throws ImsException {
            enforceConfigId(configId);
            ContentValues cv = new ContentValues();
            cv.put("phone_id", Integer.valueOf(this.mPhoneId));
            cv.put("config_id", Integer.valueOf(configId));
            cv.put(ImsConfigContract.BasicConfigTable.MIMETYPE_ID, Integer.valueOf(mimeType));
            cv.put(ImsConfigContract.BasicConfigTable.DATA, Integer.valueOf(value));
            try {
                return this.mContentResolver.update(ImsConfigContract.getConfigUri(table, this.mPhoneId, configId), cv, (String) null, (String[]) null);
            } catch (IllegalArgumentException e) {
                Log.e(ImsConfigStorage.TAG, "updateConfig int IllegalArgumentException");
                return -1;
            } catch (SecurityException e2) {
                Log.e(ImsConfigStorage.TAG, "updateConfig int SecurityException");
                return -1;
            } catch (Exception e3) {
                Log.e(ImsConfigStorage.TAG, "updateConfig int Exception");
                return -1;
            }
        }

        /* access modifiers changed from: private */
        public int updateConfig(String table, int configId, int mimeType, String value) throws ImsException {
            enforceConfigId(configId);
            ContentValues cv = new ContentValues();
            cv.put("phone_id", Integer.valueOf(this.mPhoneId));
            cv.put("config_id", Integer.valueOf(configId));
            cv.put(ImsConfigContract.BasicConfigTable.MIMETYPE_ID, Integer.valueOf(mimeType));
            cv.put(ImsConfigContract.BasicConfigTable.DATA, value);
            try {
                return this.mContentResolver.update(ImsConfigContract.getConfigUri(table, this.mPhoneId, configId), cv, (String) null, (String[]) null);
            } catch (IllegalArgumentException e) {
                Log.e(ImsConfigStorage.TAG, "updateConfig string IllegalArgumentException");
                return -1;
            } catch (SecurityException e2) {
                Log.e(ImsConfigStorage.TAG, "updateConfig string SecurityException");
                return -1;
            } catch (Exception e3) {
                Log.e(ImsConfigStorage.TAG, "updateConfig string Exception");
                return -1;
            }
        }

        /* Debug info: failed to restart local var, previous not found, register: 11 */
        private Cursor getConfigFirstCursor(String table, int configId) throws ImsException {
            String[] projection = {"phone_id", "config_id", ImsConfigContract.BasicConfigTable.MIMETYPE_ID, ImsConfigContract.BasicConfigTable.DATA};
            try {
                Cursor c = this.mContentResolver.query(ImsConfigContract.getConfigUri(table, this.mPhoneId, configId), projection, (String) null, (String[]) null, (String) null);
                if (c == null) {
                    throw new ImsException("Null cursor with config: " + configId + " in table: " + table, 101);
                } else if (c.getCount() == 1) {
                    c.moveToFirst();
                    return c;
                } else if (c.getCount() == 0) {
                    c.close();
                    throw new ImsException("Config " + configId + " shall exist in table: " + table, 101);
                } else {
                    c.close();
                    throw new ImsException("Config " + configId + " shall exist once in table: " + table, 101);
                }
            } catch (IllegalArgumentException e) {
                Log.e(ImsConfigStorage.TAG, "getConfigFirstCursor IllegalArgumentException");
                return null;
            } catch (SecurityException e2) {
                Log.e(ImsConfigStorage.TAG, "getConfigFirstCursor SecurityException");
                return null;
            } catch (Exception e3) {
                Log.e(ImsConfigStorage.TAG, "getConfigFirstCursor Exception");
                return null;
            }
        }

        /* Debug info: failed to restart local var, previous not found, register: 11 */
        /* access modifiers changed from: private */
        public int getConfigValue(String table, int configId) throws ImsException {
            int mimeType = -1;
            Cursor c = null;
            enforceConfigId(configId);
            try {
                c = getConfigFirstCursor(table, configId);
                int dataIndex = c.getColumnIndex(ImsConfigContract.BasicConfigTable.DATA);
                mimeType = c.getInt(c.getColumnIndex(ImsConfigContract.BasicConfigTable.MIMETYPE_ID));
                enforceDefaultValue(configId, c.getString(dataIndex));
                if (mimeType == 0) {
                    int result = Integer.parseInt(c.getString(dataIndex));
                    if (c != null) {
                        c.close();
                    }
                    return result;
                }
                throw new ImsException("Config " + configId + " shall be type " + 0 + ", but " + mimeType, 101);
            } catch (Exception e) {
                throw new ImsException("Config " + configId + " shall be type " + 0 + ", but " + mimeType + " or something wrong with cursor", 101);
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
                throw th;
            }
        }

        /* Debug info: failed to restart local var, previous not found, register: 11 */
        /* access modifiers changed from: private */
        public String getConfigStringValue(String table, int configId) throws ImsException {
            int mimeType = -1;
            Cursor c = null;
            enforceConfigId(configId);
            try {
                c = getConfigFirstCursor(table, configId);
                int dataIndex = c.getColumnIndex(ImsConfigContract.BasicConfigTable.DATA);
                mimeType = c.getInt(c.getColumnIndex(ImsConfigContract.BasicConfigTable.MIMETYPE_ID));
                enforceDefaultValue(configId, c.getString(dataIndex));
                if (mimeType == 1) {
                    String result = c.getString(dataIndex);
                    if (c != null) {
                        c.close();
                    }
                    return result;
                }
                throw new ImsException("Config " + configId + " shall be type " + 1 + ", but " + mimeType, 101);
            } catch (Exception e) {
                throw new ImsException("Config " + configId + " shall be type " + 1 + ", but " + mimeType + " or something wrong with cursor", 101);
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
                throw th;
            }
        }

        private void enforceDefaultValue(int configId, String data) throws ImsException {
            if (ImsConfigContract.VALUE_NO_DEFAULT.equals(data)) {
                throw new ImsException("No deafult value for config " + configId, 0);
            }
        }

        private void enforceConfigId(int configId) throws ImsException {
            if (!ImsConfigContract.Validator.isValidConfigId(configId)) {
                throw new ImsException("No deafult value for config " + configId, 101);
            }
        }
    }

    private static class ResourceHelper {
        private ContentResolver mContentResolver = null;
        private Context mContext = null;
        private int mPhoneId;

        ResourceHelper(Context context, int phoneId) {
            this.mPhoneId = phoneId;
            this.mContext = context;
            this.mContentResolver = context.getContentResolver();
        }

        private void clear() {
            try {
                this.mContentResolver.delete(ImsConfigContract.Resource.CONTENT_URI, "phone_id = ?", new String[]{String.valueOf(this.mPhoneId)});
            } catch (IllegalArgumentException e) {
                Log.e(ImsConfigStorage.TAG, "ResourceHelper.clear IllegalArgumentException");
            } catch (SecurityException e2) {
                Log.e(ImsConfigStorage.TAG, "ResourceHelper.clear SecurityException");
            } catch (Exception e3) {
                Log.e(ImsConfigStorage.TAG, "ResourceHelper.clear Exception");
            }
        }

        /* access modifiers changed from: private */
        public void updateResource(int featureId, int value) {
            ContentValues cv = new ContentValues();
            cv.put("phone_id", Integer.valueOf(this.mPhoneId));
            cv.put("feature_id", Integer.valueOf(featureId));
            cv.put("value", Integer.valueOf(value));
            try {
                int curValue = getResourceValue(featureId);
                Log.d(ImsConfigStorage.TAG, "updateResource() comparing: curValue: " + curValue + ", value:" + value);
                this.mContentResolver.update(ImsConfigContract.Resource.CONTENT_URI, cv, "phone_id=? AND feature_id=?", new String[]{String.valueOf(this.mPhoneId), String.valueOf(featureId)});
            } catch (ImsException e) {
                Log.e(ImsConfigStorage.TAG, "updateResource() ImsException featureId:" + featureId + ", value:" + value);
                try {
                    this.mContentResolver.insert(ImsConfigContract.Resource.CONTENT_URI, cv);
                } catch (IllegalArgumentException e2) {
                    Log.e(ImsConfigStorage.TAG, "updateResource insert IllegalArgumentException");
                } catch (SecurityException e3) {
                    Log.e(ImsConfigStorage.TAG, "updateResource insert SecurityException");
                } catch (Exception e4) {
                    Log.e(ImsConfigStorage.TAG, "updateResource insert Exception");
                }
            } catch (IllegalArgumentException e5) {
                Log.e(ImsConfigStorage.TAG, "updateResource update IllegalArgumentException");
            } catch (SecurityException e6) {
                Log.e(ImsConfigStorage.TAG, "updateResource update SecurityException");
            } catch (Exception e7) {
                Log.e(ImsConfigStorage.TAG, "updateResource update Exception");
            }
        }

        /* Debug info: failed to restart local var, previous not found, register: 13 */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0089, code lost:
            if (r2 == null) goto L_0x008c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:7:0x003b, code lost:
            if (r2 != null) goto L_0x003d;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int getResourceValue(int r14) throws com.android.ims.ImsException {
            /*
                r13 = this;
                java.lang.String r0 = "Feature "
                java.lang.String r1 = "ImsConfigStorage"
                r2 = 0
                r3 = -1
                java.lang.String r4 = "phone_id"
                java.lang.String r5 = "feature_id"
                java.lang.String r6 = "value"
                java.lang.String[] r9 = new java.lang.String[]{r4, r5, r6}
                r4 = 101(0x65, float:1.42E-43)
                android.content.ContentResolver r7 = r13.mContentResolver     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                int r5 = r13.mPhoneId     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                android.net.Uri r8 = com.mediatek.ims.config.ImsConfigContract.Resource.getUriWithFeatureId(r5, r14)     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                r10 = 0
                r11 = 0
                r12 = 0
                android.database.Cursor r5 = r7.query(r8, r9, r10, r11, r12)     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                r2 = r5
                if (r2 == 0) goto L_0x0041
                int r5 = r2.getCount()     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                r7 = 1
                if (r5 != r7) goto L_0x0041
                r2.moveToFirst()     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                int r5 = r2.getColumnIndex(r6)     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                int r6 = r2.getInt(r5)     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                r3 = r6
                r2.close()     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                if (r2 == 0) goto L_0x008c
            L_0x003d:
                r2.close()
                goto L_0x008c
            L_0x0041:
                com.android.ims.ImsException r5 = new com.android.ims.ImsException     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                r6.<init>()     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                r6.append(r0)     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                r6.append(r14)     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                java.lang.String r7 = " not assigned with res value!"
                r6.append(r7)     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                java.lang.String r6 = r6.toString()     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                r5.<init>(r6, r4)     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
                throw r5     // Catch:{ IllegalArgumentException -> 0x0082, SecurityException -> 0x0078, Exception -> 0x005d }
            L_0x005b:
                r0 = move-exception
                goto L_0x008d
            L_0x005d:
                r1 = move-exception
                com.android.ims.ImsException r5 = new com.android.ims.ImsException     // Catch:{ all -> 0x005b }
                java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x005b }
                r6.<init>()     // Catch:{ all -> 0x005b }
                r6.append(r0)     // Catch:{ all -> 0x005b }
                r6.append(r14)     // Catch:{ all -> 0x005b }
                java.lang.String r0 = " not assigned with res value or something wrong with cursor"
                r6.append(r0)     // Catch:{ all -> 0x005b }
                java.lang.String r0 = r6.toString()     // Catch:{ all -> 0x005b }
                r5.<init>(r0, r4)     // Catch:{ all -> 0x005b }
                throw r5     // Catch:{ all -> 0x005b }
            L_0x0078:
                r0 = move-exception
                java.lang.String r4 = "getResourceValue SecurityException"
                android.util.Log.e(r1, r4)     // Catch:{ all -> 0x005b }
                if (r2 == 0) goto L_0x008c
                goto L_0x003d
            L_0x0082:
                r0 = move-exception
                java.lang.String r4 = "getResourceValue IllegalArgumentException"
                android.util.Log.e(r1, r4)     // Catch:{ all -> 0x005b }
                if (r2 == 0) goto L_0x008c
                goto L_0x003d
            L_0x008c:
                return r3
            L_0x008d:
                if (r2 == 0) goto L_0x0092
                r2.close()
            L_0x0092:
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.config.internal.ImsConfigStorage.ResourceHelper.getResourceValue(int):int");
        }
    }
}
