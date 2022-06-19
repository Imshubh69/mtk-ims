package com.mediatek.ims.internal;

import android.os.Build;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import androidx.core.app.NotificationCompat;
import com.mediatek.ims.ImsServiceCallTracker;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ConferenceCallMessageHandler extends DefaultHandler {
    public static final int CEP_STATE_FULL = 1;
    public static final int CEP_STATE_PARTIAL = 2;
    public static final int CEP_STATE_UNKNOWN = 0;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final boolean SENLOG = TextUtils.equals(Build.TYPE, "user");
    public static final String STATUS_ALERTING = "alerting";
    public static final String STATUS_CONNECTED = "connected";
    public static final String STATUS_CONNECT_FAIL = "connect-fail";
    public static final String STATUS_DIALING_IN = "dialing-in";
    public static final String STATUS_DIALING_OUT = "dialing-out";
    public static final String STATUS_DISCONNECTED = "disconnected";
    public static final String STATUS_DISCONNECTING = "disconnecting";
    public static final String STATUS_MUTED_VIA_FOCUS = "muted-via-focus";
    public static final String STATUS_ON_HOLD = "on-hold";
    public static final String STATUS_PENDING = "pending";
    private static final String TAG = "ConferenceCallMessageHandler";
    private static final boolean TELDBG;
    private int mCEPState = 0;
    private int mCallId = -1;
    private String mHostInfo;
    private int mIndex = 0;
    private int mMaxUserCount;
    private boolean mParsingHostInfo = false;
    private String mTag;
    private String mTempVal;
    private User mUser;
    private int mUserCount = -1;
    private List<User> mUsers;
    private int mVersion = -1;

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        TELDBG = z;
    }

    public class User {
        private int mConnectionIndex = -1;
        private String mDisplayText;
        private String mEndPoint;
        private String mEntity;
        private int mIndex;
        private String mSipTelUri;
        private String mStatus = ConferenceCallMessageHandler.STATUS_DISCONNECTED;

        public User() {
        }

        /* access modifiers changed from: package-private */
        public void setEndPoint(String endPoint) {
            this.mEndPoint = endPoint;
        }

        /* access modifiers changed from: package-private */
        public void setEntity(String entity) {
            this.mEntity = entity;
        }

        /* access modifiers changed from: package-private */
        public void setSipTelUri(String uri) {
            this.mSipTelUri = uri;
        }

        /* access modifiers changed from: package-private */
        public void setDisplayText(String displayText) {
            this.mDisplayText = displayText;
        }

        /* access modifiers changed from: package-private */
        public void setStatus(String status) {
            this.mStatus = status;
        }

        /* access modifiers changed from: package-private */
        public void setIndex(int index) {
            this.mIndex = index;
        }

        public void setConnectionIndex(int index) {
            this.mConnectionIndex = index;
        }

        public String getEndPoint() {
            return this.mEndPoint;
        }

        public String getEntity() {
            return this.mEntity;
        }

        public String getSipTelUri() {
            return this.mSipTelUri;
        }

        public String getDisplayText() {
            return this.mDisplayText;
        }

        public String getStatus() {
            return this.mStatus;
        }

        public int getIndex() {
            return this.mIndex;
        }

        public int getConnectionIndex() {
            return this.mConnectionIndex;
        }
    }

    public List<User> getUsers() {
        return this.mUsers;
    }

    private void setMaxUserCount(String maxUserCount) {
        this.mMaxUserCount = Integer.parseInt(maxUserCount);
    }

    public int getMaxUserCount() {
        return this.mMaxUserCount;
    }

    public void setCallId(int callId) {
        this.mCallId = callId;
    }

    public int getCallId() {
        return this.mCallId;
    }

    public void startDocument() throws SAXException {
        this.mUsers = new ArrayList();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.mTag == null) {
            Rlog.d(TAG, "Parse val failed: tag is null");
            return;
        }
        String val = new String(ch, start, length);
        telLog("Current tag: " + this.mTag + " val: " + sensitiveEncode(val));
        if (this.mTag.equalsIgnoreCase("maximum-user-count")) {
            setMaxUserCount(val);
        } else if (this.mTag.equalsIgnoreCase("user-count")) {
            this.mUserCount = Integer.parseInt(val);
        } else if (this.mParsingHostInfo && this.mTag.equalsIgnoreCase("uri")) {
            this.mHostInfo = val;
        }
        if (this.mUser == null) {
            Rlog.d(TAG, "Parse val failed: user is null");
        } else if (this.mTag.equalsIgnoreCase("display-text")) {
            this.mUser.setDisplayText(val);
        } else if (this.mTag.equalsIgnoreCase(NotificationCompat.CATEGORY_STATUS)) {
            this.mUser.setStatus(val);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("user")) {
            this.mIndex++;
            User user = new User();
            this.mUser = user;
            user.setIndex(this.mIndex);
            this.mUser.setEntity(attributes.getValue("", "entity"));
            telLog("user - entity: " + sensitiveEncode(this.mUser.getEntity()));
        } else if (qName.equalsIgnoreCase("endpoint")) {
            this.mUser.setEndPoint(attributes.getValue("", "entity"));
            telLog("endpoint - entity: " + sensitiveEncode(this.mUser.getEndPoint()));
        } else if (qName.equalsIgnoreCase("endpoint")) {
            this.mUser.setEndPoint(attributes.getValue("", "entity"));
        } else if (qName.equalsIgnoreCase("host-info")) {
            this.mParsingHostInfo = true;
        } else if (qName.equalsIgnoreCase("conference-info")) {
            try {
                this.mVersion = Integer.valueOf(attributes.getValue("version")).intValue();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            telLog("version:" + this.mVersion);
            String state = attributes.getValue("state");
            this.mCEPState = updateCEPState(state);
            telLog("get CEP state: " + state);
        }
        this.mTag = qName;
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        List<User> list;
        if (qName.equalsIgnoreCase("user") && (list = this.mUsers) != null) {
            list.add(this.mUser);
            this.mUser = null;
        } else if (qName.equalsIgnoreCase("host-info")) {
            this.mParsingHostInfo = false;
        }
        this.mTag = null;
    }

    public int getUserCount() {
        return this.mUserCount;
    }

    public String getHostInfo() {
        return this.mHostInfo;
    }

    public int getVersion() {
        return this.mVersion;
    }

    public int getCEPState() {
        return this.mCEPState;
    }

    private int updateCEPState(String val) {
        if (val == null) {
            return 0;
        }
        if (val.equalsIgnoreCase("full")) {
            return 1;
        }
        if (val.equalsIgnoreCase("partial")) {
            return 2;
        }
        return 0;
    }

    private void telLog(String msg) {
        if (TELDBG) {
            Rlog.d(TAG, msg);
        }
    }

    private String sensitiveEncode(String msg) {
        return ImsServiceCallTracker.sensitiveEncode(msg);
    }
}
