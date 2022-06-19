package com.mediatek.ims.rcsua;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Configuration implements Parcelable {
    public static final Parcelable.Creator<Configuration> CREATOR = new Parcelable.Creator<Configuration>() {
        public Configuration createFromParcel(Parcel in) {
            return new Configuration(in);
        }

        public Configuration[] newArray(int size) {
            return new Configuration[size];
        }
    };
    public static final int IPv4 = 0;
    public static final int IPv6 = 1;
    public static final int SCTP = 8;
    public static final int TCP = 1;
    public static final int TLS = 4;
    public static final int UDP = 2;
    private String IMPI;
    private String IMPU;
    private int digitVlineNumber;
    private String homeDomain;
    private String imei;
    private String instanceId;
    private int ipVersion;
    private String localAddress;
    private int localPort;
    private String pAccessNetworkInfo;
    private String pAssociatedUri;
    private String pIdentifier;
    private String pLastAccessNetworkInfo;
    private String pPreferredAssociation;
    private int portS;
    private int protocol;
    private String proxyAddress;
    private int proxyPort;
    private String publicGruu;
    private int regRcsFeatureTags;
    private String security_verify;
    private String serviceRoute;
    private String tempGruu;
    private String userAgent;
    private int viaPort;

    @Retention(RetentionPolicy.SOURCE)
    public @interface IpVersion {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Transport {
    }

    public Configuration() {
        init();
    }

    public Configuration(Configuration other) {
        this.localAddress = other.localAddress;
        this.localPort = other.localPort;
        this.proxyAddress = other.proxyAddress;
        this.proxyPort = other.proxyPort;
        this.protocol = other.protocol;
        this.ipVersion = other.ipVersion;
        this.IMPU = other.IMPU;
        this.IMPI = other.IMPI;
        this.homeDomain = other.homeDomain;
        this.userAgent = other.userAgent;
        this.portS = other.portS;
        this.viaPort = other.viaPort;
        this.security_verify = other.security_verify;
        this.pAssociatedUri = other.pAssociatedUri;
        this.instanceId = other.instanceId;
        this.serviceRoute = other.serviceRoute;
        this.pAccessNetworkInfo = other.pAccessNetworkInfo;
        this.pLastAccessNetworkInfo = other.pLastAccessNetworkInfo;
        this.publicGruu = other.publicGruu;
        this.tempGruu = other.tempGruu;
        this.digitVlineNumber = other.digitVlineNumber;
        this.pIdentifier = other.pIdentifier;
        this.pPreferredAssociation = other.pPreferredAssociation;
        this.regRcsFeatureTags = other.regRcsFeatureTags;
        this.imei = other.imei;
    }

    protected Configuration(Parcel in) {
        this.localAddress = in.readString();
        this.localPort = in.readInt();
        this.proxyAddress = in.readString();
        this.proxyPort = in.readInt();
        this.protocol = in.readInt();
        this.ipVersion = in.readInt();
        this.IMPU = in.readString();
        this.IMPI = in.readString();
        this.homeDomain = in.readString();
        this.userAgent = in.readString();
        this.portS = in.readInt();
        this.viaPort = in.readInt();
        this.security_verify = in.readString();
        this.pAssociatedUri = in.readString();
        this.instanceId = in.readString();
        this.serviceRoute = in.readString();
        this.pAccessNetworkInfo = in.readString();
        this.pLastAccessNetworkInfo = in.readString();
        this.publicGruu = in.readString();
        this.tempGruu = in.readString();
        this.digitVlineNumber = in.readInt();
        this.pIdentifier = in.readString();
        this.pPreferredAssociation = in.readString();
        this.regRcsFeatureTags = in.readInt();
        this.imei = in.readString();
    }

    public void reset() {
        init();
    }

    public String toString() {
        return "Configuration->" + "localAddress[" + this.localAddress + "]," + "localPort[" + this.localPort + "]," + "proxyAddress[" + this.proxyAddress + "]," + "proxyPort[" + this.proxyPort + "]," + "protocol[" + this.protocol + "]," + "ipVersion[" + this.ipVersion + "]," + "IMPU[" + this.IMPU + "]," + "IMPI[" + this.IMPI + "]," + "homeDomain[" + this.homeDomain + "]," + "userAgent[" + this.userAgent + "]," + "port_s[" + this.portS + "]," + "viaPort[" + this.viaPort + "]," + "security_verify[" + this.security_verify + "]," + "pAssociatedUri[" + this.pAssociatedUri + "]," + "instanceId[" + this.instanceId + "]," + "serviceRoute[" + this.serviceRoute + "]," + "pAccessNetworkInfo[" + this.pAccessNetworkInfo + "]," + "pLastAccessNetworkInfo[" + this.pLastAccessNetworkInfo + "]," + "publicGruu[" + this.publicGruu + "]," + "tempGruu[" + this.tempGruu + "]," + "regRcsFeatureTags[" + this.regRcsFeatureTags + "]" + "imei[" + this.imei + "]";
    }

    public String getLocalAddress() {
        return this.localAddress;
    }

    public int getLocalPort() {
        return this.localPort;
    }

    public String getProxyAddress() {
        return this.proxyAddress;
    }

    public int getProxyPort() {
        return this.proxyPort;
    }

    public int getProtocol() {
        return this.protocol;
    }

    public int getIpVersion() {
        return this.ipVersion;
    }

    public String getIMPU() {
        return this.IMPU;
    }

    public String getIMPI() {
        return this.IMPI;
    }

    public String getHomeDomain() {
        return this.homeDomain;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public int getPortS() {
        return this.portS;
    }

    public int getViaPort() {
        return this.viaPort;
    }

    public String getSecurityVerify() {
        return this.security_verify;
    }

    public String[] getPAssociatedUri() {
        return this.pAssociatedUri.split(",");
    }

    public String getPAssociatedUriStr() {
        Log.d("MtkConfiguration", "getPAssociatedUri pAssociatedUri.split[0] " + this.pAssociatedUri);
        return this.pAssociatedUri;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public String[] getServiceRoute() {
        return this.serviceRoute.split(",");
    }

    public String getServiceRouteStr() {
        return this.serviceRoute;
    }

    public String getPAccessNetworkInfo() {
        return this.pAccessNetworkInfo;
    }

    public String getPLastAccessNetworkInfo() {
        return this.pLastAccessNetworkInfo;
    }

    public String getPublicGruu() {
        return this.publicGruu;
    }

    public String getTempGruu() {
        return this.tempGruu;
    }

    public int getDigitVlineNumber() {
        return this.digitVlineNumber;
    }

    public String getPIdentifier() {
        return this.pIdentifier;
    }

    public String getPPreferredAssociation() {
        return this.pPreferredAssociation;
    }

    public int getRegRcsFeatureTags() {
        return this.regRcsFeatureTags;
    }

    public String getImei() {
        return this.imei;
    }

    public void setLocalAddress(String localAddress2) {
        this.localAddress = localAddress2;
    }

    public void setLocalPort(int localPort2) {
        this.localPort = localPort2;
    }

    public void setProxyAddress(String proxyAddress2) {
        this.proxyAddress = proxyAddress2;
    }

    public void setProxyPort(int proxyPort2) {
        this.proxyPort = proxyPort2;
    }

    public void setProtocol(int protocol2) {
        this.protocol = protocol2;
    }

    public void setIpVersion(int ipVersion2) {
        this.ipVersion = ipVersion2;
    }

    public void setIMPU(String IMPU2) {
        this.IMPU = IMPU2;
    }

    public void setIMPI(String IMPI2) {
        this.IMPI = IMPI2;
    }

    public void setHomeDomain(String homeDomain2) {
        this.homeDomain = homeDomain2;
    }

    public void setUserAgent(String userAgent2) {
        this.userAgent = userAgent2;
    }

    public void setPortS(int portS2) {
        this.portS = portS2;
    }

    public void setViaPort(int viaPort2) {
        this.viaPort = viaPort2;
    }

    public void setSecurityVerify(String security_verify2) {
        this.security_verify = security_verify2;
    }

    public void setPAssociatedUri(String pAssociatedUri2) {
        this.pAssociatedUri = pAssociatedUri2;
    }

    public void setInstanceId(String instanceId2) {
        this.instanceId = instanceId2;
    }

    public void setServiceRoute(String serviceRoute2) {
        this.serviceRoute = serviceRoute2;
    }

    public void setPAccessNetworkInfo(String pAccessNetworkInfo2) {
        this.pAccessNetworkInfo = pAccessNetworkInfo2;
    }

    public void setPLastAccessNetworkInfo(String pLastAccessNetworkInfo2) {
        this.pLastAccessNetworkInfo = pLastAccessNetworkInfo2;
    }

    public void setPublicGruu(String publicGruu2) {
        this.publicGruu = publicGruu2;
    }

    public void setTempGruu(String tempGruu2) {
        this.tempGruu = tempGruu2;
    }

    public void setDigitVlineNumber(int digitVlineNumber2) {
        this.digitVlineNumber = digitVlineNumber2;
    }

    public void setPIdentifier(String pIdentifier2) {
        this.pIdentifier = pIdentifier2;
    }

    public void setPPreferredAssociation(String pPreferredAssociation2) {
        this.pPreferredAssociation = pPreferredAssociation2;
    }

    public void setRegRcsFeatureTags(int regRcsFeatureTags2) {
        this.regRcsFeatureTags = regRcsFeatureTags2;
    }

    public void setImei(String imei2) {
        this.imei = imei2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.localAddress);
        dest.writeInt(this.localPort);
        dest.writeString(this.proxyAddress);
        dest.writeInt(this.proxyPort);
        dest.writeInt(this.protocol);
        dest.writeInt(this.ipVersion);
        dest.writeString(this.IMPU);
        dest.writeString(this.IMPI);
        dest.writeString(this.homeDomain);
        dest.writeString(this.userAgent);
        dest.writeInt(this.portS);
        dest.writeInt(this.viaPort);
        dest.writeString(this.security_verify);
        dest.writeString(this.pAssociatedUri);
        dest.writeString(this.instanceId);
        dest.writeString(this.serviceRoute);
        dest.writeString(this.pAccessNetworkInfo);
        dest.writeString(this.pLastAccessNetworkInfo);
        dest.writeString(this.publicGruu);
        dest.writeString(this.tempGruu);
        dest.writeInt(this.digitVlineNumber);
        dest.writeString(this.pIdentifier);
        dest.writeString(this.pPreferredAssociation);
        dest.writeInt(this.regRcsFeatureTags);
        dest.writeString(this.imei);
    }

    private void init() {
        this.localAddress = "";
        this.localPort = 0;
        this.proxyAddress = "";
        this.proxyPort = 0;
        this.protocol = 2;
        this.ipVersion = 0;
        this.IMPU = "";
        this.IMPI = "";
        this.homeDomain = "";
        this.userAgent = "";
        this.portS = 0;
        this.viaPort = 0;
        this.security_verify = "";
        this.pAssociatedUri = "";
        this.instanceId = "";
        this.serviceRoute = "";
        this.pAccessNetworkInfo = "";
        this.pLastAccessNetworkInfo = "";
        this.publicGruu = "";
        this.tempGruu = "";
        this.digitVlineNumber = 0;
        this.pIdentifier = "";
        this.pPreferredAssociation = "";
        this.regRcsFeatureTags = 0;
        this.imei = "";
    }
}
