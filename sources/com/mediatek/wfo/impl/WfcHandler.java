package com.mediatek.wfo.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsReasonInfo;
import android.text.TextUtils;
import com.android.ims.ImsManager;
import com.mediatek.ims.ImsConstants;
import com.mediatek.ims.WfcReasonInfo;
import com.mediatek.wfo.DisconnectCause;
import com.mediatek.wfo.IWifiOffloadListener;
import com.mediatek.wfo.IWifiOffloadService;
import com.mediatek.wfo.p005op.IWosExt;
import com.mediatek.wfo.p005op.OpWosCustomizationUtils;
import com.mediatek.wfo.ril.MwiRIL;
import java.util.Arrays;
import java.util.List;

public class WfcHandler extends Handler {
    private static final String ACTION_OPERATOR_CONFIG_CHANGED = "com.mediatek.common.carrierexpress.operator_config_changed";
    /* access modifiers changed from: private */
    public static final Uri AID_SETTING_URI = Settings.Global.getUriFor(AID_SETTING_URI_STR);
    private static final String AID_SETTING_URI_STR = "wfc_aid_value";
    private static final int BASE = 2000;
    private static int CODE_WFC_EPDG_IPSEC_SETUP_ERROR = WfcReasonInfo.CODE_WFC_EPDG_IPSEC_SETUP_ERROR;
    private static final int EVENT_HANDLE_MODEM_POWER = 2000;
    public static final int EVENT_HANDLE_WFC_STATE_CHANGED = 2105;
    private static final int EVENT_HANDLE_WIFI_STATE_CHANGE = 2003;
    private static final int EVENT_INITIALIZE = 2005;
    private static final int EVENT_MULTI_SIM_CONFIG_CHANGED = 2006;
    private static final int EVENT_NOTIFY_EPDG_SCREEN_STATE = 2004;
    private static final int EVENT_NOTIFY_WIFI_NO_INTERNET = 2002;
    private static final int EVENT_ON_ALLOW_WIFI_OFF = 2202;
    private static final int EVENT_ON_LOCATION_TIMEOUT = 2201;
    public static final int EVENT_ON_PDN_ERROR = 2101;
    public static final int EVENT_ON_PDN_HANDOVER = 2100;
    public static final int EVENT_ON_ROVE_OUT = 2102;
    public static final int EVENT_ON_WFC_PDN_STATE_CHANGED = 2103;
    public static final int EVENT_ON_WIFI_PDN_OOS = 2104;
    private static final int EVENT_SET_WFC_EMERGENCY_ADDRESS_ID = 2001;
    private static final String EXTRA_POWER_ON_MODEM_KEY = "mediatek:POWER_ON_MODEM";
    private static final String IMS_REG_ERROR_NOTIFICATION = "com.android.imsconnection.DISCONNECTED";
    private static final String IMS_REG_ERROR_NOTIFICATION_PERMISSION = "com.mediatek.permission.IMS_ERR_NOTIFICATION";
    private static final String INTENT_KEY_PROP_KEY = "simPropKey";
    private static final int INVALID = -1;
    private static final int MAX_VALID_SIM_COUNT = 4;
    private static final String MTK_KEY_DISABLE_WFC_AFTER_AUTH_FAIL = "mtk_carrier_disable_wfc_after_auth_fail_bool";
    private static final int NOT_REGISTERED = 2;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final String RADIO_MANAGER_POWER_ON_MODEM = "mediatek.intent.action.WFC_POWER_ON_MODEM";
    private static final int REGISTERED = 1;
    private static final int REGISTERING = 3;
    private static final int RESPONSE_NOTIFY_EPDG_SCREEN_STATE = 2203;
    private static final int RESPONSE_SET_WFC_EMERGENCY_ADDRESS_ID = 2200;
    private static final int RETRY_TIMEOUT = 3000;
    private static int SUB_CAUSE_IKEV2_24 = 24;
    private static final String TAG = "WfcHandler";
    private static final boolean TELDBG;
    private static final int TYPE_MOBILE_IMS = 11;
    private static final boolean USR_BUILD = (TextUtils.equals(Build.TYPE, "user") || TextUtils.equals(Build.TYPE, "userdebug"));
    private static final String WFC_REQUEST_PARTIAL_SCAN = "com.mediatek.intent.action.WFC_REQUEST_PARTIAL_SCAN";
    private static final String WFC_STATUS_CHANGED = "com.mediatek.intent.action.WFC_STATUS_CHANGED";
    private static int WIFI_NO_INTERNET_ERROR_CODE = WfcReasonInfo.CODE_WFC_EPDG_CON_OR_LOCAL_OR_NULL_PTR_ERROR;
    private static int WIFI_NO_INTERNET_TIMEOUT = 8000;
    private static WfcHandler mInstance = null;
    private ConnectivityManager mConnectivityManager;
    private final ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            WfcHandler wfcHandler = WfcHandler.this;
            wfcHandler.log("SettingsObserver.onChange(), selfChange:" + selfChange + ", uri:" + uri);
            if (WfcHandler.AID_SETTING_URI.equals(uri)) {
                String aid = Settings.Global.getString(WfcHandler.this.mContext.getContentResolver(), WfcHandler.AID_SETTING_URI_STR);
                if (TextUtils.isEmpty(aid)) {
                    WfcHandler wfcHandler2 = WfcHandler.this;
                    wfcHandler2.log("empty aid: " + aid);
                    return;
                }
                String unused = WfcHandler.this.mWfcEccAid = aid;
                WfcHandler wfcHandler3 = WfcHandler.this;
                wfcHandler3.log("mWfcEccAid: " + WfcHandler.this.mWfcEccAid);
                Settings.Global.putString(WfcHandler.this.mContext.getContentResolver(), WfcHandler.AID_SETTING_URI_STR, "");
                WfcHandler wfcHandler4 = WfcHandler.this;
                wfcHandler4.sendMessage(wfcHandler4.obtainMessage(2001));
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public DisconnectCause[] mDisconnectCause;
    /* access modifiers changed from: private */
    public boolean mHasWiFiDisabledPending = false;
    private boolean[] mIsWfcSettingsOn;
    private boolean mIsWifiConnected;
    private boolean mIsWifiEnabled;
    /* access modifiers changed from: private */
    public boolean mIsWifiL2Connected = false;
    /* access modifiers changed from: private */
    public RemoteCallbackList<IWifiOffloadListener> mListeners = new RemoteCallbackList<>();
    private MwiRIL[] mMwiRil;
    private int mPartialScanCount;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            WfcHandler wfcHandler = WfcHandler.this;
            wfcHandler.log("onReceive action:" + intent.getAction());
            if (intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                WfcHandler wfcHandler2 = WfcHandler.this;
                wfcHandler2.sendMessage(wfcHandler2.obtainMessage(2003));
            } else if (intent.getAction().equals("android.net.wifi.action.WIFI_SCAN_AVAILABILITY_CHANGED")) {
                WfcHandler wfcHandler3 = WfcHandler.this;
                wfcHandler3.sendMessage(wfcHandler3.obtainMessage(2003));
            } else if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                int phoneId = intent.getIntExtra("phone", -1);
                ImsManager mgr = ImsManager.getInstance(context, phoneId);
                if (mgr != null && !mgr.isWfcEnabledByPlatform()) {
                    WfcHandler wfcHandler4 = WfcHandler.this;
                    wfcHandler4.log("isWfcEnabledByPlatform(" + phoneId + ") is false, clearPDNErrorMessages");
                    WfcHandler.this.mWosExt.clearPDNErrorMessages();
                }
                WfcHandler.this.handleModemPower();
            } else if (intent.getAction().equals(WfcHandler.ACTION_OPERATOR_CONFIG_CHANGED)) {
                WfcHandler.this.createWosExt();
            } else if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
                Parcelable parcelableExtra = intent.getParcelableExtra("networkInfo");
                if (parcelableExtra != null) {
                    boolean unused = WfcHandler.this.mIsWifiL2Connected = ((NetworkInfo) parcelableExtra).getState() == NetworkInfo.State.CONNECTED;
                    WfcHandler.this.checkIfShowNoInternetError(false);
                }
            } else if (intent.getAction().equals("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED")) {
                if ("wfc_ims_enabled".equals(intent.getStringExtra(WfcHandler.INTENT_KEY_PROP_KEY))) {
                    WfcHandler.this.log("ACTION_SUBINFO_RECORD_UPDATED: WFC_IMS_ENABLED changes");
                    if (WfcHandler.this.updateWfcUISetting()) {
                        WfcHandler wfcHandler5 = WfcHandler.this;
                        wfcHandler5.sendMessage(wfcHandler5.obtainMessage(2000));
                        WfcHandler.this.checkIfShowNoInternetError(false);
                    }
                }
            } else if (intent.getAction().equals(WfcHandler.WFC_STATUS_CHANGED)) {
                if (!WfcHandler.this.mWifiPdnHandler.isWifiPdnExisted() && WfcHandler.this.mHasWiFiDisabledPending) {
                    boolean unused2 = WfcHandler.this.mHasWiFiDisabledPending = false;
                    WfcHandler wfcHandler6 = WfcHandler.this;
                    wfcHandler6.sendMessage(wfcHandler6.obtainMessage(2202));
                }
            } else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                int unused3 = WfcHandler.this.mScreenState = ScreenState.SCREEN_OFF.ordinal();
                WfcHandler wfcHandler7 = WfcHandler.this;
                wfcHandler7.notifyEPDGScreenState(wfcHandler7.mScreenState);
            } else if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                int unused4 = WfcHandler.this.mScreenState = ScreenState.SCREEN_ON.ordinal();
                WfcHandler wfcHandler8 = WfcHandler.this;
                wfcHandler8.notifyEPDGScreenState(wfcHandler8.mScreenState);
            } else if (intent.getAction().equals("android.intent.action.USER_PRESENT")) {
                int unused5 = WfcHandler.this.mScreenState = ScreenState.USER_PRESENT.ordinal();
                WfcHandler wfcHandler9 = WfcHandler.this;
                wfcHandler9.notifyEPDGScreenState(wfcHandler9.mScreenState);
            }
        }
    };
    /* access modifiers changed from: private */
    public int mScreenState;
    private int mSimCount;
    /* access modifiers changed from: private */
    public String mWfcEccAid;
    private int[] mWfcState;
    private IWifiOffloadService mWfoService = new IWifiOffloadService.Stub() {
        public void registerForHandoverEvent(IWifiOffloadListener listener) {
            WfcHandler wfcHandler = WfcHandler.this;
            wfcHandler.log("registerForHandoverEvent for " + listener.asBinder());
            WfcHandler.this.mListeners.register(listener);
        }

        public void unregisterForHandoverEvent(IWifiOffloadListener listener) {
            WfcHandler wfcHandler = WfcHandler.this;
            wfcHandler.log("unregisterForHandoverEvent for " + listener.asBinder());
            WfcHandler.this.mListeners.unregister(listener);
        }

        public int getRatType(int simIdx) {
            WfcHandler.this.log("getRatType() not supported");
            return 0;
        }

        public DisconnectCause getDisconnectCause(int simIdx) {
            if (WfcHandler.this.checkInvalidSimIdx(simIdx, "getDisconnectCause()")) {
                return null;
            }
            return WfcHandler.this.mDisconnectCause[simIdx];
        }

        public void setEpdgFqdn(int simIdx, String fqdn, boolean wfcEnabled) {
            WfcHandler.this.log("setEpdgFqdn() not supported");
        }

        public void updateCallState(int simIdx, int callId, int callType, int callState) {
            WfcHandler.this.log("updateCallState() not supported");
        }

        public boolean isWifiConnected() {
            return WfcHandler.this.mWifiPdnHandler.isWifiConnected();
        }

        public void updateRadioState(int simIdx, int radioState) {
            WfcHandler wfcHandler = WfcHandler.this;
            wfcHandler.log("updateRadioState() : sim: " + simIdx + " radioState" + radioState);
            if (WfcHandler.this.mWifiPdnHandler != null) {
                WfcHandler.this.mWifiPdnHandler.handleRadioStateChanged(simIdx, radioState);
            }
        }

        public boolean setMccMncAllowList(String[] allowList) {
            WfcHandler.this.log("setMccMncAllowList() not supported");
            return false;
        }

        public String[] getMccMncAllowList(int mode) {
            WfcHandler.this.log("getMccMncAllowList() not supported");
            return null;
        }

        public void factoryReset() {
            WfcHandler.this.log("factoryReset()");
            WfcHandler.this.mWosExt.factoryReset();
        }

        public boolean setWifiOff() {
            if (WfcHandler.this.mWifiPdnHandler.isWifiPdnExisted()) {
                boolean unused = WfcHandler.this.mHasWiFiDisabledPending = true;
                WfcHandler.this.mWifiPdnHandler.setWifiOff();
            } else {
                boolean unused2 = WfcHandler.this.mHasWiFiDisabledPending = false;
            }
            WfcHandler wfcHandler = WfcHandler.this;
            wfcHandler.log("setWifiOff() mHasWiFiDisabledPending" + WfcHandler.this.mHasWiFiDisabledPending);
            return WfcHandler.this.mHasWiFiDisabledPending;
        }
    };
    /* access modifiers changed from: private */
    public WifiPdnHandler mWifiPdnHandler;
    IWosExt mWosExt = null;

    enum ScreenState {
        USER_PRESENT,
        SCREEN_OFF,
        SCREEN_ON
    }

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        TELDBG = z;
    }

    public void handleMessage(Message msg) {
        log("handleMessage: " + messageToString(msg));
        switch (msg.what) {
            case 2000:
                handleModemPower();
                return;
            case 2001:
                setEmergencyAddressId();
                return;
            case 2002:
                checkIfShowNoInternetError(true);
                return;
            case 2003:
                updateWifiEnabled();
                return;
            case 2004:
                notifyEPDGScreenState(this.mScreenState);
                return;
            case 2005:
                initialize();
                return;
            case 2006:
                int activeModemCount = ((Integer) msg.obj).intValue();
                log("EVENT_MULTI_SIM_CONFIG_CHANGED, activeModemCount: " + activeModemCount);
                onMultiSimConfigChanged(activeModemCount);
                return;
            case 2100:
                onPdnHandover(msg);
                return;
            case 2101:
                onWfcPdnError(msg);
                return;
            case 2102:
                onWifiRoveout(msg);
                return;
            case 2103:
                onWfcPdnStateChanged(msg);
                return;
            case 2104:
                onWifiPdnOOS(msg);
                return;
            case 2105:
                onWfcStatusChanged();
                return;
            case 2200:
                handleRetry(2001, (AsyncResult) msg.obj);
                return;
            case 2201:
                notifyLocationTimeout();
                return;
            case 2202:
                notifyOnAllowWifiOff();
                return;
            case 2203:
                handleRetry(2004, (AsyncResult) null);
                return;
            default:
                return;
        }
    }

    private void handleRetry(int msgId, AsyncResult result) {
        if (!hasMessages(msgId) && result != null && result.exception != null) {
            sendEmptyMessageDelayed(msgId, 3000);
        }
    }

    private String messageToString(Message msg) {
        switch (msg.what) {
            case 2000:
                return "EVENT_HANDLE_MODEM_POWER";
            case 2001:
                return "EVENT_SET_WFC_EMERGENCY_ADDRESS_ID";
            case 2002:
                return "EVENT_NOTIFY_WIFI_NO_INTERNET";
            case 2003:
                return "EVENT_HANDLE_WIFI_STATE_CHANGE";
            case 2004:
                return "EVENT_NOTIFY_EPDG_SCREEN_STATE";
            case 2005:
                return "EVENT_INITIALIZE";
            case 2006:
                return "EVENT_MULTI_SIM_CONFIG_CHANGED";
            case 2100:
                return "EVENT_ON_PDN_HANDOVER";
            case 2101:
                return "EVENT_ON_PDN_ERROR";
            case 2102:
                return "EVENT_ON_ROVE_OUT";
            case 2103:
                return "EVENT_ON_WFC_PDN_STATE_CHANGED";
            case 2104:
                return "EVENT_ON_WIFI_PDN_OOS";
            case 2105:
                return "EVENT_HANDLE_WFC_STATE_CHANGED";
            case 2200:
                return "RESPONSE_SET_WFC_EMERGENCY_ADDRESS_ID";
            case 2201:
                return "EVENT_ON_LOCATION_TIMEOUT";
            case 2202:
                return "EVENT_ON_ALLOW_WIFI_OFF";
            case 2203:
                return "RESPONSE_NOTIFY_EPDG_SCREEN_STATE";
            default:
                return "UNKNOWN";
        }
    }

    public Handler getHandler() {
        return this;
    }

    private void notifyOnAllowWifiOff() {
        log("notifyOnAllowWifiOff");
        int i = this.mListeners.beginBroadcast();
        while (i > 0) {
            i--;
            try {
                this.mListeners.getBroadcastItem(i).onAllowWifiOff();
            } catch (RemoteException e) {
                log("onHandover: RemoteException occurs!");
            } catch (IllegalStateException e2) {
                log("onHandover: IllegalStateException occurs!");
            }
        }
        this.mListeners.finishBroadcast();
    }

    public static WfcHandler getInstance() {
        return mInstance;
    }

    public static WfcHandler getInstance(Context context, WifiPdnHandler wifiHandler, int simCount, Looper looper, MwiRIL[] mwiRil) {
        if (mInstance == null) {
            mInstance = new WfcHandler(context, wifiHandler, simCount, looper, mwiRil);
        }
        return mInstance;
    }

    public WfcHandler(Context context, WifiPdnHandler wifiHandler, int simCount, Looper looper, MwiRIL[] mwiRil) {
        super(looper);
        this.mContext = context;
        this.mWifiPdnHandler = wifiHandler;
        if (wifiHandler != null) {
            wifiHandler.setWfcHandler(this);
        }
        int i = simCount <= 4 ? simCount : 4;
        this.mSimCount = i;
        this.mMwiRil = mwiRil;
        this.mDisconnectCause = new DisconnectCause[i];
        this.mWfcState = new int[i];
        obtainMessage(2005).sendToTarget();
    }

    private void initialize() {
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mIsWfcSettingsOn = new boolean[this.mSimCount];
        updateWfcUISetting();
        sendMessage(obtainMessage(2003));
        createWosExt();
        this.mIsWifiConnected = false;
        this.mPartialScanCount = 0;
        registerForBroadcast();
        registerIndication();
        registerForWfcAidObserver();
    }

    /* access modifiers changed from: protected */
    public void notifyMultiSimConfigChanged(int activeModemCount, MwiRIL[] mwiRil) {
        this.mMwiRil = mwiRil;
        obtainMessage(2006, Integer.valueOf(activeModemCount)).sendToTarget();
    }

    private void onMultiSimConfigChanged(int activeModemCount) {
        int prevActiveModemCount = this.mSimCount;
        Rlog.i(TAG, "notifyMultiSimConfigChanged, phone:" + prevActiveModemCount + "->" + activeModemCount + ", mSimCount:" + this.mSimCount);
        if (prevActiveModemCount != activeModemCount) {
            this.mSimCount = activeModemCount;
            if (prevActiveModemCount > activeModemCount) {
                updateWfcUISetting();
                return;
            }
            this.mDisconnectCause = (DisconnectCause[]) Arrays.copyOf(this.mDisconnectCause, activeModemCount);
            this.mWfcState = Arrays.copyOf(this.mWfcState, activeModemCount);
            this.mIsWfcSettingsOn = Arrays.copyOf(this.mIsWfcSettingsOn, activeModemCount);
            updateWfcUISetting();
            for (int i = prevActiveModemCount; i < activeModemCount; i++) {
                this.mMwiRil[i].registerWifiPdnHandover(this, 2100, (Object) null);
                this.mMwiRil[i].registerWifiPdnError(this, 2101, (Object) null);
                this.mMwiRil[i].registerWifiPdnRoveOut(this, 2102, (Object) null);
                this.mMwiRil[i].registerWfcPdnStateChanged(this, 2103, (Object) null);
                this.mMwiRil[i].registerWifiPdnOos(this, 2104, (Object) null);
            }
        }
    }

    private void registerForWfcAidObserver() {
        Context context = this.mContext;
        if (context != null) {
            context.getContentResolver().registerContentObserver(AID_SETTING_URI, false, this.mContentObserver);
            log("registerForWfcAidObserver()");
        }
    }

    private void registerForBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        filter.addAction(ACTION_OPERATOR_CONFIG_CHANGED);
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.action.WIFI_SCAN_AVAILABILITY_CHANGED");
        filter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.USER_PRESENT");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void registerIndication() {
        for (int i = 0; i < this.mSimCount; i++) {
            this.mMwiRil[i].registerWifiPdnHandover(this, 2100, (Object) null);
            this.mMwiRil[i].registerWifiPdnError(this, 2101, (Object) null);
            this.mMwiRil[i].registerWifiPdnRoveOut(this, 2102, (Object) null);
            this.mMwiRil[i].registerWfcPdnStateChanged(this, 2103, (Object) null);
            this.mMwiRil[i].registerWifiPdnOos(this, 2104, (Object) null);
        }
    }

    private void onPdnHandover(Message msg) {
        int[] result = (int[]) ((AsyncResult) msg.obj).result;
        if (result == null) {
            Rlog.e(TAG, "onPdnHandover(): result is null");
        } else if (result.length < 5) {
            Rlog.e(TAG, "onPdnHandover(): Bad params");
        } else if (result[0] != 0) {
            log("onPdnHandover(): Not IMS PDN, ignore");
        } else {
            int stage = result[1];
            int i = result[2];
            int desRat = result[3];
            notifyOnHandover(result[4], stage, desRat);
            if (stage == 1 && desRat != 2) {
                this.mWosExt.clearPDNErrorMessages();
            }
        }
    }

    private void onWfcPdnError(Message msg) {
        int[] result = (int[]) ((AsyncResult) msg.obj).result;
        if (result == null) {
            Rlog.e(TAG, "onWfcPdnError(): result is null");
        } else if (result.length < 4) {
            Rlog.e(TAG, "onWfcPdnError(): Bad params");
        } else {
            int errCount = result[0];
            int mainErr = result[1];
            int subErr = result[2];
            int simIdx = result[3];
            this.mDisconnectCause[simIdx] = new DisconnectCause(mainErr, subErr);
            Rlog.e(TAG, "onWfcPdnError(): errCount = " + errCount + ", mainErr = " + mainErr + ", subErr = " + subErr + ", simIdx = " + simIdx);
            if (mainErr != 0) {
                this.mWosExt.setWfcRegErrorCode(mainErr, simIdx);
                Intent intent = new Intent(IMS_REG_ERROR_NOTIFICATION);
                intent.putExtra("wfcErrorCode", mainErr);
                if (mainErr == CODE_WFC_EPDG_IPSEC_SETUP_ERROR && subErr == SUB_CAUSE_IKEV2_24) {
                    intent.putExtra("result", new ImsReasonInfo(1400, 1408, "WiFi_Error09-Unable to connect"));
                    intent.putExtra("stateChanged", 2);
                    intent.putExtra("imsRat", 18);
                    if (getBooleanCarrierConfig(MTK_KEY_DISABLE_WFC_AFTER_AUTH_FAIL, getSubIdBySlot(simIdx))) {
                        Rlog.d(TAG, "Set WFC setting OFF.");
                        ImsManager.getInstance(this.mContext, simIdx).setWfcSetting(false);
                    }
                }
                this.mContext.sendBroadcast(intent, "com.mediatek.permission.IMS_ERR_NOTIFICATION");
            }
            if (errCount == 0) {
                this.mWosExt.clearPDNErrorMessages();
            } else {
                this.mWosExt.showPDNErrorMessages(mainErr);
            }
        }
    }

    private void onWifiRoveout(Message msg) {
        String[] result = (String[]) ((AsyncResult) msg.obj).result;
        if (result == null) {
            Rlog.e(TAG, "onWifiRoveout(): result is null");
            return;
        }
        if (result.length == 3) {
            log("onWifiRoveout()[" + result.length + "] " + result[0] + " " + result[1] + " " + result[2]);
        } else if (result.length == 4) {
            log("onWifiRoveout()[" + result.length + "] " + result[0] + " " + result[1] + " " + result[2] + " " + result[3]);
        } else {
            Rlog.e(TAG, "onWifiRoveout(): Bad params [" + result.length + "] ");
            return;
        }
        try {
            String ifname = result[0];
            boolean roveOut = Integer.parseInt(result[1]) == 1;
            boolean mobike_ind = result.length == 4 && Integer.parseInt(result[2]) == 1;
            int simIdx = Integer.parseInt(result[result.length - 1]);
            if (roveOut) {
                notifyOnRoveOut(simIdx, roveOut, this.mWifiPdnHandler.getLastRssi());
            }
            if (mobike_ind) {
                log("onWifiRoveout(): mobike_ind=1. count = " + this.mPartialScanCount + ", connected = " + this.mIsWifiConnected);
                if (this.mIsWifiConnected && this.mPartialScanCount >= 3) {
                    if (getCurrentAssociatedApCount() > 1) {
                        log("Mobike disconnect+startscan");
                        WifiManager wifiMngr = (WifiManager) this.mContext.getSystemService("wifi");
                        wifiMngr.disconnect();
                        wifiMngr.startScan();
                        this.mPartialScanCount = 0;
                    }
                }
                log("No need to partial scan.");
            }
            log("onWifiRoveout: " + ifname + " " + roveOut + " " + mobike_ind);
        } catch (Exception e) {
            Rlog.e(TAG, "onWifiRoveout()[" + result.length + "]" + result[0] + " " + result[1] + " " + result[2] + " e:" + e.toString());
        }
    }

    private void onWfcPdnStateChanged(Message msg) {
        int[] result = (int[]) ((AsyncResult) msg.obj).result;
        if (result == null) {
            Rlog.e(TAG, "onWfcPdnStateChanged(): result is null");
            return;
        }
        int state = result[0];
        int simIdx = result[1];
        if (simIdx < this.mSimCount) {
            this.mWfcState[simIdx] = state;
        }
        log("onWfcPdnStateChanged() state:" + state + " simIdx:" + simIdx);
        if (1 == state) {
            this.mWosExt.clearPDNErrorMessages();
        }
        int i = this.mListeners.beginBroadcast();
        while (i > 0) {
            i--;
            try {
                this.mListeners.getBroadcastItem(i).onWfcStateChanged(simIdx, state);
            } catch (RemoteException e) {
                log("onWfcStateChanged: RemoteException occurs!");
            } catch (IllegalStateException e2) {
                log("onWfcStateChanged: IllegalStateException occurs!");
            }
        }
        this.mListeners.finishBroadcast();
    }

    public int getWfcState(int simIdx) {
        log("getWfcState state:" + this.mWfcState[simIdx] + " simIdx:" + simIdx);
        return this.mWfcState[simIdx];
    }

    private void onWifiPdnOOS(Message msg) {
        String[] result = (String[]) ((AsyncResult) msg.obj).result;
        if (result == null) {
            Rlog.e(TAG, "onWifiPdnOOS(): result is null");
        } else if (result.length < 4) {
            Rlog.e(TAG, "onWifiPdnOOS(): Bad params");
        } else {
            try {
                notifyOnWifiPdnOOS(result[0], Integer.parseInt(result[1]), Integer.parseInt(result[2]), Integer.parseInt(result[3]));
            } catch (Exception e) {
                Rlog.e(TAG, "onWifiPdnOOS[" + result.length + "]" + result[0] + " " + result[1] + " " + result[2] + " " + result[3] + " e:" + e.toString());
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean checkInvalidSimIdx(int simIdx, String dbgMsg) {
        if (simIdx >= 0 && simIdx < this.mSimCount) {
            return false;
        }
        log(dbgMsg);
        return true;
    }

    private void setEmergencyAddressId() {
        if (TextUtils.isEmpty(this.mWfcEccAid)) {
            log("Current AID is empty");
            return;
        }
        log("setEmergencyAddressId(), mWfcEccAid:" + this.mWfcEccAid);
        getMwiRil().setEmergencyAddressId(this.mWfcEccAid, obtainMessage(2200));
    }

    /* access modifiers changed from: private */
    public void handleModemPower() {
        log("handleModemPower() mIsWifiEnabled:" + this.mIsWifiEnabled + " mIsWfcSettingsOn: " + isWfcSettingsEnabledAny());
        if (!this.mIsWifiEnabled || !isWfcSettingsEnabledAny()) {
            notifyPowerOnModem(false);
        } else {
            notifyPowerOnModem(true);
        }
    }

    private void notifyPowerOnModem(boolean isModemOn) {
        if (!SystemProperties.get("ro.vendor.mtk_flight_mode_power_off_md").equals("1")) {
            log("modem always on, no need to control it!");
        } else if (this.mContext == null) {
            log("context is null, can't control modem!");
        } else {
            Intent intent = new Intent(RADIO_MANAGER_POWER_ON_MODEM);
            intent.setPackage(ImsConstants.PACKAGE_NAME_PHONE);
            intent.putExtra(EXTRA_POWER_ON_MODEM_KEY, isModemOn);
            this.mContext.sendBroadcast(intent);
        }
    }

    private boolean isWfcSettingsEnabledAny() {
        for (int i = 0; i < this.mSimCount; i++) {
            if (this.mIsWfcSettingsOn[i]) {
                log("isWfcSettingsEnabledAny: found Wfc settings enabled on SIM: " + i);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean updateWfcUISetting() {
        if (SystemProperties.getInt("persist.vendor.mims_support", 1) > 1) {
            for (int i = 0; i < this.mSimCount; i++) {
                boolean defValue = getBooleanCarrierConfig("carrier_default_wfc_ims_enabled_bool", getSubIdBySlot(i));
                boolean[] zArr = this.mIsWfcSettingsOn;
                boolean oldValue = zArr[i];
                zArr[i] = SubscriptionManager.getBooleanSubscriptionProperty(getSubIdBySlot(i), "wfc_ims_enabled", defValue, this.mContext);
                log("WfcSetting simId: " + i + " enabled: " + this.mIsWfcSettingsOn[i]);
                boolean[] zArr2 = this.mIsWfcSettingsOn;
                if (oldValue != zArr2[i] || zArr2[i]) {
                    return true;
                }
            }
            return false;
        }
        int mainCapabilityPhoneId = getMainCapabilityPhoneId();
        log("mainCapabilityPhoneId = " + mainCapabilityPhoneId);
        if (mainCapabilityPhoneId < 0 || mainCapabilityPhoneId >= this.mSimCount) {
            Rlog.e(TAG, "updateWfcUISetting(): mainCapabilityPhoneId invalid");
            return false;
        }
        boolean defValue2 = getBooleanCarrierConfig("carrier_default_wfc_ims_enabled_bool", getSubIdBySlot(mainCapabilityPhoneId));
        boolean[] zArr3 = this.mIsWfcSettingsOn;
        boolean oldValue2 = zArr3[mainCapabilityPhoneId];
        zArr3[mainCapabilityPhoneId] = SubscriptionManager.getBooleanSubscriptionProperty(getSubIdBySlot(mainCapabilityPhoneId), "wfc_ims_enabled", defValue2, this.mContext);
        log("WfcSetting simId: " + mainCapabilityPhoneId + " enabled: " + this.mIsWfcSettingsOn[mainCapabilityPhoneId]);
        boolean[] zArr4 = this.mIsWfcSettingsOn;
        if (oldValue2 != zArr4[mainCapabilityPhoneId] || zArr4[mainCapabilityPhoneId]) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void notifyEPDGScreenState(int state) {
        log("notifyEPDGScreenState(), state:" + state);
        getMwiRil().notifyEPDGScreenState(state, obtainMessage(2203));
    }

    private void notifyOnHandover(int simIdx, int stage, int ratType) {
        log("onHandover simIdx: " + simIdx + " stage: " + stage + " rat: " + ratType);
        int i = this.mListeners.beginBroadcast();
        while (i > 0) {
            i--;
            try {
                this.mListeners.getBroadcastItem(i).onHandover(simIdx, stage, ratType);
            } catch (RemoteException e) {
                log("onHandover: RemoteException occurs!");
            } catch (IllegalStateException e2) {
                log("onHandover: IllegalStateException occurs!");
            }
        }
        this.mListeners.finishBroadcast();
    }

    private void notifyOnRoveOut(int simIdx, boolean roveOut, int rssi) {
        log("onRoveOut simIdx: " + simIdx + " roveOut: " + roveOut + " rssi: " + rssi);
        int i = this.mListeners.beginBroadcast();
        while (i > 0) {
            i--;
            try {
                this.mListeners.getBroadcastItem(i).onRoveOut(simIdx, roveOut, rssi);
            } catch (RemoteException e) {
                log("onRoveOut: RemoteException occurs!");
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: package-private */
    public boolean isImsApn(String apn) {
        boolean isImsApn = false;
        String imsApn = getImsApnName();
        if (imsApn != null && imsApn.equals(apn)) {
            isImsApn = true;
        }
        log("URC specific apn: " + apn + ", IMS APN: " + imsApn + ", isImsApn return: " + isImsApn);
        return isImsApn;
    }

    /* access modifiers changed from: package-private */
    public String getImsApnName() {
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        if (connectivityManager == null) {
            Rlog.e(TAG, "Unexpected error, mConnectivityManager = null");
            return null;
        }
        NetworkInfo imsNetworkInfo = connectivityManager.getNetworkInfo(11);
        if (imsNetworkInfo == null) {
            return null;
        }
        String apnName = imsNetworkInfo.getExtraInfo();
        log("getImsApnName: " + apnName);
        return apnName;
    }

    private void notifyOnWifiPdnOOS(String apn, int callId, int oosState, int simId) {
        log("onWifiPdnOOS apn: " + apn + " callId: " + callId + " oosState: " + oosState + " simId: " + simId);
        if (isImsApn(apn)) {
            int i = this.mListeners.beginBroadcast();
            while (i > 0) {
                i--;
                try {
                    this.mListeners.getBroadcastItem(i).onWifiPdnOOSStateChanged(simId, oosState);
                    log("onWifiPdnOOSStateChanged");
                } catch (RemoteException e) {
                    log("onRoveOut: RemoteException occurs!");
                }
            }
            this.mListeners.finishBroadcast();
        }
    }

    public IWifiOffloadService getWfoInterface() {
        return this.mWfoService;
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
            return null;
        }
        return this.mMwiRil[phoneId];
    }

    /* access modifiers changed from: private */
    public void checkIfShowNoInternetError(boolean showImmediately) {
        int mainCapabilityPhoneId = getMainCapabilityPhoneId();
        boolean isImsReg = TelephonyManager.getDefault().isImsRegistered(getSubIdBySlot(mainCapabilityPhoneId));
        boolean isWifiConnected = this.mWifiPdnHandler.isWifiConnected();
        if (mainCapabilityPhoneId != -1) {
            if (!this.mIsWfcSettingsOn[mainCapabilityPhoneId] || isImsReg || !this.mIsWifiL2Connected || isWifiConnected) {
                if (hasMessages(2002)) {
                    log("checkIfShowNoInternetError(): cancel 8s timeout");
                    removeMessages(2002);
                }
            } else if (showImmediately) {
                this.mWosExt.showPDNErrorMessages(WIFI_NO_INTERNET_ERROR_CODE);
            } else if (!hasMessages(2002)) {
                log("checkIfShowNoInternetError(): start 8s timeout");
                sendMessageDelayed(obtainMessage(2002), (long) WIFI_NO_INTERNET_TIMEOUT);
            }
        }
    }

    private int getSubIdBySlot(int slot) {
        int[] subId = SubscriptionManager.getSubId(slot);
        return subId != null ? subId[0] : SubscriptionManager.getDefaultSubscriptionId();
    }

    /* access modifiers changed from: private */
    public void createWosExt() {
        IWosExt iWosExt = this.mWosExt;
        if (iWosExt != null) {
            iWosExt.dispose();
            this.mWosExt = null;
        }
        IWosExt makeWosExt = OpWosCustomizationUtils.getOpFactory(this.mContext).makeWosExt(this.mContext);
        this.mWosExt = makeWosExt;
        makeWosExt.initialize(this.mContext);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        if (!USR_BUILD || TELDBG) {
            Rlog.d(TAG, s);
        }
    }

    public void onLocationTimeout() {
        sendMessage(obtainMessage(2201));
    }

    private void notifyLocationTimeout() {
        IWosExt iWosExt = this.mWosExt;
        if (iWosExt != null) {
            iWosExt.showLocationTimeoutMessage();
        } else {
            Rlog.e(TAG, "notifyLocationTimeout: mWosExt null");
        }
    }

    public void updatedWifiConnectedStatus(boolean isConnected) {
        log("updatedWifiConnectedStatus: " + isConnected);
        this.mIsWifiConnected = isConnected;
        if (!isConnected) {
            this.mPartialScanCount = 0;
        }
    }

    private int getCurrentAssociatedApCount() {
        int count = 0;
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager != null) {
            List<ScanResult> scanResults = wifiManager.getScanResults();
            List<WifiConfiguration> wifiConfigList = wifiManager.getConfiguredNetworks();
            if (scanResults == null || wifiConfigList == null) {
                Rlog.e(TAG, "getCurrentAssociatedApCount() scanResults = " + scanResults + ", wifiConfigList = " + wifiConfigList);
            } else {
                for (ScanResult appInfo : scanResults) {
                    for (WifiConfiguration configuredAP : wifiConfigList) {
                        String strTrimmed = "";
                        if (configuredAP.SSID != null) {
                            strTrimmed = configuredAP.SSID.replace("\"", "");
                        }
                        if (appInfo.SSID != null && appInfo.SSID.equals(strTrimmed)) {
                            count++;
                        }
                    }
                }
            }
        } else {
            Rlog.e(TAG, "getCurrentAssociatedApCount() wifiManager null");
        }
        log("getCurrentAssociatedApCount(): count= " + count);
        return count;
    }

    private void updateWifiEnabled() {
        if (!StorageManager.inCryptKeeperBounce()) {
            WifiManager wifiMngr = (WifiManager) this.mContext.getSystemService("wifi");
            if (wifiMngr != null) {
                this.mIsWifiEnabled = wifiMngr.isWifiEnabled();
            } else {
                Rlog.e(TAG, "updateWifiEnabled: WifiManager null");
                this.mIsWifiEnabled = false;
            }
        } else {
            Rlog.e(TAG, "updateWifiEnabled: inCryptKeeperBounce");
            this.mIsWifiEnabled = false;
        }
        sendMessage(obtainMessage(2000));
    }

    private boolean getBooleanCarrierConfig(String key, int subId) {
        boolean ret;
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        if (b != null) {
            ret = b.getBoolean(key);
        } else {
            log("getBooleanCarrierConfig: get from default config");
            ret = CarrierConfigManager.getDefaultConfig().getBoolean(key);
        }
        log("getBooleanCarrierConfig sub: " + subId + " key: " + key + " ret: " + ret);
        return ret;
    }

    private void onWfcStatusChanged() {
        if (!this.mWifiPdnHandler.isWifiPdnExisted() && this.mHasWiFiDisabledPending) {
            this.mHasWiFiDisabledPending = false;
            sendMessage(obtainMessage(2202));
        }
    }
}
