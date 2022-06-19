package com.mediatek.ims;

import android.content.Context;
import android.content.Intent;
import android.hardware.radio.V1_0.LastCallFailCause;
import android.hardware.radio.V1_0.RadioError;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.DataSpecificRegistrationInfo;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VopsSupportInfo;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsCallSessionListener;
import android.telephony.ims.ImsConferenceState;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.ImsSuppServiceNotification;
import android.telephony.ims.stub.ImsCallSessionImplBase;
import android.text.TextUtils;
import androidx.core.app.NotificationCompat;
import com.android.ims.ImsManager;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsVideoCallProvider;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.mediatek.ims.DefaultConferenceHandler;
import com.mediatek.ims.ImsCallInfo;
import com.mediatek.ims.OperatorUtils;
import com.mediatek.ims.common.ImsCarrierConfigConstants;
import com.mediatek.ims.config.internal.ImsConfigUtils;
import com.mediatek.ims.ext.OpImsCallSessionProxy;
import com.mediatek.ims.ext.OpImsServiceCustomizationFactoryBase;
import com.mediatek.ims.ext.OpImsServiceCustomizationUtils;
import com.mediatek.ims.internal.ConferenceCallMessageHandler;
import com.mediatek.ims.internal.IMtkImsCallSession;
import com.mediatek.ims.internal.ImsVTProvider;
import com.mediatek.ims.internal.ImsVTProviderUtil;
import com.mediatek.ims.plugin.ExtensionFactory;
import com.mediatek.ims.plugin.ImsCallOemPlugin;
import com.mediatek.ims.plugin.impl.ImsSelfActivatorBase;
import com.mediatek.ims.ril.ImsCommandsInterface;
import com.mediatek.wfo.DisconnectCause;
import com.mediatek.wfo.IMwiService;
import com.mediatek.wfo.IWifiOffloadService;
import com.mediatek.wfo.MwisConstants;
import com.mediatek.wfo.WifiOffloadManager;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class ImsCallSessionProxy extends ImsCallSessionImplBase {
    private static final int AMR_NB = 6;
    private static final int AMR_WB = 7;
    private static final int CACHED_TERMINATE_REASON_DELAY = 100;
    public static final int CALL_INFO_MSG_TYPE_ACTIVE = 132;
    public static final int CALL_INFO_MSG_TYPE_ALERT = 2;
    public static final int CALL_INFO_MSG_TYPE_CONNECTED = 6;
    public static final int CALL_INFO_MSG_TYPE_DISCONNECTED = 133;
    public static final int CALL_INFO_MSG_TYPE_HELD = 131;
    public static final int CALL_INFO_MSG_TYPE_MO_CALL_ID_ASSIGN = 130;
    public static final int CALL_INFO_MSG_TYPE_REMOTE_HOLD = 135;
    public static final int CALL_INFO_MSG_TYPE_REMOTE_RESUME = 136;
    public static final int CALL_INFO_MSG_TYPE_SETUP = 0;
    private static final boolean DBG = true;
    private static final int EVENT_ACCEPT_RESULT = 202;
    private static final int EVENT_ADD_CONFERENCE_RESULT = 206;
    private static final int EVENT_CACHED_TERMINATE_REASON = 230;
    private static final int EVENT_CALL_ADDITIONAL_INFO = 229;
    private static final int EVENT_CALL_INFO_INDICATION = 102;
    private static final int EVENT_CALL_MODE_CHANGE_INDICATION = 106;
    private static final int EVENT_CALL_RAT_INDICATION = 228;
    private static final int EVENT_CANCEL_USSI_COMPLETE = 214;
    private static final int EVENT_DIAL_CONFERENCE_RESULT = 209;
    private static final int EVENT_DIAL_RESULT = 201;
    private static final int EVENT_DTMF_DONE = 212;
    private static final int EVENT_ECONF_RESULT_INDICATION = 104;
    private static final int EVENT_ECT_RESULT = 215;
    private static final int EVENT_ECT_RESULT_INDICATION = 109;
    private static final int EVENT_GET_LAST_CALL_FAIL_CAUSE = 105;
    private static final int EVENT_HOLD_RESULT = 203;
    private static final int EVENT_IMS_CONFERENCE_INDICATION = 111;
    private static final int EVENT_MERGE_RESULT = 205;
    private static final int EVENT_ON_SUPP_SERVICE_NOTIFICATION = 226;
    private static final int EVENT_POLL_CALLS_RESULT = 101;
    private static final int EVENT_PULL_CALL_RESULT = 216;
    private static final int EVENT_RADIO_NOT_AVAILABLE = 217;
    private static final int EVENT_REDIAL_ECC_INDICATION = 224;
    private static final int EVENT_REMOVE_CONFERENCE_RESULT = 207;
    private static final int EVENT_RESUME_RESULT = 204;
    private static final int EVENT_RETRIEVE_MERGE_FAIL_RESULT = 211;
    private static final int EVENT_RTT_AUDIO_INDICATION = 225;
    private static final int EVENT_RTT_CAPABILITY_INDICATION = 110;
    private static final int EVENT_RTT_MODIFY_REQUEST_RECEIVE = 220;
    private static final int EVENT_RTT_MODIFY_RESPONSE = 219;
    private static final int EVENT_RTT_TEXT_RECEIVE_INDICATION = 218;
    private static final int EVENT_SEND_USSI_COMPLETE = 213;
    private static final int EVENT_SIP_CODE_INDICATION = 208;
    private static final int EVENT_SIP_HEADER_INFO = 227;
    private static final int EVENT_SPEECH_CODEC_INFO = 223;
    private static final int EVENT_SWAP_BEFORE_MERGE_RESULT = 210;
    private static final int EVENT_VIDEO_CAPABILITY_INDICATION = 107;
    private static final int EVENT_VIDEO_RINGTONE_CACHED_INFO = 233;
    private static final int EVENT_VIDEO_RINGTONE_INFO = 232;
    private static final int EVENT_VIDEO_RINGTONE_REQUEST_RESULT = 231;
    private static final int EVRC = 2;
    private static final int EVRC_B = 3;
    private static final int EVRC_NW = 5;
    private static final int EVRC_WB = 4;
    private static final int EVS_AWB = 33;
    private static final int EVS_FB = 32;
    private static final int EVS_NB = 23;
    private static final int EVS_SW = 25;
    private static final int EVS_WB = 24;
    public static final String EXTRA_CALL_INFO_MESSAGE_TYPE = "mediatek:callInfoMessageType";
    public static final String EXTRA_CALL_TYPE = "mediatek:callType";
    public static final String EXTRA_EMERGENCY_CALL = "mediatek:emergencyCall";
    public static final String EXTRA_INCOMING_CALL = "mediatek:incomingCall";
    public static final String EXTRA_RAT_TYPE = "mediatek:ratType";
    public static final String EXTRA_WAS_VIDEO_CALL = "mediatek:wasVideoCall";
    private static final int GET_CACHED_VIDEO_RINGTONE_INFO = 100;
    private static final int GSM_EFR = 8;
    private static final int GSM_FR = 9;
    private static final int GSM_HR = 10;
    private static final int HANGUP_CAUSE_FORWARD = 3;
    private static final int HANGUP_CAUSE_LOW_BATTERY = 2;
    private static final int HANGUP_CAUSE_NONE = 0;
    private static final int HANGUP_CAUSE_NO_COVERAGE = 1;
    private static final int HANGUP_CAUSE_SPECIAL_HANGUP = 4;
    private static final int HEADER_CALL_ID = 13;
    private static String HEX = "0123456789ABCDEF";
    private static final String IMPORTANT_STRING = "important";
    private static final int IMS_CALL_MODE_CLIENT_API = 2;
    private static final int IMS_CALL_MODE_NORMAL = 1;
    private static final int IMS_CALL_TYPE_LTE = 1;
    private static final int IMS_CALL_TYPE_NR = 3;
    private static final int IMS_CALL_TYPE_UNKNOWN = 0;
    private static final int IMS_CALL_TYPE_WIFI = 2;
    private static final int IMS_VIDEO_CALL = 21;
    private static final int IMS_VIDEO_CONF = 23;
    private static final int IMS_VIDEO_CONF_PARTS = 25;
    private static final int IMS_VOICE_CALL = 20;
    private static final int IMS_VOICE_CONF = 22;
    private static final int IMS_VOICE_CONF_PARTS = 24;
    private static final String INVALID_CALL_ID = "65535";
    private static final int INVALID_CALL_MODE = 255;
    private static final String LOG_TAG = "ImsCallSessionProxy";
    private static final int MAX_WRONG_ECPI_COUNT = 5;
    private static final int MT_CALL_ENRICHED_CALL = 102;
    private static final int MT_CALL_IMS_GWSD = 101;
    private static final String NA_PRIOR_CLIR_PREFIX = "*82";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final int QCELP13K = 1;
    private static final int REMOTE_STATE_HOLD = 1;
    private static final int REMOTE_STATE_NONE = 0;
    private static final int REMOTE_STATE_RESUME = 2;
    private static final int RTT_AUDIO_SPEECH = 0;
    private static final boolean SENLOG = TextUtils.equals(Build.TYPE, "user");
    private static final String SIP_INVITE_HEADER_CALL_INFO = "Call-Info";
    private static final String SIP_INVITE_HEADER_PRIORITY = "Priority";
    private static final String SIP_INVITE_HEADER_SUBJECT = "Subject";
    private static final String STANDARD_STRING = "standard";
    public static final int SUB_TYPE_HEADER = 1;
    public static final int SUB_TYPE_LOCATION = 2;
    private static final String TAG_DOUBLE_QUOTE = "<ascii_34>";
    private static final boolean TELDBG;
    private static final int USSI_REQUEST = 1;
    private static final int USSI_RESPONSE = 2;
    private static final boolean VDBG = false;
    private static final int VIDEO_RINGTONE_BUTTON_SHOW_EVENT = 100;
    private static final int VIDEO_RINGTONE_CLICK_BUTTON_EVENT = 1;
    private static final int VIDEO_RINGTONE_CLICK_COORDINATOR_EVENT = 2;
    private static final int VIDEO_STATE_PAUSE = 0;
    private static final int VIDEO_STATE_RECV_ONLY = 2;
    private static final int VIDEO_STATE_SEND_ONLY = 1;
    private static final int VIDEO_STATE_SEND_RECV = 3;
    private static final String VT_PROVIDER_ID = "video_provider_id";
    private static final int WFC_GET_CAUSE_FAILED = -1;
    /* access modifiers changed from: private */
    public int mBadRssiThreshould;
    /* access modifiers changed from: private */
    public String mCachedCauseText;
    /* access modifiers changed from: private */
    public AsyncResult mCachedSuppServiceInfo;
    private ImsReasonInfo mCachedTerminateReasonInfo;
    /* access modifiers changed from: private */
    public AsyncResult mCachedUserInfo;
    private String mCachedVideoRingtoneButtonInfo;
    /* access modifiers changed from: private */
    public CallErrorState mCallErrorState;
    /* access modifiers changed from: private */
    public String mCallId;
    /* access modifiers changed from: private */
    public String mCallNumber;
    /* access modifiers changed from: private */
    public ImsCallProfile mCallProfile;
    private int mCallRat;
    private ConferenceEventListener mConfEvtListener;
    /* access modifiers changed from: private */
    public ImsCallSessionProxy mConfSessionProxy;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Message mDtmfMsg;
    /* access modifiers changed from: private */
    public Messenger mDtmfTarget;
    /* access modifiers changed from: private */
    public int mEconfCount;
    /* access modifiers changed from: private */
    public Message mEctMsg;
    /* access modifiers changed from: private */
    public Messenger mEctTarget;
    /* access modifiers changed from: private */
    public boolean mEnableSendRttBom;
    /* access modifiers changed from: private */
    public boolean mFwkPause;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private int mHangupCount;
    /* access modifiers changed from: private */
    public boolean mHangupHostDuringMerge;
    /* access modifiers changed from: private */
    public boolean mHasPendingDisconnect;
    /* access modifiers changed from: private */
    public boolean mHasPendingMo;
    private boolean mHasTriedSelfActivation;
    private String mHeaderCallId;
    private String mHeaderData;
    private int mImsCallMode;
    /* access modifiers changed from: private */
    public ImsCommandsInterface mImsRILAdapter;
    /* access modifiers changed from: private */
    public ImsReasonInfo mImsReasonInfo;
    /* access modifiers changed from: private */
    public ImsService mImsService;
    /* access modifiers changed from: private */
    public ImsServiceCallTracker mImsServiceCT;
    /* access modifiers changed from: private */
    public boolean mIsAddRemoveParticipantsCommandOK;
    /* access modifiers changed from: private */
    public boolean mIsConferenceHost;
    private boolean mIsEmergencyCall;
    /* access modifiers changed from: private */
    public boolean mIsHideHoldDuringECT;
    /* access modifiers changed from: private */
    public boolean mIsHideHoldEventDuringMerging;
    private boolean mIsIncomingCall;
    /* access modifiers changed from: private */
    public boolean mIsMerging;
    private boolean mIsNeedCacheTerminationEarly;
    /* access modifiers changed from: private */
    public boolean mIsOnTerminated;
    /* access modifiers changed from: private */
    public boolean mIsOneKeyConf;
    /* access modifiers changed from: private */
    public boolean mIsRetrievingMergeFail;
    /* access modifiers changed from: private */
    public boolean mIsRingingRedirect;
    /* access modifiers changed from: private */
    public boolean mIsRttEnabledForCallSession;
    /* access modifiers changed from: private */
    public boolean mIsWaitingClose;
    private String mLastSIPReasonHeader;
    private int mLastSipCode;
    private int mLastSipMethod;
    /* access modifiers changed from: private */
    public ImsCallSessionListener mListener;
    /* access modifiers changed from: private */
    public ImsCallProfile mLocalCallProfile;
    /* access modifiers changed from: private */
    public int mLocalTerminateReason;
    /* access modifiers changed from: private */
    public Object mLock;
    /* access modifiers changed from: private */
    public boolean mMTSetup;
    /* access modifiers changed from: private */
    public String mMergeCallId;
    private ImsCallInfo.State mMergeCallStatus;
    /* access modifiers changed from: private */
    public boolean mMerged;
    /* access modifiers changed from: private */
    public String mMergedCallId;
    private ImsCallInfo.State mMergedCallStatus;
    /* access modifiers changed from: private */
    public MtkImsCallSessionProxy mMtkConfSessionProxy;
    public MtkImsCallSessionProxy mMtkImsCallSessionProxy;
    /* access modifiers changed from: private */
    public boolean mNeedHideResumeEventDuringMerging;
    /* access modifiers changed from: private */
    public boolean mNormalCallsMerge;
    /* access modifiers changed from: private */
    public String[] mOneKeyparticipants;
    /* access modifiers changed from: private */
    public OpImsCallSessionProxy mOpImsCallSession;
    private boolean mOverallPause;
    /* access modifiers changed from: private */
    public HashMap<String, Bundle> mParticipants;
    /* access modifiers changed from: private */
    public ArrayList<String> mParticipantsList;
    /* access modifiers changed from: private */
    public int mPendingDisconnectReason;
    /* access modifiers changed from: private */
    public String[] mPendingParticipantInfo;
    /* access modifiers changed from: private */
    public int mPendingParticipantInfoIndex;
    /* access modifiers changed from: private */
    public int mPendingParticipantStatistics;
    /* access modifiers changed from: private */
    public int mPhoneId;
    /* access modifiers changed from: private */
    public int mPreLocalVideoCapability;
    /* access modifiers changed from: private */
    public int mPreRemoteVideoCapability;
    /* access modifiers changed from: private */
    public boolean mRadioUnavailable;
    /* access modifiers changed from: private */
    public int mRatType;
    /* access modifiers changed from: private */
    public ImsCallProfile mRemoteCallProfile;
    private int mRemoteState;
    /* access modifiers changed from: private */
    public String mRetryRemoveUri;
    /* access modifiers changed from: private */
    public RttTextEncoder mRttTextEncoder;
    protected ImsSelfActivatorBase mSelfActivateHelper;
    private final Handler mServiceHandler;
    /* access modifiers changed from: private */
    public boolean mShouldUpdateAddressByPau;
    /* access modifiers changed from: private */
    public boolean mShouldUpdateAddressBySipField;
    /* access modifiers changed from: private */
    public boolean mShouldUpdateAddressFromEcpi;
    /* access modifiers changed from: private */
    public boolean mSipSessionProgress;
    /* access modifiers changed from: private */
    public int mState;
    /* access modifiers changed from: private */
    public boolean mThreeWayMergeSucceeded;
    /* access modifiers changed from: private */
    public ImsVTProvider mVTProvider;
    /* access modifiers changed from: private */
    public ImsVTProviderUtil mVTProviderUtil;
    /* access modifiers changed from: private */
    public int mVideoState;
    private VtProviderListener mVtProviderListener;
    /* access modifiers changed from: private */
    public IWifiOffloadService mWfoService;
    IWifiOffloadListenerProxy mWosListener;
    /* access modifiers changed from: private */
    public int mWrongEcpiCount;

    private enum CallErrorState {
        IDLE,
        DIAL,
        DISCONNECT
    }

    public static class User {
        public String mDisplayText;
        public String mEndPoint;
        public String mEntity;
        public String mStatus = ConferenceCallMessageHandler.STATUS_DISCONNECTED;
        public String mUserAddr;
    }

    static /* synthetic */ int access$10208(ImsCallSessionProxy x0) {
        int i = x0.mEconfCount;
        x0.mEconfCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$4008(ImsCallSessionProxy x0) {
        int i = x0.mWrongEcpiCount;
        x0.mWrongEcpiCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$7608(ImsCallSessionProxy x0) {
        int i = x0.mPendingParticipantInfoIndex;
        x0.mPendingParticipantInfoIndex = i + 1;
        return i;
    }

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        TELDBG = z;
    }

    private class ImsCallProfileEx {
        public static final String EXTRA_IMPORTANCE = "mediatek:priority";
        public static final String EXTRA_IMS_GWSD = "ims_gwsd";
        public static final String EXTRA_INCOMING_MPTY = "incoming_mpty";
        public static final String EXTRA_LOCATION = "mediatek:location";
        public static final String EXTRA_MPTY = "mpty";
        public static final String EXTRA_PICTURE = "mediatek:call-info";
        public static final String EXTRA_SUBJECT = "mediatek:subject";
        public static final String EXTRA_VERSTAT = "verstat";

        private ImsCallProfileEx() {
        }
    }

    private class ImsCallLogLevel {
        public static final int DEBUG = 2;
        public static final int ERROR = 5;
        public static final int INFO = 3;
        public static final int VERBOSE = 1;
        public static final int WARNNING = 4;

        private ImsCallLogLevel() {
        }
    }

    private class VtProviderListener implements ImsVTProvider.VideoProviderStateListener {
        private VtProviderListener() {
        }

        /* synthetic */ VtProviderListener(ImsCallSessionProxy x0, C01081 x1) {
            this();
        }

        public void onReceivePauseState(boolean isPause) {
            if (ImsCallSessionProxy.this.mCallProfile != null) {
                ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
                imsCallSessionProxy.logWithCallId("onReceivePauseState() : " + isPause, 2);
                boolean unused = ImsCallSessionProxy.this.mFwkPause = isPause;
                ImsCallSessionProxy.this.updateVideoDirection();
                ImsCallSessionProxy.this.notifyCallSessionUpdated();
            }
        }

        public void onReceiveWiFiUsage(long usage) {
        }
    }

    ImsCallSessionProxy(Context context, ImsCallProfile profile, ImsCallSessionListener listener, ImsService imsService, Handler handler, ImsCommandsInterface imsRILAdapter, String callId, int phoneId, MtkImsCallSessionProxy mtkImsCallSessionProxy) {
        this(context, profile, listener, imsService, handler, imsRILAdapter, callId, phoneId);
        this.mMtkImsCallSessionProxy = mtkImsCallSessionProxy;
    }

    ImsCallSessionProxy(Context context, ImsCallProfile profile, ImsCallSessionListener listener, ImsService imsService, Handler handler, ImsCommandsInterface imsRILAdapter, String callId, int phoneId) {
        int i;
        ImsCallProfile imsCallProfile = profile;
        ImsService imsService2 = imsService;
        ImsCommandsInterface imsCommandsInterface = imsRILAdapter;
        String str = callId;
        int i2 = phoneId;
        this.mState = 0;
        this.mHasPendingMo = false;
        this.mIsMerging = false;
        this.mIsOnTerminated = false;
        this.mIsWaitingClose = false;
        this.mWrongEcpiCount = 0;
        this.mIsAddRemoveParticipantsCommandOK = false;
        this.mPendingParticipantInfoIndex = 0;
        this.mPendingParticipantStatistics = 0;
        this.mIsHideHoldEventDuringMerging = false;
        this.mNeedHideResumeEventDuringMerging = false;
        this.mMergeCallId = "";
        this.mMergeCallStatus = ImsCallInfo.State.INVALID;
        this.mMergedCallId = "";
        this.mMergedCallStatus = ImsCallInfo.State.INVALID;
        this.mNormalCallsMerge = false;
        this.mThreeWayMergeSucceeded = false;
        this.mMerged = false;
        this.mEconfCount = 0;
        this.mRadioUnavailable = false;
        this.mIsRetrievingMergeFail = false;
        this.mRetryRemoveUri = null;
        this.mHangupHostDuringMerge = false;
        this.mRatType = 1;
        this.mCallRat = 0;
        this.mVTProviderUtil = ImsVTProviderUtil.getInstance();
        this.mLocalTerminateReason = 0;
        this.mHangupCount = 0;
        this.mIsOneKeyConf = false;
        this.mOneKeyparticipants = null;
        this.mCallErrorState = CallErrorState.IDLE;
        this.mDtmfMsg = null;
        this.mDtmfTarget = null;
        this.mIsHideHoldDuringECT = false;
        this.mEctMsg = null;
        this.mEctTarget = null;
        this.mImsReasonInfo = null;
        this.mShouldUpdateAddressByPau = DBG;
        this.mShouldUpdateAddressFromEcpi = false;
        this.mShouldUpdateAddressBySipField = DBG;
        this.mRttTextEncoder = null;
        this.mOpImsCallSession = null;
        this.mBadRssiThreshould = -90;
        this.mVideoState = 3;
        this.mOverallPause = false;
        this.mFwkPause = false;
        this.mHasTriedSelfActivation = false;
        this.mSipSessionProgress = false;
        this.mIsIncomingCall = DBG;
        this.mIsEmergencyCall = false;
        this.mIsConferenceHost = false;
        this.mIsRttEnabledForCallSession = false;
        this.mEnableSendRttBom = false;
        this.mCachedUserInfo = null;
        this.mHeaderData = "";
        this.mCachedSuppServiceInfo = null;
        this.mLock = new Object();
        this.mCachedVideoRingtoneButtonInfo = null;
        this.mMTSetup = false;
        this.mCachedTerminateReasonInfo = null;
        this.mIsNeedCacheTerminationEarly = false;
        this.mRemoteState = 0;
        this.mImsCallMode = 1;
        this.mIsRingingRedirect = false;
        this.mCachedCauseText = null;
        this.mParticipants = new HashMap<>();
        this.mParticipantsList = new ArrayList<>();
        this.mVtProviderListener = new VtProviderListener(this, (C01081) null);
        this.mPhoneId = i2;
        this.mImsServiceCT = ImsServiceCallTracker.getInstance(phoneId);
        this.mServiceHandler = handler;
        MyHandler myHandler = new MyHandler(handler.getLooper());
        this.mHandler = myHandler;
        this.mContext = context;
        this.mCallProfile = imsCallProfile;
        this.mLocalCallProfile = new ImsCallProfile(imsCallProfile.mServiceType, imsCallProfile.mCallType);
        this.mRemoteCallProfile = new ImsCallProfile(imsCallProfile.mServiceType, imsCallProfile.mCallType);
        this.mPreLocalVideoCapability = imsCallProfile.mCallType;
        this.mPreRemoteVideoCapability = imsCallProfile.mCallType;
        if (OperatorUtils.isMatched(OperatorUtils.OPID.OP12, this.mPhoneId)) {
            Rlog.d(LOG_TAG, "VzW: set default as no HD icon");
            this.mRemoteCallProfile.mRestrictCause = 3;
        }
        this.mListener = listener;
        this.mImsService = imsService2;
        this.mImsRILAdapter = imsCommandsInterface;
        this.mCallId = str;
        logWithCallId("ImsCallSessionProxy() : RILAdapter:" + imsCommandsInterface + "imsService:" + imsService2 + " callID:" + str + " phoneId: " + i2, 2);
        this.mRttTextEncoder = new RttTextEncoder();
        OpImsServiceCustomizationFactoryBase opImsServiceCustomizationFactory = OpImsServiceCustomizationUtils.getOpFactory(context);
        this.mOpImsCallSession = opImsServiceCustomizationFactory.makeOpImsCallSessionProxy();
        this.mImsRILAdapter.registerForCallInfo(myHandler, 102, (Object) null);
        this.mImsRILAdapter.registerForEconfResult(myHandler, 104, (Object) null);
        this.mImsRILAdapter.registerForCallProgressIndicator(myHandler, EVENT_SIP_CODE_INDICATION, (Object) null);
        this.mImsRILAdapter.registerForCallModeChangeIndicator(myHandler, 106, (Object) null);
        this.mImsRILAdapter.registerForVideoCapabilityIndicator(myHandler, EVENT_VIDEO_CAPABILITY_INDICATION, (Object) null);
        this.mImsRILAdapter.registerForEctResult(myHandler, EVENT_ECT_RESULT_INDICATION, (Object) null);
        this.mImsRILAdapter.registerForImsConfInfoUpdate(myHandler, 111, (Object) null);
        this.mImsRILAdapter.registerForNotAvailable(myHandler, EVENT_RADIO_NOT_AVAILABLE, (Object) null);
        this.mImsRILAdapter.registerForSpeechCodecInfo(myHandler, EVENT_SPEECH_CODEC_INFO, (Object) null);
        this.mImsRILAdapter.registerForImsRedialEccInd(myHandler, EVENT_REDIAL_ECC_INDICATION, (Object) null);
        this.mImsRILAdapter.registerForSipHeaderInd(myHandler, EVENT_SIP_HEADER_INFO, (Object) null);
        this.mImsRILAdapter.registerForCallRatIndication(myHandler, EVENT_CALL_RAT_INDICATION, (Object) null);
        this.mImsRILAdapter.registerForCallAdditionalInfo(myHandler, 229, (Object) null);
        this.mSelfActivateHelper = getImsExtSelfActivator(context, handler, this, imsRILAdapter, imsService, phoneId);
        this.mImsRILAdapter.registerForRttCapabilityIndicator(myHandler, EVENT_RTT_CAPABILITY_INDICATION, (Object) null);
        this.mImsRILAdapter.registerForRttModifyRequestReceive(myHandler, EVENT_RTT_MODIFY_REQUEST_RECEIVE, (Object) null);
        this.mImsRILAdapter.registerForRttTextReceive(myHandler, EVENT_RTT_TEXT_RECEIVE_INDICATION, (Object) null);
        this.mImsRILAdapter.registerForRttModifyResponse(myHandler, EVENT_RTT_MODIFY_RESPONSE, (Object) null);
        this.mImsRILAdapter.registerForRttAudioIndicator(myHandler, EVENT_RTT_AUDIO_INDICATION, (Object) null);
        this.mImsRILAdapter.registerForVideoRingtoneInfo(myHandler, EVENT_VIDEO_RINGTONE_INFO, (Object) null);
        if (SystemProperties.get("persist.vendor.vilte_support").equals("1")) {
            logWithCallId("ImsCallSessionProxy() : start new VTProvider", 2);
            if (this.mCallId != null) {
                ImsVTProvider makeImsVtProvider = opImsServiceCustomizationFactory.makeImsVtProvider();
                this.mVTProvider = makeImsVtProvider;
                this.mVTProviderUtil.bind(makeImsVtProvider, Integer.parseInt(this.mCallId), this.mPhoneId);
            } else {
                this.mVTProvider = opImsServiceCustomizationFactory.makeImsVtProvider();
            }
            this.mVTProvider.addVideoProviderStateListener(this.mVtProviderListener);
            logWithCallId("ImsCallSessionProxy() : end new VTProvider", 2);
        }
        this.mImsRILAdapter.setOnSuppServiceNotification(myHandler, 226, (Object) null);
        IBinder b = ServiceManager.getService(WifiOffloadManager.WFO_SERVICE);
        if (b != null) {
            this.mWfoService = IWifiOffloadService.Stub.asInterface(b);
            IBinder iBinder = b;
        } else {
            IBinder b2 = ServiceManager.getService(MwisConstants.MWI_SERVICE);
            if (b2 != null) {
                try {
                    IMwiService iMwiService = IMwiService.Stub.asInterface(b2);
                    if (iMwiService != null) {
                        this.mWfoService = iMwiService.getWfcHandlerInterface();
                    }
                } catch (RemoteException e) {
                    logWithCallId("ImsCallSessionProxy() : can't get MwiService" + e, 5);
                }
            } else {
                logWithCallId("ImsCallSessionProxy() : No MwiService exist", 5);
            }
        }
        if (this.mWfoService != null) {
            try {
                if (this.mWosListener == null) {
                    this.mWosListener = new IWifiOffloadListenerProxy(this, (C01081) null);
                }
                this.mWfoService.registerForHandoverEvent(this.mWosListener);
                if (str != null) {
                    updateCallStateForWifiOffload(3);
                }
            } catch (RemoteException e2) {
                logWithCallId("ImsCallSessionProxy() : RemoteException ImsCallSessionProxy()", 5);
            }
        }
        if (this.mCallId == null) {
            i = 0;
            this.mIsIncomingCall = false;
        } else {
            i = 0;
        }
        if (imsCallProfile.mServiceType != 2) {
            updateRat(this.mImsService.getRatType(this.mPhoneId), i);
        }
        this.mConfSessionProxy = null;
        this.mMtkConfSessionProxy = null;
        updateShouldUseSipField();
    }

    ImsCallSessionProxy(Context context, ImsCallProfile profile, ImsCallSessionListener listener, ImsService imsService, Handler handler, ImsCommandsInterface imsRILAdapter, int phoneId) {
        this(context, profile, listener, imsService, handler, imsRILAdapter, (String) null, phoneId);
        logWithCallId("ImsCallSessionProxy() : ImsCallSessionProxy MO constructor", 2);
        this.mIsIncomingCall = false;
    }

    public void close() {
        IWifiOffloadListenerProxy iWifiOffloadListenerProxy;
        logWithCallId("close() : ImsCallSessionProxy is closed!!! ", 2);
        if (this.mState == -1) {
            logWithCallId("close() : ImsCallSessionProxy already closed", 2);
            return;
        }
        this.mState = -1;
        this.mImsServiceCT.removeCallConnection(this.mCallId, this);
        this.mImsRILAdapter.unregisterForCallInfo(this.mHandler);
        this.mImsRILAdapter.unregisterForEconfResult(this.mHandler);
        this.mImsRILAdapter.unregisterForCallProgressIndicator(this.mHandler);
        this.mImsRILAdapter.unregisterForCallModeChangeIndicator(this.mHandler);
        this.mImsRILAdapter.unregisterForVideoCapabilityIndicator(this.mHandler);
        this.mImsRILAdapter.unregisterForEctResult(this.mHandler);
        this.mImsRILAdapter.unregisterForImsConfInfoUpdate(this.mHandler);
        this.mImsRILAdapter.unregisterForNotAvailable(this.mHandler);
        this.mImsRILAdapter.unregisterForSpeechCodecInfo(this.mHandler);
        this.mImsRILAdapter.unregisterForImsRedialEccInd(this.mHandler);
        this.mImsRILAdapter.unregisterForSipHeaderInd(this.mHandler);
        this.mImsRILAdapter.unregisterForCallRatIndication(this.mHandler);
        this.mImsRILAdapter.unregisterForCallAdditionalInfo(this.mHandler);
        this.mImsRILAdapter.unregisterForRttCapabilityIndicator(this.mHandler);
        this.mImsRILAdapter.unregisterForRttModifyResponse(this.mHandler);
        this.mImsRILAdapter.unregisterForRttTextReceive(this.mHandler);
        this.mImsRILAdapter.unregisterForRttModifyRequestReceive(this.mHandler);
        this.mImsRILAdapter.unregisterForRttAudioIndicator(this.mHandler);
        this.mImsRILAdapter.unSetOnSuppServiceNotification(this.mHandler);
        this.mImsRILAdapter.unregisterForVideoRingtoneInfo(this.mHandler);
        this.mParticipants.clear();
        if (getVideoCallProvider() != null) {
            logWithCallId("close() : Start VtProvider setUIMode", 2);
            this.mVTProvider.onSetUIMode(65536);
            this.mVTProvider.removeVideoProviderStateListener(this.mVtProviderListener);
            this.mVTProvider = null;
        }
        ImsConferenceHandler.getInstance().closeConference(this.mCallId);
        this.mConfEvtListener = null;
        this.mOneKeyparticipants = null;
        IWifiOffloadService iWifiOffloadService = this.mWfoService;
        if (!(iWifiOffloadService == null || (iWifiOffloadListenerProxy = this.mWosListener) == null)) {
            try {
                iWifiOffloadService.unregisterForHandoverEvent(iWifiOffloadListenerProxy);
            } catch (RemoteException e) {
                logWithCallId("close() : RemoteException when unregisterForHandoverEvent", 5);
            }
        }
        ImsSelfActivatorBase imsSelfActivatorBase = this.mSelfActivateHelper;
        if (imsSelfActivatorBase != null) {
            imsSelfActivatorBase.close();
            this.mSelfActivateHelper = null;
        }
        this.mCallId = null;
        this.mListener = null;
        this.mCachedUserInfo = null;
        this.mCachedSuppServiceInfo = null;
        this.mCachedVideoRingtoneButtonInfo = null;
        this.mIsNeedCacheTerminationEarly = false;
    }

    public String getCallId() {
        return this.mCallId;
    }

    public ImsCallProfile getCallProfile() {
        return this.mCallProfile;
    }

    public ImsCallProfile getLocalCallProfile() {
        return this.mLocalCallProfile;
    }

    public ImsCallProfile getRemoteCallProfile() {
        return this.mRemoteCallProfile;
    }

    public String getProperty(String name) {
        return this.mCallProfile.getCallExtra(name);
    }

    public int getState() {
        return this.mState;
    }

    public boolean isInCall() {
        return false;
    }

    public void setListener(ImsCallSessionListener listener) {
        this.mListener = listener;
        if (listener != null) {
            if (this.mIsOnTerminated) {
                logWithCallId("setListener(), session terminated, notify terminated again.", 2);
                synchronized (this.mLock) {
                    if (this.mIsNeedCacheTerminationEarly) {
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_CACHED_TERMINATE_REASON, this.mCachedTerminateReasonInfo), 100);
                        this.mCachedTerminateReasonInfo = null;
                        this.mIsNeedCacheTerminationEarly = false;
                    }
                }
            } else if (this.mCachedUserInfo != null) {
                logWithCallId("setListener(), has unhandled ImsConference CEP", 2);
                if (!this.mHandler.hasMessages(111)) {
                    this.mHandler.obtainMessage(111, this.mCachedUserInfo).sendToTarget();
                }
                this.mCachedUserInfo = null;
            }
            synchronized (this.mLock) {
                AsyncResult asyncResult = this.mCachedSuppServiceInfo;
                if (asyncResult != null) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(226, asyncResult), 1000);
                    this.mCachedSuppServiceInfo = null;
                }
            }
        } else if (this.mIsOnTerminated) {
            logWithCallId("setListener(), session terminated and no listener, close it.", 2);
            close();
        }
    }

    public void setMute(boolean muted) {
        this.mImsRILAdapter.setMute(muted, (Message) null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00aa  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00bb  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00c5  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void start(java.lang.String r16, android.telephony.ims.ImsCallProfile r17) {
        /*
            r15 = this;
            r0 = r15
            r1 = r16
            r8 = r17
            boolean r2 = r15.isCallPull(r8)
            r9 = 1
            if (r2 == 0) goto L_0x0017
            r15.pullCall(r16, r17)
            r0.mHasPendingMo = r9
            r0.mCallNumber = r1
            r15.updateShouldUpdateAddress()
            return
        L_0x0017:
            com.mediatek.ims.ImsService r2 = r0.mImsService
            java.lang.String r3 = r0.mCallId
            int r4 = r0.mPhoneId
            r5 = 0
            r2.cleanMtkCallSessionProxyIfNeed(r15, r5, r3, r4)
            java.lang.String r2 = "oir"
            int r2 = r8.getCallExtraInt(r2, r5)
            int r3 = r8.mServiceType
            r4 = 2
            if (r3 != r4) goto L_0x002e
            r3 = r9
            goto L_0x002f
        L_0x002e:
            r3 = r5
        L_0x002f:
            r0.mIsEmergencyCall = r3
            com.mediatek.ims.ImsService r3 = r0.mImsService
            int r6 = r0.mPhoneId
            int r10 = r3.getSubIdUsingPhoneId(r6)
            boolean r3 = com.mediatek.ims.ImsCommonUtil.supportMdAutoSetupIms()
            if (r3 != 0) goto L_0x006a
            com.mediatek.ims.OperatorUtils$OPID r3 = com.mediatek.ims.OperatorUtils.OPID.OP08
            int r6 = r0.mPhoneId
            boolean r3 = com.mediatek.ims.OperatorUtils.isMatched(r3, r6)
            com.mediatek.ims.OperatorUtils$OPID r6 = com.mediatek.ims.OperatorUtils.OPID.OP50
            int r7 = r0.mPhoneId
            boolean r6 = com.mediatek.ims.OperatorUtils.isMatched(r6, r7)
            r6 = r6 ^ r9
            if (r3 == 0) goto L_0x005c
            if (r2 != r9) goto L_0x005c
            java.lang.String r7 = "*82"
            boolean r7 = r1.startsWith(r7)
            if (r7 != 0) goto L_0x0062
        L_0x005c:
            boolean r7 = r0.mIsEmergencyCall
            if (r7 == 0) goto L_0x006a
            if (r6 == 0) goto L_0x006a
        L_0x0062:
            java.lang.String r7 = "start() : Prior CLIR supported, *82 or ECC is higher priority than CLIR invocation."
            r15.logWithCallId(r7, r4)
            r2 = 0
            r11 = r2
            goto L_0x006b
        L_0x006a:
            r11 = r2
        L_0x006b:
            boolean r2 = r0.mIsEmergencyCall
            if (r2 == 0) goto L_0x0090
            boolean r2 = r15.isSpecialEccNumber(r16)
            if (r2 != 0) goto L_0x0090
            boolean r2 = r15.isImsEccSupported()
            if (r2 == 0) goto L_0x0085
            com.mediatek.ims.plugin.ImsCallOemPlugin r2 = r15.getImsOemCallUtil()
            boolean r2 = r2.needHangupOtherCallWhenEccDialing()
            if (r2 == 0) goto L_0x0090
        L_0x0085:
            java.lang.String r2 = "start() : Hangup all if IMS Ecc not supported"
            r15.logWithCallId(r2, r4)
            com.mediatek.ims.ril.ImsCommandsInterface r2 = r0.mImsRILAdapter
            r3 = 0
            r2.hangupAllCall(r3)
        L_0x0090:
            boolean r2 = r15.isVideoCall(r8)
            if (r2 == 0) goto L_0x00aa
            com.mediatek.ims.ext.OpImsCallSessionProxy r2 = r0.mOpImsCallSession
            boolean r2 = r2.isValidVtDialString(r1)
            if (r2 != 0) goto L_0x00a2
            r15.rejectDial()
            return
        L_0x00a2:
            com.mediatek.ims.ext.OpImsCallSessionProxy r2 = r0.mOpImsCallSession
            java.lang.String r1 = r2.normalizeVtDialString(r1)
            r12 = r1
            goto L_0x00ab
        L_0x00aa:
            r12 = r1
        L_0x00ab:
            com.mediatek.ims.plugin.impl.ImsSelfActivatorBase r1 = r0.mSelfActivateHelper
            if (r1 == 0) goto L_0x00c5
            boolean r2 = r0.mHasTriedSelfActivation
            if (r2 != 0) goto L_0x00c5
            int r2 = r0.mPhoneId
            boolean r1 = r1.shouldProcessSelfActivation(r2)
            if (r1 == 0) goto L_0x00c5
            com.mediatek.ims.plugin.impl.ImsSelfActivatorBase r1 = r0.mSelfActivateHelper
            boolean r2 = r0.mIsEmergencyCall
            r1.doSelfActivationDial(r12, r8, r2)
            r0.mHasTriedSelfActivation = r9
            return
        L_0x00c5:
            java.lang.String r1 = "dialstring"
            int r1 = r8.getCallExtraInt(r1, r5)
            if (r1 != r4) goto L_0x00dd
            android.os.Handler r1 = r0.mHandler
            r2 = 213(0xd5, float:2.98E-43)
            android.os.Message r1 = r1.obtainMessage(r2, r9, r5)
            r0.mCallProfile = r8
            com.mediatek.ims.ril.ImsCommandsInterface r2 = r0.mImsRILAdapter
            r2.sendUSSI(r12, r1)
            return
        L_0x00dd:
            com.mediatek.ims.plugin.ImsCallOemPlugin r1 = r15.getImsOemCallUtil()
            boolean r1 = r1.needTurnOnVolteBeforeE911()
            if (r1 == 0) goto L_0x00ec
            boolean r1 = r0.mIsEmergencyCall
            r15.tryTurnOnVolteForE911(r1)
        L_0x00ec:
            boolean r1 = r15.isEnrichedCallingSupported()
            if (r1 == 0) goto L_0x00f5
            r15.setImsPreCallInfo(r8)
        L_0x00f5:
            android.telephony.ims.ImsStreamMediaProfile r1 = r8.mMediaProfile
            boolean r1 = r1.isRttCall()
            r15.setRttModeForDial(r1)
            android.os.Handler r1 = r0.mHandler
            r2 = 201(0xc9, float:2.82E-43)
            android.os.Message r13 = r1.obtainMessage(r2)
            boolean r1 = r0.mIsEmergencyCall
            com.mediatek.ims.plugin.ImsCallOemPlugin r2 = r15.getImsOemCallUtil()
            boolean r2 = r2.useNormalDialForEmergencyCall()
            if (r2 == 0) goto L_0x0115
            r1 = 0
            r14 = r1
            goto L_0x0116
        L_0x0115:
            r14 = r1
        L_0x0116:
            com.mediatek.ims.ril.ImsCommandsInterface r1 = r0.mImsRILAdapter
            boolean r6 = r15.isVideoCall(r8)
            r2 = r12
            r3 = r17
            r4 = r11
            r5 = r14
            r7 = r13
            r1.start(r2, r3, r4, r5, r6, r7)
            r0.mHasPendingMo = r9
            r0.mCallNumber = r12
            r15.updateShouldUpdateAddress()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.ImsCallSessionProxy.start(java.lang.String, android.telephony.ims.ImsCallProfile):void");
    }

    public void startConference(String[] participants, ImsCallProfile profile) {
        int clirMode = profile.getCallExtraInt("oir", 0);
        String[] strArr = participants;
        int i = clirMode;
        this.mImsRILAdapter.startConference(strArr, i, isVideoCall(profile), this.mPhoneId, this.mHandler.obtainMessage(EVENT_DIAL_CONFERENCE_RESULT));
        this.mHasPendingMo = DBG;
        this.mIsOneKeyConf = DBG;
        this.mOneKeyparticipants = participants;
        updateShouldUpdateAddress();
    }

    public void accept(int callType, ImsStreamMediaProfile profile) {
        int videoMode;
        logWithCallId("accept() : original call Type: " + this.mCallProfile.mCallType + ", accept as: " + callType, 2);
        this.mImsService.cleanMtkCallSessionProxyIfNeed(this, DBG, this.mCallId, this.mPhoneId);
        if (this.mCallProfile.getCallExtraInt("dialstring", 0) == 2) {
            String m = this.mCallProfile.getCallExtra("m");
            String str = this.mCallProfile.getCallExtra("str");
            logWithCallId("accept() : m = " + m + ", str = " + str, 2);
            ImsCallSessionListener imsCallSessionListener = this.mListener;
            if (imsCallSessionListener != null) {
                imsCallSessionListener.callSessionUssdMessageReceived(Integer.parseInt(m), str);
                if (!m.equals(String.valueOf(1)) && this.mListener != null) {
                    logWithCallId("callSessionTerminated !", 2);
                    this.mListener.callSessionTerminated(new ImsReasonInfo());
                }
            }
        } else if (this.mCallProfile.mCallType == 2) {
            this.mImsRILAdapter.accept();
        } else {
            switch (callType) {
                case 2:
                    videoMode = 1;
                    break;
                case 4:
                    videoMode = 0;
                    break;
                case 5:
                    videoMode = 3;
                    break;
                case 6:
                    videoMode = 2;
                    break;
                default:
                    videoMode = 0;
                    break;
            }
            this.mImsRILAdapter.acceptVideoCall(videoMode, Integer.parseInt(this.mCallId));
        }
    }

    public void reject(int reason) {
        if (this.mCallId != null) {
            int cause = getHangupCause(reason);
            this.mLocalTerminateReason = reason;
            if (cause <= 0) {
                this.mImsRILAdapter.reject(Integer.parseInt(this.mCallId));
            } else {
                this.mImsRILAdapter.reject(Integer.parseInt(this.mCallId), cause);
            }
        } else {
            logWithCallId("reject() : Reject Call fail since there is no call ID. Abnormal Case", 5);
        }
    }

    public void transfer(String number, boolean isConfirmationRequired) {
        this.mImsRILAdapter.unattendedCallTransfer(number, isConfirmationRequired ? 2 : 1, this.mHandler.obtainMessage(EVENT_ECT_RESULT));
        this.mIsHideHoldDuringECT = DBG;
    }

    public void transfer(ImsCallSessionImplBase otherSession) {
        this.mImsRILAdapter.explicitCallTransfer(this.mHandler.obtainMessage(EVENT_ECT_RESULT));
        this.mIsHideHoldDuringECT = DBG;
    }

    public void terminate(int reason) {
        if (!this.mOpImsCallSession.handleHangup()) {
            if (this.mCallProfile.getCallExtraInt("dialstring", 0) == 2) {
                this.mImsRILAdapter.cancelPendingUssi(this.mHandler.obtainMessage(EVENT_CANCEL_USSI_COMPLETE));
            } else if (this.mCallId != null) {
                int cause = getHangupCause(reason);
                if (this.mHangupCount <= 0) {
                    if (cause <= 0) {
                        this.mImsRILAdapter.terminate(Integer.parseInt(this.mCallId));
                    } else {
                        this.mImsRILAdapter.terminate(Integer.parseInt(this.mCallId), cause);
                    }
                    if (this.mIsMerging) {
                        terminateConferenceSession();
                    }
                } else if (cause <= 0) {
                    this.mImsRILAdapter.forceHangup(Integer.parseInt(this.mCallId));
                } else {
                    this.mImsRILAdapter.forceHangup(Integer.parseInt(this.mCallId), cause);
                }
                this.mLocalTerminateReason = reason;
                this.mHangupCount++;
                this.mState = 7;
            } else {
                logWithCallId("terminate() : Terminate Call fail since there is no call ID. Abnormal Case", 5);
                if (this.mHasPendingMo) {
                    logWithCallId("terminate() : Pending M0, wait for assign call id", 5);
                    this.mHasPendingDisconnect = DBG;
                    this.mPendingDisconnectReason = reason;
                }
            }
        }
    }

    public void hold(ImsStreamMediaProfile profile) {
        this.mImsRILAdapter.hold(Integer.parseInt(this.mCallId), this.mHandler.obtainMessage(EVENT_HOLD_RESULT));
    }

    public void resume(ImsStreamMediaProfile profile) {
        this.mImsRILAdapter.resume(Integer.parseInt(this.mCallId), this.mHandler.obtainMessage(EVENT_RESUME_RESULT));
    }

    public void merge() {
        logWithCallId("merge()", 2);
        ImsCallInfo myCallInfo = this.mImsServiceCT.getCallInfo(this.mCallId);
        ImsCallInfo beMergedCallInfo = null;
        boolean needSwapVtConfToFg = false;
        boolean needSwapConfToFg = OperatorUtils.isMatched(OperatorUtils.OPID.OP165, this.mPhoneId) || OperatorUtils.isMatched(OperatorUtils.OPID.OP152, this.mPhoneId) || OperatorUtils.isMatched(OperatorUtils.OPID.OP117, this.mPhoneId) || OperatorUtils.isMatched(OperatorUtils.OPID.OP131, this.mPhoneId) || OperatorUtils.isMatched(OperatorUtils.OPID.OP125, this.mPhoneId) || OperatorUtils.isMatched(OperatorUtils.OPID.OP136_Peru, this.mPhoneId) || OperatorUtils.isMatched(OperatorUtils.OPID.OP18, this.mPhoneId);
        if (OperatorUtils.isMatched(OperatorUtils.OPID.OP16, this.mPhoneId) || OperatorUtils.isMatched(OperatorUtils.OPID.OP18, this.mPhoneId) || OperatorUtils.isMatched(OperatorUtils.OPID.OP147, this.mPhoneId)) {
            needSwapVtConfToFg = true;
        }
        if (myCallInfo == null) {
            logWithCallId("merge() : can't find this call callInfo", 5);
            mergeFailed();
            return;
        }
        if (myCallInfo.mState == ImsCallInfo.State.ACTIVE) {
            beMergedCallInfo = this.mImsServiceCT.getCallInfo(ImsCallInfo.State.HOLDING);
        } else if (myCallInfo.mState == ImsCallInfo.State.HOLDING) {
            beMergedCallInfo = this.mImsServiceCT.getCallInfo(ImsCallInfo.State.ACTIVE);
        }
        if (beMergedCallInfo == null) {
            logWithCallId("merge() : can't find another call's callInfo", 5);
            mergeFailed();
            return;
        }
        ImsCallProfile imsCallProfile = this.mCallProfile;
        if (imsCallProfile != null && imsCallProfile.isVideoCall()) {
            needSwapConfToFg = needSwapVtConfToFg;
        }
        logWithCallId("merge() : merge command- my call: conference type=" + myCallInfo.mIsConference + " call status=" + myCallInfo.mState + " beMergedCall: conference type=" + beMergedCallInfo.mIsConference + " call status=" + beMergedCallInfo.mState + " needSwapConfToFg=" + needSwapConfToFg, 2);
        this.mMergeCallId = myCallInfo.mCallId;
        this.mMergeCallStatus = myCallInfo.mState;
        this.mMergedCallId = beMergedCallInfo.mCallId;
        this.mMergedCallStatus = beMergedCallInfo.mState;
        this.mIsMerging = DBG;
        DefaultConferenceHandler confHdler = ImsConferenceHandler.getInstance();
        if (!myCallInfo.mIsConferenceHost && !beMergedCallInfo.mIsConferenceHost) {
            this.mImsRILAdapter.merge(this.mHandler.obtainMessage(EVENT_MERGE_RESULT));
            this.mIsHideHoldEventDuringMerging = DBG;
            this.mNormalCallsMerge = DBG;
            confHdler.firstMerge(myCallInfo.mCallId, beMergedCallInfo.mCallId, myCallInfo.mCallNum, beMergedCallInfo.mCallNum);
        } else if (myCallInfo.mIsConferenceHost && beMergedCallInfo.mIsConferenceHost) {
            logWithCallId("merge() : conference call merge conference call", 2);
            this.mImsRILAdapter.inviteParticipantsByCallId(Integer.parseInt(this.mCallId), beMergedCallInfo, this.mHandler.obtainMessage(EVENT_ADD_CONFERENCE_RESULT));
        } else {
            confHdler.tryAddParticipant(myCallInfo.mIsConferenceHost ? beMergedCallInfo.mCallNum : myCallInfo.mCallNum);
            if (!needSwapConfToFg) {
                if (myCallInfo.mIsConferenceHost) {
                    logWithCallId("merge() : active conference call merge background normal call", 2);
                    this.mImsRILAdapter.inviteParticipantsByCallId(Integer.parseInt(this.mCallId), beMergedCallInfo, this.mHandler.obtainMessage(EVENT_ADD_CONFERENCE_RESULT));
                    return;
                }
                logWithCallId("merge() : active normal call merge background conference call", 2);
                this.mImsRILAdapter.inviteParticipantsByCallId(Integer.parseInt(beMergedCallInfo.mCallId), myCallInfo, this.mHandler.obtainMessage(EVENT_ADD_CONFERENCE_RESULT));
            } else if (myCallInfo.mIsConferenceHost && myCallInfo.mState == ImsCallInfo.State.ACTIVE) {
                logWithCallId("merge() : active conference call merge background normal call", 2);
                this.mImsRILAdapter.inviteParticipantsByCallId(Integer.parseInt(this.mCallId), beMergedCallInfo, this.mHandler.obtainMessage(EVENT_ADD_CONFERENCE_RESULT));
            } else if (!beMergedCallInfo.mIsConferenceHost || beMergedCallInfo.mState != ImsCallInfo.State.ACTIVE) {
                logWithCallId("merge() : swapping before merge", 2);
                this.mImsRILAdapter.swap(this.mHandler.obtainMessage(EVENT_SWAP_BEFORE_MERGE_RESULT));
            } else {
                logWithCallId("merge() : beMergedCall in foreground merge bg normal call", 2);
                this.mImsRILAdapter.inviteParticipantsByCallId(Integer.parseInt(beMergedCallInfo.mCallId), myCallInfo, this.mHandler.obtainMessage(EVENT_ADD_CONFERENCE_RESULT));
            }
        }
    }

    public void update(int callType, ImsStreamMediaProfile profile) {
    }

    public void extendToConference(String[] participants) {
    }

    /* access modifiers changed from: package-private */
    public void explicitCallTransferWithCallback(Message result, Messenger target) {
        this.mEctMsg = result;
        this.mEctTarget = target;
        transfer((ImsCallSessionImplBase) null);
    }

    public void inviteParticipants(String[] participants) {
        Message result = this.mHandler.obtainMessage(EVENT_ADD_CONFERENCE_RESULT);
        this.mPendingParticipantInfoIndex = 0;
        this.mPendingParticipantInfo = participants;
        int length = participants.length;
        this.mPendingParticipantStatistics = length;
        if (this.mCallId == null || length == 0) {
            logWithCallId("inviteParticipants() : fail since no call ID or participants is null CallID=" + this.mCallId + " Participant number=" + this.mPendingParticipantStatistics, 5);
            ImsCallSessionListener imsCallSessionListener = this.mListener;
            if (imsCallSessionListener != null) {
                try {
                    imsCallSessionListener.callSessionInviteParticipantsRequestFailed(new ImsReasonInfo());
                } catch (RuntimeException e) {
                    logWithCallId("RuntimeException callSessionInviteParticipantsRequestFailed()", 5);
                }
            }
        } else {
            ImsConferenceHandler.getInstance().tryAddParticipant(this.mPendingParticipantInfo[this.mPendingParticipantInfoIndex]);
            this.mImsRILAdapter.inviteParticipants(Integer.parseInt(this.mCallId), this.mPendingParticipantInfo[this.mPendingParticipantInfoIndex], result);
        }
    }

    public void removeParticipants(String[] participants) {
        Message result = this.mHandler.obtainMessage(EVENT_REMOVE_CONFERENCE_RESULT);
        this.mPendingParticipantInfoIndex = 0;
        this.mPendingParticipantInfo = participants;
        int length = participants.length;
        this.mPendingParticipantStatistics = length;
        if (this.mCallId == null || length == 0) {
            logWithCallId("removeParticipants() : fail since no call ID or participants is null CallID=" + this.mCallId + " Participant number=" + this.mPendingParticipantStatistics, 5);
            ImsCallSessionListener imsCallSessionListener = this.mListener;
            if (imsCallSessionListener != null) {
                try {
                    imsCallSessionListener.callSessionRemoveParticipantsRequestFailed(new ImsReasonInfo());
                } catch (RuntimeException e) {
                    logWithCallId("RuntimeException callSessionRemoveParticipantsRequestFailed()", 5);
                }
            }
        } else {
            String addr = participants[0];
            this.mImsRILAdapter.removeParticipants(Integer.parseInt(this.mCallId), getConfParticipantUri(addr), result);
            ImsConferenceHandler.getInstance().tryRemoveParticipant(addr);
        }
    }

    public void sendDtmf(char c, Message result) {
        this.mDtmfMsg = result;
        this.mImsRILAdapter.sendDtmf(c, this.mHandler.obtainMessage(EVENT_DTMF_DONE));
    }

    public void startDtmf(char c) {
        this.mImsRILAdapter.startDtmf(c, (Message) null);
    }

    public void stopDtmf() {
        this.mImsRILAdapter.stopDtmf((Message) null);
    }

    public void sendUssd(String ussdMessage) {
        this.mImsRILAdapter.sendUSSI(ussdMessage, this.mHandler.obtainMessage(EVENT_SEND_USSI_COMPLETE, 1, 0));
    }

    public IImsVideoCallProvider getVideoCallProvider() {
        logWithCallId("getVideoCallProvider() : mVTProvider = " + this.mVTProvider, 2);
        ImsVTProvider imsVTProvider = this.mVTProvider;
        if (imsVTProvider != null) {
            return imsVTProvider.getInterface();
        }
        return null;
    }

    public boolean isMultiparty() {
        if (this.mCallProfile.getCallExtraInt(ImsCallProfileEx.EXTRA_MPTY, 0) == 1) {
            return DBG;
        }
        return false;
    }

    public boolean isIncomingCallMultiparty() {
        if (this.mCallProfile.getCallExtraInt(ImsCallProfileEx.EXTRA_INCOMING_MPTY, 0) == 1) {
            return DBG;
        }
        return false;
    }

    public void approveEccRedial(boolean isAprroved) {
        this.mImsRILAdapter.approveEccRedial((int) isAprroved, Integer.parseInt(this.mCallId), (Message) null);
        if (isAprroved) {
            turnOffAirplaneMode();
        }
    }

    private static String toHex(int n) {
        StringBuilder b = new StringBuilder();
        if (n < 0) {
            n += 256;
        }
        b.append(HEX.charAt(n >> 4));
        b.append(HEX.charAt(n & 15));
        return b.toString();
    }

    public void sendRttMessage(String rttMessage) {
        if (isRttSupported()) {
            int callId = Integer.parseInt(this.mCallId);
            if (rttMessage != null) {
                int length = rttMessage.length();
                int utf8_len = 0;
                try {
                    byte[] bytes_utf8 = rttMessage.getBytes("utf-8");
                    if (bytes_utf8 != null) {
                        utf8_len = bytes_utf8.length;
                    }
                    StringBuilder sbuild = new StringBuilder();
                    for (byte b : bytes_utf8) {
                        sbuild.append(toHex(new Byte(b).intValue()));
                    }
                    String encodeText = sbuild.toString();
                    logWithCallId("sendRttMessage rttMessage= " + sensitiveEncode(rttMessage) + " len =" + sensitiveEncode(String.valueOf(length)) + " = " + sensitiveEncode(encodeText) + " encodeText.length= " + sensitiveEncode(String.valueOf(bytes_utf8.length)), 2);
                    if (encodeText != null && utf8_len > 0) {
                        this.mImsRILAdapter.sendRttText(callId, encodeText, utf8_len, (Message) null);
                    }
                } catch (UnsupportedEncodingException e) {
                    logWithCallId("sendRttMessage: UnSupportedEncodingException", 5);
                }
            }
        }
    }

    public void sendRttModifyRequest(ImsCallProfile to) {
        logWithCallId("sendRttModifyRequest() : to = " + to, 2);
        if (isRttSupported()) {
            int callId = Integer.parseInt(this.mCallId);
            if (to == null) {
                logWithCallId("sendRttModifyRequest invalid ImsCallProfile", 5);
            } else if (to.mMediaProfile.isRttCall()) {
                logWithCallId("sendRttModifyRequest upgrade mCallId= " + this.mCallId, 2);
                this.mImsRILAdapter.sendRttModifyRequest(callId, 1, (Message) null);
                this.mEnableSendRttBom = DBG;
            } else {
                logWithCallId("sendRttModifyRequest downgrade mCallId= " + this.mCallId, 2);
                this.mImsRILAdapter.sendRttModifyRequest(callId, 0, (Message) null);
            }
        }
    }

    public void sendRttModifyResponse(boolean response) {
        if (isRttSupported()) {
            int callId = Integer.parseInt(this.mCallId);
            logWithCallId("sendRttModifyResponse = " + response, 2);
            this.mImsRILAdapter.setRttModifyRequestResponse(callId, (int) (response ^ 1), (Message) null);
            if (response) {
                this.mEnableSendRttBom = DBG;
            }
        }
    }

    private boolean isRttSupported() {
        return ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).isRttSupported();
    }

    private void setRttModeForDial(boolean isRtt) {
        logWithCallId("setRttModeForDial + isRtt: " + isRtt + " mCallId = " + this.mCallId, 2);
        if (isRttSupported()) {
            if (isRtt) {
                logWithCallId("setRttModeForDial setRttMode 1", 2);
                this.mImsRILAdapter.setRttMode(1, (Message) null);
                this.mEnableSendRttBom = DBG;
                return;
            }
            logWithCallId("setRttModeForDial setRttMode 2", 2);
            this.mImsRILAdapter.setRttMode(2, (Message) null);
        }
    }

    public void callTerminated() {
        detailLog("callTerminated() : mCallNumber = " + sensitiveEncode(this.mCallNumber));
        if (isMultiparty()) {
            ImsConferenceHandler.getInstance().closeConference(getCallId());
        }
        int i = this.mState;
        if (i == -1 || i == 8 || (i == 7 && !isUserPerfromedHangup())) {
            logWithCallId("callTerminated() : mState is INVALID, return: " + this.mState, 2);
            return;
        }
        if (OperatorUtils.isMatched(OperatorUtils.OPID.OP08, this.mPhoneId)) {
            notifyNotRingingMtIfRequired();
        }
        this.mIsOnTerminated = DBG;
        if (this.mListener == null) {
            synchronized (this.mLock) {
                this.mIsNeedCacheTerminationEarly = DBG;
                logWithCallId("callTerminated() :mIsNeedCacheTerminationEarly is true", 2);
            }
        }
        this.mState = 8;
        if (this.mHasPendingMo) {
            this.mHasPendingMo = false;
            this.mCallErrorState = CallErrorState.DIAL;
        } else {
            this.mCallErrorState = CallErrorState.DISCONNECT;
        }
        if (this.mImsReasonInfo != null) {
            logWithCallId("callTerminated() : get disconnect cause directly from +ESIPCPI", 2);
            notifyCallSessionTerminated(this.mImsReasonInfo);
        } else {
            logWithCallId("callTerminated() : get disconnect cause from AT+CEER", 2);
            this.mImsRILAdapter.getLastCallFailCause(this.mPhoneId, this.mHandler.obtainMessage(105));
        }
        updateCallStateForWifiOffload(this.mState);
    }

    private void notifyNotRingingMtIfRequired() {
        if (this.mSipSessionProgress && this.mState == 0 && this.mContext != null) {
            this.mCallProfile.setCallExtra("android.telephony.ims.extra.CALL_DISCONNECT_CAUSE", String.valueOf(WfcReasonInfo.CODE_WFC_403_FORBIDDEN));
            int serviceId = this.mImsService.mapPhoneIdToServiceId(this.mPhoneId);
            Bundle extras = new Bundle();
            extras.putString("android:imsCallID", this.mCallId);
            extras.putString("android:imsDialString", this.mCallNumber);
            extras.putInt("android:imsServiceId", serviceId);
            this.mImsService.notifyIncomingCallSession(this.mPhoneId, getServiceImpl(), extras);
            this.mImsReasonInfo = new ImsReasonInfo(WfcReasonInfo.CODE_WFC_403_FORBIDDEN, 0);
            return;
        }
        logWithCallId("notifyNotRingingMtIfRequired: sipSessionProgress = " + this.mSipSessionProgress + ", state = " + this.mState + ", mContext = " + this.mContext, 2);
    }

    private class MyHandler extends Handler {
        private static final String PAU_END_FLAG_FIELD = ">";
        private static final String PAU_NAME_FIELD = "<name:";
        private static final String PAU_NUMBER_FIELD = "<tel:";
        private static final String PAU_SIP_NUMBER_FIELD = "<sip:";
        private static final String PAU_VERSTAT_FIELD = "verstat=";

        public MyHandler(Looper looper) {
            super(looper, (Handler.Callback) null, ImsCallSessionProxy.DBG);
        }

        private String getDisplayNameFromPau(String pau) {
            char aChar;
            String newPau = pau.replace("\\\\", "\\").replaceAll(ImsCallSessionProxy.TAG_DOUBLE_QUOTE, "\"");
            String value = "";
            if (TextUtils.isEmpty(newPau)) {
                ImsCallSessionProxy.this.logWithCallId("getDisplayNameFromPau() : pau is null !", 2);
                return value;
            }
            int index = 0;
            while (index < newPau.length() && (aChar = newPau.charAt(index)) != '<') {
                value = value + aChar;
                index++;
            }
            return value;
        }

        private String getFieldValueFromPau(String pau, String field) {
            if (TextUtils.isEmpty(pau) || TextUtils.isEmpty(field) || !pau.contains(field)) {
                return "";
            }
            int startIndex = pau.indexOf(field) + field.length();
            int endIndex = pau.indexOf(PAU_END_FLAG_FIELD, startIndex);
            if (endIndex < 0) {
                return pau.substring(startIndex);
            }
            return pau.substring(startIndex, endIndex);
        }

        private int imsReasonInfoCodeFromRilReasonCode(int causeCode) {
            ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
            imsCallSessionProxy.logWithCallId("imsReasonInfoCodeFromRilReasonCode() : causeCode = " + causeCode, 2);
            switch (causeCode) {
                case 1:
                    return 1515;
                case 3:
                    return 333;
                case 6:
                case 65:
                    return 340;
                case 8:
                    return 339;
                case 17:
                    return 338;
                case 18:
                    return 202;
                case 19:
                    return RadioError.OEM_ERROR_2;
                case 21:
                    return 361;
                case 28:
                    return 337;
                case 29:
                case 43:
                case 127:
                    return 354;
                case 31:
                    return RadioError.OEM_ERROR_10;
                case 34:
                case LastCallFailCause.REQUESTED_FACILITY_NOT_IMPLEMENTED:
                case 111:
                    return 351;
                case 38:
                case 42:
                case 47:
                case 63:
                case 88:
                    return 352;
                case 41:
                case 44:
                    return 336;
                case 49:
                    return 340;
                case 55:
                case 57:
                    return 332;
                case 58:
                    if (ImsCallSessionProxy.this.mWfoService != null && ImsCallSessionProxy.this.mRatType == 2) {
                        try {
                            if (!ImsCallSessionProxy.this.mWfoService.isWifiConnected()) {
                                ImsCallSessionProxy.this.logWithCallId("imsReasonInfoCodeFromRilReasonCode() : Rat is Wifi, Wifi is disconnected, ret=SIGNAL_LOST", 2);
                                return LastCallFailCause.OEM_CAUSE_11;
                            }
                        } catch (RemoteException e) {
                            ImsCallSessionProxy.this.logWithCallId("imsReasonInfoCodeFromRilReasonCode() : RemoteException in isWifiConnected()", 5);
                        }
                    }
                    return 354;
                case 68:
                    return 141;
                case 81:
                    return 342;
                case 102:
                    return 335;
                case LastCallFailCause.CALL_BARRED:
                    return 102;
                case LastCallFailCause.FDN_BLOCKED:
                    return LastCallFailCause.FDN_BLOCKED;
                case LastCallFailCause.IMEI_NOT_ACCEPTED:
                    return LastCallFailCause.IMEI_NOT_ACCEPTED;
                case 260:
                    return 1512;
                case 380:
                    if (ImsCommonUtil.supportMdAutoSetupIms()) {
                        return 1514;
                    }
                    return LastCallFailCause.OEM_CAUSE_1;
                default:
                    int wfcReason = getWfcDisconnectCause(causeCode);
                    if (wfcReason != -1) {
                        return wfcReason;
                    }
                    int serviceState = ImsCallSessionProxy.this.mImsService.getImsServiceState(ImsCallSessionProxy.this.mPhoneId);
                    ImsCallSessionProxy imsCallSessionProxy2 = ImsCallSessionProxy.this;
                    imsCallSessionProxy2.logWithCallId("imsReasonInfoCodeFromRilReasonCode() : serviceState = " + serviceState, 2);
                    if (serviceState == 3) {
                        return 111;
                    }
                    if (serviceState == 1 && !PhoneNumberUtils.isEmergencyNumber(ImsCallSessionProxy.this.mCallNumber)) {
                        return 106;
                    }
                    if (causeCode == 16) {
                        return RadioError.OEM_ERROR_10;
                    }
                    return 0;
            }
        }

        private void updateImsReasonInfo(AsyncResult ar) {
            String[] sipMessage = (String[]) ar.result;
            if (sipMessage != null && sipMessage[3] != null && sipMessage[4] != null && ImsCallSessionProxy.this.mCallId != null && sipMessage[0].equals(ImsCallSessionProxy.this.mCallId)) {
                ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
                imsCallSessionProxy.detailLog("updateImsReasonInfo() : receive sip method = " + sipMessage[3] + " cause = " + sipMessage[4] + " reason header = " + sipMessage[5]);
                SipMessage msg = new SipMessage(sipMessage);
                updateRestrictHDForRemoteCallProfile(msg.getCode(), msg.getReasonHeader());
                ImsReasonInfo opReasonInfo = ImsCallSessionProxy.this.mOpImsCallSession.getImsReasonInfo(msg);
                if (opReasonInfo == null) {
                    opReasonInfo = ImsCallSessionProxy.this.getOpImsReasonInfo(msg);
                }
                if (opReasonInfo != null) {
                    ImsReasonInfo unused = ImsCallSessionProxy.this.mImsReasonInfo = opReasonInfo;
                    return;
                }
                switch (msg.getCode()) {
                    case 0:
                        if (msg.getMethod() == 4 && msg.getReasonHeader() != null && ImsCallSessionProxy.this.isRemoteCallDecline(msg.getReasonHeader())) {
                            ImsReasonInfo unused2 = ImsCallSessionProxy.this.mImsReasonInfo = new ImsReasonInfo(1404, 0, msg.getReasonHeader());
                            return;
                        } else if (msg.getMethod() == 4 && msg.getReasonHeader() != null && ImsCallSessionProxy.this.isAnsweredElsewhere(msg.getReasonHeader())) {
                            ImsReasonInfo unused3 = ImsCallSessionProxy.this.mImsReasonInfo = new ImsReasonInfo(1014, 0, msg.getReasonHeader());
                            return;
                        } else if (msg.getMethod() == 7 && msg.getReasonHeader() != null && msg.getReasonHeader().equals(SipMessage.PULLED_AWAY_HEADER)) {
                            ImsReasonInfo unused4 = ImsCallSessionProxy.this.mImsReasonInfo = new ImsReasonInfo(1016, 0, msg.getReasonHeader());
                            return;
                        } else if (msg.getDir() == 0 && msg.getType() == 0 && msg.getMethod() == 1 && msg.getReasonHeader() != null && (msg.getReasonHeader().equals(SipMessage.VIDEO_CALL_NOT_AVAILABLE_HEADER) || msg.getReasonHeader().equals(SipMessage.VIDEO_CALL_UNAVAILABLE_HEADER))) {
                            Rlog.d(ImsCallSessionProxy.LOG_TAG, msg.getReasonHeader());
                            ImsCallSessionProxy imsCallSessionProxy2 = ImsCallSessionProxy.this;
                            imsCallSessionProxy2.logWithCallId("updateImsReasonInfo() : " + msg.getReasonHeader(), 2);
                            ImsReasonInfo unused5 = ImsCallSessionProxy.this.mImsReasonInfo = new ImsReasonInfo(44, 0, msg.getReasonHeader());
                            return;
                        } else if (msg.getMethod() != 7 || msg.getDir() != 0 || msg.getReasonHeader() == null || !msg.getReasonHeader().toLowerCase().equals(SipMessage.NO_RTP_TIMEOUT_HEADER)) {
                            if (msg.getMethod() == 3 && msg.getDir() == 1 && msg.getType() == 0) {
                                String unused6 = ImsCallSessionProxy.this.mCachedCauseText = msg.getReasonHeader();
                                Rlog.d(ImsCallSessionProxy.LOG_TAG, "mCachedCauseText=" + ImsCallSessionProxy.this.mCachedCauseText);
                                return;
                            }
                            return;
                        } else if (!ImsCallSessionProxy.this.shouldNotifyCallDropByBadWifiQuality()) {
                            return;
                        } else {
                            if (ImsCallSessionProxy.this.getWifiRssi() < ImsCallSessionProxy.this.mBadRssiThreshould) {
                                ImsReasonInfo unused7 = ImsCallSessionProxy.this.mImsReasonInfo = new ImsReasonInfo(LastCallFailCause.OEM_CAUSE_2, 0, msg.getReasonHeader());
                                return;
                            } else {
                                ImsReasonInfo unused8 = ImsCallSessionProxy.this.mImsReasonInfo = new ImsReasonInfo(LastCallFailCause.OEM_CAUSE_3, 0, msg.getReasonHeader());
                                return;
                            }
                        }
                    case SipMessage.CODE_SESSION_RINGING:
                        if (msg.getDir() == 1 && msg.getType() == 1 && msg.getMethod() == 1) {
                            String unused9 = ImsCallSessionProxy.this.mCachedCauseText = msg.getReasonHeader();
                            Rlog.d(ImsCallSessionProxy.LOG_TAG, "mCachedCauseText=" + ImsCallSessionProxy.this.mCachedCauseText);
                            return;
                        }
                        return;
                    case SipMessage.CODE_SESSION_PROGRESS:
                        if (msg.getDir() == 0 && msg.getType() == 1) {
                            boolean unused10 = ImsCallSessionProxy.this.mSipSessionProgress = ImsCallSessionProxy.DBG;
                        }
                        if (msg.getDir() == 1 && msg.getType() == 1 && msg.getMethod() == 1) {
                            String unused11 = ImsCallSessionProxy.this.mCachedCauseText = msg.getReasonHeader();
                            Rlog.d(ImsCallSessionProxy.LOG_TAG, "mCachedCauseText=" + ImsCallSessionProxy.this.mCachedCauseText);
                            return;
                        }
                        return;
                    case ImsVTProvider.ConnectionEx.VideoProvider.SESSION_MODIFY_CANCEL_UPGRADE_FAIL /*200*/:
                        if (msg.getReasonHeader() != null && msg.getReasonHeader().toLowerCase().equals(SipMessage.CALL_COMPLETED_ELSEWHERE_HEADER)) {
                            ImsReasonInfo unused12 = ImsCallSessionProxy.this.mImsReasonInfo = new ImsReasonInfo(1014, 0, msg.getReasonHeader());
                            return;
                        }
                        return;
                    case 403:
                        if (msg.getMethod() == 1 && msg.getReasonHeader() != null && msg.getReasonHeader().length() != 0) {
                            if (msg.getReasonHeader().equals(SipMessage.CALL_MAXIMUM_ALREADY_REACHED)) {
                                ImsReasonInfo unused13 = ImsCallSessionProxy.this.mImsReasonInfo = new ImsReasonInfo(1403, 0, msg.getReasonHeader());
                                return;
                            } else if (OperatorUtils.isMatched(OperatorUtils.OPID.OP07, ImsCallSessionProxy.this.mPhoneId)) {
                                ImsReasonInfo unused14 = ImsCallSessionProxy.this.mImsReasonInfo = new ImsReasonInfo(1623, 0, msg.getReasonHeader());
                                return;
                            } else {
                                return;
                            }
                        } else {
                            return;
                        }
                    case SipMessage.CODE_SESSION_INVITE_FAILED_REMOTE_BUSY:
                        if (msg.getMethod() == 1 && msg.getDir() == 1) {
                            ImsReasonInfo unused15 = ImsCallSessionProxy.this.mImsReasonInfo = new ImsReasonInfo(338, 0, msg.getReasonHeader());
                            if (ImsCallSessionProxy.this.mMtkImsCallSessionProxy != null) {
                                ImsCallSessionProxy.this.mMtkImsCallSessionProxy.notifyCallSessionBusy();
                                return;
                            }
                            return;
                        }
                        return;
                    case RadioError.OEM_ERROR_3:
                        if (msg.getMethod() == 1 && msg.getReasonHeader() != null && msg.getReasonHeader().length() != 0 && OperatorUtils.isMatched(OperatorUtils.OPID.OP07, ImsCallSessionProxy.this.mPhoneId)) {
                            ImsReasonInfo unused16 = ImsCallSessionProxy.this.mImsReasonInfo = new ImsReasonInfo(1622, 0, msg.getReasonHeader());
                            return;
                        }
                        return;
                    case 603:
                        if (msg.getReasonHeader() != null && msg.getReasonHeader().toLowerCase().equals(SipMessage.CALL_DECLINED_HEADER)) {
                            ImsReasonInfo unused17 = ImsCallSessionProxy.this.mImsReasonInfo = new ImsReasonInfo(1404, 0, msg.getReasonHeader());
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        }

        private void updateRestrictHDForRemoteCallProfile(int sipCode, String reasonHeader) {
            if (OperatorUtils.isMatched(OperatorUtils.OPID.OP12, ImsCallSessionProxy.this.mPhoneId)) {
                Rlog.d(ImsCallSessionProxy.LOG_TAG, "updateRestrictHDForRemoteCallProfile: check for op12");
                if ((sipCode == 180 || sipCode == 200) && reasonHeader != null && reasonHeader.toLowerCase().equals("<call_w_mmtel_icsi_tag>")) {
                    Rlog.d(ImsCallSessionProxy.LOG_TAG, "updateRestrictHDForRemoteCallProfile");
                    ImsCallSessionProxy.this.mRemoteCallProfile.mRestrictCause = 0;
                    ImsCallSessionProxy.this.notifyCallSessionUpdated();
                }
            }
        }

        private boolean isCallModeUpdated(int callMode, int videoState) {
            ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
            imsCallSessionProxy.logWithCallId("isCallModeUpdated() : callMode:" + callMode + "videoState:" + videoState, 2);
            boolean isChanged = false;
            int oldCallMode = ImsCallSessionProxy.this.mCallProfile.mCallType;
            if (ImsCallSessionProxy.this.mVideoState != videoState) {
                int unused = ImsCallSessionProxy.this.mVideoState = videoState;
                ImsCallSessionProxy.this.updateVideoDirection();
                isChanged = ImsCallSessionProxy.DBG;
            }
            ImsCallSessionProxy imsCallSessionProxy2 = ImsCallSessionProxy.this;
            imsCallSessionProxy2.updateCallType(callMode, imsCallSessionProxy2.mVideoState);
            if (ImsCallSessionProxy.this.mCallProfile.mCallType != oldCallMode) {
                isChanged = ImsCallSessionProxy.DBG;
                if (ImsCallSessionProxy.this.mVTProvider != null) {
                    ImsCallSessionProxy.this.mVTProvider.onUpdateProfile(ImsCallProfile.getVideoStateFromCallType(ImsCallSessionProxy.this.mCallProfile.mCallType));
                }
            }
            boolean supportUpgradeWhenVoiceConf = OperatorUtils.isMatched(OperatorUtils.OPID.OP07, ImsCallSessionProxy.this.mPhoneId);
            if ((callMode == 22 || callMode == 24) && !supportUpgradeWhenVoiceConf) {
                isChanged |= ImsCallSessionProxy.this.removeRemoteCallVideoCapability();
            }
            if ((callMode == 20 || callMode == 22 || callMode == 24 || callMode == 25) && isChanged && ImsCallSessionProxy.this.mVTProvider != null) {
                ImsCallSessionProxy imsCallSessionProxy3 = ImsCallSessionProxy.this;
                imsCallSessionProxy3.logWithCallId("isCallModeUpdated() : Start setUIMode old: " + oldCallMode, 2);
                ImsCallSessionProxy.this.mVTProviderUtil.setUIMode(ImsCallSessionProxy.this.mVTProvider, 6);
                ImsCallSessionProxy imsCallSessionProxy4 = ImsCallSessionProxy.this;
                imsCallSessionProxy4.logWithCallId("isCallModeUpdated() : End setUIMode new: " + ImsCallSessionProxy.this.mCallProfile.mCallType, 2);
            }
            return isChanged;
        }

        private void retrieveMergeFail() {
            if (!ImsCallSessionProxy.this.mIsMerging || ImsCallSessionProxy.this.mIsRetrievingMergeFail) {
                ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
                imsCallSessionProxy.logWithCallId("retrieveMergeFail() : Shouldn't retrieve merge fail, mIsMerging:" + ImsCallSessionProxy.this.mIsMerging + ", mIsRetrievingMergeFail:" + ImsCallSessionProxy.this.mIsRetrievingMergeFail, 4);
                return;
            }
            ImsCallInfo mergeCallInfo = null;
            ImsCallInfo mergedCallInfo = null;
            boolean isNotifyMergeFail = false;
            boolean unused = ImsCallSessionProxy.this.mIsRetrievingMergeFail = ImsCallSessionProxy.DBG;
            ImsConferenceHandler.getInstance().modifyParticipantFailed();
            ImsCallSessionProxy.this.logWithCallId("retrieveMergeFail()", 2);
            if (ImsCallSessionProxy.this.mMergeCallId != null && !ImsCallSessionProxy.this.mMergeCallId.equals("")) {
                mergeCallInfo = ImsCallSessionProxy.this.mImsServiceCT.getCallInfo(ImsCallSessionProxy.this.mMergeCallId);
            }
            if (ImsCallSessionProxy.this.mMergedCallId != null && !ImsCallSessionProxy.this.mMergedCallId.equals("")) {
                mergedCallInfo = ImsCallSessionProxy.this.mImsServiceCT.getCallInfo(ImsCallSessionProxy.this.mMergedCallId);
            }
            if (mergeCallInfo != null && mergedCallInfo != null) {
                ImsCallSessionProxy imsCallSessionProxy2 = ImsCallSessionProxy.this;
                imsCallSessionProxy2.logWithCallId("retrieveMergeFail() : MergeCallInfo: callId=" + mergeCallInfo.mCallId + " call status=" + mergeCallInfo.mState + " MergedCallInfo: callId=" + mergedCallInfo.mCallId + " call status=" + mergedCallInfo.mState, 2);
                if (mergeCallInfo.mState == ImsCallInfo.State.ACTIVE && mergedCallInfo.mState == ImsCallInfo.State.HOLDING) {
                    isNotifyMergeFail = ImsCallSessionProxy.DBG;
                } else if (mergeCallInfo.mState == ImsCallInfo.State.ACTIVE && mergedCallInfo.mState == ImsCallInfo.State.ACTIVE) {
                    ImsCallSessionProxy.this.logWithCallId("retrieveMergeFail() : two active call and hold merged call", 2);
                    ImsCallSessionProxy.this.mImsRILAdapter.hold(Integer.parseInt(ImsCallSessionProxy.this.mMergedCallId), ImsCallSessionProxy.this.mHandler.obtainMessage(211));
                } else if (mergeCallInfo.mState == ImsCallInfo.State.HOLDING && mergedCallInfo.mState == ImsCallInfo.State.HOLDING) {
                    ImsCallSessionProxy.this.logWithCallId("retrieveMergeFail() : two hold call and resume merge call", 2);
                    ImsCallSessionProxy.this.mImsRILAdapter.resume(Integer.parseInt(ImsCallSessionProxy.this.mMergeCallId), ImsCallSessionProxy.this.mHandler.obtainMessage(211));
                } else {
                    isNotifyMergeFail = ImsCallSessionProxy.DBG;
                }
            } else if (mergeCallInfo == null || mergedCallInfo == null) {
                if (mergeCallInfo != null) {
                    ImsCallSessionProxy.this.logWithCallId("retrieveMergeFail() : only merge call is left", 2);
                    if (mergeCallInfo.mState != ImsCallInfo.State.ACTIVE) {
                        ImsCallSessionProxy.this.mImsRILAdapter.resume(Integer.parseInt(ImsCallSessionProxy.this.mMergeCallId), ImsCallSessionProxy.this.mHandler.obtainMessage(211));
                    } else {
                        isNotifyMergeFail = ImsCallSessionProxy.DBG;
                    }
                } else if (mergedCallInfo != null) {
                    ImsCallSessionProxy.this.logWithCallId("retrieveMergeFail() : only merged call is left", 2);
                    if (mergedCallInfo.mState != ImsCallInfo.State.HOLDING) {
                        ImsCallSessionProxy.this.mImsRILAdapter.hold(Integer.parseInt(ImsCallSessionProxy.this.mMergedCallId), ImsCallSessionProxy.this.mHandler.obtainMessage(211));
                    } else {
                        isNotifyMergeFail = ImsCallSessionProxy.DBG;
                    }
                } else {
                    isNotifyMergeFail = ImsCallSessionProxy.DBG;
                }
            }
            if (isNotifyMergeFail) {
                boolean unused2 = ImsCallSessionProxy.this.mIsRetrievingMergeFail = false;
                ImsCallSessionProxy.this.mergeFailed();
            }
        }

        public void handleMessage(Message msg) {
            int msgType;
            char c;
            int causeNum;
            int i;
            ImsReasonInfo imsReasonInfo;
            Message message = msg;
            int callMode = 255;
            ImsCallSessionProxy.this.detailLog("handleMessage() : receive message = " + ImsCallSessionProxy.this.event2String(message.what));
            DefaultConferenceHandler instance = ImsConferenceHandler.getInstance();
            int i2 = message.what;
            boolean z = ImsCallSessionProxy.DBG;
            switch (i2) {
                case 102:
                    String[] callInfo = (String[]) ((AsyncResult) message.obj).result;
                    if (callInfo[1] == null || callInfo[1].equals("")) {
                        msgType = 0;
                    } else {
                        msgType = Integer.parseInt(callInfo[1]);
                    }
                    if (callInfo[5] != null && !callInfo[5].equals("")) {
                        callMode = Integer.parseInt(callInfo[5]);
                    }
                    if (ImsCallSessionProxy.this.mCallId != null && ImsCallSessionProxy.this.mCallId.equals(callInfo[0])) {
                        if (!ImsCallSessionProxy.this.mIsWaitingClose || ImsCallSessionProxy.this.mIsMerging) {
                            ImsServiceCallTracker access$3300 = ImsCallSessionProxy.this.mImsServiceCT;
                            ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
                            access$3300.processCallInfoIndication(callInfo, imsCallSessionProxy, imsCallSessionProxy.mCallProfile);
                        } else {
                            ImsCallSessionProxy.this.logWithCallId("EVENT_CALL_INFO_INDICATION : mIsWaitingClose is " + ImsCallSessionProxy.this.mIsWaitingClose + ", count: " + ImsCallSessionProxy.this.mWrongEcpiCount, 4);
                            if (ImsCallSessionProxy.this.mWrongEcpiCount < 5) {
                                ImsCallSessionProxy.access$4008(ImsCallSessionProxy.this);
                                return;
                            } else {
                                ImsCallSessionProxy.this.close();
                                return;
                            }
                        }
                    }
                    if (ImsCallSessionProxy.this.mIsMerging && !callInfo[0].equals(ImsCallSessionProxy.this.mCallId)) {
                        switch (msgType) {
                            case ImsCallSessionProxy.CALL_INFO_MSG_TYPE_MO_CALL_ID_ASSIGN /*130*/:
                                ImsCallSessionProxy.this.logWithCallId("handleMessage() : conference assign call id = " + callInfo[0], 2);
                                ImsCallProfile imsCallProfile = new ImsCallProfile();
                                if (callInfo[5] != null && !callInfo[5].equals("")) {
                                    callMode = Integer.parseInt(callInfo[5]);
                                }
                                if (callMode == 21 || callMode == 23 || callMode == 25) {
                                    imsCallProfile.mCallType = 4;
                                } else {
                                    imsCallProfile.mCallType = 2;
                                }
                                ImsCallSessionProxy.this.sendCallEventWithRat(msgType);
                                if (callInfo[6] == null || callInfo[6].equals("")) {
                                    imsCallProfile.setCallExtraInt("oir", 2);
                                } else {
                                    imsCallProfile.setCallExtra("oi", callInfo[6]);
                                    imsCallProfile.setCallExtra("remote_uri", callInfo[6]);
                                    imsCallProfile.setCallExtraInt("oir", 2);
                                }
                                imsCallProfile.setCallExtra("CallRadioTech", ImsCallSessionProxy.this.mCallProfile.getCallExtra("CallRadioTech"));
                                ImsCallSessionProxy.this.createConferenceSession(imsCallProfile, callInfo[0]);
                                if (ImsCallSessionProxy.this.mMtkConfSessionProxy != null) {
                                    ImsCallSessionProxy.this.mImsServiceCT.processCallInfoIndication(callInfo, ImsCallSessionProxy.this.mMtkConfSessionProxy.getAospCallSessionProxy(), imsCallProfile);
                                } else if (ImsCallSessionProxy.this.mConfSessionProxy != null) {
                                    ImsCallSessionProxy.this.mImsServiceCT.processCallInfoIndication(callInfo, ImsCallSessionProxy.this.mConfSessionProxy, imsCallProfile);
                                } else {
                                    ImsCallSessionProxy.this.logWithCallId("handleMessage() : conference not create callSession", 2);
                                }
                                if (ImsCallSessionProxy.this.mHangupHostDuringMerge) {
                                    boolean unused = ImsCallSessionProxy.this.mHangupHostDuringMerge = false;
                                    ImsCallSessionProxy.this.terminateConferenceSession();
                                    return;
                                }
                                return;
                            default:
                                return;
                        }
                    } else if (ImsCallSessionProxy.this.mCallId != null && ImsCallSessionProxy.this.mCallId.equals(callInfo[0])) {
                        ImsCallSessionProxy imsCallSessionProxy2 = ImsCallSessionProxy.this;
                        StringBuilder sb = new StringBuilder();
                        sb.append("EVENT_CALL_INFO_INDICATION: msgType ");
                        sb.append(msgType);
                        sb.append(", mCallNumber = ");
                        ImsCallSessionProxy imsCallSessionProxy3 = ImsCallSessionProxy.this;
                        sb.append(imsCallSessionProxy3.sensitiveEncode(imsCallSessionProxy3.mCallNumber));
                        imsCallSessionProxy2.detailLog(sb.toString());
                        if ((ImsCallSessionProxy.this.mShouldUpdateAddressFromEcpi || TextUtils.isEmpty(ImsCallSessionProxy.this.mCallNumber)) && callInfo[6] != null && !callInfo[6].equals("")) {
                            String ecpiCallNumber = callInfo[6].replace("*31#", "").replace("#31#", "");
                            if (!ecpiCallNumber.equals(ImsCallSessionProxy.this.mCallNumber)) {
                                String unused2 = ImsCallSessionProxy.this.mCallNumber = ecpiCallNumber;
                            }
                        }
                        boolean isCallDisplayUpdated = updateCallDisplayFromNumberOrPau(ImsCallSessionProxy.this.mCallNumber, callInfo[8]);
                        updateOIR(ImsCallSessionProxy.this.mCallNumber, callInfo[8]);
                        int serviceId = ImsCallSessionProxy.this.mImsService.mapPhoneIdToServiceId(ImsCallSessionProxy.this.mPhoneId);
                        switch (msgType) {
                            case 0:
                                int unused3 = ImsCallSessionProxy.this.mState = 3;
                                boolean unused4 = ImsCallSessionProxy.this.mMTSetup = ImsCallSessionProxy.DBG;
                                if (callInfo[5] != null && !callInfo[5].equals("")) {
                                    callMode = Integer.parseInt(callInfo[5]);
                                }
                                ImsCallSessionProxy imsCallSessionProxy4 = ImsCallSessionProxy.this;
                                boolean unused5 = imsCallSessionProxy4.updateRat(imsCallSessionProxy4.mImsService.getRatType(ImsCallSessionProxy.this.mPhoneId), 0);
                                ImsCallSessionProxy imsCallSessionProxy5 = ImsCallSessionProxy.this;
                                imsCallSessionProxy5.updateCallType(callMode, imsCallSessionProxy5.mVideoState);
                                ImsCallSessionProxy.this.sendCallEventWithRat(msgType);
                                updateMultipartyState(callMode);
                                if (!ImsCallSessionProxy.this.mIsRingingRedirect) {
                                    Bundle extras = new Bundle();
                                    extras.putString("android:imsCallID", ImsCallSessionProxy.this.mCallId);
                                    extras.putString("android:imsDialString", callInfo[6]);
                                    extras.putInt("android:imsServiceId", serviceId);
                                    ImsCallSessionProxy.this.mImsService.notifyIncomingCallSession(ImsCallSessionProxy.this.mPhoneId, ImsCallSessionProxy.this.getServiceImpl(), extras);
                                } else if (ImsCallSessionProxy.this.mMtkImsCallSessionProxy != null) {
                                    ImsCallSessionProxy.this.mMtkImsCallSessionProxy.notifyCallSessionRinging(ImsCallSessionProxy.this.mCallProfile);
                                }
                                boolean unused6 = ImsCallSessionProxy.this.mEnableSendRttBom = ImsCallSessionProxy.DBG;
                                int mtVideoIbt = 0;
                                if (callInfo[2] != null) {
                                    mtVideoIbt = Integer.parseInt(callInfo[2]);
                                }
                                ImsCallSessionProxy.this.logWithCallId("ECPI 0, mtVideoIbt = " + mtVideoIbt, 2);
                                if (mtVideoIbt == 3) {
                                    ImsCallSessionProxy.this.mRemoteCallProfile.mMediaProfile.mVideoDirection = mtVideoIbt;
                                    ImsCallSessionProxy.this.notifyCallSessionUpdated();
                                    return;
                                }
                                return;
                            case 2:
                                int isIbt = ImsCallSessionProxy.this.updateIsIbt(callInfo);
                                updateMultipartyState(callMode);
                                ImsCallSessionProxy.this.updateOutgoingVideoRingtone(callMode, isIbt);
                                ImsCallSessionProxy.this.sendCallEventWithRat(msgType);
                                if (ImsCallSessionProxy.this.mState != 7) {
                                    int unused7 = ImsCallSessionProxy.this.mState = 2;
                                    if (ImsCallSessionProxy.this.mListener != null) {
                                        try {
                                            ImsCallSessionProxy.this.mListener.callSessionProgressing(ImsCallSessionProxy.this.mCallProfile.mMediaProfile);
                                        } catch (RuntimeException e) {
                                            ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionRemoveParticipantsRequestFailed()", 5);
                                        }
                                    }
                                    boolean unused8 = ImsCallSessionProxy.this.mHasPendingMo = false;
                                    if (isCallDisplayUpdated) {
                                        ImsCallSessionProxy.this.notifyCallSessionUpdated();
                                    }
                                }
                                if (callInfo[9] != null && !callInfo[9].equals("")) {
                                    try {
                                        causeNum = Integer.parseInt(callInfo[9]);
                                        i = 2;
                                    } catch (NumberFormatException e2) {
                                        ImsCallSessionProxy imsCallSessionProxy6 = ImsCallSessionProxy.this;
                                        StringBuilder sb2 = new StringBuilder();
                                        NumberFormatException numberFormatException = e2;
                                        sb2.append("Invalid Number Format:");
                                        sb2.append(callInfo[9]);
                                        i = 2;
                                        imsCallSessionProxy6.logWithCallId(sb2.toString(), 2);
                                        causeNum = 0;
                                    }
                                    ImsCallSessionProxy.this.logWithCallId("causeNum = " + causeNum, i);
                                    if (causeNum != 0) {
                                        ImsCallSessionProxy imsCallSessionProxy7 = ImsCallSessionProxy.this;
                                        imsCallSessionProxy7.notifyNotificationRingtone(causeNum, imsCallSessionProxy7.mCachedCauseText);
                                        return;
                                    }
                                    return;
                                }
                                return;
                            case 6:
                                int unused9 = ImsCallSessionProxy.this.mState = 4;
                                ImsCallSessionProxy.this.mCallProfile.mMediaProfile.mAudioDirection = 3;
                                updateMultipartyState(callMode);
                                if (ImsCallSessionProxy.this.mState != 7) {
                                    if (ImsCallSessionProxy.this.mListener != null) {
                                        try {
                                            if (ImsCallSessionProxy.this.mHasPendingMo) {
                                                ImsCallSessionProxy.this.mListener.callSessionProgressing(ImsCallSessionProxy.this.mCallProfile.mMediaProfile);
                                            }
                                            ImsCallSessionProxy.this.mListener.callSessionInitiated(ImsCallSessionProxy.this.mCallProfile);
                                        } catch (RuntimeException e3) {
                                            ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionProgressing()/callSessionInitiated()", 5);
                                        }
                                    }
                                    boolean unused10 = ImsCallSessionProxy.this.mHasPendingMo = false;
                                }
                                if (ImsCallSessionProxy.this.mVTProvider != null) {
                                    ImsCallSessionProxy.this.mVTProvider.onReceiveCallSessionEvent(6);
                                }
                                boolean notifyCallSessionUpdate = false;
                                int oldCallType = ImsCallSessionProxy.this.mCallProfile.mCallType;
                                ImsCallSessionProxy imsCallSessionProxy8 = ImsCallSessionProxy.this;
                                imsCallSessionProxy8.updateCallType(callMode, imsCallSessionProxy8.mVideoState);
                                ImsCallSessionProxy.this.sendCallEventWithRat(msgType);
                                if (ImsCallSessionProxy.this.mCallProfile.mCallType != oldCallType) {
                                    notifyCallSessionUpdate = ImsCallSessionProxy.DBG;
                                }
                                if (ImsCallSessionProxy.this.mVTProvider != null) {
                                    ImsCallSessionProxy.this.mVTProvider.onUpdateProfile(ImsCallProfile.getVideoStateFromCallType(ImsCallSessionProxy.this.mCallProfile.mCallType));
                                }
                                if (notifyCallSessionUpdate) {
                                    ImsCallSessionProxy.this.notifyCallSessionUpdated();
                                }
                                ImsCallSessionProxy imsCallSessionProxy9 = ImsCallSessionProxy.this;
                                imsCallSessionProxy9.updateCallStateForWifiOffload(imsCallSessionProxy9.mState);
                                ImsCallSessionProxy.this.checkAndSendRttBom();
                                return;
                            case 7:
                                int videoIbt = 0;
                                if (callInfo[2] != null) {
                                    videoIbt = Integer.parseInt(callInfo[2]);
                                }
                                ImsCallSessionProxy.this.logWithCallId("videoIbt= " + videoIbt, 2);
                                ImsCallSessionProxy.this.updateIncomingVideoRingtone(callMode, videoIbt);
                                ImsCallSessionProxy.this.notifyCallSessionUpdated();
                                return;
                            case 131:
                                ImsCallSessionProxy.this.sendCallEventWithRat(msgType);
                                boolean isVideoCapUpdated = ImsCallSessionProxy.this.correctVideoCapabilityForCallState();
                                if (ImsCallSessionProxy.this.mListener != null && ImsCallSessionProxy.this.mState != 7) {
                                    if (!ImsCallSessionProxy.this.mIsHideHoldEventDuringMerging && !ImsCallSessionProxy.this.mIsHideHoldDuringECT && !ImsCallSessionProxy.this.mOpImsCallSession.isDeviceSwitching()) {
                                        try {
                                            ImsCallSessionProxy.this.mListener.callSessionHeld(ImsCallSessionProxy.this.mCallProfile);
                                        } catch (RuntimeException e4) {
                                            ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionHeld()", 5);
                                        }
                                    } else if (isCallDisplayUpdated || isVideoCapUpdated) {
                                        ImsCallSessionProxy.this.notifyCallSessionUpdated();
                                    }
                                    if (ImsCallSessionProxy.this.mIsHideHoldEventDuringMerging) {
                                        boolean unused11 = ImsCallSessionProxy.this.mNeedHideResumeEventDuringMerging = ImsCallSessionProxy.DBG;
                                        return;
                                    }
                                    return;
                                }
                                return;
                            case ImsCallSessionProxy.CALL_INFO_MSG_TYPE_ACTIVE /*132*/:
                                ImsCallSessionProxy.this.sendCallEventWithRat(msgType);
                                boolean isVideoCapUpdated2 = ImsCallSessionProxy.this.correctVideoCapabilityForCallState();
                                if (ImsCallSessionProxy.this.mListener == null) {
                                    return;
                                }
                                if (ImsCallSessionProxy.this.mState == 4) {
                                    if (!ImsCallSessionProxy.this.mNeedHideResumeEventDuringMerging) {
                                        try {
                                            ImsCallSessionProxy.this.mListener.callSessionResumed(ImsCallSessionProxy.this.mCallProfile);
                                        } catch (RuntimeException e5) {
                                            ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionResumed()", 5);
                                        }
                                    }
                                    if (ImsCallSessionProxy.this.mVTProvider != null) {
                                        ImsCallSessionProxy.this.mVTProvider.onReceiveCallSessionEvent(ImsCallSessionProxy.CALL_INFO_MSG_TYPE_ACTIVE);
                                        return;
                                    }
                                    return;
                                } else if (isCallDisplayUpdated || isVideoCapUpdated2) {
                                    ImsCallSessionProxy.this.notifyCallSessionUpdated();
                                    return;
                                } else {
                                    return;
                                }
                            case ImsCallSessionProxy.CALL_INFO_MSG_TYPE_DISCONNECTED /*133*/:
                                ImsCallSessionProxy.this.sendCallEventWithRat(msgType);
                                boolean hasHoldCall = ImsCallSessionProxy.this.mImsServiceCT.getCallInfo(ImsCallInfo.State.HOLDING) != null ? ImsCallSessionProxy.DBG : false;
                                if (!ImsCallSessionProxy.this.mHasPendingMo || ImsCallSessionProxy.this.mMtkImsCallSessionProxy == null || !ImsCallSessionProxy.this.mOpImsCallSession.handleCallStartFailed(ImsCallSessionProxy.this.mMtkImsCallSessionProxy.getServiceImpl(), ImsCallSessionProxy.this.mImsRILAdapter, hasHoldCall)) {
                                    ImsCallSessionProxy.this.callTerminated();
                                    return;
                                }
                                return;
                            case ImsCallSessionProxy.CALL_INFO_MSG_TYPE_REMOTE_HOLD /*135*/:
                                ImsCallSessionProxy.this.sendCallEventWithRat(msgType);
                                boolean isVideoCapUpdated3 = ImsCallSessionProxy.this.correctVideoCapabilityForCallState();
                                int unused12 = ImsCallSessionProxy.this.updateIsIbt(callInfo);
                                ImsCallSessionProxy.this.notifyRemoteHeld();
                                if (isCallDisplayUpdated || isVideoCapUpdated3) {
                                    ImsCallSessionProxy.this.notifyCallSessionUpdated();
                                    return;
                                }
                                return;
                            case ImsCallSessionProxy.CALL_INFO_MSG_TYPE_REMOTE_RESUME /*136*/:
                                ImsCallSessionProxy.this.sendCallEventWithRat(msgType);
                                boolean isVideoCapUpdated4 = ImsCallSessionProxy.this.correctVideoCapabilityForCallState();
                                int unused13 = ImsCallSessionProxy.this.updateIsIbt(callInfo);
                                ImsCallSessionProxy.this.notifyRemoteResumed();
                                if (isCallDisplayUpdated || isVideoCapUpdated4) {
                                    ImsCallSessionProxy.this.notifyCallSessionUpdated();
                                    return;
                                }
                                return;
                            case 137:
                                handleRttECCRedialEvent();
                                return;
                            default:
                                return;
                        }
                    } else if (ImsCallSessionProxy.this.mCallId == null && msgType == 130) {
                        ImsCallSessionProxy.this.logWithCallId("handleMessage() : receive 130 URC, assign call_id = " + callInfo[0], 2);
                        ImsServiceCallTracker access$33002 = ImsCallSessionProxy.this.mImsServiceCT;
                        ImsCallSessionProxy imsCallSessionProxy10 = ImsCallSessionProxy.this;
                        access$33002.processCallInfoIndication(callInfo, imsCallSessionProxy10, imsCallSessionProxy10.mCallProfile);
                        if (ImsCallSessionProxy.this.mMtkImsCallSessionProxy != null) {
                            ImsCallSessionProxy.this.mMtkImsCallSessionProxy.notifyCallSessionCalling();
                        }
                        boolean isCallDisplayUpdated2 = updateCallDisplayFromNumberOrPau(ImsCallSessionProxy.this.mCallNumber, callInfo[8]);
                        updateOIR(ImsCallSessionProxy.this.mCallNumber, callInfo[8]);
                        if (!ImsCommonUtil.supportMdAutoSetupIms()) {
                            ImsCallSessionProxy imsCallSessionProxy11 = ImsCallSessionProxy.this;
                            c = 0;
                            boolean unused14 = imsCallSessionProxy11.updateRat(imsCallSessionProxy11.mImsService.getRatType(ImsCallSessionProxy.this.mPhoneId), 0);
                        } else {
                            c = 0;
                        }
                        int unused15 = ImsCallSessionProxy.this.mState = 3;
                        String unused16 = ImsCallSessionProxy.this.mCallId = callInfo[c];
                        ImsCallSessionProxy.this.sendCallEventWithRat(msgType);
                        if (ImsCallSessionProxy.this.mVTProvider != null) {
                            ImsCallSessionProxy.this.mVTProviderUtil.bind(ImsCallSessionProxy.this.mVTProvider, Integer.parseInt(ImsCallSessionProxy.this.mCallId), ImsCallSessionProxy.this.mPhoneId);
                        }
                        if (ImsCallSessionProxy.this.mIsOneKeyConf) {
                            ImsConferenceHandler.getInstance().startConference(ImsCallSessionProxy.this.mContext, new ConferenceEventListener(), callInfo[0], ImsCallSessionProxy.this.mPhoneId);
                            ImsConferenceHandler.getInstance().addLocalCache(ImsCallSessionProxy.this.mOneKeyparticipants);
                            String[] unused17 = ImsCallSessionProxy.this.mOneKeyparticipants = null;
                        }
                        if (isCallDisplayUpdated2 || ImsCallSessionProxy.this.mRatType == 2) {
                            ImsCallSessionProxy.this.notifyCallSessionUpdated();
                        }
                        ImsCallSessionProxy.this.updateCallStateForWifiOffload(3);
                        if (ImsCallSessionProxy.this.mHasPendingDisconnect) {
                            boolean unused18 = ImsCallSessionProxy.this.mHasPendingDisconnect = false;
                            ImsCallSessionProxy imsCallSessionProxy12 = ImsCallSessionProxy.this;
                            imsCallSessionProxy12.terminate(imsCallSessionProxy12.mPendingDisconnectReason);
                            return;
                        }
                        return;
                    } else {
                        return;
                    }
                case 104:
                    handleEconfIndication((String[]) ((AsyncResult) message.obj).result);
                    return;
                case 105:
                    AsyncResult ar = (AsyncResult) message.obj;
                    if (ar.exception != null) {
                        imsReasonInfo = new ImsReasonInfo();
                    } else if (ImsCallSessionProxy.this.mLocalTerminateReason != 0) {
                        ImsCallSessionProxy.this.logWithCallId("handleMessage() : notify disconnect cause by mLocalTerminateReason " + ImsCallSessionProxy.this.mLocalTerminateReason, 2);
                        imsReasonInfo = new ImsReasonInfo(ImsCallSessionProxy.this.mLocalTerminateReason, 0);
                    } else if (ImsCallSessionProxy.this.mImsReasonInfo == null) {
                        ImsCallSessionProxy.this.logWithCallId("handleMessage() : get disconnect cause from +CEER", 2);
                        imsReasonInfo = new ImsReasonInfo(imsReasonInfoCodeFromRilReasonCode(((com.android.internal.telephony.LastCallFailCause) ar.result).causeCode), 0);
                    } else {
                        ImsCallSessionProxy.this.logWithCallId("handleMessage() : get disconnect cause directly from +ESIPCPI", 2);
                        imsReasonInfo = ImsCallSessionProxy.this.mImsReasonInfo;
                    }
                    ImsCallSessionProxy.this.notifyCallSessionTerminated(imsReasonInfo);
                    return;
                case 106:
                    String[] callModeInfo = (String[]) ((AsyncResult) message.obj).result;
                    ImsCallSessionProxy.this.mImsServiceCT.processCallModeChangeIndication(callModeInfo);
                    if (callModeInfo != null && callModeInfo[0].equals(ImsCallSessionProxy.this.mCallId)) {
                        int videoState = 3;
                        if (callModeInfo[1] != null && !callModeInfo[1].equals("")) {
                            callMode = Integer.parseInt(callModeInfo[1]);
                        }
                        if (callModeInfo[2] != null && !callModeInfo[2].equals("")) {
                            videoState = Integer.parseInt(callModeInfo[2]);
                        }
                        ImsCallSessionProxy.this.logWithCallId("handleMessage() : mode = " + callMode + ", video state = " + videoState, 2);
                        boolean isCallModeChanged = isCallModeUpdated(callMode, videoState);
                        boolean isCallDisplayUpdated3 = callModeInfo.length >= 5 && handlePauUpdate(callModeInfo[4]);
                        if (callMode != 25) {
                            z = false;
                        }
                        boolean shouldUpdateExtras = z;
                        if (shouldUpdateExtras) {
                            ImsCallSessionProxy.this.mCallProfile.setCallExtra(ImsCallSessionProxy.VT_PROVIDER_ID, ImsCallSessionProxy.this.mCallId);
                            ImsCallSessionProxy.this.logWithCallId("handleMessage() : setCallIDAsExtras video_provider_id = " + ImsCallSessionProxy.this.mCallId, 5);
                        }
                        if (isCallModeChanged || isCallDisplayUpdated3 || shouldUpdateExtras) {
                            ImsCallSessionProxy.this.notifyCallSessionUpdated();
                            if (isCallModeChanged) {
                                ImsCallSessionProxy imsCallSessionProxy13 = ImsCallSessionProxy.this;
                                imsCallSessionProxy13.updateCallStateForWifiOffload(imsCallSessionProxy13.mState);
                            }
                        }
                        notifyMultipartyStateChanged(callMode);
                        return;
                    }
                    return;
                case ImsCallSessionProxy.EVENT_VIDEO_CAPABILITY_INDICATION /*107*/:
                    String[] videoCapabilityInfo = (String[]) ((AsyncResult) message.obj).result;
                    int lVideoCapability = 0;
                    int rVideoCapability = 0;
                    if (videoCapabilityInfo != null && videoCapabilityInfo[0].equals(ImsCallSessionProxy.this.mCallId)) {
                        if (videoCapabilityInfo[1] != null && !videoCapabilityInfo[1].equals("")) {
                            lVideoCapability = Integer.parseInt(videoCapabilityInfo[1]);
                            if (lVideoCapability == 1) {
                                ImsCallSessionProxy.this.mLocalCallProfile.mCallType = 4;
                            } else {
                                ImsCallSessionProxy.this.mLocalCallProfile.mCallType = 2;
                            }
                        }
                        if (videoCapabilityInfo[2] != null && !videoCapabilityInfo[2].equals("")) {
                            rVideoCapability = Integer.parseInt(videoCapabilityInfo[2]);
                            if (rVideoCapability == 1) {
                                ImsCallSessionProxy.this.mRemoteCallProfile.mCallType = 4;
                            } else {
                                ImsCallSessionProxy.this.mRemoteCallProfile.mCallType = 2;
                            }
                        }
                        ImsCallSessionProxy.this.correctRemoteVideoCapabilityForVideoConference();
                        boolean supportUpgradeWhenVoiceConf = OperatorUtils.isMatched(OperatorUtils.OPID.OP07, ImsCallSessionProxy.this.mPhoneId);
                        if (ImsCallSessionProxy.this.isMultiparty()) {
                            ImsCallSessionProxy imsCallSessionProxy14 = ImsCallSessionProxy.this;
                            if (!imsCallSessionProxy14.isVideoCall(imsCallSessionProxy14.mCallProfile) && !supportUpgradeWhenVoiceConf) {
                                boolean unused19 = ImsCallSessionProxy.this.removeRemoteCallVideoCapability();
                            }
                        }
                        ImsCallSessionProxy imsCallSessionProxy15 = ImsCallSessionProxy.this;
                        int unused20 = imsCallSessionProxy15.mPreLocalVideoCapability = imsCallSessionProxy15.mLocalCallProfile.mCallType;
                        ImsCallSessionProxy imsCallSessionProxy16 = ImsCallSessionProxy.this;
                        int unused21 = imsCallSessionProxy16.mPreRemoteVideoCapability = imsCallSessionProxy16.mRemoteCallProfile.mCallType;
                        boolean unused22 = ImsCallSessionProxy.this.correctVideoCapabilityForCallState();
                        ImsCallSessionProxy.this.logWithCallId("handleMessage() : local video capability = " + lVideoCapability + ", remote video capability = " + rVideoCapability, 2);
                        ImsCallSessionProxy.this.notifyCallSessionUpdated();
                        return;
                    }
                    return;
                case ImsCallSessionProxy.EVENT_ECT_RESULT_INDICATION /*109*/:
                    if (ImsCallSessionProxy.this.mMtkImsCallSessionProxy == null || !ImsCallSessionProxy.this.mOpImsCallSession.handleDeviceSwitchResult(ImsCallSessionProxy.this.mCallId, ImsCallSessionProxy.this.mMtkImsCallSessionProxy.getServiceImpl(), (AsyncResult) message.obj)) {
                        handleEctIndication((AsyncResult) message.obj);
                        return;
                    }
                    return;
                case ImsCallSessionProxy.EVENT_RTT_CAPABILITY_INDICATION /*110*/:
                    handleRttCapabilityIndication((AsyncResult) message.obj);
                    return;
                case 111:
                    handleImsConferenceIndication((AsyncResult) message.obj);
                    return;
                case 201:
                case ImsCallSessionProxy.EVENT_DIAL_CONFERENCE_RESULT /*209*/:
                case ImsCallSessionProxy.EVENT_PULL_CALL_RESULT /*216*/:
                    handleDialResult((AsyncResult) message.obj);
                    return;
                case ImsCallSessionProxy.EVENT_HOLD_RESULT /*203*/:
                    AsyncResult ar2 = (AsyncResult) message.obj;
                    ImsCallSessionProxy imsCallSessionProxy17 = ImsCallSessionProxy.this;
                    if (ar2.exception != null) {
                        z = false;
                    }
                    imsCallSessionProxy17.logEventResult(z, "handleMessage() : receive EVENT_HOLD_RESULT");
                    if (ImsCallSessionProxy.this.mListener != null && ar2.exception != null) {
                        ImsReasonInfo imsReasonInfo2 = new ImsReasonInfo();
                        if (!(ar2.exception instanceof CommandException) || ar2.exception.getCommandError() != CommandException.Error.OEM_ERROR_1) {
                            try {
                                ImsCallSessionProxy.this.mListener.callSessionHoldFailed(imsReasonInfo2);
                                return;
                            } catch (RuntimeException e6) {
                                ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionHoldFailed()", 5);
                                return;
                            }
                        } else {
                            new ImsReasonInfo(148, 0);
                            ImsCallSessionProxy.this.logWithCallId("Call hold failed by call terminated", 5);
                            return;
                        }
                    } else {
                        return;
                    }
                case ImsCallSessionProxy.EVENT_RESUME_RESULT /*204*/:
                    AsyncResult ar3 = (AsyncResult) message.obj;
                    ImsCallSessionProxy imsCallSessionProxy18 = ImsCallSessionProxy.this;
                    if (ar3.exception != null) {
                        z = false;
                    }
                    imsCallSessionProxy18.logEventResult(z, "handleMessage() : receive EVENT_RESUME_RESULT");
                    if (ImsCallSessionProxy.this.mListener != null && ar3.exception != null) {
                        try {
                            ImsCallSessionProxy.this.mListener.callSessionResumeFailed(new ImsReasonInfo());
                            return;
                        } catch (RuntimeException e7) {
                            ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionResumeFailed()", 5);
                            return;
                        }
                    } else {
                        return;
                    }
                case ImsCallSessionProxy.EVENT_MERGE_RESULT /*205*/:
                    AsyncResult ar4 = (AsyncResult) message.obj;
                    ImsCallSessionProxy imsCallSessionProxy19 = ImsCallSessionProxy.this;
                    if (ar4.exception != null) {
                        z = false;
                    }
                    imsCallSessionProxy19.logEventResult(z, "handleMessage() : receive EVENT_MERGE_RESULT");
                    if (ImsCallSessionProxy.this.mListener != null && ar4.exception != null) {
                        ImsCallSessionProxy.this.logWithCallId("handleMessage() : ConfCreated failed", 2);
                        retrieveMergeFail();
                        return;
                    }
                    return;
                case ImsCallSessionProxy.EVENT_ADD_CONFERENCE_RESULT /*206*/:
                    AsyncResult ar5 = (AsyncResult) message.obj;
                    if (ImsCallSessionProxy.this.mIsMerging) {
                        if (ar5.exception != null) {
                            retrieveMergeFail();
                            return;
                        }
                        return;
                    } else if (!ImsCallSessionProxy.this.mIsOnTerminated) {
                        if (ar5.exception == null) {
                            boolean unused23 = ImsCallSessionProxy.this.mIsAddRemoveParticipantsCommandOK = ImsCallSessionProxy.DBG;
                            ImsConferenceHandler.getInstance().modifyParticipantComplete();
                        } else {
                            ImsConferenceHandler.getInstance().modifyParticipantFailed();
                        }
                        ImsCallSessionProxy.access$7608(ImsCallSessionProxy.this);
                        if (ImsCallSessionProxy.this.mPendingParticipantInfoIndex < ImsCallSessionProxy.this.mPendingParticipantStatistics) {
                            Message result = ImsCallSessionProxy.this.mHandler.obtainMessage(ImsCallSessionProxy.EVENT_ADD_CONFERENCE_RESULT);
                            ImsConferenceHandler.getInstance().tryAddParticipant(ImsCallSessionProxy.this.mPendingParticipantInfo[ImsCallSessionProxy.this.mPendingParticipantInfoIndex]);
                            ImsCallSessionProxy.this.mImsRILAdapter.inviteParticipants(Integer.parseInt(ImsCallSessionProxy.this.mCallId), ImsCallSessionProxy.this.mPendingParticipantInfo[ImsCallSessionProxy.this.mPendingParticipantInfoIndex], result);
                            return;
                        }
                        if (ImsCallSessionProxy.this.mListener != null) {
                            try {
                                if (!ImsCallSessionProxy.this.mIsAddRemoveParticipantsCommandOK) {
                                    ImsCallSessionProxy.this.mListener.callSessionInviteParticipantsRequestFailed(new ImsReasonInfo());
                                } else {
                                    ImsCallSessionProxy.this.mListener.callSessionInviteParticipantsRequestDelivered();
                                }
                            } catch (RuntimeException e8) {
                                ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionInviteParticipantsRequest", 5);
                            }
                        }
                        boolean unused24 = ImsCallSessionProxy.this.mIsAddRemoveParticipantsCommandOK = false;
                        return;
                    } else {
                        return;
                    }
                case ImsCallSessionProxy.EVENT_REMOVE_CONFERENCE_RESULT /*207*/:
                    AsyncResult ar6 = (AsyncResult) message.obj;
                    ImsCallSessionProxy.this.logEventResult(ar6.exception == null, "receive EVENT_REMOVE_CONFERENCE_RESULT");
                    if (!ImsCallSessionProxy.this.mIsOnTerminated) {
                        if (ar6.exception == null) {
                            boolean unused25 = ImsCallSessionProxy.this.mIsAddRemoveParticipantsCommandOK = ImsCallSessionProxy.DBG;
                            ImsConferenceHandler.getInstance().modifyParticipantComplete();
                            String unused26 = ImsCallSessionProxy.this.mRetryRemoveUri = null;
                        } else if (ImsCallSessionProxy.this.mRetryRemoveUri != null) {
                            ImsCallSessionProxy.this.mImsRILAdapter.removeParticipants(Integer.parseInt(ImsCallSessionProxy.this.mCallId), ImsCallSessionProxy.this.mRetryRemoveUri, ImsCallSessionProxy.this.mHandler.obtainMessage(ImsCallSessionProxy.EVENT_REMOVE_CONFERENCE_RESULT));
                            String unused27 = ImsCallSessionProxy.this.mRetryRemoveUri = null;
                            return;
                        } else {
                            ImsConferenceHandler.getInstance().modifyParticipantFailed();
                        }
                        ImsCallSessionProxy.access$7608(ImsCallSessionProxy.this);
                        if (ImsCallSessionProxy.this.mPendingParticipantInfoIndex < ImsCallSessionProxy.this.mPendingParticipantStatistics) {
                            ImsCallSessionProxy.this.mImsRILAdapter.removeParticipants(Integer.parseInt(ImsCallSessionProxy.this.mCallId), ImsCallSessionProxy.this.mPendingParticipantInfo[ImsCallSessionProxy.this.mPendingParticipantInfoIndex], ImsCallSessionProxy.this.mHandler.obtainMessage(ImsCallSessionProxy.EVENT_REMOVE_CONFERENCE_RESULT));
                            return;
                        }
                        if (ImsCallSessionProxy.this.mListener != null) {
                            try {
                                if (!ImsCallSessionProxy.this.mIsAddRemoveParticipantsCommandOK) {
                                    ImsCallSessionProxy.this.mListener.callSessionRemoveParticipantsRequestFailed(new ImsReasonInfo());
                                } else {
                                    ImsCallSessionProxy.this.mListener.callSessionRemoveParticipantsRequestDelivered();
                                }
                            } catch (RuntimeException e9) {
                                ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionRemoveParticipantsRequest", 5);
                            }
                        }
                        boolean unused28 = ImsCallSessionProxy.this.mIsAddRemoveParticipantsCommandOK = false;
                        return;
                    }
                    return;
                case ImsCallSessionProxy.EVENT_SIP_CODE_INDICATION /*208*/:
                    updateImsReasonInfo((AsyncResult) message.obj);
                    return;
                case ImsCallSessionProxy.EVENT_SWAP_BEFORE_MERGE_RESULT /*210*/:
                    AsyncResult ar7 = (AsyncResult) message.obj;
                    ImsCallSessionProxy imsCallSessionProxy20 = ImsCallSessionProxy.this;
                    if (ar7.exception != null) {
                        z = false;
                    }
                    imsCallSessionProxy20.logEventResult(z, "handleMessage() : receive EVENT_SWAP_BEFORE_MERGE_RESULT");
                    if (ar7.exception != null) {
                        retrieveMergeFail();
                        return;
                    }
                    ImsCallInfo myCallInfo = ImsCallSessionProxy.this.mImsServiceCT.getCallInfo(ImsCallSessionProxy.this.mCallId);
                    if (myCallInfo == null) {
                        ImsCallSessionProxy.this.logWithCallId("can't find this call callInfo", 5);
                        retrieveMergeFail();
                        return;
                    }
                    ImsCallInfo beMergedCallInfo = ImsCallSessionProxy.this.mImsServiceCT.getCallInfo(ImsCallSessionProxy.this.mMergedCallId);
                    if (beMergedCallInfo == null) {
                        ImsCallSessionProxy.this.logWithCallId("can't find this another call callInfo", 5);
                        retrieveMergeFail();
                        return;
                    } else if (myCallInfo.mIsConferenceHost) {
                        ImsCallSessionProxy.this.logWithCallId("handleMessage() : myCallI is conference, merge normal call", 2);
                        ImsCallSessionProxy.this.mImsRILAdapter.inviteParticipantsByCallId(Integer.parseInt(ImsCallSessionProxy.this.mCallId), beMergedCallInfo, ImsCallSessionProxy.this.mHandler.obtainMessage(ImsCallSessionProxy.EVENT_ADD_CONFERENCE_RESULT));
                        return;
                    } else {
                        ImsCallSessionProxy.this.logWithCallId("handleMessage() : bg conference is foreground now, merge normal call", 2);
                        ImsCallSessionProxy.this.mImsRILAdapter.inviteParticipantsByCallId(Integer.parseInt(beMergedCallInfo.mCallId), myCallInfo, ImsCallSessionProxy.this.mHandler.obtainMessage(ImsCallSessionProxy.EVENT_ADD_CONFERENCE_RESULT));
                        return;
                    }
                case 211:
                    if (ImsCallSessionProxy.this.mIsRetrievingMergeFail) {
                        boolean unused29 = ImsCallSessionProxy.this.mIsRetrievingMergeFail = false;
                    }
                    ImsCallSessionProxy.this.mergeFailed();
                    return;
                case ImsCallSessionProxy.EVENT_DTMF_DONE /*212*/:
                    if (ImsCallSessionProxy.this.mDtmfMsg != null) {
                        try {
                            Messenger dtmfMessenger = ImsCallSessionProxy.this.mDtmfMsg.replyTo;
                            ImsCallSessionProxy.this.logWithCallId("dtmfMessenger " + dtmfMessenger, 2);
                            if (dtmfMessenger != null) {
                                dtmfMessenger.send(ImsCallSessionProxy.this.mDtmfMsg);
                            }
                        } catch (RemoteException e10) {
                            ImsCallSessionProxy.this.logWithCallId("handleMessage() : RemoteException for DTMF", 5);
                        }
                    }
                    Message unused30 = ImsCallSessionProxy.this.mDtmfMsg = null;
                    return;
                case ImsCallSessionProxy.EVENT_SEND_USSI_COMPLETE /*213*/:
                    AsyncResult ar8 = (AsyncResult) message.obj;
                    if (ImsCallSessionProxy.this.mListener == null) {
                        return;
                    }
                    if (message.arg1 == 1) {
                        if (ar8 == null || ar8.exception == null) {
                            ImsCallSessionProxy.this.logWithCallId("EVENT_SEND_USSI_COMPLETE : callSessionInitiated", 2);
                            ImsCallSessionProxy.this.mListener.callSessionInitiated(ImsCallSessionProxy.this.mCallProfile);
                            ImsCallSessionProxy.this.logWithCallId("EVENT_SEND_USSI_COMPLETE : callSessionTerminated", 2);
                            ImsCallSessionProxy.this.mListener.callSessionTerminated(new ImsReasonInfo());
                            return;
                        }
                        ImsCallSessionProxy.this.logWithCallId("EVENT_SEND_USSI_COMPLETE : callSessionInitiatedFailed", 2);
                        ImsReasonInfo reason = new ImsReasonInfo();
                        if (ar8.exception instanceof CommandException) {
                            CommandException.Error err = ar8.exception.getCommandError();
                            ImsCallSessionProxy.this.logWithCallId("EVENT_SEND_USSI_COMPLETE : callSessionInitiatedFailed error:" + err, 2);
                            if (err == CommandException.Error.FDN_CHECK_FAILURE) {
                                reason = new ImsReasonInfo(LastCallFailCause.FDN_BLOCKED, 0);
                            }
                        }
                        ImsCallSessionProxy.this.mListener.callSessionInitiatedFailed(reason);
                        return;
                    } else if (ar8 != null && ar8.exception != null) {
                        ImsCallSessionProxy.this.logWithCallId("EVENT_SEND_USSI_COMPLETE : callSessionInitiatedFailed", 2);
                        ImsCallSessionProxy.this.mListener.callSessionInitiatedFailed(new ImsReasonInfo());
                        return;
                    } else {
                        return;
                    }
                case ImsCallSessionProxy.EVENT_CANCEL_USSI_COMPLETE /*214*/:
                    if (ImsCallSessionProxy.this.mListener != null) {
                        ImsCallSessionProxy.this.mListener.callSessionTerminated(new ImsReasonInfo());
                        return;
                    }
                    return;
                case ImsCallSessionProxy.EVENT_ECT_RESULT /*215*/:
                    handleEctResult((AsyncResult) message.obj);
                    return;
                case ImsCallSessionProxy.EVENT_RADIO_NOT_AVAILABLE /*217*/:
                    boolean unused31 = ImsCallSessionProxy.this.mRadioUnavailable = ImsCallSessionProxy.DBG;
                    if (ImsCallSessionProxy.this.mIsOnTerminated) {
                        ImsCallSessionProxy.this.logWithCallId("handleMessage() : EVENT_RADIO_NOT_AVAILABLE, ignore", 2);
                        return;
                    }
                    if (ImsCallSessionProxy.this.mHasPendingMo) {
                        boolean unused32 = ImsCallSessionProxy.this.mHasPendingMo = false;
                        CallErrorState unused33 = ImsCallSessionProxy.this.mCallErrorState = CallErrorState.DIAL;
                    } else {
                        CallErrorState unused34 = ImsCallSessionProxy.this.mCallErrorState = CallErrorState.DISCONNECT;
                    }
                    ImsCallSessionProxy.this.notifyCallSessionTerminated(new ImsReasonInfo(106, 0));
                    return;
                case ImsCallSessionProxy.EVENT_RTT_TEXT_RECEIVE_INDICATION /*218*/:
                    handleRttTextReceived((AsyncResult) message.obj);
                    return;
                case ImsCallSessionProxy.EVENT_RTT_MODIFY_RESPONSE /*219*/:
                    handleRttModifyResponse((AsyncResult) message.obj);
                    return;
                case ImsCallSessionProxy.EVENT_RTT_MODIFY_REQUEST_RECEIVE /*220*/:
                    handleRttModifyRequestReceived((AsyncResult) message.obj);
                    return;
                case ImsCallSessionProxy.EVENT_SPEECH_CODEC_INFO /*223*/:
                    if (ImsCallSessionProxy.this.mCallId != null) {
                        ImsCallInfo myCallInfo2 = ImsCallSessionProxy.this.mImsServiceCT.getCallInfo(ImsCallSessionProxy.this.mCallId);
                        if (myCallInfo2 == null || !(myCallInfo2.mState == ImsCallInfo.State.ACTIVE || myCallInfo2.mState == ImsCallInfo.State.ALERTING)) {
                            ImsCallSessionProxy.this.logWithCallId("skip speech not active or alerting", 2);
                            return;
                        } else {
                            ImsCallSessionProxy.this.handleSpeechCodecInfo((AsyncResult) message.obj);
                            return;
                        }
                    } else {
                        ImsCallSessionProxy.this.logWithCallId("skip speech codec update when mCallId null", 2);
                        return;
                    }
                case ImsCallSessionProxy.EVENT_REDIAL_ECC_INDICATION /*224*/:
                    ImsCallSessionProxy.this.handleRedialEccIndication((AsyncResult) message.obj);
                    return;
                case ImsCallSessionProxy.EVENT_RTT_AUDIO_INDICATION /*225*/:
                    handleRttAudioIndication((AsyncResult) message.obj);
                    return;
                case 226:
                    AsyncResult ar9 = (AsyncResult) message.obj;
                    SuppServiceNotification notification = (SuppServiceNotification) ar9.result;
                    if (ImsCallSessionProxy.this.mCallId == null || notification.index == Integer.parseInt(ImsCallSessionProxy.this.mCallId)) {
                        ImsCallSessionProxy.this.logWithCallId("handleMessage() : EVENT_ON_SUPP_SERVICE_NOTIFICATION, notify", 2);
                        final ImsSuppServiceNotification imsNotification = new ImsSuppServiceNotification(notification.notificationType, notification.code, notification.index, notification.type, notification.number, notification.history);
                        synchronized (ImsCallSessionProxy.this.mLock) {
                            if (ImsCallSessionProxy.this.mListener != null) {
                                ImsCallSessionProxy.this.mHandler.postDelayed(new Runnable() {
                                    public void run() {
                                        if (ImsCallSessionProxy.this.mListener != null) {
                                            ImsCallSessionProxy.this.mListener.callSessionSuppServiceReceived(imsNotification);
                                        }
                                    }
                                }, 1000);
                                AsyncResult unused35 = ImsCallSessionProxy.this.mCachedSuppServiceInfo = null;
                            } else {
                                AsyncResult unused36 = ImsCallSessionProxy.this.mCachedSuppServiceInfo = ar9;
                            }
                        }
                        return;
                    }
                    return;
                case ImsCallSessionProxy.EVENT_SIP_HEADER_INFO /*227*/:
                    ImsCallSessionProxy.this.handleSipHeaderInfo((AsyncResult) message.obj);
                    return;
                case ImsCallSessionProxy.EVENT_CALL_RAT_INDICATION /*228*/:
                    ImsCallSessionProxy.this.handleCallRatIndication((AsyncResult) message.obj);
                    return;
                case 229:
                    ImsCallSessionProxy.this.handleCallAdditionalInfo((AsyncResult) message.obj);
                    return;
                case ImsCallSessionProxy.EVENT_CACHED_TERMINATE_REASON /*230*/:
                    ImsCallSessionProxy.this.handleCachedTerminateReason((ImsReasonInfo) message.obj);
                    return;
                case ImsCallSessionProxy.EVENT_VIDEO_RINGTONE_INFO /*232*/:
                    ImsCallSessionProxy.this.handleVideoRingtoneInfo((AsyncResult) message.obj);
                    return;
                case ImsCallSessionProxy.EVENT_VIDEO_RINGTONE_CACHED_INFO /*233*/:
                    ImsCallSessionProxy.this.notifyCachedVideoRingtoneButtonInfo();
                    return;
                default:
                    ImsCallSessionProxy.this.logWithCallId("handleMessage() : unknown messahe, ignore", 2);
                    return;
            }
        }

        private void handleEconfIndication(String[] params) {
            String confCallId = params[0];
            String result = params[3];
            String joinedCallId = params[5];
            ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
            imsCallSessionProxy.logWithCallId("handleEconfIndication() : receive EVENT_ECONF_RESULT_INDICATION mCallId:" + ImsCallSessionProxy.this.mCallId + ", conf_call_id:" + confCallId + ", op: " + params[1] + ", number: " + ImsCallSessionProxy.this.sensitiveEncode(params[2]) + ", result: " + params[3] + ", joined_call_id:" + joinedCallId, 2);
            if (ImsCallSessionProxy.this.mCallId != null && ImsCallSessionProxy.this.mCallId.equals(joinedCallId) && result.equals("0")) {
                boolean unused = ImsCallSessionProxy.this.mMerged = ImsCallSessionProxy.DBG;
            }
            if (ImsCallSessionProxy.this.mIsMerging) {
                if (!(!result.equals("0") || ImsCallSessionProxy.this.mImsServiceCT.getCallInfo(joinedCallId) == null || ImsCallSessionProxy.this.mImsServiceCT.getCallInfo(joinedCallId).mCallSession == null)) {
                    int unused2 = ImsCallSessionProxy.this.mImsServiceCT.getCallInfo(joinedCallId).mCallSession.mState = 7;
                    ImsCallSessionProxy imsCallSessionProxy2 = ImsCallSessionProxy.this;
                    imsCallSessionProxy2.logWithCallId("handleEconfIndication() : call id " + joinedCallId + " is merged successfully", 2);
                }
                if (ImsCallSessionProxy.this.mNormalCallsMerge) {
                    if (result.equals("0") && joinedCallId != null) {
                        ImsConferenceHandler.getInstance().addFirstMergeParticipant(joinedCallId);
                    }
                    ImsCallSessionProxy.access$10208(ImsCallSessionProxy.this);
                    if (result.equals("0")) {
                        boolean unused3 = ImsCallSessionProxy.this.mThreeWayMergeSucceeded = ImsCallSessionProxy.DBG;
                    }
                    if (ImsCallSessionProxy.this.mEconfCount == 2) {
                        String ret = ImsCallSessionProxy.this.mThreeWayMergeSucceeded ? " successful" : " failed";
                        ImsCallSessionProxy imsCallSessionProxy3 = ImsCallSessionProxy.this;
                        imsCallSessionProxy3.logWithCallId("handleEconfIndication() : 3 way conference merge result is" + ret, 2);
                        if (!ImsCallSessionProxy.this.mThreeWayMergeSucceeded || !ImsConferenceHandler.getInstance().isConferenceActive()) {
                            retrieveMergeFail();
                            ImsCallSessionProxy.this.mImsRILAdapter.terminate(Integer.parseInt(confCallId));
                        } else {
                            ImsCallSessionProxy.this.mergeCompleted();
                            if (ImsCallSessionProxy.this.mMerged) {
                                ImsCallSessionProxy.this.close();
                            } else {
                                if (!(ImsCallSessionProxy.this.mImsServiceCT.getCallInfo(ImsCallSessionProxy.this.mMergedCallId) == null || ImsCallSessionProxy.this.mImsServiceCT.getCallInfo(ImsCallSessionProxy.this.mMergedCallId).mCallSession == null)) {
                                    ImsCallSessionProxy.this.mImsServiceCT.getCallInfo(ImsCallSessionProxy.this.mMergedCallId).mCallSession.close();
                                }
                                if (ImsCallSessionProxy.this.mState == 8) {
                                    ImsCallSessionProxy.this.close();
                                }
                            }
                        }
                        int unused4 = ImsCallSessionProxy.this.mEconfCount = 0;
                        boolean unused5 = ImsCallSessionProxy.this.mNormalCallsMerge = false;
                        boolean unused6 = ImsCallSessionProxy.this.mThreeWayMergeSucceeded = false;
                    }
                } else if (result.equals("0")) {
                    ImsCallSessionProxy.this.logWithCallId("handleEconfIndication() : ConfCreated successed", 2);
                    ImsCallSessionProxy.this.mergeCompleted();
                } else {
                    ImsCallSessionProxy.this.logWithCallId("handleEconfIndication() : ConfCreated failed", 2);
                    retrieveMergeFail();
                }
            }
        }

        private void handleEctResult(AsyncResult ar) {
            if (ar == null || ImsCallSessionProxy.this.mListener == null) {
                ImsCallSessionProxy.this.logWithCallId("handleEctResult() : ar or mListener is null", 2);
            } else if (ar.exception != null) {
                ImsCallSessionProxy.this.logWithCallId("handleEctResult() : explicit call transfer failed!!", 2);
                boolean unused = ImsCallSessionProxy.this.mIsHideHoldDuringECT = false;
                try {
                    if (ImsCallSessionProxy.this.mListener != null) {
                        ImsCallSessionProxy.this.mListener.callSessionTransferFailed(new ImsReasonInfo());
                    }
                } catch (RuntimeException e) {
                    ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionTransferFailed()", 5);
                }
            } else {
                ImsCallSessionProxy.this.logWithCallId("handleEctResult() : explicit call transfer succeeded!!", 2);
                try {
                    if (ImsCallSessionProxy.this.mListener != null) {
                        ImsCallSessionProxy.this.mListener.callSessionTransferred();
                    }
                } catch (RuntimeException e2) {
                    ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionTransferred()", 5);
                }
            }
        }

        private void handleEctIndication(AsyncResult ar) {
            ImsCallSessionProxy.this.logWithCallId("handleEctIndication()", 2);
            boolean unused = ImsCallSessionProxy.this.mIsHideHoldDuringECT = false;
            if (ar == null || ImsCallSessionProxy.this.mMtkImsCallSessionProxy == null) {
                ImsCallSessionProxy.this.logWithCallId("handleEctIndication() : ar or mMtkImsCallSessionProxy is null", 2);
                return;
            }
            int[] result = (int[]) ar.result;
            if (ImsCallSessionProxy.this.mCallId != null && result[0] == Integer.parseInt(ImsCallSessionProxy.this.mCallId)) {
                if (result[1] == 0) {
                    try {
                        if (ImsCallSessionProxy.this.mListener != null) {
                            ImsCallSessionProxy.this.mListener.callSessionTransferFailed(new ImsReasonInfo());
                        }
                    } catch (RuntimeException e) {
                        ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionTransferFailed()", 5);
                    }
                } else if (result[1] == 1) {
                    try {
                        if (ImsCallSessionProxy.this.mListener != null) {
                            ImsCallSessionProxy.this.mListener.callSessionTransferred();
                        }
                    } catch (RuntimeException e2) {
                        ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionTransferred()", 5);
                    }
                }
                if (!(ImsCallSessionProxy.this.mDtmfTarget == null || ImsCallSessionProxy.this.mEctMsg == null || ImsCallSessionProxy.this.mEctTarget == null)) {
                    ImsCallSessionProxy.this.mEctMsg.arg1 = result[1];
                    try {
                        ImsCallSessionProxy.this.mEctTarget.send(ImsCallSessionProxy.this.mEctMsg);
                    } catch (RemoteException e3) {
                        ImsCallSessionProxy.this.logWithCallId("handleMessage() : RemoteException for ECT", 5);
                    }
                }
                Messenger unused2 = ImsCallSessionProxy.this.mEctTarget = null;
                Message unused3 = ImsCallSessionProxy.this.mEctMsg = null;
            }
        }

        private void handleDialResult(AsyncResult ar) {
            if (ar == null || ImsCallSessionProxy.this.mListener == null) {
                ImsCallSessionProxy.this.logWithCallId("handleDialResult() : ar or mListener is null", 2);
                return;
            }
            ImsCallSessionProxy.this.logEventResult(ar.exception == null ? ImsCallSessionProxy.DBG : false, "handleDialResult()");
            if (ar.exception != null) {
                Message result = ImsCallSessionProxy.this.mHandler.obtainMessage(105);
                CallErrorState unused = ImsCallSessionProxy.this.mCallErrorState = CallErrorState.DIAL;
                ImsCallSessionProxy.this.mImsRILAdapter.getLastCallFailCause(ImsCallSessionProxy.this.mPhoneId, result);
                boolean unused2 = ImsCallSessionProxy.this.mHasPendingMo = false;
            }
        }

        private void handleImsConferenceIndication(AsyncResult ar) {
            if (ImsCallSessionProxy.this.mIsConferenceHost) {
                if (ar == null) {
                    ImsCallSessionProxy.this.logWithCallId("handleImsConferenceIndication() : ar is null", 2);
                } else if (ImsCallSessionProxy.this.mListener == null) {
                    ImsCallSessionProxy.this.logWithCallId("handleImsConferenceIndication() : mListener is null, cache info", 2);
                    AsyncResult unused = ImsCallSessionProxy.this.mCachedUserInfo = ar;
                } else {
                    ArrayList<User> result = (ArrayList) ar.result;
                    if (result.size() > 0) {
                        try {
                            ImsCallSessionProxy.this.mListener.callSessionConferenceStateUpdated(convertToImsConferenceState(result));
                        } catch (RuntimeException e) {
                            ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionConferenceStateUpdated()", 5);
                        }
                    } else {
                        ImsCallSessionProxy.this.logWithCallId("handleImsConferenceIndication() : auto terminate", 2);
                        ImsCallSessionProxy.this.terminate(0);
                    }
                }
            }
        }

        private ImsConferenceState convertToImsConferenceState(ArrayList<User> users) {
            ImsConferenceState confState = new ImsConferenceState();
            ImsCallSessionProxy.this.mParticipantsList.clear();
            int index = 1;
            for (int i = 0; i < users.size(); i++) {
                Bundle userInfo = new Bundle();
                String userAddr = users.get(i).mUserAddr;
                String endPoint = users.get(i).mEndPoint;
                userInfo.putString("user", userAddr);
                userInfo.putString("display-text", users.get(i).mDisplayText);
                boolean needIgnore = ImsCallSessionProxy.this.getBooleanFromCarrierConfig("oplus_config_ignore_display_text_bool");
                ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
                imsCallSessionProxy.detailLog("packUserInfo needIgnore " + needIgnore + ", displayText " + users.get(i).mDisplayText + ", userAddr " + userAddr);
                if ((needIgnore && !TextUtils.isEmpty(userAddr)) || !isValidCNAP(userAddr, users.get(i).mDisplayText)) {
                    ImsCallSessionProxy.this.logWithCallId("Ignore display text", 2);
                    userInfo.putString("display-text", (String) null);
                }
                userInfo.putString("endpoint", endPoint);
                userInfo.putString(NotificationCompat.CATEGORY_STATUS, users.get(i).mStatus);
                if (userAddr == null || userAddr.trim().isEmpty()) {
                    userInfo.putString("endpoint", endPoint + "_" + index);
                    confState.mParticipants.put(Integer.toString(index), userInfo);
                    ImsCallSessionProxy.this.mParticipantsList.add(Integer.toString(index));
                    index++;
                } else {
                    HashMap hashMap = confState.mParticipants;
                    hashMap.put(userAddr + "_" + endPoint, userInfo);
                    ImsCallSessionProxy.this.mParticipantsList.add(userAddr);
                }
            }
            HashMap unused = ImsCallSessionProxy.this.mParticipants = confState.mParticipants;
            return confState;
        }

        private boolean updateMultipartyState(int callMode) {
            int i = 0;
            boolean isMultipartyMode = callMode == 22 || callMode == 23 || callMode == 24 || callMode == 25;
            if (callMode == 24 || callMode == 25) {
                isMultipartyMode = ImsCallSessionProxy.this.getMultipartyModeForConfPart();
            }
            if (callMode == 22 || callMode == 23) {
                boolean unused = ImsCallSessionProxy.this.mIsConferenceHost = ImsCallSessionProxy.DBG;
            } else {
                boolean unused2 = ImsCallSessionProxy.this.mIsConferenceHost = false;
            }
            if (OperatorUtils.isMatched(OperatorUtils.OPID.OP12, ImsCallSessionProxy.this.mPhoneId) && ImsCallSessionProxy.this.mIsConferenceHost) {
                Rlog.d(ImsCallSessionProxy.LOG_TAG, "VzW: set conference no constrain for HD icon");
                ImsCallSessionProxy.this.mRemoteCallProfile.mRestrictCause = 0;
            }
            if (ImsCallSessionProxy.this.isMultiparty() == isMultipartyMode) {
                return false;
            }
            ImsCallProfile access$000 = ImsCallSessionProxy.this.mCallProfile;
            if (isMultipartyMode) {
                i = 1;
            }
            access$000.setCallExtraInt(ImsCallProfileEx.EXTRA_MPTY, i);
            return ImsCallSessionProxy.DBG;
        }

        private void notifyMultipartyStateChanged(int callMode) {
            if (updateMultipartyState(callMode)) {
                ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
                imsCallSessionProxy.logWithCallId("notifyMultipartyStateChanged() : isMultiparty(): " + ImsCallSessionProxy.this.isMultiparty(), 2);
                if (ImsCallSessionProxy.this.mListener != null) {
                    try {
                        ImsCallSessionProxy.this.mListener.callSessionMultipartyStateChanged(ImsCallSessionProxy.this.isMultiparty());
                    } catch (RuntimeException e) {
                        ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionMultipartyStateChanged()", 5);
                    }
                }
            }
        }

        private void updateOIR(String num, String pau) {
            if (ImsCallSessionProxy.this.isMultiparty()) {
                ImsCallSessionProxy.this.logWithCallId("updateOIR() : ignore update OIR for mpty call: ", 2);
                return;
            }
            int oir = 2;
            String displayName = getDisplayNameFromPau(pau);
            String payPhoneName = new String("Coin line/payphone");
            ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
            imsCallSessionProxy.logWithCallId("updateOIR() : pau: [" + ImsCallSessionProxy.this.sensitiveEncode(pau) + "], displayName: [" + ImsCallSessionProxy.this.sensitiveEncode(displayName) + "]", 2);
            if (TextUtils.isEmpty(num) && TextUtils.isEmpty(pau)) {
                oir = 1;
            } else if (!TextUtils.isEmpty(pau) && pau.toLowerCase().contains("anonymous")) {
                oir = 1;
            } else if (displayName.trim().equals(payPhoneName)) {
                ImsCallSessionProxy.this.logWithCallId("updateOIR() : payhone", 2);
                oir = 4;
            } else if (TextUtils.isEmpty(pau) && OperatorUtils.isMatched(OperatorUtils.OPID.OP12, ImsCallSessionProxy.this.mPhoneId)) {
                oir = 1;
            } else if (!TextUtils.isEmpty(pau) && pau.toLowerCase().contains("unavailable") && (OperatorUtils.isMatched(OperatorUtils.OPID.OP50, ImsCallSessionProxy.this.mPhoneId) || OperatorUtils.isMatched(OperatorUtils.OPID.OP129, ImsCallSessionProxy.this.mPhoneId))) {
                oir = 3;
            } else if (!TextUtils.isEmpty(pau) && pau.toLowerCase().contains("interaction with other service") && (OperatorUtils.isMatched(OperatorUtils.OPID.OP50, ImsCallSessionProxy.this.mPhoneId) || OperatorUtils.isMatched(OperatorUtils.OPID.OP129, ImsCallSessionProxy.this.mPhoneId))) {
                oir = 3;
            } else if (TextUtils.isEmpty(num) && OperatorUtils.isMatched(OperatorUtils.OPID.OP07, ImsCallSessionProxy.this.mPhoneId)) {
                oir = 1;
            }
            ImsCallSessionProxy.this.mCallProfile.setCallExtraInt("oir", oir);
        }

        private boolean updateAddressFromPau(String pau) {
            if (!ImsCallSessionProxy.this.mShouldUpdateAddressByPau) {
                ImsCallSessionProxy.this.logWithCallId("updateAddressFromPau() : MO call, should not update addr by PAU", 2);
                return false;
            }
            String sipField = getFieldValueFromPau(pau, PAU_SIP_NUMBER_FIELD);
            String telField = getFieldValueFromPau(pau, PAU_NUMBER_FIELD);
            ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
            imsCallSessionProxy.logWithCallId("updateAddressFromPau() : updatePau()... telNumber: " + ImsCallSessionProxy.this.sensitiveEncode(telField) + " sipNumber: " + ImsCallSessionProxy.this.sensitiveEncode(sipField), 2);
            String addr = ((TextUtils.isEmpty(sipField) || !ImsCallSessionProxy.this.mShouldUpdateAddressBySipField) ? telField : sipField).split("[;@,]+")[0];
            String currentOI = ImsCallSessionProxy.this.mCallProfile.getCallExtra("oi");
            if (TextUtils.isEmpty(addr) || PhoneNumberUtils.compareLoosely(currentOI, addr)) {
                return false;
            }
            ImsCallSessionProxy.this.mCallProfile.setCallExtra("oi", addr);
            ImsCallSessionProxy imsCallSessionProxy2 = ImsCallSessionProxy.this;
            imsCallSessionProxy2.logWithCallId("updateAddressFromPau() : updatePau()... addr: " + ImsCallSessionProxy.this.sensitiveEncode(addr), 2);
            return ImsCallSessionProxy.DBG;
        }

        public boolean isAllowCNAP(Context context, int phoneId) {
            PersistableBundle config;
            int subId = ImsCallSessionProxy.this.mImsService.getSubIdUsingPhoneId(phoneId);
            CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
            if (configManager == null || !SubscriptionManager.isValidSubscriptionId(subId) || (config = configManager.getConfigForSubId(subId)) == null || !config.getBoolean("disable_cnap_bool", false)) {
                return ImsCallSessionProxy.DBG;
            }
            ImsCallSessionProxy.this.logWithCallId("disable CNAP", 2);
            return false;
        }

        private boolean isValidCNAP(String number, String cnapName) {
            if (TextUtils.isEmpty(number) || TextUtils.isEmpty(cnapName)) {
                return ImsCallSessionProxy.DBG;
            }
            return PhoneNumberUtils.compareLoosely(number, cnapName) ^ ImsCallSessionProxy.DBG;
        }

        private boolean updateDisplayNameFromPau(String pau) {
            String displayName = getDisplayNameFromPau(pau);
            String currentDisplayName = ImsCallSessionProxy.this.mCallProfile.getCallExtra("cna");
            if (TextUtils.isEmpty(displayName)) {
                return false;
            }
            ImsCallSessionProxy.this.mCallProfile.setCallExtraInt("cnap", 2);
            if (PhoneNumberUtils.compareLoosely(currentDisplayName, displayName)) {
                return false;
            }
            if (!isAllowCNAP(ImsCallSessionProxy.this.mContext, ImsCallSessionProxy.this.mPhoneId)) {
                ImsCallSessionProxy.this.mCallProfile.setCallExtraInt("cnap", 1);
                return false;
            } else if (TextUtils.isEmpty(ImsCallSessionProxy.this.mCallNumber) || PhoneNumberUtils.compareLoosely(ImsCallSessionProxy.this.mCallNumber, displayName)) {
                ImsCallSessionProxy.this.logWithCallId("nothing happen at CNAP", 2);
                return false;
            } else {
                ImsCallSessionProxy.this.logWithCallId("enable CNAP", 2);
                ImsCallSessionProxy.this.mCallProfile.setCallExtra("cna", displayName);
                ImsCallSessionProxy.this.mCallProfile.setCallExtraInt("cnap", 2);
                ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
                imsCallSessionProxy.logWithCallId("updateDisplayNameFromPau() : diaplayName: " + displayName, 2);
                return ImsCallSessionProxy.DBG;
            }
        }

        private boolean handlePauUpdate(String pau) {
            if (TextUtils.isEmpty(pau)) {
                return false;
            }
            ImsCallSessionProxy.this.mCallProfile.setCallExtra("remote_uri", getFieldValueFromPau(pau, PAU_SIP_NUMBER_FIELD));
            return updateAddressFromPau(pau) | updateDisplayNameFromPau(pau) | updateVerstatFromPau(pau);
        }

        private boolean updateVerstatFromPau(String pau) {
            String verstatField = getFieldValueFromPau(pau, PAU_VERSTAT_FIELD);
            if (TextUtils.isEmpty(verstatField)) {
                return false;
            }
            ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
            imsCallSessionProxy.logWithCallId("updateVerstatFromPau() :" + verstatField, 2);
            int currentVerstat = ImsCallSessionProxy.this.mCallProfile.getCallerNumberVerificationStatus();
            String value = verstatField.split("[;@,]+")[0];
            if (value.contains("TN-Validation-Passed") && currentVerstat != 1) {
                ImsCallSessionProxy.this.mCallProfile.setCallerNumberVerificationStatus(1);
                ImsCallSessionProxy.this.mCallProfile.setCallExtraInt(ImsCallProfileEx.EXTRA_VERSTAT, 1);
                return ImsCallSessionProxy.DBG;
            } else if (!value.contains("TN-Validation-Failed") || currentVerstat == 2) {
                return false;
            } else {
                ImsCallSessionProxy.this.mCallProfile.setCallerNumberVerificationStatus(2);
                ImsCallSessionProxy.this.mCallProfile.setCallExtraInt(ImsCallProfileEx.EXTRA_VERSTAT, 0);
                return ImsCallSessionProxy.DBG;
            }
        }

        private boolean updateCallDisplayFromNumberOrPau(String number, String pau) {
            if (!TextUtils.isEmpty(pau) || TextUtils.isEmpty(number)) {
                return handlePauUpdate(pau);
            }
            if (ImsCallSessionProxy.this.mCallProfile.getCallExtra("oi").equals(number)) {
                return false;
            }
            ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
            imsCallSessionProxy.logWithCallId("updateCallDisplayFromNumberOrPau() : number: " + ImsCallSessionProxy.this.sensitiveEncode(number), 2);
            ImsCallSessionProxy.this.mCallProfile.setCallExtra("oi", number);
            ImsCallSessionProxy.this.mCallProfile.setCallExtra("remote_uri", number);
            return ImsCallSessionProxy.DBG;
        }

        private int getWfcDisconnectCause(int causeCode) {
            ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
            imsCallSessionProxy.logWithCallId("[WFC] getWfcDisconnectCause mRatType = " + ImsCallSessionProxy.this.mRatType, 2);
            if (ImsCallSessionProxy.this.mWfoService == null || ImsCallSessionProxy.this.mRatType != 2 || causeCode == 16) {
                return -1;
            }
            DisconnectCause disconnectCause = null;
            try {
                disconnectCause = ImsCallSessionProxy.this.mWfoService.getDisconnectCause(ImsCallSessionProxy.this.mPhoneId);
            } catch (RemoteException e) {
                ImsCallSessionProxy.this.logWithCallId("getWfcDisconnectCause() : RemoteException in getWfcDisconnectCause()", 5);
            }
            if (disconnectCause == null) {
                return -1;
            }
            int wfcErrorCause = disconnectCause.getErrorCause();
            ImsCallSessionProxy imsCallSessionProxy2 = ImsCallSessionProxy.this;
            imsCallSessionProxy2.logWithCallId("[WFC] wfcErrorCause = " + wfcErrorCause, 2);
            if (wfcErrorCause == 2001) {
                return LastCallFailCause.OEM_CAUSE_11;
            }
            if (wfcErrorCause == 2003 || wfcErrorCause == 2005) {
                return LastCallFailCause.OEM_CAUSE_13;
            }
            if (wfcErrorCause == 2004) {
                return LastCallFailCause.OEM_CAUSE_14;
            }
            return -1;
        }

        private void handleRttCapabilityIndication(AsyncResult ar) {
            String callId = ImsCallSessionProxy.this.mCallId;
            if (ar == null) {
                ImsCallSessionProxy.this.logWithCallId("handleRttCapabilityIndication ar is null", 5);
                return;
            }
            int[] result = (int[]) ar.result;
            if (ImsCallSessionProxy.this.mMtkImsCallSessionProxy != null && callId != null) {
                boolean z = false;
                if (result[0] == Integer.parseInt(callId)) {
                    int localCapability = result[1];
                    int remoteCapability = result[2];
                    int localTextStatus = result[3];
                    int realRemoteTextCapability = result[4];
                    ImsCallSessionProxy.this.logWithCallId("handleRttCapabilityIndication local cap= " + localCapability + " remo status= " + remoteCapability + " local status= " + localTextStatus + " remo cap= " + realRemoteTextCapability, 2);
                    ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
                    if (remoteCapability == 1 && localTextStatus == 1) {
                        z = true;
                    }
                    boolean unused = imsCallSessionProxy.mIsRttEnabledForCallSession = z;
                    ImsCallSessionProxy.this.processMtRttWithoutPrecondition(remoteCapability);
                    ImsCallSessionProxy.this.logWithCallId("handleRttCapabilityIndication mIsRttEnabledForCallSession: " + ImsCallSessionProxy.this.mIsRttEnabledForCallSession, 2);
                    ImsCallSessionProxy.this.mMtkImsCallSessionProxy.notifyTextCapabilityChanged(localCapability, remoteCapability, localTextStatus, realRemoteTextCapability);
                    int status = 0;
                    if (ImsCallSessionProxy.this.mIsRttEnabledForCallSession) {
                        status = 1;
                    }
                    ImsCallSessionProxy.this.mCallProfile.mMediaProfile.setRttMode(status);
                    ImsCallSessionProxy.this.notifyCallSessionUpdated();
                    ImsCallSessionProxy.this.checkAndSendRttBom();
                    ImsCallSessionProxy.this.toggleRttAudioIndication();
                }
            }
        }

        private void handleRttECCRedialEvent() {
            ImsCallSessionProxy.this.logWithCallId("notifyRttECCRedialEvent", 2);
            if (ImsCallSessionProxy.this.mMtkImsCallSessionProxy != null) {
                ImsCallSessionProxy.this.mMtkImsCallSessionProxy.notifyRttECCRedialEvent();
            }
        }

        private void handleRttTextReceived(AsyncResult ar) {
            if (ar == null) {
                ImsCallSessionProxy.this.logWithCallId("handleRttTextReceived ar is null", 5);
                return;
            }
            String[] textReceived = (String[]) ar.result;
            if (textReceived[0] == null || textReceived[1] == null || textReceived[2] == null) {
                ImsCallSessionProxy.this.logWithCallId("textReceived is null", 5);
                return;
            }
            int targetCallid = Integer.parseInt(textReceived[0]);
            if (ImsCallSessionProxy.this.mListener != null && ImsCallSessionProxy.this.mCallId != null && targetCallid == Integer.parseInt(ImsCallSessionProxy.this.mCallId)) {
                ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
                imsCallSessionProxy.logWithCallId("Received call id = " + textReceived[0] + " len = " + ImsCallSessionProxy.this.sensitiveEncode(String.valueOf(textReceived[1])) + " textMessage = " + ImsCallSessionProxy.this.sensitiveEncode(textReceived[2]) + " actual len = " + ImsCallSessionProxy.this.sensitiveEncode(String.valueOf(textReceived[2].length())), 2);
                if (textReceived[2].length() == 0 || Integer.parseInt(textReceived[1]) == 0) {
                    ImsCallSessionProxy.this.logWithCallId("handleRttTextReceived: length is 0", 5);
                    return;
                }
                String decodeText = ImsCallSessionProxy.this.mRttTextEncoder.getUnicodeFromUTF8(textReceived[2]);
                if (decodeText == null || decodeText.length() == 0) {
                    ImsCallSessionProxy.this.logWithCallId("handleRttTextReceived: decodeText length is 0", 5);
                } else {
                    ImsCallSessionProxy.this.mListener.callSessionRttMessageReceived(decodeText);
                }
            }
        }

        private void handleRttModifyResponse(AsyncResult ar) {
            int status;
            if (ar == null || ImsCallSessionProxy.this.mListener == null) {
                ImsCallSessionProxy.this.logWithCallId("handleRttModifyResponse ar or mListener is null", 5);
                return;
            }
            int[] result = (int[]) ar.result;
            if (ImsCallSessionProxy.this.mCallId != null && result[0] == Integer.parseInt(ImsCallSessionProxy.this.mCallId)) {
                int response = result[1];
                if (response == 0) {
                    ImsCallSessionProxy.this.logWithCallId("handleRttModifyResponse success", 2);
                    status = 1;
                } else {
                    ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
                    imsCallSessionProxy.logWithCallId("handleRttModifyResponse fail status = " + response, 2);
                    status = 3;
                }
                ImsCallSessionProxy.this.mListener.callSessionRttModifyResponseReceived(status);
            }
        }

        private void handleRttModifyRequestReceived(AsyncResult ar) {
            if (ar == null || ImsCallSessionProxy.this.mListener == null) {
                ImsCallSessionProxy.this.logWithCallId("handleRttModifyRequestReceived ar or mListener is null", 5);
                return;
            }
            int[] result = (int[]) ar.result;
            if (ImsCallSessionProxy.this.mCallId != null && result[0] == Integer.parseInt(ImsCallSessionProxy.this.mCallId)) {
                if (!ImsCallSessionProxy.this.isAllowRttVideoSwitch()) {
                    ImsCallSessionProxy.this.logWithCallId("handleRttModifyRequestReceived() : RTT and video not switchable", 2);
                    ImsCallSessionProxy.this.sendRttModifyResponse(false);
                    return;
                }
                int status = result[1];
                ImsCallProfile imsCallProfile = new ImsCallProfile();
                ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
                imsCallSessionProxy.logWithCallId("handleRttModifyRequestReceived status: " + status, 2);
                if (status == 1) {
                    imsCallProfile.mMediaProfile.setRttMode(1);
                } else {
                    imsCallProfile.mMediaProfile.setRttMode(0);
                    ImsCallSessionProxy.this.sendRttModifyResponse(ImsCallSessionProxy.DBG);
                }
                ImsCallSessionProxy.this.mListener.callSessionRttModifyRequestReceived(imsCallProfile);
            }
        }

        private void handleRttAudioIndication(AsyncResult ar) {
            if (ar == null || ImsCallSessionProxy.this.mListener == null) {
                ImsCallSessionProxy.this.logWithCallId("handleRttAudioIndication ar or mListener is null", 5);
                return;
            }
            int[] result = (int[]) ar.result;
            if (ImsCallSessionProxy.this.mCallId != null) {
                boolean z = false;
                if (result[0] == Integer.parseInt(ImsCallSessionProxy.this.mCallId)) {
                    int status = result[1];
                    ImsStreamMediaProfile profile = new ImsStreamMediaProfile();
                    ImsCallSessionProxy imsCallSessionProxy = ImsCallSessionProxy.this;
                    StringBuilder sb = new StringBuilder();
                    sb.append("handleRttAudioIndication audio: ");
                    sb.append(status == 0);
                    imsCallSessionProxy.logWithCallId(sb.toString(), 2);
                    if (status == 0) {
                        z = true;
                    }
                    profile.setReceivingRttAudio(z);
                    ImsCallSessionProxy.this.mListener.callSessionRttAudioIndicatorChanged(profile);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateVideoDirection() {
        int i = 0;
        this.mOverallPause = (this.mFwkPause || this.mVideoState == 0) ? DBG : false;
        ImsStreamMediaProfile imsStreamMediaProfile = this.mCallProfile.mMediaProfile;
        if (!this.mOverallPause) {
            i = getVideoDirectionFromVideoState(this.mVideoState);
        }
        imsStreamMediaProfile.mVideoDirection = i;
        logWithCallId("updateVideoDirection() : mOverallPause = " + this.mOverallPause + ", mVideoDirection = " + this.mCallProfile.mMediaProfile.mVideoDirection, 2);
    }

    private int getVideoDirectionFromVideoState(int videoState) {
        switch (videoState) {
            case 0:
                logWithCallId("getVideoDirectionFromVideoState() : Should not handle pause here", 4);
                return 0;
            case 1:
                return 2;
            case 2:
                return 1;
            case 3:
                return 3;
            default:
                return -1;
        }
    }

    /* access modifiers changed from: private */
    public void updateOutgoingVideoRingtone(int callMode, int isIbt) {
        if (SystemProperties.get("persist.vendor.vilte_support").equals("1")) {
            int callType = this.mCallProfile.mCallType;
            logWithCallId("updateOutgoingVideoRingtone(): callType = " + callType, 2);
            if (callType != 4 && callType != 6) {
                this.mRemoteCallProfile.mMediaProfile.mVideoDirection = -1;
            } else if (callMode != 21 || isIbt < 2) {
                this.mRemoteCallProfile.mMediaProfile.mVideoDirection = 0;
            } else {
                this.mRemoteCallProfile.mMediaProfile.mVideoDirection = isIbt;
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateIncomingVideoRingtone(int callMode, int isIbt) {
        if (callMode == 21 && isIbt >= 1) {
            this.mRemoteCallProfile.mMediaProfile.mVideoDirection = isIbt;
        } else if (callMode == 20 && isIbt == 1) {
            this.mRemoteCallProfile.mMediaProfile.mVideoDirection = isIbt;
        } else {
            this.mRemoteCallProfile.mMediaProfile.mVideoDirection = 0;
        }
    }

    /* access modifiers changed from: private */
    public void mergeCompleted() {
        notifyCallSessionMergeComplete();
        this.mIsMerging = false;
        this.mIsHideHoldEventDuringMerging = false;
        this.mNeedHideResumeEventDuringMerging = false;
        ImsConferenceHandler.getInstance().modifyParticipantComplete();
        ImsCallSessionProxy hostCallSession = this.mImsServiceCT.getConferenceHostCall();
        if (hostCallSession != null) {
            hostCallSession.onAddParticipantComplete();
        }
        this.mConfSessionProxy = null;
        this.mMtkConfSessionProxy = null;
    }

    /* access modifiers changed from: private */
    public void mergeFailed() {
        ImsCallSessionListener imsCallSessionListener = this.mListener;
        if (imsCallSessionListener != null) {
            try {
                imsCallSessionListener.callSessionMergeFailed(new ImsReasonInfo());
            } catch (RuntimeException e) {
                logWithCallId("RuntimeException callSessionMergeFailed()", 5);
            }
        }
        this.mMergeCallId = "";
        this.mMergeCallStatus = ImsCallInfo.State.INVALID;
        this.mMergedCallId = "";
        this.mMergedCallStatus = ImsCallInfo.State.INVALID;
        this.mIsMerging = false;
        this.mMerged = false;
        this.mIsHideHoldEventDuringMerging = false;
        this.mNeedHideResumeEventDuringMerging = false;
        closeConferenceSession();
    }

    private class IWifiOffloadListenerProxy extends WifiOffloadManager.Listener {
        private IWifiOffloadListenerProxy() {
        }

        /* synthetic */ IWifiOffloadListenerProxy(ImsCallSessionProxy x0, C01081 x1) {
            this();
        }

        public void onHandover(int simIdx, int stage, int ratType) {
            if (simIdx == ImsCallSessionProxy.this.mPhoneId && ratType != ImsCallSessionProxy.this.mRatType && stage == 1) {
                boolean unused = ImsCallSessionProxy.this.updateRat(ratType, 0);
                if (ImsCallSessionProxy.this.mListener != null) {
                    ImsCallSessionProxy.this.logWithCallId("onHandover()", 2);
                    try {
                        ImsCallSessionProxy.this.mListener.callSessionUpdated(ImsCallSessionProxy.this.mCallProfile);
                        ImsCallSessionProxy.this.mListener.callSessionHandover(ImsCallSessionProxy.this.mRatType, ratType, new ImsReasonInfo());
                    } catch (RuntimeException e) {
                        ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionHandover()", 5);
                    }
                }
            }
        }

        public void onRequestImsSwitch(int simIdx, boolean isImsOn) {
        }
    }

    /* access modifiers changed from: private */
    public void updateCallStateForWifiOffload(int callState) {
        int callType;
        int wosCallState;
        if (this.mWfoService == null) {
            logWithCallId("updateCallStateForWifiOffload() : skip, no WOS!", 2);
            return;
        }
        String str = this.mCallId;
        if (str == null) {
            logWithCallId("updateCallStateForWifiOffload() : skip, no call ID!", 2);
            return;
        }
        int callId = Integer.parseInt(str);
        if (this.mCallProfile.mCallType == 2 || this.mCallProfile.mCallType == 1) {
            callType = 1;
        } else {
            callType = 2;
        }
        switch (callState) {
            case 0:
            case 7:
            case 8:
                wosCallState = 0;
                break;
            case 1:
            case 2:
            case 3:
            case 5:
            case 6:
                wosCallState = 2;
                break;
            case 4:
                wosCallState = 1;
                break;
            default:
                logWithCallId("updateCallStateForWifiOffload() : skip, unexpected state: " + callState, 2);
                return;
        }
        try {
            this.mWfoService.updateCallState(this.mPhoneId, callId, callType, wosCallState);
        } catch (RemoteException e) {
            logWithCallId("updateCallStateForWifiOffload() : RemoteException in Wos.updateCallState()", 5);
        }
    }

    /* access modifiers changed from: private */
    public void notifyCallSessionTerminated(ImsReasonInfo info) {
        if (this.mListener == null || this.mIsNeedCacheTerminationEarly) {
            logWithCallId("notifyCallSessionTerminated() : mListener = NULL", 2);
            synchronized (this.mLock) {
                this.mCachedTerminateReasonInfo = info;
            }
            if (!this.mMTSetup && !OperatorUtils.isMatched(OperatorUtils.OPID.OP08, this.mPhoneId)) {
                logWithCallId("has not received ECPI0, close here", 2);
                close();
                return;
            }
            return;
        }
        if (this.mIsMerging && (this.mLocalTerminateReason == 501 || this.mRadioUnavailable)) {
            logWithCallId("notifyCallSessionTerminated() : close while merging", 2);
            mergeFailed();
        }
        switch (C01081.$SwitchMap$com$mediatek$ims$ImsCallSessionProxy$CallErrorState[this.mCallErrorState.ordinal()]) {
            case 1:
                if (this.mListener != null) {
                    try {
                        if (info.getCode() == 241 && getImsOemCallUtil().needReportCallTerminatedForFdn()) {
                            this.mCallId = INVALID_CALL_ID;
                            this.mListener.callSessionTerminated(info);
                            break;
                        } else {
                            this.mListener.callSessionInitiatedFailed(info);
                            break;
                        }
                    } catch (RuntimeException e) {
                        logWithCallId("RuntimeException callSessionInitiatedFailed()", 5);
                        close();
                        break;
                    }
                }
                break;
            case 2:
                ImsCallSessionListener imsCallSessionListener = this.mListener;
                if (imsCallSessionListener != null) {
                    try {
                        imsCallSessionListener.callSessionTerminated(info);
                        break;
                    } catch (RuntimeException e2) {
                        logWithCallId("RuntimeException callSessionTerminated()", 5);
                        close();
                        break;
                    }
                }
                break;
        }
        this.mIsWaitingClose = DBG;
    }

    /* renamed from: com.mediatek.ims.ImsCallSessionProxy$1 */
    static /* synthetic */ class C01081 {
        static final /* synthetic */ int[] $SwitchMap$com$mediatek$ims$ImsCallSessionProxy$CallErrorState;

        static {
            int[] iArr = new int[CallErrorState.values().length];
            $SwitchMap$com$mediatek$ims$ImsCallSessionProxy$CallErrorState = iArr;
            try {
                iArr[CallErrorState.DIAL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mediatek$ims$ImsCallSessionProxy$CallErrorState[CallErrorState.DISCONNECT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean updateRat(int ratType, int callRat) {
        String radioTech;
        logWithCallId("updateRat() : ratType is " + ratType + ", callRat is " + callRat, 2);
        if (this.mRatType == ratType && this.mCallRat == callRat) {
            return false;
        }
        if (this.mCallRat != 0 && this.mIsEmergencyCall) {
            return false;
        }
        int newCallRat = 0;
        if (callRat != 0) {
            newCallRat = callRat;
        } else if (ratType == 1) {
            int dataNetworkType = getDataNetworkType();
            logWithCallId("updateRat() : dataNetworkType is " + dataNetworkType, 2);
            if (dataNetworkType == 13 || dataNetworkType == 19) {
                newCallRat = 1;
            } else if (dataNetworkType == 20) {
                newCallRat = 3;
            }
        } else if (ratType == 2) {
            newCallRat = 2;
        } else {
            newCallRat = 0;
        }
        this.mRatType = ratType;
        if (this.mCallRat == newCallRat) {
            return false;
        }
        this.mCallRat = newCallRat;
        if (newCallRat == 1) {
            radioTech = String.valueOf(14);
        } else if (newCallRat == 2) {
            radioTech = String.valueOf(18);
        } else if (newCallRat == 3) {
            radioTech = String.valueOf(20);
        } else {
            radioTech = String.valueOf(0);
        }
        this.mCallProfile.setCallExtra("CallRadioTech", radioTech);
        ImsVTProvider imsVTProvider = this.mVTProvider;
        if (imsVTProvider != null) {
            int i = this.mCallRat;
            if (i == 2) {
                imsVTProvider.onUpdateCallRat(1);
            } else if (i == 3) {
                imsVTProvider.onUpdateCallRat(2);
            } else {
                imsVTProvider.onUpdateCallRat(0);
            }
        }
        logWithCallId("updateRat() : mRatType is " + this.mRatType + ", mCallRat is " + this.mCallRat, 2);
        return DBG;
    }

    private int getDataNetworkType() {
        NetworkRegistrationInfo wwanRegInfo = getNetworkRegistrationInfo();
        if (wwanRegInfo != null) {
            return wwanRegInfo.getAccessNetworkTechnology();
        }
        logWithCallId("getDataNetworkType error", 2);
        return 0;
    }

    private NetworkRegistrationInfo getNetworkRegistrationInfo() {
        ServiceState ss = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).getServiceState();
        if (ss != null) {
            return ss.getNetworkRegistrationInfo(2, 1);
        }
        logWithCallId("getNetworkRegistrationInfo error", 2);
        return null;
    }

    private boolean isImsEccSupported() {
        DataSpecificRegistrationInfo dataRegInfo;
        VopsSupportInfo vopsInfo;
        NetworkRegistrationInfo wwanRegInfo = getNetworkRegistrationInfo();
        if (wwanRegInfo == null || (dataRegInfo = wwanRegInfo.getDataSpecificInfo()) == null || (vopsInfo = dataRegInfo.getVopsSupportInfo()) == null) {
            return false;
        }
        boolean isImsEccSupported = (vopsInfo.isEmergencyServiceSupported() || vopsInfo.isEmergencyServiceFallbackSupported()) ? DBG : false;
        logWithCallId("isImsEccSupported: " + isImsEccSupported, 2);
        return isImsEccSupported;
    }

    /* access modifiers changed from: private */
    public void handleCallRatIndication(AsyncResult ar) {
        int[] result = (int[]) ar.result;
        int domain = result[0];
        int callRat = result[1];
        if (domain != 0) {
            if (callRat <= 0) {
                callRat = 0;
            }
            if (updateRat(this.mImsService.getRatType(this.mPhoneId), callRat)) {
                notifyCallSessionUpdated();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleCachedTerminateReason(ImsReasonInfo reasonInfo) {
        ImsReasonInfo cachedInfo = reasonInfo;
        if (reasonInfo == null) {
            notifyCallSessionTerminated(new ImsReasonInfo());
        } else {
            notifyCallSessionTerminated(cachedInfo);
        }
    }

    /* access modifiers changed from: private */
    public void handleVideoRingtoneInfo(AsyncResult ar) {
        logWithCallId("handleVideoRingtoneInfo():", 2);
        String[] videoRingtoneInfo = (String[]) ar.result;
        int msgType = 0;
        String event = null;
        String str = this.mCallId;
        if (str != null && str.equals(videoRingtoneInfo[0])) {
            msgType = Integer.parseInt(videoRingtoneInfo[1]);
            switch (msgType) {
                case 100:
                    event = videoRingtoneInfo[2];
                    this.mCachedVideoRingtoneButtonInfo = videoRingtoneInfo[2];
                    break;
            }
        } else {
            logWithCallId("handleVideoRingtoneInfo: mismatch call id", 2);
        }
        MtkImsCallSessionProxy mtkImsCallSessionProxy = this.mMtkImsCallSessionProxy;
        if (mtkImsCallSessionProxy != null) {
            mtkImsCallSessionProxy.notifyVideoRingtoneEvent(msgType, event);
        }
    }

    /* access modifiers changed from: private */
    public void notifyCachedVideoRingtoneButtonInfo() {
        logWithCallId("notifyCachedVideoRingtoneButtonInfo():", 2);
        if (this.mCachedVideoRingtoneButtonInfo == null) {
            logWithCallId("cached ringtone info is null", 2);
        }
        MtkImsCallSessionProxy mtkImsCallSessionProxy = this.mMtkImsCallSessionProxy;
        if (mtkImsCallSessionProxy != null) {
            mtkImsCallSessionProxy.notifyVideoRingtoneEvent(100, this.mCachedVideoRingtoneButtonInfo);
        }
    }

    /* access modifiers changed from: private */
    public void notifyNotificationRingtone(int causeNum, String causeText) {
        logWithCallId("notifyNotificationRingtone():", 2);
        MtkImsCallSessionProxy mtkImsCallSessionProxy = this.mMtkImsCallSessionProxy;
        if (mtkImsCallSessionProxy != null) {
            mtkImsCallSessionProxy.notifyNotificationRingtone(causeNum, causeText);
        }
    }

    /* access modifiers changed from: private */
    public void notifyRemoteHeld() {
        ImsCallSessionListener imsCallSessionListener;
        if (this.mRemoteState != 1 && (imsCallSessionListener = this.mListener) != null && this.mState != 7) {
            try {
                imsCallSessionListener.callSessionHoldReceived(this.mCallProfile);
                this.mRemoteState = 1;
            } catch (RuntimeException e) {
                logWithCallId("RuntimeException callSessionHoldReceived()", 5);
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyRemoteResumed() {
        ImsCallSessionListener imsCallSessionListener;
        if (this.mRemoteState != 2 && (imsCallSessionListener = this.mListener) != null && this.mState != 7) {
            try {
                imsCallSessionListener.callSessionResumeReceived(this.mCallProfile);
                this.mRemoteState = 2;
            } catch (RuntimeException e) {
                logWithCallId("RuntimeException callSessionResumeReceived()", 5);
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyCallSessionUpdated() {
        ImsCallSessionListener imsCallSessionListener = this.mListener;
        if (imsCallSessionListener != null) {
            try {
                imsCallSessionListener.callSessionUpdated(this.mCallProfile);
            } catch (RuntimeException e) {
                logWithCallId("RuntimeException callSessionUpdated()", 5);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ConferenceEventListener getConfEvtListener() {
        if (this.mConfEvtListener == null) {
            this.mConfEvtListener = new ConferenceEventListener();
        }
        return this.mConfEvtListener;
    }

    class ConferenceEventListener extends DefaultConferenceHandler.Listener {
        ConferenceEventListener() {
        }

        public void onParticipantsUpdate(ImsConferenceState confState) {
            ImsCallSessionProxy.this.logWithCallId("onParticipantsUpdate()", 2);
            if (ImsCallSessionProxy.this.mListener != null) {
                try {
                    ImsCallSessionProxy.this.mListener.callSessionConferenceStateUpdated(confState);
                } catch (RuntimeException e) {
                    ImsCallSessionProxy.this.logWithCallId("RuntimeException callSessionConferenceStateUpdated()", 5);
                }
            }
        }

        public void onAutoTerminate() {
            ImsCallSessionProxy.this.logWithCallId("onAutoTerminate()", 2);
            ImsCallSessionProxy.this.terminate(0);
        }
    }

    public void onAddParticipantComplete() {
        Rlog.d(LOG_TAG, "onAddParticipantComplete(): " + this.mCallId);
        ImsVTProvider imsVTProvider = this.mVTProvider;
        if (imsVTProvider != null) {
            this.mVTProviderUtil.resetWrapper(imsVTProvider);
        }
    }

    private boolean isCallPull(ImsCallProfile profile) {
        Bundle extras;
        if (profile == null || profile.mCallExtras == null || (extras = profile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS")) == null) {
            return false;
        }
        return extras.getBoolean("CallPull", false);
    }

    private void pullCall(String target, ImsCallProfile profile) {
        this.mImsRILAdapter.pullCall(target, isVideoCall(profile), this.mHandler.obtainMessage(EVENT_PULL_CALL_RESULT));
    }

    /* access modifiers changed from: private */
    public boolean isVideoCall(ImsCallProfile profile) {
        if (profile == null || ImsCallProfile.getVideoStateFromImsCallProfile(profile) == 0) {
            return false;
        }
        return DBG;
    }

    private void updateShouldUpdateAddress() {
        String mOperatorNum = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).getSimOperatorNumericForPhone(this.mPhoneId);
        boolean isMatched = OperatorUtils.isMatched(OperatorUtils.OPID.OP06, this.mPhoneId);
        boolean z = DBG;
        this.mShouldUpdateAddressByPau = (isMatched || OperatorUtils.isMatched(OperatorUtils.OPID.OP08, this.mPhoneId) || OperatorUtils.isMatched(OperatorUtils.OPID.OP_EIOT, this.mPhoneId)) && !"22601".equals(mOperatorNum) && !"21401".equals(mOperatorNum);
        if (!OperatorUtils.isMatched(OperatorUtils.OPID.OP130, this.mPhoneId) && !OperatorUtils.isMatched(OperatorUtils.OPID.OP120, this.mPhoneId) && !OperatorUtils.isMatched(OperatorUtils.OPID.OP132, this.mPhoneId) && !OperatorUtils.isMatched(OperatorUtils.OPID.OPOi, this.mPhoneId)) {
            z = false;
        }
        this.mShouldUpdateAddressFromEcpi = z;
    }

    private void updateShouldUseSipField() {
        this.mShouldUpdateAddressBySipField = OperatorUtils.isMatched(OperatorUtils.OPID.OP236, this.mPhoneId) ^ DBG;
        logWithCallId("updateShouldUseSipField() : mShouldUpdateAddressBySipField = " + this.mShouldUpdateAddressBySipField, 2);
    }

    /* access modifiers changed from: private */
    public int updateIsIbt(String[] callInfo) {
        int isIbt = 1;
        if (callInfo[2] != null) {
            isIbt = Integer.parseInt(callInfo[2]);
        }
        logWithCallId("updateIsIbt() : isIbt= " + isIbt, 2);
        if (isIbt == 0) {
            this.mCallProfile.mMediaProfile.mAudioDirection = 0;
        } else {
            this.mCallProfile.mMediaProfile.mAudioDirection = 1;
        }
        return isIbt;
    }

    /* access modifiers changed from: private */
    public void createConferenceSession(ImsCallProfile imsCallProfile, String callId) {
        if (this.mMtkImsCallSessionProxy != null) {
            createMtkConferenceSession(imsCallProfile, callId);
        } else {
            createAospConferenceSession(imsCallProfile, callId);
        }
    }

    private void createMtkConferenceSession(ImsCallProfile imsCallProfile, String callId) {
        this.mMtkConfSessionProxy = new MtkImsCallSessionProxy(this.mContext, imsCallProfile, (ImsCallSessionListener) null, this.mImsService, this.mServiceHandler, this.mImsRILAdapter, callId, this.mPhoneId);
        ImsCallSessionProxy imsCallSessionProxy = new ImsCallSessionProxy(this.mContext, imsCallProfile, (ImsCallSessionListener) null, this.mImsService, this.mServiceHandler, this.mImsRILAdapter, callId, this.mPhoneId);
        this.mMtkConfSessionProxy.setAospCallSessionProxy(imsCallSessionProxy);
        imsCallSessionProxy.setMtkCallSessionProxy(this.mMtkConfSessionProxy);
        ImsConferenceHandler.getInstance().startConference(this.mContext, this.mMtkConfSessionProxy.getConfEvtListener(), callId, this.mPhoneId);
        this.mMtkImsCallSessionProxy.notifyCallSessionMergeStarted(this.mMtkConfSessionProxy.getServiceImpl(), this.mCallProfile);
    }

    private void createAospConferenceSession(ImsCallProfile imsCallProfile, String callId) {
        ImsCallSessionProxy imsCallSessionProxy = new ImsCallSessionProxy(this.mContext, imsCallProfile, (ImsCallSessionListener) null, this.mImsService, this.mServiceHandler, this.mImsRILAdapter, callId, this.mPhoneId);
        this.mConfSessionProxy = imsCallSessionProxy;
        ImsConferenceHandler.getInstance().startConference(this.mContext, imsCallSessionProxy.getConfEvtListener(), callId, this.mPhoneId);
        try {
            this.mListener.callSessionMergeStarted(this.mConfSessionProxy.getServiceImpl(), this.mCallProfile);
        } catch (RuntimeException e) {
            logWithCallId("RuntimeException callSessionMergeStarted()", 5);
        }
    }

    /* access modifiers changed from: private */
    public void terminateConferenceSession() {
        if (this.mMtkConfSessionProxy != null) {
            logWithCallId("terminateConferenceSession() : Hangup Conference: Hangup host while merging (mtk)", 2);
            if (this.mMtkConfSessionProxy.getAospCallSessionProxy() != null) {
                MtkImsCallSessionProxy confSession = this.mMtkConfSessionProxy;
                confSession.terminate(102);
                ImsConferenceHandler.getInstance().closeConference(confSession.getCallId());
                this.mParticipants.clear();
                return;
            }
            this.mHangupHostDuringMerge = DBG;
            logWithCallId("terminateConferenceSession() : init conference object not compelted.", 2);
        } else if (this.mConfSessionProxy != null) {
            logWithCallId("terminateConferenceSession() : Hangup Conference: Hangup host while merging (aosp)", 2);
            ImsCallSessionProxy confSession2 = this.mConfSessionProxy;
            confSession2.terminate(102);
            ImsConferenceHandler.getInstance().closeConference(confSession2.getCallId());
            this.mParticipants.clear();
        } else {
            this.mHangupHostDuringMerge = DBG;
        }
    }

    private void closeConferenceSession() {
        MtkImsCallSessionProxy mtkImsCallSessionProxy = this.mMtkConfSessionProxy;
        if (mtkImsCallSessionProxy != null) {
            mtkImsCallSessionProxy.close();
            this.mMtkConfSessionProxy = null;
            return;
        }
        ImsCallSessionProxy imsCallSessionProxy = this.mConfSessionProxy;
        if (imsCallSessionProxy != null) {
            imsCallSessionProxy.close();
            this.mConfSessionProxy = null;
        }
    }

    private void notifyCallSessionMergeComplete() {
        MtkImsCallSessionProxy mtkImsCallSessionProxy = this.mMtkImsCallSessionProxy;
        if (mtkImsCallSessionProxy != null) {
            MtkImsCallSessionProxy mtkImsCallSessionProxy2 = this.mMtkConfSessionProxy;
            if (mtkImsCallSessionProxy2 != null) {
                mtkImsCallSessionProxy.notifyCallSessionMergeComplete(mtkImsCallSessionProxy2.getServiceImpl());
            } else {
                mtkImsCallSessionProxy.notifyCallSessionMergeComplete((IMtkImsCallSession) null);
            }
        } else {
            ImsCallSessionListener imsCallSessionListener = this.mListener;
            if (imsCallSessionListener != null) {
                try {
                    ImsCallSessionProxy imsCallSessionProxy = this.mConfSessionProxy;
                    if (imsCallSessionProxy != null) {
                        imsCallSessionListener.callSessionMergeComplete(imsCallSessionProxy.getServiceImpl());
                    } else {
                        imsCallSessionListener.callSessionMergeComplete((IImsCallSession) null);
                    }
                } catch (RuntimeException e) {
                    logWithCallId("RuntimeException callSessionMergeComplete()", 5);
                    close();
                }
            }
        }
    }

    private boolean isUserPerfromedHangup() {
        if (this.mHangupCount > 0) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean shouldNotifyCallDropByBadWifiQuality() {
        boolean notifyWifiQualityDisconnectCause = OperatorUtils.isMatched(OperatorUtils.OPID.OP07, this.mPhoneId);
        logWithCallId("shouldNotifyCallDropByBadWifiQuality() :  carrier =  " + notifyWifiQualityDisconnectCause + " isUserPerfromedHangup = " + isUserPerfromedHangup() + " mRatType = " + this.mRatType, 2);
        if (!notifyWifiQualityDisconnectCause || isUserPerfromedHangup() || this.mRatType != 2) {
            return false;
        }
        return DBG;
    }

    /* access modifiers changed from: private */
    public int getWifiRssi() {
        WifiInfo info = ((WifiManager) this.mContext.getSystemService("wifi")).getConnectionInfo();
        if (info == null) {
            return -1;
        }
        int rssi = info.getRssi();
        logWithCallId("getWifiRssi()" + rssi, 2);
        return rssi;
    }

    /* access modifiers changed from: private */
    public void correctRemoteVideoCapabilityForVideoConference() {
        if (isMultiparty() && isVideoCall(this.mCallProfile)) {
            logWithCallId("correctRemoteVideoCapabilityForVideoConference() : Video conference, force set remote as Video Call", 2);
            this.mRemoteCallProfile.mCallType = 4;
        }
    }

    /* access modifiers changed from: private */
    public boolean correctVideoCapabilityForCallState() {
        int finalLocalCallType;
        int finalRemoteCallType;
        ImsCallInfo myCallInfo = this.mImsServiceCT.getCallInfo(this.mCallId);
        if (myCallInfo == null || isVideoCall(this.mCallProfile)) {
            return false;
        }
        boolean isChanged = false;
        int i = this.mLocalCallProfile.mCallType;
        int i2 = this.mRemoteCallProfile.mCallType;
        if (myCallInfo.mState == ImsCallInfo.State.ACTIVE && this.mPreLocalVideoCapability == 4) {
            finalLocalCallType = 4;
        } else {
            finalLocalCallType = 2;
        }
        if (myCallInfo.mIsRemoteHold || this.mPreRemoteVideoCapability != 4) {
            finalRemoteCallType = 2;
        } else {
            finalRemoteCallType = 4;
        }
        if (this.mLocalCallProfile.mCallType != finalLocalCallType) {
            logWithCallId("correctVideoCapabilityByCallState() : local changed from " + this.mLocalCallProfile.mCallType + " to " + finalLocalCallType, 2);
            this.mLocalCallProfile.mCallType = finalLocalCallType;
            isChanged = DBG;
        }
        if (this.mRemoteCallProfile.mCallType == finalRemoteCallType) {
            return isChanged;
        }
        logWithCallId("correctVideoCapabilityByCallState() : Remote changed from " + this.mRemoteCallProfile.mCallType + " to " + finalRemoteCallType, 2);
        this.mRemoteCallProfile.mCallType = finalRemoteCallType;
        return DBG;
    }

    /* access modifiers changed from: private */
    public boolean removeRemoteCallVideoCapability() {
        logWithCallId("removeRemoteCallVideoCapability()", 2);
        if (this.mRemoteCallProfile.mCallType == 2) {
            return false;
        }
        this.mRemoteCallProfile.mCallType = 2;
        return DBG;
    }

    /* access modifiers changed from: private */
    public void updateCallType(int callMode, int videoState) {
        if (callMode == 21 || callMode == 23 || callMode == 25) {
            switch (videoState) {
                case 0:
                    break;
                case 1:
                    this.mCallProfile.mCallType = 5;
                    logWithCallId("updateCallType() : mCallType = CALL_TYPE_VT_TX", 2);
                    break;
                case 2:
                    this.mCallProfile.mCallType = 6;
                    logWithCallId("updateCallType() : mCallType = CALL_TYPE_VT_RX", 2);
                    break;
                case 3:
                    this.mCallProfile.mCallType = 4;
                    logWithCallId("updateCallType() : mCallType = CALL_TYPE_VT", 2);
                    break;
                default:
                    this.mCallProfile.mCallType = 4;
                    logWithCallId("updateCallType() : mCallType = CALL_TYPE_VT", 2);
                    break;
            }
            if (isRttSupported()) {
                this.mCallProfile.setCallExtraBoolean(EXTRA_WAS_VIDEO_CALL, DBG);
                logWithCallId("updateCallType() : EXTRA_WAS_VIDEO_CALL = true", 2);
            }
        } else if (callMode == 20 || callMode == 22 || callMode == 24) {
            this.mCallProfile.mCallType = 2;
            logWithCallId("updateCallType() : mCallType = CALL_TYPE_VOICE", 2);
        }
        logWithCallId("updateCallType() : " + this.mCallProfile.mCallType, 2);
    }

    /* access modifiers changed from: private */
    public boolean isAnsweredElsewhere(String header) {
        if (header == null) {
            return false;
        }
        if (header.equalsIgnoreCase(SipMessage.COMPETION_ELSEWHERE_HEADER) || header.toLowerCase().contains(SipMessage.CALL_COMPLETED_ELSEWHERE_HEADER)) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isRemoteCallDecline(String header) {
        if (header == null) {
            return false;
        }
        if (header.equalsIgnoreCase(SipMessage.REMOTE_DECLINE_HEADER) || header.toLowerCase().contains(SipMessage.CALL_DECLINED_HEADER) || header.toLowerCase().contains(SipMessage.CALL_COMPLETED_BUSY_EVERYWHERE_HEADER)) {
            return DBG;
        }
        return false;
    }

    private void rejectDial() {
        logWithCallId("rejectDial()", 2);
        this.mCallErrorState = CallErrorState.DIAL;
        notifyCallSessionTerminated(new ImsReasonInfo());
    }

    private String getConfParticipantUri(String addr) {
        if (ImsCommonUtil.supportMdAutoSetupIms()) {
            return addr;
        }
        String participantUri = ImsConferenceHandler.getInstance().getConfParticipantUri(addr, false);
        this.mRetryRemoveUri = ImsConferenceHandler.getInstance().getConfParticipantUri(addr, DBG);
        return participantUri;
    }

    /* access modifiers changed from: private */
    public void sendCallEventWithRat(int msgType) {
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_CALL_INFO_MESSAGE_TYPE, msgType);
        extras.putInt(EXTRA_CALL_TYPE, this.mCallProfile.mCallType);
        extras.putInt(EXTRA_RAT_TYPE, this.mRatType);
        extras.putBoolean(EXTRA_INCOMING_CALL, this.mIsIncomingCall);
        extras.putBoolean(EXTRA_EMERGENCY_CALL, this.mIsEmergencyCall);
        this.mOpImsCallSession.sendCallEventWithRat(extras);
    }

    /* access modifiers changed from: private */
    public void detailLog(String msg) {
        if (TELDBG) {
            logWithCallId(msg, 2);
        }
    }

    /* access modifiers changed from: private */
    public void logWithCallId(String msg, int lvl) {
        if (1 == lvl && TELDBG) {
            Rlog.v(LOG_TAG, "[callId = " + this.mCallId + "] " + msg);
        } else if (2 == lvl) {
            Rlog.d(LOG_TAG, "[callId = " + this.mCallId + "] " + msg);
        } else if (3 == lvl) {
            Rlog.i(LOG_TAG, "[callId = " + this.mCallId + "] " + msg);
        } else if (4 == lvl) {
            Rlog.w(LOG_TAG, "[callId = " + this.mCallId + "] " + msg);
        } else if (5 == lvl) {
            Rlog.e(LOG_TAG, "[callId = " + this.mCallId + "] " + msg);
        } else {
            Rlog.d(LOG_TAG, "[callId = " + this.mCallId + "] " + msg);
        }
    }

    /* access modifiers changed from: private */
    public void logEventResult(boolean isSuccess, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        sb.append(isSuccess ? " success" : " failed");
        logWithCallId(sb.toString(), 2);
    }

    /* access modifiers changed from: private */
    public String sensitiveEncode(String msg) {
        return ImsServiceCallTracker.sensitiveEncode(msg);
    }

    private int getHangupCause(int reasionInfo) {
        logWithCallId("getHangupCause() : " + reasionInfo, 2);
        if (reasionInfo == 504) {
            return -1;
        }
        if (reasionInfo == 9040) {
            return 1;
        }
        if (reasionInfo == 505) {
            return 2;
        }
        if (reasionInfo == 9041) {
            return 3;
        }
        if (reasionInfo == 9043) {
            return 4;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public void handleSpeechCodecInfo(AsyncResult ar) {
        int newAudioQuality;
        int codec = ((int[]) ar.result)[0];
        logWithCallId("handleSpeechCodecInfo() : " + codec, 2);
        int oldAudioQuality = this.mLocalCallProfile.mMediaProfile.mAudioQuality;
        switch (codec) {
            case 1:
                newAudioQuality = 3;
                break;
            case 2:
                newAudioQuality = 4;
                break;
            case 3:
                newAudioQuality = 5;
                break;
            case 4:
                newAudioQuality = 6;
                break;
            case 5:
                newAudioQuality = 7;
                break;
            case 6:
                newAudioQuality = 1;
                break;
            case 7:
                newAudioQuality = 2;
                break;
            case 8:
                newAudioQuality = 8;
                break;
            case 9:
                newAudioQuality = 9;
                break;
            case 10:
                newAudioQuality = 10;
                break;
            case 23:
                newAudioQuality = 17;
                break;
            case 24:
                newAudioQuality = 18;
                break;
            case 25:
                newAudioQuality = 19;
                break;
            case 32:
                newAudioQuality = 20;
                break;
            case 33:
                newAudioQuality = 2;
                break;
            default:
                newAudioQuality = 0;
                break;
        }
        if (newAudioQuality != oldAudioQuality) {
            this.mLocalCallProfile.mMediaProfile.mAudioQuality = newAudioQuality;
            notifyCallSessionUpdated();
        }
    }

    private void turnOffAirplaneMode() {
        Rlog.d(LOG_TAG, "turnOffAirplaneMode()");
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) > 0) {
            Rlog.d(LOG_TAG, "turnOffAirplaneMode() : Turning off airplane mode.");
            Settings.Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", 0);
            Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
            intent.putExtra("state", false);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* access modifiers changed from: private */
    public void handleRedialEccIndication(AsyncResult ar) {
        Rlog.d(LOG_TAG, "handleRedialEccIndication()");
        if (ar == null) {
            Rlog.d(LOG_TAG, "handleRedialEccIndication() : ar is null");
        } else if (this.mState == 4) {
            Rlog.d(LOG_TAG, "handleRedialEccIndication() : Call established, ignore indication");
        } else {
            String[] result = (String[]) ar.result;
            if (result == null) {
                Rlog.d(LOG_TAG, "handleRedialEccIndication() : ar.result is null");
            } else if (result[0].equals("0")) {
                turnOffAirplaneMode();
            } else if (this.mMtkImsCallSessionProxy == null) {
            } else {
                if (result[0].equals("30")) {
                    this.mMtkImsCallSessionProxy.notifyRedialEcc(DBG);
                } else if (result[0].equals(this.mCallId)) {
                    this.mMtkImsCallSessionProxy.notifyRedialEcc(false);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public String event2String(int event) {
        if (101 == event) {
            return "EVENT_POLL_CALLS_RESULT";
        }
        if (102 == event) {
            return "EVENT_CALL_INFO_INDICATION";
        }
        if (104 == event) {
            return "EVENT_ECONF_RESULT_INDICATION";
        }
        if (105 == event) {
            return "EVENT_GET_LAST_CALL_FAIL_CAUSE";
        }
        if (106 == event) {
            return "EVENT_CALL_MODE_CHANGE_INDICATION";
        }
        if (EVENT_VIDEO_CAPABILITY_INDICATION == event) {
            return "EVENT_VIDEO_CAPABILITY_INDICATION";
        }
        if (EVENT_ECT_RESULT_INDICATION == event) {
            return "EVENT_ECT_RESULT_INDICATION";
        }
        if (EVENT_RTT_CAPABILITY_INDICATION == event) {
            return "EVENT_RTT_CAPABILITY_INDICATION";
        }
        if (111 == event) {
            return "EVENT_IMS_CONFERENCE_INDICATION";
        }
        if (201 == event) {
            return "EVENT_DIAL_RESULT";
        }
        if (202 == event) {
            return "EVENT_ACCEPT_RESULT";
        }
        if (EVENT_HOLD_RESULT == event) {
            return "EVENT_HOLD_RESULT";
        }
        if (EVENT_RESUME_RESULT == event) {
            return "EVENT_RESUME_RESULT";
        }
        if (EVENT_MERGE_RESULT == event) {
            return "EVENT_MERGE_RESULT";
        }
        if (EVENT_ADD_CONFERENCE_RESULT == event) {
            return "EVENT_ADD_CONFERENCE_RESULT";
        }
        if (EVENT_REMOVE_CONFERENCE_RESULT == event) {
            return "EVENT_REMOVE_CONFERENCE_RESULT";
        }
        if (EVENT_SIP_CODE_INDICATION == event) {
            return "EVENT_SIP_CODE_INDICATION";
        }
        if (EVENT_DIAL_CONFERENCE_RESULT == event) {
            return "EVENT_DIAL_CONFERENCE_RESULT";
        }
        if (EVENT_SWAP_BEFORE_MERGE_RESULT == event) {
            return "EVENT_SWAP_BEFORE_MERGE_RESULT";
        }
        if (211 == event) {
            return "EVENT_RETRIEVE_MERGE_FAIL_RESULT";
        }
        if (EVENT_DTMF_DONE == event) {
            return "EVENT_DTMF_DONE";
        }
        if (EVENT_SEND_USSI_COMPLETE == event) {
            return "EVENT_SEND_USSI_COMPLETE";
        }
        if (EVENT_CANCEL_USSI_COMPLETE == event) {
            return "EVENT_CANCEL_USSI_COMPLETE";
        }
        if (EVENT_ECT_RESULT == event) {
            return "EVENT_ECT_RESULT";
        }
        if (EVENT_PULL_CALL_RESULT == event) {
            return "EVENT_PULL_CALL_RESULT";
        }
        if (EVENT_RADIO_NOT_AVAILABLE == event) {
            return "EVENT_RADIO_NOT_AVAILABLE";
        }
        if (EVENT_RTT_TEXT_RECEIVE_INDICATION == event) {
            return "EVENT_RTT_TEXT_RECEIVE_INDICATION";
        }
        if (EVENT_RTT_MODIFY_RESPONSE == event) {
            return "EVENT_RTT_MODIFY_RESPONSE";
        }
        if (EVENT_RTT_MODIFY_REQUEST_RECEIVE == event) {
            return "EVENT_RTT_MODIFY_REQUEST_RECEIVE";
        }
        if (EVENT_RTT_AUDIO_INDICATION == event) {
            return "EVENT_RTT_AUDIO_INDICATION";
        }
        if (EVENT_SPEECH_CODEC_INFO == event) {
            return "EVENT_SPEECH_CODEC_INFO";
        }
        if (EVENT_REDIAL_ECC_INDICATION == event) {
            return "EVENT_REDIAL_ECC_INDICATION";
        }
        if (226 == event) {
            return "EVENT_ON_SUPP_SERVICE_NOTIFICATION";
        }
        if (EVENT_SIP_HEADER_INFO == event) {
            return "EVENT_SIP_HEADER_INFO";
        }
        if (EVENT_CALL_RAT_INDICATION == event) {
            return "EVENT_CALL_RAT_INDICATION";
        }
        if (229 == event) {
            return "EVENT_CALL_ADDITIONAL_INFO";
        }
        if (EVENT_CACHED_TERMINATE_REASON == event) {
            return "EVENT_CACHED_TERMINATE_REASON";
        }
        return "unknown msg" + event;
    }

    public MtkImsCallSessionProxy getMtkCallSessionProxy() {
        return this.mMtkImsCallSessionProxy;
    }

    public void setMtkCallSessionProxy(MtkImsCallSessionProxy callSessionProxy) {
        this.mMtkImsCallSessionProxy = callSessionProxy;
    }

    public ImsCallOemPlugin getImsOemCallUtil() {
        return ExtensionFactory.makeOemPluginFactory(this.mContext).makeImsCallPlugin(this.mContext);
    }

    public ImsSelfActivatorBase getImsExtSelfActivator(Context context, Handler handler, ImsCallSessionProxy callSessionProxy, ImsCommandsInterface imsRILAdapter, ImsService imsService, int phoneId) {
        return ExtensionFactory.makeExtensionPluginFactory(this.mContext).makeImsSelfActivator(this.mContext, handler, this, imsRILAdapter, imsService, phoneId);
    }

    private void tryTurnOnVolteForE911(boolean isEmergencyNumber) {
        if (isEmergencyNumber) {
            ImsManager imsManager = ImsManager.getInstance(this.mContext, this.mPhoneId);
            boolean volteEnabledByPlatform = imsManager.isVolteEnabledByPlatform();
            boolean volteEnabledByUser = imsManager.isEnhanced4gLteModeSettingEnabledByUser();
            boolean isSimAbsent = SubscriptionManager.getSimStateForSlotIndex(this.mPhoneId) == 1;
            logWithCallId("tryTurnOnVolteForE911() : volteEnabledByPlatform " + volteEnabledByPlatform + ", volteEnabledByUser " + volteEnabledByUser + ", isSimAbsent " + isSimAbsent, 3);
            if (!volteEnabledByPlatform || (!volteEnabledByUser && isSimAbsent)) {
                ImsConfigUtils.triggerSendCfgForVolte(this.mContext, this.mImsRILAdapter, this.mPhoneId, 1);
                this.mImsServiceCT.setEnableVolteForImsEcc(DBG);
            }
        }
    }

    /* access modifiers changed from: private */
    public void checkAndSendRttBom() {
        boolean isRttSupport = isRttSupported();
        logWithCallId("checkAndSendRttBom() : isRttSuported = " + isRttSupport + ", isRttEnabledForCallSession = " + this.mIsRttEnabledForCallSession + ", mState = " + this.mState + ", mEnableSendRttBom = " + this.mEnableSendRttBom, 2);
        if (isRttSupport && this.mIsRttEnabledForCallSession && this.mEnableSendRttBom && this.mState == 4) {
            sendRttMessage(new String(new byte[]{-17, -69, -65}));
            this.mEnableSendRttBom = false;
        }
    }

    public void setImsCallMode(int mode) {
        this.mImsCallMode = mode;
        if (mode == 2) {
            this.mIsRingingRedirect = DBG;
            this.mImsRILAdapter.setImsCallMode(2, (Message) null);
        }
    }

    public void removeLastParticipant() {
        if (this.mCallId == null) {
            logWithCallId("removeLastParticipant() : fail since no call ID CallID = " + this.mCallId, 5);
            return;
        }
        int size = this.mParticipantsList.size();
        if (this.mCallId == null || size <= 1) {
            logWithCallId("removeLastParticipant() : Participant number = " + size, 5);
            logWithCallId("removeLastParticipant() : terminate", 2);
            terminate(0);
            return;
        }
        this.mImsRILAdapter.removeParticipants(Integer.parseInt(this.mCallId), getConfParticipantUri(this.mParticipantsList.get(size - 1)), this.mHandler.obtainMessage(EVENT_REMOVE_CONFERENCE_RESULT));
    }

    public String getHeaderCallId() {
        return this.mHeaderCallId;
    }

    public void videoRingtoneOperation(int type, String operation) {
        logWithCallId("videoRingtoneOperation(): CallID = " + this.mCallId + " type = " + type, 2);
        if (type != 100) {
            ArrayList<String> event = new ArrayList<>();
            event.add(this.mCallId);
            event.add(Integer.toString(type));
            event.add(operation);
            this.mImsRILAdapter.videoRingtoneEventRequest(event, this.mHandler.obtainMessage(EVENT_VIDEO_RINGTONE_REQUEST_RESULT));
        } else if (this.mCachedVideoRingtoneButtonInfo != null) {
            this.mHandler.obtainMessage(EVENT_VIDEO_RINGTONE_CACHED_INFO).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public void handleSipHeaderInfo(AsyncResult ar) {
        String[] sipHeaderInfo = (String[]) ar.result;
        String str = this.mCallId;
        if (str != null && str.equals(sipHeaderInfo[0])) {
            int headerType = 0;
            if (sipHeaderInfo[1] != null && !sipHeaderInfo[1].equals("")) {
                headerType = Integer.parseInt(sipHeaderInfo[1]);
            }
            if (sipHeaderInfo[2] != null && !sipHeaderInfo[2].equals("")) {
                int totalCount = Integer.parseInt(sipHeaderInfo[2]);
            }
            if (sipHeaderInfo[3] != null && !sipHeaderInfo[3].equals("")) {
                int index = Integer.parseInt(sipHeaderInfo[3]);
            }
            if (headerType == 13) {
                String headerCallId = "";
                if (sipHeaderInfo[4] != null && !sipHeaderInfo[4].equals("")) {
                    headerCallId = sipHeaderInfo[4];
                }
                try {
                    this.mHeaderCallId = new String(hexToByteArray(headerCallId), "UTF-8");
                    logWithCallId("handleSipHeaderInfo() : mHeaderCallId: " + this.mHeaderCallId, 2);
                } catch (UnsupportedEncodingException ex) {
                    Rlog.e(LOG_TAG, "handleSipHeaderInfo() implausible UnsupportedEncodingException", ex);
                } catch (RuntimeException ex2) {
                    Rlog.e(LOG_TAG, "handleSipHeaderInfo() RuntimeException", ex2);
                }
            }
        }
    }

    private byte[] hexToByteArray(String hex) {
        String str;
        if (hex.length() % 2 != 0) {
            str = "0" + hex;
        } else {
            str = hex;
        }
        String hex2 = str;
        byte[] b = new byte[(hex2.length() / 2)];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            b[i] = (byte) Integer.parseInt(hex2.substring(index, index + 2), 16);
        }
        return b;
    }

    /* access modifiers changed from: private */
    public void processMtRttWithoutPrecondition(int remoteCapability) {
        boolean isWithoutPrecondition = isMtRttWithoutPrecondition();
        logWithCallId("processMtRttWithoutPrecondition: isWithoutPrecondition=" + isWithoutPrecondition, 2);
        if (isWithoutPrecondition && this.mState == 0) {
            boolean z = DBG;
            if (remoteCapability != 1) {
                z = false;
            }
            this.mIsRttEnabledForCallSession = z;
        }
    }

    private boolean isMtRttWithoutPrecondition() {
        return ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).getCarrierConfig().getBoolean(ImsCarrierConfigConstants.MTK_KEY_MT_RTT_WITHOUT_PRECONDITION_BOOL, false);
    }

    /* access modifiers changed from: private */
    public void toggleRttAudioIndication() {
        if (isRttSupported()) {
            if (!((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).getCarrierConfig().getBoolean(ImsCarrierConfigConstants.MTK_KEY_RTT_AUDIO_INDICATION_SUPPORTED_BOOL, false)) {
                logWithCallId("toggleRttAudioIndication: carrier config not supported.", 2);
                return;
            }
            int callId = Integer.parseInt(this.mCallId);
            if (this.mIsRttEnabledForCallSession) {
                logWithCallId("toggleRttAudioIndication: enable RTT audio indication.", 2);
                this.mImsRILAdapter.toggleRttAudioIndication(callId, 1, (Message) null);
            } else if (this.mState == 4) {
                logWithCallId("toggleRttAudioIndication: disable RTT audio indication.", 2);
                this.mImsRILAdapter.toggleRttAudioIndication(callId, 0, (Message) null);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isAllowRttVideoSwitch() {
        boolean wasVideoCall = this.mCallProfile.getCallExtraBoolean(EXTRA_WAS_VIDEO_CALL);
        if (!((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).getCarrierConfig().getBoolean(ImsCarrierConfigConstants.MTK_KEY_RTT_VIDEO_SWITCH_SUPPORTED_BOOL, false) && wasVideoCall) {
            return false;
        }
        return DBG;
    }

    /* access modifiers changed from: private */
    public ImsReasonInfo getOpImsReasonInfo(SipMessage sipMsg) {
        ImsReasonInfo info = null;
        logWithCallId("getOpImsReasonInfo sipCode " + sipMsg.getCode() + " reasonHeader " + sipMsg.getReasonHeader(), 2);
        int sipErrCode = sipMsg.getCode();
        String reasonHeader = sipMsg.getReasonHeader();
        if (OperatorUtils.isMatched(OperatorUtils.OPID.OP112, this.mPhoneId) && sipMsg.getDir() == 1) {
            switch (sipErrCode) {
                case 301:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9000, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9000, 0);
                        break;
                    }
                case 400:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(ImsVTProvider.SESSION_EVENT_WARNING_SERVICE_NOT_READY, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(ImsVTProvider.SESSION_EVENT_WARNING_SERVICE_NOT_READY, 0);
                        break;
                    }
                    break;
                case 401:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9002, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9002, 0);
                        break;
                    }
                case 402:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9003, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9003, 0);
                        break;
                    }
                    break;
                case 403:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9004, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9004, 0);
                        break;
                    }
                    break;
                case 404:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9005, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9005, 0);
                        break;
                    }
                    break;
                case 405:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9006, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9006, 0);
                        break;
                    }
                    break;
                case 406:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9007, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9007, 0);
                        break;
                    }
                    break;
                case 407:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9008, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9008, 0);
                        break;
                    }
                    break;
                case 408:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9009, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9009, 0);
                        break;
                    }
                    break;
                case 409:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9010, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9010, 0);
                        break;
                    }
                    break;
                case 410:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9011, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9011, 0);
                        break;
                    }
                    break;
                case 411:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9012, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9012, 0);
                        break;
                    }
                    break;
                case 413:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9013, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9013, 0);
                        break;
                    }
                    break;
                case 414:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9014, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9014, 0);
                        break;
                    }
                    break;
                case 415:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9015, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9015, 0);
                        break;
                    }
                    break;
                case 416:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9016, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9016, 0);
                        break;
                    }
                    break;
                case 420:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9017, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9017, 0);
                        break;
                    }
                    break;
                case 421:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9018, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9018, 0);
                        break;
                    }
                    break;
                case 423:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9019, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9019, 0);
                        break;
                    }
                    break;
                case 480:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9020, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9020, 0);
                        break;
                    }
                    break;
                case 481:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9021, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9021, 0);
                        break;
                    }
                    break;
                case 482:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9022, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9022, 0);
                        break;
                    }
                    break;
                case 483:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9023, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9023, 0);
                        break;
                    }
                    break;
                case 484:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9024, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9024, 0);
                        break;
                    }
                    break;
                case 485:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9025, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9025, 0);
                        break;
                    }
                    break;
                case SipMessage.CODE_SESSION_INVITE_FAILED_REMOTE_BUSY:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9026, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9026, 0);
                        break;
                    }
                    break;
                case 487:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9027, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9027, 0);
                        break;
                    }
                    break;
                case 488:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9028, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9028, 0);
                        break;
                    }
                    break;
                case 500:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9029, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9029, 0);
                        break;
                    }
                    break;
                case RadioError.OEM_ERROR_1:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9030, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9030, 0);
                        break;
                    }
                    break;
                case RadioError.OEM_ERROR_2:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9031, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9031, 0);
                        break;
                    }
                    break;
                case RadioError.OEM_ERROR_3:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9032, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9032, 0);
                        break;
                    }
                    break;
                case RadioError.OEM_ERROR_4:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9033, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9033, 0);
                        break;
                    }
                    break;
                case RadioError.OEM_ERROR_5:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9034, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9034, 0);
                        break;
                    }
                    break;
                case 513:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9035, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9035, 0);
                        break;
                    }
                    break;
                case 600:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9036, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9036, 0);
                        break;
                    }
                    break;
                case 603:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9037, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9037, 0);
                        break;
                    }
                    break;
                case 604:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9038, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9038, 0);
                        break;
                    }
                    break;
                case 606:
                    if (reasonHeader != null && reasonHeader.length() != 0) {
                        info = new ImsReasonInfo(9039, 0, reasonHeader);
                        break;
                    } else {
                        info = new ImsReasonInfo(9039, 0);
                        break;
                    }
                    break;
            }
        }
        logWithCallId("getOpImsReasonInfo(): " + info, 2);
        return info;
    }

    public boolean getMultipartyModeForConfPart() {
        PersistableBundle config;
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configManager == null || (config = configManager.getConfigForSubId(this.mImsService.getSubIdUsingPhoneId(this.mPhoneId))) == null) {
            return false;
        }
        return config.getBoolean("config_oppo_show_conf_for_part_bool", false);
    }

    /* access modifiers changed from: private */
    public boolean getBooleanFromCarrierConfig(String key) {
        PersistableBundle carrierConfig;
        int subId = this.mImsService.getSubIdUsingPhoneId(this.mPhoneId);
        CarrierConfigManager configMgr = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        boolean result = false;
        if (!(configMgr == null || (carrierConfig = configMgr.getConfigForSubId(subId)) == null)) {
            result = carrierConfig.getBoolean(key);
        }
        logWithCallId("getBooleanFromCarrierConfig() : key = " + key + " result = " + result, 2);
        return result;
    }

    private boolean isSpecialEccNumber(String number) {
        boolean isSpecialEccNumber = false;
        switch (this.mPhoneId) {
            case 0:
                isSpecialEccNumber = SystemProperties.get("vendor.ril.special.ecclist").contains(number);
                break;
            case 1:
                isSpecialEccNumber = SystemProperties.get("vendor.ril.special.ecclist1").contains(number);
                break;
            case 2:
                isSpecialEccNumber = SystemProperties.get("vendor.ril.special.ecclist2").contains(number);
                break;
            case 3:
                isSpecialEccNumber = SystemProperties.get("vendor.ril.special.ecclist3").contains(number);
                break;
        }
        logWithCallId("isSpecialEccNumber() : mPhoneId = " + this.mPhoneId + ", isSpecialEccNumber = " + isSpecialEccNumber, 2);
        return isSpecialEccNumber;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleCallAdditionalInfo(android.os.AsyncResult r22) {
        /*
            r21 = this;
            r1 = r21
            java.lang.String r0 = "UTF-8"
            java.lang.String r2 = "ImsCallSessionProxy"
            r3 = r22
            java.lang.Object r4 = r3.result
            java.lang.String[] r4 = (java.lang.String[]) r4
            r5 = 0
            r6 = r4[r5]
            int r6 = java.lang.Integer.parseInt(r6)
            r7 = 1
            r8 = r4[r7]
            r9 = 101(0x65, float:1.42E-43)
            if (r6 != r9) goto L_0x0033
            java.lang.String r0 = r1.mCallId
            if (r0 == 0) goto L_0x002f
            boolean r0 = r0.equals(r8)
            if (r0 == 0) goto L_0x002f
            android.telephony.ims.ImsCallProfile r0 = r1.mCallProfile
            java.lang.String r2 = "ims_gwsd"
            r0.setCallExtraInt(r2, r7)
            r19 = r6
            goto L_0x01e6
        L_0x002f:
            r19 = r6
            goto L_0x01e6
        L_0x0033:
            r9 = 102(0x66, float:1.43E-43)
            if (r6 != r9) goto L_0x01e4
            r9 = 6
            r10 = r4[r9]
            if (r10 == 0) goto L_0x01e1
            r10 = r4[r9]
            boolean r10 = android.text.TextUtils.isEmpty(r10)
            if (r10 == 0) goto L_0x0048
            r19 = r6
            goto L_0x01e3
        L_0x0048:
            java.lang.String r10 = r1.mCallId
            if (r10 == 0) goto L_0x01de
            boolean r10 = r10.equals(r8)
            if (r10 == 0) goto L_0x01de
            r10 = 2
            r11 = r4[r10]
            int r11 = java.lang.Integer.parseInt(r11)
            java.lang.String r12 = ","
            if (r11 != r7) goto L_0x01a6
            r13 = 3
            r13 = r4[r13]
            int r13 = java.lang.Integer.parseInt(r13)
            r14 = 4
            r15 = r4[r14]
            int r15 = java.lang.Integer.parseInt(r15)
            r16 = 5
            r16 = r4[r16]
            int r5 = java.lang.Integer.parseInt(r16)
            if (r15 != r7) goto L_0x007a
            r9 = r4[r9]
            r1.mHeaderData = r9
            goto L_0x008f
        L_0x007a:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r14 = r1.mHeaderData
            r7.append(r14)
            r9 = r4[r9]
            r7.append(r9)
            java.lang.String r7 = r7.toString()
            r1.mHeaderData = r7
        L_0x008f:
            if (r15 == r13) goto L_0x0092
            return
        L_0x0092:
            java.lang.String r7 = r1.mHeaderData     // Catch:{ UnsupportedEncodingException -> 0x019b, RuntimeException -> 0x0190 }
            java.lang.String[] r7 = r7.split(r12)     // Catch:{ UnsupportedEncodingException -> 0x019b, RuntimeException -> 0x0190 }
            int r9 = r7.length     // Catch:{ UnsupportedEncodingException -> 0x019b, RuntimeException -> 0x0190 }
            int r9 = r9 / r10
            if (r9 == r5) goto L_0x00c8
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ UnsupportedEncodingException -> 0x00c1, RuntimeException -> 0x00ba }
            r12.<init>()     // Catch:{ UnsupportedEncodingException -> 0x00c1, RuntimeException -> 0x00ba }
            java.lang.String r14 = "Header count unmatched: "
            r12.append(r14)     // Catch:{ UnsupportedEncodingException -> 0x00c1, RuntimeException -> 0x00ba }
            r12.append(r5)     // Catch:{ UnsupportedEncodingException -> 0x00c1, RuntimeException -> 0x00ba }
            java.lang.String r14 = ", "
            r12.append(r14)     // Catch:{ UnsupportedEncodingException -> 0x00c1, RuntimeException -> 0x00ba }
            r12.append(r9)     // Catch:{ UnsupportedEncodingException -> 0x00c1, RuntimeException -> 0x00ba }
            java.lang.String r12 = r12.toString()     // Catch:{ UnsupportedEncodingException -> 0x00c1, RuntimeException -> 0x00ba }
            r14 = 4
            r1.logWithCallId(r12, r14)     // Catch:{ UnsupportedEncodingException -> 0x00c1, RuntimeException -> 0x00ba }
            goto L_0x00c8
        L_0x00ba:
            r0 = move-exception
            r17 = r5
            r19 = r6
            goto L_0x0195
        L_0x00c1:
            r0 = move-exception
            r17 = r5
            r19 = r6
            goto L_0x01a0
        L_0x00c8:
            r12 = 0
        L_0x00c9:
            if (r12 >= r9) goto L_0x018b
            int r14 = r12 * 2
            r14 = r7[r14]     // Catch:{ UnsupportedEncodingException -> 0x019b, RuntimeException -> 0x0190 }
            byte[] r14 = r1.hexToByteArray(r14)     // Catch:{ UnsupportedEncodingException -> 0x019b, RuntimeException -> 0x0190 }
            int r17 = r12 * 2
            r16 = 1
            int r17 = r17 + 1
            r10 = r7[r17]     // Catch:{ UnsupportedEncodingException -> 0x019b, RuntimeException -> 0x0190 }
            byte[] r10 = r1.hexToByteArray(r10)     // Catch:{ UnsupportedEncodingException -> 0x019b, RuntimeException -> 0x0190 }
            java.lang.String r3 = new java.lang.String     // Catch:{ UnsupportedEncodingException -> 0x019b, RuntimeException -> 0x0190 }
            r3.<init>(r14, r0)     // Catch:{ UnsupportedEncodingException -> 0x019b, RuntimeException -> 0x0190 }
            r17 = r5
            java.lang.String r5 = new java.lang.String     // Catch:{ UnsupportedEncodingException -> 0x0187, RuntimeException -> 0x0183 }
            r5.<init>(r10, r0)     // Catch:{ UnsupportedEncodingException -> 0x0187, RuntimeException -> 0x0183 }
            r18 = r0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ UnsupportedEncodingException -> 0x0187, RuntimeException -> 0x0183 }
            r0.<init>()     // Catch:{ UnsupportedEncodingException -> 0x0187, RuntimeException -> 0x0183 }
            r19 = r6
            java.lang.String r6 = "key-value: "
            r0.append(r6)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            r0.append(r3)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            java.lang.String r6 = " - "
            r0.append(r6)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            java.lang.String r6 = r1.sensitiveEncode(r5)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            r0.append(r6)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            java.lang.String r0 = r0.toString()     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            r6 = 2
            r1.logWithCallId(r0, r6)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            r0 = -1
            int r6 = r3.hashCode()     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            switch(r6) {
                case -1100816956: goto L_0x012d;
                case -203231988: goto L_0x0123;
                case 910453437: goto L_0x0119;
                default: goto L_0x0118;
            }     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
        L_0x0118:
            goto L_0x0136
        L_0x0119:
            java.lang.String r6 = "Call-Info"
            boolean r6 = r3.equals(r6)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            if (r6 == 0) goto L_0x0118
            r0 = 2
            goto L_0x0136
        L_0x0123:
            java.lang.String r6 = "Subject"
            boolean r6 = r3.equals(r6)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            if (r6 == 0) goto L_0x0118
            r0 = 0
            goto L_0x0136
        L_0x012d:
            java.lang.String r6 = "Priority"
            boolean r6 = r3.equals(r6)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            if (r6 == 0) goto L_0x0118
            r0 = 1
        L_0x0136:
            switch(r0) {
                case 0: goto L_0x0168;
                case 1: goto L_0x0144;
                case 2: goto L_0x013c;
                default: goto L_0x0139;
            }     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
        L_0x0139:
            r20 = r3
            goto L_0x0172
        L_0x013c:
            android.telephony.ims.ImsCallProfile r0 = r1.mCallProfile     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            java.lang.String r6 = "android.telephony.ims.extra.PICTURE_URL"
            r0.setCallExtra(r6, r5)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            goto L_0x0172
        L_0x0144:
            java.lang.String r0 = "standard"
            boolean r0 = r5.equals(r0)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            java.lang.String r6 = "android.telephony.ims.extra.PRIORITY"
            if (r0 == 0) goto L_0x0157
            android.telephony.ims.ImsCallProfile r0 = r1.mCallProfile     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            r20 = r3
            r3 = 0
            r0.setCallExtraInt(r6, r3)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            goto L_0x0172
        L_0x0157:
            r20 = r3
            java.lang.String r0 = "important"
            boolean r0 = r5.equals(r0)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            if (r0 == 0) goto L_0x0172
            android.telephony.ims.ImsCallProfile r0 = r1.mCallProfile     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            r3 = 1
            r0.setCallExtraInt(r6, r3)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            goto L_0x0172
        L_0x0168:
            r20 = r3
            android.telephony.ims.ImsCallProfile r0 = r1.mCallProfile     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
            java.lang.String r3 = "android.telephony.ims.extra.CALL_SUBJECT"
            r0.setCallExtra(r3, r5)     // Catch:{ UnsupportedEncodingException -> 0x0181, RuntimeException -> 0x017f }
        L_0x0172:
            int r12 = r12 + 1
            r3 = r22
            r5 = r17
            r0 = r18
            r6 = r19
            r10 = 2
            goto L_0x00c9
        L_0x017f:
            r0 = move-exception
            goto L_0x0195
        L_0x0181:
            r0 = move-exception
            goto L_0x01a0
        L_0x0183:
            r0 = move-exception
            r19 = r6
            goto L_0x0195
        L_0x0187:
            r0 = move-exception
            r19 = r6
            goto L_0x01a0
        L_0x018b:
            r17 = r5
            r19 = r6
            goto L_0x01a5
        L_0x0190:
            r0 = move-exception
            r17 = r5
            r19 = r6
        L_0x0195:
            java.lang.String r3 = "handleCallAdditionalInfo() RuntimeException"
            android.telephony.Rlog.e(r2, r3, r0)
            goto L_0x01dd
        L_0x019b:
            r0 = move-exception
            r17 = r5
            r19 = r6
        L_0x01a0:
            java.lang.String r3 = "handleCallAdditionalInfo() UnsupportedEncodingException"
            android.telephony.Rlog.e(r2, r3, r0)
        L_0x01a5:
            goto L_0x01dd
        L_0x01a6:
            r19 = r6
            r0 = 2
            if (r11 != r0) goto L_0x01dd
            r0 = r4[r9]
            java.lang.String[] r0 = r0.split(r12)
            if (r0 != 0) goto L_0x01b4
            return
        L_0x01b4:
            android.location.Location r2 = new android.location.Location
            java.lang.String r3 = ""
            r2.<init>(r3)
            r2.reset()
            r3 = 0
            r3 = r0[r3]
            double r5 = java.lang.Double.parseDouble(r3)
            r2.setLatitude(r5)
            int r3 = r0.length
            r5 = 1
            if (r3 <= r5) goto L_0x01d5
            r3 = r0[r5]
            double r5 = java.lang.Double.parseDouble(r3)
            r2.setLongitude(r5)
        L_0x01d5:
            android.telephony.ims.ImsCallProfile r3 = r1.mCallProfile
            java.lang.String r5 = "android.telephony.ims.extra.LOCATION"
            r3.setCallExtraParcelable(r5, r2)
            goto L_0x01e6
        L_0x01dd:
            goto L_0x01e6
        L_0x01de:
            r19 = r6
            goto L_0x01e6
        L_0x01e1:
            r19 = r6
        L_0x01e3:
            return
        L_0x01e4:
            r19 = r6
        L_0x01e6:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.ImsCallSessionProxy.handleCallAdditionalInfo(android.os.AsyncResult):void");
    }

    private void setImsPreCallInfo(ImsCallProfile profile) {
        int headerCount;
        ImsCallProfile imsCallProfile = profile;
        ArrayList<String> headerInfo = new ArrayList<>();
        StringBuilder headerValuePair = new StringBuilder();
        int headerCount2 = 0;
        String subject = imsCallProfile.getCallExtra("android.telephony.ims.extra.CALL_SUBJECT");
        int priority = imsCallProfile.getCallExtraInt("android.telephony.ims.extra.PRIORITY", 0);
        String pictureUrl = imsCallProfile.getCallExtra("android.telephony.ims.extra.PICTURE_URL");
        logWithCallId("setImsPreCallInfo Subject: " + sensitiveEncode(subject) + " Priority: " + priority + " Call-Info: " + sensitiveEncode(pictureUrl), 2);
        if (subject != null && !TextUtils.isEmpty(subject)) {
            headerValuePair.append(toHexString(SIP_INVITE_HEADER_SUBJECT) + "," + toHexString(subject) + ",");
            headerCount2 = 0 + 1;
        }
        if (priority == 0) {
            headerValuePair.append(toHexString(SIP_INVITE_HEADER_PRIORITY) + "," + toHexString(STANDARD_STRING) + ",");
            headerCount2++;
        } else if (priority == 1) {
            headerValuePair.append(toHexString(SIP_INVITE_HEADER_PRIORITY) + "," + toHexString(IMPORTANT_STRING) + ",");
            headerCount2++;
        }
        if (pictureUrl != null && !TextUtils.isEmpty(pictureUrl)) {
            headerValuePair.append(toHexString(SIP_INVITE_HEADER_CALL_INFO) + "," + toHexString(pictureUrl) + ",");
            headerCount2++;
        }
        if (headerCount2 != 0) {
            headerValuePair.setLength(headerValuePair.length() - 1);
            String header = headerValuePair.toString();
            int total = (headerValuePair.length() / 1000) + 1;
            headerInfo.add("" + 3);
            headerInfo.add("1");
            headerInfo.add("" + total);
            headerInfo.add("");
            headerInfo.add("" + headerCount2);
            headerInfo.add("");
            int i = 1;
            while (i <= total) {
                StringBuilder headerValuePair2 = headerValuePair;
                headerInfo.set(3, "" + i);
                StringBuilder sb = new StringBuilder();
                sb.append("");
                int headerCount3 = headerCount2;
                int total2 = total;
                sb.append(header.substring((i - 1) * 1000, i * 1000 < header.length() ? i * 1000 : header.length()));
                headerInfo.set(5, sb.toString());
                this.mImsRILAdapter.setCallAdditionalInfo(headerInfo, (Message) null);
                i++;
                headerValuePair = headerValuePair2;
                headerCount2 = headerCount3;
                total = total2;
            }
            headerCount = headerCount2;
            int i2 = total;
        } else {
            headerCount = headerCount2;
        }
        Location location = (Location) imsCallProfile.getCallExtraParcelable("android.telephony.ims.extra.LOCATION");
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            ArrayList arrayList = new ArrayList();
            ArrayList<String> arrayList2 = headerInfo;
            ArrayList arrayList3 = arrayList;
            arrayList3.add("" + 3);
            arrayList3.add("2");
            arrayList3.add("" + 1);
            arrayList3.add("" + 1);
            arrayList3.add("" + 1);
            arrayList3.add("" + latitude + "," + longitude);
            this.mImsRILAdapter.setCallAdditionalInfo(arrayList3, (Message) null);
            return;
        }
        ArrayList<String> arrayList4 = headerInfo;
        int i3 = headerCount;
    }

    private String toHexString(String before) {
        if (TextUtils.isEmpty(before)) {
            return "";
        }
        byte[] bytes = before.getBytes();
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            str.append(String.format("%02x", new Object[]{Byte.valueOf(bytes[i])}));
        }
        return str.toString();
    }

    private boolean isEnrichedCallingSupported() {
        return OperatorUtils.isMatched(OperatorUtils.OPID.OP08, this.mPhoneId);
    }
}
