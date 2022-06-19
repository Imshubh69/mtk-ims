package com.mediatek.ims.ext;

import android.content.Context;
import com.mediatek.common.util.OperatorCustomizationFactoryLoader;
import java.util.ArrayList;
import java.util.List;

public class OpImsServiceCustomizationUtils {
    static OpImsServiceCustomizationFactoryBase sFactory = null;
    private static final List<OperatorCustomizationFactoryLoader.OperatorFactoryInfo> sOperatorFactoryInfoList;

    static {
        ArrayList arrayList = new ArrayList();
        sOperatorFactoryInfoList = arrayList;
        arrayList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP08Ims.apk", "com.mediatek.op08.ims.Op08ImsServiceCustomizationFactory", "com.mediatek.op08.ims", "OP08"));
        arrayList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP18Ims.jar", "com.mediatek.op18.ims.Op18ImsServiceCustomizationFactory", (String) null, "OP18"));
        arrayList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP12Ims.apk", "com.mediatek.op12.ims.Op12ImsServiceCustomizationFactory", "com.mediatek.op12.ims", "OP12"));
        arrayList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP112Ims.apk", "com.mediatek.op112.ims.Op112ImsServiceCustomizationFactory", "com.mediatek.op112.ims", "OP112"));
    }

    public static synchronized OpImsServiceCustomizationFactoryBase getOpFactory(Context context) {
        OpImsServiceCustomizationFactoryBase opImsServiceCustomizationFactoryBase;
        synchronized (OpImsServiceCustomizationUtils.class) {
            OpImsServiceCustomizationFactoryBase opImsServiceCustomizationFactoryBase2 = (OpImsServiceCustomizationFactoryBase) OperatorCustomizationFactoryLoader.loadFactory(context, sOperatorFactoryInfoList);
            sFactory = opImsServiceCustomizationFactoryBase2;
            if (opImsServiceCustomizationFactoryBase2 == null) {
                sFactory = new OpImsServiceCustomizationFactoryBase();
            }
            opImsServiceCustomizationFactoryBase = sFactory;
        }
        return opImsServiceCustomizationFactoryBase;
    }
}
