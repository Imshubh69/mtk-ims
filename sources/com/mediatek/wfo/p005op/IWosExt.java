package com.mediatek.wfo.p005op;

import android.content.Context;

/* renamed from: com.mediatek.wfo.op.IWosExt */
public interface IWosExt {
    void clearPDNErrorMessages();

    void dispose();

    void factoryReset();

    void initialize(Context context);

    void setWfcRegErrorCode(int i, int i2);

    void showLocationTimeoutMessage();

    void showPDNErrorMessages(int i);
}
