package com.mediatek.ims;

import android.annotation.SystemApi;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsService;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.feature.RcsFeature;
import android.telephony.ims.stub.ImsConfigImplBase;
import android.telephony.ims.stub.ImsFeatureConfiguration;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import android.telephony.ims.stub.SipTransportImplBase;
import android.util.Log;
import android.util.SparseArray;
import androidx.core.content.ContextCompat;
import com.mediatek.ims.config.MtkImsConfigImpl;
import com.mediatek.ims.feature.MtkMmTelFeature;
import com.mediatek.ims.feature.MtkRcsFeature;
import com.mediatek.ims.rcse.MtkSipTransport;

@SystemApi
public class MtkDynamicImsService extends ImsService {
    public static final long CAPABILITY_SIP_DELEGATE_CREATION = 2;
    private static final String PROPERTY_MTK_RCS_S_REG = "persist.vendor.mtk_rcs_single_reg_support";
    private static final String TAG = "MtkDynamicImsService";
    protected final SparseArray<ImsConfigImplBase> mImsConfig = new SparseArray<>();
    protected final SparseArray<ImsRegistrationImplBase> mImsReg = new SparseArray<>();
    protected final SparseArray<MmTelFeature> mMmTel = new SparseArray<>();
    protected final SparseArray<RcsFeature> mRcs = new SparseArray<>();
    protected final SparseArray<SipTransportImplBase> mSipTrans = new SparseArray<>();

    public IBinder onBind(Intent intent) {
        if (!"android.telephony.ims.ImsService".equals(intent.getAction())) {
            return null;
        }
        log("MtkDynamicImsService Bound.");
        return this.mImsServiceController;
    }

    public boolean onUnbind(Intent intent) {
        logi("onUnbind...");
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            MtkMmTelFeature feature = this.mMmTel.get(i);
            if (feature != null) {
                feature.close();
                this.mMmTel.delete(i);
            }
            MtkImsRegistrationImpl reg = this.mImsReg.get(i);
            if (reg != null) {
                reg.close();
                this.mImsReg.delete(i);
            }
        }
        return MtkDynamicImsService.super.onUnbind(intent);
    }

    public ImsFeatureConfiguration querySupportedImsFeatures() {
        ImsFeatureConfiguration.Builder builder = new ImsFeatureConfiguration.Builder();
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            builder.addFeature(i, 1);
            builder.addFeature(i, 0);
            builder.addFeature(i, 2);
        }
        Log.d(TAG, "Supported Ims Features: " + builder.build());
        return builder.build();
    }

    public void readyForFeatureCreation() {
        Log.d(TAG, "readyForFeatureCreation");
    }

    public void enableIms(int slotId) {
        MtkMmTelFeature feature = this.mMmTel.get(slotId);
        MtkRcsFeature rcsFeature = this.mRcs.get(slotId);
        Log.d(TAG, "RCSFEATURE : " + rcsFeature + " ,MMTELFEATURE : " + feature);
        if (feature != null && SubscriptionManager.isValidPhoneId(slotId)) {
            Log.d(TAG, "MMTELFEATURE >> enableIms");
            feature.enableIms(slotId);
        }
        if (rcsFeature != null && SubscriptionManager.isValidPhoneId(slotId)) {
            Log.d(TAG, "RCSFEATURE >> enableIms");
            rcsFeature.enableIms(slotId);
        }
    }

    public void disableIms(int slotId) {
        MtkMmTelFeature feature = this.mMmTel.get(slotId);
        MtkRcsFeature rcsFeature = this.mRcs.get(slotId);
        Log.d(TAG, "Disable >> RCSFEATURE : " + rcsFeature + ",MMTELFEATURE : " + feature);
        if (feature != null && SubscriptionManager.isValidPhoneId(slotId)) {
            feature.disableIms(slotId);
        }
        if (rcsFeature != null && SubscriptionManager.isValidPhoneId(slotId)) {
            Log.d(TAG, "RCSFEATURE >> disableIms");
            rcsFeature.disableIms(slotId);
        }
    }

    public MmTelFeature createMmTelFeature(int slotId) {
        MmTelFeature feature = this.mMmTel.get(slotId);
        if (feature == null && SubscriptionManager.isValidPhoneId(slotId)) {
            feature = new MtkMmTelFeature(slotId);
            this.mMmTel.put(slotId, feature);
        }
        Log.d(TAG, "createMmTelFeature " + feature);
        return feature;
    }

    /* JADX WARNING: type inference failed for: r3v0, types: [android.content.Context, com.mediatek.ims.MtkDynamicImsService] */
    public RcsFeature createRcsFeature(int slotId) {
        RcsFeature feature = this.mRcs.get(slotId);
        if (feature == null && SubscriptionManager.isValidPhoneId(slotId)) {
            feature = new MtkRcsFeature(slotId, this);
            this.mRcs.put(slotId, feature);
        }
        Log.d(TAG, "[" + slotId + "] createRcsFeature " + feature);
        return feature;
    }

    public ImsConfigImplBase getConfig(int slotId) {
        ImsConfigImplBase config = this.mImsConfig.get(slotId);
        if (config == null && SubscriptionManager.isValidPhoneId(slotId)) {
            MtkMmTelFeature feature = this.mMmTel.get(slotId);
            MtkRcsFeature rcsFeature = this.mRcs.get(slotId);
            if (feature != null) {
                config = new MtkImsConfigImpl(feature.getConfigInterface());
                this.mImsConfig.put(slotId, config);
            }
            if (rcsFeature != null) {
                config = new MtkImsConfigImpl(rcsFeature.getConfigInterface());
                this.mImsConfig.put(slotId, config);
                Log.d(TAG, "RCSFEATURE >> getConfig " + config);
            }
        }
        Log.d(TAG, "[" + slotId + "] getConfig " + config);
        return config;
    }

    public ImsRegistrationImplBase getRegistration(int slotId) {
        ImsRegistrationImplBase reg = this.mImsReg.get(slotId);
        Log.d(TAG, "[" + slotId + "] getRegistration >> isValidPhoneId :: " + SubscriptionManager.isValidPhoneId(slotId));
        if (reg == null && SubscriptionManager.isValidPhoneId(slotId)) {
            reg = new MtkImsRegistrationImpl(slotId);
            Log.d(TAG, "[" + slotId + "] reg " + reg);
            this.mImsReg.put(slotId, reg);
        }
        Log.d(TAG, "[" + slotId + "] getRegistration " + reg);
        return reg;
    }

    /* JADX WARNING: type inference failed for: r4v0, types: [android.content.Context, com.mediatek.ims.MtkDynamicImsService] */
    public SipTransportImplBase getSipTransport(int slotId) {
        if (!SystemProperties.getBoolean(PROPERTY_MTK_RCS_S_REG, false)) {
            return null;
        }
        log("[" + slotId + "] getSipTransport inside");
        SipTransportImplBase sipTrans = this.mSipTrans.get(slotId);
        log("[" + slotId + "] getSipTransport inside sipTrans: " + sipTrans);
        if (sipTrans == null && SubscriptionManager.isValidPhoneId(slotId)) {
            sipTrans = new MtkSipTransport(ContextCompat.getMainExecutor(this), this);
            log("[" + slotId + "] getSipTransport inside new sipTrans: " + sipTrans);
            this.mSipTrans.put(slotId, sipTrans);
        }
        log("[" + slotId + "] getSipTransport  return sipTrans" + sipTrans);
        return sipTrans;
    }

    public long getImsServiceCapabilities() {
        log("getImsServiceCapabilities inside CAPABILITY_SIP_DELEGATE_CREATION : 2");
        return 2;
    }

    private static void log(String msg) {
        Rlog.d(TAG, msg);
    }

    private static void logi(String msg) {
        Rlog.i(TAG, msg);
    }

    private static void loge(String msg) {
        Rlog.e(TAG, msg);
    }
}
