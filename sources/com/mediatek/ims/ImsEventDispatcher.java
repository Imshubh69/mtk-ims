package com.mediatek.ims;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.mediatek.ims.ImsAdapter;
import com.mediatek.ims.internal.CallControlDispatcher;
import com.mediatek.ims.internal.ImsDataTracker;
import com.mediatek.ims.internal.ImsSimservsDispatcher;
import com.mediatek.ims.internal.WfcDispatcher;
import java.util.ArrayList;
import java.util.Iterator;

public class ImsEventDispatcher extends Handler {
    private static final String TAG = "[ImsEventDispatcher]";
    private CallControlDispatcher mCallControlDispatcher;
    private Context mContext;
    private ImsDataTracker mImsDataTracker;
    private ImsSimservsDispatcher mSimservsDispatcher;
    private ImsAdapter.VaSocketIO mSocket;
    private ArrayList<VaEventDispatcher> mVaEventDispatcher = new ArrayList<>();
    private WfcDispatcher mWfcDispatcher;

    public interface VaEventDispatcher {
        void disableRequest(int i);

        void enableRequest(int i);

        void vaEventCallback(ImsAdapter.VaEvent vaEvent);
    }

    public ImsEventDispatcher(Context context, ImsAdapter.VaSocketIO IO) {
        this.mContext = context;
        this.mSocket = IO;
        createDispatcher();
    }

    /* access modifiers changed from: package-private */
    public void enableRequest(int phoneId) {
        Iterator<VaEventDispatcher> it = this.mVaEventDispatcher.iterator();
        while (it.hasNext()) {
            it.next().enableRequest(phoneId);
        }
    }

    /* access modifiers changed from: package-private */
    public void disableRequest(int phoneId) {
        Iterator<VaEventDispatcher> it = this.mVaEventDispatcher.iterator();
        while (it.hasNext()) {
            it.next().disableRequest(phoneId);
        }
    }

    private void createDispatcher() {
        CallControlDispatcher callControlDispatcher = new CallControlDispatcher(this.mContext, this.mSocket);
        this.mCallControlDispatcher = callControlDispatcher;
        this.mVaEventDispatcher.add(callControlDispatcher);
        ImsDataTracker imsDataTracker = new ImsDataTracker(this.mContext, this.mSocket);
        this.mImsDataTracker = imsDataTracker;
        this.mVaEventDispatcher.add(imsDataTracker);
        ImsSimservsDispatcher imsSimservsDispatcher = new ImsSimservsDispatcher(this.mContext, this.mSocket);
        this.mSimservsDispatcher = imsSimservsDispatcher;
        this.mVaEventDispatcher.add(imsSimservsDispatcher);
        WfcDispatcher wfcDispatcher = new WfcDispatcher(this.mContext, this.mSocket);
        this.mWfcDispatcher = wfcDispatcher;
        this.mVaEventDispatcher.add(wfcDispatcher);
    }

    public void handleMessage(Message msg) {
        dispatchCallback((ImsAdapter.VaEvent) msg.obj);
    }

    /* access modifiers changed from: package-private */
    public void dispatchCallback(ImsAdapter.VaEvent event) {
        Log.d(TAG, "dispatchCallback: request ID:" + ImsAdapter.requestIdToString(event.getRequestID()));
        switch (event.getRequestID()) {
            case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_INFORMATION_REQ /*900002*/:
            case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_REQ /*900008*/:
            case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_REQ /*900011*/:
            case VaConstants.MSG_ID_REQUEST_PCSCF_DISCOVERY /*900403*/:
                this.mImsDataTracker.vaEventCallback(event);
                return;
            case VaConstants.MSG_ID_IMS_ENABLE_IND /*900003*/:
                enableRequest(event.getPhoneId());
                return;
            case VaConstants.MSG_ID_IMS_DISABLE_IND /*900004*/:
                disableRequest(event.getPhoneId());
                return;
            case VaConstants.MSG_ID_NOTIFY_XUI_IND /*900401*/:
                this.mSimservsDispatcher.vaEventCallback(event);
                return;
            case VaConstants.MSG_ID_NOTIFY_SS_PROGRESS_INDICATION /*900402*/:
                this.mCallControlDispatcher.vaEventCallback(event);
                return;
            case VaConstants.MSG_ID_REQUEST_VOWIFI_RELATED_INFO /*900406*/:
                this.mWfcDispatcher.vaEventCallback(event);
                return;
            default:
                Log.d(TAG, "Receive unsupported Request ID");
                return;
        }
    }
}
