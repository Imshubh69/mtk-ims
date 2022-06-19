package com.mediatek.ims.ext;

import android.os.Message;
import android.telephony.ims.ImsCallProfile;
import com.mediatek.ims.SipMessage;

public class DigitsUtilBase implements DigitsUtil {
    public boolean hasDialFrom(ImsCallProfile profile) {
        return false;
    }

    public void startFrom(String callee, ImsCallProfile profile, int clirMode, boolean isVideoCall, Object imsRILAdapter, Message response) {
    }

    public void putMtToNumber(String toNumber, ImsCallProfile imsCallProfile) {
    }

    public void sendUssiFrom(Object imsRILAdapter, ImsCallProfile profile, int action, String ussi, Message response) {
    }

    public void cancelUssiFrom(Object imsRILAdapter, ImsCallProfile profile, Message response) {
    }

    public void updateCallExtras(ImsCallProfile destCallProfile, ImsCallProfile srcCallProfile) {
    }

    public boolean isRejectedByOthers(SipMessage msg) {
        return false;
    }
}
