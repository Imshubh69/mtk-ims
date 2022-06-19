package com.mediatek.ims.ril;

import android.hardware.radio.V1_0.LastCallFailCauseInfo;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_0.SendSmsResult;
import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.LastCallFailCause;
import com.mediatek.ims.MtkSmsResponse;
import com.mediatek.internal.telephony.MtkCallForwardInfo;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import vendor.mediatek.hardware.mtkradioex.V2_0.CallForwardInfoEx;

public class ImsRadioResponseV2 extends ImsRadioResponseBaseV2 {
    private int mPhoneId;
    private ImsRILAdapter mRil;

    ImsRadioResponseV2(ImsRILAdapter ril, int phoneId) {
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

    public void pullCallResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    static void responseStringArrayList(ImsRILAdapter ril, RadioResponseInfo responseInfo, ArrayList<String> strings) {
        RILRequest rr = ril.processResponse(responseInfo, true);
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

    public void videoCallAcceptResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void eccRedialApproveResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void imsEctCommandResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void controlCallResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setCallIndicationResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void imsDeregNotificationResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setImsEnableResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setImsVideoEnableResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setImscfgResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setModemImsCfgResponse(RadioResponseInfo info, String results) {
        responseString(info, results);
    }

    public void getProvisionValueResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setProvisionValueResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setImsCfgFeatureValueResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void getImsCfgFeatureValueResponse(RadioResponseInfo info, int value) {
        responseInts(info, value);
    }

    public void setImsCfgProvisionValueResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void getImsCfgProvisionValueResponse(RadioResponseInfo info, String value) {
        responseString(info, value);
    }

    public void getImsCfgResourceCapValueResponse(RadioResponseInfo info, int value) {
        responseInts(info, value);
    }

    public void controlImsConferenceCallMemberResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void hangupAllResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setWfcProfileResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void conferenceDialResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void vtDialResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void vtDialWithSipUriResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void dialWithSipUriResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void sendUssiResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void cancelUssiResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void getXcapStatusResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void resetSuppServResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setupXcapUserAgentStringResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void forceReleaseCallResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void imsBearerStateConfirmResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setImsBearerNotificationResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setImsRtpReportResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setImsRegistrationReportResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setVoiceDomainPreferenceResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setClipResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void getColrResponse(RadioResponseInfo info, int status) {
        responseInts(info, status);
    }

    public void setColrResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void getColpResponse(RadioResponseInfo info, int n, int m) {
        responseInts(info, n, m);
    }

    public void setColpResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void queryCallForwardInTimeSlotStatusResponse(RadioResponseInfo responseInfo, ArrayList<CallForwardInfoEx> callForwardInfoExs) {
        responseCallForwardInfoEx(responseInfo, callForwardInfoExs);
    }

    public void setCallForwardInTimeSlotResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void runGbaAuthenticationResponse(RadioResponseInfo responseInfo, ArrayList<String> resList) {
        responseStringArrayList(this.mRil, responseInfo, resList);
    }

    public void hangupWithReasonResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void setSipHeaderResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setSipHeaderReportResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void setImsCallModeResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    public void toggleRttAudioIndicationResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    /* access modifiers changed from: protected */
    public void riljLoge(String msg) {
        this.mRil.riljLoge(msg);
    }

    private void responseCallForwardInfoEx(RadioResponseInfo responseInfo, ArrayList<CallForwardInfoEx> callForwardInfoExs) {
        RILRequest rr = this.mRil.processResponse(responseInfo, true);
        if (rr != null) {
            ParseException[] ret = new MtkCallForwardInfo[callForwardInfoExs.size()];
            for (int i = 0; i < callForwardInfoExs.size(); i++) {
                long[] timeSlot = new long[2];
                ret[i] = new MtkCallForwardInfo();
                ret[i].status = callForwardInfoExs.get(i).status;
                ret[i].reason = callForwardInfoExs.get(i).reason;
                ret[i].serviceClass = callForwardInfoExs.get(i).serviceClass;
                ret[i].toa = callForwardInfoExs.get(i).toa;
                ret[i].number = callForwardInfoExs.get(i).number;
                ret[i].timeSeconds = callForwardInfoExs.get(i).timeSeconds;
                String[] timeSlotStr = {callForwardInfoExs.get(i).timeSlotBegin, callForwardInfoExs.get(i).timeSlotEnd};
                if (timeSlotStr[0] == null || timeSlotStr[1] == null) {
                    ret[i].timeSlot = null;
                } else {
                    int j = 0;
                    while (j < 2) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                        try {
                            timeSlot[j] = dateFormat.parse(timeSlotStr[j]).getTime();
                            j++;
                        } catch (ParseException e) {
                            riljLoge("responseCallForwardInfoEx() ParseException occured");
                            timeSlot = null;
                        }
                    }
                    ret[i].timeSlot = timeSlot;
                }
            }
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDone(rr, responseInfo, ret);
        }
    }

    private void responseVoid(RadioResponseInfo responseInfo) {
        RILRequest rr = this.mRil.processResponse(responseInfo, true);
        if (rr != null) {
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, (Object) null);
            }
            this.mRil.processResponseDone(rr, responseInfo, (Object) null);
        }
    }

    private void responseString(RadioResponseInfo responseInfo, String str) {
        RILRequest rr = this.mRil.processResponse(responseInfo, true);
        if (rr != null) {
            String ret = null;
            if (responseInfo.error == 0) {
                ret = str;
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDone(rr, responseInfo, ret);
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
        RILRequest rr = this.mRil.processResponse(responseInfo, true);
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
        RILRequest rr = this.mRil.processResponse(responseInfo, true);
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

    public void sendImsSmsExResponse(RadioResponseInfo responseInfo, SendSmsResult sms) {
        RILRequest rr = this.mRil.processResponse(responseInfo, true);
        if (rr != null) {
            MtkSmsResponse ret = new MtkSmsResponse(sms.messageRef, sms.ackPDU, sms.errorCode);
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDone(rr, responseInfo, ret);
        }
    }

    public void acknowledgeLastIncomingGsmSmsExResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void acknowledgeLastIncomingCdmaSmsExResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void queryVopsStatusResponse(RadioResponseInfo responseInfo, int vops) {
        responseInts(responseInfo, vops);
    }

    public void setVendorSettingResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void querySsacStatusResponse(RadioResponseInfo responseInfo, ArrayList<Integer> status) {
        responseIntArrayList(responseInfo, status);
    }

    public void setCallAdditionalInfoResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void videoRingtoneEventResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }
}
