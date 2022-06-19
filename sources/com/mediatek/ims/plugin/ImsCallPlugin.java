package com.mediatek.ims.plugin;

public interface ImsCallPlugin {
    int getMainCapabilityPhoneId();

    int getRealRequest(int i);

    int getSimApplicationState(int i);

    int getSimCardState(int i);

    int getUpgradeCancelFlag();

    int getUpgradeCancelTimeoutFlag();

    boolean isCapabilitySwitching();

    boolean isImsFwkRequest(int i);

    boolean isSupportMims();

    int setImsFwkRequest(int i);
}
