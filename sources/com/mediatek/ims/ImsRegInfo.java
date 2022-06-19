package com.mediatek.ims;

public class ImsRegInfo {
    public int mAccountId;
    public int mErrorCode;
    public String mErrorMsg;
    public int mExpireTime;
    public int mReportType;
    public String mUri;

    public ImsRegInfo(int type, int accountId, int expireTime, int errCode, String uri, String errMsg) {
        this.mReportType = type;
        this.mAccountId = accountId;
        this.mExpireTime = expireTime;
        this.mErrorCode = errCode;
        this.mUri = uri;
        this.mErrorMsg = errMsg;
    }

    public String toString() {
        return "ImsRegInfo :: {" + this.mReportType + ", " + this.mAccountId + ", " + this.mExpireTime + ", " + this.mErrorCode + "," + this.mErrorMsg + "}";
    }
}
