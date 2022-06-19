package com.mediatek.ims.ril;

import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.hardware.radio.V1_0.SuppSvcNotification;
import java.util.ArrayList;
import vendor.mediatek.hardware.mtkradioex.V2_0.Dialog;
import vendor.mediatek.hardware.mtkradioex.V2_0.ImsConfParticipant;
import vendor.mediatek.hardware.mtkradioex.V2_0.ImsRegStatusInfo;
import vendor.mediatek.hardware.mtkradioex.V2_0.IncomingCallNotification;
import vendor.mediatek.hardware.mtkradioex.V2_2.IImsRadioIndication;

public class ImsRadioIndicationBaseV2 extends IImsRadioIndication.Stub {
    public void ectIndication(int arg0, int arg1, int arg2, int arg3) {
        riljLoge("No implementation in ectIndication");
    }

    public void volteSetting(int arg0, boolean arg1) {
        riljLoge("No implementation in volteSetting");
    }

    public void callInfoIndication(int type, ArrayList<String> arrayList) {
        riljLoge("No implementation in callInfoIndication");
    }

    public void callmodChangeIndicator(int type, String callId, String callMode, String videoState, String audioDirection, String pau) {
        riljLoge("No implementation in callmodChangeIndicator");
    }

    public void econfResultIndication(int type, String confCallId, String op, String num, String result, String cause, String joinedCallId) {
        riljLoge("No implementation in econfResultIndication");
    }

    public void getProvisionDone(int type, String data, String arg2) {
        riljLoge("No implementation in getProvisionDone");
    }

    public void imsBearerStateNotify(int type, int data, int action, String arg2) {
        riljLoge("No implementation in imsBearerStateNotify");
    }

    public void imsBearerInit(int type) {
        riljLoge("No implementation in imsBearerInit");
    }

    public void imsDataInfoNotify(int type, String arg1, String arg2, String arg3) {
        riljLoge("No implementation in imsDataInfoNotify");
    }

    public void imsDisableDone(int type) {
        riljLoge("No implementation in imsDisableDone");
    }

    public void imsDisableStart(int type) {
        riljLoge("No implementation in imsDisableStart");
    }

    public void imsEnableDone(int type) {
        riljLoge("No implementation in imsEnableDone");
    }

    public void imsEnableStart(int type) {
        riljLoge("No implementation in imsEnableStart");
    }

    public void imsRegistrationInfo(int type, int data, int arg2) {
        riljLoge("No implementation in imsRegistrationInfo");
    }

    public void incomingCallIndication(int type, IncomingCallNotification data) {
        riljLoge("No implementation in incomingCallIndication");
    }

    public void onUssi(int type, int ussdModeType, String msg) {
        riljLoge("No implementation in onUssi");
    }

    public void onXui(int type, String accountId, String broadcastFlag, String xuiInfo) {
        riljLoge("No implementation in onXui");
    }

    public void onVolteSubscription(int type, int status) {
        riljLoge("No implementation in onVolteSubscription");
    }

    public void suppSvcNotify(int type, SuppSvcNotification data) {
        riljLoge("No implementation in suppSvcNotify");
    }

    public void sipCallProgressIndicator(int type, String callId, String dir, String sipMsgType, String method, String responseCode, String reasonText) {
        riljLoge("No implementation in sipCallProgressIndicator");
    }

    public void videoCapabilityIndicator(int type, String callId, String localVideoCap, String remoteVideoCap) {
        riljLoge("No implementation in videoCapabilityIndicator");
    }

    public void imsConferenceInfoIndication(int type, ArrayList<ImsConfParticipant> arrayList) {
        riljLoge("No implementation in imsConferenceInfoIndication");
    }

    public void lteMessageWaitingIndication(int type, String callId, String pType, String urcIdx, String totalUrcCount, String rawData) {
        riljLoge("No implementation in lteMessageWaitingIndication");
    }

    public void imsDialogIndication(int type, ArrayList<Dialog> arrayList) {
        riljLoge("No implementation in imsDialogIndication");
    }

    public void imsCfgDynamicImsSwitchComplete(int type) {
        riljLoge("No implementation in imsCfgDynamicImsSwitchComplete");
    }

    public void imsCfgFeatureChanged(int type, int phoneId, int featureId, int value) {
        riljLoge("No implementation in imsCfgFeatureChanged");
    }

    public void imsCfgConfigChanged(int type, int phoneId, String configId, String value) {
        riljLoge("No implementation in imsCfgConfigChanged");
    }

    public void imsCfgConfigLoaded(int type) {
        riljLoge("No implementation in imsCfgConfigLoaded");
    }

    public void newSmsStatusReportEx(int indicationType, ArrayList<Byte> arrayList) {
        riljLoge("No implementation in newSmsStatusReportEx");
    }

    public void newSmsEx(int indicationType, ArrayList<Byte> arrayList) {
        riljLoge("No implementation in newSmsEx");
    }

    public void cdmaNewSmsEx(int indicationType, CdmaSmsMessage msg) {
        riljLoge("No implementation in cdmaNewSmsEx");
    }

    public void noEmergencyCallbackMode(int indicationType) {
        riljLoge("No implementation in noEmergencyCallbackMode");
    }

    public void imsRtpInfo(int type, String pdnId, String networkId, String timer, String sendPktLost, String recvPktLost, String jitter, String delay) {
        riljLoge("No implementation in imsRtpInfoReport");
    }

    public void imsRedialEmergencyIndication(int type, String callId) {
        riljLoge("No implementation in imsRedialEmergencyIndication");
    }

    public void speechCodecInfoIndication(int type, int info) {
        riljLoge("No implementation in speechCodecInfoIndication");
    }

    public void imsRadioInfoChange(int type, String iid, String info) {
        riljLoge("No implementation in imsRadioInfoChange");
    }

    public void imsSupportEcc(int type, int supportLteEcc) {
        riljLoge("No implementation in isSupportLteEcc");
    }

    public void multiImsCount(int type, int count) {
        riljLoge("No implementation in multiImsCount");
    }

    public void imsEventPackageIndication(int type, String callId, String ptype, String urcIdx, String totalUrcCount, String rawData) {
        riljLoge("No implementation in imsEventPackageIndication");
    }

    public void imsDeregDone(int type) {
        riljLoge("No implementation in imsDeregDone");
    }

    public void rttModifyResponse(int indicationType, int callid, int result) {
        riljLoge("No implementation in rttModifyResponse");
    }

    public void rttTextReceive(int indicationType, int callid, int length, String text) {
        riljLoge("No implementation in rttTextReceive");
    }

    public void rttModifyRequestReceive(int indicationType, int callid, int rttType) {
        riljLoge("No implementation in rttModifyRequestReceive");
    }

    public void rttCapabilityIndication(int indicationType, int callid, int localCapability, int remoteCapability, int localStatus, int remoteStatus) {
        riljLoge("No implementation in rttCapabilityIndication");
    }

    public void audioIndication(int indicationType, int callId, int audio) {
        riljLoge("No implementation in audioIndication");
    }

    public void sendVopsIndication(int indicationType, int vops) {
        riljLoge("No implementation in sendVopsIndication");
    }

    public void sipHeaderReport(int indicationType, ArrayList<String> arrayList) {
        riljLoge("No implementation in sipHeaderReport");
    }

    public void callAdditionalInfoInd(int indicationType, int ciType, ArrayList<String> arrayList) {
        riljLoge("No implementation in callAdditionalInfoInd");
    }

    public void callRatIndication(int indicationType, int domain, int rat) {
        riljLoge("No implementation in callRatIndication");
    }

    public void sipRegInfoInd(int indicationType, int account_id, int response_code, ArrayList<String> arrayList) {
        riljLoge("No implementation in sipRegInfoInd");
    }

    public void imsRegStatusReport(int type, ImsRegStatusInfo report) {
        riljLoge("No implementation in imsRegStatusReport");
    }

    public void imsRegInfoInd(int indicationType, ArrayList<Integer> arrayList) {
        riljLoge("No implementation in imsRegInfoInd");
    }

    public void onSsacStatus(int type, ArrayList<Integer> arrayList) {
        riljLoge("No implementation in onSsacStatus");
    }

    public void onMDInternetUsageInd(int indicationType, ArrayList<Integer> arrayList) {
        riljLoge("No implementation in onMDInternetUsage");
    }

    public void videoRingtoneEventInd(int type, ArrayList<String> arrayList) {
        riljLoge("No implementation in videoRingtoneEventInd");
    }

    /* access modifiers changed from: protected */
    public void riljLoge(String msg) {
    }
}
