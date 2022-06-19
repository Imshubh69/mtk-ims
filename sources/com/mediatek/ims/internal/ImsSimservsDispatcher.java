package com.mediatek.ims.internal;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import com.mediatek.ims.ImsAdapter;
import com.mediatek.ims.ImsEventDispatcher;
import com.mediatek.ims.ImsService;
import com.mediatek.ims.ImsUtImpl;
import com.mediatek.ims.VaConstants;
import java.io.UnsupportedEncodingException;

public class ImsSimservsDispatcher implements ImsEventDispatcher.VaEventDispatcher {
    private static final boolean DUMP_TRANSACTION = true;
    private static final int IMC_MAX_XUI_LEN = 512;
    private static final boolean SENLOG = TextUtils.equals(Build.TYPE, "user");
    private static final String TAG = "ImsSimservsDispatcher";
    private static ImsSimservsDispatcher sInstance;
    private Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private Thread mHandlerThread;
    private ContactsContract.CommonDataKinds.Phone mPhone;
    private ImsAdapter.VaSocketIO mSocket;

    public ImsSimservsDispatcher(Context context, ImsAdapter.VaSocketIO io) {
        C01491 r0 = new Thread() {
            public void run() {
                Looper.prepare();
                Handler unused = ImsSimservsDispatcher.this.mHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        if (msg.obj instanceof ImsAdapter.VaEvent) {
                            ImsAdapter.VaEvent event = (ImsAdapter.VaEvent) msg.obj;
                            ImsSimservsDispatcher.log("ImsSimservsDispatcher receives request [" + msg.what + ", " + event.getDataLen() + "]");
                            switch (msg.what) {
                                case VaConstants.MSG_ID_NOTIFY_XUI_IND:
                                    ImsSimservsDispatcher.this.handleXuiUpdate(event);
                                    return;
                                default:
                                    ImsSimservsDispatcher.log("ImsSimservsDispatcher receives unhandled message [" + msg.what + "]");
                                    return;
                            }
                        }
                    }
                };
                Looper.loop();
            }
        };
        this.mHandlerThread = r0;
        this.mContext = context;
        this.mSocket = io;
        sInstance = this;
        r0.start();
    }

    public static ImsSimservsDispatcher getInstance() {
        return sInstance;
    }

    public void enableRequest(int phoneId) {
    }

    public void disableRequest(int phoneId) {
    }

    public void vaEventCallback(ImsAdapter.VaEvent event) {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(event.getRequestID(), event));
    }

    public void setSocket(ImsAdapter.VaSocketIO socket) {
        this.mSocket = socket;
    }

    private void sendVaEvent(ImsAdapter.VaEvent event) {
        log("ImsSimservsDispatcher send event [" + event.getRequestID() + ", " + event.getDataLen() + "]");
        this.mSocket.writeEvent(event);
    }

    /* access modifiers changed from: private */
    public void handleXuiUpdate(ImsAdapter.VaEvent event) {
        byte[] byteArray = event.getBytes(event.getInt());
        String xui = null;
        int phoneId = event.getPhoneId();
        if (byteArray == null) {
            log("handleXuiUpdate event.getBytes() = null");
            return;
        }
        try {
            xui = new String(byteArray, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log("handleXuiUpdate() UnsupportedEncodingException occured");
        }
        log("handleXuiUpdate xui=" + ImsUtImpl.encryptString(xui));
        ImsXuiManager.getInstance().setXui(phoneId, xui);
        ImsService imsService = ImsService.getInstance(this.mContext);
        if (imsService != null) {
            imsService.updateSelfIdentity(phoneId);
        }
    }

    /* access modifiers changed from: private */
    public static void log(String text) {
        Log.d("@M_ImsSimservsDispatcher", "[ims] ImsSimservsDispatcher " + text);
    }
}
