package com.mediatek.wfo.ril;

import android.os.Handler;
import android.os.Message;

public interface MwiCommandsInterface {
    void getWfcConfig(int i, Message message);

    void notifyEPDGScreenState(int i, Message message);

    void registerNattKeepAliveChanged(Handler handler, int i, Object obj);

    void registerRequestGeoLocation(Handler handler, int i, Object obj);

    void registerRssiThresholdChanged(Handler handler, int i, Object obj);

    void registerWfcPdnStateChanged(Handler handler, int i, Object obj);

    void registerWifiLock(Handler handler, int i, Object obj);

    void registerWifiPdnActivated(Handler handler, int i, Object obj);

    void registerWifiPdnError(Handler handler, int i, Object obj);

    void registerWifiPdnHandover(Handler handler, int i, Object obj);

    void registerWifiPdnOos(Handler handler, int i, Object obj);

    void registerWifiPdnRoveOut(Handler handler, int i, Object obj);

    void registerWifiPingRequest(Handler handler, int i, Object obj);

    void setEmergencyAddressId(String str, Message message);

    void setLocationInfo(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, String str10, String str11, String str12, String str13, String str14, String str15, String str16, Message message);

    void setNattKeepAliveStatus(String str, boolean z, String str2, int i, String str3, int i2, Message message);

    void setWfcConfig(int i, String str, String str2, Message message);

    void setWfcConfig_WifiUeMac(String str, String str2, Message message);

    void setWifiAssociated(String str, boolean z, String str2, String str3, int i, String str4, int i2, Message message);

    void setWifiEnabled(String str, int i, int i2, Message message);

    void setWifiIpAddress(String str, String str2, String str3, int i, int i2, String str4, String str5, int i3, String str6, Message message);

    void setWifiPingResult(int i, int i2, int i3, Message message);

    void setWifiSignalLevel(int i, int i2, Message message);

    void unregisterRequestGeoLocation(Handler handler);

    void unregisterRssiThresholdChanged(Handler handler);

    void unregisterWfcPdnStateChanged(Handler handler);

    void unregisterWifiLock(Handler handler);

    void unregisterWifiPdnActivate(Handler handler);

    void unregisterWifiPdnError(Handler handler);

    void unregisterWifiPdnHandover(Handler handler);

    void unregisterWifiPdnOos(Handler handler);

    void unregisterWifiPdnRoveOut(Handler handler);

    void unregisterWifiPingRequest(Handler handler);

    void unrgisterNattKeepAliveChanged(Handler handler);
}
