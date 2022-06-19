package com.mediatek.ims.rcsua.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAcsEventCallback extends IInterface {
    public static final String DESCRIPTOR = "com.mediatek.ims.rcsua.service.IAcsEventCallback";

    void onConfigChanged(boolean z, int i) throws RemoteException;

    void onConnectionChanged(boolean z) throws RemoteException;

    public static class Default implements IAcsEventCallback {
        public void onConfigChanged(boolean valid, int version) throws RemoteException {
        }

        public void onConnectionChanged(boolean status) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAcsEventCallback {
        static final int TRANSACTION_onConfigChanged = 1;
        static final int TRANSACTION_onConnectionChanged = 2;

        public Stub() {
            attachInterface(this, IAcsEventCallback.DESCRIPTOR);
        }

        public static IAcsEventCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(IAcsEventCallback.DESCRIPTOR);
            if (iin == null || !(iin instanceof IAcsEventCallback)) {
                return new Proxy(obj);
            }
            return (IAcsEventCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1598968902:
                    reply.writeString(IAcsEventCallback.DESCRIPTOR);
                    return true;
                default:
                    boolean _arg0 = false;
                    switch (code) {
                        case 1:
                            data.enforceInterface(IAcsEventCallback.DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg0 = true;
                            }
                            onConfigChanged(_arg0, data.readInt());
                            return true;
                        case 2:
                            data.enforceInterface(IAcsEventCallback.DESCRIPTOR);
                            if (data.readInt() != 0) {
                                _arg0 = true;
                            }
                            onConnectionChanged(_arg0);
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements IAcsEventCallback {
            public static IAcsEventCallback sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return IAcsEventCallback.DESCRIPTOR;
            }

            public void onConfigChanged(boolean valid, int version) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsEventCallback.DESCRIPTOR);
                    _data.writeInt(valid ? 1 : 0);
                    _data.writeInt(version);
                    if (this.mRemote.transact(1, _data, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConfigChanged(valid, version);
                    }
                } finally {
                    _data.recycle();
                }
            }

            public void onConnectionChanged(boolean status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IAcsEventCallback.DESCRIPTOR);
                    _data.writeInt(status ? 1 : 0);
                    if (this.mRemote.transact(2, _data, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConnectionChanged(status);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAcsEventCallback impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static IAcsEventCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
