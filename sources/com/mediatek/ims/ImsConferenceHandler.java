package com.mediatek.ims;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ims.ImsConferenceState;
import android.text.TextUtils;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.mediatek.ims.DefaultConferenceHandler;
import com.mediatek.ims.OperatorUtils;
import com.mediatek.ims.internal.ConferenceCallMessageHandler;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ImsConferenceHandler extends DefaultConferenceHandler {
    private static final String ANONYMOUS_URI = "sip:anonymous@anonymous.invalid";
    private static final int CEP_TIMEOUT = 5000;
    private static final int CONFERENCE_STATE_ACTIVE = 1;
    private static final int CONFERENCE_STATE_CLOSED = 0;
    private static final String CONF_HOST = "host";
    private static final boolean DBG = true;
    private static final int EVENT_CLOSE_CONFERENCE = 1;
    private static final int EVENT_HANDLE_CACHED_CONFERENCE_DATA = 2;
    private static final int EVENT_TRY_UPDATE_WITH_LOCAL_CACHE = 0;
    private static final String LOG_TAG = "ImsConferenceHandler";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final boolean SENLOG = TextUtils.equals(Build.TYPE, "user");
    private static final boolean TELDBG;
    private static final String USER_ENTITY = "user-entity";
    private static final boolean VDBG = false;
    private static DefaultConferenceHandler mConfHdler;
    private static DefaultConferenceHandler.Listener mListener;
    /* access modifiers changed from: private */
    public String mAddingParticipant;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Rlog.d(ImsConferenceHandler.LOG_TAG, "received broadcast " + action);
            if (ImsConstants.ACTION_IMS_CONFERENCE_CALL_INDICATION.equals(action)) {
                String data = intent.getStringExtra(ImsConstants.EXTRA_MESSAGE_CONTENT);
                if (intent.getIntExtra(ImsConstants.EXTRA_CALL_ID, 0) != 255 && data != null && !data.equals("")) {
                    boolean unused = ImsConferenceHandler.this.mIsCepNotified = ImsConferenceHandler.DBG;
                    if (ImsConferenceHandler.this.mAddingParticipant == null && ImsConferenceHandler.this.mRemovingParticipant == null) {
                        ImsConferenceHandler.this.handleImsConfCallMessage(data.length(), data);
                    } else {
                        String unused2 = ImsConferenceHandler.this.mCachedConferenceData = data;
                    }
                }
            } else {
                Rlog.e(ImsConferenceHandler.LOG_TAG, "can't handle conference message since no call ID. Abnormal Case");
            }
        }
    };
    /* access modifiers changed from: private */
    public String mCachedConferenceData = null;
    private int mCepVersion = -1;
    private int mConfCallId = -1;
    private LinkedHashMap mConfParticipants = new LinkedHashMap();
    private LinkedHashMap mConfParticipantsAddr = new LinkedHashMap();
    private int mConfState = 0;
    private Context mContext;
    private LinkedHashMap mFirstMergeParticipants = new LinkedHashMap();
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Rlog.d(ImsConferenceHandler.LOG_TAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case 0:
                    if (ImsConferenceHandler.this.mIsCepNotified) {
                        Rlog.d(ImsConferenceHandler.LOG_TAG, "CEP is notified, no need to update with local cache");
                        return;
                    } else {
                        ImsConferenceHandler.this.updateConferenceStateWithLocalCache();
                        return;
                    }
                case 1:
                    ImsConferenceHandler.this.closeConferenceInternal(msg.arg1);
                    return;
                case 2:
                    if (ImsConferenceHandler.this.mCachedConferenceData != null) {
                        ImsConferenceHandler imsConferenceHandler = ImsConferenceHandler.this;
                        imsConferenceHandler.handleImsConfCallMessage(imsConferenceHandler.mCachedConferenceData.length(), ImsConferenceHandler.this.mCachedConferenceData);
                        String unused = ImsConferenceHandler.this.mCachedConferenceData = null;
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHaveUpdateConferenceWithMember = false;
    private String mHostAddr = null;
    /* access modifiers changed from: private */
    public boolean mIsCepNotified = false;
    private boolean mIsFirstCep = DBG;
    private String mLatestRemovedParticipant;
    private ArrayList<String> mLocalParticipants = new ArrayList<>();
    private int mPhoneId = -1;
    private boolean mRemoveParticipantsByUserEntity = false;
    /* access modifiers changed from: private */
    public String mRemovingParticipant;
    private boolean mRestoreParticipantsAddr = DBG;
    private boolean mSupportConferenceManagement = DBG;
    private List<Bundle> mUnknowParticipants = new ArrayList();

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        TELDBG = z;
    }

    public static DefaultConferenceHandler getInstance() {
        if (mConfHdler == null) {
            if (ImsCommonUtil.supportMdAutoSetupIms()) {
                mConfHdler = new DefaultConferenceHandler();
            } else {
                mConfHdler = new ImsConferenceHandler();
            }
        }
        return mConfHdler;
    }

    private ImsConferenceHandler() {
        Rlog.d(LOG_TAG, "ImsConferenceHandler()");
    }

    public void startConference(Context ctx, DefaultConferenceHandler.Listener listener, String callId, int phoneId) {
        if (this.mContext != null) {
            Rlog.d(LOG_TAG, "startConference() failed, a conference is ongoing");
            return;
        }
        Rlog.d(LOG_TAG, "startConference()");
        mListener = listener;
        this.mContext = ctx;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ImsConstants.ACTION_IMS_CONFERENCE_CALL_INDICATION);
        LocalBroadcastManager.getInstance(this.mContext).registerReceiver(this.mBroadcastReceiver, filter);
        this.mConfCallId = Integer.parseInt(callId);
        this.mPhoneId = phoneId;
        this.mIsFirstCep = DBG;
        this.mConfState = 1;
        this.mRemoveParticipantsByUserEntity = OperatorUtils.isMatched(OperatorUtils.OPID.OP132_Peru, this.mPhoneId);
        this.mSupportConferenceManagement = OperatorUtils.isMatched(OperatorUtils.OPID.OP151, this.mPhoneId);
    }

    public void closeConference(String callId) {
        Rlog.d(LOG_TAG, "closeConference() " + callId);
        if (callId != null && this.mConfCallId == Integer.parseInt(callId)) {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(1, Integer.parseInt(callId), 0));
        }
    }

    /* access modifiers changed from: private */
    public void closeConferenceInternal(int callId) {
        Rlog.d(LOG_TAG, "closeConferenceInternal()");
        this.mConfState = 0;
        mListener = null;
        Context context = this.mContext;
        if (context != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this.mBroadcastReceiver);
            this.mContext = null;
        }
        this.mLocalParticipants.clear();
        this.mAddingParticipant = null;
        this.mRemovingParticipant = null;
        this.mConfCallId = -1;
        this.mCepVersion = -1;
        this.mPhoneId = -1;
        this.mHostAddr = null;
        this.mConfParticipants.clear();
        this.mConfParticipantsAddr.clear();
        this.mFirstMergeParticipants.clear();
        this.mUnknowParticipants.clear();
        this.mIsCepNotified = false;
        this.mHandler.removeMessages(0);
        this.mLatestRemovedParticipant = null;
        this.mHaveUpdateConferenceWithMember = false;
        this.mCachedConferenceData = null;
    }

    public boolean isConferenceActive() {
        if (this.mConfState == 1) {
            return DBG;
        }
        return false;
    }

    private String normalizeNumberFromCLIR(String number) {
        return number.replace("*31#", "").replace("#31#", "");
    }

    public void firstMerge(String callId_1, String callId_2, String num_1, String num_2) {
        this.mLocalParticipants.clear();
        this.mFirstMergeParticipants.clear();
        this.mFirstMergeParticipants.put(callId_1, normalizeNumberFromCLIR(num_1));
        this.mFirstMergeParticipants.put(callId_2, normalizeNumberFromCLIR(num_2));
    }

    public void addFirstMergeParticipant(String callId) {
        String num = (String) this.mFirstMergeParticipants.get(callId);
        if (num != null) {
            Rlog.d(LOG_TAG, "addFirstMergeParticipant() callId: " + callId + ", num: " + sensitiveEncode(num));
            this.mLocalParticipants.add(num);
        }
    }

    public void addLocalCache(String[] participants) {
        if (participants != null) {
            this.mLocalParticipants.clear();
            for (String participant : participants) {
                this.mLocalParticipants.add(normalizeNumberFromCLIR(participant));
            }
        }
    }

    public void tryAddParticipant(String addr) {
        this.mAddingParticipant = normalizeNumberFromCLIR(addr);
    }

    public void tryRemoveParticipant(String addr) {
        this.mRemovingParticipant = normalizeNumberFromCLIR(addr);
    }

    public void modifyParticipantComplete() {
        String str = this.mAddingParticipant;
        boolean isFirstMerge = str == null && this.mRemovingParticipant == null;
        if (str != null && (!this.mLocalParticipants.contains(str) || this.mAddingParticipant.isEmpty())) {
            this.mLocalParticipants.add(this.mAddingParticipant);
        }
        String str2 = this.mRemovingParticipant;
        if (str2 != null) {
            this.mLocalParticipants.remove(str2);
            this.mLatestRemovedParticipant = this.mRemovingParticipant;
        }
        this.mAddingParticipant = null;
        this.mRemovingParticipant = null;
        Iterator<String> it = this.mLocalParticipants.iterator();
        while (it.hasNext()) {
            Rlog.d(LOG_TAG, "modifyParticipantComplete: " + sensitiveEncode(it.next()));
        }
        if (this.mSupportConferenceManagement) {
            this.mHandler.sendEmptyMessageDelayed(0, 5000);
        }
        if (this.mCachedConferenceData != null) {
            this.mHandler.sendEmptyMessage(2);
        }
        if (this.mIsCepNotified && isFirstMerge) {
            Rlog.d(LOG_TAG, "CEP is notify before merge complete, notify again");
            notifyConfStateUpdate();
        }
    }

    public void modifyParticipantFailed() {
        this.mAddingParticipant = null;
        this.mRemovingParticipant = null;
        this.mLatestRemovedParticipant = null;
        Iterator<String> it = this.mLocalParticipants.iterator();
        while (it.hasNext()) {
            Rlog.d(LOG_TAG, "modifyParticipantFailed: " + sensitiveEncode(it.next()));
        }
        if (this.mCachedConferenceData != null) {
            this.mHandler.sendEmptyMessage(2);
        }
    }

    public String getConfParticipantUri(String addr, boolean isRetry) {
        String confParticipantUri;
        if (!(!this.mRestoreParticipantsAddr || this.mRemoveParticipantsByUserEntity == isRetry || (confParticipantUri = (String) this.mConfParticipantsAddr.get(addr)) == null)) {
            Rlog.d(LOG_TAG, "removeParticipants confParticipantUri: " + sensitiveEncode(confParticipantUri) + " addr: " + sensitiveEncode(addr));
            addr = confParticipantUri;
        }
        Bundle confInfo = (Bundle) this.mConfParticipants.get(addr);
        if (confInfo != null) {
            String participantUri = confInfo.getString(USER_ENTITY);
            if (participantUri == null || !participantUri.startsWith("sip:")) {
                participantUri = addr;
            }
            Rlog.d(LOG_TAG, "removeParticipants uri: " + sensitiveEncode(participantUri) + " addr: " + sensitiveEncode(addr));
            return participantUri;
        } else if (addr == null || addr.isEmpty()) {
            return ANONYMOUS_URI;
        } else {
            return addr;
        }
    }

    /* access modifiers changed from: private */
    public void updateConferenceStateWithLocalCache() {
        Rlog.d(LOG_TAG, "updateConferenceStateWithLocalCache()");
        if (this.mLocalParticipants.size() != 0 || !shouldAutoTerminateConf()) {
            ImsConferenceState confState = new ImsConferenceState();
            Iterator<String> it = this.mLocalParticipants.iterator();
            while (it.hasNext()) {
                String addr = it.next();
                confState.mParticipants.put(addr, createFakeInfo(addr));
                Rlog.d(LOG_TAG, "submit participants:  uri: " + sensitiveEncode(addr));
            }
            DefaultConferenceHandler.Listener listener = mListener;
            if (listener != null) {
                listener.onParticipantsUpdate(confState);
                return;
            }
            return;
        }
        DefaultConferenceHandler.Listener listener2 = mListener;
        if (listener2 != null) {
            listener2.onAutoTerminate();
        }
        Rlog.d(LOG_TAG, "no participants");
    }

    private Bundle createFakeInfo(String addr) {
        Bundle userInfo = new Bundle();
        userInfo.putString("user", addr);
        userInfo.putString("display-text", addr);
        userInfo.putString("endpoint", addr);
        userInfo.putString(NotificationCompat.CATEGORY_STATUS, ConferenceCallMessageHandler.STATUS_CONNECTED);
        return userInfo;
    }

    private void notifyConfStateUpdate() {
        Rlog.d(LOG_TAG, "notifyConfStateUpdate()");
        ImsConferenceState confState = new ImsConferenceState();
        for (Map.Entry<String, Bundle> entry : this.mConfParticipants.entrySet()) {
            confState.mParticipants.put(entry.getKey(), entry.getValue());
            Rlog.d(LOG_TAG, "submit participants: " + sensitiveEncode(entry.getKey()));
        }
        int key = 0;
        for (Bundle userInfo : this.mUnknowParticipants) {
            confState.mParticipants.put(Integer.toString(key), userInfo);
            Rlog.d(LOG_TAG, "submit unknow participants: " + sensitiveEncode(Integer.toString(key)));
            key++;
        }
        DefaultConferenceHandler.Listener listener = mListener;
        if (listener != null) {
            listener.onParticipantsUpdate(confState);
        }
    }

    private ConferenceCallMessageHandler parseXmlPackage(int len, String data) {
        try {
            InputStream inStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            SAXParser saxParse = factory.newSAXParser();
            ConferenceCallMessageHandler xmlData = new ConferenceCallMessageHandler();
            saxParse.parse(inStream, xmlData);
            return xmlData;
        } catch (Exception ex) {
            Rlog.d(LOG_TAG, "Parsing exception: " + sensitiveEncode(ex.toString()));
            updateConferenceStateWithLocalCache();
            return null;
        }
    }

    private Bundle packUserInfo(ConferenceCallMessageHandler.User user) {
        String entity = user.getEntity();
        String userAddr = getUserNameFromSipTelUriString(entity);
        Bundle userInfo = new Bundle();
        userInfo.putString("user", userAddr);
        userInfo.putString("display-text", user.getDisplayText());
        userInfo.putString("endpoint", user.getEndPoint());
        userInfo.putString(NotificationCompat.CATEGORY_STATUS, user.getStatus());
        userInfo.putString(USER_ENTITY, entity);
        return userInfo;
    }

    private void fullUpdateParticipants(List<ConferenceCallMessageHandler.User> users) {
        Rlog.d(LOG_TAG, "reset all users as participants");
        this.mUnknowParticipants.clear();
        this.mConfParticipants.clear();
        for (ConferenceCallMessageHandler.User user : users) {
            String entity = user.getEntity();
            String userAddr = getUserNameFromSipTelUriString(entity);
            Bundle userInfo = packUserInfo(user);
            Rlog.d(LOG_TAG, "handle user: " + sensitiveEncode(entity) + " addr: " + sensitiveEncode(userAddr));
            if (userAddr == null || userAddr.trim().length() == 0) {
                this.mUnknowParticipants.add(userInfo);
                Rlog.d(LOG_TAG, "add unknow participants");
            } else {
                this.mConfParticipants.put(userAddr, userInfo);
            }
        }
    }

    private void partialUpdateParticipants(List<ConferenceCallMessageHandler.User> users) {
        Rlog.d(LOG_TAG, "partial update participants");
        for (ConferenceCallMessageHandler.User user : users) {
            String entity = user.getEntity();
            String userAddr = getUserNameFromSipTelUriString(entity);
            if (this.mRestoreParticipantsAddr) {
                userAddr = getPairedAddressFromCache(userAddr);
            }
            Bundle userInfo = packUserInfo(user);
            Rlog.d(LOG_TAG, "handle user: " + sensitiveEncode(entity) + " addr: " + sensitiveEncode(userAddr));
            String status = user.getStatus();
            if (userAddr == null || userAddr.trim().length() == 0) {
                if (status.equals(ConferenceCallMessageHandler.STATUS_CONNECTED)) {
                    this.mUnknowParticipants.add(userInfo);
                    Rlog.d(LOG_TAG, "add unknow participants");
                } else if (status.equals(ConferenceCallMessageHandler.STATUS_DISCONNECTED) && this.mUnknowParticipants.size() > 0) {
                    List<Bundle> list = this.mUnknowParticipants;
                    list.remove(list.size() - 1);
                    Rlog.d(LOG_TAG, "remove unknow participants");
                }
            } else if (!status.equals(ConferenceCallMessageHandler.STATUS_DIALING_OUT)) {
                this.mConfParticipants.put(userAddr, userInfo);
            }
        }
    }

    private boolean isEmptyConference() {
        int userCount = this.mUnknowParticipants.size();
        for (Map.Entry<String, Bundle> entry : this.mConfParticipants.entrySet()) {
            String userHandle = entry.getKey();
            if (!entry.getValue().getString(NotificationCompat.CATEGORY_STATUS).equals(ConferenceCallMessageHandler.STATUS_DISCONNECTED) && !isSelfAddress(userHandle)) {
                userCount++;
            }
        }
        if (userCount == 0) {
            return DBG;
        }
        if (this.mHaveUpdateConferenceWithMember) {
            return false;
        }
        Rlog.d(LOG_TAG, "Set mHaveUpdateConferenceWithMember = true");
        this.mHaveUpdateConferenceWithMember = DBG;
        return false;
    }

    /* access modifiers changed from: private */
    public void handleImsConfCallMessage(int len, String data) {
        if (this.mConfCallId == -1) {
            Rlog.e(LOG_TAG, "ImsConference is closed");
        } else if (data == null || data.equals("")) {
            Rlog.e(LOG_TAG, "Failed to handleImsConfCallMessage due to data is empty");
        } else {
            Rlog.d(LOG_TAG, "handleVoLteConfCallMessage, data length = " + data.length());
            ConferenceCallMessageHandler xmlData = parseXmlPackage(len, data);
            if (xmlData == null) {
                Rlog.e(LOG_TAG, "can't create xmlData object, update conf state with local cache");
                updateConferenceStateWithLocalCache();
                return;
            }
            if (this.mHostAddr == null) {
                this.mHostAddr = getUserNameFromSipTelUriString(xmlData.getHostInfo());
            }
            int cepState = xmlData.getCEPState();
            boolean isPartialCEP = cepState == 2 ? DBG : false;
            Rlog.d(LOG_TAG, "isPartialCEP: " + isPartialCEP);
            int version = xmlData.getVersion();
            if (!isPartialCEP) {
                int i = this.mCepVersion;
                if (i <= version || i == -1) {
                    this.mCepVersion = version;
                } else {
                    Rlog.e(LOG_TAG, "version is less than local version" + this.mCepVersion + "," + version);
                    return;
                }
            }
            List<ConferenceCallMessageHandler.User> users = xmlData.getUsers();
            int userCount = xmlData.getUserCount();
            switch (cepState) {
                case 1:
                    fullUpdateParticipants(users);
                    break;
                case 2:
                    partialUpdateParticipants(users);
                    break;
                default:
                    if (userCount != -1 && userCount != users.size()) {
                        partialUpdateParticipants(users);
                        break;
                    } else {
                        fullUpdateParticipants(users);
                        break;
                    }
                    break;
            }
            if (isEmptyConference() && shouldAutoTerminateConf() && !this.mIsFirstCep && this.mHaveUpdateConferenceWithMember) {
                Rlog.d(LOG_TAG, "no participants, terminate the conference");
                DefaultConferenceHandler.Listener listener = mListener;
                if (listener != null) {
                    listener.onAutoTerminate();
                }
            }
            if (this.mRestoreParticipantsAddr) {
                restoreParticipantsAddressByLocalCache();
            }
            notifyConfStateUpdate();
            updateLocalCache();
            this.mIsFirstCep = false;
        }
    }

    private String getPairedAddressFromCache(String addr) {
        Iterator<String> it = this.mLocalParticipants.iterator();
        while (it.hasNext()) {
            String cache = it.next();
            if (PhoneNumberUtils.compareLoosely(addr, cache)) {
                Rlog.d(LOG_TAG, "getPairedAddressFromCache: " + sensitiveEncode(cache));
                this.mConfParticipantsAddr.put(cache, addr);
                return cache;
            }
        }
        String str = this.mLatestRemovedParticipant;
        if (str == null || !PhoneNumberUtils.compareLoosely(addr, str)) {
            return addr;
        }
        Rlog.d(LOG_TAG, "getPairedAddressFromLatestRemoved: " + sensitiveEncode(this.mLatestRemovedParticipant));
        return this.mLatestRemovedParticipant;
    }

    private void updateLocalCache() {
        for (Map.Entry<String, Bundle> entry : this.mConfParticipants.entrySet()) {
            Bundle confInfo = entry.getValue();
            String status = confInfo.getString(NotificationCompat.CATEGORY_STATUS);
            String addr = confInfo.getString("user");
            if (status.equals(ConferenceCallMessageHandler.STATUS_DISCONNECTED)) {
                this.mLocalParticipants.remove(addr);
            }
        }
    }

    private void restoreParticipantsAddressByLocalCache() {
        ArrayList<String> restoreCandidate;
        ArrayList<String> restoreCandidate2 = new ArrayList<>(this.mLocalParticipants);
        LinkedHashMap restoreList = new LinkedHashMap();
        for (Map.Entry<String, Bundle> entry : new LinkedHashMap(this.mConfParticipants).entrySet()) {
            String userHandle = entry.getKey();
            Bundle confInfo = entry.getValue();
            String restoreAddr = getPairedAddressFromCache(userHandle);
            if (isSelfAddress(userHandle) || restoreCandidate2.remove(restoreAddr)) {
                confInfo.putString("user", restoreAddr);
                this.mConfParticipants.put(userHandle, confInfo);
                Rlog.d(LOG_TAG, "restore participant: " + sensitiveEncode(userHandle) + " to: " + sensitiveEncode(restoreAddr));
            } else {
                restoreList.put(userHandle, confInfo);
                Rlog.d(LOG_TAG, "wait for restore: " + sensitiveEncode(restoreAddr));
            }
        }
        Iterator<Map.Entry<String, Bundle>> resIterator = restoreList.entrySet().iterator();
        ArrayList<String> restoreUnknowCandidates = new ArrayList<>(restoreCandidate2);
        int restoreIndex = 0;
        while (true) {
            if (!resIterator.hasNext()) {
                LinkedHashMap linkedHashMap = restoreList;
                break;
            } else if (restoreCandidate2.size() <= restoreIndex) {
                Rlog.d(LOG_TAG, "No candidate to restore, size: " + restoreCandidate2.size() + ", index: " + restoreIndex);
                ArrayList<String> arrayList = restoreCandidate2;
                LinkedHashMap linkedHashMap2 = restoreList;
                break;
            } else {
                Map.Entry<String, Bundle> entry2 = resIterator.next();
                String userHandle2 = entry2.getKey();
                Bundle confInfo2 = entry2.getValue();
                String restoreAddr2 = restoreCandidate2.get(restoreIndex);
                if (restoreUnknowCandidates.size() > 0) {
                    restoreCandidate = restoreCandidate2;
                    restoreUnknowCandidates.remove(0);
                } else {
                    restoreCandidate = restoreCandidate2;
                }
                String status = confInfo2.getString(NotificationCompat.CATEGORY_STATUS);
                LinkedHashMap restoreList2 = restoreList;
                if (status.equals(ConferenceCallMessageHandler.STATUS_DISCONNECTED)) {
                    restoreCandidate2 = restoreCandidate;
                    restoreList = restoreList2;
                } else {
                    this.mConfParticipantsAddr.put(restoreAddr2, userHandle2);
                    confInfo2.putString("user", restoreAddr2);
                    this.mConfParticipants.put(userHandle2, confInfo2);
                    StringBuilder sb = new StringBuilder();
                    sb.append("restore participant: ");
                    String str = status;
                    sb.append(sensitiveEncode(userHandle2));
                    sb.append(" to: ");
                    sb.append(sensitiveEncode(restoreAddr2));
                    Rlog.d(LOG_TAG, sb.toString());
                    restoreIndex++;
                    restoreCandidate2 = restoreCandidate;
                    restoreList = restoreList2;
                }
            }
        }
        restoreUnknowParticipants(restoreUnknowCandidates);
    }

    private void restoreUnknowParticipants(ArrayList<String> restoreUnknowCandidates) {
        List<Bundle> restoredUnknowParticipants = new ArrayList<>(this.mUnknowParticipants);
        int restoreIndex = 0;
        for (Bundle userInfo : this.mUnknowParticipants) {
            if (restoreUnknowCandidates.size() <= restoreIndex) {
                restoredUnknowParticipants.add(userInfo);
            } else {
                String restoreAddr = restoreUnknowCandidates.get(restoreIndex);
                userInfo.putString("user", restoreAddr);
                this.mConfParticipants.put(restoreAddr, userInfo);
                if (restoredUnknowParticipants.size() > 0) {
                    restoredUnknowParticipants.remove(0);
                }
                Rlog.d(LOG_TAG, "restore unknow participants(" + restoreIndex + ") to: " + sensitiveEncode(restoreAddr));
                restoreIndex++;
            }
        }
        this.mUnknowParticipants = restoredUnknowParticipants;
    }

    private boolean shouldAutoTerminateConf() {
        Rlog.d(LOG_TAG, "shouldTerminate:" + DBG);
        return DBG;
    }

    private String getUserNameFromSipTelUriString(String uriString) {
        String address;
        String userName;
        if (uriString == null || (address = Uri.parse(uriString).getSchemeSpecificPart()) == null || (userName = PhoneNumberUtils.getUsernameFromUriNumber(address)) == null) {
            return null;
        }
        int pIndex = userName.indexOf(59);
        int wIndex = userName.indexOf(44);
        if (pIndex >= 0 && wIndex >= 0) {
            return userName.substring(0, Math.min(pIndex, wIndex));
        }
        if (pIndex >= 0) {
            return userName.substring(0, pIndex);
        }
        if (wIndex >= 0) {
            return userName.substring(0, wIndex);
        }
        return userName;
    }

    private String sensitiveEncode(String msg) {
        return ImsServiceCallTracker.sensitiveEncode(msg);
    }

    private boolean isSelfAddress(String addr) {
        if (!PhoneNumberUtils.compareLoosely(this.mHostAddr, addr)) {
            return ImsServiceCallTracker.getInstance(this.mPhoneId).isSelfAddress(addr);
        }
        Rlog.d(LOG_TAG, "isSelfAddress(): true, meet host info in xml");
        return DBG;
    }
}
