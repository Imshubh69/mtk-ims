package com.mediatek.wfo.plugin;

import android.content.Context;
import com.mediatek.wfo.IWifiOffloadService;

public interface LegacyComponentFactory {
    IWifiOffloadService.Stub makeWifiOffloadService(Context context);
}
