package com.mediatek.ims.rcse;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import com.mediatek.ims.rcsua.AcsEventCallback;
import com.mediatek.ims.rcsua.Capability;
import com.mediatek.ims.rcsua.Client;
import com.mediatek.ims.rcsua.Configuration;
import com.mediatek.ims.rcsua.ImsEventCallback;
import com.mediatek.ims.rcsua.RcsUaService;
import com.mediatek.ims.rcsua.SipChannel;
import java.io.IOException;

public class UaServiceManager {
    private static UaServiceManager INSTANCE = null;
    /* access modifiers changed from: private */
    public AcsEventCallback acsCallback = null;
    private Capability capability = null;
    /* access modifiers changed from: private */
    public SipChannel channel = null;
    /* access modifiers changed from: private */
    public Client client = null;
    /* access modifiers changed from: private */
    public ImsEventCallback imsCallback = null;
    Context mContext = null;
    private RcsUaService.Callback serviceCallback = new RcsUaService.Callback() {
        public void serviceConnected(RcsUaService service) {
            Log.i("UaServiceManagerMtk", "serviceConnected:" + service);
            Log.i("UaServiceManagerMtk", "imsCallback:" + UaServiceManager.this.imsCallback);
            if (UaServiceManager.this.imsCallback != null) {
                UaServiceManager uaServiceManager = UaServiceManager.this;
                Client unused = uaServiceManager.client = uaServiceManager.uaService.registerClient(UaServiceManager.this.imsCallback);
                Log.i("UaServiceManagerMtk", "client:" + UaServiceManager.this.client);
                ImsEventCallback unused2 = UaServiceManager.this.imsCallback = null;
            }
            Log.i("UaServiceManagerMtk", "acsCallback:" + UaServiceManager.this.acsCallback);
            if (UaServiceManager.this.acsCallback != null && UaServiceManager.this.mContext != null && RcsUaService.isAcsAvailable(UaServiceManager.this.mContext)) {
                UaServiceManager.this.uaService.registerAcsEventCallback(UaServiceManager.this.acsCallback);
                AcsEventCallback unused3 = UaServiceManager.this.acsCallback = null;
            }
        }

        public void serviceDisconnected(RcsUaService service) {
            Log.i("UaServiceManager", "serviceDisconnected:" + service);
            if (UaServiceManager.this.client != null) {
                RcsUaService.getInstance().unregisterClient(UaServiceManager.this.client);
                Client unused = UaServiceManager.this.client = null;
                SipChannel unused2 = UaServiceManager.this.channel = null;
            }
            if (UaServiceManager.this.uaService != null) {
                RcsUaService unused3 = UaServiceManager.this.uaService = null;
            }
            boolean unused4 = UaServiceManager.this.serviceStarted = false;
        }
    };
    /* access modifiers changed from: private */
    public boolean serviceStarted;
    /* access modifiers changed from: private */
    public RcsUaService uaService = null;

    public static UaServiceManager getInstance() {
        Log.d("UaServiceManagerMTK", "getInstance:" + INSTANCE);
        if (INSTANCE == null) {
            synchronized (UaServiceManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UaServiceManager();
                }
            }
        }
        Log.d("UaServiceManagerMTK", "getInstance return:" + INSTANCE);
        return INSTANCE;
    }

    public void startService(Context context) {
        Log.i("UaServiceManagerMTK", "startService");
        if (!this.serviceStarted) {
            this.mContext = context;
            if (RcsUaService.isAvailable(context)) {
                String optr = SystemProperties.get("persist.vendor.operator.optr");
                if ("op07".equalsIgnoreCase(optr) || "op08".equalsIgnoreCase(optr)) {
                    Bundle options = new Bundle();
                    options.putBoolean(RcsUaService.OPTION_ROI_SUPPORT, true);
                    this.uaService = RcsUaService.startService(context, this.serviceCallback, options);
                    this.serviceStarted = true;
                    return;
                }
                Log.d("UaServiceManagerMTK", "startService optr:" + optr);
            }
        }
    }

    public void startService(Context context, int phoneId) {
        Log.i("UaServiceManagerMTK", "startService, phone id = " + phoneId);
        if (!this.serviceStarted) {
            this.mContext = context;
            if (RcsUaService.isAvailable(context)) {
                String str = SystemProperties.get("persist.vendor.operator.optr");
                Bundle options = new Bundle();
                options.putBoolean(RcsUaService.OPTION_ROI_SUPPORT, true);
                this.uaService = RcsUaService.startService(context, phoneId, this.serviceCallback, options);
                Log.i("UaServiceManagerMTK", "startService, uaService = " + this.uaService);
                this.serviceStarted = true;
                return;
            }
            Log.i("UaServiceManagerMTK", "RcsUaService is not started");
        }
    }

    public void stopService() {
        Log.i("UaServiceManagerMTK", "stopService:" + this.uaService);
        Client client2 = this.client;
        if (client2 != null) {
            this.uaService.unregisterClient(client2);
            this.client = null;
        }
        RcsUaService rcsUaService = this.uaService;
        if (rcsUaService != null) {
            rcsUaService.stopService();
            this.uaService = null;
        }
        this.serviceStarted = false;
    }

    public Client registerClient(ImsEventCallback callback) {
        Log.i("UaServiceManagerMTK", "registerClient:" + this.uaService + " ,client: " + this.client + " ,callback: " + callback);
        Client client2 = this.client;
        if (client2 != null) {
            return client2;
        }
        Log.i("UaServiceManagerMTK", "registerClient uaService.isConnected()" + this.uaService.isConnected());
        RcsUaService rcsUaService = this.uaService;
        if (rcsUaService != null && rcsUaService.isConnected()) {
            this.client = this.uaService.registerClient(callback);
            Log.i("UaServiceManagerMTK", "registerClient client: " + this.client);
        }
        this.imsCallback = callback;
        Log.i("UaServiceManagerMTK", "registerClient return client: " + this.client);
        return this.client;
    }

    public void unregisterClient(Client client2) {
        Log.i("UaServiceManagerMTK", "unregisterClient:" + this.uaService);
        if (client2 == this.client) {
            this.client = null;
            if (client2 == null) {
                this.imsCallback = null;
            }
        }
        RcsUaService rcsUaService = this.uaService;
        if (rcsUaService != null) {
            rcsUaService.unregisterClient(client2);
        }
    }

    public void registerAcsCallback(AcsEventCallback callback) {
        Log.i("UaServiceManagerMTK", "registerAcsCallback:" + this.uaService);
        if (this.acsCallback == null) {
            RcsUaService rcsUaService = this.uaService;
            if (rcsUaService == null || !rcsUaService.isConnected()) {
                this.acsCallback = callback;
            } else {
                this.uaService.registerAcsEventCallback(callback);
            }
        }
    }

    public void unregisterAcsCallback(AcsEventCallback callback) {
        RcsUaService rcsUaService;
        Log.i("UaServiceManagerMTK", "unregisterAcsCallback:" + this.uaService);
        if (callback != null && (rcsUaService = this.uaService) != null && rcsUaService.isConnected()) {
            this.uaService.unregisterAcsEventCallback(callback);
        }
    }

    public void activate() {
        Log.i("UaServiceManagerMTK", "activate:" + this.uaService);
        RcsUaService rcsUaService = this.uaService;
        if (rcsUaService != null) {
            rcsUaService.activate();
        }
    }

    public void deactivate() {
        Log.i("UaServiceManagerMTK", "deactivate:" + this.uaService);
        RcsUaService rcsUaService = this.uaService;
        if (rcsUaService != null) {
            rcsUaService.deactivate();
        }
    }

    public void triggerReregistration() {
        Log.i("UaServiceManagerMTK", "triggerReregistration:" + this.uaService);
        if (serviceConnected()) {
            this.uaService.triggerReregistration();
        }
    }

    public SipChannel openChannel(SipChannel.EventCallback callback) {
        Log.i("UaServiceManagerMTK", "openChannel:" + this.client);
        Client client2 = this.client;
        if (client2 != null) {
            try {
                this.channel = client2.openSipChannel(callback);
            } catch (IOException e) {
            }
        }
        return this.channel;
    }

    public void closeChannel(SipChannel channel2) {
        Log.i("UaServiceManagerMTK", "closeChannel:" + channel2);
        channel2.close();
        if (channel2 == this.channel) {
            this.channel = null;
        }
    }

    public boolean serviceConnected() {
        RcsUaService rcsUaService = this.uaService;
        return rcsUaService != null && rcsUaService.isConnected();
    }

    public boolean imsRegistered() {
        Client client2 = this.client;
        return client2 != null && client2.getRegistrationInfo().isRegistered();
    }

    public RcsUaService getService() {
        Log.e("UaServiceManagerMTK", "setService instance " + this.uaService);
        return this.uaService;
    }

    public Client getActiveClient() {
        return this.client;
    }

    public SipChannel getSipChannel() {
        return this.channel;
    }

    public Configuration readConfiguraion() {
        if (imsRegistered()) {
            return this.client.getRegistrationInfo().readImsConfiguration();
        }
        return null;
    }

    private UaServiceManager() {
    }
}
