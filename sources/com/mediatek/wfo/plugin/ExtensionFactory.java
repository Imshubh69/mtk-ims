package com.mediatek.wfo.plugin;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import com.mediatek.ims.ImsConstants;
import com.mediatek.wfo.plugin.impl.LegacyComponentFactoryBase;
import dalvik.system.PathClassLoader;

public class ExtensionFactory {
    private static final String LEGACY_COMPONENT_CLASS_PATH = "/system/framework/mediatek-wfo-legacy.jar";
    private static final String LEGACY_COMPONENT_NAME = "com.mediatek.wfo.legacy.LegacyComponentFactoryImpl";
    public static final String LOG_TAG = "WfoExtensionFactory";
    private static LegacyComponentFactory sLegacyComponentFactory;

    public static LegacyComponentFactory makeLegacyComponentFactory(Context context) {
        if (sLegacyComponentFactory == null) {
            if (SystemProperties.get(ImsConstants.SYS_PROP_MD_AUTO_SETUP_IMS).equals("1")) {
                Log.d(LOG_TAG, "Gen93 detected !");
            } else if (SystemProperties.get("ro.vendor.mtk_telephony_add_on_policy", "0").equals("0")) {
                try {
                    sLegacyComponentFactory = (LegacyComponentFactory) Class.forName(LEGACY_COMPONENT_NAME, false, new PathClassLoader(LEGACY_COMPONENT_CLASS_PATH, context.getClassLoader())).getConstructor(new Class[0]).newInstance(new Object[0]);
                    Log.d(LOG_TAG, "Use Legacy's LegacyComponentFactory");
                } catch (Exception e) {
                    Log.d(LOG_TAG, "Cannot load legacy factory");
                }
            }
            if (sLegacyComponentFactory == null) {
                sLegacyComponentFactory = new LegacyComponentFactoryBase();
            }
        }
        return sLegacyComponentFactory;
    }
}
