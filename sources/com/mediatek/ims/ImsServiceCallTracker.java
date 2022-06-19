package com.mediatek.ims;

import android.net.Uri;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import android.text.TextUtils;
import android.util.SparseArray;
import com.mediatek.ims.ImsCallInfo;
import com.mediatek.ims.internal.ImsXuiManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImsServiceCallTracker {
    private static final int CALL_MSG_TYPE_ACTIVE = 132;
    private static final int CALL_MSG_TYPE_ACTIVE_BY_REMOTE = 136;
    private static final int CALL_MSG_TYPE_ALERT = 2;
    private static final int CALL_MSG_TYPE_DISCONNECTED = 133;
    private static final int CALL_MSG_TYPE_HELD = 131;
    private static final int CALL_MSG_TYPE_HELD_BY_REMOTE = 135;
    private static final int CALL_MSG_TYPE_ID_ASSIGN = 130;
    private static final int CALL_MSG_TYPE_MT = 0;
    private static final int IMS_VIDEO = 21;
    private static final int IMS_VIDEO_CONF = 23;
    private static final int IMS_VIDEO_CONF_PARTS = 25;
    private static final int IMS_VOICE = 20;
    private static final int IMS_VOICE_CONF = 22;
    private static final int IMS_VOICE_CONF_PARTS = 24;
    private static final int INVALID_CALL = -1;
    private static final String LOG_TAG = "ImsServiceCT";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final boolean SENLOG = TextUtils.equals(Build.TYPE, "user");
    private static final boolean TELDBG;
    private static SparseArray<ImsServiceCallTracker> mImsServiceCTs = new SparseArray<>();
    private ConcurrentHashMap<String, ImsCallInfo> mCallConnections = new ConcurrentHashMap<>();
    private boolean mEnableVolteForImsEcc = false;
    private int mPhoneId;

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        TELDBG = z;
    }

    public static ImsServiceCallTracker getInstance(int phoneId) {
        if (mImsServiceCTs.get(phoneId) == null) {
            mImsServiceCTs.put(phoneId, new ImsServiceCallTracker(phoneId));
        }
        return mImsServiceCTs.get(phoneId);
    }

    private ImsServiceCallTracker(int phoneId) {
        this.mPhoneId = phoneId;
    }

    public boolean getEnableVolteForImsEcc() {
        return this.mEnableVolteForImsEcc;
    }

    public void setEnableVolteForImsEcc(boolean enable) {
        this.mEnableVolteForImsEcc = enable;
    }

    public ImsCallInfo getCallInfo(String callId) {
        if (callId == null) {
            logWithPhoneId("getCallInfo(callId) : callID is null");
            return null;
        }
        ImsCallInfo callInfo = this.mCallConnections.get(callId);
        if (callInfo != null) {
            logWithPhoneId("getCallInfo(callId) : callID: " + callInfo.mCallId + " call num: " + sensitiveEncode(callInfo.mCallNum) + " call State: " + callInfo.mState);
        } else {
            logWithPhoneId("getCallInfo(callId) : callID: " + callId + " is null");
        }
        return callInfo;
    }

    public ImsCallInfo getCallInfo(ImsCallInfo.State state) {
        for (Map.Entry<String, ImsCallInfo> entry : this.mCallConnections.entrySet()) {
            ImsCallInfo callInfo = entry.getValue();
            logWithPhoneId("getCallInfo(state) : callID: " + callInfo.mCallId + " call num: " + sensitiveEncode(callInfo.mCallNum) + " call State: " + callInfo.mState);
            if (callInfo.mState == state) {
                return callInfo;
            }
        }
        return null;
    }

    public void removeCallConnection(String callId, ImsCallSessionProxy callSession) {
        ImsCallInfo imsCallInfo;
        if (callId != null && (imsCallInfo = this.mCallConnections.get(callId)) != null && imsCallInfo.mCallSession == callSession) {
            this.mCallConnections.remove(callId);
        }
    }

    public ImsCallSessionProxy getFgCall() {
        for (Map.Entry<String, ImsCallInfo> entry : this.mCallConnections.entrySet()) {
            ImsCallInfo callInfo = entry.getValue();
            if (ImsCallInfo.State.ACTIVE == callInfo.mState) {
                return callInfo.mCallSession;
            }
        }
        return null;
    }

    public ImsCallSessionProxy getConferenceHostCall() {
        for (Map.Entry<String, ImsCallInfo> entry : this.mCallConnections.entrySet()) {
            ImsCallInfo callInfo = entry.getValue();
            if (callInfo.mIsConferenceHost) {
                return callInfo.mCallSession;
            }
        }
        return null;
    }

    public int getCurrentCallCount() {
        ConcurrentHashMap<String, ImsCallInfo> concurrentHashMap = this.mCallConnections;
        if (concurrentHashMap == null) {
            return 0;
        }
        return concurrentHashMap.size();
    }

    public int getParticipantCallId(String callNumber) {
        int participantCallId = -1;
        Iterator<Map.Entry<String, ImsCallInfo>> it = this.mCallConnections.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ImsCallInfo callInfo = it.next().getValue();
            if (callNumber.equals(callInfo.mCallNum)) {
                participantCallId = Integer.parseInt(callInfo.mCallId);
                break;
            }
        }
        logWithPhoneId("getParticipantCallId() : participantCallId = " + participantCallId);
        return participantCallId;
    }

    public boolean isVoiceCall(int mode) {
        if (20 == mode || 22 == mode || 24 == mode) {
            return true;
        }
        return false;
    }

    public boolean isVideoCall(int mode) {
        if (21 == mode || 23 == mode || 25 == mode) {
            return true;
        }
        return false;
    }

    public boolean isConferenceCall(int mode) {
        if (22 == mode || 24 == mode || 23 == mode || 25 == mode) {
            return true;
        }
        return false;
    }

    public boolean isConferenceCallHost(int mode) {
        if (22 == mode || 23 == mode) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0010  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isInCall() {
        /*
            r5 = this;
            java.util.concurrent.ConcurrentHashMap<java.lang.String, com.mediatek.ims.ImsCallInfo> r0 = r5.mCallConnections
            java.util.Set r0 = r0.entrySet()
            java.util.Iterator r0 = r0.iterator()
        L_0x000a:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x002c
            java.lang.Object r1 = r0.next()
            java.util.Map$Entry r1 = (java.util.Map.Entry) r1
            java.lang.Object r2 = r1.getValue()
            com.mediatek.ims.ImsCallInfo r2 = (com.mediatek.ims.ImsCallInfo) r2
            com.mediatek.ims.ImsCallInfo$State r3 = com.mediatek.ims.ImsCallInfo.State.ACTIVE
            com.mediatek.ims.ImsCallInfo$State r4 = r2.mState
            if (r3 == r4) goto L_0x002a
            com.mediatek.ims.ImsCallInfo$State r3 = com.mediatek.ims.ImsCallInfo.State.HOLDING
            com.mediatek.ims.ImsCallInfo$State r4 = r2.mState
            if (r3 != r4) goto L_0x0029
            goto L_0x002a
        L_0x0029:
            goto L_0x000a
        L_0x002a:
            r0 = 1
            return r0
        L_0x002c:
            r0 = 0
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.ImsServiceCallTracker.isInCall():boolean");
    }

    public boolean isInCall(ImsCallInfo callInfo) {
        if (ImsCallInfo.State.ACTIVE == callInfo.mState || ImsCallInfo.State.HOLDING == callInfo.mState) {
            return true;
        }
        return false;
    }

    public static boolean isEccExistOnAnySlot() {
        for (int i = 0; i < mImsServiceCTs.size(); i++) {
            ImsServiceCallTracker imsServiceCT = mImsServiceCTs.valueAt(i);
            if (imsServiceCT != null && imsServiceCT.isEccExist()) {
                return true;
            }
        }
        return false;
    }

    public boolean isEccExist() {
        for (Map.Entry<String, ImsCallInfo> entry : this.mCallConnections.entrySet()) {
            ImsCallInfo callInfo = entry.getValue();
            if (callInfo.mIsEcc && isInCall(callInfo)) {
                return true;
            }
        }
        return false;
    }

    public boolean isVideoCallExist() {
        for (Map.Entry<String, ImsCallInfo> entry : this.mCallConnections.entrySet()) {
            ImsCallInfo callInfo = entry.getValue();
            if (callInfo.mIsVideo && isInCall(callInfo)) {
                return true;
            }
        }
        return false;
    }

    public boolean isConferenceCallExist() {
        for (Map.Entry<String, ImsCallInfo> entry : this.mCallConnections.entrySet()) {
            ImsCallInfo callInfo = entry.getValue();
            if ((callInfo.mIsConference || callInfo.mIsConferenceHost) && isInCall(callInfo)) {
                return true;
            }
        }
        return false;
    }

    public boolean isConferenceHostCallExist() {
        for (Map.Entry<String, ImsCallInfo> entry : this.mCallConnections.entrySet()) {
            ImsCallInfo callInfo = entry.getValue();
            if (callInfo.mIsConferenceHost && isInCall(callInfo)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSelfAddress(String addr) {
        Uri[] selfUri = ImsXuiManager.getInstance().getSelfIdentifyUri(this.mPhoneId);
        if (selfUri == null) {
            return false;
        }
        for (Uri schemeSpecificPart : selfUri) {
            String[] numberParts = schemeSpecificPart.getSchemeSpecificPart().split("[@;:]");
            if (numberParts.length != 0) {
                logWithPhoneId("isSelfAddress() selfId: " + sensitiveEncode(numberParts[0]) + " addr: " + sensitiveEncode(addr));
                if (PhoneNumberUtils.compareLoosely(addr, numberParts[0])) {
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<String> getSelfAddressList() {
        Uri[] selfUri = ImsXuiManager.getInstance().getSelfIdentifyUri(this.mPhoneId);
        if (selfUri == null) {
            return null;
        }
        ArrayList<String> selfAddressList = new ArrayList<>();
        for (Uri schemeSpecificPart : selfUri) {
            String[] numberParts = schemeSpecificPart.getSchemeSpecificPart().split("[@;:]");
            if (numberParts.length != 0) {
                logWithPhoneId("isSelfAddress() selfId: " + sensitiveEncode(numberParts[0]));
                selfAddressList.add(numberParts[0]);
            }
        }
        return selfAddressList;
    }

    public void processCallInfoIndication(String[] callInfo, ImsCallSessionProxy callSession, ImsCallProfile profile) {
        boolean isConference;
        boolean isConferenceHost;
        boolean isVideo;
        boolean isEcc;
        String callId = callInfo[0];
        int msgType = Integer.parseInt(callInfo[1]);
        int callMode = TextUtils.isEmpty(callInfo[5]) ? -1 : Integer.parseInt(callInfo[5]);
        String callNum = callInfo[6];
        if (isConferenceCall(callMode)) {
            isConference = true;
        } else {
            isConference = false;
        }
        if (isConferenceCallHost(callMode)) {
            isConferenceHost = true;
        } else {
            isConferenceHost = false;
        }
        if (isVideoCall(callMode)) {
            isVideo = true;
        } else {
            isVideo = false;
        }
        if (profile.getServiceType() == 2) {
            isEcc = true;
        } else {
            isEcc = false;
        }
        switch (msgType) {
            case 0:
                boolean isConferenceHost2 = isConferenceHost;
                int i = msgType;
                String callNum2 = callNum;
                boolean isEcc2 = isEcc;
                boolean isConference2 = isConference;
                boolean isVideo2 = isVideo;
                logWithPhoneId("processCallInfoIndication() : CALL_MSG_TYPE_MT => callId = " + callId + ", callMode = " + callMode + ", isConference = " + isConference2 + ", isConferenceHost = " + isConferenceHost2 + ", isVideo = " + isVideo2 + ", isEcc = " + isEcc2);
                boolean z = isConference2;
                ImsCallInfo imsCallInfo = r2;
                boolean z2 = isConferenceHost2;
                boolean z3 = isVideo2;
                ImsCallInfo imsCallInfo2 = new ImsCallInfo(callId, callNum2, isConference2, isConferenceHost2, isVideo2, isEcc2, ImsCallInfo.State.INCOMING, false, callSession);
                this.mCallConnections.put(callId, imsCallInfo);
                return;
            case 2:
                boolean isConferenceHost3 = isConferenceHost;
                boolean isConference3 = isConference;
                int i2 = msgType;
                String str = callNum;
                StringBuilder sb = new StringBuilder();
                sb.append("processCallInfoIndication() : CALL_MSG_TYPE_ALERT => callId = ");
                sb.append(callId);
                sb.append(", callMode = ");
                sb.append(callMode);
                sb.append(", isConference = ");
                sb.append(isConference3);
                sb.append(", isConferenceHost = ");
                sb.append(isConferenceHost3);
                sb.append(", isVideo = ");
                boolean isVideo3 = isVideo;
                sb.append(isVideo3);
                sb.append(", isEcc = ");
                sb.append(isEcc);
                boolean isVideo4 = isVideo3;
                logWithPhoneId(sb.toString());
                ImsCallInfo imsCallInfo3 = this.mCallConnections.get(callId);
                if (imsCallInfo3 != null) {
                    imsCallInfo3.mIsConference = isConference3;
                    imsCallInfo3.mIsConferenceHost = isConferenceHost3;
                    imsCallInfo3.mIsVideo = isVideo4;
                    this.mCallConnections.put(callId, imsCallInfo3);
                    boolean z4 = isConference3;
                    boolean z5 = isConferenceHost3;
                    ImsCallInfo imsCallInfo4 = imsCallInfo3;
                    boolean z6 = isVideo4;
                    return;
                }
                return;
            case 130:
                logWithPhoneId("processCallInfoIndication() : CALL_MSG_TYPE_ID_ASSIGN => callId = " + callId + ", callMode = " + callMode + ", isConference = " + isConference + ", isConferenceHost = " + isConferenceHost + ", isVideo = " + isVideo + ", isEcc = " + isEcc);
                ImsCallInfo.State state = ImsCallInfo.State.ALERTING;
                int i3 = msgType;
                ImsCallInfo imsCallInfo5 = r2;
                String str2 = callNum;
                ConcurrentHashMap<String, ImsCallInfo> concurrentHashMap = this.mCallConnections;
                ImsCallInfo.State state2 = state;
                ImsCallInfo imsCallInfo6 = new ImsCallInfo(callId, callNum, isConference, isConferenceHost, isVideo, isEcc, state, false, callSession);
                concurrentHashMap.put(callId, imsCallInfo5);
                boolean z7 = isConference;
                boolean z8 = isEcc;
                boolean z9 = isVideo;
                boolean isVideo5 = isConferenceHost;
                return;
            case 131:
                logWithPhoneId("processCallInfoIndication() : CALL_MSG_TYPE_HELD => callId = " + callId + ", callMode = " + callMode + ", isConference = " + isConference + ", isConferenceHost = " + isConferenceHost + ", isVideo = " + isVideo + ", isEcc = " + isEcc);
                ImsCallInfo imsCallInfo7 = this.mCallConnections.get(callId);
                if (imsCallInfo7 != null) {
                    imsCallInfo7.mState = ImsCallInfo.State.HOLDING;
                    imsCallInfo7.mIsConference = isConference;
                    imsCallInfo7.mIsConferenceHost = isConferenceHost;
                    this.mCallConnections.put(callId, imsCallInfo7);
                    ImsCallInfo imsCallInfo8 = imsCallInfo7;
                    boolean z10 = isVideo;
                    boolean z11 = isConferenceHost;
                    boolean z12 = isConference;
                    int i4 = msgType;
                    String str3 = callNum;
                    int msgType2 = isEcc;
                    return;
                }
                return;
            case 132:
                logWithPhoneId("processCallInfoIndication() : CALL_MSG_TYPE_ACTIVE => callId = " + callId + ", callMode = " + callMode + ", isConference = " + isConference + ", isConferenceHost = " + isConferenceHost + ", isVideo = " + isVideo + ", isEcc = " + isEcc);
                ImsCallInfo imsCallInfo9 = this.mCallConnections.get(callId);
                if (imsCallInfo9 != null) {
                    imsCallInfo9.mState = ImsCallInfo.State.ACTIVE;
                    imsCallInfo9.mIsConference = isConference;
                    imsCallInfo9.mIsConferenceHost = isConferenceHost;
                    imsCallInfo9.mIsVideo = isVideo;
                    this.mCallConnections.put(callId, imsCallInfo9);
                    ImsCallInfo imsCallInfo10 = imsCallInfo9;
                    boolean z13 = isVideo;
                    boolean z14 = isConferenceHost;
                    boolean z15 = isConference;
                    int i5 = msgType;
                    String str4 = callNum;
                    int msgType3 = isEcc;
                    return;
                }
                return;
            case 133:
                logWithPhoneId("processCallInfoIndication() : CALL_MSG_TYPE_DISCONNECTED => callId = " + callId + ", callMode = " + callMode + ", isConference = " + isConference + ", isConferenceHost = " + isConferenceHost + ", isVideo = " + isVideo + ", isEcc = " + isEcc);
                ImsCallInfo imsCallInfo11 = this.mCallConnections.get(callId);
                if (imsCallInfo11 != null) {
                    imsCallInfo11.mState = ImsCallInfo.State.DISCONNECTED;
                    imsCallInfo11.mIsConference = isConference;
                    imsCallInfo11.mIsConferenceHost = isConferenceHost;
                    imsCallInfo11.mIsVideo = isVideo;
                    this.mCallConnections.put(callId, imsCallInfo11);
                    ImsCallInfo imsCallInfo12 = imsCallInfo11;
                    boolean z16 = isVideo;
                    boolean z17 = isConferenceHost;
                    boolean z18 = isConference;
                    int i6 = msgType;
                    String str5 = callNum;
                    int msgType4 = isEcc;
                    return;
                }
                return;
            case 135:
                logWithPhoneId("processCallInfoIndication() : CALL_MSG_TYPE_HELD_BY_REMOTE => callId = " + callId + ", callMode = " + callMode + ", isConference = " + isConference + ", isConferenceHost = " + isConferenceHost + ", isVideo = " + isVideo + ", isEcc = " + isEcc);
                ImsCallInfo imsCallInfo13 = this.mCallConnections.get(callId);
                if (imsCallInfo13 != null) {
                    imsCallInfo13.mIsRemoteHold = true;
                    this.mCallConnections.put(callId, imsCallInfo13);
                    ImsCallInfo imsCallInfo14 = imsCallInfo13;
                    boolean z19 = isVideo;
                    boolean z20 = isConferenceHost;
                    boolean z21 = isConference;
                    int i7 = msgType;
                    String str6 = callNum;
                    int msgType5 = isEcc;
                    return;
                }
                return;
            case 136:
                logWithPhoneId("processCallInfoIndication() : CALL_MSG_TYPE_ACTIVE_BY_REMOTE => callId = " + callId + ", callMode = " + callMode + ", isConference = " + isConference + ", isConferenceHost = " + isConferenceHost + ", isVideo = " + isVideo + ", isEcc = " + isEcc);
                ImsCallInfo imsCallInfo15 = this.mCallConnections.get(callId);
                if (imsCallInfo15 != null) {
                    imsCallInfo15.mIsRemoteHold = false;
                    this.mCallConnections.put(callId, imsCallInfo15);
                    ImsCallInfo imsCallInfo16 = imsCallInfo15;
                    boolean z22 = isVideo;
                    boolean z23 = isConferenceHost;
                    boolean z24 = isConference;
                    int i8 = msgType;
                    String str7 = callNum;
                    int msgType6 = isEcc;
                    return;
                }
                return;
            default:
                boolean z25 = isVideo;
                boolean z26 = isConferenceHost;
                boolean z27 = isConference;
                int i9 = msgType;
                String str8 = callNum;
                int msgType7 = isEcc;
                return;
        }
    }

    public void processCallModeChangeIndication(String[] callModeInfo) {
        int callMode = -1;
        if (callModeInfo != null) {
            String callId = callModeInfo[0];
            if (callModeInfo[1] != null && !callModeInfo[1].equals("")) {
                try {
                    callMode = Integer.parseInt(callModeInfo[1]);
                } catch (NumberFormatException e) {
                    logWithPhoneId("processCallModeChangeIndication() : callMode is not integer");
                    return;
                }
            }
            logWithPhoneId("processCallModeChangeIndication() :callId = " + callId + ", callMode = " + callMode);
            ImsCallInfo imsCallInfo = this.mCallConnections.get(callId);
            if (imsCallInfo != null) {
                imsCallInfo.mIsVideo = isVideoCall(callMode);
                this.mCallConnections.put(callId, imsCallInfo);
            }
        }
    }

    private void logWithPhoneId(String msg) {
        if (TELDBG) {
            Rlog.d(LOG_TAG, "[PhoneId = " + this.mPhoneId + "] " + msg);
        }
    }

    public static String sensitiveEncode(String msg) {
        if (!SENLOG || TELDBG) {
            return Rlog.pii(LOG_TAG, msg);
        }
        return "[hidden]";
    }
}
