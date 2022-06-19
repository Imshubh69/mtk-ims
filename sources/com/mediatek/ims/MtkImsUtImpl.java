package com.mediatek.ims;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.telephony.ims.ImsReasonInfo;
import android.util.Log;
import com.android.ims.internal.IImsUt;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CommandException;
import com.mediatek.ims.feature.MtkImsUtImplBase;
import com.mediatek.ims.feature.MtkImsUtListener;
import com.mediatek.ims.internal.ImsXuiManager;
import com.mediatek.ims.plugin.ExtensionFactory;
import com.mediatek.ims.plugin.ImsSSExtPlugin;
import com.mediatek.ims.plugin.ImsSSOemPlugin;
import com.mediatek.ims.ril.ImsCommandsInterface;
import com.mediatek.internal.telephony.MtkCallForwardInfo;
import java.util.HashMap;

public class MtkImsUtImpl extends MtkImsUtImplBase {
    private static final boolean DBG = true;
    static final int IMS_UT_EVENT_GET_CF_TIME_SLOT = 1200;
    static final int IMS_UT_EVENT_GET_CF_WITH_CLASS = 1204;
    static final int IMS_UT_EVENT_SETUP_XCAP_USER_AGENT_STRING = 1203;
    static final int IMS_UT_EVENT_SET_CB_WITH_PWD = 1202;
    static final int IMS_UT_EVENT_SET_CF_TIME_SLOT = 1201;
    private static final String TAG = "MtkImsUtImpl";
    private static final Object mLock = new Object();
    private static HashMap<Integer, MtkImsUtImpl> sMtkImsUtImpls = new HashMap<>();
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public ImsSSExtPlugin mExtPluginBase;
    private ResultHandler mHandler;
    private ImsCommandsInterface mImsRILAdapter;
    private ImsService mImsService = null;
    /* access modifiers changed from: private */
    public ImsUtImpl mImsUtImpl = null;
    /* access modifiers changed from: private */
    public MtkImsUtListener mListener = null;
    /* access modifiers changed from: private */
    public ImsSSOemPlugin mOemPluginBase;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;

    public static MtkImsUtImpl getInstance(Context context, int phoneId, ImsService service) {
        synchronized (sMtkImsUtImpls) {
            if (sMtkImsUtImpls.containsKey(Integer.valueOf(phoneId))) {
                MtkImsUtImpl m = sMtkImsUtImpls.get(Integer.valueOf(phoneId));
                return m;
            }
            sMtkImsUtImpls.put(Integer.valueOf(phoneId), new MtkImsUtImpl(context, phoneId, service));
            MtkImsUtImpl mtkImsUtImpl = sMtkImsUtImpls.get(Integer.valueOf(phoneId));
            return mtkImsUtImpl;
        }
    }

    public static MtkImsUtImpl getInstance(int phoneId) {
        synchronized (sMtkImsUtImpls) {
            if (!sMtkImsUtImpls.containsKey(Integer.valueOf(phoneId))) {
                return null;
            }
            MtkImsUtImpl m = sMtkImsUtImpls.get(Integer.valueOf(phoneId));
            return m;
        }
    }

    private MtkImsUtImpl(Context context, int phoneId, ImsService imsService) {
        this.mContext = context;
        this.mImsUtImpl = ImsUtImpl.getInstance(context, phoneId, imsService);
        HandlerThread thread = new HandlerThread("MtkImsUtImplResult");
        thread.start();
        this.mHandler = new ResultHandler(thread.getLooper());
        this.mImsService = imsService;
        this.mImsRILAdapter = imsService.getImsRILAdapter(phoneId);
        this.mPhoneId = phoneId;
        this.mOemPluginBase = ExtensionFactory.makeOemPluginFactory(this.mContext).makeImsSSOemPlugin(this.mContext);
        this.mExtPluginBase = ExtensionFactory.makeExtensionPluginFactory(this.mContext).makeImsSSExtPlugin(this.mContext);
    }

    public void setListener(MtkImsUtListener listener) {
        this.mListener = listener;
    }

    public IImsUt getUtInterface(int PhoneId) {
        return this.mImsUtImpl.getInterface();
    }

    public String getUtIMPUFromNetwork() {
        Log.d(TAG, "getUtIMPUFromNetwork(): phoneId = " + this.mPhoneId);
        return ImsXuiManager.getInstance().getXui(this.mPhoneId);
    }

    public void setupXcapUserAgentString(String userAgent) {
        if (userAgent != null) {
            Log.d(TAG, "setupXcapUserAgentString(): userAgent = " + userAgent);
            this.mImsRILAdapter.setupXcapUserAgentString(userAgent, this.mHandler.obtainMessage(1203));
            return;
        }
        Log.e(TAG, "setupXcapUserAgentString(): userAgent is null");
    }

    public int queryCallForwardInTimeSlot(int condition) {
        int requestId;
        synchronized (mLock) {
            requestId = ImsUtImpl.getAndIncreaseRequestId();
        }
        Log.d(TAG, "queryCallForwardInTimeSlot(): requestId = " + requestId);
        this.mImsRILAdapter.queryCallForwardInTimeSlotStatus(this.mImsUtImpl.getCFReasonFromCondition(condition), 1, this.mHandler.obtainMessage(IMS_UT_EVENT_GET_CF_TIME_SLOT, requestId, 0, (Object) null));
        return requestId;
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004c, code lost:
        r0 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int updateCallForwardInTimeSlot(int r15, int r16, java.lang.String r17, int r18, long[] r19) {
        /*
            r14 = this;
            r1 = r14
            java.lang.Object r2 = mLock
            monitor-enter(r2)
            int r0 = com.mediatek.ims.ImsUtImpl.getAndIncreaseRequestId()     // Catch:{ all -> 0x0046 }
            monitor-exit(r2)     // Catch:{ all -> 0x0046 }
            java.lang.String r2 = "MtkImsUtImpl"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "updateCallForwardInTimeSlot(): requestId = "
            r3.append(r4)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            android.util.Log.d(r2, r3)
            com.mediatek.ims.MtkImsUtImpl$ResultHandler r2 = r1.mHandler
            r3 = 1201(0x4b1, float:1.683E-42)
            r4 = 0
            r5 = 0
            android.os.Message r2 = r2.obtainMessage(r3, r0, r4, r5)
            com.mediatek.ims.ril.ImsCommandsInterface r6 = r1.mImsRILAdapter
            com.mediatek.ims.ImsUtImpl r3 = r1.mImsUtImpl
            r4 = r15
            int r7 = r3.getCFActionFromAction(r15)
            com.mediatek.ims.ImsUtImpl r3 = r1.mImsUtImpl
            r5 = r16
            int r8 = r3.getCFReasonFromCondition(r5)
            r9 = 1
            r10 = r17
            r11 = r18
            r12 = r19
            r13 = r2
            r6.setCallForwardInTimeSlot(r7, r8, r9, r10, r11, r12, r13)
            return r0
        L_0x0046:
            r0 = move-exception
            r4 = r15
            r5 = r16
        L_0x004a:
            monitor-exit(r2)     // Catch:{ all -> 0x004c }
            throw r0
        L_0x004c:
            r0 = move-exception
            goto L_0x004a
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.MtkImsUtImpl.updateCallForwardInTimeSlot(int, int, java.lang.String, int, long[]):int");
    }

    public boolean isSupportCFT() {
        return this.mImsService.isSupportCFT(this.mPhoneId);
    }

    public int updateCallBarringForServiceClass(String password, int cbType, int enable, String[] barrList, int serviceClass) {
        int requestId;
        synchronized (mLock) {
            requestId = ImsUtImpl.getAndIncreaseRequestId();
        }
        Log.d(TAG, "updateCallBarringForServiceClass(): requestId = " + requestId);
        this.mImsRILAdapter.setFacilityLock(getFacilityFromCBType(cbType), enable == 1, password, serviceClass, this.mHandler.obtainMessage(1202, requestId, 0, (Object) null));
        return requestId;
    }

    public void explicitCallTransfer(Message result, Messenger target) {
        this.mImsService.explicitCallTransfer(this.mPhoneId, result, target);
    }

    public String getXcapConflictErrorMessage() {
        return this.mOemPluginBase.getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_25, this.mPhoneId);
    }

    public int queryCFForServiceClass(int condition, String number, int serviceClass) {
        int requestId;
        synchronized (mLock) {
            requestId = ImsUtImpl.getAndIncreaseRequestId();
        }
        Log.d(TAG, "queryCFForServiceClass(): requestId = " + requestId);
        this.mImsRILAdapter.queryCallForwardStatus(this.mImsUtImpl.getCFReasonFromCondition(condition), serviceClass, number, this.mHandler.obtainMessage(IMS_UT_EVENT_GET_CF_WITH_CLASS, requestId, 0, (Object) null));
        return requestId;
    }

    private class ResultHandler extends Handler {
        public ResultHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            ImsReasonInfo reason;
            ImsReasonInfo reason2;
            ImsReasonInfo reason3;
            ImsReasonInfo reason4;
            Log.d(MtkImsUtImpl.TAG, "handleMessage(): event = " + msg.what + ", requestId = " + msg.arg1 + ", mListener=" + MtkImsUtImpl.this.mListener);
            SuppSrvConfig instance = SuppSrvConfig.getInstance(MtkImsUtImpl.this.mContext);
            switch (msg.what) {
                case MtkImsUtImpl.IMS_UT_EVENT_GET_CF_TIME_SLOT /*1200*/:
                    if (MtkImsUtImpl.this.mListener != null) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            MtkCallForwardInfo[] cfInfo = (MtkCallForwardInfo[]) ar.result;
                            MtkImsCallForwardInfo[] imsCfInfo = null;
                            if (cfInfo != null) {
                                imsCfInfo = new MtkImsCallForwardInfo[cfInfo.length];
                                for (int i = 0; i < cfInfo.length; i++) {
                                    MtkImsCallForwardInfo info = new MtkImsCallForwardInfo();
                                    ImsUtImpl unused = MtkImsUtImpl.this.mImsUtImpl;
                                    info.mCondition = ImsUtImpl.getConditionFromCFReason(cfInfo[i].reason);
                                    info.mStatus = cfInfo[i].status;
                                    info.mServiceClass = cfInfo[i].serviceClass;
                                    info.mToA = cfInfo[i].toa;
                                    info.mNumber = cfInfo[i].number;
                                    info.mTimeSeconds = cfInfo[i].timeSeconds;
                                    info.mTimeSlot = cfInfo[i].timeSlot;
                                    imsCfInfo[i] = info;
                                }
                            }
                            MtkImsUtImpl.this.mListener.onUtConfigurationCallForwardInTimeSlotQueried(msg.arg1, imsCfInfo);
                            return;
                        }
                        if (ar.exception instanceof CommandException) {
                            reason = MtkImsUtImpl.this.mOemPluginBase.commandExceptionToReason(ar.exception, MtkImsUtImpl.this.mPhoneId);
                        } else {
                            reason = new ImsReasonInfo(804, 0);
                        }
                        MtkImsUtImpl.this.mImsUtImpl.notifyUtConfigurationQueryFailed(msg, reason);
                        return;
                    }
                    return;
                case 1201:
                    if (MtkImsUtImpl.this.mListener != null) {
                        AsyncResult ar2 = (AsyncResult) msg.obj;
                        if (ar2.exception == null) {
                            Log.d(MtkImsUtImpl.TAG, "utConfigurationUpdated(): event = " + msg.what);
                            MtkImsUtImpl.this.mImsUtImpl.notifyUtConfigurationUpdated(msg);
                            return;
                        }
                        if (ar2.exception instanceof CommandException) {
                            reason2 = MtkImsUtImpl.this.mOemPluginBase.commandExceptionToReason(ar2.exception, MtkImsUtImpl.this.mPhoneId);
                        } else {
                            reason2 = new ImsReasonInfo(804, 0);
                        }
                        MtkImsUtImpl.this.mImsUtImpl.notifyUtConfigurationUpdateFailed(msg, reason2);
                        return;
                    }
                    return;
                case 1202:
                    if (MtkImsUtImpl.this.mListener != null) {
                        AsyncResult ar3 = (AsyncResult) msg.obj;
                        if (ar3.exception == null) {
                            Log.d(MtkImsUtImpl.TAG, "utConfigurationUpdated(): event = " + msg.what);
                            MtkImsUtImpl.this.mImsUtImpl.notifyUtConfigurationUpdated(msg);
                            return;
                        }
                        if (ar3.exception instanceof CommandException) {
                            reason3 = MtkImsUtImpl.this.mOemPluginBase.commandExceptionToReason(ar3.exception, MtkImsUtImpl.this.mPhoneId);
                        } else {
                            reason3 = new ImsReasonInfo(804, 0);
                        }
                        MtkImsUtImpl.this.mImsUtImpl.notifyUtConfigurationUpdateFailed(msg, reason3);
                        return;
                    }
                    return;
                case 1203:
                    if (MtkImsUtImpl.this.mListener == null) {
                        return;
                    }
                    if (((AsyncResult) msg.obj).exception == null) {
                        Log.d(MtkImsUtImpl.TAG, "Execute setupXcapUserAgentString succeed!event = " + msg.what);
                        return;
                    }
                    Log.e(MtkImsUtImpl.TAG, "Execute setupXcapUserAgentString failed!event = " + msg.what);
                    return;
                case MtkImsUtImpl.IMS_UT_EVENT_GET_CF_WITH_CLASS /*1204*/:
                    if (MtkImsUtImpl.this.mListener != null) {
                        AsyncResult ar4 = (AsyncResult) msg.obj;
                        if (ar4.exception == null) {
                            MtkImsUtImpl.this.mListener.onUtConfigurationCallForwardQueried(msg.arg1, MtkImsUtImpl.this.mExtPluginBase.getImsCallForwardInfo((CallForwardInfo[]) ar4.result));
                            return;
                        }
                        if (ar4.exception instanceof CommandException) {
                            reason4 = MtkImsUtImpl.this.mOemPluginBase.commandExceptionToReason(ar4.exception, MtkImsUtImpl.this.mPhoneId);
                        } else {
                            reason4 = new ImsReasonInfo(804, 0);
                        }
                        MtkImsUtImpl.this.mImsUtImpl.notifyUtConfigurationQueryFailed(msg, reason4);
                        return;
                    }
                    return;
                default:
                    Log.d(MtkImsUtImpl.TAG, "Unknown Event: " + msg.what);
                    return;
            }
        }
    }

    private String getFacilityFromCBType(int cbType) {
        switch (cbType) {
            case 1:
                return "AI";
            case 2:
                return "AO";
            case 3:
                return "OI";
            case 4:
                return "OX";
            case 5:
                return "IR";
            case 6:
                return "ACR";
            case 7:
                return "AB";
            case 8:
                return "AG";
            case 9:
                return "AC";
            case 10:
                return "BS_MT";
            default:
                return null;
        }
    }
}
