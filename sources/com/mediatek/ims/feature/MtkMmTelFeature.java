package com.mediatek.ims.feature;

import android.annotation.SystemApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.aidl.IImsCallSessionListener;
import android.telephony.ims.feature.CapabilityChangeRequest;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.stub.ImsCallSessionImplBase;
import android.telephony.ims.stub.ImsEcbmImplBase;
import android.telephony.ims.stub.ImsMultiEndpointImplBase;
import android.telephony.ims.stub.ImsSmsImplBase;
import android.telephony.ims.stub.ImsUtImplBase;
import android.text.TextUtils;
import com.android.ims.ImsConfigListener;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsUt;
import com.mediatek.ims.ImsCommonUtil;
import com.mediatek.ims.ImsService;
import com.mediatek.ims.ImsUtImpl;
import com.mediatek.ims.plugin.ExtensionFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SystemApi
public class MtkMmTelFeature extends MmTelFeature {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = (TextUtils.equals(Build.TYPE, "eng") || SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1);
    private static final int DELAY_IMS_SERVICE_IMPL_QUERY_MS = 5000;
    public static final int FEATURE_DISABLED = 0;
    public static final int FEATURE_ENABLED = 1;
    public static final int FEATURE_TYPE_UNKNOWN = -1;
    public static final int FEATURE_TYPE_UT_OVER_LTE = 4;
    public static final int FEATURE_TYPE_UT_OVER_WIFI = 5;
    public static final int FEATURE_TYPE_VIDEO_OVER_LTE = 1;
    public static final int FEATURE_TYPE_VIDEO_OVER_NR = 7;
    public static final int FEATURE_TYPE_VIDEO_OVER_WIFI = 3;
    public static final int FEATURE_TYPE_VOICE_OVER_LTE = 0;
    public static final int FEATURE_TYPE_VOICE_OVER_NR = 6;
    public static final int FEATURE_TYPE_VOICE_OVER_WIFI = 2;
    public static final int FEATURE_UNKNOWN = -1;
    private static final String LOG_TAG = "MtkMmTelFeature";
    private static final int MAXMUIM_IMS_SERVICE_IMPL_RETRY = 3;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final Map<Integer, Integer> REG_TECH_TO_NET_TYPE;
    private static final int WAIT_TIMEOUT_MS = 2000;
    /* access modifiers changed from: private */
    public Context mContext;
    private final ImsService.IMtkMmTelFeatureCallback mImsServiceCallback = new ImsService.IMtkMmTelFeatureCallback() {
        public void notifyContextChanged(Context context) {
            Context unused = MtkMmTelFeature.this.mContext = context;
            MtkMmTelFeature mtkMmTelFeature = MtkMmTelFeature.this;
            mtkMmTelFeature.log("Set context to " + MtkMmTelFeature.this.mContext);
        }

        public void sendSmsRsp(int token, int messageRef, int status, int reason) {
            MtkMmTelFeature mtkMmTelFeature = MtkMmTelFeature.this;
            mtkMmTelFeature.log("sendSmsRsp, token " + token + ", messageRef " + messageRef + ", status " + status + ", reason " + reason);
            MtkImsSmsImpl smsImpl = MtkMmTelFeature.this.getSmsImplementation();
            if (smsImpl != null) {
                smsImpl.sendSmsRsp(token, messageRef, status, reason);
            }
        }

        public void newStatusReportInd(byte[] pdu, String format) {
            MtkImsSmsImpl smsImpl = MtkMmTelFeature.this.getSmsImplementation();
            if (smsImpl != null) {
                smsImpl.newStatusReportInd(pdu, format);
            }
        }

        public void newImsSmsInd(byte[] pdu, String format) {
            MtkImsSmsImpl smsImpl = MtkMmTelFeature.this.getSmsImplementation();
            if (smsImpl != null) {
                smsImpl.newImsSmsInd(pdu, format);
            }
        }

        public void notifyCapabilitiesChanged(MmTelFeature.MmTelCapabilities c) {
            MtkMmTelFeature mtkMmTelFeature = MtkMmTelFeature.this;
            mtkMmTelFeature.log("notifyCapabilitiesStatusChanged " + c);
            MtkMmTelFeature.this.onCapabilitiesStatusChanged(c);
        }

        public void notifyIncomingCall(ImsCallSessionImplBase c, Bundle extras) {
            MtkMmTelFeature mtkMmTelFeature = MtkMmTelFeature.this;
            mtkMmTelFeature.log("notifyIncomingCall ImsCallSessionImplBase " + c + " extras " + Rlog.pii(MtkMmTelFeature.LOG_TAG, extras));
            MtkMmTelFeature.this.onNotifyIncomingCall(c, extras);
        }

        public void notifyIncomingCallSession(IImsCallSession c, Bundle extras) {
            MtkMmTelFeature mtkMmTelFeature = MtkMmTelFeature.this;
            mtkMmTelFeature.log("notifyIncomingCallSession IImsCallSession " + c + " extras " + Rlog.pii(MtkMmTelFeature.LOG_TAG, extras));
            MtkMmTelFeature.this.onNotifyIncomingCallSession(c, extras);
        }

        public void updateCapbilities(CapabilityChangeRequest request) {
            MtkMmTelFeature mtkMmTelFeature = MtkMmTelFeature.this;
            mtkMmTelFeature.log("updateCapbilities " + request);
            MtkMmTelFeature.this.changeEnabledCapabilities(request, (ImsFeature.CapabilityCallbackProxy) null);
        }
    };
    private ImsService mImsServiceImpl = null;
    private int mSlotId;

    static {
        HashMap hashMap = new HashMap(2);
        REG_TECH_TO_NET_TYPE = hashMap;
        hashMap.put(0, 13);
        hashMap.put(1, 18);
        hashMap.put(3, 20);
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0053  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MtkMmTelFeature(int r6) {
        /*
            r5 = this;
            r5.<init>()
            r0 = 0
            r5.mImsServiceImpl = r0
            com.mediatek.ims.feature.MtkMmTelFeature$1 r1 = new com.mediatek.ims.feature.MtkMmTelFeature$1
            r1.<init>()
            r5.mImsServiceCallback = r1
            r5.mSlotId = r6
            r1 = 0
        L_0x0010:
            com.mediatek.ims.ImsService r2 = r5.mImsServiceImpl
            if (r2 != 0) goto L_0x0051
            r3 = 3
            if (r1 >= r3) goto L_0x0051
            com.mediatek.ims.ImsService r2 = com.mediatek.ims.ImsService.getInstance(r0)
            r5.mImsServiceImpl = r2
            if (r2 != 0) goto L_0x0050
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ InterruptedException -> 0x003b }
            r2.<init>()     // Catch:{ InterruptedException -> 0x003b }
            java.lang.String r3 = "ImsService is not initialized yet. Query later - "
            r2.append(r3)     // Catch:{ InterruptedException -> 0x003b }
            r2.append(r1)     // Catch:{ InterruptedException -> 0x003b }
            java.lang.String r2 = r2.toString()     // Catch:{ InterruptedException -> 0x003b }
            r5.log(r2)     // Catch:{ InterruptedException -> 0x003b }
            r2 = 5000(0x1388, double:2.4703E-320)
            java.lang.Thread.sleep(r2)     // Catch:{ InterruptedException -> 0x003b }
            int r1 = r1 + 1
            goto L_0x0050
        L_0x003b:
            r2 = move-exception
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "Fail to get ImsService "
            r3.append(r4)
            r3.append(r2)
            java.lang.String r3 = r3.toString()
            r5.loge(r3)
        L_0x0050:
            goto L_0x0010
        L_0x0051:
            if (r2 == 0) goto L_0x007f
            com.mediatek.ims.ImsService$IMtkMmTelFeatureCallback r0 = r5.mImsServiceCallback
            r2.setMmTelFeatureCallback(r6, r0)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "initialize mContext "
            r0.append(r2)
            android.content.Context r2 = r5.mContext
            r0.append(r2)
            java.lang.String r2 = " slotId "
            r0.append(r2)
            r0.append(r6)
            java.lang.String r0 = r0.toString()
            r5.log(r0)
            android.content.Context r0 = r5.mContext
            r5.initialize(r0, r6)
            r0 = 2
            r5.setFeatureState(r0)
        L_0x007f:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "["
            r0.append(r2)
            int r2 = r5.mSlotId
            r0.append(r2)
            java.lang.String r2 = "] MtkMmTelFeature created"
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            r5.log(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.feature.MtkMmTelFeature.<init>(int):void");
    }

    private static class ConfigListener extends ImsConfigListener.Stub {
        private final int mCapability;
        private final CountDownLatch mLatch;
        private final int mTech;

        public ConfigListener(int capability, int tech, CountDownLatch latch) {
            this.mCapability = capability;
            this.mTech = tech;
            this.mLatch = latch;
        }

        public void onGetFeatureResponse(int feature, int network, int value, int status) throws RemoteException {
            if (feature == this.mCapability && network == this.mTech) {
                this.mLatch.countDown();
                getFeatureValueReceived(value);
                return;
            }
            Rlog.e(MtkMmTelFeature.LOG_TAG, "onGetFeatureResponse: response different than requested: feature=" + feature + " and network=" + network);
        }

        public void onSetFeatureResponse(int feature, int network, int value, int status) throws RemoteException {
            if (feature == this.mCapability && network == this.mTech) {
                this.mLatch.countDown();
                setFeatureValueReceived(value);
                return;
            }
            Rlog.e(MtkMmTelFeature.LOG_TAG, "onSetFeatureResponse: response different than requested: feature=" + feature + " and network=" + network);
        }

        public void onGetVideoQuality(int status, int quality) throws RemoteException {
        }

        public void onSetVideoQuality(int status) throws RemoteException {
        }

        public void getFeatureValueReceived(int value) {
        }

        public void setFeatureValueReceived(int value) {
        }
    }

    public final void onCapabilitiesStatusChanged(MmTelFeature.MmTelCapabilities c) {
        try {
            MtkMmTelFeature.super.notifyCapabilitiesStatusChanged(c);
        } catch (IllegalStateException e) {
            loge("onCapabilitiesStatusChanged error. msg " + e.getMessage());
        }
    }

    public void onNotifyIncomingCall(ImsCallSessionImplBase c, Bundle extras) {
        MtkMmTelFeature.super.notifyIncomingCall(c, extras);
    }

    public void onNotifyIncomingCallSession(IImsCallSession c, Bundle extras) {
        MtkMmTelFeature.super.notifyIncomingCallSession(c, extras);
    }

    private MmTelFeature.MmTelCapabilities convertCapabilities(int[] enabledFeatures) {
        boolean[] featuresEnabled = new boolean[enabledFeatures.length];
        int i = 0;
        while (i <= 5 && i < enabledFeatures.length) {
            if (enabledFeatures[i] == i) {
                featuresEnabled[i] = true;
            } else if (enabledFeatures[i] == -1) {
                featuresEnabled[i] = false;
            }
            i++;
        }
        MmTelFeature.MmTelCapabilities capabilities = new MmTelFeature.MmTelCapabilities();
        if (featuresEnabled[0] || featuresEnabled[2]) {
            capabilities.addCapabilities(1);
        }
        if (featuresEnabled[1] || featuresEnabled[3]) {
            capabilities.addCapabilities(2);
        }
        if (featuresEnabled[4] || featuresEnabled[5]) {
            capabilities.addCapabilities(4);
        }
        log("convertCapabilities - capabilities: " + capabilities);
        return capabilities;
    }

    private int convertCapability(int capability, int radioTech) {
        if (radioTech == 0) {
            switch (capability) {
                case 1:
                    return 0;
                case 2:
                    return 1;
                case 4:
                    return 4;
                default:
                    return -1;
            }
        } else if (radioTech == 1) {
            switch (capability) {
                case 1:
                    return 2;
                case 2:
                    return 3;
                case 4:
                    return 5;
                default:
                    return -1;
            }
        } else if (radioTech == 3) {
            switch (capability) {
                case 1:
                    return 6;
                case 2:
                    return 7;
                default:
                    return -1;
            }
        } else {
            loge("Fail to convertCapability, cap:" + capability + ", tech:" + radioTech);
            return -1;
        }
    }

    public boolean queryCapabilityConfiguration(int capability, int radioTech) {
        int capConverted = convertCapability(capability, radioTech);
        CountDownLatch latch = new CountDownLatch(1);
        int[] returnValue = {-1};
        int regTech = REG_TECH_TO_NET_TYPE.getOrDefault(Integer.valueOf(radioTech), -1).intValue();
        try {
            IImsConfig imsConfig = getConfigInterface();
            if (imsConfig != null) {
                final int[] iArr = returnValue;
                final int i = capability;
                C01322 r11 = r1;
                final int i2 = radioTech;
                C01322 r1 = new ConfigListener(capConverted, regTech, latch) {
                    public void getFeatureValueReceived(int value) {
                        iArr[0] = value;
                        MtkMmTelFeature mtkMmTelFeature = MtkMmTelFeature.this;
                        mtkMmTelFeature.log("Feature " + i + " tech " + i2 + "enable? " + iArr[0]);
                    }
                };
                imsConfig.getFeatureValue(capConverted, regTech, r11);
            } else {
                loge("Fail to queryCapabilityConfiguration, getConfigInterface is null");
            }
        } catch (RemoteException e) {
            loge("Fail to queryCapabilityConfiguration " + e.getMessage());
        }
        try {
            latch.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e2) {
            loge("queryCapabilityConfiguration - error waiting: " + e2.getMessage());
        }
        return returnValue[0] == 1;
    }

    public void changeEnabledCapabilities(CapabilityChangeRequest request, ImsFeature.CapabilityCallbackProxy c) {
        int i;
        int radioTechConverted;
        if (request != null) {
            try {
                IImsConfig imsConfig = getConfigInterface();
                Iterator it = request.getCapabilitiesToDisable().iterator();
                while (true) {
                    i = -1;
                    radioTechConverted = 1;
                    if (!it.hasNext()) {
                        break;
                    }
                    final CapabilityChangeRequest.CapabilityPair cap = (CapabilityChangeRequest.CapabilityPair) it.next();
                    CountDownLatch latch = new CountDownLatch(1);
                    int capConverted = convertCapability(cap.getCapability(), cap.getRadioTech());
                    int radioTechConverted2 = REG_TECH_TO_NET_TYPE.getOrDefault(Integer.valueOf(cap.getRadioTech()), -1).intValue();
                    if (DEBUG) {
                        log("changeEnabledCapabilities - cap: " + capConverted + " radioTech: " + radioTechConverted2 + " disabled");
                    }
                    if (capConverted >= 0) {
                        int capConverted2 = capConverted;
                        final ImsFeature.CapabilityCallbackProxy capabilityCallbackProxy = c;
                        imsConfig.setFeatureValue(capConverted2, radioTechConverted2, 0, new ConfigListener(capConverted, radioTechConverted2, latch) {
                            public void setFeatureValueReceived(int value) {
                                if (value != 0) {
                                    ImsFeature.CapabilityCallbackProxy capabilityCallbackProxy = capabilityCallbackProxy;
                                    if (capabilityCallbackProxy != null) {
                                        capabilityCallbackProxy.onChangeCapabilityConfigurationError(cap.getCapability(), cap.getRadioTech(), -1);
                                    } else {
                                        return;
                                    }
                                }
                                if (MtkMmTelFeature.DEBUG) {
                                    MtkMmTelFeature mtkMmTelFeature = MtkMmTelFeature.this;
                                    mtkMmTelFeature.log("changeEnabledCapabilities - setFeatureValueReceived with value " + value);
                                }
                            }
                        });
                        latch.await(2000, TimeUnit.MILLISECONDS);
                    }
                }
                for (final CapabilityChangeRequest.CapabilityPair cap2 : request.getCapabilitiesToEnable()) {
                    CountDownLatch latch2 = new CountDownLatch(radioTechConverted);
                    int capConverted3 = convertCapability(cap2.getCapability(), cap2.getRadioTech());
                    int radioTechConverted3 = REG_TECH_TO_NET_TYPE.getOrDefault(Integer.valueOf(cap2.getRadioTech()), Integer.valueOf(i)).intValue();
                    if (DEBUG) {
                        log("changeEnabledCapabilities - cap: " + capConverted3 + " radioTech: " + radioTechConverted3 + " enabled");
                    }
                    if (capConverted3 >= 0) {
                        C01344 r14 = r1;
                        int radioTechConverted4 = radioTechConverted3;
                        final ImsFeature.CapabilityCallbackProxy capabilityCallbackProxy2 = c;
                        C01344 r1 = new ConfigListener(capConverted3, radioTechConverted3, latch2) {
                            public void setFeatureValueReceived(int value) {
                                if (value != 1) {
                                    ImsFeature.CapabilityCallbackProxy capabilityCallbackProxy = capabilityCallbackProxy2;
                                    if (capabilityCallbackProxy != null) {
                                        capabilityCallbackProxy.onChangeCapabilityConfigurationError(cap2.getCapability(), cap2.getRadioTech(), -1);
                                    } else {
                                        return;
                                    }
                                }
                                if (MtkMmTelFeature.DEBUG) {
                                    MtkMmTelFeature mtkMmTelFeature = MtkMmTelFeature.this;
                                    mtkMmTelFeature.log("changeEnabledCapabilities - setFeatureValueReceived with value " + value);
                                }
                            }
                        };
                        imsConfig.setFeatureValue(capConverted3, radioTechConverted4, 1, r14);
                        latch2.await(2000, TimeUnit.MILLISECONDS);
                        radioTechConverted = 1;
                        i = -1;
                    }
                }
            } catch (RemoteException | InterruptedException e) {
                log("changeEnabledCapabilities: Error processing: " + e.getMessage());
            }
        }
    }

    public ImsCallProfile createCallProfile(int callSessionType, int callType) {
        log("createCallProfile: callSessionType = " + callSessionType + ",  callType = " + callType);
        ImsService imsService = this.mImsServiceImpl;
        if (imsService != null) {
            return imsService.onCreateCallProfile(this.mSlotId, callSessionType, callType);
        }
        return null;
    }

    public ImsCallSessionImplBase createCallSession(ImsCallProfile profile) {
        log("createCallSession");
        ImsService imsService = this.mImsServiceImpl;
        if (imsService != null) {
            return imsService.onCreateCallSessionProxy(this.mSlotId, profile, (IImsCallSessionListener) null);
        }
        return null;
    }

    public int shouldProcessCall(String[] numbers) {
        log("shouldProcessCall");
        return 0;
    }

    /* access modifiers changed from: protected */
    public IImsUt getUtInterface() throws RemoteException {
        ImsUtImplBase utImpl = getUt();
        if (utImpl != null) {
            return utImpl.getInterface();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public IImsEcbm getEcbmInterface() throws RemoteException {
        log("getEcbmInterface");
        ImsEcbmImplBase ecbm = getEcbm();
        if (ecbm != null) {
            return ecbm.getImsEcbm();
        }
        return null;
    }

    public IImsMultiEndpoint getMultiEndpointInterface() throws RemoteException {
        log("getMultiEndpointInterface");
        ImsMultiEndpointImplBase multiendpoint = getMultiEndpoint();
        if (multiendpoint != null) {
            return multiendpoint.getIImsMultiEndpoint();
        }
        return null;
    }

    public ImsUtImplBase getUt() {
        if (ImsCommonUtil.supportMdAutoSetupIms()) {
            return ImsUtImpl.getInstance(this.mContext, this.mSlotId, this.mImsServiceImpl);
        }
        return ExtensionFactory.makeLegacyComponentFactory(this.mContext).makeImsUt(this.mContext, this.mSlotId, this.mImsServiceImpl);
    }

    public ImsEcbmImplBase getEcbm() {
        log("getEcbm");
        ImsService imsService = this.mImsServiceImpl;
        if (imsService != null) {
            return imsService.onGetEcbmProxy(this.mSlotId);
        }
        return null;
    }

    public ImsMultiEndpointImplBase getMultiEndpoint() {
        log("getMultiEndpoint");
        ImsService imsService = this.mImsServiceImpl;
        if (imsService != null) {
            return imsService.onGetMultiEndpointProxy(this.mSlotId);
        }
        return null;
    }

    public void setUiTtyMode(int mode, Message onCompleteMessage) {
    }

    public IImsConfig getConfigInterface() {
        ImsService imsService = this.mImsServiceImpl;
        if (imsService != null) {
            return imsService.onGetConfigInterface(this.mSlotId);
        }
        return null;
    }

    public ImsSmsImplBase getSmsImplementation() {
        return MtkImsSmsImpl.getInstance(this.mContext, this.mSlotId, this.mImsServiceImpl);
    }

    private String getSmsFormat() {
        return getSmsImplementation().getSmsFormat();
    }

    public void onFeatureRemoved() {
    }

    public void onFeatureReady() {
        log("onFeatureReady called!");
    }

    public void enableIms(int slotId) {
        ImsService imsService = this.mImsServiceImpl;
        if (imsService != null) {
            imsService.enableIms(slotId);
        }
    }

    public void disableIms(int slotId) {
        ImsService imsService = this.mImsServiceImpl;
        if (imsService != null) {
            imsService.disableIms(slotId);
        }
    }

    public void close() {
        ImsService imsService = this.mImsServiceImpl;
        if (imsService != null) {
            imsService.setMmTelFeatureCallback(this.mSlotId, (ImsService.IMtkMmTelFeatureCallback) null);
            logi("Unregister callback from ImsService");
        }
    }

    /* access modifiers changed from: private */
    public void log(String msg) {
        Rlog.d(LOG_TAG, "[" + this.mSlotId + "] " + msg);
    }

    private void logi(String msg) {
        Rlog.i(LOG_TAG, "[" + this.mSlotId + "] " + msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, "[" + this.mSlotId + "] " + msg);
    }
}
