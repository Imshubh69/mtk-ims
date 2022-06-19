package com.mediatek.ims.ril;

import android.content.Context;
import android.hardware.radio.V1_0.CallForwardInfo;
import android.hardware.radio.V1_0.CdmaSmsAck;
import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.hardware.radio.V1_0.CdmaSmsSubaddress;
import android.hardware.radio.V1_0.Dial;
import android.hardware.radio.V1_0.GsmSmsMessage;
import android.hardware.radio.V1_0.IRadio;
import android.hardware.radio.V1_0.ImsSmsMessage;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_0.UusInfo;
import android.hidl.base.V1_0.IBase;
import android.net.ConnectivityManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.telephony.ModemActivityInfo;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyHistogram;
import android.telephony.ims.ImsCallProfile;
import android.util.SparseArray;
import com.android.internal.telephony.ClientWakelockTracker;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.HalVersion;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccUtils;
import com.mediatek.ims.ImsCallInfo;
import com.mediatek.ims.ImsCallSessionProxy;
import com.mediatek.ims.ImsCommonUtil;
import com.mediatek.ims.ImsServiceCallTracker;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import vendor.mediatek.hardware.mtkradioex.V3_0.CallForwardInfoEx;
import vendor.mediatek.hardware.mtkradioex.V3_0.ConferenceDial;
import vendor.mediatek.hardware.mtkradioex.V3_0.IMtkRadioEx;

public final class ImsRILAdapter extends ImsBaseCommands implements ImsCommandsInterface {
    private static final int DEFAULT_ACK_WAKE_LOCK_TIMEOUT_MS = 200;
    private static final int DEFAULT_WAKE_LOCK_TIMEOUT_MS = 60000;
    static final int EVENT_ACK_WAKE_LOCK_TIMEOUT = 4;
    static final int EVENT_BLOCKING_RESPONSE_TIMEOUT = 5;
    static final int EVENT_MTK_RADIO_PROXY_DEAD = 7;
    static final int EVENT_RADIO_PROXY_DEAD = 6;
    static final int EVENT_SEND = 1;
    static final int EVENT_TRIGGER_TO_FIRE_PENDING_URC = 8;
    static final int EVENT_WAKE_LOCK_TIMEOUT = 2;
    public static final int FOR_ACK_WAKELOCK = 1;
    public static final int FOR_WAKELOCK = 0;
    static final boolean IMSRIL_LOGD = true;
    static final boolean IMSRIL_LOGV = false;
    static final String IMSRIL_LOG_TAG = "IMS_RILA";
    static final String[] IMS_HIDL_SERVICE_NAME = {"imsAospSlot1", "imsAospSlot2", "imsAospSlot3", "imsAospSlot4"};
    static final boolean IMS_RILA_LOGD = true;
    public static final int INVALID_WAKELOCK = -1;
    static final int IRADIO_GET_SERVICE_DELAY_MILLIS = 1000;
    static final String[] MTK_IMS_HIDL_SERVICE_NAME = {"imsSlot1", "imsSlot2", "imsSlot3", "imsSlot4"};
    static final HalVersion MTK_RADIO_HAL_VERSION_2_0 = new HalVersion(2, 0);
    static final HalVersion MTK_RADIO_HAL_VERSION_2_1 = new HalVersion(2, 1);
    static final HalVersion MTK_RADIO_HAL_VERSION_2_2 = new HalVersion(2, 2);
    static final HalVersion MTK_RADIO_HAL_VERSION_3_0 = new HalVersion(3, 0);
    static final String PROPERTY_WAKE_LOCK_TIMEOUT = "ro.ril.wake_lock_timeout";
    static final HalVersion RADIO_HAL_VERSION_1_0 = new HalVersion(1, 0);
    static final HalVersion RADIO_HAL_VERSION_1_1 = new HalVersion(1, 1);
    static final HalVersion RADIO_HAL_VERSION_1_2 = new HalVersion(1, 2);
    static final HalVersion RADIO_HAL_VERSION_1_3 = new HalVersion(1, 3);
    static final HalVersion RADIO_HAL_VERSION_1_4 = new HalVersion(1, 4);
    static final HalVersion RADIO_HAL_VERSION_1_5 = new HalVersion(1, 5);
    static final HalVersion RADIO_HAL_VERSION_UNKNOWN = HalVersion.UNKNOWN;
    static final String RILJ_ACK_WAKELOCK_NAME = "IMSRIL_ACK_WL";
    static final int RIL_HISTOGRAM_BUCKET_COUNT = 5;
    static SparseArray<TelephonyHistogram> mRilTimeHistograms = new SparseArray<>();
    final PowerManager.WakeLock mAckWakeLock;
    final int mAckWakeLockTimeout;
    volatile int mAckWlSequenceNum;
    private WorkSource mActiveWakelockWorkSource;
    private final ClientWakelockTracker mClientWakelockTracker = new ClientWakelockTracker();
    Context mContext;
    private DtmfQueueHandler mDtmfReqQueue;
    ImsRadioIndication mImsRadioIndication;
    ImsRadioIndicationV2 mImsRadioIndicationV2;
    ImsRadioResponse mImsRadioResponse;
    ImsRadioResponseV2 mImsRadioResponseV2;
    boolean mIsMobileNetworkSupported;
    Object[] mLastNITZTimeInfo;
    /* access modifiers changed from: private */
    public TelephonyMetrics mMetrics;
    volatile IBase mMtkRadioProxy;
    final AtomicLong mMtkRadioProxyCookie;
    final MtkRadioProxyDeathRecipient mMtkRadioProxyDeathRecipient;
    private HalVersion mMtkRadioVersion;
    private OpImsCommandsInterface mOpCI;
    final Integer mPhoneId;
    private WorkSource mRILDefaultWorkSource;
    RadioIndicationImpl mRadioIndication;
    volatile IRadio mRadioProxy;
    final AtomicLong mRadioProxyCookie;
    final RadioProxyDeathRecipient mRadioProxyDeathRecipient;
    RadioResponseImpl mRadioResponse;
    private HalVersion mRadioVersion;
    SparseArray<RILRequest> mRequestList;
    final RilHandler mRilHandler;
    AtomicBoolean mTestingEmergencyCall;
    final PowerManager.WakeLock mWakeLock;
    int mWakeLockCount;
    final int mWakeLockTimeout;
    volatile int mWlSequenceNum;

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

    class DtmfQueueHandler {
        private final boolean DTMF_STATUS_START;
        private final boolean DTMF_STATUS_STOP;
        public final int MAXIMUM_DTMF_REQUEST;
        /* access modifiers changed from: private */
        public Vector<DtmfQueueRR> mDtmfQueue;
        private boolean mDtmfStatus;
        private boolean mIsSendChldRequest;
        private DtmfQueueRR mPendingCHLDRequest;

        public class DtmfQueueRR {
            public Object[] params;

            /* renamed from: rr */
            public RILRequest f43rr;

            public DtmfQueueRR(RILRequest rr, Object[] params2) {
                this.f43rr = rr;
                this.params = params2;
            }
        }

        public DtmfQueueHandler() {
            this.MAXIMUM_DTMF_REQUEST = 32;
            this.DTMF_STATUS_START = true;
            this.DTMF_STATUS_STOP = false;
            this.mDtmfStatus = false;
            this.mDtmfQueue = new Vector<>(32);
            this.mPendingCHLDRequest = null;
            this.mIsSendChldRequest = false;
            this.mDtmfStatus = false;
        }

        public void start() {
            this.mDtmfStatus = true;
        }

        public void stop() {
            this.mDtmfStatus = false;
        }

        public boolean isStart() {
            return this.mDtmfStatus;
        }

        public void add(DtmfQueueRR o) {
            this.mDtmfQueue.addElement(o);
        }

        public void remove(DtmfQueueRR o) {
            this.mDtmfQueue.remove(o);
        }

        public void remove(int idx) {
            this.mDtmfQueue.removeElementAt(idx);
        }

        public DtmfQueueRR get() {
            return this.mDtmfQueue.get(0);
        }

        public int size() {
            return this.mDtmfQueue.size();
        }

        public void setPendingRequest(DtmfQueueRR r) {
            this.mPendingCHLDRequest = r;
        }

        public DtmfQueueRR getPendingRequest() {
            return this.mPendingCHLDRequest;
        }

        public void setSendChldRequest() {
            this.mIsSendChldRequest = true;
        }

        public void resetSendChldRequest() {
            this.mIsSendChldRequest = false;
        }

        public boolean hasSendChldRequest() {
            ImsRILAdapter imsRILAdapter = ImsRILAdapter.this;
            imsRILAdapter.riljLog("mIsSendChldRequest = " + this.mIsSendChldRequest);
            return this.mIsSendChldRequest;
        }

        public DtmfQueueRR buildDtmfQueueRR(RILRequest rr, Object[] param) {
            if (rr == null) {
                return null;
            }
            ImsRILAdapter imsRILAdapter = ImsRILAdapter.this;
            imsRILAdapter.riljLog("DtmfQueueHandler.buildDtmfQueueRR build ([" + rr.mSerial + "] reqId=" + rr.mRequest + ")");
            return new DtmfQueueRR(rr, param);
        }
    }

    class RilHandler extends Handler {
        RilHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    synchronized (ImsRILAdapter.this.mRequestList) {
                        if (msg.arg1 == ImsRILAdapter.this.mWlSequenceNum && ImsRILAdapter.this.clearWakeLock(0)) {
                            int count = ImsRILAdapter.this.mRequestList.size();
                            Rlog.d(ImsRILAdapter.IMSRIL_LOG_TAG, "WAKE_LOCK_TIMEOUT  mRequestList=" + count);
                            for (int i = 0; i < count; i++) {
                                RILRequest rr = ImsRILAdapter.this.mRequestList.valueAt(i);
                                Rlog.d(ImsRILAdapter.IMSRIL_LOG_TAG, i + ": [" + rr.mSerial + "] " + ImsRILAdapter.requestToString(rr.mRequest));
                            }
                        }
                    }
                    return;
                case 4:
                    if (msg.arg1 == ImsRILAdapter.this.mAckWlSequenceNum) {
                        boolean unused = ImsRILAdapter.this.clearWakeLock(1);
                        return;
                    }
                    return;
                case 5:
                    RILRequest rr2 = ImsRILAdapter.this.findAndRemoveRequestFromList(msg.arg1);
                    if (rr2 != null) {
                        if (rr2.mResult != null) {
                            AsyncResult.forMessage(rr2.mResult, ImsRILAdapter.getResponseForTimedOutRILRequest(rr2), (Throwable) null);
                            rr2.mResult.sendToTarget();
                            ImsRILAdapter.this.mMetrics.writeOnRilTimeoutResponse(ImsRILAdapter.this.mPhoneId.intValue(), rr2.mSerial, rr2.mRequest);
                        }
                        ImsRILAdapter.this.decrementWakeLock(rr2);
                        rr2.release();
                        return;
                    }
                    return;
                case 6:
                    ImsRILAdapter imsRILAdapter = ImsRILAdapter.this;
                    imsRILAdapter.riljLog("handleMessage: EVENT_RADIO_PROXY_DEAD cookie = " + msg.obj + " mRadioProxyCookie = " + ImsRILAdapter.this.mRadioProxyCookie.get());
                    if (((Long) msg.obj).longValue() == ImsRILAdapter.this.mRadioProxyCookie.get()) {
                        ImsRILAdapter.this.resetProxyAndRequestList();
                        IRadio unused2 = ImsRILAdapter.this.getRadioProxy((Message) null);
                        return;
                    }
                    return;
                case 7:
                    ImsRILAdapter imsRILAdapter2 = ImsRILAdapter.this;
                    imsRILAdapter2.riljLog("handleMessage: EVENT_MTK_RADIO_PROXY_DEAD cookie = " + msg.obj + " mMtkRadioProxyCookie = " + ImsRILAdapter.this.mMtkRadioProxyCookie.get());
                    if (((Long) msg.obj).longValue() == ImsRILAdapter.this.mMtkRadioProxyCookie.get()) {
                        ImsRILAdapter.this.resetMtkProxyAndRequestList();
                        if (ImsRILAdapter.this.getMtkRadioProxy((Message) null) != null) {
                            ImsRILAdapter.this.notifyImsServiceReady();
                            return;
                        } else {
                            ImsRILAdapter.this.mRilHandler.sendMessage(ImsRILAdapter.this.mRilHandler.obtainMessage(8));
                            return;
                        }
                    } else {
                        return;
                    }
                case 8:
                    IRadio proxy = ImsRILAdapter.this.getRadioProxy((Message) null);
                    IBase mtkProxy = ImsRILAdapter.this.getMtkRadioProxy((Message) null);
                    if (proxy == null || mtkProxy == null) {
                        ImsRILAdapter imsRILAdapter3 = ImsRILAdapter.this;
                        imsRILAdapter3.riljLog("resend EVENT_TRIGGER_TO_FIRE_PENDING_URC " + ImsRILAdapter.this.mPhoneId);
                        ImsRILAdapter.this.mRilHandler.removeMessages(8);
                        ImsRILAdapter.this.mRilHandler.sendMessageDelayed(ImsRILAdapter.this.mRilHandler.obtainMessage(8), 1000);
                        return;
                    }
                    ImsRILAdapter imsRILAdapter4 = ImsRILAdapter.this;
                    imsRILAdapter4.riljLog("Trigger to fire pending URC " + ImsRILAdapter.this.mPhoneId);
                    ImsRILAdapter.this.notifyImsServiceReady();
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public static Object getResponseForTimedOutRILRequest(RILRequest rr) {
        if (rr == null) {
            return null;
        }
        switch (rr.mRequest) {
            case ImsCallSessionProxy.CALL_INFO_MSG_TYPE_REMOTE_HOLD:
                return new ModemActivityInfo(0, 0, 0, new int[ModemActivityInfo.getNumTxPowerLevels()], 0);
            default:
                return null;
        }
    }

    final class RadioProxyDeathRecipient implements IHwBinder.DeathRecipient {
        RadioProxyDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            ImsRILAdapter.this.riljLog("serviceDied");
            ImsRILAdapter.this.mRilHandler.sendMessage(ImsRILAdapter.this.mRilHandler.obtainMessage(6, Long.valueOf(cookie)));
        }
    }

    final class MtkRadioProxyDeathRecipient implements IHwBinder.DeathRecipient {
        MtkRadioProxyDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            ImsRILAdapter.this.riljLog("MtkRadioProxyDeathRecipient, serviceDied");
            ImsRILAdapter.this.mRilHandler.sendMessage(ImsRILAdapter.this.mRilHandler.obtainMessage(7, Long.valueOf(cookie)));
        }
    }

    /* access modifiers changed from: private */
    public void resetProxyAndRequestList() {
        riljLogi("resetProxyAndRequestList");
        this.mRadioProxy = null;
        this.mRadioProxyCookie.incrementAndGet();
        setRadioState(2);
        RILRequest.resetSerial();
        clearRequestList(1, false);
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003d, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x017f, code lost:
        r8.mRadioProxy = null;
        riljLoge("RadioProxy getService/setResponseFunctions: " + r3);
     */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003d A[ExcHandler: RemoteException | RuntimeException (r3v39 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:22:0x0043] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.hardware.radio.V1_0.IRadio getRadioProxy(android.os.Message r9) {
        /*
            r8 = this;
            boolean r0 = r8.mIsMobileNetworkSupported
            r1 = 0
            if (r0 != 0) goto L_0x0009
            r8.handleProxyNotExist(r9)
            return r1
        L_0x0009:
            android.hardware.radio.V1_0.IRadio r0 = r8.mRadioProxy
            if (r0 == 0) goto L_0x0010
            android.hardware.radio.V1_0.IRadio r0 = r8.mRadioProxy
            return r0
        L_0x0010:
            com.mediatek.ims.ril.ImsRILAdapter$RilHandler r0 = r8.mRilHandler
            r2 = 6
            boolean r0 = r0.hasMessages(r2)
            if (r0 == 0) goto L_0x0022
            java.lang.String r0 = "getRadioProxy service died, we try again later"
            r8.riljLogi(r0)
            r8.handleProxyNotExist(r9)
            return r1
        L_0x0022:
            r0 = 1
            java.lang.String[] r3 = IMS_HIDL_SERVICE_NAME     // Catch:{ NoSuchElementException -> 0x0040 }
            java.lang.Integer r4 = r8.mPhoneId     // Catch:{ NoSuchElementException -> 0x0040 }
            r5 = 0
            if (r4 != 0) goto L_0x002c
            r4 = r5
            goto L_0x0030
        L_0x002c:
            int r4 = r4.intValue()     // Catch:{ NoSuchElementException -> 0x0040 }
        L_0x0030:
            r3 = r3[r4]     // Catch:{ NoSuchElementException -> 0x0040 }
            android.hardware.radio.V1_0.IRadio r3 = android.hardware.radio.V1_0.IRadio.getService(r3, r5)     // Catch:{ NoSuchElementException -> 0x0040 }
            r8.mRadioProxy = r3     // Catch:{ NoSuchElementException -> 0x0040 }
            com.android.internal.telephony.HalVersion r3 = RADIO_HAL_VERSION_1_0     // Catch:{ NoSuchElementException -> 0x0040 }
            r8.mRadioVersion = r3     // Catch:{ NoSuchElementException -> 0x0040 }
            goto L_0x0046
        L_0x003d:
            r3 = move-exception
            goto L_0x017f
        L_0x0040:
            r3 = move-exception
            java.lang.String r4 = "getRadioProxy: NoSuchElementException "
            r8.riljLoge(r4)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
        L_0x0046:
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            if (r3 == 0) goto L_0x00c8
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            android.hardware.radio.V1_5.IRadio r3 = android.hardware.radio.V1_5.IRadio.castFrom(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            if (r3 == 0) goto L_0x005f
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            android.hardware.radio.V1_5.IRadio r3 = android.hardware.radio.V1_5.IRadio.castFrom(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r8.mRadioProxy = r3     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            com.android.internal.telephony.HalVersion r3 = RADIO_HAL_VERSION_1_5     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r8.mRadioVersion = r3     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            goto L_0x00b2
        L_0x005f:
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            android.hardware.radio.V1_4.IRadio r3 = android.hardware.radio.V1_4.IRadio.castFrom(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            if (r3 == 0) goto L_0x0074
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            android.hardware.radio.V1_4.IRadio r3 = android.hardware.radio.V1_4.IRadio.castFrom(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r8.mRadioProxy = r3     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            com.android.internal.telephony.HalVersion r3 = RADIO_HAL_VERSION_1_4     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r8.mRadioVersion = r3     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            goto L_0x00b2
        L_0x0074:
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            android.hardware.radio.V1_3.IRadio r3 = android.hardware.radio.V1_3.IRadio.castFrom(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            if (r3 == 0) goto L_0x0089
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            android.hardware.radio.V1_3.IRadio r3 = android.hardware.radio.V1_3.IRadio.castFrom(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r8.mRadioProxy = r3     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            com.android.internal.telephony.HalVersion r3 = RADIO_HAL_VERSION_1_3     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r8.mRadioVersion = r3     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            goto L_0x00b2
        L_0x0089:
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            android.hardware.radio.V1_2.IRadio r3 = android.hardware.radio.V1_2.IRadio.castFrom(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            if (r3 == 0) goto L_0x009e
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            android.hardware.radio.V1_2.IRadio r3 = android.hardware.radio.V1_2.IRadio.castFrom(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r8.mRadioProxy = r3     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            com.android.internal.telephony.HalVersion r3 = RADIO_HAL_VERSION_1_2     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r8.mRadioVersion = r3     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            goto L_0x00b2
        L_0x009e:
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            android.hardware.radio.V1_1.IRadio r3 = android.hardware.radio.V1_1.IRadio.castFrom(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            if (r3 == 0) goto L_0x00b2
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            android.hardware.radio.V1_1.IRadio r3 = android.hardware.radio.V1_1.IRadio.castFrom(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r8.mRadioProxy = r3     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            com.android.internal.telephony.HalVersion r3 = RADIO_HAL_VERSION_1_1     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r8.mRadioVersion = r3     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
        L_0x00b2:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r3.<init>()     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            java.lang.String r4 = "getRadioProxy: mRadioVersion "
            r3.append(r4)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            com.android.internal.telephony.HalVersion r4 = r8.mRadioVersion     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r3.append(r4)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            java.lang.String r3 = r3.toString()     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r8.riljLoge(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
        L_0x00c8:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r3.<init>()     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            java.lang.String r4 = "getRadioProxy: "
            r3.append(r4)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            android.hardware.radio.V1_0.IRadio r4 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r3.append(r4)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            java.lang.String r3 = r3.toString()     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r8.riljLogi(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            if (r3 == 0) goto L_0x0179
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            com.mediatek.ims.ril.ImsRILAdapter$RadioProxyDeathRecipient r4 = r8.mRadioProxyDeathRecipient     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            java.util.concurrent.atomic.AtomicLong r5 = r8.mRadioProxyCookie     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            long r5 = r5.incrementAndGet()     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r3.linkToDeath(r4, r5)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            com.mediatek.ims.ril.RadioResponseImpl r4 = r8.mRadioResponse     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            com.mediatek.ims.ril.RadioIndicationImpl r5 = r8.mRadioIndication     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            r3.setResponseFunctions(r4, r5)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            java.lang.String r3 = "setResponseFunctionsIms"
            r8.riljLogi(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            com.mediatek.ims.ril.ImsRILAdapter$DtmfQueueHandler r3 = r8.mDtmfReqQueue     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            if (r3 == 0) goto L_0x017e
            monitor-enter(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0176 }
            r4.<init>()     // Catch:{ all -> 0x0176 }
            java.lang.String r5 = "queue size  "
            r4.append(r5)     // Catch:{ all -> 0x0176 }
            com.mediatek.ims.ril.ImsRILAdapter$DtmfQueueHandler r5 = r8.mDtmfReqQueue     // Catch:{ all -> 0x0176 }
            int r5 = r5.size()     // Catch:{ all -> 0x0176 }
            r4.append(r5)     // Catch:{ all -> 0x0176 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0176 }
            r8.riljLog(r4)     // Catch:{ all -> 0x0176 }
            com.mediatek.ims.ril.ImsRILAdapter$DtmfQueueHandler r4 = r8.mDtmfReqQueue     // Catch:{ all -> 0x0176 }
            int r4 = r4.size()     // Catch:{ all -> 0x0176 }
            int r4 = r4 - r0
        L_0x0123:
            if (r4 < 0) goto L_0x012d
            com.mediatek.ims.ril.ImsRILAdapter$DtmfQueueHandler r5 = r8.mDtmfReqQueue     // Catch:{ all -> 0x0176 }
            r5.remove((int) r4)     // Catch:{ all -> 0x0176 }
            int r4 = r4 + -1
            goto L_0x0123
        L_0x012d:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0176 }
            r5.<init>()     // Catch:{ all -> 0x0176 }
            java.lang.String r6 = "queue size  after "
            r5.append(r6)     // Catch:{ all -> 0x0176 }
            com.mediatek.ims.ril.ImsRILAdapter$DtmfQueueHandler r6 = r8.mDtmfReqQueue     // Catch:{ all -> 0x0176 }
            int r6 = r6.size()     // Catch:{ all -> 0x0176 }
            r5.append(r6)     // Catch:{ all -> 0x0176 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0176 }
            r8.riljLog(r5)     // Catch:{ all -> 0x0176 }
            com.mediatek.ims.ril.ImsRILAdapter$DtmfQueueHandler r5 = r8.mDtmfReqQueue     // Catch:{ all -> 0x0176 }
            com.mediatek.ims.ril.ImsRILAdapter$DtmfQueueHandler$DtmfQueueRR r5 = r5.getPendingRequest()     // Catch:{ all -> 0x0176 }
            if (r5 == 0) goto L_0x0174
            java.lang.String r5 = "reset pending switch request"
            r8.riljLog(r5)     // Catch:{ all -> 0x0176 }
            com.mediatek.ims.ril.ImsRILAdapter$DtmfQueueHandler r5 = r8.mDtmfReqQueue     // Catch:{ all -> 0x0176 }
            com.mediatek.ims.ril.ImsRILAdapter$DtmfQueueHandler$DtmfQueueRR r5 = r5.getPendingRequest()     // Catch:{ all -> 0x0176 }
            com.mediatek.ims.ril.RILRequest r6 = r5.f43rr     // Catch:{ all -> 0x0176 }
            android.os.Message r7 = r6.mResult     // Catch:{ all -> 0x0176 }
            if (r7 == 0) goto L_0x016a
            android.os.Message r7 = r6.mResult     // Catch:{ all -> 0x0176 }
            android.os.AsyncResult.forMessage(r7, r1, r1)     // Catch:{ all -> 0x0176 }
            android.os.Message r7 = r6.mResult     // Catch:{ all -> 0x0176 }
            r7.sendToTarget()     // Catch:{ all -> 0x0176 }
        L_0x016a:
            com.mediatek.ims.ril.ImsRILAdapter$DtmfQueueHandler r7 = r8.mDtmfReqQueue     // Catch:{ all -> 0x0176 }
            r7.resetSendChldRequest()     // Catch:{ all -> 0x0176 }
            com.mediatek.ims.ril.ImsRILAdapter$DtmfQueueHandler r7 = r8.mDtmfReqQueue     // Catch:{ all -> 0x0176 }
            r7.setPendingRequest(r1)     // Catch:{ all -> 0x0176 }
        L_0x0174:
            monitor-exit(r3)     // Catch:{ all -> 0x0176 }
            goto L_0x017e
        L_0x0176:
            r4 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x0176 }
            throw r4     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
        L_0x0179:
            java.lang.String r3 = "getRadioProxy: mRadioProxy == null"
            r8.riljLoge(r3)     // Catch:{ RemoteException | RuntimeException -> 0x003d, RemoteException | RuntimeException -> 0x003d }
        L_0x017e:
            goto L_0x0195
        L_0x017f:
            r8.mRadioProxy = r1
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "RadioProxy getService/setResponseFunctions: "
            r4.append(r5)
            r4.append(r3)
            java.lang.String r4 = r4.toString()
            r8.riljLoge(r4)
        L_0x0195:
            android.hardware.radio.V1_0.IRadio r3 = r8.mRadioProxy
            if (r3 != 0) goto L_0x01c0
            if (r9 == 0) goto L_0x01a6
            com.android.internal.telephony.CommandException r0 = com.android.internal.telephony.CommandException.fromRilErrno(r0)
            android.os.AsyncResult.forMessage(r9, r1, r0)
            r9.sendToTarget()
        L_0x01a6:
            com.mediatek.ims.ril.ImsRILAdapter$RilHandler r0 = r8.mRilHandler
            r0.removeMessages(r2)
            com.mediatek.ims.ril.ImsRILAdapter$RilHandler r0 = r8.mRilHandler
            java.util.concurrent.atomic.AtomicLong r1 = r8.mRadioProxyCookie
            long r3 = r1.get()
            java.lang.Long r1 = java.lang.Long.valueOf(r3)
            android.os.Message r1 = r0.obtainMessage(r2, r1)
            r2 = 1000(0x3e8, double:4.94E-321)
            r0.sendMessageDelayed(r1, r2)
        L_0x01c0:
            android.hardware.radio.V1_0.IRadio r0 = r8.mRadioProxy
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.ril.ImsRILAdapter.getRadioProxy(android.os.Message):android.hardware.radio.V1_0.IRadio");
    }

    public ImsRILAdapter(Context context, int instanceId) {
        super(context, instanceId);
        boolean z = false;
        this.mWlSequenceNum = 0;
        this.mAckWlSequenceNum = 0;
        this.mRequestList = new SparseArray<>();
        this.mTestingEmergencyCall = new AtomicBoolean(false);
        HalVersion halVersion = RADIO_HAL_VERSION_UNKNOWN;
        this.mRadioVersion = halVersion;
        this.mMtkRadioVersion = halVersion;
        this.mMetrics = TelephonyMetrics.getInstance();
        this.mDtmfReqQueue = new DtmfQueueHandler();
        this.mImsRadioResponseV2 = null;
        this.mImsRadioIndicationV2 = null;
        this.mRadioProxy = null;
        this.mMtkRadioProxy = null;
        this.mRadioProxyCookie = new AtomicLong(0);
        this.mMtkRadioProxyCookie = new AtomicLong(0);
        riljLogi("Ims-RIL: init phone = " + instanceId);
        this.mContext = context;
        this.mPhoneId = Integer.valueOf(instanceId);
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mIsMobileNetworkSupported = true;
        this.mRadioResponse = new RadioResponseImpl(this, instanceId);
        this.mRadioIndication = new RadioIndicationImpl(this, instanceId);
        this.mImsRadioResponse = new ImsRadioResponse(this, instanceId);
        this.mImsRadioIndication = new ImsRadioIndication(this, instanceId);
        this.mRilHandler = new RilHandler();
        this.mRadioProxyDeathRecipient = new RadioProxyDeathRecipient();
        this.mMtkRadioProxyDeathRecipient = new MtkRadioProxyDeathRecipient();
        PowerManager pm = (PowerManager) context.getSystemService("power");
        PowerManager.WakeLock newWakeLock = pm.newWakeLock(1, IMSRIL_LOG_TAG);
        this.mWakeLock = newWakeLock;
        newWakeLock.setReferenceCounted(false);
        PowerManager.WakeLock newWakeLock2 = pm.newWakeLock(1, RILJ_ACK_WAKELOCK_NAME);
        this.mAckWakeLock = newWakeLock2;
        newWakeLock2.setReferenceCounted(false);
        this.mWakeLockTimeout = SystemProperties.getInt(PROPERTY_WAKE_LOCK_TIMEOUT, DEFAULT_WAKE_LOCK_TIMEOUT_MS);
        this.mAckWakeLockTimeout = SystemProperties.getInt(PROPERTY_WAKE_LOCK_TIMEOUT, 200);
        this.mWakeLockCount = 0;
        this.mRILDefaultWorkSource = new WorkSource(context.getApplicationInfo().uid, context.getPackageName());
        IRadio proxy = getRadioProxy((Message) null);
        StringBuilder sb = new StringBuilder();
        sb.append("Ims-RIL: proxy = ");
        sb.append(proxy == null);
        riljLogi(sb.toString());
        IBase mtkProxy = getMtkRadioProxy((Message) null);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("Mtk-Ims-RIL: proxy = ");
        sb2.append(mtkProxy == null ? true : z);
        riljLog(sb2.toString());
        this.mOpCI = OpImsRILUtil.makeCommandInterface(context, instanceId);
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
    }

    private void handleMtkRadioProxyExceptionForRR(RILRequest rr, String caller, Exception e) {
        riljLoge(caller + ": " + e);
        resetMtkProxyAndRequestList();
    }

    /* access modifiers changed from: protected */
    public String convertNullToEmptyString(String string) {
        return string != null ? string : "";
    }

    public OpImsCommandsInterface getOpCommandsInterface() {
        return this.mOpCI;
    }

    public void setMute(boolean enableMute, Message result) {
        IRadio radioProxy = getRadioProxy((Message) null);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(53, (Message) null, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " mute = " + enableMute);
            try {
                radioProxy.setMute(rr.mSerial, enableMute);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setMute", e);
            }
        }
    }

    public void start(String callee, ImsCallProfile callProfile, int clirMode, boolean isEmergency, boolean isVideoCall, Message result) {
        if (isVideoCall) {
            vtDial(callee, clirMode, (UUSInfo) null, result);
        } else if (isEmergency) {
            emergencyDial(callee, callProfile, clirMode, (UUSInfo) null, result);
        } else {
            dial(callee, clirMode, result);
        }
    }

    public void startConference(String[] participants, int clirMode, boolean isVideoCall, Message result) {
        conferenceDial(participants, clirMode, isVideoCall, result);
    }

    public void accept(Message response) {
        IRadio radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(40, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.acceptCall(rr.mSerial);
                this.mMetrics.writeRilAnswer(this.mPhoneId.intValue(), rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "acceptCall", e);
            }
        }
    }

    public void acceptVideoCall(int videoMode, int callId, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2076, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " videoMode = " + videoMode + " callId = " + callId);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).videoCallAccept(rr.mSerial, videoMode, callId);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).videoCallAccept(rr.mSerial, videoMode, callId);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "acceptCall", e);
            }
        }
    }

    public void approveEccRedial(int approve, int callId, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2185, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " approve = " + approve + " callId = " + callId);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).eccRedialApprove(rr.mSerial, approve, callId);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).eccRedialApprove(rr.mSerial, approve, callId);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "approveEccRedial", e);
            }
        }
    }

    public void hangup(int callId, Message response) {
        IRadio radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(12, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " callId = " + callId);
            try {
                radioProxy.hangup(rr.mSerial, callId);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "hangup", e);
            }
        }
    }

    public void hangup(int callId, int reason, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2179, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " callId = " + callId + "reason=" + reason);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).hangupWithReason(rr.mSerial, callId, reason);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).hangupWithReason(rr.mSerial, callId, reason);
                }
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "hangupWithReason", e);
            }
        }
    }

    public void explicitCallTransfer(Message response) {
        if (getRadioProxy(response) != null) {
            RILRequest rr = obtainRequest(72, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            handleChldRelatedRequest(rr, (Object[]) null);
        }
    }

    public void unattendedCallTransfer(String number, int type, Message response) {
        internalImsEct(number, type, response);
    }

    public void hold(int callId, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2084, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + "callId = " + callId);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).controlCall(rr.mSerial, 0, callId);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).controlCall(rr.mSerial, 0, callId);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "holdCall", e);
            }
        }
    }

    public void resume(int callId, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2085, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + "callId = " + callId);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).controlCall(rr.mSerial, 1, callId);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).controlCall(rr.mSerial, 1, callId);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "resumeCall", e);
            }
        }
    }

    public void conference(Message response) {
        if (getRadioProxy(response) != null) {
            RILRequest rr = obtainRequest(16, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            handleChldRelatedRequest(rr, (Object[]) null);
        }
    }

    public void sendDtmf(char c, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(24, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                int i = rr.mSerial;
                radioProxy.sendDtmf(i, c + "");
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendDtmf", e);
            }
        }
    }

    public void startDtmf(char c, Message result) {
        synchronized (this.mDtmfReqQueue) {
            if (!this.mDtmfReqQueue.hasSendChldRequest()) {
                int size = this.mDtmfReqQueue.size();
                Objects.requireNonNull(this.mDtmfReqQueue);
                if (size < 32) {
                    if (this.mDtmfReqQueue.isStart()) {
                        riljLog("DTMF status conflict, want to start DTMF when status is " + this.mDtmfReqQueue.isStart());
                    } else if (getRadioProxy(result) != null) {
                        RILRequest rr = obtainRequest(49, result, this.mRILDefaultWorkSource);
                        this.mDtmfReqQueue.start();
                        DtmfQueueHandler.DtmfQueueRR dqrr = this.mDtmfReqQueue.buildDtmfQueueRR(rr, new Object[]{Character.valueOf(c)});
                        this.mDtmfReqQueue.add(dqrr);
                        if (this.mDtmfReqQueue.size() == 1) {
                            riljLog("send start dtmf");
                            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
                            sendDtmfQueueRR(dqrr);
                        }
                    }
                }
            }
        }
    }

    public void stopDtmf(Message result) {
        synchronized (this.mDtmfReqQueue) {
            if (!this.mDtmfReqQueue.hasSendChldRequest()) {
                int size = this.mDtmfReqQueue.size();
                Objects.requireNonNull(this.mDtmfReqQueue);
                if (size < 32) {
                    if (!this.mDtmfReqQueue.isStart()) {
                        riljLog("DTMF status conflict, want to start DTMF when status is " + this.mDtmfReqQueue.isStart());
                    } else if (getRadioProxy(result) != null) {
                        RILRequest rr = obtainRequest(50, result, this.mRILDefaultWorkSource);
                        this.mDtmfReqQueue.stop();
                        DtmfQueueHandler.DtmfQueueRR dqrr = this.mDtmfReqQueue.buildDtmfQueueRR(rr, (Object[]) null);
                        this.mDtmfReqQueue.add(dqrr);
                        if (this.mDtmfReqQueue.size() == 1) {
                            riljLog("send stop dtmf");
                            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
                            sendDtmfQueueRR(dqrr);
                        }
                    }
                }
            }
        }
    }

    public void setCallIndication(int mode, int callId, int seqNum, int cause, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2016, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " mode = " + mode + " callId = " + callId + " seqNum = " + seqNum + " cause = " + cause);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setCallIndication(rr.mSerial, mode, callId, seqNum, cause);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setCallIndication(rr.mSerial, mode, callId, seqNum, cause);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setCallIndication", e);
            }
        }
    }

    public void deregisterIms(Message response) {
        deregisterImsWithCause(1, response);
    }

    public void deregisterImsWithCause(int cause, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2082, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest));
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).imsDeregNotification(rr.mSerial, cause);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).imsDeregNotification(rr.mSerial, cause);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "imsDeregNotification", e);
            }
        }
    }

    public void setImsCfg(int[] params, Message response) {
        Message message = response;
        boolean volteEnable = params[0] == 1;
        boolean vilteEnable = params[1] == 1;
        boolean vowifiEnable = params[2] == 1;
        boolean viwifiEnable = params[3] == 1;
        boolean smsEnable = params[4] == 1;
        boolean eimsEnable = params[5] == 1;
        IBase radioProxy = getMtkRadioProxy(message);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2077, message, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " volteEnable = " + params[0] + " vilteEnable = " + params[1] + " vowifiEnable = " + params[2] + " viwifiEnable = " + params[3] + " smsEnable = " + params[4] + " eimsEnable = " + params[5]);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setImscfg(rr.mSerial, volteEnable, vilteEnable, vowifiEnable, viwifiEnable, smsEnable, eimsEnable);
                    return;
                }
                ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setImscfg(rr.mSerial, volteEnable, vilteEnable, vowifiEnable, viwifiEnable, smsEnable, eimsEnable);
                if (ImsCommonUtil.supportMdAutoSetupIms()) {
                    findAndRemoveRequestFromList(rr.mSerial);
                    riljLog(rr.serialString() + "<  " + requestToString(rr.mRequest) + " sent and removed");
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setImsCfg", e);
            }
        }
    }

    public void setModemImsCfg(String keys, String values, int type, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2128, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " keys = " + keys + " values = " + values + " type = " + type);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setModemImsCfg(rr.mSerial, keys, values, type);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setModemImsCfg(rr.mSerial, keys, values, type);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "sendModemImsCfg", e);
            }
        }
    }

    public void turnOnIms(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2069, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " switch = ON");
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setImsEnable(rr.mSerial, true);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setImsEnable(rr.mSerial, true);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setImsEnable", e);
            }
        }
    }

    public void turnOffIms(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2069, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " switch = OFF");
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setImsEnable(rr.mSerial, false);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setImsEnable(rr.mSerial, false);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setImsEnable", e);
            }
        }
    }

    public void turnOnVolte(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2070, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " switch = ON");
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 11, Integer.toString(1));
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 11, Integer.toString(1));
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setVendorSetting", e);
            }
        }
    }

    public void turnOffVolte(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2070, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " switch = OFF");
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 11, Integer.toString(0));
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 11, Integer.toString(0));
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setVendorSetting", e);
            }
        }
    }

    public void turnOnWfc(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2071, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " switch = ON");
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 16, Integer.toString(1));
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 16, Integer.toString(1));
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setVendorSetting", e);
            }
        }
    }

    public void turnOffWfc(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2071, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " switch = OFF");
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 16, Integer.toString(0));
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 16, Integer.toString(0));
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setVendorSetting", e);
            }
        }
    }

    public void turnOnVilte(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2072, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " switch = ON");
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 12, Integer.toString(1));
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 12, Integer.toString(1));
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setVendorSetting", e);
            }
        }
    }

    public void turnOffVilte(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2072, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " switch = OFF");
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 12, Integer.toString(0));
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 12, Integer.toString(0));
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setVendorSetting", e);
            }
        }
    }

    public void turnOnViwifi(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2073, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " switch = ON");
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 13, Integer.toString(1));
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 13, Integer.toString(1));
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setVendorSetting", e);
            }
        }
    }

    public void turnOffViwifi(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2073, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " switch = OFF");
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 13, Integer.toString(0));
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 13, Integer.toString(0));
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setVendorSetting", e);
            }
        }
    }

    public void turnOnRcsUa(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2166, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " switch = ON");
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 14, Integer.toString(1));
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 14, Integer.toString(1));
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setVendorSetting", e);
            }
        }
    }

    public void turnOffRcsUa(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2166, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " switch = OFF");
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 14, Integer.toString(0));
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setVendorSetting(rr.mSerial, 14, Integer.toString(0));
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setVendorSetting", e);
            }
        }
    }

    public void getProvisionValue(String provisionStr, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2078, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " provisionStr = " + provisionStr);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).getProvisionValue(rr.mSerial, provisionStr);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).getProvisionValue(rr.mSerial, provisionStr);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getProvisionValue", e);
            }
        }
    }

    public void setProvisionValue(String provisionStr, String provisionValue, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2079, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " provisionStr = " + provisionStr + " provisionValue" + provisionValue);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setProvisionValue(rr.mSerial, provisionStr, provisionValue);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setProvisionValue(rr.mSerial, provisionStr, provisionValue);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setProvisionValue", e);
            }
        }
    }

    public void setImsCfgFeatureValue(int featureId, int network, int value, int isLast, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2136, result, this.mRILDefaultWorkSource);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setImsCfgFeatureValue(rr.mSerial, featureId, network, value, isLast);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setImsCfgFeatureValue(rr.mSerial, featureId, network, value, isLast);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setImsCfgFeatureValue", e);
            }
        }
    }

    public void getImsCfgFeatureValue(int featureId, int network, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2137, result, this.mRILDefaultWorkSource);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).getImsCfgFeatureValue(rr.mSerial, featureId, network);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).getImsCfgFeatureValue(rr.mSerial, featureId, network);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getImsCfgFeatureValue", e);
            }
        }
    }

    public void setImsCfgProvisionValue(int configId, String value, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2138, result, this.mRILDefaultWorkSource);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setImsCfgProvisionValue(rr.mSerial, configId, value);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setImsCfgProvisionValue(rr.mSerial, configId, value);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setImsCfgProvisionValue", e);
            }
        }
    }

    public void getImsCfgProvisionValue(int configId, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2139, result, this.mRILDefaultWorkSource);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).getImsCfgProvisionValue(rr.mSerial, configId);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).getImsCfgProvisionValue(rr.mSerial, configId);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getImsCfgProvisionValue", e);
            }
        }
    }

    public void getImsCfgResourceCapValue(int featureId, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2141, result, this.mRILDefaultWorkSource);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).getImsCfgResourceCapValue(rr.mSerial, featureId);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).getImsCfgResourceCapValue(rr.mSerial, featureId);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getImsCfgResourceCapValue", e);
            }
        }
    }

    public void inviteParticipants(int confCallId, String participant, Message response) {
        internalAddConferenceMember(confCallId, participant, ImsServiceCallTracker.getInstance(this.mPhoneId.intValue()).getParticipantCallId(participant), response);
    }

    public void removeParticipants(int confCallId, String participant, Message response) {
        internalRemoveConferenceMember(confCallId, participant, ImsServiceCallTracker.getInstance(this.mPhoneId.intValue()).getParticipantCallId(participant), response);
    }

    public void inviteParticipantsByCallId(int confCallId, ImsCallInfo callInfo, Message response) {
        if (callInfo == null) {
            Rlog.d(IMSRIL_LOG_TAG, "Invite participants failed, call info is null");
            return;
        }
        String callId = callInfo.mCallId;
        try {
            internalAddConferenceMember(confCallId, callInfo.mCallNum, Integer.parseInt(callId), response);
        } catch (NumberFormatException e) {
            Rlog.d(IMSRIL_LOG_TAG, "Invite participants failed: id is not integer: " + callId);
        }
    }

    public void getLastCallFailCause(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(18, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest));
            try {
                radioProxy.getLastCallFailCause(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getLastCallFailCause", e);
            }
        }
    }

    public void hangupAllCall(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2019, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest));
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).hangupAll(rr.mSerial);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).hangupAll(rr.mSerial);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "hangupAll", e);
            }
        }
    }

    public void swap(Message response) {
        if (getRadioProxy(response) != null) {
            RILRequest rr = obtainRequest(15, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            handleChldRelatedRequest(rr, (Object[]) null);
        }
    }

    public void sendWfcProfileInfo(int wfcPreference, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2095, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " wfcPreference = " + wfcPreference);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setWfcProfile(rr.mSerial, wfcPreference);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setWfcProfile(rr.mSerial, wfcPreference);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setWfcProfile", e);
            }
        }
    }

    public void dial(String address, int clirMode, Message result) {
        dial(address, clirMode, (UUSInfo) null, result);
    }

    public void dial(String address, int clirMode, UUSInfo uusInfo, Message response) {
        if (!PhoneNumberUtils.isUriNumber(address)) {
            IRadio radioProxy = getRadioProxy(response);
            if (radioProxy != null) {
                RILRequest rr = obtainRequest(2098, response, this.mRILDefaultWorkSource);
                Dial dialInfo = new Dial();
                dialInfo.address = convertNullToEmptyString(address);
                dialInfo.clir = clirMode;
                if (uusInfo != null) {
                    UusInfo info = new UusInfo();
                    info.uusType = uusInfo.getType();
                    info.uusDcs = uusInfo.getDcs();
                    info.uusData = new String(uusInfo.getUserData());
                    dialInfo.uusInfo.add(info);
                }
                riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
                try {
                    radioProxy.dial(rr.mSerial, dialInfo);
                } catch (RemoteException | RuntimeException e) {
                    handleRadioProxyExceptionForRR(rr, "dial", e);
                }
            }
        } else {
            IBase radioProxy2 = getMtkRadioProxy(response);
            if (radioProxy2 != null) {
                RILRequest rr2 = obtainRequest(2086, response, this.mRILDefaultWorkSource);
                riljLog(rr2.serialString() + "> " + requestToString(rr2.mRequest));
                try {
                    if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                        ((IMtkRadioEx) radioProxy2).dialWithSipUri(rr2.mSerial, address);
                    } else {
                        ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy2).dialWithSipUri(rr2.mSerial, address);
                    }
                } catch (RemoteException | RuntimeException e2) {
                    handleMtkRadioProxyExceptionForRR(rr2, "dialWithSipUri", e2);
                }
            }
        }
    }

    private void emergencyDial(String address, ImsCallProfile callprofile, int clirMode, UUSInfo uusInfo, Message result) {
        ArrayList arrayList;
        Message message = result;
        IRadio radioProxy = getRadioProxy(message);
        android.hardware.radio.V1_4.IRadio radioProxy14 = (android.hardware.radio.V1_4.IRadio) radioProxy;
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2087, message, this.mRILDefaultWorkSource);
            Dial dialInfo = new Dial();
            dialInfo.address = convertNullToEmptyString(address);
            dialInfo.clir = clirMode;
            if (uusInfo != null) {
                UusInfo info = new UusInfo();
                info.uusType = uusInfo.getType();
                info.uusDcs = uusInfo.getDcs();
                info.uusData = new String(uusInfo.getUserData());
                dialInfo.uusInfo.add(info);
            }
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                int i = rr.mSerial;
                int emergencyServiceCategories = callprofile.getEmergencyServiceCategories();
                if (callprofile.getEmergencyUrns() != null) {
                    arrayList = new ArrayList(callprofile.getEmergencyUrns());
                } else {
                    arrayList = new ArrayList();
                }
                radioProxy14.emergencyDial(i, dialInfo, emergencyServiceCategories, arrayList, callprofile.getEmergencyCallRouting(), false, false);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "emergencyDial", e);
            }
        } else {
            int i2 = clirMode;
        }
    }

    public void conferenceDial(String[] participants, int clirMode, boolean isVideoCall, Message result) {
        if (participants == null) {
            riljLoge("Participants MUST NOT be null in conferenceDial");
            return;
        }
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2089, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " clirMode = " + clirMode + " isVideoCall = " + isVideoCall);
            try {
                int i = 0;
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ConferenceDial dialInfo = new ConferenceDial();
                    dialInfo.clir = clirMode;
                    dialInfo.isVideoCall = isVideoCall;
                    int length = participants.length;
                    while (i < length) {
                        String dialNumber = participants[i];
                        dialInfo.dialNumbers.add(dialNumber);
                        riljLog("conferenceDial: dialNumber " + ImsServiceCallTracker.sensitiveEncode(dialNumber));
                        i++;
                    }
                    ((IMtkRadioEx) radioProxy).conferenceDial(rr.mSerial, dialInfo);
                    return;
                }
                vendor.mediatek.hardware.mtkradioex.V2_0.ConferenceDial dialInfo2 = new vendor.mediatek.hardware.mtkradioex.V2_0.ConferenceDial();
                dialInfo2.clir = clirMode;
                dialInfo2.isVideoCall = isVideoCall;
                int length2 = participants.length;
                while (i < length2) {
                    String dialNumber2 = participants[i];
                    dialInfo2.dialNumbers.add(dialNumber2);
                    riljLog("conferenceDial: dialNumber " + ImsServiceCallTracker.sensitiveEncode(dialNumber2));
                    i++;
                }
                ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).conferenceDial(rr.mSerial, dialInfo2);
            } catch (RemoteException | RuntimeException e) {
                Rlog.w(IMSRIL_LOG_TAG, "conferenceDial failed");
                handleMtkRadioProxyExceptionForRR(rr, "conferenceDial", e);
            }
        }
    }

    public void vtDial(String address, int clirMode, UUSInfo uusInfo, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy == null) {
            return;
        }
        if (!PhoneNumberUtils.isUriNumber(address)) {
            RILRequest rr = obtainRequest(2099, response, this.mRILDefaultWorkSource);
            Dial dialInfo = new Dial();
            dialInfo.address = convertNullToEmptyString(address);
            dialInfo.clir = clirMode;
            if (uusInfo != null) {
                UusInfo info = new UusInfo();
                info.uusType = uusInfo.getType();
                info.uusDcs = uusInfo.getDcs();
                info.uusData = new String(uusInfo.getUserData());
                dialInfo.uusInfo.add(info);
            }
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).vtDial(rr.mSerial, dialInfo);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).vtDial(rr.mSerial, dialInfo);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "vtDial", e);
            }
        } else {
            RILRequest rr2 = obtainRequest(2092, response, this.mRILDefaultWorkSource);
            riljLog(rr2.serialString() + "> " + requestToString(rr2.mRequest));
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).vtDialWithSipUri(rr2.mSerial, address);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).vtDialWithSipUri(rr2.mSerial, address);
                }
            } catch (RemoteException | RuntimeException e2) {
                handleMtkRadioProxyExceptionForRR(rr2, "vtDialWithSipUri", e2);
            }
        }
    }

    public void sendUSSI(String ussiString, Message response) {
        if (SystemProperties.get("persist.vendor.ims.ussi.ap").equals("1")) {
            riljLog("Wrap sendUSSI, ussiString = " + ussiString);
            response.sendToTarget();
            return;
        }
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2093, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " ussiString = " + ussiString);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).sendUssi(rr.mSerial, ussiString);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).sendUssi(rr.mSerial, ussiString);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "sendUssi", e);
            }
        }
    }

    public void cancelPendingUssi(Message response) {
        if (SystemProperties.get("persist.vendor.ims.ussi.ap").equals("1")) {
            riljLog("Wrap cancelPendingUssi");
            response.sendToTarget();
            return;
        }
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2094, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest));
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).cancelUssi(rr.mSerial);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).cancelUssi(rr.mSerial);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "cancelUssi", e);
            }
        }
    }

    public void queryFacilityLock(String facility, String password, int serviceClass, Message result) {
        queryFacilityLockForApp(facility, password, serviceClass, "A0000000871002", result);
    }

    public void queryFacilityLockForApp(String facility, String password, int serviceClass, String appId, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(42, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " facility = " + facility + " serviceClass = " + serviceClass + " appId = " + appId);
            try {
                radioProxy.getFacilityLockForApp(rr.mSerial, convertNullToEmptyString(facility), convertNullToEmptyString(password), serviceClass, convertNullToEmptyString(appId));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getFacilityLockForApp", e);
            }
        }
    }

    public void setFacilityLock(String facility, boolean lockState, String password, int serviceClass, Message result) {
        setFacilityLockForApp(facility, lockState, password, serviceClass, "A0000000871002", result);
    }

    public void setFacilityLockForApp(String facility, boolean lockState, String password, int serviceClass, String appId, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(43, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " facility = " + facility + " lockstate = " + lockState + " serviceClass = " + serviceClass + " appId = " + appId);
            try {
                radioProxy.setFacilityLockForApp(rr.mSerial, convertNullToEmptyString(facility), lockState, convertNullToEmptyString(password), serviceClass, convertNullToEmptyString(appId));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setFacilityLockForApp", e);
            }
        }
    }

    public void setCallForward(int action, int cfReason, int serviceClass, String number, int timeSeconds, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(34, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " action = " + action + " cfReason = " + cfReason + " serviceClass = " + serviceClass + " timeSeconds = " + timeSeconds);
            CallForwardInfo cfInfo = new CallForwardInfo();
            cfInfo.status = action;
            cfInfo.reason = cfReason;
            cfInfo.serviceClass = serviceClass;
            cfInfo.toa = PhoneNumberUtils.toaFromString(number);
            cfInfo.number = convertNullToEmptyString(number);
            cfInfo.timeSeconds = timeSeconds;
            try {
                radioProxy.setCallForward(rr.mSerial, cfInfo);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setCallForward", e);
            }
        }
    }

    public void queryCallForwardStatus(int cfReason, int serviceClass, String number, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(33, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " cfreason = " + cfReason + " serviceClass = " + serviceClass);
            CallForwardInfo cfInfo = new CallForwardInfo();
            cfInfo.reason = cfReason;
            cfInfo.serviceClass = serviceClass;
            cfInfo.toa = PhoneNumberUtils.toaFromString(number);
            cfInfo.number = convertNullToEmptyString(number);
            cfInfo.timeSeconds = 0;
            try {
                radioProxy.getCallForwardStatus(rr.mSerial, cfInfo);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "queryCallForwardStatus", e);
            }
        }
    }

    public void queryCallWaiting(int serviceClass, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(35, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " serviceClass = " + serviceClass);
            try {
                radioProxy.getCallWaiting(rr.mSerial, serviceClass);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "queryCallWaiting", e);
            }
        }
    }

    public void setCallWaiting(boolean enable, int serviceClass, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(36, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " enable = " + enable + " serviceClass = " + serviceClass);
            try {
                radioProxy.setCallWaiting(rr.mSerial, enable, serviceClass);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setCallWaiting", e);
            }
        }
    }

    public void getCLIR(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(31, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getClir(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "getCLIR", e);
            }
        }
    }

    public void setCLIR(int clirMode, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(32, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " clirMode = " + clirMode);
            try {
                radioProxy.setClir(rr.mSerial, clirMode);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setCLIR", e);
            }
        }
    }

    public void queryCLIP(Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(55, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getClip(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "queryCLIP", e);
            }
        }
    }

    public void setCLIP(int clipEnable, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2103, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " clipEnable = " + clipEnable);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setClip(rr.mSerial, clipEnable);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setClip(rr.mSerial, clipEnable);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setCLIP", e);
            }
        }
    }

    public void getCOLR(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2105, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).getColr(rr.mSerial);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).getColr(rr.mSerial);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getCOLR", e);
            }
        }
    }

    public void setCOLR(int colrEnable, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2124, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " colrEnable = " + colrEnable);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setColr(rr.mSerial, colrEnable);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setColr(rr.mSerial, colrEnable);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setCOLR", e);
            }
        }
    }

    public void getCOLP(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2104, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).getColp(rr.mSerial);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).getColp(rr.mSerial);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getCOLP", e);
            }
        }
    }

    public void setCOLP(int colpEnable, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2123, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " colpEnable = " + colpEnable);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setColp(rr.mSerial, colpEnable);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setColp(rr.mSerial, colpEnable);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setCOLP", e);
            }
        }
    }

    public void queryCallForwardInTimeSlotStatus(int cfReason, int serviceClass, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2125, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " cfreason = " + cfReason + " serviceClass = " + serviceClass);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    CallForwardInfoEx cfInfoEx = new CallForwardInfoEx();
                    cfInfoEx.reason = cfReason;
                    cfInfoEx.serviceClass = serviceClass;
                    cfInfoEx.toa = PhoneNumberUtils.toaFromString("");
                    cfInfoEx.number = convertNullToEmptyString("");
                    cfInfoEx.timeSeconds = 0;
                    cfInfoEx.timeSlotBegin = convertNullToEmptyString("");
                    cfInfoEx.timeSlotEnd = convertNullToEmptyString("");
                    ((IMtkRadioEx) radioProxy).queryCallForwardInTimeSlotStatus(rr.mSerial, cfInfoEx);
                    return;
                }
                vendor.mediatek.hardware.mtkradioex.V2_0.CallForwardInfoEx cfInfoEx2 = new vendor.mediatek.hardware.mtkradioex.V2_0.CallForwardInfoEx();
                cfInfoEx2.reason = cfReason;
                cfInfoEx2.serviceClass = serviceClass;
                cfInfoEx2.toa = PhoneNumberUtils.toaFromString("");
                cfInfoEx2.number = convertNullToEmptyString("");
                cfInfoEx2.timeSeconds = 0;
                cfInfoEx2.timeSlotBegin = convertNullToEmptyString("");
                cfInfoEx2.timeSlotEnd = convertNullToEmptyString("");
                ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).queryCallForwardInTimeSlotStatus(rr.mSerial, cfInfoEx2);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "queryCallForwardInTimeSlotStatus", e);
            }
        }
    }

    public void setCallForwardInTimeSlot(int action, int cfReason, int serviceClass, String number, int timeSeconds, long[] timeSlot, Message result) {
        String timeSlotBegin = "";
        String timeSlotEnd = "";
        if (timeSlot != null && timeSlot.length == 2) {
            for (int i = 0; i < timeSlot.length; i++) {
                Date date = new Date(timeSlot[i]);
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                if (i == 0) {
                    timeSlotBegin = dateFormat.format(date);
                } else {
                    timeSlotEnd = dateFormat.format(date);
                }
            }
        }
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2126, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " action = " + action + " cfReason = " + cfReason + " serviceClass = " + serviceClass + " timeSeconds = " + timeSeconds + "timeSlot = [" + timeSlotBegin + ":" + timeSlotEnd + "]");
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    CallForwardInfoEx cfInfoEx = new CallForwardInfoEx();
                    cfInfoEx.status = action;
                    cfInfoEx.reason = cfReason;
                    cfInfoEx.serviceClass = serviceClass;
                    cfInfoEx.toa = PhoneNumberUtils.toaFromString(number);
                    cfInfoEx.number = convertNullToEmptyString(number);
                    cfInfoEx.timeSeconds = timeSeconds;
                    cfInfoEx.timeSlotBegin = convertNullToEmptyString(timeSlotBegin);
                    cfInfoEx.timeSlotEnd = convertNullToEmptyString(timeSlotEnd);
                    ((IMtkRadioEx) radioProxy).setCallForwardInTimeSlot(rr.mSerial, cfInfoEx);
                    return;
                }
                vendor.mediatek.hardware.mtkradioex.V2_0.CallForwardInfoEx cfInfoEx2 = new vendor.mediatek.hardware.mtkradioex.V2_0.CallForwardInfoEx();
                cfInfoEx2.status = action;
                cfInfoEx2.reason = cfReason;
                cfInfoEx2.serviceClass = serviceClass;
                cfInfoEx2.toa = PhoneNumberUtils.toaFromString(number);
                cfInfoEx2.number = convertNullToEmptyString(number);
                cfInfoEx2.timeSeconds = timeSeconds;
                cfInfoEx2.timeSlotBegin = convertNullToEmptyString(timeSlotBegin);
                cfInfoEx2.timeSlotEnd = convertNullToEmptyString(timeSlotEnd);
                ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setCallForwardInTimeSlot(rr.mSerial, cfInfoEx2);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setCallForwardInTimeSlot", e);
            }
        }
    }

    public void runGbaAuthentication(String nafFqdn, String nafSecureProtocolId, boolean forceRun, int netId, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2127, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " nafFqdn = " + nafFqdn + " nafSecureProtocolId = " + nafSecureProtocolId + " forceRun = " + forceRun + " netId = " + netId);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).runGbaAuthentication(rr.mSerial, nafFqdn, nafSecureProtocolId, forceRun, netId);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).runGbaAuthentication(rr.mSerial, nafFqdn, nafSecureProtocolId, forceRun, netId);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "runGbaAuthentication", e);
            }
        }
    }

    public void getXcapStatus(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2163, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest));
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).getXcapStatus(rr.mSerial);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).getXcapStatus(rr.mSerial);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getXcapStatus", e);
            }
        }
    }

    public void resetSuppServ(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2164, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest));
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).resetSuppServ(rr.mSerial);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).resetSuppServ(rr.mSerial);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getXcapStatus", e);
            }
        }
    }

    public void setupXcapUserAgentString(String userAgent, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2167, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " userAgent = " + userAgent);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setupXcapUserAgentString(rr.mSerial, userAgent);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setupXcapUserAgentString(rr.mSerial, userAgent);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setupXcapUserAgentString", e);
            }
        }
    }

    public void requestExitEmergencyCallbackMode(Message response) {
        IRadio radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(99, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest));
            try {
                radioProxy.exitEmergencyCallbackMode(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "exitEmergencyCallbackMode", e);
            }
        }
    }

    public void forceHangup(int callId, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2034, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " callId = " + callId);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).forceReleaseCall(rr.mSerial, callId);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).forceReleaseCall(rr.mSerial, callId);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "forceHangup", e);
            }
        }
    }

    public void responseBearerStateConfirm(int aid, int action, int status, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2080, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " aid = " + aid + " action =" + action + " status =" + status);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).imsBearerStateConfirm(rr.mSerial, aid, action, status);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).imsBearerStateConfirm(rr.mSerial, aid, action, status);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "imsBearerStateConfirm", e);
            }
        }
    }

    public void setImsBearerNotification(int enable, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2135, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " enable = " + enable);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setImsBearerNotification(rr.mSerial, enable);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setImsBearerNotification(rr.mSerial, enable);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setImsBearerNotification", e);
            }
        }
    }

    public void pullCall(String target, boolean isVideoCall, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2096, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " target = " + isVideoCall + " isVideoCall = " + isVideoCall);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).pullCall(rr.mSerial, target, isVideoCall);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).pullCall(rr.mSerial, target, isVideoCall);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "pullCall", e);
            }
        }
    }

    public void setImsRegistrationReport(Message response) {
        IBase radioProxy = getMtkRadioProxy((Message) null);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2097, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setImsRegistrationReport(rr.mSerial);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setImsRegistrationReport(rr.mSerial);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setImsRegistrationReport", e);
            }
        }
    }

    public void setImsRtpInfo(int pdnId, int networkId, int timer, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2088, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " PDN id = " + pdnId + " network Id = " + networkId + " Timer = " + timer);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setImsRtpReport(rr.mSerial, pdnId, networkId, timer);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setImsRtpReport(rr.mSerial, pdnId, networkId, timer);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setImsRtpReport", e);
            }
        }
    }

    public void setVoiceDomainPreference(int vdp, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2122, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " vdp = " + vdp);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setVoiceDomainPreference(rr.mSerial, vdp);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setVoiceDomainPreference(rr.mSerial, vdp);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setVoiceDomainPreference", e);
            }
        }
    }

    private void internalAddConferenceMember(int confCallId, String address, int callIdToAdd, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2090, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " confCallId = " + confCallId + " address = " + ImsServiceCallTracker.sensitiveEncode(address) + " callIdToAdd =" + callIdToAdd);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).controlImsConferenceCallMember(rr.mSerial, 1, confCallId, address, callIdToAdd);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).controlImsConferenceCallMember(rr.mSerial, 1, confCallId, address, callIdToAdd);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "addImsConferenceCallMember", e);
            }
        }
    }

    private void internalRemoveConferenceMember(int confCallId, String address, int callIdToRemove, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2091, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " confCallId = " + confCallId + " address = " + ImsServiceCallTracker.sensitiveEncode(address) + " callIdToRemove =" + callIdToRemove);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).controlImsConferenceCallMember(rr.mSerial, 0, confCallId, address, callIdToRemove);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).controlImsConferenceCallMember(rr.mSerial, 0, confCallId, address, callIdToRemove);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "removeImsConferenceCallMember", e);
            }
        }
    }

    private void internalImsEct(String number, int type, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2083, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).imsEctCommand(rr.mSerial, number, type);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).imsEctCommand(rr.mSerial, number, type);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "imsEctCommand", e);
            }
        }
    }

    private void handleChldRelatedRequest(RILRequest rr, Object[] params) {
        int j;
        synchronized (this.mDtmfReqQueue) {
            int queueSize = this.mDtmfReqQueue.size();
            if (queueSize > 0) {
                if (this.mDtmfReqQueue.get().f43rr.mRequest == 49) {
                    riljLog("DTMF queue isn't 0, first request is START, send stop dtmf and pending switch");
                    if (queueSize > 1) {
                        j = 2;
                    } else {
                        j = 1;
                    }
                    riljLog("queue size  " + this.mDtmfReqQueue.size());
                    for (int i = queueSize + -1; i >= j; i--) {
                        this.mDtmfReqQueue.remove(i);
                    }
                    riljLog("queue size  after " + this.mDtmfReqQueue.size());
                    if (this.mDtmfReqQueue.size() == 1) {
                        riljLog("add dummy stop dtmf request");
                        RILRequest rr3 = obtainRequest(50, (Message) null, this.mRILDefaultWorkSource);
                        DtmfQueueHandler.DtmfQueueRR dqrr3 = this.mDtmfReqQueue.buildDtmfQueueRR(rr3, new Object[]{Integer.valueOf(rr3.mSerial)});
                        this.mDtmfReqQueue.stop();
                        this.mDtmfReqQueue.add(dqrr3);
                    }
                } else {
                    riljLog("DTMF queue isn't 0, first request is STOP, penging switch");
                    for (int i2 = queueSize - 1; i2 >= 1; i2--) {
                        this.mDtmfReqQueue.remove(i2);
                    }
                }
                if (this.mDtmfReqQueue.getPendingRequest() != null) {
                    RILRequest pendingRequest = this.mDtmfReqQueue.getPendingRequest().f43rr;
                    if (pendingRequest.mResult != null) {
                        AsyncResult.forMessage(pendingRequest.mResult, (Object) null, (Throwable) null);
                        pendingRequest.mResult.sendToTarget();
                    }
                }
                this.mDtmfReqQueue.setPendingRequest(this.mDtmfReqQueue.buildDtmfQueueRR(rr, params));
            } else {
                riljLog("DTMF queue is 0, send switch Immediately");
                this.mDtmfReqQueue.setSendChldRequest();
                sendDtmfQueueRR(this.mDtmfReqQueue.buildDtmfQueueRR(rr, params));
            }
        }
    }

    private void sendDtmfQueueRR(DtmfQueueHandler.DtmfQueueRR dqrr) {
        RILRequest rr = dqrr.f43rr;
        IRadio radioProxy = getRadioProxy(rr.mResult);
        if (radioProxy == null) {
            riljLoge("get RadioProxy null. ([" + rr.serialString() + "] request: " + requestToString(rr.mRequest) + ")");
            return;
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " (by DtmfQueueRR)");
        try {
            switch (rr.mRequest) {
                case 15:
                    radioProxy.switchWaitingOrHoldingAndActive(rr.mSerial);
                    return;
                case 16:
                    radioProxy.conference(rr.mSerial);
                    return;
                case 49:
                    Object[] params = dqrr.params;
                    if (params.length != 1) {
                        riljLoge("request " + requestToString(rr.mRequest) + " params error. (" + params.toString() + ")");
                        return;
                    }
                    char c = ((Character) params[0]).charValue();
                    int i = rr.mSerial;
                    radioProxy.startDtmf(i, c + "");
                    return;
                case 50:
                    radioProxy.stopDtmf(rr.mSerial);
                    return;
                case 52:
                    Object[] params2 = dqrr.params;
                    if (params2.length != 1) {
                        riljLoge("request " + requestToString(rr.mRequest) + " params error. (" + Arrays.toString(params2) + ")");
                        return;
                    }
                    radioProxy.separateConnection(rr.mSerial, ((Integer) params2[0]).intValue());
                    return;
                case 72:
                    radioProxy.explicitCallTransfer(rr.mSerial);
                    return;
                default:
                    riljLoge("get RadioProxy null. ([" + rr.serialString() + "] request: " + requestToString(rr.mRequest) + ")");
                    return;
            }
        } catch (RemoteException | RuntimeException e) {
            handleRadioProxyExceptionForRR(rr, "DtmfQueueRR(" + requestToString(rr.mRequest) + ")", e);
        }
        handleRadioProxyExceptionForRR(rr, "DtmfQueueRR(" + requestToString(rr.mRequest) + ")", e);
    }

    /* access modifiers changed from: package-private */
    public void handleDtmfQueueNext(int serial) {
        riljLog("handleDtmfQueueNext (serial = " + serial);
        synchronized (this.mDtmfReqQueue) {
            DtmfQueueHandler.DtmfQueueRR dqrr = null;
            int i = 0;
            while (true) {
                if (i < this.mDtmfReqQueue.mDtmfQueue.size()) {
                    DtmfQueueHandler.DtmfQueueRR adqrr = (DtmfQueueHandler.DtmfQueueRR) this.mDtmfReqQueue.mDtmfQueue.get(i);
                    if (adqrr != null && adqrr.f43rr.mSerial == serial) {
                        dqrr = adqrr;
                        break;
                    }
                    i++;
                } else {
                    break;
                }
            }
            if (dqrr == null) {
                riljLoge("cannot find serial " + serial + " from mDtmfQueue. (size = " + this.mDtmfReqQueue.size() + ")");
            } else {
                this.mDtmfReqQueue.remove(dqrr);
                riljLog("remove first item in dtmf queue done. (size = " + this.mDtmfReqQueue.size() + ")");
            }
            if (this.mDtmfReqQueue.size() > 0) {
                DtmfQueueHandler.DtmfQueueRR dqrr2 = this.mDtmfReqQueue.get();
                RILRequest rr2 = dqrr2.f43rr;
                riljLog(rr2.serialString() + "> " + requestToString(rr2.mRequest));
                sendDtmfQueueRR(dqrr2);
            } else if (this.mDtmfReqQueue.getPendingRequest() != null) {
                riljLog("send pending switch request");
                sendDtmfQueueRR(this.mDtmfReqQueue.getPendingRequest());
                this.mDtmfReqQueue.setSendChldRequest();
                this.mDtmfReqQueue.setPendingRequest((DtmfQueueHandler.DtmfQueueRR) null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void processIndication(int indicationType) {
        if (indicationType == 1) {
            sendAck();
        }
    }

    /* access modifiers changed from: package-private */
    public void processMtkIndication(int indicationType) {
        if (indicationType == 1) {
            sendMtkAck();
        }
    }

    /* access modifiers changed from: package-private */
    public void processRequestAck(int serial) {
        RILRequest rr;
        synchronized (this.mRequestList) {
            rr = this.mRequestList.get(serial);
        }
        if (rr == null) {
            Rlog.w(IMSRIL_LOG_TAG, "processRequestAck: Unexpected solicited ack response! serial: " + serial);
            return;
        }
        decrementWakeLock(rr);
        riljLog(rr.serialString() + " Ack < " + requestToString(rr.mRequest));
    }

    /* access modifiers changed from: package-private */
    public RILRequest processResponse(RadioResponseInfo responseInfo, boolean isProprietary) {
        RILRequest rr;
        int serial = responseInfo.serial;
        int error = responseInfo.error;
        int type = responseInfo.type;
        if (type == 1) {
            synchronized (this.mRequestList) {
                rr = this.mRequestList.get(serial);
            }
            if (rr == null) {
                Rlog.w(IMSRIL_LOG_TAG, "Unexpected solicited ack response! sn: " + serial);
            } else {
                decrementWakeLock(rr);
                riljLog(rr.serialString() + " Ack < " + requestToString(rr.mRequest));
            }
            return rr;
        }
        RILRequest rr2 = findAndRemoveRequestFromList(serial);
        if (rr2 == null) {
            Rlog.e(IMSRIL_LOG_TAG, "processResponse: Unexpected response! serial: " + serial + " error: " + error);
            return null;
        }
        addToRilHistogram(rr2);
        if (type == 2) {
            if (isProprietary) {
                sendMtkAck();
            } else {
                sendAck();
            }
            riljLog("Response received for " + rr2.serialString() + " " + requestToString(rr2.mRequest) + " Sending ack to ril.cpp");
        }
        switch (rr2.mRequest) {
            case 129:
                setRadioState(2);
                break;
        }
        return rr2;
    }

    /* access modifiers changed from: package-private */
    public void processResponseDone(RILRequest rr, RadioResponseInfo responseInfo, Object ret) {
        if (responseInfo.error != 0) {
            riljLog(rr.serialString() + "< " + requestToString(rr.mRequest) + " error " + responseInfo.error);
            rr.onError(responseInfo.error, ret);
        } else if (!(rr.mRequest == 2136 || rr.mRequest == 2137 || rr.mRequest == 2138 || rr.mRequest == 2139 || rr.mRequest == 2141)) {
            riljLog(rr.serialString() + "< " + requestToString(rr.mRequest) + " " + retToString(rr.mRequest, ret));
        }
        this.mMetrics.writeOnRilSolicitedResponse(this.mPhoneId.intValue(), rr.mSerial, responseInfo.error, rr.mRequest, ret);
        if (rr.mRequest == 15 || rr.mRequest == 16 || rr.mRequest == 52 || rr.mRequest == 72) {
            riljLog("clear mIsSendChldRequest");
            this.mDtmfReqQueue.resetSendChldRequest();
        }
        if (responseInfo.type == 0) {
            decrementWakeLock(rr);
        }
        rr.release();
    }

    private void sendAck() {
        RILRequest rr = RILRequest.obtain(800, (Message) null, this.mRILDefaultWorkSource);
        acquireWakeLock(rr, 1);
        IRadio radioProxy = getRadioProxy((Message) null);
        if (radioProxy != null) {
            try {
                radioProxy.responseAcknowledgement();
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendAck", e);
                riljLoge("sendAck: " + e);
            }
        } else {
            Rlog.e(IMSRIL_LOG_TAG, "Error trying to send ack, radioProxy = null");
        }
        rr.release();
    }

    private void sendMtkAck() {
        RILRequest rr = RILRequest.obtain(800, (Message) null, this.mRILDefaultWorkSource);
        acquireWakeLock(rr, 1);
        IBase radioProxy = getMtkRadioProxy((Message) null);
        if (radioProxy != null) {
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).responseAcknowledgementMtk();
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).responseAcknowledgementMtk();
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "sendMtkAck", e);
                riljLoge("sendMtkAck: " + e);
            }
        } else {
            Rlog.e(IMSRIL_LOG_TAG, "Error trying to send MTK ack, radioProxy = null");
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

    private GsmSmsMessage constructGsmSendSmsRilRequest(String smscPdu, String pdu) {
        GsmSmsMessage msg = new GsmSmsMessage();
        String str = "";
        msg.smscPdu = smscPdu == null ? str : smscPdu;
        if (pdu != null) {
            str = pdu;
        }
        msg.pdu = str;
        return msg;
    }

    /* access modifiers changed from: protected */
    public void constructCdmaSendSmsRilRequest(CdmaSmsMessage msg, byte[] pdu) {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(pdu));
        try {
            msg.teleserviceId = dis.readInt();
            boolean z = false;
            msg.isServicePresent = ((byte) dis.readInt()) == 1;
            msg.serviceCategory = dis.readInt();
            msg.address.digitMode = dis.read();
            msg.address.numberMode = dis.read();
            msg.address.numberType = dis.read();
            msg.address.numberPlan = dis.read();
            int addrNbrOfDigits = (byte) dis.read();
            for (int i = 0; i < addrNbrOfDigits; i++) {
                msg.address.digits.add(Byte.valueOf(dis.readByte()));
            }
            msg.subAddress.subaddressType = dis.read();
            CdmaSmsSubaddress cdmaSmsSubaddress = msg.subAddress;
            if (((byte) dis.read()) == 1) {
                z = true;
            }
            cdmaSmsSubaddress.odd = z;
            int subaddrNbrOfDigits = (byte) dis.read();
            for (int i2 = 0; i2 < subaddrNbrOfDigits; i2++) {
                msg.subAddress.digits.add(Byte.valueOf(dis.readByte()));
            }
            int bearerDataLength = dis.read();
            for (int i3 = 0; i3 < bearerDataLength; i3++) {
                msg.bearerData.add(Byte.valueOf(dis.readByte()));
            }
        } catch (IOException ex) {
            riljLog("sendSmsCdma: conversion from input stream to object failed: " + ex);
        }
    }

    public void sendSms(int token, int messageRef, String format, String smsc, boolean isRetry, byte[] pdu, Message response) {
        int i;
        String str = format;
        Message message = response;
        IBase radioProxy = getMtkRadioProxy(message);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2133, message, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            ImsSmsMessage msg = new ImsSmsMessage();
            msg.retry = isRetry;
            msg.messageRef = messageRef;
            if ("3gpp".equals(str)) {
                msg.tech = 1;
                msg.gsmMessage.add(constructGsmSendSmsRilRequest(smsc, IccUtils.bytesToHexString(pdu)));
                byte[] bArr = pdu;
            } else {
                String str2 = smsc;
                if ("3gpp2".equals(str)) {
                    msg.tech = 2;
                    CdmaSmsMessage cdmaMsg = new CdmaSmsMessage();
                    constructCdmaSendSmsRilRequest(cdmaMsg, pdu);
                    msg.cdmaMessage.add(cdmaMsg);
                } else {
                    byte[] bArr2 = pdu;
                    riljLog(rr.serialString() + "> SMS format Error");
                    return;
                }
            }
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).sendImsSmsEx(rr.mSerial, msg);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).sendImsSmsEx(rr.mSerial, msg);
                }
                TelephonyMetrics telephonyMetrics = this.mMetrics;
                int intValue = this.mPhoneId.intValue();
                int i2 = rr.mSerial;
                if ("3gpp".equals(str)) {
                    i = 1;
                } else {
                    i = 2;
                }
                telephonyMetrics.writeRilSendSms(intValue, i2, 3, i, 0);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "sendImsGsmSms", e);
            }
        } else {
            int i3 = messageRef;
            String str3 = smsc;
            boolean z = isRetry;
            byte[] bArr3 = pdu;
        }
    }

    public void acknowledgeLastIncomingGsmSms(boolean success, int cause, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2170, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " success = " + success + " cause = " + cause);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).acknowledgeLastIncomingGsmSmsEx(rr.mSerial, success, cause);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).acknowledgeLastIncomingGsmSmsEx(rr.mSerial, success, cause);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "acknowledgeLastIncomingGsmSms", e);
            }
        }
    }

    public void acknowledgeLastIncomingCdmaSmsEx(boolean success, int cause, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2172, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " success = " + success + " cause = " + cause);
            CdmaSmsAck msg = new CdmaSmsAck();
            msg.errorClass = success ^ true ? 1 : 0;
            msg.smsCauseCode = cause;
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).acknowledgeLastIncomingCdmaSmsEx(rr.mSerial, msg);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).acknowledgeLastIncomingCdmaSmsEx(rr.mSerial, msg);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "acknowledgeLastIncomingCdmaSms", e);
            }
        }
    }

    public void setSipHeader(int total, int index, int headerCount, String headerValuePair, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2180, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            ArrayList<String> arrList = new ArrayList<>();
            arrList.add(Integer.toString(total));
            arrList.add(Integer.toString(index));
            arrList.add(Integer.toString(headerCount));
            arrList.add(headerValuePair);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setSipHeader(rr.mSerial, arrList);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setSipHeader(rr.mSerial, arrList);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setSipHeader", e);
            }
        }
    }

    public void setSipHeaderReport(String callId, String headerType, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2181, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " callId = " + callId + " headerType = " + headerType);
            ArrayList<String> arrList = new ArrayList<>();
            arrList.add(callId);
            arrList.add(headerType);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setSipHeaderReport(rr.mSerial, arrList);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setSipHeaderReport(rr.mSerial, arrList);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setSipHeaderReport", e);
            }
        }
    }

    public void setImsCallMode(int mode, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2182, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " mode = " + mode);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setImsCallMode(rr.mSerial, mode);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setImsCallMode(rr.mSerial, mode);
                }
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setImsCallMode", e);
            }
        }
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
    private void acquireWakeLock(com.mediatek.ims.ril.RILRequest r8, int r9) {
        /*
            r7 = this;
            monitor-enter(r8)
            int r0 = r8.mWakeLockType     // Catch:{ all -> 0x00c7 }
            r1 = -1
            if (r0 == r1) goto L_0x0022
            java.lang.String r0 = "IMS_RILA"
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
            java.lang.String r0 = "IMS_RILA"
            goto L_0x00b1
        L_0x0029:
            android.os.PowerManager$WakeLock r0 = r7.mAckWakeLock     // Catch:{ all -> 0x00c7 }
            monitor-enter(r0)     // Catch:{ all -> 0x00c7 }
            android.os.PowerManager$WakeLock r1 = r7.mAckWakeLock     // Catch:{ all -> 0x004d }
            r1.acquire()     // Catch:{ all -> 0x004d }
            int r1 = r7.mAckWlSequenceNum     // Catch:{ all -> 0x004d }
            int r1 = r1 + 1
            r7.mAckWlSequenceNum = r1     // Catch:{ all -> 0x004d }
            com.mediatek.ims.ril.ImsRILAdapter$RilHandler r1 = r7.mRilHandler     // Catch:{ all -> 0x004d }
            r2 = 4
            android.os.Message r1 = r1.obtainMessage(r2)     // Catch:{ all -> 0x004d }
            int r2 = r7.mAckWlSequenceNum     // Catch:{ all -> 0x004d }
            r1.arg1 = r2     // Catch:{ all -> 0x004d }
            com.mediatek.ims.ril.ImsRILAdapter$RilHandler r2 = r7.mRilHandler     // Catch:{ all -> 0x004d }
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
            com.mediatek.ims.ril.ImsRILAdapter$RilHandler r2 = r7.mRilHandler     // Catch:{ all -> 0x00ae }
            r3 = 2
            android.os.Message r2 = r2.obtainMessage(r3)     // Catch:{ all -> 0x00ae }
            int r3 = r7.mWlSequenceNum     // Catch:{ all -> 0x00ae }
            r2.arg1 = r3     // Catch:{ all -> 0x00ae }
            com.mediatek.ims.ril.ImsRILAdapter$RilHandler r3 = r7.mRilHandler     // Catch:{ all -> 0x00ae }
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
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.ril.ImsRILAdapter.acquireWakeLock(com.mediatek.ims.ril.RILRequest, int):void");
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
                    Rlog.w(IMSRIL_LOG_TAG, "Decrementing Invalid Wakelock type " + rr.mWakeLockType);
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
                Rlog.d(IMSRIL_LOG_TAG, "NOTE: mWakeLockCount is " + this.mWakeLockCount + "at time of clearing");
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

    public void clearRequestList(int error, boolean loggable) {
        synchronized (this.mRequestList) {
            int count = this.mRequestList.size();
            if (loggable) {
                Rlog.d(IMSRIL_LOG_TAG, "clearRequestList  mWakeLockCount=" + this.mWakeLockCount + " mRequestList=" + count);
            }
            for (int i = 0; i < count; i++) {
                RILRequest rr = this.mRequestList.valueAt(i);
                if (loggable) {
                    Rlog.d(IMSRIL_LOG_TAG, i + ": [" + rr.mSerial + "] " + requestToString(rr.mRequest));
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

    public void setRttModifyRequestResponse(int callId, int result, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2177, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " callId = " + callId + " result = " + result);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).rttModifyRequestResponse(rr.mSerial, callId, result);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).rttModifyRequestResponse(rr.mSerial, callId, result);
                }
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "rttModifyRequestResponse", e);
            }
        }
    }

    public void sendRttModifyRequest(int callId, int newMode, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2175, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " callId = " + callId + " newMode = " + newMode);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).sendRttModifyRequest(rr.mSerial, callId, newMode);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).sendRttModifyRequest(rr.mSerial, callId, newMode);
                }
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendRttModifyRequest", e);
            }
        }
    }

    public void sendRttText(int callId, String text, int length, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2176, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " callId = " + callId + " text = " + text + " length = " + length);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).sendRttText(rr.mSerial, callId, length, text);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).sendRttText(rr.mSerial, callId, length, text);
                }
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendRttText", e);
            }
        }
    }

    public void setRttMode(int mode, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2174, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " mode = " + mode);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setRttMode(rr.mSerial, mode);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setRttMode(rr.mSerial, mode);
                }
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setRttMode", e);
            }
        }
    }

    static String responseToString(int response) {
        switch (response) {
            case 1000:
                return "RADIO_STATE_CHANGED";
            case 1011:
                return "RIL_UNSOL_SUPP_SVC_NOTIFICATION";
            case ImsRILConstants.RIL_UNSOL_INCOMING_CALL_INDICATION:
                return "INCOMING_CALL_INDICATION";
            case ImsRILConstants.RIL_UNSOL_CIPHER_INDICATION:
                return "CIPHER_INDICATION";
            case ImsRILConstants.RIL_UNSOL_SPEECH_CODEC_INFO:
                return "SPEECH_CODEC_INFO";
            case ImsRILConstants.RIL_UNSOL_CALL_INFO_INDICATION:
                return "CALL_INFO_INDICATION";
            case ImsRILConstants.RIL_UNSOL_ECONF_RESULT_INDICATION:
                return "ECONF_RESULT_INDICATION";
            case ImsRILConstants.RIL_UNSOL_SIP_CALL_PROGRESS_INDICATOR:
                return "SIP_CALL_PROGRESS_INDICATOR";
            case ImsRILConstants.RIL_UNSOL_CALLMOD_CHANGE_INDICATOR:
                return "CALLMOD_CHANGE_INDICATOR";
            case ImsRILConstants.RIL_UNSOL_VIDEO_CAPABILITY_INDICATOR:
                return "VIDEO_CAPABILITY_INDICATOR";
            case ImsRILConstants.RIL_UNSOL_ON_USSI:
                return "ON_USSI";
            case ImsRILConstants.RIL_UNSOL_GET_PROVISION_DONE:
                return "GET_PROVISION_DONE";
            case ImsRILConstants.RIL_UNSOL_IMS_RTP_INFO:
                return "UNSOL_IMS_RTP_INFO";
            case ImsRILConstants.RIL_UNSOL_ON_XUI:
                return "ON_XUI";
            case ImsRILConstants.RIL_UNSOL_IMS_EVENT_PACKAGE_INDICATION:
                return "IMS_EVENT_PACKAGE_INDICATION";
            case ImsRILConstants.RIL_UNSOL_IMS_REGISTRATION_INFO:
                return "IMS_REGISTRATION_INFO";
            case ImsRILConstants.RIL_UNSOL_IMS_ENABLE_DONE:
                return "IMS_ENABLE_DONE";
            case ImsRILConstants.RIL_UNSOL_IMS_DISABLE_DONE:
                return "IMS_DISABLE_DONE";
            case ImsRILConstants.RIL_UNSOL_IMS_ENABLE_START:
                return "IMS_ENABLE_START";
            case ImsRILConstants.RIL_UNSOL_IMS_DISABLE_START:
                return "IMS_DISABLE_START";
            case ImsRILConstants.RIL_UNSOL_ECT_INDICATION:
                return "ECT_INDICATION";
            case ImsRILConstants.RIL_UNSOL_VOLTE_SETTING:
                return "VOLTE_SETTING";
            case ImsRILConstants.RIL_UNSOL_IMS_BEARER_STATE_NOTIFY:
                return "IMS_BEARER_STATE_NOTIFY";
            case ImsRILConstants.RIL_UNSOL_IMS_BEARER_INIT:
                return "RIL_UNSOL_IMS_BEARER_INIT";
            case ImsRILConstants.RIL_UNSOL_IMS_DEREG_DONE:
                return "IMS_DEREG_DONE";
            case ImsRILConstants.RIL_UNSOL_IMS_CONFERENCE_INFO_INDICATION:
                return "RIL_UNSOL_IMS_CONFERENCE_INFO_INDICATION";
            case ImsRILConstants.RIL_UNSOL_LTE_MESSAGE_WAITING_INDICATION:
                return "RIL_UNSOL_LTE_MESSAGE_WAITING_INDICATION";
            case ImsRILConstants.RIL_UNSOL_IMS_CONFIG_DYNAMIC_IMS_SWITCH_COMPLETE:
                return "DYNAMIC_IMS_SWITCH_COMPLETE";
            case ImsRILConstants.RIL_UNSOL_IMS_CONFIG_FEATURE_CHANGED:
                return "IMS_FEATURE_CHANGED";
            case ImsRILConstants.RIL_UNSOL_IMS_CONFIG_CONFIG_CHANGED:
                return "IMS_CONFIG_CHANGED";
            case ImsRILConstants.RIL_UNSOL_IMS_CONFIG_CONFIG_LOADED:
                return "IMS_CONFIG_LOADED";
            case ImsRILConstants.RIL_UNSOL_ON_VOLTE_SUBSCRIPTION:
                return "ImsRILConstants.RIL_UNSOL_ON_VOLTE_SUBSCRIPTION";
            case ImsRILConstants.RIL_UNSOL_IMS_DATA_INFO_NOTIFY:
                return "RIL_UNSOL_IMS_DATA_INFO_NOTIFY";
            case ImsRILConstants.RIL_UNSOL_RESPONSE_NEW_SMS_STATUS_REPORT_EX:
                return "RIL_UNSOL_RESPONSE_NEW_SMS_STATUS_REPORT_EX";
            case ImsRILConstants.RIL_UNSOL_RESPONSE_NEW_SMS_EX:
                return "RIL_UNSOL_RESPONSE_NEW_SMS_EX";
            case ImsRILConstants.RIL_UNSOL_RESPONSE_CDMA_NEW_SMS_EX:
                return "UNSOL_CDMA_NEW_SMS_EX";
            case ImsRILConstants.RIL_UNSOL_NO_EMERGENCY_CALLBACK_MODE:
                return "RIL_UNSOL_NO_EMERGENCY_CALLBACK_MODE";
            case ImsRILConstants.RIL_UNSOL_RTT_MODIFY_RESPONSE:
                return "RIL_UNSOL_RTT_MODIFY_RESPONSE";
            case ImsRILConstants.RIL_UNSOL_RTT_TEXT_RECEIVE:
                return "RIL_UNSOL_RTT_TEXT_RECEIVE";
            case ImsRILConstants.RIL_UNSOL_RTT_CAPABILITY_INDICATION:
                return "RIL_UNSOL_RTT_CAPABILITY_INDICATION";
            case ImsRILConstants.RIL_UNSOL_RTT_MODIFY_REQUEST_RECEIVE:
                return "RIL_UNSOL_RTT_MODIFY_REQUEST_RECEIVE";
            case ImsRILConstants.RIL_UNSOL_AUDIO_INDICATION:
                return "RIL_UNSOL_AUDIO_INDICATION";
            case ImsRILConstants.RIL_UNSOL_VOPS_INDICATION:
                return "RIL_UNSOL_VOPS_INDICATION";
            case ImsRILConstants.RIL_UNSOL_SIP_HEADER:
                return "RIL_UNSOL_SIP_HEADER";
            case ImsRILConstants.RIL_UNSOL_CALL_ADDITIONAL_INFO:
                return "RIL_UNSOL_CALL_ADDITIONAL_INFO";
            case ImsRILConstants.RIL_UNSOL_IMS_DIALOG_INDICATION:
                return "RIL_UNSOL_IMS_DIALOG_INDICATION";
            case ImsRILConstants.RIL_UNSOL_VIDEO_RINGTONE_EVENT_IND:
                return "RIL_UNSOL_VIDEO_RINGTONE_EVENT_IND";
            case ImsRILConstants.RIL_UNSOL_IMS_REG_FLAG_IND:
                return "RIL_UNSOL_IMS_REG_FLAG_IND";
            default:
                return "<unknown response>" + String.valueOf(response);
        }
    }

    static String requestToString(int request) {
        switch (request) {
            case 10:
                return "DIAL";
            case 12:
                return "HANGUP";
            case 15:
                return "SWITCH_WAITING_OR_HOLDING_AND_ACTIVE";
            case 16:
                return "CONFERENCE";
            case 18:
                return "LAST_CALL_FAIL_CAUSE";
            case 24:
                return "DTMF";
            case 31:
                return "GET_CLIR";
            case 32:
                return "SET_CLIR";
            case 33:
                return "QUERY_CALL_FORWARD_STATUS";
            case 34:
                return "SET_CALL_FORWARD";
            case 35:
                return "QUERY_CALL_WAITING";
            case 36:
                return "SET_CALL_WAITING";
            case 40:
                return "ANSWER";
            case 42:
                return "QUERY_FACILITY_LOCK";
            case 43:
                return "SET_FACILITY_LOCK";
            case 44:
                return "CHANGE_BARRING_PASSWORD";
            case 49:
                return "DTMF_START";
            case 50:
                return "DTMF_STOP";
            case 53:
                return "SET_MUTE";
            case 55:
                return "QUERY_CLIP";
            case 72:
                return "EXPLICIT_CALL_TRANSFER";
            case 99:
                return "EXIT_EMERGENCY_CALLBACK_MODE";
            case 129:
                return "SHUTDOWN";
            case ImsCallSessionProxy.CALL_INFO_MSG_TYPE_REMOTE_HOLD:
                return "GET_ACTIVITY_INFO";
            case 2016:
                return "RIL_REQUEST_SET_CALL_INDICATION";
            case 2019:
                return "HANGUP_ALL";
            case 2030:
                return "SET_ECC_LIST";
            case 2069:
                return "SET_IMS_ENABLE";
            case 2070:
                return "SET_VOLTE_ENABLE";
            case 2071:
                return "SET_WFC_ENABLE";
            case 2072:
                return "SET_VILTE_ENABLE";
            case 2073:
                return "SET_VIWIFI_ENABLE";
            case 2076:
                return "VIDEO_CALL_ACCEPT";
            case 2077:
                return "SET_IMSCFG";
            case 2078:
                return "GET_PROVISION_VALUE";
            case 2079:
                return "SET_PROVISION_VALUE";
            case 2080:
                return "IMS_BEARER_STATE_CONFIRM";
            case 2082:
                return "IMS_DEREG_NOTIFICATION";
            case 2083:
                return "IMS_ECT";
            case 2084:
                return "HOLD_CALL";
            case 2085:
                return "RESUME_CALL";
            case 2087:
                return "IMS_EMERGENCY_DIAL";
            case 2088:
                return "ImsRILConstants.RIL_REQUEST_SET_IMS_RTP_REPORT";
            case 2089:
                return "RIL_REQUEST_CONFERENCE_DIAL";
            case 2090:
                return "RIL_REQUEST_ADD_IMS_CONFERENCE_CALL_MEMBER";
            case 2091:
                return "RIL_REQUEST_REMOVE_IMS_CONFERENCE_CALL_MEMBER";
            case 2092:
                return "VT_DIAL_WITH_SIP_URI";
            case 2093:
                return "SEND_USSI";
            case 2094:
                return "CANCEL_USSI";
            case 2095:
                return "SET_WFC_PROFILE";
            case 2096:
                return "PULL_CALL";
            case 2097:
                return "SET_IMS_REGISTRATION_REPORT";
            case 2098:
                return "IMS_DIAL";
            case 2099:
                return "IMS_VT_DIAL";
            case 2103:
                return "SET_CLIP";
            case 2104:
                return "GET_COLP";
            case 2105:
                return "GET_COLR";
            case 2122:
                return "RIL_REQUEST_SET_VOICE_DOMAIN_PREFERENCE";
            case 2123:
                return "SET_COLP";
            case 2124:
                return "SET_COLR";
            case 2125:
                return "QUERY_CALL_FORWARD_IN_TIME_SLOT";
            case 2126:
                return "SET_CALL_FORWARD_IN_TIME_SLOT";
            case 2127:
                return "RIL_REQUEST_RUN_GBA";
            case 2128:
                return "ImsRILConstants.RIL_REQUEST_SET_MD_IMSCFG";
            case 2133:
                return "RIL_REQUEST_IMS_SEND_SMS_EX";
            case 2135:
                return "RIL_REQUEST_SET_IMS_BEARER_NOTIFICATION";
            case 2136:
                return "IMS_CONFIG_SET_FEATURE";
            case 2137:
                return "IMS_CONFIG_GET_FEATURE";
            case 2138:
                return "IMS_CONFIG_SET_PROVISION";
            case 2139:
                return "IMS_CONFIG_GET_PROVISION";
            case 2141:
                return "IMS_CONFIG_GET_RESOURCE_CAP";
            case 2163:
                return "RIL_REQUEST_GET_XCAP_STATUS";
            case 2164:
                return "RIL_REQUEST_RESET_SUPP_SERV";
            case 2166:
                return "RIL_REQUEST_SET_RCS_UA_ENABLE";
            case 2167:
                return "SETUP_XCAP_USER_AGENT_STRING";
            case 2170:
                return "RIL_REQUEST_SMS_ACKNOWLEDGE_EX";
            case 2172:
                return "CDMA_SMS_ACKNOWLEDGE_EX";
            case 2174:
                return "RIL_REQUEST_SET_RTT_MODE";
            case 2175:
                return "RIL_REQUEST_SEND_RTT_MODIFY_REQUEST";
            case 2176:
                return "RIL_REQUEST_SEND_RTT_TEXT";
            case 2177:
                return "RIL_REQUEST_RTT_MODIFY_REQUST_RESPONSE";
            case 2178:
                return "RIL_REQUEST_QUERY_VOPS_STATUS";
            case 2179:
                return "HANGUP_WITH_REASON";
            case 2180:
                return "RIL_REQUEST_SET_SIP_HEADER";
            case 2181:
                return "RIL_REQUEST_SIP_HEADER_REPORT";
            case 2182:
                return "RIL_REQUEST_SET_IMS_CALL_MODE";
            case 2183:
                return "RIL_REQUEST_QUERY_SSAC_STATUS";
            case 2184:
                return "RIL_REQUEST_TOGGLE_RTT_AUDIO_INDICATION";
            case 2185:
                return "ECC_REDIAL_APPROVE";
            case 2186:
                return "RIL_REQUEST_SET_CALL_ADDITIONAL_INFO";
            case 2187:
                return "RIL_REQUEST_VIDEO_RINGTONE_EVENT";
            default:
                return "<unknown request>: " + String.valueOf(request);
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
    public void riljLogi(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.i(IMSRIL_LOG_TAG, sb.toString());
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
        Rlog.d(IMSRIL_LOG_TAG, sb.toString());
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
        Rlog.e(IMSRIL_LOG_TAG, sb.toString());
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
        Rlog.e(IMSRIL_LOG_TAG, sb.toString(), e);
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
        Rlog.v(IMSRIL_LOG_TAG, sb.toString());
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

    @Deprecated
    public void accept() {
        accept((Message) null);
    }

    @Deprecated
    public void getProvisionValue(int phoneid, String provisionStr, Message response) {
        getProvisionValue(provisionStr, response);
    }

    @Deprecated
    public void setProvisionValue(int phoneid, String provisionStr, String provisionValue, Message response) {
        setProvisionValue(provisionStr, provisionValue, response);
    }

    @Deprecated
    public void sendWfcProfileInfo(int wfcPreference, int phoneid, Message response) {
        sendWfcProfileInfo(wfcPreference, response);
    }

    @Deprecated
    public void responseBearerStateConfirm(int phoneid, int aid, int action, int status) {
        responseBearerStateConfirm(aid, action, status, (Message) null);
    }

    @Deprecated
    public void setImsBearerNotification(int phoneid, int enable) {
        setImsBearerNotification(enable, (Message) null);
    }

    @Deprecated
    public void startConference(String[] participants, int clirMode, boolean isVideoCall, int phoneid, Message response) {
        startConference(participants, clirMode, isVideoCall, response);
    }

    @Deprecated
    public void getLastCallFailCause(int phoneid, Message response) {
        getLastCallFailCause(response);
    }

    @Deprecated
    public void setImsCfg(int[] params, int phoneid, Message response) {
        setImsCfg(params, response);
    }

    @Deprecated
    public void setImsRegistrationReport(int phoneid, Message response) {
        setImsRegistrationReport(response);
    }

    @Deprecated
    public void turnOffIms(int phoneid, Message response) {
        turnOffIms(response);
    }

    @Deprecated
    public void turnOnIms(int phoneid, Message response) {
        turnOnIms(response);
    }

    @Deprecated
    public void reject(int callId) {
        hangup(callId, (Message) null);
    }

    @Deprecated
    public void reject(int callId, int reason) {
        riljLog(" reject with reason: " + reason);
        hangup(callId, reason, (Message) null);
    }

    @Deprecated
    public void terminate(int callId) {
        hangup(callId, (Message) null);
    }

    @Deprecated
    public void terminate(int callId, int reason) {
        riljLog("terminate with reason: " + reason);
        hangup(callId, reason, (Message) null);
    }

    @Deprecated
    public void merge(Message response) {
        conference(response);
    }

    @Deprecated
    public void forceHangup(int callId) {
        forceHangup(callId, (Message) null);
    }

    @Deprecated
    public void forceHangup(int callId, int reason) {
        riljLog("forceHangup with reason: " + reason);
        forceHangup(callId, (Message) null);
    }

    @Deprecated
    public void acceptVideoCall(int videoMode, int callId) {
        acceptVideoCall(videoMode, callId, (Message) null);
    }

    @Deprecated
    public void setCallIndication(int mode, int callId, int seqNum, int cause) {
        setCallIndication(mode, callId, seqNum, cause, (Message) null);
    }

    /* access modifiers changed from: private */
    public void resetMtkProxyAndRequestList() {
        this.mMtkRadioProxy = null;
        this.mMtkRadioProxyCookie.incrementAndGet();
        setRadioState(2);
        RILRequest.resetSerial();
        clearRequestList(1, false);
    }

    private void handleProxyNotExist(Message result) {
        if (result != null) {
            AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
            result.sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public IBase getMtkRadioProxy(Message result) {
        if (!this.mIsMobileNetworkSupported) {
            handleProxyNotExist(result);
            return null;
        } else if (this.mMtkRadioProxy != null) {
            return this.mMtkRadioProxy;
        } else {
            if (this.mRilHandler.hasMessages(8) || this.mRilHandler.hasMessages(7)) {
                riljLogi("getMtkRadioProxy service died, we try again later");
                handleProxyNotExist(result);
                return null;
            }
            try {
                String[] strArr = MTK_IMS_HIDL_SERVICE_NAME;
                Integer num = this.mPhoneId;
                this.mMtkRadioProxy = IMtkRadioEx.getService(strArr[num == null ? 0 : num.intValue()], false);
            } catch (RemoteException | RuntimeException e) {
                this.mRadioProxy = null;
                riljLoge("getMtkRadioProxy getServiceV3_0: " + e);
            }
            if (this.mMtkRadioProxy != null) {
                try {
                    this.mMtkRadioVersion = MTK_RADIO_HAL_VERSION_3_0;
                    this.mMtkRadioProxy = IMtkRadioEx.castFrom(this.mMtkRadioProxy);
                    riljLogi("getMtkRadioProxy mMtkRadioVersion = " + this.mMtkRadioVersion);
                    if (this.mMtkRadioProxy != null) {
                        ((IMtkRadioEx) this.mMtkRadioProxy).linkToDeath(this.mMtkRadioProxyDeathRecipient, this.mMtkRadioProxyCookie.incrementAndGet());
                        ((IMtkRadioEx) this.mMtkRadioProxy).setResponseFunctionsIms(this.mImsRadioResponse, this.mImsRadioIndication);
                    } else {
                        riljLoge("getMtkRadioProxy setResponseFunctionsMtkV3_0: error, castFrom failed.");
                    }
                } catch (RemoteException | RuntimeException e2) {
                    this.mRadioProxy = null;
                    riljLoge("getMtkRadioProxy setResponseFunctionsMtkV3_0: " + e2);
                }
            } else {
                try {
                    String[] strArr2 = MTK_IMS_HIDL_SERVICE_NAME;
                    Integer num2 = this.mPhoneId;
                    this.mMtkRadioProxy = vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx.getService(strArr2[num2 == null ? 0 : num2.intValue()], false);
                } catch (RemoteException | RuntimeException e3) {
                    this.mMtkRadioProxy = null;
                    riljLoge("getMtkRadioProxy getServiceV2_0: " + e3);
                }
                if (this.mMtkRadioProxy != null) {
                    if (this.mImsRadioResponseV2 == null) {
                        this.mImsRadioResponseV2 = new ImsRadioResponseV2(this, this.mPhoneId.intValue());
                    }
                    if (this.mImsRadioIndicationV2 == null) {
                        this.mImsRadioIndicationV2 = new ImsRadioIndicationV2(this, this.mPhoneId.intValue());
                    }
                    try {
                        if (vendor.mediatek.hardware.mtkradioex.V2_2.IMtkRadioEx.castFrom(this.mMtkRadioProxy) != null) {
                            this.mMtkRadioProxy = vendor.mediatek.hardware.mtkradioex.V2_2.IMtkRadioEx.castFrom(this.mMtkRadioProxy);
                            this.mMtkRadioVersion = MTK_RADIO_HAL_VERSION_2_2;
                        } else if (vendor.mediatek.hardware.mtkradioex.V2_1.IMtkRadioEx.castFrom(this.mMtkRadioProxy) != null) {
                            this.mMtkRadioProxy = vendor.mediatek.hardware.mtkradioex.V2_1.IMtkRadioEx.castFrom(this.mMtkRadioProxy);
                            this.mMtkRadioVersion = MTK_RADIO_HAL_VERSION_2_1;
                        } else {
                            this.mMtkRadioProxy = vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx.castFrom(this.mMtkRadioProxy);
                            this.mMtkRadioVersion = MTK_RADIO_HAL_VERSION_2_0;
                        }
                        riljLogi("getMtkRadioProxy mMtkRadioVersion = " + this.mMtkRadioVersion);
                        if (this.mMtkRadioProxy != null) {
                            ((IMtkRadioEx) this.mMtkRadioProxy).linkToDeath(this.mMtkRadioProxyDeathRecipient, this.mMtkRadioProxyCookie.incrementAndGet());
                            ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) this.mMtkRadioProxy).setResponseFunctionsIms(this.mImsRadioResponseV2, this.mImsRadioIndicationV2);
                        } else {
                            riljLoge("getMtkRadioProxy setResponseFunctionsV2: error, castFrom failed.");
                        }
                    } catch (RemoteException | RuntimeException e4) {
                        this.mMtkRadioProxy = null;
                        riljLoge("getMtkRadioProxy setResponseFunctionsV2: " + e4);
                    }
                }
            }
            if (this.mMtkRadioProxy == null && result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return this.mMtkRadioProxy;
        }
    }

    public void queryVopsStatus(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2178, result, this.mRILDefaultWorkSource);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).queryVopsStatus(rr.mSerial);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).queryVopsStatus(rr.mSerial);
                }
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "queryVopsStatus", e);
            }
        }
    }

    public void notifyImsServiceReady() {
        IRadio proxy = getRadioProxy((Message) null);
        IBase radioProxy = getMtkRadioProxy((Message) null);
        if (proxy == null || radioProxy == null) {
            riljLog("notify fail, send EVENT_TRIGGER_TO_FIRE_PENDING_URC " + this.mPhoneId);
            this.mRilHandler.removeMessages(8);
            RilHandler rilHandler = this.mRilHandler;
            rilHandler.sendMessageDelayed(rilHandler.obtainMessage(8), 1000);
            return;
        }
        try {
            if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                ((IMtkRadioEx) radioProxy).notifyImsServiceReady();
            } else {
                ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).notifyImsServiceReady();
            }
        } catch (RemoteException | RuntimeException e) {
            riljLoge("notifyImsServiceReady error: " + e);
        }
    }

    public void querySsacStatus(Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2183, result, this.mRILDefaultWorkSource);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).querySsacStatus(rr.mSerial);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).querySsacStatus(rr.mSerial);
                }
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "queryVopsStatus", e);
            }
        }
    }

    public void toggleRttAudioIndication(int callId, int enable, Message response) {
        IBase radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2184, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " callId = " + callId + " enable = " + enable);
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).toggleRttAudioIndication(rr.mSerial, callId, enable);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).toggleRttAudioIndication(rr.mSerial, callId, enable);
                }
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "toggleRttAudioIndication", e);
            }
        }
    }

    public void setCallAdditionalInfo(ArrayList<String> info, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2186, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).setCallAdditionalInfo(rr.mSerial, info);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_0.IMtkRadioEx) radioProxy).setCallAdditionalInfo(rr.mSerial, info);
                }
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "setCallAdditionalInfo", e);
            }
        }
    }

    public void videoRingtoneEventRequest(ArrayList<String> event, Message result) {
        IBase radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2187, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                if (this.mMtkRadioVersion.greaterOrEqual(MTK_RADIO_HAL_VERSION_3_0)) {
                    ((IMtkRadioEx) radioProxy).videoRingtoneEventRequest(rr.mSerial, event);
                } else {
                    ((vendor.mediatek.hardware.mtkradioex.V2_2.IMtkRadioEx) radioProxy).videoRingtoneEventRequest(rr.mSerial, event);
                }
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "videoRingtoneEventRequest", e);
            }
        }
    }
}
