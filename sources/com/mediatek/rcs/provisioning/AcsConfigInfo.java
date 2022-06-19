package com.mediatek.rcs.provisioning;

import android.os.Parcel;
import android.os.Parcelable;

public class AcsConfigInfo implements Parcelable {
    public static final int CONFIGURED = 1;
    public static final Parcelable.Creator<AcsConfigInfo> CREATOR = new Parcelable.Creator<AcsConfigInfo>() {
        public AcsConfigInfo createFromParcel(Parcel in) {
            return new AcsConfigInfo(in);
        }

        public AcsConfigInfo[] newArray(int size) {
            return new AcsConfigInfo[size];
        }
    };
    public static final int PRE_CONFIGURATION = 0;
    private int mConfigStatus;
    private int mVersion;
    private String mXmlFileContent;

    public AcsConfigInfo(String content, int configStatus, int version) {
        this.mXmlFileContent = null;
        this.mConfigStatus = 0;
        this.mVersion = 0;
        if (content != null) {
            this.mXmlFileContent = content;
        }
        this.mConfigStatus = configStatus;
        this.mVersion = version;
    }

    public int describeContents() {
        return 0;
    }

    public String getData() {
        return this.mXmlFileContent;
    }

    public int getStatus() {
        return this.mConfigStatus;
    }

    public int getVersion() {
        return this.mVersion;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mXmlFileContent);
        out.writeInt(this.mConfigStatus);
        out.writeInt(this.mVersion);
    }

    private AcsConfigInfo(Parcel in) {
        this.mXmlFileContent = null;
        this.mConfigStatus = 0;
        this.mVersion = 0;
        this.mXmlFileContent = in.readString();
        this.mConfigStatus = in.readInt();
        this.mVersion = in.readInt();
    }
}
