package com.mediatek.ims.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.mediatek.ims.ImsAdapter;
import com.mediatek.ims.ImsEventDispatcher;
import com.mediatek.ims.VaConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WfcDispatcher implements ImsEventDispatcher.VaEventDispatcher {
    private static final String ACTION_LOCATED_PLMN_CHANGED = "com.mediatek.intent.action.LOCATED_PLMN_CHANGED";
    /* access modifiers changed from: private */
    public static final Uri AID_SETTING_URI = Settings.Global.getUriFor(AID_SETTING_URI_STR);
    private static final String AID_SETTING_URI_STR = "wfc_aid_value";
    private static final boolean DEBUG = false;
    private static final int EVENT_MSG_HANDLE_NETWORK_LOCATION_RESPONSE = 2;
    private static final int EVENT_MSG_REQUEST_GEO_LOCATION = 0;
    private static final int EVENT_MSG_REQUEST_NETWORK_LOCATION = 1;
    private static final int EVENT_MSG_RESPONSE_GEO_LOCATION = 4;
    private static final int EVENT_MSG_UPDATE_AID_INFORMATION = 5;
    private static final String EXTRA_ISO = "iso";
    private static final int MSG_REG_IMSA_REQUEST_GEO_LOCATION_INFO = 96009;
    private static final int MSG_REG_IMSA_RESPONSE_GETO_LOCATION_INFO = 91030;
    private static final int NETWORK_LOCATION_UPDATE_TIME = 1000;
    private static final String TAG = "Wfc-IMSA";
    /* access modifiers changed from: private */
    public static final ThreadPoolExecutor sPoolExecutor;
    private static final BlockingQueue<Runnable> sPoolWorkQueue;
    private static final ThreadFactory sThreadFactory;
    /* access modifiers changed from: private */
    public String mAid;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int what = msg.what;
            Object obj = msg.obj;
            WfcDispatcher wfcDispatcher = WfcDispatcher.this;
            wfcDispatcher.log("handleMessage: msg=" + WfcDispatcher.this.handlerEventMsgToString(what));
            switch (what) {
                case 0:
                    final GeoLocationTask locationReq = (GeoLocationTask) obj;
                    WfcDispatcher wfcDispatcher2 = WfcDispatcher.this;
                    wfcDispatcher2.log("push GeoLocation task transaction-" + locationReq.transactionId + " to queue");
                    WfcDispatcher.sPoolExecutor.execute(new Runnable() {
                        public void run() {
                            WfcDispatcher wfcDispatcher = WfcDispatcher.this;
                            wfcDispatcher.log(" start for transaction-" + locationReq.transactionId);
                            WfcDispatcher.this.updateGeoLocationFromLatLong(locationReq);
                            C01624.this.obtainMessage(4, locationReq).sendToTarget();
                        }
                    });
                    return;
                case 1:
                    GeoLocationTask locationReq2 = (GeoLocationTask) obj;
                    WfcDispatcher.this.mNetworkLocationTasks.add(locationReq2);
                    if (!WfcDispatcher.this.getLastKnownLocation(locationReq2)) {
                        WfcDispatcher.this.mNetworkLocationTasks.remove(locationReq2);
                        WfcDispatcher.this.log("getLastKnownLocation failed");
                        obtainMessage(4, locationReq2).sendToTarget();
                        return;
                    }
                    return;
                case 2:
                    Location location = (Location) obj;
                    if (location == null) {
                        WfcDispatcher.this.log("network location get null, unexpected result");
                        return;
                    }
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    WfcDispatcher.this.log("update all GeoLocationTask");
                    for (GeoLocationTask locationTask : WfcDispatcher.this.mNetworkLocationTasks) {
                        locationTask.latitude = latitude;
                        locationTask.longitude = longitude;
                        WfcDispatcher wfcDispatcher3 = WfcDispatcher.this;
                        wfcDispatcher3.log("Get network location, send EVENT_MSG_REQUEST_GEO_LOCATION for transactionId-" + locationTask.transactionId);
                        obtainMessage(0, locationTask).sendToTarget();
                    }
                    WfcDispatcher.this.mNetworkLocationTasks.clear();
                    return;
                case 4:
                    GeoLocationTask locationRsp = (GeoLocationTask) obj;
                    WfcDispatcher wfcDispatcher4 = WfcDispatcher.this;
                    wfcDispatcher4.log("finish for transaction-" + locationRsp.transactionId);
                    WfcDispatcher.this.handleGeoLocationResponse(locationRsp);
                    return;
                case 5:
                    WfcDispatcher.this.handleAidInfoUpdate();
                    return;
                default:
                    Log.w(WfcDispatcher.TAG, "Unhandled message: " + WfcDispatcher.this.handlerEventMsgToString(what));
                    return;
            }
        }
    };
    private boolean mImsEnabled = false;
    private LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            WfcDispatcher wfcDispatcher = WfcDispatcher.this;
            wfcDispatcher.log("onLocationChanged: " + location);
            WfcDispatcher.this.cancelNetworkGeoLocationRequest();
            WfcDispatcher.this.mHandler.obtainMessage(2, location).sendToTarget();
        }

        public void onProviderDisabled(String provider) {
            WfcDispatcher wfcDispatcher = WfcDispatcher.this;
            wfcDispatcher.log("onProviderDisabled: " + provider);
        }

        public void onProviderEnabled(String provider) {
            WfcDispatcher wfcDispatcher = WfcDispatcher.this;
            wfcDispatcher.log("onProviderEnabled: " + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            WfcDispatcher wfcDispatcher = WfcDispatcher.this;
            wfcDispatcher.log("onStatusChanged: " + provider + ", status=" + status);
        }
    };
    private LocationManager mLocationManager;
    /* access modifiers changed from: private */
    public List<GeoLocationTask> mNetworkLocationTasks = new ArrayList();
    /* access modifiers changed from: private */
    public String mPlmnCountryCode = "";
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                WfcDispatcher wfcDispatcher = WfcDispatcher.this;
                wfcDispatcher.log("onReceive action:" + intent.getAction());
                if (intent.getAction().equals(WfcDispatcher.ACTION_LOCATED_PLMN_CHANGED)) {
                    String lowerCaseCountryCode = (String) intent.getExtra(WfcDispatcher.EXTRA_ISO);
                    if (lowerCaseCountryCode != null) {
                        String unused = WfcDispatcher.this.mPlmnCountryCode = lowerCaseCountryCode.toUpperCase();
                        WfcDispatcher wfcDispatcher2 = WfcDispatcher.this;
                        wfcDispatcher2.log("ACTION_LOCATED_PLMN_CHANGED, iso: " + WfcDispatcher.this.mPlmnCountryCode);
                        return;
                    }
                    WfcDispatcher.this.log("iso country code is null");
                }
            }
        }
    };
    private ImsAdapter.VaSocketIO mSocket;

    static {
        LinkedBlockingDeque linkedBlockingDeque = new LinkedBlockingDeque();
        sPoolWorkQueue = linkedBlockingDeque;
        C01591 r8 = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "WFC #" + this.mCount.getAndIncrement());
            }
        };
        sThreadFactory = r8;
        sPoolExecutor = new ThreadPoolExecutor(0, 3, 30, TimeUnit.SECONDS, linkedBlockingDeque, r8);
    }

    private static class GeoLocationTask {
        int accuracy;
        String city;
        String countryCode;
        double latitude;
        double longitude;
        String method;
        int phoneId;
        String state;
        int transactionId;
        String zip;

        private GeoLocationTask() {
            this.method = "";
            this.city = "";
            this.state = "";
            this.zip = "";
            this.countryCode = "";
        }

        public String toString() {
            return "[GeoLocationTask objId: " + hashCode() + ", phoneId: " + this.phoneId + ", transactionId: " + this.transactionId + ", accuracy: " + this.accuracy + ", method: " + this.method + ", city: " + this.city + ", state: " + this.state + ", zip: " + this.zip + ", countryCode: " + this.countryCode;
        }
    }

    private class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: private */
        public void register() {
            WfcDispatcher.this.mContext.getContentResolver().registerContentObserver(WfcDispatcher.AID_SETTING_URI, false, this);
        }

        private void unregister() {
            WfcDispatcher.this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange) {
            onChange(selfChange, (Uri) null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (WfcDispatcher.AID_SETTING_URI.equals(uri)) {
                WfcDispatcher wfcDispatcher = WfcDispatcher.this;
                String unused = wfcDispatcher.mAid = Settings.Global.getString(wfcDispatcher.mContext.getContentResolver(), WfcDispatcher.AID_SETTING_URI_STR);
                WfcDispatcher wfcDispatcher2 = WfcDispatcher.this;
                wfcDispatcher2.log("Receive AID changed from Setting, AID=" + WfcDispatcher.this.mAid);
                WfcDispatcher.this.mHandler.obtainMessage(5).sendToTarget();
            }
        }
    }

    public WfcDispatcher(Context context, ImsAdapter.VaSocketIO IO) {
        log("WfcDispatcher()");
        this.mContext = context;
        this.mSocket = IO;
        this.mLocationManager = (LocationManager) context.getSystemService("location");
        new SettingsObserver((Handler) null).register();
        registerForBroadcast();
        log("WfcDispatcher() end");
    }

    private void registerForBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LOCATED_PLMN_CHANGED);
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    public void enableRequest(int phoneId) {
        log("enableRequest()");
        this.mImsEnabled = true;
        this.mAid = Settings.Global.getString(this.mContext.getContentResolver(), AID_SETTING_URI_STR);
        log("Trigger AID information update to IMCB, AID=" + this.mAid);
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(5), 1000);
    }

    public void disableRequest(int phoneId) {
        log("disableRequest()");
        this.mImsEnabled = false;
        this.mNetworkLocationTasks.clear();
    }

    public void vaEventCallback(ImsAdapter.VaEvent event) {
        try {
            int requestId = event.getRequestID();
            log("vaEventCallback: ID=" + imsaMsgToString(requestId) + "(" + requestId + ")");
            switch (requestId) {
                case VaConstants.MSG_ID_REQUEST_VOWIFI_RELATED_INFO:
                    parseRequestDataPayload(event);
                    return;
                default:
                    log("Unknown request, return directly ");
                    return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Event exception", e);
        }
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
    }

    private void parseRequestDataPayload(ImsAdapter.VaEvent event) {
        int transactionId = event.getByte();
        event.getBytes(3);
        int uaMsgId = event.getInt();
        log("parseRequestDataPayload: transaction-" + transactionId + ", uaMsgId=" + uaMsgIdToString(uaMsgId) + "(" + uaMsgId + ")");
        switch (uaMsgId) {
            case MSG_REG_IMSA_REQUEST_GEO_LOCATION_INFO /*96009*/:
                handleGeoLocationRequest(transactionId, event);
                return;
            default:
                log("parseRequestDataPayload: unknown msgId");
                return;
        }
    }

    private void handleGeoLocationRequest(int transactionId, ImsAdapter.VaEvent event) {
        GeoLocationTask locationReq = new GeoLocationTask();
        locationReq.phoneId = event.getPhoneId();
        locationReq.transactionId = transactionId;
        locationReq.latitude = event.getDouble();
        locationReq.longitude = event.getDouble();
        locationReq.accuracy = event.getInt();
        log("handleGeoLocationRequest: get UA's request: " + locationReq);
        if (locationReq.latitude == 0.0d || locationReq.longitude == 0.0d) {
            log("send EVENT_MSG_REQUEST_NETWORK_LOCATION for transactionId-" + locationReq.transactionId);
            locationReq.method = "Network";
            this.mHandler.obtainMessage(1, locationReq).sendToTarget();
            return;
        }
        log("send EVENT_MSG_REQUEST_GEO_LOCATION for transactionId-" + locationReq.transactionId);
        locationReq.method = "GPS";
        this.mHandler.obtainMessage(0, locationReq).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void handleGeoLocationResponse(GeoLocationTask locationResult) {
        log("send " + imsaMsgToString(VaConstants.MSG_ID_RESPONSE_VOWIFI_RELATED_INFO) + "(" + VaConstants.MSG_ID_RESPONSE_VOWIFI_RELATED_INFO + ") to IMSM: result=" + locationResult);
        ImsAdapter.VaEvent event = new ImsAdapter.VaEvent(locationResult.phoneId, VaConstants.MSG_ID_RESPONSE_VOWIFI_RELATED_INFO);
        event.putByte(locationResult.transactionId);
        event.putBytes(new byte[3]);
        event.putInt(MSG_REG_IMSA_RESPONSE_GETO_LOCATION_INFO);
        event.putDouble(locationResult.latitude);
        event.putDouble(locationResult.longitude);
        event.putInt(locationResult.accuracy);
        String countryCode = "";
        event.putString(locationResult.method == null ? countryCode : locationResult.method, 16);
        event.putString(locationResult.city == null ? countryCode : locationResult.city, 32);
        event.putString(locationResult.state == null ? "Unknown" : locationResult.state, 32);
        event.putString(locationResult.zip == null ? countryCode : locationResult.zip, 8);
        if (locationResult.countryCode != null) {
            countryCode = locationResult.countryCode;
        }
        event.putString(countryCode, 8);
        writeEventToSocket(event);
    }

    /* access modifiers changed from: private */
    public void updateGeoLocationFromLatLong(GeoLocationTask location) {
        if (!Geocoder.isPresent()) {
            log("getGeoLocationFromLatLong: this system has no GeoCoder implementation!!");
            return;
        }
        List<Address> lstAddress = null;
        try {
            lstAddress = new Geocoder(this.mContext, Locale.US).getFromLocation(location.latitude, location.longitude, 1);
        } catch (IOException e) {
            Log.e(TAG, "geocoder.getFromLocation throw exception:", e);
        }
        if (lstAddress == null || lstAddress.isEmpty()) {
            log("getGeoLocationFromLatLong: get empty address, fill plmn:" + this.mPlmnCountryCode);
            location.countryCode = this.mPlmnCountryCode;
            return;
        }
        Address address = lstAddress.get(0);
        location.city = address.getLocality();
        if (location.city == null || location.city.equals("")) {
            location.city = address.getSubAdminArea();
        }
        location.state = address.getAdminArea();
        if (location.state == null || location.state.equals("")) {
            location.state = lstAddress.get(0).getCountryName();
        }
        location.zip = address.getPostalCode();
        location.countryCode = address.getCountryCode();
        if (location.countryCode != null && !location.countryCode.equals("")) {
            this.mPlmnCountryCode = location.countryCode;
        }
        log("getGeoLocationFromLatLong: location=" + location);
    }

    /* access modifiers changed from: private */
    public boolean getLastKnownLocation(GeoLocationTask locationReq) {
        log("getLastKnownLocation");
        LocationManager locationManager = this.mLocationManager;
        if (locationManager == null) {
            log("getLastKnownLocation: empty locationManager, return");
            return false;
        }
        Location gpsLocation = locationManager.getLastKnownLocation("gps");
        if (gpsLocation != null) {
            locationReq.method = "GPS";
            log("GPS location: " + gpsLocation);
            this.mHandler.obtainMessage(2, gpsLocation).sendToTarget();
            return true;
        }
        Location networkLocation = this.mLocationManager.getLastKnownLocation("network");
        if (networkLocation != null) {
            locationReq.method = "Network";
            log("Network location: " + networkLocation);
            this.mHandler.obtainMessage(2, networkLocation).sendToTarget();
            return true;
        }
        log("getLastKnownLocation: no last known location");
        return false;
    }

    private boolean requestGeoLocationFromNetworkLocation() {
        if (this.mLocationManager == null) {
            log("getGeoLocationFromNetworkLocation: empty locationManager, return");
            return false;
        }
        String optr = SystemProperties.get("persist.vendor.operator.optr");
        if (optr == null || !optr.equals("OP08")) {
            if (!this.mLocationManager.isProviderEnabled("network")) {
                log("requestGeoLocationFromNetworkLocation:this system has no networkProvider implementation!");
                return false;
            }
        } else if (this.mLocationManager.getProvider("network") == null) {
            log("requestGeoLocationFromNetworkLocation:getProvider() is null!");
            return false;
        }
        addPackageInLocationSettingsWhitelist();
        LocationRequest request = LocationRequest.createFromDeprecatedProvider("network", 1000, 0.0f, false);
        request.setHideFromAppOps(true);
        request.setLocationSettingsIgnored(true);
        this.mLocationManager.requestLocationUpdates(request, this.mLocationListener, (Looper) null);
        Log.d(TAG, "requestGeoLocationFromNetworkLocation");
        return true;
    }

    /* access modifiers changed from: private */
    public void cancelNetworkGeoLocationRequest() {
        LocationManager locationManager = this.mLocationManager;
        if (locationManager == null) {
            log("cancelNetworkGeoLocationRequest: empty locationManager, return");
            return;
        }
        locationManager.removeUpdates(this.mLocationListener);
        removePackageInLocationSettingsWhitelist();
        Log.d(TAG, "cancelNetworkGeoLocationRequest");
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

    /* access modifiers changed from: private */
    public void handleAidInfoUpdate() {
        if (this.mSocket == null) {
            log("handleAidInfoUpdate: socket is null, can't send AID info.");
            return;
        }
        log("send " + imsaMsgToString(VaConstants.MSG_ID_UPDATE_IMCB_AID_INFO) + "(" + VaConstants.MSG_ID_UPDATE_IMCB_AID_INFO + ") to IMSM: AID=" + this.mAid);
        ImsAdapter.VaEvent event = new ImsAdapter.VaEvent(ImsAdapter.Util.getDefaultVoltePhoneId(), VaConstants.MSG_ID_UPDATE_IMCB_AID_INFO);
        String aid = this.mAid;
        if (aid == null) {
            aid = "";
        }
        event.putString(aid, 32);
        writeEventToSocket(event);
    }

    private void writeEventToSocket(ImsAdapter.VaEvent event) {
        ImsAdapter.VaSocketIO vaSocketIO;
        if (!this.mImsEnabled || (vaSocketIO = this.mSocket) == null || event == null) {
            Log.e(TAG, "Event discarded:" + event);
            return;
        }
        vaSocketIO.writeEvent(event);
    }

    private String imsaMsgToString(int msgId) {
        switch (msgId) {
            case VaConstants.MSG_ID_REQUEST_VOWIFI_RELATED_INFO:
                return "MSG_ID_REQUEST_VOWIFI_RELATED_INFO";
            case VaConstants.MSG_ID_RESPONSE_VOWIFI_RELATED_INFO:
                return "MSG_ID_RESPONSE_VOWIFI_RELATED_INFO";
            case VaConstants.MSG_ID_UPDATE_IMCB_AID_INFO:
                return "MSG_ID_UPDATE_IMCB_AID_INFO";
            default:
                return "Unknown Msg";
        }
    }

    private String uaMsgIdToString(int uaMsgId) {
        switch (uaMsgId) {
            case MSG_REG_IMSA_RESPONSE_GETO_LOCATION_INFO /*91030*/:
                return "MSG_REG_IMSA_RESPONSE_GETO_LOCATION_INFO";
            case MSG_REG_IMSA_REQUEST_GEO_LOCATION_INFO /*96009*/:
                return "MSG_REG_IMSA_REQUEST_GEO_LOCATION_INFO";
            default:
                return "Unknown Msg";
        }
    }

    /* access modifiers changed from: private */
    public String handlerEventMsgToString(int eventMsg) {
        switch (eventMsg) {
            case 0:
                return "EVENT_MSG_REQUEST_GEO_LOCATION";
            case 1:
                return "EVENT_MSG_REQUEST_NETWORK_LOCATION";
            case 2:
                return "EVENT_MSG_HANDLE_NETWORK_LOCATION_RESPONSE";
            case 4:
                return "EVENT_MSG_RESPONSE_GEO_LOCATION";
            case 5:
                return "EVENT_MSG_UPDATE_AID_INFORMATION";
            default:
                return "EVENT_MSG_ID-" + eventMsg;
        }
    }
}
