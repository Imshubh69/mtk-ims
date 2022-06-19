package com.mediatek.ims;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.radio.V1_0.RadioError;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsCallSessionListener;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.aidl.IImsCallSessionListener;
import android.telephony.ims.aidl.IImsSmsListener;
import android.telephony.ims.feature.CapabilityChangeRequest;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.feature.RcsFeature;
import android.telephony.ims.stub.ImsCallSessionImplBase;
import android.telephony.ims.stub.ImsEcbmImplBase;
import android.telephony.ims.stub.ImsMultiEndpointImplBase;
import android.telephony.ims.stub.ImsUtImplBase;
import android.text.TextUtils;
import com.android.ims.ImsConfigListener;
import com.android.ims.ImsManager;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsEcbmListener;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsUt;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.SmsMessageBase;
import com.mediatek.gba.NafSessionKey;
import com.mediatek.ims.OperatorUtils;
import com.mediatek.ims.config.ImsConfigContract;
import com.mediatek.ims.config.internal.ImsConfigUtils;
import com.mediatek.ims.ext.IImsServiceExt;
import com.mediatek.ims.ext.OpImsServiceCustomizationUtils;
import com.mediatek.ims.feature.MtkImsUtImplBase;
import com.mediatek.ims.internal.IMtkImsCallSession;
import com.mediatek.ims.internal.IMtkImsRegistrationListener;
import com.mediatek.ims.internal.IMtkImsUt;
import com.mediatek.ims.internal.IVoDataService;
import com.mediatek.ims.internal.ImsDataTracker;
import com.mediatek.ims.internal.ImsMultiEndpointProxy;
import com.mediatek.ims.internal.ImsXuiManager;
import com.mediatek.ims.plugin.ExtensionFactory;
import com.mediatek.ims.plugin.ImsManagerOemPlugin;
import com.mediatek.ims.plugin.ImsRegistrationOemPlugin;
import com.mediatek.ims.plugin.OemPluginFactory;
import com.mediatek.ims.ril.ImsCommandsInterface;
import com.mediatek.ims.ril.ImsRILAdapter;
import com.mediatek.wfo.IMwiService;
import com.mediatek.wfo.IWifiOffloadService;
import com.mediatek.wfo.MwisConstants;
import com.mediatek.wfo.WifiOffloadManager;
import com.mediatek.wfo.impl.WfoService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class ImsService {
    private static final String CONFIG_EXTRA_PHONE_ID = "phone_id";
    private static final boolean DBG = true;
    private static final boolean ENGLOAD = "eng".equals(Build.TYPE);
    protected static final int EVENT_CALL_ADDITIONAL_INFO_INDICATION = 36;
    protected static final int EVENT_CALL_INFO_INDICATION = 8;
    protected static final int EVENT_DETAIL_IMS_REGISTRATION_IND = 38;
    protected static final int EVENT_IMS_DEREG_DONE = 16;
    protected static final int EVENT_IMS_DEREG_URC = 17;
    protected static final int EVENT_IMS_DISABLED_URC = 5;
    protected static final int EVENT_IMS_DISABLING_URC = 12;
    protected static final int EVENT_IMS_ENABLED_URC = 11;
    protected static final int EVENT_IMS_ENABLING_URC = 10;
    protected static final int EVENT_IMS_NOTIFICATION_INIT = 39;
    private static final int EVENT_IMS_REGISTRATION_INFO = 1;
    protected static final int EVENT_IMS_REGISTRATION_STATUS_REPORT_IND = 37;
    protected static final int EVENT_IMS_REG_FLAG_IND = 40;
    protected static final int EVENT_IMS_REG_FLAG_IND_TIME_OUT = 41;
    protected static final int EVENT_IMS_RTP_INFO_URC = 20;
    protected static final int EVENT_IMS_SMS_NEW_CDMA_SMS = 32;
    protected static final int EVENT_IMS_SMS_NEW_SMS = 31;
    protected static final int EVENT_IMS_SMS_STATUS_REPORT = 30;
    protected static final int EVENT_IMS_SUPPORT_ECC_URC = 25;
    protected static final int EVENT_IMS_VOLTE_SETTING_URC = 22;
    protected static final int EVENT_INCOMING_CALL_INDICATION = 7;
    protected static final int EVENT_INIT_CALL_SESSION_PROXY = 27;
    protected static final int EVENT_ON_USSI = 15;
    protected static final int EVENT_RADIO_NOT_AVAILABLE = 2;
    protected static final int EVENT_RADIO_OFF = 18;
    protected static final int EVENT_RADIO_ON = 19;
    protected static final int EVENT_READY_TO_RECEIVE_PENDING_IND = 33;
    protected static final int EVENT_RUN_GBA = 23;
    protected static final int EVENT_SELF_IDENTIFY_UPDATE = 24;
    protected static final int EVENT_SEND_SMS_DONE = 28;
    protected static final int EVENT_SET_IMS_DISABLE_DONE = 4;
    protected static final int EVENT_SET_IMS_ENABLED_DONE = 3;
    protected static final int EVENT_SET_IMS_REGISTRATION_REPORT_DONE = 21;
    protected static final int EVENT_SIP_CODE_INDICATION = 13;
    protected static final int EVENT_SIP_CODE_INDICATION_DEREG = 14;
    protected static final int EVENT_START_GBA_SERVICE = 26;
    protected static final int EVENT_TEST_QUERY_VOPS_STATUS = 35;
    protected static final int EVENT_UT_CAPABILITY_CHANGE = 29;
    private static final int EVENT_VIRTUAL_SIM_ON = 6;
    protected static final int EVENT_VOPS_STATUS_IND = 34;
    private static final CharSequence IMSSERVICE_NOTIFICATION_NAME = "ImsService notification";
    private static final int IMS_ALLOW_INCOMING_CALL_INDICATION = 0;
    private static final int IMS_DISALLOW_INCOMING_CALL_INDICATION = 1;
    private static final int IMS_MAX_FEATURE_SUPPORT_SIZE = 6;
    private static final int IMS_RCS_OVER_LTE = 2;
    private static final int IMS_REG_SIP_URI_TYPE_IMSI = 1;
    private static final int IMS_REG_SIP_URI_TYPE_MSISDN = 0;
    private static final String IMS_SERVICE = "ims";
    private static final int IMS_SMS_OVER_LTE = 4;
    private static final int IMS_SS_CMD_ERROR = 3;
    private static final int IMS_SS_CMD_SUCCESS = 4;
    private static final int IMS_SS_INTERRUPT_ERROR = 2;
    private static final int IMS_SS_TIMEOUT_ERROR = 1;
    private static final int IMS_VIDEO_OVER_LTE = 8;
    private static final int IMS_VOICE_OVER_LTE = 1;
    private static final int IMS_VOICE_OVER_WIFI = 16;
    private static final String LOG_TAG = "ImsService";
    private static final int MT_CALL_DIAL_IMS_STK = 100;
    private static final String NOTIFICATION_CHANNEL = "ImsService";
    private static final String PROPERTY_IMSCONFIG_FORCE_NOTIFY = "vendor.ril.imsconfig.force.notify";
    private static final String PROPERTY_IMS_REG_EXTINFO = "ril.ims.extinfo";
    /* access modifiers changed from: private */
    public static final boolean SENLOG = TextUtils.equals(Build.TYPE, "user");
    private static final int TIMER_IMS_EIMSUI_TIMEOUT = 10000;
    private static final boolean VDBG = false;
    protected static final Object mLock = new Object();
    /* access modifiers changed from: private */
    public static HashMap<Integer, IMtkMmTelFeatureCallback> mMmTelFeatureCallback = new HashMap<>();
    private static HashMap<Integer, IMtkRcsFeatureCallback> mRcsFeatureCallback = new HashMap<>();
    private static ImsService sInstance = null;
    private static HashMap<Integer, MtkImsRegistrationImpl> sMtkImsRegImpl = new HashMap<>();
    private static HashMap<Integer, MtkSuppServExt> sMtkSSExt = new HashMap<>();
    /* access modifiers changed from: private */
    public static IWifiOffloadService sWifiOffloadService = null;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            ImsService imsService = ImsService.this;
            imsService.log("[onReceive] action=" + intent.getAction());
            if ("com.mediatek.ims.ACTION_IMS_SIMULATE".equals(intent.getAction())) {
                boolean unused = ImsService.this.mImsRegistry = intent.getBooleanExtra("registry", false);
                ImsService imsService2 = ImsService.this;
                imsService2.logw("Simulate IMS Registration: " + ImsService.this.mImsRegistry);
                int phoneId = ImsCommonUtil.getMainCapabilityPhoneId();
                ImsService.this.mHandler[phoneId].sendMessage(ImsService.this.mHandler[phoneId].obtainMessage(1, new AsyncResult((Object) null, new int[]{ImsService.this.mImsRegistry, 15, phoneId}, (Throwable) null)));
            } else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                if (!ImsCommonUtil.supportMdAutoSetupIms()) {
                    ImsService.this.bindAndRegisterWifiOffloadService();
                } else {
                    ImsService.this.bindAndRegisterMWIService();
                }
                for (int i = 0; i < ImsService.this.mNumOfPhones; i++) {
                    if (ImsService.this.mImsState[i] == 1) {
                        Intent newIntent = new Intent("com.android.ims.IMS_SERVICE_UP");
                        newIntent.putExtra("android:phone_id", i);
                        ImsService.this.mContext.sendBroadcast(newIntent);
                        ImsService imsService3 = ImsService.this;
                        imsService3.log("broadcast IMS_SERVICE_UP for phone=" + i);
                    }
                }
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                if ("ABSENT".equals(intent.getStringExtra("ss"))) {
                    int phoneId2 = intent.getIntExtra("phone", -1);
                    if (ImsService.isValidPhoneId(phoneId2)) {
                        ImsService.this.resetXuiAndNotify(phoneId2);
                    }
                }
            } else if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                int slotId = intent.getIntExtra("slot", 0);
                if (extras != null) {
                    ServiceState ss = ServiceState.newFromBundle(extras);
                    int dataState = ss.getDataRegState();
                    int dataNetType = ss.getDataNetworkType();
                    ImsService imsService4 = ImsService.this;
                    imsService4.log("ACTION_SERVICE_STATE_CHANGED: slotId=" + slotId + ", ims=" + ImsService.this.mImsRegInfo[slotId] + ",data=" + dataState);
                    if (ImsService.this.mImsRegInfo[slotId] == 0) {
                        if (dataState != 0) {
                            ImsService.this.setNotificationVirtual(slotId, 1);
                        } else if (dataNetType == 13 || dataNetType == 19 || dataNetType == 20 || dataNetType == 18) {
                            ImsService imsService5 = ImsService.this;
                            imsService5.setNotificationVirtual(slotId, imsService5.mImsRegInfo[slotId]);
                        } else {
                            ImsService.this.setNotificationVirtual(slotId, 1);
                        }
                    }
                }
            }
            ImsService imsService6 = ImsService.this;
            imsService6.log("[onReceive] finished action=" + intent.getAction());
        }
    };
    private Object mCapLockObj = new Object();
    /* access modifiers changed from: private */
    public Context mContext;
    private IWifiOffloadServiceDeathRecipient mDeathRecipient = new IWifiOffloadServiceDeathRecipient();
    /* access modifiers changed from: private */
    public int[] mExpectedImsState;
    /* access modifiers changed from: private */
    public BroadcastReceiver mFeatureValueReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int feature = intent.getIntExtra(ImsConfigContract.EXTRA_CHANGED_ITEM, -1);
            int phoneId = intent.getIntExtra("phone_id", -1);
            ImsService imsService = ImsService.this;
            imsService.log("volte_setting mFeatureValueReceiver action: " + intent.getAction() + ", phoneId: " + phoneId + ", feature: " + feature + ", mWaitFeatureChange" + ImsService.this.mWaitFeatureChange);
            if (feature != 0) {
                return;
            }
            if (!ImsService.isValidPhoneId(phoneId)) {
                ImsService imsService2 = ImsService.this;
                imsService2.loge("volte_setting mFeatureValueReceiver error phoneId:" + phoneId);
                return;
            }
            if ((ImsService.this.mWaitFeatureChange & (1 << phoneId)) != 0) {
                ImsService imsService3 = ImsService.this;
                int unused = imsService3.mWaitFeatureChange = (~(1 << phoneId)) & imsService3.mWaitFeatureChange;
            }
            if (ImsService.this.mWaitFeatureChange == 0) {
                ImsService.this.mContext.unregisterReceiver(ImsService.this.mFeatureValueReceiver);
                SystemProperties.set(ImsService.PROPERTY_IMSCONFIG_FORCE_NOTIFY, "0");
            }
            ImsService imsService4 = ImsService.this;
            imsService4.log("volte_setting mFeatureValueReceiver finished mWaitFeatureChange:" + ImsService.this.mWaitFeatureChange);
        }
    };
    private ServiceConnection mGbaConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            ImsService.this.log("GbaService onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName name) {
            ImsService.this.log("GbaService onServiceFailed");
        }
    };
    /* access modifiers changed from: private */
    public Handler[] mHandler;
    private ImsAdapter mImsAdapter = null;
    /* access modifiers changed from: private */
    public ImsCallProfile[] mImsCallProfile;
    private String[] mImsConfigIccid = null;
    private ImsConfigManager mImsConfigManager = null;
    private String[] mImsConfigMccmnc = null;
    private ImsDataTracker mImsDataTracker;
    private ImsEcbmProxy[] mImsEcbm;
    private ImsEventPackageAdapter[] mImsEvtPkgAdapter;
    /* access modifiers changed from: private */
    public int[] mImsExtInfo;
    private ImsManagerOemPlugin mImsManagerOemPlugin = null;
    /* access modifiers changed from: private */
    public ImsCommandsInterface[] mImsRILAdapters = null;
    /* access modifiers changed from: private */
    public ImsRegInfo[] mImsRegInd;
    /* access modifiers changed from: private */
    public int[] mImsRegInfo;
    private ImsRegistrationOemPlugin mImsRegOemPlugin;
    /* access modifiers changed from: private */
    public boolean mImsRegistry = false;
    private ArrayList<HashSet<IImsSmsListener>> mImsSmsListener = new ArrayList<>();
    /* access modifiers changed from: private */
    public int[] mImsState;
    /* access modifiers changed from: private */
    public int[] mIsImsEccSupported;
    /* access modifiers changed from: private */
    public boolean[] mIsMTredirect;
    /* access modifiers changed from: private */
    public boolean[] mIsPendingMTTerminated;
    /* access modifiers changed from: private */
    public ArrayList<HashSet<IImsRegistrationListener>> mListener = new ArrayList<>();
    private Object mLockObj = new Object();
    private Object mLockUri = new Object();
    private ArrayList<HashSet<IMtkImsRegistrationListener>> mMtkListener = new ArrayList<>();
    /* access modifiers changed from: private */
    public MtkImsCallSessionProxy[] mMtkPendingMT = null;
    private ImsMultiEndpointProxy[] mMultiEndpoints;
    /* access modifiers changed from: private */
    public int mNumOfPhones = 0;
    private ImsCallSessionProxy[] mPendingMT = null;
    /* access modifiers changed from: private */
    public String[] mPendingMTCallId = null;
    private String[] mPendingMTSeqNum = null;
    /* access modifiers changed from: private */
    public Map<Object, Object> mPendingMtkImsCallSessionProxy = new HashMap();
    private IWifiOffloadListenerProxy mProxy = null;
    /* access modifiers changed from: private */
    public int[] mRAN;
    /* access modifiers changed from: private */
    public AsyncResult mRedirectIncomingAsyncResult = null;
    /* access modifiers changed from: private */
    public int mRedirectIncomingSocketId = -1;
    /* access modifiers changed from: private */
    public int[] mRegErrorCode;
    /* access modifiers changed from: private */
    public boolean mRegisterSubInfoChange = false;
    private int[] mServiceId;
    private Looper mSslooper;
    /* access modifiers changed from: private */
    public final BroadcastReceiver mSubInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            ImsService imsService = ImsService.this;
            imsService.log("volte_setting mSubInfoReceiver action: " + intent.getAction());
            if ("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED".equals(intent.getAction())) {
                boolean needDereg = ImsService.DBG;
                for (int phoneId = 0; phoneId < ImsService.this.mNumOfPhones; phoneId++) {
                    if (ImsService.this.mWaitSubInfoChange[phoneId] && ImsService.this.getSubIdUsingPhoneId(phoneId) > -1) {
                        ImsService imsService2 = ImsService.this;
                        imsService2.setEnhanced4gLteModeSetting(phoneId, imsService2.mVolteEnable[phoneId]);
                        ImsService.this.mWaitSubInfoChange[phoneId] = false;
                    }
                    if (ImsService.this.mWaitSubInfoChange[phoneId]) {
                        needDereg = false;
                    }
                }
                if (needDereg) {
                    ImsService.this.mContext.unregisterReceiver(ImsService.this.mSubInfoReceiver);
                    boolean unused = ImsService.this.mRegisterSubInfoChange = false;
                }
            }
            ImsService.this.log("volte_setting mSubInfoReceiver finished");
        }
    };
    private boolean[] mTempDisableWFC;
    /* access modifiers changed from: private */
    public boolean[] mVolteEnable;
    private int[] mVopsReport;
    /* access modifiers changed from: private */
    public int mWaitFeatureChange = 0;
    /* access modifiers changed from: private */
    public boolean[] mWaitSubInfoChange;
    /* access modifiers changed from: private */
    public int[] mWfcHandoverToLteState;
    /* access modifiers changed from: private */
    public int[] mWfcPdnState;
    private int[] mWfcRegErrorCode;

    public interface IMtkMmTelFeatureCallback {
        void newImsSmsInd(byte[] bArr, String str);

        void newStatusReportInd(byte[] bArr, String str);

        void notifyCapabilitiesChanged(MmTelFeature.MmTelCapabilities mmTelCapabilities);

        void notifyContextChanged(Context context);

        void notifyIncomingCall(ImsCallSessionImplBase imsCallSessionImplBase, Bundle bundle);

        void notifyIncomingCallSession(IImsCallSession iImsCallSession, Bundle bundle);

        void sendSmsRsp(int i, int i2, int i3, int i4);

        void updateCapbilities(CapabilityChangeRequest capabilityChangeRequest);
    }

    public interface IMtkRcsFeatureCallback {
        void notifyCapabilitiesChanged(RcsFeature.RcsImsCapabilities rcsImsCapabilities);

        void notifyContextChanged(Context context);
    }

    private class NafSessionKeyResult {
        int cmdResult;
        Object lockObj;
        NafSessionKey nafSessionKey;

        private NafSessionKeyResult() {
            this.nafSessionKey = null;
            this.cmdResult = 1;
            this.lockObj = new Object();
        }
    }

    public static ImsService getInstance(Context context) {
        ImsService imsService;
        synchronized (mLock) {
            if (sInstance == null && context != null) {
                ImsService imsService2 = new ImsService(context);
                sInstance = imsService2;
                imsService2.log("ImsService is created!");
            }
            imsService = sInstance;
        }
        return imsService;
    }

    public ImsService(Context context) {
        logi("init");
        this.mContext = context;
        this.mNumOfPhones = TelephonyManager.getDefault().getPhoneCount();
        if (!ImsCommonUtil.supportMdAutoSetupIms()) {
            this.mImsAdapter = new ImsAdapter(context);
        }
        int i = this.mNumOfPhones;
        this.mHandler = new MyHandler[i];
        this.mImsRILAdapters = new ImsCommandsInterface[i];
        for (int i2 = 0; i2 < this.mNumOfPhones; i2++) {
            this.mHandler[i2] = new MyHandler(i2);
            ImsRILAdapter ril = new ImsRILAdapter(context, i2);
            ril.registerForNotAvailable(this.mHandler[i2], 2, (Object) null);
            ril.registerForOff(this.mHandler[i2], 18, (Object) null);
            ril.registerForOn(this.mHandler[i2], 19, (Object) null);
            ril.registerForImsRegistrationInfo(this.mHandler[i2], 1, (Object) null);
            ril.registerForImsEnableStart(this.mHandler[i2], 10, (Object) null);
            ril.registerForImsEnableComplete(this.mHandler[i2], 11, (Object) null);
            ril.registerForImsDisableStart(this.mHandler[i2], 12, (Object) null);
            ril.registerForImsDisableComplete(this.mHandler[i2], 5, (Object) null);
            ril.setOnIncomingCallIndication(this.mHandler[i2], 7, (Object) null);
            ril.registerForCallProgressIndicator(this.mHandler[i2], 13, (Object) null);
            ril.registerForImsDeregisterComplete(this.mHandler[i2], 17, (Object) null);
            ril.registerForImsEccSupport(this.mHandler[i2], 25, (Object) null);
            ril.setOnUSSI(this.mHandler[i2], 15, (Object) null);
            ril.registerForImsRTPInfo(this.mHandler[i2], 20, (Object) null);
            ril.registerForVolteSettingChanged(this.mHandler[i2], 22, (Object) null);
            ril.registerForImsRegStatusInd(this.mHandler[i2], 37, (Object) null);
            ril.registerForDetailImsRegistrationInd(this.mHandler[i2], 38, (Object) null);
            if (ImsCommonUtil.supportMdAutoSetupIms()) {
                ril.registerForXuiInfo(this.mHandler[i2], 24, (Object) null);
            }
            ril.setOnSmsStatus(this.mHandler[i2], 30, (Object) null);
            ril.setOnNewSms(this.mHandler[i2], 31, (Object) null);
            ril.setOnNewCdmaSms(this.mHandler[i2], 32, (Object) null);
            ril.registerForVopsStatusInd(this.mHandler[i2], 34, (Object) null);
            ril.registerForCallAdditionalInfo(this.mHandler[i2], 36, (Object) null);
            ril.registerForImsRegFlagInd(this.mHandler[i2], 40, (Object) null);
            this.mImsRILAdapters[i2] = ril;
        }
        if (ImsCommonUtil.supportMdAutoSetupIms() != 0) {
            log("Initializing");
            this.mImsDataTracker = new ImsDataTracker(context, this.mImsRILAdapters);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.mediatek.ims.ACTION_IMS_SIMULATE");
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        if (SystemProperties.getInt("ro.vendor.mtk_ims_notification", 0) == 1) {
            filter.addAction("android.intent.action.SERVICE_STATE");
        }
        context.registerReceiver(this.mBroadcastReceiver, filter);
        int i3 = this.mNumOfPhones;
        this.mImsRegInfo = new int[i3];
        this.mImsExtInfo = new int[i3];
        this.mServiceId = new int[i3];
        this.mImsState = new int[i3];
        this.mExpectedImsState = new int[i3];
        this.mRegErrorCode = new int[i3];
        this.mRAN = new int[i3];
        this.mImsEcbm = new ImsEcbmProxy[i3];
        this.mImsEvtPkgAdapter = new ImsEventPackageAdapter[i3];
        this.mImsConfigManager = new ImsConfigManager(context, this.mImsRILAdapters);
        int i4 = this.mNumOfPhones;
        this.mImsConfigMccmnc = new String[i4];
        this.mImsConfigIccid = new String[i4];
        this.mIsImsEccSupported = new int[i4];
        this.mWaitSubInfoChange = new boolean[i4];
        this.mVolteEnable = new boolean[i4];
        this.mImsRegInd = new ImsRegInfo[i4];
        this.mPendingMT = new ImsCallSessionProxy[i4];
        this.mMtkPendingMT = new MtkImsCallSessionProxy[i4];
        this.mPendingMTCallId = new String[i4];
        this.mPendingMTSeqNum = new String[i4];
        this.mIsPendingMTTerminated = new boolean[i4];
        this.mImsCallProfile = new ImsCallProfile[i4];
        this.mMultiEndpoints = new ImsMultiEndpointProxy[i4];
        this.mIsMTredirect = new boolean[i4];
        this.mWfcPdnState = new int[i4];
        this.mWfcRegErrorCode = new int[i4];
        this.mWfcHandoverToLteState = new int[i4];
        HandlerThread ssHandlerThread = new HandlerThread("MtkSSExt");
        ssHandlerThread.start();
        this.mSslooper = ssHandlerThread.getLooper();
        for (int i5 = 0; i5 < this.mNumOfPhones; i5++) {
            this.mListener.add(new HashSet());
            this.mMtkListener.add(new HashSet());
            this.mImsRegInfo[i5] = 3;
            this.mImsExtInfo[i5] = 0;
            this.mServiceId[i5] = i5 + 1;
            this.mImsState[i5] = 0;
            this.mExpectedImsState[i5] = 0;
            this.mRegErrorCode[i5] = 0;
            this.mRAN[i5] = 1;
            this.mWfcPdnState[i5] = 0;
            this.mWfcRegErrorCode[i5] = 0;
            this.mImsEcbm[i5] = new ImsEcbmProxy(this.mContext, this.mImsRILAdapters[i5], i5);
            this.mImsConfigManager.init(i5, (ImsCommandsInterface[]) null);
            this.mIsImsEccSupported[i5] = 0;
            if (ImsCommonUtil.supportMdAutoSetupIms()) {
                sMtkSSExt.put(Integer.valueOf(i5), new MtkSuppServExt(this.mContext, i5, this, this.mSslooper));
            }
            this.mImsEvtPkgAdapter[i5] = new ImsEventPackageAdapter(this.mContext, this.mHandler[i5], this.mImsRILAdapters[i5], i5);
            this.mWaitSubInfoChange[i5] = false;
            this.mVolteEnable[i5] = false;
            this.mIsPendingMTTerminated[i5] = false;
            this.mIsMTredirect[i5] = false;
            this.mWfcHandoverToLteState[i5] = 1;
        }
        if (!ImsCommonUtil.supportMims()) {
            int mainPhoneId = ImsCommonUtil.getMainCapabilityPhoneId();
            log("getMainCapabilityPhoneId: mainPhoneId = " + mainPhoneId);
            this.mImsRILAdapters[mainPhoneId].setImsRegistrationReport(this.mHandler[mainPhoneId].obtainMessage(21));
            if (!ImsCommonUtil.supportMdAutoSetupIms() && isValidPhoneId(mainPhoneId)) {
                initImsAvailability(mainPhoneId, 0, 3, 4);
            }
        } else {
            for (int i6 = 0; i6 < this.mNumOfPhones; i6++) {
                this.mImsRILAdapters[i6].setImsRegistrationReport(this.mHandler[i6].obtainMessage(21));
                if (!ImsCommonUtil.supportMdAutoSetupIms()) {
                    initImsAvailability(i6, i6, 3, 4);
                }
            }
        }
        ExtensionFactory.makeOemPluginFactory(this.mContext);
        ExtensionFactory.makeExtensionPluginFactory(this.mContext);
        if (this.mImsManagerOemPlugin == null) {
            this.mImsManagerOemPlugin = ExtensionFactory.makeOemPluginFactory(this.mContext).makeImsManagerPlugin(this.mContext);
        }
        ImsManagerOemPlugin imsManagerOemPlugin = this.mImsManagerOemPlugin;
        if (imsManagerOemPlugin == null) {
            loge("ImsManagerOemPlugin is null");
            startWfoService();
        } else if (imsManagerOemPlugin.isWfcSupport()) {
            startWfoService();
        }
        for (int i7 = 0; i7 < this.mNumOfPhones; i7++) {
            Handler[] handlerArr = this.mHandler;
            handlerArr[i7].sendMessage(handlerArr[i7].obtainMessage(33));
        }
        IImsServiceExt opImsService = getOpImsService();
        if (opImsService != null) {
            for (int i8 = 0; i8 < this.mNumOfPhones; i8++) {
                opImsService.notifyImsServiceEvent(i8, this.mContext, this.mHandler[i8].obtainMessage(39));
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0367, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyMultiSimConfigChanged(android.content.Context r11, int r12) {
        /*
            r10 = this;
            java.lang.Object r0 = mLock
            monitor-enter(r0)
            com.mediatek.ims.ril.ImsCommandsInterface[] r1 = r10.mImsRILAdapters     // Catch:{ all -> 0x0368 }
            int r1 = r1.length     // Catch:{ all -> 0x0368 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0368 }
            r2.<init>()     // Catch:{ all -> 0x0368 }
            java.lang.String r3 = "notifyMultiSimConfigChanged, phone:"
            r2.append(r3)     // Catch:{ all -> 0x0368 }
            r2.append(r1)     // Catch:{ all -> 0x0368 }
            java.lang.String r3 = "->"
            r2.append(r3)     // Catch:{ all -> 0x0368 }
            r2.append(r12)     // Catch:{ all -> 0x0368 }
            java.lang.String r3 = ", mNumOfPhones:"
            r2.append(r3)     // Catch:{ all -> 0x0368 }
            int r3 = r10.mNumOfPhones     // Catch:{ all -> 0x0368 }
            r2.append(r3)     // Catch:{ all -> 0x0368 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0368 }
            r10.logi(r2)     // Catch:{ all -> 0x0368 }
            if (r1 != r12) goto L_0x0030
            monitor-exit(r0)     // Catch:{ all -> 0x0368 }
            return
        L_0x0030:
            r10.mNumOfPhones = r12     // Catch:{ all -> 0x0368 }
            if (r1 <= r12) goto L_0x0036
            monitor-exit(r0)     // Catch:{ all -> 0x0368 }
            return
        L_0x0036:
            java.lang.String r2 = "notifyMultiSimConfigChanged, run"
            r10.logi(r2)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r2 = r10.mHandler     // Catch:{ all -> 0x0368 }
            java.lang.Object[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r2 = (android.os.Handler[]) r2     // Catch:{ all -> 0x0368 }
            r10.mHandler = r2     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ril.ImsCommandsInterface[] r2 = r10.mImsRILAdapters     // Catch:{ all -> 0x0368 }
            java.lang.Object[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ril.ImsCommandsInterface[] r2 = (com.mediatek.ims.ril.ImsCommandsInterface[]) r2     // Catch:{ all -> 0x0368 }
            r10.mImsRILAdapters = r2     // Catch:{ all -> 0x0368 }
            r2 = r1
        L_0x0050:
            r3 = 1
            if (r2 >= r12) goto L_0x0143
            android.os.Handler[] r4 = r10.mHandler     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ImsService$MyHandler r5 = new com.mediatek.ims.ImsService$MyHandler     // Catch:{ all -> 0x0368 }
            r5.<init>(r2)     // Catch:{ all -> 0x0368 }
            r4[r2] = r5     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ril.ImsRILAdapter r4 = new com.mediatek.ims.ril.ImsRILAdapter     // Catch:{ all -> 0x0368 }
            r4.<init>(r11, r2)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r5 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r5 = r5[r2]     // Catch:{ all -> 0x0368 }
            r6 = 2
            r7 = 0
            r4.registerForNotAvailable(r5, r6, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r5 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r5 = r5[r2]     // Catch:{ all -> 0x0368 }
            r6 = 18
            r4.registerForOff(r5, r6, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r5 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r5 = r5[r2]     // Catch:{ all -> 0x0368 }
            r6 = 19
            r4.registerForOn(r5, r6, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r5 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r5 = r5[r2]     // Catch:{ all -> 0x0368 }
            r4.registerForImsRegistrationInfo(r5, r3, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 10
            r4.registerForImsEnableStart(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 11
            r4.registerForImsEnableComplete(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 12
            r4.registerForImsDisableStart(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 5
            r4.registerForImsDisableComplete(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 7
            r4.setOnIncomingCallIndication(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 13
            r4.registerForCallProgressIndicator(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 17
            r4.registerForImsDeregisterComplete(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 25
            r4.registerForImsEccSupport(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 15
            r4.setOnUSSI(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 20
            r4.registerForImsRTPInfo(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 22
            r4.registerForVolteSettingChanged(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 37
            r4.registerForImsRegStatusInd(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 38
            r4.registerForDetailImsRegistrationInd(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            boolean r3 = com.mediatek.ims.ImsCommonUtil.supportMdAutoSetupIms()     // Catch:{ all -> 0x0368 }
            if (r3 == 0) goto L_0x0105
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 24
            r4.registerForXuiInfo(r3, r5, r7)     // Catch:{ all -> 0x0368 }
        L_0x0105:
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 30
            r4.setOnSmsStatus(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 31
            r4.setOnNewSms(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 32
            r4.setOnNewCdmaSms(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 34
            r4.registerForVopsStatusInd(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 36
            r4.registerForCallAdditionalInfo(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 40
            r4.registerForImsRegFlagInd(r3, r5, r7)     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ril.ImsCommandsInterface[] r3 = r10.mImsRILAdapters     // Catch:{ all -> 0x0368 }
            r3[r2] = r4     // Catch:{ all -> 0x0368 }
            int r2 = r2 + 1
            goto L_0x0050
        L_0x0143:
            boolean r2 = com.mediatek.ims.ImsCommonUtil.supportMdAutoSetupIms()     // Catch:{ all -> 0x0368 }
            if (r2 == 0) goto L_0x0155
            java.lang.String r2 = "Initializing"
            r10.log(r2)     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.internal.ImsDataTracker r2 = r10.mImsDataTracker     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ril.ImsCommandsInterface[] r4 = r10.mImsRILAdapters     // Catch:{ all -> 0x0368 }
            r2.notifyMultiSimConfigChanged(r12, r4)     // Catch:{ all -> 0x0368 }
        L_0x0155:
            int[] r2 = r10.mImsRegInfo     // Catch:{ all -> 0x0368 }
            int[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mImsRegInfo = r2     // Catch:{ all -> 0x0368 }
            int[] r2 = r10.mImsExtInfo     // Catch:{ all -> 0x0368 }
            int[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mImsExtInfo = r2     // Catch:{ all -> 0x0368 }
            int[] r2 = r10.mServiceId     // Catch:{ all -> 0x0368 }
            int[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mServiceId = r2     // Catch:{ all -> 0x0368 }
            int[] r2 = r10.mImsState     // Catch:{ all -> 0x0368 }
            int[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mImsState = r2     // Catch:{ all -> 0x0368 }
            int[] r2 = r10.mExpectedImsState     // Catch:{ all -> 0x0368 }
            int[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mExpectedImsState = r2     // Catch:{ all -> 0x0368 }
            int[] r2 = r10.mRegErrorCode     // Catch:{ all -> 0x0368 }
            int[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mRegErrorCode = r2     // Catch:{ all -> 0x0368 }
            int[] r2 = r10.mRAN     // Catch:{ all -> 0x0368 }
            int[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mRAN = r2     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ImsEcbmProxy[] r2 = r10.mImsEcbm     // Catch:{ all -> 0x0368 }
            java.lang.Object[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ImsEcbmProxy[] r2 = (com.mediatek.ims.ImsEcbmProxy[]) r2     // Catch:{ all -> 0x0368 }
            r10.mImsEcbm = r2     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ImsEventPackageAdapter[] r2 = r10.mImsEvtPkgAdapter     // Catch:{ all -> 0x0368 }
            java.lang.Object[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ImsEventPackageAdapter[] r2 = (com.mediatek.ims.ImsEventPackageAdapter[]) r2     // Catch:{ all -> 0x0368 }
            r10.mImsEvtPkgAdapter = r2     // Catch:{ all -> 0x0368 }
            java.lang.String[] r2 = r10.mImsConfigMccmnc     // Catch:{ all -> 0x0368 }
            java.lang.Object[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            java.lang.String[] r2 = (java.lang.String[]) r2     // Catch:{ all -> 0x0368 }
            r10.mImsConfigMccmnc = r2     // Catch:{ all -> 0x0368 }
            java.lang.String[] r2 = r10.mImsConfigIccid     // Catch:{ all -> 0x0368 }
            java.lang.Object[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            java.lang.String[] r2 = (java.lang.String[]) r2     // Catch:{ all -> 0x0368 }
            r10.mImsConfigIccid = r2     // Catch:{ all -> 0x0368 }
            int[] r2 = r10.mIsImsEccSupported     // Catch:{ all -> 0x0368 }
            int[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mIsImsEccSupported = r2     // Catch:{ all -> 0x0368 }
            boolean[] r2 = r10.mWaitSubInfoChange     // Catch:{ all -> 0x0368 }
            boolean[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mWaitSubInfoChange = r2     // Catch:{ all -> 0x0368 }
            boolean[] r2 = r10.mVolteEnable     // Catch:{ all -> 0x0368 }
            boolean[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mVolteEnable = r2     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ImsRegInfo[] r2 = r10.mImsRegInd     // Catch:{ all -> 0x0368 }
            java.lang.Object[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ImsRegInfo[] r2 = (com.mediatek.ims.ImsRegInfo[]) r2     // Catch:{ all -> 0x0368 }
            r10.mImsRegInd = r2     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ImsCallSessionProxy[] r2 = r10.mPendingMT     // Catch:{ all -> 0x0368 }
            java.lang.Object[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ImsCallSessionProxy[] r2 = (com.mediatek.ims.ImsCallSessionProxy[]) r2     // Catch:{ all -> 0x0368 }
            r10.mPendingMT = r2     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.MtkImsCallSessionProxy[] r2 = r10.mMtkPendingMT     // Catch:{ all -> 0x0368 }
            java.lang.Object[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.MtkImsCallSessionProxy[] r2 = (com.mediatek.ims.MtkImsCallSessionProxy[]) r2     // Catch:{ all -> 0x0368 }
            r10.mMtkPendingMT = r2     // Catch:{ all -> 0x0368 }
            java.lang.String[] r2 = r10.mPendingMTCallId     // Catch:{ all -> 0x0368 }
            java.lang.Object[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            java.lang.String[] r2 = (java.lang.String[]) r2     // Catch:{ all -> 0x0368 }
            r10.mPendingMTCallId = r2     // Catch:{ all -> 0x0368 }
            java.lang.String[] r2 = r10.mPendingMTSeqNum     // Catch:{ all -> 0x0368 }
            java.lang.Object[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            java.lang.String[] r2 = (java.lang.String[]) r2     // Catch:{ all -> 0x0368 }
            r10.mPendingMTSeqNum = r2     // Catch:{ all -> 0x0368 }
            boolean[] r2 = r10.mIsPendingMTTerminated     // Catch:{ all -> 0x0368 }
            boolean[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mIsPendingMTTerminated = r2     // Catch:{ all -> 0x0368 }
            android.telephony.ims.ImsCallProfile[] r2 = r10.mImsCallProfile     // Catch:{ all -> 0x0368 }
            java.lang.Object[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            android.telephony.ims.ImsCallProfile[] r2 = (android.telephony.ims.ImsCallProfile[]) r2     // Catch:{ all -> 0x0368 }
            r10.mImsCallProfile = r2     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.internal.ImsMultiEndpointProxy[] r2 = r10.mMultiEndpoints     // Catch:{ all -> 0x0368 }
            java.lang.Object[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.internal.ImsMultiEndpointProxy[] r2 = (com.mediatek.ims.internal.ImsMultiEndpointProxy[]) r2     // Catch:{ all -> 0x0368 }
            r10.mMultiEndpoints = r2     // Catch:{ all -> 0x0368 }
            boolean[] r2 = r10.mIsMTredirect     // Catch:{ all -> 0x0368 }
            boolean[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mIsMTredirect = r2     // Catch:{ all -> 0x0368 }
            int[] r2 = r10.mWfcPdnState     // Catch:{ all -> 0x0368 }
            int[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mWfcPdnState = r2     // Catch:{ all -> 0x0368 }
            int[] r2 = r10.mWfcRegErrorCode     // Catch:{ all -> 0x0368 }
            int[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mWfcRegErrorCode = r2     // Catch:{ all -> 0x0368 }
            boolean[] r2 = r10.mTempDisableWFC     // Catch:{ all -> 0x0368 }
            boolean[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mTempDisableWFC = r2     // Catch:{ all -> 0x0368 }
            int[] r2 = r10.mWfcHandoverToLteState     // Catch:{ all -> 0x0368 }
            int[] r2 = java.util.Arrays.copyOf(r2, r12)     // Catch:{ all -> 0x0368 }
            r10.mWfcHandoverToLteState = r2     // Catch:{ all -> 0x0368 }
            r2 = r1
        L_0x0244:
            r4 = 3
            r5 = 0
            if (r2 >= r12) goto L_0x02df
            java.util.ArrayList<java.util.HashSet<com.android.ims.internal.IImsRegistrationListener>> r6 = r10.mListener     // Catch:{ all -> 0x0368 }
            java.util.HashSet r7 = new java.util.HashSet     // Catch:{ all -> 0x0368 }
            r7.<init>()     // Catch:{ all -> 0x0368 }
            r6.add(r7)     // Catch:{ all -> 0x0368 }
            java.util.ArrayList<java.util.HashSet<com.mediatek.ims.internal.IMtkImsRegistrationListener>> r6 = r10.mMtkListener     // Catch:{ all -> 0x0368 }
            java.util.HashSet r7 = new java.util.HashSet     // Catch:{ all -> 0x0368 }
            r7.<init>()     // Catch:{ all -> 0x0368 }
            r6.add(r7)     // Catch:{ all -> 0x0368 }
            int[] r6 = r10.mImsRegInfo     // Catch:{ all -> 0x0368 }
            r6[r2] = r4     // Catch:{ all -> 0x0368 }
            int[] r4 = r10.mImsExtInfo     // Catch:{ all -> 0x0368 }
            r4[r2] = r5     // Catch:{ all -> 0x0368 }
            int[] r4 = r10.mServiceId     // Catch:{ all -> 0x0368 }
            int r6 = r2 + 1
            r4[r2] = r6     // Catch:{ all -> 0x0368 }
            int[] r4 = r10.mImsState     // Catch:{ all -> 0x0368 }
            r4[r2] = r5     // Catch:{ all -> 0x0368 }
            int[] r4 = r10.mExpectedImsState     // Catch:{ all -> 0x0368 }
            r4[r2] = r5     // Catch:{ all -> 0x0368 }
            int[] r4 = r10.mRegErrorCode     // Catch:{ all -> 0x0368 }
            r4[r2] = r5     // Catch:{ all -> 0x0368 }
            int[] r4 = r10.mRAN     // Catch:{ all -> 0x0368 }
            r4[r2] = r3     // Catch:{ all -> 0x0368 }
            int[] r4 = r10.mWfcPdnState     // Catch:{ all -> 0x0368 }
            r4[r2] = r5     // Catch:{ all -> 0x0368 }
            int[] r4 = r10.mWfcRegErrorCode     // Catch:{ all -> 0x0368 }
            r4[r2] = r5     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ImsEcbmProxy[] r4 = r10.mImsEcbm     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ImsEcbmProxy r6 = new com.mediatek.ims.ImsEcbmProxy     // Catch:{ all -> 0x0368 }
            android.content.Context r7 = r10.mContext     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ril.ImsCommandsInterface[] r8 = r10.mImsRILAdapters     // Catch:{ all -> 0x0368 }
            r8 = r8[r2]     // Catch:{ all -> 0x0368 }
            r6.<init>(r7, r8, r2)     // Catch:{ all -> 0x0368 }
            r4[r2] = r6     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ImsConfigManager r4 = r10.mImsConfigManager     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ril.ImsCommandsInterface[] r6 = r10.mImsRILAdapters     // Catch:{ all -> 0x0368 }
            r4.init(r2, r6)     // Catch:{ all -> 0x0368 }
            int[] r4 = r10.mIsImsEccSupported     // Catch:{ all -> 0x0368 }
            r4[r2] = r5     // Catch:{ all -> 0x0368 }
            boolean r4 = com.mediatek.ims.ImsCommonUtil.supportMdAutoSetupIms()     // Catch:{ all -> 0x0368 }
            if (r4 == 0) goto L_0x02b4
            java.util.HashMap<java.lang.Integer, com.mediatek.ims.MtkSuppServExt> r4 = sMtkSSExt     // Catch:{ all -> 0x0368 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.MtkSuppServExt r7 = new com.mediatek.ims.MtkSuppServExt     // Catch:{ all -> 0x0368 }
            android.content.Context r8 = r10.mContext     // Catch:{ all -> 0x0368 }
            android.os.Looper r9 = r10.mSslooper     // Catch:{ all -> 0x0368 }
            r7.<init>(r8, r2, r10, r9)     // Catch:{ all -> 0x0368 }
            r4.put(r6, r7)     // Catch:{ all -> 0x0368 }
        L_0x02b4:
            com.mediatek.ims.ImsEventPackageAdapter[] r4 = r10.mImsEvtPkgAdapter     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ImsEventPackageAdapter r6 = new com.mediatek.ims.ImsEventPackageAdapter     // Catch:{ all -> 0x0368 }
            android.content.Context r7 = r10.mContext     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r8 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r8 = r8[r2]     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ril.ImsCommandsInterface[] r9 = r10.mImsRILAdapters     // Catch:{ all -> 0x0368 }
            r9 = r9[r2]     // Catch:{ all -> 0x0368 }
            r6.<init>(r7, r8, r9, r2)     // Catch:{ all -> 0x0368 }
            r4[r2] = r6     // Catch:{ all -> 0x0368 }
            boolean[] r4 = r10.mWaitSubInfoChange     // Catch:{ all -> 0x0368 }
            r4[r2] = r5     // Catch:{ all -> 0x0368 }
            boolean[] r4 = r10.mVolteEnable     // Catch:{ all -> 0x0368 }
            r4[r2] = r5     // Catch:{ all -> 0x0368 }
            boolean[] r4 = r10.mIsPendingMTTerminated     // Catch:{ all -> 0x0368 }
            r4[r2] = r5     // Catch:{ all -> 0x0368 }
            boolean[] r4 = r10.mIsMTredirect     // Catch:{ all -> 0x0368 }
            r4[r2] = r5     // Catch:{ all -> 0x0368 }
            int[] r4 = r10.mWfcHandoverToLteState     // Catch:{ all -> 0x0368 }
            r4[r2] = r3     // Catch:{ all -> 0x0368 }
            int r2 = r2 + 1
            goto L_0x0244
        L_0x02df:
            boolean r2 = com.mediatek.ims.ImsCommonUtil.supportMims()     // Catch:{ all -> 0x0368 }
            r3 = 21
            if (r2 != 0) goto L_0x0321
            int r2 = com.mediatek.ims.ImsCommonUtil.getMainCapabilityPhoneId()     // Catch:{ all -> 0x0368 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0368 }
            r6.<init>()     // Catch:{ all -> 0x0368 }
            java.lang.String r7 = "getMainCapabilityPhoneId: mainPhoneId = "
            r6.append(r7)     // Catch:{ all -> 0x0368 }
            r6.append(r2)     // Catch:{ all -> 0x0368 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0368 }
            r10.log(r6)     // Catch:{ all -> 0x0368 }
            com.mediatek.ims.ril.ImsCommandsInterface[] r6 = r10.mImsRILAdapters     // Catch:{ all -> 0x0368 }
            r6 = r6[r2]     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r7 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r7 = r7[r2]     // Catch:{ all -> 0x0368 }
            android.os.Message r3 = r7.obtainMessage(r3)     // Catch:{ all -> 0x0368 }
            r6.setImsRegistrationReport(r3)     // Catch:{ all -> 0x0368 }
            if (r2 < r1) goto L_0x0320
            boolean r3 = com.mediatek.ims.ImsCommonUtil.supportMdAutoSetupIms()     // Catch:{ all -> 0x0368 }
            if (r3 != 0) goto L_0x0320
            boolean r3 = isValidPhoneId(r2)     // Catch:{ all -> 0x0368 }
            if (r3 == 0) goto L_0x0320
            r3 = 4
            r10.initImsAvailability(r2, r5, r4, r3)     // Catch:{ all -> 0x0368 }
        L_0x0320:
            goto L_0x0336
        L_0x0321:
            r2 = 0
        L_0x0322:
            if (r2 >= r12) goto L_0x0336
            com.mediatek.ims.ril.ImsCommandsInterface[] r4 = r10.mImsRILAdapters     // Catch:{ all -> 0x0368 }
            r4 = r4[r2]     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r5 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r5 = r5[r2]     // Catch:{ all -> 0x0368 }
            android.os.Message r5 = r5.obtainMessage(r3)     // Catch:{ all -> 0x0368 }
            r4.setImsRegistrationReport(r5)     // Catch:{ all -> 0x0368 }
            int r2 = r2 + 1
            goto L_0x0322
        L_0x0336:
            r2 = 0
        L_0x0337:
            if (r2 >= r12) goto L_0x034b
            android.os.Handler[] r3 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r4 = r3[r2]     // Catch:{ all -> 0x0368 }
            r3 = r3[r2]     // Catch:{ all -> 0x0368 }
            r5 = 33
            android.os.Message r3 = r3.obtainMessage(r5)     // Catch:{ all -> 0x0368 }
            r4.sendMessage(r3)     // Catch:{ all -> 0x0368 }
            int r2 = r2 + 1
            goto L_0x0337
        L_0x034b:
            com.mediatek.ims.ext.IImsServiceExt r2 = r10.getOpImsService()     // Catch:{ all -> 0x0368 }
            if (r2 == 0) goto L_0x0366
            r3 = r1
        L_0x0352:
            if (r3 >= r12) goto L_0x0366
            android.content.Context r4 = r10.mContext     // Catch:{ all -> 0x0368 }
            android.os.Handler[] r5 = r10.mHandler     // Catch:{ all -> 0x0368 }
            r5 = r5[r3]     // Catch:{ all -> 0x0368 }
            r6 = 39
            android.os.Message r5 = r5.obtainMessage(r6)     // Catch:{ all -> 0x0368 }
            r2.notifyImsServiceEvent(r3, r4, r5)     // Catch:{ all -> 0x0368 }
            int r3 = r3 + 1
            goto L_0x0352
        L_0x0366:
            monitor-exit(r0)     // Catch:{ all -> 0x0368 }
            return
        L_0x0368:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0368 }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.ImsService.notifyMultiSimConfigChanged(android.content.Context, int):void");
    }

    /* access modifiers changed from: private */
    public IImsServiceExt getOpImsService() {
        return OpImsServiceCustomizationUtils.getOpFactory(this.mContext).makeImsServiceExt(this.mContext);
    }

    /* access modifiers changed from: private */
    public void enableImsAdapter(int phoneId) {
        this.mImsAdapter.enableImsAdapter(phoneId);
    }

    private void disableImsAdapter(int phoneId, boolean isNormalDisable) {
        this.mImsAdapter.disableImsAdapter(phoneId, isNormalDisable);
    }

    private void startWfoService() {
        this.mTempDisableWFC = new boolean[this.mNumOfPhones];
        WfoService wService = WfoService.getInstance(this.mContext);
        if (wService != null) {
            wService.makeWfoService();
        } else {
            loge("startWfoService fail, getInstance is null");
        }
    }

    /* access modifiers changed from: protected */
    public int onOpen(int phoneId, int serviceClass, PendingIntent incomingCallIntent, IImsRegistrationListener listener) {
        log("onOpen: phoneId=" + phoneId + " serviceClass=" + serviceClass + " listener=" + listener);
        int serviceId = mapPhoneIdToServiceId(phoneId);
        StringBuilder sb = new StringBuilder();
        sb.append("onOpen: serviceId=");
        sb.append(serviceId);
        englog(sb.toString());
        return serviceId;
    }

    /* access modifiers changed from: protected */
    public void onClose(int serviceId) {
        synchronized (this.mLockObj) {
            int phoneId = serviceId;
            try {
                ImsEcbmProxy[] imsEcbmProxyArr = this.mImsEcbm;
                if (imsEcbmProxyArr[phoneId] != null) {
                    imsEcbmProxyArr[phoneId].getImsEcbm().setListener((IImsEcbmListener) null);
                }
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean onIsConnected(int serviceId, int serviceType, int callType) {
        log("onIsConnected: serviceId=" + serviceId + ", serviceType=" + serviceType + ", callType=" + callType);
        if (this.mImsRegInfo[serviceId] == 0) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean onIsOpened(int serviceId) {
        log("onIsOpened: serviceId=" + serviceId);
        if (this.mListener.get(serviceId).size() > 0) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void onSetRegistrationListener(int serviceId, IImsRegistrationListener listener) {
        log("onSetRegistrationListener: serviceId=" + serviceId + ", listener=" + listener);
    }

    public void onAddRegistrationListener(int phoneId, int serviceType, IImsRegistrationListener listener, IMtkImsRegistrationListener mtklistener, boolean notifyOnly) {
        if (!isValidPhoneId(phoneId)) {
            loge("onAddRegistrationListener() error phoneId:" + phoneId);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("onAddRegistrationListener: phoneId=");
        sb.append(phoneId);
        sb.append(" serviceType=");
        sb.append(serviceType);
        sb.append(" listener=");
        sb.append(listener);
        sb.append(" mtklistener= ");
        sb.append(mtklistener);
        sb.append(" notifyOnly= ");
        sb.append(notifyOnly ? "true" : "false");
        log(sb.toString());
        if (!notifyOnly) {
            HashSet<IImsRegistrationListener> listeners = this.mListener.get(phoneId);
            synchronized (listeners) {
                if (listeners.contains(listener)) {
                    log("listener already exist");
                } else {
                    listeners.add(listener);
                    log("listener set size=" + listeners.size());
                }
            }
        }
        int[] iArr = this.mImsRegInfo;
        if (iArr[phoneId] != 3) {
            notifyRegistrationStateChange(phoneId, iArr[phoneId], DBG);
        }
        if (this.mImsRegInfo[phoneId] == 0) {
            notifyRegistrationCapabilityChange(phoneId, this.mImsExtInfo[phoneId], DBG);
            notifyRegistrationAssociatedUriChange(ImsXuiManager.getInstance(), phoneId);
        }
        if (!notifyOnly) {
            HashSet<IMtkImsRegistrationListener> mtklisteners = this.mMtkListener.get(phoneId);
            synchronized (mtklisteners) {
                if (mtklisteners.contains(mtklistener)) {
                    log("mtklistener already exist");
                } else {
                    mtklisteners.add(mtklistener);
                    log("mtklistener set size=" + this.mMtkListener.size());
                }
            }
        }
        notifyImsRegInd(this.mImsRegInd[phoneId], mtklistener, phoneId);
    }

    public ImsCallProfile onCreateCallProfile(int serviceId, int serviceType, int callType) {
        return new ImsCallProfile(serviceType, callType);
    }

    public IImsCallSession onCreateCallSession(int serviceId, ImsCallProfile profile, IImsCallSessionListener listener) {
        return onCreateCallSessionProxy(serviceId, profile, listener).getServiceImpl();
    }

    public ImsCallSessionProxy onCreateCallSessionProxy(int serviceId, ImsCallProfile profile, IImsCallSessionListener listener) {
        log("onCreateCallSessionProxy: serviceId =" + serviceId + " profile =" + profile + " listener =" + listener);
        ImsCallSessionListener sessionListener = null;
        if (listener != null) {
            sessionListener = new ImsCallSessionListener(listener);
        }
        int phoneId = serviceId;
        ImsCallProfile imsCallProfile = profile;
        ImsCallSessionListener imsCallSessionListener = sessionListener;
        int i = phoneId;
        ImsCallSessionProxy imsCallSessionProxy = new ImsCallSessionProxy(this.mContext, imsCallProfile, imsCallSessionListener, this, this.mHandler[phoneId], this.mImsRILAdapters[phoneId], i);
        MtkImsCallSessionProxy mtk_cs = new MtkImsCallSessionProxy(this.mContext, imsCallProfile, imsCallSessionListener, this, this.mHandler[phoneId], this.mImsRILAdapters[phoneId], i);
        mtk_cs.setAospCallSessionProxy(imsCallSessionProxy);
        imsCallSessionProxy.setMtkCallSessionProxy(mtk_cs);
        log("onCreateCallSessionProxy: cs.getServiceImpl() = " + imsCallSessionProxy.getServiceImpl());
        this.mPendingMtkImsCallSessionProxy.put(imsCallSessionProxy.getServiceImpl(), mtk_cs);
        return imsCallSessionProxy;
    }

    public IMtkImsCallSession onCreateMtkCallSession(int phoneId, ImsCallProfile profile, IImsCallSessionListener listener, IImsCallSession aospCallSessionImpl) {
        return onCreateMtkCallSessionProxy(phoneId, profile, listener, aospCallSessionImpl).getServiceImpl();
    }

    public MtkImsCallSessionProxy onCreateMtkCallSessionProxy(int phoneId, ImsCallProfile profile, IImsCallSessionListener listener, IImsCallSession aospCallSessionImpl) {
        log("onCreateMtkCallSessionProxy: aospCallSessionImpl = " + aospCallSessionImpl);
        log("onCreateMtkCallSessionProxy: containsKey = " + this.mPendingMtkImsCallSessionProxy.containsKey(aospCallSessionImpl));
        if (!this.mPendingMtkImsCallSessionProxy.containsKey(aospCallSessionImpl)) {
            return null;
        }
        MtkImsCallSessionProxy mtk_cs = (MtkImsCallSessionProxy) this.mPendingMtkImsCallSessionProxy.get(aospCallSessionImpl);
        this.mPendingMtkImsCallSessionProxy.remove(aospCallSessionImpl);
        return mtk_cs;
    }

    public void cleanMtkCallSessionProxyIfNeed(ImsCallSessionProxy cs, boolean isMtCall, String callId, int phoneId) {
        log("cleanMtkCallSessionProxyIfNeed" + cs);
        if (isMtCall) {
            MtkImsCallSessionProxy[] mtkImsCallSessionProxyArr = this.mMtkPendingMT;
            if (mtkImsCallSessionProxyArr[phoneId] != null) {
                IMtkImsCallSession pendingMTsession = mtkImsCallSessionProxyArr[phoneId].getServiceImpl();
                log("cleanMtkCallSessionProxyIfNeed : mMtkPendingMT = " + this.mMtkPendingMT[phoneId] + ", pendingMTsession = " + pendingMTsession);
                try {
                    if (pendingMTsession.getCallId().equals(callId)) {
                        this.mMtkPendingMT[phoneId] = null;
                    }
                } catch (RemoteException e) {
                }
            }
        }
        IMtkImsCallSession pendingMTsession2 = cs.getServiceImpl();
        log("cleanMtkCallSessionProxyIfNeed : aospCallSessionImpl = " + pendingMTsession2);
        if (this.mPendingMtkImsCallSessionProxy.containsKey(pendingMTsession2)) {
            MtkImsCallSessionProxy mtk_cs = (MtkImsCallSessionProxy) this.mPendingMtkImsCallSessionProxy.get(pendingMTsession2);
            log("cleanMtkCallSessionProxyIfNeed : mtk_cs = " + mtk_cs);
            this.mPendingMtkImsCallSessionProxy.remove(pendingMTsession2);
            mtk_cs.setAospCallSessionProxy((ImsCallSessionProxy) null);
            cs.setMtkCallSessionProxy((MtkImsCallSessionProxy) null);
        }
    }

    /* access modifiers changed from: protected */
    public NafSessionKey onRunGbaAuthentication(String nafFqdn, byte[] nafSecureProtocolId, boolean forceRun, int netId, int phoneId) {
        if (!isValidPhoneId(phoneId)) {
            loge("onRunGbaAuthentication() error phoneId:" + phoneId + ", use phone 0");
            phoneId = 0;
        }
        NafSessionKeyResult result = new NafSessionKeyResult();
        Message msg = this.mHandler[phoneId].obtainMessage(23, result);
        synchronized (result.lockObj) {
            this.mImsRILAdapters[phoneId].runGbaAuthentication(nafFqdn, ImsCommonUtil.bytesToHex(nafSecureProtocolId), forceRun, netId, msg);
            try {
                result.lockObj.wait(10000);
            } catch (InterruptedException e) {
                loge("onRunGbaAuthentication() InterruptedException occured");
                result.cmdResult = 2;
            }
        }
        log("onRunGbaAuthentication complete, nafSessionKey:" + result.nafSessionKey + ", cmdResult:" + result.cmdResult);
        return result.nafSessionKey;
    }

    /* access modifiers changed from: protected */
    public IImsCallSession onGetPendingCallSession(int serviceId, String callId) {
        int phoneId = serviceId;
        if (phoneId >= this.mNumOfPhones || this.mPendingMT[phoneId] == null) {
            loge("onGetPendingCallSession() : no pendingMT or wrong phoneId " + phoneId);
            return null;
        }
        log("onGetPendingCallSession() : serviceId = " + serviceId + ", callId = " + callId + ", mPendingMT" + this.mPendingMT[phoneId]);
        IImsCallSession pendingMTsession = this.mPendingMT[phoneId].getServiceImpl();
        try {
            if (pendingMTsession.getCallId().equals(callId)) {
                this.mPendingMT[phoneId] = null;
                return pendingMTsession;
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public IImsUt onGetUtInterface(int phoneId) {
        if (ImsCommonUtil.supportMdAutoSetupIms()) {
            return ImsUtImpl.getInstance(this.mContext, phoneId, this).getInterface();
        }
        ImsUtImplBase utImpl = ExtensionFactory.makeLegacyComponentFactory(this.mContext).makeImsUt(this.mContext, phoneId, this);
        if (utImpl != null) {
            return utImpl.getInterface();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public IMtkImsUt onGetMtkUtInterface(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "onGetUtInterface");
        if (!isValidPhoneId(phoneId)) {
            loge("onGetMtkUtInterface() error phoneId:" + phoneId + ", use phone 0");
            phoneId = 0;
        }
        if (ImsCommonUtil.supportMdAutoSetupIms()) {
            return MtkImsUtImpl.getInstance(this.mContext, phoneId, this).getInterface();
        }
        MtkImsUtImplBase utImpl = ExtensionFactory.makeLegacyComponentFactory(this.mContext).makeMtkImsUt(this.mContext, phoneId, this);
        if (utImpl != null) {
            return utImpl.getInterface();
        }
        return null;
    }

    public IImsConfig onGetConfigInterface(int phoneId) {
        if (!isValidPhoneId(phoneId)) {
            loge("onGetConfigInterface() error phoneId:" + phoneId + ", use phone 0");
            phoneId = 0;
        }
        if (!ImsCommonUtil.supportMdAutoSetupIms()) {
            bindAndRegisterWifiOffloadService();
        } else {
            bindAndRegisterMWIService();
        }
        return this.mImsConfigManager.get(phoneId);
    }

    public void enableIms(int phoneId) {
        log("turnOnIms phoneId = " + phoneId);
        onTurnOnIms(phoneId);
    }

    /* access modifiers changed from: protected */
    public void onTurnOnIms(int phoneId) {
        log("turnOnIms phoneId = " + phoneId);
    }

    public void disableIms(int phoneId) {
        log("turnOffIms, phoneId = " + phoneId);
        onTurnOffIms(phoneId);
    }

    /* access modifiers changed from: protected */
    public void onTurnOffIms(int phoneId) {
        log("turnOffIms, phoneId = " + phoneId);
    }

    /* access modifiers changed from: protected */
    public IImsEcbm onGetEcbmInterface(int serviceId) {
        ImsEcbmImplBase imsEcbmImplBase = onGetEcbmProxy(serviceId);
        if (imsEcbmImplBase == null) {
            return null;
        }
        return imsEcbmImplBase.getImsEcbm();
    }

    public ImsEcbmImplBase onGetEcbmProxy(int serviceId) {
        int phoneId = serviceId;
        ImsEcbmProxy[] imsEcbmProxyArr = this.mImsEcbm;
        if (imsEcbmProxyArr[phoneId] == null) {
            imsEcbmProxyArr[phoneId] = new ImsEcbmProxy(this.mContext, this.mImsRILAdapters[phoneId], phoneId);
        }
        return this.mImsEcbm[phoneId];
    }

    /* access modifiers changed from: protected */
    public void onSetUiTTYMode(int serviceId, int uiTtyMode, Message onComplete) {
        log("onSetUiTTYMode: " + uiTtyMode);
        int i = serviceId;
    }

    /* access modifiers changed from: protected */
    public IImsMultiEndpoint onGetMultiEndpointInterface(int serviceId) {
        ImsMultiEndpointImplBase imsMultiendPoinImplBase = onGetMultiEndpointProxy(serviceId);
        if (imsMultiendPoinImplBase == null) {
            return null;
        }
        return imsMultiendPoinImplBase.getIImsMultiEndpoint();
    }

    public ImsMultiEndpointImplBase onGetMultiEndpointProxy(int serviceId) {
        int phoneId = serviceId;
        log("onGetMultiEndpointProxy phoneId is " + phoneId);
        if (phoneId >= this.mNumOfPhones) {
            return null;
        }
        ImsMultiEndpointProxy[] imsMultiEndpointProxyArr = this.mMultiEndpoints;
        if (imsMultiEndpointProxyArr[phoneId] == null) {
            imsMultiEndpointProxyArr[phoneId] = new ImsMultiEndpointProxy(this.mContext);
            log("onGetMultiEndpointProxy instance is " + this.mMultiEndpoints[phoneId]);
        }
        return this.mMultiEndpoints[phoneId];
    }

    /* access modifiers changed from: protected */
    public int getImsState(int phoneId) {
        if (!isValidPhoneId(phoneId)) {
            loge("getImsState() error phoneId:" + phoneId + ", use phone 0");
            phoneId = 0;
        }
        return this.mImsState[phoneId];
    }

    /* access modifiers changed from: protected */
    public int getImsRegUriType(int phoneId) {
        if (!isValidPhoneId(phoneId)) {
            loge("getImsRegUriType() error phoneId:" + phoneId + ", use phone 0");
            phoneId = 0;
        }
        int uri_type = 1;
        String key = PROPERTY_IMS_REG_EXTINFO + phoneId;
        if (this.mImsRegInfo[phoneId] == 0) {
            uri_type = SystemProperties.getInt(key, 1);
        }
        log("getImsRegUriType, phoneId = " + phoneId + "uri_type =" + uri_type);
        return uri_type;
    }

    /* access modifiers changed from: protected */
    public void onHangupAllCall(int phoneId) {
        if (!isValidPhoneId(phoneId)) {
            loge("onHangupAllCall() error phoneId:" + phoneId);
            return;
        }
        this.mImsRILAdapters[phoneId].hangupAllCall((Message) null);
    }

    /* access modifiers changed from: protected */
    public void deregisterIms(int phoneId) {
        if (!isValidPhoneId(phoneId)) {
            loge("deregisterIms() error phoneId:" + phoneId);
            return;
        }
        log("deregisterIms, phoneId = " + phoneId);
        if (!ImsCommonUtil.supportMims()) {
            phoneId = ImsCommonUtil.getMainCapabilityPhoneId();
            log("deregisterIms, MainCapabilityPhoneId = " + phoneId);
        }
        this.mImsRILAdapters[phoneId].deregisterIms(this.mHandler[phoneId].obtainMessage(16));
    }

    public void deregisterImsWithCause(int phoneId, int cause) {
        log("deregisterImsWithCause, phoneId = " + phoneId + " cause = " + cause);
        if (!ImsCommonUtil.supportMims()) {
            phoneId = ImsCommonUtil.getMainCapabilityPhoneId();
            log("deregisterImsWithCause, MainCapabilityPhoneId = " + phoneId);
        }
        this.mImsRILAdapters[phoneId].deregisterImsWithCause(cause, this.mHandler[phoneId].obtainMessage(16));
    }

    public void updateRadioState(int radioState, int phoneId) {
        if (!isValidPhoneId(phoneId)) {
            loge("updateRadioState() error phoneId:" + phoneId);
            return;
        }
        log("updateRadioState, phoneId = " + phoneId + " radioState = " + radioState);
        bindAndRegisterWifiOffloadService();
        IWifiOffloadService iWifiOffloadService = sWifiOffloadService;
        if (iWifiOffloadService != null) {
            try {
                iWifiOffloadService.updateRadioState(phoneId, radioState);
            } catch (RemoteException e) {
                loge("can't update radio state");
            }
        } else {
            loge("can't get WifiOffloadService");
        }
        if (!ImsCommonUtil.supportMdAutoSetupIms()) {
            return;
        }
        if (!ImsCommonUtil.isPhoneIdSupportIms(phoneId)) {
            log("updateRadioState() not support IMS, phoneId:" + phoneId);
            this.mImsConfigMccmnc[phoneId] = "";
            this.mImsConfigIccid[phoneId] = "";
            return;
        }
        boolean isAirPlaneMode = false;
        if (Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
            isAirPlaneMode = true;
        }
        if (!isAirPlaneMode) {
            int simState = SubscriptionManager.getSimStateForSlotIndex(phoneId);
            if (simState != 10) {
                SubscriptionManager subMgr = SubscriptionManager.from(this.mContext);
                SubscriptionInfo subInfo = null;
                if (subMgr != null) {
                    subInfo = subMgr.getActiveSubscriptionInfo(getSubIdUsingPhoneId(phoneId));
                }
                String iccid = subInfo != null ? subInfo.getIccId() : null;
                if ((!TextUtils.isEmpty(iccid) && isOp09SimCard(iccid)) || (TextUtils.isEmpty(iccid) && this.mWaitSubInfoChange[phoneId])) {
                    log("updateRadioState CT sim state isn't loaded, don't update.");
                    return;
                }
            }
            if (2 != radioState) {
                if (this.mHandler[phoneId].hasMessages(18)) {
                    this.mHandler[phoneId].removeMessages(18);
                }
                if (this.mHandler[phoneId].hasMessages(19)) {
                    this.mHandler[phoneId].removeMessages(19);
                }
                String currentMccmnc = OperatorUtils.getSimOperatorNumericForPhone(phoneId);
                if (this.mImsConfigMccmnc[phoneId] != null) {
                    englog("updateRadioState, mImsConfigMccmnc[phoneId]: " + this.mImsConfigMccmnc[phoneId] + ", currentMccmnc: " + currentMccmnc);
                } else {
                    englog("updateRadioState, mImsConfigMccmnc[phoneId] is null, currentMccmnc: " + currentMccmnc);
                }
                SubscriptionManager subMgr2 = SubscriptionManager.from(this.mContext);
                SubscriptionInfo subInfo2 = null;
                String currentIccid = null;
                if (subMgr2 != null) {
                    subInfo2 = subMgr2.getActiveSubscriptionInfo(getSubIdUsingPhoneId(phoneId));
                }
                if (subInfo2 != null) {
                    currentIccid = subInfo2.getIccId();
                }
                if (currentIccid == null) {
                    currentIccid = "";
                }
                if (this.mImsConfigIccid[phoneId] != null) {
                    englog("updateRadioState, mImsConfigIccid[phoneId]: " + Rlog.pii("ImsService", this.mImsConfigIccid[phoneId]) + ", currentIccid: " + Rlog.pii("ImsService", currentIccid));
                } else {
                    englog("updateRadioState, mImsConfigIccid[phoneId] is null, currentIccid: " + Rlog.pii("ImsService", currentIccid));
                }
                String[] strArr = this.mImsConfigMccmnc;
                if (strArr[phoneId] != null && !strArr[phoneId].equals("") && this.mImsConfigMccmnc[phoneId].compareTo(currentMccmnc) == 0) {
                    String[] strArr2 = this.mImsConfigIccid;
                    if (strArr2[phoneId] != null && !strArr2[phoneId].equals("") && this.mImsConfigIccid[phoneId].compareTo(currentIccid) == 0) {
                        return;
                    }
                }
                if (this.mImsManagerOemPlugin == null) {
                    this.mImsManagerOemPlugin = ExtensionFactory.makeOemPluginFactory(this.mContext).makeImsManagerPlugin(this.mContext);
                }
                this.mImsConfigMccmnc[phoneId] = currentMccmnc;
                this.mImsConfigIccid[phoneId] = currentIccid;
                if (simState != 10 && !CarrierConfigManager.getDefaultConfig().getBoolean("carrier_volte_available_bool")) {
                    return;
                }
                if (simState == 1 || getSubIdUsingPhoneId(phoneId) != -1) {
                    this.mImsManagerOemPlugin.updateImsServiceConfig(this.mContext, phoneId);
                } else {
                    log("updateRadioState sim is loading, don't update.");
                }
            }
        }
    }

    public int mapPhoneIdToServiceId(int phoneId) {
        return phoneId + 1;
    }

    public int getImsServiceState(int phoneId) {
        if (!ImsCommonUtil.supportMims()) {
            phoneId = ImsCommonUtil.getMainCapabilityPhoneId();
        }
        if (phoneId >= 0 && phoneId < this.mNumOfPhones) {
            return this.mImsRegInfo[phoneId];
        }
        loge("getImsServiceState, Invalid phoneId: " + phoneId);
        return 3;
    }

    public int getModemMultiImsCount() {
        log("getModemMultiImsCount");
        int mdMultiImsCount = SystemProperties.getInt(ImsConstants.PROPERTY_MD_MULTI_IMS_SUPPORT, -1);
        log("mdMultiImsCount=" + mdMultiImsCount);
        if (mdMultiImsCount == -1) {
            logw("MD Multi IMS Count not initialized");
        }
        return mdMultiImsCount;
    }

    private IWifiOffloadListenerProxy createWifiOffloadListenerProxy() {
        if (this.mProxy == null) {
            log("create WifiOffloadListenerProxy");
            this.mProxy = new IWifiOffloadListenerProxy();
        }
        return this.mProxy;
    }

    /* access modifiers changed from: private */
    public int mapToWfcRegErrorCause(int sipErrorCode, int sipMethod, String sipReasonText) {
        int wfcRegErrorCode = WfcReasonInfo.CODE_UNSPECIFIED;
        switch (sipErrorCode) {
            case 403:
                if (sipMethod != 9 || !sipReasonText.equals("SHOW_WIFI_REG09")) {
                    if (sipMethod != 0 || !sipReasonText.equals("WiFi Calling Not Allowed from this Region")) {
                        if (sipMethod != 0) {
                            wfcRegErrorCode = WfcReasonInfo.CODE_WFC_403_MISMATCH_IDENTITIES;
                            break;
                        } else {
                            wfcRegErrorCode = WfcReasonInfo.CODE_WFC_403_FORBIDDEN;
                            break;
                        }
                    } else {
                        wfcRegErrorCode = WfcReasonInfo.CODE_WFC_403_NOT_ALLOWED_FROM_THIS_REGION;
                        break;
                    }
                } else {
                    wfcRegErrorCode = WfcReasonInfo.CODE_WFC_911_MISSING;
                    break;
                }
                break;
            case 500:
                wfcRegErrorCode = WfcReasonInfo.CODE_WFC_INTERNAL_SERVER_ERROR;
                break;
            case RadioError.OEM_ERROR_3:
                if (sipMethod == 0 && sipReasonText.equals("Emergency Calls over Wi-Fi is not allowed in this location")) {
                    wfcRegErrorCode = WfcReasonInfo.CODE_WFC_503_ECC_OVER_WIFI_NOT_ALLOWED;
                    break;
                }
            case 606:
                if (sipMethod == 0 && sipReasonText.equals("Not Acceptable")) {
                    wfcRegErrorCode = WfcReasonInfo.CODE_WFC_606_WIFI_CALLING_IP_NOT_ACCEPTABLE;
                    break;
                }
            case 40301:
                wfcRegErrorCode = WfcReasonInfo.CODE_WFC_403_UNKNOWN_USER;
                break;
            case 40302:
                wfcRegErrorCode = WfcReasonInfo.CODE_WFC_403_ROAMING_NOT_ALLOWED;
                break;
            case 40303:
                wfcRegErrorCode = WfcReasonInfo.CODE_WFC_403_MISMATCH_IDENTITIES;
                break;
            case 40304:
                wfcRegErrorCode = WfcReasonInfo.CODE_WFC_403_AUTH_SCHEME_UNSUPPORTED;
                break;
            case 40305:
                wfcRegErrorCode = WfcReasonInfo.CODE_WFC_403_HANDSET_BLACKLISTED;
                break;
        }
        log("mapToWfcRegErrorCause(), sipErrorCode:" + sipErrorCode + " sipMethod:" + sipMethod + " sipReasonText: " + sipReasonText + " wfcRegErrorCode:" + wfcRegErrorCode);
        return wfcRegErrorCode;
    }

    private void handleWifiPdnOOS(int simIdx, int oosState) {
        log("handleWifiPdnOOS oosState= " + oosState);
        if (simIdx < 0 || simIdx >= this.mNumOfPhones) {
            loge("handleWifiPdnOOS, error Invalid simIdx: " + simIdx);
        }
        switch (oosState) {
            case 0:
                this.mTempDisableWFC[simIdx] = false;
                if (this.mWfcHandoverToLteState[simIdx] == -1) {
                    this.mRAN[simIdx] = 0;
                } else {
                    this.mRAN[simIdx] = 1;
                }
                int[] iArr = this.mImsExtInfo;
                iArr[simIdx] = iArr[simIdx] & -17;
                break;
            case 1:
                this.mTempDisableWFC[simIdx] = DBG;
                break;
            case 2:
                this.mTempDisableWFC[simIdx] = false;
                break;
        }
        notifyRegistrationStateChange(simIdx, this.mImsRegInfo[simIdx], false);
        notifyRegistrationCapabilityChange(simIdx, this.mImsExtInfo[simIdx], false);
    }

    private class IWifiOffloadListenerProxy extends WifiOffloadManager.Listener {
        private IWifiOffloadListenerProxy() {
        }

        public void onHandover(int simIdx, int stage, int ratType) {
            ImsService imsService = ImsService.this;
            imsService.log("onHandover simIdx=" + simIdx + ", stage=" + stage + ", ratType=" + ratType + ", mImsRegInfo[simIdx]" + ImsService.this.mImsRegInfo[simIdx]);
            if (stage == 1 && ImsService.this.mImsRegInfo[simIdx] == 0) {
                if (ratType > 3) {
                    ratType = 2;
                }
                ImsService.this.mRAN[simIdx] = ratType;
                if (ImsService.this.mWfcHandoverToLteState[simIdx] == -1) {
                    ImsService.this.mWfcHandoverToLteState[simIdx] = 1;
                }
                ImsService imsService2 = ImsService.this;
                imsService2.log("onHandover simIdx=" + simIdx + ", ratType=" + ratType + ", mImsExtInfo[simIdx]" + ImsService.this.mImsExtInfo[simIdx]);
                ImsService imsService3 = ImsService.this;
                imsService3.notifyRegistrationStateChange(simIdx, imsService3.mImsRegInfo[simIdx], false);
                ImsService imsService4 = ImsService.this;
                imsService4.notifyRegistrationCapabilityChange(simIdx, imsService4.mImsExtInfo[simIdx], false);
            }
            if ((stage == -1 || stage == 0) && ratType == 1) {
                ImsService.this.mWfcHandoverToLteState[simIdx] = -1;
            }
        }

        public void onWfcStateChanged(int simIdx, int state) {
            ImsService imsService = ImsService.this;
            imsService.log("onWfcStateChanged simIdx=" + simIdx + ", state=" + state);
            int[] access$2000 = ImsService.this.mWfcPdnState;
            int i = 1;
            if (state != 1) {
                i = 0;
            }
            access$2000[simIdx] = i;
        }

        public void onRequestImsSwitch(int simIdx, boolean isImsOn) {
            if (!ImsCommonUtil.supportMdAutoSetupIms()) {
                int mainCapabilityPhoneId = ImsCommonUtil.getMainCapabilityPhoneId();
                ImsService imsService = ImsService.this;
                imsService.log("onRequestImsSwitch simIdx=" + simIdx + " isImsOn=" + isImsOn + " mainCapabilityPhoneId=" + mainCapabilityPhoneId);
                if (simIdx >= ImsService.this.mNumOfPhones) {
                    ImsService.this.loge("onRequestImsSwitch can't enable/disable ims due to wrong sim id");
                }
                if (!ImsCommonUtil.supportMims() && simIdx != mainCapabilityPhoneId) {
                    ImsService.this.logw("onRequestImsSwitch, ignore not MainCapabilityPhoneId request");
                } else if (isImsOn) {
                    if (ImsService.this.mImsState[simIdx] != 1 || ImsService.this.mExpectedImsState[simIdx] == 0) {
                        ImsService.this.mImsRILAdapters[simIdx].turnOnIms(ImsService.this.mHandler[simIdx].obtainMessage(3));
                        ImsService.this.mExpectedImsState[simIdx] = 1;
                        ImsService.this.mImsState[simIdx] = 2;
                        return;
                    }
                    ImsService.this.log("Ims already enable and ignore to send AT command.");
                } else if (ImsService.this.mImsState[simIdx] != 0 || ImsService.this.mExpectedImsState[simIdx] == 1) {
                    ImsService.this.mImsRILAdapters[simIdx].turnOffIms(ImsService.this.mHandler[simIdx].obtainMessage(4));
                    ImsService.this.mExpectedImsState[simIdx] = 0;
                    ImsService.this.mImsState[simIdx] = 3;
                } else {
                    ImsService.this.log("Ims already disabled and ignore to send AT command.");
                }
            }
        }

        public void onWifiPdnOOSStateChanged(int simId, int oosState) {
            ImsService imsService = ImsService.this;
            imsService.log("onWifiPdnOOSStateChanged simIdx=" + simId + ", oosState=" + oosState);
            ImsService.this.notifyRegistrationOOSStateChanged(simId, oosState);
        }
    }

    public ImsCommandsInterface getImsRILAdapter(int phoneId) {
        if (this.mImsRILAdapters[phoneId] == null) {
            logw("getImsRILAdapter phoneId=" + phoneId + ", mImsRILAdapter is null ");
        }
        return this.mImsRILAdapters[phoneId];
    }

    public ImsConfigManager getImsConfigManager() {
        return this.mImsConfigManager;
    }

    private void checkAndBindWifiOffloadService() {
        IBinder b = ServiceManager.getService(WifiOffloadManager.WFO_SERVICE);
        if (b != null) {
            try {
                b.linkToDeath(this.mDeathRecipient, 0);
            } catch (RemoteException e) {
            }
            sWifiOffloadService = IWifiOffloadService.Stub.asInterface(b);
        } else {
            loge("can't get WifiOffloadService");
            IBinder b2 = ServiceManager.getService(MwisConstants.MWI_SERVICE);
            if (b2 != null) {
                try {
                    b2.linkToDeath(this.mDeathRecipient, 0);
                    IMwiService iMwiService = IMwiService.Stub.asInterface(b2);
                    if (iMwiService != null) {
                        sWifiOffloadService = iMwiService.getWfcHandlerInterface();
                    }
                } catch (RemoteException e2) {
                    Rlog.e("ImsService", "can't get MwiService", e2);
                }
            } else {
                log("No MwiService exist");
            }
        }
        log("checkAndBindWifiOffloadService: sWifiOffloadService = " + sWifiOffloadService);
    }

    private class IWifiOffloadServiceDeathRecipient implements IBinder.DeathRecipient {
        private IWifiOffloadServiceDeathRecipient() {
        }

        public void binderDied() {
            IWifiOffloadService unused = ImsService.sWifiOffloadService = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void bindAndRegisterWifiOffloadService() {
        if (sWifiOffloadService == null) {
            checkAndBindWifiOffloadService();
            IWifiOffloadService iWifiOffloadService = sWifiOffloadService;
            if (iWifiOffloadService != null) {
                try {
                    iWifiOffloadService.registerForHandoverEvent(createWifiOffloadListenerProxy());
                } catch (RemoteException e) {
                    loge("can't register handover event");
                }
            } else if (SystemProperties.getInt("persist.vendor.mtk_wfc_support", 0) == 1) {
                loge("can't get WifiOffloadService");
            }
        }
    }

    /* access modifiers changed from: private */
    public void bindAndRegisterMWIService() {
        bindAndRegisterWifiOffloadService();
    }

    private int getRadioTech(int phoneId) throws RemoteException {
        int radioTech;
        log("getRadioTech mRAN = " + this.mRAN[phoneId]);
        if (!ImsCommonUtil.supportMdAutoSetupIms()) {
            bindAndRegisterWifiOffloadService();
            IWifiOffloadService iWifiOffloadService = sWifiOffloadService;
            if (iWifiOffloadService != null) {
                this.mRAN[phoneId] = iWifiOffloadService.getRatType(phoneId);
            }
        } else {
            bindAndRegisterMWIService();
        }
        switch (this.mRAN[phoneId]) {
            case 2:
                radioTech = 18;
                break;
            default:
                radioTech = 14;
                break;
        }
        log("getRadioTech mRAN=" + this.mRAN[phoneId] + ", radioTech=" + radioTech);
        return radioTech;
    }

    private ImsReasonInfo createImsReasonInfo(int phoneId) {
        int[] iArr = this.mRegErrorCode;
        return new ImsReasonInfo(1000, iArr[phoneId], Integer.toString(iArr[phoneId]));
    }

    /* access modifiers changed from: protected */
    public void onUpdateImsSate(int phoneId) {
        if (!isValidPhoneId(phoneId)) {
            loge("onUpdateImsSate() error phoneId:" + phoneId);
            return;
        }
        log("request onUpdateImsSate for ImsManager add local registrant");
        int[] iArr = this.mImsRegInfo;
        if (iArr[phoneId] != 3) {
            notifyRegistrationStateChange(phoneId, iArr[phoneId], false);
        }
        if (this.mImsRegInfo[phoneId] == 0) {
            ImsXuiManager xuiManager = ImsXuiManager.getInstance();
            notifyRegistrationCapabilityChange(phoneId, this.mImsExtInfo[phoneId], false);
            notifyRegistrationAssociatedUriChange(xuiManager, phoneId);
        }
    }

    /* access modifiers changed from: private */
    public void notifyRegistrationAssociatedUriChange(ImsXuiManager xuiManager, int phoneId) {
        Uri[] uris = xuiManager.getSelfIdentifyUri(phoneId);
        log("notifyRegistrationAssociatedUriChange phoneId=" + phoneId);
        englog("uris=" + Rlog.pii("ImsService", uris));
        HashSet<IImsRegistrationListener> listeners = this.mListener.get(phoneId);
        if (!(listeners == null || uris == null)) {
            synchronized (listeners) {
                listeners.forEach(new ImsService$$ExternalSyntheticLambda5(this, uris));
            }
        }
        updateAssociatedUriChanged(phoneId, uris);
    }

    public /* synthetic */ void lambda$notifyRegistrationAssociatedUriChange$0$ImsService(Uri[] uris, IImsRegistrationListener l) {
        try {
            l.registrationAssociatedUriChanged(uris);
        } catch (RemoteException e) {
            loge("handle self identify update failed!!");
        }
    }

    private void updateAssociatedUriChanged(int slotId, Uri[] uris) {
        synchronized (this.mLockUri) {
            MtkImsRegistrationImpl imsReg = sMtkImsRegImpl.get(Integer.valueOf(slotId));
            if (imsReg != null) {
                try {
                    log("[" + slotId + "] updateAssociatedUriChanged");
                    StringBuilder sb = new StringBuilder();
                    sb.append("uris=");
                    sb.append(Rlog.pii("ImsService", uris));
                    englog(sb.toString());
                    imsReg.onSubscriberAssociatedUriChanged(uris);
                } catch (IllegalStateException e) {
                    loge("Failed to updateAssociatedUriChanged " + e);
                }
            } else {
                loge("There is not ImsRegistrationImpl for slot " + slotId);
            }
        }
    }

    private void updateImsRegstrationEx(int phoneId, int state, int tech, ImsReasonInfo imsReasonInfo) {
        HashSet<IImsRegistrationListener> listeners = this.mListener.get(phoneId);
        if (listeners != null) {
            synchronized (listeners) {
                if (state == 0) {
                    listeners.forEach(new ImsService$$ExternalSyntheticLambda1(this, tech));
                } else {
                    listeners.forEach(new ImsService$$ExternalSyntheticLambda0(imsReasonInfo));
                }
            }
        }
    }

    public /* synthetic */ void lambda$updateImsRegstrationEx$1$ImsService(int tech, IImsRegistrationListener l) {
        try {
            l.registrationConnectedWithRadioTech(tech);
        } catch (RemoteException e) {
            loge("IMS: l.registrationConnectedWithRadioTech failed");
        }
    }

    static /* synthetic */ void lambda$updateImsRegstrationEx$2(ImsReasonInfo imsReasonInfo, IImsRegistrationListener l) {
        try {
            l.registrationDisconnected(imsReasonInfo);
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: private */
    public void notifyRegistrationStateChange(int phoneId, int imsRegInfo, boolean staticReg) {
        synchronized (this.mLockObj) {
            log("notifyRegistrationStateChange imsRegInfo= " + imsRegInfo + ", phoneId=" + phoneId + ", staticReg=" + staticReg + ", mRAN[phoneId]=" + this.mRAN[phoneId]);
            HashSet hashSet = this.mListener.get(phoneId);
            if (imsRegInfo == 0) {
                try {
                    int radioTech = getRadioTech(phoneId);
                    setWfcRegErrorCodeWithPdn(phoneId, 0);
                    if (!staticReg) {
                        updateImsRegstration(phoneId, 2, convertImsRegistrationTech(radioTech), (ImsReasonInfo) null);
                    }
                    updateImsRegstrationEx(phoneId, imsRegInfo, convertImsRegistrationTech(radioTech), (ImsReasonInfo) null);
                    IImsServiceExt opImsService = getOpImsService();
                    if (opImsService != null) {
                        opImsService.notifyRegistrationStateChange(this.mRAN[phoneId], this.mHandler[phoneId], this.mImsRILAdapters[phoneId]);
                    }
                    this.mRegErrorCode[phoneId] = 0;
                } catch (RemoteException e) {
                    loge("IMS: notifyStateChange fail on access WifiOffloadService");
                }
            } else {
                setWfcRegErrorCodeWithPdn(phoneId, this.mRegErrorCode[phoneId]);
                ImsReasonInfo imsReasonInfo = createImsReasonInfo(phoneId);
                updateImsRegstration(phoneId, 3, -1, imsReasonInfo);
                updateImsRegstrationEx(phoneId, imsRegInfo, -1, imsReasonInfo);
            }
        }
    }

    private void updateCapabilityChange(int phoneId, int imsExtInfo, int[] enabledFeatures, int[] disabledFeatures) {
        log("updateCapabilityChange phoneId= " + phoneId + " + imsExtInfo: " + imsExtInfo);
        for (int i = 0; i < 6; i++) {
            enabledFeatures[i] = -1;
            disabledFeatures[i] = -1;
        }
        int[] iArr = this.mRAN;
        if (iArr[phoneId] == 1 && (imsExtInfo & 1) == 1) {
            enabledFeatures[0] = 0;
            enabledFeatures[4] = 4;
        } else {
            disabledFeatures[0] = 0;
            disabledFeatures[4] = 4;
        }
        if (iArr[phoneId] == 1 && (imsExtInfo & 8) == 8) {
            enabledFeatures[1] = 1;
        } else {
            disabledFeatures[1] = 1;
        }
        if (ImsCommonUtil.supportMdAutoSetupIms()) {
            if (this.mRAN[phoneId] == 2 && (imsExtInfo & 1) == 1 && !this.mTempDisableWFC[phoneId]) {
                enabledFeatures[2] = 2;
                enabledFeatures[5] = 5;
                log("[WFC]IMS_VOICE_OVER_WIFI");
            } else {
                disabledFeatures[2] = 2;
                disabledFeatures[5] = 5;
            }
        } else if (this.mRAN[phoneId] == 2 && (imsExtInfo & 1) == 1) {
            enabledFeatures[2] = 2;
            enabledFeatures[5] = 5;
            log("[WFC]IMS_VOICE_OVER_WIFI");
        } else {
            disabledFeatures[2] = 2;
            disabledFeatures[5] = 5;
        }
        if (this.mRAN[phoneId] == 2 && (imsExtInfo & 8) == 8) {
            enabledFeatures[3] = 3;
            log("[WFC]IMS_VIDEO_OVER_WIFI");
            return;
        }
        disabledFeatures[3] = 3;
    }

    private void notifyCapabilityChangedEx(int phoneId, int[] enabledFeatures, int[] disabledFeatures) {
        HashSet<IImsRegistrationListener> listeners = this.mListener.get(phoneId);
        if (listeners != null) {
            synchronized (listeners) {
                listeners.forEach(new ImsService$$ExternalSyntheticLambda7(enabledFeatures, disabledFeatures));
            }
        }
    }

    static /* synthetic */ void lambda$notifyCapabilityChangedEx$3(int[] enabledFeatures, int[] disabledFeatures, IImsRegistrationListener l) {
        try {
            l.registrationFeatureCapabilityChanged(1, enabledFeatures, disabledFeatures);
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: private */
    public void notifyRegistrationCapabilityChange(int phoneId, int imsExtInfo, boolean staticReg) {
        log("notifyRegistrationCapabilityChange imsExtInfo= " + imsExtInfo + ", phoneId=" + phoneId + ", staticReg=" + staticReg);
        int[] enabledFeatures = new int[6];
        int[] disabledFeatures = new int[6];
        updateCapabilityChange(phoneId, imsExtInfo, enabledFeatures, disabledFeatures);
        updateUtCapabilityChange(phoneId, enabledFeatures, disabledFeatures);
        MmTelFeature.MmTelCapabilities capabilities = convertCapabilities(enabledFeatures);
        if ((imsExtInfo & 4) == 4) {
            capabilities.addCapabilities(8);
        }
        if (!staticReg) {
            notifyCapabilityChanged(phoneId, capabilities);
        }
        notifyCapabilityChangedEx(phoneId, enabledFeatures, disabledFeatures);
    }

    public void notifyUtCapabilityChange(int phoneId) {
        log("notifyUtCapabilityChange, phoneId = " + phoneId);
        Handler[] handlerArr = this.mHandler;
        handlerArr[phoneId].sendMessage(handlerArr[phoneId].obtainMessage(29, phoneId, 0));
    }

    private void updateUtCapabilityChange(int phoneId, int[] enabledFeatures, int[] disabledFeatures) {
        if (sMtkSSExt.containsKey(Integer.valueOf(phoneId))) {
            int utCap = sMtkSSExt.get(Integer.valueOf(phoneId)).getUtCapabilityFromSettings();
            boolean z = false;
            if (OperatorUtils.isMatched(OperatorUtils.OPID.OP09, phoneId) && SystemProperties.getInt("persist.vendor.mtk_ct_volte_support", 0) != 0) {
                z = true;
            }
            boolean isUtDefaultEnabled = z;
            log("updateUtCapabilityChange, add Ut capability, utCap = " + utCap + ", isUtDefaultEnabled = " + isUtDefaultEnabled + ", phoneId = " + phoneId);
            if (utCap == 1 || (utCap == 0 && isUtDefaultEnabled)) {
                enabledFeatures[4] = 4;
            }
        }
    }

    private String eventToString(int eventId) {
        switch (eventId) {
            case 1:
                return "EVENT_IMS_REGISTRATION_INFO";
            case 2:
                return "EVENT_RADIO_NOT_AVAILABLE";
            case 3:
                return "EVENT_SET_IMS_ENABLED_DONE";
            case 4:
                return "EVENT_SET_IMS_DISABLE_DONE";
            case 5:
                return "EVENT_IMS_DISABLED_URC";
            case 6:
                return "EVENT_VIRTUAL_SIM_ON";
            case 7:
                return "EVENT_INCOMING_CALL_INDICATION";
            case 8:
                return "EVENT_CALL_INFO_INDICATION";
            case 10:
                return "EVENT_IMS_ENABLING_URC";
            case 11:
                return "EVENT_IMS_ENABLED_URC";
            case 12:
                return "EVENT_IMS_DISABLING_URC";
            case 13:
                return "EVENT_SIP_CODE_INDICATION";
            case 14:
                return "EVENT_SIP_CODE_INDICATION_DEREG";
            case 15:
                return "EVENT_ON_USSI";
            case 16:
                return "EVENT_IMS_DEREG_DONE";
            case 17:
                return "EVENT_IMS_DEREG_URC";
            case 18:
                return "EVENT_RADIO_OFF";
            case 19:
                return "EVENT_RADIO_ON";
            case 20:
                return "EVENT_IMS_RTP_INFO_URC";
            case 21:
                return "EVENT_SET_IMS_REGISTRATION_REPORT_DONE";
            case 22:
                return "EVENT_IMS_VOLTE_SETTING_URC";
            case 23:
                return "EVENT_RUN_GBA";
            case 24:
                return "EVENT_SELF_IDENTIFY_UPDATE";
            case 25:
                return "EVENT_IMS_SUPPORT_ECC_URC";
            case 26:
                return "EVENT_START_GBA_SERVICE";
            case 27:
                return "EVENT_INIT_CALL_SESSION_PROXY";
            case 28:
                return "EVENT_SEND_SMS_DONE";
            case 30:
                return "EVENT_IMS_SMS_STATUS_REPORT";
            case 31:
                return "EVENT_IMS_SMS_NEW_SMS";
            case 32:
                return "EVENT_IMS_SMS_NEW_CDMA_SMS";
            case 33:
                return "EVENT_READY_TO_RECEIVE_PENDING_IND";
            case 34:
                return "EVENT_VOPS_STATUS_IND";
            case 36:
                return "EVENT_CALL_ADDITIONAL_INFO_INDICATION";
            case 40:
                return "EVENT_IMS_REG_FLAG_IND";
            case 41:
                return "EVENT_IMS_REG_FLAG_IND_TIME_OUT";
            default:
                return "UNKNOWN EVENT: " + eventId;
        }
    }

    /* access modifiers changed from: private */
    public void setNotificationVirtual(int slot, int status) {
        String detail;
        int simId = slot + 1;
        int notificationId = simId;
        if (SystemProperties.getInt("ro.vendor.mtk_ims_notification", 0) == 1) {
            log("Show setNotificationVirtual(): slot = " + slot);
            NotificationChannel channel = new NotificationChannel("ImsService", IMSSERVICE_NOTIFICATION_NAME, 3);
            NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            if (status == 0) {
                detail = "IMS " + simId + " IN SERVICE";
            } else {
                detail = "IMS " + simId + " NOT IN SERVICE";
            }
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify("Volte Icon", notificationId, new Notification.Builder(this.mContext).setSmallIcon(17301642).setContentTitle("Volte status").setContentText(detail).setAutoCancel(false).setVisibility(1).setDefaults(-1).setChannelId("ImsService").build());
        }
    }

    private class MyHandler extends Handler {
        int mSocketId;

        public MyHandler(int socketId) {
            super((Handler.Callback) null, false);
            this.mSocketId = socketId;
        }

        public void handleMessage(Message msg) {
            int newImsRegInfo;
            ImsRegistrationOemPlugin imsRegOemPlugin;
            int resultEvent;
            Message message = msg;
            ImsRegistrationOemPlugin imsRegistrationOemPlugin = null;
            switch (message.what) {
                case 1:
                case 14:
                    if (hasMessages(14)) {
                        removeMessages(14);
                    }
                    if (hasMessages(41)) {
                        removeMessages(41);
                    }
                    AsyncResult ar = (AsyncResult) message.obj;
                    if (((int[]) ar.result)[0] == 1) {
                        newImsRegInfo = 0;
                    } else {
                        newImsRegInfo = 1;
                    }
                    if (SystemProperties.getInt("persist.vendor.ims.simulate", 0) == 1) {
                        newImsRegInfo = ImsService.this.mImsRegistry ? 0 : 1;
                        ImsService.this.log("handleMessage() : Override EVENT_IMS_REGISTRATION_INFO: newImsRegInfo=" + newImsRegInfo);
                    }
                    int newImsExtInfo = ((int[]) ar.result)[1];
                    if (ImsCommonUtil.supportMdAutoSetupIms()) {
                        if ((newImsExtInfo & 16) == 16) {
                            ImsService.this.mRAN[this.mSocketId] = 2;
                        } else {
                            ImsService.this.mRAN[this.mSocketId] = 1;
                        }
                    }
                    ImsService.this.log("handleMessage() : newReg:" + newImsRegInfo + " oldReg:" + ImsService.this.mImsRegInfo[this.mSocketId]);
                    ImsService.this.mWfcHandoverToLteState[this.mSocketId] = 1;
                    int[] access$1100 = ImsService.this.mImsRegInfo;
                    int i = this.mSocketId;
                    access$1100[i] = newImsRegInfo;
                    ImsService imsService = ImsService.this;
                    imsService.notifyRegistrationStateChange(i, imsService.mImsRegInfo[this.mSocketId], false);
                    ImsService.this.log("handleMessage() : newRegExt:" + newImsExtInfo + "oldRegExt:" + ImsService.this.mImsExtInfo[this.mSocketId]);
                    if (ImsService.this.mImsRegInfo[this.mSocketId] == 0) {
                        ImsService.this.mImsExtInfo[this.mSocketId] = newImsExtInfo;
                    } else {
                        ImsService.this.mImsExtInfo[this.mSocketId] = 0;
                    }
                    ImsService imsService2 = ImsService.this;
                    imsService2.notifyRegistrationCapabilityChange(this.mSocketId, imsService2.mImsExtInfo[this.mSocketId], false);
                    ServiceState ss = TelephonyManager.getDefault().getServiceStateForSubscriber(ImsService.this.getSubIdUsingPhoneId(this.mSocketId));
                    if (ss != null) {
                        int dataState = ss.getDataRegState();
                        int dataNetType = ss.getDataNetworkType();
                        ImsService.this.log("data=" + dataState + " , dataNetType=" + dataNetType);
                        int[] access$11002 = ImsService.this.mImsRegInfo;
                        int i2 = this.mSocketId;
                        if (access$11002[i2] != 0) {
                            ImsService imsService3 = ImsService.this;
                            imsService3.setNotificationVirtual(i2, imsService3.mImsRegInfo[this.mSocketId]);
                        } else if (dataState == 0) {
                            ImsService imsService4 = ImsService.this;
                            imsService4.setNotificationVirtual(i2, imsService4.mImsRegInfo[this.mSocketId]);
                        } else {
                            ImsService.this.setNotificationVirtual(i2, 1);
                        }
                    } else {
                        ImsService imsService5 = ImsService.this;
                        imsService5.setNotificationVirtual(this.mSocketId, imsService5.mImsRegInfo[this.mSocketId]);
                    }
                    boolean isVoWiFi = false;
                    if ((ImsService.this.mRAN[this.mSocketId] == 2 && (ImsService.this.mImsExtInfo[this.mSocketId] & 1) == 1) || (ImsService.this.mImsExtInfo[this.mSocketId] & 16) == 16) {
                        isVoWiFi = ImsService.DBG;
                    }
                    OemPluginFactory oemPlugin = ExtensionFactory.makeOemPluginFactory(ImsService.this.mContext);
                    if (oemPlugin != null) {
                        imsRegOemPlugin = oemPlugin.makeImsRegistrationPlugin(ImsService.this.mContext);
                    } else {
                        imsRegOemPlugin = null;
                    }
                    if (imsRegOemPlugin != null) {
                        imsRegOemPlugin.broadcastImsRegistration(this.mSocketId, ImsService.this.mImsRegInfo[this.mSocketId], isVoWiFi);
                        break;
                    }
                    break;
                case 2:
                    ImsService.this.disableIms(this.mSocketId, false);
                    break;
                case 3:
                    if (((AsyncResult) message.obj).exception != null) {
                        ImsService.this.logw("handleMessage() : turnOnIms failed, return to disabled state!");
                        ImsService.this.disableIms(this.mSocketId, false);
                        break;
                    }
                    break;
                case 5:
                    ImsService.this.log("handleMessage() : [Info]EVENT_IMS_DISABLED_URC: socketId = " + this.mSocketId + " ExpImsState = " + ImsService.this.mExpectedImsState[this.mSocketId] + " mImsState = " + ImsService.this.mImsState[this.mSocketId]);
                    ImsService.this.disableIms(this.mSocketId, ImsService.DBG);
                    break;
                case 7:
                    AsyncResult ar2 = (AsyncResult) message.obj;
                    boolean[] access$2900 = ImsService.this.mIsMTredirect;
                    int i3 = this.mSocketId;
                    if (!access$2900[i3]) {
                        ImsService.this.sendIncomingCallIndication(i3, ar2);
                    } else {
                        ImsService.this.notifyRedirectIncomingCall(i3, ar2);
                        AsyncResult unused = ImsService.this.mRedirectIncomingAsyncResult = ar2;
                        int unused2 = ImsService.this.mRedirectIncomingSocketId = this.mSocketId;
                    }
                    setRttModeForIncomingCall(ImsService.this.mImsRILAdapters[this.mSocketId]);
                    break;
                case 8:
                    String[] callInfo = (String[]) ((AsyncResult) message.obj).result;
                    int msgType = Integer.parseInt(callInfo[1]);
                    ImsService.this.log("handleMessage() : EVENT_CALL_INFO_INDICATION, msgType: " + msgType);
                    if (msgType == 133 && ImsService.this.mPendingMTCallId[this.mSocketId] != null && ImsService.this.mPendingMTCallId[this.mSocketId].equals(callInfo[0])) {
                        if (ImsService.this.mMtkPendingMT[this.mSocketId] == null) {
                            ImsService.this.mIsPendingMTTerminated[this.mSocketId] = ImsService.DBG;
                            break;
                        } else {
                            ImsService.this.log("handle 133 in ImsService");
                            IImsCallSession cs_impl = ImsService.this.mMtkPendingMT[this.mSocketId].getAospCallSessionProxy().getServiceImpl();
                            if (ImsService.this.mPendingMtkImsCallSessionProxy.containsKey(cs_impl)) {
                                ImsService.this.mPendingMtkImsCallSessionProxy.remove(cs_impl);
                            }
                            ImsService.this.mMtkPendingMT[this.mSocketId].callTerminated();
                            ImsService.this.mMtkPendingMT[this.mSocketId].setServiceImpl((IMtkImsCallSession) null);
                            ImsService.this.mMtkPendingMT[this.mSocketId] = null;
                            ImsService.this.mIsPendingMTTerminated[this.mSocketId] = false;
                            break;
                        }
                    }
                case 10:
                    ImsService.this.log("handleMessage() : [Info]receive EVENT_IMS_ENABLING_URC, socketId = " + this.mSocketId + " ExpImsState = " + ImsService.this.mExpectedImsState[this.mSocketId] + " mImsState = " + ImsService.this.mImsState[this.mSocketId]);
                    if (ImsService.this.mImsState[this.mSocketId] != 1) {
                        Intent intent = new Intent("com.android.ims.IMS_SERVICE_UP");
                        intent.putExtra("android:phone_id", this.mSocketId);
                        ImsService.this.mContext.sendBroadcast(intent);
                        ImsService.this.log("handleMessage() : broadcast IMS_SERVICE_UP");
                    }
                    if (!ImsCommonUtil.supportMdAutoSetupIms()) {
                        ImsService.this.enableImsAdapter(this.mSocketId);
                    } else if (ImsService.this.mHandler[this.mSocketId].hasMessages(10)) {
                        ImsService.this.mHandler[this.mSocketId].removeMessages(10);
                    }
                    ImsService.this.mImsState[this.mSocketId] = 1;
                    break;
                case 13:
                    String[] sipMessage = (String[]) ((AsyncResult) message.obj).result;
                    if (sipMessage != null) {
                        ImsService.this.log("handleMessage() : Method =" + sipMessage[3] + " response_code =" + sipMessage[4] + " reason_text =" + sipMessage[5]);
                        int sipMethod = Integer.parseInt(sipMessage[3]);
                        int sipResponseCode = Integer.parseInt(sipMessage[4]);
                        String sipReasonText = sipMessage[5];
                        if (sipMethod == 0 || sipMethod == 9) {
                            IImsServiceExt opImsService = ImsService.this.getOpImsService();
                            if (ImsService.this.mRAN[this.mSocketId] != 2 && (opImsService == null || !opImsService.isWfcRegErrorCauseSupported())) {
                                ImsService.this.mRegErrorCode[this.mSocketId] = sipResponseCode;
                                break;
                            } else {
                                ImsService.this.mRegErrorCode[this.mSocketId] = ImsService.this.mapToWfcRegErrorCause(sipResponseCode, sipMethod, sipReasonText);
                                if (ImsService.this.mRegErrorCode[this.mSocketId] == 1600 && sipMethod == 0 && ImsService.this.mImsRegInfo[this.mSocketId] != 0) {
                                    ImsService.this.log("handleMessage() : L-ePDG-5025 8-13. Received SIP REG 403 response, perform ImsDiscommect flow.");
                                    sendMessageDelayed(obtainMessage(14, new AsyncResult((Object) null, new int[]{0, 16}, (Throwable) null)), 1000);
                                }
                                if (ImsService.this.mRegErrorCode[this.mSocketId] == 1701 || ImsService.this.mRegErrorCode[this.mSocketId] == 1606) {
                                    ImsService imsService6 = ImsService.this;
                                    imsService6.notifyRegistrationErrorCode(this.mSocketId, imsService6.mRegErrorCode[this.mSocketId]);
                                    break;
                                }
                            }
                        }
                    }
                    break;
                case 15:
                    String[] eiusd = (String[]) ((AsyncResult) message.obj).result;
                    ImsService.this.log("EVENT_ON_USSI, m = " + eiusd[0] + ", str = " + eiusd[1]);
                    ImsCallProfile imsCallProfile = ImsService.this.onCreateCallProfile(1, 1, 2);
                    imsCallProfile.setCallExtraInt("dialstring", 2);
                    imsCallProfile.setCallExtra("m", eiusd[0]);
                    imsCallProfile.setCallExtra("str", eiusd[1]);
                    Context access$800 = ImsService.this.mContext;
                    ImsService imsService7 = ImsService.this;
                    Handler handler = imsService7.mHandler[this.mSocketId];
                    ImsCommandsInterface[] access$2300 = ImsService.this.mImsRILAdapters;
                    int i4 = this.mSocketId;
                    ImsCallSessionProxy imsCallSessionProxy = new ImsCallSessionProxy(access$800, imsCallProfile, (ImsCallSessionListener) null, imsService7, handler, access$2300[i4], "-1", i4);
                    Bundle ussiExtras = new Bundle();
                    ussiExtras.putBoolean("android:ussd", ImsService.DBG);
                    ussiExtras.putString("android:imsCallID", "-1");
                    ussiExtras.putInt("android:imsServiceId", ImsService.this.mapPhoneIdToServiceId(this.mSocketId));
                    ImsService.this.notifyIncomingCall(this.mSocketId, imsCallSessionProxy, ussiExtras);
                    break;
                case 17:
                    Intent intent2 = new Intent("com.android.ims.IMS_SERVICE_DEREGISTERED");
                    intent2.putExtra("android:phone_id", this.mSocketId);
                    ImsService.this.mContext.sendBroadcast(intent2);
                    break;
                case 18:
                    ImsService.this.updateRadioState(0, this.mSocketId);
                    break;
                case 19:
                    ImsService.this.updateRadioState(1, this.mSocketId);
                    break;
                case 22:
                    boolean enable = ((int[]) ((AsyncResult) message.obj).result)[0] == 1;
                    if (SubscriptionManager.getSimStateForSlotIndex(this.mSocketId) == 1 || ImsService.this.getSubIdUsingPhoneId(this.mSocketId) <= -1) {
                        if (!ImsService.this.mRegisterSubInfoChange) {
                            IntentFilter filter = new IntentFilter();
                            filter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
                            ImsService.this.mContext.registerReceiver(ImsService.this.mSubInfoReceiver, filter);
                            boolean unused3 = ImsService.this.mRegisterSubInfoChange = ImsService.DBG;
                        }
                        ImsService.this.mWaitSubInfoChange[this.mSocketId] = ImsService.DBG;
                    } else {
                        ImsService.this.mWaitSubInfoChange[this.mSocketId] = false;
                    }
                    ImsService.this.mVolteEnable[this.mSocketId] = enable;
                    if (ImsService.this.mWaitFeatureChange == 0) {
                        SystemProperties.set(ImsService.PROPERTY_IMSCONFIG_FORCE_NOTIFY, "1");
                        IntentFilter filter2 = new IntentFilter();
                        filter2.addAction(ImsConfigContract.ACTION_IMS_FEATURE_CHANGED);
                        ImsService.this.mContext.registerReceiver(ImsService.this.mFeatureValueReceiver, filter2);
                    }
                    ImsService imsService8 = ImsService.this;
                    int unused4 = imsService8.mWaitFeatureChange = imsService8.mWaitFeatureChange | (1 << this.mSocketId);
                    ImsService.this.setEnhanced4gLteModeSetting(this.mSocketId, enable);
                    ImsService.this.log("handleMessage() : Volte_Setting_Enable=" + enable + ", register:" + ImsService.this.mWaitSubInfoChange[this.mSocketId] + ", mWaitFeatureChange:" + ImsService.this.mWaitFeatureChange);
                    break;
                case 23:
                    ImsService.this.log("handleMessage() : receive EVENT_RUN_GBA: Enter messege");
                    AsyncResult ar3 = (AsyncResult) message.obj;
                    String[] nafInfoTemp = (String[]) ar3.result;
                    NafSessionKeyResult result = (NafSessionKeyResult) ar3.userObj;
                    synchronized (result.lockObj) {
                        if (ar3.exception != null) {
                            result.cmdResult = 3;
                            ImsService.this.log("handleMessage() : receive EVENT_RUN_GBA: IMS_SS_CMD_ERROR");
                        } else {
                            if (!ImsService.SENLOG) {
                                ImsService.this.log("handleMessage() : receive EVENT_RUN_GBA: hexkey:" + nafInfoTemp[0] + ", btid:" + nafInfoTemp[2] + ", keylifetime:" + nafInfoTemp[3]);
                            }
                            result.nafSessionKey = new NafSessionKey(nafInfoTemp[2], ImsCommonUtil.hexToBytes(nafInfoTemp[0]), nafInfoTemp[3]);
                            result.cmdResult = 4;
                            ImsService.this.log("handleMessage() : receive EVENT_RUN_GBA: IMS_SS_CMD_SUCCESS");
                        }
                        result.lockObj.notify();
                        ImsService.this.log("handleMessage() : receive EVENT_RUN_GBA: notify result");
                    }
                    break;
                case 24:
                    ImsXuiManager xuiManager = ImsXuiManager.getInstance();
                    if (ImsCommonUtil.supportMdAutoSetupIms()) {
                        String[] exui = (String[]) ((AsyncResult) message.obj).result;
                        if (!ImsService.SENLOG) {
                            ImsService.this.log("handleMessage() : XUI_INFO=" + Rlog.pii("ImsService", exui[2]));
                        }
                        xuiManager.setXui(this.mSocketId, exui[2]);
                    }
                    ImsService.this.notifyRegistrationAssociatedUriChange(xuiManager, this.mSocketId);
                    break;
                case 25:
                    int eccSupport = ((int[]) ((AsyncResult) message.obj).result)[0];
                    ImsService.this.log("receive EVENT_IMS_SUPPORT_ECC_URC, enable = " + eccSupport + " phoneId = " + this.mSocketId);
                    if (eccSupport == 0) {
                        int[] access$4900 = ImsService.this.mIsImsEccSupported;
                        int i5 = this.mSocketId;
                        access$4900[i5] = access$4900[i5] & -2;
                    } else if (eccSupport == 1) {
                        int[] access$49002 = ImsService.this.mIsImsEccSupported;
                        int i6 = this.mSocketId;
                        access$49002[i6] = access$49002[i6] | 1;
                    } else if (eccSupport == 2) {
                        int[] access$49003 = ImsService.this.mIsImsEccSupported;
                        int i7 = this.mSocketId;
                        access$49003[i7] = access$49003[i7] & -17;
                    } else if (eccSupport == 3) {
                        int[] access$49004 = ImsService.this.mIsImsEccSupported;
                        int i8 = this.mSocketId;
                        access$49004[i8] = 16 | access$49004[i8];
                    }
                    HashSet<IImsRegistrationListener> ecclisteners = (HashSet) ImsService.this.mListener.get(this.mSocketId);
                    if (ecclisteners != null) {
                        if (ImsService.this.mIsImsEccSupported[this.mSocketId] > 0) {
                            resultEvent = 2;
                        } else {
                            resultEvent = 4;
                        }
                        synchronized (ecclisteners) {
                            try {
                                Iterator<IImsRegistrationListener> it = ecclisteners.iterator();
                                while (it.hasNext()) {
                                    it.next().registrationServiceCapabilityChanged(1, resultEvent);
                                }
                            } catch (RemoteException e) {
                            }
                        }
                        break;
                    }
                    break;
                case 27:
                    ImsService.this.log("handleMessage() : Start init call session proxy");
                    Bundle b = msg.getData();
                    String callId = b.getString("callId");
                    int phoneId = b.getInt("phoneId");
                    int seqNum = b.getInt("seqNum");
                    MtkImsCallSessionProxy[] access$5200 = ImsService.this.mMtkPendingMT;
                    Context access$8002 = ImsService.this.mContext;
                    ImsCallProfile imsCallProfile2 = ImsService.this.mImsCallProfile[phoneId];
                    ImsService imsService9 = ImsService.this;
                    String str = callId;
                    int i9 = phoneId;
                    access$5200[phoneId] = new MtkImsCallSessionProxy(access$8002, imsCallProfile2, (ImsCallSessionListener) null, imsService9, imsService9.mHandler[phoneId], ImsService.this.mImsRILAdapters[phoneId], str, i9);
                    Context access$8003 = ImsService.this.mContext;
                    ImsCallProfile imsCallProfile3 = ImsService.this.mImsCallProfile[phoneId];
                    ImsService imsService10 = ImsService.this;
                    ImsCallSessionProxy imsCallSessionProxy2 = new ImsCallSessionProxy(access$8003, imsCallProfile3, (ImsCallSessionListener) null, imsService10, imsService10.mHandler[phoneId], ImsService.this.mImsRILAdapters[phoneId], str, i9);
                    ImsService.this.mMtkPendingMT[phoneId].setAospCallSessionProxy(imsCallSessionProxy2);
                    imsCallSessionProxy2.setMtkCallSessionProxy(ImsService.this.mMtkPendingMT[phoneId]);
                    ImsService.this.mPendingMtkImsCallSessionProxy.put(imsCallSessionProxy2.getServiceImpl(), ImsService.this.mMtkPendingMT[phoneId]);
                    ImsService.this.mImsRILAdapters[phoneId].unregisterForCallInfo(ImsService.this.mHandler[phoneId]);
                    ImsService.this.mImsRILAdapters[phoneId].setCallIndication(0, Integer.parseInt(callId), seqNum, 0);
                    if (ImsService.this.mIsPendingMTTerminated[this.mSocketId]) {
                        ImsService.this.log("handleMessage() : Start deal with pending 133");
                        IImsCallSession cs_impl2 = imsCallSessionProxy2.getServiceImpl();
                        if (ImsService.this.mPendingMtkImsCallSessionProxy.containsKey(cs_impl2)) {
                            ImsService.this.mPendingMtkImsCallSessionProxy.remove(cs_impl2);
                        }
                        ImsService.this.mMtkPendingMT[phoneId].callTerminated();
                        ImsService.this.mMtkPendingMT[phoneId].setServiceImpl((IMtkImsCallSession) null);
                        ImsService.this.mMtkPendingMT[phoneId] = null;
                        ImsService.this.mIsPendingMTTerminated[this.mSocketId] = false;
                        break;
                    }
                    break;
                case 28:
                    int phone_id = message.arg1;
                    int token = message.arg2;
                    int messageRef = 0;
                    AsyncResult ar4 = (AsyncResult) message.obj;
                    if (ar4.result != null) {
                        messageRef = ((MtkSmsResponse) ar4.result).mMessageRef;
                    } else {
                        ImsService.this.log("handleMessage() : MtkSmsResponse was null");
                    }
                    if (ar4.exception != null) {
                        ImsService.this.log("handleMessage() : SMS send failed");
                        int status = 2;
                        int reason = 1;
                        if (ar4.exception.getCommandError() == CommandException.Error.SMS_FAIL_RETRY) {
                            status = 4;
                        } else if (ar4.exception.getCommandError() == CommandException.Error.FDN_CHECK_FAILURE) {
                            reason = 6;
                        }
                        if (ImsService.mMmTelFeatureCallback.get(Integer.valueOf(phone_id)) != null) {
                            ((IMtkMmTelFeatureCallback) ImsService.mMmTelFeatureCallback.get(Integer.valueOf(phone_id))).sendSmsRsp(token, messageRef, status, reason);
                            break;
                        }
                    } else {
                        ImsService.this.log("handleMessage() : SMS send complete, messageRef: " + messageRef);
                        if (ImsService.mMmTelFeatureCallback.get(Integer.valueOf(phone_id)) != null) {
                            ((IMtkMmTelFeatureCallback) ImsService.mMmTelFeatureCallback.get(Integer.valueOf(phone_id))).sendSmsRsp(token, messageRef, 1, 0);
                            break;
                        }
                    }
                    break;
                case 29:
                    ImsService.this.log("receive EVENT_UT_CAPABILITY_CHANGE, phoneId = " + message.arg1);
                    ImsService.this.notifyRegistrationCapabilityChange(message.arg1, ImsService.this.mImsExtInfo[message.arg1], false);
                    break;
                case 30:
                    AsyncResult ar5 = (AsyncResult) message.obj;
                    if (ar5 != null) {
                        byte[] pdu = (byte[]) ar5.result;
                        ImsService.this.log("EVENT_IMS_SMS_STATUS_REPORT, mSocketId = " + this.mSocketId);
                        if (ImsService.mMmTelFeatureCallback.get(Integer.valueOf(this.mSocketId)) != null) {
                            ((IMtkMmTelFeatureCallback) ImsService.mMmTelFeatureCallback.get(Integer.valueOf(this.mSocketId))).newStatusReportInd(pdu, "3gpp");
                            break;
                        }
                    }
                    break;
                case 31:
                    AsyncResult ar6 = (AsyncResult) message.obj;
                    if (ar6 != null) {
                        byte[] pdu2 = (byte[]) ar6.result;
                        ImsService.this.log("EVENT_IMS_SMS_NEW_SMS, mSocketId = " + this.mSocketId);
                        if (ImsService.mMmTelFeatureCallback.get(Integer.valueOf(this.mSocketId)) != null) {
                            ((IMtkMmTelFeatureCallback) ImsService.mMmTelFeatureCallback.get(Integer.valueOf(this.mSocketId))).newImsSmsInd(pdu2, "3gpp");
                            break;
                        }
                    }
                    break;
                case 32:
                    if (!ImsService.this.handleNewCdmaSms((AsyncResult) message.obj, this.mSocketId)) {
                        ImsService.this.acknowledgeLastIncomingCdmaSms(this.mSocketId, false, 2);
                        break;
                    }
                    break;
                case 33:
                    ImsService.this.mImsRILAdapters[this.mSocketId].notifyImsServiceReady();
                    break;
                case 34:
                    AsyncResult ar7 = (AsyncResult) message.obj;
                    if (ar7 != null) {
                        if (ar7.result == null) {
                            ImsService.this.loge("receive EVENT_VOPS_STATUS_IND, ar.result is null,  phoneId = " + this.mSocketId);
                            break;
                        } else {
                            int vops = ((int[]) ar7.result)[0];
                            ImsService.this.log("receive EVENT_VOPS_STATUS_IND, vops = " + vops + " phoneId = " + this.mSocketId);
                            break;
                        }
                    } else {
                        ImsService.this.loge("receive EVENT_VOPS_STATUS_IND, ar is null,  phoneId = " + this.mSocketId);
                        break;
                    }
                case 36:
                    String[] incomingCallInfo = (String[]) ((AsyncResult) message.obj).result;
                    if (Integer.parseInt(incomingCallInfo[0]) == 100) {
                        ImsService.this.handleImsStkCall(this.mSocketId, incomingCallInfo);
                        break;
                    }
                    break;
                case 37:
                    ImsRegInfo info = (ImsRegInfo) ((AsyncResult) message.obj).result;
                    ImsRegInfo[] access$5800 = ImsService.this.mImsRegInd;
                    int i10 = this.mSocketId;
                    access$5800[i10] = info;
                    ImsService.this.notifyImsRegInd(info, (IMtkImsRegistrationListener) null, i10);
                    break;
                case 38:
                    ArrayList<Integer> info2 = (ArrayList) ((AsyncResult) message.obj).result;
                    int rat = -1;
                    if (info2.get(0).intValue() == 1) {
                        if (info2.get(5).intValue() == 1) {
                            rat = 1;
                        } else if (info2.get(5).intValue() <= 5) {
                            rat = 0;
                        } else {
                            rat = info2.get(5).intValue();
                        }
                    }
                    ImsService.this.log("receive EVENT_DETAIL_IMS_REGISTRATION_IND, phoneId = " + this.mSocketId + ", rat = " + rat);
                    ImsService.this.updateImsRegistrationRat(this.mSocketId, rat);
                    break;
                case 40:
                    if (hasMessages(41)) {
                        removeMessages(41);
                    }
                    if (((int[]) ((AsyncResult) message.obj).result)[0] == 1) {
                        if (ImsService.this.mImsRegInfo[this.mSocketId] != 0) {
                            ImsService.this.mImsRegInfo[this.mSocketId] = 0;
                            ImsService.this.mRAN[this.mSocketId] = 1;
                            ImsService.this.mImsExtInfo[this.mSocketId] = ImsService.this.mImsExtInfo[this.mSocketId] | 1;
                            ImsService.this.log("handleMessage(), EIMSUI Reg:" + ImsService.this.mImsRegInfo[this.mSocketId] + "," + ImsService.this.mImsExtInfo[this.mSocketId]);
                            ImsService imsService11 = ImsService.this;
                            imsService11.notifyRegistrationStateChange(this.mSocketId, imsService11.mImsRegInfo[this.mSocketId], false);
                            ImsService imsService12 = ImsService.this;
                            imsService12.notifyRegistrationCapabilityChange(this.mSocketId, imsService12.mImsExtInfo[this.mSocketId], false);
                            OemPluginFactory oemPlugin1 = ExtensionFactory.makeOemPluginFactory(ImsService.this.mContext);
                            if (oemPlugin1 != null) {
                                imsRegistrationOemPlugin = oemPlugin1.makeImsRegistrationPlugin(ImsService.this.mContext);
                            }
                            ImsRegistrationOemPlugin imsRegOemPlugin1 = imsRegistrationOemPlugin;
                            if (imsRegOemPlugin1 != null) {
                                imsRegOemPlugin1.broadcastImsRegistration(this.mSocketId, ImsService.this.mImsRegInfo[this.mSocketId], false);
                            }
                            sendMessageDelayed(obtainMessage(41), 10000);
                            break;
                        } else {
                            ImsService.this.logi("handleMessage(), EIMSUI ims already reg");
                            return;
                        }
                    } else {
                        ImsService.this.logi("handleMessage(), EIMSUI flag is not 1");
                        return;
                    }
                case 41:
                    ImsService.this.logi("handleMessage(), EIMSUI time out:" + ImsService.this.mImsRegInfo[this.mSocketId] + "," + ImsService.this.mImsExtInfo[this.mSocketId]);
                    if (ImsService.this.mImsRegInfo[this.mSocketId] == 0 && ImsService.this.mImsExtInfo[this.mSocketId] == 1) {
                        ImsService.this.mImsRegInfo[this.mSocketId] = 1;
                        int[] access$1700 = ImsService.this.mImsExtInfo;
                        int i11 = this.mSocketId;
                        access$1700[i11] = 0;
                        ImsService imsService13 = ImsService.this;
                        imsService13.notifyRegistrationStateChange(i11, imsService13.mImsRegInfo[this.mSocketId], false);
                        ImsService imsService14 = ImsService.this;
                        imsService14.notifyRegistrationCapabilityChange(this.mSocketId, imsService14.mImsExtInfo[this.mSocketId], false);
                        OemPluginFactory oemPlugin2 = ExtensionFactory.makeOemPluginFactory(ImsService.this.mContext);
                        if (oemPlugin2 != null) {
                            imsRegistrationOemPlugin = oemPlugin2.makeImsRegistrationPlugin(ImsService.this.mContext);
                        }
                        ImsRegistrationOemPlugin imsRegOemPlugin2 = imsRegistrationOemPlugin;
                        if (imsRegOemPlugin2 != null) {
                            imsRegOemPlugin2.broadcastImsRegistration(this.mSocketId, ImsService.this.mImsRegInfo[this.mSocketId], false);
                            break;
                        }
                    }
                    break;
            }
            IImsServiceExt opImsService2 = ImsService.this.getOpImsService();
            if (opImsService2 != null) {
                opImsService2.notifyImsServiceEvent(this.mSocketId, ImsService.this.mContext, message);
            }
        }

        private void setRttModeForIncomingCall(ImsCommandsInterface imsRILAdapter) {
            if (isRttSupported()) {
                ImsService.this.log("setRttModeForIncomingCall: mode = 2");
                if (ImsService.this.mImsRILAdapters[this.mSocketId] != null) {
                    ImsService.this.mImsRILAdapters[this.mSocketId].setRttMode(2, (Message) null);
                }
            }
        }

        private boolean isRttSupported() {
            return ((TelephonyManager) ImsService.this.mContext.getSystemService("phone")).isRttSupported();
        }
    }

    /* access modifiers changed from: private */
    public void handleImsStkCall(int phoneId, String[] incomingCallInfo) {
        int i = phoneId;
        String[] strArr = incomingCallInfo;
        String callId = strArr[1];
        String callNum = strArr[7];
        ImsCallProfile imsCallProfile = new ImsCallProfile();
        if (callNum != null && !callNum.equals("")) {
            log("setCallIndication new call profile: " + sensitiveEncode(callNum));
            imsCallProfile.setCallExtra("oi", callNum);
            imsCallProfile.setCallExtraInt("oir", 2);
        }
        ImsCallProfile imsCallProfile2 = imsCallProfile;
        String str = callId;
        MtkImsCallSessionProxy[] mtkImsCallSessionProxyArr = this.mMtkPendingMT;
        int i2 = phoneId;
        mtkImsCallSessionProxyArr[i] = new MtkImsCallSessionProxy(this.mContext, imsCallProfile2, (ImsCallSessionListener) null, this, this.mHandler[i], this.mImsRILAdapters[i], str, i2);
        ImsCallSessionProxy imsCallSessionProxy = new ImsCallSessionProxy(this.mContext, imsCallProfile2, (ImsCallSessionListener) null, this, this.mHandler[i], this.mImsRILAdapters[i], str, i2);
        this.mMtkPendingMT[i].setAospCallSessionProxy(imsCallSessionProxy);
        imsCallSessionProxy.setMtkCallSessionProxy(this.mMtkPendingMT[i]);
        this.mPendingMtkImsCallSessionProxy.put(imsCallSessionProxy.getServiceImpl(), this.mMtkPendingMT[i]);
        ImsServiceCallTracker.getInstance(phoneId).processCallInfoIndication((String[]) Arrays.copyOfRange(strArr, 1, strArr.length), imsCallSessionProxy, imsCallSessionProxy.getCallProfile());
        Bundle extras = new Bundle();
        extras.putString("android:imsCallID", callId);
        extras.putString("android:imsDialString", strArr[6]);
        extras.putInt("android:imsServiceId", i);
        extras.putBoolean("android:isUnknown", DBG);
        notifyIncomingCallSession(i, imsCallSessionProxy.getServiceImpl(), extras);
    }

    /* access modifiers changed from: private */
    public void disableIms(int phoneId, boolean isNormalDisable) {
        if (!ImsCommonUtil.supportMdAutoSetupIms()) {
            disableImsAdapter(phoneId, isNormalDisable);
        }
        this.mImsState[phoneId] = 0;
    }

    private void initImsAvailability(int phoneId, int capabilityOffset, int enableMessageId, int disableMessageId) {
        int volteCapability = SystemProperties.getInt(ImsConfigUtils.PROPERTY_VOLTE_ENALBE, 0);
        int wfcCapability = SystemProperties.getInt(ImsConfigUtils.PROPERTY_WFC_ENALBE, 0);
        if (((1 << capabilityOffset) & volteCapability) > 0 || ((1 << capabilityOffset) & wfcCapability) > 0) {
            log("initImsAvailability turnOnIms : " + phoneId);
            this.mImsRILAdapters[phoneId].turnOnIms(this.mHandler[phoneId].obtainMessage(enableMessageId));
            this.mImsState[phoneId] = 2;
        } else {
            log("initImsAvailability turnOffIms : " + phoneId);
            this.mImsRILAdapters[phoneId].turnOffIms(this.mHandler[phoneId].obtainMessage(disableMessageId));
            this.mImsState[phoneId] = 3;
        }
        updateRadioState(2, phoneId);
    }

    public int getRatType(int phoneId) {
        return this.mRAN[phoneId];
    }

    /* access modifiers changed from: private */
    public void log(String s) {
        Rlog.d("ImsService", s);
    }

    private void englog(String s) {
        if (ENGLOAD) {
            log(s);
        }
    }

    /* access modifiers changed from: private */
    public void logw(String s) {
        Rlog.w("ImsService", s);
    }

    /* access modifiers changed from: private */
    public void loge(String s) {
        Rlog.e("ImsService", s);
    }

    /* access modifiers changed from: private */
    public void logi(String s) {
        Rlog.i("ImsService", s);
    }

    /* access modifiers changed from: protected */
    public void onSetCallIndication(int phoneId, String callId, String callNum, int seqNum, String toNumber, boolean isAllow, int cause) {
        enforceModifyPermission();
        if (!isValidPhoneId(phoneId)) {
            loge("onSetCallIndication() error phoneId:" + phoneId);
            return;
        }
        AsyncResult asyncResult = this.mRedirectIncomingAsyncResult;
        if (asyncResult != null) {
            if (!callId.equals(((String[]) asyncResult.result)[0]) || seqNum != Integer.parseInt(((String[]) this.mRedirectIncomingAsyncResult.result)[4])) {
                loge("onSetCallIndication() error callId:" + callId + ", seqNum:" + seqNum);
                return;
            }
        } else if (!callId.equals(this.mPendingMTCallId[phoneId]) || seqNum != Integer.parseInt(this.mPendingMTSeqNum[phoneId])) {
            loge("onSetCallIndication() error callId:" + callId + ", seqNum:" + seqNum);
            return;
        } else {
            this.mPendingMTSeqNum[phoneId] = "";
        }
        setCallIndicationInternal(phoneId, callId, callNum, seqNum, toNumber, isAllow, cause);
    }

    private void setCallIndicationInternal(int phoneId, String callId, String callNum, int seqNum, String toNumber, boolean isAllow, int cause) {
        if (isAllow) {
            this.mImsCallProfile[phoneId] = new ImsCallProfile();
            if (callNum != null && !callNum.equals("")) {
                log("setCallIndication new call profile: " + sensitiveEncode(callNum));
                this.mImsCallProfile[phoneId].setCallExtra("oi", callNum);
                this.mImsCallProfile[phoneId].setCallExtraInt("oir", 2);
            }
            Message msg = this.mHandler[phoneId].obtainMessage(27);
            Bundle b = new Bundle();
            b.putString("callId", callId);
            b.putInt("phoneId", phoneId);
            b.putInt("seqNum", seqNum);
            msg.setData(b);
            this.mHandler[phoneId].sendMessage(msg);
            return;
        }
        this.mImsRILAdapters[phoneId].setCallIndication(1, Integer.parseInt(callId), seqNum, cause);
    }

    private void enforceModifyPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", (String) null);
    }

    /* access modifiers changed from: package-private */
    public IMtkImsCallSession onGetPendingMtkCallSession(int phoneId, String callId) {
        log("onGetPendingMtkCallSession() : callId = " + callId + ", mPendingMT = " + this.mMtkPendingMT[phoneId]);
        if (phoneId < this.mNumOfPhones) {
            MtkImsCallSessionProxy[] mtkImsCallSessionProxyArr = this.mMtkPendingMT;
            if (mtkImsCallSessionProxyArr[phoneId] != null) {
                IMtkImsCallSession pendingMTsession = mtkImsCallSessionProxyArr[phoneId].getServiceImpl();
                try {
                    if (pendingMTsession.getCallId().equals(callId)) {
                        ImsCallSessionProxy aospCallSession = this.mMtkPendingMT[phoneId].getAospCallSessionProxy();
                        log("onGetPendingMtkCallSession() : aospCallSession = " + aospCallSession);
                        if (aospCallSession != null) {
                            IImsCallSession aospCallSessionImpl = aospCallSession.getServiceImpl();
                            if (this.mPendingMtkImsCallSessionProxy.containsKey(aospCallSessionImpl)) {
                                this.mPendingMtkImsCallSessionProxy.remove(aospCallSessionImpl);
                            }
                        }
                        this.mMtkPendingMT[phoneId] = null;
                        return pendingMTsession;
                    }
                } catch (RemoteException e) {
                }
                return null;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00fe  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x011a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendIncomingCallIndication(int r23, android.os.AsyncResult r24) {
        /*
            r22 = this;
            r8 = r22
            r9 = r23
            r10 = r24
            boolean[] r0 = r8.mIsPendingMTTerminated
            r1 = 0
            r0[r9] = r1
            com.mediatek.ims.ril.ImsCommandsInterface[] r0 = r8.mImsRILAdapters
            r0 = r0[r9]
            android.os.Handler[] r2 = r8.mHandler
            r2 = r2[r9]
            r3 = 8
            r4 = 0
            r0.registerForCallInfo(r2, r3, r4)
            java.lang.Object r0 = r10.result
            java.lang.String[] r0 = (java.lang.String[]) r0
            r11 = r0[r1]
            java.lang.String[] r0 = r8.mPendingMTCallId
            r0[r9] = r11
            java.lang.Object r0 = r10.result
            java.lang.String[] r0 = (java.lang.String[]) r0
            r1 = 1
            r12 = r0[r1]
            java.lang.Object r0 = r10.result
            java.lang.String[] r0 = (java.lang.String[]) r0
            r2 = 3
            r13 = r0[r2]
            java.lang.Object r0 = r10.result
            java.lang.String[] r0 = (java.lang.String[]) r0
            r2 = 4
            r14 = r0[r2]
            java.lang.Object r0 = r10.result
            java.lang.String[] r0 = (java.lang.String[]) r0
            r2 = 6
            r15 = r0[r2]
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "sendIncomingCallIndication() : call_id = "
            r0.append(r2)
            r0.append(r11)
            java.lang.String r2 = " dialString = "
            r0.append(r2)
            java.lang.String r2 = r8.sensitiveEncode(r12)
            r0.append(r2)
            java.lang.String r2 = " seqNum = "
            r0.append(r2)
            r0.append(r14)
            java.lang.String r2 = " phoneId = "
            r0.append(r2)
            r0.append(r9)
            java.lang.String r0 = r0.toString()
            r8.log(r0)
            com.mediatek.ims.ImsServiceCallTracker r7 = com.mediatek.ims.ImsServiceCallTracker.getInstance(r23)
            r0 = 1
            java.lang.String r2 = com.mediatek.ims.ImsConstants.PROPERTY_TBCW_MODE
            java.lang.String r3 = com.mediatek.ims.ImsConstants.TBCW_DISABLED
            java.lang.String r6 = android.telephony.TelephonyManager.getTelephonyProperty(r9, r2, r3)
            java.lang.String r2 = com.mediatek.ims.ImsConstants.TBCW_OFF
            boolean r2 = r6.equals(r2)
            if (r2 != r1) goto L_0x0090
            boolean r1 = r7.isInCall()
            if (r1 == 0) goto L_0x0090
            java.lang.String r1 = "sendIncomingCallIndication() : PROPERTY_TBCW_MODE = TBCW_OFF. Reject the call as UDUB "
            r8.log(r1)
            r0 = 0
        L_0x0090:
            boolean r1 = com.mediatek.ims.ImsServiceCallTracker.isEccExistOnAnySlot()
            if (r1 == 0) goto L_0x009c
            java.lang.String r1 = "sendIncomingCallIndication() : there is an ECC call, dis-allow this incoming call!"
            r8.log(r1)
            r0 = 0
        L_0x009c:
            java.lang.String r1 = "allow_hold_video_call_bool"
            boolean r16 = r8.getBooleanFromCarrierConfig(r9, r1)
            if (r16 != 0) goto L_0x00cc
            java.lang.String r1 = "sendIncomingCallIndication() : OP01 or OP09 case"
            r8.log(r1)
            boolean r1 = r7.isVideoCallExist()
            if (r1 == 0) goto L_0x00b6
            java.lang.String r1 = "sendIncomingCallIndication() : there is video calls, dis-allow this incoming call!"
            r8.log(r1)
            r0 = 0
            goto L_0x00cc
        L_0x00b6:
            int r1 = java.lang.Integer.parseInt(r13)
            boolean r1 = r7.isVideoCall(r1)
            if (r1 == 0) goto L_0x00cc
            boolean r1 = r7.isInCall()
            if (r1 == 0) goto L_0x00cc
            java.lang.String r1 = "sendIncomingCallIndication() : MT is video calls during call, dis-allow this incoming call!"
            r8.log(r1)
            r0 = 0
        L_0x00cc:
            com.mediatek.ims.OperatorUtils$OPID r1 = com.mediatek.ims.OperatorUtils.OPID.OP129
            boolean r1 = com.mediatek.ims.OperatorUtils.isMatched(r1, r9)
            if (r1 == 0) goto L_0x00e7
            java.lang.String r1 = "sendIncomingCallIndication() : OP129 case"
            r8.log(r1)
            boolean r1 = r7.isConferenceHostCallExist()
            if (r1 == 0) goto L_0x00e7
            java.lang.String r1 = "sendIncomingCallIndication() : there is conference call, dis-allow this incoming call!"
            r8.log(r1)
            r0 = 0
            r5 = r0
            goto L_0x00e8
        L_0x00e7:
            r5 = r0
        L_0x00e8:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "sendIncomingCallIndication() : isAllow = "
            r0.append(r1)
            r0.append(r5)
            java.lang.String r0 = r0.toString()
            r8.log(r0)
            if (r5 != 0) goto L_0x011a
            int r4 = java.lang.Integer.parseInt(r14)
            r17 = -1
            r0 = r22
            r1 = r23
            r2 = r11
            r3 = r12
            r18 = r5
            r5 = r15
            r19 = r6
            r6 = r18
            r20 = r7
            r7 = r17
            r0.setCallIndicationInternal(r1, r2, r3, r4, r5, r6, r7)
            goto L_0x018e
        L_0x011a:
            r18 = r5
            r19 = r6
            r20 = r7
            java.lang.String r0 = "mtk_support_enhanced_call_blocking_bool"
            boolean r7 = r8.getBooleanFromCarrierConfig(r9, r0)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "sendIncomingCallIndication() : needCheckEnhanceCallBlacking = "
            r0.append(r1)
            r0.append(r7)
            java.lang.String r0 = r0.toString()
            r8.log(r0)
            if (r7 == 0) goto L_0x0178
            android.content.Intent r0 = new android.content.Intent
            java.lang.String r1 = "com.android.ims.IMS_INCOMING_CALL_INDICATION"
            r0.<init>(r1)
            java.lang.String r1 = "com.android.phone"
            r0.setPackage(r1)
            java.lang.String r1 = "android:imsCallID"
            r0.putExtra(r1, r11)
            java.lang.String r1 = "android:imsDialString"
            r0.putExtra(r1, r12)
            int r1 = java.lang.Integer.parseInt(r13)
            java.lang.String r2 = "android:imsCallMode"
            r0.putExtra(r2, r1)
            int r1 = java.lang.Integer.parseInt(r14)
            java.lang.String r2 = "android:imsSeqNum"
            r0.putExtra(r2, r1)
            java.lang.String r1 = "android:phoneId"
            r0.putExtra(r1, r9)
            java.lang.String r1 = "mediatek:mtToNumber"
            r0.putExtra(r1, r15)
            java.lang.String[] r1 = r8.mPendingMTSeqNum
            r1[r9] = r14
            android.content.Context r1 = r8.mContext
            r1.sendBroadcast(r0)
            goto L_0x018e
        L_0x0178:
            int r4 = java.lang.Integer.parseInt(r14)
            r17 = -1
            r0 = r22
            r1 = r23
            r2 = r11
            r3 = r12
            r5 = r15
            r6 = r18
            r21 = r7
            r7 = r17
            r0.setCallIndicationInternal(r1, r2, r3, r4, r5, r6, r7)
        L_0x018e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.ImsService.sendIncomingCallIndication(int, android.os.AsyncResult):void");
    }

    /* access modifiers changed from: package-private */
    public int getCurrentCallCount(int phoneId) {
        if (phoneId >= 0 && phoneId <= this.mNumOfPhones - 1) {
            return ImsServiceCallTracker.getInstance(phoneId).getCurrentCallCount();
        }
        log("IMS: getCurrentCallCount() phoneId: " + phoneId);
        return 0;
    }

    public boolean isImsEccSupported(int phoneId) {
        if (this.mIsImsEccSupported[phoneId] > 0) {
            return DBG;
        }
        return false;
    }

    public boolean isImsEccSupportedWhenNormalService(int phoneId) {
        if ((this.mIsImsEccSupported[phoneId] & 16) > 0) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void resetXuiAndNotify(int phoneId) {
        log("resetXuiAndNotify() phoneId: " + phoneId);
        ImsXuiManager.getInstance().setXui(phoneId, (String) null);
        Uri[] uris = {Uri.parse("")};
        HashSet<IImsRegistrationListener> listeners = this.mListener.get(phoneId);
        if (listeners != null) {
            synchronized (listeners) {
                listeners.forEach(new ImsService$$ExternalSyntheticLambda6(this, uris));
            }
        }
        updateAssociatedUriChanged(phoneId, (Uri[]) null);
    }

    public /* synthetic */ void lambda$resetXuiAndNotify$4$ImsService(Uri[] uris, IImsRegistrationListener l) {
        try {
            l.registrationAssociatedUriChanged(uris);
        } catch (RemoteException e) {
            loge("clear self identify failed!!");
        }
    }

    public int[] getImsNetworkState(int capability) {
        return this.mImsDataTracker.getImsNetworkState(capability);
    }

    /* access modifiers changed from: protected */
    public void onAddImsSmsListener(int phoneId, IImsSmsListener listener) {
        if (!isValidPhoneId(phoneId)) {
            loge("onAddImsSmsListener() error phoneId:" + phoneId);
            return;
        }
        log("onAddImsSmsListener: phoneId=" + phoneId + " listener=" + listener);
        HashSet<IImsSmsListener> listeners = this.mImsSmsListener.get(phoneId);
        synchronized (listeners) {
            if (!listeners.isEmpty()) {
                listeners.clear();
            }
            listeners.add(listener);
            log("IMS SMS listener set size=" + listeners.size());
        }
    }

    public void sendSms(int phoneId, int token, int messageRef, String format, String smsc, boolean isRetry, byte[] pdu) {
        int i = phoneId;
        int i2 = token;
        if (!isValidPhoneId(phoneId)) {
            loge("sendSms() error phoneId:" + phoneId);
            return;
        }
        log("sendSms, token " + token + ", messageRef " + messageRef);
        this.mImsRILAdapters[i].sendSms(token, messageRef, format, smsc, isRetry, pdu, this.mHandler[i].obtainMessage(28, phoneId, token));
    }

    public void acknowledgeLastIncomingGsmSms(int phoneId, boolean success, int cause) {
        log("acknowledgeLastIncomingGsmSms, success " + success + ", cause " + cause);
        this.mImsRILAdapters[phoneId].acknowledgeLastIncomingGsmSms(success, cause, (Message) null);
    }

    public void acknowledgeLastIncomingCdmaSms(int phoneId, boolean success, int cause) {
        log("acknowledgeLastIncomingCdmaSms, success " + success + ", cause " + cause);
        this.mImsRILAdapters[phoneId].acknowledgeLastIncomingCdmaSmsEx(success, cause, (Message) null);
    }

    /* access modifiers changed from: private */
    public boolean handleNewCdmaSms(AsyncResult ar, int socketId) {
        if (ar.exception != null) {
            loge("Exception processing incoming SMS: " + ar.exception);
            return false;
        }
        SmsMessage sms = (SmsMessage) ar.result;
        if (sms == null) {
            loge("SmsMessage is null");
            return false;
        }
        SmsMessageBase smsb = sms.mWrappedSmsMessage;
        if (smsb == null) {
            loge("SmsMessageBase is null");
            return false;
        } else if (mMmTelFeatureCallback == null) {
            loge("mMmTelFeatureCallback is null");
            return false;
        } else {
            boolean statusReport = false;
            com.android.internal.telephony.cdma.SmsMessage cdmaSms = (com.android.internal.telephony.cdma.SmsMessage) smsb;
            if (cdmaSms.getMessageType() == 0) {
                try {
                    cdmaSms.parseSms();
                    if (cdmaSms.isStatusReportMessage()) {
                        statusReport = true;
                    }
                } catch (RuntimeException ex) {
                    loge("Exception dispatching message: " + ex);
                    return false;
                }
            }
            if (statusReport) {
                log("EVENT_IMS_SMS_STATUS_REPORT, socketId = " + socketId);
                mMmTelFeatureCallback.get(Integer.valueOf(socketId)).newStatusReportInd(smsb.getPdu(), "3gpp2");
                return DBG;
            }
            log("EVENT_IMS_SMS_NEW_SMS, socketId = " + socketId);
            mMmTelFeatureCallback.get(Integer.valueOf(socketId)).newImsSmsInd(smsb.getPdu(), "3gpp2");
            return DBG;
        }
    }

    public void explicitCallTransfer(int phoneId, Message result, Messenger target) {
        log("explicitCallTransfer: phoneId " + phoneId);
        ImsCallSessionProxy fgCallSession = ImsServiceCallTracker.getInstance(phoneId).getFgCall();
        if (fgCallSession != null) {
            fgCallSession.explicitCallTransferWithCallback(result, target);
        } else if (result != null && target != null) {
            result.arg1 = 0;
            try {
                target.send(result);
            } catch (RemoteException e) {
                log(e.toString());
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyRegistrationOOSStateChanged(int simId, int oosState) {
        handleWifiPdnOOS(simId, oosState);
        HashSet<IImsRegistrationListener> listeners = this.mListener.get(simId);
        if (listeners == null) {
            log("notifyRegistrationOOSStateChanged: listeners is null");
            return;
        }
        synchronized (listeners) {
            int resultEvent = 5;
            switch (oosState) {
                case 0:
                    resultEvent = 6;
                    break;
                case 1:
                    resultEvent = 5;
                    break;
                case 2:
                    resultEvent = 7;
                    break;
            }
            try {
                log("notifyRegistrationOOSStateChanged listener size: " + listeners.size());
                Iterator<IImsRegistrationListener> it = listeners.iterator();
                while (it.hasNext()) {
                    log("call registrationServiceCapabilityChanged with event: " + resultEvent);
                    it.next().registrationServiceCapabilityChanged(1, resultEvent);
                }
            } catch (RemoteException e) {
                log(e.toString());
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyRegistrationErrorCode(int phoneId, int errorCode) {
        HashSet<IMtkImsRegistrationListener> mtklisteners = this.mMtkListener.get(phoneId);
        if (mtklisteners == null) {
            log("notifyRegistrationErrorCode: listeners is null");
            return;
        }
        synchronized (mtklisteners) {
            mtklisteners.forEach(new ImsService$$ExternalSyntheticLambda2(this, errorCode));
        }
    }

    public /* synthetic */ void lambda$notifyRegistrationErrorCode$5$ImsService(int errorCode, IMtkImsRegistrationListener l) {
        try {
            log("call notifyRegistrationErrorCode with error: " + errorCode);
            l.onRegistrationErrorCodeIndication(errorCode);
        } catch (RemoteException e) {
            loge("notifyRegistrationErrorCode failed!!");
        }
    }

    public void setRcsFeatureCallback(int phoneId, IMtkRcsFeatureCallback rcsCallback) {
        mRcsFeatureCallback.remove(Integer.valueOf(phoneId));
        if (rcsCallback != null && isValidPhoneId(phoneId)) {
            mRcsFeatureCallback.put(Integer.valueOf(phoneId), rcsCallback);
            rcsCallback.notifyContextChanged(this.mContext);
            RcsFeature.RcsImsCapabilities capabilities = new RcsFeature.RcsImsCapabilities(2);
            capabilities.addCapabilities(2);
            notifyRcsCapabilityChanged(phoneId, capabilities);
        }
    }

    public void setMmTelFeatureCallback(int phoneId, IMtkMmTelFeatureCallback c) {
        mMmTelFeatureCallback.remove(Integer.valueOf(phoneId));
        if (c != null && isValidPhoneId(phoneId)) {
            mMmTelFeatureCallback.put(Integer.valueOf(phoneId), c);
            c.notifyContextChanged(this.mContext);
            Intent intent = new Intent("com.mediatek.ims.MTK_MMTEL_READY");
            intent.setPackage("com.mediatek.ims");
            intent.putExtra("android:phone_id", phoneId);
            this.mContext.sendBroadcast(intent);
            int[] enabledFeatures = new int[6];
            int[] disabledFeatures = new int[6];
            updateCapabilityChange(phoneId, this.mImsExtInfo[phoneId], enabledFeatures, disabledFeatures);
            updateUtCapabilityChange(phoneId, enabledFeatures, disabledFeatures);
            MmTelFeature.MmTelCapabilities capabilities = convertCapabilities(enabledFeatures);
            if ((this.mImsExtInfo[phoneId] & 4) == 4) {
                capabilities.addCapabilities(8);
            }
            notifyCapabilityChanged(phoneId, capabilities);
        }
    }

    private MmTelFeature.MmTelCapabilities convertCapabilities(int[] enabledFeatures) {
        boolean[] featuresEnabled = new boolean[enabledFeatures.length];
        int i = 0;
        while (i <= 5 && i < enabledFeatures.length) {
            if (enabledFeatures[i] == i) {
                featuresEnabled[i] = DBG;
            } else if (enabledFeatures[i] == -1) {
                featuresEnabled[i] = false;
            }
            i++;
        }
        MmTelFeature.MmTelCapabilities capabilities = new MmTelFeature.MmTelCapabilities();
        if (featuresEnabled[0] || featuresEnabled[2]) {
            capabilities.addCapabilities(1);
        }
        if (featuresEnabled[1] || featuresEnabled[3]) {
            capabilities.addCapabilities(2);
        }
        if (featuresEnabled[4] || featuresEnabled[5]) {
            capabilities.addCapabilities(4);
        }
        log("convertCapabilities - capabilities: " + capabilities);
        return capabilities;
    }

    private void notifyCapabilityChanged(int phoneId, MmTelFeature.MmTelCapabilities c) {
        synchronized (this.mCapLockObj) {
            if (mMmTelFeatureCallback.get(Integer.valueOf(phoneId)) != null) {
                mMmTelFeatureCallback.get(Integer.valueOf(phoneId)).notifyCapabilitiesChanged(c);
            } else {
                loge("There is not IMtkMmTelFeatureCallback for slot " + phoneId);
            }
        }
    }

    private void notifyRcsCapabilityChanged(int phoneId, RcsFeature.RcsImsCapabilities c) {
        synchronized (this.mCapLockObj) {
            if (mRcsFeatureCallback.get(Integer.valueOf(phoneId)) != null) {
                mRcsFeatureCallback.get(Integer.valueOf(phoneId)).notifyCapabilitiesChanged(c);
            } else {
                loge("There is not IMtkRcsFeatureCallback for slot " + phoneId);
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyIncomingCall(int phoneId, ImsCallSessionImplBase c, Bundle extras) {
        if (mMmTelFeatureCallback.get(Integer.valueOf(phoneId)) != null) {
            mMmTelFeatureCallback.get(Integer.valueOf(phoneId)).notifyIncomingCall(c, extras);
        }
    }

    public void notifyIncomingCallSession(int phoneId, IImsCallSession c, Bundle extras) {
        if (mMmTelFeatureCallback.get(Integer.valueOf(phoneId)) != null) {
            try {
                mMmTelFeatureCallback.get(Integer.valueOf(phoneId)).notifyIncomingCallSession(c, extras);
            } catch (RuntimeException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Fail to notifyIncomingCallSession ");
                sb.append(sensitiveEncode("" + e));
                loge(sb.toString());
            }
        }
    }

    public void setImsRegistration(int slotId, MtkImsRegistrationImpl imsRegImpl) {
        sMtkImsRegImpl.remove(Integer.valueOf(slotId));
        if (imsRegImpl != null) {
            sMtkImsRegImpl.put(Integer.valueOf(slotId), imsRegImpl);
            int[] iArr = this.mImsRegInfo;
            if (iArr[slotId] != 3) {
                if (iArr[slotId] == 0) {
                    try {
                        updateImsRegstration(slotId, 2, convertImsRegistrationTech(getRadioTech(slotId)), (ImsReasonInfo) null);
                    } catch (RemoteException e) {
                        loge("Fail to get radio tech " + e);
                    }
                } else if (iArr[slotId] == 1) {
                    updateImsRegstration(slotId, 3, -1, createImsReasonInfo(slotId));
                }
            }
            updateAssociatedUriChanged(slotId, ImsXuiManager.getInstance().getSelfIdentifyUri(slotId));
        }
    }

    private void updateImsRegstration(int slotId, int state, int imsRadioTech, ImsReasonInfo reason) {
        MtkImsRegistrationImpl imsReg = sMtkImsRegImpl.get(Integer.valueOf(slotId));
        if (imsReg != null) {
            try {
                logi("[" + slotId + "] state " + state + " updateImsRegstration, tech " + imsRadioTech + ", reason " + reason);
                switch (state) {
                    case 1:
                        imsReg.onRegistering(imsRadioTech);
                        return;
                    case 2:
                        imsReg.onRegistered(imsRadioTech);
                        return;
                    case 3:
                        imsReg.onDeregistered(reason);
                        return;
                    default:
                        return;
                }
            } catch (IllegalStateException e) {
                loge("Failed to updateImsRegstration " + e);
            }
        } else {
            loge("There is not ImsRegistrationImpl for slot " + slotId);
        }
    }

    private int convertImsRegistrationTech(int tech) {
        switch (tech) {
            case 14:
                return 0;
            case 18:
                return 1;
            default:
                return -1;
        }
    }

    public void updateSelfIdentity(int phondId) {
        log("updateSelfIdentity, send EVENT_SELF_IDENTIFY_UPDATE, phoneId = " + phondId);
        Handler[] handlerArr = this.mHandler;
        handlerArr[phondId].sendMessage(handlerArr[phondId].obtainMessage(24));
    }

    public int getSubIdUsingPhoneId(int phoneId) {
        int[] subIds = SubscriptionManager.getSubId(phoneId);
        int subId = -1;
        if (subIds != null && subIds.length >= 1) {
            subId = subIds[0];
        }
        log("[getSubIdUsingPhoneId] volte_setting subId: " + subId);
        return subId;
    }

    /* access modifiers changed from: private */
    public void setEnhanced4gLteModeSetting(int phoneId, boolean enabled) {
        int i;
        ImsManager imsMgr = ImsManager.getInstance(this.mContext, phoneId);
        if (imsMgr != null) {
            boolean defaultSupportVolte = CarrierConfigManager.getDefaultConfig().getBoolean("carrier_volte_available_bool");
            if (defaultSupportVolte) {
                imsMgr.setEnhanced4gLteModeSetting(enabled);
            } else {
                int subId = getSubIdUsingPhoneId(phoneId);
                if (SubscriptionManager.isValidSubscriptionId(subId) && SubscriptionManager.getIntegerSubscriptionProperty(subId, "volte_vt_enabled", -1, this.mContext) != enabled) {
                    SubscriptionManager.setSubscriptionProperty(subId, "volte_vt_enabled", enabled ? "1" : "0");
                }
            }
            if (imsMgr.isServiceReady() == 0 || !defaultSupportVolte) {
                try {
                    IImsConfig iImsConfig = this.mImsConfigManager.get(phoneId);
                    if (enabled) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    iImsConfig.setFeatureValue(0, 13, i, (ImsConfigListener) null);
                    log("volte_setting setEnhanced4gLteModeSetting with service not ready yet.");
                } catch (RemoteException e) {
                    log("volte_setting setEnhanced4gLteModeSetting with exception.");
                }
            }
        } else {
            loge("[" + phoneId + "] Fail to setEnhanced4gLteModeSetting because imsMgr is null");
        }
    }

    private boolean isOp09SimCard(String iccId) {
        if (iccId.startsWith("898603") || iccId.startsWith("898611") || iccId.startsWith("8985302") || iccId.startsWith("8985307") || iccId.startsWith("8985231")) {
            return DBG;
        }
        return false;
    }

    public boolean isSupportCFT(int phoneId) {
        boolean isSupport;
        synchronized (sMtkSSExt) {
            isSupport = false;
            if (sMtkSSExt.containsKey(Integer.valueOf(phoneId))) {
                isSupport = sMtkSSExt.get(Integer.valueOf(phoneId)).isSupportCFT();
            }
            log("isSupportCFT: " + isSupport);
        }
        return isSupport;
    }

    private int mapSipErrorCode(int code) {
        if (code < 300) {
            return 1000;
        }
        if (code < 400) {
            return 321;
        }
        if (code < 500) {
            switch (code) {
                case 400:
                    return 331;
                case 403:
                    return 332;
                case 404:
                    return 333;
                case 406:
                case 488:
                    return 340;
                case 408:
                    return 335;
                case 410:
                    return 341;
                case 415:
                case 416:
                case 420:
                    return 334;
                case 480:
                    return 336;
                case 484:
                    return 337;
                case SipMessage.CODE_SESSION_INVITE_FAILED_REMOTE_BUSY:
                    return 338;
                case 487:
                    return 339;
                default:
                    return 342;
            }
        } else if (code < 600) {
            switch (code) {
                case RadioError.OEM_ERROR_1:
                    return 351;
                case RadioError.OEM_ERROR_3:
                    return 352;
                case RadioError.OEM_ERROR_4:
                    return 353;
                default:
                    return 354;
            }
        } else if (code >= 700) {
            return 1000;
        } else {
            switch (code) {
                case 600:
                    return 338;
                case 603:
                    return 361;
                case 604:
                    return 341;
                case 606:
                    return 340;
                default:
                    return 362;
            }
        }
    }

    private String registrationStateToString(int state) {
        switch (state) {
            case 0:
                return "IMS_REGISTERING";
            case 1:
                return "IMS_REGISTERED";
            case 2:
                return "IMS_REGISTER_FAIL";
            default:
                return "";
        }
    }

    /* access modifiers changed from: private */
    public void notifyImsRegInd(ImsRegInfo info, IMtkImsRegistrationListener listener, int phoneId) {
        if (info == null || info.mReportType < 0) {
            log("Do not get +IMSREGURI yet.");
            return;
        }
        ImsReasonInfo imsReasonInfo = new ImsReasonInfo(mapSipErrorCode(info.mErrorCode), 0, info.mErrorMsg);
        Uri[] uris = convertUri(info.mUri);
        englog("Notify " + registrationStateToString(info.mReportType) + " uri " + Rlog.pii("ImsService", info.mUri) + " expireTime " + info.mExpireTime + " imsReasonInfo " + imsReasonInfo + " listener " + listener);
        if (listener == null) {
            HashSet<IMtkImsRegistrationListener> mtklisteners = this.mMtkListener.get(phoneId);
            if (mtklisteners != null) {
                synchronized (mtklisteners) {
                    mtklisteners.forEach(new ImsService$$ExternalSyntheticLambda4(this, info, uris, imsReasonInfo));
                }
                return;
            }
            return;
        }
        try {
            listener.onRegistrationImsStateChanged(info.mReportType, uris, info.mExpireTime, imsReasonInfo);
        } catch (RemoteException e) {
            loge("onRegistrationImsStateChanged failed!!");
        }
    }

    public /* synthetic */ void lambda$notifyImsRegInd$6$ImsService(ImsRegInfo info, Uri[] uris, ImsReasonInfo imsReasonInfo, IMtkImsRegistrationListener l) {
        try {
            l.onRegistrationImsStateChanged(info.mReportType, uris, info.mExpireTime, imsReasonInfo);
        } catch (RemoteException e) {
            loge("onRegistrationImsStateChanged failed!!");
        }
    }

    /* access modifiers changed from: private */
    public void notifyRedirectIncomingCall(int phoneId, AsyncResult ar) {
        HashSet<IMtkImsRegistrationListener> mtklisteners = this.mMtkListener.get(phoneId);
        if (mtklisteners != null) {
            synchronized (mtklisteners) {
                mtklisteners.forEach(new ImsService$$ExternalSyntheticLambda3(this, phoneId, ar));
            }
        }
    }

    public /* synthetic */ void lambda$notifyRedirectIncomingCall$7$ImsService(int phoneId, AsyncResult ar, IMtkImsRegistrationListener l) {
        try {
            l.onRedirectIncomingCallIndication(phoneId, (String[]) ar.result);
        } catch (RemoteException e) {
            loge("onRedirectIncomingCallIndication failed!!");
        }
    }

    public void setMTRedirect(int phoneId, boolean enable) {
        log("setMTRedirect: " + enable + ",phoneId: " + phoneId);
        this.mIsMTredirect[phoneId] = enable;
    }

    public void fallBackAospMTFlow(int phoneId) {
        AsyncResult asyncResult;
        log("fallBackAospMTFlow: phoneId " + phoneId);
        int i = this.mRedirectIncomingSocketId;
        if (!(i == -1 || (asyncResult = this.mRedirectIncomingAsyncResult) == null)) {
            sendIncomingCallIndication(i, asyncResult);
        }
        this.mRedirectIncomingSocketId = -1;
        this.mRedirectIncomingAsyncResult = null;
    }

    public void setSipHeader(int phoneId, HashMap<String, String> extraHeaders, String fromUri) {
        log("setSipHeader phoneId: " + phoneId + ", fromUri: " + fromUri + ", extraHeaders: " + extraHeaders);
        int headerCount = 0;
        if (extraHeaders != null || fromUri != null) {
            StringBuilder headerValuePair = new StringBuilder();
            if (extraHeaders != null) {
                int size = extraHeaders.size();
                for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    log("setSipHeader key: " + key + ", value: " + value);
                    headerValuePair.append(toHexString(key + "," + value + ","));
                }
                headerCount = 0 + size;
            }
            if (fromUri != null) {
                headerValuePair.append(toHexString("f," + fromUri + ","));
                headerCount++;
            }
            headerValuePair.setLength(headerValuePair.length() - 2);
            log("setSipHeader headerValuePair: " + headerValuePair.toString());
            this.mImsRILAdapters[phoneId].setSipHeader(0, 0, 0, "0", (Message) null);
            this.mImsRILAdapters[phoneId].setSipHeader(1, 1, headerCount, headerValuePair.toString(), (Message) null);
        }
    }

    private String toHexString(String before) {
        if (TextUtils.isEmpty(before)) {
            return "";
        }
        byte[] bytes = before.getBytes();
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            str.append(String.format("%02x", new Object[]{Byte.valueOf(bytes[i])}));
        }
        log("toHexString before: " + before + " after: " + str.toString());
        return str.toString();
    }

    public void changeEnabledCapabilities(int phoneId, CapabilityChangeRequest request) {
        if (mMmTelFeatureCallback.get(Integer.valueOf(phoneId)) != null) {
            mMmTelFeatureCallback.get(Integer.valueOf(phoneId)).updateCapbilities(request);
            return;
        }
        loge("There is not IMtkMmTelFeatureCallback for slot " + phoneId);
    }

    private String sensitiveEncode(String msg) {
        return ImsServiceCallTracker.sensitiveEncode(msg);
    }

    public Uri[] convertUri(String xui) {
        if (xui == null) {
            return null;
        }
        String[] ids = xui.split(",");
        int len = ids.length;
        Uri[] uris = new Uri[len];
        for (int i = 0; i < len; i++) {
            String number = Uri.parse(ids[i]).getSchemeSpecificPart();
            if (TextUtils.isEmpty(number)) {
                log("empty XUI");
            } else {
                String[] numberParts = number.split("[@;:]");
                if (numberParts.length == 0) {
                    log("no number in XUI handle");
                } else {
                    uris[i] = Uri.parse(numberParts[0]);
                    log("IMS: convertUri() uri = " + Rlog.pii("ImsService", uris[i]));
                }
            }
        }
        return uris;
    }

    public void setWfcRegErrorCodeWithPdn(int phoneId, int errorCode) {
        if (this.mWfcPdnState[phoneId] != 0) {
            this.mWfcRegErrorCode[phoneId] = errorCode;
        }
    }

    public void setWfcRegErrorCode(int phoneId, int errorCode) {
        this.mWfcRegErrorCode[phoneId] = errorCode;
    }

    public int getWfcRegErrorCode(int phoneId) {
        return this.mWfcRegErrorCode[phoneId];
    }

    public void setImsPreCallInfo(int phoneId, int mode, String address, String fromUri, HashMap<String, String> extraHeaders, String[] location) {
        int i = mode;
        String str = fromUri;
        String[] strArr = location;
        if (!(extraHeaders == null && str == null)) {
            ArrayList<String> headerInfo = new ArrayList<>();
            int headerCount = 0;
            StringBuilder headerValuePair = new StringBuilder();
            if (extraHeaders != null) {
                int size = extraHeaders.size();
                for (Iterator<Map.Entry<String, String>> it = extraHeaders.entrySet().iterator(); it.hasNext(); it = it) {
                    Map.Entry<String, String> entry = it.next();
                    String key = entry.getKey();
                    String value = entry.getValue();
                    log("setImsPreCallInfo key: " + key + ", value: " + value);
                    headerValuePair.append(toHexString(key) + "," + toHexString(value) + ",");
                }
                headerCount = 0 + size;
            }
            if (str != null) {
                headerValuePair.append(toHexString("f") + "," + toHexString(str) + ",");
                headerCount++;
            }
            headerValuePair.setLength(headerValuePair.length() - 1);
            String header = headerValuePair.toString();
            log("setImsPreCallInfo headerValuePair: " + header);
            headerInfo.add("" + i);
            headerInfo.add("1");
            int total = (headerValuePair.length() / 1000) + 1;
            headerInfo.add("" + total);
            headerInfo.add("");
            headerInfo.add("" + headerCount);
            headerInfo.add("");
            int i2 = 1;
            while (i2 <= total) {
                headerInfo.set(3, "" + i2);
                StringBuilder sb = new StringBuilder();
                sb.append("");
                int headerCount2 = headerCount;
                sb.append(header.substring((i2 - 1) * 1000, i2 * 1000 < header.length() ? i2 * 1000 : header.length()));
                headerInfo.set(5, sb.toString());
                this.mImsRILAdapters[phoneId].setCallAdditionalInfo(headerInfo, (Message) null);
                i2++;
                String str2 = fromUri;
                headerCount = headerCount2;
            }
        }
        if (strArr != null) {
            ArrayList<String> locationInfo = new ArrayList<>();
            StringBuilder locationString = new StringBuilder();
            for (int i3 = 0; i3 < strArr.length; i3++) {
                locationString.append(strArr[i3] + ",");
            }
            locationString.setLength(locationString.length() - 1);
            locationInfo.add("" + i);
            locationInfo.add("2");
            locationInfo.add("1");
            locationInfo.add("1");
            locationInfo.add("" + strArr.length);
            locationInfo.add(locationString.toString());
            log("setImsPreCallInfo locationString: " + locationString.toString());
            this.mImsRILAdapters[phoneId].setCallAdditionalInfo(locationInfo, (Message) null);
        }
    }

    private boolean getBooleanFromCarrierConfig(int phoneId, String key) {
        PersistableBundle carrierConfig = ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(getSubIdUsingPhoneId(phoneId));
        if (carrierConfig == null) {
            carrierConfig = CarrierConfigManager.getDefaultConfig();
        }
        boolean result = carrierConfig.getBoolean(key);
        log("getBooleanFromCarrierConfig() : key = " + key + " result = " + result);
        return result;
    }

    /* access modifiers changed from: private */
    public void updateImsRegistrationRat(int slotId, int rat) {
        IVoDataService iVoDataService = IVoDataService.Stub.asInterface(ServiceManager.getService("vodata"));
        if (iVoDataService != null) {
            try {
                log("updateImsRegistrationRat : VoDataStatus = " + rat);
                iVoDataService.setImsPdnStatus(slotId, rat);
            } catch (Exception e) {
                log("updateImsRegistrationRat error");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void clearImsRilRequest() {
        log("clearImsRilRequest()");
        for (int i = 0; i < this.mNumOfPhones; i++) {
            ImsCommandsInterface[] imsCommandsInterfaceArr = this.mImsRILAdapters;
            if (imsCommandsInterfaceArr[i] != null) {
                ((ImsRILAdapter) imsCommandsInterfaceArr[i]).clearRequestList(1, DBG);
            }
        }
    }

    /* access modifiers changed from: private */
    public static boolean isValidPhoneId(int phoneId) {
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getActiveModemCount()) {
            return false;
        }
        return DBG;
    }
}
