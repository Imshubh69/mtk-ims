package com.mediatek.ims.rcsua;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.mediatek.ims.rcsua.AcsEventCallback;
import com.mediatek.ims.rcsua.service.IAcsEventCallback;
import com.mediatek.ims.rcsua.service.IImsEventCallback;
import com.mediatek.ims.rcsua.service.IRcsUaClient;
import com.mediatek.ims.rcsua.service.IRcsUaService;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

public class RcsUaService {
    public static final String ACTION_RCSUA_SERVICE_DOWN = "com.mediatek.ims.rcsua.SERVICE_DOWN";
    public static final String ACTION_RCSUA_SERVICE_UP = "com.mediatek.ims.rcsua.SERVICE_UP";
    private static final String EXTRA_OPTIONS = "service_options";
    private static final String EXTRA_PHONE_ID = "phone_id";
    /* access modifiers changed from: private */
    public static volatile RcsUaService INSTANCE = null;
    public static final String OPTION_DEREG_SUSPEND = "OPTION_DEREG_SUSPEND";
    public static final String OPTION_ROI_SUPPORT = "OPTION_ROI_SUPPORT";
    public static final int REASON_DEFAULT_SMS_UNKNOWN = 0;
    public static final int REASON_FT_AUTHENTICATE_FAILURE = 3;
    public static final int REASON_RCS_DEFAULT_SMS = 1;
    public static final int REASON_RCS_NOT_DEFAULT_SMS = 2;
    public static final int REG_MODE_IMS = 1;
    public static final int REG_MODE_INTERNET = 2;
    public static final int REG_RADIO_3GPP = 1;
    public static final int REG_RADIO_IEEE802 = 2;
    public static final int REG_RADIO_NONE = 0;
    public static final int REG_STATE_DEREGISTERING = 3;
    public static final int REG_STATE_NOT_REGISTERED = 0;
    public static final int REG_STATE_REGISTERED = 2;
    public static final int REG_STATE_REGISTERING = 1;
    private static final String TAG = "[RcsUaService][API]";
    /* access modifiers changed from: private */
    public HashSet<AcsEventCallback> acsCallbacks;
    /* access modifiers changed from: private */
    public IAcsEventCallback acsEventCallback;
    private boolean acsSupported;
    /* access modifiers changed from: private */
    public boolean activated;
    private Callback callerCallback;
    private Context callerContext;
    private Capability capabilities;
    private int clientCount;
    private IRcsUaClient clientIntf;
    /* access modifiers changed from: private */
    public HashSet<Client> clients;
    /* access modifiers changed from: private */
    public ServiceDeathRecipient deathRecipient;
    private final IImsEventCallback imsEventCallback = new IImsEventCallback.Stub() {
        public void onStatusChanged(RegistrationInfo regInfo) {
            Log.d(RcsUaService.TAG, "onStatusChanged:" + regInfo);
            synchronized (RcsUaService.this.lock) {
                Iterator it = RcsUaService.this.clients.iterator();
                while (it.hasNext()) {
                    ((Client) it.next()).handleImsEventCallback(regInfo);
                }
            }
        }

        public void onReregistered(RegistrationInfo regInfo) {
            Log.d(RcsUaService.TAG, "onReregistered:" + regInfo);
            synchronized (RcsUaService.this.lock) {
                Iterator it = RcsUaService.this.clients.iterator();
                while (it.hasNext()) {
                    ((Client) it.next()).handleImsReregistered(regInfo);
                }
            }
        }

        public void onDeregStarted(RegistrationInfo regInfo) {
            Log.d(RcsUaService.TAG, "onDeregStarted:" + regInfo);
            synchronized (RcsUaService.this.lock) {
                Iterator it = RcsUaService.this.clients.iterator();
                while (it.hasNext()) {
                    ((Client) it.next()).handleImsDeregInd(regInfo);
                }
            }
        }

        public void onVopsIndication(int vops) {
            Log.d(RcsUaService.TAG, "onVopsIndication:" + vops);
            synchronized (RcsUaService.this.lock) {
                Iterator it = RcsUaService.this.clients.iterator();
                while (it.hasNext()) {
                    ((Client) it.next()).handleVopsInd(vops);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Object lock;
    private ServiceConnection serviceConnection;
    /* access modifiers changed from: private */
    public IRcsUaService serviceIntf;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AcsRequestReason {
    }

    public interface Callback {
        void serviceConnected(RcsUaService rcsUaService);

        void serviceDisconnected(RcsUaService rcsUaService);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ImsRadioTech {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RegistrationMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RegistrationState {
    }

    public static RcsUaService startService(Context context, Callback callback) {
        return startService(context, 0, callback, (Bundle) null);
    }

    public static RcsUaService startService(Context context, int phoneId, Callback callback) {
        return startService(context, phoneId, callback, (Bundle) null);
    }

    public static RcsUaService startService(Context context, Callback callback, Bundle options) {
        return startService(context, 0, callback, options);
    }

    public static RcsUaService startService(Context context, int phoneId, Callback callback, Bundle options) {
        Log.d(TAG, "startService, current instance:" + INSTANCE);
        if (INSTANCE == null) {
            synchronized (RcsUaService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RcsUaService(context, phoneId, callback, options);
                }
            }
        }
        return INSTANCE;
    }

    public void stopService() {
        Log.d(TAG, "stopService, current instance:" + INSTANCE);
        synchronized (RcsUaService.class) {
            IRcsUaService iRcsUaService = this.serviceIntf;
            if (iRcsUaService != null) {
                IAcsEventCallback iAcsEventCallback = this.acsEventCallback;
                if (iAcsEventCallback != null) {
                    try {
                        iRcsUaService.unregisterAcsCallback(iAcsEventCallback);
                    } catch (RemoteException e) {
                    }
                    this.acsEventCallback = null;
                }
                this.acsCallbacks.clear();
                this.clients.clear();
                this.callerContext.unbindService(this.serviceConnection);
                this.serviceIntf.asBinder().unlinkToDeath(this.deathRecipient, 0);
                this.serviceIntf = null;
            }
            if (INSTANCE != null) {
                INSTANCE = null;
            }
        }
    }

    public static RcsUaService getInstance() {
        return getInstance(0);
    }

    public static RcsUaService getInstance(int phoneId) {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        throw new NullPointerException("startService() must be called before getInstance()");
    }

    public void activate() {
        Log.d(TAG, "activate");
        if (!isConnected()) {
            this.activated = true;
            return;
        }
        try {
            this.serviceIntf.activate();
        } catch (RemoteException e) {
        }
    }

    public void deactivate() {
        Log.d(TAG, "deactivate");
        if (!isConnected()) {
            this.activated = false;
            return;
        }
        try {
            this.serviceIntf.deactivate();
        } catch (RemoteException e) {
        }
    }

    public void triggerReregistration() {
        Log.d(TAG, "triggerReregistraion");
        if (isConnected()) {
            try {
                this.serviceIntf.triggerReregistration((Capability) null);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public void triggerForceReregistration() {
        Log.d(TAG, "triggerForceReregistration");
        if (isConnected()) {
            try {
                this.serviceIntf.triggerForceReregistration((Capability) null);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public void triggerReregistration(Capability features) {
        Log.d(TAG, "triggerReregistraion");
        if (isConnected()) {
            try {
                this.serviceIntf.triggerReregistration(features);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public void triggerRestoration() {
        Log.d(TAG, "triggerRestoration");
        if (isConnected()) {
            try {
                this.serviceIntf.triggerRestoration();
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public void addCapability(Capability feature) {
        Log.d(TAG, "addCapability");
        if (isConnected()) {
            try {
                this.serviceIntf.addCapability(feature);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public void removeCapability(Capability feature) {
        Log.d(TAG, "removeCapability");
        if (isConnected()) {
            try {
                this.serviceIntf.removeCapability(feature);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public void updateCapabilities(Capability features) {
        Log.d(TAG, "updateCapabilities");
        if (isConnected()) {
            try {
                this.serviceIntf.updateCapabilities(features);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public void updateCapabilities(String featuresText) {
        Log.d(TAG, "updateCapabilities");
        if (isConnected()) {
            try {
                this.serviceIntf.updateCapabilities(new Capability(featuresText));
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    public Client registerClient(ImsEventCallback callback) {
        Client newClient = null;
        if (this.serviceIntf != null) {
            try {
                Log.d(TAG, "registerClient:" + this.clientIntf);
                synchronized (this.lock) {
                    int i = this.clientCount;
                    this.clientCount = i + 1;
                    if (i == 0) {
                        this.clientIntf = this.serviceIntf.registerClient(this.imsEventCallback);
                    }
                    newClient = new Client(this, this.clientIntf);
                    newClient.registerImsEventCallback(callback);
                    this.clients.add(newClient);
                }
            } catch (RemoteException e) {
            }
            return newClient;
        }
        throw new IllegalStateException("RCS UA service disconnected");
    }

    public void unregisterClient(Client client) {
        Log.d(TAG, "unregisterClient");
        if (!this.clients.contains(client)) {
            Log.d(TAG, "unregisterClient: client doesn't exist");
            return;
        }
        synchronized (this.lock) {
            int i = this.clientCount - 1;
            this.clientCount = i;
            if (i == 0) {
                try {
                    IRcsUaService iRcsUaService = this.serviceIntf;
                    if (iRcsUaService != null) {
                        iRcsUaService.unregisterClient(this.clientIntf);
                    }
                } catch (RemoteException e) {
                }
                this.clientIntf = null;
            }
            this.clients.remove(client);
        }
    }

    public Capability getCapabilities() {
        if (isConnected()) {
            try {
                return this.serviceIntf.getCapabilities();
            } catch (RemoteException e) {
                return null;
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public boolean isActivated() {
        if (!isConnected()) {
            return this.activated;
        }
        try {
            return this.serviceIntf.isActivated();
        } catch (RemoteException e) {
            return true;
        }
    }

    public void setOptions(Bundle options) {
        if (isConnected()) {
            try {
                this.serviceIntf.setOptions(options);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public Client[] getActiveClients() {
        if (isConnected()) {
            return (Client[]) this.clients.toArray();
        }
        throw new IllegalStateException("RCS UA service disconnected");
    }

    public Bundle getOptions() {
        if (isConnected()) {
            try {
                return this.serviceIntf.getOptions();
            } catch (RemoteException e) {
                return null;
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public void registerAcsEventCallback(AcsEventCallback callback) {
        if (!this.acsSupported) {
            throw new UnsupportedOperationException("ACS not supported");
        } else if (isConnected()) {
            synchronized (this.lock) {
                if (this.acsCallbacks.isEmpty()) {
                    C01741 r1 = new IAcsEventCallback.Stub() {
                        public void onConfigChanged(boolean valid, int version) {
                            Log.d(RcsUaService.TAG, "onConfigChanged:valid:" + valid + ",version:" + version);
                            synchronized (RcsUaService.this.lock) {
                                Iterator it = RcsUaService.this.acsCallbacks.iterator();
                                while (it.hasNext()) {
                                    AcsEventCallback callback = (AcsEventCallback) it.next();
                                    Objects.requireNonNull(callback);
                                    Integer[] numArr = new Integer[3];
                                    int i = 0;
                                    numArr[0] = 0;
                                    if (valid) {
                                        i = 1;
                                    }
                                    numArr[1] = Integer.valueOf(i);
                                    numArr[2] = Integer.valueOf(version);
                                    callback.run(new AcsEventCallback.Runner(numArr));
                                }
                            }
                        }

                        public void onConnectionChanged(boolean status) {
                            Log.d(RcsUaService.TAG, "onConnectionChanged:status:" + status);
                            synchronized (RcsUaService.this.lock) {
                                Iterator it = RcsUaService.this.acsCallbacks.iterator();
                                while (it.hasNext()) {
                                    AcsEventCallback callback = (AcsEventCallback) it.next();
                                    if (status) {
                                        Objects.requireNonNull(callback);
                                        callback.run(new AcsEventCallback.Runner(1, 0, 0));
                                    } else {
                                        Objects.requireNonNull(callback);
                                        callback.run(new AcsEventCallback.Runner(2, 0, 0));
                                    }
                                }
                            }
                        }
                    };
                    this.acsEventCallback = r1;
                    try {
                        this.serviceIntf.registerAcsCallback(r1);
                    } catch (RemoteException e) {
                    }
                }
                this.acsCallbacks.add(callback);
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public void unregisterAcsEventCallback(AcsEventCallback callback) {
        if (this.acsSupported) {
            synchronized (this.lock) {
                this.acsCallbacks.remove(callback);
                if (this.acsCallbacks.isEmpty()) {
                    try {
                        IRcsUaService iRcsUaService = this.serviceIntf;
                        if (iRcsUaService != null) {
                            iRcsUaService.unregisterAcsCallback(this.acsEventCallback);
                        }
                        this.acsEventCallback = null;
                    } catch (RemoteException e) {
                    }
                }
            }
            return;
        }
        throw new UnsupportedOperationException("ACS not supported");
    }

    public AcsConfiguration getAcsConfiguration() {
        if (!this.acsSupported) {
            throw new UnsupportedOperationException("ACS not supported");
        } else if (isConnected()) {
            try {
                return this.serviceIntf.getAcsConfiguration();
            } catch (RemoteException e) {
                return null;
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public String getAospAcsInfo() {
        if (!this.acsSupported) {
            throw new UnsupportedOperationException("ACS not supported");
        } else if (isConnected()) {
            try {
                return this.serviceIntf.getAcsConfiguration().readXmlData();
            } catch (RemoteException e) {
                return "";
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public int getAcsConfigInt(String configItem) {
        if (!this.acsSupported) {
            throw new UnsupportedOperationException("ACS not supported");
        } else if (isConnected()) {
            try {
                return this.serviceIntf.getAcsConfigInt(configItem);
            } catch (RemoteException e) {
                return 0;
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public String getAcsConfigString(String configItem) {
        if (!this.acsSupported) {
            throw new UnsupportedOperationException("ACS not supported");
        } else if (isConnected()) {
            try {
                return this.serviceIntf.getAcsConfigString(configItem);
            } catch (RemoteException e) {
                return "";
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public boolean isAcsConnected() {
        if (!this.acsSupported) {
            throw new UnsupportedOperationException("ACS not supported");
        } else if (isConnected()) {
            try {
                return this.serviceIntf.isAcsConnected();
            } catch (RemoteException e) {
                return false;
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public void triggerAcsRequest(int reason) {
        if (!this.acsSupported) {
            throw new UnsupportedOperationException("ACS not supported");
        } else if (isConnected()) {
            try {
                this.serviceIntf.triggerAcsRequest(reason);
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("RCS UA service disconnected");
        }
    }

    public boolean setAcsSwitchState(boolean state) {
        if (this.acsSupported) {
            try {
                return this.serviceIntf.setAcsSwitchState(state);
            } catch (RemoteException e) {
                return false;
            }
        } else {
            throw new UnsupportedOperationException("ACS not supported");
        }
    }

    public void setAcsonfiguration(String rcsVersion, String rcsProfile, String clientVendor, String clientVersion) {
        Log.d(TAG, "setAcsonfiguration rcsVersion: " + rcsVersion + " ,rcsProfile: " + rcsProfile + " ,clientVendor: " + clientVendor + " ,clientVersion: " + clientVersion);
        StringBuilder sb = new StringBuilder();
        sb.append("setAcsonfiguration !acsSupported: ");
        sb.append(this.acsSupported);
        Log.d(TAG, sb.toString());
        if (this.acsSupported) {
            try {
                Log.e(TAG, "serviceIntf value " + this.serviceIntf);
                this.serviceIntf.setAcsonfiguration(rcsVersion, rcsProfile, clientVendor, clientVersion);
            } catch (RemoteException e) {
            }
        } else {
            throw new UnsupportedOperationException("ACS not supported");
        }
    }

    public boolean getAcsSwitchState() {
        if (this.acsSupported) {
            try {
                return this.serviceIntf.getAcsSwitchState();
            } catch (RemoteException e) {
                return false;
            }
        } else {
            throw new UnsupportedOperationException("ACS not supported");
        }
    }

    public boolean setAcsProvisioningAddress(String address) {
        if (this.acsSupported) {
            try {
                return this.serviceIntf.setAcsProvisioningAddress(address);
            } catch (RemoteException e) {
                return false;
            }
        } else {
            throw new UnsupportedOperationException("ACS not supported");
        }
    }

    public boolean clearAcsConfiguration() {
        if (this.acsSupported) {
            try {
                return this.serviceIntf.clearAcsConfiguration();
            } catch (RemoteException e) {
                return false;
            }
        } else {
            throw new UnsupportedOperationException("ACS not supported");
        }
    }

    public static boolean isAvailable(Context context) {
        String value = getSystemProperties(context, "persist.vendor.mtk_rcs_ua_support", "0");
        Log.d(TAG, "isAvailable:" + value);
        return Integer.parseInt(value) == 1;
    }

    public static boolean isAcsAvailable(Context context) {
        PackageInfo info = null;
        PackageManager pm = context.getPackageManager();
        Log.e(TAG, "context value " + context);
        Log.e(TAG, "PackageManager pm  value " + pm);
        boolean z = false;
        try {
            info = pm.getPackageInfo("com.mediatek.rcs.provisioning", 0);
            Log.e(TAG, "info value " + info);
        } catch (PackageManager.NameNotFoundException e) {
        }
        StringBuilder sb = new StringBuilder();
        sb.append("isAcsAvailable:");
        if (info != null) {
            z = true;
        }
        sb.append(z);
        Log.d(TAG, sb.toString());
        return true;
    }

    public boolean isConnected() {
        return this.serviceIntf != null;
    }

    /* access modifiers changed from: package-private */
    public Context getContext() {
        return this.callerContext;
    }

    private RcsUaService(Context context, int phoneId, Callback callback, Bundle options) {
        Bundle bundle;
        boolean z = false;
        this.clientCount = 0;
        this.deathRecipient = new ServiceDeathRecipient();
        this.clients = new HashSet<>();
        this.acsCallbacks = new HashSet<>();
        this.lock = new Object();
        this.serviceConnection = new ServiceConnection() {
            /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
                r5.linkToDeath(com.mediatek.ims.rcsua.RcsUaService.access$300(r3.this$0), 0);
                com.mediatek.ims.rcsua.RcsUaService.access$402(r3.this$0, com.mediatek.ims.rcsua.service.IRcsUaService.Stub.asInterface(r5));
             */
            /* JADX WARNING: Code restructure failed: missing block: B:11:0x003d, code lost:
                if (com.mediatek.ims.rcsua.RcsUaService.access$500(r3.this$0) == false) goto L_0x0049;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:12:0x003f, code lost:
                com.mediatek.ims.rcsua.RcsUaService.access$400(r3.this$0).activate();
             */
            /* JADX WARNING: Code restructure failed: missing block: B:13:0x0049, code lost:
                com.mediatek.ims.rcsua.RcsUaService.access$400(r3.this$0).deactivate();
             */
            /* JADX WARNING: Code restructure failed: missing block: B:14:0x0052, code lost:
                com.mediatek.ims.rcsua.RcsUaService.access$600(r3.this$0);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
                return;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
                return;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
                return;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:8:0x0022, code lost:
                if (r5 == null) goto L_?;
             */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onServiceConnected(android.content.ComponentName r4, android.os.IBinder r5) {
                /*
                    r3 = this;
                    java.lang.String r0 = "[RcsUaService][API]"
                    java.lang.StringBuilder r1 = new java.lang.StringBuilder
                    r1.<init>()
                    java.lang.String r2 = "onServiceConnected:"
                    r1.append(r2)
                    r1.append(r5)
                    java.lang.String r1 = r1.toString()
                    android.util.Log.d(r0, r1)
                    java.lang.Class<com.mediatek.ims.rcsua.RcsUaService> r0 = com.mediatek.ims.rcsua.RcsUaService.class
                    monitor-enter(r0)
                    com.mediatek.ims.rcsua.RcsUaService r1 = com.mediatek.ims.rcsua.RcsUaService.INSTANCE     // Catch:{ all -> 0x005c }
                    if (r1 != 0) goto L_0x0021
                    monitor-exit(r0)     // Catch:{ all -> 0x005c }
                    return
                L_0x0021:
                    monitor-exit(r0)     // Catch:{ all -> 0x005c }
                    if (r5 == 0) goto L_0x005a
                    com.mediatek.ims.rcsua.RcsUaService r0 = com.mediatek.ims.rcsua.RcsUaService.this     // Catch:{ RemoteException -> 0x0058 }
                    com.mediatek.ims.rcsua.RcsUaService$ServiceDeathRecipient r0 = r0.deathRecipient     // Catch:{ RemoteException -> 0x0058 }
                    r1 = 0
                    r5.linkToDeath(r0, r1)     // Catch:{ RemoteException -> 0x0058 }
                    com.mediatek.ims.rcsua.RcsUaService r0 = com.mediatek.ims.rcsua.RcsUaService.this     // Catch:{ RemoteException -> 0x0058 }
                    com.mediatek.ims.rcsua.service.IRcsUaService r1 = com.mediatek.ims.rcsua.service.IRcsUaService.Stub.asInterface(r5)     // Catch:{ RemoteException -> 0x0058 }
                    com.mediatek.ims.rcsua.service.IRcsUaService unused = r0.serviceIntf = r1     // Catch:{ RemoteException -> 0x0058 }
                    com.mediatek.ims.rcsua.RcsUaService r0 = com.mediatek.ims.rcsua.RcsUaService.this     // Catch:{ RemoteException -> 0x0058 }
                    boolean r0 = r0.activated     // Catch:{ RemoteException -> 0x0058 }
                    if (r0 == 0) goto L_0x0049
                    com.mediatek.ims.rcsua.RcsUaService r0 = com.mediatek.ims.rcsua.RcsUaService.this     // Catch:{ RemoteException -> 0x0058 }
                    com.mediatek.ims.rcsua.service.IRcsUaService r0 = r0.serviceIntf     // Catch:{ RemoteException -> 0x0058 }
                    r0.activate()     // Catch:{ RemoteException -> 0x0058 }
                    goto L_0x0052
                L_0x0049:
                    com.mediatek.ims.rcsua.RcsUaService r0 = com.mediatek.ims.rcsua.RcsUaService.this     // Catch:{ RemoteException -> 0x0058 }
                    com.mediatek.ims.rcsua.service.IRcsUaService r0 = r0.serviceIntf     // Catch:{ RemoteException -> 0x0058 }
                    r0.deactivate()     // Catch:{ RemoteException -> 0x0058 }
                L_0x0052:
                    com.mediatek.ims.rcsua.RcsUaService r0 = com.mediatek.ims.rcsua.RcsUaService.this     // Catch:{ RemoteException -> 0x0058 }
                    r0.notifyServiceUp()     // Catch:{ RemoteException -> 0x0058 }
                    goto L_0x005a
                L_0x0058:
                    r0 = move-exception
                    goto L_0x005b
                L_0x005a:
                L_0x005b:
                    return
                L_0x005c:
                    r1 = move-exception
                    monitor-exit(r0)     // Catch:{ all -> 0x005c }
                    throw r1
                */
                throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.rcsua.RcsUaService.C01752.onServiceConnected(android.content.ComponentName, android.os.IBinder):void");
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(RcsUaService.TAG, "onServiceDisconnected:" + name);
                if (RcsUaService.this.serviceIntf != null) {
                    RcsUaService.this.serviceIntf.asBinder().unlinkToDeath(RcsUaService.this.deathRecipient, 0);
                    IRcsUaService unused = RcsUaService.this.serviceIntf = null;
                    RcsUaService.this.notifyServiceDown();
                }
            }
        };
        this.callerContext = context.getApplicationContext();
        this.callerCallback = callback;
        Log.e(TAG, "callerCallback value " + callback);
        Log.e(TAG, "callerContext value " + this.callerContext);
        this.activated = Integer.valueOf(getSystemProperties(this.callerContext, "persist.vendor.service.rcs", "1")).intValue() == 1 ? true : z;
        this.acsSupported = isAcsAvailable(this.callerContext);
        Intent intent = new Intent("com.mediatek.ims.rcsua.BIND_SERVICE");
        intent.putExtra("phone_id", phoneId);
        if (options != null) {
            bundle = new Bundle(options);
        }
        bundle.putBoolean("OPTION_ACS_SUPPORT", this.acsSupported);
        intent.putExtra(EXTRA_OPTIONS, bundle);
        intent.setPackage("com.mediatek.ims.rcsua.service");
        this.callerContext.startService(intent);
        this.callerContext.bindService(intent, this.serviceConnection, 1);
    }

    /* access modifiers changed from: private */
    public void notifyServiceUp() {
        Log.d(TAG, "notifyServiceUp to callback[" + this.callerCallback + "]");
        Callback callback = this.callerCallback;
        if (callback != null) {
            callback.serviceConnected(INSTANCE);
        }
    }

    /* access modifiers changed from: private */
    public void notifyServiceDown() {
        Callback callback = this.callerCallback;
        if (callback != null) {
            callback.serviceDisconnected(INSTANCE);
        }
    }

    private class ServiceDeathRecipient implements IBinder.DeathRecipient {
        private ServiceDeathRecipient() {
        }

        public void binderDied() {
            IRcsUaService unused = RcsUaService.this.serviceIntf = null;
            RcsUaService.this.clients.clear();
            RcsUaService.this.acsCallbacks.clear();
            IAcsEventCallback unused2 = RcsUaService.this.acsEventCallback = null;
            RcsUaService.this.notifyServiceDown();
        }
    }

    private static String getSystemProperties(Context context, String name, String defaultValue) {
        String value = defaultValue;
        try {
            Class systemProperties = context.getClassLoader().loadClass("android.os.SystemProperties");
            return (String) systemProperties.getMethod("get", new Class[]{String.class, String.class}).invoke(systemProperties, new Object[]{new String(name), new String(defaultValue)});
        } catch (Exception e) {
            return value;
        }
    }
}
