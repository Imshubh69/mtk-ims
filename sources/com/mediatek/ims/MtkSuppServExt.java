package com.mediatek.ims;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.ims.ImsManager;
import com.android.internal.telephony.CommandException;
import com.mediatek.ims.OperatorUtils;
import com.mediatek.ims.plugin.ExtensionFactory;
import com.mediatek.ims.plugin.ImsSSOemPlugin;
import com.mediatek.ims.ril.ImsCommandsInterface;
import java.util.ArrayList;

public class MtkSuppServExt extends Handler {
    private static final boolean DBG = true;
    private static final int EVENT_IMS_REGISTRATION_INFO = 1;
    private static final int EVENT_IMS_UT_EVENT_QUERY_XCAP = 0;
    private static final int EVENT_ON_VOLTE_SUBSCRIPTION = 5;
    private static final int EVENT_RADIO_NOT_AVAILABLE = 2;
    private static final int EVENT_RADIO_OFF = 3;
    private static final int EVENT_RADIO_ON = 4;
    private static final String ICCID_KEY = "iccid_key";
    private static final String LOG_TAG = "SuppServExt";
    private static final boolean SDBG = (SystemProperties.get("ro.build.type").equals("user") ? false : DBG);
    private static final String SETTING_UT_CAPABILITY = "ut_capability";
    private static final String SYS_PROP_QUERY_VOLTE_SUB = "persist.vendor.suppserv.query_volte_sub";
    private static final int TASK_QUERY_XCAP = 0;
    private static final int TASK_RESET_AND_QUERY_XCAP = 1;
    private static final int TASK_SET_UT_CAPABILITY = 3;
    private static final int TASK_SET_VOLTE_SUBSCRIPTION_DIRECLY = 2;
    private static final int UT_CAPABILITY_DISABLE = 2;
    private static final int UT_CAPABILITY_ENABLE = 1;
    private static final int UT_CAPABILITY_UNKNOWN = 0;
    /* access modifiers changed from: private */
    public static final boolean VDBG = SystemProperties.get("ro.build.type").equals("eng");
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED")) {
                MtkSuppServExt.this.post(new Runnable() {
                    public void run() {
                        MtkSuppServExt.this.handleSubinfoUpdate();
                    }
                });
            } else if (action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE")) {
                boolean unused = MtkSuppServExt.this.mQueryXcapDone = false;
                MtkSuppServExt.this.mSuppServTaskDriven.appendTask(new Task(0, false, "Radio capability done"));
            } else if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                boolean bAirplaneModeOn = intent.getBooleanExtra("state", false);
                MtkSuppServExt mtkSuppServExt = MtkSuppServExt.this;
                mtkSuppServExt.logd("ACTION_AIRPLANE_MODE_CHANGED, bAirplaneModeOn = " + bAirplaneModeOn);
                if (bAirplaneModeOn) {
                    boolean unused2 = MtkSuppServExt.this.mQueryXcapDone = false;
                }
            } else if (action.equals("android.telephony.action.SIM_APPLICATION_STATE_CHANGED")) {
                int simStatus = intent.getIntExtra("android.telephony.extra.SIM_STATE", 0);
                int subId = intent.getIntExtra("subscription", -1);
                MtkSuppServExt mtkSuppServExt2 = MtkSuppServExt.this;
                mtkSuppServExt2.logd("ACTION_SIM_APPLICATION_STATE_CHANGED: " + simStatus + ", subId: " + subId);
                if (subId == MtkSuppServExt.getSubIdUsingPhoneId(MtkSuppServExt.this.mPhoneId)) {
                    if (10 != simStatus) {
                        MtkSuppServExt.this.setSimLoaded(false);
                        return;
                    }
                    MtkSuppServExt.this.setSimLoaded(MtkSuppServExt.DBG);
                    MtkSuppServExt.this.mSuppServTaskDriven.appendTask(new Task(0, false, "SIM loaded."));
                    if (MtkSuppServExt.this.isOp(OperatorUtils.OPID.OP09) && SystemProperties.getInt("persist.vendor.mtk_ct_volte_support", 0) != 0) {
                        MtkSuppServExt.this.mImsService.notifyUtCapabilityChange(MtkSuppServExt.this.mPhoneId);
                    }
                }
            } else if (action.equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                int subId2 = intent.getIntExtra("subscription", -1);
                if (subId2 == -1) {
                    MtkSuppServExt mtkSuppServExt3 = MtkSuppServExt.this;
                    mtkSuppServExt3.logd("ACTION_CARRIER_CONFIG_CHANGED: not loaded, subId: " + subId2);
                } else if (subId2 != MtkSuppServExt.getSubIdUsingPhoneId(MtkSuppServExt.this.mPhoneId)) {
                    MtkSuppServExt mtkSuppServExt4 = MtkSuppServExt.this;
                    mtkSuppServExt4.logd("ACTION_CARRIER_CONFIG_CHANGED: not for this phone, subId: " + subId2);
                } else {
                    MtkSuppServExt mtkSuppServExt5 = MtkSuppServExt.this;
                    mtkSuppServExt5.logd("ACTION_CARRIER_CONFIG_CHANGED: loaded, subId: " + subId2);
                    MtkSuppServExt.this.mSuppServTaskDriven.appendTask(new Task(0, false, "Carrier config changed"));
                }
            }
        }
    };
    private Context mContext;
    private ImsManager mImsManager = null;
    private ImsCommandsInterface mImsRILAdapter;
    /* access modifiers changed from: private */
    public ImsService mImsService;
    private String mOldIccId = "";
    /* access modifiers changed from: private */
    public int mPhoneId = 1;
    private PhoneStateListener mPhoneStateListener;
    private ImsSSOemPlugin mPluginBase;
    /* access modifiers changed from: private */
    public boolean mQueryXcapDone = false;
    private int mRadioState = ImsCommandsInterface.RadioState.RADIO_UNAVAILABLE.ordinal();
    private boolean mSimIsChangedAfterBoot = false;
    private boolean mSimLoaded = false;
    private MtkSuppServExt mSuppServExt = null;
    /* access modifiers changed from: private */
    public SuppServTaskDriven mSuppServTaskDriven = null;

    private class Task {
        private boolean mExtraBool = false;
        private int mExtraInt = -1;
        private String mExtraMsg = "";
        private int mTaskId = -1;

        public Task(int taskId, boolean b, String extraMsg) {
            this.mTaskId = taskId;
            this.mExtraBool = b;
            this.mExtraMsg = extraMsg;
        }

        public Task(int taskId, String extraMsg) {
            this.mTaskId = taskId;
            this.mExtraMsg = extraMsg;
        }

        public Task(int taskId, int extraInt, String extraMsg) {
            this.mTaskId = taskId;
            this.mExtraInt = extraInt;
            this.mExtraMsg = extraMsg;
        }

        public int getTaskId() {
            return this.mTaskId;
        }

        public int getExtraInt() {
            return this.mExtraInt;
        }

        public boolean getExtraBoolean() {
            return this.mExtraBool;
        }

        public String getExtraMsg() {
            return this.mExtraMsg;
        }

        public String toString() {
            return "Task ID: " + this.mTaskId + ", ExtraBool: " + this.mExtraBool + ", ExtraInt: " + this.mExtraInt + ", ExtraMsg: " + this.mExtraMsg;
        }
    }

    private class SuppServTaskDriven extends Handler {
        private static final int EVENT_DONE = 0;
        private static final int EVENT_EXEC_NEXT = 1;
        private static final int STATE_DOING = 1;
        private static final int STATE_DONE = 2;
        private static final int STATE_NO_PENDING = 0;
        private ArrayList<Task> mPendingTask = new ArrayList<>();
        private int mState = 0;
        private Object mStateLock = new Object();
        private Object mTaskLock = new Object();

        public SuppServTaskDriven() {
        }

        public SuppServTaskDriven(Looper looper) {
            super(looper);
        }

        public void appendTask(Task task) {
            synchronized (this.mTaskLock) {
                this.mPendingTask.add(task);
            }
            obtainMessage(1).sendToTarget();
        }

        private int getState() {
            int i;
            synchronized (this.mStateLock) {
                i = this.mState;
            }
            return i;
        }

        private void setState(int state) {
            synchronized (this.mStateLock) {
                this.mState = state;
            }
        }

        private Task getCurrentPendingTask() {
            synchronized (this.mTaskLock) {
                if (this.mPendingTask.size() == 0) {
                    return null;
                }
                Task task = this.mPendingTask.get(0);
                return task;
            }
        }

        private void removePendingTask(int index) {
            synchronized (this.mTaskLock) {
                if (this.mPendingTask.size() > 0) {
                    this.mPendingTask.remove(index);
                }
            }
        }

        public void clearPendingTask() {
            synchronized (this.mTaskLock) {
                this.mPendingTask.clear();
            }
        }

        public void exec() {
            Task task = getCurrentPendingTask();
            if (task == null) {
                setState(0);
            } else if (getState() != 1) {
                setState(1);
                int taskId = task.getTaskId();
                if (MtkSuppServExt.VDBG) {
                    MtkSuppServExt.this.logd(task.toString());
                }
                switch (taskId) {
                    case 0:
                        break;
                    case 1:
                        boolean unused = MtkSuppServExt.this.mQueryXcapDone = false;
                        break;
                    case 2:
                        int currentVolteStatus = MtkSuppServExt.this.getVolteSubscriptionFromSettings();
                        int newVolteStatus = task.getExtraInt();
                        MtkSuppServExt mtkSuppServExt = MtkSuppServExt.this;
                        mtkSuppServExt.logd("TASK_SET_VOLTE_SUBSCRIPTION_DIRECLY, currentVolteStatus: " + currentVolteStatus + " newVolteStatus: " + newVolteStatus);
                        if (currentVolteStatus != newVolteStatus) {
                            MtkSuppServExt.this.setVolteSubscriptionToSettings(newVolteStatus);
                        }
                        boolean unused2 = MtkSuppServExt.this.mQueryXcapDone = MtkSuppServExt.DBG;
                        MtkSuppServExt.this.taskDone();
                        return;
                    case 3:
                        int currentUtStatus = MtkSuppServExt.this.getUtCapabilityFromSettings();
                        int newUtStatus = task.getExtraInt();
                        MtkSuppServExt mtkSuppServExt2 = MtkSuppServExt.this;
                        mtkSuppServExt2.logd("TASK_SET_UT_CAPABILITY, currentUtStatus: " + currentUtStatus + " newUtStatus: " + newUtStatus);
                        if (currentUtStatus != newUtStatus) {
                            MtkSuppServExt.this.setUtCapabilityToSettings(newUtStatus);
                        }
                        boolean unused3 = MtkSuppServExt.this.mQueryXcapDone = MtkSuppServExt.DBG;
                        MtkSuppServExt.this.taskDone();
                        return;
                    default:
                        MtkSuppServExt.this.taskDone();
                        return;
                }
                MtkSuppServExt.this.startHandleXcapQueryProcess(task.getExtraBoolean(), task.getExtraMsg());
            }
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    removePendingTask(0);
                    setState(2);
                    break;
                case 1:
                    break;
                default:
                    return;
            }
            exec();
        }

        private String stateToString(int state) {
            switch (state) {
                case 0:
                    return "STATE_NO_PENDING";
                case 1:
                    return "STATE_DOING";
                case 2:
                    return "STATE_DONE";
                default:
                    return "UNKNOWN_STATE";
            }
        }

        private String eventToString(int event) {
            switch (event) {
                case 0:
                    return "EVENT_DONE";
                case 1:
                    return "EVENT_EXEC_NEXT";
                default:
                    return "UNKNOWN_EVENT";
            }
        }
    }

    private boolean checkNeedQueryXcap() {
        if ("0".equals(SystemProperties.get(SYS_PROP_QUERY_VOLTE_SUB, "0"))) {
            return false;
        }
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        int subId = getSubIdUsingPhoneId(this.mPhoneId);
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        if (b != null) {
            logd("checkNeedQueryXcap: carrier config is ready, config = " + b.getBoolean(this.mPluginBase.getXcapQueryCarrierConfigKey(), false));
            return b.getBoolean(this.mPluginBase.getXcapQueryCarrierConfigKey(), false);
        }
        logd("checkNeedQueryXcap: carrier config not ready, return false");
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isOp(OperatorUtils.OPID id) {
        return OperatorUtils.isOperator(OperatorUtils.getSimOperatorNumericForPhone(this.mPhoneId), id);
    }

    public MtkSuppServExt(Context context, int phoneId, ImsService imsService, Looper looper) {
        super(looper);
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mImsService = imsService;
        this.mImsRILAdapter = imsService.getImsRILAdapter(phoneId);
        this.mSuppServTaskDriven = new SuppServTaskDriven(looper);
        this.mImsManager = ImsManager.getInstance(context, phoneId);
        this.mPluginBase = ExtensionFactory.makeOemPluginFactory(this.mContext).makeImsSSOemPlugin(this.mContext);
        checkImsInService();
        registerBroadcastReceiver();
        registerEvent();
        logd("MtkSuppServExt init done.");
    }

    private void checkImsInService() {
        if (this.mImsService.getImsServiceState(this.mPhoneId) == 0) {
            this.mQueryXcapDone = DBG;
            setVolteSubscriptionToSettings(this.mPluginBase.getVolteSubEnableConstant());
        }
    }

    private void initPhoneStateListener(Looper looper) {
        C01191 r1 = new PhoneStateListener(looper) {
            public void onServiceStateChanged(ServiceState serviceState) {
                switch (serviceState.getDataRegState()) {
                    case 0:
                        MtkSuppServExt.this.mSuppServTaskDriven.appendTask(new Task(0, false, "Data reg state in service."));
                        return;
                    default:
                        return;
                }
            }
        };
        this.mPhoneStateListener = r1;
        ((TelephonyManager) this.mContext.getSystemService("phone")).listen(r1, 1);
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        filter.addAction("android.telephony.action.SIM_APPLICATION_STATE_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    private void registerEvent() {
        this.mImsRILAdapter.registerForImsRegistrationInfo(this, 1, (Object) null);
        this.mImsRILAdapter.registerForNotAvailable(this, 2, (Object) null);
        this.mImsRILAdapter.registerForOff(this, 3, (Object) null);
        this.mImsRILAdapter.registerForOn(this, 4, (Object) null);
        this.mImsRILAdapter.registerForVolteSubscription(this, 5, (Object) null);
    }

    private void unRegisterEvent() {
        this.mImsRILAdapter.unregisterForImsRegistrationInfo(this);
        this.mImsRILAdapter.unregisterForNotAvailable(this);
        this.mImsRILAdapter.unregisterForOff(this);
        this.mImsRILAdapter.unregisterForOn(this);
        this.mImsRILAdapter.unregisterForVolteSubscription(this);
    }

    private void unRegisterBroadReceiver() {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
    }

    public void dispose() {
        unRegisterBroadReceiver();
    }

    private boolean checkInitCriteria(StringBuilder criteriaFailReason) {
        if (!checkNeedQueryXcap()) {
            criteriaFailReason.append("No need to support for this operator OR carrier config not ready, ");
            return false;
        } else if (!isDataEnabled()) {
            criteriaFailReason.append("Data is not enabled, ");
            return false;
        } else if (!isDataEnabled()) {
            criteriaFailReason.append("Data is not enabled, ");
            return false;
        } else if (!isSubInfoReady()) {
            criteriaFailReason.append("SubInfo not ready, ");
            return false;
        } else if (!getSimLoaded()) {
            criteriaFailReason.append("Sim not loaded, ");
            return false;
        } else if (!isDataRegStateInService()) {
            criteriaFailReason.append("Data reg state is not in service, ");
            return false;
        } else if (this.mQueryXcapDone) {
            criteriaFailReason.append("No need query, ");
            return false;
        } else if (!this.mSimIsChangedAfterBoot) {
            criteriaFailReason.append("Sim not changed, ");
            return false;
        } else if (this.mRadioState != ImsCommandsInterface.RadioState.RADIO_ON.ordinal()) {
            criteriaFailReason.append("radio not available, ");
            return false;
        } else {
            criteriaFailReason.append("All Criteria ready.");
            return DBG;
        }
    }

    /* access modifiers changed from: private */
    public void startHandleXcapQueryProcess(boolean forceQuery, String reason) {
        StringBuilder criteriaFailReason = new StringBuilder();
        boolean checkCriteria = checkInitCriteria(criteriaFailReason);
        logd("startHandleXcapQueryProcess(), forceQuery: " + forceQuery + ", reason: " + reason + ", checkCriteria: " + checkCriteria + ", criteriaFailReason: " + criteriaFailReason.toString());
        if (!checkCriteria) {
            taskDone();
            return;
        }
        this.mSimIsChangedAfterBoot = false;
        startXcapQuery();
    }

    /* access modifiers changed from: private */
    public void taskDone() {
        this.mSuppServTaskDriven.obtainMessage(0).sendToTarget();
    }

    private boolean isSubInfoReady() {
        if (!TextUtils.isEmpty(((TelephonyManager) this.mContext.getSystemService("phone")).getSimSerialNumber(getSubIdUsingPhoneId(this.mPhoneId)))) {
            return DBG;
        }
        return false;
    }

    private boolean isDataEnabled() {
        return ((TelephonyManager) this.mContext.getSystemService("phone")).getDataEnabled(getSubIdUsingPhoneId(this.mPhoneId));
    }

    private boolean isDataRegStateInService() {
        ServiceState state = ((TelephonyManager) this.mContext.getSystemService("phone")).getServiceStateForSubscriber(getSubIdUsingPhoneId(this.mPhoneId));
        if (state == null) {
            logi("isDataRegStateInService, state is null ");
            return false;
        } else if (state.getDataRegState() == 0) {
            return DBG;
        } else {
            return false;
        }
    }

    private void startXcapQuery() {
        if (ImsCommonUtil.supportMdAutoSetupIms()) {
            this.mImsRILAdapter.getXcapStatus(obtainMessage(0, (Object) null));
            return;
        }
        taskDone();
    }

    private boolean getSimLoaded() {
        logi("mSimLoaded: " + this.mSimLoaded);
        return this.mSimLoaded;
    }

    /* access modifiers changed from: private */
    public void setSimLoaded(boolean value) {
        logi("Set mSimLoaded: " + value);
        this.mSimLoaded = value;
    }

    /* access modifiers changed from: private */
    public void handleSubinfoUpdate() {
        if (isSubInfoReady()) {
            handleSuppServInit();
        }
    }

    private void handleSuppServInit() {
        String iccid = ((TelephonyManager) this.mContext.getSystemService("phone")).getSimSerialNumber(getSubIdUsingPhoneId(this.mPhoneId));
        if (!TextUtils.isEmpty(iccid)) {
            handleXcapQueryIfSimChangedOrBootup(iccid);
        }
    }

    private void handleXcapQueryIfSimChangedOrBootup(String iccid) {
        logw("handleXcapQueryIfSimChangedOrBootup mySubId " + getSubIdUsingPhoneId(this.mPhoneId) + " old iccid : " + Rlog.pii(LOG_TAG, this.mOldIccId) + " new iccid : " + Rlog.pii(LOG_TAG, iccid));
        if (!iccid.equals(this.mOldIccId)) {
            this.mOldIccId = iccid;
            this.mSimIsChangedAfterBoot = DBG;
            this.mSuppServTaskDriven.clearPendingTask();
            setVolteSubscriptionDirectly(this.mPluginBase.getVolteSubUnknownConstant(), "Reset VoLTE subscription status");
            setUtCapabilityDirectly(0, "Reset Ut capabatility status");
            this.mSuppServTaskDriven.appendTask(new Task(1, false, "Sim Changed or Bootup"));
        } else if (VDBG) {
            logd("handleXcapQueryIfSimChangedOrBootup: Same SIM.");
        }
    }

    private void setVolteSubscriptionDirectly(int status, String msgStr) {
        this.mSuppServTaskDriven.appendTask(new Task(2, status, msgStr));
    }

    private void setUtCapabilityDirectly(int status, String msgStr) {
        this.mSuppServTaskDriven.appendTask(new Task(3, status, msgStr));
    }

    public void handleMessage(Message msg) {
        logd("handleMessage: " + toEventString(msg.what) + "(" + msg.what + ")");
        AsyncResult ar = (AsyncResult) msg.obj;
        switch (msg.what) {
            case 0:
                this.mQueryXcapDone = DBG;
                taskDone();
                return;
            case 1:
                int status = ((int[]) ar.result)[0];
                logd("EVENT_IMS_REGISTRATION_INFO: " + status);
                if (status == 1) {
                    setVolteSubscriptionDirectly(this.mPluginBase.getVolteSubEnableConstant(), "Ims registered.");
                    return;
                }
                return;
            case 2:
                this.mRadioState = ImsCommandsInterface.RadioState.RADIO_UNAVAILABLE.ordinal();
                return;
            case 3:
                this.mRadioState = ImsCommandsInterface.RadioState.RADIO_OFF.ordinal();
                return;
            case 4:
                this.mRadioState = ImsCommandsInterface.RadioState.RADIO_ON.ordinal();
                this.mSuppServTaskDriven.appendTask(new Task(0, false, "Radio on"));
                return;
            case 5:
                int volteSubstatus = ((int[]) ar.result)[0];
                logd(" EVENT_ON_VOLTE_SUBSCRIPTION, volteSubstatus = " + volteSubstatus);
                if (volteSubstatus == 1) {
                    setVolteSubscriptionDirectly(this.mPluginBase.getVolteSubEnableConstant(), "Receive VoLTE Subscription URC");
                    setUtCapabilityDirectly(1, "Receive VoLTE Subscription URC");
                    return;
                } else if (volteSubstatus == 2) {
                    setVolteSubscriptionDirectly(this.mPluginBase.getVolteSubDisableConstant(), "Receive VoLTE Subscription URC");
                    setUtCapabilityDirectly(2, "Receive VoLTE Subscription URC");
                    return;
                } else {
                    return;
                }
            default:
                logd("Unhandled msg: " + msg.what);
                return;
        }
    }

    private int commandExceptionToVolteServiceStatus(CommandException commandException) {
        int status = this.mPluginBase.getVolteSubUnknownConstant();
        CommandException.Error err = commandException.getCommandError();
        logd("commandException: " + err);
        if (err == CommandException.Error.OEM_ERROR_2) {
            return this.mPluginBase.getVolteSubDisableConstant();
        }
        if (err == CommandException.Error.OEM_ERROR_4) {
            return this.mPluginBase.getVolteSubEnableConstant();
        }
        if (err == CommandException.Error.OEM_ERROR_25) {
            return this.mPluginBase.getVolteSubEnableConstant();
        }
        if (err == CommandException.Error.REQUEST_NOT_SUPPORTED) {
            return this.mPluginBase.getVolteSubDisableConstant();
        }
        return status;
    }

    public boolean isSupportCFT() {
        int status = getVolteSubscriptionFromSettings();
        logd("isSupportCFT: getVolteSubscriptionFromSettings = " + status);
        if (isOp(OperatorUtils.OPID.OP01)) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public int getVolteSubscriptionFromSettings() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        return Settings.Global.getInt(contentResolver, this.mPluginBase.getVolteSubscriptionKey() + this.mPhoneId, this.mPluginBase.getVolteSubUnknownConstant());
    }

    /* access modifiers changed from: private */
    public void setVolteSubscriptionToSettings(int status) {
        logd("setVolteSubscriptionToSettings: " + status);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Global.putInt(contentResolver, this.mPluginBase.getVolteSubscriptionKey() + this.mPhoneId, status);
    }

    public int getUtCapabilityFromSettings() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        return Settings.Global.getInt(contentResolver, SETTING_UT_CAPABILITY + this.mPhoneId, 0);
    }

    /* access modifiers changed from: private */
    public void setUtCapabilityToSettings(int status) {
        logd("setUtCapabilityToSettings: " + status);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Global.putInt(contentResolver, SETTING_UT_CAPABILITY + this.mPhoneId, status);
        this.mImsService.notifyUtCapabilityChange(this.mPhoneId);
    }

    /* access modifiers changed from: private */
    public static int getSubIdUsingPhoneId(int phoneId) {
        int[] values = SubscriptionManager.getSubId(phoneId);
        if (values == null || values.length <= 0) {
            return SubscriptionManager.getDefaultSubscriptionId();
        }
        return values[0];
    }

    private String getIccIdFromSp() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        String iccid = sp.getString(ICCID_KEY + this.mPhoneId, "");
        logd("getIccIdFromSp: " + iccid);
        return iccid;
    }

    private String toEventString(int event) {
        switch (event) {
            case 0:
                return "EVENT_IMS_UT_EVENT_QUERY_XCAP";
            case 1:
                return "EVENT_IMS_REGISTRATION_INFO";
            case 2:
                return "EVENT_RADIO_NOT_AVAILABLE";
            case 3:
                return "EVENT_RADIO_OFF";
            case 4:
                return "EVENT_RADIO_ON";
            case 5:
                return "EVENT_ON_VOLTE_SUBSCRIPTION";
            default:
                return "UNKNOWN_IMS_EVENT_ID";
        }
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, "[" + this.mPhoneId + "]" + s);
    }

    private void logw(String s) {
        Rlog.w(LOG_TAG, "[" + this.mPhoneId + "]" + s);
    }

    private void logi(String s) {
        Rlog.i(LOG_TAG, "[" + this.mPhoneId + "]" + s);
    }

    /* access modifiers changed from: private */
    public void logd(String s) {
        Rlog.d(LOG_TAG, "[" + this.mPhoneId + "]" + s);
    }

    private void logv(String s) {
        Rlog.v(LOG_TAG, "[" + this.mPhoneId + "]" + s);
    }
}
