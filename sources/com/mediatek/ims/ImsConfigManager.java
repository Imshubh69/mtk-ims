package com.mediatek.ims;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.ImsManager;
import com.android.ims.internal.IImsConfig;
import com.mediatek.ims.config.ImsConfigContract;
import com.mediatek.ims.config.internal.ImsConfigAdapter;
import com.mediatek.ims.config.internal.ImsConfigImpl;
import com.mediatek.ims.config.internal.MtkImsConfigImpl;
import com.mediatek.ims.internal.IMtkImsConfig;
import com.mediatek.ims.plugin.ExtensionFactory;
import com.mediatek.ims.plugin.ImsManagerOemPlugin;
import com.mediatek.ims.ril.ImsCommandsInterface;
import java.util.HashMap;
import java.util.Map;

public class ImsConfigManager {
    private static final boolean DEBUG = (TextUtils.equals(Build.TYPE, "eng") || SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1);
    private static final String LOG_TAG = "ImsConfigManager";
    private static final String PROPERTY_CAPABILITY_SWITCH = "persist.vendor.radio.simswitch";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final boolean SENLOG = TextUtils.equals(Build.TYPE, "user");
    private static final boolean TELDBG;
    private final BroadcastReceiver mBroadcastReceiver;
    /* access modifiers changed from: private */
    public Context mContext;
    private Map<Integer, ImsConfigAdapter> mImsConfigAdapterMap = new HashMap();
    private Map<Integer, ImsConfigImpl> mImsConfigInstanceMap = new HashMap();
    private ImsManagerOemPlugin mImsManagerOemPlugin = null;
    private ImsCommandsInterface[] mImsRILAdapters = null;
    private Map<Integer, IMtkImsConfig> mMtkImsConfigInstanceMap = new HashMap();

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        TELDBG = z;
    }

    public ImsConfigManager(Context context, ImsCommandsInterface[] imsRILAdapters) {
        C01121 r0 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int phoneId;
                if (ImsConfigContract.ACTION_DYNAMIC_IMS_SWITCH_TRIGGER.equals(intent.getAction())) {
                    int phoneId2 = intent.getIntExtra("phone", -1);
                    String simState = intent.getStringExtra("ss");
                    Rlog.d(ImsConfigManager.LOG_TAG, "DYNAMIC_IMS_SWITCH_TRIGGER phoneId:" + phoneId2 + ", simState:" + simState);
                    ImsConfigManager.this.updateImsResrouceCapability(context, intent);
                } else if (ImsConfigContract.ACTION_IMS_CONFIG_CHANGED.equals(intent.getAction())) {
                    int phoneId3 = intent.getIntExtra("phone_id", 0);
                    int itemId = intent.getIntExtra(ImsConfigContract.EXTRA_CHANGED_ITEM, -1);
                    Log.d(ImsConfigManager.LOG_TAG, "ACTION_IMS_CONFIG_CHANGED phoneId:" + phoneId3 + ", itemId:" + itemId);
                    if (1003 == itemId && "OP236".equals(SystemProperties.get("persist.vendor.operator.optr", "OM"))) {
                        ImsConfigManager.this.setVdpProvision(phoneId3, Integer.parseInt(intent.getStringExtra("value")));
                    }
                } else if (ImsConfigContract.ACTION_CONFIG_LOADED.equals(intent.getAction()) && "OP236".equals(SystemProperties.get("persist.vendor.operator.optr", "OM")) && (phoneId = intent.getIntExtra("phone", -1)) >= 0) {
                    try {
                        int value = ImsConfigManager.this.getEx(phoneId).getProvisionedValue(1003);
                        if (!ImsManager.getInstance(ImsConfigManager.this.mContext, phoneId).isVolteEnabledByPlatform() || value != 1) {
                            ImsConfigManager.this.setVdpProvision(phoneId, value);
                        } else {
                            Log.d(ImsConfigManager.LOG_TAG, "ignore setVdpProvision for internal test.");
                        }
                    } catch (RemoteException e) {
                        Log.e(ImsConfigManager.LOG_TAG, "getProvisionedValue fail: " + e);
                    }
                }
            }
        };
        this.mBroadcastReceiver = r0;
        if (DEBUG) {
            Rlog.d(LOG_TAG, "ImsConfigManager Enter");
        }
        this.mContext = context;
        this.mImsRILAdapters = imsRILAdapters;
        if (!ImsCommonUtil.supportMdAutoSetupIms()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ImsConfigContract.ACTION_DYNAMIC_IMS_SWITCH_TRIGGER);
            context.registerReceiver(r0, filter);
        } else if ("OP236".equals(SystemProperties.get("persist.vendor.operator.optr", "OM"))) {
            IntentFilter filter2 = new IntentFilter();
            filter2.addAction(ImsConfigContract.ACTION_IMS_CONFIG_CHANGED);
            filter2.addAction(ImsConfigContract.ACTION_CONFIG_LOADED);
            context.registerReceiver(r0, filter2);
        }
    }

    public void init(int phoneId, ImsCommandsInterface[] imsRILAdapters) {
        if (imsRILAdapters != null) {
            this.mImsRILAdapters = imsRILAdapters;
        }
        ImsConfigAdapter configAdapter = getImsConfigAdapter(this.mContext, this.mImsRILAdapters, phoneId);
        if (DEBUG) {
            Rlog.d(LOG_TAG, "init ImsConfigImpl phoneId:" + phoneId);
        }
        synchronized (this.mImsConfigInstanceMap) {
            this.mImsConfigInstanceMap.put(Integer.valueOf(phoneId), new ImsConfigImpl(this.mContext, this.mImsRILAdapters[phoneId], configAdapter, phoneId));
        }
    }

    public IImsConfig get(int phoneId) {
        IImsConfig instance;
        ImsConfigAdapter configAdapter = getImsConfigAdapter(this.mContext, this.mImsRILAdapters, phoneId);
        synchronized (this.mImsConfigInstanceMap) {
            if (this.mImsConfigInstanceMap.containsKey(Integer.valueOf(phoneId))) {
                instance = this.mImsConfigInstanceMap.get(Integer.valueOf(phoneId)).getIImsConfig();
            } else {
                ImsConfigImpl imsConfigImpl = new ImsConfigImpl(this.mContext, this.mImsRILAdapters[phoneId], configAdapter, phoneId);
                instance = imsConfigImpl.getIImsConfig();
                this.mImsConfigInstanceMap.put(Integer.valueOf(phoneId), imsConfigImpl);
            }
        }
        return instance;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0041, code lost:
        if (r3 == false) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0043, code lost:
        android.telephony.Rlog.d(LOG_TAG, "initEx MtkImsConfigImpl phoneId:" + r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0059, code lost:
        r1 = r10.mMtkImsConfigInstanceMap;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x005b, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r10.mMtkImsConfigInstanceMap.put(java.lang.Integer.valueOf(r11), new com.mediatek.ims.config.internal.MtkImsConfigImpl(r10.mContext, r10.mImsRILAdapters[r11], r7, r2, r11));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0073, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0074, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void initEx(int r11) {
        /*
            r10 = this;
            r0 = 0
            r1 = 0
            android.content.Context r2 = r10.mContext
            com.mediatek.ims.ril.ImsCommandsInterface[] r3 = r10.mImsRILAdapters
            com.mediatek.ims.config.internal.ImsConfigAdapter r2 = r10.getImsConfigAdapter(r2, r3, r11)
            boolean r3 = DEBUG
            if (r3 == 0) goto L_0x0024
            java.lang.String r4 = "ImsConfigManager"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "initEx ImsConfigImpl phoneId:"
            r5.append(r6)
            r5.append(r11)
            java.lang.String r5 = r5.toString()
            android.telephony.Rlog.d(r4, r5)
        L_0x0024:
            java.util.Map<java.lang.Integer, com.mediatek.ims.config.internal.ImsConfigImpl> r4 = r10.mImsConfigInstanceMap
            monitor-enter(r4)
            com.mediatek.ims.config.internal.ImsConfigImpl r5 = new com.mediatek.ims.config.internal.ImsConfigImpl     // Catch:{ all -> 0x0078 }
            android.content.Context r6 = r10.mContext     // Catch:{ all -> 0x0078 }
            com.mediatek.ims.ril.ImsCommandsInterface[] r7 = r10.mImsRILAdapters     // Catch:{ all -> 0x0078 }
            r7 = r7[r11]     // Catch:{ all -> 0x0078 }
            r5.<init>(r6, r7, r2, r11)     // Catch:{ all -> 0x0078 }
            r0 = r5
            com.android.ims.internal.IImsConfig r7 = r0.getIImsConfig()     // Catch:{ all -> 0x0078 }
            java.util.Map<java.lang.Integer, com.mediatek.ims.config.internal.ImsConfigImpl> r1 = r10.mImsConfigInstanceMap     // Catch:{ all -> 0x007d }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r11)     // Catch:{ all -> 0x007d }
            r1.put(r5, r0)     // Catch:{ all -> 0x007d }
            monitor-exit(r4)     // Catch:{ all -> 0x007d }
            if (r3 == 0) goto L_0x0059
            java.lang.String r1 = "ImsConfigManager"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "initEx MtkImsConfigImpl phoneId:"
            r3.append(r4)
            r3.append(r11)
            java.lang.String r3 = r3.toString()
            android.telephony.Rlog.d(r1, r3)
        L_0x0059:
            java.util.Map<java.lang.Integer, com.mediatek.ims.internal.IMtkImsConfig> r1 = r10.mMtkImsConfigInstanceMap
            monitor-enter(r1)
            com.mediatek.ims.config.internal.MtkImsConfigImpl r3 = new com.mediatek.ims.config.internal.MtkImsConfigImpl     // Catch:{ all -> 0x0075 }
            android.content.Context r5 = r10.mContext     // Catch:{ all -> 0x0075 }
            com.mediatek.ims.ril.ImsCommandsInterface[] r4 = r10.mImsRILAdapters     // Catch:{ all -> 0x0075 }
            r6 = r4[r11]     // Catch:{ all -> 0x0075 }
            r4 = r3
            r8 = r2
            r9 = r11
            r4.<init>(r5, r6, r7, r8, r9)     // Catch:{ all -> 0x0075 }
            java.util.Map<java.lang.Integer, com.mediatek.ims.internal.IMtkImsConfig> r4 = r10.mMtkImsConfigInstanceMap     // Catch:{ all -> 0x0075 }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r11)     // Catch:{ all -> 0x0075 }
            r4.put(r5, r3)     // Catch:{ all -> 0x0075 }
            monitor-exit(r1)     // Catch:{ all -> 0x0075 }
            return
        L_0x0075:
            r3 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0075 }
            throw r3
        L_0x0078:
            r3 = move-exception
            r7 = r1
            r1 = r3
        L_0x007b:
            monitor-exit(r4)     // Catch:{ all -> 0x007d }
            throw r1
        L_0x007d:
            r1 = move-exception
            goto L_0x007b
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.ImsConfigManager.initEx(int):void");
    }

    public IMtkImsConfig getEx(int phoneId) {
        IImsConfig instance;
        IMtkImsConfig instanceEx;
        ImsConfigAdapter configAdapter = getImsConfigAdapter(this.mContext, this.mImsRILAdapters, phoneId);
        synchronized (this.mImsConfigInstanceMap) {
            if (this.mImsConfigInstanceMap.containsKey(Integer.valueOf(phoneId))) {
                instance = this.mImsConfigInstanceMap.get(Integer.valueOf(phoneId)).getIImsConfig();
            } else {
                ImsConfigImpl imsConfigImpl = new ImsConfigImpl(this.mContext, this.mImsRILAdapters[phoneId], configAdapter, phoneId);
                instance = imsConfigImpl.getIImsConfig();
                this.mImsConfigInstanceMap.put(Integer.valueOf(phoneId), imsConfigImpl);
            }
        }
        synchronized (this.mMtkImsConfigInstanceMap) {
            if (this.mMtkImsConfigInstanceMap.containsKey(Integer.valueOf(phoneId))) {
                instanceEx = this.mMtkImsConfigInstanceMap.get(Integer.valueOf(phoneId));
            } else {
                instanceEx = new MtkImsConfigImpl(this.mContext, this.mImsRILAdapters[phoneId], instance, configAdapter, phoneId);
                this.mMtkImsConfigInstanceMap.put(Integer.valueOf(phoneId), instanceEx);
            }
        }
        return instanceEx;
    }

    private ImsConfigAdapter getImsConfigAdapter(Context context, ImsCommandsInterface[] imsRilAdapters, int phoneId) {
        ImsConfigAdapter configAdapter;
        synchronized (this.mImsConfigAdapterMap) {
            if (this.mImsConfigAdapterMap.containsKey(Integer.valueOf(phoneId))) {
                configAdapter = this.mImsConfigAdapterMap.get(Integer.valueOf(phoneId));
            } else {
                if (DEBUG) {
                    Rlog.d(LOG_TAG, "init ImsConfigAdapter phone:" + phoneId);
                }
                configAdapter = new ImsConfigAdapter(context, imsRilAdapters[phoneId], phoneId);
                this.mImsConfigAdapterMap.put(Integer.valueOf(phoneId), configAdapter);
            }
        }
        return configAdapter;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0124 A[Catch:{ RemoteException -> 0x0253 }] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0163 A[Catch:{ RemoteException -> 0x0253 }] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0169 A[Catch:{ RemoteException -> 0x0253 }] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0176 A[Catch:{ RemoteException -> 0x0253 }] */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x017c A[Catch:{ RemoteException -> 0x0253 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateImsResrouceCapability(android.content.Context r22, android.content.Intent r23) {
        /*
            r21 = this;
            r1 = r21
            r2 = r22
            r3 = r23
            r4 = 0
            r5 = 0
            r0 = 0
            r6 = 0
            java.lang.String r7 = "ss"
            java.lang.String r8 = r3.getStringExtra(r7)
            java.lang.String r9 = "phone"
            r10 = -1
            int r10 = r3.getIntExtra(r9, r10)
            java.lang.String r11 = "persist.vendor.mtk_dynamic_ims_switch"
            java.lang.String r11 = android.os.SystemProperties.get(r11)
            java.lang.String r12 = "1"
            boolean r11 = r12.equals(r11)
            java.lang.String r12 = "LOADED"
            java.lang.String r13 = "ABSENT"
            java.lang.String r14 = "ImsConfigManager"
            if (r11 != 0) goto L_0x004f
            boolean r7 = r8.equalsIgnoreCase(r13)
            if (r7 != 0) goto L_0x0037
            boolean r7 = r8.equalsIgnoreCase(r12)
            if (r7 == 0) goto L_0x004e
        L_0x0037:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r9 = "updateImsServiceConfig after SIM event, phoneId:"
            r7.append(r9)
            r7.append(r10)
            java.lang.String r7 = r7.toString()
            android.telephony.Rlog.d(r14, r7)
            r1.updateImsServiceConfig(r2, r10)
        L_0x004e:
            return
        L_0x004f:
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r15 = "get MtkImsConfigImpl of phone "
            r11.append(r15)
            r11.append(r10)
            java.lang.String r11 = r11.toString()
            android.telephony.Rlog.d(r14, r11)
            com.mediatek.ims.internal.IMtkImsConfig r11 = r1.getEx(r10)
            boolean r0 = r8.equalsIgnoreCase(r13)     // Catch:{ RemoteException -> 0x0253 }
            r13 = 0
            if (r0 == 0) goto L_0x0092
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x0253 }
            r0.<init>()     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r12 = "setImsResCapability to volte only w/o SIM on phone "
            r0.append(r12)     // Catch:{ RemoteException -> 0x0253 }
            r0.append(r10)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r0 = r0.toString()     // Catch:{ RemoteException -> 0x0253 }
            android.telephony.Rlog.w(r14, r0)     // Catch:{ RemoteException -> 0x0253 }
            r0 = 1
            r12 = 0
            r15 = 0
            r11.setImsResCapability(r13, r0)     // Catch:{ RemoteException -> 0x0253 }
            r13 = 1
            r11.setImsResCapability(r13, r12)     // Catch:{ RemoteException -> 0x0253 }
            r13 = 2
            r11.setImsResCapability(r13, r15)     // Catch:{ RemoteException -> 0x0253 }
            goto L_0x0223
        L_0x0092:
            boolean r0 = r8.equalsIgnoreCase(r12)     // Catch:{ RemoteException -> 0x0253 }
            if (r0 == 0) goto L_0x0223
            boolean r0 = isTestSim(r10)     // Catch:{ RemoteException -> 0x0253 }
            if (r0 != 0) goto L_0x01d5
            java.lang.Object r0 = r2.getSystemService(r9)     // Catch:{ RemoteException -> 0x0253 }
            android.telephony.TelephonyManager r0 = (android.telephony.TelephonyManager) r0     // Catch:{ RemoteException -> 0x0253 }
            r12 = r0
            java.lang.String r0 = com.mediatek.ims.OperatorUtils.getSimOperatorNumericForPhone(r10)     // Catch:{ RemoteException -> 0x0253 }
            r6 = r0
            java.lang.String r15 = "Invalid mccMnc:"
            if (r6 == 0) goto L_0x01c2
            boolean r0 = r6.isEmpty()     // Catch:{ RemoteException -> 0x0253 }
            if (r0 == 0) goto L_0x00b7
            goto L_0x01c2
        L_0x00b7:
            r0 = 3
            java.lang.String r16 = r6.substring(r13, r0)     // Catch:{ NumberFormatException -> 0x01ae }
            int r16 = java.lang.Integer.parseInt(r16)     // Catch:{ NumberFormatException -> 0x01ae }
            r4 = r16
            java.lang.String r0 = r6.substring(r0)     // Catch:{ NumberFormatException -> 0x01ae }
            int r0 = java.lang.Integer.parseInt(r0)     // Catch:{ NumberFormatException -> 0x01ae }
            r5 = r0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x0253 }
            r0.<init>()     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r15 = "SIM loaded on phone "
            r0.append(r15)     // Catch:{ RemoteException -> 0x0253 }
            r0.append(r10)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r15 = " with mcc: "
            r0.append(r15)     // Catch:{ RemoteException -> 0x0253 }
            r0.append(r4)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r15 = " mnc: "
            r0.append(r15)     // Catch:{ RemoteException -> 0x0253 }
            r0.append(r5)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r0 = r0.toString()     // Catch:{ RemoteException -> 0x0253 }
            android.telephony.Rlog.d(r14, r0)     // Catch:{ RemoteException -> 0x0253 }
            int r0 = com.mediatek.ims.common.SubscriptionManagerHelper.getSubIdUsingPhoneId(r10)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r15 = r12.getSimSerialNumber(r0)     // Catch:{ RemoteException -> 0x0253 }
            boolean r16 = SENLOG     // Catch:{ RemoteException -> 0x0253 }
            if (r16 == 0) goto L_0x0104
            boolean r16 = TELDBG     // Catch:{ RemoteException -> 0x0253 }
            if (r16 == 0) goto L_0x0101
            goto L_0x0104
        L_0x0101:
            r17 = r0
            goto L_0x011e
        L_0x0104:
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x0253 }
            r13.<init>()     // Catch:{ RemoteException -> 0x0253 }
            r17 = r0
            java.lang.String r0 = "check iccid:"
            r13.append(r0)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r0 = android.telephony.Rlog.pii(r14, r15)     // Catch:{ RemoteException -> 0x0253 }
            r13.append(r0)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r0 = r13.toString()     // Catch:{ RemoteException -> 0x0253 }
            android.telephony.Rlog.d(r14, r0)     // Catch:{ RemoteException -> 0x0253 }
        L_0x011e:
            boolean r0 = android.text.TextUtils.isEmpty(r15)     // Catch:{ RemoteException -> 0x0253 }
            if (r0 != 0) goto L_0x0157
            java.lang.String r0 = "8988605"
            boolean r0 = r15.startsWith(r0)     // Catch:{ RemoteException -> 0x0253 }
            if (r0 == 0) goto L_0x013b
            boolean r0 = DEBUG     // Catch:{ RemoteException -> 0x0253 }
            if (r0 == 0) goto L_0x0135
            java.lang.String r0 = "Replace mccmnc for APTG roaming case"
            android.telephony.Rlog.d(r14, r0)     // Catch:{ RemoteException -> 0x0253 }
        L_0x0135:
            r0 = 466(0x1d2, float:6.53E-43)
            r4 = 5
            r5 = r4
            r4 = r0
            goto L_0x0157
        L_0x013b:
            java.lang.String r0 = "898603"
            boolean r0 = r15.startsWith(r0)     // Catch:{ RemoteException -> 0x0253 }
            if (r0 != 0) goto L_0x014b
            java.lang.String r0 = "898611"
            boolean r0 = r15.startsWith(r0)     // Catch:{ RemoteException -> 0x0253 }
            if (r0 == 0) goto L_0x0157
        L_0x014b:
            r4 = 460(0x1cc, float:6.45E-43)
            r5 = 3
            boolean r0 = DEBUG     // Catch:{ RemoteException -> 0x0253 }
            if (r0 == 0) goto L_0x0157
            java.lang.String r0 = "Replace mccmnc for CT roaming case"
            android.telephony.Rlog.d(r14, r0)     // Catch:{ RemoteException -> 0x0253 }
        L_0x0157:
            android.content.res.Resources r0 = r22.getResources()     // Catch:{ RemoteException -> 0x0253 }
            android.content.res.Configuration r13 = r0.getConfiguration()     // Catch:{ RemoteException -> 0x0253 }
            r13.mcc = r4     // Catch:{ RemoteException -> 0x0253 }
            if (r5 != 0) goto L_0x0169
            r18 = 65535(0xffff, float:9.1834E-41)
            r1 = r18
            goto L_0x016a
        L_0x0169:
            r1 = r5
        L_0x016a:
            r13.mnc = r1     // Catch:{ RemoteException -> 0x0253 }
            r1 = 0
            r0.updateConfiguration(r13, r1)     // Catch:{ RemoteException -> 0x0253 }
            boolean r1 = com.mediatek.ims.OperatorUtils.isCTVolteDisabled(r10)     // Catch:{ RemoteException -> 0x0253 }
            if (r1 == 0) goto L_0x017c
            r1 = 0
            r18 = 0
            r19 = 0
            goto L_0x01a9
        L_0x017c:
            r1 = 17891497(0x11100a9, float:2.6632768E-38)
            boolean r1 = r0.getBoolean(r1)     // Catch:{ RemoteException -> 0x0253 }
            int r1 = mapFeatureValue(r1)     // Catch:{ RemoteException -> 0x0253 }
            r18 = r1
            r1 = 17891498(0x11100aa, float:2.663277E-38)
            boolean r1 = r0.getBoolean(r1)     // Catch:{ RemoteException -> 0x0253 }
            int r1 = mapFeatureValue(r1)     // Catch:{ RemoteException -> 0x0253 }
            r19 = r1
            r1 = 17891499(0x11100ab, float:2.6632773E-38)
            boolean r1 = r0.getBoolean(r1)     // Catch:{ RemoteException -> 0x0253 }
            int r1 = mapFeatureValue(r1)     // Catch:{ RemoteException -> 0x0253 }
            r20 = r19
            r19 = r1
            r1 = r18
            r18 = r20
        L_0x01a9:
            r0 = r18
            r12 = r19
            goto L_0x01f3
        L_0x01ae:
            r0 = move-exception
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x0253 }
            r1.<init>()     // Catch:{ RemoteException -> 0x0253 }
            r1.append(r15)     // Catch:{ RemoteException -> 0x0253 }
            r1.append(r6)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r1 = r1.toString()     // Catch:{ RemoteException -> 0x0253 }
            android.telephony.Rlog.w(r14, r1)     // Catch:{ RemoteException -> 0x0253 }
            return
        L_0x01c2:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x0253 }
            r0.<init>()     // Catch:{ RemoteException -> 0x0253 }
            r0.append(r15)     // Catch:{ RemoteException -> 0x0253 }
            r0.append(r6)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r0 = r0.toString()     // Catch:{ RemoteException -> 0x0253 }
            android.telephony.Rlog.w(r14, r0)     // Catch:{ RemoteException -> 0x0253 }
            return
        L_0x01d5:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x0253 }
            r0.<init>()     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r1 = "Found test SIM on phone "
            r0.append(r1)     // Catch:{ RemoteException -> 0x0253 }
            r0.append(r10)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r0 = r0.toString()     // Catch:{ RemoteException -> 0x0253 }
            android.telephony.Rlog.w(r14, r0)     // Catch:{ RemoteException -> 0x0253 }
            r1 = 1
            r18 = 1
            r0 = 1
            r19 = r0
            r0 = r18
            r12 = r19
        L_0x01f3:
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x0253 }
            r13.<init>()     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r15 = "Set res capability: volte = "
            r13.append(r15)     // Catch:{ RemoteException -> 0x0253 }
            r13.append(r1)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r15 = ", vilte = "
            r13.append(r15)     // Catch:{ RemoteException -> 0x0253 }
            r13.append(r0)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r15 = ", wfc = "
            r13.append(r15)     // Catch:{ RemoteException -> 0x0253 }
            r13.append(r12)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r13 = r13.toString()     // Catch:{ RemoteException -> 0x0253 }
            android.telephony.Rlog.d(r14, r13)     // Catch:{ RemoteException -> 0x0253 }
            r13 = 0
            r11.setImsResCapability(r13, r1)     // Catch:{ RemoteException -> 0x0253 }
            r13 = 1
            r11.setImsResCapability(r13, r0)     // Catch:{ RemoteException -> 0x0253 }
            r13 = 2
            r11.setImsResCapability(r13, r12)     // Catch:{ RemoteException -> 0x0253 }
        L_0x0223:
            android.content.Intent r0 = new android.content.Intent     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r1 = "com.mediatek.ims.config.action.DYNAMIC_IMS_SWITCH_COMPLETE"
            r0.<init>(r1)     // Catch:{ RemoteException -> 0x0253 }
            r0.putExtra(r9, r10)     // Catch:{ RemoteException -> 0x0253 }
            r0.putExtra(r7, r8)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r1 = "android.permission.READ_PHONE_STATE"
            r2.sendBroadcast(r0, r1)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x0253 }
            r1.<init>()     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r7 = "DYNAMIC_IMS_SWITCH_COMPLETE phoneId:"
            r1.append(r7)     // Catch:{ RemoteException -> 0x0253 }
            r1.append(r10)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r7 = ", simState:"
            r1.append(r7)     // Catch:{ RemoteException -> 0x0253 }
            r1.append(r8)     // Catch:{ RemoteException -> 0x0253 }
            java.lang.String r1 = r1.toString()     // Catch:{ RemoteException -> 0x0253 }
            android.telephony.Rlog.d(r14, r1)     // Catch:{ RemoteException -> 0x0253 }
            goto L_0x0268
        L_0x0253:
            r0 = move-exception
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r7 = "SetImsCapability fail: "
            r1.append(r7)
            r1.append(r0)
            java.lang.String r1 = r1.toString()
            android.telephony.Rlog.e(r14, r1)
        L_0x0268:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ims.ImsConfigManager.updateImsResrouceCapability(android.content.Context, android.content.Intent):void");
    }

    private static int mapFeatureValue(boolean value) {
        if (value) {
            return 1;
        }
        return 0;
    }

    private static boolean isTestSim(int phoneId) {
        switch (phoneId) {
            case 0:
                return "1".equals(SystemProperties.get("vendor.gsm.sim.ril.testsim", "0"));
            case 1:
                return "1".equals(SystemProperties.get("vendor.gsm.sim.ril.testsim.2", "0"));
            case 2:
                return "1".equals(SystemProperties.get("vendor.gsm.sim.ril.testsim.3", "0"));
            case 3:
                return "1".equals(SystemProperties.get("vendor.gsm.sim.ril.testsim.4", "0"));
            default:
                return false;
        }
    }

    private int getMainCapabilityPhoneId() {
        int phoneId = SystemProperties.getInt("persist.vendor.radio.simswitch", 1) - 1;
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            return -1;
        }
        return phoneId;
    }

    private void updateImsServiceConfig(Context context, int phoneId) {
        if (this.mImsManagerOemPlugin == null) {
            this.mImsManagerOemPlugin = ExtensionFactory.makeOemPluginFactory(context).makeImsManagerPlugin(context);
        }
        if (ImsCommonUtil.supportMims()) {
            this.mImsManagerOemPlugin.updateImsServiceConfig(context, phoneId);
        } else if (phoneId == getMainCapabilityPhoneId()) {
            this.mImsManagerOemPlugin.updateImsServiceConfig(context, phoneId);
        } else if (DEBUG) {
            Rlog.d(LOG_TAG, "Do not update if phoneId is not main capability");
        }
    }

    /* access modifiers changed from: private */
    public void setVdpProvision(int phoneId, int value) {
        Log.d(LOG_TAG, "phoneId:" + phoneId + " ,value:" + value);
        if (phoneId >= 0) {
            try {
                getEx(phoneId).setModemImsCfg(new String[]{"provision_setting_vdp"}, new String[]{Integer.toString(value)}, phoneId);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "setModemImsCfg fail: " + e);
            }
        }
    }
}
