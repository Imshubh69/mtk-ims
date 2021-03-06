package com.mediatek.ims.config.internal;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.ImsManager;
import com.mediatek.ims.ImsCommonUtil;
import com.mediatek.ims.ImsConstants;
import com.mediatek.ims.common.SubscriptionManagerHelper;
import com.mediatek.ims.ril.ImsCommandsInterface;

public class ImsConfigUtils {
    private static final String ACTION_WIFI_ONLY_MODE_CHANGED = "android.intent.action.ACTION_WIFI_ONLY_MODE";
    private static final boolean DEBUG = (TextUtils.equals(Build.TYPE, "eng") || SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1);
    private static final String EXTRA_WIFI_ONLY_MODE_CHANGED = "state";
    public static final String PROPERTY_IMS_SUPPORT = "persist.vendor.ims_support";
    public static final String PROPERTY_IMS_VIDEO_ENALBE = "persist.vendor.mtk.ims.video.enable";
    public static final String PROPERTY_VILTE_ENALBE = "persist.vendor.mtk.vilte.enable";
    public static final String PROPERTY_VIWIFI_ENALBE = "persist.vendor.mtk.viwifi.enable";
    public static final String PROPERTY_VOLTE_ENALBE = "persist.vendor.mtk.volte.enable";
    public static final String PROPERTY_WFC_ENALBE = "persist.vendor.mtk.wfc.enable";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final String TAG = "ImsConfigUtils";
    private static final boolean TELDBG;

    public static class MdConfigType {
        public static final int TYPE_IMSCFG = 0;
        public static final int TYPE_IMSIWLANCFG = 2;
        public static final int TYPE_IMSWOCFG = 1;
        public static final int TYPE_UNKNOWN = -1;
    }

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        TELDBG = z;
    }

    public static void sendWifiOnlyModeIntent(Context context, int phoneId, boolean mode) {
        if (ImsCommonUtil.supportMims() || ImsCommonUtil.getMainCapabilityPhoneId() == phoneId) {
            Intent intent = new Intent(ACTION_WIFI_ONLY_MODE_CHANGED);
            intent.setPackage(ImsConstants.PACKAGE_NAME_PHONE);
            intent.putExtra(EXTRA_WIFI_ONLY_MODE_CHANGED, mode);
            if (ImsCommonUtil.supportMims()) {
                intent.putExtra("phone", phoneId);
            }
            if (TELDBG) {
                Log.d(TAG, "sendWifiOnlyModeIntent() intent, mode:" + mode + ", phoneId:" + phoneId);
            }
            context.sendBroadcast(intent);
        }
    }

    public static int getFeaturePropValue(String propName, int phoneId) {
        int propResult;
        int featureValue = SystemProperties.getInt(propName, 0);
        if (!checkIsPhoneIdValid(phoneId)) {
            if (DEBUG) {
                Log.d(TAG, "Multi IMS getFeaturePropValue():" + propName + ", phoneId invalid return default value");
            }
            return 0;
        }
        int i = 0;
        if (ImsCommonUtil.supportMims()) {
            if (((1 << phoneId) & featureValue) > 0) {
                i = 1;
            }
            propResult = i;
        } else {
            if ((featureValue & 1) > 0) {
                i = 1;
            }
            propResult = i;
        }
        if (DEBUG) {
            Log.d(TAG, "Multi IMS getFeaturePropValue() featureValue:" + featureValue + ", propName:" + propName + ", propResult:" + propResult);
        }
        return propResult;
    }

    public static void setFeaturePropValue(String propName, String enabled, int phoneId) {
        int sumFeatureValue;
        int featureValue = SystemProperties.getInt(propName, 0);
        if (checkIsPhoneIdValid(phoneId)) {
            int enabledValue = Integer.parseInt(enabled);
            if (ImsCommonUtil.supportMims()) {
                sumFeatureValue = setBitForPhone(featureValue, enabledValue, phoneId);
            } else {
                sumFeatureValue = setBitForPhone(featureValue, enabledValue, 0);
            }
            SystemProperties.set(propName, Integer.toString(sumFeatureValue));
            if (DEBUG) {
                Log.d(TAG, "Multi IMS setFeaturePropValue() featureValue:" + featureValue + ", propName:" + propName + ", sumFeatureValue:" + sumFeatureValue + ", enabledValue:" + enabledValue);
            }
        } else if (DEBUG) {
            Log.d(TAG, "Multi IMS setFeaturePropValue():" + propName + ", phoneId invalid don't set and return");
        }
    }

    private static int setBitForPhone(int featureValue, int enabled, int phoneId) {
        if (enabled == 1) {
            return (1 << phoneId) | featureValue;
        }
        return (~(1 << phoneId)) & featureValue;
    }

    public static void triggerSendCfg(Context context, ImsCommandsInterface imsRilAdapter, int phoneId) {
        int[] params = new int[6];
        int isAllowTurnOff = getBooleanCarrierConfig(context, "carrier_allow_turnoff_ims_bool", phoneId) ? 1 : 0;
        params[0] = getFeaturePropValue(PROPERTY_VOLTE_ENALBE, phoneId);
        params[1] = getFeaturePropValue(PROPERTY_VILTE_ENALBE, phoneId);
        params[2] = getFeaturePropValue(PROPERTY_WFC_ENALBE, phoneId);
        params[3] = getFeaturePropValue(PROPERTY_VIWIFI_ENALBE, phoneId);
        params[4] = SystemProperties.getInt(PROPERTY_IMS_SUPPORT, 0);
        params[5] = params[0] | params[1] | params[2] | params[3] | params[4] | (isAllowTurnOff ^ 1);
        Log.i(TAG, "After 93, send EIMS feature value volte:" + params[0] + ", vilte:" + params[1] + ", vowifi:" + params[2] + ", viwifi:" + params[3] + ", sms:" + params[4] + ", isAllowTurnOff:" + isAllowTurnOff + ", eims:" + params[5] + ", phoneId:" + phoneId);
        imsRilAdapter.setImsCfg(params, phoneId, (Message) null);
    }

    public static void triggerSendCfgForVolte(Context context, ImsCommandsInterface imsRilAdapter, int phoneId, int volteOn) {
        int[] params = new int[6];
        int isAllowTurnOff = getBooleanCarrierConfig(context, "carrier_allow_turnoff_ims_bool", phoneId) ? 1 : 0;
        params[0] = volteOn;
        params[1] = getFeaturePropValue(PROPERTY_VILTE_ENALBE, phoneId);
        params[2] = getFeaturePropValue(PROPERTY_WFC_ENALBE, phoneId);
        params[3] = getFeaturePropValue(PROPERTY_VIWIFI_ENALBE, phoneId);
        params[4] = SystemProperties.getInt(PROPERTY_IMS_SUPPORT, 0);
        params[5] = params[0] | params[1] | params[2] | params[3] | params[4] | (isAllowTurnOff ^ 1);
        Log.i(TAG, "After 93, send EIMS feature value volte:" + params[0] + ", vilte:" + params[1] + ", vowifi:" + params[2] + ", viwifi:" + params[3] + ", sms:" + params[4] + ", isAllowTurnOff:" + isAllowTurnOff + ", eims:" + params[5] + ", phoneId:" + phoneId);
        imsRilAdapter.setImsCfg(params, phoneId, (Message) null);
    }

    private static boolean checkIsPhoneIdValid(int phoneId) {
        if (ImsCommonUtil.supportMims()) {
            if (phoneId <= 3 && phoneId >= 0) {
                return true;
            }
            if (DEBUG) {
                Log.d(TAG, "Multi IMS support but phone id invalid, phoneId:" + phoneId);
            }
            return false;
        } else if (phoneId <= 3 && phoneId >= 0) {
            return true;
        } else {
            if (DEBUG) {
                Log.d(TAG, "Mutli IMS not support and phone id invalid, phoneId:" + phoneId);
            }
            return false;
        }
    }

    public static boolean getBooleanCarrierConfig(Context context, String key, int phoneId) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        int subId = SubscriptionManagerHelper.getSubIdUsingPhoneId(phoneId);
        if (TELDBG) {
            Log.d(TAG, "getBooleanCarrierConfig: phoneId=" + phoneId + " subId=" + subId);
        }
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        if (b != null) {
            return b.getBoolean(key);
        }
        return CarrierConfigManager.getDefaultConfig().getBoolean(key);
    }

    public static int getIntCarrierConfig(Context context, String key, int phoneId) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        int subId = SubscriptionManagerHelper.getSubIdUsingPhoneId(phoneId);
        if (DEBUG) {
            Log.d(TAG, "getIntCarrierConfig: phoneId=" + phoneId + " subId=" + subId);
        }
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        if (b != null) {
            return b.getInt(key);
        }
        return CarrierConfigManager.getDefaultConfig().getInt(key);
    }

    public static boolean isWfcEnabledByUser(Context context, int phoneId) {
        if (ImsCommonUtil.supportMims()) {
            return ImsManager.getInstance(context, phoneId).isWfcEnabledByUser();
        }
        return ImsManager.isWfcEnabledByUser(context);
    }

    public static int getWfcMode(Context context, int phoneId) {
        if (ImsCommonUtil.supportMims()) {
            return ImsManager.getInstance(context, phoneId).getWfcMode();
        }
        return ImsManager.getWfcMode(context);
    }

    public static String arrayToString(Object[] array) {
        int maxLength;
        if (array == null || array.length - 1 == -1) {
            return "null";
        }
        StringBuilder b = new StringBuilder();
        b.append('\"');
        int i = 0;
        while (true) {
            b.append(String.valueOf(array[i]));
            if (i == maxLength) {
                b.append('\"');
                return b.toString();
            }
            b.append("\",\"");
            i++;
        }
    }
}
