package com.mediatek.ims.internal;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.mediatek.ims.ImsAdapter;
import com.mediatek.ims.ImsEventDispatcher;

public class CallControlDispatcher implements ImsEventDispatcher.VaEventDispatcher {
    private static final int IMC_PROGRESS_NOTIFY_CONFERENCE = 257;
    private static final int IMC_PROGRESS_NOTIFY_DIALOG = 256;
    private static final int IMC_PROGRESS_NOTIFY_MWI = 258;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final boolean SENLOG = TextUtils.equals(Build.TYPE, "user");
    private static final String TAG = "[CallControlDispatcher]";
    private static final boolean TELDBG;
    private Context mContext;
    private ImsAdapter.VaSocketIO mSocket;

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        TELDBG = z;
    }

    public CallControlDispatcher(Context context, ImsAdapter.VaSocketIO IO) {
        this.mContext = context;
        this.mSocket = IO;
    }

    public void enableRequest(int phoneId) {
        Rlog.d(TAG, "enableRequest()");
    }

    public void disableRequest(int phoneId) {
        Rlog.d(TAG, "disableRequest()");
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x00b0 A[SYNTHETIC, Splitter:B:13:0x00b0] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x00cc A[Catch:{ Exception -> 0x012b }] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x00f1 A[Catch:{ Exception -> 0x012b }] */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0116 A[Catch:{ Exception -> 0x012b }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void vaEventCallback(com.mediatek.ims.ImsAdapter.VaEvent r17) {
        /*
            r16 = this;
            r1 = r16
            java.lang.String r2 = "[CallControlDispatcher]"
            int r0 = r17.getRequestID()     // Catch:{ Exception -> 0x012b }
            int r3 = r17.getDataLen()     // Catch:{ Exception -> 0x012b }
            int r4 = r17.getInt()     // Catch:{ Exception -> 0x012b }
            int r5 = r17.getPhoneId()     // Catch:{ Exception -> 0x012b }
            int r6 = r17.getInt()     // Catch:{ Exception -> 0x012b }
            r7 = 4000(0xfa0, float:5.605E-42)
            r8 = r17
            byte[] r9 = r8.getBytes(r7)     // Catch:{ Exception -> 0x012b }
            java.lang.String r10 = new java.lang.String     // Catch:{ Exception -> 0x012b }
            r10.<init>(r9)     // Catch:{ Exception -> 0x012b }
            int r7 = r1.getDataLength(r9, r7)     // Catch:{ Exception -> 0x012b }
            r3 = r7
            boolean r7 = SENLOG     // Catch:{ Exception -> 0x012b }
            java.lang.String r11 = ", serviceId = "
            java.lang.String r12 = ", phoneId = "
            java.lang.String r13 = ", callId = "
            java.lang.String r14 = ", length = "
            java.lang.String r15 = "requestId = "
            r8 = 0
            if (r7 == 0) goto L_0x006e
            boolean r7 = TELDBG     // Catch:{ Exception -> 0x012b }
            if (r7 == 0) goto L_0x003e
            goto L_0x006e
        L_0x003e:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x012b }
            r7.<init>()     // Catch:{ Exception -> 0x012b }
            r7.append(r15)     // Catch:{ Exception -> 0x012b }
            r7.append(r0)     // Catch:{ Exception -> 0x012b }
            r7.append(r14)     // Catch:{ Exception -> 0x012b }
            r7.append(r3)     // Catch:{ Exception -> 0x012b }
            r7.append(r13)     // Catch:{ Exception -> 0x012b }
            r7.append(r4)     // Catch:{ Exception -> 0x012b }
            r7.append(r12)     // Catch:{ Exception -> 0x012b }
            r7.append(r5)     // Catch:{ Exception -> 0x012b }
            r7.append(r11)     // Catch:{ Exception -> 0x012b }
            r7.append(r6)     // Catch:{ Exception -> 0x012b }
            java.lang.String r11 = ", data = [hidden]"
            r7.append(r11)     // Catch:{ Exception -> 0x012b }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x012b }
            android.telephony.Rlog.d(r2, r7)     // Catch:{ Exception -> 0x012b }
            goto L_0x00a8
        L_0x006e:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x012b }
            r7.<init>()     // Catch:{ Exception -> 0x012b }
            r7.append(r15)     // Catch:{ Exception -> 0x012b }
            r7.append(r0)     // Catch:{ Exception -> 0x012b }
            r7.append(r14)     // Catch:{ Exception -> 0x012b }
            r7.append(r3)     // Catch:{ Exception -> 0x012b }
            r7.append(r13)     // Catch:{ Exception -> 0x012b }
            r7.append(r4)     // Catch:{ Exception -> 0x012b }
            r7.append(r12)     // Catch:{ Exception -> 0x012b }
            r7.append(r5)     // Catch:{ Exception -> 0x012b }
            r7.append(r11)     // Catch:{ Exception -> 0x012b }
            r7.append(r6)     // Catch:{ Exception -> 0x012b }
            java.lang.String r11 = ", data = "
            r7.append(r11)     // Catch:{ Exception -> 0x012b }
            java.lang.String r11 = r10.substring(r8, r3)     // Catch:{ Exception -> 0x012b }
            java.lang.String r11 = com.mediatek.ims.ImsServiceCallTracker.sensitiveEncode(r11)     // Catch:{ Exception -> 0x012b }
            r7.append(r11)     // Catch:{ Exception -> 0x012b }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x012b }
            android.telephony.Rlog.d(r2, r7)     // Catch:{ Exception -> 0x012b }
        L_0x00a8:
            java.lang.String r7 = "call.id"
            java.lang.String r11 = "phone.id"
            switch(r6) {
                case 256: goto L_0x00f1;
                case 257: goto L_0x00cc;
                case 258: goto L_0x00b0;
                default: goto L_0x00af;
            }
        L_0x00af:
            goto L_0x0116
        L_0x00b0:
            android.content.Intent r7 = new android.content.Intent     // Catch:{ Exception -> 0x012b }
            java.lang.String r8 = "mediatek.intent.action.lte.mwi"
            r7.<init>(r8)     // Catch:{ Exception -> 0x012b }
            java.lang.String r8 = "lte_mwi_body"
            r7.putExtra(r8, r10)     // Catch:{ Exception -> 0x012b }
            r7.putExtra(r11, r5)     // Catch:{ Exception -> 0x012b }
            android.content.Context r8 = r1.mContext     // Catch:{ Exception -> 0x012b }
            java.lang.String r11 = "com.mediatek.permission.READ_LTE_MESSAGE_WAITING_INDICATION"
            r8.sendBroadcast(r7, r11)     // Catch:{ Exception -> 0x012b }
            java.lang.String r8 = "Message Waiting Message is sent."
            android.telephony.Rlog.d(r2, r8)     // Catch:{ Exception -> 0x012b }
            goto L_0x012a
        L_0x00cc:
            android.content.Intent r12 = new android.content.Intent     // Catch:{ Exception -> 0x012b }
            java.lang.String r13 = "android.intent.action.ims.conference"
            r12.<init>(r13)     // Catch:{ Exception -> 0x012b }
            java.lang.String r13 = "message.content"
            java.lang.String r8 = r10.substring(r8, r3)     // Catch:{ Exception -> 0x012b }
            r12.putExtra(r13, r8)     // Catch:{ Exception -> 0x012b }
            r12.putExtra(r7, r4)     // Catch:{ Exception -> 0x012b }
            r12.putExtra(r11, r5)     // Catch:{ Exception -> 0x012b }
            android.content.Context r7 = r1.mContext     // Catch:{ Exception -> 0x012b }
            androidx.localbroadcastmanager.content.LocalBroadcastManager r7 = androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(r7)     // Catch:{ Exception -> 0x012b }
            r7.sendBroadcast(r12)     // Catch:{ Exception -> 0x012b }
            java.lang.String r7 = "Conference call XML message is sent."
            android.telephony.Rlog.d(r2, r7)     // Catch:{ Exception -> 0x012b }
            goto L_0x012a
        L_0x00f1:
            android.content.Intent r12 = new android.content.Intent     // Catch:{ Exception -> 0x012b }
            java.lang.String r13 = "com.mediatek.intent.action.ims.dialogEventPackage"
            r12.<init>(r13)     // Catch:{ Exception -> 0x012b }
            java.lang.String r13 = "com.mediatek.intent.extra.DEP_CONTENT"
            java.lang.String r8 = r10.substring(r8, r3)     // Catch:{ Exception -> 0x012b }
            r12.putExtra(r13, r8)     // Catch:{ Exception -> 0x012b }
            r12.putExtra(r7, r4)     // Catch:{ Exception -> 0x012b }
            r12.putExtra(r11, r5)     // Catch:{ Exception -> 0x012b }
            android.content.Context r7 = r1.mContext     // Catch:{ Exception -> 0x012b }
            androidx.localbroadcastmanager.content.LocalBroadcastManager r7 = androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(r7)     // Catch:{ Exception -> 0x012b }
            r7.sendBroadcast(r12)     // Catch:{ Exception -> 0x012b }
            java.lang.String r7 = "Dialog event package intent is sent."
            android.telephony.Rlog.d(r2, r7)     // Catch:{ Exception -> 0x012b }
            goto L_0x012a
        L_0x0116:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x012b }
            r7.<init>()     // Catch:{ Exception -> 0x012b }
            java.lang.String r8 = "Unkonwn serviceId: "
            r7.append(r8)     // Catch:{ Exception -> 0x012b }
            r7.append(r6)     // Catch:{ Exception -> 0x012b }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x012b }
            android.telephony.Rlog.d(r2, r7)     // Catch:{ Exception -> 0x012b }
        L_0x012a:
            goto L_0x0155
        L_0x012b:
            r0 = move-exception
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "vaEventCallback exception: "
            r3.append(r4)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = ""
            r4.append(r5)
            r4.append(r0)
            java.lang.String r4 = r4.toString()
            java.lang.String r4 = com.mediatek.ims.ImsServiceCallTracker.sensitiveEncode(r4)
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.telephony.Rlog.e(r2, r3)
        L_0x0155:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.internal.CallControlDispatcher.vaEventCallback(com.mediatek.ims.ImsAdapter$VaEvent):void");
    }

    private int getDataLength(byte[] data, int originLen) {
        int i = 0;
        while (i < originLen && data[i] != 0) {
            i++;
        }
        return i;
    }
}
