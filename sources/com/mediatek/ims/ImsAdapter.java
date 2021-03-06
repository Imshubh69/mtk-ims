package com.mediatek.ims;

import android.content.Context;
import android.net.LocalSocket;
import android.os.Build;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import vendor.mediatek.hardware.imsa.V1_0.IImsa;
import vendor.mediatek.hardware.imsa.V1_0.IImsaIndication;

public class ImsAdapter extends Handler {
    protected static final int IIMSA_GET_SERVICE_DELAY_MILLIS = 1000;
    private static final int IMSA_RETRY_SOCKET_TIME = 500;
    private static final int MSG_IMSA_EVENT_PROXY_DEAD = 3;
    private static final int MSG_IMSA_RETRY_CONNECT_SOCKET = 1;
    private static final int MSG_IMSA_RETRY_IMS_ENABLE = 2;
    private static final String MULTI_IMS_SUPPORT = "persist.vendor.mims_support";
    private static final String SOCKET_NAME1 = "volte_imsm";
    private static final String TAG = "[ImsAdapter]";
    /* access modifiers changed from: private */
    public static ImsEventDispatcher mImsEventDispatcher;
    private static boolean mImsServiceUp = false;
    /* access modifiers changed from: private */
    public static ImsAdapter mInstance;
    private static int mNumOfPhones = 0;
    private static boolean[] misImsAdapterEnabled;
    private boolean IS_ENG_BUILD = "eng".equals(Build.TYPE);
    private boolean IS_USERDEBUG_BUILD = "userdebug".equals(Build.TYPE);
    private boolean IS_USER_BUILD = "user".equals(Build.TYPE);
    private Object ImsEnabledThreadLock = new Object();
    private Context mContext;
    private VaSocketIO mIO;
    private ImsaIndication mImsaIndication = new ImsaIndication();
    private volatile IImsa mImsaProxy = null;
    /* access modifiers changed from: private */
    public final AtomicLong mImsaProxyCookie = new AtomicLong(0);
    private final ImsaProxyDeathRecipient mImsaProxyDeathRecipient = new ImsaProxyDeathRecipient();

    public static class VaEvent {
        public static final int DEFAULT_MAX_DATA_LENGTH = 40960;
        private byte[] data;
        private int data_len;
        private int event_max_data_len;
        private int mPhoneId;
        private int read_offset;
        private int request_id;

        public VaEvent(int phoneId, int rid) {
            this(phoneId, rid, DEFAULT_MAX_DATA_LENGTH);
        }

        public VaEvent(int phoneId, int rid, int length) {
            this.mPhoneId = -1;
            this.event_max_data_len = DEFAULT_MAX_DATA_LENGTH;
            this.mPhoneId = phoneId;
            this.request_id = rid;
            this.event_max_data_len = length;
            this.data = new byte[length];
            this.data_len = 0;
            this.read_offset = 0;
        }

        public int putInt(int value) {
            if (this.data_len > this.event_max_data_len - 4) {
                return -1;
            }
            synchronized (this) {
                for (int i = 0; i < 4; i++) {
                    byte[] bArr = this.data;
                    int i2 = this.data_len;
                    bArr[i2] = (byte) ((value >> (i * 8)) & 255);
                    this.data_len = i2 + 1;
                }
            }
            return 0;
        }

        public int putShort(int value) {
            if (this.data_len > this.event_max_data_len - 2) {
                return -1;
            }
            synchronized (this) {
                for (int i = 0; i < 2; i++) {
                    byte[] bArr = this.data;
                    int i2 = this.data_len;
                    bArr[i2] = (byte) ((value >> (i * 8)) & 255);
                    this.data_len = i2 + 1;
                }
            }
            return 0;
        }

        public int putByte(int value) {
            if (this.data_len > this.event_max_data_len - 1) {
                return -1;
            }
            synchronized (this) {
                byte[] bArr = this.data;
                int i = this.data_len;
                bArr[i] = (byte) (value & 255);
                this.data_len = i + 1;
            }
            return 0;
        }

        public int putString(String str, int len) {
            if (this.data_len > this.event_max_data_len - len) {
                return -1;
            }
            synchronized (this) {
                byte[] s = str.getBytes();
                if (len < str.length()) {
                    System.arraycopy(s, 0, this.data, this.data_len, len);
                    this.data_len += len;
                } else {
                    int remain = len - str.length();
                    System.arraycopy(s, 0, this.data, this.data_len, str.length());
                    this.data_len += str.length();
                    for (int i = 0; i < remain; i++) {
                        byte[] bArr = this.data;
                        int i2 = this.data_len;
                        bArr[i2] = 0;
                        this.data_len = i2 + 1;
                    }
                }
            }
            return 0;
        }

        public int putBytes(byte[] value) {
            int len = value.length;
            if (len > this.event_max_data_len) {
                return -1;
            }
            synchronized (this) {
                System.arraycopy(value, 0, this.data, this.data_len, len);
                this.data_len += len;
            }
            return 0;
        }

        public int putDouble(double value) {
            if (this.data_len > this.event_max_data_len - 8) {
                return -1;
            }
            ByteBuffer buf = ByteBuffer.allocate(8);
            buf.putDouble(value);
            synchronized (this) {
                byte[] bArr = this.data;
                int i = this.data_len;
                this.data_len = i + 1;
                bArr[i] = buf.get(7);
                byte[] bArr2 = this.data;
                int i2 = this.data_len;
                this.data_len = i2 + 1;
                bArr2[i2] = buf.get(6);
                byte[] bArr3 = this.data;
                int i3 = this.data_len;
                this.data_len = i3 + 1;
                bArr3[i3] = buf.get(5);
                byte[] bArr4 = this.data;
                int i4 = this.data_len;
                this.data_len = i4 + 1;
                bArr4[i4] = buf.get(4);
                byte[] bArr5 = this.data;
                int i5 = this.data_len;
                this.data_len = i5 + 1;
                bArr5[i5] = buf.get(3);
                byte[] bArr6 = this.data;
                int i6 = this.data_len;
                this.data_len = i6 + 1;
                bArr6[i6] = buf.get(2);
                byte[] bArr7 = this.data;
                int i7 = this.data_len;
                this.data_len = i7 + 1;
                bArr7[i7] = buf.get(1);
                byte[] bArr8 = this.data;
                int i8 = this.data_len;
                this.data_len = i8 + 1;
                bArr8[i8] = buf.get(0);
            }
            return 0;
        }

        public int putLong(long value) {
            if (this.data_len > this.event_max_data_len - 8) {
                return -1;
            }
            ByteBuffer buf = ByteBuffer.allocate(8);
            buf.putLong(value);
            synchronized (this) {
                byte[] bArr = this.data;
                int i = this.data_len;
                this.data_len = i + 1;
                bArr[i] = buf.get(7);
                byte[] bArr2 = this.data;
                int i2 = this.data_len;
                this.data_len = i2 + 1;
                bArr2[i2] = buf.get(6);
                byte[] bArr3 = this.data;
                int i3 = this.data_len;
                this.data_len = i3 + 1;
                bArr3[i3] = buf.get(5);
                byte[] bArr4 = this.data;
                int i4 = this.data_len;
                this.data_len = i4 + 1;
                bArr4[i4] = buf.get(4);
                byte[] bArr5 = this.data;
                int i5 = this.data_len;
                this.data_len = i5 + 1;
                bArr5[i5] = buf.get(3);
                byte[] bArr6 = this.data;
                int i6 = this.data_len;
                this.data_len = i6 + 1;
                bArr6[i6] = buf.get(2);
                byte[] bArr7 = this.data;
                int i7 = this.data_len;
                this.data_len = i7 + 1;
                bArr7[i7] = buf.get(1);
                byte[] bArr8 = this.data;
                int i8 = this.data_len;
                this.data_len = i8 + 1;
                bArr8[i8] = buf.get(0);
            }
            return 0;
        }

        public byte[] getData() {
            return this.data;
        }

        public int getDataLen() {
            return this.data_len;
        }

        public int getRequestID() {
            return this.request_id;
        }

        public int getPhoneId() {
            return this.mPhoneId;
        }

        public int getInt() {
            int ret;
            synchronized (this) {
                byte[] bArr = this.data;
                int i = this.read_offset;
                ret = ((bArr[i + 3] & 255) << 24) | ((bArr[i + 2] & 255) << 16) | ((bArr[i + 1] & 255) << 8) | (bArr[i] & 255);
                this.read_offset = i + 4;
            }
            return ret;
        }

        public int getShort() {
            int ret;
            synchronized (this) {
                byte[] bArr = this.data;
                int i = this.read_offset;
                ret = ((bArr[i + 1] & 255) << 8) | (bArr[i] & 255);
                this.read_offset = i + 2;
            }
            return ret;
        }

        public int getByte() {
            int ret;
            synchronized (this) {
                byte[] bArr = this.data;
                int i = this.read_offset;
                ret = bArr[i] & 255;
                this.read_offset = i + 1;
            }
            return ret;
        }

        public byte[] getBytes(int length) {
            if (length > this.data_len - this.read_offset) {
                return null;
            }
            byte[] ret = new byte[length];
            synchronized (this) {
                for (int i = 0; i < length; i++) {
                    byte[] bArr = this.data;
                    int i2 = this.read_offset;
                    ret[i] = bArr[i2];
                    this.read_offset = i2 + 1;
                }
            }
            return ret;
        }

        public String getString(int len) {
            byte[] buf = new byte[len];
            synchronized (this) {
                System.arraycopy(this.data, this.read_offset, buf, 0, len);
                this.read_offset += len;
            }
            return new String(buf).trim();
        }

        public double getDouble() {
            byte[] bArr = this.data;
            int i = this.read_offset;
            byte[] buf = {bArr[i + 7], bArr[i + 6], bArr[i + 5], bArr[i + 4], bArr[i + 3], bArr[i + 2], bArr[i + 1], bArr[i]};
            this.read_offset = i + 8;
            return ByteBuffer.wrap(buf).getDouble();
        }
    }

    final class ImsaProxyDeathRecipient implements IHwBinder.DeathRecipient {
        ImsaProxyDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            Log.d("@M_[ImsAdapter]", "IMSA HIDL serviceDied");
            ImsAdapter.mInstance.sendMessageDelayed(ImsAdapter.mInstance.obtainMessage(3, Long.valueOf(ImsAdapter.this.mImsaProxyCookie.incrementAndGet())), 1000);
        }
    }

    public class ImsaIndication extends IImsaIndication.Stub {
        public ImsaIndication() {
        }

        private byte[] arrayListTobyte(ArrayList<Byte> data, int length) {
            byte[] byteList = new byte[length];
            for (int i = 0; i < length; i++) {
                byteList[i] = data.get(i).byteValue();
            }
            Log.d("@M_[ImsAdapter]", "IMSA HIDL : arrayListTobyte, byteList = " + byteList);
            return byteList;
        }

        public void readEvent(ArrayList<Byte> data, int request_id, int length) {
            int phoneId;
            Log.d("@M_[ImsAdapter]", "readEvent : data = " + data + ", request_id = " + request_id + ", length = " + length);
            byte[] buf = arrayListTobyte(data, length);
            int requestId = request_id;
            if (SystemProperties.getInt(ImsAdapter.MULTI_IMS_SUPPORT, 1) > 1) {
                phoneId = (-16777216 & requestId) >> 24;
                Log.d("@M_[ImsAdapter]", "DUAL IMS Phone ID:" + phoneId);
            } else {
                phoneId = Util.getDefaultVoltePhoneId();
            }
            VaEvent event = new VaEvent(phoneId, 16777215 & requestId, length);
            event.putBytes(buf);
            Message msg = new Message();
            msg.obj = event;
            ImsAdapter.mImsEventDispatcher.sendMessage(msg);
        }
    }

    public class VaSocketIO extends Thread {
        private byte[] buf = new byte[8];
        private DataInputStream mDin = null;
        private int mId = -1;
        private volatile IImsa mImsaProxy = null;
        private OutputStream mOut = null;
        private int mPhoneId = -1;
        private LocalSocket mSocket = null;
        private int mTyp = -1;

        public VaSocketIO(IImsa imsa_proxy) {
            Log.d("@M_[ImsAdapter]", "VaSocketIO(): Enter");
            this.mImsaProxy = imsa_proxy;
        }

        public void updateImsaProxy(IImsa imsa_proxy) {
            Log.d("@M_[ImsAdapter]", "VaSocketIO(): updateImsaProxy");
            this.mImsaProxy = imsa_proxy;
        }

        public void clearImsaProxy() {
            Log.d("@M_[ImsAdapter]", "VaSocketIO(): clearImsaProxy");
            this.mImsaProxy = null;
        }

        public void run() {
            Log.d("@M_[ImsAdapter]", "VaSocketIO(): Run");
        }

        private ArrayList<Byte> byteToArrayList(int length, byte[] value) {
            ArrayList<Byte> myarraylist = new ArrayList<>();
            Log.d("@M_[ImsAdapter]", " byteToArrayList, value.length = " + value.length + ", value = " + value + ", length = " + length);
            for (int i = 0; i < length; i++) {
                myarraylist.add(Byte.valueOf(value[i]));
            }
            return myarraylist;
        }

        /* Debug info: failed to restart local var, previous not found, register: 7 */
        public int writeEvent(VaEvent event) {
            Log.d("@M_[ImsAdapter]", "writeEvent Enter, phoneId = " + event.getPhoneId());
            int ret = -1;
            try {
                synchronized (this) {
                    if (this.mImsaProxy == null) {
                        Log.d("@M_[ImsAdapter]", "mImsaProxy is null, HIDL is not setup");
                    } else if (event.getPhoneId() == -1) {
                        Log.d("@M_[ImsAdapter]", "writeEvent event phoneId mismatch, event skipped. (event requestId=" + event.getRequestID() + ", phoneId=" + event.getPhoneId() + ", socket phoneId=" + this.mPhoneId + ")");
                    } else {
                        int requestId = event.getRequestID();
                        dumpEvent(event);
                        if (SystemProperties.getInt(ImsAdapter.MULTI_IMS_SUPPORT, 1) > 1) {
                            requestId |= event.getPhoneId() << 24;
                            Log.d("@M_[ImsAdapter]", "DUAL IMS request ID:" + requestId);
                        }
                        this.mImsaProxy.writeEvent(requestId, event.getDataLen(), byteToArrayList(event.getDataLen(), event.getData()));
                        ret = 0;
                    }
                }
                return ret;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

        private void dumpEvent(VaEvent event) {
            Log.d("@M_[ImsAdapter]", "dumpEvent: phone_id:" + event.getPhoneId() + ",request_id:" + ImsAdapter.requestIdToString(event.getRequestID()) + ",data_len:" + event.getDataLen() + ",event:" + event.getData());
        }
    }

    public ImsAdapter(Context context) {
        this.mContext = context;
        if (mInstance == null) {
            mInstance = this;
        }
        Log.d("@M_[ImsAdapter]", "ImsAdapter(): ImsAdapter Enter");
        this.mIO = new VaSocketIO(getImsaProxy());
        mImsEventDispatcher = new ImsEventDispatcher(this.mContext, this.mIO);
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        mNumOfPhones = phoneCount;
        misImsAdapterEnabled = new boolean[phoneCount];
        for (int i = 0; i < mNumOfPhones; i++) {
            misImsAdapterEnabled[i] = false;
        }
        this.mIO.start();
    }

    public IImsa getImsaProxy() {
        if (this.mImsaProxy != null) {
            return this.mImsaProxy;
        }
        try {
            this.mImsaProxy = IImsa.getService("imsa");
            Log.d("@M_[ImsAdapter]", "IImsa.getService");
            if (this.mImsaProxy != null) {
                this.mImsaProxy.linkToDeath(this.mImsaProxyDeathRecipient, this.mImsaProxyCookie.incrementAndGet());
                Log.d("@M_[ImsAdapter]", "setResponseFunctions");
                this.mImsaProxy.setResponseFunctions(this.mImsaIndication);
            } else {
                Log.d("@M_[ImsAdapter]", "getImsaProxy: mImsaProxy == null");
            }
        } catch (RemoteException | RuntimeException e) {
            this.mImsaProxy = null;
            Log.d("@M_[ImsAdapter]", "ImsaProxy getService/setResponseFunctions: " + e);
        }
        if (this.mImsaProxy == null) {
            sendMessageDelayed(obtainMessage(3, Long.valueOf(this.mImsaProxyCookie.incrementAndGet())), 1000);
        }
        return this.mImsaProxy;
    }

    private void resetProxyAndRequestList() {
        this.mImsaProxy = null;
        this.mIO.clearImsaProxy();
        this.mImsaProxyCookie.incrementAndGet();
    }

    public static ImsAdapter getInstance() {
        return mInstance;
    }

    public void enableImsAdapter(int phoneId) {
        Log.d("@M_[ImsAdapter]", "enableImsAdapter: misImsAdapterEnabled[phoneId]=" + misImsAdapterEnabled[phoneId] + ", phoneId = " + phoneId);
        synchronized (this.ImsEnabledThreadLock) {
            if (!misImsAdapterEnabled[phoneId]) {
                if (getImsaProxy() != null) {
                    Log.d("@M_[ImsAdapter]", "enalbeImsAdapter(): mImsaProxy success");
                    VaEvent event = new VaEvent(phoneId, VaConstants.MSG_ID_IMS_ENABLE_IND);
                    Message msg = new Message();
                    msg.obj = event;
                    mImsEventDispatcher.sendMessage(msg);
                    misImsAdapterEnabled[phoneId] = true;
                    enableImsStack(phoneId);
                } else {
                    Log.d("@M_[ImsAdapter]", "enableImsAdapter(): mImsaProxy create fail");
                }
            }
        }
    }

    public boolean getImsAdapterEnable() {
        int i = 0;
        while (true) {
            boolean[] zArr = misImsAdapterEnabled;
            if (i >= zArr.length) {
                return false;
            }
            if (zArr[i]) {
                return true;
            }
            i++;
        }
    }

    public void disableImsAdapter(int phoneId, boolean isNormalDisable) {
        Log.d("@M_[ImsAdapter]", "disableImsAdapter(): misImsAdapterEnabled[phoneId]=" + misImsAdapterEnabled[phoneId] + ", isNormalDisable = " + isNormalDisable + ", phoneId = " + phoneId);
        synchronized (this.ImsEnabledThreadLock) {
            boolean[] zArr = misImsAdapterEnabled;
            if (zArr[phoneId]) {
                zArr[phoneId] = false;
                disableImsStack(phoneId);
                VaEvent event = new VaEvent(phoneId, VaConstants.MSG_ID_IMS_DISABLE_IND);
                Message msg = new Message();
                msg.obj = event;
                mImsEventDispatcher.sendMessage(msg);
            }
        }
    }

    public void disableImsAdapterForAllPhone(boolean isNormalDisable) {
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            disableImsAdapter(Util.getDefaultVoltePhoneId(), isNormalDisable);
            return;
        }
        for (int i = 0; i < mNumOfPhones; i++) {
            disableImsAdapter(i, isNormalDisable);
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 2:
                Log.d("@M_[ImsAdapter]", "RETRY_IMS_ENABLE, phoneId:" + ((Integer) msg.obj).intValue());
                enableImsAdapter(((Integer) msg.obj).intValue());
                return;
            case 3:
                Log.d("@M_[ImsAdapter]", "MSG_IMSA_EVENT_PROXY_DEAD");
                if (((Long) msg.obj).longValue() == this.mImsaProxyCookie.get()) {
                    resetProxyAndRequestList();
                    getImsaProxy();
                    if (this.mImsaProxy != null) {
                        this.mIO.updateImsaProxy(this.mImsaProxy);
                        return;
                    }
                    return;
                }
                return;
            default:
                Log.d("@M_[ImsAdapter]", "handleMessage receive unsupported message: " + msg.what);
                return;
        }
    }

    public void sendTestEvent(VaEvent event) {
        mImsEventDispatcher.dispatchCallback(event);
    }

    public void ImsServiceUp() {
        mImsServiceUp = true;
        Log.d("@M_[ImsAdapter]", "ImsServiceUp, start to ACTION_IMS_SERVICE_UP intent");
    }

    public boolean getImsServiceUp() {
        return mImsServiceUp;
    }

    public static class Util {
        public static int getDefaultVoltePhoneId() {
            int phoneId = SystemProperties.getInt(ImsConstants.PROPERTY_CAPABILITY_SWITCH, 1) - 1;
            if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
                return -1;
            }
            return phoneId;
        }
    }

    private void enableImsStack(int phoneId) {
        this.mIO.writeEvent(new VaEvent(phoneId, VaConstants.MSG_ID_IMS_ENABLE_IND));
    }

    private void disableImsStack(int phoneId) {
        this.mIO.writeEvent(new VaEvent(phoneId, VaConstants.MSG_ID_IMS_DISABLE_IND));
    }

    public static String requestIdToString(int requestId) {
        switch (requestId) {
            case VaConstants.MSG_ID_WRAP_IMSPA_IMSM_INFORMATION_IND /*900001*/:
                return "MSG_ID_WRAP_IMSPA_IMSM_INFORMATION_IND";
            case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_INFORMATION_REQ /*900002*/:
                return "MSG_ID_WRAP_IMSM_IMSPA_INFORMATION_REQ";
            case VaConstants.MSG_ID_IMS_ENABLE_IND /*900003*/:
                return "MSG_ID_IMS_ENABLE_IND";
            case VaConstants.MSG_ID_IMS_DISABLE_IND /*900004*/:
                return "MSG_ID_IMS_DISABLE_IND";
            case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_REQ /*900008*/:
                return "MSG_ID_WRAP_IMSM_IMSPA_PDN_ACT_REQ";
            case VaConstants.MSG_ID_WRAP_IMSPA_IMSM_PDN_ACT_ACK_RESP /*900009*/:
                return "MSG_ID_WRAP_IMSPA_IMSM_PDN_ACT_ACK_RESP";
            case VaConstants.MSG_ID_WRAP_IMSPA_IMSM_PDN_ACT_REJ_RESP /*900010*/:
                return "MSG_ID_WRAP_IMSPA_IMSM_PDN_ACT_REJ_RESP";
            case VaConstants.MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_REQ /*900011*/:
                return "MSG_ID_WRAP_IMSM_IMSPA_PDN_DEACT_REQ";
            case VaConstants.MSG_ID_WRAP_IMSPA_IMSM_PDN_DEACT_ACK_RESP /*900012*/:
                return "MSG_ID_WRAP_IMSPA_IMSM_PDN_DEACT_ACK_RESP";
            case VaConstants.MSG_ID_WRAP_IMSPA_IMSM_PDN_DEACT_REJ_RESP /*900013*/:
                return "MSG_ID_WRAP_IMSPA_IMSM_PDN_DEACT_REJ_RESP";
            case VaConstants.MSG_ID_WRAP_IMSPA_IMSM_PDN_DEACT_IND /*900014*/:
                return "MSG_ID_WRAP_IMSPA_IMSM_PDN_DEACT_IND";
            case VaConstants.MSG_ID_NOTIFY_XUI_IND /*900401*/:
                return "MSG_ID_NOTIFY_XUI_IND";
            case VaConstants.MSG_ID_NOTIFY_SS_PROGRESS_INDICATION /*900402*/:
                return "MSG_ID_NOTIFY_SS_PROGRESS_INDICATION";
            case VaConstants.MSG_ID_REQUEST_PCSCF_DISCOVERY /*900403*/:
                return "MSG_ID_REQUEST_PCSCF_DISCOVERY";
            case VaConstants.MSG_ID_RESPONSE_PCSCF_DISCOVERY /*900404*/:
                return "MSG_ID_RESPONSE_PCSCF_DISCOVERY";
            case VaConstants.MSG_ID_REJECT_PCSCF_DISCOVERY /*900405*/:
                return "MSG_ID_REJECT_PCSCF_DISCOVERY";
            default:
                return Integer.toString(requestId);
        }
    }
}
