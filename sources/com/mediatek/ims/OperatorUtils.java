package com.mediatek.ims;

import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.mediatek.ims.common.SubscriptionManagerHelper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperatorUtils {
    private static final String LOG_TAG = "OperatorUtils";
    private static final String PROPERTY_MTK_CT_VOLTE_SUPPORT = "persist.vendor.mtk_ct_volte_support";
    private static final String PROPERTY_MTK_DYNAMIC_IMS_SWITCH = "persist.vendor.mtk_dynamic_ims_switch";
    private static final String PROPERTY_MTK_UIM_SUBSCRIBERID = "vendor.ril.uim.subscriberid";
    private static final Map<OPID, List> mOPMap = new HashMap<OPID, List>() {
        {
            put(OPID.OP01, Arrays.asList(new String[]{"46000", "46002", "46004", "46007", "46008"}));
            put(OPID.OP02, Arrays.asList(new String[]{"46001", "46006", "46009", "45407"}));
            put(OPID.OP03, Arrays.asList(new String[]{"20801", "20802"}));
            put(OPID.OP05, Arrays.asList(new String[]{"23203", "23204", "21901", "23001", "21630", "29702", "20416", "20420", "26002", "22004", "23430", "26201", "26206", "26278"}));
            put(OPID.OP06, Arrays.asList(new String[]{"21401", "21406", "20404", "28602", "23415", "27602", "23003", "23099", "60202", "28802", "54201", "26202", "26204", "26209", "62002", "20205", "21670", "27402", "27403", "27201", "22210", "27801", "53001", "26801", "22601", "42702", "23591", "90128"}));
            put(OPID.OP07, Arrays.asList(new String[]{"310030", "310070", "310090", "310150", "310170", "310280", "310380", "310410", "310560", "310680", "311180"}));
            put(OPID.OP08, Arrays.asList(new String[]{"310160", "310260", "310490", "310580", "310660", "310200", "310210", "310220", "310230", "310240", "310250", "310270", "310310", "310800"}));
            put(OPID.OP09, Arrays.asList(new String[]{"46003", "46011", "46012", "45502", "45507"}));
            put(OPID.OP11, Arrays.asList(new String[]{"23420"}));
            put(OPID.OP12, Arrays.asList(new String[]{"310590", "310890", "311270", "311480"}));
            put(OPID.OP15, Arrays.asList(new String[]{"26203", "26207", "26208", "26211", "26277"}));
            put(OPID.OP16, Arrays.asList(new String[]{"23430", "23431", "23432", "23433", "23434"}));
            put(OPID.OP18, Arrays.asList(new String[]{"405854", "405855", "405856", "405872", "405857", "405858", "405859", "405860", "405861", "405862", "405873", "405863", "405864", "405874", "405865", "405866", "405867", "405868", "405869", "405871", "405870", "405840"}));
            put(OPID.OP50, Arrays.asList(new String[]{"44020"}));
            put(OPID.OP112, Arrays.asList(new String[]{"334020"}));
            put(OPID.OP129, Arrays.asList(new String[]{"44007", "44008", "44050", "44051", "44052", "44053", "44054", "44055", "44056", "44070", "44071", "44072", "44073", "44074", "44075", "44076", "44077", "44078", "44079", "44088", "44089", "44110", "44170"}));
            put(OPID.OP156, Arrays.asList(new String[]{"23802"}));
            put(OPID.OP130, Arrays.asList(new String[]{"72402", "72403", "72404"}));
            put(OPID.OP120, Arrays.asList(new String[]{"72405"}));
            put(OPID.OP132, Arrays.asList(new String[]{"72406", "72410", "72411", "72423"}));
            put(OPID.OPOi, Arrays.asList(new String[]{"72416", "72424", "72431"}));
            put(OPID.OP165, Arrays.asList(new String[]{"22802"}));
            put(OPID.OP152, Arrays.asList(new String[]{"50502"}));
            put(OPID.OP117, Arrays.asList(new String[]{"51009", "51028"}));
            put(OPID.OP131, Arrays.asList(new String[]{"52004"}));
            put(OPID.OP125, Arrays.asList(new String[]{"52005"}));
            put(OPID.OP132_Peru, Arrays.asList(new String[]{"71606"}));
            put(OPID.OP136_Peru, Arrays.asList(new String[]{"71617"}));
            put(OPID.OP147, Arrays.asList(new String[]{"40410", "40431", "40440", "40445", "40449", "40551", "40552", "40553", "40554", "40555", "40556", "40490", "40492", "40493", "40494", "40495", "40496", "40497", "40498", "40402", "40403", "40416", "40470", "405030", "405035", "405036", "405037", "405038", "405039", "405044"}));
            put(OPID.OP151, Arrays.asList(new String[]{"52503"}));
            put(OPID.OP236, Arrays.asList(new String[]{"31100", "311220", "311221", "311222", "311223", "311224", "311225", "311226", "311227", "311228", "311229", "311580", "311581", "311582", "311583", "311584", "311585", "311586", "311587", "311588", "311589", "311003"}));
            put(OPID.OP_EIOT, Arrays.asList(new String[]{"24099"}));
            put(OPID.OPClaro, Arrays.asList(new String[]{"72405", "722310", "74810", "73003", "732101"}));
        }
    };

    public enum OPID {
        OP01,
        OP02,
        OP03,
        OP05,
        OP06,
        OP07,
        OP08,
        OP09,
        OP11,
        OP12,
        OP15,
        OP16,
        OP18,
        OP50,
        OP112,
        OP129,
        OP156,
        OP130,
        OP120,
        OP132,
        OPOi,
        OP165,
        OP152,
        OP117,
        OP131,
        OP125,
        OP132_Peru,
        OP136_Peru,
        OP147,
        OP151,
        OP236,
        OP_EIOT,
        OPClaro
    }

    public static boolean isMainCapabilitySimOperator(OPID id) {
        return isMatched(id, getMainCapabilityPhoneId());
    }

    public static boolean isMatched(OPID id, int phoneId) {
        String mccMnc = getSimOperatorNumericForPhone(phoneId);
        List mccMncList = mOPMap.get(id);
        if (mccMncList == null || !mccMncList.contains(mccMnc)) {
            return false;
        }
        return true;
    }

    private static int getMainCapabilityPhoneId() {
        int phoneId = SystemProperties.getInt(ImsConstants.PROPERTY_CAPABILITY_SWITCH, 1) - 1;
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            phoneId = -1;
        }
        Rlog.d(LOG_TAG, "getMainCapabilityPhoneId = " + phoneId);
        return phoneId;
    }

    public static String getSimOperatorNumericForPhone(int phoneId) {
        String mccMncPropertyName;
        if (TelephonyManager.getDefault().getCurrentPhoneType(SubscriptionManagerHelper.getSubIdUsingPhoneId(phoneId)) == 2) {
            if (phoneId == 0) {
                mccMncPropertyName = "vendor.cdma.ril.uicc.mccmnc";
            } else {
                mccMncPropertyName = "vendor.cdma.ril.uicc.mccmnc." + phoneId;
            }
        } else if (phoneId == 0) {
            mccMncPropertyName = "vendor.gsm.ril.uicc.mccmnc";
        } else {
            mccMncPropertyName = "vendor.gsm.ril.uicc.mccmnc." + phoneId;
        }
        String mccMnc = SystemProperties.get(mccMncPropertyName, "");
        Rlog.w(LOG_TAG, "getMccMnc, mccMnc value:" + Rlog.pii(LOG_TAG, mccMnc));
        return mccMnc;
    }

    public static boolean isCTVolteDisabled(int phoneId) {
        if (!"1".equals(SystemProperties.get(PROPERTY_MTK_DYNAMIC_IMS_SWITCH)) || SystemProperties.getInt(PROPERTY_MTK_CT_VOLTE_SUPPORT, 0) != 0 || !isMatched(OPID.OP09, phoneId)) {
            return false;
        }
        Rlog.d(LOG_TAG, "SIM loaded, but CT VoLTE shall not support");
        return true;
    }

    public static boolean isOperator(String mccMnc, OPID id) {
        boolean r = false;
        if (mOPMap.get(id).contains(mccMnc)) {
            r = true;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("isOperator: id = ");
        sb.append(Rlog.pii(LOG_TAG, id));
        sb.append(", matched = ");
        sb.append(r ? "true" : "false");
        Rlog.d(LOG_TAG, sb.toString());
        return r;
    }
}
