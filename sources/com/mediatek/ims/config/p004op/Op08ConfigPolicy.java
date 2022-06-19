package com.mediatek.ims.config.p004op;

import com.mediatek.ims.config.ImsConfigPolicy;

/* renamed from: com.mediatek.ims.config.op.Op08ConfigPolicy */
public class Op08ConfigPolicy extends ImsConfigPolicy {
    public Op08ConfigPolicy() {
        super("Op08ConfigPolicy");
    }

    public boolean onSetDefaultValue(int configId, ImsConfigPolicy.DefaultConfig config) {
        switch (configId) {
            case 10:
                config.defVal = "0";
                return true;
            default:
                return false;
        }
    }
}
