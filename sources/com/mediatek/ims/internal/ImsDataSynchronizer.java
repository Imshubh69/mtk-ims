package com.mediatek.ims.internal;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.util.SparseArray;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.mediatek.ims.common.SubscriptionManagerHelper;
import java.io.Serializable;

public class ImsDataSynchronizer {
    public static final int ACTION_ACTIVATION = 1;
    public static final int ACTION_DEACTIVATION = 0;
    public static final int EVENT_BEARER_STATE_CHANGED = 8;
    public static final int EVENT_CONNECT = 0;
    public static final int EVENT_CONNECT_DONE = 1;
    public static final int EVENT_DISCONNECT = 2;
    public static final int EVENT_DISCONNECT_DONE = 3;
    public static final int EVENT_IMS_DATA_INFO = 7;
    public static final int EVENT_MD_RESTART = 5;
    public static final int EVENT_SET_BEARER_NOTIFICATION_DONE = 6;
    public static final int EVENT_SUBSCRIPTIONS_CHANGED = 4;
    private String TAG = ImsDataSynchronizer.class.getSimpleName();
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public ImsDataTracker mDataTracker;
    private DataConnection mEmcDataConnection;
    private DataConnection mImsDataConnection;
    private HandlerThread mImsDcHandlerThread;
    /* access modifiers changed from: private */
    public int mPhoneId;
    /* access modifiers changed from: private */
    public int mSubId;

    public ImsDataSynchronizer(Context context, ImsDataTracker dataTracker, int phoneId) {
        this.mContext = context;
        this.mDataTracker = dataTracker;
        this.mPhoneId = phoneId;
        this.mSubId = SubscriptionManagerHelper.getSubIdUsingPhoneId(phoneId);
        HandlerThread handlerThread = new HandlerThread("ImsDcHandlerThread");
        this.mImsDcHandlerThread = handlerThread;
        handlerThread.start();
        this.mImsDataConnection = new DataConnection("ims", new Handler(this.mImsDcHandlerThread.getLooper()), 4);
        this.mEmcDataConnection = new DataConnection("emergency", new Handler(this.mImsDcHandlerThread.getLooper()), 10);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyMdRequest(com.mediatek.ims.internal.ImsDataSynchronizer.ImsBearerRequest r3) {
        /*
            r2 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "got request: "
            r0.append(r1)
            r0.append(r3)
            java.lang.String r0 = r0.toString()
            r2.logd(r0)
            java.lang.String r0 = r3.getCapability()
            int r1 = r0.hashCode()
            switch(r1) {
                case 104399: goto L_0x002a;
                case 1629013393: goto L_0x0020;
                default: goto L_0x001f;
            }
        L_0x001f:
            goto L_0x0034
        L_0x0020:
            java.lang.String r1 = "emergency"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x001f
            r0 = 1
            goto L_0x0035
        L_0x002a:
            java.lang.String r1 = "ims"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x001f
            r0 = 0
            goto L_0x0035
        L_0x0034:
            r0 = -1
        L_0x0035:
            switch(r0) {
                case 0: goto L_0x0068;
                case 1: goto L_0x0051;
                default: goto L_0x0038;
            }
        L_0x0038:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "not support capability: "
            r0.append(r1)
            java.lang.String r1 = r3.getCapability()
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            r2.loge(r0)
            goto L_0x007f
        L_0x0051:
            com.mediatek.ims.internal.ImsDataSynchronizer$DataConnection r0 = r2.mEmcDataConnection
            android.os.Handler r0 = r0.getHandler()
            com.mediatek.ims.internal.ImsDataSynchronizer$DataConnection r1 = r2.mEmcDataConnection
            r1.putRequest(r3)
            int r1 = r3.getRequest()
            android.os.Message r1 = r0.obtainMessage(r1)
            r0.sendMessage(r1)
            goto L_0x007f
        L_0x0068:
            com.mediatek.ims.internal.ImsDataSynchronizer$DataConnection r0 = r2.mImsDataConnection
            android.os.Handler r0 = r0.getHandler()
            com.mediatek.ims.internal.ImsDataSynchronizer$DataConnection r1 = r2.mImsDataConnection
            r1.putRequest(r3)
            int r1 = r3.getRequest()
            android.os.Message r1 = r0.obtainMessage(r1)
            r0.sendMessage(r1)
        L_0x007f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.internal.ImsDataSynchronizer.notifyMdRequest(com.mediatek.ims.internal.ImsDataSynchronizer$ImsBearerRequest):void");
    }

    public void notifyMdRestart() {
        logd("notifyMdRestart");
        Handler imsHandle = this.mImsDataConnection.getHandler();
        imsHandle.sendMessage(imsHandle.obtainMessage(5));
        Handler emcHandle = this.mEmcDataConnection.getHandler();
        emcHandle.sendMessage(emcHandle.obtainMessage(5));
    }

    public void notifyClearCodesEvent(int cause, int capability) {
        logd("notifyClearCodesEvent,cause= " + cause + " capability= " + capability);
    }

    public void onSubscriptionsChanged() {
        int newSubId = SubscriptionManagerHelper.getSubIdUsingPhoneId(this.mPhoneId);
        if (this.mSubId != newSubId) {
            logd("onSubscriptionsChanged: subId: " + this.mSubId + ", newSubId: " + newSubId);
            this.mSubId = newSubId;
            this.mImsDataConnection.onSubscriptionsChanged();
            this.mEmcDataConnection.onSubscriptionsChanged();
        }
    }

    /* access modifiers changed from: private */
    public boolean earlyConfirmReqNetworkToMd() {
        if ("OP07".equals(SystemProperties.get("persist.vendor.operator.optr", ""))) {
            return false;
        }
        return true;
    }

    public void logd(String s) {
        Rlog.d(this.TAG, s);
    }

    public void logi(String s) {
        Rlog.i(this.TAG, s);
    }

    public void loge(String s) {
        Rlog.e(this.TAG, s);
    }

    public class DataConnection extends StateMachine {
        private static final int STATUS_ABORT = 1;
        private static final int STATUS_SUCCESS = 0;
        private String TAG = "DC-";
        /* access modifiers changed from: private */
        public ActivatingState mActivatingState = new ActivatingState();
        /* access modifiers changed from: private */
        public ActiveState mActiveState = new ActiveState();
        private NetworkAvailableCallback mAvailableListener;
        private int mCapability;
        /* access modifiers changed from: private */
        public DataConnection mConn = this;
        /* access modifiers changed from: private */
        public ConnectivityManager mConnectivityManager;
        private DefaultState mDefaultState = new DefaultState();
        /* access modifiers changed from: private */
        public DisconnectingState mDisconnectingState = new DisconnectingState();
        /* access modifiers changed from: private */
        public SparseArray<ImsBearerRequest> mImsNetworkRequests = new SparseArray<>();
        /* access modifiers changed from: private */
        public InactiveState mInactiveState = new InactiveState();
        private NetworkLostCallback mLostListener;
        /* access modifiers changed from: private */
        public String mPdnSatate = "DefaultState";

        public DataConnection(String name, Handler mHandler, int capability) {
            super(name, mHandler);
            this.mCapability = capability;
            this.mConnectivityManager = (ConnectivityManager) ImsDataSynchronizer.this.mContext.getSystemService("connectivity");
            this.mAvailableListener = new NetworkAvailableCallback();
            this.mLostListener = new NetworkLostCallback();
            addState(this.mDefaultState);
            addState(this.mInactiveState, this.mDefaultState);
            addState(this.mActivatingState, this.mDefaultState);
            addState(this.mActiveState, this.mDefaultState);
            addState(this.mDisconnectingState, this.mDefaultState);
            setInitialState(this.mInactiveState);
            start();
        }

        private class DefaultState extends State {
            private DefaultState() {
            }

            public void enter() {
                String unused = DataConnection.this.mPdnSatate = "DefaultState";
                DataConnection.this.logd("enter");
            }

            public void exit() {
                DataConnection.this.logd("exit");
            }

            public boolean processMessage(Message msg) {
                int i = msg.what;
                return true;
            }
        }

        private class InactiveState extends State {
            private InactiveState() {
            }

            public void enter() {
                String unused = DataConnection.this.mPdnSatate = "InactiveState";
                DataConnection.this.refreshNetworkLostListener();
                DataConnection.this.logd(" enter");
            }

            public void exit() {
                DataConnection.this.logd("exit");
            }

            public boolean processMessage(Message msg) {
                DataConnection dataConnection = DataConnection.this;
                dataConnection.logd("msg=" + DataConnection.this.msgToString(msg.what));
                switch (msg.what) {
                    case 0:
                    case 4:
                        if (DataConnection.this.requestNetwork()) {
                            if (ImsDataSynchronizer.this.earlyConfirmReqNetworkToMd()) {
                                DataConnection.this.confirmRequestNetworkToMd(0, 0);
                            }
                            DataConnection dataConnection2 = DataConnection.this;
                            dataConnection2.transitionTo(dataConnection2.mActivatingState);
                        }
                        return true;
                    case 1:
                        DataConnection dataConnection3 = DataConnection.this;
                        dataConnection3.transitionTo(dataConnection3.mActiveState);
                        return true;
                    case 2:
                        if (DataConnection.this.mImsNetworkRequests.get(0) != null) {
                            DataConnection.this.logd("handle pdn abort requested");
                            DataConnection.this.confirmRequestNetworkToMd(0, 1);
                        }
                        DataConnection.this.releaseNetwork();
                        DataConnection.this.confirmReleaseNetworkToMd(2);
                        return true;
                    case 5:
                        DataConnection.this.mConn.clear();
                        DataConnection.this.releaseNetwork();
                        return true;
                    default:
                        DataConnection dataConnection4 = DataConnection.this;
                        dataConnection4.loge("not handle the messag " + DataConnection.this.msgToString(msg.what));
                        return false;
                }
            }
        }

        private class ActivatingState extends State {
            private ActivatingState() {
            }

            public void enter() {
                String unused = DataConnection.this.mPdnSatate = "ActivatingState";
                DataConnection.this.logd("enter");
            }

            public void exit() {
                DataConnection.this.logd("exit");
            }

            public boolean processMessage(Message msg) {
                DataConnection dataConnection = DataConnection.this;
                dataConnection.logd("msg=" + DataConnection.this.msgToString(msg.what));
                switch (msg.what) {
                    case 0:
                        DataConnection dataConnection2 = DataConnection.this;
                        dataConnection2.loge("just send confirm: " + DataConnection.this.msgToString(msg.what));
                        DataConnection.this.confirmRequestNetworkToMd(0, 1);
                        return true;
                    case 1:
                        if (!ImsDataSynchronizer.this.earlyConfirmReqNetworkToMd()) {
                            DataConnection.this.confirmRequestNetworkToMd(0, 0);
                        }
                        DataConnection dataConnection3 = DataConnection.this;
                        dataConnection3.transitionTo(dataConnection3.mActiveState);
                        return true;
                    case 2:
                        DataConnection.this.confirmRequestNetworkToMd(0, 1);
                        DataConnection.this.deferMessage(msg);
                        DataConnection dataConnection4 = DataConnection.this;
                        dataConnection4.transitionTo(dataConnection4.mInactiveState);
                        return true;
                    case 5:
                        DataConnection.this.mConn.clear();
                        DataConnection.this.releaseNetwork();
                        DataConnection dataConnection5 = DataConnection.this;
                        dataConnection5.transitionTo(dataConnection5.mInactiveState);
                        return true;
                    default:
                        DataConnection dataConnection6 = DataConnection.this;
                        dataConnection6.loge("not handle the message " + DataConnection.this.msgToString(msg.what));
                        return false;
                }
            }
        }

        private class ActiveState extends State {
            private ActiveState() {
            }

            public void enter() {
                String unused = DataConnection.this.mPdnSatate = "ActiveState";
                DataConnection.this.logd("enter");
            }

            public void exit() {
                DataConnection.this.logd("exit");
            }

            public boolean processMessage(Message msg) {
                DataConnection dataConnection = DataConnection.this;
                dataConnection.logd("msg=" + DataConnection.this.msgToString(msg.what));
                switch (msg.what) {
                    case 2:
                        DataConnection.this.releaseNetwork();
                        DataConnection dataConnection2 = DataConnection.this;
                        dataConnection2.transitionTo(dataConnection2.mDisconnectingState);
                        return true;
                    case 3:
                        DataConnection dataConnection3 = DataConnection.this;
                        dataConnection3.transitionTo(dataConnection3.mInactiveState);
                        return true;
                    case 5:
                        DataConnection.this.mConn.clear();
                        DataConnection.this.releaseNetwork();
                        DataConnection dataConnection4 = DataConnection.this;
                        dataConnection4.transitionTo(dataConnection4.mInactiveState);
                        return true;
                    default:
                        DataConnection dataConnection5 = DataConnection.this;
                        dataConnection5.loge("not handle the message " + DataConnection.this.msgToString(msg.what));
                        return false;
                }
            }
        }

        private class DisconnectingState extends State {
            private DisconnectingState() {
            }

            public void enter() {
                String unused = DataConnection.this.mPdnSatate = "DisconnectingState";
                DataConnection.this.logd("enter");
            }

            public void exit() {
                DataConnection.this.logd("exit");
            }

            public boolean processMessage(Message msg) {
                DataConnection dataConnection = DataConnection.this;
                dataConnection.logd("msg=" + DataConnection.this.msgToString(msg.what));
                switch (msg.what) {
                    case 3:
                        DataConnection.this.confirmReleaseNetworkToMd(2);
                        DataConnection dataConnection2 = DataConnection.this;
                        dataConnection2.transitionTo(dataConnection2.mInactiveState);
                        return true;
                    case 5:
                        DataConnection.this.mConn.clear();
                        DataConnection dataConnection3 = DataConnection.this;
                        dataConnection3.transitionTo(dataConnection3.mInactiveState);
                        return true;
                    default:
                        DataConnection dataConnection4 = DataConnection.this;
                        dataConnection4.loge("not handle the message " + DataConnection.this.msgToString(msg.what));
                        return false;
                }
            }
        }

        /* access modifiers changed from: private */
        public boolean requestNetwork() {
            logd("requestNetwork");
            if (ImsDataSynchronizer.this.mSubId < 0 && this.mCapability != 10) {
                loge("inValid subId: " + ImsDataSynchronizer.this.mSubId);
                return false;
            } else if (this.mImsNetworkRequests.get(0) == null) {
                loge("ImsBearerRequest is NULL");
                return false;
            } else {
                NetworkRequest.Builder builder = new NetworkRequest.Builder();
                builder.addCapability(this.mCapability);
                builder.addTransportType(0);
                builder.setNetworkSpecifier(String.valueOf(ImsDataSynchronizer.this.mSubId));
                NetworkRequest nwRequest = builder.build();
                logd("start requestNetwork for " + getName());
                this.mAvailableListener.setNetworkCallbackRegistered(true);
                this.mConnectivityManager.requestNetwork(nwRequest, this.mAvailableListener);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public void refreshNetworkLostListener() {
            logd("refreshNetworkLostListener");
            if (this.mPdnSatate.equals("ActivatingState") || this.mPdnSatate.equals("ActiveState") || this.mPdnSatate.equals("DisconnectingState")) {
                loge("inValid state: " + this.mPdnSatate);
                return;
            }
            try {
                this.mConnectivityManager.unregisterNetworkCallback(this.mLostListener);
            } catch (IllegalArgumentException e) {
                loge("cb already has been released!!");
            }
            long token = Binder.clearCallingIdentity();
            logd("refreshNetworkLostListener, uid = " + Binder.getCallingUid() + ", package name: " + ImsDataSynchronizer.this.mContext.getOpPackageName());
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.addCapability(this.mCapability);
            builder.addTransportType(0);
            builder.setNetworkSpecifier(String.valueOf(ImsDataSynchronizer.this.mSubId));
            this.mConnectivityManager.registerNetworkCallback(builder.build(), this.mLostListener);
            Binder.restoreCallingIdentity(token);
        }

        /* access modifiers changed from: private */
        public void confirmRequestNetworkToMd(int reqId, int status) {
            logd("confirmRequestNetworkToMd");
            ImsBearerRequest n = this.mImsNetworkRequests.get(reqId);
            for (int i = 0; i < this.mImsNetworkRequests.size(); i++) {
                logd("found Req: " + this.mImsNetworkRequests.valueAt(i));
            }
            if (n != null) {
                this.mImsNetworkRequests.remove(n.getRequest());
                ImsDataSynchronizer.this.mDataTracker.responseBearerConfirm(n.getRequest(), n.getAid(), n.getAction(), status, ImsDataSynchronizer.this.mPhoneId);
            }
        }

        /* access modifiers changed from: private */
        public void releaseNetwork() {
            logd("releaseNetwork");
            try {
                this.mConnectivityManager.unregisterNetworkCallback(this.mAvailableListener);
                this.mAvailableListener.setNetworkCallbackRegistered(false);
            } catch (IllegalArgumentException e) {
                loge("cb already has been released!!");
            }
        }

        /* access modifiers changed from: private */
        public void confirmReleaseNetworkToMd(int reqId) {
            logd("confirmReleaseNetworkToMd");
            ImsBearerRequest n = this.mImsNetworkRequests.get(reqId);
            for (int i = 0; i < this.mImsNetworkRequests.size(); i++) {
                logd("found Req: " + this.mImsNetworkRequests.valueAt(i));
            }
            if (n != null) {
                this.mImsNetworkRequests.remove(n.getRequest());
                ImsDataSynchronizer.this.mDataTracker.responseBearerConfirm(n.getRequest(), n.getAid(), n.getAction(), 0, ImsDataSynchronizer.this.mPhoneId);
            }
        }

        public void putRequest(ImsBearerRequest request) {
            if (this.mImsNetworkRequests.get(request.getRequest()) == null) {
                this.mImsNetworkRequests.put(request.getRequest(), request);
                return;
            }
            loge("request already exist: " + request);
        }

        public void clear() {
            logd("clear");
            this.mImsNetworkRequests.clear();
        }

        private class NetworkAvailableCallback extends ConnectivityManager.NetworkCallback {
            private boolean mNetworkCallbackRegistered = false;

            public NetworkAvailableCallback() {
            }

            public void onAvailable(Network network) {
                NetworkInfo netInfo = DataConnection.this.mConnectivityManager.getNetworkInfo(network);
                DataConnection dataConnection = DataConnection.this;
                dataConnection.logd("onAvailable: networInfo: " + netInfo + " mNetworkCallbackRegistered " + this.mNetworkCallbackRegistered);
                if (this.mNetworkCallbackRegistered) {
                    DataConnection.this.mConn.sendMessage(DataConnection.this.mConn.obtainMessage(1));
                }
            }

            public void setNetworkCallbackRegistered(boolean regState) {
                DataConnection dataConnection = DataConnection.this;
                dataConnection.logd("setNetworkCallbackRegistered regState= " + regState);
                this.mNetworkCallbackRegistered = regState;
            }
        }

        private class NetworkLostCallback extends ConnectivityManager.NetworkCallback {
            public NetworkLostCallback() {
            }

            public void onLost(Network network) {
                NetworkInfo netInfo = DataConnection.this.mConnectivityManager.getNetworkInfo(network);
                DataConnection dataConnection = DataConnection.this;
                dataConnection.logd("onLost: networInfo: " + netInfo);
                DataConnection.this.mConn.sendMessage(DataConnection.this.mConn.obtainMessage(3));
            }
        }

        public void onSubscriptionsChanged() {
            logd("onSubscriptionsChanged");
            refreshNetworkLostListener();
            if (this.mImsNetworkRequests.get(0) != null) {
                sendMessage(obtainMessage(4));
            }
        }

        /* access modifiers changed from: private */
        public String msgToString(int msg) {
            switch (msg) {
                case 0:
                    return "EVENT_CONNECT";
                case 1:
                    return "EVENT_CONNECT_DONE";
                case 2:
                    return "EVENT_DISCONNECT";
                case 3:
                    return "EVENT_DISCONNECT_DONE";
                case 4:
                    return "EVENT_SUBSCRIPTIONS_CHANGED";
                case 5:
                    return "EVENT_MD_RESTART";
                default:
                    return "<unknown request>";
            }
        }

        public void logd(String s) {
            Rlog.d(this.TAG + getName() + "[" + ImsDataSynchronizer.this.mPhoneId + "]", this.mPdnSatate + ": " + s);
        }

        public void logi(String s) {
            Rlog.i(this.TAG + getName() + "[" + ImsDataSynchronizer.this.mPhoneId + "]", this.mPdnSatate + ": " + s);
        }

        public void loge(String s) {
            Rlog.e(this.TAG + getName() + "[" + ImsDataSynchronizer.this.mPhoneId + "]", this.mPdnSatate + ": " + s);
        }
    }

    public static class ImsBearerRequest implements Serializable {
        private static final long serialVersionUID = -5053412967314724078L;
        private int mAction;
        private int mAid;
        private String mCapability;
        private int mPhoneId;
        private int mRequest;

        public ImsBearerRequest(int aid, int action, int phoneId, int request, String capability) {
            this.mAid = aid;
            this.mAction = action;
            this.mPhoneId = phoneId;
            this.mRequest = request;
            this.mCapability = capability;
        }

        public int getAid() {
            return this.mAid;
        }

        public int getAction() {
            return this.mAction;
        }

        public int getPhoneId() {
            return this.mPhoneId;
        }

        public int getRequest() {
            return this.mRequest;
        }

        public String getCapability() {
            return this.mCapability;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("aid: " + this.mAid);
            builder.append(" action: " + this.mAction);
            builder.append(" phoneId: " + this.mPhoneId);
            switch (this.mRequest) {
                case 0:
                    builder.append(" Request: EVENT_CONNECT");
                    break;
                case 2:
                    builder.append(" Request: EVENT_DISCONNECT");
                    break;
            }
            builder.append(" Capability: " + this.mCapability + " }");
            return builder.toString();
        }
    }
}
