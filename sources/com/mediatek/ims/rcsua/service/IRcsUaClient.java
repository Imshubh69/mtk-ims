package com.mediatek.ims.rcsua.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.ims.rcsua.RegistrationInfo;
import com.mediatek.ims.rcsua.service.ISipChannel;
import com.mediatek.ims.rcsua.service.ISipEventCallback;

public interface IRcsUaClient extends IInterface {
    public static final String DESCRIPTOR = "com.mediatek.ims.rcsua.service.IRcsUaClient";

    RegistrationInfo getRegistrationInfo() throws RemoteException;

    ISipChannel openSipChannel(ISipEventCallback iSipEventCallback, int i, RcsUaException rcsUaException) throws RemoteException;

    void resumeImsDeregistration() throws RemoteException;

    public static class Default implements IRcsUaClient {
        public ISipChannel openSipChannel(ISipEventCallback callback, int mode, RcsUaException exeception) throws RemoteException {
            return null;
        }

        public void resumeImsDeregistration() throws RemoteException {
        }

        public RegistrationInfo getRegistrationInfo() throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRcsUaClient {
        static final int TRANSACTION_getRegistrationInfo = 3;
        static final int TRANSACTION_openSipChannel = 1;
        static final int TRANSACTION_resumeImsDeregistration = 2;

        public Stub() {
            attachInterface(this, IRcsUaClient.DESCRIPTOR);
        }

        public static IRcsUaClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(IRcsUaClient.DESCRIPTOR);
            if (iin == null || !(iin instanceof IRcsUaClient)) {
                return new Proxy(obj);
            }
            return (IRcsUaClient) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1598968902:
                    reply.writeString(IRcsUaClient.DESCRIPTOR);
                    return true;
                default:
                    switch (code) {
                        case 1:
                            data.enforceInterface(IRcsUaClient.DESCRIPTOR);
                            ISipEventCallback _arg0 = ISipEventCallback.Stub.asInterface(data.readStrongBinder());
                            int _arg1 = data.readInt();
                            RcsUaException _arg2 = new RcsUaException();
                            ISipChannel _result = openSipChannel(_arg0, _arg1, _arg2);
                            reply.writeNoException();
                            reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                            reply.writeInt(1);
                            _arg2.writeToParcel(reply, 1);
                            return true;
                        case 2:
                            data.enforceInterface(IRcsUaClient.DESCRIPTOR);
                            resumeImsDeregistration();
                            reply.writeNoException();
                            return true;
                        case 3:
                            data.enforceInterface(IRcsUaClient.DESCRIPTOR);
                            RegistrationInfo _result2 = getRegistrationInfo();
                            reply.writeNoException();
                            if (_result2 != null) {
                                reply.writeInt(1);
                                _result2.writeToParcel(reply, 1);
                            } else {
                                reply.writeInt(0);
                            }
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements IRcsUaClient {
            public static IRcsUaClient sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return IRcsUaClient.DESCRIPTOR;
            }

            public ISipChannel openSipChannel(ISipEventCallback callback, int mode, RcsUaException exeception) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaClient.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openSipChannel(callback, mode, exeception);
                    }
                    _reply.readException();
                    ISipChannel _result = ISipChannel.Stub.asInterface(_reply.readStrongBinder());
                    if (_reply.readInt() != 0) {
                        exeception.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resumeImsDeregistration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaClient.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resumeImsDeregistration();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RegistrationInfo getRegistrationInfo() throws RemoteException {
                RegistrationInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(IRcsUaClient.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRegistrationInfo();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RegistrationInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRcsUaClient impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static IRcsUaClient getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
