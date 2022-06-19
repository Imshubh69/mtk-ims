package com.mediatek.ims.ril;

import android.hardware.radio.V1_0.CdmaCallWaiting;
import android.hardware.radio.V1_0.CdmaInformationRecords;
import android.hardware.radio.V1_0.CdmaSignalInfoRecord;
import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.hardware.radio.V1_0.CellInfo;
import android.hardware.radio.V1_0.HardwareConfig;
import android.hardware.radio.V1_0.LceDataInfo;
import android.hardware.radio.V1_0.PcoDataInfo;
import android.hardware.radio.V1_0.RadioCapability;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.hardware.radio.V1_0.SignalStrength;
import android.hardware.radio.V1_0.SimRefreshResult;
import android.hardware.radio.V1_0.StkCcUnsolSsResult;
import android.hardware.radio.V1_0.SuppSvcNotification;
import android.hardware.radio.V1_1.KeepaliveStatus;
import android.hardware.radio.V1_1.NetworkScanResult;
import android.hardware.radio.V1_2.LinkCapacityEstimate;
import android.hardware.radio.V1_2.PhysicalChannelConfig;
import android.hardware.radio.V1_4.EmergencyNumber;
import android.hardware.radio.V1_4.IRadioIndication;
import java.util.ArrayList;

public class RadioIndicationBase extends IRadioIndication.Stub {
    public void callRing(int type, boolean data, CdmaSignalInfoRecord arg2) {
        riljLoge("No implementation in callRing");
    }

    public void callStateChanged(int type) {
        riljLoge("No implementation in callStateChanged");
    }

    public void cdmaCallWaiting(int type, CdmaCallWaiting data) {
        riljLoge("No implementation in cdmaCallWaiting");
    }

    public void cdmaInfoRec(int type, CdmaInformationRecords data) {
        riljLoge("No implementation in cdmaInfoRec");
    }

    public void cdmaNewSms(int type, CdmaSmsMessage data) {
        riljLoge("No implementation in cdmaNewSms");
    }

    public void cdmaOtaProvisionStatus(int type, int data) {
        riljLoge("No implementation in cdmaOtaProvisionStatus");
    }

    public void cdmaPrlChanged(int type, int data) {
        riljLoge("No implementation in cdmaPrlChanged");
    }

    public void cdmaRuimSmsStorageFull(int type) {
        riljLoge("No implementation in cdmaRuimSmsStorageFull");
    }

    public void cdmaSubscriptionSourceChanged(int type, int data) {
        riljLoge("No implementation in cdmaSubscriptionSourceChanged");
    }

    public void cellInfoList(int type, ArrayList<CellInfo> arrayList) {
        riljLoge("No implementation in cellInfoList");
    }

    public void currentSignalStrength(int type, SignalStrength data) {
        riljLoge("No implementation in currentSignalStrength");
    }

    public void dataCallListChanged(int type, ArrayList<SetupDataCallResult> arrayList) {
        riljLoge("No implementation in dataCallListChanged");
    }

    public void enterEmergencyCallbackMode(int type) {
        riljLoge("No implementation in enterEmergencyCallbackMode");
    }

    public void exitEmergencyCallbackMode(int type) {
        riljLoge("No implementation in exitEmergencyCallbackMode");
    }

    public void hardwareConfigChanged(int type, ArrayList<HardwareConfig> arrayList) {
        riljLoge("No implementation in hardwareConfigChanged");
    }

    public void imsNetworkStateChanged(int type) {
        riljLoge("No implementation in imsNetworkStateChanged");
    }

    public void indicateRingbackTone(int type, boolean data) {
        riljLoge("No implementation in indicateRingbackTone");
    }

    public void lceData(int type, LceDataInfo data) {
        riljLoge("No implementation in lceData");
    }

    public void modemReset(int type, String data) {
        riljLoge("No implementation in modemReset");
    }

    public void networkStateChanged(int type) {
        riljLoge("No implementation in networkStateChanged");
    }

    public void newBroadcastSms(int type, ArrayList<Byte> arrayList) {
        riljLoge("No implementation in newBroadcastSms");
    }

    public void newSms(int type, ArrayList<Byte> arrayList) {
        riljLoge("No implementation in newSms");
    }

    public void newSmsOnSim(int type, int data) {
        riljLoge("No implementation in newSmsOnSim");
    }

    public void newSmsStatusReport(int type, ArrayList<Byte> arrayList) {
        riljLoge("No implementation in newSmsStatusReport");
    }

    public void nitzTimeReceived(int type, String data, long arg2) {
        riljLoge("No implementation in nitzTimeReceived");
    }

    public void onSupplementaryServiceIndication(int type, StkCcUnsolSsResult data) {
        riljLoge("No implementation in onSupplementaryServiceIndication");
    }

    public void onUssd(int type, int data, String arg2) {
        riljLoge("No implementation in onUssd");
    }

    public void pcoData(int type, PcoDataInfo data) {
        riljLoge("No implementation in pcoData");
    }

    public void radioCapabilityIndication(int type, RadioCapability data) {
        riljLoge("No implementation in radioCapabilityIndication");
    }

    public void radioStateChanged(int type, int data) {
        riljLoge("No implementation in radioStateChanged");
    }

    public void resendIncallMute(int type) {
        riljLoge("No implementation in resendIncallMute");
    }

    public void restrictedStateChanged(int type, int data) {
        riljLoge("No implementation in restrictedStateChanged");
    }

    public void rilConnected(int type) {
        riljLoge("No implementation in rilConnected");
    }

    public void simRefresh(int type, SimRefreshResult data) {
        riljLoge("No implementation in simRefresh");
    }

    public void simSmsStorageFull(int type) {
        riljLoge("No implementation in simSmsStorageFull");
    }

    public void simStatusChanged(int type) {
        riljLoge("No implementation in simStatusChanged");
    }

    public void srvccStateNotify(int type, int data) {
        riljLoge("No implementation in srvccStateNotify");
    }

    public void stkCallControlAlphaNotify(int type, String data) {
        riljLoge("No implementation in stkCallControlAlphaNotify");
    }

    public void stkCallSetup(int type, long data) {
        riljLoge("No implementation in stkCallSetup");
    }

    public void stkEventNotify(int type, String data) {
        riljLoge("No implementation in stkEventNotify");
    }

    public void stkProactiveCommand(int type, String data) {
        riljLoge("No implementation in stkProactiveCommand");
    }

    public void stkSessionEnd(int type) {
        riljLoge("No implementation in stkSessionEnd");
    }

    public void subscriptionStatusChanged(int type, boolean data) {
        riljLoge("No implementation in subscriptionStatusChanged");
    }

    public void suppSvcNotify(int type, SuppSvcNotification data) {
        riljLoge("No implementation in suppSvcNotify");
    }

    public void voiceRadioTechChanged(int type, int data) {
        riljLoge("No implementation in voiceRadioTechChanged");
    }

    public void keepaliveStatus(int type, KeepaliveStatus status) {
        riljLoge("No implementation in keepaliveStatus");
    }

    public void carrierInfoForImsiEncryption(int type) {
        riljLoge("No implementation in carrierInfoForImsiEncryption");
    }

    public void networkScanResult(int type, NetworkScanResult result) {
        riljLoge("No implementation in networkScanResult");
    }

    public void currentSignalStrength_1_2(int type, android.hardware.radio.V1_2.SignalStrength signalStrength) {
        riljLoge("No implementation in currentSignalStrength_1_2");
    }

    public void currentPhysicalChannelConfigs(int type, ArrayList<PhysicalChannelConfig> arrayList) {
        riljLoge("No implementation in currentPhysicalChannelConfigs");
    }

    public void currentLinkCapacityEstimate(int type, LinkCapacityEstimate lce) {
        riljLoge("No implementation in currentLinkCapacityEstimate");
    }

    public void cellInfoList_1_2(int type, ArrayList<android.hardware.radio.V1_2.CellInfo> arrayList) {
        riljLoge("No implementation in cellInfoList_1_2");
    }

    public void networkScanResult_1_2(int type, android.hardware.radio.V1_2.NetworkScanResult result) {
        riljLoge("No implementation in networkScanResult_1_2");
    }

    public void currentEmergencyNumberList(int indicationType, ArrayList<EmergencyNumber> arrayList) {
        riljLoge("No implementation in currentEmergencyNumberList");
    }

    public void cellInfoList_1_4(int indicationType, ArrayList<android.hardware.radio.V1_4.CellInfo> arrayList) {
        riljLoge("No implementation in cellInfoList_1_4");
    }

    public void networkScanResult_1_4(int indicationType, android.hardware.radio.V1_4.NetworkScanResult result) {
        riljLoge("No implementation in networkScanResult_1_4");
    }

    public void currentPhysicalChannelConfigs_1_4(int indicationType, ArrayList<android.hardware.radio.V1_4.PhysicalChannelConfig> arrayList) {
        riljLoge("No implementation in currentPhysicalChannelConfigs_1_4");
    }

    public void dataCallListChanged_1_4(int indicationType, ArrayList<android.hardware.radio.V1_4.SetupDataCallResult> arrayList) {
        riljLoge("No implementation in dataCallListChanged_1_4");
    }

    public void currentSignalStrength_1_4(int indicationType, android.hardware.radio.V1_4.SignalStrength signalStrength) {
        riljLoge("No implementation in currentSignalStrength_1_4");
    }

    /* access modifiers changed from: protected */
    public void riljLoge(String msg) {
    }
}
