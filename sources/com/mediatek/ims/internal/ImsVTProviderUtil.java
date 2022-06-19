package com.mediatek.ims.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.os.SomeArgs;
import com.mediatek.ims.ImsCommonUtil;
import com.mediatek.ims.ImsService;
import com.mediatek.ims.common.SubscriptionManagerHelper;
import com.mediatek.ims.config.ImsConfigContract;
import com.mediatek.ims.internal.ImsVTUsageManager;
import com.mediatek.ims.plugin.ExtensionFactory;
import com.mediatek.ims.plugin.ImsCallOemPlugin;
import com.mediatek.ims.plugin.impl.ImsCallPluginBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class ImsVTProviderUtil {
    public static final int CALL_RAT_LTE = 0;
    public static final int CALL_RAT_NR = 2;
    public static final int CALL_RAT_WIFI = 1;
    private static final String EXTRA_PHONE_ID = "phone_id";
    public static final int HIDE_ME_TYPE_DISABLE = 1;
    public static final int HIDE_ME_TYPE_FREEZE = 2;
    public static final int HIDE_ME_TYPE_NONE = 0;
    public static final int HIDE_ME_TYPE_PICTURE = 3;
    public static final int HIDE_YOU_TYPE_DISABLE = 0;
    public static final int HIDE_YOU_TYPE_ENABLE = 1;
    private static final int MSG_BIND = 2;
    private static final int MSG_INIT_REFVTP = 1;
    private static final int MSG_REINIT_REFVTP = 7;
    private static final int MSG_RESET_WRAPPER = 6;
    private static final int MSG_SETUIMODE = 3;
    private static final int MSG_SWITCH_FEATURE = 4;
    private static final int MSG_SWITCH_ROAMING = 5;
    private static final int MSG_TRIGGER_OPERATOR_ID = 8;
    public static final String PROPERTY_MAX_DRAM_SIZE = "ro.vendor.mtk_config_max_dram_size";
    private static final String PROPERTY_NO_CAMERA_MODE = "persist.vendor.vt.no_camera_mode";
    public static final String PROPERTY_RIL_ICCID_SIM = "vendor.ril.iccid.sim";
    public static final String PROPERTY_TEL_LOG = "persist.log.tag.tel_dbg";
    private static final String PROPERTY_VILTE_ENABLE = "persist.vendor.mtk.vilte.enable";
    private static final String PROPERTY_VIWIFI_ENABLE = "persist.vendor.mtk.viwifi.enable";
    /* access modifiers changed from: private */
    public static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static final String TAG = "ImsVT Util";
    public static final int TAG_VILTE_MOBILE = -16777216;
    public static final int TAG_VILTE_WIFI = -15728640;
    public static final int TURN_OFF_CAMERA = -1;
    public static final int UI_MODE_BG = 1;
    public static final int UI_MODE_CAMERA_STREAM = 5;
    public static final int UI_MODE_DESTROY = 65536;
    public static final int UI_MODE_FG = 0;
    public static final int UI_MODE_FULL_SCREEN = 2;
    public static final int UI_MODE_IMAGE_STREAM = 4;
    public static final int UI_MODE_NORMAL_SCREEN = 3;
    public static final int UI_MODE_RESET = 6;
    public static final int UI_MODE_UNCHANGED = -1;
    private static final String VILTE_SUPPORT = "persist.vendor.vilte_support";
    private static final String VIWIFI_SUPPORT = "persist.vendor.viwifi_support";
    public static final int VT_SIM_ID_ABSENT = -1;
    private static ImsVTProviderUtil mInstance = getInstance();
    public static boolean sIsNoCameraMode;
    /* access modifiers changed from: private */
    public ConnectivityManager mConnectivityManager;
    public Context mContext;
    private Map<String, Object> mDataUsageById = new HashMap();
    private FeatureValueReceiver mFeatureValueReceiver;
    private NetworkAvailableCallback mNetworkAvailableCallback;
    private ImsVTMessagePacker mPacker = new ImsVTMessagePacker();
    private Map<Integer, PhoneStateListener> mPhoneServicesStateListeners;
    private Map<String, Object> mProviderById = new ConcurrentHashMap();
    private Handler mProviderHandler;
    protected HandlerThread mProviderHandlerThread;
    private int[] mSimAppState;
    private int[] mSimCardState;
    /* access modifiers changed from: private */
    public ConditionVariable[] mSimReadyVariable;
    private SimStateReceiver mSimStateReceiver;
    private SubscriptionManager mSubscriptionManager;
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionsChangedlistener;
    private TelephonyManager mTelephonyManager;

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROPERTY_NO_CAMERA_MODE, 0) == 1) {
            z = true;
        }
        sIsNoCameraMode = z;
    }

    public static class Size {
        public int height;
        public int width;

        public Size(int w, int h) {
            this.width = w;
            this.height = h;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Size)) {
                return false;
            }
            Size s = (Size) obj;
            if (this.width == s.width && this.height == s.height) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (this.width * 32713) + this.height;
        }
    }

    public class FeatureValueReceiver extends BroadcastReceiver {
        private ImsVTProviderUtil mOwner;
        private ArrayList<Boolean> mViLTEValue = new ArrayList<>();
        private ArrayList<Boolean> mViWifiValue = new ArrayList<>();

        public FeatureValueReceiver() {
            int slotCount = ImsVTProviderUtil.SIM_NUM > 1 ? ImsVTProviderUtil.SIM_NUM : 2;
            for (int i = 0; i < slotCount; i++) {
                int propValueBit = ImsCommonUtil.supportMims() ? i : 0;
                boolean z = false;
                boolean enable = (SystemProperties.getInt("persist.vendor.mtk.vilte.enable", 0) & (1 << propValueBit)) > 0;
                Log.d(ImsVTProviderUtil.TAG, "Getprop [persist.vendor.mtk.vilte.enable][" + i + "]=" + enable);
                this.mViLTEValue.add(Boolean.valueOf(enable));
                if ((SystemProperties.getInt("persist.vendor.mtk.viwifi.enable", 0) & (1 << propValueBit)) > 0) {
                    z = true;
                }
                boolean enable2 = z;
                Log.d(ImsVTProviderUtil.TAG, "Getprop [persist.vendor.mtk.viwifi.enable][" + i + "]=" + enable2);
                this.mViWifiValue.add(Boolean.valueOf(enable2));
            }
        }

        public void setOwner(ImsVTProviderUtil owner) {
            this.mOwner = owner;
        }

        public boolean getInitViLTEValue(int phondId) {
            return this.mViLTEValue.get(phondId).booleanValue();
        }

        public boolean getInitViWifiValue(int phondId) {
            return this.mViWifiValue.get(phondId).booleanValue();
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals(ImsConfigContract.ACTION_IMS_FEATURE_CHANGED)) {
                int feature = intent.getIntExtra(ImsConfigContract.EXTRA_CHANGED_ITEM, -1);
                int phoneId = intent.getIntExtra("phone_id", -1);
                int status = intent.getIntExtra("value", -1);
                if (phoneId < 0) {
                    Log.d(ImsVTProviderUtil.TAG, "ignore it for invalid SIM id");
                } else if (feature == 1) {
                    Log.d(ImsVTProviderUtil.TAG, "onRecevied feature changed phoneId: " + phoneId + ", feature: " + feature + ", status: " + status);
                    if (status == 0) {
                        this.mViLTEValue.set(phoneId, Boolean.FALSE);
                    } else if (status == 1) {
                        this.mViLTEValue.set(phoneId, Boolean.TRUE);
                    }
                    this.mOwner.switchFeature(phoneId, -16777216, this.mViLTEValue.get(phoneId).booleanValue());
                } else if (feature == 3) {
                    Log.d(ImsVTProviderUtil.TAG, "onRecevied feature changed phoneId: " + phoneId + ", feature: " + feature + ", status: " + status);
                    if (status == 0) {
                        this.mViWifiValue.set(phoneId, Boolean.FALSE);
                    } else if (status == 1) {
                        this.mViWifiValue.set(phoneId, Boolean.TRUE);
                    }
                    this.mOwner.switchFeature(phoneId, ImsVTProviderUtil.TAG_VILTE_WIFI, this.mViWifiValue.get(phoneId).booleanValue());
                }
            }
        }
    }

    public class VTPhoneStateListener extends PhoneStateListener {
        private Context mContext;
        private ImsVTProviderUtil mOwner;

        public VTPhoneStateListener() {
        }

        public void setOwner(ImsVTProviderUtil owner) {
            this.mOwner = owner;
        }

        public void setContext(Context context) {
            this.mContext = context;
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            if (serviceState == null) {
                Log.d(ImsVTProviderUtil.TAG, "[onServiceStateChanged] " + this.mSubId + ": serviceState is null");
            } else if (!SubscriptionManager.isValidPhoneId(SubscriptionManager.getPhoneId(this.mSubId.intValue()))) {
                Log.d(ImsVTProviderUtil.TAG, "[onServiceStateChanged] : phondId is invalid");
            } else {
                boolean dataRoaming = serviceState.getDataRoaming();
                boolean voiceRoaming = serviceState.getVoiceRoaming();
                TelephonyManager.getDefault().getSimState();
            }
        }
    }

    private class SimStateReceiver extends BroadcastReceiver {
        private ImsVTProviderUtil mOwner;

        private SimStateReceiver() {
        }

        public void setOwner(ImsVTProviderUtil owner) {
            this.mOwner = owner;
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int slotId = intent.getIntExtra("phone", -1);
            int simState = intent.getIntExtra("android.telephony.extra.SIM_STATE", 0);
            if (slotId != -1) {
                if (action.equals("android.telephony.action.SIM_CARD_STATE_CHANGED")) {
                    Log.d(ImsVTProviderUtil.TAG, "Received ACTION_SIM_CARD_STATE_CHANGED, slotId:" + slotId + ", simState:" + simState);
                    ImsVTProviderUtil.this.setSimCardState(slotId, simState);
                } else if (action.equals("android.telephony.action.SIM_APPLICATION_STATE_CHANGED")) {
                    Log.d(ImsVTProviderUtil.TAG, "Received ACTION_SIM_APPLICATION_STATE_CHANGED, slotId:" + slotId + ", simState:" + simState);
                    ImsVTProviderUtil.this.setSimAppState(slotId, simState);
                }
                if (ImsVTProviderUtil.this.isSimStateStable(slotId)) {
                    ImsVTProviderUtil.this.mSimReadyVariable[slotId].open();
                }
            }
        }
    }

    private class NetworkAvailableCallback extends ConnectivityManager.NetworkCallback {
        private NetworkAvailableCallback() {
        }

        public void onAvailable(Network network) {
            Log.d(ImsVTProviderUtil.TAG, "NetworkAvailableCallback.onAvailable: network=" + network);
            if (network != null) {
                LinkProperties linkProp = ImsVTProviderUtil.this.mConnectivityManager.getLinkProperties(network);
                if (linkProp != null) {
                    String ifName = linkProp.getInterfaceName();
                    String netId = network.toString();
                    if (netId == null) {
                        Log.d(ImsVTProviderUtil.TAG, "NetworkAvailableCallback.onAvailable: network id is null");
                        return;
                    }
                    Log.d(ImsVTProviderUtil.TAG, "NetworkAvailableCallback.onAvailable: (network_id, if_name) = (" + netId + ", " + ifName + ")");
                    if (ImsVTProviderUtil.isVideoCallOnByPlatform()) {
                        ImsVTProvider.nUpdateNetworkTable(true, Integer.valueOf(netId).intValue(), ifName);
                        return;
                    }
                    return;
                }
                Log.w(ImsVTProviderUtil.TAG, "NetworkAvailableCallback.onAvailable: linkProp = null");
                return;
            }
            Log.w(ImsVTProviderUtil.TAG, "NetworkAvailableCallback.onAvailable: network = null");
        }

        public void onLost(Network network) {
            Log.d(ImsVTProviderUtil.TAG, "NetworkAvailableCallback.onLost: network=" + network);
            if (network != null) {
                String netId = network.toString();
                if (netId == null) {
                    Log.d(ImsVTProviderUtil.TAG, "NetworkAvailableCallback.onLost: network id is null");
                } else if (ImsVTProviderUtil.isVideoCallOnByPlatform()) {
                    ImsVTProvider.nUpdateNetworkTable(false, Integer.valueOf(netId).intValue(), (String) null);
                }
            } else {
                Log.w(ImsVTProviderUtil.TAG, "NetworkAvailableCallback.onLost: network = null");
            }
        }
    }

    public class ImsVTMessagePacker {
        public ImsVTMessagePacker() {
        }

        public String packFromVdoProfile(VideoProfile videoProfile) {
            StringBuilder flattened = new StringBuilder();
            flattened.append("mVideoState");
            flattened.append("=");
            flattened.append("" + videoProfile.getVideoState());
            flattened.append(";");
            flattened.append("mQuality");
            flattened.append("=");
            flattened.append("" + videoProfile.getQuality());
            flattened.append(";");
            flattened.deleteCharAt(flattened.length() + -1);
            Log.d(ImsVTProviderUtil.TAG, "[packFromVdoProfile] profile = " + flattened.toString());
            return flattened.toString();
        }

        public VideoProfile unPackToVdoProfile(String flattened) {
            Log.d(ImsVTProviderUtil.TAG, "[unPackToVdoProfile] flattened = " + flattened);
            StringTokenizer tokenizer = new StringTokenizer(flattened, ";");
            int state = 3;
            int qty = 4;
            while (tokenizer.hasMoreElements()) {
                String kv = tokenizer.nextToken();
                int pos = kv.indexOf(61);
                if (pos != -1) {
                    String k = kv.substring(0, pos);
                    String v = kv.substring(pos + 1);
                    Log.d(ImsVTProviderUtil.TAG, "[unPackToVdoProfile] k = " + k + ", v = " + v);
                    if (k.equals("mVideoState")) {
                        state = Integer.valueOf(v).intValue();
                    } else if (k.equals("mQuality")) {
                        qty = Integer.valueOf(v).intValue();
                    }
                }
            }
            Log.d(ImsVTProviderUtil.TAG, "[unPackToVdoProfile] state = " + state + ", qty = " + qty);
            return new VideoProfile(state, qty);
        }
    }

    private ImsVTProviderUtil() {
        int i = SIM_NUM;
        this.mSimCardState = new int[i];
        this.mSimAppState = new int[i];
        this.mSimReadyVariable = new ConditionVariable[i];
        this.mPhoneServicesStateListeners = new ConcurrentHashMap();
        this.mSubscriptionsChangedlistener = new SubscriptionManager.OnSubscriptionsChangedListener() {
            public void onSubscriptionsChanged() {
                ImsVTProviderUtil.this.updateServiceStateListeners();
            }
        };
        HandlerThread handlerThread = new HandlerThread("ProviderHandlerThread");
        this.mProviderHandlerThread = handlerThread;
        handlerThread.start();
        this.mProviderHandler = new Handler(this.mProviderHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        ImsVTProviderUtil.this.setContextAndInitRefVTPInternal((Context) msg.obj);
                        return;
                    case 2:
                        SomeArgs args = (SomeArgs) msg.obj;
                        try {
                            int cid = ((Integer) args.arg2).intValue();
                            int pid = ((Integer) args.arg3).intValue();
                            ImsVTProviderUtil.this.bindInternal((ImsVTProvider) args.arg1, cid, pid);
                            return;
                        } finally {
                            args.recycle();
                        }
                    case 3:
                        SomeArgs args2 = (SomeArgs) msg.obj;
                        try {
                            int mode = ((Integer) args2.arg2).intValue();
                            ImsVTProviderUtil.this.setUIModeInternal((ImsVTProvider) args2.arg1, mode);
                            return;
                        } finally {
                            args2.recycle();
                        }
                    case 4:
                        SomeArgs args3 = (SomeArgs) msg.obj;
                        try {
                            ImsVTProviderUtil.this.switchFeatureInternal(((Integer) args3.arg1).intValue(), ((Integer) args3.arg2).intValue(), ((Boolean) args3.arg3).booleanValue());
                            return;
                        } finally {
                            args3.recycle();
                        }
                    case 5:
                        SomeArgs args4 = (SomeArgs) msg.obj;
                        try {
                            ImsVTProviderUtil.this.switchRoamingInternal(((Integer) args4.arg1).intValue(), ((Boolean) args4.arg2).booleanValue());
                            return;
                        } finally {
                            args4.recycle();
                        }
                    case 6:
                        SomeArgs args5 = (SomeArgs) msg.obj;
                        try {
                            ImsVTProviderUtil.this.resetWrapperInternal((ImsVTProvider) args5.arg1);
                            return;
                        } finally {
                            args5.recycle();
                        }
                    case 7:
                        if (ImsVTProviderUtil.isVideoCallOnByPlatform()) {
                            Log.d(ImsVTProviderUtil.TAG, "reInitRefVTP, ViLTE on, do natvie ReInit");
                            ImsVTProvider.nInitRefVTP();
                            ImsVTProvider.nTagSocketWithUid(1001);
                            return;
                        }
                        return;
                    case 8:
                        ImsVTProvider.nTriggerGetOperatorId();
                        return;
                    default:
                        return;
                }
            }
        };
        FeatureValueReceiver featureValueReceiver = new FeatureValueReceiver();
        this.mFeatureValueReceiver = featureValueReceiver;
        featureValueReceiver.setOwner(this);
        for (int i2 = 0; i2 < SIM_NUM; i2++) {
            this.mSimCardState[i2] = getImsExtCallUtil().getSimCardState(i2);
            this.mSimAppState[i2] = getImsExtCallUtil().getSimApplicationState(i2);
            this.mSimReadyVariable[i2] = new ConditionVariable();
        }
        SimStateReceiver simStateReceiver = new SimStateReceiver();
        this.mSimStateReceiver = simStateReceiver;
        simStateReceiver.setOwner(this);
        this.mNetworkAvailableCallback = new NetworkAvailableCallback();
    }

    public static synchronized ImsVTProviderUtil getInstance() {
        ImsVTProviderUtil imsVTProviderUtil;
        synchronized (ImsVTProviderUtil.class) {
            if (mInstance == null) {
                mInstance = new ImsVTProviderUtil();
            }
            imsVTProviderUtil = mInstance;
        }
        return imsVTProviderUtil;
    }

    public void notifyMultiSimConfigChanged(int activeModemCount) {
        int prevActiveModemCount = this.mSimCardState.length;
        Log.d(TAG, "notifyMultiSimConfigChanged, phone:" + prevActiveModemCount + "->" + activeModemCount);
        if (prevActiveModemCount != activeModemCount && prevActiveModemCount <= activeModemCount) {
            this.mSimCardState = Arrays.copyOf(this.mSimCardState, activeModemCount);
            this.mSimAppState = Arrays.copyOf(this.mSimAppState, activeModemCount);
            this.mSimReadyVariable = (ConditionVariable[]) Arrays.copyOf(this.mSimReadyVariable, activeModemCount);
            for (int i = prevActiveModemCount; i < activeModemCount; i++) {
                this.mSimCardState[i] = getImsExtCallUtil().getSimCardState(i);
                this.mSimAppState[i] = getImsExtCallUtil().getSimApplicationState(i);
                this.mSimReadyVariable[i] = new ConditionVariable();
            }
        }
    }

    public ImsCallPluginBase getImsExtCallUtil() {
        return ExtensionFactory.makeExtensionPluginFactory(this.mContext).makeImsCallPlugin(this.mContext);
    }

    public ImsCallOemPlugin getImsOemCallUtil() {
        return ExtensionFactory.makeOemPluginFactory(this.mContext).makeImsCallPlugin(this.mContext);
    }

    /* access modifiers changed from: private */
    public void updateServiceStateListeners() {
        if (this.mSubscriptionManager == null) {
            Log.d(TAG, "[updateServiceStateListeners] Unexpected error, mSubscriptionManager=null");
        } else if (this.mTelephonyManager == null) {
            Log.d(TAG, "[updateServiceStateListeners] Unexpected error, mTelephonyManager=null");
        } else {
            HashSet<Integer> unUsedSubscriptions = new HashSet<>(this.mPhoneServicesStateListeners.keySet());
            List<SubscriptionInfo> slist = this.mSubscriptionManager.getActiveSubscriptionInfoList();
            if (slist != null) {
                for (SubscriptionInfo subInfoRecord : slist) {
                    int subId = subInfoRecord.getSubscriptionId();
                    if (this.mPhoneServicesStateListeners.get(Integer.valueOf(subId)) == null) {
                        Log.d(TAG, "[updateServiceStateListeners] create ServicesStateListener for " + subId);
                        VTPhoneStateListener listener = new VTPhoneStateListener();
                        this.mTelephonyManager.listen(listener, 1);
                        this.mPhoneServicesStateListeners.put(Integer.valueOf(subId), listener);
                    } else {
                        unUsedSubscriptions.remove(Integer.valueOf(subId));
                    }
                }
            }
            Iterator<Integer> it = unUsedSubscriptions.iterator();
            while (it.hasNext()) {
                Integer key = it.next();
                Log.d(TAG, "[updateServiceStateListeners] remove unused ServicesStateListener for " + key);
                this.mTelephonyManager.listen(this.mPhoneServicesStateListeners.get(key), 0);
                this.mPhoneServicesStateListeners.remove(key);
            }
        }
    }

    public String packFromVdoProfile(VideoProfile VideoProfile) {
        return this.mPacker.packFromVdoProfile(VideoProfile);
    }

    public VideoProfile unPackToVdoProfile(String flattened) {
        return this.mPacker.unPackToVdoProfile(flattened);
    }

    public void usageSet(int Id, ImsVTUsageManager.ImsVTUsage usage) {
        Log.d(TAG, "[usageSet][id =" + Id + "]" + usage.toString());
        Map<String, Object> map = this.mDataUsageById;
        StringBuilder sb = new StringBuilder();
        sb.append("");
        sb.append(Id);
        map.put(sb.toString(), new ImsVTUsageManager.ImsVTUsage("Record", usage));
    }

    public ImsVTUsageManager.ImsVTUsage usageGet(int Id) {
        Map<String, Object> map = this.mDataUsageById;
        ImsVTUsageManager.ImsVTUsage usage = (ImsVTUsageManager.ImsVTUsage) map.get("" + Id);
        if (usage == null) {
            return new ImsVTUsageManager.ImsVTUsage("Dummy");
        }
        Log.d(TAG, "[usageGet][id =" + Id + "]" + usage.toString());
        return usage;
    }

    public void recordAdd(int Id, ImsVTProvider p) {
        Log.d(TAG, "recordAdd id = " + Id + ", size = " + recordSize());
        Map<String, Object> map = this.mProviderById;
        StringBuilder sb = new StringBuilder();
        sb.append("");
        sb.append(Id);
        map.put(sb.toString(), p);
    }

    public void recordRemove(int Id) {
        Log.d(TAG, "recordRemove id = " + Id + ", size = " + recordSize());
        Map<String, Object> map = this.mProviderById;
        StringBuilder sb = new StringBuilder();
        sb.append("");
        sb.append(Id);
        map.remove(sb.toString());
    }

    public void recordRemoveAll() {
        Log.d(TAG, "recordRemoveAll, size = " + recordSize());
        this.mProviderById.clear();
    }

    public ImsVTProvider recordGet(int Id) {
        if (isTelephonyLogEnable()) {
            Log.d(TAG, "recordGet id = " + Id + ", size = " + recordSize());
        }
        Map<String, Object> map = this.mProviderById;
        return (ImsVTProvider) map.get("" + Id);
    }

    public int recordPopId() {
        if (this.mProviderById.size() == 0) {
            return ImsVTProvider.VT_PROVIDER_INVALIDE_ID;
        }
        Iterator<Object> it = this.mProviderById.values().iterator();
        if (it.hasNext()) {
            return ((ImsVTProvider) it.next()).getId();
        }
        return ImsVTProvider.VT_PROVIDER_INVALIDE_ID;
    }

    public boolean recordContain(int Id) {
        return this.mProviderById.containsKey(Integer.valueOf(Id));
    }

    public int recordSize() {
        return this.mProviderById.size();
    }

    /* Debug info: failed to restart local var, previous not found, register: 1 */
    public synchronized void setSimCardState(int simId, int state) {
        if (simId >= 0) {
            if (simId < SIM_NUM) {
                this.mSimCardState[simId] = state;
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 1 */
    public synchronized int getSimCardState(int simId) {
        if (simId >= 0) {
            if (simId < SIM_NUM) {
                return this.mSimCardState[simId];
            }
        }
        return 1;
    }

    /* Debug info: failed to restart local var, previous not found, register: 1 */
    public synchronized void setSimAppState(int simId, int state) {
        if (simId >= 0) {
            if (simId < SIM_NUM) {
                this.mSimAppState[simId] = state;
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 1 */
    public synchronized int getSimAppState(int simId) {
        if (simId >= 0) {
            if (simId < SIM_NUM) {
                return this.mSimAppState[simId];
            }
        }
        return 1;
    }

    public boolean isSimStateStable(int simId) {
        int cardState = getSimCardState(simId);
        return cardState == 1 || cardState == 11;
    }

    public void waitSimStateStable(int simId) {
        if (!isSimStateStable(simId)) {
            Log.d(TAG, "waitSimStateStable, simId = " + simId);
            this.mSimReadyVariable[simId].close();
            this.mSimReadyVariable[simId].block();
        }
    }

    public void quitAllThread() {
        if (this.mProviderById.size() != 0) {
            for (Object p : this.mProviderById.values()) {
                Log.d(TAG, "quitThread, id = " + ((ImsVTProvider) p).getId());
                ((ImsVTProvider) p).quitThread();
            }
        }
    }

    public void updateCameraUsage(int Id) {
        Log.d(TAG, "updateCameraUsage");
        if (this.mProviderById.size() != 0) {
            for (Object p : this.mProviderById.values()) {
                if (((ImsVTProvider) p).getId() != Id) {
                    ((ImsVTProvider) p).setCameraInternal((String) null);
                }
            }
        }
    }

    public void releaseVTSourceAll() {
        if (this.mProviderById.size() != 0) {
            for (Object p : this.mProviderById.values()) {
                Log.d(TAG, "releaseVTSourceAll, id = " + ((ImsVTProvider) p).getId());
                ((ImsVTProvider) p).mSource.release();
            }
        }
    }

    public boolean isVideoCallOn(int phoneId) {
        return this.mFeatureValueReceiver.getInitViLTEValue(phoneId);
    }

    public boolean isViWifiOn(int phoneId) {
        return this.mFeatureValueReceiver.getInitViWifiValue(phoneId);
    }

    public static boolean isVideoCallOnByPlatform() {
        return SystemProperties.get(VILTE_SUPPORT, "0").equals("1") || SystemProperties.get(VIWIFI_SUPPORT, "0").equals("1");
    }

    public static boolean is512mbProject() {
        return SystemProperties.get(PROPERTY_MAX_DRAM_SIZE, "0x40000000").equals("0x20000000");
    }

    public static boolean isVideoQualityTestMode() {
        int labOp = SystemProperties.getInt("persist.vendor.vt.lab_op_code", 0);
        if (labOp == 1 || labOp == 9) {
            return true;
        }
        return false;
    }

    public static boolean isCameraAvailable() {
        return VTSource.getAllCameraResolutions() != null;
    }

    public static boolean isTelephonyLogEnable() {
        return SystemProperties.get(PROPERTY_TEL_LOG, "0").equals("1");
    }

    public void setContextAndInitRefVTP(Context context) {
        this.mProviderHandler.obtainMessage(1, context).sendToTarget();
    }

    public void reInitRefVTP() {
        this.mProviderHandler.obtainMessage(7).sendToTarget();
        this.mProviderHandler.sendMessageDelayed(this.mProviderHandler.obtainMessage(8), 2000);
    }

    public void bind(ImsVTProvider p, int CallId, int PhoneId) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = p;
        args.arg2 = Integer.valueOf(CallId);
        args.arg3 = Integer.valueOf(PhoneId);
        this.mProviderHandler.obtainMessage(2, args).sendToTarget();
    }

    public void setUIMode(ImsVTProvider p, int mode) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = p;
        args.arg2 = Integer.valueOf(mode);
        this.mProviderHandler.obtainMessage(3, args).sendToTarget();
    }

    public void switchFeature(int phoneId, int feature, boolean isOn) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = Integer.valueOf(phoneId);
        args.arg2 = Integer.valueOf(feature);
        args.arg3 = Boolean.valueOf(isOn);
        this.mProviderHandler.obtainMessage(4, args).sendToTarget();
    }

    public void switchRoaming(int phoneId, boolean isRoaming) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = Integer.valueOf(phoneId);
        args.arg2 = Boolean.valueOf(isRoaming);
        this.mProviderHandler.obtainMessage(5, args).sendToTarget();
    }

    public void resetWrapper(ImsVTProvider p) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = p;
        this.mProviderHandler.obtainMessage(6, args).sendToTarget();
    }

    public void setContextAndInitRefVTPInternal(Context context) {
        Log.d(TAG, "setContextAndInitRefVTPInternal(), context =" + context);
        this.mContext = context;
        if (sIsNoCameraMode) {
            VTDummySource.setContext(context);
        } else {
            VTSource.setContext(context);
        }
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (this.mFeatureValueReceiver != null) {
            Log.d(TAG, "setContextAndInitRefVTP, register FeatureValueReceiver");
            IntentFilter filter = new IntentFilter();
            filter.addAction(ImsConfigContract.ACTION_IMS_FEATURE_CHANGED);
            this.mContext.registerReceiver(this.mFeatureValueReceiver, filter);
        }
        if (this.mSimStateReceiver != null) {
            Log.d(TAG, "setContextAndInitRefVTP, register SimStateReceiver");
            this.mContext.registerReceiver(this.mSimStateReceiver, new IntentFilter("android.telephony.action.SIM_CARD_STATE_CHANGED"));
        }
        SubscriptionManager from = SubscriptionManager.from(this.mContext);
        this.mSubscriptionManager = from;
        from.addOnSubscriptionsChangedListener(this.mSubscriptionsChangedlistener);
        registerNetworkRequestWithCallback(4);
        registerNetworkRequestWithCallback(10);
        if (isVideoCallOnByPlatform()) {
            Log.d(TAG, "setContextAndInitRefVTPInternal(), ViLTE on, do natvie init");
            ImsVTProvider.nInitRefVTP();
            ImsVTProvider.nTagSocketWithUid(1001);
        }
    }

    public void registerNetworkRequestWithCallback(int cap) {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(cap);
        NetworkRequest networkRequest = builder.build();
        Log.d(TAG, "registerNetworkRequestwithCallback(), networkRequest:" + networkRequest);
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        this.mConnectivityManager.registerNetworkCallback(networkRequest, this.mNetworkAvailableCallback);
    }

    public void bindInternal(ImsVTProvider p, int CallId, int PhoneId) {
        if (p == null) {
            Log.d(TAG, "ImsVTProvider == null");
            return;
        }
        Log.d(TAG, "bindInternal(), vtp = " + p + ", id = " + CallId + ", phone id = " + PhoneId);
        if (65536 == p.mMode) {
            Log.d(TAG, "Ignore bind ImsVTProvider because UI_MODE_DESTROY");
            return;
        }
        int id = CallId;
        if (ImsService.getInstance(this.mContext).getModemMultiImsCount() > 1) {
            id = (PhoneId << 16) | CallId;
        }
        if (p.getId() == -10000) {
            int wait_time = 0;
            Log.d(TAG, "bind ImsVTProvider check if exist the same id");
            while (true) {
                if (recordGet(id) == null) {
                    break;
                }
                Log.d(TAG, "bind ImsVTProvider the same id exist, wait ...");
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                }
                wait_time++;
                if (wait_time > 10) {
                    Log.d(TAG, "bind ImsVTProvider the same id exist, break!");
                    break;
                }
            }
            p.setId(id);
            p.setSimId(PhoneId);
            p.mUsager.setInitUsage(new ImsVTUsageManager.ImsVTUsage("Init", usageGet(id)));
            recordAdd(id, p);
            if (sIsNoCameraMode) {
                p.mSource = new VTDummySource();
            } else {
                p.mSource = new VTSource(2, p.getId(), p);
            }
            ImsVTProvider.nInitialization(id, p.getSimId());
            p.mInitComplete = true;
        }
    }

    public void setUIModeInternal(ImsVTProvider p, int mode) {
        if (p != null) {
            p.onSetUIMode(mode);
        }
    }

    public void switchFeatureInternal(int phoneId, int feature, boolean isOn) {
        Log.d(TAG, "switchFeatureInternal, feature = " + feature + "isOn = " + isOn);
        if (this.mProviderById.size() != 0) {
            for (Object p : this.mProviderById.values()) {
                if (phoneId == ((ImsVTProvider) p).getSimId()) {
                    Log.d(TAG, "switchFeatureInternal, id = " + ((ImsVTProvider) p).getId());
                    ((ImsVTProvider) p).onSwitchFeature(feature, isOn);
                }
            }
        }
    }

    public void switchRoamingInternal(int phoneId, boolean isRoaming) {
        Log.d(TAG, "switchRoamingInternal, phoneId = " + phoneId + "isRoaming = " + isRoaming);
        if (this.mProviderById.size() != 0) {
            for (Object p : this.mProviderById.values()) {
                if (phoneId == ((ImsVTProvider) p).getSimId()) {
                    Log.d(TAG, "switchRoamingInternal, id = " + ((ImsVTProvider) p).getId());
                    ((ImsVTProvider) p).onSwitchRoaming(isRoaming);
                }
            }
        }
    }

    public void resetWrapperInternal(ImsVTProvider p) {
        if (p != null) {
            p.onResetWrapper();
        }
    }

    public boolean getBooleanFromCarrierConfig(String key, int simId) {
        PersistableBundle carrierConfig = ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(SubscriptionManagerHelper.getSubIdUsingPhoneId(simId));
        if (carrierConfig == null) {
            return false;
        }
        boolean result = carrierConfig.getBoolean(key);
        Log.d(TAG, "getBooleanFromCarrierConfig() key: " + key + " result: " + result);
        return result;
    }
}
