package com.mediatek.wfo.ril;

import android.os.AsyncResult;
import java.util.ArrayList;
import vendor.mediatek.hardware.mtkradioex.V3_0.IMwiRadioIndication;

public class MwiRadioIndication extends IMwiRadioIndication.Stub {
    private int mPhoneId;
    private MwiRIL mRil;

    MwiRadioIndication(MwiRIL ril, int phoneId) {
        this.mRil = ril;
        this.mPhoneId = phoneId;
        ril.riljLogv("MwiRadioIndication, phone = " + this.mPhoneId);
    }

    public void onWifiMonitoringThreshouldChanged(int type, ArrayList<Integer> indStgs) {
        this.mRil.processIndication(type);
        this.mRil.unsljLogRet(MwiRILConstants.RIL_UNSOL_WIFI_RSSI_MONITORING_CONFIG, indStgs);
        if (this.mRil.mRssiThresholdChangedRegistrants != null) {
            this.mRil.mRssiThresholdChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, convertArrayListToIntArray(indStgs), (Throwable) null));
        }
    }

    public void onWifiPdnActivate(int type, ArrayList<Integer> indStgs) {
        this.mRil.processIndication(type);
        this.mRil.unsljLogRet(MwiRILConstants.RIL_UNSOL_ACTIVE_WIFI_PDN_COUNT, indStgs);
        if (this.mRil.mWifiPdnActivatedRegistrants != null) {
            this.mRil.mWifiPdnActivatedRegistrants.notifyRegistrants(new AsyncResult((Object) null, convertArrayListToIntArray(indStgs), (Throwable) null));
        }
    }

    public void onWfcPdnError(int type, ArrayList<Integer> indStgs) {
        this.mRil.processIndication(type);
        this.mRil.unsljLogRet(MwiRILConstants.RIL_UNSOL_WIFI_PDN_ERROR, indStgs);
        if (this.mRil.mWifiPdnErrorRegistrants != null) {
            this.mRil.mWifiPdnErrorRegistrants.notifyRegistrants(new AsyncResult((Object) null, convertArrayListToIntArray(indStgs), (Throwable) null));
        }
    }

    public void onPdnHandover(int type, ArrayList<Integer> indStgs) {
        this.mRil.processIndication(type);
        this.mRil.unsljLogRet(MwiRILConstants.RIL_UNSOL_MOBILE_WIFI_HANDOVER, indStgs);
        if (this.mRil.mWifiPdnHandoverRegistrants != null) {
            this.mRil.mWifiPdnHandoverRegistrants.notifyRegistrants(new AsyncResult((Object) null, convertArrayListToIntArray(indStgs), (Throwable) null));
        }
    }

    public void onWifiRoveout(int type, ArrayList<String> indStgs) {
        this.mRil.processIndication(type);
        this.mRil.unsljLogRet(MwiRILConstants.RIL_UNSOL_MOBILE_WIFI_ROVEOUT, indStgs);
        indStgs.add(Integer.toString(this.mRil.mPhoneId.intValue()));
        String[] ret = (String[]) indStgs.toArray(new String[indStgs.size()]);
        if (this.mRil.mWifiPdnRoveOutRegistrants != null) {
            this.mRil.mWifiPdnRoveOutRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void onLocationRequest(int type, ArrayList<String> indStgs) {
        this.mRil.processIndication(type);
        this.mRil.unsljLog(MwiRILConstants.RIL_UNSOL_REQUEST_GEO_LOCATION);
        indStgs.add(Integer.toString(this.mRil.mPhoneId.intValue()));
        String[] ret = (String[]) indStgs.toArray(new String[indStgs.size()]);
        if (this.mRil.mRequestGeoLocationRegistrants != null) {
            this.mRil.mRequestGeoLocationRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void onWfcPdnStateChanged(int type, ArrayList<Integer> indStgs) {
        this.mRil.processIndication(type);
        this.mRil.unsljLogRet(MwiRILConstants.RIL_UNSOL_WFC_PDN_STATE, indStgs);
        if (this.mRil.mWfcPdnStateChangedRegistrants != null) {
            this.mRil.mWfcPdnStateChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, convertArrayListToIntArray(indStgs), (Throwable) null));
        }
    }

    public void onNattKeepAliveChanged(int type, ArrayList<String> indStgs) {
        this.mRil.processIndication(type);
        this.mRil.unsljLog(MwiRILConstants.RIL_UNSOL_NATT_KEEP_ALIVE_CHANGED);
        indStgs.add(Integer.toString(this.mRil.mPhoneId.intValue()));
        String[] ret = (String[]) indStgs.toArray(new String[indStgs.size()]);
        if (this.mRil.mNattKeepAliveChangedRegistrants != null) {
            this.mRil.mNattKeepAliveChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void onWifiPdnOOS(int type, ArrayList<String> indStgs) {
        this.mRil.processIndication(type);
        this.mRil.unsljLogRet(MwiRILConstants.RIL_UNSOL_WIFI_PDN_OOS, indStgs);
        indStgs.add(Integer.toString(this.mRil.mPhoneId.intValue()));
        String[] ret = (String[]) indStgs.toArray(new String[indStgs.size()]);
        if (this.mRil.mWifiPdnOosRegistrants != null) {
            this.mRil.mWifiPdnOosRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void onWifiLock(int type, ArrayList<String> indStgs) {
        this.mRil.processIndication(type);
        this.mRil.unsljLogRet(3127, indStgs);
        indStgs.add(Integer.toString(this.mRil.mPhoneId.intValue()));
        String[] ret = (String[]) indStgs.toArray(new String[indStgs.size()]);
        if (this.mRil.mWifiLockRegistrants != null) {
            this.mRil.mWifiLockRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void onWifiPingRequest(int type, ArrayList<Integer> indStgs) {
        this.mRil.processIndication(type);
        this.mRil.unsljLogRet(MwiRILConstants.RIL_UNSOL_WIFI_PING_REQUEST, indStgs);
        if (this.mRil.mWifiPingRequestRegistrants != null) {
            this.mRil.mWifiPingRequestRegistrants.notifyRegistrants(new AsyncResult((Object) null, convertArrayListToIntArray(indStgs), (Throwable) null));
        }
    }

    private int[] convertArrayListToIntArray(ArrayList<Integer> input) {
        int[] ret = new int[input.size()];
        for (int i = 0; i < input.size(); i++) {
            ret[i] = input.get(i).intValue();
        }
        return ret;
    }
}
