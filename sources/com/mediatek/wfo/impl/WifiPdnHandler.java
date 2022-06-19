package com.mediatek.wfo.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.mediatek.ims.ImsConstants;
import com.mediatek.wfo.ril.MwiRIL;
import com.mediatek.wfo.util.PacketKeepAliveProcessor;
import com.mediatek.wfo.util.RssiMonitoringProcessor;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class WifiPdnHandler extends Handler {
    private static final int BASE = 1000;
    private static final int DEFAULT_MTU_SIZE = 1500;
    private static final int EVENT_GET_WIFI_CONN_STATE_SUPPORT_INFO = 1017;
    public static final int EVENT_HANDLE_AIRPLANE_MODE = 1152;
    private static final int EVENT_INITIALIZE = 1015;
    private static final int EVENT_MULTI_SIM_CONFIG_CHANGED = 1016;
    public static final int EVENT_ON_NATT_KEEP_ALIVE_CHANGED = 1103;
    public static final int EVENT_ON_WIFI_LOCK = 1153;
    public static final int EVENT_ON_WIFI_MONITORING_THRESHOLD_CHANGED = 1101;
    public static final int EVENT_ON_WIFI_PDN_ACTIVATE = 1102;
    public static final int EVENT_ON_WIFI_PING_REQUEST = 1150;
    private static final int EVENT_RADIO_AVAILABLE = 1005;
    private static final int EVENT_RETRY_CHECK_IF_START_WIFI_SCAN = 1010;
    private static final int EVENT_RETRY_INIT = 1008;
    private static final int EVENT_RETRY_UPDATE_LAST_RSSI = 1011;
    private static final int EVENT_RETRY_UPDATE_WIFI_CONNTECTED_INFO = 1009;
    public static final int EVENT_SET_NATT_STATUS = 1007;
    private static final int EVENT_SET_WIFI_ASSOC = 1003;
    private static final int EVENT_SET_WIFI_ENABLED = 1002;
    private static final int EVENT_SET_WIFI_IP_ADDR = 1004;
    public static final int EVENT_SET_WIFI_PING_RESULT = 1151;
    private static final int EVENT_SET_WIFI_SIGNAL_STRENGTH = 1001;
    private static final int EVENT_SET_WIFI_UE_MAC = 1014;
    private static final int EVENT_WIFI_NETWORK_STATE_CHANGE = 1000;
    private static final int EVENT_WIFI_SCAN = 1006;
    private static final int EVENT_WIFI_SCAN_AVAILABLE = 1012;
    private static final int EVENT_WIFI_STATE_CHANGE = 1013;
    private static final int EWIFIEN_AP_MODE_STATE = 8;
    private static final int EWIFIEN_CAUSE = 1;
    private static final int EWIFIEN_NEED_SEND_AP_MODE = 16;
    private static final int EWIFIEN_NEED_SEND_WIFI_ENABLED = 4;
    private static final int EWIFIEN_WIFI_ENABLED_STATE = 2;
    private static final String EXTRA_WFC_STATUS_KEY = "wfc_status";
    private static final int MAX_RETRY_COUNT = 3;
    private static final String MTK_KEY_WOS_SUPPORT_WFC_IN_FLIGHTMODE = "wos_flight_mode_support_bool";
    private static final int NEED_DEFER = 1;
    private static final String NONE_SSID = "<unknown ssid>";
    private static final int NO_NEED_DEFER = 0;
    private static final int PING_PASS_LATENCY = 50;
    private static final int PING_PASS_LOSS_RATE = 0;
    private static final String PROPERTY_MIMS_SUPPORT = "persist.vendor.mims_support";
    private static final String PROPERTY_WFC_ENABLE = "persist.vendor.mtk.wfc.enable";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final int RESPONSE_GET_WIFI_CONN_STATE_SUPPORT_INFO = 1207;
    private static final int RESPONSE_SET_NATT_KEEP_ALIVE_STATUS = 1204;
    private static final int RESPONSE_SET_WIFI_ASSOC = 1202;
    private static final int RESPONSE_SET_WIFI_ENABLED = 1200;
    private static final int RESPONSE_SET_WIFI_IP_ADDR = 1203;
    private static final int RESPONSE_SET_WIFI_PING_RESULT = 1205;
    private static final int RESPONSE_SET_WIFI_SIGNAL_LEVEL = 1201;
    private static final int RESPONSE_SET_WIFI_UE_MAC = 1206;
    private static final int RETRY_TIMEOUT = 1000;
    public static final int SNR_UNKNOWN = 60;
    private static final String TAG = "WifiPdnHandler";
    private static final boolean TELDBG;
    private static final boolean USR_BUILD = (TextUtils.equals(Build.TYPE, "user") || TextUtils.equals(Build.TYPE, "userdebug"));
    private static final int WFC_NOTIFY_GO = 2;
    private static final String WFC_REQUEST_PARTIAL_SCAN = "com.mediatek.intent.action.WFC_REQUEST_PARTIAL_SCAN";
    private static final String WFC_STATUS_CHANGED = "com.mediatek.intent.action.WFC_STATUS_CHANGED";
    private static final String WIFI_IF_NAME = "wlan0";
    private static final int WIFI_SCAN_DELAY = 3000;
    private static final int WIFI_STATE_UI_DISABLING = 9900;
    private static String mWifiUeMac = "02:00:00:00:00:00";
    /* access modifiers changed from: private */
    public ConnectivityManager mConnectivityManager;
    private Context mContext;
    private boolean mDeferredNotificationToWifi = false;
    private List<InetAddress> mDnsServers = null;
    private boolean mHasWiFiDisabledPending;
    private String mIfName = "";
    private boolean mIsAirplaneModeChange = false;
    /* access modifiers changed from: private */
    public boolean mIsAirplaneModeOn = false;
    /* access modifiers changed from: private */
    public boolean mIsWifiConnected = false;
    private boolean mIsWifiEnabled;
    /* access modifiers changed from: private */
    public int mLastRssi;
    private Object mLock = new Object();
    private boolean[] mModemReqWifiLock;
    private int mMtu = 0;
    private MwiRIL[] mMwiRil;
    /* access modifiers changed from: private */
    public Network mNetwork = null;
    /* access modifiers changed from: private */
    public boolean mOldWifiConnectedFromNetworkInfo = false;
    private PacketKeepAliveProcessor mPacketKeepAliveProcessor;
    private ArrayList<Message> mPendingMsgs = new ArrayList<>();
    private int mRadioState = 2;
    private int[] mRatType;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                WifiPdnHandler.this.log("onReceive action:" + intent.getAction());
                if (intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                    WifiPdnHandler.this.obtainMessage(1013, intent.getIntExtra("wifi_state", 4), 0).sendToTarget();
                } else if (intent.getAction().equals("android.intent.action.AIRPLANE_MODE")) {
                    boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
                    WifiPdnHandler.this.log("ACTION_AIRPLANE_MODE_CHANGED isAirplaneModeOn: " + isAirplaneModeOn);
                    WifiPdnHandler.this.sendMessage(WifiPdnHandler.this.obtainMessage(WifiPdnHandler.EVENT_HANDLE_AIRPLANE_MODE));
                } else if (intent.getAction().equals("android.net.wifi.action.WIFI_SCAN_AVAILABILITY_CHANGED")) {
                    Rlog.d(WifiPdnHandler.TAG, "Receive WIFI_SCAN_AVAILABLE, state: " + intent.getBooleanExtra("android.net.wifi.extra.SCAN_AVAILABLE", false));
                    WifiPdnHandler.this.sendMessage(WifiPdnHandler.this.obtainMessage(1012));
                } else if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    int i = 1;
                    boolean isWifiConnected = networkInfo != null && networkInfo.isConnected();
                    WifiPdnHandler.this.log("Receive NETWORK_STATE_CHANGED_ACTION, mIsWifiConnected: " + WifiPdnHandler.this.mIsWifiConnected + " networkInfo.isConnected(): " + isWifiConnected);
                    if (WifiPdnHandler.this.mIsWifiConnected && WifiPdnHandler.this.mOldWifiConnectedFromNetworkInfo) {
                        WifiPdnHandler wifiPdnHandler = WifiPdnHandler.this;
                        if (!isWifiConnected) {
                            i = 0;
                        }
                        WifiPdnHandler.this.sendMessage(wifiPdnHandler.obtainMessage(1000, i, 0, (Object) null));
                    }
                    boolean unused = WifiPdnHandler.this.mOldWifiConnectedFromNetworkInfo = isWifiConnected;
                } else if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    WifiPdnHandler.this.log("ACTION_CARRIER_CONFIG_CHANGED mIsAirplaneModeOn: " + WifiPdnHandler.this.mIsAirplaneModeOn);
                    if (WifiPdnHandler.this.mIsAirplaneModeOn) {
                        WifiPdnHandler.this.setWifiEnabled();
                    }
                }
            }
        }
    };
    private int mRetryCount = 0;
    /* access modifiers changed from: private */
    public boolean mRssiChange = false;
    private RssiMonitoringProcessor mRssiMonitoringProcessor;
    private int mSimCount;
    private String mSsid = "";
    private TelephonyManager mTelephonyManager;
    private WfcHandler mWfcHandler = null;
    private String mWifiApMac = "";
    /* access modifiers changed from: private */
    public int mWifiConnState = WifiConnState.DISCONNECTED.ordinal();
    private int mWifiConnStateSupportInfo = MwiRIL.WfcFeatureState.WFC_FEATURE_UNKNOWN.ordinal();
    private String mWifiIpv4Address = "";
    private String mWifiIpv4Gateway = "";
    private int mWifiIpv4PrefixLen = -1;
    private String mWifiIpv6Address = "";
    private String mWifiIpv6Gateway = "";
    private int mWifiIpv6PrefixLen = -1;
    private WifiManager.WifiLock mWifiLock;
    private int mWifiLockCount = 0;
    private WifiManager mWifiManager;
    private boolean[] mWifiPdnExisted;

    private enum WifiConnState {
        DISCONNECTED,
        VALIDATED_CONNECTED,
        DEFAULT_NETWORK_VALIDATED_CONNECTED
    }

    enum WifiLockSource {
        WIFI_STATE_CHANGE,
        MODEM_STATE_CHANGE
    }

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        TELDBG = z;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1000:
                int isConnected = msg.arg1;
                if (isConnected == 0) {
                    log("wifi is disconnect, notify packet keep alive to stop");
                    this.mPacketKeepAliveProcessor.notifyWifiDisconnect();
                } else {
                    String currentUeMac = updateWlanMacAddr();
                    if (!TextUtils.equals(currentUeMac, "02:00:00:00:00:00") && !TextUtils.equals(mWifiUeMac, currentUeMac)) {
                        mWifiUeMac = currentUeMac;
                        log("WifiPdnHandler updateWlanMacAddr mWifiUeMac: " + maskString(mWifiUeMac));
                    }
                }
                updateWifiConnectedInfo(isConnected);
                return;
            case 1001:
                if (this.mIsWifiConnected) {
                    setWifiSignalLevel();
                    this.mRssiChange = false;
                    return;
                }
                return;
            case 1002:
                setWifiEnabled();
                return;
            case 1003:
                setWifiAssoc();
                return;
            case 1004:
                setWifiIpAddress();
                return;
            case 1005:
                log("Sync airplane mode to MD: " + this.mIsAirplaneModeChange);
                if (this.mIsAirplaneModeChange) {
                    this.mIsAirplaneModeChange = false;
                    allowWfcInAirplaneMode();
                    setWifiEnabledWithSyncAPMode();
                }
                setWifiAssoc();
                setWifiEnabled();
                if (this.mIsWifiConnected) {
                    setWifiSignalLevel();
                    setWifiIpAddress();
                    this.mRssiChange = false;
                    return;
                }
                return;
            case 1006:
                checkIfstartWifiScan(true);
                return;
            case 1007:
                setNattKeepAliveStatus(msg);
                return;
            case 1008:
                initWifiManager();
                return;
            case 1009:
                updateWifiConnectedInfo(msg.arg1);
                return;
            case 1010:
                boolean scanImmediately = ((Boolean) msg.obj).booleanValue();
                Rlog.d(TAG, "Retry checkIfstartWifiScan, scanImmediately: " + scanImmediately);
                checkIfstartWifiScan(scanImmediately);
                return;
            case 1011:
                updateLastRssi();
                setWifiSignalLevel();
                return;
            case 1012:
                synchronized (this.mLock) {
                    Iterator<Message> it = this.mPendingMsgs.iterator();
                    while (it.hasNext()) {
                        Message retryMsg = it.next();
                        log("Retry: " + messageToString(retryMsg));
                        retryMsg.sendToTarget();
                    }
                    this.mPendingMsgs.clear();
                }
                return;
            case 1013:
                handleWifiStateChange(msg.arg1);
                return;
            case 1014:
                setWifiUeMac();
                return;
            case 1015:
                initialize();
                return;
            case 1016:
                int activeModemCount = ((Integer) msg.obj).intValue();
                log("EVENT_MULTI_SIM_CONFIG_CHANGED, activeModemCount: " + activeModemCount);
                onMultiSimConfigChanged(activeModemCount);
                return;
            case 1017:
                getWifiConnStateSupportInfo();
                return;
            case 1101:
                onWifiMonitoringThreshouldChanged(msg);
                return;
            case EVENT_ON_WIFI_PDN_ACTIVATE /*1102*/:
                onWifiPdnActivate(msg);
                return;
            case EVENT_ON_NATT_KEEP_ALIVE_CHANGED /*1103*/:
                onNattKeepAliveChanged(msg);
                return;
            case EVENT_ON_WIFI_PING_REQUEST /*1150*/:
                onWifiPingRequest(msg);
                return;
            case EVENT_SET_WIFI_PING_RESULT /*1151*/:
                setWifiPingResult(msg);
                return;
            case EVENT_HANDLE_AIRPLANE_MODE /*1152*/:
                handleAirplaneMode();
                return;
            case EVENT_ON_WIFI_LOCK /*1153*/:
                onWifiLock(msg);
                return;
            case RESPONSE_SET_WIFI_ENABLED /*1200*/:
                handleResponse(1002, (AsyncResult) msg.obj);
                return;
            case 1201:
                handleResponse(1001, (AsyncResult) msg.obj);
                return;
            case 1202:
                handleResponse(1003, (AsyncResult) msg.obj);
                return;
            case 1203:
                handleResponse(1004, (AsyncResult) msg.obj);
                return;
            case RESPONSE_SET_NATT_KEEP_ALIVE_STATUS /*1204*/:
                handleResponse(1007, (AsyncResult) msg.obj);
                return;
            case RESPONSE_SET_WIFI_PING_RESULT /*1205*/:
                handleResponse(EVENT_SET_WIFI_PING_RESULT, (AsyncResult) msg.obj);
                return;
            case RESPONSE_SET_WIFI_UE_MAC /*1206*/:
                handleResponse(1014, (AsyncResult) msg.obj);
                return;
            case RESPONSE_GET_WIFI_CONN_STATE_SUPPORT_INFO /*1207*/:
                handleResponseWifiConnStateSupportInfo((AsyncResult) msg.obj);
                return;
            default:
                return;
        }
    }

    private void getWifiConnStateSupportInfo() {
        log("getWifiConnStateSupportInfo");
        getMwiRil().getWfcConfig(MwiRIL.WfcConfigType.WFC_SETTING_WIFI_CONN_STATE.ordinal(), obtainMessage(RESPONSE_GET_WIFI_CONN_STATE_SUPPORT_INFO));
    }

    private void handleWifiStateChange(int wifiState) {
        if (this.mWifiManager == null) {
            log("Unexpected error, mWifiManager is null!");
            Message msg = obtainMessage(1013, wifiState, 0);
            synchronized (this.mLock) {
                this.mPendingMsgs.add(msg);
            }
            return;
        }
        log("handleWifiStateChange wifiState: " + wifiState);
        boolean isAirplaneModeOn = isAirPlaneMode();
        if (this.mIsAirplaneModeOn != isAirplaneModeOn) {
            this.mIsAirplaneModeOn = isAirplaneModeOn;
            this.mIsAirplaneModeChange = true;
            log("handleWifiStateChange change due to airplane mode change");
        }
        handleWifiDefferOff(WifiLockSource.WIFI_STATE_CHANGE, wifiState);
        if (wifiState == WIFI_STATE_UI_DISABLING) {
            this.mIsWifiEnabled = false;
            setWifiEnabled();
            return;
        }
        boolean isWifiEnabled = this.mWifiManager.isWifiEnabled();
        if (isWifiEnabled != this.mIsWifiEnabled) {
            this.mIsWifiEnabled = isWifiEnabled;
            setWifiEnabled();
        }
    }

    private void handleAirplaneMode() {
        boolean isAirplaneModeOn = false;
        try {
            isAirplaneModeOn = getAirplaneModeFromSettings();
        } catch (Settings.SettingNotFoundException e) {
            Rlog.e(TAG, "Can not get AIRPLANE_MODE_ON from provider.");
        }
        log("handleAirplaneMode mIsAirplaneModeOn: " + this.mIsAirplaneModeOn);
        if (this.mIsAirplaneModeOn != isAirplaneModeOn) {
            this.mIsAirplaneModeOn = isAirplaneModeOn;
        }
        this.mIsAirplaneModeChange = true;
    }

    private boolean getAirplaneModeFromSettings() throws Settings.SettingNotFoundException {
        try {
            boolean value = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on") == 1;
            log("getAirplaneModeFromSettings: " + value);
            if (!isEccInProgress() && this.mRadioState != 1) {
                return value;
            }
            log("Disable airplane mode after get from setting if radio is on");
            return false;
        } catch (Settings.SettingNotFoundException e) {
            Rlog.e(TAG, "Can not get AIRPLANE_MODE_ON from provider.");
            throw e;
        }
    }

    private void checkRadioPowerState() {
        Context context;
        if (this.mTelephonyManager == null && (context = this.mContext) != null) {
            this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        }
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            this.mRadioState = telephonyManager.getRadioPowerState();
        } else {
            log("mTelephonyManager is null, keep the original RadioState");
        }
        log("checkRadioPowerState: " + this.mRadioState);
    }

    private void handleResponse(int msgId, AsyncResult result) {
    }

    private void handleRetry(int msgId, AsyncResult result) {
        if (hasMessages(msgId)) {
            Rlog.e(TAG, "handleRetry already exist: " + msgId);
        } else if (result != null && result.exception != null) {
            sendEmptyMessageDelayed(msgId, 1000);
        }
    }

    private void handleResponseWifiConnStateSupportInfo(AsyncResult ar) {
        if (ar == null) {
            Rlog.e(TAG, "ResponseWifiConnStateSupportInfo no ar");
        } else if (ar.exception != null) {
            Rlog.e(TAG, "ResponseWifiConnStateSupportInfo exception: " + ar.exception + ", mRetryCount: " + this.mRetryCount);
            if (this.mRetryCount < 3) {
                sendEmptyMessageDelayed(1017, 1000);
                this.mRetryCount++;
            }
        } else {
            if (ar.result != null) {
                this.mWifiConnStateSupportInfo = ((Integer) ar.result).intValue();
                log("ResponseWifiConnStateSupportInfo: " + this.mWifiConnStateSupportInfo);
                if (this.mWifiConnStateSupportInfo == MwiRIL.WfcFeatureState.WFC_FEATURE_SUPPORTED.ordinal()) {
                    setupCallbacksForWifiStatusEx();
                }
            } else {
                Rlog.e(TAG, "ResponseWifiConnStateSupportInfo no result");
            }
            this.mRetryCount = 0;
        }
    }

    private String messageToString(Message msg) {
        switch (msg.what) {
            case 1000:
                return "EVENT_WIFI_NETWORK_STATE_CHANGE";
            case 1001:
                return "EVENT_SET_WIFI_SIGNAL_STRENGTH";
            case 1002:
                return "EVENT_SET_WIFI_ENABLED";
            case 1003:
                return "EVENT_SET_WIFI_ASSOC";
            case 1004:
                return "EVENT_SET_WIFI_IP_ADDR";
            case 1005:
                return "EVENT_RADIO_AVAILABLE";
            case 1006:
                return "EVENT_WIFI_SCAN";
            case 1007:
                return "EVENT_SET_NATT_STATUS";
            case 1008:
                return "EVENT_RETRY_INIT";
            case 1009:
                return "EVENT_RETRY_UPDATE_WIFI_CONNTECTED_INFO";
            case 1010:
                return "EVENT_RETRY_CHECK_IF_START_WIFI_SCAN";
            case 1011:
                return "EVENT_RETRY_UPDATE_LAST_RSSI";
            case 1012:
                return "EVENT_WIFI_SCAN_AVAILABLE";
            case 1013:
                return "EVENT_WIFI_STATE_CHANGE";
            case 1015:
                return "EVENT_INITIALIZE";
            case 1016:
                return "EVENT_MULTI_SIM_CONFIG_CHANGED";
            case 1017:
                return "EVENT_GET_WIFI_CONN_STATE_SUPPORT_INFO";
            case 1101:
                return "EVENT_ON_WIFI_MONITORING_THRESHOLD_CHANGED";
            case EVENT_ON_WIFI_PDN_ACTIVATE /*1102*/:
                return "EVENT_ON_WIFI_PDN_ACTIVATE";
            case EVENT_ON_NATT_KEEP_ALIVE_CHANGED /*1103*/:
                return "EVENT_ON_NATT_KEEP_ALIVE_CHANGED";
            case EVENT_ON_WIFI_PING_REQUEST /*1150*/:
                return "EVENT_ON_WIFI_PING_REQUEST";
            case EVENT_SET_WIFI_PING_RESULT /*1151*/:
                return "EVENT_SET_WIFI_PING_RESULT";
            case EVENT_HANDLE_AIRPLANE_MODE /*1152*/:
                return "EVENT_HANDLE_AIRPLANE_MODE";
            case EVENT_ON_WIFI_LOCK /*1153*/:
                return "EVENT_ON_WIFI_LOCK";
            case RESPONSE_SET_WIFI_ENABLED /*1200*/:
                return "RESPONSE_SET_WIFI_ENABLED";
            case 1201:
                return "RESPONSE_SET_WIFI_SIGNAL_LEVEL";
            case 1202:
                return "RESPONSE_SET_WIFI_ASSOC";
            case 1203:
                return "RESPONSE_SET_WIFI_IP_ADDR";
            case RESPONSE_SET_NATT_KEEP_ALIVE_STATUS /*1204*/:
                return "RESPONSE_SET_NATT_KEEP_ALIVE_STATUS";
            case RESPONSE_SET_WIFI_PING_RESULT /*1205*/:
                return "RESPONSE_SET_WIFI_PING_RESULT";
            case RESPONSE_GET_WIFI_CONN_STATE_SUPPORT_INFO /*1207*/:
                return "RESPONSE_GET_WIFI_CONN_STATE_SUPPORT_INFO";
            default:
                return "UNKNOWN:" + msg.what;
        }
    }

    public Handler getHandler() {
        return this;
    }

    public void handleRadioStateChanged(int simIdx, int intRadioState) {
        log("handleRadioStateChanged intRadioState: " + intRadioState);
        this.mRadioState = intRadioState;
        if (intRadioState != 2) {
            log("send EVENT_RADIO_AVAILABLE");
            if (isEccInProgress() || (intRadioState == 1 && this.mIsAirplaneModeOn)) {
                log("Disable airplane mode if radio is on");
                this.mIsAirplaneModeOn = false;
                this.mIsAirplaneModeChange = true;
            }
            if (intRadioState == 1) {
                getMwiRil().handleRadioProxyForRadioAvailable();
            }
            sendMessage(obtainMessage(1005));
        }
    }

    public WifiPdnHandler(Context context, int simCount, Looper looper, MwiRIL[] mwiRil) {
        super(looper);
        this.mContext = context;
        this.mSimCount = simCount;
        this.mMwiRil = mwiRil;
        obtainMessage(1015).sendToTarget();
        obtainMessage(1017).sendToTarget();
    }

    private void initialize() {
        initWifiManager();
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mConnectivityManager = connectivityManager;
        this.mRssiMonitoringProcessor = new RssiMonitoringProcessor(connectivityManager);
        this.mPacketKeepAliveProcessor = new PacketKeepAliveProcessor(this.mConnectivityManager, this, this.mContext);
        int i = this.mSimCount;
        this.mWifiPdnExisted = new boolean[i];
        this.mModemReqWifiLock = new boolean[i];
        this.mRatType = new int[i];
        this.mRssiMonitoringProcessor.initialize(i);
        boolean z = false;
        if (Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
            z = true;
        }
        this.mIsAirplaneModeOn = z;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        registerForBroadcast();
        registerIndication();
        setupCallbacksForWifiStatus();
        setWifiEnabled();
    }

    /* access modifiers changed from: protected */
    public void notifyMultiSimConfigChanged(int activeModemCount, MwiRIL[] mwiRil) {
        this.mMwiRil = mwiRil;
        obtainMessage(1016, Integer.valueOf(activeModemCount)).sendToTarget();
    }

    private void onMultiSimConfigChanged(int activeModemCount) {
        int prevActiveModemCount = this.mSimCount;
        Rlog.i(TAG, "notifyMultiSimConfigChanged, phone:" + prevActiveModemCount + "->" + activeModemCount + ", mSimCount:" + this.mSimCount);
        if (prevActiveModemCount != activeModemCount) {
            this.mSimCount = activeModemCount;
            if (prevActiveModemCount <= activeModemCount) {
                this.mWifiPdnExisted = Arrays.copyOf(this.mWifiPdnExisted, activeModemCount);
                this.mModemReqWifiLock = Arrays.copyOf(this.mModemReqWifiLock, activeModemCount);
                this.mRatType = Arrays.copyOf(this.mRatType, activeModemCount);
                this.mRssiMonitoringProcessor.notifyMultiSimConfigChanged(activeModemCount);
                for (int i = prevActiveModemCount; i < activeModemCount; i++) {
                    this.mMwiRil[i].registerRssiThresholdChanged(this, 1101, (Object) null);
                    this.mMwiRil[i].registerWifiPdnActivated(this, EVENT_ON_WIFI_PDN_ACTIVATE, (Object) null);
                    this.mMwiRil[i].registerNattKeepAliveChanged(this, EVENT_ON_NATT_KEEP_ALIVE_CHANGED, (Object) null);
                    this.mMwiRil[i].registerWifiPingRequest(this, EVENT_ON_WIFI_PING_REQUEST, (Object) null);
                    this.mMwiRil[i].registerWifiLock(this, EVENT_ON_WIFI_LOCK, (Object) null);
                }
            }
        }
    }

    private void initWifiManager() {
        if (this.mWifiManager == null) {
            Rlog.d(TAG, "initWifiManager.");
            if (!StorageManager.inCryptKeeperBounce()) {
                this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            }
            WifiManager wifiManager = this.mWifiManager;
            if (wifiManager != null) {
                boolean isWifiEnabled = wifiManager.isWifiEnabled();
                if (isWifiEnabled != this.mIsWifiEnabled) {
                    this.mIsWifiEnabled = isWifiEnabled;
                    setWifiEnabled();
                }
                WifiManager.WifiLock createWifiLock = this.mWifiManager.createWifiLock("WifiOffloadService-Wifi Lock");
                this.mWifiLock = createWifiLock;
                if (createWifiLock != null) {
                    createWifiLock.setReferenceCounted(false);
                    return;
                }
                return;
            }
            log("WifiManager null");
            this.mIsWifiEnabled = false;
            this.mWifiLock = null;
            Message msg = obtainMessage(1008);
            synchronized (this.mLock) {
                this.mPendingMsgs.add(msg);
            }
        }
    }

    public boolean isWifiConnected() {
        return this.mIsWifiConnected;
    }

    private void registerForBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        filter.addAction("android.net.wifi.action.WIFI_SCAN_AVAILABILITY_CHANGED");
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void registerIndication() {
        for (int i = 0; i < this.mSimCount; i++) {
            this.mMwiRil[i].registerRssiThresholdChanged(this, 1101, (Object) null);
            this.mMwiRil[i].registerWifiPdnActivated(this, EVENT_ON_WIFI_PDN_ACTIVATE, (Object) null);
            this.mMwiRil[i].registerNattKeepAliveChanged(this, EVENT_ON_NATT_KEEP_ALIVE_CHANGED, (Object) null);
            this.mMwiRil[i].registerWifiPingRequest(this, EVENT_ON_WIFI_PING_REQUEST, (Object) null);
            this.mMwiRil[i].registerWifiLock(this, EVENT_ON_WIFI_LOCK, (Object) null);
        }
    }

    private void onWifiMonitoringThreshouldChanged(Message msg) {
        int[] result = (int[]) ((AsyncResult) msg.obj).result;
        if (result == null) {
            Rlog.e(TAG, "onWifiMonitoringThreshouldChanged(): result is null");
            return;
        }
        boolean enable = false;
        if (result[0] == 1) {
            enable = true;
        }
        int length = result.length;
        int simIdx = result[length - 1];
        if (!enable) {
            log("Turn off RSSI monitoring");
            this.mRssiMonitoringProcessor.unregisterAllRssiMonitoring(simIdx);
            return;
        }
        int count = result[1];
        if (count + 2 + 1 < length) {
            Rlog.e(TAG, "onWifiMonitoringThreshouldChanged(): Bad params");
        } else if (!checkInvalidSimIdx(simIdx, "onWifiMonitoringThreshouldChanged: invalid SIM id")) {
            int[] rssi = new int[count];
            for (int i = 0; i < count; i++) {
                rssi[i] = result[i + 2];
                log("onWifiMonitoringThreshouldChanged(): rssi = " + rssi[i]);
            }
            onRssiMonitorRequest(simIdx, count, rssi);
            updateLastRssi();
            setWifiSignalLevel();
        }
    }

    private void onWifiPdnActivate(Message msg) {
        int[] result = (int[]) ((AsyncResult) msg.obj).result;
        if (result == null) {
            Rlog.e(TAG, "onWifiPdnActivate(): result is null");
        } else if (result.length < 2) {
            Rlog.e(TAG, "onWifiPdnActivate(): Bad params");
        } else {
            int pdnCount = result[0];
            boolean z = true;
            int simIdx = result[1];
            if (!checkInvalidSimIdx(simIdx, "onWifiPdnActivate(): invalid SIM id")) {
                boolean preWifiPdnExited = isWifiPdnExisted();
                if (SystemProperties.getInt(PROPERTY_MIMS_SUPPORT, 0) < 2) {
                    log("MIMS does not support, sync up pdn status to all slots.");
                    for (int i = 0; i < this.mSimCount; i++) {
                        this.mWifiPdnExisted[i] = pdnCount > 0;
                    }
                } else {
                    log("MIMS supported, update pdn status to specific slot[" + simIdx + "].");
                    boolean[] zArr = this.mWifiPdnExisted;
                    if (pdnCount <= 0) {
                        z = false;
                    }
                    zArr[simIdx] = z;
                }
                checkIfstartWifiScan(false);
                if (preWifiPdnExited != isWifiPdnExisted()) {
                    handleWifiDefferOff(WifiLockSource.MODEM_STATE_CHANGE, 0);
                }
            }
        }
    }

    private boolean isWifiDeferOffNeeded() {
        return isWifiPdnExisted() || isModemReqWifiLock();
    }

    /* renamed from: com.mediatek.wfo.impl.WifiPdnHandler$4 */
    static /* synthetic */ class C01954 {
        static final /* synthetic */ int[] $SwitchMap$com$mediatek$wfo$impl$WifiPdnHandler$WifiLockSource;

        static {
            int[] iArr = new int[WifiLockSource.values().length];
            $SwitchMap$com$mediatek$wfo$impl$WifiPdnHandler$WifiLockSource = iArr;
            try {
                iArr[WifiLockSource.MODEM_STATE_CHANGE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mediatek$wfo$impl$WifiPdnHandler$WifiLockSource[WifiLockSource.WIFI_STATE_CHANGE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    private void handleWifiDefferOff(WifiLockSource source, int state) {
        switch (C01954.$SwitchMap$com$mediatek$wfo$impl$WifiPdnHandler$WifiLockSource[source.ordinal()]) {
            case 1:
                if (!this.mDeferredNotificationToWifi && isWifiDeferOffNeeded()) {
                    broadcastWfcStatusIntent(1);
                    WifiManager.WifiLock wifiLock = this.mWifiLock;
                    if (wifiLock != null) {
                        wifiLock.acquire();
                        this.mWifiLockCount++;
                    }
                    this.mDeferredNotificationToWifi = true;
                }
                if (this.mHasWiFiDisabledPending && !isWifiDeferOffNeeded()) {
                    this.mHasWiFiDisabledPending = false;
                    broadcastWfcStatusIntent(2);
                }
                if (this.mDeferredNotificationToWifi && !isWifiDeferOffNeeded()) {
                    this.mDeferredNotificationToWifi = false;
                    broadcastWfcStatusIntent(0);
                    WifiManager.WifiLock wifiLock2 = this.mWifiLock;
                    if (wifiLock2 != null) {
                        wifiLock2.release();
                        int i = this.mWifiLockCount - 1;
                        this.mWifiLockCount = i;
                        if (i > 0) {
                            log("Warning: mWifiLockCount: " + this.mWifiLockCount);
                            break;
                        }
                    }
                }
                break;
            case 2:
                if (state != WIFI_STATE_UI_DISABLING) {
                    if (state != 1) {
                        if (state == 3 && !this.mDeferredNotificationToWifi && isWifiDeferOffNeeded()) {
                            broadcastWfcStatusIntent(1);
                            WifiManager.WifiLock wifiLock3 = this.mWifiLock;
                            if (wifiLock3 != null) {
                                wifiLock3.acquire();
                                this.mWifiLockCount++;
                            }
                            this.mDeferredNotificationToWifi = true;
                            break;
                        }
                    } else {
                        if (this.mHasWiFiDisabledPending) {
                            this.mHasWiFiDisabledPending = false;
                        }
                        if (this.mDeferredNotificationToWifi) {
                            this.mDeferredNotificationToWifi = false;
                            log("Wi-Fi fwk automaticlly disable defer Wi-Fi off process due to timeout");
                            WifiManager.WifiLock wifiLock4 = this.mWifiLock;
                            if (wifiLock4 != null) {
                                wifiLock4.release();
                                int i2 = this.mWifiLockCount - 1;
                                this.mWifiLockCount = i2;
                                if (i2 > 0) {
                                    log("Warning: mWifiLockCount: " + this.mWifiLockCount);
                                    break;
                                }
                            }
                        }
                    }
                } else if (!this.mHasWiFiDisabledPending) {
                    if (!isWifiDeferOffNeeded()) {
                        broadcastWfcStatusIntent(2);
                        break;
                    } else {
                        this.mHasWiFiDisabledPending = true;
                        break;
                    }
                }
                break;
        }
        log("new handleWifiDefferOff(): WifiLockSource: " + source + " state: " + state + " mHasWiFiDisabledPending: " + this.mHasWiFiDisabledPending + " isWifiDeferOffNeeded(): " + isWifiDeferOffNeeded() + " mDeferredNotificationToWifi: " + this.mDeferredNotificationToWifi);
    }

    private void onNattKeepAliveChanged(Message msg) {
        this.mPacketKeepAliveProcessor.handleKeepAliveChanged((String[]) ((AsyncResult) msg.obj).result);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:156:0x0395, code lost:
        r0 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateWifiConnectedInfo(int r27) {
        /*
            r26 = this;
            r1 = r26
            r2 = r27
            r0 = 0
            r3 = 0
            com.mediatek.wfo.impl.WifiPdnHandler$WifiConnState r4 = com.mediatek.wfo.impl.WifiPdnHandler.WifiConnState.DISCONNECTED
            int r4 = r4.ordinal()
            r5 = 0
            if (r2 != r4) goto L_0x004e
            com.mediatek.wfo.impl.WfcHandler r4 = r1.mWfcHandler
            if (r4 == 0) goto L_0x0016
            r4.updatedWifiConnectedStatus(r5)
        L_0x0016:
            boolean r4 = r1.mIsWifiConnected
            if (r4 == 0) goto L_0x035d
            r1.mIsWifiConnected = r5
            com.mediatek.wfo.impl.WifiPdnHandler$WifiConnState r4 = com.mediatek.wfo.impl.WifiPdnHandler.WifiConnState.DISCONNECTED
            int r4 = r4.ordinal()
            r1.mWifiConnState = r4
            java.lang.String r4 = ""
            r1.mWifiApMac = r4
            java.lang.String r4 = ""
            r1.mWifiIpv4Address = r4
            java.lang.String r4 = ""
            r1.mWifiIpv6Address = r4
            java.lang.String r4 = ""
            r1.mWifiIpv4Gateway = r4
            java.lang.String r4 = ""
            r1.mWifiIpv6Gateway = r4
            r4 = -1
            r1.mWifiIpv4PrefixLen = r4
            r1.mWifiIpv6PrefixLen = r4
            r4 = 0
            r1.mDnsServers = r4
            java.lang.String r4 = ""
            r1.mIfName = r4
            java.lang.String r4 = ""
            r1.mSsid = r4
            r1.mMtu = r5
            r0 = 1
            r3 = 1
            goto L_0x035d
        L_0x004e:
            com.mediatek.wfo.impl.WfcHandler r4 = r1.mWfcHandler
            r6 = 1
            if (r4 == 0) goto L_0x0056
            r4.updatedWifiConnectedStatus(r6)
        L_0x0056:
            java.lang.String r4 = ""
            java.lang.String r7 = ""
            java.lang.String r8 = ""
            java.lang.String r9 = ""
            java.lang.String r10 = ""
            r11 = -1
            r12 = -1
            r13 = 0
            r14 = 0
            java.lang.String r15 = ""
            java.lang.String r16 = ""
            r17 = 0
            r18 = 0
            com.mediatek.wfo.impl.WifiPdnHandler$WifiConnState r19 = com.mediatek.wfo.impl.WifiPdnHandler.WifiConnState.DEFAULT_NETWORK_VALIDATED_CONNECTED
            int r5 = r19.ordinal()
            if (r2 != r5) goto L_0x0085
            boolean r5 = r1.mIsWifiConnected
            if (r5 != 0) goto L_0x0079
            r0 = 1
        L_0x0079:
            r1.mIsWifiConnected = r6
            com.mediatek.wfo.impl.WifiPdnHandler$WifiConnState r5 = com.mediatek.wfo.impl.WifiPdnHandler.WifiConnState.VALIDATED_CONNECTED
            int r5 = r5.ordinal()
            r1.mWifiConnState = r5
            r5 = r0
            goto L_0x00ad
        L_0x0085:
            com.mediatek.wfo.impl.WifiPdnHandler$WifiConnState r5 = com.mediatek.wfo.impl.WifiPdnHandler.WifiConnState.VALIDATED_CONNECTED
            int r5 = r5.ordinal()
            if (r2 != r5) goto L_0x00ac
            int r5 = r1.mWifiConnState
            com.mediatek.wfo.impl.WifiPdnHandler$WifiConnState r19 = com.mediatek.wfo.impl.WifiPdnHandler.WifiConnState.VALIDATED_CONNECTED
            int r6 = r19.ordinal()
            if (r5 == r6) goto L_0x0098
            r0 = 1
        L_0x0098:
            com.mediatek.wfo.impl.WifiPdnHandler$WifiConnState r5 = com.mediatek.wfo.impl.WifiPdnHandler.WifiConnState.VALIDATED_CONNECTED
            int r5 = r5.ordinal()
            r1.mWifiConnState = r5
            boolean r5 = r1.mIsWifiConnected
            if (r5 == 0) goto L_0x00aa
            java.lang.String r5 = "updateWifiConnectedInfo: no need update"
            r1.log(r5)
            return
        L_0x00aa:
            r5 = r0
            goto L_0x00ad
        L_0x00ac:
            r5 = r0
        L_0x00ad:
            boolean r0 = r1.mIsWifiEnabled
            if (r0 != 0) goto L_0x00bf
            android.net.wifi.WifiManager r0 = r1.mWifiManager
            boolean r0 = r0.isWifiEnabled()
            if (r0 == 0) goto L_0x00bf
            r0 = 1
            r1.mIsWifiEnabled = r0
            r26.setWifiEnabled()
        L_0x00bf:
            r6 = 0
            android.net.wifi.WifiManager r0 = r1.mWifiManager
            if (r0 == 0) goto L_0x00cd
            android.net.wifi.WifiInfo r6 = r0.getConnectionInfo()
            r19 = r3
            r20 = r4
            goto L_0x00e1
        L_0x00cd:
            r0 = 1009(0x3f1, float:1.414E-42)
            r19 = r3
            r20 = r4
            r3 = 0
            android.os.Message r4 = r1.obtainMessage(r0, r2, r3)
            java.lang.Object r3 = r1.mLock
            monitor-enter(r3)
            java.util.ArrayList<android.os.Message> r0 = r1.mPendingMsgs     // Catch:{ all -> 0x0390 }
            r0.add(r4)     // Catch:{ all -> 0x0390 }
            monitor-exit(r3)     // Catch:{ all -> 0x0390 }
        L_0x00e1:
            if (r6 == 0) goto L_0x0139
            java.lang.String r4 = r6.getBSSID()
            java.lang.String r0 = r6.getSSID()
            java.lang.String r3 = "<unknown ssid>"
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x00fa
            java.lang.String r0 = ""
            java.lang.String r3 = "updateWifiConnectedInfo: <unknown ssid>"
            r1.log(r3)
        L_0x00fa:
            java.lang.String r10 = r1.updateSsidToHexString(r0)
            java.lang.String r3 = r1.mWifiApMac
            boolean r3 = r3.equals(r4)
            if (r3 != 0) goto L_0x0136
            java.lang.String r3 = r1.mWifiApMac
            boolean r3 = android.text.TextUtils.isEmpty(r3)
            if (r3 == 0) goto L_0x0110
            if (r4 == 0) goto L_0x013b
        L_0x0110:
            if (r4 != 0) goto L_0x0115
            java.lang.String r3 = ""
            goto L_0x0116
        L_0x0115:
            r3 = r4
        L_0x0116:
            r1.mWifiApMac = r3
            r5 = 1
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r20 = r0
            java.lang.String r0 = "updateWifiConnectedInfo(): mWifiApMac = "
            r3.append(r0)
            java.lang.String r0 = r1.mWifiApMac
            java.lang.String r0 = r1.maskString(r0)
            r3.append(r0)
            java.lang.String r0 = r3.toString()
            r1.log(r0)
            goto L_0x013b
        L_0x0136:
            r20 = r0
            goto L_0x013b
        L_0x0139:
            r4 = r20
        L_0x013b:
            android.net.ConnectivityManager r0 = r1.mConnectivityManager
            android.net.Network[] r0 = r0.getAllNetworks()
            int r3 = r0.length
            r20 = r4
            r21 = r5
            r22 = r6
            r2 = r16
            r4 = r17
            r5 = r18
            r6 = 0
        L_0x014f:
            if (r6 >= r3) goto L_0x0226
            r23 = r3
            r3 = r0[r6]
            r24 = r0
            android.net.ConnectivityManager r0 = r1.mConnectivityManager
            android.net.LinkProperties r0 = r0.getLinkProperties(r3)
            if (r0 == 0) goto L_0x0218
            java.lang.String r16 = r0.getInterfaceName()
            if (r16 == 0) goto L_0x0218
            r16 = r3
            java.lang.String r3 = r0.getInterfaceName()
            r25 = r13
            java.lang.String r13 = "wlan"
            boolean r3 = r3.startsWith(r13)
            if (r3 != 0) goto L_0x0177
            goto L_0x021c
        L_0x0177:
            java.util.List r3 = r0.getLinkAddresses()
            java.util.Iterator r3 = r3.iterator()
        L_0x017f:
            boolean r13 = r3.hasNext()
            if (r13 == 0) goto L_0x01c8
            java.lang.Object r13 = r3.next()
            android.net.LinkAddress r13 = (android.net.LinkAddress) r13
            r17 = r3
            java.net.InetAddress r3 = r13.getAddress()
            r18 = r7
            boolean r7 = r3 instanceof java.net.Inet4Address
            if (r7 == 0) goto L_0x01a6
            boolean r7 = r3.isLoopbackAddress()
            if (r7 != 0) goto L_0x01a6
            java.lang.String r7 = r3.getHostAddress()
            int r11 = r13.getPrefixLength()
            goto L_0x01c5
        L_0x01a6:
            boolean r7 = r3 instanceof java.net.Inet6Address
            if (r7 == 0) goto L_0x01c3
            boolean r7 = r3.isLinkLocalAddress()
            if (r7 != 0) goto L_0x01c3
            boolean r7 = r3.isLoopbackAddress()
            if (r7 != 0) goto L_0x01c3
            java.lang.String r7 = r3.getHostAddress()
            int r8 = r13.getPrefixLength()
            r12 = r8
            r8 = r7
            r7 = r18
            goto L_0x01c5
        L_0x01c3:
            r7 = r18
        L_0x01c5:
            r3 = r17
            goto L_0x017f
        L_0x01c8:
            r18 = r7
            java.util.List r3 = r0.getDnsServers()
            int r4 = r0.getMtu()
            java.util.List r5 = r0.getRoutes()
            java.util.Iterator r5 = r5.iterator()
        L_0x01da:
            boolean r7 = r5.hasNext()
            if (r7 == 0) goto L_0x020a
            java.lang.Object r7 = r5.next()
            android.net.RouteInfo r7 = (android.net.RouteInfo) r7
            java.net.InetAddress r13 = r7.getGateway()
            if (r13 == 0) goto L_0x01fa
            r17 = r2
            boolean r2 = r13 instanceof java.net.Inet4Address
            if (r2 == 0) goto L_0x01fc
            java.lang.String r2 = r13.getHostAddress()
            r15 = r2
            r2 = r17
            goto L_0x0209
        L_0x01fa:
            r17 = r2
        L_0x01fc:
            if (r13 == 0) goto L_0x0207
            boolean r2 = r13 instanceof java.net.Inet6Address
            if (r2 == 0) goto L_0x0207
            java.lang.String r2 = r13.getHostAddress()
            goto L_0x0209
        L_0x0207:
            r2 = r17
        L_0x0209:
            goto L_0x01da
        L_0x020a:
            r17 = r2
            java.lang.String r2 = r0.getInterfaceName()
            r9 = r2
            r5 = r4
            r2 = r17
            r7 = r18
            r4 = r3
            goto L_0x021c
        L_0x0218:
            r16 = r3
            r25 = r13
        L_0x021c:
            int r6 = r6 + 1
            r3 = r23
            r0 = r24
            r13 = r25
            goto L_0x014f
        L_0x0226:
            r25 = r13
            java.lang.String r0 = r1.mWifiIpv4Address
            boolean r0 = r0.equals(r7)
            if (r0 != 0) goto L_0x025c
            boolean r0 = android.text.TextUtils.isEmpty(r7)
            if (r0 == 0) goto L_0x0239
            java.lang.String r0 = ""
            goto L_0x023a
        L_0x0239:
            r0 = r7
        L_0x023a:
            r1.mWifiIpv4Address = r0
            r1.mWifiIpv4PrefixLen = r11
            r1.mWifiIpv4Gateway = r15
            r3 = 1
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r6 = "updateWifiConnectedInfo(): mWifiIpv4Address = "
            r0.append(r6)
            java.lang.String r6 = r1.mWifiIpv4Address
            java.lang.String r6 = r1.maskString(r6)
            r0.append(r6)
            java.lang.String r0 = r0.toString()
            r1.log(r0)
            goto L_0x025e
        L_0x025c:
            r3 = r19
        L_0x025e:
            java.lang.String r0 = r1.mWifiIpv6Address
            boolean r0 = r0.equals(r8)
            if (r0 != 0) goto L_0x0291
            boolean r0 = android.text.TextUtils.isEmpty(r8)
            if (r0 == 0) goto L_0x026f
            java.lang.String r0 = ""
            goto L_0x0270
        L_0x026f:
            r0 = r8
        L_0x0270:
            r1.mWifiIpv6Address = r0
            r1.mWifiIpv6PrefixLen = r12
            r1.mWifiIpv6Gateway = r2
            r3 = 1
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r6 = "updateWifiConnectedInfo(): mWifiIpv6Address = "
            r0.append(r6)
            java.lang.String r6 = r1.mWifiIpv6Address
            java.lang.String r6 = r1.maskString(r6)
            r0.append(r6)
            java.lang.String r0 = r0.toString()
            r1.log(r0)
        L_0x0291:
            java.lang.String r0 = r1.mIfName
            boolean r0 = r0.equals(r9)
            if (r0 != 0) goto L_0x02b9
            if (r9 != 0) goto L_0x029e
            java.lang.String r0 = ""
            goto L_0x029f
        L_0x029e:
            r0 = r9
        L_0x029f:
            r1.mIfName = r0
            r0 = 1
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r13 = "updateWifiConnectedInfo(): mIfName = "
            r6.append(r13)
            java.lang.String r13 = r1.mIfName
            r6.append(r13)
            java.lang.String r6 = r6.toString()
            r1.log(r6)
            goto L_0x02bb
        L_0x02b9:
            r0 = r21
        L_0x02bb:
            java.lang.String r6 = r1.mSsid
            boolean r6 = r6.equals(r10)
            if (r6 != 0) goto L_0x02e6
            if (r10 != 0) goto L_0x02c8
            java.lang.String r6 = ""
            goto L_0x02c9
        L_0x02c8:
            r6 = r10
        L_0x02c9:
            r1.mSsid = r6
            r0 = 1
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r13 = "updateWifiConnectedInfo(): mSsid = "
            r6.append(r13)
            java.lang.String r13 = r1.mSsid
            java.lang.String r13 = r1.maskString(r13)
            r6.append(r13)
            java.lang.String r6 = r6.toString()
            r1.log(r6)
        L_0x02e6:
            if (r4 == 0) goto L_0x0339
            java.util.List<java.net.InetAddress> r6 = r1.mDnsServers
            if (r6 != 0) goto L_0x0308
            r1.mDnsServers = r4
            r3 = 1
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r13 = "updateWifiConnectedInfo(): mDnsServers ="
            r6.append(r13)
            java.util.List<java.net.InetAddress> r13 = r1.mDnsServers
            r6.append(r13)
            java.lang.String r6 = r6.toString()
            r1.log(r6)
            r16 = r0
            goto L_0x033b
        L_0x0308:
            java.util.ArrayList r6 = new java.util.ArrayList
            java.util.List<java.net.InetAddress> r13 = r1.mDnsServers
            r6.<init>(r13)
            r6.retainAll(r4)
            int r13 = r6.size()
            r16 = r0
            int r0 = r4.size()
            if (r13 == r0) goto L_0x033b
            r0 = 1
            r1.mDnsServers = r4
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r13 = "updateWifiConnectedInfo(): mDnsServers ="
            r3.append(r13)
            java.util.List<java.net.InetAddress> r13 = r1.mDnsServers
            r3.append(r13)
            java.lang.String r3 = r3.toString()
            r1.log(r3)
            r3 = r0
            goto L_0x033b
        L_0x0339:
            r16 = r0
        L_0x033b:
            if (r5 < 0) goto L_0x035b
            int r0 = r1.mMtu
            if (r0 == r5) goto L_0x035b
            r1.mMtu = r5
            r0 = 1
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r13 = "updateWifiConnectedInfo(): mMtu = "
            r6.append(r13)
            int r13 = r1.mMtu
            r6.append(r13)
            java.lang.String r6 = r6.toString()
            r1.log(r6)
            goto L_0x035d
        L_0x035b:
            r0 = r16
        L_0x035d:
            r2 = 0
            r1.checkIfstartWifiScan(r2)
            if (r0 == 0) goto L_0x0366
            r26.setWifiAssoc()
        L_0x0366:
            if (r3 == 0) goto L_0x036b
            r26.setWifiIpAddress()
        L_0x036b:
            boolean r2 = r1.mIsWifiConnected
            if (r2 == 0) goto L_0x038c
            boolean r2 = r1.mRssiChange
            if (r2 == 0) goto L_0x038c
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "updateWifiConnectedInfo(): mRssiChange = "
            r2.append(r4)
            boolean r4 = r1.mRssiChange
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            r1.log(r2)
            r26.setWifiSignalLevel()
        L_0x038c:
            r2 = 0
            r1.mRssiChange = r2
            return
        L_0x0390:
            r0 = move-exception
            r25 = r13
        L_0x0393:
            monitor-exit(r3)     // Catch:{ all -> 0x0395 }
            throw r0
        L_0x0395:
            r0 = move-exception
            goto L_0x0393
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.wfo.impl.WifiPdnHandler.updateWifiConnectedInfo(int):void");
    }

    private int generateWifiEnableCause(boolean needAPMode, boolean apMode, boolean needWifiEnabled, boolean wifiEnabled, boolean bCause) {
        int cause = 0;
        if (needAPMode) {
            cause = 0 | 16;
        }
        int i = 0;
        int cause2 = cause | (apMode ? 8 : 0);
        if (needWifiEnabled) {
            cause2 |= 4;
        }
        if (wifiEnabled) {
            i = 2;
        }
        int cause3 = cause2 | i | bCause;
        log("generateWifiEnableCause(): " + ((int) cause3));
        return cause3;
    }

    /* access modifiers changed from: private */
    public void setWifiEnabled() {
        boolean isInEcc = isEccInProgress();
        log("setWifiEnabled(): " + this.mIsWifiEnabled + ", mIsAirplaneModeOn: " + this.mIsAirplaneModeOn + ", isEccInProgress: " + isInEcc);
        boolean wifiEnable = this.mIsWifiEnabled;
        getMwiRil().setWifiEnabled(WIFI_IF_NAME, wifiEnable, generateWifiEnableCause(false, this.mIsAirplaneModeOn, true, wifiEnable, SystemProperties.getInt("persist.vendor.mtk.wfc.enable", 0) >= 1 && this.mIsAirplaneModeOn && !isInEcc), obtainMessage(RESPONSE_SET_WIFI_ENABLED));
    }

    private void setWifiEnabledWithSyncAPMode() {
        Message result = obtainMessage(RESPONSE_SET_WIFI_ENABLED);
        boolean isInEcc = isEccInProgress();
        try {
            boolean isAirplaneModeOn = getAirplaneModeFromSettings();
            if (this.mIsAirplaneModeOn != isAirplaneModeOn) {
                this.mIsAirplaneModeOn = isAirplaneModeOn;
                log("setWifiEnabledWithSyncAPMode(): update mIsAirplaneModeOn from settings");
            }
        } catch (Settings.SettingNotFoundException e) {
            Rlog.e(TAG, "Can not get AIRPLANE_MODE_ON from provider.");
        }
        log("setWifiEnabledWithSyncAPMode(): " + this.mIsWifiEnabled + ", mIsAirplaneModeOn: " + this.mIsAirplaneModeOn + ", isEccInProgress: " + isInEcc);
        boolean bCause = SystemProperties.getInt("persist.vendor.mtk.wfc.enable", 0) >= 1 && this.mIsAirplaneModeOn && !isInEcc;
        getMwiRil().setWifiEnabled(WIFI_IF_NAME, this.mIsWifiEnabled ? 1 : 0, generateWifiEnableCause(true, bCause, true, this.mIsWifiEnabled, bCause), result);
    }

    private void allowWfcInAirplaneMode() {
        boolean isAirPlaneModeOn = isAirPlaneMode();
        if (SystemProperties.getInt(PROPERTY_MIMS_SUPPORT, 1) <= 1) {
            int phoneId = getMainCapabilityPhoneId();
            if (!getBooleanCarrierConfig(MTK_KEY_WOS_SUPPORT_WFC_IN_FLIGHTMODE, phoneId, true)) {
                updateWfcCapabilityByPhoneId(phoneId, isAirPlaneModeOn);
                return;
            }
            return;
        }
        for (int i = 0; i < this.mSimCount; i++) {
            if (!getBooleanCarrierConfig(MTK_KEY_WOS_SUPPORT_WFC_IN_FLIGHTMODE, i, true)) {
                updateWfcCapabilityByPhoneId(i, isAirPlaneModeOn);
            }
        }
    }

    private void updateWfcCapabilityByPhoneId(int phoneId, boolean isAirPlaneModeOn) {
        ImsManager imsMgr = ImsManager.getInstance(this.mContext, phoneId);
        boolean enabled = imsMgr.isWfcEnabledByUser();
        log("updateWfcCapabilityByPhoneId: phoneId = " + phoneId + ", enabled = " + enabled + ", ApMode = " + isAirPlaneModeOn);
        try {
            imsMgr.changeMmTelCapability(!isAirPlaneModeOn && enabled, 1, new int[]{1});
        } catch (ImsException e) {
            Rlog.e(TAG, "changeMmTelCapability failed.");
        }
    }

    private boolean getBooleanCarrierConfig(String key, int phoneId, boolean defaultValue) {
        boolean ret;
        if (this.mContext == null) {
            Rlog.e(TAG, "getBooleanCarrierConfig fail, mContext = null");
        }
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        int subId = getSubId(phoneId);
        log("getBooleanCarrierConfig: phoneId=" + phoneId + " subId=" + subId);
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        if (b != null) {
            ret = b.getBoolean(key, defaultValue);
        } else {
            log("getBooleanCarrierConfig: get from default config");
            ret = CarrierConfigManager.getDefaultConfig().getBoolean(key, defaultValue);
        }
        log("getBooleanCarrierConfig sub: " + subId + " key: " + key + " ret: " + ret);
        return ret;
    }

    private boolean isAirPlaneMode() {
        Context context = this.mContext;
        boolean z = false;
        if (context == null) {
            Rlog.e(TAG, "isAirPlaneMode: no context!");
            return false;
        }
        if (Settings.System.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0) {
            z = true;
        }
        boolean isAirPlaneMode = z;
        log("isAirPlaneMode: " + isAirPlaneMode);
        return isAirPlaneMode;
    }

    private int getSubId(int phoneId) {
        int[] subIds = SubscriptionManager.getSubId(phoneId);
        if (subIds == null || subIds.length < 1) {
            return -1;
        }
        return subIds[0];
    }

    public void setWifiOff() {
        this.mIsWifiEnabled = false;
        setWifiEnabled();
    }

    private void setWifiSignalLevel() {
        Message result = obtainMessage(1201);
        log("setWifiSignalLevel(): " + this.mLastRssi);
        getMwiRil().setWifiSignalLevel(this.mLastRssi, 60, result);
    }

    private void setWifiAssoc() {
        boolean isWifiConnected;
        Message result = obtainMessage(1202);
        int mtu = this.mMtu;
        int mtu2 = mtu == 0 ? DEFAULT_MTU_SIZE : mtu;
        int wifiConnState = this.mWifiConnState;
        log("setWifiAssoc() ifName: " + this.mIfName + " associated: " + this.mIsWifiConnected + " ssid: " + maskString(this.mSsid) + " apMac: " + maskString(this.mWifiApMac) + ", mtu = " + mtu2 + " wifiConnState: " + wifiConnState);
        boolean isWifiConnected2 = this.mIsWifiConnected;
        if (TextUtils.isEmpty(this.mSsid)) {
            isWifiConnected = false;
        } else {
            isWifiConnected = isWifiConnected2;
        }
        getMwiRil().setWifiAssociated(WIFI_IF_NAME, isWifiConnected, this.mSsid, this.mWifiApMac, mtu2, mWifiUeMac, wifiConnState, result);
    }

    private void setWifiIpAddress() {
        Message result = obtainMessage(1203);
        log("setWifiIpAddr() ifName: " + this.mIfName + " ipv4Addr: " + maskString(this.mWifiIpv4Address) + " ipv6Addr: " + maskString(this.mWifiIpv6Address) + " ipv4PrefixLen: " + this.mWifiIpv4PrefixLen + " ipv6PrefixLen: " + this.mWifiIpv6PrefixLen);
        List<InetAddress> list = this.mDnsServers;
        if (list != null) {
            int dnsCount = list.size();
            StringBuilder dnsServers = new StringBuilder();
            for (InetAddress address : this.mDnsServers) {
                if (dnsServers.length() > 0) {
                    dnsServers.append(",");
                }
                String dnsServerAddress = address.getHostAddress();
                dnsServers.append("\"");
                dnsServers.append(dnsServerAddress);
                dnsServers.append("\"");
                log("setWifiIpAddress(): dnsServerAddress: " + dnsServerAddress);
            }
            getMwiRil().setWifiIpAddress(WIFI_IF_NAME, this.mWifiIpv4Address, this.mWifiIpv6Address, this.mWifiIpv4PrefixLen, this.mWifiIpv6PrefixLen, this.mWifiIpv4Gateway, this.mWifiIpv6Gateway, dnsCount, dnsServers.toString(), result);
            return;
        }
        Rlog.e(TAG, "setWifiIpAddress(): mDnsServers = null");
        getMwiRil().setWifiIpAddress(WIFI_IF_NAME, this.mWifiIpv4Address, this.mWifiIpv6Address, this.mWifiIpv4PrefixLen, this.mWifiIpv6PrefixLen, this.mWifiIpv4Gateway, this.mWifiIpv6Gateway, 0, "\"\"", result);
    }

    private void setNattKeepAliveStatus(Message msg) {
        Message result = obtainMessage(RESPONSE_SET_NATT_KEEP_ALIVE_STATUS);
        PacketKeepAliveProcessor.KeepAliveConfig config = (PacketKeepAliveProcessor.KeepAliveConfig) msg.obj;
        getMwiRil().setNattKeepAliveStatus(WIFI_IF_NAME, config.isEnabled(), config.getSrcIp(), config.getSrcPort(), config.getDstIp(), config.getDstPort(), result);
    }

    private void setWifiPingResult(Message msg) {
        int pktLoss;
        int latency;
        Message result = obtainMessage(RESPONSE_SET_WIFI_PING_RESULT);
        PingData data = (PingData) msg.obj;
        if (this.mIsWifiConnected) {
            log("setWifiPingResult() As optr server has delayed response");
            pktLoss = 0;
            latency = 50;
        } else {
            latency = (int) data.getPingLatency();
            pktLoss = data.getPacketLoss();
        }
        int simIdx = msg.arg1;
        int rat = msg.arg2;
        log("setWifiPingResult() latency: = " + latency + ", packetLost: = " + pktLoss);
        this.mMwiRil[simIdx].setWifiPingResult(rat, latency, pktLoss, result);
    }

    private void setWifiUeMac() {
        getMwiRil().setWfcConfig_WifiUeMac(WIFI_IF_NAME, mWifiUeMac, obtainMessage(RESPONSE_SET_WIFI_UE_MAC));
    }

    private void checkIfstartWifiScan(boolean scanImmediately) {
        boolean wifiPdnExisted = isWifiPdnExisted();
        if (this.mIsWifiConnected || !wifiPdnExisted) {
            removeMessages(1006);
        } else if (scanImmediately) {
            log("call WifiManager.startScan()");
            WifiManager wifiManager = this.mWifiManager;
            if (wifiManager != null) {
                wifiManager.startScan();
                return;
            }
            Rlog.e(TAG, "checkIfstartWifiScan(): WifiManager null");
            Message msg = obtainMessage(1010, Boolean.valueOf(scanImmediately));
            synchronized (this.mLock) {
                this.mPendingMsgs.add(msg);
            }
        } else if (!hasMessages(1006)) {
            log("start 3s delay to trigger wifi scan");
            sendMessageDelayed(obtainMessage(1006), 3000);
        }
    }

    private void onRssiMonitorRequest(int simId, int size, int[] rssiThresholds) {
        this.mRssiMonitoringProcessor.registerRssiMonitoring(simId, size, rssiThresholds);
    }

    private void setupCallbacksForWifiStatusEx() {
        if (this.mConnectivityManager == null) {
            log("Unexpected error, mConnectivityManager = null");
            return;
        }
        this.mConnectivityManager.registerNetworkCallback(new NetworkRequest.Builder().addTransportType(1).addCapability(16).build(), new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                WifiPdnHandler wifiPdnHandler = WifiPdnHandler.this;
                wifiPdnHandler.log("[Gen98]WIFI onAvailable, mWifiConnState: " + WifiPdnHandler.this.mWifiConnState);
                if (WifiPdnHandler.this.mWifiConnState != WifiConnState.VALIDATED_CONNECTED.ordinal()) {
                    WifiPdnHandler.this.sendMessage(WifiPdnHandler.this.obtainMessage(1000, WifiConnState.VALIDATED_CONNECTED.ordinal(), 0, (Object) null));
                }
            }
        });
    }

    private void setupCallbacksForWifiStatus() {
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        if (connectivityManager == null) {
            log("Unexpected error, mConnectivityManager = null");
        } else {
            connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                public void onAvailable(Network network) {
                    NetworkCapabilities nc = WifiPdnHandler.this.mConnectivityManager.getNetworkCapabilities(network);
                    if (nc == null) {
                        WifiPdnHandler wifiPdnHandler = WifiPdnHandler.this;
                        wifiPdnHandler.log("Empty network capability:" + network);
                    } else if (!WifiPdnHandler.this.ignoreVpnCallback(nc, "onAvailable")) {
                        if (!nc.hasTransport(1) || !nc.hasCapability(16)) {
                            WifiPdnHandler.this.log("Without TRANSPORT_WIFI.");
                            if (WifiPdnHandler.this.mIsWifiConnected) {
                                WifiPdnHandler.this.log("TRANSPORT_WIFI lost.");
                                WifiPdnHandler.this.sendMessage(WifiPdnHandler.this.obtainMessage(1000, WifiConnState.DISCONNECTED.ordinal(), 0, (Object) null));
                                return;
                            }
                            return;
                        }
                        WifiPdnHandler.this.log("WIFI onAvailable.");
                        WifiPdnHandler.this.sendMessage(WifiPdnHandler.this.obtainMessage(1000, WifiConnState.DEFAULT_NETWORK_VALIDATED_CONNECTED.ordinal(), 0, (Object) null));
                        Network unused = WifiPdnHandler.this.mNetwork = network;
                    }
                }

                public void onLost(Network network) {
                    NetworkCapabilities nc = WifiPdnHandler.this.mConnectivityManager.getNetworkCapabilities(network);
                    if (nc == null) {
                        if (WifiPdnHandler.this.mNetwork != null && !WifiPdnHandler.this.mNetwork.equals(network)) {
                            WifiPdnHandler wifiPdnHandler = WifiPdnHandler.this;
                            wifiPdnHandler.log("OnLost with " + network + ", mNetwork: " + WifiPdnHandler.this.mNetwork);
                            return;
                        }
                    } else if (!WifiPdnHandler.this.ignoreVpnCallback(nc, "onLost")) {
                        if (!nc.hasTransport(1) || !nc.hasCapability(16)) {
                            WifiPdnHandler.this.log("OnLost without TRANSPORT_WIFI.");
                            return;
                        }
                    } else {
                        return;
                    }
                    WifiPdnHandler.this.log("WIFI onLost.");
                    WifiPdnHandler.this.sendMessage(WifiPdnHandler.this.obtainMessage(1000, WifiConnState.DISCONNECTED.ordinal(), 0, (Object) null));
                    Network unused = WifiPdnHandler.this.mNetwork = null;
                }

                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    if (networkCapabilities == null) {
                        WifiPdnHandler.this.log("NetworkCallback.onCapabilitiesChanged, Capabilities=null");
                    } else if (!WifiPdnHandler.this.ignoreVpnCallback(networkCapabilities, "onCapabilitiesChanged") && networkCapabilities.hasTransport(1) && networkCapabilities.hasCapability(16)) {
                        int rssi = networkCapabilities.getSignalStrength();
                        WifiPdnHandler wifiPdnHandler = WifiPdnHandler.this;
                        wifiPdnHandler.log("NetworkCallback.onCapabilitiesChanged, rssi == " + rssi);
                        if (!WifiPdnHandler.this.mIsWifiConnected) {
                            WifiPdnHandler.this.sendMessage(WifiPdnHandler.this.obtainMessage(1000, WifiConnState.DEFAULT_NETWORK_VALIDATED_CONNECTED.ordinal(), 0, (Object) null));
                        }
                        Network unused = WifiPdnHandler.this.mNetwork = network;
                        if (WifiPdnHandler.this.mLastRssi != rssi) {
                            int unused2 = WifiPdnHandler.this.mLastRssi = rssi;
                            boolean unused3 = WifiPdnHandler.this.mRssiChange = true;
                            WifiPdnHandler.this.sendEmptyMessage(1001);
                        }
                    }
                }

                public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                    NetworkCapabilities nc = WifiPdnHandler.this.mConnectivityManager.getNetworkCapabilities(network);
                    if (nc == null) {
                        WifiPdnHandler wifiPdnHandler = WifiPdnHandler.this;
                        wifiPdnHandler.log("onLinkPropertiesChanged Empty network capability:" + network);
                    } else if (!WifiPdnHandler.this.ignoreVpnCallback(nc, "onLinkPropertiesChanged")) {
                        if (!nc.hasTransport(1) || !nc.hasCapability(16)) {
                            WifiPdnHandler.this.log("onLinkPropertiesChanged Without TRANSPORT_WIFI.");
                            return;
                        }
                        WifiPdnHandler wifiPdnHandler2 = WifiPdnHandler.this;
                        wifiPdnHandler2.log("onLinkPropertiesChanged TRANSPORT_WIFI: " + WifiPdnHandler.this.mIsWifiConnected);
                        if (WifiPdnHandler.this.mIsWifiConnected) {
                            WifiPdnHandler.this.sendMessage(WifiPdnHandler.this.obtainMessage(1000, WifiConnState.DEFAULT_NETWORK_VALIDATED_CONNECTED.ordinal(), 0, (Object) null));
                        }
                        Network unused = WifiPdnHandler.this.mNetwork = network;
                    }
                }
            });
        }
    }

    private boolean checkInvalidSimIdx(int simIdx, String dbgMsg) {
        if (simIdx >= 0 && simIdx < this.mSimCount) {
            return false;
        }
        log(dbgMsg);
        return true;
    }

    public boolean isWifiPdnExisted() {
        for (int i = 0; i < this.mSimCount; i++) {
            if (this.mWifiPdnExisted[i]) {
                log("isWifiPdnExisted: found WiFi PDN on SIM: " + i);
                return true;
            }
        }
        return false;
    }

    private boolean isModemReqWifiLock() {
        for (int i = 0; i < this.mSimCount; i++) {
            if (this.mModemReqWifiLock[i]) {
                return true;
            }
        }
        return false;
    }

    private void updateLastRssi() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            Rlog.e(TAG, "updateLastRssi(): WifiManager null");
            Message msg = obtainMessage(1011);
            synchronized (this.mLock) {
                this.mPendingMsgs.add(msg);
            }
            return;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            this.mLastRssi = wifiInfo.getRssi();
        }
    }

    private String updateSsidToHexString(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return "";
        }
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        byte[] bytes = ssid.getBytes();
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            str.append(String.format("%02x", new Object[]{Byte.valueOf(bytes[i])}));
        }
        return str.toString();
    }

    public int getLastRssi() {
        return this.mLastRssi;
    }

    private int getMainCapabilityPhoneId() {
        int phoneId = SystemProperties.getInt(ImsConstants.PROPERTY_CAPABILITY_SWITCH, 1) - 1;
        if (phoneId >= 0 && phoneId < this.mMwiRil.length) {
            return phoneId;
        }
        Rlog.e(TAG, "getMainCapabilityPhoneId error: " + phoneId);
        return -1;
    }

    private MwiRIL getMwiRil() {
        int phoneId = getMainCapabilityPhoneId();
        if (phoneId == -1) {
            return this.mMwiRil[0];
        }
        return this.mMwiRil[phoneId];
    }

    public static String getUeWlanMacAddr() {
        return mWifiUeMac;
    }

    private String updateWlanMacAddr() {
        try {
            NetworkInterface wnif = NetworkInterface.getByName(WIFI_IF_NAME);
            if (wnif == null) {
                log("updateWlanMacAddr wnif == null");
                return "";
            }
            byte[] macBytes = wnif.getHardwareAddress();
            if (macBytes == null) {
                log("updateWlanMacAddr macBytes == null");
                return "";
            }
            StringBuilder res1 = new StringBuilder();
            int length = macBytes.length;
            for (int i = 0; i < length; i++) {
                res1.append(String.format("%02X:", new Object[]{Byte.valueOf(macBytes[i])}));
            }
            if (res1.length() > 0) {
                res1.deleteCharAt(res1.length() - 1);
            }
            return res1.toString();
        } catch (Exception e) {
            return "02:00:00:00:00:00";
        }
    }

    private void onWifiPingRequest(Message msg) {
        int[] result = (int[]) ((AsyncResult) msg.obj).result;
        if (result == null) {
            Rlog.e(TAG, "onWifiPingRequest(): result is null");
        } else if (result.length < 2) {
            Rlog.e(TAG, "onWifiPingRequest(): Bad params");
        } else {
            int rat = result[0];
            int simIdx = result[1];
            if (!checkInvalidSimIdx(simIdx, "onWifiPingRequest(): invalid SIM id")) {
                log("onWifiPingRequest: rat = " + rat + ", simIdx = " + simIdx);
                pingWifiGateway(simIdx, rat, this);
            }
        }
    }

    private void pingWifiGateway(int simId, int rat, Handler hdlr) {
        hdlr.sendMessage(hdlr.obtainMessage(EVENT_SET_WIFI_PING_RESULT, simId, rat, new PingData(simId, 50.0d, 0)));
    }

    private void onWifiLock(Message msg) {
        String[] result = (String[]) ((AsyncResult) msg.obj).result;
        if (result == null) {
            Rlog.e(TAG, "onWifiLock(): result is null");
        } else if (result.length < 3) {
            Rlog.e(TAG, "onWifiLock(): Bad params");
        } else {
            try {
                String str = result[0];
                int enableLock = Integer.parseInt(result[1]);
                int simIdx = Integer.parseInt(result[2]);
                boolean preModemWifiLockState = isModemReqWifiLock();
                this.mModemReqWifiLock[simIdx] = enableLock != 0;
                if (preModemWifiLockState != isModemReqWifiLock()) {
                    handleWifiDefferOff(WifiLockSource.MODEM_STATE_CHANGE, 0);
                }
            } catch (Exception e) {
                Rlog.e(TAG, "onWifiLock[" + result.length + "]" + result[0] + " " + result[1] + " " + result[2] + "  e:" + e.toString());
            }
        }
    }

    private boolean isEccInProgress() {
        TelecomManager tm = (TelecomManager) this.mContext.getSystemService("telecom");
        if (tm != null) {
            return tm.isInEmergencyCall();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        if (!USR_BUILD || TELDBG) {
            Rlog.d(TAG, s);
        }
    }

    public void setWfcHandler(WfcHandler wfcHandler) {
        this.mWfcHandler = wfcHandler;
    }

    private void broadcastWfcStatusIntent(int wfcStatus) {
        WfcHandler wfcHandler = this.mWfcHandler;
        wfcHandler.sendMessage(wfcHandler.obtainMessage(2105));
    }

    private String maskString(String s) {
        StringBuilder sb = new StringBuilder();
        if (TextUtils.isEmpty(s)) {
            return s;
        }
        int maskLength = s.length() / 2;
        if (maskLength < 1) {
            sb.append("*");
            return sb.toString();
        }
        for (int i = 0; i < maskLength; i++) {
            sb.append("*");
        }
        return sb.toString() + s.substring(maskLength);
    }

    /* access modifiers changed from: private */
    public boolean ignoreVpnCallback(NetworkCapabilities nc, String tagType) {
        if (nc == null || !nc.hasTransport(4) || !nc.hasTransport(1)) {
            return false;
        }
        return true;
    }
}
