package com.mediatek.ims.uce;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.ims.ImsException;
import android.telephony.ims.RcsContactTerminatedReason;
import android.telephony.ims.aidl.ICapabilityExchangeEventListener;
import android.telephony.ims.aidl.IOptionsRequestCallback;
import android.telephony.ims.aidl.IPublishResponseCallback;
import android.telephony.ims.aidl.ISubscribeResponseCallback;
import android.telephony.ims.stub.CapabilityExchangeEventListener;
import android.telephony.ims.stub.RcsCapabilityExchangeImplBase;
import android.util.Log;
import com.android.ims.internal.uce.options.IOptionsService;
import com.android.ims.internal.uce.uceservice.IUceListener;
import com.android.ims.internal.uce.uceservice.IUceService;
import com.android.ims.internal.uce.uceservice.ImsUceManager;
import com.mediatek.ims.Manifest;
import com.mediatek.presence.service.IMtkCoreServiceWrapper;
import com.mediatek.presence.service.api.IMtkPresenceService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MtkRcsCapabilityExchange extends RcsCapabilityExchangeImplBase {
    public static final int DEFAULT_SLOT_ID = 0;
    public static int MTK_RCS_S_REG = 0;
    public static final int OPTIONS_SERVICE_HDL = 0;
    public static final int PRESENCE_SERVICE_HDL = 0;
    private static final String PROPERTY_MTK_RCS_S_REG = "persist.vendor.mtk_rcs_single_reg_support";
    private static final String TAG = "MtkRcsCapabilityExchange";
    public static final int UCE_SERVUCE_NOT_STARTED = -1;
    /* access modifiers changed from: private */
    public RcsCapabilityExchangeImplBase.PublishResponseCallback mCallBack = null;
    /* access modifiers changed from: private */
    public CapabilityExchangeEventListener mCapEventListener;
    private Context mContext;
    /* access modifiers changed from: private */
    public IMtkCoreServiceWrapper mCoreServiceWrapperBinder = null;
    private ImsUceManager mImsUceManager;
    /* access modifiers changed from: private */
    public boolean mIsServiceStarted = false;
    public IPublishResponseCallback mMtkResponseCallback = new IPublishResponseCallback.Stub() {
        public void onCommandError(int code) {
            try {
                Log.d(MtkRcsCapabilityExchange.TAG, "IPublishResponseCallback >> onCommandError " + code);
                MtkRcsCapabilityExchange.this.mCallBack.onCommandError(code);
            } catch (Exception e) {
                Log.e(MtkRcsCapabilityExchange.TAG, "Exception >> onCommandError " + e);
            }
        }

        public void onNetworkResponse(int code, String reason) {
            try {
                Log.e(MtkRcsCapabilityExchange.TAG, "onNetworkResponse :: " + code + ",reason :: " + reason);
                MtkRcsCapabilityExchange.this.mCallBack.onNetworkResponse(code, reason);
            } catch (Exception e) {
                Log.e(MtkRcsCapabilityExchange.TAG, "Exception >> onNetworkResponse " + e);
            }
        }

        public void onNetworkRespHeader(int code, String reasonPhrase, int reasonHeaderCause, String reasonHeaderText) {
            try {
                Log.e(MtkRcsCapabilityExchange.TAG, "onNetworkRespHeader " + code);
                MtkRcsCapabilityExchange.this.mCallBack.onNetworkResponse(code, reasonPhrase, reasonHeaderCause, reasonHeaderText);
            } catch (Exception e) {
                Log.e(MtkRcsCapabilityExchange.TAG, "Exception >> onNetworkRespHeader " + e);
            }
        }
    };
    /* access modifiers changed from: private */
    public IOptionsService mOptionsService;
    /* access modifiers changed from: private */
    public IMtkPresenceService mPresenceService;
    private int mSlotId = -1;
    /* access modifiers changed from: private */
    public RcsCapabilityExchangeImplBase.SubscribeResponseCallback mSubscribeCallBack = null;
    public ISubscribeResponseCallback mSubscribeResponseCallback = new ISubscribeResponseCallback.Stub() {
        public void onCommandError(int code) {
            try {
                Log.d(MtkRcsCapabilityExchange.TAG, "subscribeForCapabilities >> onCommandError callback >> Sip code : " + code);
                MtkRcsCapabilityExchange.this.mSubscribeCallBack.onCommandError(code);
            } catch (Exception e) {
                Log.d(MtkRcsCapabilityExchange.TAG, "subscribeForCapabilities >> ImsException onCommandError callback " + e);
            }
        }

        public void onNetworkResponse(int code, String reason) {
            try {
                Log.d(MtkRcsCapabilityExchange.TAG, "subscribeForCapabilities >> onNetworkResponse callback >> Sip code : " + code + ", reason : " + reason);
                MtkRcsCapabilityExchange.this.mSubscribeCallBack.onNetworkResponse(code, reason);
            } catch (Exception e) {
                Log.d(MtkRcsCapabilityExchange.TAG, "subscribeForCapabilities >> ImsException onNetworkResponse callback " + e);
            }
        }

        public void onNetworkRespHeader(int code, String reasonPhrase, int reasonHeaderCause, String reasonHeaderText) {
            try {
                Log.d(MtkRcsCapabilityExchange.TAG, "subscribeForCapabilities >> onNetworkRespHeader callback >> Sip code : " + code + ", reasonPhrase : " + reasonPhrase + ", reasonHeaderCause : " + reasonHeaderCause + ", reasonHeaderText : " + reasonHeaderText);
                MtkRcsCapabilityExchange.this.mSubscribeCallBack.onNetworkResponse(code, reasonPhrase, reasonHeaderCause, reasonHeaderText);
            } catch (Exception e) {
                Log.d(MtkRcsCapabilityExchange.TAG, "subscribeForCapabilities >> ImsException onNetworkRespHeader callback " + e);
            }
        }

        public void onNotifyCapabilitiesUpdate(List<String> pidfXmls) {
            try {
                Log.d(MtkRcsCapabilityExchange.TAG, "subscribeForCapabilities >> onNotifyCapabilitiesUpdate callback >> pidfXmls : " + pidfXmls);
                MtkRcsCapabilityExchange.this.mSubscribeCallBack.onNotifyCapabilitiesUpdate(pidfXmls);
            } catch (Exception e) {
                Log.d(MtkRcsCapabilityExchange.TAG, "subscribeForCapabilities >> ImsException onNotifyCapabilitiesUpdate callback " + e);
            }
        }

        public void onResourceTerminated(List<RcsContactTerminatedReason> terminatedList) {
            Log.d(MtkRcsCapabilityExchange.TAG, "subscribeForCapabilities >> onResourceTerminated callback >> terminatedList : " + terminatedList);
        }

        public void onTerminated(String reason, long retryAfterMillis) {
            try {
                Log.d(MtkRcsCapabilityExchange.TAG, "subscribeForCapabilities >> onTerminated callback >> reason : " + reason + ", retryAfterMillis : " + retryAfterMillis);
                MtkRcsCapabilityExchange.this.mSubscribeCallBack.onTerminated(reason, retryAfterMillis);
            } catch (Exception e) {
                Log.d(MtkRcsCapabilityExchange.TAG, "subscribeForCapabilities >> ImsException onTerminated callback " + e);
            }
        }
    };
    private IUceListener mUceListener = null;
    private IUceService mUceService;
    public ICapabilityExchangeEventListener mtkCapabilityEventListener = new ICapabilityExchangeEventListener.Stub() {
        public void onRequestPublishCapabilities(int type) {
            try {
                Log.d(MtkRcsCapabilityExchange.TAG, "onRequestPublishCapabilities trigger :: " + type);
                MtkRcsCapabilityExchange.this.mCapEventListener.onRequestPublishCapabilities(type);
            } catch (ImsException e) {
                Log.e(MtkRcsCapabilityExchange.TAG, "ImsException :: " + e);
            }
        }

        public void onUnpublish() {
            try {
                MtkRcsCapabilityExchange.this.mCapEventListener.onUnpublish();
            } catch (ImsException e) {
                Log.e(MtkRcsCapabilityExchange.TAG, "ImsException :: " + e);
            }
        }

        public void onRemoteCapabilityRequest(Uri contactUri, List<String> list, IOptionsRequestCallback cb) {
        }
    };
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            IMtkCoreServiceWrapper unused = MtkRcsCapabilityExchange.this.mCoreServiceWrapperBinder = IMtkCoreServiceWrapper.Stub.asInterface(service);
            boolean unused2 = MtkRcsCapabilityExchange.this.mIsServiceStarted = true;
            try {
                MtkRcsCapabilityExchange.this.getPresenceService().setRcsCapabilityExchangeAvailable(true, MtkRcsCapabilityExchange.this.mtkCapabilityEventListener);
            } catch (RemoteException e) {
                Log.e(MtkRcsCapabilityExchange.TAG, "Remote Exception found :: " + e);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(MtkRcsCapabilityExchange.TAG, "onServiceDisconnected entry " + className);
            try {
                MtkRcsCapabilityExchange.this.getPresenceService().setRcsCapabilityExchangeAvailable(false, MtkRcsCapabilityExchange.this.mtkCapabilityEventListener);
            } catch (RemoteException e) {
                Log.e(MtkRcsCapabilityExchange.TAG, "Remote Exception found :: " + e);
            }
            IMtkCoreServiceWrapper unused = MtkRcsCapabilityExchange.this.mCoreServiceWrapperBinder = null;
            IMtkPresenceService unused2 = MtkRcsCapabilityExchange.this.mPresenceService = null;
            IOptionsService unused3 = MtkRcsCapabilityExchange.this.mOptionsService = null;
            boolean unused4 = MtkRcsCapabilityExchange.this.mIsServiceStarted = false;
        }
    };

    public MtkRcsCapabilityExchange(int slotId, Context context, CapabilityExchangeEventListener listener) {
        this.mSlotId = slotId;
        this.mContext = context;
        saveCapbilityCallback(listener);
        MTK_RCS_S_REG = SystemProperties.getInt(PROPERTY_MTK_RCS_S_REG, 0);
        PackageManager pm = this.mContext.getPackageManager();
        Log.d(TAG, "mCapEventListener :: " + this.mCapEventListener);
        Log.d(TAG, "MTK_RCS_S_REG  : " + MTK_RCS_S_REG + "=" + SystemProperties.getInt(PROPERTY_MTK_RCS_S_REG, 0));
        Log.d(TAG, "MtkRcsCapabilityExchange slotId : " + slotId + " ,context: " + context + " ,pm :" + pm);
        if (MTK_RCS_S_REG == 1) {
            connectToRcsCoreService();
        }
        log("MtkRcsCapabilityExchange is loaded");
    }

    private void saveCapbilityCallback(CapabilityExchangeEventListener listener) {
        this.mCapEventListener = listener;
        Log.d(TAG, "CapabilityExchangeEvent callback successfully saved");
    }

    public void publishCapabilities(String pidfXml, RcsCapabilityExchangeImplBase.PublishResponseCallback cb) {
        IMtkCoreServiceWrapper iMtkCoreServiceWrapper;
        setCallBack(cb);
        log("publishCapabilities > pidfXml : " + pidfXml);
        log("publishCapabilities > P ublishResponseCallback : " + cb);
        Log.d(TAG, "publishCapabilities mCoreServiceWrapperBinder: " + this.mCoreServiceWrapperBinder);
        Log.d(TAG, "publishCapabilities mIsServiceStarted: " + this.mIsServiceStarted);
        try {
            if (this.mIsServiceStarted && (iMtkCoreServiceWrapper = this.mCoreServiceWrapperBinder) != null) {
                IMtkPresenceService asInterface = IMtkPresenceService.Stub.asInterface(iMtkCoreServiceWrapper.getMtkPresenceServiceBinder(this.mSlotId));
                this.mPresenceService = asInterface;
                if (asInterface != null) {
                    asInterface.publishMyCap(pidfXml, 1001, this.mMtkResponseCallback);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getAospPresenceServiceBinder Fail" + e);
        }
    }

    public void setCallBack(RcsCapabilityExchangeImplBase.PublishResponseCallback cb) {
        this.mCallBack = cb;
        Log.d(TAG, "PublishResponseCallback Saved");
    }

    public void subscribeForCapabilities(Collection<Uri> uris, RcsCapabilityExchangeImplBase.SubscribeResponseCallback cb) {
        setSubscribeCallBack(cb);
        List<Uri> contactList = new ArrayList<>();
        Log.d(TAG, "subscribeForCapabilities mCoreServiceWrapperBinder: " + this.mCoreServiceWrapperBinder);
        Log.d(TAG, "subscribeForCapabilities mIsServiceStarted: " + this.mIsServiceStarted);
        try {
            if (this.mIsServiceStarted && this.mCoreServiceWrapperBinder != null && getPresenceService() != null) {
                contactList.addAll(uris);
                Log.d(TAG, "subscribeForCapabilities > getContactListCapAosp trigger >> contactList : " + contactList);
                getPresenceService().getContactListCapAosp(contactList, 1002, this.mSubscribeResponseCallback);
            }
        } catch (Exception e) {
            Log.e(TAG, "subscribeForCapabilities >> getMtkPresenceServiceBinder Fail" + e);
        }
    }

    public void setSubscribeCallBack(RcsCapabilityExchangeImplBase.SubscribeResponseCallback cb) {
        this.mSubscribeCallBack = cb;
    }

    /* access modifiers changed from: private */
    public IMtkPresenceService getPresenceService() {
        try {
            IMtkPresenceService asInterface = IMtkPresenceService.Stub.asInterface(this.mCoreServiceWrapperBinder.getMtkPresenceServiceBinder(this.mSlotId));
            this.mPresenceService = asInterface;
            return asInterface;
        } catch (Exception e) {
            Log.d(TAG, "getPresenceService RemoteException e: " + e);
            return null;
        }
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }

    private static void loge(String msg) {
        Log.e(TAG, msg);
    }

    private void connectToRcsCoreService() {
        Log.d(TAG, "connectToRcsCoreService called");
        ComponentName cmp = new ComponentName("com.mediatek.presence", "com.mediatek.presence.service.RcsCoreService");
        if (this.mContext.checkCallingOrSelfPermission(Manifest.permission.PRESENCE) == 0) {
            Intent intent = new Intent();
            intent.setComponent(cmp);
            Log.d(TAG, "connectToRcsCoreService intent: " + intent + " ,serviceConnection: " + this.serviceConnection + " ,Context.BIND_AUTO_CREATE: " + 1);
            this.mContext.bindService(intent, this.serviceConnection, 1);
            return;
        }
        Log.d(TAG, "connectToRcsCoreService permission not granted");
    }
}
