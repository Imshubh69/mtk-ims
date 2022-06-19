package com.mediatek.ims;

import android.annotation.SystemApi;
import android.telephony.Rlog;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public class MtkImsRegistrationImpl extends ImsRegistrationImplBase {
    private static final int DELAY_IMS_SERVICE_IMPL_QUERY_MS = 5000;
    private static final String LOG_TAG = "MtkImsRegImpl";
    private static final int MAXMUIM_IMS_SERVICE_IMPL_RETRY = 3;
    public static final int REGISTRATION_STATE_DEREGISTERED = 3;
    public static final int REGISTRATION_STATE_REGISTERED = 2;
    public static final int REGISTRATION_STATE_REGISTERING = 1;
    public static final int REGISTRATION_STATE_UNKNOWN = 0;
    private ImsService mImsServiceImpl = null;
    private int mSlotId = -1;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ImsRegistrationState {
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x004f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MtkImsRegistrationImpl(int r6) {
        /*
            r5 = this;
            r5.<init>()
            r0 = -1
            r5.mSlotId = r0
            r0 = 0
            r5.mImsServiceImpl = r0
            r5.mSlotId = r6
            r1 = 0
        L_0x000c:
            com.mediatek.ims.ImsService r2 = r5.mImsServiceImpl
            if (r2 != 0) goto L_0x004d
            r3 = 3
            if (r1 >= r3) goto L_0x004d
            com.mediatek.ims.ImsService r2 = com.mediatek.ims.ImsService.getInstance(r0)
            r5.mImsServiceImpl = r2
            if (r2 != 0) goto L_0x004c
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ InterruptedException -> 0x0037 }
            r2.<init>()     // Catch:{ InterruptedException -> 0x0037 }
            java.lang.String r3 = "ImsService is not initialized yet. Query later - "
            r2.append(r3)     // Catch:{ InterruptedException -> 0x0037 }
            r2.append(r1)     // Catch:{ InterruptedException -> 0x0037 }
            java.lang.String r2 = r2.toString()     // Catch:{ InterruptedException -> 0x0037 }
            log(r2)     // Catch:{ InterruptedException -> 0x0037 }
            r2 = 5000(0x1388, double:2.4703E-320)
            java.lang.Thread.sleep(r2)     // Catch:{ InterruptedException -> 0x0037 }
            int r1 = r1 + 1
            goto L_0x004c
        L_0x0037:
            r2 = move-exception
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "Fail to get ImsService "
            r3.append(r4)
            r3.append(r2)
            java.lang.String r3 = r3.toString()
            loge(r3)
        L_0x004c:
            goto L_0x000c
        L_0x004d:
            if (r2 == 0) goto L_0x0054
            int r0 = r5.mSlotId
            r2.setImsRegistration(r0, r5)
        L_0x0054:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "["
            r0.append(r2)
            int r2 = r5.mSlotId
            r0.append(r2)
            java.lang.String r2 = "] MtkImsRegistrationImpl created"
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            log(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.MtkImsRegistrationImpl.<init>(int):void");
    }

    public void close() {
        ImsService imsService = this.mImsServiceImpl;
        if (imsService != null) {
            imsService.setImsRegistration(this.mSlotId, (MtkImsRegistrationImpl) null);
        }
        logi("[" + this.mSlotId + "] MtkImsRegistrationImpl closed");
    }

    private static void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void logi(String msg) {
        Rlog.i(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
