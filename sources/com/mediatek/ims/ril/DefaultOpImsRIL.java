package com.mediatek.ims.ril;

import android.os.Message;

public class DefaultOpImsRIL implements OpImsCommandsInterface {
    DefaultOpImsRIL(int slotId) {
    }

    public void dialFrom(String address, String fromAddress, int clirMode, boolean isVideoCall, Message result) {
    }

    public void sendUssiFrom(String from, int action, String ussi, Message response) {
    }

    public void cancelUssiFrom(String from, Message response) {
    }

    public void deviceSwitch(String number, String deviceId, Message response) {
    }

    public void cancelDeviceSwitch(Message response) {
    }
}
