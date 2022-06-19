package com.mediatek.wfo.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.mediatek.ims.ImsConstants;
import com.mediatek.wfo.ril.MwiRIL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class WfcLocationHandler extends Handler {
    private static final String ACTION_LOCATION_CACHE = "com.mediatek.intent.action.LOCATION_HANDLE";
    private static final int BASE = 3000;
    private static final int BROADCAST_FLAG_ENABLE = 1;
    private static final int CACHE_DISABLE = 0;
    private static final int CACHE_ENABLE = 1;
    private static final String CACHE_ENABLE_EXTRA = "enable_location_handle";
    private static final String COUNTRY_CODE_HK = "HK";
    private static final int DEFAULT_CONFIDENCE_LEVEL = 68;
    /* access modifiers changed from: private */
    public static final boolean ENGLOAD = "eng".equals(Build.TYPE);
    private static final int EVENT_ALL_RETRY_GET_LOCATION_REQUST = 3008;
    private static final int EVENT_DIALING_E911 = 3006;
    private static final int EVENT_GET_LAST_KNOWN_LOCATION = 3001;
    public static final int EVENT_GET_LOCATION_REQUEST = 3000;
    private static final int EVENT_HANDLE_LAST_KNOWN_LOCATION_RESPONSE = 3003;
    private static final int EVENT_HANDLE_NETWORK_LOCATION_RESPONSE = 3002;
    private static final int EVENT_LOCATION_CACHE = 3011;
    private static final int EVENT_LOCATION_MODE_CHANGED = 3009;
    private static final int EVENT_LOCATION_PROVIDERS_CHANGED = 3013;
    private static final int EVENT_REQUEST_NETWORK_LOCATION = 3010;
    private static final int EVENT_RETRY_GET_LOCATION_REQUEST = 3007;
    private static final int EVENT_RETRY_NETWORK_LOCATION_REQUEST = 3012;
    private static final int EVENT_SET_COUNTRY_CODE = 3005;
    private static final int EVENT_SET_LOCATION_INFO = 3004;
    private static final String KEY_LOCATION_CACHE = "key_ocation_cache";
    private static final String KEY_LOCATION_CACHE_ACCOUNTID = "key_accountid";
    private static final String KEY_LOCATION_CACHE_ACCURACY = "key_accuracy";
    private static final String KEY_LOCATION_CACHE_BROADCASTFLAG = "key_broadcastflag";
    private static final String KEY_LOCATION_CACHE_CITY = "key_city";
    private static final String KEY_LOCATION_CACHE_COUNTRYCODE = "key_countrycode";
    private static final String KEY_LOCATION_CACHE_LATITUDE = "key_latitude";
    private static final String KEY_LOCATION_CACHE_LONGTITUDE = "key_longitude";
    private static final String KEY_LOCATION_CACHE_METHOD = "key_method";
    private static final String KEY_LOCATION_CACHE_STATE = "key_state";
    private static final String KEY_LOCATION_CACHE_ZIP = "key_zip";
    private static final String LOCATION_PERMISSION_NAME = "android.permission.ACCESS_FINE_LOCATION";
    private static int MAX_GEOCODING_FAILURE_RETRY = 5;
    private static int MAX_NETWORK_LOCATION_RETRY = 15;
    private static int MAX_NUM_OF_GET_LOCATION_TASKS_THREAD = 3;
    private static final int MAX_VALID_PARAM_COUNT = 7;
    private static final int MAX_VALID_SIM_COUNT = 4;
    private static final int MSG_REG_IMSA_REQUEST_GEO_LOCATION_INFO = 96009;
    private static final int MSG_REG_IMSA_RESPONSE_GETO_LOCATION_INFO = 91030;
    public static final String MTK_KEY_WFC_GET_CONFIDENCE_LEVEL = "mtk_carrier_wfc_get_confidence_level";
    public static final String MTK_KEY_WFC_GET_LOCATION_ALWAYS = "mtk_carrier_wfc_get_location_always";
    public static final String MTK_KEY_WFC_LOCATION_RESPONSE_TIMEOUT = "mtk_carrier_wfc_location_response_timeout";
    private static int NETWORK_LOCATION_UPDATE_TIME = 1000;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static int REQUEST_GEOLOCATION_FROM_NETWORK_TIMEOUT = 55000;
    private static final int REQUEST_LOCATION_RETRY_TIMEOUT = 5000;
    private static final int REQUEST_NETWORK_LOCATION_RETRY_TIMEOUT = 3000;
    private static final int RESPONSE_SET_LOCATION_ENABLED = 3101;
    private static final int RESPONSE_SET_LOCATION_INFO = 3100;
    private static final float[] STANDARD_NORMAL_DISTRIBUTION_TABLE = {0.5f, 0.5398f, 0.5793f, 0.6179f, 0.6554f, 0.6915f, 0.7257f, 0.758f, 0.7881f, 0.8159f, 0.8413f, 0.8643f, 0.8849f, 0.9032f, 0.9192f, 0.9332f, 0.9452f, 0.9554f, 0.9641f, 0.9713f, 0.9772f, 0.9821f, 0.9861f, 0.9893f, 0.9918f, 0.9938f, 0.9953f, 0.9965f, 0.9974f, 0.9981f, 0.9987f, 1.0f};
    private static final String TAG = "WfcLocationHandler";
    private static final boolean TELDBG;
    private static final boolean USR_BUILD = (TextUtils.equals(Build.TYPE, "user") || TextUtils.equals(Build.TYPE, "userdebug"));
    private CallStateListener mCallStateListener = new CallStateListener();
    private CarrierConfigManager mConfigManager;
    private Context mContext;
    private Context mDeviceContext;
    private Geocoder mGeoCoder;
    /* access modifiers changed from: private */
    public int mGeocodingFailRetry;
    private String mGnssProxyPackageName;
    /* access modifiers changed from: private */
    public List<String> mIgnoreList = Arrays.asList(new String[]{"186119"});
    private boolean mLastLocationSetting = false;
    /* access modifiers changed from: private */
    public ArrayList<LocationInfo> mLocationInfoQueue = new ArrayList<>();
    private LocationListenerImp mLocationListener = new LocationListenerImp();
    /* access modifiers changed from: private */
    public LocationManager mLocationManager;
    private Object mLocationRequestLock = new Object();
    private boolean mLocationRequestRegistered = false;
    private boolean mLocationSetting = false;
    /* access modifiers changed from: private */
    public boolean mLocationTimeout = false;
    /* access modifiers changed from: private */
    public Object mLocationTimeoutLock = new Object();
    private MwiRIL[] mMwiRil;
    /* access modifiers changed from: private */
    public boolean mNetworkAvailable = false;
    private int mNetworkLocationRetry;
    private ArrayList<LocationInfo> mNetworkLocationTasks = new ArrayList<>();
    private PackageManager mPackageManager;
    private ArrayList<Message> mPendingLocationRequest = new ArrayList<>();
    private String mPlmnCountryCode = "";
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                WfcLocationHandler wfcLocationHandler = WfcLocationHandler.this;
                wfcLocationHandler.log("onReceive action:" + intent.getAction());
                if (intent.getAction().equals("android.telephony.action.NETWORK_COUNTRY_CHANGED")) {
                    String lowerCaseCountryCode = (String) intent.getExtra("android.telephony.extra.NETWORK_COUNTRY");
                    if (lowerCaseCountryCode == null || TextUtils.isEmpty(lowerCaseCountryCode)) {
                        Rlog.w(WfcLocationHandler.TAG, "iso country code is null");
                        return;
                    }
                    String isoCountryCode = lowerCaseCountryCode.toUpperCase();
                    Rlog.i(WfcLocationHandler.TAG, "ACTION_LOCATED_PLMN_CHANGED, iso: " + isoCountryCode);
                    if (!WfcLocationHandler.this.isCtaNotAllow()) {
                        WfcLocationHandler.this.obtainMessage(WfcLocationHandler.EVENT_SET_COUNTRY_CODE, isoCountryCode).sendToTarget();
                    }
                } else if (intent.getAction().equals("android.location.MODE_CHANGED")) {
                    Rlog.i(WfcLocationHandler.TAG, "LocationManager.MODE_CHANGED_ACTION");
                    WfcLocationHandler.this.obtainMessage(WfcLocationHandler.EVENT_LOCATION_MODE_CHANGED).sendToTarget();
                } else if (intent.getAction().equals("android.location.PROVIDERS_CHANGED")) {
                    boolean isNlpEnabled = WfcLocationHandler.this.mLocationManager.isProviderEnabled("network");
                    synchronized (WfcLocationHandler.this.mLocationTimeoutLock) {
                        Rlog.i(WfcLocationHandler.TAG, "LocationManager.PROVIDERS_CHANGED_ACTION isNlpEnabled: " + isNlpEnabled + ", location timeout = " + WfcLocationHandler.this.mLocationTimeout);
                        if (WfcLocationHandler.this.mLocationTimeout && isNlpEnabled) {
                            WfcLocationHandler.this.obtainMessage(WfcLocationHandler.EVENT_LOCATION_PROVIDERS_CHANGED).sendToTarget();
                        }
                    }
                } else if (intent.getAction().equals(WfcLocationHandler.ACTION_LOCATION_CACHE)) {
                    WfcLocationHandler.this.obtainMessage(WfcLocationHandler.EVENT_LOCATION_CACHE, intent.getIntExtra(WfcLocationHandler.CACHE_ENABLE_EXTRA, 0), 0).sendToTarget();
                }
            }
        }
    };
    private int mSimCount;
    private TelecomManager mTelecomManager;
    private TelephonyManager mTelephonyManager;
    private WfcHandler mWfcHandler;
    private String mWifiMacAddr = "";
    /* access modifiers changed from: private */
    public WifiPdnHandler mWifiPdnHandler;

    static /* synthetic */ int access$808(WfcLocationHandler x0) {
        int i = x0.mGeocodingFailRetry;
        x0.mGeocodingFailRetry = i + 1;
        return i;
    }

    static /* synthetic */ int access$810(WfcLocationHandler x0) {
        int i = x0.mGeocodingFailRetry;
        x0.mGeocodingFailRetry = i - 1;
        return i;
    }

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        TELDBG = z;
    }

    class CallStateListener extends PhoneStateListener {
        CallStateListener() {
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            Rlog.i(WfcLocationHandler.TAG, "onCallStateChanged state=" + state);
            if (state == 2 && WfcLocationHandler.this.isEccInProgress()) {
                Rlog.i(WfcLocationHandler.TAG, "E911 is dialing");
                if (WfcLocationHandler.this.mIgnoreList.contains(incomingNumber)) {
                    Rlog.e(WfcLocationHandler.TAG, "onCallStateChanged: ignore");
                } else if (WfcLocationHandler.this.mWifiPdnHandler.isWifiConnected() || WfcLocationHandler.this.mNetworkAvailable) {
                    WfcLocationHandler.this.obtainMessage(WfcLocationHandler.EVENT_DIALING_E911).sendToTarget();
                } else {
                    Rlog.e(WfcLocationHandler.TAG, "E911, Wi-Fi isn't connected and network unavailable");
                    WfcLocationHandler.this.addRetryLocationRequestForECC();
                }
            }
        }
    }

    private class LocationListenerImp implements LocationListener {
        private LocationListenerImp() {
        }

        public void onLocationChanged(Location location) {
            if (WfcLocationHandler.ENGLOAD) {
                Rlog.i(WfcLocationHandler.TAG, "onLocationChanged: " + location);
            } else {
                Rlog.i(WfcLocationHandler.TAG, "onLocationChanged");
            }
            long newNlpTime = location.getTime();
            WfcLocationHandler wfcLocationHandler = WfcLocationHandler.this;
            wfcLocationHandler.log("onLocationChanged newNlpTime: " + newNlpTime);
            boolean isCache = false;
            Iterator it = WfcLocationHandler.this.mLocationInfoQueue.iterator();
            while (it.hasNext()) {
                LocationInfo locationInfo = (LocationInfo) it.next();
                WfcLocationHandler wfcLocationHandler2 = WfcLocationHandler.this;
                wfcLocationHandler2.log("onLocationChanged locationInfo time: " + locationInfo.mTime);
                if (locationInfo.mTime == newNlpTime) {
                    isCache = true;
                }
            }
            if (isCache) {
                WfcLocationHandler wfcLocationHandler3 = WfcLocationHandler.this;
                wfcLocationHandler3.log("onLocationChanged isCache: " + isCache);
                WfcLocationHandler.access$808(WfcLocationHandler.this);
            }
            WfcLocationHandler.this.cancelNetworkLocationRequest();
            synchronized (WfcLocationHandler.this.mLocationTimeoutLock) {
                boolean unused = WfcLocationHandler.this.mLocationTimeout = false;
            }
            WfcLocationHandler.this.log("removeMessages: EVENT_GET_LAST_KNOWN_LOCATION");
            WfcLocationHandler.this.removeMessages(WfcLocationHandler.EVENT_GET_LAST_KNOWN_LOCATION);
            WfcLocationHandler.this.obtainMessage(WfcLocationHandler.EVENT_HANDLE_NETWORK_LOCATION_RESPONSE, 0, 0, location).sendToTarget();
        }

        public void onProviderDisabled(String provider) {
            WfcLocationHandler wfcLocationHandler = WfcLocationHandler.this;
            wfcLocationHandler.log("onProviderDisabled: " + provider);
        }

        public void onProviderEnabled(String provider) {
            WfcLocationHandler wfcLocationHandler = WfcLocationHandler.this;
            wfcLocationHandler.log("onProviderEnabled: " + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            WfcLocationHandler wfcLocationHandler = WfcLocationHandler.this;
            wfcLocationHandler.log("onStatusChanged: " + provider + ", status=" + status);
        }
    }

    public class LocationInfo {
        public int mAccountId;
        public float mAccuracy;
        public double mAltitude;
        public int mBroadcastFlag;
        public String mCity = "";
        public int mConfidence;
        public String mCountryCode = "";
        public double mLatitude;
        public double mLongitude;
        public float mMajorAxisAccuracy;
        public String mMethod = "";
        public float mMinorAxisAccuracy;
        public int mSimIdx;
        public String mState = "";
        public long mTime;
        public float mVericalAxisAccuracy;
        public String mZip = "";

        LocationInfo(int simIdx, int accountId, int broadcastFlag, double latitude, double longitude, float accuracy) {
            this.mSimIdx = simIdx;
            this.mAccountId = accountId;
            this.mBroadcastFlag = broadcastFlag;
            this.mConfidence = 0;
            this.mLatitude = latitude;
            this.mLongitude = longitude;
            this.mAltitude = 0.0d;
            this.mAccuracy = accuracy;
            this.mMajorAxisAccuracy = accuracy;
            this.mMinorAxisAccuracy = accuracy;
            this.mVericalAxisAccuracy = 0.0f;
            this.mTime = 0;
        }

        LocationInfo(int simIdx, int accountId, int broadcastFlag, double latitude, double longitude, float accuracy, int confidence) {
            this.mSimIdx = simIdx;
            this.mAccountId = accountId;
            this.mBroadcastFlag = broadcastFlag;
            this.mConfidence = confidence;
            this.mLatitude = latitude;
            this.mLongitude = longitude;
            this.mAltitude = 0.0d;
            this.mAccuracy = accuracy;
            this.mMajorAxisAccuracy = accuracy;
            this.mMinorAxisAccuracy = accuracy;
            this.mVericalAxisAccuracy = 0.0f;
            this.mTime = 0;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[LocationInfo objId: ");
            sb.append(System.identityHashCode(this));
            sb.append(", phoneId: " + this.mSimIdx);
            sb.append(", transationId: " + this.mAccountId);
            sb.append(", accuracy: " + this.mAccuracy);
            sb.append(", confidence: " + this.mConfidence);
            sb.append(", vericalAxisAccuracy: " + this.mVericalAxisAccuracy);
            sb.append(", broadcastFlag: " + this.mBroadcastFlag);
            sb.append(", method: " + this.mMethod);
            sb.append(", city: " + WfcLocationHandler.this.maskString(this.mCity));
            sb.append(", state: " + WfcLocationHandler.this.maskString(this.mState));
            sb.append(", zip: " + WfcLocationHandler.this.maskString(this.mZip));
            sb.append(", countryCode: " + WfcLocationHandler.this.maskString(this.mCountryCode));
            sb.append(", time: " + this.mTime);
            return sb.toString();
        }
    }

    public void handleMessage(Message msg) {
        Message message = msg;
        log("handleMessage: msg= " + messageToString(msg));
        switch (message.what) {
            case 3000:
                if (!isCtaNotAllow()) {
                    if (this.mWifiPdnHandler.isWifiConnected() || this.mNetworkAvailable) {
                        handleLocationRequest(msg);
                        return;
                    }
                    Rlog.e(TAG, "Wi-Fi isn't connected and network unavailable.");
                    addRetryLocationRequest(msg);
                    return;
                }
                return;
            case EVENT_GET_LAST_KNOWN_LOCATION /*3001*/:
                synchronized (this.mLocationTimeoutLock) {
                    this.mLocationTimeout = true;
                }
                WfcHandler wfcHandler = this.mWfcHandler;
                if (wfcHandler != null) {
                    wfcHandler.onLocationTimeout();
                } else {
                    Rlog.e(TAG, "EVENT_GET_LAST_KNOWN_LOCATION: WfcHandler null");
                }
                if (getLastKnownLocation((LocationInfo) message.obj)) {
                    cancelNetworkLocationRequest();
                    return;
                }
                return;
            case EVENT_HANDLE_NETWORK_LOCATION_RESPONSE /*3002*/:
            case EVENT_HANDLE_LAST_KNOWN_LOCATION_RESPONSE /*3003*/:
                handleNetworkLocationUpdate((Location) message.obj);
                return;
            case EVENT_SET_LOCATION_INFO /*3004*/:
                setLocationInfo((LocationInfo) message.obj);
                return;
            case EVENT_SET_COUNTRY_CODE /*3005*/:
                String iso = (String) message.obj;
                if (!TextUtils.isEmpty(iso)) {
                    if (TextUtils.isEmpty(this.mPlmnCountryCode)) {
                        setCountryCode(iso);
                    } else if (!iso.equals(this.mPlmnCountryCode)) {
                        if (this.mWifiPdnHandler.isWifiConnected() || this.mNetworkAvailable) {
                            dispatchLocationRequest(new LocationInfo(0, 0, 1, 0.0d, 0.0d, 0.0f));
                        } else {
                            setCountryCode(iso);
                        }
                    }
                    this.mPlmnCountryCode = iso;
                    return;
                }
                return;
            case EVENT_DIALING_E911 /*3006*/:
            case EVENT_REQUEST_NETWORK_LOCATION /*3010*/:
            case EVENT_LOCATION_PROVIDERS_CHANGED /*3013*/:
                dispatchLocationRequest(new LocationInfo(0, 0, 1, 0.0d, 0.0d, 0.0f));
                return;
            case EVENT_RETRY_GET_LOCATION_REQUEST /*3007*/:
                handleRetryLocationRequest(msg);
                return;
            case EVENT_ALL_RETRY_GET_LOCATION_REQUST /*3008*/:
                handleAllRetryLocationRequest();
                return;
            case EVENT_LOCATION_MODE_CHANGED /*3009*/:
                boolean isLocationEnabled = this.mLocationManager.isLocationEnabled();
                this.mLocationSetting = isLocationEnabled;
                if (isLocationEnabled != this.mLastLocationSetting) {
                    setLocationEnabled();
                    return;
                }
                log("Same location setting:" + this.mLocationSetting);
                return;
            case EVENT_LOCATION_CACHE /*3011*/:
                int cacheEnable = message.arg1;
                Rlog.d(TAG, "cacheEnable: " + cacheEnable);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mDeviceContext).edit();
                if (cacheEnable == 1) {
                    LocationInfo locationInfo = r1;
                    LocationInfo locationInfo2 = new LocationInfo(0, 0, 0, 0.0d, 0.0d, 0.0f);
                    getLocationCache(locationInfo);
                    setLocationCacheEnable(true);
                    proccessLocationFromNetwork(new LocationInfo(0, 8, 0, 0.0d, 0.0d, 0.0f));
                    return;
                }
                setLocationCacheEnable(false);
                if (!editor.commit()) {
                    log("Failed to commit location cache");
                }
                cancelNetworkLocationRequest();
                return;
            case EVENT_RETRY_NETWORK_LOCATION_REQUEST /*3012*/:
                LocationInfo info = (LocationInfo) message.obj;
                if (!requestLocationFromNetworkLocation()) {
                    int i = this.mNetworkLocationRetry;
                    this.mNetworkLocationRetry = i - 1;
                    if (i > 0) {
                        log("EVENT_RETRY_NETWORK_LOCATION_REQUEST retry.");
                        sendMessageDelayed(obtainMessage(EVENT_RETRY_NETWORK_LOCATION_REQUEST, 0, 0, info), 3000);
                        return;
                    }
                    log("EVENT_RETRY_NETWORK_LOCATION_REQUEST retry fail, skip.");
                    this.mNetworkLocationTasks.remove(info);
                    this.mNetworkLocationRetry = MAX_NETWORK_LOCATION_RETRY;
                    return;
                }
                return;
            default:
                return;
        }
    }

    private String messageToString(Message msg) {
        switch (msg.what) {
            case 3000:
                return "EVENT_GET_LOCATION_REQUEST";
            case EVENT_GET_LAST_KNOWN_LOCATION /*3001*/:
                return "EVENT_GET_LAST_KNOWN_LOCATION";
            case EVENT_HANDLE_NETWORK_LOCATION_RESPONSE /*3002*/:
                return "EVENT_HANDLE_NETWORK_LOCATION_RESPONSE";
            case EVENT_HANDLE_LAST_KNOWN_LOCATION_RESPONSE /*3003*/:
                return "EVENT_HANDLE_LAST_KNOWN_LOCATION_RESPONSE";
            case EVENT_SET_LOCATION_INFO /*3004*/:
                return "EVENT_SET_LOCATION_INFO";
            case EVENT_SET_COUNTRY_CODE /*3005*/:
                return "EVENT_SET_COUNTRY_CODE";
            case EVENT_DIALING_E911 /*3006*/:
                return "EVENT_DIALING_E911";
            case EVENT_RETRY_GET_LOCATION_REQUEST /*3007*/:
                return "EVENT_RETRY_GET_LOCATION_REQUEST";
            case EVENT_ALL_RETRY_GET_LOCATION_REQUST /*3008*/:
                return "EVENT_ALL_RETRY_GET_LOCATION_REQUST";
            case EVENT_LOCATION_MODE_CHANGED /*3009*/:
                return "EVENT_LOCATION_MODE_CHANGED";
            case EVENT_REQUEST_NETWORK_LOCATION /*3010*/:
                return "EVENT_REQUEST_NETWORK_LOCATION";
            case EVENT_LOCATION_CACHE /*3011*/:
                return "EVENT_LOCATION_CACHE";
            case EVENT_RETRY_NETWORK_LOCATION_REQUEST /*3012*/:
                return "EVENT_RETRY_NETWORK_LOCATION_REQUEST";
            case EVENT_LOCATION_PROVIDERS_CHANGED /*3013*/:
                return "EVENT_LOCATION_PROVIDERS_CHANGED";
            case RESPONSE_SET_LOCATION_INFO /*3100*/:
                return "RESPONSE_SET_LOCATION_INFO";
            case RESPONSE_SET_LOCATION_ENABLED /*3101*/:
                return "RESPONSE_SET_LOCATION_ENABLED";
            default:
                return "UNKNOWN";
        }
    }

    public Handler getHandler() {
        return this;
    }

    public WfcLocationHandler(Context context, WfcHandler wfcHandler, WifiPdnHandler wifiPdnHandler, int simCount, Looper looper, MwiRIL[] mwiRil) {
        super(looper);
        this.mContext = context;
        Context createDeviceProtectedStorageContext = context.createDeviceProtectedStorageContext();
        this.mDeviceContext = createDeviceProtectedStorageContext;
        if (createDeviceProtectedStorageContext == null) {
            this.mDeviceContext = this.mContext;
            log("replace mContext to mDeviceContext");
        }
        this.mWfcHandler = wfcHandler;
        this.mWifiPdnHandler = wifiPdnHandler;
        this.mSimCount = simCount <= 4 ? simCount : 4;
        this.mMwiRil = mwiRil;
        this.mGeocodingFailRetry = MAX_GEOCODING_FAILURE_RETRY;
        this.mNetworkLocationRetry = MAX_NETWORK_LOCATION_RETRY;
        this.mGeoCoder = new Geocoder(this.mContext, Locale.US);
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mTelephonyManager = telephonyManager;
        telephonyManager.listen(this.mCallStateListener, 32);
        this.mLocationSetting = this.mLocationManager.isLocationEnabled();
        log("1st time send location setting to modem, mLocationSetting:" + this.mLocationSetting);
        setLocationEnabled();
        this.mTelecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        this.mGnssProxyPackageName = loadProxyNameFromCarrierConfig();
        this.mPackageManager = this.mContext.getPackageManager();
        this.mConfigManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        registerForBroadcast();
        registerIndication();
        registerDefaultNetwork();
    }

    /* access modifiers changed from: protected */
    public void notifyMultiSimConfigChanged(int activeModemCount, MwiRIL[] mwiRil) {
        int prevActiveModemCount = this.mSimCount;
        this.mMwiRil = mwiRil;
        Rlog.i(TAG, "notifyMultiSimConfigChanged, phone:" + prevActiveModemCount + "->" + activeModemCount + ", mSimCount:" + this.mSimCount);
        if (prevActiveModemCount != activeModemCount) {
            this.mSimCount = activeModemCount;
            if (prevActiveModemCount <= activeModemCount) {
                for (int i = prevActiveModemCount; i < activeModemCount; i++) {
                    this.mMwiRil[i].registerRequestGeoLocation(this, 3000, (Object) null);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        if (!USR_BUILD || TELDBG) {
            Rlog.d(TAG, s);
        }
    }

    private void addRetryLocationRequest(Message msg) {
        synchronized (this.mLocationRequestLock) {
            String[] result = (String[]) ((AsyncResult) msg.obj).result;
            String[] retryRet = new String[7];
            String[] delayRet = new String[7];
            if (result != null) {
                if (result.length <= 7) {
                    System.arraycopy(result, 0, retryRet, 0, result.length);
                    System.arraycopy(result, 0, delayRet, 0, result.length);
                    AsyncResult retryAr = new AsyncResult((Object) null, retryRet, (Throwable) null);
                    AsyncResult delayAr = new AsyncResult((Object) null, delayRet, (Throwable) null);
                    Message retryMsg = obtainMessage(EVENT_RETRY_GET_LOCATION_REQUEST, retryAr);
                    if (this.mPendingLocationRequest.size() == 0) {
                        this.mPendingLocationRequest.add(retryMsg);
                    } else {
                        this.mPendingLocationRequest.set(0, retryMsg);
                    }
                    Rlog.i(TAG, "Added, current PendingLocationRequest size: " + this.mPendingLocationRequest.size());
                    sendMessageDelayed(obtainMessage(EVENT_RETRY_GET_LOCATION_REQUEST, delayAr), 5000);
                    return;
                }
            }
            Rlog.e(TAG, "addRetryLocationRequest: params invalid");
        }
    }

    /* access modifiers changed from: private */
    public void addRetryLocationRequestForECC() {
        String[] retryRet = {"0", "0", "0", "0", "0", "0"};
        retryRet[1] = String.valueOf(1);
        Message retryMsg = obtainMessage(EVENT_RETRY_GET_LOCATION_REQUEST, new AsyncResult((Object) null, retryRet, (Throwable) null));
        if (this.mPendingLocationRequest.size() == 0) {
            this.mPendingLocationRequest.add(retryMsg);
        } else {
            this.mPendingLocationRequest.set(0, retryMsg);
        }
        Rlog.i(TAG, "Added for ECC, current PendingLocationRequest size: " + this.mPendingLocationRequest.size());
    }

    private void handleRetryLocationRequest(Message msg) {
        synchronized (this.mLocationRequestLock) {
            log("Current PendingLocationRequest size: " + this.mPendingLocationRequest.size());
            if (!this.mWifiPdnHandler.isWifiConnected()) {
                if (!this.mNetworkAvailable) {
                    log("Network not available, ignore EVENT_RETRY_GET_LOCATION_REQUEST.");
                }
            }
            handleLocationRequest(msg);
        }
    }

    private void handleAllRetryLocationRequest() {
        log("handleAllRetryLocationRequest mPendingLocationRequest.size(): " + this.mPendingLocationRequest.size());
        synchronized (this.mLocationRequestLock) {
            if (hasMessages(EVENT_RETRY_GET_LOCATION_REQUEST)) {
                removeMessages(EVENT_RETRY_GET_LOCATION_REQUEST);
            }
            Iterator<Message> it = this.mPendingLocationRequest.iterator();
            while (it.hasNext()) {
                Message msg = it.next();
                if (!this.mWifiPdnHandler.isWifiConnected()) {
                    if (!this.mNetworkAvailable) {
                        log("Network not available, ignore EVENT_RETRY_GET_LOCATION_REQUEST.");
                    }
                }
                handleLocationRequest(msg);
            }
            this.mPendingLocationRequest.clear();
        }
    }

    private void registerDefaultNetwork() {
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                Rlog.i(WfcLocationHandler.TAG, "NetworkCallback.onAvailable()");
                boolean unused = WfcLocationHandler.this.mNetworkAvailable = true;
                WfcLocationHandler.this.sendEmptyMessage(WfcLocationHandler.EVENT_ALL_RETRY_GET_LOCATION_REQUST);
            }

            public void onLost(Network network) {
                Rlog.i(WfcLocationHandler.TAG, "NetworkCallback.onLost()");
                boolean unused = WfcLocationHandler.this.mNetworkAvailable = false;
            }
        });
    }

    private void handleLocationRequest(Message msg) {
        Message message = msg;
        if (message.obj == null) {
            Rlog.e(TAG, "handleLocationInfo(): msg.obj is null");
            return;
        }
        String[] result = (String[]) ((AsyncResult) message.obj).result;
        if (result == null) {
            Rlog.e(TAG, "handleLocationInfo(): result is null");
        } else if (result.length < 7) {
            Rlog.e(TAG, "handleLocationInfo(): params invalid");
        } else {
            try {
                LocationInfo locationInfo = new LocationInfo(Integer.parseInt(result[6]), Integer.parseInt(result[0]), Integer.parseInt(result[1]), Double.parseDouble(result[2]), Double.parseDouble(result[3]), Float.parseFloat(result[4]), Integer.parseInt(result[5]));
                log("handleLocationRequest(): " + locationInfo);
                dispatchLocationRequest(locationInfo);
            } catch (Exception e) {
                log("handleLocationRequest(), [" + result.length + "], accId:" + result[0] + ", broadcastFlag:" + result[1] + ", confidence:" + result[5] + ", simIdx:" + result[6] + ", error:" + e);
            }
        }
    }

    private void dispatchLocationRequest(LocationInfo info) {
        double latitude = info.mLatitude;
        double longitude = info.mLongitude;
        float accuracy = info.mAccuracy;
        if (latitude == 0.0d && longitude == 0.0d && accuracy == 0.0f) {
            proccessLocationFromNetwork(info);
        } else {
            cancelNetworkLocationRequest();
            synchronized (this.mLocationTimeoutLock) {
                this.mLocationTimeout = false;
            }
            log("removeMessages: EVENT_GET_LAST_KNOWN_LOCATION");
            removeMessages(EVENT_GET_LAST_KNOWN_LOCATION);
            info.mMethod = "GPS";
            this.mLocationInfoQueue.add(info);
            pollLocationInfo();
        }
        log("dispatchLocationRequest(): " + info.mMethod);
    }

    private void handleNetworkLocationUpdate(Location location) {
        if (location == null) {
            log("network location get null, unexpected result");
            return;
        }
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double altitude = location.getAltitude();
        float accuracy = location.getAccuracy();
        float verticalAccuracy = location.getVerticalAccuracyMeters();
        long time = location.getTime();
        log("update all LocationInfo with  time: " + time + " accuracy: " + accuracy + " altitude: " + altitude + " verticalAccuracy: " + verticalAccuracy);
        ArrayList<LocationInfo> duplicatedInfo = new ArrayList<>();
        Iterator<LocationInfo> it = this.mNetworkLocationTasks.iterator();
        while (it.hasNext()) {
            LocationInfo locationInfo = it.next();
            locationInfo.mLatitude = latitude;
            locationInfo.mLongitude = longitude;
            locationInfo.mAltitude = altitude;
            locationInfo.mAccuracy = accuracy;
            locationInfo.mMajorAxisAccuracy = accuracy;
            locationInfo.mMinorAxisAccuracy = accuracy;
            locationInfo.mVericalAxisAccuracy = verticalAccuracy;
            locationInfo.mTime = time;
            duplicatedInfo.clear();
            Iterator<LocationInfo> it2 = this.mLocationInfoQueue.iterator();
            while (it2.hasNext()) {
                LocationInfo gpsLocationInfo = it2.next();
                double latitude2 = latitude;
                if (gpsLocationInfo.mAccountId == locationInfo.mAccountId) {
                    duplicatedInfo.add(gpsLocationInfo);
                }
                latitude = latitude2;
            }
            double latitude3 = latitude;
            Iterator<LocationInfo> it3 = duplicatedInfo.iterator();
            while (it3.hasNext()) {
                this.mLocationInfoQueue.remove(it3.next());
            }
            this.mLocationInfoQueue.add(locationInfo);
            latitude = latitude3;
        }
        pollLocationInfo();
        this.mNetworkLocationTasks.clear();
    }

    private void proccessLocationFromNetwork(LocationInfo info) {
        info.mMethod = "Network";
        this.mNetworkLocationTasks.add(info);
        if (!requestLocationFromNetworkLocation()) {
            log("requestLocationFromNetworkLocation failed");
            setLocationInfo(info);
            if (hasMessages(EVENT_RETRY_NETWORK_LOCATION_REQUEST)) {
                removeMessages(EVENT_RETRY_NETWORK_LOCATION_REQUEST);
                this.mNetworkLocationRetry = MAX_NETWORK_LOCATION_RETRY;
            }
            int i = this.mNetworkLocationRetry;
            this.mNetworkLocationRetry = i - 1;
            if (i > 0) {
                log("requestLocationFromNetworkLocation retry.");
                sendMessageDelayed(obtainMessage(EVENT_RETRY_NETWORK_LOCATION_REQUEST, 0, 0, info), 3000);
            } else {
                log("requestLocationFromNetworkLocation retry fail, skip.");
                this.mNetworkLocationTasks.remove(info);
                this.mNetworkLocationRetry = MAX_NETWORK_LOCATION_RETRY;
            }
        }
        if (!hasMessages(EVENT_GET_LAST_KNOWN_LOCATION)) {
            log("Add delayed message: EVENT_GET_LAST_KNOWN_LOCATION");
            sendMessageDelayed(obtainMessage(EVENT_GET_LAST_KNOWN_LOCATION, 0, 0, info), (long) getIntCarrierConfig(MTK_KEY_WFC_LOCATION_RESPONSE_TIMEOUT, REQUEST_GEOLOCATION_FROM_NETWORK_TIMEOUT, info.mSimIdx));
        }
    }

    private void pollLocationInfo() {
        if (this.mLocationInfoQueue.isEmpty()) {
            log("No GeoLocation task");
            return;
        }
        final List<LocationInfo> LocationInfoQueueCopy = new ArrayList<>(this.mLocationInfoQueue);
        this.mLocationInfoQueue.clear();
        new Thread(new Runnable() {
            public void run() {
                for (LocationInfo gpsLocationInfo : LocationInfoQueueCopy) {
                    Boolean retry = false;
                    LocationInfo res = WfcLocationHandler.this.getGeoLocationFromLatLong(gpsLocationInfo, retry);
                    if (res != null) {
                        WfcLocationHandler.this.obtainMessage(WfcLocationHandler.EVENT_SET_LOCATION_INFO, 0, 0, res).sendToTarget();
                    }
                    if (retry.booleanValue()) {
                        WfcLocationHandler wfcLocationHandler = WfcLocationHandler.this;
                        wfcLocationHandler.log("GeoCoding fail, retry = " + WfcLocationHandler.this.mGeocodingFailRetry);
                        if (WfcLocationHandler.this.mWifiPdnHandler.isWifiConnected() && WfcLocationHandler.this.mGeocodingFailRetry > 0) {
                            WfcLocationHandler.access$810(WfcLocationHandler.this);
                            WfcLocationHandler.this.obtainMessage(WfcLocationHandler.EVENT_REQUEST_NETWORK_LOCATION).sendToTarget();
                        }
                    }
                }
            }
        }).start();
    }

    private int getSubIdBySlot(int slot) {
        int[] subId = SubscriptionManager.getSubId(slot);
        return subId != null ? subId[0] : SubscriptionManager.getDefaultDataSubscriptionId();
    }

    private int getIntCarrierConfig(String key, int def, int simIdx) {
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        this.mConfigManager = carrierConfigManager;
        if (carrierConfigManager == null) {
            Rlog.e(TAG, "getIntCarrierConfig: Carrier Config service is NOT ready");
            return def;
        }
        int subId = getSubIdBySlot(simIdx);
        PersistableBundle configs = SubscriptionManager.isValidSubscriptionId(subId) ? this.mConfigManager.getConfigForSubId(subId) : null;
        if (configs == null) {
            Rlog.e(TAG, "getIntCarrierConfig: SIM not ready, use default carrier config");
            configs = CarrierConfigManager.getDefaultConfig();
        }
        int ret = configs.getInt(key, def);
        log("getIntCarrierConfig sub: " + subId + " key: " + key + " ret: " + ret);
        return ret;
    }

    private float getSigmaFromConf(float conf) {
        if (conf >= 100.0f) {
            return 6.0f;
        }
        if (conf <= 0.0f || conf == 67.0f || conf == 68.0f) {
            return 1.0f;
        }
        if (conf == 90.0f) {
            return 1.65f;
        }
        if (conf == 95.0f) {
            return 1.96f;
        }
        float distribution = (float) ((((double) conf) + 100.0d) / 200.0d);
        int index = 0;
        while (true) {
            float[] fArr = STANDARD_NORMAL_DISTRIBUTION_TABLE;
            if (index >= fArr.length) {
                return 0.0f;
            }
            if (fArr[index] == distribution) {
                return (float) (((double) index) * 0.1d);
            }
            if (index >= 1 && fArr[index] > distribution) {
                return (float) ((((double) (index - 1)) * 0.1d) + ((((double) (distribution - fArr[index - 1])) * 0.1d) / ((double) (fArr[index] - fArr[index - 1]))));
            }
            index++;
        }
    }

    private float adjustAccuracyForConfidence(float srcAccuracy, float srcConf, float destConf) {
        return (getSigmaFromConf(destConf) / getSigmaFromConf(srcConf)) * srcAccuracy;
    }

    private void setLocationInfo(LocationInfo info) {
        LocationInfo locationInfo = info;
        if (TextUtils.isEmpty(locationInfo.mState)) {
            locationInfo.mState = "Unknown";
        }
        if ((!TextUtils.isEmpty(this.mPlmnCountryCode) && TextUtils.length(locationInfo.mCountryCode) != 2) || COUNTRY_CODE_HK.equals(this.mPlmnCountryCode)) {
            locationInfo.mCountryCode = this.mPlmnCountryCode;
        } else if (TextUtils.isEmpty(this.mPlmnCountryCode) && TextUtils.length(locationInfo.mCountryCode) != 2) {
            locationInfo.mCountryCode = getSimCountryCode();
        } else if (TextUtils.length(locationInfo.mCountryCode) == 2) {
            this.mPlmnCountryCode = locationInfo.mCountryCode;
        }
        Rlog.i(TAG, "setLocationInfo info=" + locationInfo + ", mPlmnCountryCode:" + this.mPlmnCountryCode);
        int destConf = locationInfo.mConfidence;
        if (destConf == 0) {
            destConf = getIntCarrierConfig(MTK_KEY_WFC_GET_CONFIDENCE_LEVEL, 68, locationInfo.mSimIdx);
        }
        float destAccuracy = adjustAccuracyForConfidence(locationInfo.mAccuracy, 68.0f, (float) destConf);
        log("setGeoLocation new accuracy:" + destAccuracy + ", new confidence:" + destConf);
        locationInfo.mAccuracy = destAccuracy;
        locationInfo.mMajorAxisAccuracy = locationInfo.mAccuracy;
        locationInfo.mMinorAxisAccuracy = locationInfo.mAccuracy;
        int i = destConf;
        float f = destAccuracy;
        getMwiRil().setLocationInfo(Integer.toString(locationInfo.mAccountId), Integer.toString(locationInfo.mBroadcastFlag), String.valueOf(locationInfo.mLatitude), String.valueOf(locationInfo.mLongitude), String.valueOf(locationInfo.mAccuracy), locationInfo.mMethod, locationInfo.mCity, locationInfo.mState, locationInfo.mZip, locationInfo.mCountryCode, WifiPdnHandler.getUeWlanMacAddr(), Integer.toString(destConf), String.valueOf(locationInfo.mAltitude), String.valueOf(locationInfo.mMajorAxisAccuracy), String.valueOf(locationInfo.mMinorAxisAccuracy), String.valueOf(locationInfo.mVericalAxisAccuracy), obtainMessage(RESPONSE_SET_LOCATION_INFO));
        pollLocationInfo();
    }

    /* access modifiers changed from: private */
    public LocationInfo getGeoLocationFromLatLong(LocationInfo location, Boolean retry) {
        if (this.mGeoCoder == null) {
            log("getGeoLocationFromLatLong: empty geoCoder, return an empty location");
            return location;
        } else if (!Geocoder.isPresent()) {
            log("getGeoLocationFromLatLong: this system has no GeoCoder implementation!!");
            return location;
        } else {
            List<Address> lstAddress = null;
            try {
                lstAddress = this.mGeoCoder.getFromLocation(location.mLatitude, location.mLongitude, 1);
            } catch (IOException e) {
                log("mGeoCoder.getFromLocation throw IOException:" + e);
            } catch (IllegalArgumentException e2) {
                log("mGeoCoder.getFromLocation throw IllegalArgumentException");
            }
            if (lstAddress == null || lstAddress.isEmpty()) {
                log("getGeoLocationFromLatLong: get empty address");
                if (!getLocationCacheEnable()) {
                    return location;
                }
                getLocationCache(location);
                if ("".equals(location.mCity)) {
                    return null;
                }
                return location;
            }
            location.mCity = lstAddress.get(0).getLocality();
            if (TextUtils.isEmpty(location.mCity)) {
                location.mCity = lstAddress.get(0).getSubAdminArea();
            }
            location.mState = lstAddress.get(0).getAdminArea();
            if (TextUtils.isEmpty(location.mState)) {
                location.mState = lstAddress.get(0).getCountryName();
            }
            location.mZip = lstAddress.get(0).getPostalCode();
            location.mCountryCode = lstAddress.get(0).getCountryCode();
            if (getLocationCacheEnable()) {
                saveLocationCache(location);
            }
            log("getGeoLocationFromLatLong: location=" + location);
            return location;
        }
    }

    private String getSimCountryCode() {
        String simCountryCode = this.mTelephonyManager.getSimCountryIso().toUpperCase(Locale.US);
        log("getSimCountryCode: " + simCountryCode);
        return simCountryCode;
    }

    private void setLocationCacheEnable(boolean enable) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mDeviceContext).edit();
        editor.putBoolean(KEY_LOCATION_CACHE, enable);
        Rlog.i(TAG, "set location cache enable:" + enable);
        if (!editor.commit()) {
            Rlog.e(TAG, "Failed to commit location cache");
        }
    }

    private boolean getLocationCacheEnable() {
        boolean locationCacheEnable = PreferenceManager.getDefaultSharedPreferences(this.mDeviceContext).getBoolean(KEY_LOCATION_CACHE, false);
        Rlog.i(TAG, "get location cache enable status:" + locationCacheEnable);
        return locationCacheEnable;
    }

    private void saveLocationCache(LocationInfo location) {
        Rlog.i(TAG, "saveLocationCache, location=" + location);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mDeviceContext).edit();
        editor.putInt(KEY_LOCATION_CACHE_ACCOUNTID, location.mAccountId);
        editor.putInt(KEY_LOCATION_CACHE_BROADCASTFLAG, location.mBroadcastFlag);
        editor.putFloat(KEY_LOCATION_CACHE_LATITUDE, (float) location.mLatitude);
        editor.putFloat(KEY_LOCATION_CACHE_LONGTITUDE, (float) location.mLongitude);
        editor.putFloat(KEY_LOCATION_CACHE_ACCURACY, location.mAccuracy);
        editor.putString(KEY_LOCATION_CACHE_METHOD, location.mMethod);
        editor.putString(KEY_LOCATION_CACHE_CITY, location.mCity);
        editor.putString(KEY_LOCATION_CACHE_STATE, location.mState);
        editor.putString(KEY_LOCATION_CACHE_ZIP, location.mZip);
        editor.putString(KEY_LOCATION_CACHE_COUNTRYCODE, location.mCountryCode);
        if (!editor.commit()) {
            Rlog.e(TAG, "Failed to commit saveLocationCache");
        }
        getLocationCache(new LocationInfo(0, 0, 0, 0.0d, 0.0d, 0.0f));
    }

    private LocationInfo getLocationCache(LocationInfo location) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mDeviceContext);
        location.mAccountId = sp.getInt(KEY_LOCATION_CACHE_ACCOUNTID, 0);
        if (location.mBroadcastFlag == 0) {
            location.mBroadcastFlag = sp.getInt(KEY_LOCATION_CACHE_BROADCASTFLAG, 0);
        }
        location.mLatitude = (double) sp.getFloat(KEY_LOCATION_CACHE_LATITUDE, 0.0f);
        location.mLongitude = (double) sp.getFloat(KEY_LOCATION_CACHE_LONGTITUDE, 0.0f);
        location.mAccuracy = sp.getFloat(KEY_LOCATION_CACHE_ACCURACY, 0.0f);
        location.mMethod = sp.getString(KEY_LOCATION_CACHE_METHOD, "");
        location.mCity = sp.getString(KEY_LOCATION_CACHE_CITY, "");
        location.mState = sp.getString(KEY_LOCATION_CACHE_STATE, "");
        location.mZip = sp.getString(KEY_LOCATION_CACHE_ZIP, "");
        location.mCountryCode = sp.getString(KEY_LOCATION_CACHE_COUNTRYCODE, "");
        Rlog.i(TAG, "get geolocation from cache, location=" + location);
        return location;
    }

    private boolean getLastKnownLocation(LocationInfo info) {
        log("getLastKnownLocation");
        if (isCtaNotAllow()) {
            Rlog.e(TAG, "getLastKnownLocation: CTA not allow");
            return false;
        }
        LocationManager locationManager = this.mLocationManager;
        if (locationManager == null) {
            log("getLastKnownLocation: empty locationManager, return");
            return false;
        } else if (locationManager.getProvider("gps") == null) {
            log("getLastKnownLocation: GPS_PROVIDER doesn't exist or not ready");
            return false;
        } else {
            Location gpsLocation = this.mLocationManager.getLastKnownLocation("gps");
            if (gpsLocation != null) {
                log("GPS location: " + gpsLocation);
                if (System.currentTimeMillis() - gpsLocation.getTime() < 1800000) {
                    obtainMessage(EVENT_HANDLE_LAST_KNOWN_LOCATION_RESPONSE, 0, 0, gpsLocation).sendToTarget();
                    return true;
                }
            }
            if (this.mLocationManager.getProvider("network") == null) {
                log("getLastKnownLocation: NETWORK_PROVIDER doesn't exist or not ready");
                return false;
            }
            Location networkLocation = this.mLocationManager.getLastKnownLocation("network");
            if (networkLocation != null) {
                log("Network location: " + networkLocation);
                if (System.currentTimeMillis() - networkLocation.getTime() < 1800000) {
                    obtainMessage(EVENT_HANDLE_LAST_KNOWN_LOCATION_RESPONSE, 0, 0, networkLocation).sendToTarget();
                    return true;
                }
            }
            log("getLastKnownLocation: no last known location");
            setLocationInfo(info);
            return false;
        }
    }

    private boolean requestLocationFromNetworkLocation() {
        LocationManager locationManager = this.mLocationManager;
        if (locationManager == null) {
            Rlog.e(TAG, "requestLocationFromNetworkLocation failed: empty locationManager");
            return false;
        } else if (locationManager.getProvider("network") == null) {
            Rlog.e(TAG, "requestLocationFromNetworkLocation failed: NETWORK_PROVIDER not ready");
            return false;
        } else if (isCtaNotAllow()) {
            Rlog.e(TAG, "requestLocationFromNetworkLocation failed: CTA not allow");
            return false;
        } else {
            boolean isProxyAppPermissionGranted = checkLocationProxyAppPermission();
            boolean isEcc = isEccInProgress();
            boolean mustGetLocation = isEcc || isGetLocationAlways();
            if (mustGetLocation) {
                addPackageInLocationSettingsWhitelist();
            }
            if (mustGetLocation || isProxyAppPermissionGranted) {
                if (!this.mLocationRequestRegistered) {
                    String method = "network";
                    if (isEcc) {
                        method = "fused";
                    }
                    LocationRequest request = LocationRequest.createFromDeprecatedProvider(method, (long) NETWORK_LOCATION_UPDATE_TIME, 0.0f, false);
                    request.setHideFromAppOps(true);
                    if (isEcc) {
                        request.setQuality(100);
                    }
                    request.setLocationSettingsIgnored(mustGetLocation);
                    this.mLocationManager.requestLocationUpdates(request, this.mLocationListener, getLooper());
                    this.mLocationRequestRegistered = true;
                    Rlog.i(TAG, "requestLocationFromNetworkLocation: success");
                }
                return true;
            }
            Rlog.w(TAG, "requestLocationFromNetworkLocation failed: is NOT in ECC & non-framework location proxy app is NOT granted");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void cancelNetworkLocationRequest() {
        if (hasMessages(EVENT_RETRY_NETWORK_LOCATION_REQUEST)) {
            removeMessages(EVENT_RETRY_NETWORK_LOCATION_REQUEST);
        }
        LocationManager locationManager = this.mLocationManager;
        if (locationManager == null) {
            this.mLocationRequestRegistered = false;
            Rlog.e(TAG, "cancelNetworkLocationRequest: empty locationManager, return");
            return;
        }
        locationManager.removeUpdates(this.mLocationListener);
        this.mLocationRequestRegistered = false;
        removePackageInLocationSettingsWhitelist();
        Rlog.d(TAG, "cancelNetworkLocationRequest");
    }

    private void addPackageInLocationSettingsWhitelist() {
        String outStr;
        String packageName = this.mContext.getPackageName();
        String whitelist = Settings.Global.getString(this.mContext.getContentResolver(), "location_ignore_settings_package_whitelist");
        if (whitelist == null || whitelist.indexOf(packageName) == -1) {
            if (whitelist == null) {
                outStr = "";
            } else {
                outStr = whitelist + ",";
            }
            String outStr2 = outStr + packageName;
            log("Add WFC in location setting whitelist:" + outStr2);
            Settings.Global.putString(this.mContext.getContentResolver(), "location_ignore_settings_package_whitelist", outStr2);
        }
    }

    private void removePackageInLocationSettingsWhitelist() {
        String packageName = this.mContext.getPackageName();
        String whitelist = Settings.Global.getString(this.mContext.getContentResolver(), "location_ignore_settings_package_whitelist");
        int index = -1;
        String outStr = "";
        if (whitelist != null) {
            index = whitelist.indexOf("," + packageName);
            if (index != -1) {
                outStr = whitelist.replace("," + packageName, "");
            } else {
                index = whitelist.indexOf(packageName);
                if (index != -1) {
                    outStr = whitelist.replace(packageName, "");
                }
            }
        }
        if (index != -1) {
            log("Remove WFC in location setting whitelist:" + outStr);
            Settings.Global.putString(this.mContext.getContentResolver(), "location_ignore_settings_package_whitelist", outStr);
        }
    }

    private void utGeoLocationRequest() {
        dispatchLocationRequest(new LocationInfo(0, 8, 0, 212.0d, 147.0d, 1.0f));
    }

    private void utNetworkLocationRequest() {
        dispatchLocationRequest(new LocationInfo(0, 8, 0, 0.0d, 0.0d, 0.0f));
    }

    private void setLocationEnabled() {
        Message result = obtainMessage(RESPONSE_SET_LOCATION_ENABLED);
        log("setLocationEnabled(): last location setting:" + this.mLastLocationSetting + ", new location setting:" + this.mLocationSetting);
        getMwiRil().setWfcConfig(MwiRIL.WfcConfigType.WFC_SETTING_LOCATION_SETTING.ordinal(), "locenable", this.mLocationSetting ? "1" : "0", result);
        this.mLastLocationSetting = this.mLocationSetting;
    }

    private int getMainCapabilityPhoneId() {
        int phoneId = SystemProperties.getInt(ImsConstants.PROPERTY_CAPABILITY_SWITCH, 1) - 1;
        if (phoneId < 0 || phoneId >= this.mMwiRil.length) {
            Rlog.e(TAG, "getMainCapabilityPhoneId error: " + phoneId);
            phoneId = -1;
        }
        log("getMainCapabilityPhoneId = " + phoneId);
        return phoneId;
    }

    private MwiRIL getMwiRil() {
        int phoneId = getMainCapabilityPhoneId();
        if (phoneId == -1) {
            return this.mMwiRil[0];
        }
        return this.mMwiRil[phoneId];
    }

    private void setCountryCode(String iso) {
        LocationInfo info = new LocationInfo(0, 0, 1, 0.0d, 0.0d, 0.0f);
        info.mCountryCode = iso;
        setLocationInfo(info);
    }

    private void registerForBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.telephony.action.NETWORK_COUNTRY_CHANGED");
        filter.addAction("android.location.MODE_CHANGED");
        filter.addAction("android.location.PROVIDERS_CHANGED");
        filter.addAction(ACTION_LOCATION_CACHE);
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void registerIndication() {
        for (int i = 0; i < this.mSimCount; i++) {
            this.mMwiRil[i].registerRequestGeoLocation(this, 3000, (Object) null);
        }
    }

    /* access modifiers changed from: private */
    public boolean isEccInProgress() {
        boolean isInEcc = false;
        TelecomManager telecomManager = this.mTelecomManager;
        if (telecomManager != null) {
            isInEcc = telecomManager.isInEmergencyCall();
        }
        Rlog.i(TAG, "isEccInProgress: " + isInEcc);
        return isInEcc;
    }

    private boolean isGetLocationAlways() {
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        this.mConfigManager = carrierConfigManager;
        if (carrierConfigManager == null) {
            Rlog.e(TAG, "isGetLocationAlways: Carrier Config service is NOT ready");
            return false;
        }
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        PersistableBundle configs = SubscriptionManager.isValidSubscriptionId(subId) ? this.mConfigManager.getConfigForSubId(subId) : null;
        if (configs == null) {
            Rlog.e(TAG, "isGetLocationAlways: SIM not ready, use default carrier config");
            configs = CarrierConfigManager.getDefaultConfig();
        }
        boolean getLocationAlways = configs.getBoolean(MTK_KEY_WFC_GET_LOCATION_ALWAYS);
        Rlog.i(TAG, "isGetLocationAlways: " + getLocationAlways);
        return getLocationAlways;
    }

    /* access modifiers changed from: private */
    public boolean isCtaNotAllow() {
        boolean isCtaNotAllow = false;
        boolean isCtaSecurity = false;
        boolean isCtaSet = SystemProperties.getInt("ro.vendor.mtk_cta_set", 0) == 1;
        if (SystemProperties.getInt("ro.vendor.mtk_mobile_management", 0) == 1) {
            isCtaSecurity = true;
        }
        boolean isNlpEnabled = this.mLocationManager.isProviderEnabled("network");
        log("isCtaNotAllow: isCtaSet:" + isCtaSet + ", isCtaSecurity:" + isCtaSecurity + ", isNlpEnabled:" + isNlpEnabled);
        if (isCtaSet && isCtaSecurity && !isNlpEnabled) {
            isCtaNotAllow = true;
        }
        Rlog.i(TAG, "isCtaNotAllow: " + isCtaNotAllow);
        return isCtaNotAllow;
    }

    private String loadProxyNameFromCarrierConfig() {
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        this.mConfigManager = carrierConfigManager;
        if (carrierConfigManager == null) {
            log("loadProxyNameFromCarrierConfig: Carrier Config service is NOT ready");
            return "";
        }
        int ddSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        PersistableBundle configs = SubscriptionManager.isValidSubscriptionId(ddSubId) ? this.mConfigManager.getConfigForSubId(ddSubId) : null;
        if (configs == null) {
            log("SIM not ready, use default carrier config");
            configs = CarrierConfigManager.getDefaultConfig();
        }
        String value = (String) configs.get("gps.nfw_proxy_apps");
        log("gps.nfw_proxy_apps: " + value);
        if (value != null) {
            return value.trim().split(" ")[0];
        }
        log("Cannot get location proxy APP package name");
        return "";
    }

    private boolean isPackageInstalled(String packagename) {
        try {
            this.mPackageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Rlog.e(TAG, "non-FWK permission apk not found, treat it as granted" + packagename);
            return false;
        }
    }

    private boolean checkLocationProxyAppPermission() {
        String loadProxyNameFromCarrierConfig = loadProxyNameFromCarrierConfig();
        this.mGnssProxyPackageName = loadProxyNameFromCarrierConfig;
        boolean z = true;
        if (loadProxyNameFromCarrierConfig != null && loadProxyNameFromCarrierConfig.length() == 0) {
            Rlog.i(TAG, "The package name is empty, treat it as granted");
            return true;
        } else if (isPackageInstalled(this.mGnssProxyPackageName)) {
            if (this.mPackageManager.checkPermission(LOCATION_PERMISSION_NAME, this.mGnssProxyPackageName) != 0) {
                z = false;
            }
            boolean proxyAppLocationGranted = z;
            Rlog.i(TAG, "proxyAppLocationGranted: " + proxyAppLocationGranted);
            return proxyAppLocationGranted;
        } else {
            Rlog.i(TAG, "non-FWK permission app not installed, treat it as granted");
            return true;
        }
    }

    /* access modifiers changed from: private */
    public String maskString(String s) {
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
}
