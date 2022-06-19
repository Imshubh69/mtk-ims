package com.mediatek.ims.acs;

import android.content.Context;
import android.os.Bundle;
import android.telephony.ims.RcsClientConfiguration;
import android.telephony.ims.stub.ImsConfigImplBase;
import android.util.Log;
import com.android.ims.internal.IImsConfig;
import com.mediatek.ims.rcsua.RcsUaService;

public class MtkAcsImsConfigImpl extends ImsConfigImplBase {
    public static final int FAILED = 1;
    private static final String KEY_DEREG_SUSPEND = "OPTION_DEREG_SUSPEND";
    public static final int SUCCESS = 0;
    private static final String TAG = "MtkAcsImsConfigImpl";
    public static final int UNKNOWN = -1;
    private static Context mContext;
    private final IImsConfig mOldConfigInterface;

    public MtkAcsImsConfigImpl(Context context, IImsConfig config) {
        this.mOldConfigInterface = config;
        mContext = context;
        Log.e(TAG, "MtkAcsImsConfigImpl called ..with config with context" + config + "context " + mContext + " context" + context);
    }

    public class mAcsCallback implements RcsUaService.Callback {
        public mAcsCallback() {
        }

        public void serviceConnected(RcsUaService service) {
            Log.e(MtkAcsImsConfigImpl.TAG, " second Service connected called ");
        }

        public void serviceDisconnected(RcsUaService service) {
            Log.e(MtkAcsImsConfigImpl.TAG, "second Service disconnected called ");
        }
    }

    public void setRcsClientConfiguration(RcsClientConfiguration rcc) {
        Log.d(TAG, "setRcsClientConfig called in ACS vendor with rcc value " + rcc);
        Bundle bundle = new Bundle();
        bundle.putInt("OPTION_DEREG_SUSPEND", 1);
        RcsUaService.startService(mContext, 0, new mAcsCallback(), bundle);
    }
}
