package com.mediatek.presence.service.api;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.aidl.ICapabilityExchangeEventListener;
import android.telephony.ims.aidl.IPublishResponseCallback;
import android.telephony.ims.aidl.ISubscribeResponseCallback;
import java.util.List;

public interface IMtkPresenceService extends IInterface {
    public static final String DESCRIPTOR = "com.mediatek.presence.service.api.IMtkPresenceService";

    void getContactListCapAosp(List<Uri> list, int i, ISubscribeResponseCallback iSubscribeResponseCallback) throws RemoteException;

    void publishMyCap(String str, int i, IPublishResponseCallback iPublishResponseCallback) throws RemoteException;

    void setRcsCapabilityExchangeAvailable(boolean z, ICapabilityExchangeEventListener iCapabilityExchangeEventListener) throws RemoteException;

    public static class Default implements IMtkPresenceService {
        public void publishMyCap(String pidfXml, int userData, IPublishResponseCallback cb) throws RemoteException {
        }

        public void getContactListCapAosp(List<Uri> list, int userData, ISubscribeResponseCallback cb) throws RemoteException {
        }

        public void setRcsCapabilityExchangeAvailable(boolean value, ICapabilityExchangeEventListener listener) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkPresenceService {
        static final int TRANSACTION_getContactListCapAosp = 2;
        static final int TRANSACTION_publishMyCap = 1;
        static final int TRANSACTION_setRcsCapabilityExchangeAvailable = 3;

        public Stub() {
            attachInterface(this, IMtkPresenceService.DESCRIPTOR);
        }

        public static IMtkPresenceService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(IMtkPresenceService.DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkPresenceService)) {
                return new Proxy(obj);
            }
            return (IMtkPresenceService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1598968902:
                    reply.writeString(IMtkPresenceService.DESCRIPTOR);
                    return true;
                default:
                    switch (code) {
                        case 1:
                            data.enforceInterface(IMtkPresenceService.DESCRIPTOR);
                            publishMyCap(data.readString(), data.readInt(), IPublishResponseCallback.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            return true;
                        case 2:
                            data.enforceInterface(IMtkPresenceService.DESCRIPTOR);
                            getContactListCapAosp(data.createTypedArrayList(Uri.CREATOR), data.readInt(), ISubscribeResponseCallback.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            return true;
                        case 3:
                            data.enforceInterface(IMtkPresenceService.DESCRIPTOR);
                            setRcsCapabilityExchangeAvailable(data.readInt() != 0, ICapabilityExchangeEventListener.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements IMtkPresenceService {
            public static IMtkPresenceService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return IMtkPresenceService.DESCRIPTOR;
            }

            public void publishMyCap(String pidfXml, int userData, IPublishResponseCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkPresenceService.DESCRIPTOR);
                    _data.writeString(pidfXml);
                    _data.writeInt(userData);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().publishMyCap(pidfXml, userData, cb);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getContactListCapAosp(List<Uri> contactList, int userData, ISubscribeResponseCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkPresenceService.DESCRIPTOR);
                    _data.writeTypedList(contactList);
                    _data.writeInt(userData);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getContactListCapAosp(contactList, userData, cb);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRcsCapabilityExchangeAvailable(boolean value, ICapabilityExchangeEventListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IMtkPresenceService.DESCRIPTOR);
                    _data.writeInt(value ? 1 : 0);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRcsCapabilityExchangeAvailable(value, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkPresenceService impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static IMtkPresenceService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
