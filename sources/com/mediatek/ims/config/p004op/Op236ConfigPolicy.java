package com.mediatek.ims.config.p004op;

import com.mediatek.ims.config.ImsConfigPolicy;

/* renamed from: com.mediatek.ims.config.op.Op236ConfigPolicy */
public class Op236ConfigPolicy extends ImsConfigPolicy {
    public Op236ConfigPolicy() {
        super("Op236ConfigPolicy");
    }

    public boolean onSetDefaultValue(int configId, ImsConfigPolicy.DefaultConfig config) {
        switch (configId) {
            case 1003:
                config.defVal = "1";
                return true;
            default:
                return false;
        }
    }
}
