package com.mediatek.ims.config.p004op;

import com.mediatek.ims.config.ImsConfigPolicy;

/* renamed from: com.mediatek.ims.config.op.DefaultConfigPolicy */
public class DefaultConfigPolicy extends ImsConfigPolicy {
    public DefaultConfigPolicy() {
        super("DefaultConfigPolicy");
    }

    public boolean onSetDefaultValue(int configId, ImsConfigPolicy.DefaultConfig config) {
        return false;
    }
}
