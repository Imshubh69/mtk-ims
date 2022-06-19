package com.mediatek.ims.plugin.impl;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.mediatek.ims.ImsCallSessionProxy;
import com.mediatek.ims.ImsService;
import com.mediatek.ims.plugin.ExtensionPluginFactory;
import com.mediatek.ims.ril.ImsCommandsInterface;

public class ExtensionPluginFactoryBase implements ExtensionPluginFactory {
    public ImsCallPluginBase makeImsCallPlugin(Context context) {
        Log.d("ExtensionPluginFactoryBase", "makeImsCallPlugin()");
        return new ImsCallPluginBase(context);
    }

    public ImsSelfActivatorBase makeImsSelfActivator(Context context, Handler handler, ImsCallSessionProxy callSessionProxy, ImsCommandsInterface imsRILAdapter, ImsService imsService, int phoneId) {
        Log.d("ExtensionPluginFactoryBase", "makeImsSelfActivator()");
        return new ImsSelfActivatorBase(context, handler, callSessionProxy, imsRILAdapter, imsService, phoneId);
    }

    public ImsSSExtPluginBase makeImsSSExtPlugin(Context context) {
        Log.d("ExtensionPluginFactoryBase", "makeImsSSExtPlugin()");
        return new ImsSSExtPluginBase(context);
    }
}
