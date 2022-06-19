package com.mediatek.wfo.p005op;

import android.content.Context;
import android.util.Log;
import com.mediatek.common.util.OperatorCustomizationFactoryLoader;
import java.util.ArrayList;
import java.util.List;

/* renamed from: com.mediatek.wfo.op.OpWosCustomizationUtils */
public class OpWosCustomizationUtils {
    private static final String TAG = "OpWosCustomizationUtils";
    static OpWosCustomizationFactoryBase sFactory = null;
    private static final List<OperatorCustomizationFactoryLoader.OperatorFactoryInfo> sOperatorFactoryInfoList;

    static {
        ArrayList arrayList = new ArrayList();
        sOperatorFactoryInfoList = arrayList;
        arrayList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP07Wos.apk", "com.mediatek.op07.wfo.Op07WosCustomizationFactory", "com.mediatek.op07.wfo", "OP07"));
        arrayList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP08Wos.apk", "com.mediatek.op08.wfo.Op08WosCustomizationFactory", "com.mediatek.op08.wfo", "OP08"));
    }

    public static synchronized OpWosCustomizationFactoryBase getOpFactory(Context context) {
        OpWosCustomizationFactoryBase opWosCustomizationFactoryBase;
        synchronized (OpWosCustomizationUtils.class) {
            try {
                sFactory = (OpWosCustomizationFactoryBase) OperatorCustomizationFactoryLoader.loadFactory(context, sOperatorFactoryInfoList);
            } catch (ClassCastException e) {
                Log.e(TAG, "OpWosCustomizationFactoryBase ClassCastException", e);
            }
            if (sFactory == null) {
                sFactory = new OpWosCustomizationFactoryBase();
            }
            opWosCustomizationFactoryBase = sFactory;
        }
        return opWosCustomizationFactoryBase;
    }
}
