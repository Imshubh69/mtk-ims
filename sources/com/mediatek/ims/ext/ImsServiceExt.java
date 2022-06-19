package com.mediatek.ims.ext;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ImsServiceExt implements IImsServiceExt {
    private static final String TAG = "ImsServiceExt";

    public ImsServiceExt(Context context) {
    }

    public void notifyImsServiceEvent(int phoneId, Context context, Message msg) {
    }

    public void notifyRegistrationStateChange(int ran, Handler handler, Object imsRILAdapter) {
    }

    public boolean isWfcRegErrorCauseSupported() {
        Log.d(TAG, "isWfcRegErrorCauseSupported return false");
        return false;
    }
}
