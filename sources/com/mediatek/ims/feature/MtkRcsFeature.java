package com.mediatek.ims.feature;

import android.content.Context;
import android.telephony.ims.feature.CapabilityChangeRequest;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.feature.RcsFeature;
import android.telephony.ims.stub.CapabilityExchangeEventListener;
import android.telephony.ims.stub.RcsCapabilityExchangeImplBase;
import android.util.Log;
import com.android.ims.internal.IImsConfig;
import com.mediatek.ims.ImsService;
import com.mediatek.ims.uce.MtkRcsCapabilityExchange;

public class MtkRcsFeature extends RcsFeature {
    private static final int DELAY_IMS_SERVICE_IMPL_QUERY_MS = 5000;
    private static final int MAXMUIM_IMS_SERVICE_IMPL_RETRY = 3;
    private static final String TAG = "MtkRcsFeature";
    private CapabilityExchangeEventListener mCapEventListener;
    private MtkRcsCapabilityExchange mCapExchangeImpl;
    /* access modifiers changed from: private */
    public Context mContext;
    private ImsService mImsServiceImpl = null;
    private final ImsService.IMtkRcsFeatureCallback mImsServiceRcsCallback = new ImsService.IMtkRcsFeatureCallback() {
        public void notifyContextChanged(Context context) {
            Context unused = MtkRcsFeature.this.mContext = context;
            Log.d(MtkRcsFeature.TAG, "Set context to this " + MtkRcsFeature.this.mContext);
        }

        public void notifyCapabilitiesChanged(RcsFeature.RcsImsCapabilities c) {
            Log.d(MtkRcsFeature.TAG, "notifyCapabilitiesStatusChanged " + c);
            MtkRcsFeature.this.onCapabilitiesStatusChanged(c);
        }
    };
    private final RcsFeature.RcsImsCapabilities mRcsCapabilitiesIWan;
    private final RcsFeature.RcsImsCapabilities mRcsCapabilitiesLte;
    private int mSlotId;

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0066  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MtkRcsFeature(int r8, android.content.Context r9) {
        /*
            r7 = this;
            r7.<init>()
            r0 = 0
            r7.mImsServiceImpl = r0
            com.mediatek.ims.feature.MtkRcsFeature$1 r1 = new com.mediatek.ims.feature.MtkRcsFeature$1
            r1.<init>()
            r7.mImsServiceRcsCallback = r1
            r7.mContext = r9
            r7.mSlotId = r8
            r1 = 0
            android.telephony.ims.feature.RcsFeature$RcsImsCapabilities r2 = new android.telephony.ims.feature.RcsFeature$RcsImsCapabilities
            r3 = 2
            r2.<init>(r3)
            r7.mRcsCapabilitiesLte = r2
            android.telephony.ims.feature.RcsFeature$RcsImsCapabilities r2 = new android.telephony.ims.feature.RcsFeature$RcsImsCapabilities
            r2.<init>(r3)
            r7.mRcsCapabilitiesIWan = r2
        L_0x0021:
            com.mediatek.ims.ImsService r2 = r7.mImsServiceImpl
            java.lang.String r4 = "MtkRcsFeature"
            if (r2 != 0) goto L_0x0064
            r5 = 3
            if (r1 >= r5) goto L_0x0064
            com.mediatek.ims.ImsService r2 = com.mediatek.ims.ImsService.getInstance(r0)
            r7.mImsServiceImpl = r2
            if (r2 != 0) goto L_0x0063
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x004e }
            r2.<init>()     // Catch:{ Exception -> 0x004e }
            java.lang.String r5 = "ImsService is not initialized yet. Query later - "
            r2.append(r5)     // Catch:{ Exception -> 0x004e }
            r2.append(r1)     // Catch:{ Exception -> 0x004e }
            java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x004e }
            android.util.Log.d(r4, r2)     // Catch:{ Exception -> 0x004e }
            r5 = 5000(0x1388, double:2.4703E-320)
            java.lang.Thread.sleep(r5)     // Catch:{ Exception -> 0x004e }
            int r1 = r1 + 1
            goto L_0x0063
        L_0x004e:
            r2 = move-exception
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Fail to get ImsService "
            r5.append(r6)
            r5.append(r2)
            java.lang.String r5 = r5.toString()
            android.util.Log.e(r4, r5)
        L_0x0063:
            goto L_0x0021
        L_0x0064:
            if (r2 == 0) goto L_0x008e
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "initialize mContext "
            r0.append(r2)
            android.content.Context r2 = r7.mContext
            r0.append(r2)
            java.lang.String r2 = " slotId "
            r0.append(r2)
            r0.append(r8)
            java.lang.String r0 = r0.toString()
            android.util.Log.d(r4, r0)
            com.mediatek.ims.ImsService r0 = r7.mImsServiceImpl
            com.mediatek.ims.ImsService$IMtkRcsFeatureCallback r2 = r7.mImsServiceRcsCallback
            r0.setRcsFeatureCallback(r8, r2)
            r7.setFeatureState(r3)
        L_0x008e:
            java.lang.String r0 = "MtkRcsFeature loded"
            android.util.Log.d(r4, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.feature.MtkRcsFeature.<init>(int, android.content.Context):void");
    }

    public RcsCapabilityExchangeImplBase createCapabilityExchangeImpl(CapabilityExchangeEventListener listener) {
        this.mCapEventListener = listener;
        this.mCapExchangeImpl = new MtkRcsCapabilityExchange(this.mSlotId, this.mContext, listener);
        Log.d(TAG, "MtkRcsFeature >> mCapExchangeImpl : " + this.mCapExchangeImpl + ",mCapEventListener : " + this.mCapEventListener + ", Context : " + this.mContext + ",SLot id : " + this.mSlotId);
        return this.mCapExchangeImpl;
    }

    public void destroyCapabilityExchangeImpl(RcsCapabilityExchangeImplBase capExchangeImpl) {
        this.mCapEventListener = null;
        Log.d(TAG, "destroyCapabilityExchangeImpl >> mCapEventListener : " + this.mCapEventListener);
    }

    public void onFeatureReady() {
        Log.d(TAG, "onFeatureReady called!");
    }

    public void enableIms(int slotId) {
        Log.d(TAG, "mImsServiceImpl >> enableIms called! >> " + this.mImsServiceImpl);
        ImsService imsService = this.mImsServiceImpl;
        if (imsService != null) {
            imsService.enableIms(slotId);
        }
    }

    public void disableIms(int slotId) {
        Log.d(TAG, "mImsServiceImpl >> disableIms called! >> " + this.mImsServiceImpl);
        ImsService imsService = this.mImsServiceImpl;
        if (imsService != null) {
            imsService.disableIms(slotId);
        }
    }

    public IImsConfig getConfigInterface() {
        ImsService imsService = this.mImsServiceImpl;
        if (imsService != null) {
            return imsService.onGetConfigInterface(this.mSlotId);
        }
        return null;
    }

    public final void onCapabilitiesStatusChanged(RcsFeature.RcsImsCapabilities c) {
        try {
            MtkRcsFeature.super.notifyCapabilitiesStatusChanged(c);
        } catch (IllegalStateException e) {
            Log.e(TAG, "onCapabilitiesStatusChanged error. msg " + e.getMessage());
        }
    }

    public void changeEnabledCapabilities(CapabilityChangeRequest request, ImsFeature.CapabilityCallbackProxy c) {
        Log.d(TAG, "changeEnabledCapabilities : " + request.getCapabilitiesToEnable());
        for (CapabilityChangeRequest.CapabilityPair pair : request.getCapabilitiesToEnable()) {
            Log.d(TAG, "CapabilityChangeRequest : " + pair.getRadioTech());
            if (pair.getRadioTech() == 0) {
                this.mRcsCapabilitiesLte.addCapabilities(pair.getCapability());
            } else if (pair.getRadioTech() == 1) {
                this.mRcsCapabilitiesIWan.addCapabilities(pair.getCapability());
            }
        }
        Log.d(TAG, "Disabled RCS capabilities : " + request.getCapabilitiesToDisable());
        for (CapabilityChangeRequest.CapabilityPair pair2 : request.getCapabilitiesToDisable()) {
            Log.d(TAG, "Disabled RCS capabilities >> CapabilityChangeRequest : " + pair2.getRadioTech());
            if (pair2.getRadioTech() == 0) {
                this.mRcsCapabilitiesLte.removeCapabilities(pair2.getCapability());
            } else if (pair2.getRadioTech() == 1) {
                this.mRcsCapabilitiesIWan.removeCapabilities(pair2.getCapability());
            }
        }
    }

    public boolean queryCapabilityConfiguration(int capability, int radioTech) {
        Log.d(TAG, "queryCapabilityConfiguration >> radioTech : " + radioTech);
        if (radioTech == 0) {
            return this.mRcsCapabilitiesLte.isCapable(capability);
        }
        if (radioTech == 1) {
            return this.mRcsCapabilitiesIWan.isCapable(capability);
        }
        return false;
    }
}
