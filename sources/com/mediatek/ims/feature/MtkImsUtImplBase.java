package com.mediatek.ims.feature;

import android.annotation.SystemApi;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import com.android.ims.internal.IImsUt;
import com.mediatek.ims.internal.IMtkImsUt;
import com.mediatek.ims.internal.IMtkImsUtListener;

@SystemApi
public class MtkImsUtImplBase {
    private IMtkImsUt.Stub mServiceImpl = new IMtkImsUt.Stub() {
        public void setListener(IMtkImsUtListener listener) throws RemoteException {
            MtkImsUtImplBase.this.setListener(new MtkImsUtListener(listener));
        }

        public IImsUt getUtInterface(int PhoneId) throws RemoteException {
            return MtkImsUtImplBase.this.getUtInterface(PhoneId);
        }

        public String getUtIMPUFromNetwork() {
            return MtkImsUtImplBase.this.getUtIMPUFromNetwork();
        }

        public void setupXcapUserAgentString(String userAgent) {
            MtkImsUtImplBase.this.setupXcapUserAgentString(userAgent);
        }

        public int queryCallForwardInTimeSlot(int condition) {
            return MtkImsUtImplBase.this.queryCallForwardInTimeSlot(condition);
        }

        public int updateCallForwardInTimeSlot(int action, int condition, String number, int timeSeconds, long[] timeSlot) {
            return MtkImsUtImplBase.this.updateCallForwardInTimeSlot(action, condition, number, timeSeconds, timeSlot);
        }

        public int updateCallBarringForServiceClass(String password, int cbType, int enable, String[] barrList, int serviceClass) {
            return MtkImsUtImplBase.this.updateCallBarringForServiceClass(password, cbType, enable, barrList, serviceClass);
        }

        public void processECT(Message result, Messenger target) {
            MtkImsUtImplBase.this.explicitCallTransfer(result, target);
        }

        public boolean isSupportCFT() {
            return MtkImsUtImplBase.this.isSupportCFT();
        }

        public String getXcapConflictErrorMessage() {
            return MtkImsUtImplBase.this.getXcapConflictErrorMessage();
        }

        public int queryCFForServiceClass(int condition, String number, int serviceClass) {
            return MtkImsUtImplBase.this.queryCFForServiceClass(condition, number, serviceClass);
        }
    };

    public int transact(Bundle ssInfo) {
        return -1;
    }

    public int queryCallForwardInTimeSlot(int condition) {
        return -1;
    }

    public int updateCallForwardInTimeSlot(int action, int condition, String number, int timeSeconds, long[] timeSlot) {
        return -1;
    }

    public int updateCallBarringForServiceClass(String password, int cbType, int enable, String[] barrList, int serviceClass) {
        return -1;
    }

    public void explicitCallTransfer(Message result, Messenger target) {
    }

    public boolean isSupportCFT() {
        return false;
    }

    public String getXcapConflictErrorMessage() {
        return "";
    }

    public void setListener(MtkImsUtListener listener) {
    }

    public String getUtIMPUFromNetwork() {
        return "";
    }

    public void setupXcapUserAgentString(String userAgent) {
    }

    public int queryCFForServiceClass(int condition, String number, int serviceClass) {
        return -1;
    }

    public IImsUt getUtInterface(int PhoneId) {
        return null;
    }

    public IMtkImsUt getInterface() {
        return this.mServiceImpl;
    }
}
