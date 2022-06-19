package com.mediatek.wfo.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.SocketKeepalive;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import androidx.core.p003os.EnvironmentCompat;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;

public class PacketKeepAliveProcessor {
    private static final int NATT_PORT = 4500;
    static final String PROPERTY_ENABLE_OFFLOAD = "persist.vendor.mtk_enable_keep_alive_offload";
    static final String TAG = "PacketKeepAliveProcessor";
    ConnectivityManager mConnectivityManager;
    Context mContext;
    ArrayList<KeepAliveInfo> mKeepAlives = new ArrayList<>();
    Handler mWifiPdnHandler;

    public class KeepAliveConfig {
        InetAddress dstIp;
        int dstPort;
        boolean enable;
        int interval;
        InetAddress srcIp;
        int srcPort;

        public KeepAliveConfig() {
            Rlog.d(PacketKeepAliveProcessor.TAG, "KeepAliveConfig default ctor");
            this.enable = false;
            this.interval = 0;
            this.srcIp = null;
            this.srcPort = 0;
            this.dstIp = null;
            this.dstPort = 0;
        }

        public String getSrcIp() {
            return this.srcIp.getHostAddress();
        }

        public int getSrcPort() {
            return this.srcPort;
        }

        public String getDstIp() {
            return this.dstIp.getHostAddress();
        }

        public int getDstPort() {
            return this.dstPort;
        }

        public KeepAliveConfig(boolean enable2, int interval2, String srcIp2, int srcPort2, String dstIp2, int dstPort2) throws UnknownHostException {
            this.enable = enable2;
            this.interval = interval2;
            this.srcIp = InetAddress.getByName(srcIp2);
            this.srcPort = srcPort2;
            this.dstIp = InetAddress.getByName(dstIp2);
            this.dstPort = dstPort2;
        }

        public boolean isEnabled() {
            return this.enable;
        }

        public boolean isEnabledAndAvailable() {
            return this.enable && this.interval >= 20 && this.dstPort == PacketKeepAliveProcessor.NATT_PORT;
        }

        public String toString() {
            return "enable: " + this.enable + " interval: " + this.interval + " srcIp: " + PacketKeepAliveProcessor.this.maskString(this.srcIp.getHostAddress()) + " srcPort: " + this.srcPort + " dstIp: " + PacketKeepAliveProcessor.this.maskString(this.dstIp.getHostAddress()) + " dstPort: " + this.dstPort;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            KeepAliveConfig kac = (KeepAliveConfig) o;
            if (this.interval == kac.interval && this.srcIp.equals(kac.srcIp) && this.srcPort == kac.srcPort && this.dstIp.equals(kac.dstIp) && this.dstPort == kac.dstPort) {
                return true;
            }
            return false;
        }
    }

    class WfcKeepAliveCallback extends SocketKeepalive.Callback {
        KeepAliveConfig config;
        boolean mIsError = false;
        boolean mIsStarted = false;

        public WfcKeepAliveCallback(KeepAliveConfig config2) {
            this.config = config2;
        }

        public boolean isStarted() {
            return this.mIsStarted;
        }

        public boolean isError() {
            return this.mIsError;
        }

        public void onStarted() {
            Rlog.d(PacketKeepAliveProcessor.TAG, "keepAlive is started!");
            this.mIsStarted = true;
            PacketKeepAliveProcessor.this.mWifiPdnHandler.sendMessage(PacketKeepAliveProcessor.this.mWifiPdnHandler.obtainMessage(1007, 0, 0, this.config));
        }

        public void onStopped() {
            Rlog.d(PacketKeepAliveProcessor.TAG, "keepAlive is stopped!");
            this.mIsStarted = false;
            this.mIsError = false;
            this.config.enable = false;
            PacketKeepAliveProcessor.this.mWifiPdnHandler.sendMessage(PacketKeepAliveProcessor.this.mWifiPdnHandler.obtainMessage(1007, 0, 0, this.config));
        }

        public void onError(int error) {
            Rlog.d(PacketKeepAliveProcessor.TAG, "keepAlive is failed, error: " + error + ", " + errorToString(error));
            this.mIsError = true;
        }

        private String errorToString(int error) {
            switch (error) {
                case -31:
                    return "ERROR_HARDWARE_ERROR";
                case -30:
                    return "ERROR_HARDWARE_UNSUPPORTED";
                case -24:
                    return "ERROR_INVALID_INTERVAL";
                case -23:
                    return "ERROR_INVALID_LENGTH";
                case -22:
                    return "ERROR_INVALID_PORT";
                case -21:
                    return "ERROR_INVALID_IP_ADDRESS";
                case -20:
                    return "ERROR_INVALID_NETWORK";
                default:
                    return EnvironmentCompat.MEDIA_UNKNOWN;
            }
        }
    }

    public class KeepAliveInfo {
        WfcKeepAliveCallback callback;
        KeepAliveConfig config;

        /* renamed from: ka */
        SocketKeepalive f44ka;

        public KeepAliveInfo(SocketKeepalive pka, KeepAliveConfig c, WfcKeepAliveCallback callback2) {
            this.f44ka = pka;
            this.config = c;
            this.callback = callback2;
        }

        public KeepAliveConfig getConfig() {
            return this.config;
        }

        public void stop() {
            Rlog.d(PacketKeepAliveProcessor.TAG, "KeepAliveInfo.stop, config: " + this.config + ", isStarted: " + this.callback.isStarted() + ", isError: " + this.callback.isError());
            if (this.callback.isStarted() || this.callback.isError()) {
                this.f44ka.stop();
            }
        }
    }

    public PacketKeepAliveProcessor(ConnectivityManager cm, Handler handler, Context context) {
        this.mConnectivityManager = cm;
        this.mWifiPdnHandler = handler;
        this.mContext = context;
    }

    public void notifyWifiDisconnect() {
        stopAllPacketKeepAlive();
    }

    public void handleKeepAliveChanged(String[] result) {
        if (!isWifiOffloadEnabledFromSystemProperty()) {
            Rlog.d(TAG, "mtk_enable_keep_alive_offload is disabled by System Property.");
            return;
        }
        Rlog.d(TAG, "handleKeepAliveChanged");
        KeepAliveConfig config = parseKeepAliveConfig(result);
        if (config == null) {
            Rlog.e(TAG, "KeepAliveConfig is null, return directly");
        } else if (config.isEnabledAndAvailable()) {
            startPacketKeepAlive(config);
        } else {
            stopPacketKeepAlive(config);
        }
    }

    private boolean isWifiOffloadEnabledFromSystemProperty() {
        return "1".equals(SystemProperties.get(PROPERTY_ENABLE_OFFLOAD, "1"));
    }

    private String getStringFromArray(String[] result) {
        if (result == null) {
            return "null";
        }
        String ret = "";
        for (int i = 0; i < result.length; i++) {
            ret = ret + result[i] + " ";
        }
        return ret;
    }

    private KeepAliveConfig parseKeepAliveConfig(String[] result) {
        if (result == null) {
            Rlog.d(TAG, "parseKeepAliveConfig() result is null");
            return new KeepAliveConfig();
        }
        KeepAliveConfig config = null;
        try {
            config = new KeepAliveConfig(Integer.parseInt(result[0]) == 1, Integer.parseInt(result[1]), result[2], Integer.parseInt(result[3]), result[4], Integer.parseInt(result[5]));
        } catch (Exception e) {
            Rlog.e(TAG, "parseKeepAliveConfig() exception: " + e.toString());
        }
        Rlog.d(TAG, "parseKeepAliveConfig() config: " + config);
        return config;
    }

    /* access modifiers changed from: package-private */
    public void startPacketKeepAlive(KeepAliveConfig config) {
        if (this.mConnectivityManager == null) {
            Rlog.e(TAG, "ConnectivityManager is null");
        } else if (this.mKeepAlives.size() >= 3) {
            Rlog.e(TAG, "startNattKeepalive cannot start! Due to mKeepAlives.size() >=3, size: " + this.mKeepAlives.size());
        } else {
            WfcKeepAliveCallback callback = new WfcKeepAliveCallback(config);
            Network network = this.mConnectivityManager.getActiveNetwork();
            if (isActiveWifiNetwork(network)) {
                try {
                    Socket socket = new Socket();
                    socket.bind(new InetSocketAddress(config.srcIp, config.srcPort));
                    ParcelFileDescriptor fd = ParcelFileDescriptor.fromSocket(socket);
                    socket.close();
                    SocketKeepalive ka = this.mConnectivityManager.createNattKeepalive(network, fd, config.srcIp, config.dstIp, Executors.newSingleThreadExecutor(), callback);
                    ka.start(config.interval);
                    this.mKeepAlives.add(new KeepAliveInfo(ka, config, callback));
                } catch (IOException e) {
                    Rlog.e(TAG, "startPacketKeepAlive exception: " + e.toString());
                }
            } else {
                Rlog.e(TAG, "It's not a active wifi network, network: " + network);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void stopPacketKeepAlive(KeepAliveConfig config) {
        Rlog.d(TAG, "stopPacketKeepAlive");
        if (this.mConnectivityManager == null) {
            Rlog.e(TAG, "ConnectivityManager is null");
            return;
        }
        KeepAliveInfo foundKai = findConfigInList(config);
        if (foundKai != null) {
            stopAndRemoveKeepAlive(foundKai);
            return;
        }
        config.enable = false;
        Handler handler = this.mWifiPdnHandler;
        handler.sendMessage(handler.obtainMessage(1007, 0, 0, config));
    }

    private boolean isActiveWifiNetwork(Network network) {
        if (network == null || this.mConnectivityManager.getNetworkInfo(network) == null || this.mConnectivityManager.getNetworkInfo(network).getType() != 1) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void stopAllPacketKeepAlive() {
        Rlog.d(TAG, "stopAllPacketKeepAlive");
        Iterator<KeepAliveInfo> iter = this.mKeepAlives.iterator();
        while (iter.hasNext()) {
            iter.next().stop();
            iter.remove();
        }
    }

    /* access modifiers changed from: package-private */
    public KeepAliveInfo findConfigInList(KeepAliveConfig config) {
        Iterator<KeepAliveInfo> it = this.mKeepAlives.iterator();
        while (it.hasNext()) {
            KeepAliveInfo kai = it.next();
            if (kai.getConfig().equals(config)) {
                Rlog.d(TAG, "findConfigInList found config: " + config);
                return kai;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void stopAndRemoveKeepAlive(KeepAliveInfo kai) {
        kai.stop();
        this.mKeepAlives.remove(kai);
    }

    /* access modifiers changed from: private */
    public String maskString(String s) {
        StringBuilder sb = new StringBuilder();
        if (TextUtils.isEmpty(s)) {
            return s;
        }
        int maskLength = s.length() / 2;
        if (maskLength < 1) {
            sb.append("*");
            return sb.toString();
        }
        for (int i = 0; i < maskLength; i++) {
            sb.append("*");
        }
        return sb.toString() + s.substring(maskLength);
    }
}
