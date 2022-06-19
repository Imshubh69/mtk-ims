package com.mediatek.ims.ext;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Message;
import android.telephony.ims.ImsReasonInfo;
import com.mediatek.ims.SipMessage;
import com.mediatek.ims.internal.IMtkImsCallSession;

public class OpImsCallSessionProxyBase implements OpImsCallSessionProxy {
    public void broadcastForNotRingingMtIfRequired(boolean sipSessionProgress, int state, int serviceId, String callNumber, Context context) {
    }

    public void deviceSwitch(Object imsRILAdapter, String number, String deviceId, Message response) {
    }

    public void cancelDeviceSwitch(Object imsRILAdapter) {
    }

    public void handleDeviceSwitchResponse(IMtkImsCallSession imsCallSession, AsyncResult result) {
    }

    public boolean handleDeviceSwitchResult(String callId, IMtkImsCallSession imsCallSession, AsyncResult result) {
        return false;
    }

    public boolean isValidVtDialString(String number) {
        return true;
    }

    public String normalizeVtDialString(String number) {
        return number;
    }

    public boolean isDeviceSwitching() {
        return false;
    }

    public ImsReasonInfo getImsReasonInfo(SipMessage sipMsg) {
        return null;
    }

    public boolean handleCallStartFailed(IMtkImsCallSession imsCallSession, Object imsRILAdapter, boolean hasHoldingCall) {
        return false;
    }

    public boolean handleHangup() {
        return false;
    }

    public void sendCallEventWithRat(Bundle extras) {
    }
}
