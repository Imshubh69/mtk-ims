package com.mediatek.wfo.p005op;

import android.content.Context;

/* renamed from: com.mediatek.wfo.op.OpWosCustomizationFactoryBase */
public class OpWosCustomizationFactoryBase {
    public IWosExt makeWosExt(Context context) {
        return new DefaultWosExt(context);
    }
}
