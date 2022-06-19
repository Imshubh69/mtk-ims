package com.mediatek.wfo.ril;

import android.content.Context;
import android.os.Handler;
import android.os.Registrant;
import android.os.RegistrantList;

public abstract class MwiBaseCommands implements MwiCommandsInterface {
    protected Context mContext;
    protected RegistrantList mNattKeepAliveChangedRegistrants = new RegistrantList();
    protected int mPhoneId;
    protected RegistrantList mRequestGeoLocationRegistrants = new RegistrantList();
    protected RegistrantList mRssiThresholdChangedRegistrants = new RegistrantList();
    protected RegistrantList mWfcPdnStateChangedRegistrants = new RegistrantList();
    protected RegistrantList mWifiLockRegistrants = new RegistrantList();
    protected RegistrantList mWifiPdnActivatedRegistrants = new RegistrantList();
    protected RegistrantList mWifiPdnErrorRegistrants = new RegistrantList();
    protected RegistrantList mWifiPdnHandoverRegistrants = new RegistrantList();
    protected RegistrantList mWifiPdnOosRegistrants = new RegistrantList();
    protected RegistrantList mWifiPdnRoveOutRegistrants = new RegistrantList();
    protected RegistrantList mWifiPingRequestRegistrants = new RegistrantList();

    public MwiBaseCommands(Context context, int instanceId) {
        this.mContext = context;
        this.mPhoneId = instanceId;
    }

    public void registerRssiThresholdChanged(Handler h, int what, Object obj) {
        this.mRssiThresholdChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterRssiThresholdChanged(Handler h) {
        this.mRssiThresholdChangedRegistrants.remove(h);
    }

    public void registerWifiPdnActivated(Handler h, int what, Object obj) {
        this.mWifiPdnActivatedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterWifiPdnActivate(Handler h) {
        this.mWifiPdnActivatedRegistrants.remove(h);
    }

    public void registerWifiPdnError(Handler h, int what, Object obj) {
        this.mWifiPdnErrorRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterWifiPdnError(Handler h) {
        this.mWifiPdnErrorRegistrants.remove(h);
    }

    public void registerWifiPdnHandover(Handler h, int what, Object obj) {
        this.mWifiPdnHandoverRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterWifiPdnHandover(Handler h) {
        this.mWifiPdnHandoverRegistrants.remove(h);
    }

    public void registerWifiPdnRoveOut(Handler h, int what, Object obj) {
        this.mWifiPdnRoveOutRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterWifiPdnRoveOut(Handler h) {
        this.mWifiPdnRoveOutRegistrants.remove(h);
    }

    public void registerRequestGeoLocation(Handler h, int what, Object obj) {
        this.mRequestGeoLocationRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterRequestGeoLocation(Handler h) {
        this.mRequestGeoLocationRegistrants.remove(h);
    }

    public void registerWfcPdnStateChanged(Handler h, int what, Object obj) {
        this.mWfcPdnStateChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterWfcPdnStateChanged(Handler h) {
        this.mWfcPdnStateChangedRegistrants.remove(h);
    }

    public void registerWifiPingRequest(Handler h, int what, Object obj) {
        this.mWifiPingRequestRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterWifiPingRequest(Handler h) {
        this.mWifiPingRequestRegistrants.remove(h);
    }

    public void registerWifiPdnOos(Handler h, int what, Object obj) {
        this.mWifiPdnOosRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterWifiPdnOos(Handler h) {
        this.mWifiPdnOosRegistrants.remove(h);
    }

    public void registerWifiLock(Handler h, int what, Object obj) {
        this.mWifiLockRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterWifiLock(Handler h) {
        this.mWifiLockRegistrants.remove(h);
    }

    public void registerNattKeepAliveChanged(Handler h, int what, Object obj) {
        this.mNattKeepAliveChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unrgisterNattKeepAliveChanged(Handler h) {
        this.mNattKeepAliveChangedRegistrants.remove(h);
    }
}
