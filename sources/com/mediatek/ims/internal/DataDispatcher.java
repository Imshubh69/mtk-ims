package com.mediatek.ims.internal;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.PreciseDataConnectionState;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.util.SparseArray;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.InputDeviceCompat;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.mediatek.ims.ImsAdapter;
import com.mediatek.ims.ImsEventDispatcher;
import com.mediatek.ims.VaConstants;
import com.mediatek.ims.common.SubscriptionManagerHelper;
import com.mediatek.ims.internal.DataDispatcherUtil;
import java.util.HashMap;
import vendor.mediatek.hardware.netdagent.V1_0.INetdagent;

public class DataDispatcher implements ImsEventDispatcher.VaEventDispatcher {
    private static final String FAILCAUSE_LOST_CONNECTION = "LOST_CONNECTION";
    private static final String FAILCAUSE_NONE = "";
    private static final String FAILCAUSE_UNKNOWN = "UNKNOWN";
    static final int MAX_NETWORK_ACTIVE_TIMEOUT_MS = 20000;
    static final int MSG_ID_WRAP_IMSM_IMSPA_PDN_ABORT = 800004;
    static final int MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_COMPLETED = 800001;
    static final int MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_FAIL = 800003;
    static final int MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_COMPLETED = 800002;
    static final int MSG_ON_NOTIFY_ACTIVE_DATA_TIMEOUT = 800005;
    private static final String TAG = DataDispatcher.class.getSimpleName();
    private final int MSG_ID_IMSA_DISABLE_SERVICE = 700001;
    private final int MSG_ID_NOTIFY_SUBCRIPTION_CHANAGED = 600001;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public DataDispatcherUtil mDataDispatcherUtil;
    private HandlerThread mDcHandlerThread;
    /* access modifiers changed from: private */
    public DataConnection mEmcConnection;
    /* access modifiers changed from: private */
    public HashMap<String, Integer> mFailCauses = new HashMap<String, Integer>() {
        private static final long serialVersionUID = 1;

        {
            put(DataDispatcher.FAILCAUSE_NONE, 0);
            put("OPERATOR_BARRED", 8);
            put("NAS_SIGNALLING", 14);
            put("MBMS_CAPABILITIES_INSUFFICIENT", 24);
            put("LLC_SNDCP", 25);
            put("INSUFFICIENT_RESOURCES", 26);
            put("MISSING_UNKNOWN_APN", 27);
            put("UNKNOWN_PDP_ADDRESS_TYPE", 28);
            put("USER_AUTHENTICATION", 29);
            put("ACTIVATION_REJECT_GGSN", 30);
            put("ACTIVATION_REJECT_UNSPECIFIED", 31);
            put("SERVICE_OPTION_NOT_SUPPORTED", 32);
            put("SERVICE_OPTION_NOT_SUBSCRIBED", 33);
            put("SERVICE_OPTION_OUT_OF_ORDER", 34);
            put("NSAPI_IN_USE", 35);
            put("REGULAR_DEACTIVATION", 36);
            put("QOS_NOT_ACCEPTED", 37);
            put("NETWORK_FAILURE", 38);
            put("UMTS_REACTIVATION_REQ", 39);
            put("FEATURE_NOT_SUPP", 40);
            put("TFT_SEMANTIC_ERROR", 41);
            put("TFT_SYTAX_ERROR", 42);
            put("UNKNOWN_PDP_CONTEXT", 43);
            put("FILTER_SEMANTIC_ERROR", 44);
            put("FILTER_SYTAX_ERROR", 45);
            put("PDP_WITHOUT_ACTIVE_TFT", 46);
            put("MULTICAST_GROUP_MEMBERSHIP_TIMEOUT", 47);
            put("BCM_VIOLATION", 48);
            put("LAST_PDN_DISC_NOT_ALLOWED", 49);
            put("ONLY_IPV4_ALLOWED", 50);
            put("ONLY_IPV6_ALLOWED", 51);
            put("ONLY_SINGLE_BEARER_ALLOWED", 52);
            put("ESM_INFO_NOT_RECEIVED", 53);
            put("PDN_CONN_DOES_NOT_EXIST", 54);
            put("MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED", 55);
            put("COLLISION_WITH_NW_INITIATED_REQUEST", 56);
            put("UNSUPPORTED_QCI_VALUE", 59);
            put("BEARER_HANDLING_NOT_SUPPORT", 60);
            put("MAX_ACTIVE_PDP_CONTEXT_REACHED", 65);
            put("UNSUPPORTED_APN_IN_CURRENT_PLMN", 66);
            put("INVALID_TRANSACTION_ID", 81);
            put("MESSAGE_INCORRECT_SEMANTIC", 95);
            put("INVALID_MANDATORY_INFO", 96);
            put("MESSAGE_TYPE_UNSUPPORTED", 97);
            put("MSG_TYPE_NONCOMPATIBLE_STATE", 98);
            put("UNKNOWN_INFO_ELEMENT", 99);
            put("CONDITIONAL_IE_ERROR", 100);
            put("MSG_AND_PROTOCOL_STATE_UNCOMPATIBLE", 101);
            put("PROTOCOL_ERRORS", 111);
            put("APN_TYPE_CONFLICT", 112);
            put("REGISTRATION_FAIL", -1);
            put("GPRS_REGISTRATION_FAIL", -2);
            put("SIGNAL_LOST", -3);
            put("PREF_RADIO_TECH_CHANGED", -4);
            put("RADIO_POWER_OFF", -5);
            put("TETHERED_CALL_ACTIVE", -6);
            put("PDP_FAIL_ROUTER_ADVERTISEMENT", -7);
            put("PDP_FAIL_FALLBACK_RETRY", Integer.valueOf(NotificationManagerCompat.IMPORTANCE_UNSPECIFIED));
            put("INSUFFICIENT_LOCAL_RESOURCES", 1048574);
            put("ERROR_UNSPECIFIED", 65535);
            put(DataDispatcher.FAILCAUSE_UNKNOWN, 65536);
            put("RADIO_NOT_AVAILABLE", 65537);
            put("UNACCEPTABLE_NETWORK_PARAMETER", 65538);
            put("CONNECTION_TO_DATACONNECTIONAC_BROKEN", 65539);
            put(DataDispatcher.FAILCAUSE_LOST_CONNECTION, Integer.valueOf(InputDeviceCompat.SOURCE_TRACKBALL));
            put("RESET_BY_FRAMEWORK", 65541);
            put("PAM_ATT_PDN_ACCESS_REJECT_IMS_PDN_BLOCK_TEMP", 5122);
            put("TCM_ESM_TIMER_TIMEOUT", 86058);
            put("MTK_TCM_ESM_TIMER_TIMEOUT", 3910);
            put("DUE_TO_REACH_RETRY_COUNTER", 3599);
            put("OEM_DCFAILCAUSE_12", 4108);
        }
    };
    /* access modifiers changed from: private */
    public Handler mHandler;
    private Thread mHandlerThread = new Thread() {
        public void run() {
            Looper.prepare();
            Handler unused = DataDispatcher.this.mHandler = new Handler() {
                /* Debug info: failed to restart local var, previous not found, register: 3 */
                public synchronized void handleMessage(Message msg) {
                    DataDispatcher dataDispatcher = DataDispatcher.this;
                    dataDispatcher.logd("receives request [" + msg.what + "]");
                    switch (msg.what) {
                        case DataDispatcher.MSG_ON_NOTIFY_ACTIVE_DATA_TIMEOUT /*800005*/:
                            DataDispatcher.this.mImsConnection.onImsRequestTimeout();
                            break;
                        default:
                            DataDispatcher dataDispatcher2 = DataDispatcher.this;
                            dataDispatcher2.logd("receives unhandled message [" + msg.what + "]");
                            break;
                    }
                }
            };
            Looper.loop();
        }
    };
    /* access modifiers changed from: private */
    public DataConnection mImsConnection;
    private boolean mIsEnable;
    private Object mLock = new Object();
    /* access modifiers changed from: private */
    public int mPhoneId;
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState state) {
            String apn = ApnSetting.getApnTypesStringFromBitmask(state.getDataConnectionApnTypeBitMask());
            int failure = state.getDataConnectionFailCause();
            DataDispatcher dataDispatcher = DataDispatcher.this;
            dataDispatcher.logd("APN: " + apn + " failCause: " + failure);
            if (failure > 0) {
                char c = 65535;
                switch (apn.hashCode()) {
                    case 104399:
                        if (apn.equals("ims")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 1629013393:
                        if (apn.equals("emergency")) {
                            c = 1;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        Handler imsHandle = DataDispatcher.this.mImsConnection.getHandler();
                        imsHandle.sendMessage(imsHandle.obtainMessage(DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_FAIL, Integer.valueOf(failure)));
                        DataDispatcher.this.mHandler.removeMessages(DataDispatcher.MSG_ON_NOTIFY_ACTIVE_DATA_TIMEOUT);
                        return;
                    case 1:
                        Handler emcHandle = DataDispatcher.this.mEmcConnection.getHandler();
                        emcHandle.sendMessage(emcHandle.obtainMessage(DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_FAIL, Integer.valueOf(failure)));
                        return;
                    default:
                        DataDispatcher dataDispatcher2 = DataDispatcher.this;
                        dataDispatcher2.loge("UnKnown APN: " + apn);
                        return;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mSubId;
    private TelephonyManager mTelephonyManager;
    private ImsDataTracker mTracker;

    public DataDispatcher(Context context, ImsDataTracker tracker, int phoneId) {
        logd("constructor");
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mSubId = SubscriptionManagerHelper.getSubIdUsingPhoneId(phoneId);
        this.mTracker = tracker;
        this.mHandlerThread.start();
        HandlerThread handlerThread = new HandlerThread("DcHandlerThread");
        this.mDcHandlerThread = handlerThread;
        handlerThread.start();
        this.mImsConnection = new DataConnection("ims", new Handler(this.mDcHandlerThread.getLooper()), 4);
        this.mEmcConnection = new DataConnection("emergency", new Handler(this.mDcHandlerThread.getLooper()), 10);
        this.mDataDispatcherUtil = new DataDispatcherUtil();
    }

    public void enableRequest(int phoneId) {
        logi("receive enableRequest");
        synchronized (this.mLock) {
            this.mIsEnable = true;
            registerPhoneStateListener(this.mContext, this.mSubId);
        }
    }

    public void disableRequest(int phoneId) {
        logi("receive disableRequest");
        synchronized (this.mLock) {
            this.mIsEnable = false;
            this.mHandler.removeMessages(MSG_ON_NOTIFY_ACTIVE_DATA_TIMEOUT);
            unRegisterPhoneStateListener(this.mContext, this.mSubId);
            this.mImsConnection.disable();
            this.mEmcConnection.disable();
        }
    }

    private TelephonyManager getTelephonyManager(Context context, int subId) {
        return ((TelephonyManager) context.getSystemService(TelephonyManager.class)).createForSubscriptionId(subId);
    }

    private void registerPhoneStateListener(Context context, int subId) {
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            this.mTelephonyManager = getTelephonyManager(context, subId);
        }
        logd("registerPhoneStateListener ");
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 4096);
        }
    }

    private void unRegisterPhoneStateListener(Context context, int subId) {
        logd("unRegisterPhoneStateListener ");
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            this.mTelephonyManager = getTelephonyManager(context, subId);
        }
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 0);
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x00a1, code lost:
        if (r2.equals("emergency") != false) goto L_0x00af;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void vaEventCallback(com.mediatek.ims.ImsAdapter.VaEvent r6) {
        /*
            r5 = this;
            boolean r0 = r5.mIsEnable
            r1 = 1
            if (r0 != r1) goto L_0x00fa
            com.mediatek.ims.internal.DataDispatcherUtil r0 = r5.mDataDispatcherUtil
            com.mediatek.ims.internal.DataDispatcherUtil$ImsBearerRequest r0 = r0.deCodeEvent(r6)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "got request: "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            r5.logi(r2)
            if (r0 != 0) goto L_0x0027
            java.lang.String r1 = "request is null"
            r5.loge(r1)
            return
        L_0x0027:
            int r2 = r0.getRequestID()
            r3 = 900403(0xdbd33, float:1.261733E-39)
            if (r2 != r3) goto L_0x008e
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            int r3 = r0.getTransId()
            r2.append(r3)
            java.lang.String r3 = ","
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r1.append(r2)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            int r4 = r5.mPhoneId
            r2.append(r4)
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r1.append(r2)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.util.HashMap<java.lang.String, java.lang.Integer> r3 = r5.mFailCauses
            java.lang.String r4 = "UNKNOWN"
            java.lang.Object r3 = r3.get(r4)
            r2.append(r3)
            java.lang.String r3 = ""
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r1.append(r2)
            com.mediatek.ims.internal.DataDispatcherUtil r2 = r5.mDataDispatcherUtil
            r3 = 900405(0xdbd35, float:1.261736E-39)
            java.lang.String r4 = r1.toString()
            com.mediatek.ims.ImsAdapter$VaEvent r2 = r2.enCodeEvent(r3, r4)
            r5.sendVaEvent(r2)
            return
        L_0x008e:
            java.lang.String r2 = r0.getCapability()
            r3 = -1
            int r4 = r2.hashCode()
            switch(r4) {
                case 104399: goto L_0x00a4;
                case 1629013393: goto L_0x009b;
                default: goto L_0x009a;
            }
        L_0x009a:
            goto L_0x00ae
        L_0x009b:
            java.lang.String r4 = "emergency"
            boolean r2 = r2.equals(r4)
            if (r2 == 0) goto L_0x009a
            goto L_0x00af
        L_0x00a4:
            java.lang.String r1 = "ims"
            boolean r1 = r2.equals(r1)
            if (r1 == 0) goto L_0x009a
            r1 = 0
            goto L_0x00af
        L_0x00ae:
            r1 = r3
        L_0x00af:
            switch(r1) {
                case 0: goto L_0x00e2;
                case 1: goto L_0x00cb;
                default: goto L_0x00b2;
            }
        L_0x00b2:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "not support capbility: "
            r1.append(r2)
            java.lang.String r2 = r0.getCapability()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r5.loge(r1)
            goto L_0x00f9
        L_0x00cb:
            com.mediatek.ims.internal.DataDispatcher$DataConnection r1 = r5.mEmcConnection
            android.os.Handler r1 = r1.getHandler()
            com.mediatek.ims.internal.DataDispatcher$DataConnection r2 = r5.mEmcConnection
            r2.putRequest(r0)
            int r2 = r0.getRequestID()
            android.os.Message r2 = r1.obtainMessage(r2)
            r1.sendMessage(r2)
            goto L_0x00f9
        L_0x00e2:
            com.mediatek.ims.internal.DataDispatcher$DataConnection r1 = r5.mImsConnection
            android.os.Handler r1 = r1.getHandler()
            com.mediatek.ims.internal.DataDispatcher$DataConnection r2 = r5.mImsConnection
            r2.putRequest(r0)
            int r2 = r0.getRequestID()
            android.os.Message r2 = r1.obtainMessage(r2)
            r1.sendMessage(r2)
        L_0x00f9:
            goto L_0x00ff
        L_0x00fa:
            java.lang.String r0 = "ims service not be enabled"
            r5.loge(r0)
        L_0x00ff:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.internal.DataDispatcher.vaEventCallback(com.mediatek.ims.ImsAdapter$VaEvent):void");
    }

    public void sendVaEvent(ImsAdapter.VaEvent event) {
        if (this.mIsEnable) {
            logi("send event [" + event.getRequestID() + ", " + event.getDataLen() + "]");
            this.mTracker.sendVaEvent(event);
            return;
        }
        loge("ims service not be enabled");
    }

    public void onSubscriptionsChanged() {
        int newSubId = SubscriptionManagerHelper.getSubIdUsingPhoneId(this.mPhoneId);
        logd("onSubscriptionsChanged: subId: " + this.mSubId + ", newSubId: " + newSubId);
        int i = this.mSubId;
        if (i != newSubId) {
            if (newSubId < 0) {
                unRegisterPhoneStateListener(this.mContext, i);
            }
            if (newSubId > 0) {
                registerPhoneStateListener(this.mContext, newSubId);
            }
            this.mSubId = newSubId;
            this.mImsConnection.onSubscriptionsChanged();
            this.mEmcConnection.onSubscriptionsChanged();
        }
    }

    public void logd(String s) {
        String str = TAG;
        Rlog.d(str, "[" + this.mPhoneId + "]" + s);
    }

    public void logi(String s) {
        String str = TAG;
        Rlog.i(str, "[" + this.mPhoneId + "]" + s);
    }

    public void loge(String s) {
        String str = TAG;
        Rlog.e(str, "[" + this.mPhoneId + "]" + s);
    }

    public class DataConnection extends StateMachine {
        private String TAG = "DC-";
        /* access modifiers changed from: private */
        public ActivatingState mActivatingState = new ActivatingState();
        /* access modifiers changed from: private */
        public ActiveState mActiveState = new ActiveState();
        private int mCapabiliy;
        /* access modifiers changed from: private */
        public ConnectivityManager mConnectivityManager;
        private DefaultState mDefaultState = new DefaultState();
        /* access modifiers changed from: private */
        public DisconnectingState mDisconnectingState = new DisconnectingState();
        /* access modifiers changed from: private */
        public String mFwInterface = DataDispatcher.FAILCAUSE_NONE;
        private SparseArray<DataDispatcherUtil.ImsBearerRequest> mImsNetworkRequests = new SparseArray<>();
        /* access modifiers changed from: private */
        public InactiveState mInactiveState = new InactiveState();
        /* access modifiers changed from: private */
        public String mInterface = DataDispatcher.FAILCAUSE_NONE;
        /* access modifiers changed from: private */
        public long mNetworkHandle = 0;
        /* access modifiers changed from: private */
        public int mNetworkId;
        private NwAvailableCallback mNwAvailableCallback;
        private NwLostCallback mNwLostCallback;

        public DataConnection(String name, Handler mHandler, int capability) {
            super(name, mHandler);
            this.mCapabiliy = capability;
            this.mConnectivityManager = (ConnectivityManager) DataDispatcher.this.mContext.getSystemService("connectivity");
            this.mNwAvailableCallback = new NwAvailableCallback(this);
            this.mNwLostCallback = new NwLostCallback(this);
            addState(this.mDefaultState);
            addState(this.mInactiveState, this.mDefaultState);
            addState(this.mActivatingState, this.mDefaultState);
            addState(this.mActiveState, this.mDefaultState);
            addState(this.mDisconnectingState, this.mDefaultState);
            setInitialState(this.mInactiveState);
            start();
        }

        private class DefaultState extends State {
            private DefaultState() {
            }

            public void enter() {
                DataConnection.this.logd("DefaultState: enter");
            }

            public void exit() {
                DataConnection.this.logd("DefaultState: exit");
            }

            public boolean processMessage(Message msg) {
                DataConnection dataConnection = DataConnection.this;
                dataConnection.logd("DefaultState msg: " + DataConnection.this.msgToString(msg.what));
                switch (msg.what) {
                    case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_REQ:
                        DataConnection dataConnection2 = DataConnection.this;
                        dataConnection2.rejectNetworkRequest(((Integer) DataDispatcher.this.mFailCauses.get(DataDispatcher.FAILCAUSE_NONE)).intValue());
                        break;
                    case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_REQ:
                        DataConnection dataConnection3 = DataConnection.this;
                        dataConnection3.rejectNetworkReleased(((Integer) DataDispatcher.this.mFailCauses.get(DataDispatcher.FAILCAUSE_NONE)).intValue());
                        break;
                    default:
                        DataConnection dataConnection4 = DataConnection.this;
                        dataConnection4.loge("DefaultState not handled request: " + DataConnection.this.msgToString(msg.what));
                        break;
                }
                return true;
            }
        }

        private class InactiveState extends State {
            private InactiveState() {
            }

            public void enter() {
                DataConnection.this.logd("InactiveState: enter");
            }

            public void exit() {
                DataConnection.this.logd("InactiveState: exit");
            }

            public boolean processMessage(Message msg) {
                DataConnection dataConnection = DataConnection.this;
                dataConnection.logd("InactiveState msg: " + DataConnection.this.msgToString(msg.what));
                switch (msg.what) {
                    case 600001:
                    case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_REQ:
                        if (!DataConnection.this.requestNetwork()) {
                            return true;
                        }
                        DataConnection dataConnection2 = DataConnection.this;
                        dataConnection2.transitionTo(dataConnection2.mActivatingState);
                        return true;
                    case 700001:
                        DataConnection.this.clearNwInfo(true);
                        return true;
                    case DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_COMPLETED /*800002*/:
                        break;
                    case DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_ABORT /*800004*/:
                        DataConnection dataConnection3 = DataConnection.this;
                        dataConnection3.rejectNetworkRequest(((Integer) DataDispatcher.this.mFailCauses.get(DataDispatcher.FAILCAUSE_UNKNOWN)).intValue());
                        DataConnection.this.releaseNetwork();
                        DataConnection.this.onAbortNetworkCompleted();
                        return true;
                    case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_REQ:
                        DataConnection.this.releaseNetwork();
                        break;
                    default:
                        DataConnection dataConnection4 = DataConnection.this;
                        dataConnection4.loge("InactiveState not handled request: " + DataConnection.this.msgToString(msg.what));
                        return false;
                }
                DataConnection.this.onReleaseNetworkCompleted();
                return true;
            }
        }

        private class ActivatingState extends State {
            private ActivatingState() {
            }

            public void enter() {
                DataConnection.this.logd("ActivatingState: enter");
            }

            public void exit() {
                DataConnection.this.logd("ActivatingState: exit");
            }

            public boolean processMessage(Message msg) {
                DataConnection dataConnection = DataConnection.this;
                dataConnection.logd("ActivatingState msg: " + DataConnection.this.msgToString(msg.what));
                switch (msg.what) {
                    case 600001:
                        DataConnection.this.releaseNetwork();
                        DataConnection dataConnection2 = DataConnection.this;
                        dataConnection2.rejectNetworkRequest(((Integer) DataDispatcher.this.mFailCauses.get(DataDispatcher.FAILCAUSE_UNKNOWN)).intValue());
                        DataConnection dataConnection3 = DataConnection.this;
                        dataConnection3.transitionTo(dataConnection3.mInactiveState);
                        return true;
                    case 700001:
                        DataConnection.this.releaseNetwork();
                        DataConnection dataConnection4 = DataConnection.this;
                        dataConnection4.transitionTo(dataConnection4.mInactiveState);
                        DataConnection.this.clearNwInfo(true);
                        return true;
                    case DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_COMPLETED /*800001*/:
                        DataConnection.this.deferMessage(msg);
                        DataConnection dataConnection5 = DataConnection.this;
                        dataConnection5.transitionTo(dataConnection5.mActiveState);
                        return true;
                    case DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_FAIL /*800003*/:
                        DataConnection.this.releaseNetwork();
                        DataConnection.this.rejectNetworkRequest(((Integer) msg.obj).intValue());
                        DataConnection dataConnection6 = DataConnection.this;
                        dataConnection6.transitionTo(dataConnection6.mInactiveState);
                        return true;
                    case DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_ABORT /*800004*/:
                        DataConnection.this.deferMessage(msg);
                        DataConnection dataConnection7 = DataConnection.this;
                        dataConnection7.transitionTo(dataConnection7.mInactiveState);
                        return true;
                    default:
                        DataConnection dataConnection8 = DataConnection.this;
                        dataConnection8.loge("ActivatingState not handled request: " + DataConnection.this.msgToString(msg.what));
                        return false;
                }
            }
        }

        private class ActiveState extends State {
            private ActiveState() {
            }

            public void enter() {
                DataConnection.this.logd("ActiveState: enter");
                DataConnection.this.setFirewallInterfaceChain(true);
                DataConnection dataConnection = DataConnection.this;
                String unused = dataConnection.mFwInterface = dataConnection.mInterface;
            }

            public void exit() {
                DataConnection.this.logd("ActiveState: exit");
                DataConnection.this.setFirewallInterfaceChain(false);
            }

            public boolean processMessage(Message msg) {
                DataConnection dataConnection = DataConnection.this;
                dataConnection.logd("ActiveState msg: " + DataConnection.this.msgToString(msg.what));
                switch (msg.what) {
                    case 700001:
                        DataConnection.this.releaseNetwork();
                        DataConnection dataConnection2 = DataConnection.this;
                        dataConnection2.transitionTo(dataConnection2.mInactiveState);
                        DataConnection.this.clearNwInfo(true);
                        return true;
                    case DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_COMPLETED /*800001*/:
                        DataConnection.this.onRequestNetworkCompleted();
                        return true;
                    case DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_COMPLETED /*800002*/:
                        DataConnection.this.notifyNetworkLosted();
                        DataConnection dataConnection3 = DataConnection.this;
                        dataConnection3.transitionTo(dataConnection3.mInactiveState);
                        return true;
                    case DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_ABORT /*800004*/:
                        DataConnection.this.deferMessage(msg);
                        DataConnection dataConnection4 = DataConnection.this;
                        dataConnection4.transitionTo(dataConnection4.mInactiveState);
                        return true;
                    case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_REQ:
                        DataConnection.this.deferMessage(msg);
                        DataConnection.this.releaseNetwork();
                        DataConnection dataConnection5 = DataConnection.this;
                        dataConnection5.transitionTo(dataConnection5.mDisconnectingState);
                        return true;
                    case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_REQ:
                        DataConnection.this.releaseNetwork();
                        DataConnection dataConnection6 = DataConnection.this;
                        dataConnection6.transitionTo(dataConnection6.mDisconnectingState);
                        return true;
                    default:
                        DataConnection dataConnection7 = DataConnection.this;
                        dataConnection7.loge("ActiveState not handled request: " + DataConnection.this.msgToString(msg.what));
                        return false;
                }
            }
        }

        private class DisconnectingState extends State {
            private DisconnectingState() {
            }

            public void enter() {
                DataConnection.this.logd("DisconnectingState: enter");
            }

            public void exit() {
                DataConnection.this.logd("DisconnectingState: exit");
            }

            public boolean processMessage(Message msg) {
                DataConnection dataConnection = DataConnection.this;
                dataConnection.logd("DisconnectingState msg: " + DataConnection.this.msgToString(msg.what));
                switch (msg.what) {
                    case 600001:
                    case DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_COMPLETED /*800002*/:
                        DataConnection.this.onReleaseNetworkCompleted();
                        DataConnection dataConnection2 = DataConnection.this;
                        dataConnection2.transitionTo(dataConnection2.mInactiveState);
                        return true;
                    case 700001:
                        DataConnection dataConnection3 = DataConnection.this;
                        dataConnection3.transitionTo(dataConnection3.mInactiveState);
                        DataConnection.this.clearNwInfo(true);
                        return true;
                    case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_REQ:
                        DataConnection.this.deferMessage(msg);
                        return true;
                    default:
                        DataConnection dataConnection4 = DataConnection.this;
                        dataConnection4.loge("DisconnectingState not handled request: " + DataConnection.this.msgToString(msg.what));
                        return false;
                }
            }
        }

        /* access modifiers changed from: private */
        public boolean requestNetwork() {
            logd("requestNetwork");
            if (!SubscriptionManager.isValidSubscriptionId(DataDispatcher.this.mSubId) && this.mCapabiliy != 10) {
                loge("inValid subId: " + DataDispatcher.this.mSubId);
                return false;
            } else if (this.mImsNetworkRequests.get(VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_REQ) == null) {
                loge("ImsBearerRequest is NULL");
                return false;
            } else {
                NetworkRequest.Builder builder = new NetworkRequest.Builder();
                builder.addCapability(this.mCapabiliy);
                builder.addTransportType(0);
                builder.setNetworkSpecifier(String.valueOf(DataDispatcher.this.mSubId));
                NetworkRequest nwRequest = builder.build();
                refreshNwLostCallBack(nwRequest);
                if (this.mCapabiliy == 4) {
                    DataDispatcher.this.mHandler.removeMessages(DataDispatcher.MSG_ON_NOTIFY_ACTIVE_DATA_TIMEOUT);
                    DataDispatcher.this.mHandler.sendMessageDelayed(DataDispatcher.this.mHandler.obtainMessage(DataDispatcher.MSG_ON_NOTIFY_ACTIVE_DATA_TIMEOUT), 20000);
                }
                logd("start requestNetwork for " + getName());
                this.mConnectivityManager.requestNetwork(nwRequest, this.mNwAvailableCallback);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public void rejectNetworkRequest(int cause) {
            logd("rejectNetworkRequest cause: " + cause);
            DataDispatcherUtil.ImsBearerRequest n = this.mImsNetworkRequests.get(VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_REQ);
            for (int i = 0; i < this.mImsNetworkRequests.size(); i++) {
                logd("found Req: " + this.mImsNetworkRequests.valueAt(i));
            }
            if (n != null) {
                DataDispatcher.this.mHandler.removeMessages(DataDispatcher.MSG_ON_NOTIFY_ACTIVE_DATA_TIMEOUT);
                StringBuilder builder = new StringBuilder();
                builder.append(n.getTransId() + ",");
                builder.append(DataDispatcher.this.mPhoneId + ",");
                builder.append(cause + DataDispatcher.FAILCAUSE_NONE);
                DataDispatcher dataDispatcher = DataDispatcher.this;
                dataDispatcher.sendVaEvent(dataDispatcher.mDataDispatcherUtil.enCodeEvent(VaConstants.MSG_ID_WRAP_IMSPA_IMSM_PDN_ACT_REJ_RESP, builder.toString()));
                clearNwInfo(false);
                this.mImsNetworkRequests.remove(n.getRequestID());
            }
        }

        /* access modifiers changed from: private */
        public void onRequestNetworkCompleted() {
            logd("onRequestNetworkComplete");
            DataDispatcherUtil.ImsBearerRequest n = this.mImsNetworkRequests.get(VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_REQ);
            for (int i = 0; i < this.mImsNetworkRequests.size(); i++) {
                logd("found Req: " + this.mImsNetworkRequests.valueAt(i));
            }
            if (n != null) {
                notifyNetworkHandle();
                StringBuilder builder = new StringBuilder();
                builder.append(n.getTransId() + ",");
                builder.append(DataDispatcher.this.mPhoneId + ",");
                builder.append(this.mNetworkId + ",");
                builder.append(this.mInterface + DataDispatcher.FAILCAUSE_NONE);
                logd("netId:" + this.mNetworkId + " IfaceName:" + this.mInterface);
                DataDispatcher dataDispatcher = DataDispatcher.this;
                dataDispatcher.sendVaEvent(dataDispatcher.mDataDispatcherUtil.enCodeEvent(VaConstants.MSG_ID_WRAP_IMSPA_IMSM_PDN_ACT_ACK_RESP, builder.toString()));
                this.mImsNetworkRequests.remove(n.getRequestID());
            }
        }

        private void notifyNetworkHandle() {
            log("notifyNetworkHandle() netHandle: " + this.mNetworkHandle);
            StringBuilder builder = new StringBuilder();
            builder.append(DataDispatcher.this.mPhoneId + ",");
            builder.append(this.mNetworkHandle);
            DataDispatcher dataDispatcher = DataDispatcher.this;
            dataDispatcher.sendVaEvent(dataDispatcher.mDataDispatcherUtil.enCodeEvent(VaConstants.MSG_ID_WRAP_IMSPA_IMSM_PDN_NETWORK_HANDLE_NOTIFY, builder.toString()));
        }

        /* access modifiers changed from: private */
        public void releaseNetwork() {
            logd("releaseNetwork");
            DataDispatcherUtil.ImsBearerRequest imsBearerRequest = this.mImsNetworkRequests.get(VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_REQ);
            DataDispatcher.this.mHandler.removeMessages(DataDispatcher.MSG_ON_NOTIFY_ACTIVE_DATA_TIMEOUT);
            try {
                this.mConnectivityManager.unregisterNetworkCallback(this.mNwAvailableCallback);
            } catch (IllegalArgumentException e) {
                loge("cb already has been released!!");
            }
        }

        /* access modifiers changed from: private */
        public void rejectNetworkReleased(int cause) {
            logd("rejectNetworkReleased cause: " + cause);
            DataDispatcherUtil.ImsBearerRequest n = this.mImsNetworkRequests.get(VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_REQ);
            for (int i = 0; i < this.mImsNetworkRequests.size(); i++) {
                logd("found Req: " + this.mImsNetworkRequests.valueAt(i));
            }
            if (n != null) {
                StringBuilder builder = new StringBuilder();
                builder.append(n.getTransId() + ",");
                builder.append(DataDispatcher.this.mPhoneId + ",");
                builder.append(cause + DataDispatcher.FAILCAUSE_NONE);
                DataDispatcher dataDispatcher = DataDispatcher.this;
                dataDispatcher.sendVaEvent(dataDispatcher.mDataDispatcherUtil.enCodeEvent(VaConstants.MSG_ID_WRAP_IMSPA_IMSM_PDN_DEACT_REJ_RESP, builder.toString()));
                this.mImsNetworkRequests.remove(n.getRequestID());
            }
        }

        /* access modifiers changed from: private */
        public void onReleaseNetworkCompleted() {
            logd("onReleaseNetworkCompleted");
            DataDispatcherUtil.ImsBearerRequest n = this.mImsNetworkRequests.get(VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_REQ);
            for (int i = 0; i < this.mImsNetworkRequests.size(); i++) {
                logd("found Req: " + this.mImsNetworkRequests.valueAt(i));
            }
            if (n != null) {
                StringBuilder builder = new StringBuilder();
                builder.append(n.getTransId() + ",");
                builder.append(DataDispatcher.this.mPhoneId + ",");
                builder.append(DataDispatcher.this.mFailCauses.get(DataDispatcher.FAILCAUSE_UNKNOWN) + ",");
                builder.append(this.mInterface + DataDispatcher.FAILCAUSE_NONE);
                DataDispatcher dataDispatcher = DataDispatcher.this;
                dataDispatcher.sendVaEvent(dataDispatcher.mDataDispatcherUtil.enCodeEvent(VaConstants.MSG_ID_WRAP_IMSPA_IMSM_PDN_DEACT_ACK_RESP, builder.toString()));
                this.mImsNetworkRequests.remove(n.getRequestID());
            }
            clearNwInfo(false);
        }

        /* access modifiers changed from: private */
        public void onAbortNetworkCompleted() {
            logd("onAbortNetworkCompleted");
            DataDispatcherUtil.ImsBearerRequest n = this.mImsNetworkRequests.get(DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_ABORT);
            for (int i = 0; i < this.mImsNetworkRequests.size(); i++) {
                logd("found Req: " + this.mImsNetworkRequests.valueAt(i));
            }
            if (n != null) {
                StringBuilder builder = new StringBuilder();
                builder.append(n.getTransId() + ",");
                builder.append(DataDispatcher.this.mPhoneId + ",");
                builder.append(DataDispatcher.this.mFailCauses.get(DataDispatcher.FAILCAUSE_UNKNOWN) + ",");
                builder.append(this.mInterface + DataDispatcher.FAILCAUSE_NONE);
                DataDispatcher dataDispatcher = DataDispatcher.this;
                dataDispatcher.sendVaEvent(dataDispatcher.mDataDispatcherUtil.enCodeEvent(VaConstants.MSG_ID_WRAP_IMSPA_IMSM_PDN_DEACT_ACK_RESP, builder.toString()));
                this.mImsNetworkRequests.remove(n.getRequestID());
            }
            clearNwInfo(false);
        }

        /* access modifiers changed from: private */
        public void notifyNetworkLosted() {
            logd("notifyNetworkLosted");
            try {
                this.mConnectivityManager.unregisterNetworkCallback(this.mNwAvailableCallback);
            } catch (IllegalArgumentException e) {
                loge("cb already has been released!!");
            }
            StringBuilder builder = new StringBuilder();
            builder.append(DataDispatcher.this.mPhoneId + ",");
            builder.append(DataDispatcher.this.mFailCauses.get(DataDispatcher.FAILCAUSE_LOST_CONNECTION) + ",");
            builder.append(this.mInterface + DataDispatcher.FAILCAUSE_NONE);
            DataDispatcher dataDispatcher = DataDispatcher.this;
            dataDispatcher.sendVaEvent(dataDispatcher.mDataDispatcherUtil.enCodeEvent(VaConstants.MSG_ID_WRAP_IMSPA_IMSM_PDN_DEACT_IND, builder.toString()));
            clearNwInfo(false);
        }

        /* access modifiers changed from: private */
        public void disable() {
            sendMessage(obtainMessage(700001));
        }

        /* access modifiers changed from: private */
        public void clearNwInfo(boolean disable) {
            logd("clearNwInfo");
            if (disable) {
                this.mImsNetworkRequests.clear();
            }
            this.mNetworkId = 0;
            this.mNetworkHandle = 0;
            this.mInterface = DataDispatcher.FAILCAUSE_NONE;
        }

        public void putRequest(DataDispatcherUtil.ImsBearerRequest request) {
            if (this.mImsNetworkRequests.get(request.getRequestID()) == null) {
                this.mImsNetworkRequests.put(request.getRequestID(), request);
                return;
            }
            loge("request already exist: " + request);
        }

        public void onSubscriptionsChanged() {
            logd("onSubscriptionsChanged");
            DataDispatcherUtil.ImsBearerRequest n1 = this.mImsNetworkRequests.get(VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_REQ);
            DataDispatcherUtil.ImsBearerRequest n2 = this.mImsNetworkRequests.get(VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_REQ);
            if (n1 != null || n2 != null) {
                sendMessage(obtainMessage(600001));
            }
        }

        public void onImsRequestTimeout() {
            logd("onImsRequestTimeout");
            DataDispatcherUtil.ImsBearerRequest n1 = this.mImsNetworkRequests.get(VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_REQ);
            if (n1 != null) {
                logd("get request type " + n1.getCapability());
                if (n1.getCapability() == "ims") {
                    Handler imsHandle = DataDispatcher.this.mImsConnection.getHandler();
                    imsHandle.sendMessage(imsHandle.obtainMessage(DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_FAIL, 65536));
                    DataDispatcher.this.mHandler.removeMessages(DataDispatcher.MSG_ON_NOTIFY_ACTIVE_DATA_TIMEOUT);
                }
            }
        }

        private void refreshNwLostCallBack(NetworkRequest nwRequest) {
            logd("refreshNwLostCallBack nwRequest: " + nwRequest);
            try {
                this.mConnectivityManager.unregisterNetworkCallback(this.mNwLostCallback);
            } catch (IllegalArgumentException e) {
                loge("cb already has been released!!");
            }
            this.mConnectivityManager.registerNetworkCallback(nwRequest, this.mNwLostCallback);
        }

        private class NwAvailableCallback extends ConnectivityManager.NetworkCallback {
            private DataConnection mConn;

            public NwAvailableCallback(DataConnection conn) {
                this.mConn = conn;
            }

            public void onAvailable(Network network) {
                if (network == null) {
                    DataConnection.this.loge("onAvailable: network is null");
                    return;
                }
                DataDispatcher.this.mHandler.removeMessages(DataDispatcher.MSG_ON_NOTIFY_ACTIVE_DATA_TIMEOUT);
                LinkProperties mLink = DataConnection.this.mConnectivityManager.getLinkProperties(network);
                if (mLink == null) {
                    DataConnection.this.loge("LinkProperties is null");
                    return;
                }
                NetworkInfo netInfo = DataConnection.this.mConnectivityManager.getNetworkInfo(network);
                DataConnection dataConnection = DataConnection.this;
                dataConnection.logd("onAvailable: networInfo: " + netInfo);
                int unused = DataConnection.this.mNetworkId = Integer.valueOf(network.toString()).intValue();
                long unused2 = DataConnection.this.mNetworkHandle = network.getNetworkHandle();
                String unused3 = DataConnection.this.mInterface = mLink.getInterfaceName();
                DataConnection dataConnection2 = this.mConn;
                dataConnection2.sendMessage(dataConnection2.obtainMessage(DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_COMPLETED));
            }
        }

        private class NwLostCallback extends ConnectivityManager.NetworkCallback {
            private DataConnection mConn;

            public NwLostCallback(DataConnection conn) {
                this.mConn = conn;
            }

            public void onLost(Network network) {
                DataDispatcher.this.mHandler.removeMessages(DataDispatcher.MSG_ON_NOTIFY_ACTIVE_DATA_TIMEOUT);
                NetworkInfo netInfo = DataConnection.this.mConnectivityManager.getNetworkInfo(network);
                DataConnection dataConnection = DataConnection.this;
                dataConnection.logd("onLost: networInfo: " + netInfo);
                DataConnection dataConnection2 = this.mConn;
                dataConnection2.sendMessage(dataConnection2.obtainMessage(DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_COMPLETED));
            }
        }

        /* access modifiers changed from: private */
        public String msgToString(int msg) {
            switch (msg) {
                case 700001:
                    return "MSG_ID_IMSA_DISABLE_SERVICE";
                case DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_COMPLETED /*800001*/:
                    return "MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_COMPLETED";
                case DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_COMPLETED /*800002*/:
                    return "MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_COMPLETED";
                case DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_FAIL /*800003*/:
                    return "MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_FAIL";
                case DataDispatcher.MSG_ID_WRAP_IMSM_IMSPA_PDN_ABORT /*800004*/:
                    return "MSG_ID_WRAP_IMSM_IMSPA_PDN_ABORT";
                case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_REQ:
                    return "MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_REQ";
                case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_REQ:
                    return "MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_REQ";
                case VaConstants.MSG_ID_REQUEST_PCSCF_DISCOVERY:
                    return "MSG_ID_REQUEST_PCSCF_DISCOVERY";
                default:
                    return DataDispatcher.FAILCAUSE_NONE + msg;
            }
        }

        public void logd(String s) {
            Rlog.d(this.TAG + getName(), "[" + DataDispatcher.this.mPhoneId + "] " + s);
        }

        public void logi(String s) {
            Rlog.i(this.TAG + getName(), "[" + DataDispatcher.this.mPhoneId + "] " + s);
        }

        public void loge(String s) {
            Rlog.e(this.TAG + getName(), "[" + DataDispatcher.this.mPhoneId + "] " + s);
        }

        /* access modifiers changed from: private */
        public void setFirewallInterfaceChain(final boolean isAdded) {
            logd("setFirewallInterfaceChain:" + isAdded);
            new Thread("setFirewallInterfaceChain") {
                public void run() {
                    try {
                        INetdagent agent = INetdagent.getService();
                        if (agent == null) {
                            DataConnection.this.loge("agnet is null");
                            return;
                        }
                        String cmd = String.format("netdagent firewall set_interface_for_chain_rule %s dozable %s", new Object[]{DataConnection.this.mFwInterface, isAdded ? "allow" : "deny"});
                        DataConnection dataConnection = DataConnection.this;
                        dataConnection.logd("cmd:" + cmd);
                        agent.dispatchNetdagentCmd(cmd);
                    } catch (Exception e) {
                        DataConnection dataConnection2 = DataConnection.this;
                        dataConnection2.loge("setFirewallInterfaceChain:" + e);
                    }
                }
            }.start();
        }
    }
}
