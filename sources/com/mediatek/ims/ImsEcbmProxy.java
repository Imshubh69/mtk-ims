package com.mediatek.ims;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ims.stub.ImsEcbmImplBase;
import com.android.ims.ImsManager;
import com.mediatek.ims.config.internal.ImsConfigUtils;
import com.mediatek.ims.plugin.ExtensionFactory;
import com.mediatek.ims.plugin.ImsCallOemPlugin;
import com.mediatek.ims.ril.ImsCommandsInterface;

public class ImsEcbmProxy extends ImsEcbmImplBase {
    protected static final int EVENT_ON_ENTER_ECBM = 1;
    protected static final int EVENT_ON_EXIT_ECBM = 2;
    protected static final int EVENT_ON_NO_ECBM = 3;
    private static final String LOG_TAG = "ImsEcbmProxy";
    private static final boolean MTK_VZW_SUPPORT = "OP12".equals(SystemProperties.get("persist.vendor.operator.optr", "OM"));
    private Context mContext;
    /* access modifiers changed from: private */
    public boolean mHandleExitEcbmInd;
    private final Handler mHandler;
    private ImsCommandsInterface mImsRILAdapter;
    private ImsServiceCallTracker mImsServiceCT;
    private int mPhoneId;

    ImsEcbmProxy(Context context, ImsCommandsInterface adapter, int phoneId) {
        C01131 r0 = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        ImsEcbmProxy.this.enteredEcbm();
                        return;
                    case 2:
                        if (ImsEcbmProxy.this.mHandleExitEcbmInd) {
                            boolean unused = ImsEcbmProxy.this.mHandleExitEcbmInd = false;
                            ImsEcbmProxy.this.exitedEcbm();
                            if (ImsEcbmProxy.this.getImsOemCallUtil().needTurnOffVolteAfterE911()) {
                                ImsEcbmProxy.this.tryTurnOffVolteAfterE911();
                                return;
                            }
                            return;
                        }
                        return;
                    case 3:
                        if (ImsEcbmProxy.this.getImsOemCallUtil().needTurnOffVolteAfterE911()) {
                            ImsEcbmProxy.this.tryTurnOffVolteAfterE911();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mHandler = r0;
        logWithPhoneId("new EcbmProxy");
        this.mContext = context;
        this.mImsRILAdapter = adapter;
        this.mPhoneId = phoneId;
        this.mImsServiceCT = ImsServiceCallTracker.getInstance(phoneId);
        ImsCommandsInterface imsCommandsInterface = this.mImsRILAdapter;
        if (imsCommandsInterface != null) {
            imsCommandsInterface.registerForOnEnterECBM(r0, 1, (Object) null);
            this.mImsRILAdapter.registerForOnExitECBM(r0, 2, (Object) null);
            this.mImsRILAdapter.registerForOnNoECBM(r0, 3, (Object) null);
        }
    }

    public void exitEmergencyCallbackMode() {
        if (this.mImsRILAdapter != null) {
            logWithPhoneId("request exit ECBM");
            this.mHandleExitEcbmInd = true;
            this.mImsRILAdapter.requestExitEmergencyCallbackMode((Message) null);
            return;
        }
        logWithPhoneId("request exit ECBM failed");
    }

    /* access modifiers changed from: private */
    public void tryTurnOffVolteAfterE911() {
        ImsManager imsManager = ImsManager.getInstance(this.mContext, this.mPhoneId);
        boolean volteEnabledByPlatform = imsManager.isVolteEnabledByPlatform();
        boolean volteEnabledByUser = imsManager.isEnhanced4gLteModeSettingEnabledByUser();
        if (!this.mImsServiceCT.getEnableVolteForImsEcc()) {
            return;
        }
        if (!volteEnabledByPlatform || !volteEnabledByUser) {
            ImsConfigUtils.triggerSendCfgForVolte(this.mContext, this.mImsRILAdapter, this.mPhoneId, 0);
            this.mImsServiceCT.setEnableVolteForImsEcc(false);
        }
    }

    public ImsCallOemPlugin getImsOemCallUtil() {
        return ExtensionFactory.makeOemPluginFactory(this.mContext).makeImsCallPlugin(this.mContext);
    }

    private void logWithPhoneId(String msg) {
        Rlog.d(LOG_TAG, "[PhoneId = " + this.mPhoneId + "] " + msg);
    }
}
