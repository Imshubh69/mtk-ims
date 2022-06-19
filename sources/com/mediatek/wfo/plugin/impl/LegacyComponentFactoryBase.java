package com.mediatek.wfo.plugin.impl;

import android.content.Context;
import com.mediatek.wfo.IWifiOffloadService;
import com.mediatek.wfo.plugin.LegacyComponentFactory;

public class LegacyComponentFactoryBase implements LegacyComponentFactory {
    public IWifiOffloadService.Stub makeWifiOffloadService(Context context) {
        return null;
    }
}
