package com.mediatek.ims.internal;

import android.content.Context;
import android.net.NetworkInfo;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.mediatek.ims.ImsAdapter;
import com.mediatek.ims.ImsEventDispatcher;
import com.mediatek.ims.internal.ImsDataSynchronizer;
import com.mediatek.ims.ril.ImsCommandsInterface;
import java.util.Arrays;

public class ImsDataTracker implements ImsEventDispatcher.VaEventDispatcher {
    private String TAG = ImsDataTracker.class.getSimpleName();
    private Context mContext;
    /* access modifiers changed from: private */
    public DataDispatcher[] mDispatchers;
    private ImsCommandsInterface[] mImsRILAdapters = null;
    private int mIsBearerNotify = 1;
    /* access modifiers changed from: private */
    public MdCapability mMdCapability;
    private int[] mModemEmergencyPdnState;
    private int[] mModemImsPdnState;
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangedListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        public void onSubscriptionsChanged() {
            ImsDataTracker.this.logd("onSubscriptionsChanged");
            switch (C01463.$SwitchMap$com$mediatek$ims$internal$ImsDataTracker$MdCapability[ImsDataTracker.this.mMdCapability.ordinal()]) {
                case 1:
                    for (int i = 0; i < ImsDataTracker.this.mPhoneNum; i++) {
                        ImsDataTracker.this.mDispatchers[i].onSubscriptionsChanged();
                    }
                    return;
                case 2:
                    for (int i2 = 0; i2 < ImsDataTracker.this.mPhoneNum; i2++) {
                        ImsDataTracker.this.mSynchronizers[i2].onSubscriptionsChanged();
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private final Object mPdnStateLock = new Object();
    /* access modifiers changed from: private */
    public int mPhoneNum;
    private ImsAdapter.VaSocketIO mSocket;
    /* access modifiers changed from: private */
    public ImsDataSynchronizer[] mSynchronizers;
    private Handler mdHander = new Handler() {
        /* Debug info: failed to restart local var, previous not found, register: 3 */
        public synchronized void handleMessage(Message msg) {
            switch (msg.what) {
                case 5:
                    ImsDataTracker.this.onMdRestart((AsyncResult) msg.obj);
                    break;
                case 7:
                    ImsDataTracker.this.onImsDataInfo((AsyncResult) msg.obj);
                    break;
                case 8:
                    ImsDataTracker.this.onImsBearerChanged((AsyncResult) msg.obj);
                    break;
                default:
                    ImsDataTracker imsDataTracker = ImsDataTracker.this;
                    imsDataTracker.loge("not handle the message: " + msg.what);
                    break;
            }
        }
    };

    public enum MdCapability {
        LEGACY,
        AUTOSETUPIMS
    }

    public ImsDataTracker(Context context, ImsAdapter.VaSocketIO IO) {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        this.mPhoneNum = phoneCount;
        this.mDispatchers = new DataDispatcher[phoneCount];
        for (int i = 0; i < this.mPhoneNum; i++) {
            this.mDispatchers[i] = new DataDispatcher(context, this, i);
        }
        this.mSocket = IO;
        this.mMdCapability = MdCapability.LEGACY;
        SubscriptionManager.from(context).addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
    }

    public void enableRequest(int phoneId) {
        logd("receive enableRequest on phone: " + phoneId);
        this.mDispatchers[phoneId].enableRequest(phoneId);
    }

    public void disableRequest(int phoneId) {
        logd("receive disableRequest on phone: " + phoneId);
        this.mDispatchers[phoneId].disableRequest(phoneId);
    }

    public void vaEventCallback(ImsAdapter.VaEvent event) {
        logd("send event" + event.getRequestID() + " to phone " + event.getPhoneId());
        this.mDispatchers[event.getPhoneId()].vaEventCallback(event);
    }

    public void sendVaEvent(ImsAdapter.VaEvent event) {
        this.mSocket.writeEvent(event);
    }

    public ImsDataTracker(Context context, ImsCommandsInterface[] adapters) {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        this.mPhoneNum = phoneCount;
        this.mSynchronizers = new ImsDataSynchronizer[phoneCount];
        this.mModemImsPdnState = new int[phoneCount];
        this.mModemEmergencyPdnState = new int[phoneCount];
        this.mContext = context;
        this.mImsRILAdapters = adapters;
        getImsPdnNotifyRule();
        for (int i = 0; i < this.mPhoneNum; i++) {
            this.mSynchronizers[i] = new ImsDataSynchronizer(context, this, i);
            this.mImsRILAdapters[i].registerForBearerState(this.mdHander, 8, (Object) null);
            this.mImsRILAdapters[i].registerForBearerInit(this.mdHander, 5, (Object) null);
            this.mImsRILAdapters[i].registerForImsDataInfoNotify(this.mdHander, 7, (Object) null);
            this.mModemImsPdnState[i] = NetworkInfo.State.UNKNOWN.ordinal();
            this.mModemEmergencyPdnState[i] = NetworkInfo.State.UNKNOWN.ordinal();
            setImsBearerNotification(i, this.mIsBearerNotify);
        }
        this.mMdCapability = MdCapability.AUTOSETUPIMS;
        SubscriptionManager.from(this.mContext).addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
    }

    public void notifyMultiSimConfigChanged(int activeModemCount, ImsCommandsInterface[] adapters) {
        int prevActiveModemCount = this.mModemImsPdnState.length;
        logi("notifyMultiSimConfigChanged, phone:" + prevActiveModemCount + "->" + activeModemCount);
        this.mPhoneNum = activeModemCount;
        if (prevActiveModemCount != activeModemCount && prevActiveModemCount <= activeModemCount) {
            this.mImsRILAdapters = adapters;
            if (this.mContext != null) {
                this.mSynchronizers = (ImsDataSynchronizer[]) Arrays.copyOf(this.mSynchronizers, activeModemCount);
                this.mModemImsPdnState = Arrays.copyOf(this.mModemImsPdnState, activeModemCount);
                this.mModemEmergencyPdnState = Arrays.copyOf(this.mModemEmergencyPdnState, activeModemCount);
                for (int i = prevActiveModemCount; i < activeModemCount; i++) {
                    this.mSynchronizers[i] = new ImsDataSynchronizer(this.mContext, this, i);
                    this.mImsRILAdapters[i].registerForBearerState(this.mdHander, 8, (Object) null);
                    this.mImsRILAdapters[i].registerForBearerInit(this.mdHander, 5, (Object) null);
                    this.mImsRILAdapters[i].registerForImsDataInfoNotify(this.mdHander, 7, (Object) null);
                    this.mModemImsPdnState[i] = NetworkInfo.State.UNKNOWN.ordinal();
                    this.mModemEmergencyPdnState[i] = NetworkInfo.State.UNKNOWN.ordinal();
                    setImsBearerNotification(i, this.mIsBearerNotify);
                }
            }
        }
    }

    private void getImsPdnNotifyRule() {
        this.mIsBearerNotify = SystemProperties.getInt("persist.vendor.radio.ims.pdn.notify", 1);
        logd("mIsBearerNotify rule set to " + this.mIsBearerNotify);
    }

    /* access modifiers changed from: private */
    public void onImsBearerChanged(AsyncResult ar) {
        int event;
        logd("onImsBearerChanged");
        String[] bearerInfo = (String[]) ar.result;
        if (bearerInfo == null) {
            loge("parameter is NULL");
        } else if (bearerInfo.length == 4) {
            logd(Arrays.toString(bearerInfo));
            int phoneId = Integer.parseInt(bearerInfo[0]);
            int aid = Integer.parseInt(bearerInfo[1]);
            int action = Integer.parseInt(bearerInfo[2]);
            String capability = bearerInfo[3];
            if (action == 1) {
                event = 0;
            } else if (action == 0) {
                event = 2;
            } else {
                loge("unknown action: " + action);
                event = -1;
            }
            if (event >= 0) {
                updateModemPdnState(phoneId, capability, event);
                int i = this.mIsBearerNotify;
                if (i == 1 || i == 3) {
                    this.mSynchronizers[phoneId].notifyMdRequest(new ImsDataSynchronizer.ImsBearerRequest(aid, action, phoneId, event, capability));
                }
            }
        } else {
            loge("parameter format error: " + Arrays.toString(bearerInfo));
        }
    }

    /* access modifiers changed from: private */
    public void onMdRestart(AsyncResult ar) {
        logd("onMdRestart");
        int[] phoneArray = (int[]) ar.result;
        if (phoneArray == null || phoneArray.length == 0) {
            logd("can't get phone instance");
            return;
        }
        int phoneId = phoneArray[0];
        logd("onMdRestart, reset phone = " + phoneId + " connection state");
        this.mSynchronizers[phoneId].notifyMdRestart();
        clearModemPdnState();
        setImsBearerNotification(phoneId, this.mIsBearerNotify);
    }

    /* access modifiers changed from: private */
    public void onImsDataInfo(AsyncResult ar) {
        int cap;
        String[] bearerInfo = (String[]) ar.result;
        if (bearerInfo == null) {
            loge("parameter is NULL");
        } else if (bearerInfo.length == 4) {
            logd("onImsDataInfo: " + Arrays.toString(bearerInfo));
            int phoneId = Integer.parseInt(bearerInfo[0]);
            String capability = bearerInfo[1];
            String event = bearerInfo[2];
            if (capability.equals("emergency")) {
                cap = 10;
            } else {
                cap = 4;
            }
            if ("ClearCodes".equals(event)) {
                this.mSynchronizers[phoneId].notifyClearCodesEvent(Integer.parseInt(bearerInfo[3]), cap);
            }
        } else {
            loge("parameter format error: " + Arrays.toString(bearerInfo));
        }
    }

    public void responseBearerConfirm(int event, int aid, int action, int status, int phoneId) {
        logd("send to MD, aid:" + aid + ", action:" + action + ", status:" + status + ", phoneId:" + phoneId);
        switch (event) {
            case 0:
                this.mImsRILAdapters[phoneId].responseBearerStateConfirm(aid, action, status, (Message) null);
                return;
            case 2:
                this.mImsRILAdapters[phoneId].responseBearerStateConfirm(aid, action, status, (Message) null);
                return;
            default:
                return;
        }
    }

    private void setImsBearerNotification(int phoneId, int enable) {
        logd("setImsBearerNotification enable: " + enable);
        this.mImsRILAdapters[phoneId].setImsBearerNotification(enable, (Message) null);
    }

    /* renamed from: com.mediatek.ims.internal.ImsDataTracker$3 */
    static /* synthetic */ class C01463 {
        static final /* synthetic */ int[] $SwitchMap$com$mediatek$ims$internal$ImsDataTracker$MdCapability;

        static {
            int[] iArr = new int[MdCapability.values().length];
            $SwitchMap$com$mediatek$ims$internal$ImsDataTracker$MdCapability = iArr;
            try {
                iArr[MdCapability.LEGACY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mediatek$ims$internal$ImsDataTracker$MdCapability[MdCapability.AUTOSETUPIMS.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public int[] getImsNetworkState(int capability) {
        int[] iArr;
        int[] iArr2;
        logd("capability/mPhoneNum/imsPdnState/emergencyPdnState are : " + capability + "/" + this.mPhoneNum + "/" + Arrays.toString(this.mModemImsPdnState) + Arrays.toString(this.mModemEmergencyPdnState));
        if (capability == 4) {
            synchronized (this.mPdnStateLock) {
                iArr2 = this.mModemImsPdnState;
            }
            return iArr2;
        } else if (capability == 10) {
            synchronized (this.mPdnStateLock) {
                iArr = this.mModemEmergencyPdnState;
            }
            return iArr;
        } else {
            int[] pdnState = new int[this.mPhoneNum];
            Arrays.fill(pdnState, NetworkInfo.State.UNKNOWN.ordinal());
            loge("getImsNetworkState failed becase of invalid capability : " + capability);
            return pdnState;
        }
    }

    private void updateModemPdnState(int phoneId, String capability, int event) {
        if (event == 0) {
            if (capability.equals("ims")) {
                this.mModemImsPdnState[phoneId] = NetworkInfo.State.CONNECTED.ordinal();
            } else if (capability.equals("emergency")) {
                this.mModemEmergencyPdnState[phoneId] = NetworkInfo.State.CONNECTED.ordinal();
            } else {
                loge("Not handle the capability: " + capability);
            }
        } else if (event != 2) {
            loge("Not handle the event: " + event);
        } else if (capability.equals("ims")) {
            this.mModemImsPdnState[phoneId] = NetworkInfo.State.DISCONNECTED.ordinal();
        } else if (capability.equals("emergency")) {
            this.mModemEmergencyPdnState[phoneId] = NetworkInfo.State.DISCONNECTED.ordinal();
        } else {
            loge("Not handle the capability: " + capability);
        }
    }

    private void clearModemPdnState() {
        synchronized (this.mPdnStateLock) {
            for (int i = 0; i < this.mPhoneNum; i++) {
                this.mModemImsPdnState[i] = NetworkInfo.State.UNKNOWN.ordinal();
                this.mModemEmergencyPdnState[i] = NetworkInfo.State.UNKNOWN.ordinal();
            }
        }
    }

    /* access modifiers changed from: private */
    public void logd(String s) {
        Rlog.d(this.TAG, s);
    }

    private void logi(String s) {
        Rlog.i(this.TAG, s);
    }

    /* access modifiers changed from: private */
    public void loge(String s) {
        Rlog.e(this.TAG, s);
    }
}
