package com.mediatek.ims.config.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.ImsManager;
import com.mediatek.ims.ImsCommonUtil;
import com.mediatek.ims.common.ImsCarrierConfigConstants;
import com.mediatek.ims.plugin.ExtensionFactory;
import com.mediatek.ims.plugin.ImsManagerOemPlugin;
import com.mediatek.ims.ril.ImsCommandsInterface;

public class ImsConfigReceiver extends BroadcastReceiver {
    private static final String ACTION_CXP_NOTIFY_FEATURE = "com.mediatek.common.carrierexpress.cxp_notify_feature";
    private static final boolean DEBUG;
    private static final String PROPERTY_MTK_RCS_UA_SUPPORT = "persist.vendor.mtk_rcs_ua_support";
    private static final String PROPERTY_MTK_VILTE_SUPPORT = "persist.vendor.vilte_support";
    private static final String PROPERTY_MTK_VIWIFI_SUPPORT = "persist.vendor.viwifi_support";
    private static final String PROPERTY_MTK_VOLTE_SUPPORT = "persist.vendor.volte_support";
    private static final String PROPERTY_MTK_WFC_SUPPORT = "persist.vendor.mtk_wfc_support";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final String TAG = "ImsConfigReceiver";
    private Handler mHandler;
    private ImsManagerOemPlugin mImsManagerOemPlugin = null;
    private int mMainPhoneId = -1;
    private final int mPhoneId;
    private ImsCommandsInterface mRilAdapter;

    static {
        boolean z = false;
        if (TextUtils.equals(Build.TYPE, "eng") || SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        DEBUG = z;
    }

    public ImsConfigReceiver(Handler handler, int phoneId, ImsCommandsInterface imsRilAdapter) {
        this.mPhoneId = phoneId;
        this.mHandler = handler;
        this.mRilAdapter = imsRilAdapter;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0168, code lost:
        if (r5.equals("LOCKED") != false) goto L_0x0174;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onReceive(android.content.Context r17, android.content.Intent r18) {
        /*
            r16 = this;
            r0 = r16
            r1 = r17
            r2 = r18
            r3 = -1
            java.lang.String r4 = r18.getAction()
            int r5 = r4.hashCode()
            r7 = 4
            r8 = 1
            r9 = 3
            r10 = 2
            r11 = -1
            switch(r5) {
                case -1917430950: goto L_0x0040;
                case -1138588223: goto L_0x0036;
                case -445172446: goto L_0x002c;
                case -229777127: goto L_0x0022;
                case 1030265252: goto L_0x0018;
                default: goto L_0x0017;
            }
        L_0x0017:
            goto L_0x004a
        L_0x0018:
            java.lang.String r5 = "android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE"
            boolean r4 = r4.equals(r5)
            if (r4 == 0) goto L_0x0017
            r4 = r9
            goto L_0x004b
        L_0x0022:
            java.lang.String r5 = "android.intent.action.SIM_STATE_CHANGED"
            boolean r4 = r4.equals(r5)
            if (r4 == 0) goto L_0x0017
            r4 = 0
            goto L_0x004b
        L_0x002c:
            java.lang.String r5 = "com.mediatek.common.carrierexpress.cxp_notify_feature"
            boolean r4 = r4.equals(r5)
            if (r4 == 0) goto L_0x0017
            r4 = r7
            goto L_0x004b
        L_0x0036:
            java.lang.String r5 = "android.telephony.action.CARRIER_CONFIG_CHANGED"
            boolean r4 = r4.equals(r5)
            if (r4 == 0) goto L_0x0017
            r4 = r8
            goto L_0x004b
        L_0x0040:
            java.lang.String r5 = "com.mediatek.ims.MTK_MMTEL_READY"
            boolean r4 = r4.equals(r5)
            if (r4 == 0) goto L_0x0017
            r4 = r10
            goto L_0x004b
        L_0x004a:
            r4 = r11
        L_0x004b:
            r5 = 9
            java.lang.String r12 = "ImsConfigReceiver"
            switch(r4) {
                case 0: goto L_0x00ed;
                case 1: goto L_0x00e8;
                case 2: goto L_0x00b0;
                case 3: goto L_0x0062;
                case 4: goto L_0x0054;
                default: goto L_0x0052;
            }
        L_0x0052:
            goto L_0x0207
        L_0x0054:
            android.os.Bundle r4 = r18.getExtras()
            if (r4 == 0) goto L_0x0207
            r0.updateFeatureSupportProperty(r4)
            r0.updateImsServiceConfig(r1, r3)
            goto L_0x0207
        L_0x0062:
            r4 = 1
            int r6 = r0.mMainPhoneId
            if (r6 != r11) goto L_0x006f
            int r6 = com.mediatek.ims.ImsCommonUtil.getMainCapabilityPhoneId()
            r0.mMainPhoneId = r6
            r4 = 0
            goto L_0x0080
        L_0x006f:
            int r3 = com.mediatek.ims.ImsCommonUtil.getMainCapabilityPhoneId()
            int r6 = r0.mMainPhoneId
            if (r3 != r6) goto L_0x0079
            r4 = 0
            goto L_0x0080
        L_0x0079:
            r0.mMainPhoneId = r3
            int r6 = r0.mPhoneId
            if (r6 == r3) goto L_0x0080
            r4 = 0
        L_0x0080:
            if (r4 == 0) goto L_0x0207
            java.lang.String r6 = "ACTION_SET_RADIO_CAPABILITY_DONE"
            r0.resetWfcModeFlag(r6)
            android.os.Handler r6 = r0.mHandler
            r6.removeMessages(r5)
            android.os.Message r6 = new android.os.Message
            r6.<init>()
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "SET_RADIO_CAPABILITY_DONE, update IMS config with phoneId:"
            r7.append(r8)
            int r8 = r0.mPhoneId
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            android.util.Log.d(r12, r7)
            r6.what = r5
            android.os.Handler r5 = r0.mHandler
            r5.sendMessage(r6)
            goto L_0x0207
        L_0x00b0:
            java.lang.String r4 = "android:phone_id"
            int r3 = r2.getIntExtra(r4, r11)
            int r4 = r0.mPhoneId
            if (r3 != r4) goto L_0x0207
            java.lang.String r4 = "ACTION_MTK_MMTEL_READY"
            r0.resetWfcModeFlag(r4)
            android.os.Handler r4 = r0.mHandler
            r4.removeMessages(r5)
            android.os.Message r4 = new android.os.Message
            r4.<init>()
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "ACTION_MTK_MMTEL_READY, update IMS config with phoneId:"
            r6.append(r7)
            int r7 = r0.mPhoneId
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            android.util.Log.d(r12, r6)
            r4.what = r5
            android.os.Handler r5 = r0.mHandler
            r5.sendMessage(r4)
            goto L_0x0207
        L_0x00e8:
            r16.handleCarrierConfigChanged(r17, r18)
            goto L_0x0207
        L_0x00ed:
            java.lang.String r4 = "ss"
            java.lang.String r5 = r2.getStringExtra(r4)
            java.lang.String r13 = "phone"
            int r3 = r2.getIntExtra(r13, r11)
            int r14 = r0.mPhoneId
            if (r3 != r14) goto L_0x0207
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            java.lang.String r15 = "Update LatestSimState, phoneId = "
            r14.append(r15)
            int r15 = r0.mPhoneId
            r14.append(r15)
            java.lang.String r15 = ", state = "
            r14.append(r15)
            r14.append(r5)
            java.lang.String r14 = r14.toString()
            android.util.Log.d(r12, r14)
            java.util.HashMap<java.lang.Integer, java.lang.String> r14 = com.mediatek.ims.config.internal.ImsConfigProvider.LatestSimState
            int r15 = r0.mPhoneId
            java.lang.Integer r15 = java.lang.Integer.valueOf(r15)
            r14.put(r15, r5)
            int r14 = r5.hashCode()
            java.lang.String r15 = "ABSENT"
            java.lang.String r6 = "LOADED"
            switch(r14) {
                case -2044189691: goto L_0x016b;
                case -2044123382: goto L_0x0162;
                case -1830845986: goto L_0x0158;
                case 77848963: goto L_0x014e;
                case 433141802: goto L_0x0144;
                case 1034051831: goto L_0x013a;
                case 1924388665: goto L_0x0132;
                default: goto L_0x0131;
            }
        L_0x0131:
            goto L_0x0173
        L_0x0132:
            boolean r7 = r5.equals(r15)
            if (r7 == 0) goto L_0x0131
            r7 = 0
            goto L_0x0174
        L_0x013a:
            java.lang.String r7 = "NOT_READY"
            boolean r7 = r5.equals(r7)
            if (r7 == 0) goto L_0x0131
            r7 = r8
            goto L_0x0174
        L_0x0144:
            java.lang.String r7 = "UNKNOWN"
            boolean r7 = r5.equals(r7)
            if (r7 == 0) goto L_0x0131
            r7 = r10
            goto L_0x0174
        L_0x014e:
            java.lang.String r7 = "READY"
            boolean r7 = r5.equals(r7)
            if (r7 == 0) goto L_0x0131
            r7 = 6
            goto L_0x0174
        L_0x0158:
            java.lang.String r7 = "CARD_IO_ERROR"
            boolean r7 = r5.equals(r7)
            if (r7 == 0) goto L_0x0131
            r7 = r9
            goto L_0x0174
        L_0x0162:
            java.lang.String r14 = "LOCKED"
            boolean r14 = r5.equals(r14)
            if (r14 == 0) goto L_0x0131
            goto L_0x0174
        L_0x016b:
            boolean r7 = r5.equals(r6)
            if (r7 == 0) goto L_0x0131
            r7 = 5
            goto L_0x0174
        L_0x0173:
            r7 = r11
        L_0x0174:
            java.lang.String r11 = "Sim state changed, event = "
            switch(r7) {
                case 0: goto L_0x0196;
                case 1: goto L_0x01be;
                case 2: goto L_0x01be;
                case 3: goto L_0x01be;
                case 4: goto L_0x0195;
                case 5: goto L_0x0195;
                case 6: goto L_0x017a;
                default: goto L_0x0179;
            }
        L_0x0179:
            goto L_0x01e7
        L_0x017a:
            int r7 = r0.mPhoneId
            boolean r7 = com.mediatek.ims.config.internal.ImsConfigUtils.isWfcEnabledByUser(r1, r7)
            if (r7 == 0) goto L_0x01e7
            int r7 = r0.mPhoneId
            int r7 = com.mediatek.ims.config.internal.ImsConfigUtils.getWfcMode(r1, r7)
            if (r7 != 0) goto L_0x01e7
            int r7 = r0.mPhoneId
            com.mediatek.ims.config.internal.ImsConfigUtils.sendWifiOnlyModeIntent(r1, r7, r8)
            java.lang.String r7 = "Turn OFF radio, after sim ready and wfc mode is wifi_only"
            android.util.Log.d(r12, r7)
            goto L_0x01e7
        L_0x0195:
            goto L_0x01e7
        L_0x0196:
            android.os.Handler r7 = r0.mHandler
            r7.removeMessages(r9)
            android.os.Message r7 = new android.os.Message
            r7.<init>()
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r11)
            r8.append(r5)
            java.lang.String r14 = ", check for ECC flag"
            r8.append(r14)
            java.lang.String r8 = r8.toString()
            android.util.Log.d(r12, r8)
            r7.what = r9
            android.os.Handler r8 = r0.mHandler
            r8.sendMessage(r7)
        L_0x01be:
            android.os.Handler r7 = r0.mHandler
            r7.removeMessages(r10)
            android.os.Message r7 = new android.os.Message
            r7.<init>()
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r11)
            r8.append(r5)
            java.lang.String r9 = ", reset broadcast flag"
            r8.append(r9)
            java.lang.String r8 = r8.toString()
            android.util.Log.d(r12, r8)
            r7.what = r10
            android.os.Handler r8 = r0.mHandler
            r8.sendMessage(r7)
        L_0x01e7:
            boolean r7 = r5.equals(r15)
            if (r7 != 0) goto L_0x01f3
            boolean r6 = r5.equals(r6)
            if (r6 == 0) goto L_0x0207
        L_0x01f3:
            android.content.Intent r6 = new android.content.Intent
            java.lang.String r7 = "com.mediatek.ims.config.action.DYNAMIC_IMS_SWITCH_TRIGGER"
            r6.<init>(r7)
            int r7 = r0.mPhoneId
            r6.putExtra(r13, r7)
            r6.putExtra(r4, r5)
            java.lang.String r4 = "android.permission.READ_PHONE_STATE"
            r1.sendBroadcast(r6, r4)
        L_0x0207:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.config.internal.ImsConfigReceiver.onReceive(android.content.Context, android.content.Intent):void");
    }

    private void updateFeatureSupportProperty(Bundle bundle) {
        if (bundle.getString(PROPERTY_MTK_WFC_SUPPORT, "0").equals("1")) {
            this.mRilAdapter.turnOnWfc((Message) null);
        } else {
            this.mRilAdapter.turnOffWfc((Message) null);
        }
        if (bundle.getString(PROPERTY_MTK_VOLTE_SUPPORT, "0").equals("1")) {
            this.mRilAdapter.turnOnVolte((Message) null);
        } else {
            this.mRilAdapter.turnOffVolte((Message) null);
        }
        if (bundle.getString(PROPERTY_MTK_VILTE_SUPPORT, "0").equals("1")) {
            this.mRilAdapter.turnOnVilte((Message) null);
        } else {
            this.mRilAdapter.turnOffVilte((Message) null);
        }
        if (bundle.getString(PROPERTY_MTK_VIWIFI_SUPPORT, "0").equals("1")) {
            this.mRilAdapter.turnOnViwifi((Message) null);
        } else {
            this.mRilAdapter.turnOffViwifi((Message) null);
        }
        if (bundle.getString(PROPERTY_MTK_RCS_UA_SUPPORT, "0").equals("1")) {
            this.mRilAdapter.turnOnRcsUa((Message) null);
        } else {
            this.mRilAdapter.turnOffRcsUa((Message) null);
        }
    }

    private void handleCarrierConfigChanged(Context context, Intent intent) {
        int phoneId = intent.getIntExtra("phone", -1);
        if (phoneId == this.mPhoneId) {
            TelephonyManager tm = TelephonyManager.getDefault();
            if (tm == null || tm.hasIccCard(phoneId)) {
                resetWfcModeFlag("ACTION_CARRIER_CONFIG_CHANGED");
                boolean removeWfcPrefMode = ImsConfigUtils.getBooleanCarrierConfig(context, ImsCarrierConfigConstants.MTK_KEY_WFC_REMOVE_PREFERENCE_MODE_BOOL, phoneId);
                boolean wfcModeEditable = ImsConfigUtils.getBooleanCarrierConfig(context, "editable_wfc_mode_bool", phoneId);
                Log.d(TAG, "KEY_WFC_REMOVE_PREFERENCE_MODE_BOOL = " + removeWfcPrefMode);
                Log.d(TAG, "KEY_EDITABLE_WFC_MODE_BOOL = " + wfcModeEditable);
                if (removeWfcPrefMode || !wfcModeEditable) {
                    int wfcMode = ImsConfigUtils.getIntCarrierConfig(context, "carrier_default_wfc_ims_mode_int", phoneId);
                    Log.d(TAG, "ACTION_CARRIER_CONFIG_CHANGED: set wfc mode = " + wfcMode + ", phoneId:" + phoneId);
                    if (ImsCommonUtil.supportMims() || ImsCommonUtil.getMainPhoneIdForSingleIms() == phoneId) {
                        ImsManager.getInstance(context, phoneId).setWfcMode(wfcMode);
                    } else {
                        Log.d(TAG, "no set wfc mode due to mims: " + ImsCommonUtil.supportMims() + ", main phone id:" + ImsCommonUtil.getMainPhoneIdForSingleIms());
                    }
                }
                int operatorCode = ImsConfigUtils.getIntCarrierConfig(context, ImsCarrierConfigConstants.KEY_OPERATOR_ID_INT, phoneId);
                this.mHandler.removeMessages(1);
                Message msg = new Message();
                Log.d(TAG, "carrier config changed, operatorCode = " + operatorCode + " on phone " + phoneId);
                msg.what = 1;
                msg.obj = Integer.valueOf(operatorCode);
                this.mHandler.sendMessage(msg);
                return;
            }
            Log.e(TAG, "No need to reload config storage");
        }
    }

    private void resetWfcModeFlag(String reason) {
        this.mHandler.removeMessages(8);
        Message msg = new Message();
        Log.d(TAG, "resetWfcModeFlag, reason: " + reason);
        msg.what = 8;
        this.mHandler.sendMessage(msg);
    }

    private void forceToSendWfcMode() {
        this.mHandler.removeMessages(10);
        Message msg = new Message();
        msg.what = 10;
        this.mHandler.sendMessage(msg);
    }

    private void updateImsServiceConfig(Context context, int phoneId) {
        if (this.mImsManagerOemPlugin == null) {
            this.mImsManagerOemPlugin = ExtensionFactory.makeOemPluginFactory(context).makeImsManagerPlugin(context);
        }
        if (ImsCommonUtil.supportMims() || phoneId == ImsCommonUtil.getMainCapabilityPhoneId()) {
            this.mImsManagerOemPlugin.updateImsServiceConfig(context, phoneId);
        } else if (DEBUG) {
            Log.d(TAG, "Do not update if phoneId is not main capability");
        }
    }
}
