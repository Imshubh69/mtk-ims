package com.mediatek.ims.plugin;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import com.mediatek.ims.ImsConstants;
import com.mediatek.ims.plugin.impl.ExtensionPluginFactoryBase;
import com.mediatek.ims.plugin.impl.LegacyComponentFactoryBase;
import com.mediatek.ims.plugin.impl.OemPluginFactoryBase;
import dalvik.system.PathClassLoader;

public class ExtensionFactory {
    private static final String EXTENSION_PLUG_IN_CLASS_PATH = "/system/framework/mediatek-ims-extension-plugin.jar";
    private static final String EXTENSION_PLUG_IN_NAME = "com.mediatek.imsplugin.ExtensionPluginFactoryImpl";
    private static final String LEGACY_COMPONENT_CLASS_PATH = "/system/framework/mediatek-ims-legacy.jar";
    private static final String LEGACY_COMPONENT_NAME = "com.mediatek.ims.legacy.LegacyComponentFactoryImpl";
    public static final String LOG_TAG = "ImsExtensionFactory";
    private static final String OEM_PLUG_IN_CLASS_PATH = "/system/framework/mediatek-ims-oem-plugin.jar";
    private static final String OEM_PLUG_IN_NAME = "com.mediatek.imsplugin.OemPluginFactoryImpl";
    private static ExtensionPluginFactory sExtensionPluginFactory;
    private static LegacyComponentFactory sLegacyComponentFactory;
    private static OemPluginFactory sOemPluginFactory;

    public static OemPluginFactory makeOemPluginFactory(Context mContext) {
        if (sOemPluginFactory == null) {
            try {
                PathClassLoader pathClassLoader = new PathClassLoader(OEM_PLUG_IN_CLASS_PATH, mContext.getClassLoader());
                Log.d(LOG_TAG, "pathClassLoader = " + pathClassLoader);
                sOemPluginFactory = (OemPluginFactory) Class.forName(OEM_PLUG_IN_NAME, false, pathClassLoader).getConstructor(new Class[0]).newInstance(new Object[0]);
                Log.d(LOG_TAG, "Use customer's OemPluginFactory");
            } catch (Exception e) {
                Log.d(LOG_TAG, "Use default OemPluginFactory");
            }
            if (sOemPluginFactory == null) {
                sOemPluginFactory = new OemPluginFactoryBase();
            }
        }
        return sOemPluginFactory;
    }

    public static ExtensionPluginFactory makeExtensionPluginFactory(Context mContext) {
        if (sExtensionPluginFactory == null) {
            if (SystemProperties.get("ro.vendor.mtk_telephony_add_on_policy", "0").equals("0")) {
                try {
                    sExtensionPluginFactory = (ExtensionPluginFactory) Class.forName(EXTENSION_PLUG_IN_NAME, false, new PathClassLoader(EXTENSION_PLUG_IN_CLASS_PATH, mContext.getClassLoader())).getConstructor(new Class[0]).newInstance(new Object[0]);
                    Log.d(LOG_TAG, "Use MTK's ExtensionPluginFactory");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (sExtensionPluginFactory == null) {
                Log.d(LOG_TAG, "Use default ExtensionPluginFactory");
                sExtensionPluginFactory = new ExtensionPluginFactoryBase();
            }
        }
        return sExtensionPluginFactory;
    }

    public static LegacyComponentFactory makeLegacyComponentFactory(Context mContext) {
        if (sLegacyComponentFactory == null) {
            if (SystemProperties.get(ImsConstants.SYS_PROP_MD_AUTO_SETUP_IMS).equals("1")) {
                Log.d(LOG_TAG, "Gen93 detected !");
            } else if (SystemProperties.get("ro.vendor.mtk_telephony_add_on_policy", "0").equals("0")) {
                try {
                    sLegacyComponentFactory = (LegacyComponentFactory) Class.forName(LEGACY_COMPONENT_NAME, false, new PathClassLoader(LEGACY_COMPONENT_CLASS_PATH, mContext.getClassLoader())).getConstructor(new Class[0]).newInstance(new Object[0]);
                    Log.d(LOG_TAG, "Use Legacy's LegacyComponentFactory");
                } catch (Exception e) {
                    Log.d(LOG_TAG, "Cannot load legacy factory");
                }
            }
            if (sLegacyComponentFactory == null) {
                Log.d(LOG_TAG, "Use default LegacyComponentFactory");
                sLegacyComponentFactory = new LegacyComponentFactoryBase();
            }
        }
        return sLegacyComponentFactory;
    }
}
