package com.mediatek.ims;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.ims.ImsExternalCallState;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.mediatek.ims.ril.ImsCommandsInterface;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import vendor.mediatek.hardware.mtkradioex.V3_0.Dialog;

public class ImsEventPackageAdapter {
    static final int EVENT_IMS_DIALOG_INDICATION = 1;
    static final int EVENT_LTE_MESSAGE_WAITING = 0;
    private static final String LOG_TAG = "ImsEventPackageAdapter";
    private static final String TAG_DOUBLE_QUOTE = "<ascii_34>";
    private static final String TAG_NEXT_LINE = "<ascii_10>";
    private static final String TAG_RETURN = "<ascii_13>";
    private static final int TYPE_CONFERENCE_EVT_PKG = 1;
    private static final int TYPE_DIALOG_EVT_PKG = 2;
    private static final int TYPE_MWI = 3;
    static Constructor sImsExternalCallStateConstructfunc = null;
    private String mCEPData;
    private Context mContext;
    private String mDEPData;
    private MyHandler mHandler;
    private ImsCommandsInterface mImsRilAdapter;
    private String mMWIData;
    private int mPhoneId;

    ImsEventPackageAdapter(Context ctx, Handler handler, ImsCommandsInterface imsRilAdapter, int phoneId) {
        Rlog.d(LOG_TAG, "ImsEventPackageAdapter()");
        MyHandler myHandler = new MyHandler(handler.getLooper());
        this.mHandler = myHandler;
        this.mImsRilAdapter = imsRilAdapter;
        this.mContext = ctx;
        this.mPhoneId = phoneId;
        imsRilAdapter.registerForLteMsgWaiting(myHandler, 0, (Object) null);
        this.mImsRilAdapter.registerForImsDialog(this.mHandler, 1, (Object) null);
        needToReportMoreInfo();
    }

    public void close() {
        this.mImsRilAdapter.unregisterForLteMsgWaiting(this.mHandler);
        this.mImsRilAdapter.unregisterForImsDialog(this.mHandler);
    }

    /* access modifiers changed from: private */
    public void handleLetMessageWaiting(String[] msg) {
        Rlog.d(LOG_TAG, "handleLetMessageWaiting()");
        int[] intData = new int[4];
        int i = 0;
        while (i < 4) {
            try {
                intData[i] = Integer.parseInt(msg[i]);
                i++;
            } catch (NumberFormatException e) {
                Rlog.d(LOG_TAG, "handleLetMessageWaiting failed: invalid params");
                return;
            }
        }
        boolean isFirstPkt = false;
        int i2 = intData[0];
        int urcIdx = intData[2];
        int totalUrcCount = intData[3];
        String rawData = msg[4];
        if (msg.length < 6 || this.mPhoneId == Integer.parseInt(msg[5])) {
            if (urcIdx == 1) {
                isFirstPkt = true;
            }
            String concatData = concatData(isFirstPkt, this.mMWIData, rawData);
            this.mMWIData = concatData;
            if (urcIdx == totalUrcCount && this.mContext != null) {
                this.mMWIData = recoverDataFromAsciiTag(concatData);
                Intent intent = new Intent(ImsConstants.ACTION_LTE_MESSAGE_WAITING_INDICATION);
                intent.putExtra(ImsConstants.EXTRA_LTE_MWI_BODY, this.mMWIData);
                intent.putExtra(ImsConstants.EXTRA_PHONE_ID, this.mPhoneId);
                intent.addFlags(16777216);
                this.mContext.sendBroadcast(intent, "com.mediatek.permission.READ_LTE_MESSAGE_WAITING_INDICATION");
                return;
            }
            return;
        }
        Rlog.d(LOG_TAG, "handleLetMessageWaiting ignore, not the correct phone id");
    }

    private String concatData(boolean isFirst, String origData, String appendData) {
        if (isFirst) {
            return appendData;
        }
        return origData.concat(appendData);
    }

    private String recoverDataFromAsciiTag(String data) {
        return data.replaceAll(TAG_RETURN, "\r").replaceAll(TAG_DOUBLE_QUOTE, "\"").replaceAll(TAG_NEXT_LINE, "\n");
    }

    /* access modifiers changed from: private */
    public void handleDialogEventPackage(ArrayList<Dialog> dialogList) {
        Exception e;
        Rlog.d(LOG_TAG, "handleDialogEventPackage()");
        ArrayList arrayList = new ArrayList();
        if (sImsExternalCallStateConstructfunc == null) {
            Iterator<Dialog> it = dialogList.iterator();
            while (it.hasNext()) {
                Dialog dialog = it.next();
                Uri localAddr = Uri.parse(dialog.address);
                arrayList.add(new ImsExternalCallState(dialog.dialogId, Uri.parse(dialog.remoteAddress), localAddr, dialog.isPullable, dialog.callState, dialog.callType, dialog.isCallHeld));
                Rlog.d(LOG_TAG, "handleDialogEventPackage exCallState:" + dialog.dialogId + ImsServiceCallTracker.sensitiveEncode(dialog.remoteAddress) + ImsServiceCallTracker.sensitiveEncode(dialog.address) + dialog.isPullable + dialog.callState + dialog.callType + dialog.isCallHeld);
            }
        } else {
            Iterator<Dialog> it2 = dialogList.iterator();
            while (it2.hasNext()) {
                Dialog dialog2 = it2.next();
                Uri localAddr2 = Uri.parse(dialog2.address);
                Uri addr = Uri.parse(dialog2.remoteAddress);
                try {
                    e = (ImsExternalCallState) sImsExternalCallStateConstructfunc.newInstance(new Object[]{Integer.valueOf(dialog2.dialogId), addr, localAddr2, Boolean.valueOf(dialog2.isPullable), Integer.valueOf(dialog2.callState), Integer.valueOf(dialog2.callType), Boolean.valueOf(dialog2.isCallHeld), Boolean.valueOf(dialog2.isMt)});
                } catch (Exception e2) {
                    Rlog.d(LOG_TAG, "Use AOSP default ImsExternalCallState.");
                    e = new ImsExternalCallState(dialog2.dialogId, addr, localAddr2, dialog2.isPullable, dialog2.callState, dialog2.callType, dialog2.isCallHeld);
                }
                arrayList.add(e);
                Rlog.d(LOG_TAG, "handleDialogEventPackage exCallState:" + dialog2.dialogId + ImsServiceCallTracker.sensitiveEncode(dialog2.remoteAddress) + ImsServiceCallTracker.sensitiveEncode(dialog2.address) + dialog2.isPullable + dialog2.callState + dialog2.callType + dialog2.isCallHeld + dialog2.isMt);
            }
        }
        Intent intent = new Intent(ImsConstants.ACTION_IMS_DIALOG_EVENT_PACKAGE);
        intent.putParcelableArrayListExtra(ImsConstants.EXTRA_DEP_CONTENT, arrayList);
        LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(intent);
    }

    private void needToReportMoreInfo() {
        if (sImsExternalCallStateConstructfunc == null) {
            try {
                Constructor sImsExternalCallStateConstructfunc2 = ImsExternalCallState.class.getDeclaredConstructor(new Class[]{Integer.TYPE, Uri.class, Uri.class, Boolean.class, Integer.TYPE, Integer.TYPE, Boolean.class, Boolean.class});
                Rlog.d(LOG_TAG, "constructor function = " + sImsExternalCallStateConstructfunc2);
            } catch (Exception e) {
                Rlog.d(LOG_TAG, "Use AOSP default ImsExternalCallState.");
            }
        }
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper, (Handler.Callback) null, true);
        }

        public void handleMessage(Message msg) {
            Rlog.d(ImsEventPackageAdapter.LOG_TAG, "MsgId: " + msg.what);
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null) {
                Rlog.d(ImsEventPackageAdapter.LOG_TAG, "message error");
                return;
            }
            switch (msg.what) {
                case 0:
                    ImsEventPackageAdapter.this.handleLetMessageWaiting((String[]) ar.result);
                    return;
                case 1:
                    ImsEventPackageAdapter.this.handleDialogEventPackage((ArrayList) ar.result);
                    return;
                default:
                    Rlog.d(ImsEventPackageAdapter.LOG_TAG, "Unregistered event");
                    return;
            }
        }
    }
}
