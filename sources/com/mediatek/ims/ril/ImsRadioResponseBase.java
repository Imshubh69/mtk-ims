package com.mediatek.ims.ril;

import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_0.SendSmsResult;
import java.util.ArrayList;
import vendor.mediatek.hardware.mtkradioex.V3_0.CallForwardInfoEx;
import vendor.mediatek.hardware.mtkradioex.V3_0.IImsRadioResponse;

public class ImsRadioResponseBase extends IImsRadioResponse.Stub {
    public void acknowledgeLastIncomingGsmSmsExResponse(RadioResponseInfo info) {
        riljLoge("No implementation in acknowledgeLastIncomingGsmSmsExResponse");
    }

    public void acknowledgeLastIncomingCdmaSmsExResponse(RadioResponseInfo info) {
        riljLoge("No implementation in acknowledgeLastIncomingCdmaSmsExResponse");
    }

    public void sendImsSmsExResponse(RadioResponseInfo responseInfo, SendSmsResult sms) {
        riljLoge("No implementation in sendImsSmsExResponse");
    }

    public void cancelUssiResponse(RadioResponseInfo info) {
        riljLoge("No implementation in cancelUssiResponse");
    }

    public void getXcapStatusResponse(RadioResponseInfo info) {
        riljLoge("No implementation in getXcapStatusResponse");
    }

    public void resetSuppServResponse(RadioResponseInfo info) {
        riljLoge("No implementation in resetSuppServResponse");
    }

    public void setupXcapUserAgentStringResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setupXcapUserAgentStringResponse");
    }

    public void conferenceDialResponse(RadioResponseInfo info) {
        riljLoge("No implementation in conferenceDialResponse");
    }

    public void imsDeregNotificationResponse(RadioResponseInfo info) {
        riljLoge("No implementation in deregisterImsResponse");
    }

    public void dialWithSipUriResponse(RadioResponseInfo info) {
        riljLoge("No implementation in dialWithSipUriResponse");
    }

    public void forceReleaseCallResponse(RadioResponseInfo info) {
        riljLoge("No implementation in forceReleaseCallResponse");
    }

    public void getProvisionValueResponse(RadioResponseInfo info) {
        riljLoge("No implementation in getProvisionValueResponse");
    }

    public void hangupAllResponse(RadioResponseInfo info) {
        riljLoge("No implementation in hangupAllResponse");
    }

    public void controlCallResponse(RadioResponseInfo info) {
        riljLoge("No implementation in controlCallResponse");
    }

    public void imsBearerStateConfirmResponse(RadioResponseInfo info) {
        riljLoge("No implementation in imsBearerStateConfirm");
    }

    public void setImsBearerNotificationResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setImsBearerNotificationResponse");
    }

    public void imsEctCommandResponse(RadioResponseInfo info) {
        riljLoge("No implementation in imsEctCommandResponse");
    }

    public void controlImsConferenceCallMemberResponse(RadioResponseInfo info) {
        riljLoge("No implementation in controlImsConferenceCallMemberResponse");
    }

    public void sendUssiResponse(RadioResponseInfo info) {
        riljLoge("No implementation in sendUssiResponse");
    }

    public void setCallIndicationResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setCallIndicationResponse");
    }

    public void setImsCallStatusResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setImsCallStatusResponse");
    }

    public void setImsEnableResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setImsEnableResponse");
    }

    public void setImsRtpReportResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setImsRtpReportResponse");
    }

    public void setImscfgResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setImscfgResponse");
    }

    public void setModemImsCfgResponse(RadioResponseInfo info, String results) {
        riljLoge("No implementation in setModemImsCfgResponse");
    }

    public void pullCallResponse(RadioResponseInfo info) {
        riljLoge("No implementation in pullCallResponse");
    }

    public void setProvisionValueResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setProvisionValueResponse");
    }

    public void setImsCfgFeatureValueResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setImsCfgFeatureValueResponse");
    }

    public void getImsCfgFeatureValueResponse(RadioResponseInfo info, int value) {
        riljLoge("No implementation in getImsCfgFeatureValueResponse");
    }

    public void setImsCfgProvisionValueResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setImsCfgProvisionValueResponse");
    }

    public void getImsCfgProvisionValueResponse(RadioResponseInfo info, String value) {
        riljLoge("No implementation in getImsCfgProvisionValueResponse");
    }

    public void getImsCfgResourceCapValueResponse(RadioResponseInfo info, int value) {
        riljLoge("No implementation in getImsCfgResourceCapValueResponse");
    }

    public void setWfcProfileResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setWfcProfileResponse");
    }

    public void updateImsRegistrationStatusResponse(RadioResponseInfo info) {
        riljLoge("No implementation in updateImsRegistrationStatusResponse");
    }

    public void videoCallAcceptResponse(RadioResponseInfo info) {
        riljLoge("No implementation in videoCallAcceptResponse");
    }

    public void eccRedialApproveResponse(RadioResponseInfo info) {
        riljLoge("No implementation in eccRedialApproveResponse");
    }

    public void vtDialResponse(RadioResponseInfo info) {
        riljLoge("No implementation in vtDialResponse");
    }

    public void vtDialWithSipUriResponse(RadioResponseInfo info) {
        riljLoge("No implementation in vtDialWithSipUriResponse");
    }

    public void setImsRegistrationReportResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setImsRegistrationReportResponse");
    }

    public void setVoiceDomainPreferenceResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setImsVoiceDomainPreferenceResponse");
    }

    public void getVoiceDomainPreferenceResponse(RadioResponseInfo info, int vdp) {
        riljLoge("No implementation in getImsVoiceDomainPreferenceResponse");
    }

    public void setCallForwardInTimeSlotResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setCallForwardInTimeSlotResponse");
    }

    public void runGbaAuthenticationResponse(RadioResponseInfo info, ArrayList<String> arrayList) {
        riljLoge("No implementation in runGbaAuthenticationResponse");
    }

    public void queryCallForwardInTimeSlotStatusResponse(RadioResponseInfo info, ArrayList<CallForwardInfoEx> arrayList) {
        riljLoge("No implementation in queryCallForwardInTimeSlotStatusResponse");
    }

    public void setColrResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setColrResponse");
    }

    public void setColpResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setColpResponse");
    }

    public void getColrResponse(RadioResponseInfo responseInfo, int status) {
        riljLoge("No implementation in getColrResponse");
    }

    public void getColpResponse(RadioResponseInfo responseInfo, int n, int m) {
        riljLoge("No implementation in getColpResponse");
    }

    public void setClipResponse(RadioResponseInfo responseInfo) {
        riljLoge("No implementation in setClipResponse");
    }

    public void hangupWithReasonResponse(RadioResponseInfo responseInfo) {
        riljLoge("No implementation in hangupWithReasonResponse");
    }

    public void setRttModeResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setRttModeResponse");
    }

    public void rttModifyRequestResponseResponse(RadioResponseInfo info) {
        riljLoge("No implementation in rttModifyRequestResponseResponse");
    }

    public void sendRttTextResponse(RadioResponseInfo info) {
        riljLoge("No implementation in sendRttTextResponse");
    }

    public void sendRttModifyRequestResponse(RadioResponseInfo info) {
        riljLoge("No implementation in sendRttModifyRequestResponse");
    }

    public void queryVopsStatusResponse(RadioResponseInfo responseInfo, int vops) {
        riljLoge("No implementation in queryVopsStatusResponse");
    }

    public void setSipHeaderResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setSipHeaderResponse");
    }

    public void setSipHeaderReportResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setSipHeaderReportResponse");
    }

    public void setImsCallModeResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setImsCallModeResponse");
    }

    public void setVendorSettingResponse(RadioResponseInfo responseInfo) {
    }

    public void querySsacStatusResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
        riljLoge("No implementation in querySsacStatusResponse");
    }

    public void toggleRttAudioIndicationResponse(RadioResponseInfo responseInfo) {
        riljLoge("No implementation in toggleRttAudioIndicationResponse");
    }

    public void setCallAdditionalInfoResponse(RadioResponseInfo responseInfo) {
        riljLoge("No implementation in setCallAdditionalInfoResponse");
    }

    public void videoRingtoneEventResponse(RadioResponseInfo responseInfo) {
        riljLoge("No implementation in videoRingtoneEventResponse");
    }

    /* access modifiers changed from: protected */
    public void riljLoge(String msg) {
    }
}
