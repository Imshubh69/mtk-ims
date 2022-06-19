package com.mediatek.ims.ril;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Registrant;
import android.os.RegistrantList;

public abstract class ImsBaseCommands implements ImsCommandsInterface {
    protected RegistrantList mAvailRegistrants = new RegistrantList();
    protected RegistrantList mBearerInitRegistrants = new RegistrantList();
    protected RegistrantList mBearerStateRegistrants = new RegistrantList();
    protected RegistrantList mCallAdditionalInfoRegistrants = new RegistrantList();
    protected RegistrantList mCallInfoRegistrants = new RegistrantList();
    protected RegistrantList mCallModeChangeIndicatorRegistrants = new RegistrantList();
    protected RegistrantList mCallProgressIndicatorRegistrants = new RegistrantList();
    protected RegistrantList mCallRatIndicationRegistrants = new RegistrantList();
    protected Registrant mCdmaSmsRegistrant;
    protected Context mContext;
    protected RegistrantList mEconfResultRegistrants = new RegistrantList();
    protected RegistrantList mEctResultRegistrants = new RegistrantList();
    protected RegistrantList mEiregIndRegistrants = new RegistrantList();
    protected RegistrantList mEnterECBMRegistrants = new RegistrantList();
    protected RegistrantList mEregrtIndRegistrants = new RegistrantList();
    protected RegistrantList mExitECBMRegistrants = new RegistrantList();
    protected RegistrantList mImsCfgConfigChangedRegistrants = new RegistrantList();
    protected RegistrantList mImsCfgConfigLoadedRegistrants = new RegistrantList();
    protected RegistrantList mImsCfgDynamicImsSwitchCompleteRegistrants = new RegistrantList();
    protected RegistrantList mImsCfgFeatureChangedRegistrants = new RegistrantList();
    protected RegistrantList mImsConfInfoRegistrants = new RegistrantList();
    protected RegistrantList mImsDataInfoNotifyRegistrants = new RegistrantList();
    protected RegistrantList mImsDeregistrationDoneRegistrants = new RegistrantList();
    protected RegistrantList mImsDialogRegistrant = new RegistrantList();
    protected RegistrantList mImsDisableDoneRegistrants = new RegistrantList();
    protected RegistrantList mImsDisableStartRegistrants = new RegistrantList();
    protected RegistrantList mImsEccSupportRegistrants = new RegistrantList();
    protected RegistrantList mImsEnableDoneRegistrants = new RegistrantList();
    protected RegistrantList mImsEnableStartRegistrants = new RegistrantList();
    protected RegistrantList mImsEvtPkgRegistrants = new RegistrantList();
    protected RegistrantList mImsGetProvisionDoneRegistrants = new RegistrantList();
    protected RegistrantList mImsRedialEccIndRegistrants = new RegistrantList();
    protected RegistrantList mImsRegFlagIndRegistrants = new RegistrantList();
    protected RegistrantList mImsRegStatusIndRistrants = new RegistrantList();
    protected RegistrantList mImsRegistrationInfoRegistrants = new RegistrantList();
    protected RegistrantList mImsSipHeaderRegistrants = new RegistrantList();
    protected RegistrantList mIncomingCallIndicationRegistrants = new RegistrantList();
    protected RegistrantList mLteMsgWaitingRegistrants = new RegistrantList();
    protected RegistrantList mMDInternetUsageRegistrants = new RegistrantList();
    protected Registrant mNewSmsRegistrant;
    protected RegistrantList mNoECBMRegistrants = new RegistrantList();
    protected RegistrantList mNotAvailRegistrants = new RegistrantList();
    protected RegistrantList mOffOrNotAvailRegistrants = new RegistrantList();
    protected RegistrantList mOffRegistrants = new RegistrantList();
    protected RegistrantList mOnRegistrants = new RegistrantList();
    protected int mPhoneId;
    protected RegistrantList mRTPInfoRegistrants = new RegistrantList();
    protected RegistrantList mRadioStateChangedRegistrants = new RegistrantList();
    protected RegistrantList mRttAudioIndicatorRegistrants = new RegistrantList();
    protected RegistrantList mRttCapabilityIndicatorRegistrants = new RegistrantList();
    protected RegistrantList mRttModifyRequestReceiveRegistrants = new RegistrantList();
    protected RegistrantList mRttModifyResponseRegistrants = new RegistrantList();
    protected RegistrantList mRttTextReceiveRegistrants = new RegistrantList();
    protected Registrant mSmsStatusRegistrant;
    protected RegistrantList mSpeechCodecInfoRegistrant = new RegistrantList();
    protected RegistrantList mSsacIndRegistrants = new RegistrantList();
    protected int mState = 2;
    protected Object mStateMonitor = new Object();
    protected RegistrantList mSuppServiceNotificationRegistrants = new RegistrantList();
    protected RegistrantList mUSSIRegistrants = new RegistrantList();
    protected RegistrantList mVideoCapabilityIndicatorRegistrants = new RegistrantList();
    protected RegistrantList mVideoRingtoneRegistrants = new RegistrantList();
    protected RegistrantList mVolteSettingRegistrants = new RegistrantList();
    protected Object mVolteSettingValue = null;
    protected RegistrantList mVolteSubscriptionRegistrants = new RegistrantList();
    protected RegistrantList mVopsStatusIndRegistrants = new RegistrantList();
    protected RegistrantList mXuiRegistrants = new RegistrantList();

    public ImsBaseCommands(Context context, int phoneid) {
        this.mContext = context;
        this.mPhoneId = phoneid;
    }

    public void setOnIncomingCallIndication(Handler h, int what, Object obj) {
        this.mIncomingCallIndicationRegistrants.add(new Registrant(h, what, obj));
    }

    public void unsetOnIncomingCallIndication(Handler h) {
        this.mIncomingCallIndicationRegistrants.remove(h);
    }

    public void registerForCallAdditionalInfo(Handler h, int what, Object obj) {
        this.mCallAdditionalInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallAdditionalInfo(Handler h) {
        this.mCallAdditionalInfoRegistrants.remove(h);
    }

    public void registerForCallRatIndication(Handler h, int what, Object obj) {
        this.mCallRatIndicationRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallRatIndication(Handler h) {
        this.mCallRatIndicationRegistrants.remove(h);
    }

    public void registerForEconfResult(Handler h, int what, Object obj) {
        this.mEconfResultRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForEconfResult(Handler h) {
        this.mEconfResultRegistrants.remove(h);
    }

    public void registerForCallInfo(Handler h, int what, Object obj) {
        this.mCallInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallInfo(Handler h) {
        this.mCallInfoRegistrants.remove(h);
    }

    public void registerForImsEnableStart(Handler h, int what, Object obj) {
        this.mImsEnableStartRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsEnableStart(Handler h) {
        this.mImsEnableStartRegistrants.remove(h);
    }

    public void registerForImsDisableStart(Handler h, int what, Object obj) {
        this.mImsDisableStartRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsDisableStart(Handler h) {
        this.mImsDisableStartRegistrants.remove(h);
    }

    public void registerForImsEnableComplete(Handler h, int what, Object obj) {
        this.mImsEnableDoneRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsEnableComplete(Handler h) {
        this.mImsEnableDoneRegistrants.remove(h);
    }

    public void registerForImsDisableComplete(Handler h, int what, Object obj) {
        this.mImsDisableDoneRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsDisableComplete(Handler h) {
        this.mImsDisableDoneRegistrants.remove(h);
    }

    public void registerForImsRegistrationInfo(Handler h, int what, Object obj) {
        this.mImsRegistrationInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsRegistrationInfo(Handler h) {
        this.mImsRegistrationInfoRegistrants.remove(h);
    }

    public void registerForCallProgressIndicator(Handler h, int what, Object obj) {
        this.mCallProgressIndicatorRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallProgressIndicator(Handler h) {
        this.mCallProgressIndicatorRegistrants.remove(h);
    }

    public void registerForOnEnterECBM(Handler h, int what, Object obj) {
        this.mEnterECBMRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForOnEnterECBM(Handler h) {
        this.mEnterECBMRegistrants.remove(h);
    }

    public void registerForOnExitECBM(Handler h, int what, Object obj) {
        this.mExitECBMRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForOnExitECBM(Handler h) {
        this.mExitECBMRegistrants.remove(h);
    }

    public void registerForOnNoECBM(Handler h, int what, Object obj) {
        this.mNoECBMRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForOnNoECBM(Handler h) {
        this.mNoECBMRegistrants.remove(h);
    }

    public void registerForGetProvisionComplete(Handler h, int what, Object obj) {
        this.mImsGetProvisionDoneRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForGetProvisionComplete(Handler h) {
        this.mImsGetProvisionDoneRegistrants.remove(h);
    }

    public void registerForEctResult(Handler h, int what, Object obj) {
        this.mEctResultRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForEctResult(Handler h) {
        this.mEctResultRegistrants.remove(h);
    }

    public void registerForCallModeChangeIndicator(Handler h, int what, Object obj) {
        this.mCallModeChangeIndicatorRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallModeChangeIndicator(Handler h) {
        this.mCallModeChangeIndicatorRegistrants.remove(h);
    }

    public void registerForVideoCapabilityIndicator(Handler h, int what, Object obj) {
        this.mVideoCapabilityIndicatorRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVideoCapabilityIndicator(Handler h) {
        this.mVideoCapabilityIndicatorRegistrants.remove(h);
    }

    public void registerForImsRTPInfo(Handler h, int what, Object obj) {
        this.mRTPInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsRTPInfo(Handler h) {
        this.mRTPInfoRegistrants.remove(h);
    }

    public void registerForVolteSettingChanged(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVolteSettingRegistrants.add(r);
        if (this.mVolteSettingValue != null) {
            r.notifyRegistrant(new AsyncResult((Object) null, this.mVolteSettingValue, (Throwable) null));
        }
    }

    public void unregisterForVolteSettingChanged(Handler h) {
        this.mVolteSettingRegistrants.remove(h);
    }

    public void registerForImsRegFlagInd(Handler h, int what, Object obj) {
        this.mImsRegFlagIndRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsRegFlagInd(Handler h) {
        this.mImsRegFlagIndRegistrants.remove(h);
    }

    public void registerForBearerState(Handler h, int what, Object obj) {
        this.mBearerStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForBearerState(Handler h) {
        this.mBearerStateRegistrants.remove(h);
    }

    public void registerForImsDataInfoNotify(Handler h, int what, Object obj) {
        this.mImsDataInfoNotifyRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsDataInfoNotify(Handler h) {
        this.mImsDataInfoNotifyRegistrants.remove(h);
    }

    public void registerForXuiInfo(Handler h, int what, Object obj) {
        this.mXuiRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForXuiInfo(Handler h) {
        this.mXuiRegistrants.remove(h);
    }

    public void registerForVolteSubscription(Handler h, int what, Object obj) {
        this.mVolteSubscriptionRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVolteSubscription(Handler h) {
        this.mVolteSubscriptionRegistrants.remove(h);
    }

    public void setOnSuppServiceNotification(Handler h, int what, Object obj) {
        this.mSuppServiceNotificationRegistrants.add(new Registrant(h, what, obj));
    }

    public void unSetOnSuppServiceNotification(Handler h) {
        this.mSuppServiceNotificationRegistrants.remove(h);
    }

    public void registerForImsEventPackage(Handler h, int what, Object obj) {
        this.mImsEvtPkgRegistrants.add(new Registrant(h, what, obj));
    }

    public void setOnUSSI(Handler h, int what, Object obj) {
        this.mUSSIRegistrants.add(new Registrant(h, what, obj));
    }

    public void unSetOnUSSI(Handler h) {
        this.mUSSIRegistrants.remove(h);
    }

    public void unregisterForImsEventPackage(Handler h) {
        this.mImsEvtPkgRegistrants.remove(h);
    }

    public void registerForBearerInit(Handler h, int what, Object obj) {
        this.mBearerInitRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForBearerInit(Handler h) {
        this.mBearerInitRegistrants.remove(h);
    }

    public void registerForNotAvailable(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mNotAvailRegistrants.add(r);
            if (this.mState == 2) {
                r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        }
    }

    public void unregisterForNotAvailable(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mNotAvailRegistrants.remove(h);
        }
    }

    public void registerForOff(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mOffRegistrants.add(r);
            if (this.mState == 0) {
                r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        }
    }

    public void unregisterForOff(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mOffRegistrants.remove(h);
        }
    }

    public void registerForOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mOnRegistrants.add(r);
            if (this.mState == 1) {
                r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        }
    }

    public void unregisterForOn(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mOnRegistrants.remove(h);
        }
    }

    public void registerForImsDeregisterComplete(Handler h, int what, Object obj) {
        this.mImsDeregistrationDoneRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsDeregisterComplete(Handler h) {
        this.mImsDeregistrationDoneRegistrants.remove(h);
    }

    public void registerForImsEccSupport(Handler h, int what, Object obj) {
        this.mImsEccSupportRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsEccSupport(Handler h) {
        this.mImsEccSupportRegistrants.remove(h);
    }

    public void registerForSpeechCodecInfo(Handler h, int what, Object obj) {
        this.mSpeechCodecInfoRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForSpeechCodecInfo(Handler h) {
        this.mSpeechCodecInfoRegistrant.remove(h);
    }

    public void registerForImsConfInfoUpdate(Handler h, int what, Object obj) {
        this.mImsConfInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsConfInfoUpdate(Handler h) {
        this.mImsConfInfoRegistrants.remove(h);
    }

    public void registerForLteMsgWaiting(Handler h, int what, Object obj) {
        this.mLteMsgWaitingRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForLteMsgWaiting(Handler h) {
        this.mLteMsgWaitingRegistrants.remove(h);
    }

    public void registerForImsDialog(Handler h, int what, Object obj) {
        this.mImsDialogRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsDialog(Handler h) {
        this.mImsDialogRegistrant.remove(h);
    }

    public void registerForImsCfgDynamicImsSwitchComplete(Handler h, int what, Object obj) {
        this.mImsCfgDynamicImsSwitchCompleteRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsCfgDynamicImsSwitchComplete(Handler h) {
        this.mImsCfgDynamicImsSwitchCompleteRegistrants.remove(h);
    }

    public void registerForImsCfgFeatureChanged(Handler h, int what, Object obj) {
        this.mImsCfgFeatureChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsCfgFeatureChanged(Handler h) {
        this.mImsCfgFeatureChangedRegistrants.remove(h);
    }

    public void registerForImsCfgConfigChanged(Handler h, int what, Object obj) {
        this.mImsCfgConfigChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsCfgConfigChanged(Handler h) {
        this.mImsCfgConfigChangedRegistrants.remove(h);
    }

    public void registerForImsCfgConfigLoaded(Handler h, int what, Object obj) {
        this.mImsCfgConfigLoadedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsCfgConfigLoaded(Handler h) {
        this.mImsCfgConfigLoadedRegistrants.remove(h);
    }

    public void setOnSmsStatus(Handler h, int what, Object obj) {
        this.mSmsStatusRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnSmsStatus(Handler h) {
        Registrant registrant = this.mSmsStatusRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mSmsStatusRegistrant.clear();
            this.mSmsStatusRegistrant = null;
        }
    }

    public void setOnNewSms(Handler h, int what, Object obj) {
        this.mNewSmsRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnNewSms(Handler h) {
        Registrant registrant = this.mNewSmsRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mNewSmsRegistrant.clear();
            this.mNewSmsRegistrant = null;
        }
    }

    public void setOnNewCdmaSms(Handler h, int what, Object obj) {
        this.mCdmaSmsRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnNewCdmaSms(Handler h) {
        Registrant registrant = this.mCdmaSmsRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mCdmaSmsRegistrant.clear();
            this.mCdmaSmsRegistrant = null;
        }
    }

    public void registerForImsRedialEccInd(Handler h, int what, Object obj) {
        this.mImsRedialEccIndRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsRedialEccInd(Handler h) {
        this.mImsRedialEccIndRegistrants.remove(h);
    }

    /* access modifiers changed from: protected */
    public void onRadioAvailable() {
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0062, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setRadioState(int r6) {
        /*
            r5 = this;
            java.lang.Object r0 = r5.mStateMonitor
            monitor-enter(r0)
            int r1 = r5.mState     // Catch:{ all -> 0x0063 }
            r5.mState = r6     // Catch:{ all -> 0x0063 }
            if (r1 != r6) goto L_0x0023
            java.lang.String r2 = "ImsBaseCommands"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0063 }
            r3.<init>()     // Catch:{ all -> 0x0063 }
            java.lang.String r4 = "no state transition: "
            r3.append(r4)     // Catch:{ all -> 0x0063 }
            int r4 = r5.mState     // Catch:{ all -> 0x0063 }
            r3.append(r4)     // Catch:{ all -> 0x0063 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0063 }
            android.telephony.Rlog.d(r2, r3)     // Catch:{ all -> 0x0063 }
            monitor-exit(r0)     // Catch:{ all -> 0x0063 }
            return
        L_0x0023:
            android.os.RegistrantList r2 = r5.mRadioStateChangedRegistrants     // Catch:{ all -> 0x0063 }
            r2.notifyRegistrants()     // Catch:{ all -> 0x0063 }
            int r2 = r5.mState     // Catch:{ all -> 0x0063 }
            r3 = 2
            if (r2 == r3) goto L_0x0034
            if (r1 != r3) goto L_0x0034
            android.os.RegistrantList r2 = r5.mAvailRegistrants     // Catch:{ all -> 0x0063 }
            r2.notifyRegistrants()     // Catch:{ all -> 0x0063 }
        L_0x0034:
            int r2 = r5.mState     // Catch:{ all -> 0x0063 }
            if (r2 != r3) goto L_0x003f
            if (r1 == r3) goto L_0x003f
            android.os.RegistrantList r2 = r5.mNotAvailRegistrants     // Catch:{ all -> 0x0063 }
            r2.notifyRegistrants()     // Catch:{ all -> 0x0063 }
        L_0x003f:
            int r2 = r5.mState     // Catch:{ all -> 0x0063 }
            r4 = 1
            if (r2 != r4) goto L_0x004b
            if (r1 == r4) goto L_0x004b
            android.os.RegistrantList r2 = r5.mOnRegistrants     // Catch:{ all -> 0x0063 }
            r2.notifyRegistrants()     // Catch:{ all -> 0x0063 }
        L_0x004b:
            int r2 = r5.mState     // Catch:{ all -> 0x0063 }
            if (r2 == 0) goto L_0x0051
            if (r2 != r3) goto L_0x0058
        L_0x0051:
            if (r1 != r4) goto L_0x0058
            android.os.RegistrantList r2 = r5.mOffOrNotAvailRegistrants     // Catch:{ all -> 0x0063 }
            r2.notifyRegistrants()     // Catch:{ all -> 0x0063 }
        L_0x0058:
            int r2 = r5.mState     // Catch:{ all -> 0x0063 }
            if (r2 != 0) goto L_0x0061
            android.os.RegistrantList r2 = r5.mOffRegistrants     // Catch:{ all -> 0x0063 }
            r2.notifyRegistrants()     // Catch:{ all -> 0x0063 }
        L_0x0061:
            monitor-exit(r0)     // Catch:{ all -> 0x0063 }
            return
        L_0x0063:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0063 }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.ril.ImsBaseCommands.setRadioState(int):void");
    }

    /* access modifiers changed from: protected */
    public void notifyRadioStateChanged(int newState) {
    }

    public void registerForRttCapabilityIndicator(Handler h, int what, Object obj) {
        this.mRttCapabilityIndicatorRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRttCapabilityIndicator(Handler h) {
        this.mRttCapabilityIndicatorRegistrants.remove(h);
    }

    public void registerForRttModifyResponse(Handler h, int what, Object obj) {
        this.mRttModifyResponseRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRttModifyResponse(Handler h) {
        this.mRttModifyResponseRegistrants.remove(h);
    }

    public void registerForRttTextReceive(Handler h, int what, Object obj) {
        this.mRttTextReceiveRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRttTextReceive(Handler h) {
        this.mRttTextReceiveRegistrants.remove(h);
    }

    public void registerForRttModifyRequestReceive(Handler h, int what, Object obj) {
        this.mRttModifyRequestReceiveRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRttModifyRequestReceive(Handler h) {
        this.mRttModifyRequestReceiveRegistrants.remove(h);
    }

    public void registerForRttAudioIndicator(Handler h, int what, Object obj) {
        this.mRttAudioIndicatorRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRttAudioIndicator(Handler h) {
        this.mRttAudioIndicatorRegistrants.remove(h);
    }

    public void registerForVopsStatusInd(Handler h, int what, Object obj) {
        this.mVopsStatusIndRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVopsStatusInd(Handler h) {
        this.mVopsStatusIndRegistrants.remove(h);
    }

    public void registerForImsRegStatusInd(Handler h, int what, Object obj) {
        this.mImsRegStatusIndRistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsRegStatusInd(Handler h) {
        this.mImsRegStatusIndRistrants.remove(h);
    }

    public void registerForSipHeaderInd(Handler h, int what, Object obj) {
        this.mImsSipHeaderRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForSipHeaderInd(Handler h) {
        this.mImsSipHeaderRegistrants.remove(h);
    }

    public void registerForDetailImsRegistrationInd(Handler h, int what, Object obj) {
        this.mEiregIndRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForDetailImsRegistrationInd(Handler h) {
        this.mEiregIndRegistrants.remove(h);
    }

    public void registerForSsacStateInd(Handler h, int what, Object obj) {
        this.mSsacIndRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForSsacStateInd(Handler h) {
        this.mSsacIndRegistrants.remove(h);
    }

    public void registerForEregrtInd(Handler h, int what, Object obj) {
        this.mEregrtIndRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForEregrtInd(Handler h) {
        this.mEregrtIndRegistrants.remove(h);
    }

    public void registerForVideoRingtoneInfo(Handler h, int what, Object obj) {
        this.mVideoRingtoneRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVideoRingtoneInfo(Handler h) {
        this.mVideoRingtoneRegistrants.remove(h);
    }

    public void registerForMDInternetUsage(Handler h, int what, Object obj) {
        this.mMDInternetUsageRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForMDInternetUsage(Handler h) {
        this.mMDInternetUsageRegistrants.remove(h);
    }
}
