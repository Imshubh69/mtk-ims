package com.mediatek.ims.feature;

import android.annotation.SystemApi;
import android.os.RemoteException;
import android.telephony.ims.ImsCallForwardInfo;
import android.util.Log;
import com.mediatek.ims.MtkImsCallForwardInfo;
import com.mediatek.ims.internal.IMtkImsUt;
import com.mediatek.ims.internal.IMtkImsUtListener;

@SystemApi
public class MtkImsUtListener {
    private static final String LOG_TAG = "MtkImsUtListener";
    private IMtkImsUtListener mServiceInterface;

    public void onUtConfigurationCallForwardInTimeSlotQueried(int id, MtkImsCallForwardInfo[] cfInfo) {
        try {
            this.mServiceInterface.utConfigurationCallForwardInTimeSlotQueried((IMtkImsUt) null, id, cfInfo);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "onUtConfigurationCallForwardInTimeSlotQueried: remote exception");
        }
    }

    public void onUtConfigurationCallForwardQueried(int id, ImsCallForwardInfo[] cfInfo) {
        try {
            this.mServiceInterface.utConfigurationCallForwardQueried((IMtkImsUt) null, id, cfInfo);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationCallForwardQueried: remote exception");
        }
    }

    public MtkImsUtListener(IMtkImsUtListener serviceInterface) {
        this.mServiceInterface = serviceInterface;
    }
}
