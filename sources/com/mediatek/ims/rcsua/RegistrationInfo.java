package com.mediatek.ims.rcsua;

import android.os.Parcel;
import android.os.Parcelable;

public class RegistrationInfo implements Parcelable {
    public static final Parcelable.Creator<RegistrationInfo> CREATOR = new Parcelable.Creator<RegistrationInfo>() {
        public RegistrationInfo createFromParcel(Parcel in) {
            return new RegistrationInfo(in);
        }

        public RegistrationInfo[] newArray(int size) {
            return new RegistrationInfo[size];
        }
    };
    private Configuration imsConfig;
    private int radioTech;
    private int regMode;
    private int regState;

    public RegistrationInfo(int regState2, int radioTech2, int regMode2) {
        this.regState = regState2;
        this.radioTech = radioTech2;
        this.regMode = regMode2;
        this.imsConfig = new Configuration();
    }

    public RegistrationInfo() {
        this(0, 0, 1);
    }

    protected RegistrationInfo(Parcel in) {
        this.regState = in.readInt();
        this.radioTech = in.readInt();
        this.regMode = in.readInt();
        this.imsConfig = (Configuration) in.readParcelable(Configuration.class.getClassLoader());
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.regState);
        dest.writeInt(this.radioTech);
        dest.writeInt(this.regMode);
        dest.writeParcelable(this.imsConfig, flags);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return (("RegistrationInfo: " + "regState[" + this.regState + "]") + ",radioTech[" + this.radioTech + "]") + ",regMode[" + this.regMode + "]";
    }

    public int getRegState() {
        return this.regState;
    }

    public int getRadioTech() {
        return this.radioTech;
    }

    public int getRegMode() {
        return this.regMode;
    }

    public Configuration getImsConfig() {
        return this.imsConfig;
    }

    public Configuration readImsConfiguration() {
        if (isRegistered()) {
            return new Configuration(this.imsConfig);
        }
        return null;
    }

    public boolean isRegistered() {
        return this.regState != 0;
    }

    public boolean isRegistered(int mode) {
        return this.regMode == mode && this.regState != 0;
    }

    public void setRegState(int regState2) {
        this.regState = regState2;
    }

    public void setRadioTech(int radioTech2) {
        this.radioTech = radioTech2;
    }

    public void setRegMode(int regMode2) {
        this.regMode = regMode2;
    }

    public void setImsConfiguration(Configuration imsConfig2) {
        this.imsConfig = imsConfig2;
    }
}
