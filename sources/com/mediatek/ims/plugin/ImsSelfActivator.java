package com.mediatek.ims.plugin;

import android.telephony.ims.ImsCallProfile;

public interface ImsSelfActivator {
    void close();

    void doSelfActivationDial(String str, ImsCallProfile imsCallProfile, boolean z);

    boolean shouldProcessSelfActivation(int i);
}
