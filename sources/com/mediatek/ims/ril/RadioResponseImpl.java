package com.mediatek.ims.ril;

import android.hardware.radio.V1_0.CallForwardInfo;
import android.hardware.radio.V1_0.LastCallFailCauseInfo;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.LastCallFailCause;
import java.util.ArrayList;

public class RadioResponseImpl extends RadioResponseBase {
    private int mPhoneId;
    private ImsRILAdapter mRil;

    RadioResponseImpl(ImsRILAdapter ril, int phoneId) {
        this.mRil = ril;
        this.mPhoneId = phoneId;
        ril.riljLogv("ImsRadioResponse, phone = " + this.mPhoneId);
    }

    static void sendMessageResponse(Message msg, Object ret) {
        if (msg != null) {
            AsyncResult.forMessage(msg, ret, (Throwable) null);
            msg.sendToTarget();
        }
    }

    public void getLastCallFailCauseResponse(RadioResponseInfo responseInfo, LastCallFailCauseInfo failCauseInfo) {
        responseFailCause(responseInfo, failCauseInfo);
    }

    public void acceptCallResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void conferenceResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void dialResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void emergencyDialResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void exitEmergencyCallbackModeResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void explicitCallTransferResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void hangupConnectionResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void sendDtmfResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void setMuteResponse(RadioResponseInfo response) {
        responseVoid(response);
    }

    public void startDtmfResponse(RadioResponseInfo responseInfo) {
        this.mRil.handleDtmfQueueNext(responseInfo.serial);
        responseVoid(responseInfo);
    }

    public void stopDtmfResponse(RadioResponseInfo info) {
        this.mRil.handleDtmfQueueNext(info.serial);
        responseVoid(info);
    }

    public void switchWaitingOrHoldingAndActiveResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    static void responseStringArrayList(ImsRILAdapter ril, RadioResponseInfo responseInfo, ArrayList<String> strings) {
        RILRequest rr = ril.processResponse(responseInfo, false);
        if (rr != null) {
            String[] ret = null;
            if (responseInfo.error == 0) {
                ret = new String[strings.size()];
                for (int i = 0; i < strings.size(); i++) {
                    ret[i] = strings.get(i);
                }
                sendMessageResponse(rr.mResult, ret);
            }
            ril.processResponseDone(rr, responseInfo, ret);
        }
    }

    public void getFacilityLockForAppResponse(RadioResponseInfo info, int resp) {
        responseInts(info, resp);
    }

    public void setFacilityLockForAppResponse(RadioResponseInfo info, int retry) {
        responseInts(info, retry);
    }

    public void setCallForwardResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void getCallForwardStatusResponse(RadioResponseInfo info, ArrayList<CallForwardInfo> callForwardInfos) {
        responseCallForwardInfo(info, callForwardInfos);
    }

    public void getCallWaitingResponse(RadioResponseInfo info, boolean enable, int serviceClass) {
        responseInts(info, enable, serviceClass);
    }

    public void setCallWaitingResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void getClirResponse(RadioResponseInfo info, int n, int m) {
        responseInts(info, n, m);
    }

    public void setClirResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void getClipResponse(RadioResponseInfo info, int status) {
        responseInts(info, status);
    }

    /* access modifiers changed from: protected */
    public void riljLoge(String msg) {
        this.mRil.riljLoge(msg);
    }

    private void responseCallForwardInfo(RadioResponseInfo responseInfo, ArrayList<CallForwardInfo> callForwardInfos) {
        RILRequest rr = this.mRil.processResponse(responseInfo, false);
        if (rr != null) {
            com.android.internal.telephony.CallForwardInfo[] ret = new com.android.internal.telephony.CallForwardInfo[callForwardInfos.size()];
            for (int i = 0; i < callForwardInfos.size(); i++) {
                ret[i] = new com.android.internal.telephony.CallForwardInfo();
                ret[i].status = callForwardInfos.get(i).status;
                ret[i].reason = callForwardInfos.get(i).reason;
                ret[i].serviceClass = callForwardInfos.get(i).serviceClass;
                ret[i].toa = callForwardInfos.get(i).toa;
                ret[i].number = callForwardInfos.get(i).number;
                ret[i].timeSeconds = callForwardInfos.get(i).timeSeconds;
            }
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDone(rr, responseInfo, ret);
        }
    }

    private void responseVoid(RadioResponseInfo responseInfo) {
        RILRequest rr = this.mRil.processResponse(responseInfo, false);
        if (rr != null) {
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, (Object) null);
            }
            this.mRil.processResponseDone(rr, responseInfo, (Object) null);
        }
    }

    public void responseInts(RadioResponseInfo responseInfo, int... var) {
        ArrayList<Integer> ints = new ArrayList<>();
        for (int valueOf : var) {
            ints.add(Integer.valueOf(valueOf));
        }
        responseIntArrayList(responseInfo, ints);
    }

    public void responseIntArrayList(RadioResponseInfo responseInfo, ArrayList<Integer> var) {
        RILRequest rr = this.mRil.processResponse(responseInfo, false);
        if (rr != null) {
            int[] ret = new int[var.size()];
            for (int i = 0; i < var.size(); i++) {
                ret[i] = var.get(i).intValue();
            }
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDone(rr, responseInfo, ret);
        }
    }

    private void responseFailCause(RadioResponseInfo responseInfo, LastCallFailCauseInfo info) {
        RILRequest rr = this.mRil.processResponse(responseInfo, false);
        if (rr != null) {
            LastCallFailCause failCause = null;
            if (responseInfo.error == 0) {
                failCause = new LastCallFailCause();
                failCause.causeCode = info.causeCode;
                failCause.vendorCause = info.vendorCause;
                sendMessageResponse(rr.mResult, failCause);
            }
            this.mRil.processResponseDone(rr, responseInfo, failCause);
        }
    }
}
