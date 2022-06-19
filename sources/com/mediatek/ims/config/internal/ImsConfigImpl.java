package com.mediatek.ims.config.internal;

import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ims.compat.stub.ImsConfigImplBase;
import android.text.TextUtils;
import com.android.ims.ImsConfigListener;
import com.android.ims.ImsException;
import com.mediatek.ims.ImsCommonUtil;
import com.mediatek.ims.plugin.ExtensionFactory;
import com.mediatek.ims.plugin.ImsCallOemPlugin;
import com.mediatek.ims.ril.ImsCommandsInterface;

public class ImsConfigImpl extends ImsConfigImplBase {
    private static final boolean DEBUG;
    private static final String PROPERTY_IMSCONFIG_FORCE_NOTIFY = "vendor.ril.imsconfig.force.notify";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final String TAG = "ImsConfigImpl";
    private ImsConfigAdapter mConfigAdapter = null;
    private Context mContext;
    private ImsCallOemPlugin mImsCallOemPlugin = null;
    private String mLogTag;
    private int mPhoneId;
    private ImsCommandsInterface mRilAdapter;

    static {
        boolean z = false;
        if (TextUtils.equals(Build.TYPE, "eng") || SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        DEBUG = z;
    }

    public ImsConfigImpl(Context context, ImsCommandsInterface imsRilAdapter, ImsConfigAdapter configAdapter, int phoneId) {
        super(context);
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mRilAdapter = imsRilAdapter;
        this.mConfigAdapter = configAdapter;
        this.mLogTag = "ImsConfigImpl[" + phoneId + "]";
    }

    public int getProvisionedValue(int item) {
        try {
            int result = this.mConfigAdapter.getProvisionedValue(item);
            String str = this.mLogTag;
            Rlog.i(str, "getProvisionedValue(" + item + ") : " + result + " on phone" + this.mPhoneId + " from binder pid " + Binder.getCallingPid() + ", binder uid " + Binder.getCallingUid() + ", process pid " + Process.myPid() + ", process uid " + Process.myUid());
            return result;
        } catch (ImsException e) {
            String str2 = this.mLogTag;
            Rlog.e(str2, "getProvisionedValue(" + item + ") failed, code: " + e.getCode());
            if (Binder.getCallingPid() == Process.myPid()) {
                return 0;
            }
            return -1;
        }
    }

    public String getProvisionedStringValue(int item) {
        try {
            String result = this.mConfigAdapter.getProvisionedStringValue(item);
            String str = this.mLogTag;
            Rlog.i(str, "getProvisionedStringValue(" + item + ") : " + result + " on phone " + this.mPhoneId + " from binder pid " + Binder.getCallingPid() + ", binder uid " + Binder.getCallingUid() + ", process pid " + Process.myPid() + ", process uid " + Process.myUid());
            return result;
        } catch (ImsException e) {
            String str2 = this.mLogTag;
            Rlog.e(str2, "getProvisionedStringValue(" + item + ") failed, code: " + e.getCode());
            if (Binder.getCallingPid() == Process.myPid()) {
                return "Unknown";
            }
            return null;
        }
    }

    public int setProvisionedValue(int item, int value) {
        try {
            this.mConfigAdapter.setProvisionedValue(item, value);
            if (DEBUG) {
                String str = this.mLogTag;
                Rlog.i(str, "setProvisionedValue(" + item + ", " + value + ") on phone " + this.mPhoneId + " from pid " + Binder.getCallingPid() + ", uid " + Binder.getCallingUid() + " ,retVal:" + 0);
            }
            return 0;
        } catch (ImsException e) {
            String str2 = this.mLogTag;
            Rlog.e(str2, "setProvisionedValue(" + item + ") failed, code: " + e.getCode());
            return 1;
        }
    }

    public int setProvisionedStringValue(int item, String value) {
        try {
            this.mConfigAdapter.setProvisionedStringValue(item, value);
            String str = this.mLogTag;
            Rlog.i(str, "setProvisionedStringValue(" + item + ", " + value + ") on phone " + this.mPhoneId + " from pid " + Binder.getCallingPid() + ", uid " + Binder.getCallingUid() + " ,retVal:" + 0);
            return 0;
        } catch (ImsException e) {
            String str2 = this.mLogTag;
            Rlog.e(str2, "setProvisionedValue(" + item + ") failed, code: " + e.getCode());
            return 1;
        }
    }

    public void getFeatureValue(int feature, int network, ImsConfigListener listener) {
        try {
            int value = this.mConfigAdapter.getFeatureValue(feature, network);
            String str = this.mLogTag;
            Rlog.i(str, "getFeatureValue(" + feature + ", " + network + ") : " + value + " on phone " + this.mPhoneId);
            listener.onGetFeatureResponse(feature, network, value, 0);
        } catch (ImsException e) {
            try {
                String str2 = this.mLogTag;
                Rlog.e(str2, "getFeatureValue(" + feature + ") failed, code: " + e.getCode());
                listener.onGetFeatureResponse(feature, network, 0, 1);
            } catch (RemoteException e2) {
                String str3 = this.mLogTag;
                Rlog.e(str3, "getFeatureValue(" + feature + ") remote failed!");
                throw new RuntimeException(e2);
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    public void setFeatureValue(int feature, int network, int value, ImsConfigListener listener) {
        try {
            String str = this.mLogTag;
            Rlog.i(str, "setFeatureValue(" + feature + ", " + network + ", " + value + ") on phone " + this.mPhoneId + " from pid " + Binder.getCallingPid() + ", uid " + Binder.getCallingUid() + ", listener " + listener);
            if (feature == 4 || feature == 5 || feature == -1) {
                String str2 = this.mLogTag;
                Rlog.i(str2, "setFeatureValue is not support currently:" + feature);
                throw new ImsException("setFeatureValue is not support UT currently.", 102);
            }
            if (!ImsCommonUtil.supportMims()) {
                if (ImsCommonUtil.getMainCapabilityPhoneId() != this.mPhoneId) {
                    String str3 = this.mLogTag;
                    Rlog.i(str3, "setFeatureValue is not allow on non main capability phoneId:" + this.mPhoneId + " in non MIMS project");
                    throw new ImsException("Do not setFeatureValue for non MIMS not main capability phoneId: " + this.mPhoneId, 102);
                }
            }
            if (value == 1 && "1".equals(SystemProperties.get("persist.vendor.mtk_dynamic_ims_switch")) && this.mConfigAdapter.getImsResCapability(feature) != 1 && SystemProperties.getInt(PROPERTY_IMSCONFIG_FORCE_NOTIFY, 0) == 0) {
                Rlog.i(this.mLogTag, "setFeatureValue, modify the value in ImsConfig.");
                value = 0;
            }
            this.mConfigAdapter.setFeatureValue(feature, network, value, -1);
            if (this.mImsCallOemPlugin == null) {
                this.mImsCallOemPlugin = ExtensionFactory.makeOemPluginFactory(this.mContext).makeImsCallPlugin(this.mContext);
            }
            if (this.mImsCallOemPlugin.isUpdateViwifiFeatureValueAsViLTE() && 1 == feature) {
                this.mConfigAdapter.setFeatureValue(3, 1, value, -1);
            }
            if (!ImsCommonUtil.supportMdAutoSetupIms()) {
                switch (feature) {
                    case 0:
                        if (value != ImsConfigUtils.getFeaturePropValue(ImsConfigUtils.PROPERTY_VOLTE_ENALBE, this.mPhoneId)) {
                            if (value != 1) {
                                this.mRilAdapter.turnOffVolte((Message) null);
                                break;
                            } else {
                                this.mRilAdapter.turnOnVolte((Message) null);
                                break;
                            }
                        }
                        break;
                    case 1:
                        if (value != ImsConfigUtils.getFeaturePropValue(ImsConfigUtils.PROPERTY_VILTE_ENALBE, this.mPhoneId)) {
                            if (value != 1) {
                                this.mRilAdapter.turnOffVilte((Message) null);
                                if (this.mImsCallOemPlugin.isUpdateViwifiFeatureValueAsViLTE()) {
                                    this.mRilAdapter.turnOffViwifi((Message) null);
                                    break;
                                }
                            } else {
                                this.mRilAdapter.turnOnVilte((Message) null);
                                if (this.mImsCallOemPlugin.isUpdateViwifiFeatureValueAsViLTE()) {
                                    this.mRilAdapter.turnOnViwifi((Message) null);
                                    break;
                                }
                            }
                        }
                        break;
                    case 2:
                        if (value != ImsConfigUtils.getFeaturePropValue(ImsConfigUtils.PROPERTY_WFC_ENALBE, this.mPhoneId)) {
                            if (value != 1) {
                                this.mRilAdapter.turnOffWfc((Message) null);
                                break;
                            } else {
                                this.mRilAdapter.turnOnWfc((Message) null);
                                break;
                            }
                        }
                        break;
                    case 3:
                        if (value != ImsConfigUtils.getFeaturePropValue(ImsConfigUtils.PROPERTY_VIWIFI_ENALBE, this.mPhoneId)) {
                            if (value != 1) {
                                this.mRilAdapter.turnOffViwifi((Message) null);
                                break;
                            } else {
                                this.mRilAdapter.turnOnViwifi((Message) null);
                                break;
                            }
                        }
                        break;
                }
            }
            if (listener != null) {
                listener.onSetFeatureResponse(feature, network, value, 0);
            }
        } catch (ImsException e) {
            try {
                String str4 = this.mLogTag;
                Rlog.e(str4, "setFeatureValue(" + feature + ") failed, code: " + e.getCode());
                if (listener != null) {
                    listener.onSetFeatureResponse(feature, network, 0, 1);
                }
            } catch (RemoteException e2) {
                String str5 = this.mLogTag;
                Rlog.e(str5, "setFeatureValue(" + feature + ") remote failed!");
                throw new RuntimeException(e2);
            }
        }
    }

    public boolean getVolteProvisioned() {
        return true;
    }
}
