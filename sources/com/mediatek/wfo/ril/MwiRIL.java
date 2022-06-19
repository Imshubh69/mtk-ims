package com.mediatek.wfo.ril;

import android.content.Context;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.telephony.Rlog;
import android.telephony.TelephonyHistogram;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import com.android.internal.telephony.ClientWakelockTracker;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import vendor.mediatek.hardware.mtkradioex.V3_0.IMtkRadioEx;

public final class MwiRIL extends MwiBaseCommands implements MwiCommandsInterface {
    private static final int DEFAULT_ACK_WAKE_LOCK_TIMEOUT_MS = 200;
    private static final int DEFAULT_WAKE_LOCK_TIMEOUT_MS = 60000;
    static final int EVENT_ACK_WAKE_LOCK_TIMEOUT = 4;
    static final int EVENT_BLOCKING_RESPONSE_TIMEOUT = 5;
    static final int EVENT_RADIO_PROXY_DEAD = 6;
    static final int EVENT_SEND = 1;
    static final int EVENT_WAKE_LOCK_TIMEOUT = 2;
    public static final int FOR_ACK_WAKELOCK = 1;
    public static final int FOR_WAKELOCK = 0;
    static final String[] IMS_HIDL_SERVICE_NAME = {"imsSlot1", "imsSlot2", "imsSlot3", "imsSlot4"};
    public static final int INVALID_WAKELOCK = -1;
    static final int IRADIO_GET_SERVICE_DELAY_MILLIS = 4000;
    static final boolean MWIRIL_LOGD = true;
    static final boolean MWIRIL_LOGV = false;
    static final String MWIRIL_LOG_TAG = "MwiRIL";
    static final boolean MWI_RILA_LOGD = true;
    static final String PROPERTY_WAKE_LOCK_TIMEOUT = "ro.ril.wake_lock_timeout";
    static final String RILJ_ACK_WAKELOCK_NAME = "MWIRIL_ACK_WL";
    static final int RIL_HISTOGRAM_BUCKET_COUNT = 5;
    static SparseArray<TelephonyHistogram> mRilTimeHistograms = new SparseArray<>();
    final PowerManager.WakeLock mAckWakeLock;
    final int mAckWakeLockTimeout;
    volatile int mAckWlSequenceNum;
    private WorkSource mActiveWakelockWorkSource;
    private final ClientWakelockTracker mClientWakelockTracker = new ClientWakelockTracker();
    Context mContext;
    boolean mIsMobileNetworkSupported;
    Object[] mLastNITZTimeInfo;
    /* access modifiers changed from: private */
    public TelephonyMetrics mMetrics;
    final Integer mPhoneId;
    private WorkSource mRILDefaultWorkSource;
    MwiRadioIndication mRadioIndication;
    volatile IMtkRadioEx mRadioProxy;
    final AtomicLong mRadioProxyCookie;
    final RadioProxyDeathRecipient mRadioProxyDeathRecipient;
    MwiRadioResponse mRadioResponse;
    SparseArray<RILRequest> mRequestList;
    final RilHandler mRilHandler;
    AtomicBoolean mTestingEmergencyCall;
    final PowerManager.WakeLock mWakeLock;
    int mWakeLockCount;
    final int mWakeLockTimeout;
    volatile int mWlSequenceNum;

    public enum WfcConfigType {
        WFC_SETTING_WIFI_UEMAC,
        WFC_SETTING_LOCATION_SETTING,
        WFC_SETTING_WIFI_CONN_STATE
    }

    public enum WfcFeatureState {
        WFC_FEATURE_UNSUPPORTED,
        WFC_FEATURE_SUPPORTED,
        WFC_FEATURE_UNKNOWN
    }

    public static List<TelephonyHistogram> getTelephonyRILTimingHistograms() {
        List<TelephonyHistogram> list;
        synchronized (mRilTimeHistograms) {
            list = new ArrayList<>(mRilTimeHistograms.size());
            for (int i = 0; i < mRilTimeHistograms.size(); i++) {
                list.add(new TelephonyHistogram(mRilTimeHistograms.valueAt(i)));
            }
        }
        return list;
    }

    class RilHandler extends Handler {
        public RilHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    synchronized (MwiRIL.this.mRequestList) {
                        if (msg.arg1 == MwiRIL.this.mWlSequenceNum && MwiRIL.this.clearWakeLock(0)) {
                            int count = MwiRIL.this.mRequestList.size();
                            Rlog.d(MwiRIL.MWIRIL_LOG_TAG, "WAKE_LOCK_TIMEOUT  mRequestList=" + count);
                        }
                    }
                    return;
                case 4:
                    if (msg.arg1 == MwiRIL.this.mAckWlSequenceNum) {
                        boolean unused = MwiRIL.this.clearWakeLock(1);
                        return;
                    }
                    return;
                case 5:
                    RILRequest rr = MwiRIL.this.findAndRemoveRequestFromList(msg.arg1);
                    if (rr != null) {
                        if (rr.mResult != null) {
                            AsyncResult.forMessage(rr.mResult, MwiRIL.getResponseForTimedOutRILRequest(rr), (Throwable) null);
                            rr.mResult.sendToTarget();
                            MwiRIL.this.mMetrics.writeOnRilTimeoutResponse(MwiRIL.this.mPhoneId.intValue(), rr.mSerial, rr.mRequest);
                        }
                        MwiRIL.this.decrementWakeLock(rr);
                        rr.release();
                        return;
                    }
                    return;
                case 6:
                    MwiRIL mwiRIL = MwiRIL.this;
                    mwiRIL.riljLog("handleMessage: EVENT_RADIO_PROXY_DEAD cookie = " + msg.obj + " mRadioProxyCookie = " + MwiRIL.this.mRadioProxyCookie.get());
                    if (((Long) msg.obj).longValue() == MwiRIL.this.mRadioProxyCookie.get()) {
                        MwiRIL.this.resetProxyAndRequestList();
                        IMtkRadioEx unused2 = MwiRIL.this.getRadioProxy((Message) null);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public static Object getResponseForTimedOutRILRequest(RILRequest rr) {
        return null;
    }

    final class RadioProxyDeathRecipient implements IHwBinder.DeathRecipient {
        RadioProxyDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            MwiRIL mwiRIL = MwiRIL.this;
            mwiRIL.riljLog("serviceDied: " + cookie);
            MwiRIL.this.mRilHandler.sendMessageDelayed(MwiRIL.this.mRilHandler.obtainMessage(6, Long.valueOf(cookie)), 4000);
        }
    }

    /* access modifiers changed from: private */
    public void resetProxyAndRequestList() {
        this.mRadioProxy = null;
        this.mRadioProxyCookie.incrementAndGet();
        RILRequest.resetSerial();
        clearRequestList(1, false);
    }

    /* access modifiers changed from: private */
    public IMtkRadioEx getRadioProxy(Message result) {
        if (!this.mIsMobileNetworkSupported) {
            return null;
        }
        if (this.mRadioProxy != null) {
            return this.mRadioProxy;
        }
        try {
            String[] strArr = IMS_HIDL_SERVICE_NAME;
            Integer num = this.mPhoneId;
            this.mRadioProxy = IMtkRadioEx.getService(strArr[num == null ? 0 : num.intValue()]);
            riljLoge("mRadioProxy getService() done");
            if (this.mRadioProxy != null) {
                riljLoge("mRadioProxy getService() done 2");
                this.mRadioProxy.linkToDeath(this.mRadioProxyDeathRecipient, this.mRadioProxyCookie.incrementAndGet());
                riljLoge("mRadioProxy linkToDeath() done");
                this.mRadioProxy.setResponseFunctionsMwi(this.mRadioResponse, this.mRadioIndication);
                riljLoge("mRadioProxy setResponseFunctionsMwi() done");
            } else {
                riljLoge("getRadioProxy: mRadioProxy == null");
            }
        } catch (RemoteException | RuntimeException e) {
            this.mRadioProxy = null;
            riljLoge("RadioProxy getService/setResponseFunctions: " + e);
        }
        if (this.mRadioProxy == null) {
            if (result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            RilHandler rilHandler = this.mRilHandler;
            rilHandler.sendMessageDelayed(rilHandler.obtainMessage(6, Long.valueOf(this.mRadioProxyCookie.incrementAndGet())), 4000);
        }
        return this.mRadioProxy;
    }

    public MwiRIL(Context context, int instanceId, Looper looper) {
        super(context, instanceId);
        boolean z = false;
        this.mWlSequenceNum = 0;
        this.mAckWlSequenceNum = 0;
        this.mRequestList = new SparseArray<>();
        this.mTestingEmergencyCall = new AtomicBoolean(false);
        this.mMetrics = TelephonyMetrics.getInstance();
        this.mRadioProxy = null;
        this.mRadioProxyCookie = new AtomicLong(0);
        this.mContext = context;
        this.mPhoneId = Integer.valueOf(instanceId);
        this.mIsMobileNetworkSupported = ((TelephonyManager) context.getSystemService(TelephonyManager.class)).isDataCapable();
        riljLog("MwiRIL: isDataCapable() = " + this.mIsMobileNetworkSupported);
        this.mRadioResponse = new MwiRadioResponse(this, instanceId);
        this.mRadioIndication = new MwiRadioIndication(this, instanceId);
        this.mRilHandler = new RilHandler(looper);
        this.mRadioProxyDeathRecipient = new RadioProxyDeathRecipient();
        PowerManager pm = (PowerManager) context.getSystemService("power");
        PowerManager.WakeLock newWakeLock = pm.newWakeLock(1, MWIRIL_LOG_TAG);
        this.mWakeLock = newWakeLock;
        newWakeLock.setReferenceCounted(false);
        PowerManager.WakeLock newWakeLock2 = pm.newWakeLock(1, RILJ_ACK_WAKELOCK_NAME);
        this.mAckWakeLock = newWakeLock2;
        newWakeLock2.setReferenceCounted(false);
        this.mWakeLockTimeout = SystemProperties.getInt(PROPERTY_WAKE_LOCK_TIMEOUT, DEFAULT_WAKE_LOCK_TIMEOUT_MS);
        this.mAckWakeLockTimeout = SystemProperties.getInt(PROPERTY_WAKE_LOCK_TIMEOUT, 200);
        this.mWakeLockCount = 0;
        this.mRILDefaultWorkSource = new WorkSource(context.getApplicationInfo().uid, context.getPackageName());
        IMtkRadioEx proxy = getRadioProxy((Message) null);
        StringBuilder sb = new StringBuilder();
        sb.append("MwiRIL: proxy = ");
        sb.append(proxy == null ? true : z);
        riljLog(sb.toString());
    }

    private void addRequest(RILRequest rr) {
        acquireWakeLock(rr, 0);
        synchronized (this.mRequestList) {
            rr.mStartTimeMs = SystemClock.elapsedRealtime();
            this.mRequestList.append(rr.mSerial, rr);
        }
    }

    private RILRequest obtainRequest(int request, Message result, WorkSource workSource) {
        RILRequest rr = RILRequest.obtain(request, result, workSource);
        addRequest(rr);
        return rr;
    }

    private void handleRadioProxyExceptionForRR(RILRequest rr, String caller, Exception e) {
        riljLoge(caller + ": " + e);
        resetProxyAndRequestList();
        RilHandler rilHandler = this.mRilHandler;
        rilHandler.sendMessageDelayed(rilHandler.obtainMessage(6, Long.valueOf(this.mRadioProxyCookie.incrementAndGet())), 4000);
    }

    public void handleRadioProxyForRadioAvailable() {
        if (this.mRilHandler.hasMessages(6)) {
            riljLog("handleRadioProxyForRadioAvailable mRadioProxyCookie = " + this.mRadioProxyCookie.get());
            resetProxyAndRequestList();
            RilHandler rilHandler = this.mRilHandler;
            rilHandler.sendMessageDelayed(rilHandler.obtainMessage(6, Long.valueOf(this.mRadioProxyCookie.incrementAndGet())), 4000);
        }
    }

    /* access modifiers changed from: protected */
    public String convertNullToEmptyString(String string) {
        return string != null ? string : "";
    }

    public void setWifiEnabled(String ifName, int isWifiEnabled, int isFlightModeOn, Message response) {
        IMtkRadioEx radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2116, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " ifName:" + ifName + " isWifiEnabled:" + isWifiEnabled + " isFlightModeOn:" + isFlightModeOn);
            try {
                radioProxy.setWifiEnabled(rr.mSerial, ifName, isWifiEnabled, isFlightModeOn);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setWifiEnabled", e);
            }
        }
    }

    public void setWifiAssociated(String ifName, boolean associated, String ssid, String apMac, int mtuSize, String ueMac, int wifiConnState, Message response) {
        IMtkRadioEx radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2117, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " ifName:" + ifName + " associated:" + associated + ", mtu: " + mtuSize + " wifiConnState:" + wifiConnState);
            try {
                ArrayList<String> list = new ArrayList<>();
                list.add(convertNullToEmptyString(ifName));
                list.add(convertNullToEmptyString(String.valueOf(associated ? 1 : 0)));
                list.add(convertNullToEmptyString(ssid));
                list.add(convertNullToEmptyString(apMac));
                list.add(convertNullToEmptyString(String.valueOf(mtuSize)));
                list.add(convertNullToEmptyString(ueMac));
                list.add(convertNullToEmptyString(String.valueOf(wifiConnState)));
                radioProxy.setWifiAssociated(rr.mSerial, list);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setWifiAssociatedWithMtu", e);
            }
        }
    }

    public void setWifiSignalLevel(int rssi, int snr, Message response) {
        IMtkRadioEx radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2118, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " rssi: " + rssi + " snr:" + snr);
            try {
                radioProxy.setWifiSignalLevel(rr.mSerial, rssi, snr);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setWifiSignalLevel", e);
            }
        }
    }

    public void setWifiIpAddress(String ifName, String ipv4Addr, String ipv6Addr, int ipv4PrefixLen, int ipv6PrefixLen, String ipv4Gateway, String ipv6Gateway, int dnsCount, String dnsAddresses, Message response) {
        IMtkRadioEx radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2119, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " ifName:" + ifName + " ipv4PrefixLen: " + ipv4PrefixLen + " ipv6PrefixLen: " + ipv6PrefixLen + " ipv4Gateway: " + ipv4Gateway + " ipv6Gateway: " + ipv6Gateway + " dnsCount: " + dnsCount + " dnsAddresses: " + dnsAddresses);
            try {
                ArrayList<String> list = new ArrayList<>();
                list.add(convertNullToEmptyString(ifName));
                list.add(convertNullToEmptyString(ipv4Addr));
                list.add(convertNullToEmptyString(ipv6Addr));
                list.add(convertNullToEmptyString(String.valueOf(ipv4PrefixLen)));
                list.add(convertNullToEmptyString(String.valueOf(ipv6PrefixLen)));
                list.add(convertNullToEmptyString(ipv4Gateway));
                list.add(convertNullToEmptyString(ipv6Gateway));
                list.add(convertNullToEmptyString(String.valueOf(dnsCount)));
                list.add(convertNullToEmptyString(dnsAddresses));
                radioProxy.setWifiIpAddress(rr.mSerial, list);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setWifiIpAddressWithDns", e);
            }
        }
    }

    public void setWfcConfig(int setting, String ifName, String value, Message response) {
        IMtkRadioEx radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2187, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " setting:" + setting + " ifName:" + ifName + " value:" + value);
            try {
                radioProxy.setWfcConfig(rr.mSerial, setting, ifName, value);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setWfcConfig", e);
            }
        }
    }

    public void getWfcConfig(int setting, Message response) {
        IMtkRadioEx radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2188, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " setting:" + setting);
            try {
                radioProxy.getWfcConfig(rr.mSerial, setting);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getWfcConfig", e);
            }
        }
    }

    public void setWfcConfig_WifiUeMac(String ifName, String value, Message response) {
        setWfcConfig(WfcConfigType.WFC_SETTING_WIFI_UEMAC.ordinal(), ifName, value, response);
    }

    public void setLocationInfo(String accountId, String broadcastFlag, String latitude, String longitude, String accuracy, String method, String city, String state, String zip, String countryCode, String ueWlanMac, String confidence, String altitude, String majorAxisAccuracy, String minorAxisAccuracy, String vericalAxisAccuracy, Message response) {
        String str = broadcastFlag;
        String str2 = accuracy;
        String str3 = method;
        Message message = response;
        IMtkRadioEx radioProxy = getRadioProxy(message);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2120, message, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " accountId:" + accountId + " broadcastFlag:" + str + " accuracy:" + str2 + " method:" + str3);
            ArrayList arrayList = new ArrayList();
            arrayList.add(convertNullToEmptyString(accountId));
            arrayList.add(convertNullToEmptyString(str));
            arrayList.add(convertNullToEmptyString(latitude));
            arrayList.add(convertNullToEmptyString(longitude));
            arrayList.add(convertNullToEmptyString(str2));
            arrayList.add(convertNullToEmptyString(str3));
            arrayList.add(convertNullToEmptyString(city));
            arrayList.add(convertNullToEmptyString(state));
            arrayList.add(convertNullToEmptyString(zip));
            arrayList.add(convertNullToEmptyString(countryCode));
            arrayList.add(convertNullToEmptyString(ueWlanMac));
            arrayList.add(convertNullToEmptyString(confidence));
            arrayList.add(convertNullToEmptyString(altitude));
            arrayList.add(convertNullToEmptyString(majorAxisAccuracy));
            arrayList.add(convertNullToEmptyString(minorAxisAccuracy));
            arrayList.add(convertNullToEmptyString(vericalAxisAccuracy));
            try {
                radioProxy.setLocationInfo(rr.mSerial, arrayList);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setLocationInfo", e);
            }
        } else {
            String str4 = accountId;
            String str5 = latitude;
            String str6 = longitude;
            String str7 = city;
            String str8 = state;
            String str9 = zip;
            String str10 = countryCode;
        }
    }

    public void setEmergencyAddressId(String aid, Message response) {
        IMtkRadioEx radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2121, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " aid:" + aid);
            try {
                radioProxy.setEmergencyAddressId(rr.mSerial, aid);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setEmergencyAddressId", e);
            }
        }
    }

    public void setNattKeepAliveStatus(String ifName, boolean enable, String srcIp, int srcPort, String dstIp, int dstPort, Message response) {
        Message message = response;
        IMtkRadioEx radioProxy = getRadioProxy(message);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2131, message, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " ifName:" + ifName + " enable:" + enable);
            try {
                radioProxy.setNattKeepAliveStatus(rr.mSerial, ifName, enable, srcIp, srcPort, dstIp, dstPort);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setNattKeepAliveStatus", e);
            }
        } else {
            String str = ifName;
            boolean z = enable;
        }
    }

    public void setWifiPingResult(int rat, int latency, int pktloss, Message response) {
        IMtkRadioEx radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2132, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " rat = " + rat + " latency = " + latency + " pktloss = " + pktloss);
            try {
                radioProxy.setWifiPingResult(rr.mSerial, rat, latency, pktloss);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setWifiPingResult", e);
            }
        }
    }

    public void notifyEPDGScreenState(int state, Message response) {
        IMtkRadioEx radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2179, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.notifyEPDGScreenState(rr.mSerial, state);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "notifyEPDGScreenState", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void processIndication(int indicationType) {
        if (indicationType == 1) {
            sendAck();
            riljLog("Unsol response received; Sending ack to ril.cpp");
        }
    }

    /* access modifiers changed from: package-private */
    public void processRequestAck(int serial) {
        RILRequest rr;
        synchronized (this.mRequestList) {
            rr = this.mRequestList.get(serial);
        }
        if (rr == null) {
            Rlog.w(MWIRIL_LOG_TAG, "processRequestAck: Unexpected solicited ack response! serial: " + serial);
            return;
        }
        decrementWakeLock(rr);
        riljLog(rr.serialString() + " Ack < " + requestToString(rr.mRequest));
    }

    /* access modifiers changed from: package-private */
    public RILRequest processResponse(RadioResponseInfo responseInfo) {
        RILRequest rr;
        int serial = responseInfo.serial;
        int error = responseInfo.error;
        int type = responseInfo.type;
        if (type == 1) {
            synchronized (this.mRequestList) {
                rr = this.mRequestList.get(serial);
            }
            if (rr == null) {
                Rlog.w(MWIRIL_LOG_TAG, "Unexpected solicited ack response! sn: " + serial);
            } else {
                decrementWakeLock(rr);
                riljLog(rr.serialString() + " Ack < " + requestToString(rr.mRequest));
            }
            return rr;
        }
        RILRequest rr2 = findAndRemoveRequestFromList(serial);
        if (rr2 == null) {
            Rlog.e(MWIRIL_LOG_TAG, "processResponse: Unexpected response! serial: " + serial + " error: " + error);
            return null;
        }
        addToRilHistogram(rr2);
        if (type == 2) {
            sendAck();
            riljLog("Response received for " + rr2.serialString() + " " + requestToString(rr2.mRequest) + " Sending ack to ril.cpp");
        }
        int i = rr2.mRequest;
        return rr2;
    }

    /* access modifiers changed from: package-private */
    public void processResponseDone(RILRequest rr, RadioResponseInfo responseInfo, Object ret) {
        if (responseInfo.error == 0) {
            riljLog(rr.serialString() + "< " + requestToString(rr.mRequest) + " " + retToString(rr.mRequest, ret));
        } else {
            riljLog(rr.serialString() + "< " + requestToString(rr.mRequest) + " error " + responseInfo.error);
            rr.onError(responseInfo.error, ret);
        }
        this.mMetrics.writeOnRilSolicitedResponse(this.mPhoneId.intValue(), rr.mSerial, responseInfo.error, rr.mRequest, ret);
        if (rr != null) {
            if (responseInfo.type == 0) {
                decrementWakeLock(rr);
            }
            rr.release();
        }
    }

    private void sendAck() {
        RILRequest rr = RILRequest.obtain(800, (Message) null, this.mRILDefaultWorkSource);
        acquireWakeLock(rr, 1);
        IMtkRadioEx radioProxy = getRadioProxy((Message) null);
        if (radioProxy != null) {
            try {
                radioProxy.responseAcknowledgementMtk();
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendAck", e);
                riljLoge("sendAck: " + e);
            }
        } else {
            Rlog.e(MWIRIL_LOG_TAG, "Error trying to send ack, radioProxy = null");
        }
        rr.release();
    }

    private WorkSource getDeafultWorkSourceIfInvalid(WorkSource workSource) {
        if (workSource == null) {
            return this.mRILDefaultWorkSource;
        }
        return workSource;
    }

    private String getWorkSourceClientId(WorkSource workSource) {
        if (workSource == null) {
            return null;
        }
        return String.valueOf(workSource.get(0)) + ":" + workSource.getName(0);
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        r8.mWakeLockType = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00ad, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void acquireWakeLock(com.mediatek.wfo.ril.RILRequest r8, int r9) {
        /*
            r7 = this;
            monitor-enter(r8)
            int r0 = r8.mWakeLockType     // Catch:{ all -> 0x00c7 }
            r1 = -1
            if (r0 == r1) goto L_0x0022
            java.lang.String r0 = "MwiRIL"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x00c7 }
            r1.<init>()     // Catch:{ all -> 0x00c7 }
            java.lang.String r2 = "Failed to aquire wakelock for "
            r1.append(r2)     // Catch:{ all -> 0x00c7 }
            java.lang.String r2 = r8.serialString()     // Catch:{ all -> 0x00c7 }
            r1.append(r2)     // Catch:{ all -> 0x00c7 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00c7 }
            android.telephony.Rlog.d(r0, r1)     // Catch:{ all -> 0x00c7 }
            monitor-exit(r8)     // Catch:{ all -> 0x00c7 }
            return
        L_0x0022:
            switch(r9) {
                case 0: goto L_0x0050;
                case 1: goto L_0x0029;
                default: goto L_0x0025;
            }     // Catch:{ all -> 0x00c7 }
        L_0x0025:
            java.lang.String r0 = "MwiRIL"
            goto L_0x00b1
        L_0x0029:
            android.os.PowerManager$WakeLock r0 = r7.mAckWakeLock     // Catch:{ all -> 0x00c7 }
            monitor-enter(r0)     // Catch:{ all -> 0x00c7 }
            android.os.PowerManager$WakeLock r1 = r7.mAckWakeLock     // Catch:{ all -> 0x004d }
            r1.acquire()     // Catch:{ all -> 0x004d }
            int r1 = r7.mAckWlSequenceNum     // Catch:{ all -> 0x004d }
            int r1 = r1 + 1
            r7.mAckWlSequenceNum = r1     // Catch:{ all -> 0x004d }
            com.mediatek.wfo.ril.MwiRIL$RilHandler r1 = r7.mRilHandler     // Catch:{ all -> 0x004d }
            r2 = 4
            android.os.Message r1 = r1.obtainMessage(r2)     // Catch:{ all -> 0x004d }
            int r2 = r7.mAckWlSequenceNum     // Catch:{ all -> 0x004d }
            r1.arg1 = r2     // Catch:{ all -> 0x004d }
            com.mediatek.wfo.ril.MwiRIL$RilHandler r2 = r7.mRilHandler     // Catch:{ all -> 0x004d }
            int r3 = r7.mAckWakeLockTimeout     // Catch:{ all -> 0x004d }
            long r3 = (long) r3     // Catch:{ all -> 0x004d }
            r2.sendMessageDelayed(r1, r3)     // Catch:{ all -> 0x004d }
            monitor-exit(r0)     // Catch:{ all -> 0x004d }
            goto L_0x00aa
        L_0x004d:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x004d }
            throw r1     // Catch:{ all -> 0x00c7 }
        L_0x0050:
            android.os.PowerManager$WakeLock r0 = r7.mWakeLock     // Catch:{ all -> 0x00c7 }
            monitor-enter(r0)     // Catch:{ all -> 0x00c7 }
            android.os.PowerManager$WakeLock r1 = r7.mWakeLock     // Catch:{ all -> 0x00ae }
            r1.acquire()     // Catch:{ all -> 0x00ae }
            int r1 = r7.mWakeLockCount     // Catch:{ all -> 0x00ae }
            int r1 = r1 + 1
            r7.mWakeLockCount = r1     // Catch:{ all -> 0x00ae }
            int r1 = r7.mWlSequenceNum     // Catch:{ all -> 0x00ae }
            int r1 = r1 + 1
            r7.mWlSequenceNum = r1     // Catch:{ all -> 0x00ae }
            android.os.WorkSource r1 = r8.mWorkSource     // Catch:{ all -> 0x00ae }
            java.lang.String r1 = r7.getWorkSourceClientId(r1)     // Catch:{ all -> 0x00ae }
            com.android.internal.telephony.ClientWakelockTracker r2 = r7.mClientWakelockTracker     // Catch:{ all -> 0x00ae }
            boolean r2 = r2.isClientActive(r1)     // Catch:{ all -> 0x00ae }
            if (r2 != 0) goto L_0x0087
            android.os.WorkSource r2 = r7.mActiveWakelockWorkSource     // Catch:{ all -> 0x00ae }
            if (r2 == 0) goto L_0x007c
            android.os.WorkSource r3 = r8.mWorkSource     // Catch:{ all -> 0x00ae }
            r2.add(r3)     // Catch:{ all -> 0x00ae }
            goto L_0x0080
        L_0x007c:
            android.os.WorkSource r2 = r8.mWorkSource     // Catch:{ all -> 0x00ae }
            r7.mActiveWakelockWorkSource = r2     // Catch:{ all -> 0x00ae }
        L_0x0080:
            android.os.PowerManager$WakeLock r2 = r7.mWakeLock     // Catch:{ all -> 0x00ae }
            android.os.WorkSource r3 = r7.mActiveWakelockWorkSource     // Catch:{ all -> 0x00ae }
            r2.setWorkSource(r3)     // Catch:{ all -> 0x00ae }
        L_0x0087:
            com.android.internal.telephony.ClientWakelockTracker r2 = r7.mClientWakelockTracker     // Catch:{ all -> 0x00ae }
            java.lang.String r3 = r8.mClientId     // Catch:{ all -> 0x00ae }
            int r4 = r8.mRequest     // Catch:{ all -> 0x00ae }
            int r5 = r8.mSerial     // Catch:{ all -> 0x00ae }
            int r6 = r7.mWakeLockCount     // Catch:{ all -> 0x00ae }
            r2.startTracking(r3, r4, r5, r6)     // Catch:{ all -> 0x00ae }
            com.mediatek.wfo.ril.MwiRIL$RilHandler r2 = r7.mRilHandler     // Catch:{ all -> 0x00ae }
            r3 = 2
            android.os.Message r2 = r2.obtainMessage(r3)     // Catch:{ all -> 0x00ae }
            int r3 = r7.mWlSequenceNum     // Catch:{ all -> 0x00ae }
            r2.arg1 = r3     // Catch:{ all -> 0x00ae }
            com.mediatek.wfo.ril.MwiRIL$RilHandler r3 = r7.mRilHandler     // Catch:{ all -> 0x00ae }
            int r4 = r7.mWakeLockTimeout     // Catch:{ all -> 0x00ae }
            long r4 = (long) r4     // Catch:{ all -> 0x00ae }
            r3.sendMessageDelayed(r2, r4)     // Catch:{ all -> 0x00ae }
            monitor-exit(r0)     // Catch:{ all -> 0x00ae }
        L_0x00aa:
            r8.mWakeLockType = r9     // Catch:{ all -> 0x00c7 }
            monitor-exit(r8)     // Catch:{ all -> 0x00c7 }
            return
        L_0x00ae:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x00ae }
            throw r1     // Catch:{ all -> 0x00c7 }
        L_0x00b1:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x00c7 }
            r1.<init>()     // Catch:{ all -> 0x00c7 }
            java.lang.String r2 = "Acquiring Invalid Wakelock type "
            r1.append(r2)     // Catch:{ all -> 0x00c7 }
            r1.append(r9)     // Catch:{ all -> 0x00c7 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00c7 }
            android.telephony.Rlog.w(r0, r1)     // Catch:{ all -> 0x00c7 }
            monitor-exit(r8)     // Catch:{ all -> 0x00c7 }
            return
        L_0x00c7:
            r0 = move-exception
            monitor-exit(r8)     // Catch:{ all -> 0x00c7 }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.wfo.ril.MwiRIL.acquireWakeLock(com.mediatek.wfo.ril.RILRequest, int):void");
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    /* access modifiers changed from: private */
    public void decrementWakeLock(RILRequest rr) {
        WorkSource workSource;
        synchronized (rr) {
            switch (rr.mWakeLockType) {
                case -1:
                    break;
                case 0:
                    synchronized (this.mWakeLock) {
                        ClientWakelockTracker clientWakelockTracker = this.mClientWakelockTracker;
                        String str = rr.mClientId;
                        int i = rr.mRequest;
                        int i2 = rr.mSerial;
                        int i3 = this.mWakeLockCount;
                        clientWakelockTracker.stopTracking(str, i, i2, i3 > 1 ? i3 - 1 : 0);
                        if (!this.mClientWakelockTracker.isClientActive(getWorkSourceClientId(rr.mWorkSource)) && (workSource = this.mActiveWakelockWorkSource) != null) {
                            workSource.remove(rr.mWorkSource);
                            if (this.mActiveWakelockWorkSource.size() == 0) {
                                this.mActiveWakelockWorkSource = null;
                            }
                            this.mWakeLock.setWorkSource(this.mActiveWakelockWorkSource);
                        }
                        int i4 = this.mWakeLockCount;
                        if (i4 > 1) {
                            this.mWakeLockCount = i4 - 1;
                        } else {
                            this.mWakeLockCount = 0;
                            this.mWakeLock.release();
                        }
                    }
                    break;
                case 1:
                    break;
                default:
                    Rlog.w(MWIRIL_LOG_TAG, "Decrementing Invalid Wakelock type " + rr.mWakeLockType);
                    break;
            }
            rr.mWakeLockType = -1;
        }
    }

    /* access modifiers changed from: private */
    public boolean clearWakeLock(int wakeLockType) {
        if (wakeLockType == 0) {
            synchronized (this.mWakeLock) {
                if (this.mWakeLockCount == 0 && !this.mWakeLock.isHeld()) {
                    return false;
                }
                Rlog.d(MWIRIL_LOG_TAG, "NOTE: mWakeLockCount is " + this.mWakeLockCount + "at time of clearing");
                this.mWakeLockCount = 0;
                this.mWakeLock.release();
                this.mClientWakelockTracker.stopTrackingAll();
                this.mActiveWakelockWorkSource = null;
                return true;
            }
        }
        synchronized (this.mAckWakeLock) {
            if (!this.mAckWakeLock.isHeld()) {
                return false;
            }
            this.mAckWakeLock.release();
            return true;
        }
    }

    private void clearRequestList(int error, boolean loggable) {
        synchronized (this.mRequestList) {
            int count = this.mRequestList.size();
            if (loggable) {
                Rlog.d(MWIRIL_LOG_TAG, "clearRequestList  mWakeLockCount=" + this.mWakeLockCount + " mRequestList=" + count);
            }
            for (int i = 0; i < count; i++) {
                RILRequest rr = this.mRequestList.valueAt(i);
                if (loggable) {
                    Rlog.d(MWIRIL_LOG_TAG, i + ": [" + rr.mSerial + "] " + requestToString(rr.mRequest));
                }
                rr.onError(error, (Object) null);
                decrementWakeLock(rr);
                rr.release();
            }
            this.mRequestList.clear();
        }
    }

    /* access modifiers changed from: private */
    public RILRequest findAndRemoveRequestFromList(int serial) {
        RILRequest rr;
        synchronized (this.mRequestList) {
            rr = this.mRequestList.get(serial);
            if (rr != null) {
                this.mRequestList.remove(serial);
            }
        }
        return rr;
    }

    private void addToRilHistogram(RILRequest rr) {
        int totalTime = (int) (SystemClock.elapsedRealtime() - rr.mStartTimeMs);
        synchronized (mRilTimeHistograms) {
            TelephonyHistogram entry = mRilTimeHistograms.get(rr.mRequest);
            if (entry == null) {
                entry = new TelephonyHistogram(1, rr.mRequest, 5);
                mRilTimeHistograms.put(rr.mRequest, entry);
            }
            entry.addTimeTaken(totalTime);
        }
    }

    static String responseToString(int request) {
        switch (request) {
            case MwiRILConstants.RIL_UNSOL_MOBILE_WIFI_ROVEOUT:
                return "MwiRILConstants.RIL_UNSOL_MOBILE_WIFI_ROVEOUT";
            case MwiRILConstants.RIL_UNSOL_MOBILE_WIFI_HANDOVER:
                return "MwiRILConstants.RIL_UNSOL_MOBILE_WIFI_HANDOVER";
            case MwiRILConstants.RIL_UNSOL_ACTIVE_WIFI_PDN_COUNT:
                return "MwiRILConstants.RIL_UNSOL_ACTIVE_WIFI_PDN_COUNT";
            case MwiRILConstants.RIL_UNSOL_WIFI_RSSI_MONITORING_CONFIG:
                return "MwiRILConstants.RIL_UNSOL_WIFI_RSSI_MONITORING_CONFIG";
            case MwiRILConstants.RIL_UNSOL_WIFI_PDN_ERROR:
                return "MwiRILConstants.RIL_UNSOL_WIFI_PDN_ERROR";
            case MwiRILConstants.RIL_UNSOL_REQUEST_GEO_LOCATION:
                return "MwiRILConstants.RIL_UNSOL_REQUEST_GEO_LOCATION";
            case MwiRILConstants.RIL_UNSOL_WFC_PDN_STATE:
                return "MwiRILConstants.RIL_UNSOL_WFC_PDN_STATE";
            case MwiRILConstants.RIL_UNSOL_NATT_KEEP_ALIVE_CHANGED:
                return "MwiRILConstants.RIL_UNSOL_NATT_KEEP_ALIVE_CHANGED";
            case MwiRILConstants.RIL_UNSOL_WIFI_PING_REQUEST:
                return "MwiRILConstants.RIL_UNSOL_WIFI_PING_REQUEST";
            case MwiRILConstants.RIL_UNSOL_WIFI_PDN_OOS:
                return "MwiRILConstants.RIL_UNSOL_WIFI_PDN_OOS";
            case 3127:
                return "MwiRILConstants.RIL_UNSOL_WIFI_LOCK";
            default:
                return "<unknown response>";
        }
    }

    static String requestToString(int request) {
        switch (request) {
            case 2116:
                return "MwiRILConstants.RIL_REQUEST_SET_WIFI_ENABLED";
            case 2117:
                return "MwiRILConstants.RIL_REQUEST_SET_WIFI_ASSOCIATED";
            case 2118:
                return "MwiRILConstants.RIL_REQUEST_SET_WIFI_SIGNAL_LEVEL";
            case 2119:
                return "MwiRILConstants.RIL_REQUEST_SET_WIFI_IP_ADDRESS";
            case 2120:
                return "MwiRILConstants.RIL_REQUEST_SET_GEO_LOCATION";
            case 2121:
                return "MwiRILConstants.RIL_REQUEST_SET_EMERGENCY_ADDRESS_ID";
            case 2131:
                return "MwiRILConstants.RIL_REQUEST_SET_NATT_KEEPALIVE_STATUS";
            case 2132:
                return "MwiRILConstants.RIL_REQUEST_SET_WIFI_PING_RESULT";
            case 2179:
                return "MwiRILConstants.RIL_REQUEST_NOTIFY_EPDG_SCREEN_STATE";
            case 2187:
                return "MwiRILConstants.RIL_REQUEST_SET_WFC_CONFIG";
            case 2188:
                return "MwiRILConstants.RIL_REQUEST_GET_WFC_CONFIG";
            default:
                return "<unknown request>";
        }
    }

    static String retToString(int req, Object ret) {
        if (ret == null) {
            return "";
        }
        if (ret instanceof int[]) {
            int[] intArray = (int[]) ret;
            int length = intArray.length;
            StringBuilder sb = new StringBuilder("{");
            if (length > 0) {
                sb.append(intArray[0]);
                for (int i = 0 + 1; i < length; i++) {
                    sb.append(", ");
                    sb.append(intArray[i]);
                }
            }
            sb.append("}");
            return sb.toString();
        } else if (!(ret instanceof String[])) {
            return ret.toString();
        } else {
            String[] strings = (String[]) ret;
            int length2 = strings.length;
            StringBuilder sb2 = new StringBuilder("{");
            if (length2 > 0) {
                sb2.append(strings[0]);
                for (int i2 = 0 + 1; i2 < length2; i2++) {
                    sb2.append(", ");
                    sb2.append(strings[i2]);
                }
            }
            sb2.append("}");
            return sb2.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public void riljLog(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.d(MWIRIL_LOG_TAG, sb.toString());
    }

    /* access modifiers changed from: package-private */
    public void riljLoge(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.e(MWIRIL_LOG_TAG, sb.toString());
    }

    /* access modifiers changed from: package-private */
    public void riljLoge(String msg, Exception e) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.e(MWIRIL_LOG_TAG, sb.toString(), e);
    }

    /* access modifiers changed from: package-private */
    public void riljLogv(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.v(MWIRIL_LOG_TAG, sb.toString());
    }

    /* access modifiers changed from: package-private */
    public void unsljLog(int response) {
        riljLog("[UNSL]< " + responseToString(response));
    }

    /* access modifiers changed from: package-private */
    public void unsljLogMore(int response, String more) {
        riljLog("[UNSL]< " + responseToString(response) + " " + more);
    }

    /* access modifiers changed from: package-private */
    public void unsljLogRet(int response, Object ret) {
        riljLog("[UNSL]< " + responseToString(response) + " " + retToString(response, ret));
    }

    /* access modifiers changed from: package-private */
    public void unsljLogvRet(int response, Object ret) {
        riljLogv("[UNSL]< " + responseToString(response) + " " + retToString(response, ret));
    }
}
