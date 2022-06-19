package com.mediatek.ims;

import android.content.Context;
import android.os.Build;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.aidl.IImsCallSessionListener;
import android.telephony.ims.aidl.IImsSmsListener;
import android.telephony.ims.feature.CapabilityChangeRequest;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsRegistrationListener;
import com.mediatek.gba.NafSessionKey;
import com.mediatek.ims.internal.IMtkImsCallSession;
import com.mediatek.ims.internal.IMtkImsConfig;
import com.mediatek.ims.internal.IMtkImsRegistrationListener;
import com.mediatek.ims.internal.IMtkImsService;
import com.mediatek.ims.internal.IMtkImsUt;
import com.mediatek.ims.internal.ImsVTProviderUtil;
import java.util.HashMap;
import java.util.Map;

public class MtkImsService extends IMtkImsService.Stub {
    private static final boolean DBG = true;
    private static final boolean ENGLOAD = "eng".equals(Build.TYPE);
    private static final String LOG_TAG = "MtkImsService";
    private Context mContext;
    private ImsService mImsService = null;

    public MtkImsService(Context context, ImsService imsService) {
        this.mImsService = imsService;
        this.mContext = context;
        log("init");
    }

    public void setCallIndication(int phoneId, String callId, String callNum, int seqNum, String toNumber, boolean isAllow, int cause) {
        this.mImsService.onSetCallIndication(phoneId, callId, callNum, seqNum, toNumber, isAllow, cause);
    }

    public void UpdateImsState(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "UpdateImsState");
        this.mImsService.onUpdateImsSate(phoneId);
    }

    public int getImsState(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "getImsState");
        return this.mImsService.getImsState(phoneId);
    }

    public int getImsRegUriType(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "getImsRegUriType");
        return this.mImsService.getImsRegUriType(phoneId);
    }

    public void hangupAllCall(int phoneId) {
        this.mImsService.onHangupAllCall(phoneId);
    }

    public void deregisterIms(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "deregisterIms");
        this.mImsService.deregisterIms(phoneId);
    }

    public void updateRadioState(int radioState, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "updateRadioState");
        this.mImsService.updateRadioState(radioState, phoneId);
    }

    public IMtkImsConfig getConfigInterfaceEx(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "getConfigInterfaceEx");
        this.mImsService.bindAndRegisterWifiOffloadService();
        return this.mImsService.getImsConfigManager().getEx(phoneId);
    }

    public IMtkImsCallSession createMtkCallSession(int phoneId, ImsCallProfile profile, IImsCallSessionListener listener, IImsCallSession aospCallSessionImpl) {
        return this.mImsService.onCreateMtkCallSession(phoneId, profile, listener, aospCallSessionImpl);
    }

    public IMtkImsCallSession getPendingMtkCallSession(int phoneId, String callId) {
        return this.mImsService.onGetPendingMtkCallSession(phoneId, callId);
    }

    public IMtkImsUt getMtkUtInterface(int phoneId) {
        return this.mImsService.onGetMtkUtInterface(phoneId);
    }

    public NafSessionKey runGbaAuthentication(String nafFqdn, byte[] nafSecureProtocolId, boolean forceRun, int netId, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "runGbaAuthentication");
        return this.mImsService.onRunGbaAuthentication(nafFqdn, nafSecureProtocolId, forceRun, netId, phoneId);
    }

    public int getModemMultiImsCount() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "getModemMultiImsCount");
        return this.mImsService.getModemMultiImsCount();
    }

    public int getCurrentCallCount(int phoneId) {
        return this.mImsService.getCurrentCallCount(phoneId);
    }

    public int[] getImsNetworkState(int capability) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "getImsNetworkState");
        return this.mImsService.getImsNetworkState(capability);
    }

    public boolean isCameraAvailable() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CAMERA", "isCameraAvailable");
        return ImsVTProviderUtil.isCameraAvailable();
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private void englog(String s) {
        if (ENGLOAD) {
            log(s);
        }
    }

    private void logw(String s) {
        Rlog.w(LOG_TAG, s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }

    public void addImsSmsListener(int phoneId, IImsSmsListener listener) {
        this.mContext.enforceCallingPermission("android.permission.SEND_SMS", "addImsSmsListener");
        this.mImsService.onAddImsSmsListener(phoneId, listener);
    }

    public void sendSms(int phoneId, int token, int messageRef, String format, String smsc, boolean isRetry, byte[] pdu) {
        this.mContext.enforceCallingPermission("android.permission.SEND_SMS", "sendSms");
        this.mImsService.sendSms(phoneId, token, messageRef, format, smsc, isRetry, pdu);
    }

    public void registerProprietaryImsListener(int phoneId, IImsRegistrationListener listener, IMtkImsRegistrationListener mtklistener, boolean notifyOnly) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "registerProprietaryImsListener");
        this.mImsService.onAddRegistrationListener(phoneId, 1, listener, mtklistener, notifyOnly);
    }

    public void setMTRedirect(int phoneId, boolean enable) {
        this.mImsService.setMTRedirect(phoneId, enable);
    }

    public void fallBackAospMTFlow(int phoneId) {
        this.mImsService.fallBackAospMTFlow(phoneId);
    }

    public void setSipHeader(int phoneId, Map extraHeaders, String fromUri) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "changeEnabledCapabilities");
        this.mImsService.setSipHeader(phoneId, (HashMap) extraHeaders, fromUri);
    }

    public void changeEnabledCapabilities(int phoneId, CapabilityChangeRequest request) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "changeEnabledCapabilities");
        this.mImsService.changeEnabledCapabilities(phoneId, request);
    }

    public void setWfcRegErrorCode(int phoneId, int errorCode) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", "setWfcRegErrorCode");
        this.mImsService.setWfcRegErrorCode(phoneId, errorCode);
    }

    public int getWfcRegErrorCode(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "getWfcRegErrorCode");
        return this.mImsService.getWfcRegErrorCode(phoneId);
    }

    public void setImsPreCallInfo(int phoneId, int mode, String address, String fromUri, Map extraHeaders, String[] location) {
        this.mImsService.setImsPreCallInfo(phoneId, mode, address, fromUri, (HashMap) extraHeaders, location);
    }
}
