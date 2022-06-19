package com.mediatek.ims.rcsua.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISipEventCallback extends IInterface {
    public static final String DESCRIPTOR = "com.mediatek.ims.rcsua.service.ISipEventCallback";

    void messageReceived(int i, byte[] bArr) throws RemoteException;

    public static class Default implements ISipEventCallback {
        public void messageReceived(int transport, byte[] message) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISipEventCallback {
        static final int TRANSACTION_messageReceived = 1;

        public Stub() {
            attachInterface(this, ISipEventCallback.DESCRIPTOR);
        }

        public static ISipEventCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(ISipEventCallback.DESCRIPTOR);
            if (iin == null || !(iin instanceof ISipEventCallback)) {
                return new Proxy(obj);
            }
            return (ISipEventCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1598968902:
                    reply.writeString(ISipEventCallback.DESCRIPTOR);
                    return true;
                default:
                    switch (code) {
                        case 1:
                            data.enforceInterface(ISipEventCallback.DESCRIPTOR);
                            messageReceived(data.readInt(), data.createByteArray());
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements ISipEventCallback {
            public static ISipEventCallback sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return ISipEventCallback.DESCRIPTOR;
            }

            public void messageReceived(int transport, byte[] message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(ISipEventCallback.DESCRIPTOR);
                    _data.writeInt(transport);
                    _data.writeByteArray(message);
                    if (this.mRemote.transact(1, _data, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().messageReceived(transport, message);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISipEventCallback impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static ISipEventCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
