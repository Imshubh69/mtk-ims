package com.mediatek.ims.config.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.ims.ImsManager;
import com.mediatek.ims.ImsCommonUtil;
import com.mediatek.ims.common.ImsCarrierConfigConstants;
import com.mediatek.ims.plugin.ExtensionFactory;
import com.mediatek.ims.plugin.ImsManagerOemPlugin;
import com.mediatek.ims.ril.ImsCommandsInterface;

public class ImsConfigEventReceiver extends BroadcastReceiver {
    private static final String ACTION_CXP_NOTIFY_FEATURE = "com.mediatek.common.carrierexpress.cxp_notify_feature";
    private static final boolean DEBUG;
    private static final String INVALID_VALUE = "-1";
    private static final String PROPERTY_MTK_RCS_UA_SUPPORT = "persist.vendor.mtk_rcs_ua_support";
    private static final String PROPERTY_MTK_VILTE_SUPPORT = "persist.vendor.vilte_support";
    private static final String PROPERTY_MTK_VIWIFI_SUPPORT = "persist.vendor.viwifi_support";
    private static final String PROPERTY_MTK_VOLTE_SUPPORT = "persist.vendor.volte_support";
    private static final String PROPERTY_MTK_WFC_SUPPORT = "persist.vendor.mtk_wfc_support";
    private static final String PROPERTY_SUPPORT = "1";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final String TAG = "ImsConfigEventReceiver";
    private Handler mHandler;
    private ImsManagerOemPlugin mImsManagerOemPlugin;
    private boolean mIsCarrierConfigLoaded;
    private String mLogTag;
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

    public ImsConfigEventReceiver(Handler handler, int phoneId, ImsCommandsInterface imsRilAdapter) {
        boolean z = false;
        this.mIsCarrierConfigLoaded = false;
        this.mImsManagerOemPlugin = null;
        this.mPhoneId = phoneId;
        this.mHandler = handler;
        this.mRilAdapter = imsRilAdapter;
        this.mLogTag = "ImsConfigEventReceiver[" + phoneId + "]";
        int simState = SubscriptionManager.getSimStateForSlotIndex(phoneId);
        this.mIsCarrierConfigLoaded = (simState == 1 || simState == 10) ? true : z;
        String str = this.mLogTag;
        Rlog.d(str, "init with phoneId:" + phoneId + ", simState = " + simState + ", carrier config loaded: " + this.mIsCarrierConfigLoaded);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x013e, code lost:
        if (r1.equals("LOADED") != false) goto L_0x0142;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onReceive(android.content.Context r10, android.content.Intent r11) {
        /*
            r9 = this;
            r0 = -1
            java.lang.String r1 = r11.getAction()
            int r2 = r1.hashCode()
            r3 = 2
            r4 = 0
            r5 = 1
            r6 = -1
            switch(r2) {
                case -1917430950: goto L_0x0039;
                case -1138588223: goto L_0x002f;
                case -445172446: goto L_0x0025;
                case -229777127: goto L_0x001b;
                case 1030265252: goto L_0x0011;
                default: goto L_0x0010;
            }
        L_0x0010:
            goto L_0x0043
        L_0x0011:
            java.lang.String r2 = "android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0010
            r1 = 3
            goto L_0x0044
        L_0x001b:
            java.lang.String r2 = "android.intent.action.SIM_STATE_CHANGED"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0010
            r1 = r4
            goto L_0x0044
        L_0x0025:
            java.lang.String r2 = "com.mediatek.common.carrierexpress.cxp_notify_feature"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0010
            r1 = 4
            goto L_0x0044
        L_0x002f:
            java.lang.String r2 = "android.telephony.action.CARRIER_CONFIG_CHANGED"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0010
            r1 = r5
            goto L_0x0044
        L_0x0039:
            java.lang.String r2 = "com.mediatek.ims.MTK_MMTEL_READY"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0010
            r1 = r3
            goto L_0x0044
        L_0x0043:
            r1 = r6
        L_0x0044:
            java.lang.String r2 = "carrier_volte_available_bool"
            switch(r1) {
                case 0: goto L_0x010c;
                case 1: goto L_0x0107;
                case 2: goto L_0x00a6;
                case 3: goto L_0x0062;
                case 4: goto L_0x004b;
                default: goto L_0x0049;
            }
        L_0x0049:
            goto L_0x01b3
        L_0x004b:
            android.os.Bundle r1 = r11.getExtras()
            if (r1 == 0) goto L_0x0059
            r9.updateFeatureSupportProperty(r1)
            r9.updateImsServiceConfig(r10, r0)
            goto L_0x01b3
        L_0x0059:
            java.lang.String r2 = r9.mLogTag
            java.lang.String r3 = "ACTION_CXP_NOTIFY_FEATURE, opFeature is null"
            android.telephony.Rlog.d(r2, r3)
            goto L_0x01b3
        L_0x0062:
            r1 = 1
            int r2 = r9.mMainPhoneId
            if (r2 != r6) goto L_0x006f
            int r2 = com.mediatek.ims.ImsCommonUtil.getMainCapabilityPhoneId()
            r9.mMainPhoneId = r2
            r1 = 0
            goto L_0x0080
        L_0x006f:
            int r0 = com.mediatek.ims.ImsCommonUtil.getMainCapabilityPhoneId()
            int r2 = r9.mMainPhoneId
            if (r0 != r2) goto L_0x0079
            r1 = 0
            goto L_0x0080
        L_0x0079:
            r9.mMainPhoneId = r0
            int r2 = r9.mPhoneId
            if (r2 == r0) goto L_0x0080
            r1 = 0
        L_0x0080:
            if (r1 == 0) goto L_0x01b3
            java.lang.String r2 = "ACTION_SET_RADIO_CAPABILITY_DONE"
            r9.resetWfcModeFlag(r2)
            java.lang.String r2 = r9.mLogTag
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "SET_RADIO_CAPABILITY_DONE, update IMS config with phoneId:"
            r3.append(r4)
            int r4 = r9.mPhoneId
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.telephony.Rlog.d(r2, r3)
            int r2 = r9.mPhoneId
            r9.updateImsServiceConfig(r10, r2)
            goto L_0x01b3
        L_0x00a6:
            java.lang.String r1 = "android:phone_id"
            int r0 = r11.getIntExtra(r1, r6)
            int r1 = r9.mPhoneId
            if (r0 != r1) goto L_0x01b3
            java.lang.String r1 = "ACTION_MTK_MMTEL_READY"
            r9.resetWfcModeFlag(r1)
            int r1 = r9.mPhoneId
            int r1 = android.telephony.SubscriptionManager.getSimStateForSlotIndex(r1)
            android.os.PersistableBundle r3 = android.telephony.CarrierConfigManager.getDefaultConfig()
            boolean r2 = r3.getBoolean(r2)
            java.lang.String r3 = r9.mLogTag
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "ACTION_MTK_MMTEL_READY, update IMS config with phoneId:"
            r4.append(r5)
            r4.append(r0)
            java.lang.String r5 = ", simState = "
            r4.append(r5)
            r4.append(r1)
            java.lang.String r5 = ", carrier config loaded: "
            r4.append(r5)
            boolean r5 = r9.mIsCarrierConfigLoaded
            r4.append(r5)
            java.lang.String r5 = ", defaultSupportVolte: "
            r4.append(r5)
            r4.append(r2)
            java.lang.String r4 = r4.toString()
            android.telephony.Rlog.d(r3, r4)
            if (r2 != 0) goto L_0x0102
            boolean r3 = r9.mIsCarrierConfigLoaded
            if (r3 == 0) goto L_0x00fa
            goto L_0x0102
        L_0x00fa:
            java.lang.String r3 = r9.mLogTag
            java.lang.String r4 = "defaultSupportVolte =false and mIsCarrierConfigLoaded =false, don't update ims service config."
            android.telephony.Rlog.i(r3, r4)
            goto L_0x0105
        L_0x0102:
            r9.updateImsServiceConfig(r10, r0)
        L_0x0105:
            goto L_0x01b3
        L_0x0107:
            r9.handleCarrierConfigChanged(r10, r11)
            goto L_0x01b3
        L_0x010c:
            java.lang.String r1 = "ss"
            java.lang.String r1 = r11.getStringExtra(r1)
            java.lang.String r7 = "phone"
            int r0 = r11.getIntExtra(r7, r6)
            int r7 = r9.mPhoneId
            if (r0 != r7) goto L_0x01b3
            int r7 = r1.hashCode()
            java.lang.String r8 = "ABSENT"
            switch(r7) {
                case -2044189691: goto L_0x0138;
                case 77848963: goto L_0x012e;
                case 1924388665: goto L_0x0126;
                default: goto L_0x0125;
            }
        L_0x0125:
            goto L_0x0141
        L_0x0126:
            boolean r3 = r1.equals(r8)
            if (r3 == 0) goto L_0x0125
            r3 = r5
            goto L_0x0142
        L_0x012e:
            java.lang.String r3 = "READY"
            boolean r3 = r1.equals(r3)
            if (r3 == 0) goto L_0x0125
            r3 = r4
            goto L_0x0142
        L_0x0138:
            java.lang.String r7 = "LOADED"
            boolean r7 = r1.equals(r7)
            if (r7 == 0) goto L_0x0125
            goto L_0x0142
        L_0x0141:
            r3 = r6
        L_0x0142:
            switch(r3) {
                case 0: goto L_0x0197;
                case 1: goto L_0x0146;
                case 2: goto L_0x0146;
                default: goto L_0x0145;
            }
        L_0x0145:
            goto L_0x01b3
        L_0x0146:
            if (r1 != r8) goto L_0x0165
            java.lang.String r3 = r9.mLogTag
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "ICC_ABSENT["
            r5.append(r6)
            r5.append(r0)
            java.lang.String r6 = "], reset carrier config loaded flag."
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.telephony.Rlog.d(r3, r5)
            r9.mIsCarrierConfigLoaded = r4
        L_0x0165:
            android.os.PersistableBundle r3 = android.telephony.CarrierConfigManager.getDefaultConfig()
            boolean r2 = r3.getBoolean(r2)
            if (r2 == 0) goto L_0x01b3
            java.lang.String r3 = "persist.vendor.mtk_dynamic_ims_switch"
            java.lang.String r3 = android.os.SystemProperties.get(r3)
            java.lang.String r4 = "1"
            boolean r3 = r4.equals(r3)
            if (r3 != 0) goto L_0x01b3
            java.lang.String r3 = r9.mLogTag
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "updateImsServiceConfig after SIM event, phoneId:"
            r4.append(r5)
            r4.append(r0)
            java.lang.String r4 = r4.toString()
            android.telephony.Rlog.d(r3, r4)
            r9.updateImsServiceConfig(r10, r0)
            goto L_0x01b3
        L_0x0197:
            int r2 = r9.mPhoneId
            boolean r2 = com.mediatek.ims.config.internal.ImsConfigUtils.isWfcEnabledByUser(r10, r2)
            if (r2 == 0) goto L_0x01b3
            int r2 = r9.mPhoneId
            int r2 = com.mediatek.ims.config.internal.ImsConfigUtils.getWfcMode(r10, r2)
            if (r2 != 0) goto L_0x01b3
            int r2 = r9.mPhoneId
            com.mediatek.ims.config.internal.ImsConfigUtils.sendWifiOnlyModeIntent(r10, r2, r5)
            java.lang.String r2 = r9.mLogTag
            java.lang.String r3 = "Turn OFF radio, after sim ready and wfc mode is wifi_only"
            android.telephony.Rlog.d(r2, r3)
        L_0x01b3:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.config.internal.ImsConfigEventReceiver.onReceive(android.content.Context, android.content.Intent):void");
    }

    private void updateFeatureSupportProperty(Bundle bundle) {
        String isWfcOn = bundle.getString(PROPERTY_MTK_WFC_SUPPORT, INVALID_VALUE);
        if (!INVALID_VALUE.equals(isWfcOn)) {
            if (PROPERTY_SUPPORT.equals(isWfcOn)) {
                this.mRilAdapter.turnOnWfc((Message) null);
            } else {
                this.mRilAdapter.turnOffWfc((Message) null);
            }
        }
        String isVolteOn = bundle.getString(PROPERTY_MTK_VOLTE_SUPPORT, INVALID_VALUE);
        if (!INVALID_VALUE.equals(isVolteOn)) {
            if (PROPERTY_SUPPORT.equals(isVolteOn)) {
                this.mRilAdapter.turnOnVolte((Message) null);
            } else {
                this.mRilAdapter.turnOffVolte((Message) null);
            }
        }
        String isVilteOn = bundle.getString(PROPERTY_MTK_VILTE_SUPPORT, INVALID_VALUE);
        if (!INVALID_VALUE.equals(isVilteOn)) {
            if (PROPERTY_SUPPORT.equals(isVilteOn)) {
                this.mRilAdapter.turnOnVilte((Message) null);
            } else {
                this.mRilAdapter.turnOffVilte((Message) null);
            }
        }
        String isViWiFiOn = bundle.getString(PROPERTY_MTK_VIWIFI_SUPPORT, INVALID_VALUE);
        if (!INVALID_VALUE.equals(isViWiFiOn)) {
            if (PROPERTY_SUPPORT.equals(isViWiFiOn)) {
                this.mRilAdapter.turnOnViwifi((Message) null);
            } else {
                this.mRilAdapter.turnOffViwifi((Message) null);
            }
        }
        String isRcsUaOn = bundle.getString(PROPERTY_MTK_RCS_UA_SUPPORT, INVALID_VALUE);
        if (!INVALID_VALUE.equals(isRcsUaOn)) {
            if (PROPERTY_SUPPORT.equals(isRcsUaOn)) {
                this.mRilAdapter.turnOnRcsUa((Message) null);
            } else {
                this.mRilAdapter.turnOffRcsUa((Message) null);
            }
        }
        String str = this.mLogTag;
        Rlog.d(str, "updateFeatureSupportProperty(), volte:" + isVolteOn + " wfc:" + isWfcOn + " vilte:" + isVilteOn + " viwifi:" + isViWiFiOn + " isRcsUaOn:" + isRcsUaOn);
    }

    private void handleCarrierConfigChanged(Context context, Intent intent) {
        int phoneId = intent.getIntExtra("phone", -1);
        int i = this.mPhoneId;
        if (phoneId == i) {
            int simState = SubscriptionManager.getSimStateForSlotIndex(i);
            String str = this.mLogTag;
            Rlog.d(str, "received CARRIER_CONFIG_CHANGED[" + this.mPhoneId + "], simState = " + simState);
            if (simState == 10) {
                this.mIsCarrierConfigLoaded = true;
            }
            TelephonyManager tm = TelephonyManager.getDefault();
            if (tm == null || tm.hasIccCard(phoneId)) {
                resetWfcModeFlag("ACTION_CARRIER_CONFIG_CHANGED");
                boolean removeWfcPrefMode = ImsConfigUtils.getBooleanCarrierConfig(context, ImsCarrierConfigConstants.MTK_KEY_WFC_REMOVE_PREFERENCE_MODE_BOOL, phoneId);
                boolean wfcModeEditable = ImsConfigUtils.getBooleanCarrierConfig(context, "editable_wfc_mode_bool", phoneId);
                String str2 = this.mLogTag;
                Rlog.d(str2, "KEY_WFC_REMOVE_PREFERENCE_MODE_BOOL = " + removeWfcPrefMode);
                String str3 = this.mLogTag;
                Rlog.d(str3, "KEY_EDITABLE_WFC_MODE_BOOL = " + wfcModeEditable);
                if (!wfcModeEditable) {
                    int wfcMode = ImsConfigUtils.getIntCarrierConfig(context, "carrier_default_wfc_ims_mode_int", phoneId);
                    String str4 = this.mLogTag;
                    Rlog.d(str4, "ACTION_CARRIER_CONFIG_CHANGED: set wfc mode = " + wfcMode + ", phoneId:" + phoneId);
                    if (ImsCommonUtil.supportMims() || ImsCommonUtil.getMainPhoneIdForSingleIms() == phoneId) {
                        ImsManager.getInstance(context, phoneId).setWfcMode(wfcMode);
                        return;
                    }
                    String str5 = this.mLogTag;
                    Rlog.d(str5, "no set wfc mode due to mims: " + ImsCommonUtil.supportMims() + ", main phone id:" + ImsCommonUtil.getMainPhoneIdForSingleIms());
                    return;
                }
                return;
            }
            Rlog.e(this.mLogTag, "No need to reload config storage");
        }
    }

    private void resetWfcModeFlag(String reason) {
        this.mHandler.removeMessages(108);
        Message msg = new Message();
        String str = this.mLogTag;
        Rlog.d(str, "resetWfcModeFlag, reason: " + reason);
        msg.what = 108;
        this.mHandler.sendMessage(msg);
    }

    private void updateImsServiceConfig(Context context, int phoneId) {
        if (this.mImsManagerOemPlugin == null) {
            this.mImsManagerOemPlugin = ExtensionFactory.makeOemPluginFactory(context).makeImsManagerPlugin(context);
        }
        if (ImsCommonUtil.supportMims() || phoneId == ImsCommonUtil.getMainCapabilityPhoneId()) {
            this.mImsManagerOemPlugin.updateImsServiceConfig(context, phoneId);
        } else if (DEBUG) {
            Rlog.d(this.mLogTag, "Do not update if phoneId is not main capability");
        }
    }
}
