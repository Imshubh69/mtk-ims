package com.mediatek.ims.rcse;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkRequest;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.DelegateMessageCallback;
import android.telephony.ims.DelegateRegistrationState;
import android.telephony.ims.DelegateRequest;
import android.telephony.ims.DelegateStateCallback;
import android.telephony.ims.FeatureTagState;
import android.telephony.ims.SipDelegateConfiguration;
import android.telephony.ims.stub.SipDelegate;
import android.telephony.ims.stub.SipTransportImplBase;
import android.text.TextUtils;
import android.util.Log;
import com.mediatek.ims.rcsua.Configuration;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

public class MtkSipTransport extends SipTransportImplBase {
    private static final String LOG_TAG = "MtkSipTransport";
    private ConnectivityManager.NetworkCallback cellularCallback = null;
    /* access modifiers changed from: private */

    /* renamed from: cm */
    public ConnectivityManager f41cm = null;
    /* access modifiers changed from: private */
    public String ipv4localAddress = null;
    /* access modifiers changed from: private */
    public String ipv6localAddress = null;
    private InetSocketAddress localAddr = null;
    private String mCid = "";
    private Configuration mConfig = null;
    private Context mContext = null;
    private String mCurrentPlmn = "";
    private DelegateMessageCallback mDelegateMessageCallback = null;
    private DelegateStateCallback mDelegateStateCallBack = null;
    private final ArrayList<MtkSipDelegate> mDelegates = new ArrayList<>();
    private String mLac = "";
    /* access modifiers changed from: private */
    public LinkProperties mLinkProp = null;
    /* access modifiers changed from: private */
    public Network mNetworkObj = null;
    private String mOldPlmn = "";
    private String[] mPcscfList;
    private int mSlotId = -1;
    private SipDelegateConfiguration sipConfig = null;

    /* renamed from: tm */
    private TelephonyManager f42tm = null;
    private ConnectivityManager.NetworkCallback wifiCallback = null;

    public MtkSipTransport(Executor executor, Context context) {
        super(executor);
        this.mContext = context;
        Log.d(LOG_TAG, " MtkSipTransport executor: " + executor + " ,mContext: " + this.mContext);
        this.f41cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        StringBuilder sb = new StringBuilder();
        sb.append(" MtkSipTransport cm: ");
        sb.append(this.f41cm);
        Log.d(LOG_TAG, sb.toString());
        this.f42tm = (TelephonyManager) this.mContext.getSystemService("phone");
        Log.d(LOG_TAG, " MtkSipTransport tm: " + this.f42tm);
        registerNetworkCallback();
    }

    public void createSipDelegate(int subscriptionId, DelegateRequest request, DelegateStateCallback dc, DelegateMessageCallback mc) {
        int i = subscriptionId;
        DelegateRequest delegateRequest = request;
        DelegateStateCallback delegateStateCallback = dc;
        DelegateMessageCallback delegateMessageCallback = mc;
        Log.d(LOG_TAG, "2 MtkSipTransport called createSipDelegate subscriptionId: " + i);
        Log.d(LOG_TAG, "MtkSipTransport called createSipDelegate request: " + delegateRequest);
        Log.d(LOG_TAG, "MtkSipTransport called createSipDelegate dc: " + delegateStateCallback);
        Log.d(LOG_TAG, "MtkSipTransport called createSipDelegate mc: " + delegateMessageCallback);
        this.mDelegateStateCallBack = delegateStateCallback;
        this.mDelegateMessageCallback = delegateMessageCallback;
        MtkSipDelegate d = new MtkSipDelegate(i, delegateRequest, delegateStateCallback, delegateMessageCallback);
        Log.d(LOG_TAG, "createSipDelegate d: " + d);
        Log.d(LOG_TAG, "createSipDelegate request.toString: " + request.toString());
        Log.d(LOG_TAG, "createSipDelegate request getFeature: " + request.getFeatureTags());
        Log.d(LOG_TAG, "createSipDelegate UaServiceManager.getInstance().getService() : " + UaServiceManager.getInstance().getService());
        if (UaServiceManager.getInstance().getService() != null) {
            this.mConfig = UaServiceManager.getInstance().readConfiguraion();
            Log.d(LOG_TAG, "createSipDelegate mConfig : " + this.mConfig.toString());
            Log.d(LOG_TAG, "createSipDelegate mConfig : " + this.mConfig.getPAssociatedUri());
            String pLast = this.mConfig.getPLastAccessNetworkInfo();
            Log.d(LOG_TAG, "createSipDelegate pLast : " + pLast);
            if (TextUtils.isEmpty(pLast)) {
                pLast = this.mConfig.getPAccessNetworkInfo();
            }
            Log.d(LOG_TAG, "createSipDelegate ipv6localAddr ess : " + this.ipv6localAddress);
            Log.d(LOG_TAG, "createSipDelegate ipv4localAddress : " + this.ipv4localAddress);
            if (this.ipv6localAddress != null) {
                this.localAddr = new InetSocketAddress(this.ipv6localAddress, this.mConfig.getLocalPort());
            } else {
                this.localAddr = new InetSocketAddress(this.ipv4localAddress, this.mConfig.getLocalPort());
            }
            Log.d(LOG_TAG, "createSipDelegate localAddr : " + this.localAddr);
            SipDelegateConfiguration.IpSecConfiguration ipSecConfiguration = new SipDelegateConfiguration.IpSecConfiguration(0, 0, 0, 0, 0, 0, this.mConfig.getSecurityVerify());
            InetSocketAddress inetSocketAddress = this.localAddr;
            SipDelegateConfiguration.Builder builder = r10;
            SipDelegateConfiguration.Builder builder2 = new SipDelegateConfiguration.Builder(1, 1, inetSocketAddress, inetSocketAddress);
            this.sipConfig = builder.setPublicUserIdentifier(this.mConfig.getPIdentifier()).setHomeDomain(this.mConfig.getHomeDomain()).setSipAssociatedUriHeader(this.mConfig.getPAssociatedUriStr()).setIpSecConfiguration(ipSecConfiguration).setSipServiceRouteHeader(this.mConfig.getServiceRouteStr()).setSipContactUserParameter("user").setImei(this.mConfig.getImei()).setSipPaniHeader(this.mConfig.getPAccessNetworkInfo()).setSipPlaniHeader(pLast).setSipUserAgentHeader(this.mConfig.getUserAgent()).setMaxUdpPayloadSizeBytes(1500).build();
            Log.d(LOG_TAG, "createSipDelegate, sipConfig: " + this.sipConfig);
        }
        Set<FeatureTagState> deniedTags = new HashSet<>();
        String featureTagStr = "";
        for (String featureTag : request.getFeatureTags()) {
            Log.d(LOG_TAG, "createSipDelegate featureTag: " + featureTag);
            featureTagStr = featureTag;
            deniedTags.add(new FeatureTagState("", 2));
        }
        Log.d(LOG_TAG, "createSipDelegate deniedTags: " + deniedTags);
        Log.d(LOG_TAG, "createSipDelegate featureTagStr: " + featureTagStr);
        Log.d(LOG_TAG, "createSipDelegate mDelegateStateCallBack.onCreated called: ");
        this.mDelegateStateCallBack.onCreated(d, deniedTags);
        Log.d(LOG_TAG, "createSipDelegate mDelegateStateCallBack.onConfigurationChanged called: ");
        this.mDelegateStateCallBack.onConfigurationChanged(this.sipConfig);
        this.mDelegateStateCallBack.onFeatureTagRegistrationChanged(new DelegateRegistrationState.Builder().addRegisteredFeatureTag(featureTagStr).build());
        Log.d(LOG_TAG, "createSipDelegate d: " + d);
        this.mDelegates.add(d);
    }

    public void destroySipDelegate(SipDelegate delegate, int reason) {
        Log.d(LOG_TAG, "SipTransport called destroySipDelegate delegate: " + delegate + " ,reason: " + reason);
        if (delegate instanceof MtkSipDelegate) {
            Log.d(LOG_TAG, "SipTransport called destroySipDelegate delegate instanceof MtkSipDelegate");
            Log.d(LOG_TAG, "destroySipDelegate mDelegateStateCallBack: " + this.mDelegateStateCallBack);
            DelegateStateCallback delegateStateCallback = this.mDelegateStateCallBack;
            if (delegateStateCallback != null) {
                delegateStateCallback.onDestroyed(reason);
            }
            this.mDelegates.remove(delegate);
        }
    }

    public List<MtkSipDelegate> getDelegates() {
        return this.mDelegates;
    }

    public static int getCurrentUserPhoneId() {
        int phoneId = SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
        Log.d(LOG_TAG, "getCurrentUserPhoneId : " + phoneId);
        return phoneId;
    }

    private void registerNetworkCallback() {
        Log.d(LOG_TAG, " MtkSipTransport registerNetworkCallback inside");
        NetworkRequest cellularRequest = new NetworkRequest.Builder().addTransportType(0).addCapability(4).build();
        Log.d(LOG_TAG, "cellularRequest: " + cellularRequest);
        this.cellularCallback = new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                Log.d(MtkSipTransport.LOG_TAG, "cellularCallback onAvailable is called: " + network);
                super.onAvailable(network);
                Network unused = MtkSipTransport.this.mNetworkObj = network;
                MtkSipTransport mtkSipTransport = MtkSipTransport.this;
                LinkProperties unused2 = mtkSipTransport.mLinkProp = mtkSipTransport.f41cm.getLinkProperties(MtkSipTransport.this.mNetworkObj);
                Log.d(MtkSipTransport.LOG_TAG, "cellularCallback onAvailable mLinkProp: " + MtkSipTransport.this.mLinkProp);
                MtkSipTransport mtkSipTransport2 = MtkSipTransport.this;
                String unused3 = mtkSipTransport2.ipv6localAddress = mtkSipTransport2.getIpv6Address(mtkSipTransport2.mLinkProp.getAddresses());
                Log.d(MtkSipTransport.LOG_TAG, "cellularCallback onAvailable ipv6localAddress: " + MtkSipTransport.this.ipv6localAddress);
                MtkSipTransport mtkSipTransport3 = MtkSipTransport.this;
                String unused4 = mtkSipTransport3.ipv4localAddress = mtkSipTransport3.getIpv4Address(mtkSipTransport3.mLinkProp.getAddresses());
                Log.d(MtkSipTransport.LOG_TAG, "cellularCallback onAvailable ipv4localAddress: " + MtkSipTransport.this.ipv4localAddress);
            }

            public void onLost(Network network) {
                super.onLost(network);
                Log.d(MtkSipTransport.LOG_TAG, "cellularCallback onLost is called: " + network);
            }
        };
        Log.d(LOG_TAG, " MtkSipTransport registerNetworkCallback inside cellularCallback: " + this.cellularCallback);
        NetworkRequest wifiRequest = new NetworkRequest.Builder().addTransportType(1).addCapability(4).build();
        Log.d(LOG_TAG, "wifiRequest: " + wifiRequest);
        this.wifiCallback = new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                Log.d(MtkSipTransport.LOG_TAG, "wifiCallback onAvailable is called: " + network);
                super.onAvailable(network);
                Network unused = MtkSipTransport.this.mNetworkObj = network;
                MtkSipTransport mtkSipTransport = MtkSipTransport.this;
                LinkProperties unused2 = mtkSipTransport.mLinkProp = mtkSipTransport.f41cm.getLinkProperties(MtkSipTransport.this.mNetworkObj);
                Log.d(MtkSipTransport.LOG_TAG, "wifiCallback onAvailable mLinkProp: " + MtkSipTransport.this.mLinkProp);
                MtkSipTransport mtkSipTransport2 = MtkSipTransport.this;
                String unused3 = mtkSipTransport2.ipv6localAddress = mtkSipTransport2.getIpv6Address(mtkSipTransport2.mLinkProp.getAddresses());
                Log.d(MtkSipTransport.LOG_TAG, "wifiCallback onAvailable ipv6localAddress: " + MtkSipTransport.this.ipv6localAddress);
                MtkSipTransport mtkSipTransport3 = MtkSipTransport.this;
                String unused4 = mtkSipTransport3.ipv4localAddress = mtkSipTransport3.getIpv4Address(mtkSipTransport3.mLinkProp.getAddresses());
                Log.d(MtkSipTransport.LOG_TAG, "wifiCallback onAvailable ipv4localAddress: " + MtkSipTransport.this.ipv4localAddress);
            }

            public void onLost(Network network) {
                super.onLost(network);
                Log.d(MtkSipTransport.LOG_TAG, "wifiCallback onLost is called: " + network);
            }
        };
        Log.d(LOG_TAG, " MtkSipTransport registerNetworkCallback inside wifiCallback: " + this.wifiCallback);
        this.f41cm.registerNetworkCallback(cellularRequest, this.cellularCallback);
        this.f41cm.registerNetworkCallback(wifiRequest, this.wifiCallback);
    }

    /* access modifiers changed from: private */
    public String getIpv6Address(List<InetAddress> addresses) {
        Log.d(LOG_TAG, "getIpv6Address addresses: " + addresses);
        for (InetAddress addr : addresses) {
            if ((addr instanceof Inet6Address) && !addr.isLinkLocalAddress()) {
                Log.d(LOG_TAG, "getIpv6Address addr.getHostAddress() " + addr.getHostAddress());
                Log.d(LOG_TAG, "getIpv6Address addr.getHostName() " + addr.getHostName());
                return "[" + addr.getHostName() + "]";
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public String getIpv4Address(List<InetAddress> addresses) {
        Log.d(LOG_TAG, "getIpv4Address addresses: " + addresses);
        for (InetAddress addr : addresses) {
            if (addr instanceof Inet4Address) {
                Log.d(LOG_TAG, "getIpv4Address addr.getHostAddress() " + addr.getHostAddress());
                Log.d(LOG_TAG, "getIpv4Address addr.getHostName() " + addr.getHostName());
                return addr.getHostName();
            }
        }
        return null;
    }
}
