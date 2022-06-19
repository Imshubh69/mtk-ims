package com.mediatek.ims.plugin;

import android.content.Context;

public interface ImsManagerOemPlugin {
    boolean hasPlugin();

    boolean isWfcSupport();

    void updateImsServiceConfig(Context context, int i);
}
