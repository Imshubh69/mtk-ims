package com.mediatek.ims;

public class ImsCallInfo {
    public String mCallId;
    public String mCallNum;
    public ImsCallSessionProxy mCallSession;
    public boolean mIsConference;
    public boolean mIsConferenceHost;
    public boolean mIsEcc;
    public boolean mIsRemoteHold;
    public boolean mIsVideo;
    public State mState;

    public enum State {
        ACTIVE,
        HOLDING,
        ALERTING,
        INCOMING,
        DISCONNECTED,
        INVALID
    }

    public ImsCallInfo(String callId, String callNum, boolean isConference, boolean isConferenceHost, boolean isVideo, boolean isEcc, State state, boolean isRemoteHold, ImsCallSessionProxy callSession) {
        this.mCallId = callId;
        this.mCallNum = callNum;
        this.mIsConference = isConference;
        this.mIsConferenceHost = isConferenceHost;
        this.mIsVideo = isVideo;
        this.mIsEcc = isEcc;
        this.mState = state;
        this.mIsRemoteHold = isRemoteHold;
        this.mCallSession = callSession;
    }
}
