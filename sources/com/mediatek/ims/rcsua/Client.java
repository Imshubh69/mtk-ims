package com.mediatek.ims.rcsua;

import android.os.RemoteException;
import android.util.Log;
import com.mediatek.ims.rcsua.ImsEventCallback;
import com.mediatek.ims.rcsua.SipChannel;
import com.mediatek.ims.rcsua.service.IRcsUaClient;
import com.mediatek.ims.rcsua.service.ISipChannel;
import com.mediatek.ims.rcsua.service.ISipEventCallback;
import com.mediatek.ims.rcsua.service.RcsUaException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

public final class Client {
    private static final String TAG = "[RcsUaService][API]";
    /* access modifiers changed from: private */
    public volatile ArrayList<SipChannel> activeChannels = new ArrayList<>();
    private HashSet<ImsEventCallback> callbacks = new HashSet<>();
    private ISipChannel channelIntf;
    private IRcsUaClient clientIntf;
    private Object lock = new Object();
    private RcsUaService service;
    ISipEventCallback sipEventCallback = new ISipEventCallback.Stub() {
        public void messageReceived(int transport, byte[] message) throws RemoteException {
            Iterator it = Client.this.activeChannels.iterator();
            while (it.hasNext()) {
                SipChannel channel = (SipChannel) it.next();
                if (channel.isTransportSupported(transport)) {
                    channel.handleSipMessageReceived(message);
                    return;
                }
            }
        }
    };

    public SipChannel openSipChannel(SipChannel.EventCallback sipCallback) throws IOException {
        return openSipChannel(3, sipCallback, 0);
    }

    public SipChannel openSipChannel(SipChannel.EventCallback sipCallback, int mode) throws IOException {
        return openSipChannel(3, sipCallback, mode);
    }

    public SipChannel openSipChannel(int transport, SipChannel.EventCallback sipCallback, int mode) throws IOException {
        Log.d(TAG, "Client openSipChannel with active:" + this.activeChannels);
        if (this.service.isConnected()) {
            if (this.channelIntf == null) {
                synchronized (this.lock) {
                    if (this.channelIntf == null) {
                        try {
                            RcsUaException ex = new RcsUaException();
                            this.channelIntf = this.clientIntf.openSipChannel(this.sipEventCallback, mode, ex);
                            if (ex.isSet()) {
                                ex.throwException();
                            }
                        } catch (RemoteException e) {
                            this.channelIntf = null;
                            return null;
                        }
                    }
                }
            } else {
                Iterator<SipChannel> it = this.activeChannels.iterator();
                while (it.hasNext()) {
                    SipChannel channel = it.next();
                    if (channel.isTransportSupported(transport)) {
                        return channel;
                    }
                }
            }
            SipChannel sipChannel = new SipChannel(this.service, this, this.channelIntf, transport, sipCallback);
            this.activeChannels.add(sipChannel);
            return sipChannel;
        }
        throw new IllegalStateException("RCS UA service disconnected");
    }

    public void registerImsEventCallback(ImsEventCallback callback) {
        synchronized (this.lock) {
            this.callbacks.add(callback);
        }
    }

    public void unregisterImsEventCallback(ImsEventCallback callback) {
        synchronized (this.lock) {
            this.callbacks.remove(callback);
        }
    }

    public RegistrationInfo getRegistrationInfo() {
        if (this.service.isConnected()) {
            RegistrationInfo regInfo = null;
            try {
                regInfo = this.clientIntf.getRegistrationInfo();
            } catch (RemoteException e) {
            }
            if (regInfo == null) {
                return new RegistrationInfo();
            }
            return regInfo;
        }
        throw new IllegalStateException("RCS UA service disconnected");
    }

    public void resumeImsDeregistration() {
        if (this.service.isConnected()) {
            try {
                this.clientIntf.resumeImsDeregistration();
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    /* access modifiers changed from: package-private */
    public IRcsUaClient getInterface() {
        return this.clientIntf;
    }

    /* access modifiers changed from: package-private */
    public void handleImsEventCallback(RegistrationInfo regInfo) {
        Log.d(TAG, "handleImsEventCallback");
        int state = regInfo.getRegState();
        int mode = regInfo.getRegMode();
        Iterator<ImsEventCallback> it = this.callbacks.iterator();
        while (it.hasNext()) {
            ImsEventCallback callback = it.next();
            Objects.requireNonNull(callback);
            callback.run(new ImsEventCallback.Runner(Integer.valueOf(state), Integer.valueOf(mode)));
        }
    }

    /* access modifiers changed from: package-private */
    public void handleImsReregistered(RegistrationInfo regInfo) {
        Log.d(TAG, "handleImsReregistered");
        int mode = regInfo.getRegMode();
        Iterator<ImsEventCallback> it = this.callbacks.iterator();
        while (it.hasNext()) {
            ImsEventCallback callback = it.next();
            Objects.requireNonNull(callback);
            callback.run(new ImsEventCallback.Runner(256, Integer.valueOf(mode)));
        }
    }

    /* access modifiers changed from: package-private */
    public void handleImsDeregInd(RegistrationInfo regInfo) {
        Log.d(TAG, "handleImsDeregInd");
        int mode = regInfo.getRegMode();
        Iterator<ImsEventCallback> it = this.callbacks.iterator();
        while (it.hasNext()) {
            ImsEventCallback callback = it.next();
            Objects.requireNonNull(callback);
            callback.run(new ImsEventCallback.Runner(128, Integer.valueOf(mode)));
        }
    }

    /* access modifiers changed from: package-private */
    public void handleVopsInd(int vops) {
        Log.d(TAG, "handleVopsInd:" + vops);
        Iterator<ImsEventCallback> it = this.callbacks.iterator();
        while (it.hasNext()) {
            ImsEventCallback callback = it.next();
            Objects.requireNonNull(callback);
            callback.run(new ImsEventCallback.Runner(512, 0, Integer.valueOf(vops)));
        }
    }

    Client(RcsUaService service2, IRcsUaClient clientIntf2) {
        this.service = service2;
        this.clientIntf = clientIntf2;
    }

    /* access modifiers changed from: package-private */
    public int channelClosed(SipChannel channel) {
        this.activeChannels.remove(channel);
        int activeChannelNum = this.activeChannels.size();
        Log.d(TAG, "channelClosed, activeChannelNum = " + activeChannelNum);
        if (activeChannelNum == 0) {
            this.channelIntf = null;
        }
        return activeChannelNum;
    }
}
