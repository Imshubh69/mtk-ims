package com.mediatek.ims;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsCallSessionListener;
import android.telephony.ims.ImsStreamMediaProfile;
import com.android.ims.internal.IImsCallSession;
import com.mediatek.ims.ImsCallSessionProxy;
import com.mediatek.ims.internal.IMtkImsCallSession;
import com.mediatek.ims.internal.IMtkImsCallSessionListener;
import com.mediatek.ims.ril.ImsCommandsInterface;

public class MtkImsCallSessionProxy implements AutoCloseable {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "MtkImsCallSessionProxy";
    private ImsCallSessionProxy mAospImsCallSessionProxy;
    private IMtkImsCallSessionListener mMtkListener;
    private IMtkImsCallSession mServiceImpl;

    private class ImsCallLogLevel {
        public static final int DEBUG = 2;
        public static final int ERROR = 5;
        public static final int INFO = 3;
        public static final int VERBOSE = 1;
        public static final int WARNNING = 4;

        private ImsCallLogLevel() {
        }
    }

    MtkImsCallSessionProxy(Context context, ImsCallProfile profile, ImsCallSessionListener listener, ImsService imsService, Handler handler, ImsCommandsInterface ci, String callId, int phoneId) {
        this.mServiceImpl = new IMtkImsCallSession.Stub() {
            public void close() {
                MtkImsCallSessionProxy.this.close();
            }

            public String getCallId() {
                return MtkImsCallSessionProxy.this.getCallId();
            }

            public ImsCallProfile getCallProfile() {
                return MtkImsCallSessionProxy.this.getCallProfile();
            }

            public void setListener(IMtkImsCallSessionListener listener) {
                MtkImsCallSessionProxy.this.setListener(listener);
            }

            public IImsCallSession getIImsCallSession() {
                return MtkImsCallSessionProxy.this.getIImsCallSession();
            }

            public void setIImsCallSession(IImsCallSession iSession) {
                MtkImsCallSessionProxy.this.setIImsCallSession(iSession);
            }

            public boolean isIncomingCallMultiparty() {
                return MtkImsCallSessionProxy.this.isIncomingCallMultiparty();
            }

            public void approveEccRedial(boolean isAprroved) {
                MtkImsCallSessionProxy.this.approveEccRedial(isAprroved);
            }

            public void resume() {
                MtkImsCallSessionProxy.this.resume();
            }

            public void callTerminated() {
                MtkImsCallSessionProxy.this.callTerminated();
            }

            public void setImsCallMode(int mode) {
                MtkImsCallSessionProxy.this.setImsCallMode(mode);
            }

            public void removeLastParticipant() {
                MtkImsCallSessionProxy.this.removeLastParticipant();
            }

            public String getHeaderCallId() {
                return MtkImsCallSessionProxy.this.getHeaderCallId();
            }

            public void videoRingtoneOperation(int type, String operation) {
                MtkImsCallSessionProxy.this.videoRingtoneOperation(type, operation);
            }
        };
    }

    MtkImsCallSessionProxy(Context context, ImsCallProfile profile, ImsCallSessionListener listener, ImsService imsService, Handler handler, ImsCommandsInterface ci, int phoneId) {
        this(context, profile, listener, imsService, handler, ci, (String) null, phoneId);
        StringBuilder sb = new StringBuilder();
        sb.append("MtkImsCallSessionProxy() : RILAdapter = ");
        ImsCommandsInterface imsCommandsInterface = ci;
        sb.append(ci);
        logWithCallId(sb.toString(), 2);
    }

    public void close() {
        logWithCallId("close() : MtkImsCallSessionProxy is going to be closed!!! ", 2);
        ImsCallSessionProxy imsCallSessionProxy = this.mAospImsCallSessionProxy;
        if (imsCallSessionProxy != null) {
            imsCallSessionProxy.close();
            this.mAospImsCallSessionProxy = null;
        }
        this.mMtkListener = null;
    }

    public String getCallId() {
        ImsCallSessionProxy imsCallSessionProxy = this.mAospImsCallSessionProxy;
        if (imsCallSessionProxy == null || imsCallSessionProxy.getServiceImpl() == null) {
            logWithCallId("getCallId() : mCallSessionImpl is null", 5);
            return "";
        }
        try {
            return this.mAospImsCallSessionProxy.getServiceImpl().getCallId();
        } catch (RemoteException e) {
            logWithCallId("getCallId() : RemoteException getCallId()", 5);
            return "";
        }
    }

    public ImsCallProfile getCallProfile() {
        ImsCallSessionProxy imsCallSessionProxy = this.mAospImsCallSessionProxy;
        if (imsCallSessionProxy == null || imsCallSessionProxy.getServiceImpl() == null) {
            logWithCallId("getCallProfile() : mCallSessionImpl is null", 5);
            return null;
        }
        try {
            return this.mAospImsCallSessionProxy.getServiceImpl().getCallProfile();
        } catch (RemoteException e) {
            logWithCallId("getCallProfile() : RemoteException getCallProfile()", 5);
            return null;
        }
    }

    public void setListener(IMtkImsCallSessionListener listener) {
        this.mMtkListener = listener;
    }

    public IImsCallSession getIImsCallSession() {
        ImsCallSessionProxy imsCallSessionProxy = this.mAospImsCallSessionProxy;
        if (imsCallSessionProxy != null) {
            return imsCallSessionProxy.getServiceImpl();
        }
        logWithCallId("getIImsCallSession() : mAospImsCallSessionProxy is null", 5);
        return null;
    }

    /* access modifiers changed from: package-private */
    public void setIImsCallSession(IImsCallSession iSession) {
        ImsCallSessionProxy imsCallSessionProxy = this.mAospImsCallSessionProxy;
        if (imsCallSessionProxy != null) {
            imsCallSessionProxy.setServiceImpl(iSession);
        }
    }

    public boolean isIncomingCallMultiparty() {
        ImsCallSessionProxy imsCallSessionProxy = this.mAospImsCallSessionProxy;
        if (imsCallSessionProxy != null && imsCallSessionProxy.getServiceImpl() != null) {
            return this.mAospImsCallSessionProxy.isIncomingCallMultiparty();
        }
        logWithCallId("isIncomingCallMultiparty() : mCallSessionImpl is null", 5);
        return false;
    }

    public void approveEccRedial(boolean isAprroved) {
        ImsCallSessionProxy imsCallSessionProxy = this.mAospImsCallSessionProxy;
        if (imsCallSessionProxy == null || imsCallSessionProxy.getServiceImpl() == null) {
            logWithCallId("approveEccRedial() : mCallSessionImpl is null", 5);
        } else {
            this.mAospImsCallSessionProxy.approveEccRedial(isAprroved);
        }
    }

    public void notifyTextCapabilityChanged(int localCapability, int remoteCapability, int localTextStatus, int realRemoteCapability) {
        IMtkImsCallSessionListener iMtkImsCallSessionListener = this.mMtkListener;
        if (iMtkImsCallSessionListener == null) {
            Rlog.d(LOG_TAG, "notifyTextCapabilityChanged() listener is null");
            return;
        }
        try {
            iMtkImsCallSessionListener.callSessionTextCapabilityChanged(this.mServiceImpl, localCapability, remoteCapability, localTextStatus, realRemoteCapability);
        } catch (RemoteException e) {
            Rlog.e(LOG_TAG, "RemoteException callSessionTextCapabilityChanged()");
        }
    }

    public void notifyRttECCRedialEvent() {
        IMtkImsCallSessionListener iMtkImsCallSessionListener = this.mMtkListener;
        if (iMtkImsCallSessionListener == null) {
            Rlog.d(LOG_TAG, "notifyRttECCRedialEvent() listener is null");
            return;
        }
        try {
            iMtkImsCallSessionListener.callSessionRttEventReceived(this.mServiceImpl, 137);
        } catch (RemoteException e) {
            Rlog.e(LOG_TAG, "RemoteException callSessionRttEventReceived()");
        }
    }

    public void resume() {
        ImsCallSessionProxy imsCallSessionProxy = this.mAospImsCallSessionProxy;
        if (imsCallSessionProxy == null) {
            logWithCallId("resume() : mAospImsCallSessionProxy is null", 5);
        } else {
            imsCallSessionProxy.resume((ImsStreamMediaProfile) null);
        }
    }

    public void callTerminated() {
        ImsCallSessionProxy imsCallSessionProxy = this.mAospImsCallSessionProxy;
        if (imsCallSessionProxy == null) {
            logWithCallId("callTerminated() : mAospImsCallSessionProxy is null", 5);
        } else {
            imsCallSessionProxy.callTerminated();
        }
    }

    public void setImsCallMode(int mode) {
        this.mAospImsCallSessionProxy.setImsCallMode(mode);
    }

    public void removeLastParticipant() {
        this.mAospImsCallSessionProxy.removeLastParticipant();
    }

    public String getHeaderCallId() {
        return this.mAospImsCallSessionProxy.getHeaderCallId();
    }

    public void videoRingtoneOperation(int type, String operation) {
        this.mAospImsCallSessionProxy.videoRingtoneOperation(type, operation);
    }

    /* access modifiers changed from: package-private */
    public void notifyCallSessionMergeStarted(IMtkImsCallSession mtkConfSession, ImsCallProfile imsCallProfile) {
        IMtkImsCallSessionListener iMtkImsCallSessionListener = this.mMtkListener;
        if (iMtkImsCallSessionListener == null) {
            logWithCallId("notifyCallSessionMergeStarted() : mMtkListener is null", 5);
            return;
        }
        try {
            iMtkImsCallSessionListener.callSessionMergeStarted(this.mServiceImpl, mtkConfSession, imsCallProfile);
        } catch (RemoteException e) {
            logWithCallId("notifyCallSessionMergeStarted() : RemoteException when MTK session merged started", 5);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyCallSessionMergeComplete(IMtkImsCallSession mtkConfSession) {
        IMtkImsCallSessionListener iMtkImsCallSessionListener = this.mMtkListener;
        if (iMtkImsCallSessionListener == null) {
            logWithCallId("notifyCallSessionMergeComplete() : mMtkListener is null", 5);
            return;
        }
        try {
            iMtkImsCallSessionListener.callSessionMergeComplete(mtkConfSession);
        } catch (RemoteException e) {
            logWithCallId("notifyCallSessionMergeComplete() : RemoteException when MTK session merged started", 5);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyRedialEcc(boolean isNeedUserConfirm) {
        IMtkImsCallSessionListener iMtkImsCallSessionListener = this.mMtkListener;
        if (iMtkImsCallSessionListener == null) {
            logWithCallId("notifyRedialEcc() : mMtkListener is null", 5);
            return;
        }
        try {
            iMtkImsCallSessionListener.callSessionRedialEcc(this.mServiceImpl, isNeedUserConfirm);
        } catch (RemoteException e) {
            logWithCallId("notifyRedialEcc() : RemoteException callSessionRedialEcc()", 5);
        }
    }

    public void notifyCallSessionRinging(ImsCallProfile imsCallProfile) {
        IMtkImsCallSessionListener iMtkImsCallSessionListener = this.mMtkListener;
        if (iMtkImsCallSessionListener == null) {
            logWithCallId("notifyCallSessionRinging() : mMtkListener is null", 2);
            return;
        }
        try {
            iMtkImsCallSessionListener.callSessionRinging(this.mServiceImpl, imsCallProfile);
        } catch (RemoteException e) {
            logWithCallId("notifyCallSessionRinging() : RemoteException notifyCallSessionRinging()", 5);
        }
    }

    public void notifyCallSessionCalling() {
        IMtkImsCallSessionListener iMtkImsCallSessionListener = this.mMtkListener;
        if (iMtkImsCallSessionListener == null) {
            logWithCallId("notifyCallSessionCalling() : mMtkListener is null", 2);
            return;
        }
        try {
            iMtkImsCallSessionListener.callSessionCalling(this.mServiceImpl);
        } catch (RemoteException e) {
            logWithCallId("notifyCallSessionCalling() : RemoteException notifyCallSessionCalling()", 5);
        }
    }

    public void notifyCallSessionBusy() {
        IMtkImsCallSessionListener iMtkImsCallSessionListener = this.mMtkListener;
        if (iMtkImsCallSessionListener == null) {
            logWithCallId("notifyCallSessionBusy() : mMtkListener is null", 2);
            return;
        }
        try {
            iMtkImsCallSessionListener.callSessionBusy(this.mServiceImpl);
        } catch (RemoteException e) {
            logWithCallId("notifyCallSessionBusy() : RemoteException notifyCallSessionBusy()", 5);
        }
    }

    public void notifyVideoRingtoneEvent(int eventType, String event) {
        IMtkImsCallSessionListener iMtkImsCallSessionListener = this.mMtkListener;
        if (iMtkImsCallSessionListener == null) {
            logWithCallId("notifyVideoRingtoneEvent() : mMtkListener is null", 2);
            return;
        }
        try {
            iMtkImsCallSessionListener.callSessionVideoRingtoneEventReceived(this.mServiceImpl, eventType, event);
        } catch (RemoteException e) {
            logWithCallId("notifyVideoRingtoneEvent() : RemoteException callSessionVideoRingtoneEvent()", 5);
        }
    }

    public void notifyNotificationRingtone(int causeNum, String causeText) {
        IMtkImsCallSessionListener iMtkImsCallSessionListener = this.mMtkListener;
        if (iMtkImsCallSessionListener == null) {
            logWithCallId("notifyNotificationRingtone() : mMtkListener is null", 2);
            return;
        }
        try {
            iMtkImsCallSessionListener.callSessionNotificationRingtoneReceived(this.mServiceImpl, causeNum, causeText);
        } catch (RemoteException e) {
            logWithCallId("notifyNotificationRingtone() : RemoteException callSessionVideoRingtoneEvent()", 5);
        }
    }

    /* access modifiers changed from: package-private */
    public ImsCallSessionProxy.ConferenceEventListener getConfEvtListener() {
        ImsCallSessionProxy imsCallSessionProxy = this.mAospImsCallSessionProxy;
        if (imsCallSessionProxy != null) {
            return imsCallSessionProxy.getConfEvtListener();
        }
        logWithCallId("ConferenceEventListener() : mAospImsCallSessionProxy is null", 5);
        return null;
    }

    /* access modifiers changed from: package-private */
    public void terminate(int reason) {
        ImsCallSessionProxy imsCallSessionProxy = this.mAospImsCallSessionProxy;
        if (imsCallSessionProxy == null) {
            logWithCallId("terminate() : mAospImsCallSessionProxy is null", 5);
        } else {
            imsCallSessionProxy.terminate(reason);
        }
    }

    public IMtkImsCallSession getServiceImpl() {
        return this.mServiceImpl;
    }

    public void setServiceImpl(IMtkImsCallSession serviceImpl) {
        this.mServiceImpl = serviceImpl;
    }

    public ImsCallSessionProxy getAospCallSessionProxy() {
        return this.mAospImsCallSessionProxy;
    }

    public void setAospCallSessionProxy(ImsCallSessionProxy callSessionProxy) {
        this.mAospImsCallSessionProxy = callSessionProxy;
    }

    private void logWithCallId(String msg, int lvl) {
        ImsCallSessionProxy imsCallSessionProxy = this.mAospImsCallSessionProxy;
        if (imsCallSessionProxy == null) {
            Rlog.d(LOG_TAG, "logWithCallId with mAospImsCallSessionProxy = null");
            return;
        }
        String mCallId = imsCallSessionProxy.getCallId();
        if (1 == lvl) {
            Rlog.v(LOG_TAG, "[callId = " + mCallId + "] " + msg);
        } else if (2 == lvl) {
            Rlog.d(LOG_TAG, "[callId = " + mCallId + "] " + msg);
        } else if (3 == lvl) {
            Rlog.i(LOG_TAG, "[callId = " + mCallId + "] " + msg);
        } else if (4 == lvl) {
            Rlog.w(LOG_TAG, "[callId = " + mCallId + "] " + msg);
        } else if (5 == lvl) {
            Rlog.e(LOG_TAG, "[callId = " + mCallId + "] " + msg);
        } else {
            Rlog.d(LOG_TAG, "[callId = " + mCallId + "] " + msg);
        }
    }
}
