package com.mediatek.ims.feature;

import android.annotation.SystemApi;
import android.content.Context;
import android.hardware.radio.V1_0.SmsAcknowledgeFailCause;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ims.stub.ImsSmsImplBase;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.gsm.SmsMessage;
import com.android.internal.telephony.uicc.IccUtils;
import com.mediatek.ims.ImsService;
import com.mediatek.ims.OperatorUtils;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@SystemApi
public class MtkImsSmsImpl extends ImsSmsImplBase {
    private static final boolean ENG = "eng".equals(Build.TYPE);
    private static final String LOG_TAG = "MtkImsSmsImpl";
    private static HashMap<Integer, MtkImsSmsImpl> sMtkImsSmsImpltances = new HashMap<>();
    private Context mContext;
    private ImsService mImsServiceImpl = null;
    private ConcurrentHashMap<Integer, String> mInboundSmsFormat = new ConcurrentHashMap<>();
    private boolean mIsReady = false;
    private AtomicInteger mNextToken = new AtomicInteger();
    private int mPhoneId = -1;
    private ConcurrentHashMap<Integer, Integer> mToken = new ConcurrentHashMap<>();

    public static MtkImsSmsImpl getInstance(Context context, int phoneId, ImsService service) {
        if (sMtkImsSmsImpltances.containsKey(Integer.valueOf(phoneId))) {
            return sMtkImsSmsImpltances.get(Integer.valueOf(phoneId));
        }
        sMtkImsSmsImpltances.put(Integer.valueOf(phoneId), new MtkImsSmsImpl(context, phoneId, service));
        return sMtkImsSmsImpltances.get(Integer.valueOf(phoneId));
    }

    public MtkImsSmsImpl(Context context, int phoneId, ImsService service) {
        configure(context, phoneId, service);
    }

    public MtkImsSmsImpl(int phoneId) {
        configure((Context) null, phoneId, (ImsService) null);
    }

    public void configure(Context context, int phoneId, ImsService service) {
        this.mImsServiceImpl = service;
        this.mPhoneId = phoneId;
        this.mContext = context;
        log("configure phone " + this.mPhoneId);
    }

    public void sendSms(int token, int messageRef, String format, String smsc, boolean isRetry, byte[] pdu) {
        if (this.mIsReady) {
            this.mImsServiceImpl.sendSms(this.mPhoneId, token, messageRef, format, smsc, isRetry, pdu);
            return;
        }
        throw new RuntimeException("onReady is not called yet");
    }

    public void sendSmsRsp(int token, int messageRef, int status, int reason) throws RuntimeException {
        log("sendSmsRsp toke=" + token + ",messageRef=" + messageRef + ",status=" + status + ",reason=" + reason);
        if (status == 1) {
            this.mToken.put(Integer.valueOf(messageRef), Integer.valueOf(token));
        }
        onSendSmsResult(token, messageRef, status, reason);
    }

    public void newStatusReportInd(byte[] pdu, String format) {
        SmsMessageBase sms = null;
        if ("3gpp".equals(format)) {
            sms = SmsMessage.createFromPdu(pdu);
        } else if ("3gpp2".equals(format)) {
            sms = com.android.internal.telephony.cdma.SmsMessage.createFromPdu(pdu);
        }
        boolean mayAckHere = true;
        if (sms != null) {
            int messageRef = sms.mMessageRef;
            int token = this.mToken.getOrDefault(Integer.valueOf(messageRef), -1).intValue();
            log("newStatusReportInd token=" + token + ", messageRef=" + messageRef + ", pdu: " + IccUtils.bytesToHexString(pdu));
            if (token >= 0) {
                mayAckHere = false;
                this.mInboundSmsFormat.put(Integer.valueOf(token), format);
                onSmsStatusReportReceived(token, messageRef, format, pdu);
            } else {
                loge("newStatusReportInd, token < 0, shouldn't be here");
            }
        } else {
            loge("newStatusReportInd, sms is null, shouldn't be here");
        }
        if (!mayAckHere) {
            return;
        }
        if ("3gpp".equals(format)) {
            this.mImsServiceImpl.acknowledgeLastIncomingGsmSms(this.mPhoneId, false, 1);
        } else if ("3gpp2".equals(format)) {
            this.mImsServiceImpl.acknowledgeLastIncomingCdmaSms(this.mPhoneId, false, 2);
        } else {
            loge("SMS format error.");
        }
    }

    public void newImsSmsInd(byte[] pdu, String format) {
        int token = this.mNextToken.incrementAndGet();
        this.mInboundSmsFormat.put(Integer.valueOf(token), format);
        onSmsReceived(token, format, pdu);
    }

    public void acknowledgeSmsReport(int token, int messageRef, int result) {
        if (this.mIsReady) {
            log("acknowledgeSmsReport toke=" + token + ",messageRef=" + messageRef + ",result=" + result);
            this.mToken.remove(Integer.valueOf(token), Integer.valueOf(messageRef));
            acknowledgeSms(token, messageRef, result);
            return;
        }
        throw new RuntimeException("onReady is not called yet");
    }

    public void acknowledgeSms(int token, int messageRef, int result) {
        if (this.mIsReady) {
            String format = this.mInboundSmsFormat.remove(Integer.valueOf(token));
            boolean ok = true;
            if (result != 1) {
                ok = false;
            }
            if ("3gpp".equals(format)) {
                this.mImsServiceImpl.acknowledgeLastIncomingGsmSms(this.mPhoneId, ok, resultToCauseForGsm(result));
            } else if ("3gpp2".equals(format)) {
                this.mImsServiceImpl.acknowledgeLastIncomingCdmaSms(this.mPhoneId, ok, resultToCauseForCdma(result));
            } else {
                loge("SMS format error.");
            }
        } else {
            throw new RuntimeException("onReady is not called yet");
        }
    }

    public String getSmsFormat() {
        if (this.mIsReady) {
            boolean is3GPP2Format = OperatorUtils.isMatched(OperatorUtils.OPID.OP236, this.mPhoneId);
            String smsFormat = SystemProperties.get("persist.vendor.radio.smsformat", "");
            log("mPhoneId:" + this.mPhoneId + ",is3GPP2Format:" + is3GPP2Format + ",format:" + smsFormat);
            if (smsFormat.equals("3gpp2")) {
                return "3gpp2";
            }
            if (!smsFormat.equals("3gpp") && true == is3GPP2Format) {
                return "3gpp2";
            }
            return "3gpp";
        }
        throw new RuntimeException("onReady is not called yet");
    }

    public void onReady() {
        this.mIsReady = true;
        log("onReady");
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, "[" + this.mPhoneId + "] " + msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, "[" + this.mPhoneId + "] " + msg);
    }

    private static int resultToCauseForCdma(int rc) {
        switch (rc) {
            case 1:
                return 0;
            case 3:
                return 35;
            case 4:
                return 4;
            default:
                return 39;
        }
    }

    private static int resultToCauseForGsm(int rc) {
        switch (rc) {
            case 1:
                return 0;
            case 3:
                return SmsAcknowledgeFailCause.MEMORY_CAPACITY_EXCEEDED;
            default:
                return 255;
        }
    }
}
