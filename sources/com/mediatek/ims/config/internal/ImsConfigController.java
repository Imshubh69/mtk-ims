package com.mediatek.ims.config.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.ims.ImsException;
import com.mediatek.ims.ImsCommonUtil;
import com.mediatek.ims.config.ImsConfigContract;
import com.mediatek.ims.plugin.ExtensionFactory;
import com.mediatek.ims.plugin.ImsManagerOemPlugin;
import com.mediatek.ims.ril.ImsCommandsInterface;
import java.util.HashMap;

public class ImsConfigController {
    private static final String ACTION_CXP_NOTIFY_FEATURE = "com.mediatek.common.carrierexpress.cxp_notify_feature";
    static final int CONFIG_CMD_ERROR = 2;
    static final int CONFIG_CMD_SUCCESS = 1;
    static final int CONFIG_INTERRUPT_ERROR = 4;
    static final int CONFIG_TIMEOUT_ERROR = 3;
    /* access modifiers changed from: private */
    public static final boolean DEBUG;
    static final int EVENT_IMS_CFG_CONFIG_CHANGED = 1003;
    static final int EVENT_IMS_CFG_CONFIG_LOADED = 1004;
    static final int EVENT_IMS_CFG_DYNAMIC_IMS_SWITCH_COMPLETE = 1001;
    static final int EVENT_IMS_CFG_FEATURE_CHANGED = 1002;
    static final int MSG_IMS_GET_FEATURE_DONE = 103;
    static final int MSG_IMS_GET_PROVISION_DONE = 101;
    static final int MSG_IMS_GET_RESOURCE_DONE = 106;
    static final int MSG_IMS_SET_FEATURE_DONE = 104;
    static final int MSG_IMS_SET_MDCFG_DONE = 107;
    static final int MSG_IMS_SET_PROVISION_DONE = 102;
    static final int MSG_RESET_WFC_MODE_FLAG = 108;
    static final int MSG_UPDATE_IMS_SERVICE_CONFIG = 109;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final String TAG = "ImsConfigController";
    static final int TIMER_IMS_CFG_TIMEOUT = 2000;
    /* access modifiers changed from: private */
    public Context mContext = null;
    private int mCurWfcMode = -1;
    private Handler mEventHandler;
    private Object mFeatureValueLock = new Object();
    private Handler mHandler;
    private HashMap<Integer, Integer> mImsCapabilities = new HashMap<>();
    private HashMap<Integer, Boolean> mImsCapabilitiesIsCache = new HashMap<>();
    /* access modifiers changed from: private */
    public ImsManagerOemPlugin mImsManagerOemPlugin = null;
    /* access modifiers changed from: private */
    public String mLogTag;
    private Object mMdCfgLock = new Object();
    private int mPhoneId = -1;
    private Object mProvisionedValueLock = new Object();
    private BroadcastReceiver mReceiver;
    private Object mResourceValueLock = new Object();
    private ImsCommandsInterface mRilAdapter;
    private Object mWfcLock = new Object();

    static {
        boolean z = false;
        if (TextUtils.equals(Build.TYPE, "eng") || SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        DEBUG = z;
    }

    private class ProvisioningResult {
        Object lockObj;
        String provisionInfo;
        int provisionResult;

        private ProvisioningResult() {
            this.provisionResult = 3;
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
            this.configResult = 3;
            this.lockObj = new Object();
        }
    }

    private class FeatureResult {
        int featureResult;
        int featureValue;
        Object lockObj;

        private FeatureResult() {
            this.lockObj = new Object();
        }
    }

    private ImsConfigController() {
    }

    public ImsConfigController(Context context, int phoneId, ImsCommandsInterface imsRilAdapter) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mRilAdapter = imsRilAdapter;
        this.mLogTag = "ImsConfigController[" + phoneId + "]";
        StringBuilder sb = new StringBuilder();
        sb.append("ImsConfigThread-");
        sb.append(this.mPhoneId);
        HandlerThread configThread = new HandlerThread(sb.toString());
        configThread.start();
        HandlerThread eventThread = new HandlerThread("ImsEventThread-" + this.mPhoneId);
        eventThread.start();
        this.mHandler = new EventHandler(this.mPhoneId, configThread.getLooper());
        this.mEventHandler = new EventHandler(this.mPhoneId, eventThread.getLooper());
        this.mReceiver = new ImsConfigEventReceiver(this.mEventHandler, this.mPhoneId, this.mRilAdapter);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        filter.addAction(ACTION_CXP_NOTIFY_FEATURE);
        filter.addAction("com.mediatek.ims.MTK_MMTEL_READY");
        if (ImsCommonUtil.isDssNoResetSupport()) {
            filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        }
        this.mContext.registerReceiver(this.mReceiver, filter);
        this.mRilAdapter.registerForImsCfgDynamicImsSwitchComplete(this.mHandler, 1001, (Object) null);
        this.mRilAdapter.registerForImsCfgConfigChanged(this.mHandler, 1003, (Object) null);
        this.mRilAdapter.registerForImsCfgFeatureChanged(this.mHandler, 1002, (Object) null);
        this.mRilAdapter.registerForImsCfgConfigLoaded(this.mHandler, 1004, (Object) null);
        initImsCapabilities();
    }

    private String messageToString(int msg) {
        switch (msg) {
            case 101:
                return "MSG_IMS_GET_PROVISION_DONE";
            case 102:
                return "MSG_IMS_SET_PROVISION_DONE";
            case 103:
                return "MSG_IMS_GET_FEATURE_DONE";
            case 104:
                return "MSG_IMS_SET_FEATURE_DONE";
            case 106:
                return "MSG_IMS_GET_RESOURCE_DONE";
            case MSG_IMS_SET_MDCFG_DONE /*107*/:
                return "MSG_IMS_SET_MDCFG_DONE";
            case MSG_RESET_WFC_MODE_FLAG /*108*/:
                return "MSG_RESET_WFC_MODE_FLAG";
            case MSG_UPDATE_IMS_SERVICE_CONFIG /*109*/:
                return "MSG_UPDATE_IMS_SERVICE_CONFIG";
            case 1001:
                return "EVENT_IMS_CFG_DYNAMIC_IMS_SWITCH_COMPLETE";
            case 1002:
                return "EVENT_IMS_CFG_FEATURE_CHANGED";
            case 1003:
                return "EVENT_IMS_CFG_CONFIG_CHANGED";
            case 1004:
                return "EVENT_IMS_CFG_CONFIG_LOADED";
            default:
                return "" + msg;
        }
    }

    class EventHandler extends Handler {
        private int mPhoneId;

        EventHandler(int phoneId, Looper looper) {
            super(looper);
            this.mPhoneId = phoneId;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    ProvisioningResult result = (ProvisioningResult) ar.userObj;
                    synchronized (result.lockObj) {
                        if (ar.exception != null) {
                            result.provisionResult = 2;
                            String access$000 = ImsConfigController.this.mLogTag;
                            Rlog.e(access$000, "MSG_IMS_GET_PROVISION_DONE: error ret null, e=" + ar.exception);
                        } else {
                            String str = (String) ar.result;
                            result.provisionInfo = str;
                            ar.result = str;
                            result.provisionResult = 1;
                            if (ImsConfigController.DEBUG) {
                                String access$0002 = ImsConfigController.this.mLogTag;
                                Rlog.d(access$0002, "MSG_IMS_GET_PROVISION_DONE: provisionInfo:" + result.provisionInfo);
                            }
                        }
                        result.lockObj.notify();
                    }
                    return;
                case 102:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    ProvisioningResult result2 = (ProvisioningResult) ar2.userObj;
                    synchronized (result2.lockObj) {
                        if (ar2.exception != null) {
                            result2.provisionResult = 2;
                            String access$0003 = ImsConfigController.this.mLogTag;
                            Rlog.e(access$0003, "MSG_IMS_SET_PROVISION_DONE: error ret null, e=" + ar2.exception);
                        } else {
                            result2.provisionResult = 1;
                            if (ImsConfigController.DEBUG) {
                                Rlog.d(ImsConfigController.this.mLogTag, "MSG_IMS_SET_PROVISION_DONE: Finish set provision!");
                            }
                        }
                        result2.lockObj.notify();
                    }
                    return;
                case 103:
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    FeatureResult result3 = (FeatureResult) ar3.userObj;
                    synchronized (result3.lockObj) {
                        if (ar3.exception != null) {
                            result3.featureResult = 2;
                            String access$0004 = ImsConfigController.this.mLogTag;
                            Rlog.e(access$0004, "MSG_IMS_GET_FEATURE_DONE: error ret null, e=" + ar3.exception);
                        } else {
                            result3.featureValue = ((int[]) ar3.result)[0];
                            result3.featureResult = 1;
                            if (ImsConfigController.DEBUG) {
                                String access$0005 = ImsConfigController.this.mLogTag;
                                Rlog.d(access$0005, "MSG_IMS_GET_FEATURE_DONE: featureValue:" + result3.featureValue);
                            }
                        }
                        result3.lockObj.notify();
                    }
                    return;
                case 104:
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    FeatureResult result4 = (FeatureResult) ar4.userObj;
                    synchronized (result4.lockObj) {
                        if (ar4.exception != null) {
                            result4.featureResult = 2;
                            String access$0006 = ImsConfigController.this.mLogTag;
                            Rlog.e(access$0006, "MSG_IMS_SET_FEATURE_DONE: error ret null, e=" + ar4.exception);
                        } else {
                            result4.featureResult = 1;
                            if (ImsConfigController.DEBUG) {
                                Rlog.d(ImsConfigController.this.mLogTag, "MSG_IMS_SET_FEATURE_DONE: Finish set feature!");
                            }
                        }
                        result4.lockObj.notify();
                    }
                    return;
                case 106:
                    AsyncResult ar5 = (AsyncResult) msg.obj;
                    FeatureResult result5 = (FeatureResult) ar5.userObj;
                    synchronized (result5.lockObj) {
                        if (ar5.exception == null) {
                            if (ar5.result != null) {
                                result5.featureValue = ((int[]) ar5.result)[0];
                                result5.featureResult = 1;
                                result5.lockObj.notify();
                            }
                        }
                        result5.featureResult = 2;
                        String access$0007 = ImsConfigController.this.mLogTag;
                        Rlog.e(access$0007, "MSG_IMS_GET_RESOURCE_DONE: error ret null, e=" + ar5.exception + ", result:" + ar5.result);
                        result5.lockObj.notify();
                    }
                    return;
                case ImsConfigController.MSG_IMS_SET_MDCFG_DONE /*107*/:
                    AsyncResult ar6 = (AsyncResult) msg.obj;
                    MdConfigResult cfgResult = (MdConfigResult) ar6.userObj;
                    synchronized (cfgResult.lockObj) {
                        if (ar6.exception != null) {
                            int[] errorResult = new int[cfgResult.requestConfigNum];
                            for (int i = 0; i < errorResult.length; i++) {
                                errorResult[i] = -1;
                            }
                            cfgResult.resultArray = errorResult;
                            cfgResult.configResult = 2;
                            String access$0008 = ImsConfigController.this.mLogTag;
                            Rlog.e(access$0008, "SET_MDCFG_DONE, error ret, e=" + ar6.exception);
                        } else {
                            String[] resultStrArray = ((String) ar6.result).split(",");
                            int[] resultIntArray = new int[resultStrArray.length];
                            for (int i2 = 0; i2 < resultStrArray.length; i2++) {
                                resultIntArray[i2] = Integer.parseInt(resultStrArray[i2]);
                            }
                            cfgResult.resultArray = resultIntArray;
                            cfgResult.configResult = 1;
                            if (ImsConfigController.DEBUG) {
                                Rlog.d(ImsConfigController.this.mLogTag, "SET_MDCFG_DONE, finish set MD Ims config!");
                            }
                        }
                        cfgResult.lockObj.notify();
                    }
                    return;
                case ImsConfigController.MSG_RESET_WFC_MODE_FLAG /*108*/:
                    ImsConfigController.this.resetWfcModeFlag();
                    return;
                case ImsConfigController.MSG_UPDATE_IMS_SERVICE_CONFIG /*109*/:
                    if (ImsConfigController.this.mImsManagerOemPlugin == null) {
                        ImsConfigController imsConfigController = ImsConfigController.this;
                        ImsManagerOemPlugin unused = imsConfigController.mImsManagerOemPlugin = ExtensionFactory.makeOemPluginFactory(imsConfigController.mContext).makeImsManagerPlugin(ImsConfigController.this.mContext);
                    }
                    ImsConfigController.this.mImsManagerOemPlugin.updateImsServiceConfig(ImsConfigController.this.mContext, ImsCommonUtil.getMainCapabilityPhoneId());
                    return;
                case 1001:
                    ImsConfigController.this.initImsCapabilities();
                    Intent intent = new Intent(ImsConfigContract.ACTION_DYNAMIC_IMS_SWITCH_COMPLETE);
                    intent.putExtra("phone", this.mPhoneId);
                    ImsConfigController.this.mContext.sendBroadcast(intent, "android.permission.READ_PHONE_STATE");
                    if (ImsConfigController.DEBUG) {
                        String access$0009 = ImsConfigController.this.mLogTag;
                        Rlog.d(access$0009, "DYNAMIC_IMS_SWITCH_COMPLETE phoneId:" + this.mPhoneId);
                        return;
                    }
                    return;
                case 1002:
                    int[] value = (int[]) ((AsyncResult) msg.obj).result;
                    Intent intent2 = new Intent(ImsConfigContract.ACTION_IMS_FEATURE_CHANGED);
                    intent2.putExtra("phone_id", value[0]);
                    intent2.putExtra(ImsConfigContract.EXTRA_CHANGED_ITEM, value[1]);
                    intent2.putExtra("value", value[2]);
                    ImsConfigController.this.mContext.sendBroadcast(intent2);
                    if (ImsConfigController.DEBUG) {
                        Rlog.d(ImsConfigController.TAG, "EVENT_IMS_CFG_FEATURE_CHANGED: phoneId = " + value[0] + " feature =" + value[1] + " value=" + value[2]);
                        return;
                    }
                    return;
                case 1004:
                    Intent intent3 = new Intent(ImsConfigContract.ACTION_CONFIG_LOADED);
                    intent3.putExtra("phone", this.mPhoneId);
                    ImsConfigController.this.mContext.sendBroadcast(intent3, "android.permission.READ_PHONE_STATE");
                    if (ImsConfigController.DEBUG) {
                        String access$00010 = ImsConfigController.this.mLogTag;
                        Rlog.d(access$00010, "EVENT_IMS_CFG_CONFIG_LOADED phoneId:" + this.mPhoneId);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    public int getFeatureValue(int featureId, int network) throws ImsException {
        int i;
        synchronized (this.mFeatureValueLock) {
            FeatureResult result = new FeatureResult();
            Message msg = this.mHandler.obtainMessage(103, result);
            synchronized (result.lockObj) {
                this.mRilAdapter.getImsCfgFeatureValue(featureId, network, msg);
                try {
                    result.lockObj.wait(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    result.featureResult = 4;
                }
            }
            if (isConfigSuccess(result.featureResult)) {
                i = result.featureValue;
            } else {
                throw new ImsException("Something wrong, reason:" + result.featureResult, 101);
            }
        }
        return i;
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    public void setFeatureValue(int featureId, int network, int value, int isLast) throws ImsException {
        synchronized (this.mFeatureValueLock) {
            FeatureResult result = new FeatureResult();
            Message msg = this.mHandler.obtainMessage(104, result);
            synchronized (result.lockObj) {
                this.mRilAdapter.setImsCfgFeatureValue(featureId, network, value, isLast, msg);
                try {
                    result.lockObj.wait(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    result.featureResult = 4;
                }
                if (!isConfigSuccess(result.featureResult)) {
                    throw new ImsException("Something wrong, reason:" + result.featureResult, 101);
                }
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public String getProvisionedValue(int configId) throws ImsException {
        String str;
        synchronized (this.mProvisionedValueLock) {
            ProvisioningResult result = new ProvisioningResult();
            Message msg = this.mHandler.obtainMessage(101, result);
            synchronized (result.lockObj) {
                this.mRilAdapter.getImsCfgProvisionValue(configId, msg);
                try {
                    result.lockObj.wait(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    result.provisionResult = 4;
                }
            }
            if (isConfigSuccess(result.provisionResult)) {
                str = result.provisionInfo;
            } else {
                throw new ImsException("Something wrong, reason:" + result.provisionResult, 101);
            }
        }
        return str;
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    public void setProvisionedValue(int configId, String value) throws ImsException {
        synchronized (this.mProvisionedValueLock) {
            ProvisioningResult result = new ProvisioningResult();
            Message msg = this.mHandler.obtainMessage(102, result);
            synchronized (result.lockObj) {
                this.mRilAdapter.setImsCfgProvisionValue(configId, value, msg);
                try {
                    result.lockObj.wait(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    result.provisionResult = 4;
                }
            }
            if (!isConfigSuccess(result.provisionResult)) {
                throw new ImsException("Something wrong, reason:" + result.provisionResult, 101);
            }
        }
    }

    public void setProvisionedStringValue(int configId, String value) throws ImsException {
        setProvisionedValue(configId, value);
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00b6, code lost:
        r1 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int getImsResCapability(int r8) throws com.android.ims.ImsException {
        /*
            r7 = this;
            monitor-enter(r7)
            java.lang.Object r0 = r7.mResourceValueLock     // Catch:{ all -> 0x00b8 }
            monitor-enter(r0)     // Catch:{ all -> 0x00b8 }
            java.util.HashMap<java.lang.Integer, java.lang.Boolean> r1 = r7.mImsCapabilitiesIsCache     // Catch:{ all -> 0x00b3 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x00b3 }
            java.lang.Object r1 = r1.get(r2)     // Catch:{ all -> 0x00b3 }
            java.lang.Boolean r1 = (java.lang.Boolean) r1     // Catch:{ all -> 0x00b3 }
            boolean r1 = r1.booleanValue()     // Catch:{ all -> 0x00b3 }
            if (r1 == 0) goto L_0x0045
            boolean r1 = DEBUG     // Catch:{ all -> 0x00b3 }
            if (r1 == 0) goto L_0x0032
            java.lang.String r1 = r7.mLogTag     // Catch:{ all -> 0x00b3 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b3 }
            r2.<init>()     // Catch:{ all -> 0x00b3 }
            java.lang.String r3 = "getImsResCapability from cache, phoneId:"
            r2.append(r3)     // Catch:{ all -> 0x00b3 }
            int r3 = r7.mPhoneId     // Catch:{ all -> 0x00b3 }
            r2.append(r3)     // Catch:{ all -> 0x00b3 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x00b3 }
            android.telephony.Rlog.d(r1, r2)     // Catch:{ all -> 0x00b3 }
        L_0x0032:
            java.util.HashMap<java.lang.Integer, java.lang.Integer> r1 = r7.mImsCapabilities     // Catch:{ all -> 0x00b3 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x00b3 }
            java.lang.Object r1 = r1.get(r2)     // Catch:{ all -> 0x00b3 }
            java.lang.Integer r1 = (java.lang.Integer) r1     // Catch:{ all -> 0x00b3 }
            int r1 = r1.intValue()     // Catch:{ all -> 0x00b3 }
            monitor-exit(r0)     // Catch:{ all -> 0x00b3 }
            monitor-exit(r7)
            return r1
        L_0x0045:
            com.mediatek.ims.config.internal.ImsConfigController$FeatureResult r1 = new com.mediatek.ims.config.internal.ImsConfigController$FeatureResult     // Catch:{ all -> 0x00b3 }
            r2 = 0
            r1.<init>()     // Catch:{ all -> 0x00b3 }
            android.os.Handler r2 = r7.mHandler     // Catch:{ all -> 0x00b3 }
            r3 = 106(0x6a, float:1.49E-43)
            android.os.Message r2 = r2.obtainMessage(r3, r1)     // Catch:{ all -> 0x00b3 }
            java.lang.Object r3 = r1.lockObj     // Catch:{ all -> 0x00b3 }
            monitor-enter(r3)     // Catch:{ all -> 0x00b3 }
            com.mediatek.ims.ril.ImsCommandsInterface r4 = r7.mRilAdapter     // Catch:{ all -> 0x00b0 }
            r4.getImsCfgResourceCapValue(r8, r2)     // Catch:{ all -> 0x00b0 }
            java.lang.Object r4 = r1.lockObj     // Catch:{ InterruptedException -> 0x0063 }
            r5 = 2000(0x7d0, double:9.88E-321)
            r4.wait(r5)     // Catch:{ InterruptedException -> 0x0063 }
            goto L_0x006a
        L_0x0063:
            r4 = move-exception
            r4.printStackTrace()     // Catch:{ all -> 0x00b0 }
            r5 = 4
            r1.featureResult = r5     // Catch:{ all -> 0x00b0 }
        L_0x006a:
            monitor-exit(r3)     // Catch:{ all -> 0x00b0 }
            int r3 = r1.featureResult     // Catch:{ all -> 0x00b3 }
            boolean r3 = isConfigSuccess(r3)     // Catch:{ all -> 0x00b3 }
            if (r3 == 0) goto L_0x0095
            java.util.HashMap<java.lang.Integer, java.lang.Integer> r3 = r7.mImsCapabilities     // Catch:{ all -> 0x00b3 }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x00b3 }
            int r5 = r1.featureValue     // Catch:{ all -> 0x00b3 }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x00b3 }
            r3.put(r4, r5)     // Catch:{ all -> 0x00b3 }
            java.util.HashMap<java.lang.Integer, java.lang.Boolean> r3 = r7.mImsCapabilitiesIsCache     // Catch:{ all -> 0x00b3 }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x00b3 }
            r5 = 1
            java.lang.Boolean r5 = java.lang.Boolean.valueOf(r5)     // Catch:{ all -> 0x00b3 }
            r3.put(r4, r5)     // Catch:{ all -> 0x00b3 }
            int r3 = r1.featureValue     // Catch:{ all -> 0x00b3 }
            monitor-exit(r0)     // Catch:{ all -> 0x00b3 }
            monitor-exit(r7)
            return r3
        L_0x0095:
            com.android.ims.ImsException r3 = new com.android.ims.ImsException     // Catch:{ all -> 0x00b3 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b3 }
            r4.<init>()     // Catch:{ all -> 0x00b3 }
            java.lang.String r5 = "Something wrong, reason:"
            r4.append(r5)     // Catch:{ all -> 0x00b3 }
            int r5 = r1.featureResult     // Catch:{ all -> 0x00b3 }
            r4.append(r5)     // Catch:{ all -> 0x00b3 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x00b3 }
            r5 = 101(0x65, float:1.42E-43)
            r3.<init>(r4, r5)     // Catch:{ all -> 0x00b3 }
            throw r3     // Catch:{ all -> 0x00b3 }
        L_0x00b0:
            r4 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x00b0 }
            throw r4     // Catch:{ all -> 0x00b3 }
        L_0x00b3:
            r1 = move-exception
        L_0x00b4:
            monitor-exit(r0)     // Catch:{ all -> 0x00b6 }
            throw r1     // Catch:{ all -> 0x00b8 }
        L_0x00b6:
            r1 = move-exception
            goto L_0x00b4
        L_0x00b8:
            r8 = move-exception
            monitor-exit(r7)
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.config.internal.ImsConfigController.getImsResCapability(int):int");
    }

    private static boolean isConfigSuccess(int reason) {
        switch (reason) {
            case 1:
                return true;
            default:
                return false;
        }
    }

    public synchronized void setVoltePreference(int mode) {
        String str = this.mLogTag;
        Rlog.i(str, "setVoltePreference mode:" + mode + ", phoneId:" + this.mPhoneId);
        this.mRilAdapter.setVoiceDomainPreference(mode, (Message) null);
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    public int[] setModemImsCfg(String[] keys, String[] values, int type) {
        synchronized (this.mMdCfgLock) {
            if (keys == null) {
                Rlog.d(this.mLogTag, "keys is null, return null");
                return null;
            }
            if (keys.length >= 1) {
                if (values.length >= 1) {
                    if (keys.length == values.length) {
                        Rlog.d(this.mLogTag, "keys and values length equals");
                        String keysStr = ImsConfigUtils.arrayToString(keys);
                        String valuesStr = ImsConfigUtils.arrayToString(values);
                        String str = this.mLogTag;
                        Rlog.d(str, "keysStr:" + keysStr + ", valuesStr:" + valuesStr);
                        MdConfigResult cfgResult = new MdConfigResult();
                        cfgResult.requestConfigNum = keys.length;
                        Message msg = this.mHandler.obtainMessage(MSG_IMS_SET_MDCFG_DONE, cfgResult);
                        synchronized (cfgResult.lockObj) {
                            this.mRilAdapter.setModemImsCfg(keysStr, valuesStr, type, msg);
                            try {
                                cfgResult.lockObj.wait(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                cfgResult.configResult = 4;
                            }
                        }
                        int[] resultArray = cfgResult.resultArray;
                        return resultArray;
                    }
                    Rlog.d(this.mLogTag, "keys and values length not equals");
                    return null;
                }
            }
            Rlog.d(this.mLogTag, "keys or values length is smaller than 1, return null");
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void resetWfcModeFlag() {
        Rlog.d(this.mLogTag, "resetWfcModeFlag()");
        synchronized (this.mWfcLock) {
            this.mCurWfcMode = -1;
        }
    }

    public void sendWfcProfileInfo(int rilWfcMode) {
        synchronized (this.mWfcLock) {
            String str = this.mLogTag;
            Rlog.i(str, "sendWfcProfileInfo rilWfcMode:" + rilWfcMode + ", mCurWfcMode:" + this.mCurWfcMode);
            if (rilWfcMode != this.mCurWfcMode) {
                this.mRilAdapter.sendWfcProfileInfo(rilWfcMode, (Message) null);
                if (rilWfcMode != 3) {
                    if (DEBUG) {
                        Rlog.d(this.mLogTag, "Not wifi-only mode, turn radio ON");
                    }
                    ImsConfigUtils.sendWifiOnlyModeIntent(this.mContext, this.mPhoneId, false);
                } else if (ImsConfigUtils.isWfcEnabledByUser(this.mContext, this.mPhoneId)) {
                    if (DEBUG) {
                        Rlog.d(this.mLogTag, "Wifi-only and WFC setting enabled, send intent to turn radio OFF");
                    }
                    ImsConfigUtils.sendWifiOnlyModeIntent(this.mContext, this.mPhoneId, true);
                } else {
                    if (DEBUG) {
                        Rlog.d(this.mLogTag, "Wifi-only and WFC setting disabled, send intent to turn radio ON");
                    }
                    ImsConfigUtils.sendWifiOnlyModeIntent(this.mContext, this.mPhoneId, false);
                }
                this.mCurWfcMode = rilWfcMode;
            }
        }
    }

    /* access modifiers changed from: private */
    public void initImsCapabilities() {
        this.mImsCapabilitiesIsCache.put(0, false);
        this.mImsCapabilitiesIsCache.put(1, false);
        this.mImsCapabilitiesIsCache.put(2, false);
        this.mImsCapabilitiesIsCache.put(3, false);
        this.mImsCapabilitiesIsCache.put(4, false);
        this.mImsCapabilitiesIsCache.put(5, false);
        this.mImsCapabilitiesIsCache.put(6, false);
        this.mImsCapabilitiesIsCache.put(7, false);
    }
}
