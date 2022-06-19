package com.mediatek.ims.config;

import com.mediatek.ims.config.ImsConfigContract;
import java.util.HashMap;

public abstract class ImsConfigPolicy {
    private HashMap<Integer, DefaultConfig> mDefConfigs = new HashMap<>();
    private String mTag = "ImsConfigPolicy";

    public abstract boolean onSetDefaultValue(int i, DefaultConfig defaultConfig);

    public ImsConfigPolicy(String tag) {
        this.mTag = tag;
    }

    /* access modifiers changed from: package-private */
    public HashMap<Integer, DefaultConfig> fetchDefaultValues() {
        for (Integer configId : ImsConfigSettings.getConfigSettings().keySet()) {
            DefaultConfig config = new DefaultConfig(configId.intValue());
            if (onSetDefaultValue(configId.intValue(), config)) {
                if (ImsConfigContract.Validator.isValidUnitId(config.unitId)) {
                    this.mDefConfigs.put(configId, config);
                } else {
                    throw new IllegalArgumentException("Invalid unitId " + config.unitId + " on config " + configId);
                }
            }
        }
        return this.mDefConfigs;
    }

    public static class DefaultConfig {
        int configId = 0;
        public String defVal = null;
        public int unitId = -1;

        private DefaultConfig() {
        }

        public DefaultConfig(int _configId) {
            this.configId = _configId;
        }
    }
}
