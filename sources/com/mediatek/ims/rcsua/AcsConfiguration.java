package com.mediatek.ims.rcsua;

import android.os.Parcel;
import android.os.Parcelable;

public class AcsConfiguration implements Parcelable {
    public static final Parcelable.Creator<AcsConfiguration> CREATOR = new Parcelable.Creator<AcsConfiguration>() {
        public AcsConfiguration createFromParcel(Parcel in) {
            return new AcsConfiguration(in);
        }

        public AcsConfiguration[] newArray(int size) {
            return new AcsConfiguration[size];
        }
    };
    private int version;
    private String xmlData;

    protected AcsConfiguration(Parcel in) {
        this.xmlData = in.readString();
        this.version = in.readInt();
    }

    public AcsConfiguration(String xmlData2, int version2) {
        this.xmlData = xmlData2;
        this.version = version2;
    }

    public String readXmlData() {
        return this.xmlData;
    }

    public int getVersion() {
        return this.version;
    }

    /* access modifiers changed from: package-private */
    public void setXmlData(String xml) {
        this.xmlData = xml;
    }

    /* access modifiers changed from: package-private */
    public void setVersion(int version2) {
        this.version = version2;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.xmlData);
        dest.writeInt(this.version);
    }

    public int describeContents() {
        return 0;
    }
}
