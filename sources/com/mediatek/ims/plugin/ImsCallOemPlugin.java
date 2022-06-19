package com.mediatek.ims.plugin;

public interface ImsCallOemPlugin {
    boolean alwaysSetPreviewSurface();

    String getVTUsageAction();

    String getVTUsagePermission();

    boolean isUpdateViwifiFeatureValueAsViLTE();

    boolean needHangupOtherCallWhenEccDialing();

    boolean needNotifyBadBitRate();

    boolean needReportCallTerminatedForFdn();

    boolean needTurnOffVolteAfterE911();

    boolean needTurnOnVolteBeforeE911();

    boolean useNormalDialForEmergencyCall();
}
