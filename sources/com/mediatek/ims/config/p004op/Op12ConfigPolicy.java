package com.mediatek.ims.config.p004op;

import com.mediatek.ims.config.ImsConfigPolicy;

/* renamed from: com.mediatek.ims.config.op.Op12ConfigPolicy */
public class Op12ConfigPolicy extends ImsConfigPolicy {
    public Op12ConfigPolicy() {
        super("Op12ConfigPolicy");
    }

    public boolean onSetDefaultValue(int configId, ImsConfigPolicy.DefaultConfig config) {
        switch (configId) {
            case 0:
                config.defVal = "7";
                return true;
            case 1:
                config.defVal = "2";
                return true;
            case 2:
                config.defVal = "300";
                config.unitId = 3;
                return true;
            case 3:
                config.defVal = "300";
                config.unitId = 3;
                return true;
            case 4:
                config.defVal = "6";
                config.unitId = 3;
                return true;
            case 5:
                config.defVal = "5";
                config.unitId = 3;
                return true;
            case 6:
                config.defVal = "1";
                return true;
            case 7:
                config.defVal = "3";
                config.unitId = 3;
                return true;
            case 8:
                config.defVal = "16";
                config.unitId = 3;
                return true;
            case 9:
                config.defVal = "30";
                config.unitId = 3;
                return true;
            case 10:
                config.defVal = "0";
                return true;
            case 11:
                config.defVal = "1";
                return true;
            case 12:
                config.defVal = "vzims.com";
                return true;
            case 13:
                config.defVal = "1";
                return true;
            case 14:
                config.defVal = "1";
                return true;
            case 15:
                config.defVal = "1200";
                config.unitId = 3;
                return true;
            case 16:
                config.defVal = "86400";
                config.unitId = 3;
                return true;
            case 17:
                config.defVal = "1";
                return true;
            case 18:
                config.defVal = "7776000";
                config.unitId = 3;
                return true;
            case 19:
                config.defVal = "60";
                config.unitId = 3;
                return true;
            case 20:
                config.defVal = "625000";
                config.unitId = 3;
                return true;
            case 21:
                config.defVal = "60";
                config.unitId = 3;
                return true;
            case 22:
                config.defVal = "100";
                return true;
            case 23:
                config.defVal = "30";
                config.unitId = 3;
                return true;
            case 24:
                config.defVal = "1";
                return true;
            case 25:
                config.defVal = "0";
                return true;
            case 26:
                config.defVal = "0";
                return true;
            case 27:
                config.defVal = String.valueOf(2);
                return true;
            case 1001:
                config.defVal = "21600";
                config.unitId = 3;
                return true;
            case 1002:
                config.defVal = "";
                return true;
            default:
                return false;
        }
    }
}
