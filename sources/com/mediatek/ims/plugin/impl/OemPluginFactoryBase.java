package com.mediatek.ims.plugin.impl;

import android.content.Context;
import com.mediatek.ims.plugin.ImsCallOemPlugin;
import com.mediatek.ims.plugin.ImsManagerOemPlugin;
import com.mediatek.ims.plugin.ImsRegistrationOemPlugin;
import com.mediatek.ims.plugin.ImsSSOemPlugin;
import com.mediatek.ims.plugin.OemPluginFactory;

public class OemPluginFactoryBase implements OemPluginFactory {
    public ImsManagerOemPlugin makeImsManagerPlugin(Context context) {
        return new ImsManagerOemPluginBase();
    }

    public ImsRegistrationOemPlugin makeImsRegistrationPlugin(Context context) {
        return new ImsRegistrationOemPluginBase();
    }

    public ImsSSOemPlugin makeImsSSOemPlugin(Context context) {
        return new ImsSSOemPluginBase(context);
    }

    public ImsCallOemPlugin makeImsCallPlugin(Context context) {
        return new ImsCallOemPluginBase(context);
    }
}
