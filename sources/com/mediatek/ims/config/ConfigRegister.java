package com.mediatek.ims.config;

import android.content.Context;
import android.os.Handler;
import com.mediatek.ims.config.ImsConfigContract;
import com.mediatek.ims.config.Register;
import java.util.Iterator;

public final class ConfigRegister extends Register {
    public ConfigRegister(Context context, int phoneId) {
        this(context, phoneId, (Handler) null);
    }

    public ConfigRegister(Context context, int phoneId, Handler handler) {
        super(context, phoneId, handler);
        this.argType = "config";
    }

    public Register addArg(int argId) {
        if (ImsConfigContract.Validator.isValidConfigId(argId)) {
            super.addArg(argId);
            return this;
        }
        throw new IllegalArgumentException("Invalid config id " + argId + " to register");
    }

    public void register(Register.IArgsChangeListener listener) {
        super.register(listener);
        Iterator it = this.mRegArgs.iterator();
        while (it.hasNext()) {
            this.mContext.getContentResolver().registerContentObserver(ImsConfigContract.Master.getUriWithConfigId(this.mPhoneId, ((Integer) it.next()).intValue()), false, this.mArgsObserver);
        }
    }

    public void unregister() {
        super.unregister();
    }
}
