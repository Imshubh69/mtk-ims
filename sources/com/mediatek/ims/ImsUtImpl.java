package com.mediatek.ims;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.ims.ImsCallForwardInfo;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsSsInfo;
import android.telephony.ims.ImsUtListener;
import android.telephony.ims.stub.ImsUtImplBase;
import android.util.Log;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CommandException;
import com.mediatek.ims.plugin.ExtensionFactory;
import com.mediatek.ims.plugin.ImsSSExtPlugin;
import com.mediatek.ims.plugin.ImsSSOemPlugin;
import com.mediatek.ims.ril.ImsCommandsInterface;
import com.mediatek.internal.telephony.MtkCallForwardInfo;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.TimeZone;

public class ImsUtImpl extends ImsUtImplBase {
    private static final boolean DBG = true;
    private static final int DEFAULT_INVALID_PHONE_ID = -1;
    static final int IMS_UT_EVENT_GET_CB = 1000;
    static final int IMS_UT_EVENT_GET_CF = 1001;
    static final int IMS_UT_EVENT_GET_CF_TIME_SLOT = 1200;
    static final int IMS_UT_EVENT_GET_CLIP = 1004;
    static final int IMS_UT_EVENT_GET_CLIR = 1003;
    static final int IMS_UT_EVENT_GET_COLP = 1006;
    static final int IMS_UT_EVENT_GET_COLR = 1005;
    static final int IMS_UT_EVENT_GET_CW = 1002;
    static final int IMS_UT_EVENT_SET_CB = 1007;
    static final int IMS_UT_EVENT_SET_CF = 1008;
    static final int IMS_UT_EVENT_SET_CF_TIME_SLOT = 1201;
    static final int IMS_UT_EVENT_SET_CLIP = 1011;
    static final int IMS_UT_EVENT_SET_CLIR = 1010;
    static final int IMS_UT_EVENT_SET_COLP = 1013;
    static final int IMS_UT_EVENT_SET_COLR = 1012;
    static final int IMS_UT_EVENT_SET_CW = 1009;
    private static final boolean SDBG = (SystemProperties.get("ro.build.type").equals("user") ? false : DBG);
    private static final String SS_SERVICE_CLASS_PROP = "vendor.gsm.radio.ss.sc";
    private static final String TAG = "ImsUtImpl";
    static final int TIME_VALUE_HOUR_MAX = 23;
    static final int TIME_VALUE_MIN = 0;
    static final int TIME_VALUE_MINUTE_MAX = 59;
    private static final Object mLock = new Object();
    private static HashMap<Integer, ImsUtImpl> sImsUtImpls = new HashMap<>();
    private static int sRequestId = 0;
    private Context mContext;
    /* access modifiers changed from: private */
    public ImsSSExtPlugin mExtPluginBase;
    private ResultHandler mHandler;
    private ImsCommandsInterface mImsRILAdapter;
    private ImsService mImsService = null;
    /* access modifiers changed from: private */
    public ImsUtListener mListener = null;
    /* access modifiers changed from: private */
    public ImsSSOemPlugin mOemPluginBase;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;

    public static ImsUtImpl getInstance(Context context, int phoneId, ImsService service) {
        synchronized (sImsUtImpls) {
            if (sImsUtImpls.containsKey(Integer.valueOf(phoneId))) {
                ImsUtImpl m = sImsUtImpls.get(Integer.valueOf(phoneId));
                return m;
            }
            sImsUtImpls.put(Integer.valueOf(phoneId), new ImsUtImpl(context, phoneId, service));
            ImsUtImpl imsUtImpl = sImsUtImpls.get(Integer.valueOf(phoneId));
            return imsUtImpl;
        }
    }

    private ImsUtImpl(Context context, int phoneId, ImsService imsService) {
        this.mContext = context;
        HandlerThread thread = new HandlerThread("ImsUtImplResult");
        thread.start();
        this.mHandler = new ResultHandler(thread.getLooper());
        this.mImsService = imsService;
        this.mImsRILAdapter = imsService.getImsRILAdapter(phoneId);
        this.mPhoneId = phoneId;
        this.mOemPluginBase = ExtensionFactory.makeOemPluginFactory(this.mContext).makeImsSSOemPlugin(this.mContext);
        this.mExtPluginBase = ExtensionFactory.makeExtensionPluginFactory(this.mContext).makeImsSSExtPlugin(this.mContext);
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
            ImsReasonInfo reason5;
            ImsReasonInfo reason6;
            ImsReasonInfo reason7;
            ImsReasonInfo reason8;
            Log.d(ImsUtImpl.TAG, "handleMessage(): event = " + msg.what + ", requestId = " + msg.arg1 + ", mListener=" + ImsUtImpl.this.mListener);
            switch (msg.what) {
                case 1000:
                    if (ImsUtImpl.this.mListener != null) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            int[] result = (int[]) ar.result;
                            ImsSsInfo[] info = {new ImsSsInfo()};
                            info[0].mStatus = result[0];
                            Log.d(ImsUtImpl.TAG, "IMS_UT_EVENT_GET_CB: status = " + result[0]);
                            ImsUtImpl.this.mListener.onUtConfigurationCallBarringQueried(msg.arg1, info);
                            return;
                        }
                        if (ar.exception instanceof CommandException) {
                            reason = ImsUtImpl.this.mOemPluginBase.commandExceptionToReason(ar.exception, ImsUtImpl.this.mPhoneId);
                        } else {
                            reason = new ImsReasonInfo(804, 0);
                        }
                        ImsUtImpl.this.mListener.onUtConfigurationQueryFailed(msg.arg1, reason);
                        return;
                    }
                    return;
                case 1001:
                    if (ImsUtImpl.this.mListener != null) {
                        AsyncResult ar2 = (AsyncResult) msg.obj;
                        if (ar2.exception == null) {
                            ImsUtImpl.this.mListener.onUtConfigurationCallForwardQueried(msg.arg1, ImsUtImpl.this.mExtPluginBase.getImsCallForwardInfo((CallForwardInfo[]) ar2.result));
                            return;
                        }
                        if (ar2.exception instanceof CommandException) {
                            reason2 = ImsUtImpl.this.mOemPluginBase.commandExceptionToReason(ar2.exception, ImsUtImpl.this.mPhoneId);
                        } else {
                            reason2 = new ImsReasonInfo(804, 0);
                        }
                        ImsUtImpl.this.mListener.onUtConfigurationQueryFailed(msg.arg1, reason2);
                        return;
                    }
                    return;
                case 1002:
                    if (ImsUtImpl.this.mListener != null) {
                        AsyncResult ar3 = (AsyncResult) msg.obj;
                        if (ar3.exception == null) {
                            int[] result2 = (int[]) ar3.result;
                            ImsSsInfo[] info2 = {new ImsSsInfo()};
                            info2[0].mStatus = result2[0];
                            Log.d(ImsUtImpl.TAG, "IMS_UT_EVENT_GET_CW: status = " + result2[0]);
                            ImsUtImpl.this.mListener.onUtConfigurationCallWaitingQueried(msg.arg1, info2);
                            return;
                        }
                        if (ar3.exception instanceof CommandException) {
                            reason3 = ImsUtImpl.this.mOemPluginBase.commandExceptionToReason(ar3.exception, ImsUtImpl.this.mPhoneId);
                        } else {
                            reason3 = new ImsReasonInfo(804, 0);
                        }
                        ImsUtImpl.this.mListener.onUtConfigurationQueryFailed(msg.arg1, reason3);
                        return;
                    }
                    return;
                case 1003:
                    if (ImsUtImpl.this.mListener != null) {
                        AsyncResult ar4 = (AsyncResult) msg.obj;
                        if (ar4.exception == null) {
                            int[] result3 = (int[]) ar4.result;
                            if (result3 == null || result3.length != 2) {
                                Log.e(ImsUtImpl.TAG, "IMS_UT_EVENT_GET_CLIR: Something funny going on");
                                return;
                            }
                            Log.d(ImsUtImpl.TAG, "UT GET CLIR result = " + result3);
                            ImsUtImpl.this.mListener.onLineIdentificationSupplementaryServiceResponse(msg.arg1, new ImsSsInfo.Builder(-1).setClirOutgoingState(result3[0]).setClirInterrogationStatus(result3[1]).build());
                            return;
                        }
                        if (ar4.exception instanceof CommandException) {
                            reason4 = ImsUtImpl.this.mOemPluginBase.commandExceptionToReason(ar4.exception, ImsUtImpl.this.mPhoneId);
                        } else {
                            reason4 = new ImsReasonInfo(804, 0);
                        }
                        ImsUtImpl.this.mListener.onUtConfigurationQueryFailed(msg.arg1, reason4);
                        return;
                    }
                    return;
                case 1004:
                case 1005:
                case 1006:
                    if (ImsUtImpl.this.mListener != null) {
                        AsyncResult ar5 = (AsyncResult) msg.obj;
                        if (ar5.exception == null) {
                            ImsSsInfo ssInfo = new ImsSsInfo();
                            ssInfo.mStatus = ((int[]) ar5.result)[0];
                            Bundle info3 = new Bundle();
                            info3.putParcelable("imsSsInfo", ssInfo);
                            ImsUtImpl.this.mListener.onUtConfigurationQueried(msg.arg1, info3);
                            return;
                        }
                        if (ar5.exception instanceof CommandException) {
                            reason5 = ImsUtImpl.this.mOemPluginBase.commandExceptionToReason(ar5.exception, ImsUtImpl.this.mPhoneId);
                        } else {
                            reason5 = new ImsReasonInfo(804, 0);
                        }
                        ImsUtImpl.this.mListener.onUtConfigurationQueryFailed(msg.arg1, reason5);
                        return;
                    }
                    return;
                case 1007:
                case 1008:
                    if (ImsUtImpl.this.mListener != null) {
                        AsyncResult ar6 = (AsyncResult) msg.obj;
                        if (ar6.exception == null && ar6.result != null && (ar6.result instanceof CallForwardInfo[])) {
                            CallForwardInfo[] cfInfo = (CallForwardInfo[]) ar6.result;
                            ImsCallForwardInfo[] imsCfInfo = null;
                            if (!(cfInfo == null || cfInfo.length == 0)) {
                                imsCfInfo = new ImsCallForwardInfo[cfInfo.length];
                                for (int i = 0; i < cfInfo.length; i++) {
                                    imsCfInfo[i] = ImsUtImpl.this.getImsCallForwardInfo(cfInfo[i]);
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("IMS_UT_EVENT_SET_CF: cfInfo[");
                                    sb.append(i);
                                    sb.append("] = , Condition: ");
                                    sb.append(imsCfInfo[i].getCondition());
                                    sb.append(", Status: ");
                                    sb.append(imsCfInfo[i].getStatus() == 0 ? "disabled" : "enabled");
                                    sb.append(", ToA: ");
                                    sb.append(imsCfInfo[i].getToA());
                                    sb.append(", Service Class: ");
                                    sb.append(imsCfInfo[i].getServiceClass());
                                    sb.append(", Number=");
                                    sb.append(ImsUtImpl.encryptString(imsCfInfo[i].getNumber()));
                                    sb.append(", Time (seconds): ");
                                    sb.append(imsCfInfo[i].getTimeSeconds());
                                    Log.d(ImsUtImpl.TAG, sb.toString());
                                }
                            }
                            ImsUtImpl.this.mListener.onUtConfigurationCallForwardQueried(msg.arg1, imsCfInfo);
                            return;
                        }
                    }
                    break;
                case 1009:
                case 1010:
                case 1011:
                case 1012:
                case 1013:
                    break;
                case ImsUtImpl.IMS_UT_EVENT_GET_CF_TIME_SLOT /*1200*/:
                    if (ImsUtImpl.this.mListener != null) {
                        AsyncResult ar7 = (AsyncResult) msg.obj;
                        if (ar7.exception == null) {
                            MtkCallForwardInfo[] cfInfo2 = (MtkCallForwardInfo[]) ar7.result;
                            ImsCallForwardInfo[] imsCfInfo2 = null;
                            if (cfInfo2 != null) {
                                imsCfInfo2 = new ImsCallForwardInfo[cfInfo2.length];
                                for (int i2 = 0; i2 < cfInfo2.length; i2++) {
                                    ImsCallForwardInfo info4 = new ImsCallForwardInfo();
                                    info4.mCondition = ImsUtImpl.getConditionFromCFReason(cfInfo2[i2].reason);
                                    info4.mStatus = cfInfo2[i2].status;
                                    info4.mServiceClass = cfInfo2[i2].serviceClass;
                                    info4.mToA = cfInfo2[i2].toa;
                                    info4.mNumber = cfInfo2[i2].number;
                                    info4.mTimeSeconds = cfInfo2[i2].timeSeconds;
                                    imsCfInfo2[i2] = info4;
                                }
                            }
                            ImsUtImpl.this.mListener.onUtConfigurationCallForwardQueried(msg.arg1, imsCfInfo2);
                            return;
                        }
                        if (ar7.exception instanceof CommandException) {
                            reason7 = ImsUtImpl.this.mOemPluginBase.commandExceptionToReason(ar7.exception, ImsUtImpl.this.mPhoneId);
                        } else {
                            reason7 = new ImsReasonInfo(804, 0);
                        }
                        ImsUtImpl.this.mListener.onUtConfigurationQueryFailed(msg.arg1, reason7);
                        return;
                    }
                    return;
                case 1201:
                    if (ImsUtImpl.this.mListener != null) {
                        AsyncResult ar8 = (AsyncResult) msg.obj;
                        if (ar8.exception == null) {
                            Log.d(ImsUtImpl.TAG, "utConfigurationUpdated(): event = " + msg.what);
                            ImsUtImpl.this.notifyUtConfigurationUpdated(msg);
                            return;
                        }
                        if (ar8.exception instanceof CommandException) {
                            reason8 = ImsUtImpl.this.mOemPluginBase.commandExceptionToReason(ar8.exception, ImsUtImpl.this.mPhoneId);
                        } else {
                            reason8 = new ImsReasonInfo(804, 0);
                        }
                        ImsUtImpl.this.notifyUtConfigurationUpdateFailed(msg, reason8);
                        return;
                    }
                    return;
                default:
                    Log.d(ImsUtImpl.TAG, "Unknown Event: " + msg.what);
                    return;
            }
            if (ImsUtImpl.this.mListener != null) {
                AsyncResult ar9 = (AsyncResult) msg.obj;
                if (ar9.exception == null) {
                    Log.d(ImsUtImpl.TAG, "utConfigurationUpdated(): event = " + msg.what);
                    ImsUtImpl.this.mListener.onUtConfigurationUpdated(msg.arg1);
                    return;
                }
                if (ar9.exception instanceof CommandException) {
                    reason6 = ImsUtImpl.this.mOemPluginBase.commandExceptionToReason(ar9.exception, ImsUtImpl.this.mPhoneId);
                } else {
                    reason6 = new ImsReasonInfo(804, 0);
                }
                ImsUtImpl.this.mListener.onUtConfigurationUpdateFailed(msg.arg1, reason6);
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

    public int getCFActionFromAction(int cfAction) {
        switch (cfAction) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 3:
                return 3;
            case 4:
                return 4;
            default:
                return 0;
        }
    }

    public int getCFReasonFromCondition(int condition) {
        switch (condition) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            default:
                return 3;
        }
    }

    public static int getConditionFromCFReason(int reason) {
        switch (reason) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            default:
                return -1;
        }
    }

    /* access modifiers changed from: private */
    public ImsCallForwardInfo getImsCallForwardInfo(CallForwardInfo info) {
        ImsCallForwardInfo imsCfInfo = new ImsCallForwardInfo();
        imsCfInfo.mCondition = getConditionFromCFReason(info.reason);
        imsCfInfo.mStatus = info.status;
        imsCfInfo.mServiceClass = info.serviceClass;
        imsCfInfo.mToA = info.toa;
        imsCfInfo.mNumber = info.number;
        imsCfInfo.mTimeSeconds = info.timeSeconds;
        return imsCfInfo;
    }

    public int queryCallBarring(int cbType) {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "queryCallBarring(): requestId = " + requestId);
        this.mImsRILAdapter.queryFacilityLock(getFacilityFromCBType(cbType), (String) null, getServiceClass() != -1 ? getServiceClass() : 0, this.mHandler.obtainMessage(1000, requestId, 0, (Object) null));
        resetServcieClass();
        return requestId;
    }

    public int queryCallBarringForServiceClass(int cbType, int serviceClass) {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "queryCallBarringForServiceClass(): requestId = " + requestId);
        this.mImsRILAdapter.queryFacilityLock(getFacilityFromCBType(cbType), (String) null, serviceClass, this.mHandler.obtainMessage(1000, requestId, 0, (Object) null));
        resetServcieClass();
        return requestId;
    }

    public int queryCallForward(int condition, String number) {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "queryCallForward(): requestId = " + requestId);
        this.mImsRILAdapter.queryCallForwardStatus(getCFReasonFromCondition(condition), getServiceClass() != -1 ? getServiceClass() : 0, number, this.mHandler.obtainMessage(1001, requestId, 0, (Object) null));
        resetServcieClass();
        return requestId;
    }

    public int queryCallWaiting() {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "queryCallWaiting(): requestId = " + requestId);
        this.mImsRILAdapter.queryCallWaiting(1, this.mHandler.obtainMessage(1002, requestId, 0, (Object) null));
        return requestId;
    }

    public int queryCLIR() {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "queryCLIR(): requestId = " + requestId);
        this.mImsRILAdapter.getCLIR(this.mHandler.obtainMessage(1003, requestId, 0, (Object) null));
        return requestId;
    }

    public int queryCLIP() {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "queryCLIP(): requestId = " + requestId);
        this.mImsRILAdapter.queryCLIP(this.mHandler.obtainMessage(1004, requestId, 0, (Object) null));
        return requestId;
    }

    public int queryCOLR() {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "queryCOLR(): requestId = " + requestId);
        this.mImsRILAdapter.getCOLR(this.mHandler.obtainMessage(1005, requestId, 0, (Object) null));
        return requestId;
    }

    public int queryCOLP() {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "queryCOLP(): requestId = " + requestId);
        this.mImsRILAdapter.getCOLP(this.mHandler.obtainMessage(1006, requestId, 0, (Object) null));
        return requestId;
    }

    public int transact(Bundle ssInfo) {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        return requestId;
    }

    public int updateCallBarring(int cbType, int enable, String[] barrList) {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "updateCallBarring(): requestId = " + requestId);
        this.mImsRILAdapter.setFacilityLock(getFacilityFromCBType(cbType), enable == 1, "", getServiceClass() != -1 ? getServiceClass() : 0, this.mHandler.obtainMessage(1007, requestId, 0, (Object) null));
        resetServcieClass();
        return requestId;
    }

    public int updateCallBarringForServiceClass(int cbType, int enable, String[] barrList, int serviceClass) {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "updateCallBarringForServiceClass(): requestId = " + requestId);
        this.mImsRILAdapter.setFacilityLock(getFacilityFromCBType(cbType), enable == 1, "", serviceClass, this.mHandler.obtainMessage(1007, requestId, 0, (Object) null));
        return requestId;
    }

    public int updateCallBarringWithPassword(int cbType, int enable, String[] barrList, int serviceClass, String password) {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "updateCallBarringWithPassword(): requestId = " + requestId);
        this.mImsRILAdapter.setFacilityLock(getFacilityFromCBType(cbType), enable == 1, password, serviceClass, this.mHandler.obtainMessage(1007, requestId, 0, (Object) null));
        return requestId;
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0046, code lost:
        r0 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int updateCallForward(int r14, int r15, java.lang.String r16, int r17, int r18) {
        /*
            r13 = this;
            r1 = r13
            java.lang.Object r2 = mLock
            monitor-enter(r2)
            int r0 = sRequestId     // Catch:{ all -> 0x0042 }
            r3 = r0
            int r0 = r0 + 1
            sRequestId = r0     // Catch:{ all -> 0x0042 }
            monitor-exit(r2)     // Catch:{ all -> 0x0042 }
            java.lang.String r0 = "ImsUtImpl"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "updateCallForward(): requestId = "
            r2.append(r4)
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.d(r0, r2)
            com.mediatek.ims.ImsUtImpl$ResultHandler r0 = r1.mHandler
            r2 = 1008(0x3f0, float:1.413E-42)
            r4 = 0
            r5 = 0
            android.os.Message r0 = r0.obtainMessage(r2, r3, r4, r5)
            com.mediatek.ims.ril.ImsCommandsInterface r6 = r1.mImsRILAdapter
            int r7 = r13.getCFActionFromAction(r14)
            r4 = r15
            int r8 = r13.getCFReasonFromCondition(r15)
            r9 = r17
            r10 = r16
            r11 = r18
            r12 = r0
            r6.setCallForward(r7, r8, r9, r10, r11, r12)
            return r3
        L_0x0042:
            r0 = move-exception
            r4 = r15
        L_0x0044:
            monitor-exit(r2)     // Catch:{ all -> 0x0046 }
            throw r0
        L_0x0046:
            r0 = move-exception
            goto L_0x0044
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.ImsUtImpl.updateCallForward(int, int, java.lang.String, int, int):int");
    }

    public int updateCallWaiting(boolean enable, int serviceClass) {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "updateCallWaiting(): requestId = " + requestId);
        this.mImsRILAdapter.setCallWaiting(enable, serviceClass, this.mHandler.obtainMessage(1009, requestId, 0, (Object) null));
        return requestId;
    }

    public int updateCLIR(int clirMode) {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "updateCLIR(): requestId = " + requestId);
        this.mImsRILAdapter.setCLIR(clirMode, this.mHandler.obtainMessage(1010, requestId, 0, (Object) null));
        return requestId;
    }

    public int updateCLIP(boolean enable) {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "updateCLIP(): requestId = " + requestId);
        this.mImsRILAdapter.setCLIP(enable, this.mHandler.obtainMessage(1011, requestId, 0, (Object) null));
        return requestId;
    }

    public int updateCOLR(int presentation) {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "updateCOLR(): requestId = " + requestId);
        this.mImsRILAdapter.setCOLR(presentation, this.mHandler.obtainMessage(1012, requestId, 0, (Object) null));
        return requestId;
    }

    public int updateCOLP(boolean enable) {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        Log.d(TAG, "updateCOLP(): requestId = " + requestId);
        this.mImsRILAdapter.setCOLP(enable, this.mHandler.obtainMessage(1013, requestId, 0, (Object) null));
        return requestId;
    }

    public void setListener(ImsUtListener listener) {
        this.mListener = listener;
    }

    public static int getAndIncreaseRequestId() {
        int requestId;
        synchronized (mLock) {
            int i = sRequestId;
            requestId = i;
            sRequestId = i + 1;
        }
        return requestId;
    }

    private static int getServiceClass() {
        return Integer.parseInt(SystemProperties.get(SS_SERVICE_CLASS_PROP, "-1"));
    }

    private static void resetServcieClass() {
        SystemProperties.set(SS_SERVICE_CLASS_PROP, "-1");
    }

    public void notifyUtConfigurationUpdated(Message msg) {
        this.mListener.onUtConfigurationUpdated(msg.arg1);
    }

    public void notifyUtConfigurationUpdateFailed(Message msg, ImsReasonInfo error) {
        this.mListener.onUtConfigurationUpdateFailed(msg.arg1, error);
    }

    public void notifyUtConfigurationQueried(Message msg, Bundle ssInfo) {
        this.mListener.onUtConfigurationQueried(msg.arg1, ssInfo);
    }

    public void notifyUtConfigurationQueryFailed(Message msg, ImsReasonInfo error) {
        this.mListener.onUtConfigurationQueryFailed(msg.arg1, error);
    }

    private long[] convertToTimeSlotArray(String timeSlotString) {
        long[] timeSlot = null;
        if (timeSlotString != null) {
            String[] timeArray = timeSlotString.split(",", 2);
            if (timeArray.length == 2) {
                timeSlot = new long[2];
                int i = 0;
                while (i < 2) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                    try {
                        timeSlot[i] = dateFormat.parse(timeArray[i]).getTime();
                        i++;
                    } catch (ParseException e) {
                        Log.e(TAG, "convertToTimeSlotArray() ParseException occured");
                        return null;
                    }
                }
            }
        }
        return timeSlot;
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b5, code lost:
        r0 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int updateCallForwardUncondTimer(int r21, int r22, int r23, int r24, int r25, int r26, java.lang.String r27) {
        /*
            r20 = this;
            r7 = r20
            r8 = r21
            r9 = r22
            r10 = r23
            r11 = r24
            if (r8 != 0) goto L_0x0028
            if (r9 != 0) goto L_0x0028
            if (r10 != 0) goto L_0x0028
            if (r11 != 0) goto L_0x0028
            java.lang.String r0 = "ImsUtImpl"
            java.lang.String r1 = "updateCallForwardUncondTimer(): Time is all zero! use updateCallForward"
            android.util.Log.i(r0, r1)
            r5 = 1
            r6 = 0
            r1 = r20
            r2 = r25
            r3 = r26
            r4 = r27
            int r0 = r1.updateCallForward(r2, r3, r4, r5, r6)
            return r0
        L_0x0028:
            java.lang.Object r1 = mLock
            monitor-enter(r1)
            int r0 = sRequestId     // Catch:{ all -> 0x00ae }
            r2 = r0
            r3 = 1
            int r0 = r0 + r3
            sRequestId = r0     // Catch:{ all -> 0x00ae }
            monitor-exit(r1)     // Catch:{ all -> 0x00ae }
            java.lang.String r0 = "ImsUtImpl"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r4 = "updateCallForwardUncondTimer(): requestId = "
            r1.append(r4)
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
            if (r8 < 0) goto L_0x005d
            r0 = 23
            if (r8 > r0) goto L_0x005d
            if (r9 < 0) goto L_0x005d
            r1 = 59
            if (r9 > r1) goto L_0x005d
            if (r10 < 0) goto L_0x005d
            if (r10 > r0) goto L_0x005d
            if (r11 < 0) goto L_0x005d
            if (r11 <= r1) goto L_0x0064
        L_0x005d:
            java.lang.String r0 = "ImsUtImpl"
            java.lang.String r1 = "updateCallForwardUncondTimer(): Time is wrong! "
            android.util.Log.e(r0, r1)
        L_0x0064:
            java.lang.String r0 = "%02d:%02d,%02d:%02d"
            r1 = 4
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.Integer r4 = java.lang.Integer.valueOf(r21)
            r5 = 0
            r1[r5] = r4
            java.lang.Integer r4 = java.lang.Integer.valueOf(r22)
            r1[r3] = r4
            r3 = 2
            java.lang.Integer r4 = java.lang.Integer.valueOf(r23)
            r1[r3] = r4
            r3 = 3
            java.lang.Integer r4 = java.lang.Integer.valueOf(r24)
            r1[r3] = r4
            java.lang.String r0 = java.lang.String.format(r0, r1)
            com.mediatek.ims.ImsUtImpl$ResultHandler r1 = r7.mHandler
            r3 = 1201(0x4b1, float:1.683E-42)
            r4 = 0
            android.os.Message r1 = r1.obtainMessage(r3, r2, r5, r4)
            com.mediatek.ims.ril.ImsCommandsInterface r12 = r7.mImsRILAdapter
            r3 = r25
            int r13 = r7.getCFActionFromAction(r3)
            r4 = r26
            int r14 = r7.getCFReasonFromCondition(r4)
            r15 = 1
            r17 = 0
            long[] r18 = r7.convertToTimeSlotArray(r0)
            r16 = r27
            r19 = r1
            r12.setCallForwardInTimeSlot(r13, r14, r15, r16, r17, r18, r19)
            return r2
        L_0x00ae:
            r0 = move-exception
            r3 = r25
            r4 = r26
        L_0x00b3:
            monitor-exit(r1)     // Catch:{ all -> 0x00b5 }
            throw r0
        L_0x00b5:
            r0 = move-exception
            goto L_0x00b3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.ImsUtImpl.updateCallForwardUncondTimer(int, int, int, int, int, int, java.lang.String):int");
    }

    public static String encryptString(String message) {
        byte[] textByte;
        Base64.Encoder encoder = Base64.getEncoder();
        if (message == null) {
            return "null";
        }
        try {
            textByte = message.getBytes("UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "encryptString() exception occured");
            textByte = null;
        }
        if (textByte == null) {
            return "";
        }
        return encoder.encodeToString(textByte);
    }
}
