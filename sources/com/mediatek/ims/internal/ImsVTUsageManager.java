package com.mediatek.ims.internal;

import android.content.Context;
import android.content.Intent;
import android.net.INetworkStatsService;
import android.net.NetworkStats;
import android.os.PersistableBundle;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.mediatek.ims.common.SubscriptionManagerHelper;

public class ImsVTUsageManager {
    static final String TAG = "ImsVT Usage";
    public static final int VILTE_UID = 1001;
    private Context mContext;
    private ImsVTUsage mCurrentUsage = new ImsVTUsage("Current");
    private RegistrantList mDataUsageUpdateRegistrants = new RegistrantList();
    public int mId;
    private ImsVTUsage mInitialUsage;
    private boolean mNeedReportDataUsage = true;
    private ImsVTUsage mPreviousUsage = new ImsVTUsage("Previous");
    protected int mSimId;
    public ImsVTProviderUtil mVTProviderUtil = ImsVTProviderUtil.getInstance();

    public static class ImsVTUsage {
        public static final int STATE_RX = 2;
        public static final int STATE_TX = 1;
        public static final int STATE_TXRX = 3;
        private long mLteRxUsage;
        private long mLteTxUsage;
        private long mLteUsage;
        private String mUsedForName;
        private long mWifiRxUsage;
        private long mWifiTxUsage;
        private long mWifiUsage;

        public ImsVTUsage(String name) {
            this.mUsedForName = name;
            this.mLteUsage = 0;
            this.mLteTxUsage = 0;
            this.mLteRxUsage = 0;
            this.mWifiUsage = 0;
            this.mWifiTxUsage = 0;
            this.mWifiRxUsage = 0;
            Log.d(ImsVTUsageManager.TAG, "[ImsVTUsage]" + toString());
        }

        public ImsVTUsage(String name, long lteTx, long lteRx, long lteTxRx, long wifiTx, long wifiRx, long wifiTxRx) {
            this.mUsedForName = name;
            this.mLteTxUsage = lteTx;
            this.mLteRxUsage = lteRx;
            this.mLteUsage = lteTxRx;
            this.mWifiTxUsage = wifiTx;
            this.mWifiRxUsage = wifiRx;
            this.mWifiUsage = wifiTxRx;
            Log.d(ImsVTUsageManager.TAG, "[ImsVTUsage]" + toString());
        }

        public ImsVTUsage(String name, ImsVTUsage usage) {
            this.mUsedForName = name;
            this.mLteTxUsage = usage.getLteUsage(1);
            this.mLteRxUsage = usage.getLteUsage(2);
            this.mLteUsage = usage.getLteUsage(3);
            this.mWifiTxUsage = usage.getWifiUsage(1);
            this.mWifiRxUsage = usage.getWifiUsage(2);
            this.mWifiUsage = usage.getWifiUsage(3);
            Log.d(ImsVTUsageManager.TAG, "[ImsVTUsage]" + toString());
        }

        public long getLteUsage(int state) {
            if (1 == state) {
                return this.mLteTxUsage;
            }
            if (2 == state) {
                return this.mLteRxUsage;
            }
            if (3 == state) {
                return this.mLteUsage;
            }
            return 0;
        }

        public long getWifiUsage(int state) {
            if (1 == state) {
                return this.mWifiTxUsage;
            }
            if (2 == state) {
                return this.mWifiRxUsage;
            }
            if (3 == state) {
                return this.mWifiUsage;
            }
            return 0;
        }

        public void setLteUsage(int state, long usage) {
            if (1 == state) {
                this.mLteTxUsage = usage;
            } else if (2 == state) {
                this.mLteRxUsage = usage;
            } else if (3 == state) {
                this.mLteUsage = usage;
            }
            Log.d(ImsVTUsageManager.TAG, "[setLTEUsage] state: " + state + ", usage: " + usage);
        }

        public void setWifiUsage(int state, long usage) {
            if (1 == state) {
                this.mWifiTxUsage = usage;
            } else if (2 == state) {
                this.mWifiRxUsage = usage;
            } else if (3 == state) {
                this.mWifiUsage = usage;
            }
            Log.d(ImsVTUsageManager.TAG, "[setWifiUsage] state: " + state + ", usage: " + usage);
        }

        public void setAllUsage(long lteTx, long lteRx, long lteTxRx, long wifiTx, long wifiRx, long wifiTxRx) {
            this.mLteTxUsage = lteTx;
            this.mLteRxUsage = lteRx;
            this.mLteUsage = lteTxRx;
            this.mWifiTxUsage = wifiTx;
            this.mWifiRxUsage = wifiRx;
            this.mWifiUsage = wifiTxRx;
            Log.d(ImsVTUsageManager.TAG, "[setAllUsage]" + toString());
        }

        public void updateFrom(ImsVTUsage usage) {
            this.mLteTxUsage = usage.getLteUsage(1);
            this.mLteRxUsage = usage.getLteUsage(2);
            this.mLteUsage = usage.getLteUsage(3);
            this.mWifiTxUsage = usage.getWifiUsage(1);
            this.mWifiRxUsage = usage.getWifiUsage(2);
            this.mWifiUsage = usage.getWifiUsage(3);
            Log.d(ImsVTUsageManager.TAG, "[updateFrom]" + toString());
        }

        public void subtraction(ImsVTUsage subUsage) {
            this.mLteTxUsage -= subUsage.getLteUsage(1);
            this.mLteRxUsage -= subUsage.getLteUsage(2);
            this.mLteUsage -= subUsage.getLteUsage(3);
            this.mWifiTxUsage -= subUsage.getWifiUsage(1);
            this.mWifiRxUsage -= subUsage.getWifiUsage(2);
            this.mWifiUsage -= subUsage.getWifiUsage(3);
            Log.d(ImsVTUsageManager.TAG, "[subtraction]" + toString());
        }

        public String toString() {
            return "[" + this.mUsedForName + "] lteTx=" + this.mLteTxUsage + ", lteRx=" + this.mLteRxUsage + ", lteTxRx=" + this.mLteUsage + ", wifiTx=" + this.mWifiTxUsage + ", wifiRx=" + this.mWifiRxUsage + ", wifiTxRx=" + this.mWifiUsage;
        }
    }

    public void setId(int id) {
        this.mId = id;
    }

    public void setSimId(int simId) {
        this.mSimId = simId;
    }

    public void setInitUsage(ImsVTUsage initUsage) {
        this.mInitialUsage = initUsage;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public ImsVTUsage requestCallDataUsage() {
        Log.d(TAG, "[ID=" + this.mId + "] [onRequestCallDataUsage] Start");
        if (!canRequestDataUsage()) {
            return null;
        }
        try {
            NetworkStats uidSnapshot = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats")).getDataLayerSnapshotForUid(1001);
            if (uidSnapshot == null) {
                Log.d(TAG, "fail to getDataLayerSnapshotForUid");
                return null;
            }
            updateUsage(getTagUsage(uidSnapshot));
            Log.d(TAG, "[ID=" + this.mId + "] [onRequestCallDataUsage] Finish (VIWIFI usage:" + this.mCurrentUsage.getWifiUsage(3) + ")");
            return this.mCurrentUsage;
        } catch (RemoteException e) {
            Log.d(TAG, "Exception:" + e);
            return null;
        }
    }

    private boolean canRequestDataUsage() {
        Log.d(TAG, "[canRequestDataUsage]");
        if (SystemProperties.get("persist.vendor.vt.data_simulate").equals("1")) {
            return true;
        }
        boolean ignoreDataEnabledChanged = getBooleanCarrierConfig(this.mVTProviderUtil.mContext, "ignore_data_enabled_changed_for_video_calls", SubscriptionManagerHelper.getSubIdUsingPhoneId(this.mSimId));
        if (this.mNeedReportDataUsage && ignoreDataEnabledChanged) {
            Log.d(TAG, "[canRequestDataUsage] set dataUsage as false");
            this.mNeedReportDataUsage = false;
        }
        return this.mNeedReportDataUsage;
    }

    private boolean getBooleanCarrierConfig(Context context, String key, int subId) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        PersistableBundle carrierConfig = null;
        if (configManager != null) {
            carrierConfig = configManager.getConfigForSubId(subId);
        }
        if (carrierConfig != null) {
            return carrierConfig.getBoolean(key);
        }
        return CarrierConfigManager.getDefaultConfig().getBoolean(key);
    }

    private ImsVTUsage getTagUsage(NetworkStats uidSnapshot) {
        Log.i(TAG, "getTagUsage uid:1001");
        long TotalLteTxBytes = 0;
        long TotalLteRxBytes = 0;
        long TotalWifiTxBytes = 0;
        long TotalWifiRxBytes = 0;
        NetworkStats.Entry entry = null;
        int i = this.mId;
        int lte_tag = -16777216 + i;
        int wifi_tag = i + ImsVTProviderUtil.TAG_VILTE_WIFI;
        int j = 0;
        while (j < uidSnapshot.size()) {
            entry = uidSnapshot.getValues(j, entry);
            if (entry.uid == 1001 && entry.tag == lte_tag) {
                Log.i(TAG, "getTaggedSnapshot LTE entry:" + entry.toString());
                TotalLteTxBytes += entry.txBytes;
                TotalLteRxBytes += entry.rxBytes;
            }
            if (entry.uid == 1001 && entry.tag == wifi_tag) {
                Log.i(TAG, "getTaggedSnapshot WiFi entry:" + entry.toString());
                TotalWifiTxBytes += entry.txBytes;
                TotalWifiRxBytes += entry.rxBytes;
            }
            j++;
        }
        Log.i(TAG, "getTaggedSnapshot LTE: Tx=" + Long.toString(TotalLteTxBytes) + ", Rx=" + Long.toString(TotalLteRxBytes) + ", Total=" + Long.toString(TotalLteTxBytes + TotalLteRxBytes));
        Log.i(TAG, "getTaggedSnapshot WiFi: Tx=" + Long.toString(TotalWifiTxBytes) + ", Rx=" + Long.toString(TotalWifiRxBytes) + ", Total=" + Long.toString(TotalWifiTxBytes + TotalWifiRxBytes));
        return new ImsVTUsage("Tag", TotalLteTxBytes, TotalLteRxBytes, TotalLteRxBytes + TotalLteTxBytes, TotalWifiTxBytes, TotalWifiRxBytes, TotalWifiRxBytes + TotalWifiTxBytes);
    }

    private void updateUsage(ImsVTUsage tagUsage) {
        ImsVTUsage imsVTUsage = tagUsage;
        int subId = SubscriptionManagerHelper.getSubIdUsingPhoneId(this.mSimId);
        String subscriberId = ((TelephonyManager) this.mVTProviderUtil.mContext.getSystemService("phone")).getSubscriberId(subId);
        long initWifiTxBytes = this.mInitialUsage.getWifiUsage(1);
        long initWifiRxBytes = this.mInitialUsage.getWifiUsage(2);
        long initWifiTxRxBytes = this.mInitialUsage.getWifiUsage(3);
        long curWifiTxBytes = this.mCurrentUsage.getWifiUsage(1);
        long curWifiRxBytes = this.mCurrentUsage.getWifiUsage(2);
        long curWifiTxRxBytes = this.mCurrentUsage.getWifiUsage(3);
        this.mPreviousUsage.updateFrom(this.mCurrentUsage);
        if (imsVTUsage.getWifiUsage(1) != 0) {
        } else if (imsVTUsage.getWifiUsage(2) == 0) {
            imsVTUsage.setWifiUsage(1, curWifiTxBytes + initWifiTxBytes);
            imsVTUsage.setWifiUsage(2, curWifiRxBytes + initWifiRxBytes);
            imsVTUsage.setWifiUsage(3, curWifiTxRxBytes + initWifiTxRxBytes);
        }
        this.mVTProviderUtil.usageSet(this.mId, imsVTUsage);
        imsVTUsage.subtraction(this.mInitialUsage);
        this.mCurrentUsage.updateFrom(imsVTUsage);
        ImsVTUsage deltaUsage = new ImsVTUsage("Delta", this.mCurrentUsage);
        deltaUsage.subtraction(this.mPreviousUsage);
        Intent VTUsageIntent = new Intent(this.mVTProviderUtil.getImsOemCallUtil().getVTUsageAction());
        VTUsageIntent.putExtra("lterxbytes", deltaUsage.getLteUsage(2));
        VTUsageIntent.putExtra("ltetxbytes", deltaUsage.getLteUsage(1));
        VTUsageIntent.putExtra("wifirxbytes", deltaUsage.getWifiUsage(2));
        VTUsageIntent.putExtra("wifiTxbytes", deltaUsage.getWifiUsage(1));
        VTUsageIntent.putExtra("subscriberId", subscriberId);
        Log.i(TAG, "sendVTusageBroadcast:  lterxbytes=" + Long.toString(deltaUsage.getLteUsage(2)) + ", ltetxbytes=" + Long.toString(deltaUsage.getLteUsage(1)) + ", wifirxbytes=" + Long.toString(deltaUsage.getWifiUsage(2)) + ", wifiTxbytes=" + Long.toString(deltaUsage.getWifiUsage(1)) + ", subId=" + subId);
    }
}
