package com.mediatek.rcs.provisioning;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAcsCallback extends IInterface {
    public static final String DESCRIPTOR = "com.mediatek.rcs.provisioning.IAcsCallback";

    void onAcsConfigChange(int i, int i2) throws RemoteException;

    void onAcsConnectionStatusChange(int i) throws RemoteException;

    public static class Default implements IAcsCallback {
        public void onAcsConfigChange(int configStatus, int configVersion) throws RemoteException {
        }

        public void onAcsConnectionStatusChange(int connectionStatus) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAcsCallback {
        static final int TRANSACTION_onAcsConfigChange = 1;
        static final int TRANSACTION_onAcsConnectionStatusChange = 2;

        public Stub() {
            attachInterface(this, IAcsCallback.DESCRIPTOR);
        }

        public static IAcsCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(IAcsCallback.DESCRIPTOR);
            if (iin == null || !(iin instanceof IAcsCallback)) {
                return new Proxy(obj);
            }
            return (IAcsCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1598968902:
                    reply.writeString(IAcsCallback.DESCRIPTOR);
                    return true;
                default:
                    switch (code) {
                        case 1:
                            data.enforceInterface(IAcsCallback.DESCRIPTOR);
                            onAcsConfigChange(data.readInt(), data.readInt());
                            reply.writeNoException();
                            return true;
                        case 2:
                            data.enforceInterface(IAcsCallback.DESCRIPTOR);
                            onAcsConnectionStatusChange(data.readInt());
                            reply.writeNoException();
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements IAcsCallback {
            public static IAcsCallback sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return IAcsCallback.DESCRIPTOR;
            }

            public void onAcsConfigChange(int configStatus, int configVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsCallback.DESCRIPTOR);
                    _data.writeInt(configStatus);
                    _data.writeInt(configVersion);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onAcsConfigChange(configStatus, configVersion);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAcsConnectionStatusChange(int connectionStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsCallback.DESCRIPTOR);
                    _data.writeInt(connectionStatus);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onAcsConnectionStatusChange(connectionStatus);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAcsCallback impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static IAcsCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
