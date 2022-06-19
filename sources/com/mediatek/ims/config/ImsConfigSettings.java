package com.mediatek.ims.config;

import java.util.HashMap;
import java.util.Map;

public class ImsConfigSettings {
    private static HashMap<Integer, Setting> sImsConfigurations = new HashMap<>();

    static {
        buildConfigSettings();
    }

    private static void buildConfigSettings() {
        sImsConfigurations.put(0, new Setting(String.class, 1));
        sImsConfigurations.put(1, new Setting(String.class, 1));
        sImsConfigurations.put(2, new Setting(Integer.class, 0));
        sImsConfigurations.put(3, new Setting(Integer.class, 0));
        sImsConfigurations.put(4, new Setting(Integer.class, 0));
        sImsConfigurations.put(5, new Setting(Integer.class, 0));
        sImsConfigurations.put(6, new Setting(Integer.class, 0));
        sImsConfigurations.put(7, new Setting(Integer.class, 0));
        sImsConfigurations.put(8, new Setting(Integer.class, 0));
        sImsConfigurations.put(9, new Setting(Integer.class, 0));
        sImsConfigurations.put(10, new Setting(Integer.class, 0));
        sImsConfigurations.put(11, new Setting(Integer.class, 0, "video_over_ps_support"));
        sImsConfigurations.put(12, new Setting(String.class, 1));
        sImsConfigurations.put(13, new Setting(Integer.class, 0));
        sImsConfigurations.put(14, new Setting(Integer.class, 0));
        sImsConfigurations.put(15, new Setting(Integer.class, 0));
        sImsConfigurations.put(17, new Setting(Integer.class, 0));
        sImsConfigurations.put(16, new Setting(Integer.class, 0));
        sImsConfigurations.put(18, new Setting(Integer.class, 0));
        sImsConfigurations.put(19, new Setting(Integer.class, 0));
        sImsConfigurations.put(20, new Setting(Integer.class, 0));
        sImsConfigurations.put(21, new Setting(Integer.class, 0));
        sImsConfigurations.put(22, new Setting(Integer.class, 0));
        sImsConfigurations.put(23, new Setting(Integer.class, 0));
        sImsConfigurations.put(24, new Setting(Integer.class, 0));
        sImsConfigurations.put(25, new Setting(Integer.class, 0));
        sImsConfigurations.put(26, new Setting(Integer.class, 0));
        sImsConfigurations.put(27, new Setting(Integer.class, 0));
        sImsConfigurations.put(29, new Setting(Integer.class, 0));
        sImsConfigurations.put(30, new Setting(Integer.class, 0));
        sImsConfigurations.put(31, new Setting(Integer.class, 0));
        sImsConfigurations.put(32, new Setting(Integer.class, 0));
        sImsConfigurations.put(33, new Setting(Integer.class, 0));
        sImsConfigurations.put(34, new Setting(Integer.class, 0));
        sImsConfigurations.put(35, new Setting(Integer.class, 0));
        sImsConfigurations.put(36, new Setting(Integer.class, 0));
        sImsConfigurations.put(37, new Setting(Integer.class, 0));
        sImsConfigurations.put(38, new Setting(Integer.class, 0));
        sImsConfigurations.put(39, new Setting(Integer.class, 0));
        sImsConfigurations.put(40, new Setting(Integer.class, 0));
        sImsConfigurations.put(41, new Setting(Integer.class, 0));
        sImsConfigurations.put(42, new Setting(Integer.class, 0));
        sImsConfigurations.put(43, new Setting(Integer.class, 0));
        sImsConfigurations.put(44, new Setting(Integer.class, 0));
        sImsConfigurations.put(45, new Setting(Integer.class, 0));
        sImsConfigurations.put(46, new Setting(Integer.class, 0));
        sImsConfigurations.put(47, new Setting(Integer.class, 0));
        sImsConfigurations.put(48, new Setting(Integer.class, 0));
        sImsConfigurations.put(49, new Setting(Integer.class, 0));
        sImsConfigurations.put(50, new Setting(Integer.class, 0));
        sImsConfigurations.put(51, new Setting(Integer.class, 0));
        sImsConfigurations.put(52, new Setting(Integer.class, 0));
        sImsConfigurations.put(53, new Setting(Integer.class, 0));
        sImsConfigurations.put(54, new Setting(Integer.class, 0));
        sImsConfigurations.put(55, new Setting(Integer.class, 0));
        sImsConfigurations.put(28, new Setting(Integer.class, 0));
        sImsConfigurations.put(56, new Setting(Integer.class, 0));
        sImsConfigurations.put(57, new Setting(Integer.class, 0));
        sImsConfigurations.put(58, new Setting(Integer.class, 0));
        sImsConfigurations.put(59, new Setting(Integer.class, 0));
        sImsConfigurations.put(60, new Setting(Integer.class, 0));
        sImsConfigurations.put(61, new Setting(Integer.class, 0));
        sImsConfigurations.put(62, new Setting(Integer.class, 0));
        sImsConfigurations.put(63, new Setting(Integer.class, 0));
        sImsConfigurations.put(64, new Setting(Integer.class, 0));
        sImsConfigurations.put(65, new Setting(Integer.class, 0));
        sImsConfigurations.put(66, new Setting(Integer.class, 0));
        sImsConfigurations.put(1000, new Setting(String.class, 1, "epdg_identifier"));
        sImsConfigurations.put(1001, new Setting(String.class, 0));
        sImsConfigurations.put(1002, new Setting(String.class, 1));
        sImsConfigurations.put(1003, new Setting(Integer.class, 0));
    }

    public static class Setting {
        public Class clazz;
        public boolean mIsStoreModem;
        public String mProvisionStr;
        public int mimeType;

        public Setting(Class _clazz, int _mimeType) {
            this.clazz = _clazz;
            this.mimeType = _mimeType;
            this.mIsStoreModem = false;
            this.mProvisionStr = "";
        }

        public Setting(Class _clazz, int _mimeType, String _provisionStr) {
            this.clazz = _clazz;
            this.mimeType = _mimeType;
            if (_provisionStr != "" && _provisionStr != null) {
                this.mIsStoreModem = true;
                this.mProvisionStr = _provisionStr;
            }
        }
    }

    public static Map<Integer, Setting> getConfigSettings() {
        return sImsConfigurations;
    }

    public static int getMimeType(int configId) {
        return sImsConfigurations.get(Integer.valueOf(configId)).mimeType;
    }

    public static boolean getIsStoreModem(int configId) {
        return sImsConfigurations.get(Integer.valueOf(configId)).mIsStoreModem;
    }

    public static String getProvisionStr(int configId) {
        return sImsConfigurations.get(Integer.valueOf(configId)).mProvisionStr;
    }
}
