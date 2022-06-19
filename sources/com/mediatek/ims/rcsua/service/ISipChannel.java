package com.mediatek.ims.rcsua.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.aidl.ISipDelegateMessageCallback;
import com.mediatek.ims.rcsua.Configuration;

public interface ISipChannel extends IInterface {
    public static final String DESCRIPTOR = "com.mediatek.ims.rcsua.service.ISipChannel";

    void close(RcsUaException rcsUaException) throws RemoteException;

    boolean isAvailable() throws RemoteException;

    Configuration readConfiguration() throws RemoteException;

    int sendMessage(byte[] bArr, RcsUaException rcsUaException) throws RemoteException;

    int sendMessageAosp(byte[] bArr, RcsUaException rcsUaException, ISipDelegateMessageCallback iSipDelegateMessageCallback) throws RemoteException;

    public static class Default implements ISipChannel {
        public int sendMessage(byte[] message, RcsUaException e) throws RemoteException {
            return 0;
        }

        public int sendMessageAosp(byte[] message, RcsUaException e, ISipDelegateMessageCallback mMessageCallback) throws RemoteException {
            return 0;
        }

        public void close(RcsUaException e) throws RemoteException {
        }

        public boolean isAvailable() throws RemoteException {
            return false;
        }

        public Configuration readConfiguration() throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISipChannel {
        static final int TRANSACTION_close = 3;
        static final int TRANSACTION_isAvailable = 4;
        static final int TRANSACTION_readConfiguration = 5;
        static final int TRANSACTION_sendMessage = 1;
        static final int TRANSACTION_sendMessageAosp = 2;

        public Stub() {
            attachInterface(this, ISipChannel.DESCRIPTOR);
        }

        public static ISipChannel asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(ISipChannel.DESCRIPTOR);
            if (iin == null || !(iin instanceof ISipChannel)) {
                return new Proxy(obj);
            }
            return (ISipChannel) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1598968902:
                    reply.writeString(ISipChannel.DESCRIPTOR);
                    return true;
                default:
                    switch (code) {
                        case 1:
                            data.enforceInterface(ISipChannel.DESCRIPTOR);
                            byte[] _arg0 = data.createByteArray();
                            RcsUaException _arg1 = new RcsUaException();
                            int _result = sendMessage(_arg0, _arg1);
                            reply.writeNoException();
                            reply.writeInt(_result);
                            reply.writeInt(1);
                            _arg1.writeToParcel(reply, 1);
                            return true;
                        case 2:
                            data.enforceInterface(ISipChannel.DESCRIPTOR);
                            byte[] _arg02 = data.createByteArray();
                            RcsUaException _arg12 = new RcsUaException();
                            int _result2 = sendMessageAosp(_arg02, _arg12, ISipDelegateMessageCallback.Stub.asInterface(data.readStrongBinder()));
                            reply.writeNoException();
                            reply.writeInt(_result2);
                            reply.writeInt(1);
                            _arg12.writeToParcel(reply, 1);
                            return true;
                        case 3:
                            data.enforceInterface(ISipChannel.DESCRIPTOR);
                            RcsUaException _arg03 = new RcsUaException();
                            close(_arg03);
                            reply.writeNoException();
                            reply.writeInt(1);
                            _arg03.writeToParcel(reply, 1);
                            return true;
                        case 4:
                            data.enforceInterface(ISipChannel.DESCRIPTOR);
                            boolean _result3 = isAvailable();
                            reply.writeNoException();
                            reply.writeInt(_result3);
                            return true;
                        case 5:
                            data.enforceInterface(ISipChannel.DESCRIPTOR);
                            Configuration _result4 = readConfiguration();
                            reply.writeNoException();
                            if (_result4 != null) {
                                reply.writeInt(1);
                                _result4.writeToParcel(reply, 1);
                            } else {
                                reply.writeInt(0);
                            }
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements ISipChannel {
            public static ISipChannel sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return ISipChannel.DESCRIPTOR;
            }

            public int sendMessage(byte[] message, RcsUaException e) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(ISipChannel.DESCRIPTOR);
                    _data.writeByteArray(message);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendMessage(message, e);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        e.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendMessageAosp(byte[] message, RcsUaException e, ISipDelegateMessageCallback mMessageCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(ISipChannel.DESCRIPTOR);
                    _data.writeByteArray(message);
                    _data.writeStrongBinder(mMessageCallback != null ? mMessageCallback.asBinder() : null);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendMessageAosp(message, e, mMessageCallback);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        e.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void close(RcsUaException e) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(ISipChannel.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            e.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().close(e);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(ISipChannel.DESCRIPTOR);
                    boolean z = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAvailable();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Configuration readConfiguration() throws RemoteException {
                Configuration _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(ISipChannel.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readConfiguration();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Configuration.CREATOR.createFromParcel(_reply);
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

        public static boolean setDefaultImpl(ISipChannel impl) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException("setDefaultImpl() called twice");
            } else if (impl == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = impl;
                return true;
            }
        }

        public static ISipChannel getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
