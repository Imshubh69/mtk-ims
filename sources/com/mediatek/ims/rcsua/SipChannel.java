package com.mediatek.ims.rcsua;

import android.os.RemoteException;
import android.telephony.ims.aidl.ISipDelegateMessageCallback;
import android.util.Log;
import com.mediatek.ims.rcsua.AppCallback;
import com.mediatek.ims.rcsua.service.ISipChannel;
import com.mediatek.ims.rcsua.service.RcsUaException;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public final class SipChannel {
    public static final int MODE_STANDALONE_PRESENCE = 1;
    public static final int MODE_UNIFIED = 0;
    private static final String TAG = "[RcsUaService][API]";
    private EventCallback callback;
    private ISipChannel channelIntf;
    private Client client;
    private RcsUaService service;
    private int transport;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ChannelMode {
    }

    public void sendMessage(byte[] message) throws IOException {
        if (isConnected()) {
            try {
                RcsUaException ex = new RcsUaException();
                this.channelIntf.sendMessage(message, ex);
                if (ex.isSet()) {
                    ex.throwException();
                }
            } catch (RemoteException e) {
            }
        } else {
            throw new IOException("SIP channel not available");
        }
    }

    public void sendMessageAosp(byte[] message, ISipDelegateMessageCallback mMessageCallback) throws IOException {
        Log.d(TAG, "sendMessage message[" + message.toString() + "]");
        Log.d(TAG, "sendMessage channelIntf[" + this.channelIntf + "] , mMessageCallback: " + mMessageCallback);
        if (isConnected()) {
            try {
                RcsUaException ex = new RcsUaException();
                this.channelIntf.sendMessageAosp(message, ex, mMessageCallback);
                if (ex.isSet()) {
                    ex.throwException();
                }
            } catch (RemoteException e) {
            }
        } else {
            throw new IOException("SIP channel not available");
        }
    }

    public void close() {
        if (this.channelIntf != null && this.client.channelClosed(this) == 0) {
            try {
                this.channelIntf.close(new RcsUaException());
                this.channelIntf = null;
            } catch (RemoteException e) {
            }
        }
    }

    public boolean isConnected() {
        boolean connected = false;
        if (this.channelIntf != null && this.service.isActivated()) {
            try {
                connected = this.channelIntf.isAvailable();
            } catch (RemoteException e) {
            }
        }
        Log.d(TAG, "isConnected[" + connected + "]: channelIntf[" + this.channelIntf + "]");
        return connected;
    }

    public static abstract class EventCallback extends AppCallback {
        public void messageReceived(byte[] message) {
        }

        class Runner extends AppCallback.BaseRunner<byte[]> {
            Runner(byte[]... params) {
                super(params);
            }

            /* access modifiers changed from: package-private */
            public void exec(byte[]... params) {
                EventCallback.this.messageReceived(params[0]);
            }
        }
    }

    SipChannel(RcsUaService service2, Client client2, ISipChannel channel, int transport2, EventCallback callback2) {
        this.channelIntf = channel;
        this.callback = callback2;
        this.service = service2;
        this.client = client2;
        this.transport = transport2;
    }

    /* access modifiers changed from: package-private */
    public boolean isTransportSupported(int transport2) {
        return (this.transport & transport2) > 0;
    }

    /* access modifiers changed from: package-private */
    public void handleSipMessageReceived(byte[] message) {
        EventCallback eventCallback = this.callback;
        Objects.requireNonNull(eventCallback);
        eventCallback.run(new EventCallback.Runner(message));
    }
}
