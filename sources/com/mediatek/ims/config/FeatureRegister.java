package com.mediatek.ims.config;

import android.content.Context;
import android.os.Handler;
import com.mediatek.ims.config.ImsConfigContract;
import com.mediatek.ims.config.Register;
import java.util.Iterator;

public final class FeatureRegister extends Register {
    private int mNetworkType;

    public FeatureRegister(Context context, int phoneId, int networkType) {
        this(context, phoneId, networkType, (Handler) null);
    }

    public FeatureRegister(Context context, int phoneId, int networkType, Handler handler) {
        super(context, phoneId, handler);
        this.argType = "feature";
        this.mNetworkType = networkType;
    }

    public Register addArg(int argId) {
        if (ImsConfigContract.Validator.isValidFeatureId(argId)) {
            super.addArg(argId);
            return this;
        }
        throw new IllegalArgumentException("Invalid feature id " + argId + " to register");
    }

    public void register(Register.IArgsChangeListener listener) {
        super.register(listener);
        Iterator it = this.mRegArgs.iterator();
        while (it.hasNext()) {
            this.mContext.getContentResolver().registerContentObserver(ImsConfigContract.Feature.getUriWithFeatureId(this.mPhoneId, ((Integer) it.next()).intValue(), this.mNetworkType), false, this.mArgsObserver);
        }
    }

    public void unregister() {
        super.unregister();
    }
}
