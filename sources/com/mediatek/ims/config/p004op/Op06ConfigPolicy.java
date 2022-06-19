package com.mediatek.ims.config.p004op;

import com.mediatek.ims.config.ImsConfigPolicy;

/* renamed from: com.mediatek.ims.config.op.Op06ConfigPolicy */
public class Op06ConfigPolicy extends ImsConfigPolicy {
    public Op06ConfigPolicy() {
        super("Op06ConfigPolicy");
    }

    public boolean onSetDefaultValue(int configId, ImsConfigPolicy.DefaultConfig config) {
        switch (configId) {
            case 28:
                config.defVal = "0";
                return true;
            case 1000:
                config.defVal = "";
                return true;
            default:
                return false;
        }
    }
}
