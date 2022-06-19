package com.mediatek.ims.config;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import androidx.core.app.NotificationManagerCompat;

public class ImsConfigContract {
    public static final String ACTION_CONFIG_LOADED = "com.mediatek.ims.config.action.CONFIG_LOADED";
    public static final String ACTION_CONFIG_UPDATE = "com.mediatek.ims.config.action.CONFIG_UPDATE";
    public static final String ACTION_DYNAMIC_IMS_SWITCH_COMPLETE = "com.mediatek.ims.config.action.DYNAMIC_IMS_SWITCH_COMPLETE";
    public static final String ACTION_DYNAMIC_IMS_SWITCH_TRIGGER = "com.mediatek.ims.config.action.DYNAMIC_IMS_SWITCH_TRIGGER";
    public static final String ACTION_IMS_CONFIG_CHANGED = "com.android.intent.action.IMS_CONFIG_CHANGED";
    public static final String ACTION_IMS_FEATURE_CHANGED = "com.android.intent.action.IMS_FEATURE_CHANGED";
    public static final String AUTHORITY = "com.mediatek.ims.config.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://com.mediatek.ims.config.provider");
    public static final String EXTRA_CHANGED_ITEM = "item";
    public static final String EXTRA_CONFIG_ID = "config_id";
    public static final String EXTRA_MIMETYPE = "mimetype";
    public static final String EXTRA_NEW_VALUE = "value";
    public static final String EXTRA_PHONE_ID = "phone_id";
    public static final String TABLE_CONFIG_SETTING = "tb_config_setting";
    public static final String TABLE_DEFAULT = "tb_default";
    public static final String TABLE_FEATURE = "tb_feature";
    public static final String TABLE_MASTER = "tb_master";
    public static final String TABLE_PROVISION = "tb_provision";
    public static final String TABLE_RESOURCE = "tb_resource";
    public static final String VALUE_NO_DEFAULT = "n/a";
    private static String[] sConfigNames = {"VOCODER_AMRMODESET", "VOCODER_AMRWBMODESET", "SIP_SESSION_TIMER", "MIN_SE", "CANCELLATION_TIMER", "TDELAY", "SILENT_REDIAL_ENABLE", "SIP_T1_TIMER", "SIP_T2_TIMER", "SIP_TF_TIMER", "VLT_SETTING_ENABLED", "LVC_SETTING_ENABLED", "DOMAIN_NAME", "SMS_FORMAT", "SMS_OVER_IP", "PUBLISH_TIMER", "PUBLISH_TIMER_EXTENDED", "CAPABILITY_DISCOVERY_ENABLED", "CAPABILITIES_CACHE_EXPIRATION", "AVAILABILITY_CACHE_EXPIRATION", "CAPABILITIES_POLL_INTERVAL", "SOURCE_THROTTLE_PUBLISH", "MAX_NUMENTRIES_IN_RCL", "CAPAB_POLL_LIST_SUB_EXP", "GZIP_FLAG", "EAB_SETTING_ENABLED", "VOICE_OVER_WIFI_ROAMING", "VOICE_OVER_WIFI_MODE", "VOICE_OVER_WIFI_SETTING_ENABLED", "MOBILE_DATA_ENABLED", "VOLTE_USER_OPT_IN_STATUS", "LBO_PCSCF_ADDRESS", "KEEP_ALIVE_ENABLED", "REGISTRATION_RETRY_BASE_TIME_SEC", "REGISTRATION_RETRY_MAX_TIME_SEC", "SPEECH_START_PORT", "SPEECH_END_PORT", "SIP_INVITE_REQ_RETX_INTERVAL_MSEC", "SIP_INVITE_RSP_WAIT_TIME_MSEC", "SIP_INVITE_RSP_RETX_WAIT_TIME_MSEC", "SIP_NON_INVITE_REQ_RETX_INTERVAL_MSEC", "SIP_NON_INVITE_TXN_TIMEOUT_TIMER_MSEC", "SIP_INVITE_RSP_RETX_INTERVAL_MSEC", "SIP_ACK_RECEIPT_WAIT_TIME_MSEC", "SIP_ACK_RETX_WAIT_TIME_MSEC", "SIP_NON_INVITE_REQ_RETX_WAIT_TIME_MSEC", "SIP_NON_INVITE_RSP_RETX_WAIT_TIME_MSEC", "AMR_WB_OCTET_ALIGNED_PT", "AMR_WB_BANDWIDTH_EFFICIENT_PT", "AMR_OCTET_ALIGNED_PT", "AMR_BANDWIDTH_EFFICIENT_PT", "DTMF_WB_PT", "DTMF_NB_PT", "AMR_DEFAULT_MODE", "SMS_PSI", "VIDEO_QUALITY", "TH_LTE1", "TH_LTE2", "TH_LTE3", "TH_1x", "VOWT_A", "VOWT_B", "T_EPDG_LTE", "T_EPDG_WIFI", "T_EPDG_1X", "VICE_SETTING_ENABLED", "RTT_SETTING_ENABLED"};
    private static String[] sMtkConfigNames = {"EPDG_ADDRESS", "PUBLISH_ERROR_RETRY_TIMER", "VOICE_OVER_WIFI_MDN", "VOICE_DOMAIN_PREFERENCE"};

    public static abstract class BasicConfigTable implements BaseColumns {
        public static final String CONFIG_ID = "config_id";
        public static final String DATA = "data";
        public static final String MIMETYPE_ID = "mimetype_id";
        public static final String PHONE_ID = "phone_id";
    }

    public interface MimeType {
        public static final int FLOAT = 2;
        public static final int INTEGER = 0;
        public static final int JSON = 3;
        public static final int STRING = 1;
    }

    public static class Operator {
        public static final int OP_06 = 6;
        public static final int OP_08 = 8;
        public static final int OP_12 = 12;
        public static final int OP_DEFAULT = 0;
        public static final int OP_NONE = -1;
    }

    public interface Unit {
        public static final int DAYS = 6;
        public static final int HOURS = 5;
        public static final int MICROSECONDS = 1;
        public static final int MILLISECONDS = 2;
        public static final int MINUTES = 4;
        public static final int NANOSECONDS = 0;
        public static final int SECONDS = 3;
        public static final int UNIT_NONE = -1;
    }

    public static class ConfigSetting implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.mediatek.ims.config.provider/tb_config_setting");
        public static final String PHONE_ID = "phone_id";
        public static final String SETTING_ID = "setting_id";
        public static final int SETTING_ID_OPCODE = 0;
        public static final String VALUE = "value";

        public static Uri getUriWithSettingId(int phoneId, int settingId) {
            return ContentUris.withAppendedId(ContentUris.withAppendedId(CONTENT_URI, (long) phoneId), (long) settingId);
        }
    }

    public static class Feature implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.mediatek.ims.config.provider/tb_feature");
        public static final String FEATURE_ID = "feature_id";
        public static final String NETWORK_ID = "network_id";
        public static final String PHONE_ID = "phone_id";
        public static final String VALUE = "value";

        public static Uri getUriWithFeatureId(int phoneId, int featureId, int network) {
            return ContentUris.withAppendedId(ContentUris.withAppendedId(ContentUris.withAppendedId(CONTENT_URI, (long) phoneId), (long) featureId), (long) network);
        }
    }

    public static class Resource implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.mediatek.ims.config.provider/tb_resource");
        public static final String FEATURE_ID = "feature_id";
        public static final String PHONE_ID = "phone_id";
        public static final String VALUE = "value";

        public static Uri getUriWithFeatureId(int phoneId, int featureId) {
            return ContentUris.withAppendedId(ContentUris.withAppendedId(CONTENT_URI, (long) phoneId), (long) featureId);
        }
    }

    public static class Provision extends BasicConfigTable {
        public static final Uri CONTENT_URI = Uri.parse("content://com.mediatek.ims.config.provider/tb_provision");
        public static final String DATETIME = "datetime";

        public static Uri getUriWithConfigId(int phoneId, int configId) {
            return Uri.withAppendedPath(ContentUris.withAppendedId(CONTENT_URI, (long) phoneId), ImsConfigContract.configIdToName(configId));
        }
    }

    public static class Default extends BasicConfigTable {
        public static final Uri CONTENT_URI = Uri.parse("content://com.mediatek.ims.config.provider/tb_default");
        public static final String UNIT_ID = "unit_id";

        public static Uri getUriWithConfigId(int phoneId, int configId) {
            return Uri.withAppendedPath(ContentUris.withAppendedId(CONTENT_URI, (long) phoneId), ImsConfigContract.configIdToName(configId));
        }
    }

    public static class Master extends BasicConfigTable {
        public static final Uri CONTENT_URI = Uri.parse("content://com.mediatek.ims.config.provider/tb_master");

        public static Uri getUriWithConfigId(int phoneId, int configId) {
            return Uri.withAppendedPath(ContentUris.withAppendedId(CONTENT_URI, (long) phoneId), ImsConfigContract.configIdToName(configId));
        }
    }

    public static Uri getTableUri(String table) {
        if (Validator.isValidTable(table)) {
            char c = 65535;
            switch (table.hashCode()) {
                case -2133078972:
                    if (table.equals(TABLE_CONFIG_SETTING)) {
                        c = 0;
                        break;
                    }
                    break;
                case 45084740:
                    if (table.equals(TABLE_PROVISION)) {
                        c = 2;
                        break;
                    }
                    break;
                case 1412604243:
                    if (table.equals(TABLE_MASTER)) {
                        c = 3;
                        break;
                    }
                    break;
                case 1545420144:
                    if (table.equals(TABLE_DEFAULT)) {
                        c = 1;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    return ConfigSetting.CONTENT_URI;
                case 1:
                    return Default.CONTENT_URI;
                case 2:
                    return Provision.CONTENT_URI;
                case 3:
                    return Master.CONTENT_URI;
                default:
                    return null;
            }
        } else {
            throw new IllegalArgumentException("Invalid table: " + table);
        }
    }

    public static Uri getConfigUri(String table, int phoneId, int itemId) {
        if (Validator.isValidTable(table)) {
            char c = 65535;
            switch (table.hashCode()) {
                case -2133078972:
                    if (table.equals(TABLE_CONFIG_SETTING)) {
                        c = 0;
                        break;
                    }
                    break;
                case 45084740:
                    if (table.equals(TABLE_PROVISION)) {
                        c = 2;
                        break;
                    }
                    break;
                case 1412604243:
                    if (table.equals(TABLE_MASTER)) {
                        c = 3;
                        break;
                    }
                    break;
                case 1545420144:
                    if (table.equals(TABLE_DEFAULT)) {
                        c = 1;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    return ConfigSetting.getUriWithSettingId(phoneId, itemId);
                case 1:
                    return Default.getUriWithConfigId(phoneId, itemId);
                case 2:
                    return Provision.getUriWithConfigId(phoneId, itemId);
                case 3:
                    return Master.getUriWithConfigId(phoneId, itemId);
                default:
                    return null;
            }
        } else {
            throw new IllegalArgumentException("Invalid table: " + table);
        }
    }

    public static String configIdToName(int configId) {
        if (configId >= 1000 && configId <= 1004) {
            return sMtkConfigNames[configId + NotificationManagerCompat.IMPORTANCE_UNSPECIFIED];
        }
        String[] strArr = sConfigNames;
        if (configId < strArr.length) {
            return strArr[configId];
        }
        throw new IllegalArgumentException("Invalid config id: " + configId);
    }

    public static int configNameToId(String configName) {
        int i = 0;
        while (true) {
            String[] strArr = sConfigNames;
            if (i >= strArr.length) {
                int i2 = 0;
                while (true) {
                    String[] strArr2 = sMtkConfigNames;
                    if (i2 >= strArr2.length) {
                        throw new IllegalArgumentException("Unknown config: " + configName);
                    } else if (strArr2[i2].equals(configName)) {
                        return i2 + 1000;
                    } else {
                        i2++;
                    }
                }
            } else if (strArr[i].equals(configName)) {
                return i;
            } else {
                i++;
            }
        }
    }

    public static class Validator {
        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public static boolean isValidTable(java.lang.String r2) {
            /*
                r0 = 1
                int r1 = r2.hashCode()
                switch(r1) {
                    case -2133078972: goto L_0x003b;
                    case -978591195: goto L_0x0031;
                    case -321961281: goto L_0x0027;
                    case 45084740: goto L_0x001d;
                    case 1412604243: goto L_0x0013;
                    case 1545420144: goto L_0x0009;
                    default: goto L_0x0008;
                }
            L_0x0008:
                goto L_0x0045
            L_0x0009:
                java.lang.String r1 = "tb_default"
                boolean r1 = r2.equals(r1)
                if (r1 == 0) goto L_0x0008
                r1 = 1
                goto L_0x0046
            L_0x0013:
                java.lang.String r1 = "tb_master"
                boolean r1 = r2.equals(r1)
                if (r1 == 0) goto L_0x0008
                r1 = 4
                goto L_0x0046
            L_0x001d:
                java.lang.String r1 = "tb_provision"
                boolean r1 = r2.equals(r1)
                if (r1 == 0) goto L_0x0008
                r1 = 3
                goto L_0x0046
            L_0x0027:
                java.lang.String r1 = "tb_resource"
                boolean r1 = r2.equals(r1)
                if (r1 == 0) goto L_0x0008
                r1 = 5
                goto L_0x0046
            L_0x0031:
                java.lang.String r1 = "tb_feature"
                boolean r1 = r2.equals(r1)
                if (r1 == 0) goto L_0x0008
                r1 = 0
                goto L_0x0046
            L_0x003b:
                java.lang.String r1 = "tb_config_setting"
                boolean r1 = r2.equals(r1)
                if (r1 == 0) goto L_0x0008
                r1 = 2
                goto L_0x0046
            L_0x0045:
                r1 = -1
            L_0x0046:
                switch(r1) {
                    case 0: goto L_0x004b;
                    case 1: goto L_0x004b;
                    case 2: goto L_0x004b;
                    case 3: goto L_0x004b;
                    case 4: goto L_0x004b;
                    case 5: goto L_0x004b;
                    default: goto L_0x0049;
                }
            L_0x0049:
                r0 = 0
                goto L_0x004c
            L_0x004b:
            L_0x004c:
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.config.ImsConfigContract.Validator.isValidTable(java.lang.String):boolean");
        }

        public static boolean isValidSettingId(int settingId) {
            switch (settingId) {
                case 0:
                    return true;
                default:
                    return false;
            }
        }

        public static boolean isValidFeatureId(int featureId) {
            switch (featureId) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    return true;
                default:
                    return false;
            }
        }

        public static boolean isValidFeatureValue(int featureVal) {
            switch (featureVal) {
                case 0:
                case 1:
                    return true;
                default:
                    return false;
            }
        }

        public static boolean isValidNetwork(int network) {
            return network != 0;
        }

        public static boolean isValidConfigId(int configId) {
            if (configId < 0 || configId > 1004) {
                return false;
            }
            if (configId <= 67 || configId >= 1000) {
                return true;
            }
            return false;
        }

        public static boolean isValidMimeTypeId(int mimeTypeId) {
            switch (mimeTypeId) {
                case 0:
                case 1:
                case 2:
                case 3:
                    return true;
                default:
                    return false;
            }
        }

        public static boolean isValidUnitId(int unitId) {
            switch (unitId) {
                case -1:
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    return true;
                default:
                    return false;
            }
        }
    }

    public static int getNetworkTypeByFeature(int imsFeatureType) {
        switch (imsFeatureType) {
            case 0:
                return 13;
            case 1:
                return 13;
            case 2:
                return 18;
            default:
                return 0;
        }
    }
}
