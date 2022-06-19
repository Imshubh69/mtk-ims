package com.mediatek.ims;

public class ImsConstants {
    public static final String ACTION_IMS_CONFERENCE_CALL_INDICATION = "android.intent.action.ims.conference";
    public static final String ACTION_IMS_DIALOG_EVENT_PACKAGE = "com.mediatek.intent.action.ims.dialogEventPackage";
    public static final String ACTION_LTE_MESSAGE_WAITING_INDICATION = "mediatek.intent.action.lte.mwi";
    public static final String EXTRA_CALL_ID = "call.id";
    public static final String EXTRA_DEP_CONTENT = "com.mediatek.intent.extra.DEP_CONTENT";
    public static final String EXTRA_LTE_MWI_BODY = "lte_mwi_body";
    public static final String EXTRA_MESSAGE_CONTENT = "message.content";
    public static final String EXTRA_PHONE_ID = "phone.id";
    public static final int IMS_STATE_DISABLED = 0;
    public static final int IMS_STATE_DISABLING = 3;
    public static final int IMS_STATE_ENABLE = 1;
    public static final int IMS_STATE_ENABLING = 2;
    public static final String PACKAGE_NAME_PHONE = "com.android.phone";
    public static final String PERMISSION_READ_LTE_MESSAGE_WAITING_INDICATION = "com.mediatek.permission.READ_LTE_MESSAGE_WAITING_INDICATION";
    public static final String PROPERTY_CAPABILITY_SWITCH = "persist.vendor.radio.simswitch";
    public static final String PROPERTY_MD_MULTI_IMS_SUPPORT = "ro.vendor.md_mims_support";
    public static String PROPERTY_TBCW_MODE = "persist.vendor.radio.terminal-based.cw";
    public static final int REGISTRATION_TECH_CELLULAR = 0;
    public static final int REGISTRATION_TECH_IWLAN = 1;
    public static final int REGISTRATION_TECH_NONE = -1;
    public static final int REGISTRATION_TECH_VODATA1 = 6;
    public static final int REGISTRATION_TECH_VODATA2 = 7;
    public static final int REGISTRATION_TECH_VODATA3 = 8;
    public static final int REGISTRATION_TECH_VODATA4 = 9;
    public static final String SELF_IDENTIFY_UPDATE = "com.mediatek.ims.action.self_identify_update";
    public static final String SYS_PROP_MD_AUTO_SETUP_IMS = "ro.vendor.md_auto_setup_ims";
    public static String TBCW_DISABLED = "disabled_tbcw";
    public static String TBCW_OFF = "enabled_tbcw_off";
}
