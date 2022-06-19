package com.mediatek.ims.config;

import android.os.SystemProperties;
import android.util.Log;
import com.mediatek.ims.config.ImsConfigPolicy;
import com.mediatek.ims.config.p004op.DefaultConfigPolicy;
import com.mediatek.ims.config.p004op.Op06ConfigPolicy;
import com.mediatek.ims.config.p004op.Op08ConfigPolicy;
import com.mediatek.ims.config.p004op.Op12ConfigPolicy;
import java.util.HashMap;

public class DefaultConfigPolicyFactory {
    private static final String TAG = "DefaultCfgPolicyFactory";
    private ImsConfigPolicy mConfigPolicy = null;
    private HashMap<Integer, ImsConfigPolicy.DefaultConfig> mDefConfigs = new HashMap<>();

    private DefaultConfigPolicyFactory() {
    }

    public static DefaultConfigPolicyFactory getInstance(int phoneId) {
        String optr = SystemProperties.get("persist.vendor.operator.optr");
        int opCode = 0;
        if (optr != null && optr.length() > 2) {
            opCode = Integer.parseInt(optr.substring(2));
        }
        return getInstanceByOpCode(opCode);
    }

    public static DefaultConfigPolicyFactory getInstanceByOpCode(int opCode) {
        return new DefaultConfigPolicyFactory(opCode);
    }

    private DefaultConfigPolicyFactory(int op) {
        Log.d(TAG, "Load defalut policy operator: " + op);
        switch (op) {
            case 6:
                this.mConfigPolicy = new Op06ConfigPolicy();
                return;
            case 8:
                this.mConfigPolicy = new Op08ConfigPolicy();
                return;
            case 12:
                this.mConfigPolicy = new Op12ConfigPolicy();
                return;
            default:
                this.mConfigPolicy = new DefaultConfigPolicy();
                return;
        }
    }

    public HashMap load() {
        ImsConfigPolicy imsConfigPolicy = this.mConfigPolicy;
        if (imsConfigPolicy != null) {
            this.mDefConfigs = imsConfigPolicy.fetchDefaultValues();
        }
        return this.mDefConfigs;
    }

    public void clear() {
        this.mDefConfigs = null;
    }

    public boolean hasDefaultValue(int configId) {
        return this.mDefConfigs.containsKey(Integer.valueOf(configId));
    }
}
