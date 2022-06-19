package com.mediatek.wfo.ril;

import android.hardware.radio.V1_0.RadioResponseInfo;
import android.os.AsyncResult;
import android.os.Message;
import vendor.mediatek.hardware.mtkradioex.V3_0.IMwiRadioResponse;

public class MwiRadioResponse extends IMwiRadioResponse.Stub {
    private int mPhoneId;
    private MwiRIL mRil;

    MwiRadioResponse(MwiRIL ril, int phoneId) {
        this.mRil = ril;
        this.mPhoneId = phoneId;
        ril.riljLogv("MwiRadioResponse, phone = " + this.mPhoneId);
    }

    static void sendMessageResponse(Message msg, Object ret) {
        if (msg != null) {
            AsyncResult.forMessage(msg, ret, (Throwable) null);
            msg.sendToTarget();
        }
    }

    public void setWifiEnabledResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void setWifiAssociatedResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void setWfcConfigResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void getWfcConfigResponse(RadioResponseInfo responseInfo, int integer) {
        responseInt(responseInfo, integer);
    }

    public void setWifiSignalLevelResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void setWifiIpAddressResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void setLocationInfoResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void setEmergencyAddressIdResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void setNattKeepAliveStatusResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void setWifiPingResultResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void notifyEPDGScreenStateResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    private void responseVoid(RadioResponseInfo responseInfo) {
        RILRequest rr = this.mRil.processResponse(responseInfo);
        if (rr != null) {
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, (Object) null);
            }
            this.mRil.processResponseDone(rr, responseInfo, (Object) null);
        }
    }

    private void responseString(RadioResponseInfo responseInfo, String str) {
        RILRequest rr = this.mRil.processResponse(responseInfo);
        if (rr != null) {
            String ret = null;
            if (responseInfo.error == 0) {
                ret = str;
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDone(rr, responseInfo, ret);
        }
    }

    private void responseInt(RadioResponseInfo responseInfo, int integer) {
        RILRequest rr = this.mRil.processResponse(responseInfo);
        if (rr != null) {
            int ret = -1;
            if (responseInfo.error == 0) {
                ret = integer;
                MwiRIL mwiRIL = this.mRil;
                mwiRIL.riljLog("responseInt, ret = " + ret);
                sendMessageResponse(rr.mResult, Integer.valueOf(ret));
            }
            this.mRil.processResponseDone(rr, responseInfo, Integer.valueOf(ret));
        }
    }
}
