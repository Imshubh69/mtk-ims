package com.mediatek.ims.ril;

import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.hardware.radio.V1_0.SuppSvcNotification;
import android.os.AsyncResult;
import android.telephony.Rlog;
import android.telephony.SmsMessage;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.cdma.SmsMessageConverter;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.mediatek.ims.ImsCallSessionProxy;
import com.mediatek.ims.ImsRegInfo;
import com.mediatek.ims.ImsServiceCallTracker;
import java.util.ArrayList;
import java.util.Iterator;
import vendor.mediatek.hardware.mtkradioex.V3_0.Dialog;
import vendor.mediatek.hardware.mtkradioex.V3_0.ImsConfParticipant;
import vendor.mediatek.hardware.mtkradioex.V3_0.ImsRegStatusInfo;
import vendor.mediatek.hardware.mtkradioex.V3_0.IncomingCallNotification;

public class ImsRadioIndication extends ImsRadioIndicationBase {
    private static final int INVALID_CALL_MODE = 255;
    private int mPhoneId;
    private ImsRILAdapter mRil;

    ImsRadioIndication(ImsRILAdapter ril, int phoneId) {
        this.mRil = ril;
        this.mPhoneId = phoneId;
    }

    public void noEmergencyCallbackMode(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(ImsRILConstants.RIL_UNSOL_NO_EMERGENCY_CALLBACK_MODE);
        if (this.mRil.mNoECBMRegistrants != null) {
            this.mRil.mNoECBMRegistrants.notifyRegistrants();
        }
    }

    public void videoCapabilityIndicator(int type, String callId, String localVideoCap, String remoteVideoCap) {
        this.mRil.processIndication(type);
        String[] ret = {callId, localVideoCap, remoteVideoCap};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_VIDEO_CAPABILITY_INDICATOR, ret);
        if (this.mRil.mVideoCapabilityIndicatorRegistrants != null) {
            this.mRil.mVideoCapabilityIndicatorRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void callmodChangeIndicator(int type, String callId, String callMode, String videoState, String audioDirection, String pau) {
        this.mRil.processIndication(type);
        String[] ret = {callId, callMode, videoState, audioDirection, pau};
        ImsRILAdapter imsRILAdapter = this.mRil;
        imsRILAdapter.unsljLogRet(ImsRILConstants.RIL_UNSOL_CALLMOD_CHANGE_INDICATOR, ImsServiceCallTracker.sensitiveEncode("" + ret));
        if (this.mRil.mCallModeChangeIndicatorRegistrants != null) {
            this.mRil.mCallModeChangeIndicatorRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void volteSetting(int type, boolean isEnable) {
        this.mRil.processIndication(type);
        int[] ret = {isEnable, this.mPhoneId};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_VOLTE_SETTING, ret);
        this.mRil.mVolteSettingValue = ret;
        if (this.mRil.mVolteSettingRegistrants != null) {
            this.mRil.mVolteSettingRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void onXui(int type, String accountId, String broadcastFlag, String xuiInfo) {
        this.mRil.processIndication(type);
        String[] ret = {accountId, broadcastFlag, xuiInfo, Integer.toString(this.mPhoneId)};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_ON_XUI, Rlog.pii("IMS_RILA", ret));
        if (this.mRil.mXuiRegistrants != null) {
            this.mRil.mXuiRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void onVolteSubscription(int type, int status) {
        this.mRil.processIndication(type);
        int[] ret = {status, this.mPhoneId};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_ON_VOLTE_SUBSCRIPTION, ret);
        if (this.mRil.mVolteSubscriptionRegistrants != null) {
            this.mRil.mVolteSubscriptionRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void suppSvcNotify(int type, SuppSvcNotification data) {
        this.mRil.processIndication(type);
        SuppServiceNotification notification = new SuppServiceNotification();
        notification.notificationType = data.isMT ? 1 : 0;
        notification.code = data.code;
        notification.index = data.index;
        notification.type = data.type;
        notification.number = data.number;
        this.mRil.unsljLogRet(1011, data);
        if (this.mRil.mSuppServiceNotificationRegistrants != null) {
            this.mRil.mSuppServiceNotificationRegistrants.notifyRegistrants(new AsyncResult((Object) null, notification, (Throwable) null));
        }
    }

    public void ectIndication(int type, int callId, int ectResult, int cause) {
        this.mRil.processIndication(type);
        int[] ret = {callId, ectResult, cause};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_ECT_INDICATION, ret);
        if (this.mRil.mEctResultRegistrants != null) {
            this.mRil.mEctResultRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void onUssi(int type, int ussdModeType, String msg) {
        this.mRil.processIndication(type);
        String[] ret = {Integer.toString(ussdModeType), msg, Integer.toString(this.mPhoneId)};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_ON_USSI, ret);
        if (this.mRil.mUSSIRegistrants != null) {
            this.mRil.mUSSIRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void sipCallProgressIndicator(int type, String callId, String dir, String sipMsgType, String method, String responseCode, String reasonText) {
        this.mRil.processIndication(type);
        String[] ret = {callId, dir, sipMsgType, method, responseCode, reasonText};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_SIP_CALL_PROGRESS_INDICATOR, ret);
        if (this.mRil.mCallProgressIndicatorRegistrants != null) {
            this.mRil.mCallProgressIndicatorRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void econfResultIndication(int type, String confCallId, String op, String num, String result, String cause, String joinedCallId) {
        this.mRil.processIndication(type);
        String[] ret = {confCallId, op, num, result, cause, joinedCallId};
        ImsRILAdapter imsRILAdapter = this.mRil;
        imsRILAdapter.unsljLogRet(ImsRILConstants.RIL_UNSOL_ECONF_RESULT_INDICATION, ImsServiceCallTracker.sensitiveEncode("" + ret));
        if (this.mRil.mEconfResultRegistrants != null) {
            ImsRILAdapter imsRILAdapter2 = this.mRil;
            imsRILAdapter2.riljLog("ECONF result = " + ret[3]);
            this.mRil.mEconfResultRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void callInfoIndication(int indicationType, ArrayList<String> result) {
        this.mRil.processIndication(indicationType);
        if (result != null && result.size() != 0) {
            String[] callInfo = (String[]) result.toArray(new String[result.size()]);
            ImsRILAdapter imsRILAdapter = this.mRil;
            imsRILAdapter.unsljLogRet(ImsRILConstants.RIL_UNSOL_CALL_INFO_INDICATION, ImsServiceCallTracker.sensitiveEncode("" + callInfo));
            if (this.mRil.mCallInfoRegistrants != null) {
                this.mRil.mCallInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, callInfo, (Throwable) null));
            }
        }
    }

    public void incomingCallIndication(int type, IncomingCallNotification inCallNotify) {
        this.mRil.processIndication(type);
        String[] ret = {inCallNotify.callId, inCallNotify.number, inCallNotify.type, inCallNotify.callMode, inCallNotify.seqNo, inCallNotify.redirectNumber, inCallNotify.toNumber};
        ImsRILAdapter imsRILAdapter = this.mRil;
        imsRILAdapter.unsljLogRet(ImsRILConstants.RIL_UNSOL_INCOMING_CALL_INDICATION, ImsServiceCallTracker.sensitiveEncode("" + ret));
        if (this.mRil.mIncomingCallIndicationRegistrants != null) {
            this.mRil.mIncomingCallIndicationRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void getProvisionDone(int type, String result1, String result2) {
        this.mRil.processIndication(type);
        String[] ret = {result1, result2};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_GET_PROVISION_DONE, ret);
        if (this.mRil.mImsGetProvisionDoneRegistrants != null) {
            this.mRil.mImsGetProvisionDoneRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsRtpInfo(int type, String pdnId, String networkId, String timer, String sendPktLost, String recvPktLost, String jitter, String delay) {
        this.mRil.processIndication(type);
        String[] ret = {pdnId, networkId, timer, sendPktLost, recvPktLost, jitter, delay};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_RTP_INFO, ret);
        if (this.mRil.mRTPInfoRegistrants != null) {
            this.mRil.mRTPInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsEventPackageIndication(int type, String callId, String pType, String urcIdx, String totalUrcCount, String rawData) {
        this.mRil.processIndication(type);
        String[] ret = {callId, pType, urcIdx, totalUrcCount, rawData, Integer.toString(this.mPhoneId)};
        ImsRILAdapter imsRILAdapter = this.mRil;
        imsRILAdapter.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_EVENT_PACKAGE_INDICATION, ImsServiceCallTracker.sensitiveEncode("" + ret));
        if (this.mRil.mImsEvtPkgRegistrants != null) {
            this.mRil.mImsEvtPkgRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsRegistrationInfo(int type, int status, int capability) {
        this.mRil.processIndication(type);
        int[] ret = {status, capability, this.mPhoneId};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_REGISTRATION_INFO, ret);
        if (this.mRil.mImsRegistrationInfoRegistrants != null) {
            this.mRil.mImsRegistrationInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsEnableDone(int type) {
        this.mRil.processIndication(type);
        int[] ret = {this.mPhoneId};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_ENABLE_DONE, ret);
        if (this.mRil.mImsEnableDoneRegistrants != null) {
            this.mRil.mImsEnableDoneRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsDisableDone(int type) {
        this.mRil.processIndication(type);
        int[] ret = {this.mPhoneId};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_DISABLE_DONE, ret);
        if (this.mRil.mImsDisableDoneRegistrants != null) {
            this.mRil.mImsDisableDoneRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsEnableStart(int type) {
        this.mRil.processIndication(type);
        int[] ret = {this.mPhoneId};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_ENABLE_START, ret);
        if (this.mRil.mImsEnableStartRegistrants != null) {
            this.mRil.mImsEnableStartRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsDisableStart(int type) {
        this.mRil.processIndication(type);
        int[] ret = {this.mPhoneId};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_DISABLE_START, ret);
        if (this.mRil.mImsDisableStartRegistrants != null) {
            this.mRil.mImsDisableStartRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsBearerStateNotify(int type, int aid, int action, String capability) {
        this.mRil.processIndication(type);
        String[] ret = {String.valueOf(this.mPhoneId), String.valueOf(aid), String.valueOf(action), capability};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_BEARER_STATE_NOTIFY, ret);
        if (this.mRil.mBearerStateRegistrants != null) {
            this.mRil.mBearerStateRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsBearerInit(int type) {
        this.mRil.processIndication(type);
        int[] ret = {this.mPhoneId};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_BEARER_INIT, ret);
        if (this.mRil.mBearerInitRegistrants != null) {
            this.mRil.mBearerInitRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsDataInfoNotify(int type, String capability, String event, String extra) {
        this.mRil.processIndication(type);
        String[] ret = {String.valueOf(this.mPhoneId), capability, event, extra};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_DATA_INFO_NOTIFY, ret);
        if (this.mRil.mImsDataInfoNotifyRegistrants != null) {
            this.mRil.mImsDataInfoNotifyRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsDeregDone(int type) {
        this.mRil.processIndication(type);
        int[] ret = {this.mPhoneId};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_DEREG_DONE, ret);
        if (this.mRil.mImsDeregistrationDoneRegistrants != null) {
            this.mRil.mImsDeregistrationDoneRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsSupportEcc(int type, int supportLteEcc) {
        this.mRil.processIndication(type);
        int[] ret = {supportLteEcc, this.mPhoneId};
        ImsRILAdapter imsRILAdapter = this.mRil;
        imsRILAdapter.riljLog(" ImsRILConstants.RIL_UNSOL_IMS_ECC_SUPPORT, " + supportLteEcc + " phoneId = " + this.mPhoneId);
        if (this.mRil.mImsEccSupportRegistrants != null) {
            this.mRil.mImsEccSupportRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsRadioInfoChange(int type, String iid, String info) {
    }

    public void speechCodecInfoIndication(int type, int info) {
        this.mRil.processIndication(type);
        int[] ret = {info};
        ImsRILAdapter imsRILAdapter = this.mRil;
        imsRILAdapter.riljLog(" ImsRILConstants.RIL_UNSOL_SPEECH_CODEC_INFO, " + info + " phoneId = " + this.mPhoneId);
        if (this.mRil.mSpeechCodecInfoRegistrant != null) {
            this.mRil.mSpeechCodecInfoRegistrant.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsConferenceInfoIndication(int type, ArrayList<ImsConfParticipant> participants) {
        this.mRil.processIndication(type);
        ArrayList<ImsCallSessionProxy.User> ret = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            ImsCallSessionProxy.User user = new ImsCallSessionProxy.User();
            user.mUserAddr = participants.get(i).user_addr;
            user.mEndPoint = participants.get(i).end_point;
            user.mEntity = participants.get(i).entity;
            user.mDisplayText = participants.get(i).display_text;
            user.mStatus = participants.get(i).status;
            ret.add(user);
        }
        ImsRILAdapter imsRILAdapter = this.mRil;
        imsRILAdapter.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_CONFERENCE_INFO_INDICATION, ImsServiceCallTracker.sensitiveEncode("" + ret));
        if (this.mRil.mImsConfInfoRegistrants != null) {
            this.mRil.mImsConfInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void lteMessageWaitingIndication(int type, String callId, String pType, String urcIdx, String totalUrcCount, String rawData) {
        this.mRil.processIndication(type);
        String[] ret = {callId, pType, urcIdx, totalUrcCount, rawData, Integer.toString(this.mPhoneId)};
        ImsRILAdapter imsRILAdapter = this.mRil;
        imsRILAdapter.unsljLogRet(ImsRILConstants.RIL_UNSOL_LTE_MESSAGE_WAITING_INDICATION, ImsServiceCallTracker.sensitiveEncode("" + ret));
        if (this.mRil.mLteMsgWaitingRegistrants != null) {
            this.mRil.mLteMsgWaitingRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsDialogIndication(int type, ArrayList<Dialog> dialogList) {
        this.mRil.processIndication(type);
        this.mRil.unsljLog(ImsRILConstants.RIL_UNSOL_IMS_DIALOG_INDICATION);
        Iterator<Dialog> it = dialogList.iterator();
        while (it.hasNext()) {
            Dialog d = it.next();
            ImsRILAdapter imsRILAdapter = this.mRil;
            imsRILAdapter.riljLog("RIL_UNSOL_IMS_DIALOG_INDICATION dialogId = " + d.dialogId + ", address:" + ImsServiceCallTracker.sensitiveEncode(d.address));
        }
        if (this.mRil.mImsDialogRegistrant != null) {
            this.mRil.mImsDialogRegistrant.notifyRegistrants(new AsyncResult((Object) null, dialogList, (Throwable) null));
        }
    }

    public void imsCfgDynamicImsSwitchComplete(int type) {
        this.mRil.processIndication(type);
        int[] ret = {this.mPhoneId};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_CONFIG_DYNAMIC_IMS_SWITCH_COMPLETE, ret);
        if (this.mRil.mImsCfgDynamicImsSwitchCompleteRegistrants != null) {
            this.mRil.mImsCfgDynamicImsSwitchCompleteRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsCfgFeatureChanged(int type, int phoneId, int featureId, int value) {
        this.mRil.processIndication(type);
        int[] ret = {this.mPhoneId, featureId, value};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_CONFIG_FEATURE_CHANGED, ret);
        if (this.mRil.mImsCfgFeatureChangedRegistrants != null) {
            this.mRil.mImsCfgFeatureChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsCfgConfigChanged(int type, int phoneId, String configId, String value) {
        this.mRil.processIndication(type);
        String[] ret = {Integer.toString(this.mPhoneId), configId, value};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_CONFIG_CONFIG_CHANGED, ret);
        if (this.mRil.mImsCfgConfigChangedRegistrants != null) {
            this.mRil.mImsCfgConfigChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void imsCfgConfigLoaded(int type) {
        this.mRil.processIndication(type);
        String[] ret = {Integer.toString(this.mPhoneId)};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_CONFIG_CONFIG_LOADED, ret);
        if (this.mRil.mImsCfgConfigLoadedRegistrants != null) {
            this.mRil.mImsCfgConfigLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void newSmsStatusReportEx(int indicationType, ArrayList<Byte> pdu) {
        this.mRil.processIndication(indicationType);
        String[] ret = {Integer.toString(this.mPhoneId)};
        byte[] pduArray = RIL.arrayListToPrimitiveArray(pdu);
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_RESPONSE_NEW_SMS_STATUS_REPORT_EX, ret);
        if (this.mRil.mSmsStatusRegistrant != null) {
            this.mRil.mSmsStatusRegistrant.notifyRegistrant(new AsyncResult((Object) null, pduArray, (Throwable) null));
        }
    }

    public void newSmsEx(int indicationType, ArrayList<Byte> pdu) {
        this.mRil.processIndication(indicationType);
        String[] ret = {Integer.toString(this.mPhoneId)};
        byte[] pduArray = RIL.arrayListToPrimitiveArray(pdu);
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_RESPONSE_NEW_SMS_EX, ret);
        if (this.mRil.mNewSmsRegistrant != null) {
            this.mRil.mNewSmsRegistrant.notifyRegistrant(new AsyncResult((Object) null, pduArray, (Throwable) null));
        }
    }

    public void cdmaNewSmsEx(int indicationType, CdmaSmsMessage msg) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(ImsRILConstants.RIL_UNSOL_RESPONSE_CDMA_NEW_SMS_EX);
        SmsMessage sms = SmsMessageConverter.newSmsMessageFromCdmaSmsMessage(msg);
        if (this.mRil.mCdmaSmsRegistrant != null) {
            this.mRil.mCdmaSmsRegistrant.notifyRegistrant(new AsyncResult((Object) null, sms, (Throwable) null));
        }
    }

    public void imsRedialEmergencyIndication(int type, String callId) {
        this.mRil.processIndication(type);
        String[] ret = {callId, Integer.toString(this.mPhoneId)};
        ImsRILAdapter imsRILAdapter = this.mRil;
        imsRILAdapter.riljLog(" ImsRILConstants.RIL_UNSOL_REDIAL_EMERGENCY_INDICATION, " + callId + " phoneId = " + this.mPhoneId);
        if (this.mRil.mImsRedialEccIndRegistrants != null) {
            this.mRil.mImsRedialEccIndRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void rttModifyResponse(int indicationType, int callid, int result) {
        this.mRil.processIndication(indicationType);
        int[] ret = {callid, result};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_RTT_MODIFY_RESPONSE, ret);
        if (this.mRil.mRttModifyResponseRegistrants != null) {
            this.mRil.mRttModifyResponseRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void rttTextReceive(int indicationType, int callid, int length, String text) {
        this.mRil.processIndication(indicationType);
        String[] ret = {Integer.toString(callid), Integer.toString(length), text};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_RTT_TEXT_RECEIVE, ret);
        if (this.mRil.mRttTextReceiveRegistrants != null) {
            this.mRil.mRttTextReceiveRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void rttModifyRequestReceive(int indicationType, int callid, int rttType) {
        this.mRil.processIndication(indicationType);
        int[] ret = {callid, rttType};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_RTT_MODIFY_REQUEST_RECEIVE, ret);
        if (this.mRil.mRttModifyRequestReceiveRegistrants != null) {
            this.mRil.mRttModifyRequestReceiveRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void rttCapabilityIndication(int indicationType, int callid, int localCapability, int remoteCapability, int localStatus, int remoteStatus) {
        this.mRil.processIndication(indicationType);
        int[] ret = {callid, localCapability, remoteCapability, localStatus, remoteStatus};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_RTT_CAPABILITY_INDICATION, ret);
        if (this.mRil.mRttCapabilityIndicatorRegistrants != null) {
            this.mRil.mRttCapabilityIndicatorRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void audioIndication(int indicationType, int callid, int audio) {
        this.mRil.processIndication(indicationType);
        int[] ret = {callid, audio};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_AUDIO_INDICATION, ret);
        if (this.mRil.mRttAudioIndicatorRegistrants != null) {
            this.mRil.mRttAudioIndicatorRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void callAdditionalInfoInd(int indicationType, int ciType, ArrayList<String> info) {
        this.mRil.processIndication(indicationType);
        String[] notification = new String[(info.size() + 1)];
        notification[0] = Integer.toString(ciType);
        for (int i = 0; i < info.size(); i++) {
            notification[i + 1] = info.get(i);
        }
        ImsRILAdapter imsRILAdapter = this.mRil;
        imsRILAdapter.unsljLogRet(ImsRILConstants.RIL_UNSOL_CALL_ADDITIONAL_INFO, ImsServiceCallTracker.sensitiveEncode("" + notification));
        if (this.mRil.mCallAdditionalInfoRegistrants != null) {
            this.mRil.mCallAdditionalInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, notification, (Throwable) null));
        }
    }

    public void callRatIndication(int indicationType, int domain, int rat) {
        this.mRil.processIndication(indicationType);
        int[] ret = {domain, rat};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_CALL_RAT_INDICATION, ret);
        if (this.mRil.mCallRatIndicationRegistrants != null) {
            this.mRil.mCallRatIndicationRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void sipHeaderReport(int indicationType, ArrayList<String> data) {
        this.mRil.processIndication(indicationType);
        if (data != null && data.size() != 0) {
            String[] sipHeaderInfo = (String[]) data.toArray(new String[data.size()]);
            this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_SIP_HEADER, sipHeaderInfo);
            if (this.mRil.mImsSipHeaderRegistrants != null) {
                this.mRil.mImsSipHeaderRegistrants.notifyRegistrants(new AsyncResult((Object) null, sipHeaderInfo, (Throwable) null));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void riljLoge(String msg) {
        this.mRil.riljLoge(msg);
    }

    public void sendVopsIndication(int indicationType, int vops) {
        int[] ret = {vops};
        ImsRILAdapter imsRILAdapter = this.mRil;
        imsRILAdapter.riljLog("ImsRILConstants.RIL_UNSOL_VOPS_INDICATION, " + vops + " phoneId = " + this.mPhoneId + ", ret = " + ret);
        if (this.mRil.mVopsStatusIndRegistrants != null) {
            this.mRil.mVopsStatusIndRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    public void sipRegInfoInd(int indicationType, int account_id, int response_code, ArrayList<String> info) {
        StringBuilder b = new StringBuilder();
        b.append("sipRegInfoInd: ");
        b.append(account_id);
        b.append(",");
        b.append(response_code);
        Iterator<String> it = info.iterator();
        while (it.hasNext()) {
            b.append(",");
            b.append(it.next());
        }
        this.mRil.riljLog(b.toString());
    }

    public void imsRegStatusReport(int type, ImsRegStatusInfo report) {
        this.mRil.processIndication(type);
        ImsRegInfo imsRegInfo = new ImsRegInfo(report.report_type, report.account_id, report.expire_time, report.error_code, report.uri, report.error_msg);
        this.mRil.riljLogv(imsRegInfo.toString());
        if (this.mRil.mImsRegStatusIndRistrants != null) {
            this.mRil.mImsRegStatusIndRistrants.notifyRegistrants(new AsyncResult((Object) null, imsRegInfo, (Throwable) null));
        }
    }

    public void imsRegInfoInd(int indicationType, ArrayList<Integer> info) {
        this.mRil.processIndication(indicationType);
        StringBuilder b = new StringBuilder();
        b.append("imsRegInfoInd: ");
        Iterator<Integer> it = info.iterator();
        while (it.hasNext()) {
            b.append(it.next());
            b.append(", ");
        }
        b.deleteCharAt(b.length() - 1);
        this.mRil.riljLog(b.toString());
        if (this.mRil.mEiregIndRegistrants != null) {
            this.mRil.mEiregIndRegistrants.notifyRegistrants(new AsyncResult((Object) null, info, (Throwable) null));
        }
    }

    public void onSsacStatus(int indicationType, ArrayList<Integer> status) {
        this.mRil.processIndication(indicationType);
        StringBuilder b = new StringBuilder();
        b.append("onSsacStatus: ");
        Iterator<Integer> it = status.iterator();
        while (it.hasNext()) {
            b.append(it.next());
            b.append(", ");
        }
        b.deleteCharAt(b.length() - 1);
        this.mRil.riljLog(b.toString());
        if (this.mRil.mSsacIndRegistrants != null) {
            this.mRil.mSsacIndRegistrants.notifyRegistrants(new AsyncResult((Object) null, status, (Throwable) null));
        }
    }

    public void eregrtInfoInd(int indicationType, ArrayList<Integer> info) {
        this.mRil.processIndication(indicationType);
        StringBuilder b = new StringBuilder();
        b.append("eregrtInfoInd: ");
        Iterator<Integer> it = info.iterator();
        while (it.hasNext()) {
            b.append(it.next());
            b.append(", ");
        }
        b.deleteCharAt(b.length() - 1);
        this.mRil.riljLog(b.toString());
        if (this.mRil.mEregrtIndRegistrants != null) {
            this.mRil.mEregrtIndRegistrants.notifyRegistrants(new AsyncResult((Object) null, info, (Throwable) null));
        }
    }

    public void videoRingtoneEventInd(int type, ArrayList<String> event) {
        this.mRil.processIndication(type);
        this.mRil.unsljLog(ImsRILConstants.RIL_UNSOL_VIDEO_RINGTONE_EVENT_IND);
        if (event != null && event.size() != 0) {
            String[] eventInfo = (String[]) event.toArray(new String[event.size()]);
            if (this.mRil.mVideoRingtoneRegistrants != null) {
                this.mRil.mVideoRingtoneRegistrants.notifyRegistrants(new AsyncResult((Object) null, eventInfo, (Throwable) null));
            }
        }
    }

    public void onMDInternetUsageInd(int indicationType, ArrayList<Integer> info) {
        this.mRil.processIndication(indicationType);
        StringBuilder b = new StringBuilder();
        b.append("onMDInternetUsage: ");
        Iterator<Integer> it = info.iterator();
        while (it.hasNext()) {
            b.append(it.next());
            b.append(", ");
        }
        b.deleteCharAt(b.length() - 1);
        this.mRil.riljLog(b.toString());
        if (this.mRil.mMDInternetUsageRegistrants != null) {
            this.mRil.mMDInternetUsageRegistrants.notifyRegistrants(new AsyncResult((Object) null, info, (Throwable) null));
        }
    }

    public void imsRegFlagInd(int type, int flag) {
        this.mRil.processIndication(type);
        int[] ret = {flag, this.mPhoneId};
        this.mRil.unsljLogRet(ImsRILConstants.RIL_UNSOL_IMS_REG_FLAG_IND, ret);
        if (this.mRil.mImsRegFlagIndRegistrants != null) {
            this.mRil.mImsRegFlagIndRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }
}
