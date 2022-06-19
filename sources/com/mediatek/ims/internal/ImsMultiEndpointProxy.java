package com.mediatek.ims.internal;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.ims.ImsExternalCallState;
import android.telephony.ims.stub.ImsMultiEndpointImplBase;
import com.android.ims.internal.IImsExternalCallStateListener;
import com.mediatek.ims.internal.DialogEventPackageReceiver;
import java.util.List;

public class ImsMultiEndpointProxy extends ImsMultiEndpointImplBase {
    private static final int MSG_UPDATE = 1;
    private static final String TAG = "ImsMultiEndpointProxy";
    private final DialogEventPackageReceiver.Listener mDepListener;
    /* access modifiers changed from: private */
    public List<ImsExternalCallState> mExternalCallStateList;
    private Handler mHandler;
    private IImsExternalCallStateListener mListener = null;
    private DialogEventPackageReceiver mReceiver;

    public ImsMultiEndpointProxy(Context context) {
        C01482 r0 = new DialogEventPackageReceiver.ListenerBase() {
            public void onStateChanged(List<ImsExternalCallState> list) {
                ImsMultiEndpointProxy.this.update(list);
            }
        };
        this.mDepListener = r0;
        this.mReceiver = new DialogEventPackageReceiver(context, r0);
        this.mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        ImsMultiEndpointProxy imsMultiEndpointProxy = ImsMultiEndpointProxy.this;
                        imsMultiEndpointProxy.update(imsMultiEndpointProxy.mExternalCallStateList);
                        return;
                    default:
                        return;
                }
            }
        };
        Rlog.d(TAG, TAG);
    }

    public void requestImsExternalCallStateInfo() {
        this.mHandler.sendEmptyMessage(1);
    }

    /* access modifiers changed from: private */
    public void update(List<ImsExternalCallState> externalCallDialogs) {
        this.mExternalCallStateList = externalCallDialogs;
        onImsExternalCallStateUpdate(externalCallDialogs);
    }
}
