package com.mediatek.ims.rcse;

import android.telephony.ims.DelegateMessageCallback;
import android.telephony.ims.DelegateRequest;
import android.telephony.ims.DelegateStateCallback;
import android.telephony.ims.SipMessage;
import android.telephony.ims.aidl.ISipDelegateMessageCallback;
import android.telephony.ims.stub.SipDelegate;
import android.util.Log;
import com.mediatek.ims.rcsua.Client;
import com.mediatek.ims.rcsua.ImsEventCallback;
import com.mediatek.ims.rcsua.RcsUaService;
import com.mediatek.ims.rcsua.SipChannel;
import java.io.IOException;

public class MtkSipDelegate implements SipDelegate {
    private static final String LOG_TAG = "MtkSipDelegate";
    public final DelegateRequest delegateRequest;
    private Client mClient = null;
    /* access modifiers changed from: private */
    public final DelegateMessageCallback mMessageCallback;
    private RcsUaService mRcsUaService = null;
    private SipChannel mSipChannel = null;
    private final DelegateStateCallback mStateCallback;
    public ISipDelegateMessageCallback mtkSipDelegateMc = new ISipDelegateMessageCallback.Stub() {
        public void onMessageReceived(SipMessage message) {
        }

        public void onMessageSent(String viaTransactionId) {
            Log.d(MtkSipDelegate.LOG_TAG, "mtkSipDelegateMc onMessageSent viaTransactionId: " + viaTransactionId);
            MtkSipDelegate.this.mMessageCallback.onMessageSent(viaTransactionId);
        }

        public void onMessageSendFailure(String viaTransactionId, int reason) {
            Log.d(MtkSipDelegate.LOG_TAG, "mtkSipDelegateMc onMessageSendFailure viaTransactionId: " + viaTransactionId + " ,reason: " + reason);
            MtkSipDelegate.this.mMessageCallback.onMessageSendFailure(viaTransactionId, reason);
        }
    };
    public final int subId;

    public MtkSipDelegate(int sub, DelegateRequest request, DelegateStateCallback cb, DelegateMessageCallback mc) {
        Log.d(LOG_TAG, "UaServiceManager.getService(): " + UaServiceManager.getInstance().getService());
        this.subId = sub;
        this.delegateRequest = request;
        this.mStateCallback = cb;
        this.mMessageCallback = mc;
        if (UaServiceManager.getInstance().getService() != null) {
            this.mClient = UaServiceManager.getInstance().registerClient(new ImsEventCallbackExt());
            Log.d(LOG_TAG, "UaServiceManager mClient: " + this.mClient);
        }
        Log.d(LOG_TAG, " MtkSipDelegate subId: " + sub + " ,delegateRequest: " + request + " ,mStateCallback: " + cb + " ,mMessageCallback: " + mc);
    }

    public void sendMessage(SipMessage message, long configVersion) {
        Log.d(LOG_TAG, " sendMessage message: " + message + " ,configVersion: " + configVersion);
        StringBuilder sb = new StringBuilder();
        sb.append("sendMessage message.getContext: ");
        sb.append(message.getContent());
        Log.d(LOG_TAG, sb.toString());
        Log.d(LOG_TAG, "sendMessage message.toString: " + message.toString());
        Log.d(LOG_TAG, "sendMessage message.toEncodedMessage: " + message.toEncodedMessage());
        Log.d(LOG_TAG, "UaServiceManager.getInstance: " + UaServiceManager.getInstance());
        Log.d(LOG_TAG, "UaServiceManager.imsRegistered(): " + UaServiceManager.getInstance().imsRegistered());
        Log.d(LOG_TAG, "UaServiceManager.getService(): " + UaServiceManager.getInstance().getService());
        Log.d(LOG_TAG, "UaServiceManager.getSipChannel(): " + UaServiceManager.getInstance().getSipChannel());
        try {
            Client client = this.mClient;
            if (client != null) {
                this.mSipChannel = client.openSipChannel(new SipCallback(), 0);
                Log.d(LOG_TAG, "UaServiceManager mSipChannel: " + this.mSipChannel);
                Log.d(LOG_TAG, "UaServiceManager mSipChannel.isConnected(): " + this.mSipChannel.isConnected());
                SipChannel sipChannel = this.mSipChannel;
                if (sipChannel != null && sipChannel.isConnected()) {
                    this.mSipChannel.sendMessageAosp(message.toEncodedMessage(), this.mtkSipDelegateMc);
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "OpenSipChannel fail", e);
        }
    }

    public void cleanupSession(String callId) {
    }

    public void notifyMessageReceived(String viaTransactionId) {
    }

    public void notifyMessageReceiveError(String viaTransactionId, int reason) {
    }

    public class ImsEventCallbackExt extends ImsEventCallback {
        public ImsEventCallbackExt() {
        }

        public void onRegistering(int mode) {
            Log.d(MtkSipDelegate.LOG_TAG, "onRegistering mode: " + mode);
        }

        public void onRegistered(int mode) {
            Log.d(MtkSipDelegate.LOG_TAG, "onRegistered mode: " + mode);
        }

        public void onDeregistering(int mode) {
            Log.d(MtkSipDelegate.LOG_TAG, "onDeregistering mode: " + mode);
        }

        public void onDeregistered(int mode) {
            Log.d(MtkSipDelegate.LOG_TAG, "onDeregistered mode: " + mode);
        }

        public void onReregistered(int mode) {
            Log.d(MtkSipDelegate.LOG_TAG, "onReregistered mode: " + mode);
        }

        public void onDeregStart(int mode) {
            Log.d(MtkSipDelegate.LOG_TAG, "onDeregStart mode: " + mode);
        }

        public void onVopsIndication(int vops) {
            Log.d(MtkSipDelegate.LOG_TAG, "onVopsIndication: " + vops);
        }
    }

    public class SipCallback extends SipChannel.EventCallback {
        public SipCallback() {
        }

        public void messageReceived(byte[] message) {
            Log.d(MtkSipDelegate.LOG_TAG, "messageReceived new message: " + message);
            String msgStr = new String(message);
            Log.d(MtkSipDelegate.LOG_TAG, "messageReceived msgStr: " + msgStr);
            String startLine = msgStr.split("\n")[0];
            Log.d(MtkSipDelegate.LOG_TAG, "messageReceived startLine: " + startLine);
            String[] headerArray = msgStr.split("\n");
            String headerSection = "";
            for (int i = 1; i < headerArray.length; i++) {
                Log.d(MtkSipDelegate.LOG_TAG, "headerArray i: " + i + " string [" + headerArray[i] + "]");
                StringBuilder sb = new StringBuilder();
                sb.append(headerSection);
                sb.append(headerArray[i]);
                sb.append("\n");
                headerSection = sb.toString();
            }
            Log.d(MtkSipDelegate.LOG_TAG, "messageReceived headerSection: " + headerSection);
            MtkSipDelegate.this.mMessageCallback.onMessageReceived(new SipMessage(startLine, headerSection, message));
        }
    }
}
