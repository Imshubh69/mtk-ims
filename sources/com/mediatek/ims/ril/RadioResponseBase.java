package com.mediatek.ims.ril;

import android.hardware.radio.V1_0.ActivityStatsInfo;
import android.hardware.radio.V1_0.Call;
import android.hardware.radio.V1_0.CallForwardInfo;
import android.hardware.radio.V1_0.CardStatus;
import android.hardware.radio.V1_0.CarrierRestrictions;
import android.hardware.radio.V1_0.CdmaBroadcastSmsConfigInfo;
import android.hardware.radio.V1_0.CellInfo;
import android.hardware.radio.V1_0.DataRegStateResult;
import android.hardware.radio.V1_0.GsmBroadcastSmsConfigInfo;
import android.hardware.radio.V1_0.HardwareConfig;
import android.hardware.radio.V1_0.IccIoResult;
import android.hardware.radio.V1_0.LastCallFailCauseInfo;
import android.hardware.radio.V1_0.LceDataInfo;
import android.hardware.radio.V1_0.LceStatusInfo;
import android.hardware.radio.V1_0.NeighboringCell;
import android.hardware.radio.V1_0.OperatorInfo;
import android.hardware.radio.V1_0.RadioCapability;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_0.SendSmsResult;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.hardware.radio.V1_0.SignalStrength;
import android.hardware.radio.V1_0.VoiceRegStateResult;
import android.hardware.radio.V1_1.KeepaliveStatus;
import android.hardware.radio.V1_4.CarrierRestrictionsWithPriority;
import android.hardware.radio.V1_4.IRadioResponse;
import java.util.ArrayList;

public class RadioResponseBase extends IRadioResponse.Stub {
    public void acceptCallResponse(RadioResponseInfo info) {
        riljLoge("No implementation in acceptCallResponse");
    }

    public void acknowledgeIncomingGsmSmsWithPduResponse(RadioResponseInfo info) {
        riljLoge("No implementation in acknowledgeIncomingGsmSmsWithPduResponse");
    }

    public void acknowledgeLastIncomingCdmaSmsResponse(RadioResponseInfo info) {
        riljLoge("No implementation in acknowledgeLastIncomingCdmaSmsResponse");
    }

    public void acknowledgeLastIncomingGsmSmsResponse(RadioResponseInfo info) {
        riljLoge("No implementation in acknowledgeLastIncomingGsmSmsResponse");
    }

    public void acknowledgeRequest(int info) {
        riljLoge("No implementation in acknowledgeRequest");
    }

    public void cancelPendingUssdResponse(RadioResponseInfo info) {
        riljLoge("No implementation in cancelPendingUssdResponse");
    }

    public void changeIccPin2ForAppResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in changeIccPin2ForAppResponse");
    }

    public void changeIccPinForAppResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in changeIccPinForAppResponse");
    }

    public void conferenceResponse(RadioResponseInfo info) {
        riljLoge("No implementation in conferenceResponse");
    }

    public void deactivateDataCallResponse(RadioResponseInfo info) {
        riljLoge("No implementation in deactivateDataCallResponse");
    }

    public void deleteSmsOnRuimResponse(RadioResponseInfo info) {
        riljLoge("No implementation in deleteSmsOnRuimResponse");
    }

    public void deleteSmsOnSimResponse(RadioResponseInfo info) {
        riljLoge("No implementation in deleteSmsOnSimResponse");
    }

    public void dialResponse(RadioResponseInfo info) {
        riljLoge("No implementation in dialResponse");
    }

    public void exitEmergencyCallbackModeResponse(RadioResponseInfo info) {
        riljLoge("No implementation in exitEmergencyCallbackModeResponse");
    }

    public void explicitCallTransferResponse(RadioResponseInfo info) {
        riljLoge("No implementation in explicitCallTransferResponse");
    }

    public void getAllowedCarriersResponse(RadioResponseInfo info, boolean arg1, CarrierRestrictions arg2) {
        riljLoge("No implementation in getAllowedCarriersResponse");
    }

    public void getAvailableBandModesResponse(RadioResponseInfo info, ArrayList<Integer> arrayList) {
        riljLoge("No implementation in getAvailableBandModesResponse");
    }

    public void getAvailableNetworksResponse(RadioResponseInfo info, ArrayList<OperatorInfo> arrayList) {
        riljLoge("No implementation in getAvailableNetworksResponse");
    }

    public void getBasebandVersionResponse(RadioResponseInfo info, String arg1) {
        riljLoge("No implementation in getBasebandVersionResponse");
    }

    public void getCDMASubscriptionResponse(RadioResponseInfo info, String arg1, String arg2, String arg3, String arg4, String arg5) {
        riljLoge("No implementation in getCDMASubscriptionResponse");
    }

    public void getCallForwardStatusResponse(RadioResponseInfo info, ArrayList<CallForwardInfo> arrayList) {
        riljLoge("No implementation in getCallForwardStatusResponse");
    }

    public void getCallWaitingResponse(RadioResponseInfo info, boolean arg1, int arg2) {
        riljLoge("No implementation in getCallWaitingResponse");
    }

    public void getCdmaBroadcastConfigResponse(RadioResponseInfo info, ArrayList<CdmaBroadcastSmsConfigInfo> arrayList) {
        riljLoge("No implementation in getCdmaBroadcastConfigResponse");
    }

    public void getCdmaRoamingPreferenceResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in getCdmaRoamingPreferenceResponse");
    }

    public void getCdmaSubscriptionSourceResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in getCdmaSubscriptionSourceResponse");
    }

    public void getCellInfoListResponse(RadioResponseInfo info, ArrayList<CellInfo> arrayList) {
        riljLoge("No implementation in getCellInfoListResponse");
    }

    public void getClipResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in getClipResponse");
    }

    public void getClirResponse(RadioResponseInfo info, int arg1, int arg2) {
        riljLoge("No implementation in getClirResponse");
    }

    public void getCurrentCallsResponse(RadioResponseInfo info, ArrayList<Call> arrayList) {
        riljLoge("No implementation in getCurrentCallsResponse");
    }

    public void getDataCallListResponse(RadioResponseInfo info, ArrayList<SetupDataCallResult> arrayList) {
        riljLoge("No implementation in getDataCallListResponse");
    }

    public void getDataRegistrationStateResponse(RadioResponseInfo info, DataRegStateResult arg1) {
        riljLoge("No implementation in getDataRegistrationStateResponse");
    }

    public void getDeviceIdentityResponse(RadioResponseInfo info, String arg1, String arg2, String arg3, String arg4) {
        riljLoge("No implementation in getDeviceIdentityResponse");
    }

    public void getFacilityLockForAppResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in getFacilityLockForAppResponse");
    }

    public void getGsmBroadcastConfigResponse(RadioResponseInfo info, ArrayList<GsmBroadcastSmsConfigInfo> arrayList) {
        riljLoge("No implementation in getGsmBroadcastConfigResponse");
    }

    public void getHardwareConfigResponse(RadioResponseInfo info, ArrayList<HardwareConfig> arrayList) {
        riljLoge("No implementation in getHardwareConfigResponse");
    }

    public void getIMSIForAppResponse(RadioResponseInfo info, String arg1) {
        riljLoge("No implementation in getIMSIForAppResponse");
    }

    public void getIccCardStatusResponse(RadioResponseInfo info, CardStatus arg1) {
        riljLoge("No implementation in getIccCardStatusResponse");
    }

    public void getImsRegistrationStateResponse(RadioResponseInfo info, boolean arg1, int arg2) {
        riljLoge("No implementation in getImsRegistrationStateResponse");
    }

    public void getLastCallFailCauseResponse(RadioResponseInfo info, LastCallFailCauseInfo arg1) {
        riljLoge("No implementation in getLastCallFailCauseResponse");
    }

    public void getModemActivityInfoResponse(RadioResponseInfo info, ActivityStatsInfo arg1) {
        riljLoge("No implementation in getModemActivityInfoResponse");
    }

    public void getMuteResponse(RadioResponseInfo info, boolean arg1) {
        riljLoge("No implementation in getMuteResponse");
    }

    public void getNeighboringCidsResponse(RadioResponseInfo info, ArrayList<NeighboringCell> arrayList) {
        riljLoge("No implementation in getNeighboringCidsResponse");
    }

    public void getNetworkSelectionModeResponse(RadioResponseInfo info, boolean arg1) {
        riljLoge("No implementation in getNetworkSelectionModeResponse");
    }

    public void getOperatorResponse(RadioResponseInfo info, String arg1, String arg2, String arg3) {
        riljLoge("No implementation in getOperatorResponse");
    }

    public void getPreferredNetworkTypeResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in getPreferredNetworkTypeResponse");
    }

    public void getPreferredVoicePrivacyResponse(RadioResponseInfo info, boolean arg1) {
        riljLoge("No implementation in getPreferredVoicePrivacyResponse");
    }

    public void getRadioCapabilityResponse(RadioResponseInfo info, RadioCapability arg1) {
        riljLoge("No implementation in getRadioCapabilityResponse");
    }

    public void getSignalStrengthResponse(RadioResponseInfo info, SignalStrength arg1) {
        riljLoge("No implementation in getSignalStrengthResponse");
    }

    public void getSmscAddressResponse(RadioResponseInfo info, String arg1) {
        riljLoge("No implementation in getSmscAddressResponse");
    }

    public void getTTYModeResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in getTTYModeResponse");
    }

    public void getVoiceRadioTechnologyResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in getVoiceRadioTechnologyResponse");
    }

    public void getVoiceRegistrationStateResponse(RadioResponseInfo info, VoiceRegStateResult arg1) {
        riljLoge("No implementation in getVoiceRegistrationStateResponse");
    }

    public void handleStkCallSetupRequestFromSimResponse(RadioResponseInfo info) {
        riljLoge("No implementation in handleStkCallSetupRequestFromSimResponse");
    }

    public void hangupConnectionResponse(RadioResponseInfo info) {
        riljLoge("No implementation in hangupConnectionResponse");
    }

    public void hangupForegroundResumeBackgroundResponse(RadioResponseInfo info) {
        riljLoge("No implementation in hangupForegroundResumeBackgroundResponse");
    }

    public void hangupWaitingOrBackgroundResponse(RadioResponseInfo info) {
        riljLoge("No implementation in hangupWaitingOrBackgroundResponse");
    }

    public void iccCloseLogicalChannelResponse(RadioResponseInfo info) {
        riljLoge("No implementation in iccCloseLogicalChannelResponse");
    }

    public void iccIOForAppResponse(RadioResponseInfo info, IccIoResult arg1) {
        riljLoge("No implementation in iccIOForAppResponse");
    }

    public void iccOpenLogicalChannelResponse(RadioResponseInfo info, int arg1, ArrayList<Byte> arrayList) {
        riljLoge("No implementation in iccOpenLogicalChannelResponse");
    }

    public void iccTransmitApduBasicChannelResponse(RadioResponseInfo info, IccIoResult arg1) {
        riljLoge("No implementation in iccTransmitApduBasicChannelResponse");
    }

    public void iccTransmitApduLogicalChannelResponse(RadioResponseInfo info, IccIoResult arg1) {
        riljLoge("No implementation in iccTransmitApduLogicalChannelResponse");
    }

    public void nvReadItemResponse(RadioResponseInfo info, String arg1) {
        riljLoge("No implementation in nvReadItemResponse");
    }

    public void nvResetConfigResponse(RadioResponseInfo info) {
        riljLoge("No implementation in nvResetConfigResponse");
    }

    public void nvWriteCdmaPrlResponse(RadioResponseInfo info) {
        riljLoge("No implementation in nvWriteCdmaPrlResponse");
    }

    public void nvWriteItemResponse(RadioResponseInfo info) {
        riljLoge("No implementation in nvWriteItemResponse");
    }

    public void pullLceDataResponse(RadioResponseInfo info, LceDataInfo arg1) {
        riljLoge("No implementation in pullLceDataResponse");
    }

    public void rejectCallResponse(RadioResponseInfo info) {
        riljLoge("No implementation in rejectCallResponse");
    }

    public void reportSmsMemoryStatusResponse(RadioResponseInfo info) {
        riljLoge("No implementation in reportSmsMemoryStatusResponse");
    }

    public void reportStkServiceIsRunningResponse(RadioResponseInfo info) {
        riljLoge("No implementation in reportStkServiceIsRunningResponse");
    }

    public void requestIccSimAuthenticationResponse(RadioResponseInfo info, IccIoResult arg1) {
        riljLoge("No implementation in requestIccSimAuthenticationResponse");
    }

    public void requestIsimAuthenticationResponse(RadioResponseInfo info, String arg1) {
        riljLoge("No implementation in requestIsimAuthenticationResponse");
    }

    public void requestShutdownResponse(RadioResponseInfo info) {
        riljLoge("No implementation in requestShutdownResponse");
    }

    public void sendBurstDtmfResponse(RadioResponseInfo info) {
        riljLoge("No implementation in sendBurstDtmfResponse");
    }

    public void sendCDMAFeatureCodeResponse(RadioResponseInfo info) {
        riljLoge("No implementation in sendCDMAFeatureCodeResponse");
    }

    public void sendCdmaSmsResponse(RadioResponseInfo info, SendSmsResult arg1) {
        riljLoge("No implementation in sendCdmaSmsResponse");
    }

    public void sendDeviceStateResponse(RadioResponseInfo info) {
        riljLoge("No implementation in sendDeviceStateResponse");
    }

    public void sendDtmfResponse(RadioResponseInfo info) {
        riljLoge("No implementation in sendDtmfResponse");
    }

    public void sendEnvelopeResponse(RadioResponseInfo info, String arg1) {
        riljLoge("No implementation in sendEnvelopeResponse");
    }

    public void sendEnvelopeWithStatusResponse(RadioResponseInfo info, IccIoResult arg1) {
        riljLoge("No implementation in sendEnvelopeWithStatusResponse");
    }

    public void sendImsSmsResponse(RadioResponseInfo info, SendSmsResult arg1) {
        riljLoge("No implementation in sendImsSmsResponse");
    }

    public void sendSMSExpectMoreResponse(RadioResponseInfo info, SendSmsResult arg1) {
        riljLoge("No implementation in sendSMSExpectMoreResponse");
    }

    public void sendSmsResponse(RadioResponseInfo info, SendSmsResult arg1) {
        riljLoge("No implementation in sendSmsResponse");
    }

    public void sendTerminalResponseToSimResponse(RadioResponseInfo info) {
        riljLoge("No implementation in sendTerminalResponseToSimResponse");
    }

    public void sendUssdResponse(RadioResponseInfo info) {
        riljLoge("No implementation in sendUssdResponse");
    }

    public void separateConnectionResponse(RadioResponseInfo info) {
        riljLoge("No implementation in separateConnectionResponse");
    }

    public void setAllowedCarriersResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in setAllowedCarriersResponse");
    }

    public void setBandModeResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setBandModeResponse");
    }

    public void setBarringPasswordResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setBarringPasswordResponse");
    }

    public void setCallForwardResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setCallForwardResponse");
    }

    public void setCallWaitingResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setCallWaitingResponse");
    }

    public void setCdmaBroadcastActivationResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setCdmaBroadcastActivationResponse");
    }

    public void setCdmaBroadcastConfigResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setCdmaBroadcastConfigResponse");
    }

    public void setCdmaRoamingPreferenceResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setCdmaRoamingPreferenceResponse");
    }

    public void setCdmaSubscriptionSourceResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setCdmaSubscriptionSourceResponse");
    }

    public void setCellInfoListRateResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setCellInfoListRateResponse");
    }

    public void setClirResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setClirResponse");
    }

    public void setDataAllowedResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setDataAllowedResponse");
    }

    public void setDataProfileResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setDataProfileResponse");
    }

    public void setFacilityLockForAppResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in setFacilityLockForAppResponse");
    }

    public void setGsmBroadcastActivationResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setGsmBroadcastActivationResponse");
    }

    public void setGsmBroadcastConfigResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setGsmBroadcastConfigResponse");
    }

    public void setIndicationFilterResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setIndicationFilterResponse");
    }

    public void setInitialAttachApnResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setInitialAttachApnResponse");
    }

    public void setLocationUpdatesResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setLocationUpdatesResponse");
    }

    public void setMuteResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setMuteResponse");
    }

    public void setNetworkSelectionModeAutomaticResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setNetworkSelectionModeAutomaticResponse");
    }

    public void setNetworkSelectionModeManualResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setNetworkSelectionModeManualResponse");
    }

    public void setPreferredNetworkTypeResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setPreferredNetworkTypeResponse");
    }

    public void setPreferredVoicePrivacyResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setPreferredVoicePrivacyResponse");
    }

    public void setRadioCapabilityResponse(RadioResponseInfo info, RadioCapability arg1) {
        riljLoge("No implementation in setRadioCapabilityResponse");
    }

    public void setRadioPowerResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setRadioPowerResponse");
    }

    public void setSimCardPowerResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setSimCardPowerResponse");
    }

    public void setSmscAddressResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setSmscAddressResponse");
    }

    public void setSuppServiceNotificationsResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setSuppServiceNotificationsResponse");
    }

    public void setTTYModeResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setTTYModeResponse");
    }

    public void setUiccSubscriptionResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setUiccSubscriptionResponse");
    }

    public void setupDataCallResponse(RadioResponseInfo info, SetupDataCallResult arg1) {
        riljLoge("No implementation in setupDataCallResponse");
    }

    public void startDtmfResponse(RadioResponseInfo info) {
        riljLoge("No implementation in startDtmfResponse");
    }

    public void startLceServiceResponse(RadioResponseInfo info, LceStatusInfo arg1) {
        riljLoge("No implementation in startLceServiceResponse");
    }

    public void stopDtmfResponse(RadioResponseInfo info) {
        riljLoge("No implementation in stopDtmfResponse");
    }

    public void stopLceServiceResponse(RadioResponseInfo info, LceStatusInfo arg1) {
        riljLoge("No implementation in stopLceServiceResponse");
    }

    public void supplyIccPin2ForAppResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in supplyIccPin2ForAppResponse");
    }

    public void supplyIccPinForAppResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in supplyIccPinForAppResponse");
    }

    public void supplyIccPuk2ForAppResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in supplyIccPuk2ForAppResponse");
    }

    public void supplyIccPukForAppResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in supplyIccPukForAppResponse");
    }

    public void supplyNetworkDepersonalizationResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in supplyNetworkDepersonalizationResponse");
    }

    public void switchWaitingOrHoldingAndActiveResponse(RadioResponseInfo info) {
        riljLoge("No implementation in switchWaitingOrHoldingAndActiveResponse");
    }

    public void writeSmsToRuimResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in writeSmsToRuimResponse");
    }

    public void writeSmsToSimResponse(RadioResponseInfo info, int arg1) {
        riljLoge("No implementation in writeSmsToSimResponse");
    }

    public void startNetworkScanResponse(RadioResponseInfo info) {
        riljLoge("No implementation in startNetworkScanResponse");
    }

    public void stopKeepaliveResponse(RadioResponseInfo info) {
        riljLoge("No implementation in stopKeepaliveResponse");
    }

    public void setCarrierInfoForImsiEncryptionResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setCarrierInfoForImsiEncryptionResponse");
    }

    public void stopNetworkScanResponse(RadioResponseInfo info) {
        riljLoge("No implementation in stopNetworkScanResponse");
    }

    public void setSimCardPowerResponse_1_1(RadioResponseInfo info) {
        riljLoge("No implementation in setSimCardPowerResponse_1_1");
    }

    public void startKeepaliveResponse(RadioResponseInfo info, KeepaliveStatus status) {
        riljLoge("No implementation in startKeepaliveResponse");
    }

    public void getSignalStrengthResponse_1_2(RadioResponseInfo info, android.hardware.radio.V1_2.SignalStrength signalStrength) {
        riljLoge("No implementation in getSignalStrengthResponse_1_2");
    }

    public void getCurrentCallsResponse_1_2(RadioResponseInfo info, ArrayList<android.hardware.radio.V1_2.Call> arrayList) {
        riljLoge("No implementation in getCurrentCallsResponse_1_2");
    }

    public void setLinkCapacityReportingCriteriaResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setLinkCapacityReportingCriteriaResponse");
    }

    public void setSignalStrengthReportingCriteriaResponse(RadioResponseInfo info) {
        riljLoge("No implementation in setSignalStrengthReportingCriteriaResponse");
    }

    public void getIccCardStatusResponse_1_2(RadioResponseInfo info, android.hardware.radio.V1_2.CardStatus cardStatus) {
        riljLoge("No implementation in getIccCardStatusResponse_1_2");
    }

    public void getCellInfoListResponse_1_2(RadioResponseInfo info, ArrayList<android.hardware.radio.V1_2.CellInfo> arrayList) {
        riljLoge("No implementation in getCellInfoListResponse_1_2");
    }

    public void getDataRegistrationStateResponse_1_2(RadioResponseInfo info, android.hardware.radio.V1_2.DataRegStateResult dataRegResponse) {
        riljLoge("No implementation in getDataRegistrationStateResponse_1_2");
    }

    public void getVoiceRegistrationStateResponse_1_2(RadioResponseInfo info, android.hardware.radio.V1_2.VoiceRegStateResult voiceRegStateResult) {
        riljLoge("No implementation in getVoiceRegistrationStateResponse_1_2");
    }

    public void setSystemSelectionChannelsResponse(RadioResponseInfo responseInfo) {
        riljLoge("No implementation in setSystemSelectionChannelsResponse");
    }

    public void enableModemResponse(RadioResponseInfo responseInfo) {
        riljLoge("No implementation in enableModemResponse");
    }

    public void getModemStackStatusResponse(RadioResponseInfo responseInfo, boolean isEnabled) {
        riljLoge("No implementation in getModemStackStatusResponse");
    }

    public void emergencyDialResponse(RadioResponseInfo responseInfo) {
        riljLoge("No implementation in emergencyDialResponse");
    }

    public void startNetworkScanResponse_1_4(RadioResponseInfo responseInfo) {
        riljLoge("No implementation in startNetworkScanResponse_1_4");
    }

    public void getCellInfoListResponse_1_4(RadioResponseInfo responseInfo, ArrayList<android.hardware.radio.V1_4.CellInfo> arrayList) {
        riljLoge("No implementation in getCellInfoListResponse_1_4");
    }

    public void getDataRegistrationStateResponse_1_4(RadioResponseInfo responseInfo, android.hardware.radio.V1_4.DataRegStateResult dataRegResponse) {
        riljLoge("No implementation in getDataRegistrationStateResponse_1_4");
    }

    public void getIccCardStatusResponse_1_4(RadioResponseInfo responseInfo, android.hardware.radio.V1_4.CardStatus cardStatus) {
        riljLoge("No implementation in getIccCardStatusResponse_1_4");
    }

    public void getPreferredNetworkTypeBitmapResponse(RadioResponseInfo responseInfo, int halRadioAccessFamilyBitmap) {
        riljLoge("No implementation in getPreferredNetworkTypeBitmapResponse");
    }

    public void setPreferredNetworkTypeBitmapResponse(RadioResponseInfo responseInfo) {
        riljLoge("No implementation in setPreferredNetworkTypeBitmapResponse");
    }

    public void getDataCallListResponse_1_4(RadioResponseInfo responseInfo, ArrayList<android.hardware.radio.V1_4.SetupDataCallResult> arrayList) {
        riljLoge("No implementation in getDataCallListResponse_1_4");
    }

    public void setupDataCallResponse_1_4(RadioResponseInfo responseInfo, android.hardware.radio.V1_4.SetupDataCallResult setupDataCallResult) {
        riljLoge("No implementation in setupDataCallResponse_1_4");
    }

    public void setAllowedCarriersResponse_1_4(RadioResponseInfo responseInfo) {
        riljLoge("No implementation in setAllowedCarriersResponse_1_4");
    }

    public void getAllowedCarriersResponse_1_4(RadioResponseInfo responseInfo, CarrierRestrictionsWithPriority carrierRestrictions, int multiSimPolicy) {
        riljLoge("No implementation in getAllowedCarriersResponse_1_4");
    }

    public void getSignalStrengthResponse_1_4(RadioResponseInfo responseInfo, android.hardware.radio.V1_4.SignalStrength signalStrength) {
        riljLoge("No implementation in getSignalStrengthResponse_1_4");
    }

    /* access modifiers changed from: protected */
    public void riljLoge(String msg) {
    }
}
