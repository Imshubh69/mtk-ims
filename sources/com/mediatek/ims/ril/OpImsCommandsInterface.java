package com.mediatek.ims.ril;

import android.os.Message;

public interface OpImsCommandsInterface {
    void cancelDeviceSwitch(Message message);

    void cancelUssiFrom(String str, Message message);

    void deviceSwitch(String str, String str2, Message message);

    void dialFrom(String str, String str2, int i, boolean z, Message message);

    void sendUssiFrom(String str, int i, String str2, Message message);
}
