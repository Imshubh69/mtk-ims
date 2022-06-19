package com.mediatek.ims.plugin;

import android.content.Context;
import android.telephony.ims.stub.ImsUtImplBase;
import com.mediatek.ims.ImsService;
import com.mediatek.ims.feature.MtkImsUtImplBase;

public interface LegacyComponentFactory {
    ImsUtImplBase makeImsUt(Context context, int i, ImsService imsService);

    MtkImsUtImplBase makeMtkImsUt(Context context, int i, ImsService imsService);
}
