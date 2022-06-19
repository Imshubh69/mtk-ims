package com.mediatek.wfo.p005op;

import android.content.Context;
import android.util.Log;

/* renamed from: com.mediatek.wfo.op.DefaultWosExt */
public class DefaultWosExt implements IWosExt {
    static final String TAG = "DefaultWosExt";

    public DefaultWosExt(Context context) {
        Log.d(TAG, "DefaultWosExt constructor");
    }

    public void showPDNErrorMessages(int errorCode) {
        Log.d(TAG, "showPDNErrorMessages is empty");
    }

    public void clearPDNErrorMessages() {
        Log.d(TAG, "clearPDNErrorMessages is empty");
    }

    public void factoryReset() {
        Log.d(TAG, "factoryReset is empty");
    }

    public void showLocationTimeoutMessage() {
        Log.d(TAG, "showLocationTimeoutMessage is empty");
    }

    public void initialize(Context context) {
        Log.d(TAG, "initialize is empty");
    }

    public void dispose() {
        Log.d(TAG, "dispose is empty");
    }

    public void setWfcRegErrorCode(int mainErr, int simIdx) {
        Log.d(TAG, "setWfcRegErrorCode is empty");
    }
}
