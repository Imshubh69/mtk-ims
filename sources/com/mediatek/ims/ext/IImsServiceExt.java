package com.mediatek.ims.ext;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public interface IImsServiceExt {
    boolean isWfcRegErrorCauseSupported();

    void notifyImsServiceEvent(int i, Context context, Message message);

    void notifyRegistrationStateChange(int i, Handler handler, Object obj);
}
