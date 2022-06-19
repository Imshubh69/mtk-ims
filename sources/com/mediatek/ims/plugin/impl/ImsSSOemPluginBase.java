package com.mediatek.ims.plugin.impl;

import android.content.Context;
import android.hardware.radio.V1_0.LastCallFailCause;
import android.os.SystemProperties;
import android.telephony.ims.ImsReasonInfo;
import android.util.Log;
import com.android.internal.telephony.CommandException;
import com.mediatek.ims.plugin.ImsSSOemPlugin;

public class ImsSSOemPluginBase implements ImsSSOemPlugin {
    private static final boolean DBG = true;
    public static final String ERROR_MSG_PROP_PREFIX = "vendor.gsm.radio.ss.errormsg.";
    private static final String TAG = "ImsSSOemPluginBase";
    private Context mContext;

    public ImsSSOemPluginBase(Context context) {
        this.mContext = context;
    }

    public ImsReasonInfo commandExceptionToReason(CommandException commandException, int phoneId) {
        CommandException.Error err = commandException.getCommandError();
        Log.d(TAG, "commandException: " + err);
        if (err == CommandException.Error.OEM_ERROR_2) {
            return new ImsReasonInfo(LastCallFailCause.OEM_CAUSE_6, 0);
        }
        if (err == CommandException.Error.OEM_ERROR_3) {
            return new ImsReasonInfo(LastCallFailCause.OEM_CAUSE_7, 0);
        }
        if (err == CommandException.Error.OEM_ERROR_4) {
            return new ImsReasonInfo(LastCallFailCause.OEM_CAUSE_8, 0);
        }
        if (err == CommandException.Error.OEM_ERROR_25) {
            return new ImsReasonInfo(LastCallFailCause.OEM_CAUSE_9, 0, getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_25, phoneId));
        }
        if (err == CommandException.Error.OEM_ERROR_7) {
            return new ImsReasonInfo(LastCallFailCause.OEM_CAUSE_10, 0);
        }
        if (err == CommandException.Error.OEM_ERROR_6) {
            return new ImsReasonInfo(61456, 0, getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_6, phoneId));
        }
        if (err == CommandException.Error.OEM_ERROR_24) {
            return new ImsReasonInfo(61457, 0, getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_24, phoneId));
        }
        if (err == CommandException.Error.OEM_ERROR_23) {
            return new ImsReasonInfo(61458, 0, getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_23, phoneId));
        }
        if (err == CommandException.Error.OEM_ERROR_22) {
            return new ImsReasonInfo(61459, 0, getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_22, phoneId));
        }
        if (err == CommandException.Error.REQUEST_NOT_SUPPORTED) {
            return new ImsReasonInfo(801, 0);
        }
        if (err == CommandException.Error.RADIO_NOT_AVAILABLE) {
            return new ImsReasonInfo(802, 0);
        }
        if (err == CommandException.Error.PASSWORD_INCORRECT) {
            return new ImsReasonInfo(821, 0);
        }
        if (err == CommandException.Error.FDN_CHECK_FAILURE) {
            return new ImsReasonInfo(LastCallFailCause.FDN_BLOCKED, 0);
        }
        return new ImsReasonInfo(804, 0);
    }

    public String getXcapQueryCarrierConfigKey() {
        return "mtk_carrier_ss_xcap_query";
    }

    public String getVolteSubscriptionKey() {
        return "volte_subscription";
    }

    public int getVolteSubUnknownConstant() {
        return 0;
    }

    public int getVolteSubEnableConstant() {
        return 1;
    }

    public int getVolteSubDisableConstant() {
        return 2;
    }

    public String getXCAPErrorMessageFromSysProp(CommandException.Error error, int phondId) {
        String errorCode;
        String propNamePrefix = ERROR_MSG_PROP_PREFIX + phondId;
        String fullErrorMsg = "";
        int idx = 0;
        String propValue = SystemProperties.get(propNamePrefix + "." + 0, "");
        while (!propValue.equals("")) {
            fullErrorMsg = fullErrorMsg + propValue;
            idx++;
            propValue = SystemProperties.get(propNamePrefix + "." + idx, "");
        }
        Log.d(TAG, "fullErrorMsg: " + fullErrorMsg);
        switch (C01641.$SwitchMap$com$android$internal$telephony$CommandException$Error[error.ordinal()]) {
            case 1:
                errorCode = "409";
                break;
            case 2:
                errorCode = "412";
                break;
            case 3:
                errorCode = "415";
                break;
            case 4:
                errorCode = "500";
                break;
            case 5:
                errorCode = "503";
                break;
            default:
                Log.d(TAG, "errorMsg: " + null);
                return null;
        }
        if (!fullErrorMsg.startsWith(errorCode)) {
            Log.d(TAG, "errorMsg: " + null);
            return null;
        }
        String errorMsg = fullErrorMsg.substring(errorCode.length() + 1);
        Log.d(TAG, "errorMsg: " + errorMsg);
        return errorMsg;
    }

    /* renamed from: com.mediatek.ims.plugin.impl.ImsSSOemPluginBase$1 */
    static /* synthetic */ class C01641 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$CommandException$Error;

        static {
            int[] iArr = new int[CommandException.Error.values().length];
            $SwitchMap$com$android$internal$telephony$CommandException$Error = iArr;
            try {
                iArr[CommandException.Error.OEM_ERROR_25.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_6.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_24.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_23.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_22.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }
}
